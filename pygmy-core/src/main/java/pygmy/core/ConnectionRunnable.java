package pygmy.core;

import lombok.extern.slf4j.Slf4j;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.Properties;

@Slf4j
public class ConnectionRunnable implements Runnable {

    protected Server server;
    protected Socket connection;
    protected Properties config;
    protected String scheme;

    public ConnectionRunnable(Server aServer, String aScheme, Socket aConnection, Properties aConnectionConfig) {
        this.scheme = aScheme;
        this.server = aServer;
        this.connection = aConnection;
        this.config = aConnectionConfig;
    }

    public void run() {
        try {
            boolean next = false;
            do {
                HttpRequest request = createRequest();
                if (!connection.isClosed() && request.readRequest(connection.getInputStream())) {
                    HttpResponse response = new HttpResponse(request, connection.getOutputStream(), server.getResponseListeners());
                    if (log.isInfoEnabled()) {
                        log.info(connection.getInetAddress().getHostAddress() + ":" + connection.getPort() + " - " + request.getUrl());
                    }
                    if (!server.post(request, response)) {
                        response.sendError(HttpURLConnection.HTTP_NOT_FOUND, " was not found on this server.");
                    }
                    next = response.isKeepAlive();
                    if (!next) {
                        log.info("Closing connection.");
                        response.addHeader("Connection", "close");
                    }
                    response.commitResponse();
                } else {
                    log.info("No request sent.  Closing connection.");
                    next = false;
                }
            } while (next);
        } catch (EOFException eof) {
            log.debug("Closing connection");
            // do nothing
        } catch (InterruptedIOException io) {
            log.debug("Client timed out.  Closing connection.");
        } catch (IOException e) {
            log.warn("IOException", e);
        } catch (Exception e) {
            log.warn("Handler threw an exception.", e);
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
            }
        }
    }

    protected HttpRequest createRequest() throws IOException {
        return new HttpRequest(scheme, connection, config);
    }
}
