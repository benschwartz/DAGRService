import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

public class GetModifiedTimerange extends ServerResource {
Logger LOGGER = Logger.getLogger("DAGR");
	@Get("application/json")
	public Representation represent() {
		DAGRApplication.fixResponseHeader(getResponse());
		StringBuilder response = new StringBuilder();
		response.append("{\"response\":[");
		List<DAGR> dagrs;
		try {
			dagrs = getDAGRList();
		} catch (NumberFormatException e) {
			LOGGER.log(Level.SEVERE,"Invalid parameter",e);
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid parameter:"+e.getMessage());
			return new EmptyRepresentation();
		}
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

	private List<DAGR> getDAGRList() throws NumberFormatException {
		String startTime = getQueryValue("start");
		String stopTime = getQueryValue("stop");
		long start = 0;
		long stop = System.currentTimeMillis();
		if (startTime != null) {
			start = Long.parseLong(startTime);
		}
		if (stopTime != null) {
			stop = Long.parseLong(stopTime);
		}
		List<DAGR> DAGRs = Utils.getModifiedTimeRange(start, stop);
		return DAGRs;
	}
}
