package org2.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org2.apache.commons.io.ByteOrderMark;

public class BOMInputStream extends ProxyInputStream {
    private final List<ByteOrderMark> boms;
    private ByteOrderMark byteOrderMark;
    private int fbIndex;
    private int fbLength;
    private int[] firstBytes;
    private final boolean include;
    private int markFbIndex;
    private boolean markedAtStart;

    public BOMInputStream(InputStream delegate) {
        this(delegate, false, ByteOrderMark.UTF_8);
    }

    public BOMInputStream(InputStream delegate, boolean include) {
        this(delegate, include, ByteOrderMark.UTF_8);
    }

    public BOMInputStream(InputStream delegate, ByteOrderMark... boms) {
        this(delegate, false, boms);
    }

    public BOMInputStream(InputStream delegate, boolean include, ByteOrderMark... boms) {
        super(delegate);
        if (boms == null || boms.length == 0) {
            throw new IllegalArgumentException("No BOMs specified");
        }
        this.include = include;
        this.boms = Arrays.asList(boms);
    }

    public boolean hasBOM() throws IOException {
        return getBOM() != null;
    }

    public boolean hasBOM(ByteOrderMark bom) throws IOException {
        if (this.boms.contains(bom)) {
            return this.byteOrderMark != null && getBOM().equals(bom);
        } else {
            throw new IllegalArgumentException("Stream not configure to detect " + bom);
        }
    }

    public ByteOrderMark getBOM() throws IOException {
        if (this.firstBytes == null) {
            this.fbLength = 0;
            int max = 0;
            for (ByteOrderMark bom : this.boms) {
                max = Math.max(max, bom.length());
            }
            this.firstBytes = new int[max];
            int i = 0;
            while (i < this.firstBytes.length) {
                this.firstBytes[i] = this.in.read();
                this.fbLength++;
                if (this.firstBytes[i] < 0) {
                    break;
                }
                this.byteOrderMark = find();
                if (this.byteOrderMark == null) {
                    i++;
                } else if (!this.include) {
                    this.fbLength = 0;
                }
            }
        }
        return this.byteOrderMark;
    }

    public String getBOMCharsetName() throws IOException {
        getBOM();
        return this.byteOrderMark == null ? null : this.byteOrderMark.getCharsetName();
    }

    private int readFirstBytes() throws IOException {
        getBOM();
        if (this.fbIndex >= this.fbLength) {
            return -1;
        }
        int[] iArr = this.firstBytes;
        int i = this.fbIndex;
        this.fbIndex = i + 1;
        return iArr[i];
    }

    private ByteOrderMark find() {
        for (ByteOrderMark bom : this.boms) {
            if (matches(bom)) {
                return bom;
            }
        }
        return null;
    }

    private boolean matches(ByteOrderMark bom) {
        if (bom.length() != this.fbLength) {
            return false;
        }
        for (int i = 0; i < bom.length(); i++) {
            if (bom.get(i) != this.firstBytes[i]) {
                return false;
            }
        }
        return true;
    }

    public int read() throws IOException {
        int b = readFirstBytes();
        return b >= 0 ? b : this.in.read();
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        int firstCount = 0;
        int b = 0;
        int off2 = off;
        while (len > 0 && b >= 0) {
            b = readFirstBytes();
            if (b >= 0) {
                off = off2 + 1;
                buf[off2] = (byte) (b & 255);
                len--;
                firstCount++;
                off2 = off;
            }
        }
        int secondCount = this.in.read(buf, off2, len);
        if (secondCount < 0) {
            return firstCount > 0 ? firstCount : -1;
        } else {
            return firstCount + secondCount;
        }
    }

    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    public synchronized void mark(int readlimit) {
        this.markFbIndex = this.fbIndex;
        this.markedAtStart = this.firstBytes == null;
        this.in.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
        this.fbIndex = this.markFbIndex;
        if (this.markedAtStart) {
            this.firstBytes = null;
        }
        this.in.reset();
    }

    public long skip(long n) throws IOException {
        while (n > 0 && readFirstBytes() >= 0) {
            n--;
        }
        return this.in.skip(n);
    }
}
