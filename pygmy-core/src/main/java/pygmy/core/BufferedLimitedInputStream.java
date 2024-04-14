package pygmy.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferedLimitedInputStream extends InputStream {

    private InputStream delegate;
    private byte[] buffer;
    private int offset = 0;
    private int bufferEnd = 0;
    private long totalBytesRead = 0L;
    private long totalByteLimit = -1L;
    private boolean shouldCloseDelegate = false;

    public BufferedLimitedInputStream(InputStream delegate) {
        this(delegate, -1);
    }

    public BufferedLimitedInputStream(InputStream delegate, long limit) {
        this(delegate, limit, 16 * 1024);
    }

    public BufferedLimitedInputStream(InputStream delegate, long limit, int bufferSize) {
        this.delegate = delegate;
        totalByteLimit = limit;
        buffer = new byte[bufferSize];
    }

    public BufferedLimitedInputStream closeDelegate(boolean shouldCloseDelegate) {
        this.shouldCloseDelegate = shouldCloseDelegate;
        return this;
    }

    public BufferedLimitedInputStream limit(long limit) {
        totalByteLimit = limit;
        totalBytesRead = 0L;
        return this;
    }

    public int read() throws IOException {
        if (ensureBufferIsFilled() < 0) return -1;
        totalBytesRead++;
        return buffer[offset++] & 0xff;
    }

    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    public int read(byte[] bytes, int start, int length) throws IOException {
        if (ensureBufferIsFilled() < 0) return -1;
        int len = Math.min(length, bufferEnd - offset);
        System.arraycopy(buffer, offset, bytes, start, len);
        totalBytesRead += len;
        offset += len;
        return len;
    }

    private int ensureBufferIsFilled() throws IOException {
        if (totalByteLimit >= 0 && totalBytesRead >= totalByteLimit) return -1;
        if (bufferEnd < 1 || offset >= bufferEnd) {
            bufferEnd = delegate.read(buffer, 0, totalByteLimit > 0 ? Math.min(buffer.length, (int) (totalByteLimit - totalBytesRead)) : buffer.length);
            offset = 0;
            return bufferEnd;
        } else {
            return 0;
        }
    }

    public long skip(long l) throws IOException {
        offset = bufferEnd = 0;
        totalBytesRead = delegate.skip(l);
        return totalBytesRead;
    }

    public int available() throws IOException {
        return (bufferEnd - offset) + delegate.available();
    }

    public void close() throws IOException {
        if (shouldCloseDelegate) {
            delegate.close();
        }
    }

    public synchronized void mark(int i) {
        delegate.mark(i);
    }

    public synchronized void reset() throws IOException {
        totalBytesRead = 0L;
        offset = bufferEnd = 0;
        delegate.reset();
    }

    public boolean markSupported() {
        return delegate.markSupported();
    }

    public static void main(String[] args) throws IOException {
        BufferedLimitedInputStream stream = new BufferedLimitedInputStream(new FileInputStream(new File("/Users/charlie/Music/GetItUpForLove.mp3")));
        stream.closeDelegate(true);
        byte[] buf = new byte[1024];
        int len = 0;
        int i = 0;
        while ((len = stream.read(buf)) >= 0) {
            System.out.println(len);
            i += len;
        }
        System.out.println("Total bytes read: " + i);
        stream.close();
    }
}
