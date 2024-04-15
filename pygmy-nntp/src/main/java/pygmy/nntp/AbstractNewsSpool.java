package pygmy.nntp;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;

import java.io.IOException;

public class AbstractNewsSpool implements ArticleSpool {

    private Forum forum;
    private Analyzer ws = new WhitespaceAnalyzer();

    public AbstractNewsSpool() {
    }

    public void addArticle(Article article, String host) throws IOException {
        doAddArticle( article, host );
    }

    public void setForum(Forum aForum) {
        forum = aForum;
    }

    protected void doAddArticle(Article article, String host) throws IOException {
        String[] newsgroup = article.getNewsgroups();
        for( int i = 0; i < newsgroup.length; i++ ) {
            NewsGroup group = forum.getNewsgroup( newsgroup[i] );
            if( group != null ) {
                group.addArticle( article, host );
            }
        }
        indexArticle( article );
    }

    private void indexArticle(Article article) throws IOException {
        Document doc = article.getOveriewDocument();
        IndexWriter writer = new IndexWriter( forum.getArticleOverview(), ws, true );
        writer.addDocument( doc );
        writer.close();
    }
}
