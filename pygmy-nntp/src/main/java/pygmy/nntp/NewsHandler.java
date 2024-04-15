package pygmy.nntp;

import java.io.IOException;
import java.util.Iterator;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class NewsHandler extends NntpHandler {

    Forum forum;

    public NewsHandler(Forum forum) {
        this.forum = forum;
    }

    public boolean handleNntp(NntpRequest nntpRequest, NntpResponse nntpResponse) throws IOException {
        try {
            if( nntpRequest.getCommand().equalsIgnoreCase("ihave") ) {
                retrieveArticleFromClient( nntpRequest.getParameter(0), nntpRequest, nntpResponse );
            } else if( nntpRequest.getCommand().equalsIgnoreCase("newgroups") ) {
                Date since = getNntpDate( nntpRequest, 0 );
                searchNewsgroups( since, getDistributionList( nntpRequest ), nntpResponse );
            } else if( nntpRequest.getCommand().equalsIgnoreCase("newnews") ) {
                Date since = getNntpDate( nntpRequest, 0 );
                searchForNews( nntpRequest.getParameter(0), since, getDistributionList( nntpRequest ), nntpResponse );
            } else {
                return false;
            }
        } catch (ParseException e) {
            respondSyntaxError( nntpResponse );
        }
        return true;
    }

    private void searchForNews(String newsgroup, Date since, String[] distributionList, NntpResponse nntpResponse) {
        List list = forum.getArticle( since );
    }

    private void searchNewsgroups( Date since, String[] distributions, NntpResponse response ) throws IOException {
        List newsgroups = forum.getNewsgroups( since, distributions );
        NntpOutputStream writer = response.getOutputStream();
        respondListOfNewsgroups( response );
        for( Iterator i = newsgroups.iterator(); i.hasNext(); ) {
            writer.println( ((NewsGroup)i.next()).getName() );
        }
        writer.printEnd();
    }

    private Date getNntpDate( NntpRequest nntpRequest, int startingIndex ) throws ParseException {
        String dateStr = nntpRequest.getParameter( startingIndex );
        String timeStr = nntpRequest.getParameter( startingIndex + 1 );
        TimeZone timeZone = TimeZone.getDefault();
        if( startingIndex + 2 < nntpRequest.parameterLength() ) {
            String zone = nntpRequest.getParameter( startingIndex + 2 );
            if( zone.equalsIgnoreCase("gmt") ) {
                timeZone = TimeZone.getTimeZone( zone );
            }
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd HHmmss z");
        return formatter.parse( dateStr + " " + timeStr + " " + timeZone.getID() );
    }

    private String[] getDistributionList( NntpRequest nntpRequest ) {
        String[] distributions = null;
        for( int i = 0; i < nntpRequest.parameterLength(); i++ ) {
            String distribs = nntpRequest.getParameter( i );
            if( distribs.startsWith("<") && distribs.endsWith(">") ) {
                distributions = distribs.split(",");
                distributions[0] = distributions[0].substring(1);
                String last = distributions[ distributions.length - 1 ];
                distributions[ distributions.length  - 1 ] = last.substring( 0, last.length() - 1 );
                break;
            }
        }
        return distributions;
    }

    private void retrieveArticleFromClient(String messageId, NntpRequest nntpRequest, NntpResponse nntpResponse) throws IOException {
        if( isArticlePresent(messageId) ) {
            respondArticleNotWanted( nntpResponse );
        } else {
            respondArticleWanted( nntpResponse );
            retrieveArticle(nntpRequest, nntpResponse);
        }
    }

    private void retrieveArticle(NntpRequest nntpRequest, NntpResponse nntpResponse) throws IOException {
        try {
            Article article = new Article( nntpRequest.getInput() );
            forum.addArticle( article, nntpRequest.getProperty("Host","") );
            respondArticleTransferedOk( nntpResponse );
        } catch( IOException e ) {
            respondTryTransferAgain( nntpResponse );
        }
    }

    private boolean isArticlePresent(String messageId) {
        try {
            forum.getArticle( messageId );
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
