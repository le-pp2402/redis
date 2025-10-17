package utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RedisInputStream extends FilterInputStream {
    protected final int INPUT_BUFFER_SIZE = 8192;
    protected final byte[] buf;
    protected int id, limit;

    public RedisInputStream(InputStream in) {
        super(in);
        this.buf = new byte[INPUT_BUFFER_SIZE];
        this.id = 0;
        this.limit = 0;
    }
    
    private void fill() {
        if (id >= limit) {
            try {
                limit = in.read(buf);
                id = 0;
                if (limit == -1) {
                    throw new RuntimeException("Unexpected end of stream.");
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void ensureCrLf() {
        fill();
        if (buf[id++] == '\r') {
            fill();
            if (buf[id++] == '\n') {
                return;
            }
        }
        throw new RuntimeException("Unexpected CRLF encoding.");
    }

    public byte readByte() throws RuntimeException {
        fill(); // Đảm bảo buffer được fill trước khi đọc
        return buf[id++];
    }

    public String readBytes(int len) throws RuntimeException {
        StringBuilder sb = new StringBuilder(len);
        while (len > 0) {
            fill();
            sb.append((char) buf[id++]);
            len--;
        }
        return sb.toString();
    }

    public String readLine() {
        final StringBuilder sb = new StringBuilder();
        while (true) {
            fill();

            byte b = buf[id++];
            if (b == '\r') {
                fill();
                byte c = buf[id++];
                if (c == '\n') {
                    break;
                }
                sb.append((char) b);
                sb.append((char) c);
            } else {
                sb.append((char) b);
            }
        }

        final String reply = sb.toString();
        if (reply.isEmpty()) {
            throw new RuntimeException("It seems like server has closed the connection.");
        }

        return reply;
    }
}
