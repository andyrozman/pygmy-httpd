package pygmy.nntp.http;

import pygmy.nntp.NewsGroup;
import pygmy.core.HttpRequest;
import pygmy.core.UUID;

import java.util.Iterator;
import java.util.ListIterator;
import java.io.IOException;

public class TopicView extends View {
    NewsGroup topic;
    UUID startingMessage;
    int displayLength;

    public TopicView( String urlPrefix, NewsGroup topic, UUID startingMessage, int displayLength ) {
        super( urlPrefix );
        this.topic = topic;
        this.startingMessage = startingMessage;
        this.displayLength = displayLength;
    }

    public String render( HttpRequest request ) throws IOException {
        buffer.append( "<div class=\"box2\">" );
        buffer.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"0\" >");

        String[] headings = new String[] { "Message", "Author", "Replies", "Date" };
        addNavigationBar( request, headings );
        addTableHeaders( headings );

        addTopicTable( startingMessage, displayLength );
        addNextAndPreviousButtons( startingMessage, headings.length );
        buffer.append( "</table>\n");
        buffer.append( "</div>\n" );

        return buffer.toString();
    }

    private void addTopicTable( UUID startMessage, int length ) {
        String[] styles = { "fileentry", "altfileentry" };
        int count = 0;
//        ListIterator i = topic.getTopicIterator( startMessage );
//        for( ; i.hasNext() && count < length; count++ ) {
//            ForumMessage message = (ForumMessage) i.next();
//            addTableRow( styles[ count % 2] );
//            addTableColumn( message.getSubject(), getForumUrl( message.getUrl() ), "left" );
//            addTableColumn( message.getAuthor(), null );
//            addTableColumn( String.valueOf( message.getThreadSize() ), null );
//            addTableColumn( message.getPostDate().toString(), null );
//            tableRowEnd();
//        }
//        return (i.hasNext()) ? ((ForumMessage)i.next()).getGuid() : null;
    }

    private void addNavigationBar(HttpRequest request, String[] columns ) throws IOException {
        buffer.append( "<tr class=\"navigationbar\"><td colspan=\"");
        buffer.append( columns.length );
        buffer.append( "\">" );
        buffer.append( createIcon( request.createUrl( "/topic_icon.gif" ), 16, 16 ) );
        buffer.append( "<b><font class=\"title\">&nbsp;" );
//        createWhiteLink( topic.getName(), getForumUrl( topic.getUrl() ) );
        buffer.append( "</font></b>\n</td>\n" );
//        buffer.append( "<td align=\"right\">" );
//        createWhiteLink( "[back to fourms]", getForumUrl( "/" ) );
//        buffer.append( "</td>\n" );
        buffer.append( "</tr>\n" );
    }

    private void addNextAndPreviousButtons( UUID startMessage, int span) {
//        buffer.append( "<tr class=\"navigationbar\"><td align=\"right\" colspan=\"");
//        buffer.append( span );
//        buffer.append( "\">&nbsp;" );
//        try {
//            createWhiteLink( "[Prev]", getForumUrl( topic.getPrevUrl( startMessage, displayLength ) ) );
//        } catch( IndexOutOfBoundsException e ) {
//            buffer.append( "[Prev]" );
//        }
//        buffer.append( "&nbsp;" );
//        try {
//            createWhiteLink( "[Next]", getForumUrl( topic.getNextUrl( startMessage, displayLength ) ) );
//        } catch( IndexOutOfBoundsException e ) {
//            buffer.append( "[Next]" );
//        }
//        buffer.append( "\n</td>\n" );
    }

}

