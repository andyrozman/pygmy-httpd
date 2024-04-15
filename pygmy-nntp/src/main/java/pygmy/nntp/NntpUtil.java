package pygmy.nntp;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Charlie
 * Date: Oct 15, 2003
 * Time: 9:59:41 PM
 * To change this template use Options | File Templates.
 */
public class NntpUtil {
    private static DateFormat format = new SimpleDateFormat("EE, dd-MM-yy HH:mm:ss Z");
    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();

    public static String toDateString( Date aDate ) {
        synchronized( format ) {
            return format.format( aDate );
        }
    }

    public static String base64Encode( String messageId ) {
        synchronized( encoder ) {
            return encoder.encode( messageId.getBytes() );
        }
    }

    public static byte[] base64Decode( String base64Message ) throws IOException {
        synchronized( decoder ) {
            return decoder.decodeBuffer( base64Message );
        }
    }

    public static Date toDate(String dateString) throws ParseException {
        synchronized( format ) {
            return format.parse( dateString );
        }
    }

}
