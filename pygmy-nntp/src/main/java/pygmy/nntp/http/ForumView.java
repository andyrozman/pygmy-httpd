package pygmy.nntp.http;

import pygmy.nntp.Forum;
import pygmy.nntp.NewsGroup;
import pygmy.core.HttpRequest;

import java.io.IOException;
import java.util.Iterator;

public class ForumView extends View {

    Forum forum = null;

    public ForumView(String urlPrefix, Forum forum) {
        super(urlPrefix);
        this.forum = forum;
    }

    public String getForumUrl() {
        return getForumUrl( "/" );
    }


    public String render(HttpRequest request) throws IOException {
        buffer.append( "<div class=\"box2\">" );
        buffer.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"3\" border=\"0\" >\n");
        addTableRow( "navigationbar" );
        addTableColumn( "<b><font class=\"title\">Forums</font></b>", null, "align=\"left\" colspan=\"2\"" );
        tableRowEnd();
        for( Iterator i = forum.newsgroupIterator(); i.hasNext(); ) {
            NewsGroup t = (NewsGroup) i.next();
            addTableRow( "tableheader" );
            buffer.append( "\n<td align=\"center\" valign=\"top\">" );
            buffer.append( createIcon( request.createUrl("/linkOpaque.gif"), 6, 11 ) );
            buffer.append( "</td>\n" );
            buffer.append( "\n<td nowrap align=\"left\"><p><small>");
//            createLink( t.getName(), getForumUrl( t.getUrl() ), null );
            buffer.append( "<br>&nbsp;&nbsp;created by ");
//            buffer.append( t.getCreator() );
            buffer.append( "<br>&nbsp;&nbsp;");
            buffer.append( t.size() );
            buffer.append( " Messages" );
            buffer.append( "<br>&nbsp;&nbsp;last modified (" );
            buffer.append( t.getLastModified() );
            buffer.append( ")" );
            buffer.append( "</small></p></td>\n" );
            tableRowEnd();
        }
        buffer.append("</table>\n");
        buffer.append("</div>\n");
        return buffer.toString();
    }
}
