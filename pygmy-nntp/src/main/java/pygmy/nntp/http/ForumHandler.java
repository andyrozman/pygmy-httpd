package pygmy.nntp.http;

import pygmy.nntp.Forum;
import pygmy.nntp.NewsGroup;
import pygmy.nntp.http.ForumView;
import pygmy.core.HttpRequest;
import pygmy.core.HttpResponse;
import pygmy.core.UUID;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ForumHandler {//extends AbstractXmlHandler {

    public static final String CSS = "css";

    private Forum forum;

    public ForumHandler() {
//        forum = new Forum();
//
//        NewsGroup t = forum.createNewsgroup( "Foo", "Dude" );
//        System.out.println("t.getGuid() = " + t.getGuid());
//        ForumMessage message = new ForumMessage("This is a subject", "charlie", "Read my contents.");
//        ForumMessage message1 = new ForumMessage( message.getGuid(), "Bring it", "charlie", "Read my contents.");
//        ForumMessage message2 = new ForumMessage("What is this", "charlie", "Read my contents.");
//        ForumMessage message3 = new ForumMessage( message.getGuid(), "DotCrap!", "charlie", "Read my contents.");
//        ForumMessage message4 = new ForumMessage("What is this shit", "charlie", "Read my contents.");
//        ForumMessage message5 = new ForumMessage("Football is cool.", "charlie", "Read my contents.");
//        ForumMessage message6 = new ForumMessage("I have nothing of interest to say", "charlie", "Read my contents.");
//        ForumMessage message7 = new ForumMessage("work sucks.  I know.", "charlie", "Read my contents.");

//        t.addMessage( message );
//        t.addMessage( message1 );
//        t.addMessage( message2 );
//        t.addMessage( message3 );
//        t.addMessage( message4 );
//        t.addMessage( message5 );
//        t.addMessage( message6 );
//        t.addMessage( message7 );
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        if( request.getUrl().endsWith("post") && request.getMethod().equalsIgnoreCase("GET") ) {
            return createPost( request, response );
        } else if( request.getUrl().endsWith("post") && request.getMethod().equalsIgnoreCase("POST") ) {
            return postMessage( request, response );
        } else if( request.getUrl().endsWith( "read") ) {
            return readMessage( request, response );
        } else if( request.getUrl().endsWith("topic") ) {
            return readTopic( request, response );
        } else {
            return readForum( request, response );
        }
    }

    private boolean postMessage(HttpRequest request, HttpResponse response) throws IOException {
        String subject = request.getParameter("subject");
        String author = request.getParameter("author");
        String messageUuidStr = request.getParameter("message");
        String topicUuidStr = request.getParameter("topic");
        String contents = request.getParameter("contents");

//        if( topicUuidStr == null ) return readForum( request, response );

        UUID topicUuid = UUID.parse( topicUuidStr );
        UUID parentMessage = null;
        if( messageUuidStr != null ) {
            parentMessage = UUID.parse( messageUuidStr );
        }

//        NewsGroup topic = forum.getTopic( topicUuid );
        if( request.getParameter("preview") == null ) {
//            showMessage( topicUuid, topic.addMessage( new ForumMessage( parentMessage, subject, author, contents ) ), request, response );
        } else {
//            previewMessage( topic, parentMessage, author, subject, contents, request, response );
        }
        return true;
    }

//    private void previewMessage(NewsGroup topic, UUID parentMessage, String author, String subject, String contents, HttpRequest request, HttpResponse response ) throws IOException {
//        CompositeView composite = new CompositeView( getUrlPrefix(), "<p>" );
//        ForumMessage preview = new ForumMessage( parentMessage, subject, author, contents );
//        composite.addView( createPostView( topic, preview ) );
//        composite.addView( new MessageView( getUrlPrefix(), topic, preview ) );
//        View view = new DocumentView( getUrlPrefix(), getStyleSheet(request), composite );
//        response.sendResponse( HttpURLConnection.HTTP_OK, "text/html", view.render( request ) );
//    }
//
    private boolean readForum(HttpRequest request, HttpResponse response) throws IOException {
//        ForumView forumView = new ForumView( getUrlPrefix(), forum );
//        NavigationTabsView tabs = new NavigationTabsView( getUrlPrefix(), forumView, "Forums", new String[] { "Forums" }, new String[] { forumView.getForumUrl() } );
//        View view = new DocumentView( getUrlPrefix(), getStyleSheet(request), tabs );
//        response.sendResponse( HttpURLConnection.HTTP_OK, "text/html", view.render( request ) );
        return true;
    }

    private boolean readTopic(HttpRequest request, HttpResponse response) throws IOException {
        String messageStart = request.getParameter("messageStart");
        String numberToDisplay = request.getParameter("display");
        UUID topicUuid = UUID.parse( request.getParameter("topic") );

        UUID startingMessage = null;
        if( messageStart == null ) {
//            startingMessage = forum.getTopic( topicUuid ).first();
        } else {
            startingMessage = UUID.parse( messageStart );
        }

        int displayLength = 20;
        if( numberToDisplay != null ) {
            displayLength = Integer.parseInt( numberToDisplay );
        }

//        NewsGroup topic = forum.getTopic( topicUuid );
//        TopicView topicView = new TopicView( getUrlPrefix(), topic, startingMessage, displayLength );
//        NavigationTabsView tabs = new NavigationTabsView( getUrlPrefix(), topicView, "NewsGroup", new String[] { "Forums", "NewsGroup", "Post" }, new String[] { getUrlPrefix() + "/", null, getUrlPrefix() + "/post?topic=" + topic.getGuid() } );

//        View view = new DocumentView( getUrlPrefix(), getStyleSheet(request), tabs );
//        response.sendResponse( HttpURLConnection.HTTP_OK, "text/html", view.render(request) );
        return true;
    }

    private boolean readMessage(HttpRequest request, HttpResponse response) throws IOException {
        String messageStr = request.getParameter("message");
        String topicStr = request.getParameter("topic");
        if( messageStr == null ) return false;

        UUID message = UUID.parse( messageStr );
        UUID topicUuid = UUID.parse( topicStr );

        showMessage(topicUuid, message, request, response);
        return true;
    }

    private void showMessage(UUID topicUuid, UUID messageGuid, HttpRequest request, HttpResponse response) throws IOException {
//        NewsGroup topic = forum.getTopic( topicUuid );
//        CompositeView composite = new CompositeView( getUrlPrefix(), "<p>" );
//        ForumMessage message = topic.getMessage( messageGuid );
//        MessageControls controls = new MessageControls( getUrlPrefix(), new MessageView( getUrlPrefix(), topic, message ), message, topic );
//        NavigationTabsView tabs = new NavigationTabsView( getUrlPrefix(), controls, "Message", new String[] { "Forums", "NewsGroup", "Message" }, new String[] { getUrlPrefix() + "/", getUrlPrefix() + topic.getUrl(), null } );
//        composite.addView( tabs );
//        composite.addView( new ThreadMapView( getUrlPrefix(), topic, message ) );
//        View view = new DocumentView( getUrlPrefix(), getStyleSheet(request), composite );
//        response.sendResponse( HttpURLConnection.HTTP_OK, "text/html", view.render( request ) );
    }

    private boolean createPost(HttpRequest request, HttpResponse response) throws IOException {
//        String messageStr = request.getParameter("message");
//        String topicStr = request.getParameter("topic");
//
//        UUID topicUuid = UUID.parse( topicStr );
//        NewsGroup topic = forum.getTopic( topicUuid );
//
//        UUID parentGuid = null;
//        if( messageStr != null ) {
//            parentGuid = UUID.parse( messageStr );
//        }
//
//        ForumMessage message = null;
//        if( parentGuid != null ) {
//            ForumMessage parentMessage = topic.getMessage( parentGuid );
//            message = new ForumMessage( parentMessage.getGuid(), "Re: " + parentMessage.getSubject(), "", parentMessage.getContents() );
//        }
//        View tabs = createPostView(topic, message);
//        View view = new DocumentView( getUrlPrefix(), getStyleSheet(request), tabs );
//        response.sendResponse( HttpURLConnection.HTTP_OK, "text/html", view.render( request ) );
        return true;
    }

    private View createPostView(NewsGroup topic, ForumMessage message) {
//        String[] headings = null;
//        String[] links = null;
//        if( message == null ) {
//            headings = new String[] { "Forums", "NewsGroup", "Post" };
//            links = new String[] { getUrlPrefix() + "/", getUrlPrefix() + topic.getUrl(), null };
//        } else {
//            headings = new String[] { "Forums", "NewsGroup", "Message", "Post" };
//            links = new String[] { getUrlPrefix() + "/", getUrlPrefix() + topic.getUrl(), getUrlPrefix() + topic.getMessage( message.getParentGuid() ).getUrl() , null };
//        }
//        PostView postView = new PostView( getUrlPrefix(), topic, message );
//        NavigationTabsView tabs = new NavigationTabsView( getUrlPrefix(), postView, "Post", headings, links );
//        return tabs;
        return null;
    }

//    private String getStyleSheet(HttpRequest request) throws IOException {
//        return request.createUrl( server.getHandlerProperty( handlerName, CSS, "/style_ie.css" ) );
//    }
}
