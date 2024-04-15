package pygmy.nntp.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import pygmy.nntp.NewsHandler;
import pygmy.nntp.NntpRequest;
import pygmy.nntp.NntpResponse;
import pygmy.core.Http;

import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class NewsHandlerTest extends NntpHandlerTestCase {

    public void testHandler() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ihave <blkdu9$pd8$1@hood.uits.indiana.edu>");
        buffer.append(Http.CRLF);
        buffer.append( new String( getArticleBytes("test.eml") ) );
        buffer.append( "." );
        buffer.append( Http.CRLF );
        buffer.append("ihave <blkdu9$pd8$1@hood.uits.indiana.edu>");
        buffer.append(Http.CRLF);

        NntpRequest request = new NntpRequest( null, new Properties(), new ByteArrayInputStream( buffer.toString().getBytes() ) );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NntpResponse response = new NntpResponse( baos );
        forum.createNewsgroup("comp.lang.java.programmer");

        request.nextCommand();
        NewsHandler handler = new NewsHandler( forum );
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println( baos.toString() );
        assertTrue( "Assert the response asked to send the article", baos.toString().startsWith("335 send article to be transferred.  End with <CR-LF>.<CR-LF>") );
        assertEquals( "Assert the forum size has increased by one.", 1, forum.getNewsgroup( "comp.lang.java.programmer" ).size() );
        assertTrue( "Assert the response received the article.", baos.toString().indexOf( "235 article transferred ok" ) >= 0 );

        request.nextCommand();
        baos.reset();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
        assertTrue( "Assert the response denied the article", baos.toString().startsWith("435 article not wanted - do not send i") );

        forum.addArticle( NntpTestUtil.createArticle("test.eml"), "localhost" );
    }

    public static Test suite() {
        return new TestSuite(NewsHandlerTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
