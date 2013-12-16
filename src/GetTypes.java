import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

public class GetTypes extends ServerResource {
	@Get("application/json")
	public Representation represent() {
		DAGRApplication.fixResponseHeader(getResponse());
		StringBuilder response = new StringBuilder();
		response.append("{\"response\":[");
		List<String> types = Utils.getTypes();
		// response.append("Query returned " + dagrs.size() + " results:\n");
		for (String type : types) {
			response.append(new Gson().toJson(type) + ",");
		}
		if (types.size() > 0) {
			response.replace(response.length() - 1, response.length(), "");
		}
		response.append("]}");
		return new StringRepresentation(response.toString(),
				MediaType.APPLICATION_JSON);
	}

}
