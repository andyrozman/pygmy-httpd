package pygmy.nntp;

import pygmy.core.Handler;
import pygmy.core.Server;
import pygmy.core.Request;
import pygmy.core.Response;

import java.io.IOException;

public abstract class NntpHandler implements Handler {

    private String name;
    private Server server;

    public boolean initialize(String handlerName, Server aServer) {
        name = handlerName;
        server = aServer;
        return true;
    }

    public String getName() {
        return name;
    }

    public boolean isPostingAllowed( Request nntpRequest ) {
        return Boolean.valueOf( nntpRequest.getProperty("posting","true") ).booleanValue();
    }

    public boolean handle(Request request, Response response) throws IOException {
        if( request instanceof NntpRequest ) {
            return handleNntp( (NntpRequest)request, (NntpResponse)response );
        }
        return false;
    }

    public abstract boolean handleNntp(NntpRequest nntpRequest, NntpResponse nntpResponse) throws IOException;

    public boolean shutdown(Server server) {
        return true;
    }

    public void respondGroupSelected(NewsGroup group, NntpResponse nntpResponse) throws IOException {
        StringBuffer buffer = new StringBuffer();
        buffer.append( group.size() );
        buffer.append( " " );
        buffer.append( group.getFirstIndex() );
        buffer.append( " " );
        buffer.append( group.getLastIndex() );
        buffer.append( " " );
        buffer.append( group.getName() );
        buffer.append( " group selected." );
        nntpResponse.sendResponse( 211, buffer.toString() );
    }

    public void respondNewsgroupList(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 215, "list of newsgroups follows" );
    }

    public void respondArticleRetrieved(Article message, NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 220, new String[] { Integer.toString( message.getArticleNumber() ), message.getMessageId(), "article retrieved - head and body follow" } );
    }

    public void respondNextArticleFound( Article article, NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 223, new String[] { String.valueOf( article.getArticleNumber() ), article.getMessageId(), "article retrieved - request text separately" } );
    }

    public void respondListOfNews( NntpResponse response ) throws IOException {
        response.sendResponse( 230, "list of new articles by message-id follows" );
    }

    public void respondListOfNewsgroups(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 231, "list of new newsgroups follows" );
    }

    public void respondArticleTransferedOk(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 235, "article transferred ok" );
    }

    public void respondPostingOk(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 240, "article posted ok" );
    }

    public void respondArticleWanted(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 335, "send article to be transferred.  End with <CR-LF>.<CR-LF>" );
    }

    public void respondSendArticleToBePosted( NntpResponse nntpResponse ) throws IOException {
        nntpResponse.sendResponse( 340, "send article to be posted. End with <CR-LF>.<CR-LF>" );
    }

    public void respondNoSuchGroup(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 411, "no such news group" );
    }

    public void respondNoNewsGroup(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse(412, "no newsgroup has been selected");
    }

    public void respondNoCurrentArticle(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse(420,"no current article has been selected");
    }

    public void respondNoNextArticle(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 421, "no next article in this group");
    }

    public void respondNoPreviousArticle(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 422, "no previous article in this group");
    }

    public void respondNoSuchArticleIndex(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 423, "no such article number in this group" );
    }

    public void respondNoSuchArticle(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 430, "no such article found" );
    }

    public void respondArticleNotWanted(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 435, "article not wanted - do not send it" );
    }

    public void respondTryTransferAgain(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 436, "transfer failed - try again later" );
    }

    public void respondTransferRejected(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 437, "article rejected - do not try again" );
    }

    public void respondPostingNotAllowed( NntpResponse nntpResponse ) throws IOException {
        nntpResponse.sendResponse( 440, "posting not allowed" );
    }

    public void respondPostingFailed( NntpResponse nntpResponse ) throws IOException {
        nntpResponse.sendResponse( 441, "posting failed" );
    }

    public void respondSyntaxError(NntpResponse nntpResponse) throws IOException {
        nntpResponse.sendResponse( 501, "command syntax error" );
    }
}
