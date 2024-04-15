package pygmy.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This object represents an instance of a url that matched an instance of {@link pygmy.core.UrlRule}.
 * It encapsulates all of the specifics values of variables default in the UrlRule as well as any
 * trailing content after the rule.
 */
public class UrlMatch {
    private UrlRule rule;
    private Map<String, String> values = new HashMap<String, String>();
    private String trailing;

    /**
     * Create a new instace of a match against the given {@link pygmy.core.UrlRule}.
     *
     * @param rule the rule that matched against a url.
     */
    public UrlMatch(UrlRule rule) {
        this.rule = rule;
        values.putAll(this.rule.getDefaults());
    }

    /**
     * The rule that created this match.
     *
     * @return the {@link pygmy.core.UrlRule} that created this match.
     */
    public UrlRule getRule() {
        return rule;
    }

    /**
     * Returns the variables value given it's name.
     *
     * @param variable the variable's name
     * @return the value of the variable, or it's default value if one was provided in the {@link UrlRule}.
     */
    public String get(String variable) {
        return values.get(variable);
    }

    /**
     * Binds a value to a given variable.
     *
     * @param variable the name of the variable
     * @param value    it's value.
     */
    public void put(String variable, String value) {
        values.put(variable, value);
    }

    /**
     * This returns a Map all of the variables and their values.
     *
     * @return the Map of variable name to values.
     */
    public Map<String, String> getValues() {
        return values;
    }

    /**
     * Return an iterator across all of the variables in this match object.
     *
     * @return an Iterator over the variables.
     */
    public Iterator<String> keys() {
        return values.keySet().iterator();
    }

    /**
     * Returns any trailing portion of the url that was not apart of the UrlRule.  For example,
     * say the UrlRule was "/".  And it was matched against the url "/foobar/baz".  This would
     * return foobar/baz.  This makes it easier to write rules for matching heirarchial urls.
     *
     * @return the trailing portion not directly matched by this UrlRule.
     */
    public String getTrailing() {
        return trailing;
    }

    public void setTrailing(String trailing) {
        this.trailing = trailing;
    }

}
