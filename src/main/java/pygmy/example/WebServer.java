package pygmy.example;

import pygmy.core.Server;
import pygmy.core.ServerSocketEndPoint;
import pygmy.core.UrlRule;
import pygmy.handlers.FileHandler;

import java.io.IOException;

public class WebServer {

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.add("http", new ServerSocketEndPoint(8090));
        server.add(new UrlRule("/"), new FileHandler( args[0] ).allowDirectoryListing(true) );
        server.start();
        System.out.println("Press Any Key to Stop.");
        System.in.read();
        server.shutdown();
    }
}
