package pygmy.nntp;

import org.apache.lucene.search.Hits;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.io.FileNotFoundException;

public class ArticleHandler extends NntpHandler {

    Forum forum;

    public ArticleHandler(Forum forum) {
        this.forum = forum;
    }

    public boolean handleNntp(NntpRequest nntpRequest, NntpResponse nntpResponse) throws IOException {
        try {
            if( nntpRequest.getCommand().equalsIgnoreCase("article") ) {
                readArticle( nntpRequest, nntpResponse, true, true );
            } else if( nntpRequest.getCommand().equalsIgnoreCase("head") ) {
                readArticle( nntpRequest, nntpResponse, true, false );
            } else if( nntpRequest.getCommand().equalsIgnoreCase("body") ) {
                readArticle( nntpRequest, nntpResponse, false, true );
            } else if( nntpRequest.getCommand().equalsIgnoreCase("next") ) {
                gotoNextArticle( nntpRequest, nntpResponse );
            } else if( nntpRequest.getCommand().equalsIgnoreCase("last") ) {
                gotoPreviousArticle( nntpRequest, nntpResponse );
            } else if( nntpRequest.getCommand().equalsIgnoreCase("post") ) {
                postArticle( nntpRequest, nntpResponse );
            } else if( nntpRequest.getCommand().equalsIgnoreCase("stat") ) {
                retrieveStatistics(nntpRequest, nntpResponse);
            } else if( nntpRequest.getCommand().equalsIgnoreCase("mode") ) {
                if( nntpRequest.getParameter(0).equalsIgnoreCase("reader") ) {
                    nntpResponse.sendResponse( 200, "Hello you can post." );
                }
            } else if( nntpRequest.getCommand().equalsIgnoreCase("xover") ) {
                nntpResponse.sendResponse( 224, "Header follows" );
                NewsGroup group = forum.getNewsgroup( nntpRequest.getCurrentNewsgroup() );
                Hits hits = forum.getOverview( group );
                for( int i = 0; i < hits.length(); i++ ) {
                    Document doc = hits.doc( i );
                    NntpOutputStream stream = nntpResponse.getOutputStream();
                    stream.print( doc.getField("article." + group.getName() + ".article-number").stringValue() );
                    stream.print( "\t" );
                    stream.print( doc.getField("article.subject").stringValue() );
                    stream.print( "\t" );
                    stream.print( doc.getField("article.from").stringValue() );
                    stream.print( "\t" );
                    stream.print( doc.getField("article.date").stringValue() );
                    stream.print( "\t" );
                    stream.print( doc.getField("article.message-id").stringValue() );
                    stream.print( "\t" );
                    stream.print( doc.getField("article.references").stringValue() );
                    stream.print( "\t" );
                    stream.print( doc.getField("article.bytes").stringValue() );
                    stream.print( "\t" );
                    stream.print( doc.getField("article.lines").stringValue() );
                    stream.print( "\t" );
                    stream.print( doc.getField("article.xref").stringValue() );
                }
                nntpResponse.getOutputStream().printEnd();
//            } else if( nntpRequest.getCommand().equalsIgnoreCase("xhdr") ) {
//                for( int i = 0; i < nntpRequest.parameterLength(); i++ ) {
//                    System.out.println( nntpRequest.getParameter( i ) );
//                }
//                nntpResponse.sendResponse( 224, "Overview information follows" );
//                nntpResponse.getOutputStream().printEnd();
            } else {
                return false;
            }
        } catch (NoCurrentNewsgroupException e) {
            respondNoNewsGroup(nntpResponse);
        } catch( NoCurrentArticleException e ) {
            respondNoCurrentArticle( nntpResponse );
        } catch( FileNotFoundException e ) {
            respondNoSuchArticle( nntpResponse );
        } catch (ParseException e) {
            respondSyntaxError( nntpResponse );
        }
        return true;
    }

    private void retrieveStatistics(NntpRequest nntpRequest, NntpResponse nntpResponse) throws NoCurrentNewsgroupException, IOException {
        String index = nntpRequest.getParameter(0);
        Article article = null;
        if( index.startsWith("<") ) {
            article = forum.getArticle( index );
        } else {
            NewsGroup group = forum.getNewsgroup( nntpRequest.getCurrentNewsgroup() );
            article = group.getMessage( Integer.parseInt( index ) );
            nntpRequest.setCurrentArticle( index );
        }
        respondNextArticleFound( article, nntpResponse );
    }

    private void postArticle(NntpRequest nntpRequest, NntpResponse nntpResponse) throws IOException {
        try {
            if( isPostingAllowed( nntpRequest ) ) {
                respondSendArticleToBePosted( nntpResponse );
                Article article = readArticle(nntpRequest);
                forum.addArticle( article, nntpRequest.getProperty("Host","") );
                respondArticleTransferedOk( nntpResponse );
            } else {
                respondPostingNotAllowed( nntpResponse );
            }
        } catch( IOException ioe ) {
            respondPostingFailed( nntpResponse );
        }
    }

    private Article readArticle(NntpRequest nntpRequest) throws IOException {
        Article article = new Article( nntpRequest.getInput() );
        return article;
    }

    private void gotoPreviousArticle(NntpRequest nntpRequest, NntpResponse nntpResponse) throws NoCurrentArticleException, NoCurrentNewsgroupException, IOException {
        int index = getCurrentArticleAsInt( nntpRequest );
        if( index - 1 < forum.getNewsgroup( nntpRequest.getCurrentNewsgroup() ).getFirstIndex() ) {
            respondNoPreviousArticle( nntpResponse );
            return;
        }
        sendNextArticleIndex(index-1, nntpRequest, nntpResponse);
    }

    private void gotoNextArticle( NntpRequest nntpRequest, NntpResponse nntpResponse) throws IOException, NoCurrentNewsgroupException, NoCurrentArticleException {
        int index = getCurrentArticleAsInt( nntpRequest ) ;
        if( index + 1 > forum.getNewsgroup( nntpRequest.getCurrentNewsgroup() ).getLastIndex() ) {
            respondNoNextArticle( nntpResponse );
            return;
        }
        sendNextArticleIndex( index + 1, nntpRequest, nntpResponse);
    }

    private void sendNextArticleIndex( int newIndex, NntpRequest nntpRequest, NntpResponse nntpResponse) throws NoCurrentNewsgroupException, IOException {
        nntpRequest.setCurrentArticle( String.valueOf( newIndex ) );
        Article article = forum.getNewsgroup( nntpRequest.getCurrentNewsgroup() ).getMessage( newIndex );
        respondNextArticleFound( article, nntpResponse );
    }

    private int getCurrentArticleAsInt( NntpRequest nntpRequest ) throws NoCurrentArticleException {
        String next = nntpRequest.getCurrentArticle();
        return Integer.parseInt( next );
    }

    private void readArticle(  NntpRequest nntpRequest, NntpResponse nntpResponse, boolean head, boolean body) throws IOException, NoCurrentNewsgroupException, NoCurrentArticleException {
        String article = nntpRequest.getParameter(0);
        if( article == null ) {
            article = nntpRequest.getCurrentArticle();
        }
        if( article.startsWith("<") ) {
            readArticleByMessageId( article, nntpResponse, head, body );
        } else {
            readArticleByIndex( nntpRequest.getCurrentNewsgroup(), article, nntpRequest, nntpResponse, head, body );
        }
    }

    private void readArticleByIndex( String newsgroup, String article, NntpRequest request, NntpResponse nntpResponse, boolean head, boolean body ) throws IOException {
        Article message = forum.getNewsgroup( newsgroup ).getMessage( Integer.parseInt( article ) );
        if( message == null ) {
            respondNoSuchArticleIndex(nntpResponse);
        } else {
            request.setCurrentArticle( article );
            sendArticle( message, nntpResponse, head, body );
        }
    }

    private void readArticleByMessageId( String article, NntpResponse nntpResponse, boolean head, boolean body ) throws IOException {
        Article message = forum.getArticle( article );
        sendArticle( message, nntpResponse, head, body );
    }

    private void sendArticle( Article message, NntpResponse nntpResponse, boolean head, boolean body ) throws IOException {
        respondArticleRetrieved(message, nntpResponse);
        NntpOutputStream out = nntpResponse.getOutputStream();
        if( head ) {
            message.getHeader().print( out );
        }
        if( body ) {
            out.print( message.getBody() );
        }
        out.println();
        out.printEnd();
        out.flush();
    }
}
