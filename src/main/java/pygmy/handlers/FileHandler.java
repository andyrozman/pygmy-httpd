package pygmy.handlers;

import lombok.extern.slf4j.Slf4j;
import pygmy.core.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * <p>
 * This is the most basic Handler of a web server.  It serves up files from a specified directory.  For all urls
 * matching the url-prefix parameter, it translates those into files on the root files system starting at the root
 * parameter.  It does pay attention to the Range http header parameter, and will send a range of bytes from a
 * request file.  It also sets the <i>file-path</i> request property to the translate local system path of the file.
 * Other handlers could use this so that they don't have to translate the URL into a path.  This handler provides
 * a default file for directory requests (index.html as default), but you can override that by configuring it.  This
 * handler offers both file and directory listing that can be turned on or off.  By default it is turned off.
 * </p>
 */
@Slf4j
public class FileHandler extends AbstractHandler implements Handler {

    public static final String IF_MODIFIED = "If-Modified-Since";
    public static final String LAST_MODIFIED_KEY = "Last-Modified";
    public static final String RANGE_HEADER_KEY = "Range";

    private File root;
    private String defaultFile;
    private boolean allowDirectoryListing = false;
    private String css;

    public FileHandler(String root) {
        this(new File(root));
    }

    public FileHandler(File root) {
        this(root, "index.html");
    }

    public FileHandler(File root, String defaultFile) {
        this.root = root;
        this.defaultFile = defaultFile;
    }

    public FileHandler allowDirectoryListing(boolean listing) {
        allowDirectoryListing = listing;
        return this;
    }

    public FileHandler css(String css) {
        this.css = css;
        return this;
    }

    public boolean start(Server server) {
        super.start(server);
        return true;
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        File file = Http.translatePath(root, request.getUrlMatch().getTrailing());
        if (!Http.isSecure(root, file)) {
            log.warn("Access denied to " + file.getAbsolutePath());
            return false;
        }
        request.putProperty("file-path", file.getAbsolutePath());
        if (file.isDirectory()) {
            if (allowDirectoryListing) {
                return directoryListing(file, request, response);
            } else {
                file = new File(file, defaultFile);
            }
        }

        if (!file.exists()) {
            log.warn("File " + file.getAbsolutePath() + " was not found.");
            return false;
        }
        String type = getMimeType(file.getName());
        if (type != null) {
            sendFile(request, response, file, type);
            return true;
        } else {
            log.warn("Mime type for file " + file.getAbsolutePath() + " was not found.");
            return false;
        }
    }

    static public void sendFile(HttpRequest request, HttpResponse response, File file, String type) throws IOException {
        if (!file.isFile()) {
            response.sendError(HttpURLConnection.HTTP_NOT_FOUND, " not a normal file");
            return;
        }
        if (!file.canRead()) {
            response.sendError(HttpURLConnection.HTTP_FORBIDDEN, " Permission Denied");
            return;
        }

        if (request.getRequestHeader(IF_MODIFIED) != null) {
            try {
                long modified = Http.parseTime(request.getRequestHeader(IF_MODIFIED));
                if (file.lastModified() <= modified) {
                    response.setStatusCode(HttpURLConnection.HTTP_NOT_MODIFIED);
                    return;
                }
            } catch (ParseException ignore) {
                // ignore the date.
            }
        }
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        response.addHeader(LAST_MODIFIED_KEY, Http.formatTime(file.lastModified()));
        long[] range = getRange(request, file);
        response.setMimeType(type);
        response.sendResponse(in, range[0], range[1]);
    }

    private static long[] getRange(HttpRequest request, File file) {
        long[] range = new long[2];
        range[0] = 0;
        range[1] = file.length();
        String rangeStr = request.getRequestHeader(RANGE_HEADER_KEY, "bytes=0-");
        int equalSplit = rangeStr.indexOf("=") + 1;
        int split = rangeStr.indexOf("-");
        if (split < -1) {
            try {
                range[0] = Integer.parseInt(rangeStr.substring(equalSplit));
            } catch (NumberFormatException e) {
            }
        } else {
            range[0] = Integer.parseInt(rangeStr.substring(equalSplit, split));
            if (split + 1 < rangeStr.length()) {
                try {
                    range[1] = Integer.parseInt(rangeStr.substring(split + 1, rangeStr.length()));
                } catch (NumberFormatException e) {
                }
            }
        }
        return range;
    }

    private boolean directoryListing(File directory, HttpRequest request, HttpResponse response) throws IOException {
        StringBuffer templateHeader = new StringBuffer();

        String decodedUrl = java.net.URLDecoder.decode(request.getUrl(), "UTF-8");
        addFolderNavigation(templateHeader, decodedUrl);
        addTableHeaders(templateHeader);
        addFilesAndFolders(request, directory.listFiles(), templateHeader);
        addTableFooter(templateHeader);

        response.setMimeType("text/html");
        PrintWriter out = response.getPrintWriter();
        out.write(addHtmlHeader(request));
        out.write("<body>\n");
        out.write(templateHeader.toString());
        out.write("</body>\n");
        out.write("</html>");
        return true;
    }

    private void addTableFooter(StringBuffer templateHeader) {
        templateHeader.append("</table>\n");
        templateHeader.append("</div>\n");
    }

    private String addHtmlHeader(HttpRequest request) throws IOException {
        StringBuffer templateHeader = new StringBuffer();
        templateHeader.append("<html>\n");
        templateHeader.append("<head>\n");
        if (css != null) {
            templateHeader.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
            templateHeader.append(request.createUrl(css));
            templateHeader.append("\">\n");
        } else {
            addStyleDefintion(templateHeader);

        }
        templateHeader.append("</head>\n");
        return templateHeader.toString();
    }

    private void addStyleDefintion(StringBuffer templateHeader) {
        templateHeader.append("<style>\n");
        templateHeader.append("body,td,ul,li,a,div,p,pre,span  {color: #333333; font-family: Verdana, Arial, Helvetica; font-size: 10pt;}\n");
        templateHeader.append("th {color:#333333; font-family: Verdana, Arial, Helvetica; font-size:10pt; text-align:center; }\n");
        templateHeader.append(".navigationbar {background-color:#7094b8;color:#f6f6ee;border-bottom:1px #666 solid;border-right:1px #666 solid;font:bold 11px tahoma,verdana,sans-serif;padding:3px 2px 3px 4px;margin-top:10px;}\n");
        templateHeader.append(".box { background-color:#d1dde9; border:1px #369 solid; border-top:0; padding:4px 4px 4px 4px; background-color:#77AADD}\n");
        templateHeader.append(".directory {	padding-top: 2px;padding-right: 0px;padding-bottom: 0px;padding-left: 16px;background-image: url(/web/folder16.gif);background-repeat: no-repeat;background-position: left center;font-size: small;}\n");
        templateHeader.append(".file { padding-top: 2px;padding-right: 0px;padding-bottom: 0px;padding-left: 16px;font-size: small;}\n");
        templateHeader.append(".topHeader { padding-top: 2px;padding-right: 0px;padding-bottom: 0px;padding-left: 16px;background-image: url(/web/folder16.gif);background-repeat: no-repeat;background-position: left center;}\n");
        templateHeader.append("tr.tableheader { background-color: #ffffe4; }\n");
        templateHeader.append("tr.fileentry { background-color: #EEEEEE; }\n");
        templateHeader.append("tr.altfileentry { background-color: #FFFFFF; }\n");
        templateHeader.append("td.nameColumn { text-align: left; }\n");
        templateHeader.append("td.typeColumn { text-align: center; }\n");
        templateHeader.append("td.sizeColumn { text-align: right; }\n");

        templateHeader.append("a {color: #0000A0;font-family: Verdana, Arial, Helvetica;text-decoration:none;}\n");
        templateHeader.append("a:active {color: #FFFFFF; text-decoration : none;}\n");
        templateHeader.append("a:link {color: #336699; text-decoration : none;}\n");
        templateHeader.append("a:visited {color: #336699; text-decoration : none;}\n");
        templateHeader.append("a:hover {color: #000000; text-decoration : none;}\n");

        templateHeader.append("a.whitelink {color: #FFFFFF; text-decoration: none;}\n");
        templateHeader.append("a.whitelink:visited {color: #FFFFFF;}\n");
        templateHeader.append("a.whitelink:hover {color: #AAAAAA; }\n");
        templateHeader.append("</style>");
    }

    private void addFolderNavigation(StringBuffer templateHeader, String decodedUrl) {
        templateHeader.append("<div class=\"navigationbar\">\n");
        templateHeader.append("<span class=\"topHeader\">\n");
        StringTokenizer token = new StringTokenizer(decodedUrl, "/");
        StringBuffer buf = new StringBuffer(decodedUrl.length());
        templateHeader.append("&nbsp;<a href=\"/\" class=\"whitelink\">[home]</a>\n");
        while (token.hasMoreElements()) {
            String path = token.nextToken();
            buf.append("/");
            buf.append(path);
            templateHeader.append("/");
            templateHeader.append("<a href=\"");
            templateHeader.append(buf.toString());
            templateHeader.append("\" class=\"whitelink\">");
            templateHeader.append(path);
            templateHeader.append("</a>");
        }
        templateHeader.append("</span>\n</div>\n");
    }

    private void addTableHeaders(StringBuffer templateHeader) {
        templateHeader.append("<div class=\"box\">\n");
        templateHeader.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"2\">\n");
        templateHeader.append("<tr class=\"tableheader\">\n");
        templateHeader.append("<th>Name</td>");
        templateHeader.append("<th>Type</td>");
        templateHeader.append("<th>Size</td>");
        templateHeader.append("\n</tr>\n");
    }

    private void addFilesAndFolders(HttpRequest request, File[] files, StringBuffer templateHeader) throws IOException {
        URI rootUri = this.root.toURI();
        ComparableComparator comp = new ComparableComparator();
        TreeMap dirMap = new TreeMap(comp);
        TreeMap fileMap = new TreeMap(comp);
        StringBuffer fileBuffer = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
            fileBuffer.delete(0, fileBuffer.length());
            if (files[i].isDirectory()) {
                String name = files[i].getName();
                fileBuffer.append("<td class=\"nameColumn\"><span class=\"directory\">");
                fileBuffer.append("<a href=\"");
                fileBuffer.append(getHttpHyperlink(request, rootUri, files[i]));
                fileBuffer.append("\">&nbsp;");
                fileBuffer.append(name);
                fileBuffer.append("</a></span></td>\n");
                fileBuffer.append("<td class=\"typeColumn\">Folder</td>\n");
                fileBuffer.append("<td class=\"sizeColumn\"></td>\n");
                fileBuffer.append("</tr>\n");
                dirMap.put(name, fileBuffer.toString());
            } else {
                String absolutePath = files[i].getAbsolutePath();
                String mimeType = getMimeType(absolutePath);
                if (mimeType != null) {
                    String name = files[i].getName();
                    fileBuffer.append("<td class=\"nameColumn\"><span class=\"file\"><a href=\"");
                    fileBuffer.append(getHttpHyperlink(request, rootUri, files[i]));
                    fileBuffer.append("\">");
                    fileBuffer.append(name);
                    fileBuffer.append("</a></span></td>\n");
                    fileBuffer.append("<td class=\"typeColumn\">");
                    fileBuffer.append(mimeType);
                    fileBuffer.append("</td>\n");
                    fileBuffer.append("<td class=\"sizeColumn\">");
                    fileBuffer.append(NumberFormat.getIntegerInstance().format(files[i].length()));
                    fileBuffer.append("</td>\n");
                    fileMap.put(name, fileBuffer.toString());
                }
            }
        }
        int count = 0;
        count = writeOutMap(templateHeader, dirMap, count);
        count = writeOutMap(templateHeader, fileMap, count);
    }

    private int writeOutMap(StringBuffer templateHeader, TreeMap dirMap, int count) {
        String[] styles = {"fileentry", "altfileentry"};
        for (Iterator i = dirMap.keySet().iterator(); i.hasNext(); ) {
            templateHeader.append("<tr class=\"");
            templateHeader.append(styles[count % 2]);
            templateHeader.append("\">\n");
            templateHeader.append((String) dirMap.get(i.next()));
            templateHeader.append("</tr>\n");
            count++;
        }

        return count;
    }

    private String getHttpHyperlink(HttpRequest request, URI directory, File file) throws IOException {
        String prefixRelative = directory.relativize(file.toURI()).getPath();
        String urlPrefix = request.getUrl().substring(0, request.getUrl().indexOf(request.getUrlMatch().getTrailing()));
        if (urlPrefix.endsWith("/")) {
            return urlPrefix + prefixRelative;
        } else if (prefixRelative.startsWith("/")) {
            return urlPrefix + prefixRelative;
        } else {
            return request.createUrl(urlPrefix + "/" + prefixRelative);
        }
    }

    /**
     * This orders directories by calling compareTo() method.
     */
    public static class ComparableComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Comparable c1 = (Comparable) o1;
            Comparable c2 = (Comparable) o2;

            return c1.compareTo(c2);
        }
    }

}
