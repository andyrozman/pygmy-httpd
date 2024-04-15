package pygmy.nntp.http;

import pygmy.core.HttpRequest;

import java.io.IOException;

public class DocumentView extends ViewDecorator {

    String css;

    public DocumentView(String urlPrefix, String css, View view ) {
        super(urlPrefix, view);
        this.css = css;
    }

    protected void addHtmlHeader( String styleSheet ) {
        buffer.append( "<html>\n");
        buffer.append( "<head>\n");
        buffer.append( "<link rel=\"stylesheet\" type=\"text/css\" href=\"" );
        buffer.append( styleSheet );
        buffer.append( "\">\n" );
        buffer.append( "</head>\n");
    }

    public String render(HttpRequest request) throws IOException {
        addHtmlHeader( css );
        buffer.append( "<body>\n" );
        buffer.append( super.render( request ) );
        buffer.append( "</body>\n" );
        buffer.append( "</html>\n" );

        return buffer.toString();
    }

}
