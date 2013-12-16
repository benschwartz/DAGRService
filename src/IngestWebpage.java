import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

public class IngestWebpage extends ServerResource {
	Logger LOGGER = Logger.getLogger("DAGR");

	@Get("application/json")
	@Post("application/json")
	@Options("application/json")
	public Representation acceptItem(Representation entity) {
		DAGRApplication.fixResponseHeader(getResponse());
		StringBuilder response = new StringBuilder();
		response.append("{\"response\":[");
		String URL = getQueryValue("url");
		String author = getQueryValue("author");
		if (URL == null || author == null) {
			LOGGER.severe("missing parameter");
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "missing parameter");
			return new EmptyRepresentation();
		}
		List<String> processSite = processSite(URL, author);
		if (processSite == null) {
			LOGGER.severe("Could not ingest " + URL);
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Could not ingest "
					+ URL);
			return new EmptyRepresentation();
		}
		List<DAGR> dagrs = Utils.getDAGRs(processSite);
		if (dagrs.size() > 0) {
			for (DAGR dagr : dagrs) {
				response.append(new Gson().toJson(dagr) + ",");
			}
			response.replace(response.length() - 1, response.length(), "");
		}
		response.append("]}");
		return new StringRepresentation(response.toString(),
				MediaType.APPLICATION_JSON);
	}

	private List<String> processSite(String webURL, String author) {

		String siteGUID = processItem(webURL, Utils.getWebUUID(), author);
		if (siteGUID == null) {
			return null;
		}
		DAGR siteDAGR = Utils.getDAGRs(Arrays.asList(siteGUID)).get(0);
		List<String> guids = new ArrayList<String>();
		if (siteGUID != null) {
			guids.add(siteGUID);
			if (siteDAGR.getType().equals("html")
					|| siteDAGR.getType().equals("htm")
					|| siteDAGR.getType().equals("website")) {
				guids.addAll(processHTML(webURL, siteGUID, author));
			}
		}
		return guids;
	}

	private List<String> processHTML(String url, String parent, String author) {
		List<String> guids = new ArrayList<String>();
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			Elements links = doc.select("a[href]");
			Elements media = doc.select("[src]");
			Elements imports = doc.select("link[href]");
			for (Element src : media) {
				String guid = processItem(src.attr("abs:src"), parent, author);
				if (guid != null) {
					guids.add(guid);
				} else {
					LOGGER.severe("Could not ingest " + src.attr("abs:src"));
				}
			}
			for (Element link : imports) {
				String guid = processItem(link.attr("abs:href"), parent, author);
				if (guid != null) {
					guids.add(guid);
				} else {
					LOGGER.severe("Could not ingest " + link.attr("abs:href"));
				}
			}
			for (Element link : links) {
				String guid = processItem(link.attr("abs:href"), parent, author);
				if (guid != null) {
					guids.add(guid);
				} else {
					LOGGER.severe("Could not ingest " + link.attr("abs:href"));
				}
			}

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Could not parse DAGR" + parent, e);
			return Collections.emptyList();
		}

		return guids;
	}

	private String processItem(String webURL, String parent, String author) {
		String GUID = UUID.randomUUID().toString();
		URL url;
		try {
			url = new URL(webURL);
		} catch (MalformedURLException e1) {
			return null;
		}
		String name = "";
		String type = "";
		String location = url.getHost();
		if (url.getPath().isEmpty() || url.getPath().equals("/")) {
			name = url.getHost();
			type = "website";
		} else {
			String urlArray[] = url.getPath().split("/");
			if (urlArray.length > 0) {
				name = urlArray[urlArray.length - 1];
			}
			if (!url.getPath().contains(".")) {
				type = "website";
			} else {
				String type_split[] = name.split("\\.");
				if (type_split.length > 1) {
					type = type_split[type_split.length - 1];
				}
			}
			for (int i = 0; i < urlArray.length - 1; i++) {
				if (i == 0) {
					location = location + urlArray[i];
				} else {
					location = location + "/" + urlArray[i];
				}
			}
		}
		long modified = System.currentTimeMillis();
		long created = System.currentTimeMillis();
		long size = 0;
		LOGGER.info(name + " " + type + " " + location + " " + modified + " "
				+ created + " " + size);
		try {
			URLConnection connection = url.openConnection();
			connection.connect();
			modified = connection.getLastModified();
			if (modified == 0) {
				modified = System.currentTimeMillis();
			}
			created = connection.getLastModified();
			if (created == 0) {
				created = System.currentTimeMillis();
			}
			size = connection.getContentLengthLong();
			if (size < 0) {
				size = 0;
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error parsing URL metadata from "
					+ webURL, e);
			return null;
		}

		return Utils.insertDAGR(GUID, name, created, modified, location,
				parent, author, type, size);
	}
}
