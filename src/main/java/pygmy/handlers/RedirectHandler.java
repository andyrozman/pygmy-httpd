package pygmy.handlers;

import pygmy.core.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * <p>
 * This redirects or rewrites URLs based on a regular expression.  It tests the requested
 * URLs against a regular expression.  If it finds a match it then uses the substiution
 * expression to rewrite a new URL.  For a description of the regular expression language see
 * {@see http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html}.  This handler
 * operates in two modes either using external redirects (i.e. 302 HTTP code), or internal
 * redirects.  The new URL expression can reference groups in the regular expression using
 * ${&lt;group number&gt;}.  Substitution expressions can also reference configuration
 * properties by using the notation.
 * </p>
 *
 * <p>
 * Here is an example of a URL rule and substition expression for creating a URL to user's
 * home directories.  To help you debug these problems this Handler will log a message at
 * the debug level so you can see what the regular expression has been set to.
 * </p>
 *
 * <div class="code">
 * ResourceHandler handler new RedirectHandler("/~(\\w+)", "/home/${1}/public_html");
 * </div>
 *
 * <p>
 * The new URL built from the subst expression by default will be sent back to the client
 * in a 302 HTTP status code.  But, there are cases when you don't want to expose the
 * rewritten URL to the outside world.  This handler can internally redirect so that the
 * new URL won't be sent back to the client.  In our example above we might want to keep
 * the URL to the user's directory private.  Using internal redirects the browser won't see
 * the new URL containing: /home/chuck/public_html.  It could be a serious security hole if
 * someone is allowed to request /home/chuck!
 * </p>
 *
 * <p>
 * RedirectHandler <b>only</b> responds to non-internal requests.  <b>This handler will not
 * redirect or rewrite interal requests.</b>  This is so redirects don't get into an
 * infinite loop when processing.  Something to look out for when using external redirects.
 * Most clients fail if they are redirected too many times.
 * </p>
 *
 */
public class RedirectHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger( RedirectHandler.class.getName() );

    Pattern rule;
    String substitution;
    boolean isInternalRedirect;
    int redirectHttpCode = HttpURLConnection.HTTP_MOVED_TEMP;

    public RedirectHandler(String rule, String substitution) {
        this( rule, substitution, false);
    }

    public RedirectHandler(String rule, String substitution, boolean internalRedirect) {
        this.rule = Pattern.compile( rule, Pattern.CASE_INSENSITIVE );
        this.substitution = substitution;
        isInternalRedirect = internalRedirect;
    }

    public RedirectHandler redirectHttpCode( int code ) {
        redirectHttpCode = code;
        return this;
    }

    public boolean start(Server server) {
        super.start(server);
        if( log.isLoggable( Level.FINE ) ) {
            log.fine( "Rule=" + rule.pattern() + ",subst=" + substitution + ",useInternal=" + isInternalRedirect + ",redirectCode=" + redirectHttpCode);
        }
        return true;
    }

    protected boolean isRequestdForHandler(HttpRequest request) {
        return !request.isInternal();
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        Matcher urlMatch = rule.matcher( request.getUrl() );
        StringBuilder buffer = new StringBuilder( substitution );
        if(urlMatch.find()) {
            int lastIndex = 0;
            do {
                lastIndex = replaceGroupInSubst(buffer, urlMatch);
            } while( lastIndex < buffer.length() );

            if( isInternalRedirect ) {
                return server.post( new HttpRequest( buffer.toString(), server.getConfig(), true), response );
            } else {
                response.setStatusCode( redirectHttpCode );
                response.addHeader("Location", buffer.toString());
                return true;
            }
        } else {
            return false;
        }
    }

    private int replaceGroupInSubst(StringBuilder buffer, Matcher urlMatch) {
        int index = buffer.indexOf("${");
        if( index >= 0 ) {
            int endIndex = substitution.indexOf("}");
            String reference = substitution.substring( index + 2, endIndex );
            String subst = null;
            if( Character.isDigit( reference.charAt(0) ) ) {
                int group = Integer.parseInt( reference );
                subst = urlMatch.group( group );
            } else {
                subst = server.getProperty( subst );
            }
            if( subst != null ) {
                buffer.replace( index, endIndex+1, subst );
            }
            return endIndex + 1;
        } else {
            return buffer.length();
        }
    }
}
