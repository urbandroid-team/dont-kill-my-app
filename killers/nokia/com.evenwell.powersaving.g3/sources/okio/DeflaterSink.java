package okio;

import java.io.IOException;
import java.util.zip.Deflater;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

public final class DeflaterSink implements Sink {
    private boolean closed;
    private final Deflater deflater;
    private final BufferedSink sink;

    public DeflaterSink(Sink sink, Deflater deflater) {
        this(Okio.buffer(sink), deflater);
    }

    DeflaterSink(BufferedSink sink, Deflater deflater) {
        if (sink == null) {
            throw new IllegalArgumentException("source == null");
        } else if (deflater == null) {
            throw new IllegalArgumentException("inflater == null");
        } else {
            this.sink = sink;
            this.deflater = deflater;
        }
    }

    public void write(Buffer source, long byteCount) throws IOException {
        Util.checkOffsetAndCount(source.size, 0, byteCount);
        while (byteCount > 0) {
            Segment head = source.head;
            int toDeflate = (int) Math.min(byteCount, (long) (head.limit - head.pos));
            this.deflater.setInput(head.data, head.pos, toDeflate);
            deflate(false);
            source.size -= (long) toDeflate;
            head.pos += toDeflate;
            if (head.pos == head.limit) {
                source.head = head.pop();
                SegmentPool.recycle(head);
            }
            byteCount -= (long) toDeflate;
        }
    }

    @IgnoreJRERequirement
    private void deflate(boolean syncFlush) throws IOException {
        Buffer buffer = this.sink.buffer();
        while (true) {
            int deflated;
            Segment s = buffer.writableSegment(1);
            if (syncFlush) {
                deflated = this.deflater.deflate(s.data, s.limit, 8192 - s.limit, 2);
            } else {
                deflated = this.deflater.deflate(s.data, s.limit, 8192 - s.limit);
            }
            if (deflated > 0) {
                s.limit += deflated;
                buffer.size += (long) deflated;
                this.sink.emitCompleteSegments();
            } else if (this.deflater.needsInput()) {
                break;
            }
        }
        if (s.pos == s.limit) {
            buffer.head = s.pop();
            SegmentPool.recycle(s);
        }
    }

    public void flush() throws IOException {
        deflate(true);
        this.sink.flush();
    }

    void finishDeflate() throws IOException {
        this.deflater.finish();
        deflate(false);
    }

    public void close() throws IOException {
        if (!this.closed) {
            Throwable thrown = null;
            try {
                finishDeflate();
            } catch (Throwable e) {
                thrown = e;
            }
            try {
                this.deflater.end();
            } catch (Throwable e2) {
                if (thrown == null) {
                    thrown = e2;
                }
            }
            try {
                this.sink.close();
            } catch (Throwable e22) {
                if (thrown == null) {
                    thrown = e22;
                }
            }
            this.closed = true;
            if (thrown != null) {
                Util.sneakyRethrow(thrown);
            }
        }
    }

    public Timeout timeout() {
        return this.sink.timeout();
    }

    public String toString() {
        return "DeflaterSink(" + this.sink + ")";
    }
}
