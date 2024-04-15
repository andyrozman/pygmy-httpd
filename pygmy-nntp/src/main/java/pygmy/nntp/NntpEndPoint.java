package pygmy.nntp;

import pygmy.core.ServerSocketEndPoint;
import pygmy.core.ConfigOption;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

public class NntpEndPoint extends ServerSocketEndPoint {
    private static final Logger log = Logger.getLogger( NntpEndPoint.class.getName() );

    private static final ConfigOption POSTING_OPTION = new ConfigOption( "posting", "true", "Enable posting");

    public NntpEndPoint() {
    }

    protected Runnable createRunnable(Socket client, Properties config) throws IOException {
        return new NntpRunnable( client, config );
    }

    public class NntpRunnable implements Runnable {
        Socket client;
        Properties config;

        public NntpRunnable( Socket client, Properties config ) {
            this.client = client;
            this.config = config;
        }

        public void run() {
            try {
                handleConnection( client );
            } catch( IOException ioe ) {
                log.log( Level.WARNING, "IOException while reading socket.", ioe );
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    log.log( Level.WARNING, "Exception while closing socket.", e );
                }
            }
        }

        private void handleConnection(Socket client) throws IOException {
            NntpRequest request = new NntpRequest( client, config, client.getInputStream()  );
            NntpResponse response = new NntpResponse( client.getOutputStream() );
            sendGreeting( response );
            while( !request.isDone() ) {
                try {
                    request.nextCommand();
                    if( !request.isDone() && !server.post( request, response ) ) {
                        log.log( Level.WARNING, "Command " + request.getCommand() + " not recognized." );
                        response.sendResponse( 500, "command not recognized" );
                    }
                } catch( Exception e ) {
                    log.log( Level.WARNING, "Exception received while handling command.", e );
                    response.sendResponse( 503, "program fault - command not performed" );
                }
            }
            response.sendResponse( 205, "closing connection - goodbye!" );
        }

        private void sendGreeting( NntpResponse response ) throws IOException {
            Boolean canPost = POSTING_OPTION.getBoolean( server, getName() );
            config.put( "posting", canPost.toString() );
            if( canPost.booleanValue() ) {
                response.respondHello( client.getLocalAddress().getHostName() );
            } else {
                response.respondHelloNoPosting( client.getLocalAddress().getHostName() );
            }
        }
    }
}
