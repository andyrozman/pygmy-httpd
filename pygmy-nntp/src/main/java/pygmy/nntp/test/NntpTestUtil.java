package pygmy.nntp.test;

import pygmy.nntp.NewsGroup;
import pygmy.nntp.Article;
import pygmy.nntp.NntpInputStream;
import pygmy.nntp.Forum;

import java.io.*;

public class NntpTestUtil {
    public static void deleteTree( File directory ) {
        File[] files = directory.listFiles();
        if( files != null ) {
            for( int i = 0; i < files.length; i++ ) {
                if( files[i].isDirectory() )
                    deleteTree( files[i] );
                else
                    if( !files[i].delete() ) {
                        System.out.println("File was not deleted! " + files[i].getAbsolutePath() );
                    }
            }
        }
        if( !directory.delete() ) {
            System.out.println("Directory was not deleted!: " + directory.getAbsolutePath() );
        }
    }

    public static NewsGroup createNewsGroup( String name ) {
        NewsGroup group = new NewsGroup( new File( System.getProperty("nntp.root"), name ), name );
        return group;
    }

    public static Article createArticle(String filename) throws IOException {
        File file = new File( System.getProperty("nntp.root"), filename );
        NntpInputStream is = null;
        try {
            is = new NntpInputStream( new FileInputStream( file ) );
            Article article = new Article( is );
            return article;
        } finally {
            if( is != null ) {
                is.close();
            }
        }
    }

    public static Forum createTestForum() throws IOException {
        if( System.getProperty("nntp.root") == null ) throw new IllegalArgumentException("This test requires that the system property nntp.root be set.");
//        return new Forum( new File( System.getProperty("nntp.root"), new TestSpool() ) );
        return null;
    }
}
