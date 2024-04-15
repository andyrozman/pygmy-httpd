package pygmy.nntp.http;

import pygmy.nntp.http.ForumMessage;

import java.util.Comparator;

public class MessageDateComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        ForumMessage msg1 = (ForumMessage)o1;
        ForumMessage msg2 = (ForumMessage)o2;

        if( msg1.getPostDate().after( msg2.getPostDate() ) ) {
            return -1;
        } else if( msg1.getPostDate().equals( msg2.getPostDate() ) ) {
            return msg1.getGuid().compareTo( msg2.getGuid() );
        } else {
            return 1;
        }
    }
}
