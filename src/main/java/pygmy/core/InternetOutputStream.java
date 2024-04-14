package pygmy.core;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

public class InternetOutputStream extends FilterOutputStream {

    private String encoding;
    private ChunkedEncodingOutputStream chunkedStream;

    public InternetOutputStream(OutputStream out) {
        super(new BufferedOutputStream(out));
    }

    public InternetOutputStream(OutputStream out, int size) {
        super(new BufferedOutputStream(out, size));
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) throws IOException {
        this.encoding = encoding;
        out.flush();
        if (encoding.equalsIgnoreCase("gzip") || encoding.equalsIgnoreCase("x-gzip")) {
            out = new GZIPOutputStream(out);
        } else if (encoding.equalsIgnoreCase("compress") || encoding.equalsIgnoreCase("x-compress")) {
            out = new DeflaterOutputStream(out, new Deflater());
        }
    }

    public void print(String buffer) throws IOException {
        print(buffer, 0, buffer.length());
    }

    public void println() throws IOException {
        write(Http.CRLF.getBytes());
    }

    public void print(String text, int offset, int len) throws IOException {
        write(text.getBytes(), offset, len);
    }

    public void println(String text) throws IOException {
        print(text);
        println();
    }

    public void print(int i) throws IOException {
        print(String.valueOf(i));
    }

    public void println(int i) throws IOException {
        print(i);
        println();
    }

    public void finish() throws IOException {
        if (out instanceof DeflaterOutputStream) {
            ((DeflaterOutputStream) out).finish();
        }
        flush();
        if (chunkedStream != null) {
            chunkedStream.finish();
        }
    }

    public void setChunkedEncoded(boolean chunked) {
        out = this.chunkedStream = new ChunkedEncodingOutputStream(out);
    }
}
