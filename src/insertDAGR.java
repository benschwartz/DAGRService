import java.io.IOException;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;


public class insertDAGR extends ServerResource {
	@Post("json")
	public Representation acceptItem(Representation entity) {
		Representation result = null;
		// Parse the given representation and retrieve pairs of
		// "name=value" tokens.
		Form form = new Form(entity);
		String dagrJson = form.getFirstValue("dagr");
		DAGR dagr = new Gson().fromJson(dagrJson, DAGR.class);
		Utils.connect();
		if(!Utils.insertDAGR(dagr)){
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return generateErrorRepresentation("Could not insert DAGR", "1");
		}
		Utils.disconnnect();
		return result;
	}
	/**
	 * Generate an XML representation of an error response.
	 * 
	 * @param errorMessage
	 *            the error message.
	 * @param errorCode
	 *            the error code.
	 */
	private Representation generateErrorRepresentation(String errorMessage,
			String errorCode) {
		DomRepresentation result = null;
		// This is an error
		// Generate the output representation
		try {
			result = new DomRepresentation(MediaType.TEXT_XML);
			// Generate a DOM document representing the list of
			// items.
			Document d = result.getDocument();

			Element eltError = d.createElement("error");

			Element eltCode = d.createElement("code");
			eltCode.appendChild(d.createTextNode(errorCode));
			eltError.appendChild(eltCode);

			Element eltMessage = d.createElement("message");
			eltMessage.appendChild(d.createTextNode(errorMessage));
			eltError.appendChild(eltMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	
}
