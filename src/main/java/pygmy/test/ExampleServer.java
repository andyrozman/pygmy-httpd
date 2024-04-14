package pygmy.test;

import pygmy.core.Server;
import pygmy.core.UrlRule;
import pygmy.core.ServerSocketEndPoint;
import pygmy.handlers.FileHandler;
import pygmy.handlers.ResourceHandler;
import pygmy.handlers.BasicWebAuthHandler;
import pygmy.handlers.PrintHandler;

import java.io.File;
import java.io.IOException;

public class ExampleServer {

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.add("http", new ServerSocketEndPoint( 8080 ) );
        server.add( new UrlRule("/web"), new ResourceHandler("/pygmy/web") );
        server.add( new UrlRule("/upload"), new PrintHandler() );
        server.add( new UrlRule("/"), new BasicWebAuthHandler("Admin", new File("users.rlm"), new FileHandler( new File( "/Users/charlie/web" ), "upload.html" ) ) );
        server.start();
        System.in.read();
        server.shutdown();
    }
}
