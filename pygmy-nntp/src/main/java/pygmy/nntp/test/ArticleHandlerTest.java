package pygmy.nntp.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import pygmy.core.Http;
import pygmy.nntp.*;

import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ArticleHandlerTest extends NntpHandlerTestCase {

    public void testHandlePost() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "post" );
        buffer.append( Http.CRLF );
        buffer.append( new String( getArticleBytes("testpost.eml") ) );
        buffer.append( "." );
        buffer.append( Http.CRLF );
        buffer.append("article 1");
        buffer.append(Http.CRLF);
        NntpRequest request = new NntpRequest( null, new Properties(), new ByteArrayInputStream( buffer.toString().getBytes() ) );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NntpResponse response = new NntpResponse( baos );

        NewsGroup programmer = forum.createNewsgroup("comp.lang.java.programmer");
        forum.createNewsgroup("comp.lang.ada").addArticle( NntpTestUtil.createArticle("test.eml"), "localhost" );
        forum.createNewsgroup("rec.music.makers").addArticle( NntpTestUtil.createArticle("test.eml"), "localhost" );
        forum.getNewsgroup("rec.music.makers").addArticle( NntpTestUtil.createArticle("test.eml"), "localhost" );

        assertEquals( 0, programmer.size() );

        request.nextCommand();
        ArticleHandler handler = new ArticleHandler( forum );
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
        assertTrue( baos.toString().indexOf("340 send article to be posted. End with <CR-LF>.<CR-LF>") >= 0 );
        assertTrue( baos.toString().indexOf("235 article transferred ok") >= 0 );
        programmer = forum.getNewsgroup("comp.lang.java.programmer");
        assertEquals( 1, programmer.size() );

        request.setCurrentNewsgroup("comp.lang.java.programmer");
        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
        assertTrue( baos.toString().indexOf("220") >= 0 );

    }

    public void testHandleNntp() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("article");
        buffer.append(Http.CRLF);
        buffer.append("article 1");
        buffer.append(Http.CRLF);
        buffer.append("next");
        buffer.append(Http.CRLF);
        buffer.append("last");
        buffer.append(Http.CRLF);
        buffer.append("article 1");
        buffer.append(Http.CRLF);
        buffer.append("next");
        buffer.append(Http.CRLF);
        buffer.append("last");
        buffer.append(Http.CRLF);
        buffer.append("article <blkdu9$pd8$1@hood.uits.indiana.edu>");
        buffer.append(Http.CRLF);
        buffer.append("head <blkdu9$pd8$1@hood.uits.indiana.edu>");
        buffer.append(Http.CRLF);
        buffer.append("body <blkdu9$pd8$1@hood.uits.indiana.edu>");
        buffer.append(Http.CRLF);
        buffer.append("head 1");
        buffer.append(Http.CRLF);
        buffer.append("body 1");
        buffer.append(Http.CRLF);

        NntpRequest request = new NntpRequest( null, new Properties(), new ByteArrayInputStream( buffer.toString().getBytes() ) );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NntpResponse response = new NntpResponse( baos );

        forum.createNewsgroup("comp.lang.java.programmer");
        forum.createNewsgroup("comp.lang.ada").addArticle( NntpTestUtil.createArticle("test.eml"), "localhost" );
        forum.createNewsgroup("rec.music.makers").addArticle( NntpTestUtil.createArticle("test.eml"), "localhost" );
        forum.getNewsgroup("rec.music.makers").addArticle( NntpTestUtil.createArticle("test.eml"), "localhost" );
        forum.addArticle( NntpTestUtil.createArticle("test.eml"), "localhost" );

        baos.reset();
        request.nextCommand();
        ArticleHandler handler = new ArticleHandler( forum );
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("420 no current article has been selected") >= 0 );

        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("412 no newsgroup has been selected") >= 0 );

        request.setCurrentNewsgroup("comp.lang.ada");
        request.setCurrentArticle("1");
        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("421 no next article in this group") >= 0 );

        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("422 no previous article in this group") >= 0 );

        request.setCurrentNewsgroup("rec.music.makers");
        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("220 1 <blkdu9$pd8$1@hood.uits.indiana.edu> article retrieved - head and body follow") >= 0 );

        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("223 2 <blkdu9$pd8$1@hood.uits.indiana.edu> article retrieved - request text separately") >= 0 );

        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("223 1 <blkdu9$pd8$1@hood.uits.indiana.edu> article retrieved - request text separately") >= 0 );

        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("220 0 <blkdu9$pd8$1@hood.uits.indiana.edu> article retrieved - head and body follow") >= 0 );

        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("220 0 <blkdu9$pd8$1@hood.uits.indiana.edu> article retrieved - head and body follow") >= 0 );
        assertTrue( baos.toString().indexOf("\"John C. Bollinger\" <jobollin@indiana.edu> wrote in message") == -1 );

        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("220 0 <blkdu9$pd8$1@hood.uits.indiana.edu> article retrieved - head and body follow") >= 0 );
        assertTrue( baos.toString().indexOf("Message-ID: <blkdu9$pd8$1@hood.uits.indiana.edu>") == -1 );

        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("220 1 <blkdu9$pd8$1@hood.uits.indiana.edu> article retrieved - head and body follow") >= 0 );
        assertTrue( baos.toString().indexOf("\"John C. Bollinger\" <jobollin@indiana.edu> wrote in message") == -1 );

        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
//        System.out.println(baos.toString());
        assertTrue( baos.toString().indexOf("220 1 <blkdu9$pd8$1@hood.uits.indiana.edu> article retrieved - head and body follow") >= 0 );
        assertTrue( baos.toString().indexOf("Message-ID: <blkdu9$pd8$1@hood.uits.indiana.edu>") == -1 );
    }


    public static Test suite() {
        return new TestSuite(ArticleHandlerTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
