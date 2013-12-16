import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

public class OrphanSterile extends ServerResource {

	@Get("application/json")
	public Representation getRep() {
		DAGRApplication.fixResponseHeader(getResponse());
		StringBuilder response = new StringBuilder();
		response.append("{\"orphans\":[");
		List<DAGR> orphans = Utils.getOrphans();
		if (orphans.size() > 0) {
			for(DAGR dagr: orphans){
				response.append(new Gson().toJson(dagr) + ",");
			}
			response.replace(response.length() - 1, response.length(), "");
		}
		response.append("],");
		response.append("\"sterile\":[");
		List<DAGR> sterile = Utils.getSterile();
		if (sterile.size() > 0) {
			for(DAGR dagr: sterile){
				response.append(new Gson().toJson(dagr) + ",");
			}
			response.replace(response.length() - 1, response.length(), "");
		}
		response.append("]}");
		return new StringRepresentation(response.toString(),
				MediaType.APPLICATION_JSON);
	}

}
