package pygmy.nntp.http;

import pygmy.core.HttpRequest;

import java.io.IOException;

import pygmy.nntp.NewsGroup;

public class PostView extends View {
    NewsGroup topic;
    ForumMessage message;

    public PostView(String urlPrefix, NewsGroup topic, ForumMessage aMessage ) {
        super( urlPrefix );
        this.topic = topic;
        this.message = aMessage;
    }

    public String render(HttpRequest request) throws IOException {
        buffer.append( "<div class=\"box2\">" );
        buffer.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"0\" >\n");
        startPost();
        addHeader();
        addContents();
        // todo replace with template file.
        buffer.append( "</table>\n");
        buffer.append( "</div>\n" );
        return buffer.toString();

    }

    private void addContents() {
        addTableRow( "fileentry" );
        buffer.append("<td>\n");
        buffer.append("<textarea name=\"contents\" cols=\"80\" rows=\"20\">" );
        if( message != null ) {
            buffer.append( message.getContents() );
        }
        buffer.append("</textarea>\n<br>");
        buffer.append("<input type=\"submit\" name=\"preview\" value=\"Preview\">&nbsp;");
        buffer.append("<input type=\"submit\" name=\"submit\" value=\"Post Message\">&nbsp;");
        buffer.append("</td>\n");
        tableRowEnd();
        buffer.append("</form>\n");
    }

    private void startPost() {
        buffer.append("<form action=\"" );
        buffer.append( getForumUrl( "/post" ) );
        buffer.append( "\" method=\"post\" name=\"messageForm\">\n");
//        createHiddenField( "topic", topic.getGuid() );
        if( message != null && message.getParentGuid() != null ) {
            createHiddenField("message", message.getParentGuid() );
        }
        addTableRow( "navigationbar" );
        buffer.append( "\n<td nowrap align=\"left\">" );
        buffer.append( "<font size=\"+1\" class=\"title\">" );
        if( message != null ) {
            buffer.append( message.getSubject() );
        } else {
            buffer.append("Post a Message");
        }
        buffer.append( "</font>");
        buffer.append( "</td>\n" );
        tableRowEnd();
    }

    private void addHeader() {
        addTableRow("tableheader");
        buffer.append("<td>\n");

        buffer.append("<table>\n");
        buffer.append("<tr>\n");
        buffer.append("\n<td>\n<b>Subject:</b></td>\n");
        buffer.append("<td>");
        if( message != null ) {
            createTextField( "subject", message.getSubject() ,60 );
        } else {
            createTextField( "subject", "", 60 );
        }
        buffer.append("</td>\n");
        tableRowEnd();

        buffer.append("<tr>\n");
        buffer.append("\n<td><b>Author:</b></td>\n");
        buffer.append("<td>");
        if( message != null ) {
            createTextField( "author", message.getAuthor(), 60 );
        } else {
            createTextField( "author", null, 60 );
        }
        buffer.append("</td>\n");
        tableRowEnd();
        buffer.append("</table>\n");

        buffer.append("</td>\n");
        tableRowEnd();
    }

}
