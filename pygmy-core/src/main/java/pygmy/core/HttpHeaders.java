package pygmy.core;

import java.io.IOException;
import java.util.*;

public class HttpHeaders {
    public static final String CONNECTION = "Connection";

    private Map<String, String> map;
    private Integer contentLength = null;
    private String charset = "ISO-8859-1";
    private String contentType = null;

    public HttpHeaders() {
        this.map = new LinkedHashMap<String, String>();
    }

    public HttpHeaders(InternetInputStream stream) throws IOException {
        this();
        this.map = stream.readHeader();
        Map<String, String> options = getHeaderOptions("Content-Type");
        contentType = options.get("Content-Type");
        if (options.containsKey("charset")) {
            charset = options.get("charset");
        }
    }

    public static List<Map<String, String>> getHeaderListOptions(Map<String, String> headers, String key) {
        String value = headers.get(key);
        if (value != null) {
            List<Map<String, String>> options = new ArrayList<Map<String, String>>();
            String[] valuesSplit = value.split(",");
            for (String v : valuesSplit) {
                options.add(parseForOptions(key, v));
            }
            return options;
        } else {
            return Collections.emptyList();
        }
    }

    public static Map<String, String> getHeaderOptions(Map<String, String> headers, String key) {
        String value = headers.get(key);
        if (value != null) {
            return parseForOptions(key, value);
        } else {
            return Collections.emptyMap();
        }
    }

    private static Map<String, String> parseForOptions(String key, String value) {
        String[] valueSplit = value.split(";");
        Map<String, String> options = new HashMap<String, String>();
        options.put(key, valueSplit[0]);
        for (int i = 1; i < valueSplit.length; i++) {
            String option = valueSplit[i];
            String[] pair = option.split("=");
            if (pair.length == 2) {
                // only do this if there are two values.
                String v = pair[1].trim();
                if (v.startsWith("\"") && v.endsWith("\"")) v = v.substring(1, v.length() - 1);
                options.put(pair[0].trim(), v);
            }
        }
        return options;
    }

    public Map<String, String> getHeaderOptions(String key) {
        return HttpHeaders.getHeaderOptions(map, key);
    }

    public List<Map<String, String>> getHeaderListOptions(String key) {
        return HttpHeaders.getHeaderListOptions(map, key);
    }

    public String get(String key) {
        return map.get(key);
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        return (value == null) ? defaultValue : value;
    }

    public void put(String key, String value) {
        map.put(key, value);
    }

    public boolean contains(String headerKey) {
        return map.containsKey(headerKey);
    }

    public void clear() {
        map.clear();
    }

    public Iterator iterator() {
        return map.keySet().iterator();
    }

    public void print(InternetOutputStream stream) throws IOException {
        for (Iterator i = iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            stream.println(key + ": " + get(key));
        }

        stream.println();
        stream.flush();
    }

    public Integer getContentLength() {
        if (contentLength == null && map.containsKey("Content-Length")) {
            contentLength = Integer.parseInt(map.get("Content-Length"));
        }
        return contentLength;
    }

    public String getCharset() {
        return charset;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isKeepAlive() {
        if ("Keep-Alive".equalsIgnoreCase(get("Connection"))) {
            return true;
        } else if ("close".equalsIgnoreCase(get("Connection"))) {
            return false;
        } else {
            return false;
        }
    }
}
