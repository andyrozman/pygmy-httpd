package pygmy.handlers;

import pygmy.core.*;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.HttpURLConnection;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * <p>
 * This handler implements the Basic web authentication protocol outlined in RFC 2617.  This handler sits in front
 * of a set of a Handler to protect it from unauthorized access.  If you need to protect several you can use
 * {@link pygmy.handlers.DefaultChainHandler}.  Everything it's in front of will be protected.
 * </p>
 * <p>This handler requires a file containing all of the known users.  You can create a file by running this
 * class' {@link #main(String[])} method.
 * <p>
 * Here is the syntax for running this class to create a password file:
 * </p>
 * <div class="code">
 * java pgymy.handlers.BasicWebAuthHandler <i>&lt;file&gt; &lt;username&gt; &lt;password&gt;</i>
 * </div>
 * <p>
 * An existing file can be added to by calling this program again.
 * </p>
 */

public class BasicWebAuthHandler extends AbstractHandler implements Handler {
    private static final Logger log = Logger.getLogger( BasicWebAuthHandler.class.getName() );
    private Properties users;
    private File usersFile;
    private String realm;
    private Handler delegate;

    /**
     * Creates a Handler that makes sure connections are authenticated before the delegate is allowed
     * to process them.
     *
     * @param realm This is the realm reported to the client.  See RFC 2617 for explanation of the realm parameter.
     * @param usersFile  This the path to a file containing all the users and their passwords
     * allowed to access this url. To create a file you can run this class and hand it the file, username,
     * and password to create.  <b>WARNING</b> do not put this file in a place where it could be
     * downloaded through this server.
     * @param delegate the Handler you want to require authentication on.
     */
    public BasicWebAuthHandler(String realm, File usersFile, Handler delegate) {
        this.realm = realm;
        this.usersFile = usersFile;
        this.delegate = delegate;
    }

    public boolean start(Server server) {
        super.start(server );
        this.users = new Properties();
        return loadProperties() && delegate.start( server );
    }

    private boolean loadProperties() {
        InputStream is;
        try {
            is = new BufferedInputStream( new FileInputStream( usersFile ) );
            users.load( is );
            is.close();
            return true;
        } catch (IOException e) {
            log.error( "loadProperties failed due to IOException.", e );
            return false;
        }
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        String auth = request.getRequestHeader("Authorization");
        if( auth == null ) {
            return askForAuthorization(request, response);
        }
        int index = auth.indexOf(" ");
        if( index < -1 ) {
            return askForAuthorization( request, response );
        }
        auth = auth.substring( index + 1 );
        BASE64Decoder decoder = new BASE64Decoder();
        auth = new String( decoder.decodeBuffer( auth ) );
        String[] credentials = auth.split(":");
        try {
            if( !users.containsKey( credentials[0]) || !isPasswordVerified( credentials ) ) {
                log.severe( "Access denied for user " + credentials[0] );
                return askForAuthorization( request, response );
            }
        } catch (NoSuchAlgorithmException e) {
            log.error( "Authorization failed due to NoSuchAlgorithmException.", e );
            response.sendError( HttpURLConnection.HTTP_INTERNAL_ERROR, Http.getStatusPhrase( HttpURLConnection.HTTP_INTERNAL_ERROR ) );
            return true;
        }
        return delegate.handle( request, response );
    }

    private boolean isPasswordVerified( String[] credentials ) throws NoSuchAlgorithmException {
        return hashPassword( credentials[1] ).equals( users.getProperty( credentials[0] ) );
    }

    private boolean askForAuthorization(HttpRequest request, HttpResponse response) {
        response.addHeader( "WWW-Authenticate", "Basic realm=\"" + realm + "\"" );
        response.sendError( HttpURLConnection.HTTP_UNAUTHORIZED, Http.getStatusPhrase( HttpURLConnection.HTTP_UNAUTHORIZED ));
        return true;
    }

    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5password = md5.digest( password.getBytes() );
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode( md5password );
    }

    /**
     * Use this method from the command line to create a user file.
     *
     * @param args the filename, username, and user's password.
     * @throws IOException if some IO error occurs in writing or reading this file.
     * @throws NoSuchAlgorithmException if the JVM doesn't have the MD5 hash algorithm needed to create the file.
     */
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if( args.length < 3 ) {
            System.out.println("Usage: BasicWebAuthHandler <file> <user> <password>");
            return;
        }

        File userFile = new File(args[0]);
        Properties users = new Properties();
        if( userFile.exists() ) {
            InputStream is = new BufferedInputStream( new FileInputStream( userFile ) );
            users.load( is );
            is.close();
        }

        System.out.println("Creating hash for " + args[1]);
        users.setProperty( args[1], hashPassword( args[2]) );

        System.out.println("Writing password for " + args[1]);
        OutputStream os = new BufferedOutputStream( new FileOutputStream( userFile ) );
        users.store( os, "" );
        os.flush();
        os.close();
        System.out.println("done");
    }

    public boolean shutdown(Server server) {
        return delegate.shutdown( server );
    }
}
