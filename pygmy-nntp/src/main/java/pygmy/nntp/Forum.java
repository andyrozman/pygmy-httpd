package pygmy.nntp;

import java.util.*;
import java.io.*;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class Forum {

    private ArticleSpool spool;
    private File root;
    private File groupsRoot;
    private File spoolDirectory;

    private Directory articleOverviewDirectory;

    private WeakHashMap newsgroupMap;

    public Forum(File aRoot, ArticleSpool aSpool) throws IOException {
        this.root = aRoot;
        this.groupsRoot = new File( root, "news" );
        this.spoolDirectory = new File( root, "spool" );
        this.spool = aSpool;
        this.newsgroupMap = new WeakHashMap();
        this.articleOverviewDirectory  = createArticleOverviewDirectory();
    }

    private Directory createArticleOverviewDirectory() throws IOException {
        groupsRoot.mkdir();
        spoolDirectory.mkdir();
        File segments = new File( groupsRoot, "segments" );
        return FSDirectory.getDirectory( groupsRoot, segments.exists() );
    }

    public NewsGroup createNewsgroup( String newsgroupName ) {
        File newsgroupFile = new File( groupsRoot, newsgroupName );
        NewsGroup newsgroup = new NewsGroup( newsgroupFile, newsgroupName );
        newsgroupMap.put( newsgroupName, newsgroup );
        return newsgroup;
    }

    public Iterator newsgroupIterator() {
        ArrayList list = new ArrayList();
        File[] groups = groupsRoot.listFiles();
        for( int i = 0; i < groups.length; i++ ) {
            if( groups[i].isDirectory() ) {
                list.add( getNewsgroup( groups[i].getName() ) );
            }
        }

        return list.iterator();
    }

    public NewsGroup getNewsgroup( final String groupName ) {
        if( newsgroupMap.containsKey( groupName ) ) {
            return (NewsGroup) newsgroupMap.get( groupName );
        }
        String[] names = groupsRoot.list( new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if( name.equalsIgnoreCase( groupName ) ) {
                    return true;
                } else {
                    return false;
                }
            }
        } );
        if( names.length > 0 ) {
            NewsGroup newGroup = new NewsGroup( new File( groupsRoot, names[0] ), names[0] );
            newsgroupMap.put( groupName, newGroup );
            return newGroup;
        } else {
            return null;
        }
    }

    public List getNewsgroups( final Date since, final String[] distributions ) {
        final ArrayList list = new ArrayList();
        groupsRoot.listFiles( new FileFilter() {
            public boolean accept(File pathname) {
                if( pathname.isDirectory() && pathname.lastModified() >= since.getTime() && matchesDistribution( distributions, pathname ) ) {
                    list.add( getNewsgroup( pathname.getName() ) );
                    return true;
                }
                return false;
            }
        });
        return list;
    }

    public List getArticle(final Date since) {
        final ArrayList list = new ArrayList();
        spoolDirectory.listFiles( new FileFilter() {
            public boolean accept(File pathname) {
                if( pathname.isFile() && pathname.lastModified() >= since.getTime() ) {
                    list.add( pathname.getName() );
                    return true;
                }
                return false;
            }
        });
        return list;
    }

    private boolean matchesDistribution(String[] distributions, File newsgroup) {
        if( distributions == null ) return true;

        for( int i = 0; i < distributions.length; i++ ) {
            if( newsgroup.getName().startsWith( distributions[i] ) ) {
                return true;
            }
        }
        return false;
    }

    public File getNewsRepository() {
        return groupsRoot;
    }

    public File getArticleRepository() {
        return spoolDirectory;
    }

    public File getRootRepository() {
        return root;
    }

    public Article getArticle( String messageId ) throws IOException {
        NntpInputStream stream = null;
        try {
            stream = new NntpInputStream( new FileInputStream( getArticleFile( messageId ) ) );
            return new Article( stream );
        } finally {
            if( stream != null )
                stream.close();
        }
    }

    private File getArticleFile( String messageId ) {
        return new File( spoolDirectory, NntpUtil.base64Encode( messageId ) );
    }

    public void addArticle(Article article, String host) throws IOException {
        spool.addArticle( article, host );
    }

    public Directory getArticleOverview() {
        return articleOverviewDirectory;
    }

//    private void saveUniqueArticle(Article article) throws IOException {
//        File articleFile = getArticleFile(article.getMessageId());
//        InternetOutputStream stream = null;
//        try {
//            stream = new InternetOutputStream( new FileOutputStream( articleFile ) );
//            article.save( stream );
//        } finally {
//            if( stream != null ) {
//                stream.flush();
//                stream.close();
//            }
//        }
//    }

    public static void main(String[] args) {
        File dir = new File( args[0] );
        File[] files = dir.listFiles();
        for( int i = 0; i < files.length; i++ ) {
            String base64 = NntpUtil.base64Encode( "<" + files[i].getName() + ">" );
            System.out.println( files[i].getName() + ": " + base64 );
        }
    }

    public Hits getOverview( NewsGroup group ) throws IOException, ParseException {
        IndexSearcher searcher = new IndexSearcher( articleOverviewDirectory );
        Query query = QueryParser.parse( group.getName(), "newsgroup", new StandardAnalyzer());
        Hits hits = searcher.search( query );
        return hits;
    }
}
