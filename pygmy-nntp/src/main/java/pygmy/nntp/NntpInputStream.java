package pygmy.nntp;

import pygmy.core.InternetInputStream;
import pygmy.core.Http;

import java.io.IOException;
import java.io.InputStream;

public class NntpInputStream extends InternetInputStream {

    public NntpInputStream(InputStream in) {
        super(in);
    }

    public String readText() throws IOException {
        StringBuffer buffer = new StringBuffer();
        while( true ) {
            StringBuffer line = readBuffer();
            if( line == null || ( line.length() == 1 && line.charAt(0) == '.') ) {
                break;
            } else if( line.length() > 1 && line.charAt(0) == '.' && line.charAt(1) == '.' ) {
                line.deleteCharAt(0);
                buffer.append( line );
            } else {
                buffer.append( line );
            }
            buffer.append( Http.CRLF );
        }
        return buffer.toString();
    }
}
