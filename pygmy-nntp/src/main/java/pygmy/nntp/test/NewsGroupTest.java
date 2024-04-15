package pygmy.nntp.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import pygmy.nntp.NewsGroup;
import pygmy.nntp.Article;

import java.io.IOException;

public class NewsGroupTest extends TestCase {

    NewsGroup group;
    String groupName = "comp.lang.java";

    protected void setUp() throws Exception {
        System.out.println("NewsGroupTest.setUp");
        group = NntpTestUtil.createNewsGroup( groupName );
    }

    protected void tearDown() throws Exception {
        System.out.println("NewsGroupTest.tearDown");
        NntpTestUtil.deleteTree( group.getDirectory() );
    }

    public void testConstruction() throws Exception {
        System.out.println("NewsGroupTest.testConstruction");
        assertEquals( "Assert that the newsgroup name is what we created it with.", groupName, group.getName() );
        assertTrue( "Assert that an empty group's firstIndex is greater than the lastIndex", group.getFirstIndex() > group.getLastIndex() );
        assertEquals( "Assert that the size of the newsgroup is zero.", 0, group.size() );
    }

    public void testAddArticle() throws Exception {
        System.out.println("NewsGroupTest.testAddArticle");
        assertEquals( 0, group.size() );

        Article article = addTestArticle();
        assertEquals( 1, group.size() );
        assertEquals( group.getFirstIndex(), group.getLastIndex() );
        assertEquals( group.getLastIndex(), article.getArticleNumber() );

        Article article2 = NntpTestUtil.createArticle("test.eml");
        article2.setMessageId(null);
        String oldPath2 = article2.getHeader().get("Path");
        group.addArticle( article2, "localhost" );
        assertEquals( 2, group.size() );
        assertTrue( "Assert that first and last indexes are different after adding two articles.", group.getFirstIndex() != group.getLastIndex() );
        assertEquals( "Assert the first article is the firstIndex", group.getFirstIndex(), article.getArticleNumber() );
        assertEquals( "Assert the last article is the last index", group.getLastIndex(), article2.getArticleNumber() );
        assertTrue( "Assert the old path is contained with the new path, and it doesn't start at index 0.", article2.getHeader().get("Path").indexOf(oldPath2) > 0 );
        assertTrue( "Assert the old path starts with localhost.", article2.getHeader().get("Path").startsWith("localhost") );
        assertNotNull( "Assert messsage ID is not NULL.", article2.getMessageId() );
        assertNotNull( "Assert Date-Received is not NULL", article2.getHeader().get("Date-Received") );
    }

    private Article addTestArticle() throws IOException {
        Article article = NntpTestUtil.createArticle("test.eml");
        group.addArticle( article, "localhost" );
        return article;
    }

    public void testGetMessage() throws Exception {
        System.out.println("NewsGroupTest.testGetMessage");

        Article article = addTestArticle();
        Article sameArticle = group.getMessage( article.getArticleNumber() );

        assertEquals( "Assert that their message IDs are the same.  Assume the rest is the same.", sameArticle.getMessageId(), article.getMessageId() );
        assertEquals( "Assert that their article numbers are the same.  Assume the rest is the same.", sameArticle.getArticleNumber(), article.getArticleNumber() );
    }

    public static Test suite() {
        return new TestSuite( NewsGroupTest.class );
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run( suite() );
    }
}
