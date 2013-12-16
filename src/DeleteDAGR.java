import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Options;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;


public class DeleteDAGR extends ServerResource {
	Logger LOGGER = Logger.getLogger("DAGR");	
	
	@Delete("application/json")
	@Options("application/json")
	public Representation acceptItem(Representation entity) {
		DAGRApplication.fixResponseHeader(getResponse());
		
		String GUID = getQuery().getFirstValue("guid");
		String cascadeString = getQuery().getFirstValue("cascade");
		if(GUID == null || cascadeString == null){
			LOGGER.log(Level.SEVERE,"Missing parameter(s)");
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "missing parameter");
			return new EmptyRepresentation();
		}
		boolean cascade = cascadeString.toLowerCase().equals("true");
		Set<DAGR> DAGRs = Utils.deleteDAGR(GUID, cascade);
		Utils.cleanUpAuthors();
		Utils.cleanUpTypes();
		StringBuilder res = new StringBuilder();
		res.append("{\"response\":[");
		if (!DAGRs.isEmpty()) {
			for(DAGR dagr: DAGRs){
				res.append(new Gson().toJson(dagr) + ",");
			}
			res.replace(res.length() - 1, res.length(), "");
		}
		res.append("]}");
		return new StringRepresentation(res.toString(),MediaType.APPLICATION_JSON);
	}
}
