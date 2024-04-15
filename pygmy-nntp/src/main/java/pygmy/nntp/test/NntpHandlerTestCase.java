package pygmy.nntp.test;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import pygmy.nntp.Forum;
import pygmy.nntp.Article;
import pygmy.core.InternetOutputStream;

public abstract class NntpHandlerTestCase extends TestCase {
    Forum forum;

    protected void setUp() throws Exception {
        forum = NntpTestUtil.createTestForum();
    }

    protected void tearDown() throws Exception {
        File[] list = forum.getRootRepository().listFiles();
        if( list != null ) {
            for (int i = 0; i < list.length; i++) {
                File file = list[i];
                if( file.isDirectory() ) NntpTestUtil.deleteTree( file );
            }
        }
    }

    protected byte[] getArticleBytes( String filename ) throws IOException {
        Article article = NntpTestUtil.createArticle( filename );
        ByteArrayOutputStream articleBaos = new ByteArrayOutputStream();
        article.save( new InternetOutputStream( articleBaos ) );
        return articleBaos.toByteArray();
    }
}
