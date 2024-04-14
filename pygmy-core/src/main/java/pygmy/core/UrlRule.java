package pygmy.core;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UrlRules allow to do some very interesting pattern matching on each request's url
 * to parse out values from the URL and make them available as parameters to your
 * Handlers.  They are a very powerful concept in creating elegant URLs for your
 * applications.  Here is an example of UrlRule with a single variable defined
 * in it.
 *
 * <div class="code">
 * UrlRule rule = new UrlRule( "/blog/${method}" ).defaultTo("method", "read");
 * </div>
 * <p>
 * This UrlRule will match any url starting with blog.  So it matches "/blog",
 * "/blog/post", "/blog/post/2008", etc.  The second path after blog will be
 * stored in the variable method.  If there is no second path in the url
 * method variable will be defaulted to the value of "read".  If a variable
 * does not have a default it is considered required.  So for example:
 *
 * <div class="code">
 * UrlRule rule = new UrlRule( "/blog/${method}" );
 * </div>
 * <p>
 * The above UrlRule will not match "/blog" because no method variable was specified.
 * If you'd like it to default to null.  Then call defaultTo("method", null).
 * <p>
 * You may have as many variables as you wish.  Variables defined in the url will
 * override any values passed in on the query string.  So be aware this is the case,
 * and don't overlap variable names between UrlRule and query strings.
 * <p>
 * In addition to default values you can use validations to validate the values
 * in request urls with rules in the UrlRule.  For example:
 *
 * <div class="code">
 * UrlRule rule = new UrlRule("/blog/date/${year}/${month}")
 *    .variable("year", null, "[0-9]{4,}")
 *    .variable("month", null, "[0-3][0-9]" );
 * </div>
 * <p>
 * In this example we have two variables "year" and "month".  Each have a default value of
 * null (i.e. not required), and they are validate by two regular expressions (2nd argument).
 * In this example we are using the short hand method
 * {@link pygmy.core.UrlRule#variable(String, String, String)}.  This method allows us to
 * specify validate rules and defaults all in one that way we don't specify the variable
 * name twice.  If you want to validate required parameters then you can use
 * {@link pygmy.core.UrlRule#validate(String, String)} instead.
 */
@Slf4j
public class UrlRule {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(\\w+)\\}");

    private String rule;
    private Pattern pattern;
    private Map<Integer, String> variables = new HashMap<Integer, String>();
    private Map<String, String> validations = new HashMap<String, String>();
    private Map<String, String> defaults = new HashMap<String, String>();

    public UrlRule(String rule) {
        this.rule = rule;
    }

    /**
     * This method compiles the underlying regular expression used by the {@link #matches(String)}
     * method.  It's not neccessary for you to call this upfront by default.  {@link #matches(String)}
     * method will do this if a rule hasn't been already compiled.  This method only needs to be
     * called once.
     *
     * @return this for method chaining.
     */
    public UrlRule compile() {
        Matcher matcher = VARIABLE_PATTERN.matcher(rule);

        StringBuilder builder = new StringBuilder();
        int last = 0;
        int group = 1;
        while (matcher.find(last)) {
            builder.append(escape(rule.substring(last, matcher.start())));
            String variable = matcher.group(1);
            variables.put(group, variable);
            if (validations.containsKey(variable)) {
                builder.append("(");
                builder.append(validations.get(variable));
                builder.append(")");
            } else {
                builder.append("(\\w+)");
            }
            if (defaults.containsKey(variable)) {
                builder.append("?");
            }
            last = matcher.end();
            group++;
        }

        if (last < rule.length()) {
            builder.append(escape(rule.substring(last, rule.length())));
        }

        builder.append("(.*)");

        pattern = Pattern.compile(builder.toString());
        return this;
    }

    /**
     * This method allows you to specify default values for variables in your rule.
     *
     * @param key   the name of the url variable.  The text inside the variable tokens ${&lt;variable_name&gt;}.
     * @param value the default value to use.
     * @return this for method chaining.
     */
    public UrlRule defaultTo(String key, String value) {
        defaults.put(key, value);
        return this;
    }

    /**
     * This method allows you to specify the regular expression that will be used to validate the
     * variable's value.  Urls that don't match or validate against this pattern will be consider
     * non-matching urls (i.e. {@link #matches(String)} will return null).
     *
     * @param variable the name of the url variable.  The text inside the variable tokens ${&lt;variable_name&gt;}.
     * @param pattern  the regular expression pattern to be used here.  By default it's \w+ for variables.
     * @return this for method chaining.
     */
    public UrlRule validate(String variable, String pattern) {
        validations.put(variable, pattern);
        return this;
    }

    /**
     * This is a short hand method allowing you to specify the default value and validation pattern
     * all in one method call.  This eliminates the need to specify the variable name twice in a rule.
     *
     * @param variable     the name of the url variable.  The text inside the variable tokens ${&lt;variable_name&gt;}.
     * @param defaultValue the default value to use.
     * @param pattern      the regular expression pattern to be used here.  By default it's \w+ for variables.
     * @return this for method chaining.
     */
    public UrlRule variable(String variable, String defaultValue, String pattern) {
        validations.put(variable, pattern);
        defaults.put(variable, defaultValue);
        return this;
    }

    /**
     * Return a collections of all the variable names detected in this url rule.
     *
     * @return all the variable names defined.
     */
    public Collection<String> getVariables() {
        return variables.values();
    }

    /**
     * Return the default value for a given variable
     *
     * @param variable the name of a variable to find defaults for.
     * @return the default value of a given variable.  null means there is not default value or
     * the default value is null.
     */
    public String getDefault(String variable) {
        return defaults.get(variable);
    }

    /**
     * Return the rule's overall pattern used in the {@link #matches(String)} method to recognize urls.
     *
     * @return the regular expression pattern as a String.
     */
    public String getPattern() {
        return pattern.pattern();
    }

    /**
     * Given a url this method tries to match this rule against that url, and parse out any
     * variables defined by this UrlRule.  If this rule does not match the url null is
     * returned.  Otherwise a {@link pygmy.core.UrlMatch} instance is returned so you can
     * query for variable values, and other parts of the url.
     *
     * @param url the url you wish to match against this rule.
     * @return an instance of {@link pygmy.core.UrlMatch} when this rule matches the given url.  If not null is returned.
     */
    public UrlMatch matches(String url) {
        if (pattern == null) compile();

        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            UrlMatch match = new UrlMatch(this);
            for (int i = 1; i < matcher.groupCount(); i++) {
                String variable = variables.get(i);
                match.put(variable, matcher.group(i));
            }
            match.setTrailing(matcher.group(matcher.groupCount()));
            return match;
        } else {
            return null;
        }
    }

    private String escape(String value) {
        return value.replaceAll("\\.", "\\.").replaceAll("\\:", "\\:");
    }

    public Map<String, String> getDefaults() {
        return defaults;
    }

    public static void main(String[] args) {
        UrlRule rule = new UrlRule("/hello/${controller}.${format}/world").defaultTo("controller", "home").variable("format", "html", "xml|json|html");
        UrlRule home = new UrlRule("/${controller}/${action}").defaultTo("controller", "home").defaultTo("action", "welcome");
        UrlRule f = new UrlRule("/");


        printMatch(rule, "/hello/foobar.xml/world");
        printMatch(home, "/visitor/howdy");
        printMatch(home, "/visitor/");

        printMatch(f, "/");
        printMatch(f, "/foo.mp3");
        printMatch(f, "/bar/foo.mp3");
        printMatch(f, "/foo/bar/bazz");

        UrlMatch match;

        match = rule.matches("/hello");
        if (match != null) {
            System.out.println(match.get("controller"));
        }
    }

    private static void printMatch(UrlRule rule, String url) {
        UrlMatch match = rule.matches(url);
        log.debug("Pattern: " + rule.getPattern());
        if (match != null) {
            Iterator i = match.keys();
            while (i.hasNext()) {
                String key = (String) i.next();
                log.debug(key + "=" + match.get(key));
            }
            log.debug("Trailing: " + match.getTrailing());
        }
        //System.out.println();
    }

}
