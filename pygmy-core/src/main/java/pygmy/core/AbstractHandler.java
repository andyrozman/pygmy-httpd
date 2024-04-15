package pygmy.core;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public abstract class AbstractHandler implements Handler {

    protected Server server;
    protected String handlerName;
    protected String urlPrefix;

    public static final ConfigOption URL_PREFIX_OPTION = new ConfigOption("url-prefix", "/", "URL prefix path for this handler.  Anything that matches starts with this prefix will be handled by this handler.");

    public AbstractHandler() {
    }

    public boolean initialize(String handlerName, Server server) {
        this.server = server;
        this.handlerName = handlerName;
        this.urlPrefix = URL_PREFIX_OPTION.getProperty(server, handlerName);
        return true;
    }

    public String getName() {
        return handlerName;
    }

    public boolean handle(Request aRequest, Response aResponse) throws IOException {
        if (aRequest instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) aRequest;
            HttpResponse response = (HttpResponse) aResponse;
            if (isRequestdForHandler(request)) {
                return handleBody(request, response);
            }
            if (log.isDebugEnabled()) {
                log.debug("'" + request.getUrl() + "' does not start with prefix '" + getUrlPrefix() + "'");
            }
        }
        return false;
    }

    protected boolean isRequestdForHandler(HttpRequest request) {
        //log.debug("requestGetUrl: {}, prefix {}", request.getUrl(), getUrlPrefix());
        return request.getUrl().startsWith(getUrlPrefix());
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        return false;
    }

    public boolean shutdown(Server server) {
        return true;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    protected String getMimeType(String filename) {
        int index = filename.lastIndexOf(".");
        String mimeType = null;
        if (index > 0) {
            mimeType = server.getProperty("mime" + filename.substring(index).toLowerCase());
        }

        return mimeType;
    }

}
