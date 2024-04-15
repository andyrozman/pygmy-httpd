package pygmy.handlers;

import lombok.extern.slf4j.Slf4j;
import pygmy.core.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This is the default implementation of a chain of handlers.  The .chain parameter defines the names of the
 * handlers in the chain, and it defines the order in which those handlers will be called.  Each handler name is
 * seperated by either a ' ' (space) or a ',' (comma).  This handler will then try to create a handler for each of
 * the handler names by looking at configuration property {handler-name}.class.  This handler also has a .url-prefix
 * parameter it uses to know when this handler should pass the request to the chain.
 *
 * <table class="inner">
 * <tr class="header"><td>Parameter Name</td><td>Explanation</td><td>Default Value</td><td>Required</td></tr>
 * <tr class="row"><td>url-prefix</td><td>The prefix to filter request urls.</td><td>None</td><td>Yes</td></tr>
 * <tr class="altrow"><td>chain</td><td>A space or comma seperated list of the names of the handlers within the chain.</td><td>None</td><td>Yes</td></tr>
 * <tr class="row"><td>class</td><td>For each of the names in the chain property, this is appended the name to find the classname to instatiate.</td><td>None</td><td>Yes</td></tr>
 * </table>
 */
@Slf4j
public class DefaultChainHandler extends AbstractHandler implements Handler {

    public static String CHAIN = ".chain";

    public static final ConfigOption CHAIN_OPTION = new ConfigOption("chain", true, "A comma seperated list of handler names to chain together.");

    private List chain;

    public boolean initialize(String handlerName, Server server) {
        super.initialize(handlerName, server);
        this.chain = new ArrayList();
        initializeChain(server);
        return true;
    }

    private void initializeChain(Server server) {
        StringTokenizer tokenizer = new StringTokenizer(CHAIN_OPTION.getProperty(server, handlerName), " ,");
        while (tokenizer.hasMoreTokens()) {
            String chainChildName = tokenizer.nextToken();
            try {
                Handler handler = (Handler) server.constructPygmyObject(chainChildName);
                if (handler.initialize(chainChildName, server)) {
                    chain.add(handler);
                } else {
                    log.error(chainChildName + " was not initialized");
                }
            } catch (ClassCastException e) {
                log.error(chainChildName + " class does not implement the Handler interface.", e);
            }
        }
    }

    public boolean handle(Request request, Response response) throws IOException {
        boolean hasBeenHandled = false;
        for (Iterator i = chain.iterator(); i.hasNext() && !hasBeenHandled; ) {
            Handler handler = (Handler) i.next();
            hasBeenHandled = handler.handle(request, response);
        }
        return hasBeenHandled;
    }

    public boolean shutdown(Server server) {
        boolean success = true;
        if (chain != null) {
            for (Iterator i = chain.iterator(); i.hasNext(); ) {
                Handler current = (Handler) i.next();
                boolean currentSuccess = current.shutdown(server);
                success = success && currentSuccess;
            }
        }

        return success;
    }
}
