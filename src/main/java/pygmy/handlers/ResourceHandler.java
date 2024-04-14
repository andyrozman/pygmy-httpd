package pygmy.handlers;

import pygmy.core.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * <p>
 * This serves up files from the class path.  Usually this is used to serve files from within a jar file that is
 * within the classpath of the application running.  However, you could serve up files from anywhere in the classpath,
 * no just jar files.  This makes it easy to package part of your web content inside a jar file for easy distribution,
 * but make it transparently available without the user needing to extract the content from the jar file.  This is
 * great for static or default content that you don't want users to have to manage.  By putting a FileHandler and a
 * ResourceHandler in a chain filtering on the same url-prefix, you can allow users to override or augment content
 * from the jar file by placing the same file on the file system, but load the default from the jar file if the file
 * not on the file system.
 * </p>
 *
 */
public class ResourceHandler extends AbstractHandler implements Handler {
    private static final Logger log = Logger.getLogger( ResourceHandler.class.getName() );

    private String resourceMount;
    private String defaultResource;

    public ResourceHandler(String resourceMount) {
        this( resourceMount, "index.html");
    }

    public ResourceHandler(String resourceMount, String defaultResource) {
        this.resourceMount = resourceMount;
        this.defaultResource = defaultResource;
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        String resource = Http.join( resourceMount, request.getUrlMatch().getTrailing() );
        if( resource.endsWith("/") ) {
            resource += defaultResource;
        } else if( resource.lastIndexOf('.') < 0 ) {
            resource += "/" + defaultResource;
        }
        if( log.isLoggable( Level.INFO ) ) {
            log.info( "Loading resource: " + resource );
        }
        String mimeType = getMimeType( resource );
        InputStream is = getClass().getResourceAsStream( resource );

        if( mimeType == null || is == null ) {
            log.warn( "Resource was not found or the mime type was not understood. (Found file=" + (is != null) + ") (Found mime-type=" + ( mimeType != null ) + ")" );
            if( is != null ) is.close();
            return false;
        }
        response.setMimeType( mimeType );
        response.sendResponse( is, -1 );
        return true;
    }

}
