package pygmy.nntp.test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import pygmy.nntp.Article;

import java.io.File;
import java.io.IOException;

public class ArticleTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testArticle() throws IOException {
        Article article = NntpTestUtil.createArticle("test.eml");

        assertEquals( "1.0", article.getHeader().get("Mime-Version") );
        assertEquals( "<blkdu9$pd8$1@hood.uits.indiana.edu>", article.getMessageId() );
        assertEquals( "Re: Static inner classes", article.getSubject() );
        String[] newsgroups = { "comp.lang.java.programmer" };
        for( int i = 0; i < newsgroups.length; i++ ) {
            assertEquals( newsgroups[i], article.getNewsgroups()[i] );
        }
    }

    public static Test suite() {
        return new TestSuite(ArticleTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
