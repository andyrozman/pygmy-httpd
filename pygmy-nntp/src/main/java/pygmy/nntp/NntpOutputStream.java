package pygmy.nntp;

import pygmy.core.InternetOutputStream;

import java.io.IOException;
import java.io.OutputStream;

public class NntpOutputStream extends InternetOutputStream {

    String seperator;

    public NntpOutputStream( OutputStream out ) {
        super( out );
    }

    public void print( String text, int start, int length ) throws IOException {
        if( text == null ) {
            super.print( "null" );
        } else {
            encodeText( text, start, length );
        }
    }

    private void encodeText( String text, int begin, int length ) throws IOException {
        int tag = 0;
        int start = begin;
        while( start < length ) {
            tag = text.indexOf( seperator + ".", start );
            if( tag < 0 ) {
                tag = length;
                write( text.getBytes(), start, tag - start );
            } else {
                tag += 3;
                write( text.getBytes(), start, tag - start );
                print( "." );
            }
            start = tag;
        }
    }

    public void printEnd() throws IOException {
//        println();
        println(".");
        flush();
    }
}
