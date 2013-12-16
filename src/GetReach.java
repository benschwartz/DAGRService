import java.util.List;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

public class GetReach extends ServerResource {
	Logger LOGGER = Logger.getLogger("DAGR");
	@Get("application/json")
	public Representation represent() {
		DAGRApplication.fixResponseHeader(getResponse());
		StringBuilder response = new StringBuilder();
		response.append("{\"response\":[");
		String GUID = getQueryValue("guid");
		if(GUID==null){
			LOGGER.severe("Missing parameter");
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Missing parameter");
			return new EmptyRepresentation();
		}
		List<DAGR> dagrs = Utils.getReach(GUID);
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

}
