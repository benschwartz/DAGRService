import java.util.UUID;
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
import com.google.gson.JsonParseException;

public class InsertDAGR extends ServerResource {
	Logger LOGGER = Logger.getLogger("DAGR");
	@Post("text/plain|text/xml")
	@Options("text/plain|text/xml")
	public Representation acceptItem(Representation entity) {
		DAGRApplication.fixResponseHeader(getResponse());
		// Parse the given representation and retrieve pairs of
		// "name=value" tokens.
		String dagrJson = getQuery().getFirstValue("dagr");
		String parentGUID = getQuery().getFirstValue("parent");
		if (dagrJson == null || parentGUID == null) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "missing parameter");
			return new EmptyRepresentation();
		}
		DAGR dagr;
		try{
			dagr = new Gson().fromJson(dagrJson, DAGR.class);
		}
		catch(JsonParseException e){
			LOGGER.log(Level.SEVERE,"Could not parse DAGR JSON:" + dagrJson);
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse DAGR JSON");
			return new EmptyRepresentation();
		}
		if(dagr.getAuthor()==null || dagr.getCreateTime()==null || dagr.getLocation()==null || dagr.getModifiedTime()==null || dagr.getName()==null || dagr.getSize()==null || dagr.getType()==null){
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "missing members of dagr");
			LOGGER.log(Level.SEVERE,"Missing members of dagr:" + dagrJson);
			return new EmptyRepresentation();
		}
		if (dagr.getGUID() == null) {
			dagr.setGUID(UUID.randomUUID().toString());
		}
		String GUID = Utils.insertDAGR(dagr, parentGUID);
		if (GUID == null) {
			LOGGER.log(Level.SEVERE,"Could not insert DAGR:" + dagrJson + " " + parentGUID);
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Could not insert DAGR");
			return new EmptyRepresentation();
		}
		return new StringRepresentation(GUID, MediaType.TEXT_PLAIN);
	}

}
