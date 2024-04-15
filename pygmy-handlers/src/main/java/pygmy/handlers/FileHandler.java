package pygmy.handlers;

import lombok.extern.slf4j.Slf4j;
import pygmy.core.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.text.ParseException;

/**
 * <p>
 * This is the most basic Handler of a web server.  It serves up files from a specified directory.  For all urls
 * matching the url-prefix parameter, it translates those into files on the root files system starting at the root
 * parameter.  It does pay attention to the Range http header parameter, and will send a range of bytes from a
 * request file.  It also sets the <i>file-path</i> request property to the translate local system path of the file.
 * Other handlers could use this so that they don't have to translate the URL into a path.  This handler ignores
 * directory requests, but will serve up default files ( like index.html, if the config parameter is set ).  It does
 * not provide a directory listing see {@link DirectoryHandler} for that functionality.
 * </p>
 *
 * <table class="inner">
 * <tr class="header"><td>Parameter Name</td><td>Explanation</td><td>Default Value</td><td>Required</td></tr>
 * <tr class="row"><td>url-prefix</td><td>The prefix to filter request urls.</td><td>None</td><td>Yes</td></tr>
 * <tr class="altrow"><td>root</td><td>A local system path to the root of the folder to share.</td><td>None</td><td>Yes</td></tr>
 * <tr class="row"><td>default-file</td><td>The name of the default file that should be used if no file is specified in the URL.</td><td>index.html</td><td>No</td></tr>
 * </table>
 */
@Slf4j
public class FileHandler extends AbstractHandler implements Handler {

    public static final ConfigOption ROOT_OPTION = new ConfigOption("root", true, "The path to the directory share files.");
    public static final ConfigOption DEFAULT_FILE_OPTION = new ConfigOption("default-file", "index.html", "The default file to send if no file is specified.");

    public static final String IF_MODIFIED = "If-Modified-Since";
    public static final String LAST_MODIFIED_KEY = "Last-Modified";
    public static final String RANGE_HEADER_KEY = "Range";

    private String root;
    private String defaultFile;

    public boolean initialize(String handlerName, Server server) {
        super.initialize(handlerName, server);
        root = ROOT_OPTION.getProperty(server, handlerName);
        defaultFile = DEFAULT_FILE_OPTION.getProperty(server, handlerName);
        return true;
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        File file = Http.translatePath(root, request.getUrl().substring(getUrlPrefix().length()));
        if (!Http.isSecure(root, file)) {
            log.warn("Access denied to " + file.getAbsolutePath());
            return false;
        }
        request.putProperty("file-path", file.getAbsolutePath());
        if (file.isDirectory()) {
            file = new File(file, defaultFile);
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
        long range[] = new long[2];
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
}
