package pygmy.handlers;


import pygmy.core.*;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * <p>
 * This Handler prints out the incoming Http Request, and echos it back as plain text.  This is great for debugging
 * request being submitted to the server.  Past though this it has little production use.  It does not filter
 * by URL so if it is called it short circuits any other handler down stream.  If it is installed at the head of a chain
 * no other handlers in the chain will be called.
 * </p>
 */
public class PrintHandler extends AbstractHandler implements Handler {

    public boolean handle(Request aRequest, Response aResponse) throws IOException {
        if( aRequest instanceof HttpRequest ) {
            HttpRequest request = (HttpRequest)aRequest;
            HttpResponse response = (HttpResponse) aResponse;
            StringBuffer buffer = new StringBuffer();

            buffer.append( request.toString() );
            buffer.append("\r\n");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            request.getHeaders().print( new InternetOutputStream( baos ) );
            if( request.getPostData() != null ) {
                baos.write( request.getPostData() );
            }
            buffer.append( baos.toString("UTF-8") );
            response.setMimeType("text/plain");
            PrintWriter out = response.getPrintWriter();
            out.write( buffer.toString() );
            return true;
        }
        return false;
    }

}
