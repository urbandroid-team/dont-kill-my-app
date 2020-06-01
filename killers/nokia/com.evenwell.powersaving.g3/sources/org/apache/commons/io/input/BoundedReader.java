package org.apache.commons.io.input;

import java.io.IOException;
import java.io.Reader;

public class BoundedReader extends Reader {
    private static final int INVALID = -1;
    private int charsRead = 0;
    private int markedAt = -1;
    private final int maxCharsFromTargetReader;
    private int readAheadLimit;
    private final Reader target;

    public BoundedReader(Reader target, int maxCharsFromTargetReader) throws IOException {
        this.target = target;
        this.maxCharsFromTargetReader = maxCharsFromTargetReader;
    }

    public void close() throws IOException {
        this.target.close();
    }

    public void reset() throws IOException {
        this.charsRead = this.markedAt;
        this.target.reset();
    }

    public void mark(int readAheadLimit) throws IOException {
        this.readAheadLimit = readAheadLimit - this.charsRead;
        this.markedAt = this.charsRead;
        this.target.mark(readAheadLimit);
    }

    public int read() throws IOException {
        if (this.charsRead >= this.maxCharsFromTargetReader) {
            return -1;
        }
        if (this.markedAt >= 0 && this.charsRead - this.markedAt >= this.readAheadLimit) {
            return -1;
        }
        this.charsRead++;
        return this.target.read();
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            int c = read();
            if (c == -1) {
                return i;
            }
            cbuf[off + i] = (char) c;
        }
        return len;
    }
}
