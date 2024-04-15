package pygmy.nntp.http;

import pygmy.nntp.NewsGroup;
import pygmy.core.HttpRequest;

import java.io.IOException;

public class MessageControls extends ViewDecorator {

    ForumMessage message;
    NewsGroup topic;

    public MessageControls(String urlPrefix, View component, ForumMessage message, NewsGroup topic) {
        super(urlPrefix, component);
        this.message = message;
        this.topic = topic;
    }

    private void addMessageControls() {
        createLink("Post a Reply", getForumUrl( message.getReplyUrl() ), "replylink" );
        buffer.append("<br>");
    }

//    private void addNavigationControls() {
//        buffer.append("<table>\n");
//        addTableRow("fileentry");
//        addTableColumn("[Previous Thread]", );
//        tableRowEnd();
//        buffer.append("</table><br>\n");
//    }

    public String render(HttpRequest request) throws IOException {
//        addNavigationControls();
        buffer.append( super.render(request) );
        addMessageControls();
        return buffer.toString();
    }
}
