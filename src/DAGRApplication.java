import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class DAGRApplication extends Application {
	@Override
	public synchronized Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/getDAGR", GetDAGR.class);
		return router;
	}
}
