package pygmy.nntp;

import pygmy.core.HttpHeaders;
import pygmy.core.InternetOutputStream;

import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;
import java.text.ParseException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class Article {

    int articleNumber;
    HttpHeaders headers;
    String body;

    public Article( NntpInputStream stream ) throws IOException {
        headers = new HttpHeaders( stream );
        body = stream.readText();
    }

    public int getArticleNumber() {
        return articleNumber;
    }

    public String getMessageId() {
        return headers.get("Message-ID");
    }

    public HttpHeaders getHeader() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getSubject() {
        return headers.get("Subject");
    }

    public String[] getNewsgroups() {
        return headers.get("Newsgroups").split(",\\s?");
    }

    public String getFrom() {
        return headers.get("From");
    }

    public Date getDate() throws ParseException {
        return NntpUtil.toDate( headers.get("Date") );
    }

    public void setArticleNumber(int index) {
        articleNumber = index;
    }

    public void save(InternetOutputStream stream) throws IOException {
        headers.print( stream );
        stream.print( body );
    }

    public void setMessageId( String uuid ) {
        headers.put("Message-ID", uuid );
    }

    public void addXRef( NewsGroup newsGroup, int articleNumber ) {
        if( !headers.contains("XRef") ) {
            headers.put("XRef", "localhost "); //todo this should change to be the right host
        }
        String xref = getXRef();
        xref += " " + newsGroup.getName() + ":" + articleNumber;
        headers.put("XRef", xref );
    }

    public void addPath(String host) {
        String path = headers.get("Path");
        headers.put("Path", host + "!" + path );
    }

    public void setDateReceived(Date date) {
        headers.put("Date-Received", NntpUtil.toDateString( date ) );
    }

    public String getXRef() {
        return headers.get("Xref", "");
    }

    public String getReferences() {
        return headers.get("References", "");
    }

    public String getLines() {
        return headers.get("Lines", "");
    }

    public Document getOveriewDocument() {
        Document doc = new Document();
        doc.add( Field.Keyword("article.message-id", getMessageId()) );
        doc.add( Field.Text("article.subject", getSubject()) );
        doc.add( Field.Keyword("article.from", getFrom()) );
        try {
            doc.add( Field.Keyword("article.date", getDate()) );
        } catch( ParseException e ) {
        }
        doc.add( Field.Text("article.references", getReferences()) );
        doc.add( Field.UnIndexed("article.bytes", String.valueOf(getBody().length()) ) );
        doc.add( Field.UnIndexed("article.lines", getLines()) );
        doc.add( Field.UnIndexed("article.xref", getXRef()) );
        addNewsgroupArticleNumbers(doc);
        return doc;
    }

    private void addNewsgroupArticleNumbers(Document doc) {
        StringTokenizer tokenizer = new StringTokenizer( getXRef(), " " );
        tokenizer.nextToken();
        while( tokenizer.hasMoreTokens() ) {
            String newsgroup = tokenizer.nextToken(":");
            String articleNumber = tokenizer.nextToken();
            doc.add( Field.Keyword("article." + newsgroup + ".article-number", articleNumber) );
        }
    }
}
