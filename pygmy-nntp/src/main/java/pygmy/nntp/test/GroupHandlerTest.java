package pygmy.nntp.test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import pygmy.core.Http;
import pygmy.nntp.NntpRequest;
import pygmy.nntp.NntpResponse;
import pygmy.nntp.GroupHandler;

import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class GroupHandlerTest extends NntpHandlerTestCase {

    public void testHandleNntp() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("list");
        buffer.append(Http.CRLF);
        buffer.append("list");
        buffer.append(Http.CRLF);
        buffer.append("group comp.lang.java");
        buffer.append(Http.CRLF);
        buffer.append("group foo.bar.baz");
        buffer.append(Http.CRLF);
        NntpRequest request = new NntpRequest( null, new Properties(), new ByteArrayInputStream( buffer.toString().getBytes() ) );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NntpResponse response = new NntpResponse( baos );

        request.nextCommand();
        GroupHandler handler = new GroupHandler( forum );
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
        assertTrue( baos.toString().indexOf("215 list of newsgroups follows" + Http.CRLF + ".") >= 0 );

        forum.createNewsgroup("comp.lang.java");
        forum.createNewsgroup("comp.lang.ada").addArticle( NntpTestUtil.createArticle("test.eml"), "localhost" );
        forum.createNewsgroup("rec.music.makers").addArticle( NntpTestUtil.createArticle("test.eml"), "localhost" );
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
        assertTrue( baos.toString().indexOf("215 list of newsgroups follows") >= 0 );

        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
        assertTrue( baos.toString().indexOf("211 0 2147483647 0 comp.lang.java") >= 0 );

        baos.reset();
        request.nextCommand();
        assertTrue( "Assert that the handler handled the request.", handler.handleNntp( request, response ) );
        assertTrue( baos.toString().indexOf("411 no such news group") >= 0 );
    }

    public static Test suite() {
        return new TestSuite(GroupHandlerTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
