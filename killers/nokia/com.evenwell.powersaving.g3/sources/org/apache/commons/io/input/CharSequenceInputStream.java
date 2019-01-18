package org.apache.commons.io.input;

import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public class CharSequenceInputStream extends InputStream {
    private static final int BUFFER_SIZE = 2048;
    private static final int NO_MARK = -1;
    private final ByteBuffer bbuf;
    private final CharBuffer cbuf;
    private final CharsetEncoder encoder;
    private int mark_bbuf;
    private int mark_cbuf;

    public CharSequenceInputStream(CharSequence cs, Charset charset, int bufferSize) {
        this.encoder = charset.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        float maxBytesPerChar = this.encoder.maxBytesPerChar();
        if (((float) bufferSize) < maxBytesPerChar) {
            throw new IllegalArgumentException("Buffer size " + bufferSize + " is less than maxBytesPerChar " + maxBytesPerChar);
        }
        this.bbuf = ByteBuffer.allocate(bufferSize);
        this.bbuf.flip();
        this.cbuf = CharBuffer.wrap(cs);
        this.mark_cbuf = -1;
        this.mark_bbuf = -1;
    }

    public CharSequenceInputStream(CharSequence cs, String charset, int bufferSize) {
        this(cs, Charset.forName(charset), bufferSize);
    }

    public CharSequenceInputStream(CharSequence cs, Charset charset) {
        this(cs, charset, 2048);
    }

    public CharSequenceInputStream(CharSequence cs, String charset) {
        this(cs, charset, 2048);
    }

    private void fillBuffer() throws CharacterCodingException {
        this.bbuf.compact();
        CoderResult result = this.encoder.encode(this.cbuf, this.bbuf, true);
        if (result.isError()) {
            result.throwException();
        }
        this.bbuf.flip();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException("Byte array is null");
        } else if (len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException("Array Size=" + b.length + ", offset=" + off + ", length=" + len);
        } else if (len == 0) {
            return 0;
        } else {
            if (!this.bbuf.hasRemaining() && !this.cbuf.hasRemaining()) {
                return -1;
            }
            int bytesRead = 0;
            while (len > 0) {
                if (!this.bbuf.hasRemaining()) {
                    fillBuffer();
                    if (!(this.bbuf.hasRemaining() || this.cbuf.hasRemaining())) {
                        break;
                    }
                }
                int chunk = Math.min(this.bbuf.remaining(), len);
                this.bbuf.get(b, off, chunk);
                off += chunk;
                len -= chunk;
                bytesRead += chunk;
            }
            if (bytesRead == 0 && !this.cbuf.hasRemaining()) {
                bytesRead = -1;
            }
            return bytesRead;
        }
    }

    public int read() throws IOException {
        while (!this.bbuf.hasRemaining()) {
            fillBuffer();
            if (!this.bbuf.hasRemaining() && !this.cbuf.hasRemaining()) {
                return -1;
            }
        }
        return this.bbuf.get() & 255;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public long skip(long n) throws IOException {
        long skipped = 0;
        while (n > 0 && available() > 0) {
            read();
            n--;
            skipped++;
        }
        return skipped;
    }

    public int available() throws IOException {
        return this.bbuf.remaining() + this.cbuf.remaining();
    }

    public void close() throws IOException {
    }

    public synchronized void mark(int readlimit) {
        this.mark_cbuf = this.cbuf.position();
        this.mark_bbuf = this.bbuf.position();
        this.cbuf.mark();
        this.bbuf.mark();
    }

    public synchronized void reset() throws IOException {
        if (this.mark_cbuf != -1) {
            if (this.cbuf.position() != 0) {
                this.encoder.reset();
                this.cbuf.rewind();
                this.bbuf.rewind();
                this.bbuf.limit(0);
                while (this.cbuf.position() < this.mark_cbuf) {
                    this.bbuf.rewind();
                    this.bbuf.limit(0);
                    fillBuffer();
                }
            }
            if (this.cbuf.position() != this.mark_cbuf) {
                throw new IllegalStateException("Unexpected CharBuffer postion: actual=" + this.cbuf.position() + SYMBOLS.SPACE + "expected=" + this.mark_cbuf);
            }
            this.bbuf.position(this.mark_bbuf);
            this.mark_cbuf = -1;
            this.mark_bbuf = -1;
        }
    }

    public boolean markSupported() {
        return true;
    }
}
