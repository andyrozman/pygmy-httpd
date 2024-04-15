package pygmy.nntp.http;

import pygmy.core.HttpRequest;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;

public class CompositeView extends View {

    List views = new ArrayList();
    String seperator;

    public CompositeView(String urlPrefix, String seperator) {
        super(urlPrefix);
        this.seperator = seperator;
    }

    public void addView( View view ) {
        views.add( view );
    }

    public String render(HttpRequest request) throws IOException {
        for( Iterator i = views.iterator(); i.hasNext(); ) {
            View view = (View) i.next();
            buffer.append( view.render( request ) );
            if( seperator != null && seperator != "" ) {
                buffer.append( seperator );
            }
        }
        return buffer.toString();
    }
}
