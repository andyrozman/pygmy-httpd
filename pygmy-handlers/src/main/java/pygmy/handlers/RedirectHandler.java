package pygmy.handlers;

import lombok.extern.slf4j.Slf4j;
import pygmy.core.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * This redirects or rewrites URLs based on a regular expression.  It tests the requested
 * URLs against a regular expression.  If it finds a match it then uses the substiution
 * expression to rewrite a new URL.  For a description of the regular expression language see
 * {@see http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html}.  This handler
 * operates in two modes either using external redirects (i.e. 302 HTTP code), or internal
 * redirects.  The new URL expression can reference groups in the regular expression using
 * ${&lt;group number&gt;}.  Substitution expressions can also reference configuration
 * properties by using the notation.  For example, ${http.port} would return the port of
 * the pygmy server.
 * </p>
 *
 * <p>
 * Here is an example of a URL rule and substition expression for creating a URL to user's
 * home directories.  Remember to escape &quote;\&quote; character in your properties files
 * otherwise your expression will not work.  To help you debug these problems this Handler
 * will log a message at the debug level so you can see what the regular expression has been
 * set to.
 * </p>
 *
 * <blockquote>
 * aRedirect.class=pygmy.handlers.RedirectHandler
 * aRedirect.rule=/~(\\w+)
 * aRedirect.subst=/home/${1}/public_html
 * </blockquote>
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
 * <p>
 * <table class="inner">
 * <tr class="header"><td>Parameter Name</td><td>Explanation</td><td>Default Value</td><td>Required</td></tr>
 * <tr class="row"><td>rule</td><td>The regular expression rule to use for matching on the requested URL.</td><td>None</td><td>Yes</td></tr>
 * <tr class="altrow"><td>subst</td><td>The string to use for rewriting a new URL that will be used in another request.</td><td>None</td><td>Yes</td></tr>
 * <tr class="row"><td>useInternal</td><td>Indicates the new URL will be internally redirected.
 * If it is true, then the new URL will be used internally redirected.
 * If false, then the new URL will be sent back to the client with the HTTP code specified by redirectCode.</td><td>false</td><td>No</td></tr>
 * <tr class="altrow"><td>redirectCode</td><td>This defines the HTTP code that will be sent back when we substitue or rewrite a URL.</td><td>302</td><td>No, but ignored if useInternal is true.</td></tr>
 * </table>
 * </p>
 */
@Slf4j
public class RedirectHandler extends AbstractHandler {

    public static final ConfigOption RULE_OPTION = new ConfigOption("rule", true, "Regular expression for matching URLs.");
    public static final ConfigOption SUBST_OPTION = new ConfigOption("subst", true, "The substiution expression to re-writing the new URL.");
    public static final ConfigOption INTERNAL_OPTION = new ConfigOption("useInternal", "false", "Internal redirect without sending a response.");
    public static final ConfigOption REDIRECT_CODE_OPTION = new ConfigOption("redirectCode", "302", "The HTTP code to send back to the client when the URL matches the rule.");

    Pattern rule;
    String substitution;
    boolean isInternalRedirect;
    int redirectHttpCode = HttpURLConnection.HTTP_MOVED_TEMP;

    public boolean initialize(String handlerName, Server server) {
        try {
            super.initialize(handlerName, server);

            rule = Pattern.compile(RULE_OPTION.getProperty(server, handlerName), Pattern.CASE_INSENSITIVE);
            substitution = SUBST_OPTION.getProperty(server, handlerName);
            isInternalRedirect = INTERNAL_OPTION.getBoolean(server, handlerName).booleanValue();
            try {
                redirectHttpCode = REDIRECT_CODE_OPTION.getInteger(server, handlerName).intValue();
            } catch (NumberFormatException e) {
                log.warn("redirectCode was not a number!  Defaulting to " + redirectHttpCode);
            }

            if (log.isDebugEnabled()) {
                log.debug("Rule=" + rule.pattern() + ",subst=" + substitution + ",useInternal=" + isInternalRedirect + ",redirectCode=" + redirectHttpCode);
            }
            return true;
        } catch (IllegalArgumentException e) {
            log.error("IllegalArgumentException: {}", e.getMessage());
            return false;
        }
    }

    protected boolean isRequestdForHandler(HttpRequest request) {
        return !request.isInternal();
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        Matcher urlMatch = rule.matcher(request.getUrl());
        StringBuffer buffer = null;
        if (urlMatch.find()) {
            if (buffer == null) {
                buffer = new StringBuffer(substitution);
            }
            int lastIndex = 0;
            do {
                lastIndex = replaceGroupInSubst(buffer, urlMatch);
            } while (lastIndex < buffer.length());

            if (isInternalRedirect) {
                return server.post(new HttpRequest(buffer.toString(), server.getConfig(), true), response);
            } else {
                response.setStatusCode(redirectHttpCode);
                response.addHeader("Location", buffer.toString());
                return true;
            }
        } else {
            return false;
        }
    }

    private int replaceGroupInSubst(StringBuffer buffer, Matcher urlMatch) {
        int index = buffer.indexOf("${");
        if (index >= 0) {
            int endIndex = substitution.indexOf("}");
            String reference = substitution.substring(index + 2, endIndex);
            String subst = null;
            if (Character.isDigit(reference.charAt(0))) {
                int group = Integer.parseInt(reference);
                subst = urlMatch.group(group);
            } else {
                subst = server.getProperty(subst);
            }
            if (subst != null) {
                buffer.replace(index, endIndex + 1, subst);
            }
            return endIndex + 1;
        } else {
            return buffer.length();
        }
    }
}
