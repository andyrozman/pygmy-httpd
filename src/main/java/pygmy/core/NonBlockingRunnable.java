package pygmy.core;

import lombok.extern.slf4j.Slf4j;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;

@Slf4j
public class NonBlockingRunnable implements Runnable {

    Server server;
    Socket socket;
    InputStream is;
    OutputStream os;

    public NonBlockingRunnable(Server server, Socket aSocket, InputStream anIn, OutputStream anOut) {
        this.server = server;
        socket = aSocket;
        is = anIn;
        os = anOut;
    }

    public void run() {
        try {
            boolean next = false;
            do {
                HttpRequest request = new HttpRequest("http", socket, server.getConfig());
                next = request.readRequest(is);
                if (next) {
                    HttpResponse response = new HttpResponse(request, os, server.getResponseListeners());
                    if (!server.post(request, response)) {
                        response.sendError(HttpURLConnection.HTTP_NOT_FOUND, " was not found on this server.");
                    }
                    next = response.isKeepAlive();
                    if (!next) {
                        response.addHeader("Connection", "close");
                    }
                    response.commitResponse();
                }
            } while (next);
        } catch (EOFException eof) {
            log.debug("Closing connection. EOF Exception: {}", eof.getMessage());
            // do nothing
        } catch (IOException e) {
            log.error("IOException", e);
        } catch (Exception e) {
            log.warn("Handler threw an exception.", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }

            try {
                os.close();
            } catch (IOException e) {
            }
        }
    }
}
