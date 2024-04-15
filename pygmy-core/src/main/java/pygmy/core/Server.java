package pygmy.core;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.LogManager;

/**
 * <p>
 * Server is the core of the system.  A server glues together {@link Handler}s
 * and {@link EndPoint}s.  {@link EndPoint}s are responsible for reading the
 * {@link HttpRequest} from a source and sending the HttpResponse over that source.
 * {@link EndPoint}s then sends the request to the {@link Handler} by calling the post()
 * method on the server to send the request to this server's {@link Handler}s.  {@link Handler}s
 * process the {@link HttpRequest} and produce an appropriate {@link HttpResponse}.
 * </p><p>
 * The server contains the configuration for the entire server.  What are the expected values
 * in the configuration is mainly controlled by what handlers and endpoints are configured.
 * Depending on which handlers and endpoints have been enabled, the configuration will vary.
 * The only two parameters are required: <i>handler</i> and <i>&lt;handler's name&gt;.class</i>.
 * Here is an example configuration:
 * </p><pre>
 * <div class="code">
 * handler=my\ handler
 * my\ handler.class=pygmy.handlers.DefaultChainHandler
 * my\ handler.chain=handler1, handler2
 * my\ handler.url-prefix=/
 *
 * handler1.class=pygmy.handlers.FileHandler
 * handler1.root=C:\temp
 * handler1.url-prefix=/home-directory
 *
 * handler2.class=pygmy.handlers.ResourceHandler
 * handler2.url-prefix=/jar
 * handler2.resourceMount=/html
 * handler2.default=index.html
 * </pre>
 * </div
 * <p>
 * In the above configuration, <i>handler</i> property is the name of first handler.
 * The name is used to find all the other properties for that particular handler.
 * The .class property is used to tell the Server the name of the class to instantiate.
 * The two other properties, .chain and .url-prefix, are particular to the
 * {@link pygmy.handlers.DefaultChainHandler}.
 * </p><p>
 * Server's only have <b>one</b> {@link Handler}.  However, the power of
 * {@link Handler}s is the ability to have more than one. The
 * {@link pygmy.handlers.DefaultChainHandler} provides the ability to create a
 * chain of multiple handlers.  See {@link pygmy.handlers.DefaultChainHandler} for
 * information on configuring it.
 * </p><p>
 * Server also contains a set of {@link pygmy.core.EndPoint}s.  When the server initializes
 * itself it looks in the configuration for the <i>endpoints</i> parameter.  The <i>endpoints</i>
 * parameter contains a space seperated list of the names of the endpoints this server will
 * create.  For each name in the list it will look for a config parameter
 * <i>&lt;name of endpoint&gt;.class</i> in the configuration.  It will instantiate the classname
 * using the no-argument constructor and add it to the set of endpoints in the server.
 * </p><p>
 * If the server does not find the <i>endpoints</i> parameter, then it will create a default EndPoint
 * of type {@link pygmy.core.ServerSocketEndPoint} named http.  Here is an example of using the <i>endpoints</i>
 * parameter:
 * </p>
 * <div class="code">
 * <pre>
 * endpoints=endpoint1 endpoint2
 * handler=handler1
 *
 * endpoint1.class=my.package.MyEndPoint
 * endpoint1.param1=foo
 * endpoint1.param2=bar
 * endpoint2.class=my.package.AnotherEndPoint
 * endpoint2.param1=foo
 * endpoint2.param2=bar
 * endpoint2.param3=baz
 * ...
 * </pre>
 * </div>
 * <p>
 * Server class looks for the following properties in the configuration:
 * </p>
 * <table class="inner">
 * <tr class="header"><td>Parameter Name</td><td>Default Value</td><td>Required</td></tr>
 * <tr class="row"><td>handler</td><td>None</td><td>Yes</td></tr>
 * <tr class="altrow"><td>endpoints</td><td>http</td><td>No</td></tr>
 * <tr class="row"><td>&lt;handler name&gt;.class</td><td>None</td><td>Yes</td></tr>
 * <tr class="altrow"><td>&lt;endpoint name&gt;.class</td><td>None</td><td>iff endpoints param is defined</td></tr>
 * <tr class="row"><td>threadpool.size</td><td>5</td><td>No</td></tr>
 * </table>
 */
@Slf4j
public class Server implements Runnable {

    Properties config = new ChainableProperties();
    HashMap endpoints = new HashMap();
    Handler handler = null;
    ResponseListener responseListener = null;
    ThreadPool threadPool;
    public static final String PYGMY_SERVER_VERSION = "v0.4.3";

    private static final String CLAZZ = ".class";

    /**
     * This creates a server using the given filename as the configuration for this server.  The configuration file
     * should follow format of normal {@link java.util.Properties} file.
     *
     * @param filename the path to a file to use as the configuration of this server.
     * @throws IOException
     */
    public Server(String filename) throws IOException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(filename));
            config.load(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * This creates a server using the given configuration.
     *
     * @param config the configuration to use for this server.
     */
    public Server(Properties config) {
        this.config = config;
    }

    /**
     * This creates a server from commandline arguments.
     *
     * @param args an array of config parameters, the format of the list is a '-' followed by either 'config' or some
     *             name of a parameter (i.e. http.port), and a space and a value for that property.  All -config will load a file
     *             either from the filesystem or the class path if the file doesn't exist on the filesystem.
     */
    public Server(String[] args) throws IOException {
        config = new ChainableProperties();
        processArguments(args, config);
    }

    /**
     * This method adds an {@link EndPoint} to this server.  It will be initialized once the {@link #start} method is called.
     *
     * @param name     The name of this EndPoint instance.
     * @param endpoint The instance of the endpoint to add.
     */
    public void addEndPoint(String name, EndPoint endpoint) {
        endpoints.put(name, endpoint);
    }

// todo delete this method.  it isn't used.
//    /**
//     * This sets the handler instance for the server.  The handlername is used to pass to the
//     * {@link Handler#initialize} method.
//     *
//     * @param handlerName the name that should be assigned to this handler.
//     * @param handler the instance of the handler used by this server to service requests.
//     */
//    public void setHandler( String handlerName, Handler handler ) {
//        this.rootHandlersName = handlerName;
//        this.handler = handler;
//    }

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

    public Object getRegisteredComponent(Class clazz) {
        return config.get(clazz);
    }

    /**
     * This is called when a program wants to register a shared component for Handlers to have access to.  The objects's
     * class will be the defining key for identitying the object.  Registering more than one instance will not
     * be supported.  If you're program must differ between two instances, then register a manager and allow the
     * Handler's to interact with that to differentiate the individual instances.
     *
     * @param object the object the user wants to make available to Handler instances.
     */
    public void registerComponent(Object object) {
        config.put(object.getClass(), object);
    }

    /**
     * This method is called to start the web server.  It will initialize the server's Handler and all the EndPoints
     * then call the {@link EndPoint#start} on each EndPoint.  This method will return after the above steps are
     * done.
     */
    public void start() {
        log.debug("Starting Pygmy Server ({})", PYGMY_SERVER_VERSION);
        Runtime.getRuntime().addShutdownHook(new Thread(this, "PygmyShutdown"));
        initializeThreads();
        initializeHandler();
        if (handler == null) {
            return;
        }

        constructEndPoints();

        for (Iterator i = endpoints.values().iterator(); i.hasNext(); ) {
            EndPoint currentEndPoint = (EndPoint) i.next();
            currentEndPoint.start();
        }
        log.info("Pygmy Server ({}) running.", PYGMY_SERVER_VERSION);
    }

    // copied from old code, might be usefull in the future
    private void initalizeMimeTypes() {
//        InputStream is = getClass().getResourceAsStream("/config/mime-types.properties");
//        if (is != null) {
//            try {
//                defaults.load(is);
//            } finally {
//                is.close();
//            }
//        }
    }

    private void initializeThreads() {
        try {
            threadPool = new ThreadPool(Integer.parseInt(config.getProperty("threadpool.size", "5")));
        } catch (NumberFormatException e) {
            log.warn("threadpool.size was not a number using default of 5");
            threadPool = new ThreadPool(5);
        }
    }

    protected void initializeHandler() {
        if (handler == null) {
            handler = (Handler) constructPygmyObject(getProperty("handler"));
        }
        handler.initialize(getProperty("handler"), this);
    }

    /**
     * This is the method used to construct pygmy objects.  Given the object name it appends .class onto the end and
     * looks for the classname in the server's configuration.  It then analyzes the class's constructor parameters for
     * objects that it depends on.  Then it looks those objects up by class in the registered object pool.  Finally, it
     * calls the constructor using reflection passing any registered objects as arguments.  It returns
     * the newly constructed object or null if there was a problem.
     *
     * @param objectName the name of the object defined in the server's configuration.
     * @return the newly constructed object, or null there was a problem instantiated the object.
     */
    public Object constructPygmyObject(String objectName) {
        Object theObject = null;
        String objectClassname = getProperty(objectName + CLAZZ);
        try {
            if (objectClassname == null)
                throw new ClassNotFoundException(objectName + CLAZZ + " configuration property not found.");
            Class handlerClass = Class.forName(objectClassname);
            Constructor[] constructors = handlerClass.getConstructors();
            Class[] paramClass = constructors[0].getParameterTypes();
            Object[] params = new Object[paramClass.length];
            for (int i = 0; i < paramClass.length; i++) {
                if (paramClass[i].equals(Server.class)) {
                    params[i] = this;
                } else if (paramClass[i].equals(String.class)) {
                    params[i] = objectName;
                } else {
                    params[i] = getRegisteredComponent(paramClass[i]);
                }
            }
            theObject = constructors[0].newInstance(params);
            log.debug("Pygmy object constructed. object=" + objectName + " class=" + objectClassname);
        } catch (IllegalAccessException e) {
            log.error("Could not access constructor. Make sure it has the constructor is public. Service not started.  class=" + objectClassname, e);
        } catch (InstantiationException e) {
            log.error("Could not instantiate object. Service not started. class=" + objectClassname, e);
        } catch (ClassNotFoundException e) {
            log.error("Could not find class. Service not started.  class=" + objectClassname, e);
        } catch (InvocationTargetException e) {
            log.error("Could not instantiate object because constructor threw an exception.  Service not started.  class=" + objectClassname, e);
            log.error("Cause:", e.getTargetException());
        }
        return theObject;
    }

    private void constructEndPoints() {
        String val = getProperty("endpoints");
        if (val != null) {
            StringTokenizer tokenizer = new StringTokenizer(val);
            while (tokenizer.hasMoreTokens()) {
                String endPointName = tokenizer.nextToken();
                try {
                    EndPoint endPoint = (EndPoint) constructPygmyObject(endPointName);
                    endPoint.initialize(endPointName, this);
                    addEndPoint(endPointName, endPoint);
                } catch (IOException e) {
                    log.error(endPointName + " was not initialized properly.", e);
                }
            }
        } else {
            log.error("No endpoints defined.");
        }
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
        log.debug("Starting Pygmy Server shutdown.");
        try {
            threadPool.shutdown();
            if (handler != null) {
                log.debug("Shutting down handlers.");
                handler.shutdown(this);
            }
            Collection values = endpoints.values();
            if (values != null) {
                for (Iterator i = values.iterator(); i.hasNext(); ) {
                    EndPoint currentEndPoint = (EndPoint) i.next();
                    log.debug("Shutting down endpoint " + currentEndPoint.getName());
                    currentEndPoint.shutdown(this);
                }
            }
        } finally {
            log.info("Pygmy Server ({}) shutdown complete.", PYGMY_SERVER_VERSION);
        }
    }

    /**
     * This method is used to post a {@link HttpRequest} to the server's handler.  It will create a HttpResponse
     * for the EndPoint to send to the client.
     *
     * @param request
     * @return A HttpResponse that corresponds to the HttpRequest being handled.
     * @throws IOException
     */
    public boolean post(Request request, Response response) throws IOException {
        return handler.handle(request, response);
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

    public static void main(String[] args) throws IOException {
        Server server = new Server(args);
        try {
            server.start();
            System.out.println("Server started.  Press <Ctrl-C> to stop.");
            synchronized (server) {
                server.wait();
            }
        } catch (InterruptedException e) {
            log.info("Server Interupted.");
        }
    }

    protected void processArguments(String[] args, Properties props) throws IOException {
        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equalsIgnoreCase("-config")) {
                if (i + 1 < args.length) {
                    loadConfiguration(args[i + 1], props);
                } else {
                    throw new IOException("-config parameter must be followed by a config file.");
                }
            } else if (args[i].startsWith("-")) {
                props.setProperty(args[i].substring(1), args[i + 1]);
            }
        }
        setDefaultProperties(props);
        setupLogging(props);
    }

    private void setupLogging(Properties props) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        props.store(stream, "");
        ByteArrayInputStream bais = new ByteArrayInputStream(stream.toByteArray());
        LogManager.getLogManager().readConfiguration(bais);
    }

    private static void setDefaultProperties(Properties props) {
        setDefaultProperty(props, "mime.html", "text/html");
        setDefaultProperty(props, "mime.zip", "application/x-zip-compressed");
        setDefaultProperty(props, "mime.gif", "image/gif");
        setDefaultProperty(props, "mime.jpeg", "image/jpeg");
        setDefaultProperty(props, "mime.jpg", "image/jpeg");
        setDefaultProperty(props, "mime.css", "text/css");
        setDefaultProperty(props, "http.port", "80");
        setDefaultProperty(props, "handler", "chain");
        setDefaultProperty(props, "chain.class", "pygmy.handlers.DefaultChainHandler");
        setDefaultProperty(props, "chain.chain", "root");
        // sets a default endpoint for http
        setDefaultProperty(props, "endpoints", "http");
        setDefaultProperty(props, "http.class", "pygmy.core.ServerSocketEndPoint");
        // these are properties read by the ResourceHandler named 'root'
        setDefaultProperty(props, "root.class", "pygmy.handlers.ResourceHandler");
        setDefaultProperty(props, "root.urlPrefix", "/");
        setDefaultProperty(props, "root.resourceMount", "/doc");
    }

    private static void setDefaultProperty(Properties props, String key, String value) {
        if (props.getProperty(key) == null) {
            props.setProperty(key, value);
        }
    }

    protected void loadConfiguration(String config, Properties props) throws IOException {
        InputStream is = openInputStream(config);
        props.load(is);
        is.close();
    }

    private InputStream openInputStream(String config) throws FileNotFoundException {
        InputStream is;
        try {
            is = new FileInputStream(config);
        } catch (FileNotFoundException e) {
            is = Server.class.getResourceAsStream("/" + config);
            if (is == null) throw e;
        }
        return is;
    }
}
