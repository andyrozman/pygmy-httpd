package pygmy.nntp.http;

import pygmy.core.UUID;

import java.util.*;

public class ForumMessage {

    UUID guid;

    UUID parentGuid;

    String subject;

    String contents;

    String author;

    Date postDate;

    UUID topicGuid;

    ArrayList replies = new ArrayList();

    public ForumMessage(String subject, String author, String contents ) {
        this( null, subject, author, contents );
    }

    public ForumMessage( UUID parentGuid, String subject, String author, String contents ) {
        this( UUID.createUUID(), parentGuid, subject, author, new Date(), contents );
    }

    public ForumMessage(UUID messageGuid, UUID parentGuid, String subject, String author, Date postDate, String contents ) {
        this.guid = messageGuid;
        this.parentGuid = parentGuid;
        this.subject = subject;
        this.contents = contents;
        this.author = author;
        this.postDate = postDate;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForumMessage)) return false;

        final ForumMessage forumMessage = (ForumMessage) o;

        if (!guid.equals(forumMessage.guid)) return false;

        return true;
    }

    public int hashCode() {
        return guid.hashCode();
    }

    public UUID getGuid() {
        return guid;
    }

    public UUID getParentGuid() {
        return parentGuid;
    }

    public String getContents() {
        return contents;
    }

    public String getAuthor() {
        return author;
    }

    public Date getPostDate() {
        return postDate;
    }

    public String getSubject() {
        return subject;
    }

    public void addToThread( ForumMessage message ) {
        int insertionPoint = Collections.binarySearch( replies, message, new MessageDateComparator() );
        if( insertionPoint < 0 ) {
            replies.add( Math.abs(insertionPoint + 1), message );
        }
    }

    public int getThreadSize() {
        return replies.size();
    }

    public boolean isReply() {
        return parentGuid != null;
    }

    public Iterator threadIterator() {
        return replies.iterator();
    }

    public void setTopicGuid( UUID topicGuid ) {
        this.topicGuid = topicGuid;
    }

    public String getUrl() {
        return "/read?message=" + getGuid() + "&topic=" + topicGuid;
    }

    public String getReplyUrl() {
        return "/post?message=" + getGuid() + "&topic=" + topicGuid;
    }
}
