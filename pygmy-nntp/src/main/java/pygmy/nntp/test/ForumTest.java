package pygmy.nntp.test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import pygmy.nntp.Forum;
import pygmy.nntp.NewsGroup;
import pygmy.nntp.Article;
import pygmy.nntp.NntpUtil;

import java.io.File;
import java.util.Iterator;
import java.util.Date;
import java.util.List;

public class ForumTest extends TestCase {

    Forum forum;

    protected void setUp() throws Exception {
        forum = NntpTestUtil.createTestForum();
    }

    protected void tearDown() throws Exception {
        File[] groups = forum.getNewsRepository().listFiles();
        for( int i = 0; i < groups.length; i++ ) {
            if( groups[i].isDirectory() ) {
                NntpTestUtil.deleteTree( groups[i] );
            }
        }
    }

    public void testCreateNewsgroup() throws Exception {
        String name = "comp.lang.java";
        NewsGroup group = forum.createNewsgroup(name);
        assertEquals( name, group.getName() );

        NewsGroup sameGroup = forum.getNewsgroup( group.getName() );
        assertEquals( group.getName(), sameGroup.getName() );
        assertEquals( group.getFirstIndex(), sameGroup.getFirstIndex() );
        assertEquals( group.getLastIndex(), sameGroup.getLastIndex() );
        assertEquals( group.size(), sameGroup.size() );
    }

    public void testAddArticle() throws Exception {
        String name = "comp.lang.java.programmer";
        NewsGroup group = forum.createNewsgroup(name);
        Article article = NntpTestUtil.createArticle("test.eml");
        forum.addArticle( article, "foo");
        File repository = forum.getArticleRepository();

        File[] list = repository.listFiles();
        assertEquals( NntpUtil.base64Encode( article.getMessageId() ), list[0].getName() );
        assertEquals( 1, group.size() );
    }


    public void testGroupIterator() throws Exception {
        String[] groups = createSomeNewsGroups();

        for( Iterator it = forum.newsgroupIterator(); it.hasNext(); ) {
            Object next = it.next();
            assertTrue( "Assert we get newsgroup instances.", next instanceof NewsGroup );
            assertNewsGroupContained( groups, (NewsGroup)next );
        }
    }

    public void testGetMessageSince() {
        String groups[] = createSomeNewsGroups();

        NewsGroup group = forum.getNewsgroup( groups[0] );
        group.getDirectory().setLastModified( 1L );

        List list = forum.getNewsgroups( new Date(0), null );
        assertEquals( "Assert we get all the groups", groups.length, list.size() );

        list = forum.getNewsgroups( new Date(2), null );
        assertEquals( "Assert we get all the groups but the first", groups.length - 1, list.size() );
        for (int it = 0; it < list.size(); it++) {
            NewsGroup newsgroup = (NewsGroup) list.get(it);
            assertNewsGroupContained(groups, newsgroup);
        }

        list = forum.getNewsgroups( new Date(2), new String[] { "comp." } );
        assertEquals( "Assert we get only comp.lang.ada", 1, list.size() );
        assertEquals( "Assert we get only comp.lang.ada", "comp.lang.ada", ((NewsGroup)list.get(0)).getName() );
    }

    private void assertNewsGroupContained(String[] groups, NewsGroup newsgroup) {
        boolean found = false;
        for( int i = 0; i < groups.length; i++ ) {
            if( groups[i].equals( newsgroup.getName() ) ) {
                found = true;
            }
        }
        assertTrue( "Assert newsgroup " + newsgroup.getName() + " is in the list.", found );
    }

    private String[] createSomeNewsGroups() {
        String[] groups = { "comp.lang.java", "comp.lang.ada", "alt.blah.blah", "alt.hack" };
        for( int i = 0; i < groups.length; i++ ) {
            forum.createNewsgroup( groups[i] );
        }
        return groups;
    }

    public static Test suite() {
        return new TestSuite(ForumTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
