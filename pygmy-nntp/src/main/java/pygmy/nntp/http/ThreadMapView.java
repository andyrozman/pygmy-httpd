package pygmy.nntp.http;

import pygmy.nntp.NewsGroup;
import pygmy.nntp.http.View;

import java.util.Iterator;
import java.io.IOException;

import pygmy.core.HttpRequest;

public class ThreadMapView extends View {

    NewsGroup topic;
    ForumMessage message;

    public ThreadMapView(String urlPrefix, NewsGroup topic, ForumMessage message) {
        super(urlPrefix);
        this.topic = topic;
        this.message = message;
    }

    public String render(HttpRequest request) throws IOException {
        addThreadSummary();
        return buffer.toString();
    }

    private void addThreadSummary() {
//        buffer.append("<p><p>\n<table width=\"100%\" cellspacing=\"0\" cellpadding=\"1\" border=\"0\" bgcolor=\"#000000\">\n<tr>\n<td>\n<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"0\" >\n");
//        addTableRow( "navigationbar" );
//        buffer.append("<td align=\"left\"><font class=\"title\">Threads</font></td>\n");
//        tableRowEnd();
//        addTableRow("fileentry");
        buffer.append( "<div class=\"box\">\n" );
        buffer.append( "<h5>Threads</h5>\n" );
        buffer.append( "<div class=\"body\">\n" );
        ForumMessage parent = getRootMessage( message );
        renderThreadTree( parent, 0 );
        buffer.append( "</div>\n" );
        buffer.append( "</div>\n" );
//        tableRowEnd();
//        buffer.append( "</table></td></tr></table>\n");
    }

    private ForumMessage getRootMessage( ForumMessage aMessage ) {
        ForumMessage parent = aMessage;
        while( parent.isReply() ) {
//            parent = topic.getMessage( parent.getParentGuid() );
        }
        return parent;
    }

    private void renderThreadTree(ForumMessage parent, int indention ) {
        String clazz = "even";
        if( parent.equals( message ) ) {
            clazz = "selected";
        }
        buffer.append("<div class=\"" );
        buffer.append( clazz );
        buffer.append( "\">" );

        for( int i = 0; i < indention; i++) {
            buffer.append("&nbsp;");
        }
        createLink( parent.getSubject(), getForumUrl( parent.getUrl() ), null );
        buffer.append( "<br>" );
        for( int i = 0; i < indention; i++ ) {
            buffer.append("&nbsp;");
        }
        buffer.append("<small>Posted by ");
        buffer.append( message.getAuthor() );
        buffer.append( " on " );
        buffer.append( message.getPostDate() );
        buffer.append( "</small>\n" );
        buffer.append( "</div>\n" );

        for( Iterator i = parent.threadIterator(); i.hasNext(); ) {
            renderThreadTree( (ForumMessage)i.next(), indention + 5 );
        }
    }
}
