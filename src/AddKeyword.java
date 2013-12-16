import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;


public class AddKeyword extends ServerResource {
	Logger LOGGER = Logger.getLogger("DAGR");	
	@Post("application/json")
	@Options("application/json")
	public Representation acceptItem(Representation entity) {
		DAGRApplication.fixResponseHeader(getResponse());
		
		String GUID = getQuery().getFirstValue("guid");
		String keyword = getQuery().getFirstValue("keyword");
		if(GUID == null || keyword == null){
			LOGGER.log(Level.SEVERE,"Missing parameter(s)");
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "missing parameter");
			return new EmptyRepresentation();
		}
		List<String> keywords = Utils.addKeyword(GUID, keyword);
		StringBuilder res = new StringBuilder();
		res.append("{\"response\":[");
		if (!keywords.isEmpty()) {
			for(String kw: keywords){
				res.append(new Gson().toJson(kw) + ",");
			}
			res.replace(res.length() - 1, res.length(), "");
		}
		res.append("]}");
		return new StringRepresentation(res.toString(),MediaType.APPLICATION_JSON);
	}
}
