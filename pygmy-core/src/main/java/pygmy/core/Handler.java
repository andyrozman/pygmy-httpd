package pygmy.core;

import java.io.IOException;

/**
 * <p>
 * Objects that implement this interface handle the HttpRequests.  It processes
 * the request by returning true for the handler() method.
 * </p>
 */
public interface Handler {

    boolean initialize(String handlerName, Server server);

    String getName();

    boolean handle(Request request, Response response) throws IOException;

    boolean shutdown(Server server);
}
