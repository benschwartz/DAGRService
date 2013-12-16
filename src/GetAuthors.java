import java.util.List;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

public class GetAuthors extends ServerResource {
	Logger LOGGER = Logger.getLogger("DAGR");

	@Get("application/json")
	public Representation represent() {
		DAGRApplication.fixResponseHeader(getResponse());
		StringBuilder response = new StringBuilder();
		response.append("{\"response\":[");
		List<String> authors = Utils.getAuthors();
		// response.append("Query returned " + dagrs.size() + " results:\n");
		for (String author : authors) {
			response.append(new Gson().toJson(author) + ",");
		}
		if (authors.size() > 0) {
			response.replace(response.length() - 1, response.length(), "");
		}
		response.append("]}");
		return new StringRepresentation(response.toString(),
				MediaType.APPLICATION_JSON);
	}

}
