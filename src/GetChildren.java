import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

public class GetChildren extends ServerResource {

	@Get("application/json")
	public Representation represent() {
		DAGRApplication.fixResponseHeader(getResponse());
		StringBuilder response = new StringBuilder();
		response.append("{\"response\":[");
		List<DAGR> dagrs = getDAGRList();
		// response.append("Query returned " + dagrs.size() + " results:\n");
		for (DAGR dagr : dagrs) {
			response.append(new Gson().toJson(dagr) + ",");
		}
		if (dagrs.size() > 0) {
			response.replace(response.length() - 1, response.length(), "");
		}
		response.append("]}");
		return new StringRepresentation(response.toString(),
				MediaType.APPLICATION_JSON);
	}

	private List<DAGR> getDAGRList() {
		String GUID = getQueryValue("guid");
		List<String> childGUIDs = Utils.getChildren(GUID);
		List<DAGR> DAGRs = Utils.getDAGRs(childGUIDs);
		return DAGRs;
	}
}
