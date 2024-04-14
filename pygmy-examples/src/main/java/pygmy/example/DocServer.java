package pygmy.example;

import pygmy.core.Server;
import pygmy.core.UrlRule;
import pygmy.core.ServerSocketEndPoint;
import pygmy.handlers.ResourceHandler;

import java.io.IOException;

/**
 * Quick example to demonstrate some of pygmy's features.  This server is used to server up
 * the project documentation.
 */
public class DocServer {

    public static void main(String[] args) throws IOException {
        int port = 80;
        if( args.length > 0 ) {
            port = Integer.parseInt( args[0] );
        }
        Server server = new Server();
        server.add("http", new ServerSocketEndPoint(port) );
        server.add( new UrlRule("/"), new ResourceHandler("/doc") );
        server.start();
        System.out.println("Press Any Key to Stop.");
        System.in.read();
        server.shutdown();
    }
}
