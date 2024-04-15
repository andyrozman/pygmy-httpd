package pygmy.core;

import java.io.IOException;
import java.io.OutputStream;

public interface ResponseData {

    long getLength();

    void send(OutputStream os) throws IOException;

}
