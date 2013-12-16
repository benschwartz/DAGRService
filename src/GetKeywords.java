import java.util.List;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

public class GetKeywords extends ServerResource {
	Logger LOGGER = Logger.getLogger("DAGR");

	@Get("application/json")
	public Representation represent() {
		return represent(getQuery().getFirstValue("guid"));
	}

	public Representation represent(String GUID) {
		DAGRApplication.fixResponseHeader(getResponse());
		StringBuilder response = new StringBuilder();
		response.append("{\"response\":[");
		List<String> keywords;
		if (GUID == null) {
			keywords = Utils.getKeywords();
		} else {
			keywords = Utils.getKeywords(GUID);
		}
		for (String author : keywords) {
			response.append(new Gson().toJson(author) + ",");
		}
		if (keywords.size() > 0) {
			response.replace(response.length() - 1, response.length(), "");
		}
		response.append("]}");
		return new StringRepresentation(response.toString(),
				MediaType.APPLICATION_JSON);
	}
}
