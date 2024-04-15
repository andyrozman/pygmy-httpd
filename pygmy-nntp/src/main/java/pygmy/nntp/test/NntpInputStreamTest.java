package pygmy.nntp.test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import pygmy.nntp.NntpInputStream;

import java.io.ByteArrayInputStream;

import pygmy.core.Http;

public class NntpInputStreamTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testReadText() throws Exception {
        String testText = "Fry told him to go back to the jungle." + Http.CRLF;

        StringBuffer buffer = new StringBuffer();
        buffer.append(testText);
        writeTextEnd(buffer);

        String testText2 = ".Gunter wanted to be an ape of moderate intelligence...." + Http.CRLF;
        buffer.append( "." );
        buffer.append( testText2 );
        writeTextEnd(buffer);

        String testText3 = "." + Http.CRLF;
        buffer.append( "." );
        buffer.append( testText3 );
        writeTextEnd(buffer);

        NntpInputStream stream = new NntpInputStream( new ByteArrayInputStream( buffer.toString().getBytes() ) );
        assertEquals( testText, stream.readText() );
        assertEquals( testText2,  stream.readText() );
        assertEquals( testText3,  stream.readText() );
    }

    private void writeTextEnd(StringBuffer buffer) {
        buffer.append(".");
        buffer.append(Http.CRLF);
    }

    public static Test suite() {
        return new TestSuite(NntpInputStreamTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
