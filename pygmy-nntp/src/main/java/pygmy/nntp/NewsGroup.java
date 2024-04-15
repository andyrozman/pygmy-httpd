package pygmy.nntp;

import pygmy.core.InternetOutputStream;
import pygmy.core.UUID;

import java.util.*;
import java.io.*;


public class NewsGroup {

    File newsgroupFile;

    String name;

    int firstIndex;

    int lastIndex;

    public NewsGroup( File newsgroupFile, String name ) {
        this.newsgroupFile = newsgroupFile;
        if( !newsgroupFile.exists() ) {
            newsgroupFile.mkdir();
        }
        this.name = name;
        String[] names = newsgroupFile.list();
        firstIndex = Integer.MAX_VALUE;
        lastIndex = 0;
        for( int i = 0; names != null && i < names.length; i++ ) {
            try {
                int index = getIndex( names[i] );
                if( index <= firstIndex ) {
                    firstIndex = index;
                }
                if( index >= lastIndex ) {
                    lastIndex = index;
                }
            } catch( NumberFormatException ignore ) {
            }
        }
    }

    public synchronized int size() {
        int size = lastIndex - firstIndex + 1;
        return size > 0 ? size : 0;
    }

    public String getName() {
        return name;
    }

    public Date getLastModified() {
        return new Date( newsgroupFile.lastModified() );
    }

    public void addArticle( Article article, String host ) throws IOException {
        if( article.getMessageId() == null ) {
            article.setMessageId( UUID.createUUID() + "@" + host );
        }
        article.addPath( host );
        article.setDateReceived( new Date() );
        article.setArticleNumber( nextIndex() );
        saveArticle(article);
    }

    private void saveArticle(Article article) throws IOException {
        File articleFile = new File( newsgroupFile, getIndexFile(article.getArticleNumber()) );
        InternetOutputStream stream = new InternetOutputStream( new FileOutputStream( articleFile ) );
        try {
            article.save( stream );
        } finally {
            stream.flush();
            stream.close();
        }
    }

    private String getIndexFile(int index) {
        return String.valueOf( index );
    }

    private int getIndex( String filename ) {
        return Integer.parseInt( filename );
    }

    private synchronized int nextIndex() {
        lastIndex++;
        if( firstIndex > lastIndex ) {
            firstIndex = lastIndex;
        }
        return lastIndex;
    }

    public synchronized int getLastIndex() {
        return lastIndex;
    }

    public synchronized int getFirstIndex() {
        return firstIndex;
    }

    public boolean getPostingAllowed() {
        return true;
    }

    public Article getMessage( int messageIndex ) throws IOException {
        NntpInputStream is = null;
        try {
            is = getMessageInputStream( messageIndex );
            Article article = new Article( is );
            article.setArticleNumber( messageIndex );
            return article;
        } finally {
            if( is != null ) {
                is.close();
            }
        }
    }

    public NntpInputStream getMessageInputStream( int messageIndex ) throws FileNotFoundException {
        return new NntpInputStream( new FileInputStream( new File( newsgroupFile, getIndexFile( messageIndex ) ) ) );
    }

    public File getDirectory() {
        return newsgroupFile;
    }

//    public Article getMessage( String messageId ) {
//        return (Article)messages.get( messageId );
//    }
}
