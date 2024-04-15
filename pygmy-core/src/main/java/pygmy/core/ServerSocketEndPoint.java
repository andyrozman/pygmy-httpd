package pygmy.core;

import lombok.extern.slf4j.Slf4j;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * This EndPoint provides normal sockets for the http protocol.  It can be sublasses and overriden for protocols other
 * than http.
 *
 * <table class="inner">
 * <tr class="header"><td>Parameter Name</td><td>Explanation</td><td>Default Value</td><td>Required</td></tr>
 * <tr class="row"><td>port</td><td>The port the socket should listen on.</td><td>80</td><td>No</td></tr>
 * <tr class="altrow"><td>host</td><td>The ip or dns of the host adapter this socket should bind to.</td><td>None</td><td>No</td></tr>
 * <tr class="row"><td>resolveHostName</td><td>If the server should do a reverse DNS on the connections so the logs will show the DNS name of the client.</td><td>false</td><td>No</td></tr>
 * </table>
 */
@Slf4j
public class ServerSocketEndPoint implements EndPoint, Runnable {

    private static final ConfigOption PORT_OPTION = new ConfigOption("port", "80", "HTTP server port.");
    private static final ConfigOption RESOLVE_HOSTNAME_OPTION = new ConfigOption("resolveHostName", "false", "Resolve host names");

    protected ServerSocketFactory factory;
    protected ServerSocket socket;
    protected Server server;
    protected String endpointName;
    protected boolean resolveHostName;

    public ServerSocketEndPoint() {
        factory = ServerSocketFactory.getDefault();
    }

    public void initialize(String name, Server server) throws IOException {
        this.endpointName = name;
        this.server = server;
        resolveHostName = RESOLVE_HOSTNAME_OPTION.getBoolean(server, endpointName).booleanValue();
    }

    public String getName() {
        return endpointName;
    }

    protected ServerSocket createSocket(int port) throws IOException {
        ServerSocket socket = factory.createServerSocket(port);
        return socket;
    }


    public void start() {
        try {
            this.socket = createSocket(PORT_OPTION.getInteger(server, endpointName).intValue());
            log.debug("Socket listening on port " + socket.getLocalPort());
            Thread thread = new Thread(this, endpointName + "[" + socket.getLocalPort() + "] ServerSocketEndPoint");
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            log.debug("IOException ignored: {}", e.getMessage());
        } catch (NumberFormatException e) {
            log.debug("NumberFormatException ignored: {}", e.getMessage());
        }
    }

    public void run() {
        try {
            while (true) {
                Socket client = socket.accept();
                Properties config = new ChainableProperties(server.getConfig());
                Runnable runnable = createRunnable(client, config);
                if (resolveHostName) {
                    // after resolving, the host name appears Socket.toString.
                    InetAddress clientAddress = client.getInetAddress();
                    clientAddress.getHostName();
                }
                if (log.isDebugEnabled()) {
                    log.debug("Connection from: " + client.toString());
                }
                server.post(runnable);
            }
        } catch (IOException e) {
            log.debug("IOException ignored: {}", e.getMessage());
        }
    }

    private String getHost(Socket socket) {
        String host = server.getProperty("host");
        if (host != null) return host;
        return socket.getLocalAddress().getHostName();
    }

    protected String getProtocol() {
        return "http";
    }

    protected Runnable createRunnable(Socket client, Properties config) throws IOException {
        ConnectionRunnable runnable = new ConnectionRunnable(server, getProtocol(), client, config);
        return runnable;
    }

    public void shutdown(Server server) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
