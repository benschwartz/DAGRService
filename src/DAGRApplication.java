import org.restlet.Application;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.routing.Router;
import org.restlet.util.Series;

public class DAGRApplication extends Application {
	@Override
	public synchronized Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/getDAGR", GetDAGR.class);
		router.attach("/getChildren", GetChildren.class);
		router.attach("/getParents", GetParents.class);
		router.attach("/getTypes",GetTypes.class);
		router.attach("/getAuthors",GetAuthors.class);
		router.attach("/insertDAGR", InsertDAGR.class);
		router.attach("/deleteDAGR",DeleteDAGR.class);
		router.attach("/getKeywords",GetKeywords.class);
		router.attach("/addKeyword",AddKeyword.class);
		router.attach("/deleteKeyword",DeleteKeyword.class);
		router.attach("/orphanSterile",OrphanSterile.class);
		router.attach("/getModifiedTimerange",GetModifiedTimerange.class);
		router.attach("/getCreatedTimerange",GetCreatedTimerange.class);
		router.attach("/getReach",GetReach.class);
		router.attach("/ingestWebpage",IngestWebpage.class);
		return router;
	}

	@SuppressWarnings("unchecked")
	public static void fixResponseHeader(Response r) {
		Series<Header> responseHeaders = (Series<Header>) r.getAttributes()
				.get(HeaderConstants.ATTRIBUTE_HEADERS);
		if (responseHeaders == null) {
			responseHeaders = new Series<Header>(Header.class);
			r.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
					responseHeaders);
		}
		responseHeaders.add(new Header("Access-Control-Allow-Origin", "*"));
	}
}
