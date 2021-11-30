
/**
 * OrcOfficer
 */
public class OrcOfficer extends RequestHandler {

    public OrcOfficer(RequestHandler handler) {
        super(handler);
    }

    @Override
    public void handleRequest(Request req) {
        if (req.getRequestType().equals(RequestType.TORTURE_PRISONER)) {
          printHandling(req);
          req.markHandled();
        } else {
          super.handleRequest(req);
        }
    }

    @Override
    public String toString() {
        return "Orc officer";
    }

}
