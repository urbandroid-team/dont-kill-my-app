package net2.lingala.zip4j.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import net2.lingala.zip4j.unzip.UnzipEngine;

public class InflaterInputStream extends PartInputStream {
    private byte[] buff = new byte[4096];
    private long bytesWritten;
    private Inflater inflater = new Inflater(true);
    private byte[] oneByteBuff = new byte[1];
    private long uncompressedSize;
    private UnzipEngine unzipEngine;

    public InflaterInputStream(RandomAccessFile raf, long start, long len, UnzipEngine unzipEngine) {
        super(raf, start, len, unzipEngine);
        this.unzipEngine = unzipEngine;
        this.bytesWritten = 0;
        this.uncompressedSize = unzipEngine.getFileHeader().getUncompressedSize();
    }

    public int read() throws IOException {
        return read(this.oneByteBuff, 0, 1) == -1 ? -1 : this.oneByteBuff[0] & 255;
    }

    public int read(byte[] b) throws IOException {
        if (b != null) {
            return read(b, 0, b.length);
        }
        throw new NullPointerException("input buffer is null");
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException("input buffer is null");
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            try {
                if (this.bytesWritten >= this.uncompressedSize) {
                    return -1;
                }
                while (true) {
                    int n = this.inflater.inflate(b, off, len);
                    if (n != 0) {
                        this.bytesWritten += (long) n;
                        return n;
                    } else if (!this.inflater.finished() && !this.inflater.needsDictionary()) {
                        if (this.inflater.needsInput()) {
                            fill();
                        }
                    }
                }
                return -1;
            } catch (DataFormatException e) {
                e.printStackTrace();
                String s = "Invalid ZLIB data format";
                if (e.getMessage() != null) {
                    s = e.getMessage();
                }
                if (this.unzipEngine != null && this.unzipEngine.getLocalFileHeader().isEncrypted() && this.unzipEngine.getLocalFileHeader().getEncryptionMethod() == 0) {
                    s = s + " - Wrong Password?";
                }
                throw new IOException(s);
            }
        }
    }

    private void fill() throws IOException {
        int len = super.read(this.buff, 0, this.buff.length);
        if (len == -1) {
            throw new EOFException("Unexpected end of ZLIB input stream");
        }
        this.inflater.setInput(this.buff, 0, len);
    }

    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
        int max = (int) Math.min(n, 2147483647L);
        int total = 0;
        byte[] b = new byte[512];
        while (total < max) {
            int len = max - total;
            if (len > b.length) {
                len = b.length;
            }
            len = read(b, 0, len);
            if (len == -1) {
                break;
            }
            total += len;
        }
        return (long) total;
    }

    public void seek(long pos) throws IOException {
        super.seek(pos);
    }

    public int available() {
        return this.inflater.finished() ? 0 : 1;
    }

    public void close() throws IOException {
        this.inflater.end();
        super.close();
    }

    public UnzipEngine getUnzipEngine() {
        return super.getUnzipEngine();
    }
}
