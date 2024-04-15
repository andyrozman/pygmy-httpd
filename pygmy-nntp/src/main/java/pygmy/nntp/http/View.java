package pygmy.nntp.http;

import pygmy.core.HttpRequest;

import java.io.IOException;

public abstract class View {
    StringBuffer buffer = new StringBuffer();
    String urlPrefix;

    public View(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public abstract String render( HttpRequest request ) throws IOException;

    protected String getForumUrl(String relativeUrl) {
        return urlPrefix + relativeUrl;
    }

    protected void createLink( String text, String url, String clazz ) {
        buffer.append( "<a href=\"" );
        buffer.append( url );
        buffer.append( "\"" );
        if( clazz != null ) {
            buffer.append( " class=\"" );
            buffer.append( clazz );
            buffer.append( "\"");
        }
        buffer.append( ">" );
        buffer.append( text );
        buffer.append( "</a>\n" );
    }

    protected void createWhiteLink( String text, String url ) {
        createLink( text, url, "whitelink" );
    }

    protected void addTableHeaders( String[] headers ) {
        buffer.append("<tr class=\"tableheader\">\n");
        for( int i = 0; i < headers.length; i++ ) {
            buffer.append("<td align=\"center\">");
            buffer.append( headers[i] );
            buffer.append( "</td>\n");
        }
        buffer.append("</tr>\n");
    }
    protected void addTableColumn( String text, String url ) {
        addTableColumn( text, url, "center" );
    }

    protected void addTableColumnOptions( String text, String url, String options ) {
        buffer.append( "\n<td ");
        buffer.append( options );
        buffer.append( ">&nbsp;<small>" );
        if( url != null ) {
            createLink( text, url, null );
        } else {
            buffer.append( text );
        }
        buffer.append( "</small></td>\n" );
    }


    protected void addTableColumn( String text, String url, String alignment ) {
        addTableColumnOptions( text, url, "align=\"" + alignment + "\"" );
    }

    protected void addTableRow( String rowStyle ) {
        buffer.append( "<tr class=\"");
        buffer.append( rowStyle );
        buffer.append( "\">\n");
    }

    protected void addTableRow( String rowStyle, int colspan ) {
        buffer.append( "<tr class=\"");
        buffer.append( rowStyle );
        buffer.append( "\" colspan=\"" );
        buffer.append( colspan );
        buffer.append( "\">\n");
    }

    protected void tableRowEnd() {
        buffer.append("</tr>\n");
    }

    protected void createTextField( String name, String defaultValue, int size ) {
        buffer.append("<input type=\"text\" name=\"");
        buffer.append( name );
        buffer.append( "\" size=\"" );
        buffer.append( size );
        buffer.append( "\" ");
        if( defaultValue != null ) {
            buffer.append( "value=\"" );
            buffer.append( defaultValue );
            buffer.append( "\"" );
        }
        buffer.append(">");
    }

    protected void createHiddenField( String name, Object value ) {
        buffer.append("<input type=\"hidden\" name=\"" );
        buffer.append( name );
        buffer.append("\" value=\"" );
        buffer.append( value.toString() );
        buffer.append( "\">" );
    }

    protected String createIcon( String iconUrl, int width, int height ) {
        StringBuffer buf = new StringBuffer( "<img src=\"" );
        buf.append( iconUrl );
        buf.append( "\" width=\"" );
        buf.append( width );
        buf.append( "\" height=\"" );
        buf.append( height );
        buf.append( "\" border=\"0\">");
        return buf.toString();
    }

}
