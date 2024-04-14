package pygmy.core;

import java.io.IOException;

public abstract class AbstractHandler implements Handler {

    protected Server server;

    public AbstractHandler() {
    }

    public boolean start(Server server) {
        this.server = server;
        return true;
    }

    public boolean handle( Request aRequest, Response aResponse) throws IOException {
        if( aRequest instanceof HttpRequest ) {
            HttpRequest request = (HttpRequest) aRequest;
            HttpResponse response = (HttpResponse) aResponse;
            return handleBody( request, response );
        }
        return false;
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        return false;
    }

    public boolean shutdown(Server server) {
        return true;
    }

    protected String getMimeType( String filename ) {
        int index = filename.lastIndexOf(".");
        String mimeType = "application/octet-stream";
        if( index > 0 ) {
            mimeType = server.getProperty( "mime" + filename.substring( index ).toLowerCase(), mimeType );
        }

        return mimeType;
    }

}
