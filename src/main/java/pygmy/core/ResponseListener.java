package pygmy.core;

import java.io.IOException;

/**
 * Interface for monitoring progress of sending {@link HttpResponse}.  Class that implement this interface can listen
 * for all HttpResponses being sent to clients.  They also can veto or block certain responses during their transmissions
 * by throwing a java.lang.IOException in the {@link #notify(HttpRequest, int, int) notify} method.  The
 * {@link Server#setResponseListener(ResponseListener) setResponseListener} method must be called in order to set
 * the ResponseListener for a Server.
 */
public interface ResponseListener {

    /**
     * This method is called at the beginning of an HttpRequest.
     *
     * @param request the request that is being respond to.
     */
    void startTransfer(HttpRequest request);

    /**
     * This method is called at regular intervals to notify the progress of a HttpResponse transmission.  The number of
     * time this method is called depends on how much data is being set to the client.  It will be called at least once.
     *
     * @param request     the request that is being responded to.
     * @param bytesSent   the amount of data sent so far in bytes.  Always be zero or greater.
     * @param totalLength the total length of the data being sent in bytes.  Can be -1 for an unknown total.
     * @throws IOException throws an IOException if a ResponseListener wishes to interrupt the transmission.
     */
    void notify(HttpRequest request, int bytesSent, int totalLength) throws IOException;

    /**
     * This method is called once the transmission of the HttpResponse has concluded.
     *
     * @param request the request that is being respond to.
     * @param e       the exception if there was one that ended the transmission.  It is null if it completed successfully.
     */
    void endTransfer(HttpRequest request, Exception e);

}
