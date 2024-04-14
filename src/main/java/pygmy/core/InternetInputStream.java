package pygmy.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class InternetInputStream extends PushbackInputStream {

    public static final int BUFFER_LENGTH = 8096;

    public InternetInputStream(InputStream in) {
        super(in, BUFFER_LENGTH);
    }

    public String readline() throws IOException {
        StringBuilder buf = readBuffer();
        if (buf == null) return null;
        return buf.toString();
    }

    public StringBuilder readBuffer() throws IOException {
        StringBuilder buffer = null;

        int ch = -1;
        while ((ch = read()) >= 0) {
            if (buffer == null) {
                buffer = new StringBuilder();
            }
            if (ch == '\r') {
                ch = read();
                if (ch > 0 && ch != '\n') {
                    unread(ch);
                }
                break;
            } else if (ch == '\n') {
                break;
            }
            buffer.append((char) ch);
        }
        return buffer;
    }

    public Map<String, String> readHeader() throws IOException {
        Map<String, String> header = new LinkedHashMap<String, String>();
        String currentKey = null;
        while (true) {
            String line = readline();
            if ((line == null) || (line.length() == 0)) {
                break;
            }

            if (!Character.isSpaceChar(line.charAt(0))) {
                int index = line.indexOf(':');
                if (index >= 0) {
                    currentKey = line.substring(0, index).trim();
                    String value = line.substring(index + 1).trim();
                    header.put(currentKey, value);
                }
            } else if (currentKey != null) {
                String value = header.get(currentKey);
                header.put(currentKey, value + "\r\n\t" + line.trim());
            }
        }
        return header;
    }

    public void writeFormData(OutputStream upload, String boundary, String charset) throws IOException {
        try {
            byte[] boundaryBytes = boundary.getBytes(charset);
            byte[] buffer = new byte[BUFFER_LENGTH];
            int len = 0;
            int j = 0;
            while (len >= 0) {
                len = read(buffer, j, buffer.length - j);
                for (int i = 0; i < len - boundaryBytes.length; i++) {
                    for (; j < boundaryBytes.length && i + j < len; j++) {
                        if (buffer[i + j] != boundaryBytes[j]) {
                            j = 0;
                            break;
                        }
                    }

                    if (j == boundaryBytes.length) {
                        // we've reached the boundary
                        upload.write(buffer, 0, i);
                        unread(buffer, i, len - i); // make sure we put the boundary back.
                        upload.flush();
                        return;
                    } else if (j > 0) {
                        // we must be at the end of the buffer, but we could be split on the boundary
                        upload.write(buffer, 0, i);
                        System.arraycopy(buffer, len - boundaryBytes.length + j, buffer, 0, boundaryBytes.length - j);
                    }
                }

                if (j == 0) {
                    upload.write(buffer, 0, len);
                }
            }
            upload.flush();
        } finally {
            upload.close();
        }
    }
}
