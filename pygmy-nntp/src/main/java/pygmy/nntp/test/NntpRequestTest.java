package pygmy.nntp.test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import pygmy.nntp.NntpRequest;
import pygmy.nntp.NoCurrentNewsgroupException;
import pygmy.nntp.NoCurrentArticleException;

import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import pygmy.core.Http;

public class NntpRequestTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNextCommand() throws IOException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("list");
        buffer.append( Http.CRLF );
        buffer.append("article <foo>");
        buffer.append( Http.CRLF );
        buffer.append("foo bar baz");
        buffer.append( Http.CRLF );
        NntpRequest request = createNntpRequest(buffer);
        request.nextCommand();
        assertEquals( "list", request.getCommand() );
        assertEquals( 0, request.parameterLength() );
        assertNull( request.getParameter(0) );

        request.nextCommand();
        assertEquals( "article", request.getCommand() );
        assertEquals( 1, request.parameterLength() );
        assertEquals( "<foo>", request.getParameter(0) );

        request.nextCommand();
        assertEquals( "foo", request.getCommand() );
        assertEquals( 2, request.parameterLength() );
        assertEquals( "bar", request.getParameter(0) );
        assertEquals( "baz", request.getParameter(1) );
    }

    public void testIsDone() throws IOException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("quit");
        buffer.append( Http.CRLF );
        NntpRequest request = createNntpRequest(buffer);
        request.nextCommand();
        assertEquals( "quit", request.getCommand() );
        assertTrue( request.isDone() );
    }

    public void testGetCurrentNewsgroup() throws IOException, NoCurrentNewsgroupException, NoCurrentArticleException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("list");
        buffer.append( Http.CRLF );
        NntpRequest request = createNntpRequest(buffer);

        try {
            request.getCurrentNewsgroup();
            fail("Assert NntpRequest throws an exception when there is no current group.");
        } catch( NoCurrentNewsgroupException e ) {
            assertTrue( true );
        }

        try {
            request.getCurrentArticle();
            fail("Assert NntpRequest throws an exception when there is no current article.");
        } catch (NoCurrentArticleException e) {
            assertTrue(true);
        }

        String newsgroup = "comp.lang.java";
        request.setCurrentNewsgroup(newsgroup);
        assertEquals( "Assert current news group is " + newsgroup, newsgroup, request.getCurrentNewsgroup() );

        String articlePointer = "1";
        request.setCurrentArticle(articlePointer);
        assertEquals( "Assert current article pointer is " + articlePointer, articlePointer, request.getCurrentArticle() );
    }

    private NntpRequest createNntpRequest(StringBuffer buffer) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream( buffer.toString().getBytes() );
        NntpRequest request = new NntpRequest( null, new Properties(), bais );
        return request;
    }

    public static Test suite() {
        return new TestSuite(NntpRequestTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
