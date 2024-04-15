package pygmy.nntp;

import java.io.IOException;
import java.util.Iterator;

public class GroupHandler extends NntpHandler {

    Forum forum;

    public GroupHandler(Forum forum) {
        this.forum = forum;
    }

    public boolean handleNntp(NntpRequest nntpRequest, NntpResponse nntpResponse) throws IOException {
        if( nntpRequest.getCommand().equalsIgnoreCase("list") ) {
            respondNewsgroupList(nntpResponse);
            sendGroupList( nntpResponse.getOutputStream() );
        } else if( nntpRequest.getCommand().equalsIgnoreCase("group") ) {
            selectGroup(nntpRequest, nntpResponse);
        } else {
            return false;
        }
        return true;
    }

    private void selectGroup(NntpRequest nntpRequest, NntpResponse nntpResponse) throws IOException {
        String groupName = nntpRequest.getParameter(0);
        if( groupName == null || forum.getNewsgroup( groupName ) == null ) {
            respondNoSuchGroup(nntpResponse);
        } else {
            NewsGroup group = forum.getNewsgroup( groupName );
            nntpRequest.setCurrentNewsgroup( groupName );
            nntpRequest.setCurrentArticle( String.valueOf( group.getFirstIndex() ) );
            respondGroupSelected(group, nntpResponse);
        }
    }

    private void sendGroupList( NntpOutputStream writer ) throws IOException {
        for( Iterator i = forum.newsgroupIterator(); i.hasNext(); ) {
            NewsGroup group = (NewsGroup) i.next();
            writer.print( group.getName() );
            writer.print( " " );
            writer.print( group.getLastIndex() );
            writer.print( " " );
            writer.print( group.getFirstIndex() );
            writer.print( " " );
            writer.println( (group.getPostingAllowed() ? "y" : "n") );
        }
        writer.printEnd();
    }
}
