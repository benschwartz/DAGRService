import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

public class GetDAGR extends ServerResource {

	@Get("application/json")
	public Representation represent() {
		DAGRApplication.fixResponseHeader(getResponse());
		StringBuilder response = new StringBuilder();
		response.append("{\"response\":[");
		List<DAGR> dagrs = getDAGRList();
		// response.append("Query returned " + dagrs.size() + " results:\n");
		
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

	private List<DAGR> getDAGRList() {
		String GUID = getQueryValue("guid");
		String location = getQueryValue("location");
		String type = getQueryValue("type");
		String author = getQueryValue("author");
		String name = getQueryValue("name");
		String size = getQueryValue("size");
		String c_start = getQueryValue("created_start");
		String c_stop = getQueryValue("created_stop");
		String m_start = getQueryValue("modified_start");
		String m_stop = getQueryValue("modified_stop");
		List<DAGR> DAGRs = Utils.getDAGRs(GUID, location, type, author, name,
				size, c_start, c_stop, m_start,m_stop);
		return DAGRs;
	}
}
