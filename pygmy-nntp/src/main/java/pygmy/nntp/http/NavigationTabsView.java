package pygmy.nntp.http;

import pygmy.core.HttpRequest;

import java.io.IOException;

public class NavigationTabsView extends ViewDecorator {

    String[] headings;
    String[] links;
    String selected;

    public NavigationTabsView(String urlPrefix, View component, String selected, String[] headings, String[] links) {
        super(urlPrefix, component);
        this.headings = headings;
        this.links = links;
        this.selected = selected;
    }

    public String render(HttpRequest request) throws IOException {
        buffer.append( "<div class=\"tabs\">" );
        for( int i = 0; i < headings.length; i++ ) {
            String clazz = "plain";
            if( headings[i].equalsIgnoreCase( selected ) ) {
                clazz = "selected";
            }
            if( links[i] == null ) {
                buffer.append( "<span class=\"" );
                buffer.append( clazz );
                buffer.append( "\">" );
                buffer.append( headings[i] );
                buffer.append("</span>\n");
            } else {
                createLink( headings[i], links[i], clazz );
            }
        }
        buffer.append( "</div>" );
        buffer.append( super.render(request) );

        return buffer.toString();
    }
}
