package okio;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Inflater;

public final class GzipSource implements Source {
    private static final byte FCOMMENT = (byte) 4;
    private static final byte FEXTRA = (byte) 2;
    private static final byte FHCRC = (byte) 1;
    private static final byte FNAME = (byte) 3;
    private static final byte SECTION_BODY = (byte) 1;
    private static final byte SECTION_DONE = (byte) 3;
    private static final byte SECTION_HEADER = (byte) 0;
    private static final byte SECTION_TRAILER = (byte) 2;
    private final CRC32 crc = new CRC32();
    private final Inflater inflater;
    private final InflaterSource inflaterSource;
    private int section = 0;
    private final BufferedSource source;

    public GzipSource(Source source) {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        this.inflater = new Inflater(true);
        this.source = Okio.buffer(source);
        this.inflaterSource = new InflaterSource(this.source, this.inflater);
    }

    public long read(Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        } else if (byteCount == 0) {
            return 0;
        } else {
            if (this.section == 0) {
                consumeHeader();
                this.section = 1;
            }
            if (this.section == 1) {
                long offset = sink.size;
                long result = this.inflaterSource.read(sink, byteCount);
                if (result != -1) {
                    updateCrc(sink, offset, result);
                    return result;
                }
                this.section = 2;
            }
            if (this.section == 2) {
                consumeTrailer();
                this.section = 3;
                if (!this.source.exhausted()) {
                    throw new IOException("gzip finished without exhausting source");
                }
            }
            return -1;
        }
    }

    private void consumeHeader() throws IOException {
        long index;
        this.source.require(10);
        byte flags = this.source.buffer().getByte(3);
        boolean fhcrc = ((flags >> 1) & 1) == 1;
        if (fhcrc) {
            updateCrc(this.source.buffer(), 0, 10);
        }
        checkEqual("ID1ID2", 8075, this.source.readShort());
        this.source.skip(8);
        if (((flags >> 2) & 1) == 1) {
            this.source.require(2);
            if (fhcrc) {
                updateCrc(this.source.buffer(), 0, 2);
            }
            int xlen = this.source.buffer().readShortLe();
            this.source.require((long) xlen);
            if (fhcrc) {
                updateCrc(this.source.buffer(), 0, (long) xlen);
            }
            this.source.skip((long) xlen);
        }
        if (((flags >> 3) & 1) == 1) {
            index = this.source.indexOf((byte) SECTION_HEADER);
            if (index == -1) {
                throw new EOFException();
            }
            if (fhcrc) {
                updateCrc(this.source.buffer(), 0, 1 + index);
            }
            this.source.skip(1 + index);
        }
        if (((flags >> 4) & 1) == 1) {
            index = this.source.indexOf((byte) SECTION_HEADER);
            if (index == -1) {
                throw new EOFException();
            }
            if (fhcrc) {
                updateCrc(this.source.buffer(), 0, 1 + index);
            }
            this.source.skip(1 + index);
        }
        if (fhcrc) {
            checkEqual("FHCRC", this.source.readShortLe(), (short) ((int) this.crc.getValue()));
            this.crc.reset();
        }
    }

    private void consumeTrailer() throws IOException {
        checkEqual("CRC", this.source.readIntLe(), (int) this.crc.getValue());
        checkEqual("ISIZE", this.source.readIntLe(), (int) this.inflater.getBytesWritten());
    }

    public Timeout timeout() {
        return this.source.timeout();
    }

    public void close() throws IOException {
        this.inflaterSource.close();
    }

    private void updateCrc(Buffer buffer, long offset, long byteCount) {
        Segment s = buffer.head;
        while (offset >= ((long) (s.limit - s.pos))) {
            offset -= (long) (s.limit - s.pos);
            s = s.next;
        }
        while (byteCount > 0) {
            int pos = (int) (((long) s.pos) + offset);
            int toUpdate = (int) Math.min((long) (s.limit - pos), byteCount);
            this.crc.update(s.data, pos, toUpdate);
            byteCount -= (long) toUpdate;
            offset = 0;
            s = s.next;
        }
    }

    private void checkEqual(String name, int expected, int actual) throws IOException {
        if (actual != expected) {
            throw new IOException(String.format("%s: actual 0x%08x != expected 0x%08x", new Object[]{name, Integer.valueOf(actual), Integer.valueOf(expected)}));
        }
    }
}
