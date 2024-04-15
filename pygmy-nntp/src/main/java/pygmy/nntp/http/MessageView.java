package pygmy.nntp.http;

import pygmy.core.HttpRequest;

import java.io.IOException;
import java.util.StringTokenizer;

import pygmy.nntp.NewsGroup;

public class MessageView extends View {
    NewsGroup topic;
    ForumMessage message;

    public MessageView(String urlPrefix, NewsGroup topic, ForumMessage message ) {
        super( urlPrefix );
        this.topic = topic;
        this.message = message;
    }

    public String render(HttpRequest request) throws IOException {
        renderMessage();
        return buffer.toString();
    }

    private void renderMessage() {
        buffer.append( "<div class=\"box2\">" );
        buffer.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"0\" >\n");
        startMessage();
        addMessageHeader();
        addMessageContents();
        buffer.append( "</table>\n");
        buffer.append( "</div>\n" );
    }

    private void startMessage() {
        addTableRow( "navigationbar" );
        buffer.append( "\n<td nowrap align=\"left\">" );
        buffer.append( "<font size=\"+1\" class=\"title\">" );
        buffer.append( message.getSubject() );
        buffer.append( "</font>");
        buffer.append( "</td>\n" );
        tableRowEnd();
    }

    private void addMessageHeader() {
        addTableRow( "tableheader" );
        buffer.append( "<td>" );
        buffer.append( "<b>Author:</b>&nbsp;");
        buffer.append( message.getAuthor() );
        buffer.append( "</br>" );
        buffer.append( "<b>Date:</b>&nbsp;" );
        buffer.append( message.getPostDate() );
        buffer.append( "</br>" );
        buffer.append( "</td>" );
        tableRowEnd();
    }

    private void addMessageContents() {
        addTableRow( "fileentry" );
        buffer.append("<td width=\"100%\" cellpadding=\"5\">");
        buffer.append("<br>");
        buffer.append( renderContents( message.getContents() ) );
        buffer.append("</td>\n");
        tableRowEnd();
    }

    private String renderContents(String contents) {
        StringBuffer buffer = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer( contents, "\n\r");
        while( tokenizer.hasMoreTokens() ) {
            buffer.append( tokenizer.nextToken() );
            buffer.append( "<p>" );
        }

        return buffer.toString();
    }

}

