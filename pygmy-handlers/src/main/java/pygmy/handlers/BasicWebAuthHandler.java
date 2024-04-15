package pygmy.handlers;

import lombok.extern.slf4j.Slf4j;
import pygmy.core.*;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * <p>
 * This handler implements the Basic web authentication protocol outlined in RFC 2617.  This handler sits in front
 * of a set of handlers within a chain see {@link pygmy.handlers.DefaultChainHandler}.  Everything it's in front of
 * will be protected.  It checks it's url-prefix configuration to know when it should handle a request.  Properties used
 * for configuring:
 * </p>
 *
 * <table class="inner">
 * <tr class="header"><td>Parameter Name</td><td>Explanation</td><td>Default Value</td><td>Required</td></tr>
 * <tr class="row"><td>url-prefix</td><td>The prefix to filter request urls.</td><td>None</td><td>Yes</td></tr>
 * <tr class="altrow"><td>realm</td><td>This is the realm reported to the client.  See RFC 2617 for explanation of the realm parameter.</td><td>None</td><td>Yes</td></tr>
 * <tr class="row"><td>users</td><td>This the path to a file containing all the users and their passwords allowed to access this url.
 * To create a file you can run this class and hand it the file, username, and password to create.  <b>WARNING</b> do
 * not put this file in a place where it could be downloaded through this server.</td><td>None</td><td>Yes</td></tr>
 * </table>
 *
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
@Slf4j
public class BasicWebAuthHandler extends AbstractHandler implements Handler {

    private Properties users;

    public static final ConfigOption REALM_OPTION = new ConfigOption("realm", "", "The default realm to authenticate against.");
    public static final ConfigOption USERS_OPTION = new ConfigOption("users", true, "The file used to authenticate users.");

    public boolean initialize(String handlerName, Server server) {
        super.initialize(handlerName, server);
        this.users = new Properties();
        return loadProperties();
    }

    private boolean loadProperties() {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(USERS_OPTION.getProperty(server, handlerName)));
            users.load(is);
            is.close();
            return true;
        } catch (IOException e) {
            log.error("loadProperties failed due to IOException. {}", e.getMessage());
            return false;
        }
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        String auth = request.getRequestHeader("Authorization");
        if (auth == null) {
            return askForAuthorization(request, response);
        }
        int index = auth.indexOf(" ");
        if (index < -1) {
            return askForAuthorization(request, response);
        }
        auth = auth.substring(index + 1);
        BASE64Decoder decoder = new BASE64Decoder();
        auth = new String(decoder.decodeBuffer(auth));
        String[] credentials = auth.split(":");
        try {
            if (!users.containsKey(credentials[0]) || !isPasswordVerified(credentials)) {
                log.error("Access denied for user " + credentials[0]);
                return askForAuthorization(request, response);
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("Authorization failed due to NoSuchAlgorithmException.", e);
            response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR, Http.getStatusPhrase(HttpURLConnection.HTTP_INTERNAL_ERROR));
            return true;
        }
        return false;
    }

    private boolean isPasswordVerified(String[] credentials) throws NoSuchAlgorithmException {
        return hashPassword(credentials[1]).equals(users.getProperty(credentials[0]));
    }

    private boolean askForAuthorization(HttpRequest request, HttpResponse response) {
        String realm = REALM_OPTION.getProperty(server, handlerName);

        response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.sendError(HttpURLConnection.HTTP_UNAUTHORIZED, Http.getStatusPhrase(HttpURLConnection.HTTP_UNAUTHORIZED));
        return true;
    }

    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5password = md5.digest(password.getBytes());
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(md5password);
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length < 3) {
            System.out.println("Usage: BasicWebAuthHandler <file> <user> <password>");
            return;
        }

        File userFile = new File(args[0]);
        Properties users = new Properties();
        if (userFile.exists()) {
            InputStream is = new BufferedInputStream(new FileInputStream(userFile));
            users.load(is);
            is.close();
        }

        System.out.println("Creating hash for " + args[1]);
        users.setProperty(args[1], hashPassword(args[2]));

        System.out.println("Writing password for " + args[1]);
        OutputStream os = new BufferedOutputStream(new FileOutputStream(userFile));
        users.store(os, "");
        os.flush();
        os.close();
        System.out.println("done");
    }

    public boolean shutdown(Server server) {
        return false;
    }
}
