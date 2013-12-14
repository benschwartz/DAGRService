import java.util.List;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

public class GetDAGR extends ServerResource {

	@Get("json")
	public String represent() {
		StringBuilder response = new StringBuilder();
		List<DAGR> dagrs = getDAGRList();
		//response.append("Query returned " + dagrs.size() + " results:\n");
		for(DAGR dagr: dagrs){
			response.append(new Gson().toJson(dagr) + "\n");
		}
		return response.toString();
	}

	private List<DAGR> getDAGRList() {
		String GUID = getQueryValue("guid");
		String parentGUID = getQueryValue("parent_guid");
		String location = getQueryValue("location");
		String type = getQueryValue("type");
		String author = getQueryValue("author");
		String name = getQueryValue("name");
		String size = getQueryValue("size");
		String createTime = getQueryValue("time_created");
		String modifiedTime = getQueryValue("time_modified");
		List<DAGR> DAGRs = Utils.getDAGRs(GUID, parentGUID, location, type,
				author, name, size, createTime, modifiedTime);
		return DAGRs;
	}
}
