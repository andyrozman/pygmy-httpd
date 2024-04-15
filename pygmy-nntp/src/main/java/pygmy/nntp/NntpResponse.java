package pygmy.nntp;

import pygmy.core.Response;

import java.io.*;

public class NntpResponse extends Response {

    NntpOutputStream output;

    public NntpResponse(OutputStream output) {
        this.output = new NntpOutputStream( output );
    }

    public NntpOutputStream getOutputStream() {
        return output;
    }

    public void sendResponse( int code, String[] params ) throws IOException {
        StringBuffer buffer = new StringBuffer();
        buffer.append( code );
        if( params != null ) {
            for( int i = 0; i < params.length; i++ ) {
                buffer.append( " " );
                buffer.append( params[i] );
            }
        }
        output.println( buffer.toString() );
        output.flush();
    }

    public void sendResponse( int code, String param ) throws IOException {
        sendResponse( code, new String[] { param } );
    }

    public void respondHello( String client ) throws IOException {
        sendResponse( 200, client + " news server ready - posting ok." );
    }

    public void respondHelloNoPosting( String client ) throws IOException {
        sendResponse( 201, client + " news server ready - no posting allowed." );
    }

//    public static void main(String[] args) throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        NntpResponse response = new NntpResponse( new InternetOutputStream( baos ) );
//
//        response.sendText("Foo bar" + Http.CRLF + ".You are the dude" + Http.CRLF + ".");
//
//        System.out.print( baos.toString("ASCII") );
//        NntpRequest request = new NntpRequest( new Properties(), new InternetInputStream( new ByteArrayInputStream( baos.toByteArray() ) ) );
//        System.out.print( request.readText() );
//        baos.reset();
//        response.sendText("This is retarded.  Why do they do this to me.");
//        System.out.print( baos.toString("ASCII") );
//        request = new NntpRequest( new Properties(),new InternetInputStream( new ByteArrayInputStream( baos.toByteArray() ) ) );
//        System.out.print( request.readText() );
//    }
}
