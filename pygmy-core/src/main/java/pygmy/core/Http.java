package pygmy.core;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.PermissionCollection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class for HTTP error codes, and other command HTTP tasks.
 */
public class Http {

    private static final HashMap<Integer, String> codesMap = new HashMap<>();

    private static final HashMap<String, String> htmlCharacterEncodings = new HashMap<>();

    private static final SimpleDateFormat dateFormat;

    public static final String CRLF = "\r\n";

    static {
        dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));
        dateFormat.setLenient(true);
    }

    static {
        htmlCharacterEncodings.put("&", "amp");
        htmlCharacterEncodings.put("<", "lt");
        htmlCharacterEncodings.put(">", "gt");
        htmlCharacterEncodings.put("\"", "quot");
        htmlCharacterEncodings.put("'", "apos");
    }

    static {
        codesMap.put(100, "Continue");
        codesMap.put(101, "Switching Protocols");
        codesMap.put(200, "OK");
        codesMap.put(new Integer(201), "Created");
        codesMap.put(new Integer(202), "Accepted");
        codesMap.put(new Integer(203), "Non-Authoritative Information");
        codesMap.put(new Integer(204), "No Content");
        codesMap.put(new Integer(205), "Reset Content");
        codesMap.put(new Integer(206), "Partial Content");
        codesMap.put(new Integer(300), "Multiple Choices");
        codesMap.put(new Integer(301), "Moved Permanently");
        codesMap.put(new Integer(302), "Moved Temporarily");
        codesMap.put(new Integer(303), "See Other");
        codesMap.put(new Integer(304), "Not Modified");
        codesMap.put(new Integer(305), "Use Proxy");
        codesMap.put(new Integer(400), "Bad Request");
        codesMap.put(new Integer(401), "Unauthorized");
        codesMap.put(new Integer(402), "Payment Required");
        codesMap.put(new Integer(403), "Forbidden");
        codesMap.put(new Integer(404), "Not Found");
        codesMap.put(new Integer(405), "Method Not Allowed");
        codesMap.put(new Integer(406), "Not Acceptable");
        codesMap.put(new Integer(407), "Proxy Authentication Required");
        codesMap.put(new Integer(408), "Request Time-out");
        codesMap.put(new Integer(409), "Conflict");
        codesMap.put(new Integer(410), "Gone");
        codesMap.put(new Integer(411), "Length Required");
        codesMap.put(new Integer(412), "Precondition Failed");
        codesMap.put(new Integer(413), "Request Entity Too Large");
        codesMap.put(new Integer(414), "Request-URI Too Large");
        codesMap.put(new Integer(415), "Unsupported Media Type");
        codesMap.put(new Integer(500), "Server Error");
        codesMap.put(new Integer(501), "Not Implemented");
        codesMap.put(new Integer(502), "Bad Gateway");
        codesMap.put(new Integer(503), "Service Unavailable");
        codesMap.put(new Integer(504), "Gateway Time-out");
        codesMap.put(new Integer(505), "HTTP Version not supported");
    }

    /**
     * Given a HTTP response code, this method will return the english phrase.
     *
     * @param code the HTTP response code to look up.
     * @return A string describing what the HTTP response code is.
     */
    public static String getStatusPhrase(int code) {
        String phrase = (String) codesMap.get(new Integer(code));
        if (phrase == null) {
            return "Error";
        }

        return phrase;
    }

    /**
     * This encodes the message into Html.  It encodes characters '&', ''', '<', '>', and '"' into &amp;, &apos;,
     * &lt;, &gt;, and &quot;.
     *
     * @param message the message to encode.
     * @return the encoded message.
     */
    public static String encodeHtml(String message) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < message.length(); i++) {
            String tmp = Character.toString(message.charAt(i));
            if (htmlCharacterEncodings.containsKey(tmp)) {
                result.append("&");
                result.append(htmlCharacterEncodings.get(tmp));
                result.append(";");
            } else {
                result.append(tmp);
            }
        }
        return result.toString();
    }

    /**
     * This creates the current time as a string conforming to a HTTP date following the format
     * <i>EEE, dd MMM yyyy HH:mm:ss z</i>.
     *
     * @return The a HTTP formated string of the current time.
     */
    public static String getCurrentTime() {
        return formatTime(System.currentTimeMillis());
    }

    /**
     * This formats the given time as a HTTP formatted string.  The format is  <i>EEE, dd MMM yyyy HH:mm:ss z</i>.
     *
     * @param time the time to format into a string.
     * @return the formatted date.
     */
    public static String formatTime(long time) {
        return dateFormat.format(new Date(time)).substring(0, 29);
    }

    /**
     * This parses the given time as a HTTP formatted string.
     *
     * @param date The date represented as the format String <i>EEE, dd MMM yyyy HH:mm:ss z</i>.
     * @return the time in milliseconds.
     * @throws ParseException throws this exeception when it cannot parse the date.
     */
    public static long parseTime(String date) throws ParseException {
        return dateFormat.parse(date).getTime();
    }

    /**
     * Takes two paths a joins them together properly adding the '/' charater between them.
     *
     * @param path         the first part of the path.
     * @param relativePath the second part to join to the first path.
     * @return the combined path of path and relativePath.
     */
    public static String join(String path, String relativePath) {
        boolean pathEnds = path.endsWith("/");
        boolean relativeStarts = relativePath.startsWith("/");
        if ((pathEnds && !relativeStarts) || (!relativeStarts && pathEnds)) {
            return path + relativePath;
        } else if (pathEnds && relativeStarts) {
            return path + relativePath.substring(1);
        } else {
            return path + "/" + relativePath;
        }
    }

    /**
     * This translates a url into a path on the filesystem.  This decodes the url using the URLDecoder class, and
     * creates an absolute path beginning with root.
     *
     * @param root this is an absolute path to prepend onto the url.
     * @param url  the url to resolve into the filesystem.
     * @return the a File object representing this URL.
     * @throws UnsupportedEncodingException
     */
    public static File translatePath(String root, String url) throws UnsupportedEncodingException {
        String name = URLDecoder.decode(url, "UTF-8");
        name = name.replace('/', File.separatorChar);
        File file = new File(root, name);
        return file;
    }

    public static boolean isSecure(String root, File file) throws IOException {
        PermissionCollection rootDirectory;
        if (root.endsWith(File.separator)) {
            FilePermission fp = new FilePermission(root + "-", "read");
            rootDirectory = fp.newPermissionCollection();
            rootDirectory.add(fp);
            rootDirectory.add(new FilePermission(root.substring(0, root.length() - 1), "read"));
        } else {
            FilePermission fp = new FilePermission(root, "read");
            rootDirectory = fp.newPermissionCollection();
            rootDirectory.add(fp);
            rootDirectory.add(new FilePermission(root + File.separator + "-", "read"));
        }
        return (rootDirectory.implies(new FilePermission(file.getCanonicalPath(), "read")));
    }

    /**
     * This method tries to find a suitable InetAddress that is routable.  It calls {@link java.net.InetAddress#getLocalHost}
     * to find the local host.  If that address it a site local address (192.168.*.*, 10.*.*.*, or 172.16.*.*) or the
     * loopback address (127.0.0.1), it enumerates all the NetworkInterfaces, and tries to find an address that is
     * not site local or loopback address.  If it cannot it simply returns whatever {@link InetAddress#getLocalHost}.
     *
     * @return the address of a non site local or non loopback address, unless there is only loopback or site local addresses.
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static InetAddress findRoutableHostAddress() throws UnknownHostException, SocketException {
        InetAddress localAddress = InetAddress.getLocalHost();
        if (localAddress.isSiteLocalAddress() || localAddress.isLoopbackAddress()) {
            for (Enumeration networkEnum = NetworkInterface.getNetworkInterfaces(); networkEnum.hasMoreElements(); ) {
                NetworkInterface netInterface = (NetworkInterface) networkEnum.nextElement();
                for (Enumeration inetAddressEnum = netInterface.getInetAddresses(); inetAddressEnum.hasMoreElements(); ) {
                    InetAddress address = (InetAddress) inetAddressEnum.nextElement();
                    if (!address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
                        return address;
                    }
                }
            }
        }
        return localAddress;
    }

    public static List findAllHostAddresses(boolean includeLoopback) throws SocketException {
        List addresses = new ArrayList();
        for (Enumeration networkEnum = NetworkInterface.getNetworkInterfaces(); networkEnum.hasMoreElements(); ) {
            NetworkInterface netInterface = (NetworkInterface) networkEnum.nextElement();
            for (Enumeration inetAddressEnum = netInterface.getInetAddresses(); inetAddressEnum.hasMoreElements(); ) {
                InetAddress address = (InetAddress) inetAddressEnum.nextElement();
                if (includeLoopback || !address.isLoopbackAddress()) {
                    addresses.add(address);
                }
            }
        }
        return addresses;
    }

}
