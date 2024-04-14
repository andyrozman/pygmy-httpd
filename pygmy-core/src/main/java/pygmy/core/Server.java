package pygmy.core;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.LogManager;

/**
 * <p>
 * Server is the core of the system.  A server glues together {@link Handler}s
 * and {@link EndPoint}s.  {@link EndPoint}s are responsible for reading the
 * {@link HttpRequest} from a source and sending the HttpResponse over that source.
 * {@link EndPoint}s then sends the request to the {@link Handler} by calling the post()
 * method on the server to send the request to this server's {@link Handler}s.  {@link Handler}s
 * process the {@link HttpRequest} and produce an appropriate {@link HttpResponse}.
 * </p>
 * The server is configured through Java code (in prior releases it used a properties file that
 * has been removed).  The server by itself is not very useful so it needs to be configured
 * by adding any number of {@link pygmy.core.Handler}s to it.  Handlers are attached or mounted
 * onto a {@link pygmy.core.UrlRule}.  When the given {@link pygmy.core.UrlRule} matches the
 * request that {@link pygmy.core.Handler} is invoked to handle the request.  Here is a quick
 * example:
 * </p>
 *
 * <div class="code">
 * public static void main( String[] args ) {
 *    Server server = new Server();
 *    server.add( new UrlRule("/"), new FileHandler("/home/charlie/public_html").allowDirectoryListing(true) );
 *    server.start();
 * }
 * </div>
 * <p>
 * Here we are setting up a single {@link UrlRule} that responds to urls starting with / (i.e. root), and
 * mapping that onto the file system path of "/home/charlie/public_html" using a
 * {@link pygmy.handlers.FileHandler}.  So any files inside that root directory are visible to our web clients.
 * We've also turned on directory listings so requests that map to directories will result in a directory
 * list of all the files in them.  By default there are some images that the directory listing uses that
 * ship with Pygmy so we can also server those up using the {@link pygmy.handlers.ResourceHandler}.  Here is
 * an example of setting that up.
 * </p>
 *
 * <div class="code">
 * public static void main( String[] args ) {
 *    Server server = new Server();
 *    server.add( new UrlRule("/web", new ResourceHandler("/pygmy/web") );
 *    server.add( new UrlRule("/"), new FileHandler("/home/charlie/public_html").allowDirectoryListing(true) );
 *    server.start();
 * }
 * </div>
 * <p>
 * Notice the rule for "/web" was added before "/" rule.  The more specific url (i.e. "/web") will be matched
 * first hence intercepting our least specific rule (i.e. "/").  <b>The first UrlRule to match wins</b>.
 * Keep that in mind when adding your Handlers.  So what these rules accomplish are all requests made to
 * "/web" will be routed to our {@link pygmy.handlers.ResourceHandler} which will server up files from a
 * path inside our jar file or classpath.  All other requests will be served up from the file system under
 * "/home/charlie/public_html".
 * </p>
 * <p>
 * As mentioned above Server class is a collection {@link pygmy.core.Handler}s and {@link pygmy.core.EndPoint}s.
 * By default Server will create a default endpoint of type {@link pygmy.core.ServerSocketEndPoint} named http
 * if there are no other endpoints added.  Here is an example of adding more than one endpoint.
 * </p>
 * <div class="code">
 * public static void main( String[] args ) {
 *    File keystoreFile = new File("mykeystore.ks");
 *    Server server = new Server();
 *    server.add( new UrlRule("/"), new FileHandler("/home/charlie/public_html").allowDirectoryListing(true) );
 *    server.add( "http", new ServerSocketEndPoint( 8080 ) );
 *    server.add( "https", new SSLServerSocketEndPoint( keystoreFile, "myCert", "supersecretpassword" ) );
 *    server.start();
 * }
 * </div>
 * <p>
 * Here we have two endpoints being registered.  One is a plain old http endpoint on port 8080 (default is 80),
 * and another endpoint that talks https.  The {@link pygmy.core.SSLServerSocketEndPoint} requires a
 * keystore, certificate alias, and a password to unlock the keystore.  So now the server will respond to
 * normal http requests on port 8080, and secure http on port 143 (the default).
 * </p>
 */
@Slf4j
public class Server implements Runnable {


    private ChainableProperties defaults = new ChainableProperties(System.getProperties());
    private ChainableProperties config = new ChainableProperties(defaults);
    private Map<String, EndPoint> endpoints = new HashMap<String, EndPoint>();
    private ResponseListener responseListener = null;
    private ThreadPool threadPool;
    private Map<UrlRule, Handler> rules = new ConcurrentHashMap<UrlRule, Handler>();
    private List<UrlRule> ruleOrder = new CopyOnWriteArrayList<UrlRule>();

    private int threadPoolSize = 5;

    /**
     * This method adds an {@link EndPoint} to this server.  It will be initialized once the
     * {@link #start} method is called.
     *
     * @param name     The unique name of this EndPoint instance.
     * @param endpoint The instance of the endpoint to add.
     */
    public void add(String name, EndPoint endpoint) {
        endpoints.put(name, endpoint);
    }

    /**
     * This puts a configuration property into the server's configuration.
     *
     * @param key   The unique key to store the value under.
     * @param value The value of the key.
     */
    public void putProperty(Object key, Object value) {
        config.put(key, value);
    }

    /**
     * Returns the property stored under the key.
     *
     * @param key the configuration key to look up.
     * @return the value stored in the configuration at this key.
     */
    public String getProperty(String key) {
        return config.getProperty(key);
    }

    /**
     * Returns the property stored under the key.  If there isn't a property called key, then it returns the defaultValue.
     *
     * @param key          the configuration key to look up.
     * @param defaultValue the defaultValue returned if nothing is found under the key.
     * @return the value stored in the configuration at this key.
     */
    public String getProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    /**
     * Returns true iff the key is in the configuration
     *
     * @param key the name of the key.
     * @return true if and only if the key is contained in the configuration. False otherwise.
     */
    public boolean hasProperty(String key) {
        return config.containsKey(key);
    }

    /**
     * This returns the object stored under the given key.
     *
     * @param key the key to look up the stored object.
     * @return the object stored at the given key.
     */
    public Object get(Object key) {
        return config.get(key);
    }

    /**
     * Returns the configuration for the server.
     *
     * @return the configuration for the server.
     */
    public Properties getConfig() {
        return config;
    }

    /**
     * Attaches a Handler object to this UrlRule pattern.
     *
     * @param rule    rule used to match against a URL.
     * @param handler to invoke when the given rule matches a url.
     * @return the server instance for chaining.
     */
    public Server add(UrlRule rule, Handler handler) {
        rules.put(rule, handler);
        ruleOrder.add(rule);
        return this;
    }

    /**
     * Given a rule return the Handler associated with that rule.
     *
     * @param rule a rule that was previously registered with a Handler.
     * @return the Handler that responds that the given UrlRule.
     */
    public Handler getHandler(UrlRule rule) {
        return rules.get(rule);
    }

    /**
     * Return the handler that matches the given URL.  This is an absolute URL starting from the path.
     *
     * @param url an absolute URL starting at the path portion.
     * @return The Handler that matches the given URL.
     */
    public Handler getHandler(String url) {
        for (UrlRule rule : ruleOrder) {
            UrlMatch match = rule.matches(url);
            if (match != null) return getHandler(rule);
        }
        return null;
    }

    /**
     * Removes the registered Handler at the given UrlRule.
     *
     * @param rule a UrlRule that was previously registered.
     * @return This server instance for chaining calls.
     */
    public Server remove(UrlRule rule) {
        rules.remove(rule);
        ruleOrder.remove(rule);
        return this;
    }

    /**
     * This method is called to start the web server.  It will initialize the server's Handler and all the EndPoints
     * then call the {@link EndPoint#start} on each EndPoint.  This method will return after the above steps are
     * done.
     *
     * @throws java.io.IOException if it can't load default configuration.
     */
    public void start() throws IOException {
        InputStream is = getClass().getResourceAsStream("/config/mime-types.properties");
        if (is != null) {
            try {
                defaults.load(is);
            } finally {
                is.close();
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this, "PygmyShutdown"));
        initializeThreads();

        for (Handler handler : rules.values()) {
            handler.start(this);
        }

        if (!endpoints.isEmpty()) {
            for (EndPoint currentEndPoint : endpoints.values()) {
                try {
                    currentEndPoint.start(this);
                } catch (IOException e) {
                    log.warn("Error trying to startup endpoint", e);
                }
            }
        } else {
            add("http", new ServerSocketEndPoint());
            try {
                endpoints.get("http").start(this);
            } catch (IOException e) {
                log.warn("Error trying to startup default endpoint", e);
            }
        }
    }

    private void initializeThreads() {
        threadPool = new ThreadPool(threadPoolSize);
    }

    /**
     * This is called when the server is shutdown thread is called.
     */
    public void run() {
        shutdown();
        synchronized (this) {
            this.notify();
        }
    }

    /**
     * This method will shutdown the Handler, and call {@link EndPoint#shutdown} on each EndPoint.
     */
    public void shutdown() {
        log.info("Starting shutdown.");
        try {
            threadPool.shutdown();
            for (Handler handler : rules.values()) {
                log.debug("Shutting down handlers.");
                handler.shutdown(this);
            }
            Set keys = endpoints.keySet();
            if (keys != null) {
                for (String name : endpoints.keySet()) {
                    EndPoint currentEndPoint = endpoints.get(name);
                    log.debug("Shutting down endpoint " + name);
                    currentEndPoint.shutdown(this);
                }
            }
        } finally {
            log.debug("Shutdown complete.");
        }
    }

    /**
     * This method is used to post a {@link HttpRequest} to the server's handler.  It will create a HttpResponse
     * for the EndPoint to send to the client.
     *
     * @param request  the http request
     * @param response the http response
     * @return A HttpResponse that corresponds to the HttpRequest being handled.
     * @throws IOException when the underlying IO throws one.
     */
    public boolean post(HttpRequest request, HttpResponse response) throws IOException {
        try {
            for (UrlRule rule : ruleOrder) {
                UrlMatch match = rule.matches(request.getUrl());
                if (match != null) {
                    request.setUrlMatch(match);
                    Handler handler = rules.get(rule);
                    return handler.handle(request, response);
                }
            }
            return false;
        } finally {
            request.cleanup();
        }
    }

    /**
     * This method posts a Runnable onto the Server's task queue.  The server's {@link ThreadPool} will service the
     * runnable once a thread is freed up.
     *
     * @param runnable An instance of Runnable that the user wishes to run on the server's {@link ThreadPool}.
     */
    public void post(Runnable runnable) {
        threadPool.execute(runnable);
    }

    /**
     * Returns the instance of the ResponseListener for this Server.
     *
     * @return the ResponseListener for this Server, or null if there is none.
     */
    public ResponseListener getResponseListeners() {
        return responseListener;
    }

    /**
     * This sets the ResponseListener for entire server.  All replys being sent to any client will
     * be notified to this instance.
     *
     * @param listener the instance of a ResponseListener to use for this Server.
     */
    public void setResponseListener(ResponseListener listener) {
        this.responseListener = listener;
    }

    /**
     * Gets the absolute URI that this server's given endpoint is responding to.
     * (i.e. http://x.x.x.x:y/ where x.x.x.x is the IP/hostname and y is the port).
     *
     * @param endpointName the name of the endpoint to use to determine the URI to base on.
     * @return The URI for this endpointName and server.
     * @throws URISyntaxException thrown only if URI constructor doesn't like the resulting URI.
     */
    public URI getServerURL(String endpointName) throws URISyntaxException {
        return getServerURL(endpointName, null, null, null);
    }

    /**
     * Gets the absolute URI that this server's given endpoint is responding to, and adds the given path,
     * query, and fragment portions.
     * (i.e. http://x.x.x.x:y/$path?$query#$fragment where x.x.x.x is the IP/hostname and y is the port).
     *
     * @param endpointName the name of the EndPoint to use.
     * @param path         the path to append (can be null)
     * @param query        the query to append (can be null)
     * @param fragment     the fragment to append (can be null)
     * @return The URI for this endpointName and server with path, query, and fragment returned.
     * @throws URISyntaxException thrown only if URI constructor doesn't like the resulting URI.
     */
    public URI getServerURL(String endpointName, String path, String query, String fragment) throws URISyntaxException {
        EndPoint endpoint = endpoints.get(endpointName);
        if (endpoint instanceof ServerSocketEndPoint) {
            ServerSocketEndPoint serverSocketEndPoint = (ServerSocketEndPoint) endpoint;
            return new URI(serverSocketEndPoint.getProtocol(), null, serverSocketEndPoint.getAddress().getHostName(), serverSocketEndPoint.getPort(), path, query, fragment);
        } else {
            return null;
        }
    }

    public void configureLogging(Properties props) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        props.store(stream, "");
        ByteArrayInputStream bais = new ByteArrayInputStream(stream.toByteArray());
        LogManager.getLogManager().readConfiguration(bais);
    }
}
