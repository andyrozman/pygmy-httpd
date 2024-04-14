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
 */
@Slf4j
public class ServerSocketEndPoint implements EndPoint, Runnable {

    protected ServerSocketFactory factory;
    protected ServerSocket socket;
    protected Server server;
    protected String endpointName;
    protected boolean resolveHostName = false;
    protected int port = 80;
    protected int clientTimeout = 5000;

    public ServerSocketEndPoint() {
        factory = ServerSocketFactory.getDefault();
    }

    public ServerSocketEndPoint(int port) {
        this();
        this.port = port;
    }

    public ServerSocketEndPoint resolveHostName(boolean enabled) {
        resolveHostName = enabled;
        return this;
    }

    public ServerSocketEndPoint port(int port) {
        this.port = port;
        return this;
    }

    public ServerSocketEndPoint clientTimeout(int timeout) {
        this.clientTimeout = timeout;
        return this;
    }

    protected ServerSocket createSocket(int port) throws IOException {
        return factory.createServerSocket(port);
    }

    public void start(Server server) throws IOException {
        try {
            this.server = server;
            this.socket = createSocket(port);
            log.info("Socket listening on port " + socket.getLocalPort());
            Thread thread = new Thread(this, endpointName + "[" + socket.getLocalPort() + "] ServerSocketEndPoint");
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            log.debug("IOException ignored: {}", e.getMessage());
        } catch (NumberFormatException e) {
            log.debug("NumberFormatException ignored, {}", e.getMessage());
        }
    }

    public void run() {
        try {
            while (true) {
                Socket client = socket.accept();
                Properties config = new ChainableProperties(server.getConfig());
                client.setSoTimeout(clientTimeout);
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
            log.debug("IOException ignored. Ex.: {}", e.getMessage());
        }
    }

    private String getHost(Socket socket) {
        String host = server.getProperty("host");
        if (host != null) return host;
        return socket.getLocalAddress().getHostName();
    }

    public InetAddress getAddress() {
        return socket.getInetAddress();
    }

    public int getPort() {
        return port;
    }

    protected String getProtocol() {
        return "http";
    }

    protected Runnable createRunnable(Socket client, Properties config) throws IOException {
        return new ConnectionRunnable(server, getProtocol(), client, config);
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
