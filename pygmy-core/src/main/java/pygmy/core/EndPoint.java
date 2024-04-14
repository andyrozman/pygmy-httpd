package pygmy.core;

import java.io.IOException;

/**
 * <p>
 * An Endpoint is reponsible for recieving HTTP requests (from sockets, files, etc),
 * posting the requests to the Server, and sending the HttpResponse back.  They are
 * also in control of threading and honoring keepAlives. All implementing classes
 * must provide a no-arg constructor so that the Server can instantiate them.
 * </p>
 */
public interface EndPoint {

    void start(Server server) throws IOException;

    void shutdown(Server server);

}
