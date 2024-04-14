package pygmy.core;

import java.io.*;
import java.net.HttpURLConnection;

/**
 * This holds the response data for the http response.
 */
public class HttpResponse extends Response {

    private int statusCode = HttpURLConnection.HTTP_OK;

    private String mimeType = "text/html";

    private HttpHeaders responseHeaders;

    private InternetOutputStream stream;

    private ResponseDataList dataStreamList;

    private HttpRequest request;

    private boolean keepConnectionOpen;

    private ResponseListener responseListener;

    public HttpResponse(HttpRequest request, OutputStream aStream) {
        this(request, aStream, null);
    }

    public HttpResponse(HttpRequest request, OutputStream aStream, ResponseListener listener) {
        this.stream = new InternetOutputStream(aStream);
        this.request = request;
        this.dataStreamList = new ResponseDataList();
        this.responseHeaders = new HttpHeaders();
        this.keepConnectionOpen = request.isKeepAlive();
        this.responseListener = listener;
    }

    public boolean isKeepAlive() {
        return (keepConnectionOpen && request.isKeepAlive());
    }

    public void addHeader(String key, String value) {
        responseHeaders.put(key, value);
    }

    public PrintWriter getPrintWriter() {
        return dataStreamList.addPrintWriter();
    }

    public void setMimeType(String aMimeType) {
        mimeType = aMimeType;
    }

    public void sendError(int statusCode, String errorMessage) {
        sendError(statusCode, errorMessage, null);
    }

    public void sendError(int statusCode, String errorMessage, Exception e) {
        keepConnectionOpen = false;
        String body = "<html>\n<head>\n"
                + "<title>Error: " + statusCode + "</title>\n"
                + "<body>\n<h1>" + statusCode + " <b>"
                + Http.getStatusPhrase(statusCode)
                + "</b></h1><br>\nThe requested URL <b>"
                + ((request.getUrl() == null) ? "<i>unknown URL</i>" : Http.encodeHtml(request.getUrl()))
                + "</b>\n " + Http.encodeHtml(errorMessage)
                + "\n<hr>";
        if (e != null) {
            StringWriter writer = new StringWriter();
            writer.write("<pre>");
            e.printStackTrace(new PrintWriter(writer));
            writer.write("</pre>");
            body += writer.toString();
        }
        body += "</body>\n</html>";

        this.dataStreamList.reset();
        this.statusCode = statusCode;
        this.mimeType = "text/html";
        PrintWriter out = getPrintWriter();
        out.write(body);
    }

    public void sendResponse(InputStream is, int length) throws IOException {
        this.dataStreamList.addResponse(is, length);
    }

    public void sendResponse(InputStream is, long beginning, long ending) throws IOException {
        this.dataStreamList.addResponse(is, beginning, ending - beginning);
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void commitResponse() throws IOException {
        try {
            String encoding = request.getAcceptedEncoding();
            boolean compressed = encoding.equalsIgnoreCase("gzip") || encoding.equalsIgnoreCase("x-gzip") || encoding.equalsIgnoreCase("compress") || encoding.equalsIgnoreCase("x-compress");

            startTransfer();
            sendHttpReply(statusCode);
            sendHeaders(mimeType, compressed ? -1 : dataStreamList.getTotalLength());
            if (!isHeadMethod()) {
                sendBody(encoding, compressed);
            }
            endTransfer();
        } catch (IOException e) {
            endTransfer(e);
            throw e;
        }
    }

    private void sendBody(String encoding, boolean compressed) throws IOException {
        if (!request.isProtocolVersionLessThan(1, 1) && (dataStreamList.getTotalLength() < 0 || compressed)) {
            stream.setChunkedEncoded(true);
        }
        stream.setEncoding(encoding);
        dataStreamList.sendData(stream);
    }

    private void sendHttpReply(int code) throws IOException {
        StringBuffer buffer = new StringBuffer(request.getProtocol());
        buffer.append(" ");
        buffer.append(code);
        buffer.append(" ");
        buffer.append(Http.getStatusPhrase(code));
        buffer.append(Http.CRLF);
        stream.write(buffer.toString().getBytes());
    }

    private void sendHeaders(String mimeType, long contentLength) throws IOException {
        responseHeaders.put("Date", Http.getCurrentTime());
        responseHeaders.put("Server", "Pygmy");
        String str = request.isKeepAlive() ? "Keep-Alive" : "close";
        responseHeaders.put(HttpHeaders.CONNECTION, str);
        if (contentLength >= 0) {
            responseHeaders.put("Content-Length", Long.toString(contentLength));
        } else if (!request.isProtocolVersionLessThan(1, 1)) {
            responseHeaders.put("Transfer-Encoding", "chunked");
        }

        if (mimeType != null) {
            responseHeaders.put("Content-Type", mimeType);
        }

        String encoding = request.getAcceptedEncoding();
        responseHeaders.put("Content-Encoding", encoding);
        responseHeaders.print(stream);
    }

    private boolean isHeadMethod() {
        return "HEAD".equalsIgnoreCase(request.getMethod());
    }

    public OutputStream getOutputStream() {
        return stream;
    }

    protected void startTransfer() {
        if (responseListener != null) {
            responseListener.startTransfer(request);
        }
    }

    protected void notifyListeners(int bytesSent, int length) throws IOException {
        if (responseListener != null) {
            responseListener.notify(request, bytesSent, length);
        }
    }

    protected void endTransfer() throws IOException {
        endTransfer(null);
    }

    protected void endTransfer(Exception e) throws IOException {
        stream.finish();
        if (responseListener != null) {
            responseListener.endTransfer(request, e);
        }
    }
}
