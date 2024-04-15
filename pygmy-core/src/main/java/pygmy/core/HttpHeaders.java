package pygmy.core;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpHeaders {
    private Map map;

    public HttpHeaders() {
        this.map = new LinkedHashMap();
    }

    public HttpHeaders(InternetInputStream stream) throws IOException {
        this();
        String currentKey = null;
        while (true) {
            String line = stream.readline();
            if ((line == null) || (line.length() == 0)) {
                break;
            }

            if (!Character.isSpaceChar(line.charAt(0))) {
                int index = line.indexOf(':');
                if (index >= 0) {
                    currentKey = line.substring(0, index).trim();
                    String value = line.substring(index + 1).trim();
                    put(currentKey, value);
                }
            } else if (currentKey != null) {
                String value = get(currentKey);
                put(currentKey, value + "\r\n\t" + line.trim());
            }
        }
    }

    public String get(String key) {
        return (String) map.get(key);
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
}
