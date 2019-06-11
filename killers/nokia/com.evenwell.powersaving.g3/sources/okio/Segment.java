package okio;

final class Segment {
    static final int SHARE_MINIMUM = 1024;
    static final int SIZE = 8192;
    final byte[] data;
    int limit;
    Segment next;
    boolean owner;
    int pos;
    Segment prev;
    boolean shared;

    Segment() {
        this.data = new byte[8192];
        this.owner = true;
        this.shared = false;
    }

    Segment(Segment shareFrom) {
        this(shareFrom.data, shareFrom.pos, shareFrom.limit);
        shareFrom.shared = true;
    }

    Segment(byte[] data, int pos, int limit) {
        this.data = data;
        this.pos = pos;
        this.limit = limit;
        this.owner = false;
        this.shared = true;
    }

    public Segment pop() {
        Segment result;
        if (this.next != this) {
            result = this.next;
        } else {
            result = null;
        }
        this.prev.next = this.next;
        this.next.prev = this.prev;
        this.next = null;
        this.prev = null;
        return result;
    }

    public Segment push(Segment segment) {
        segment.prev = this;
        segment.next = this.next;
        this.next.prev = segment;
        this.next = segment;
        return segment;
    }

    public Segment split(int byteCount) {
        if (byteCount <= 0 || byteCount > this.limit - this.pos) {
            throw new IllegalArgumentException();
        }
        Segment prefix;
        if (byteCount >= 1024) {
            prefix = new Segment(this);
        } else {
            prefix = SegmentPool.take();
            System.arraycopy(this.data, this.pos, prefix.data, 0, byteCount);
        }
        prefix.limit = prefix.pos + byteCount;
        this.pos += byteCount;
        this.prev.push(prefix);
        return prefix;
    }

    public void compact() {
        if (this.prev == this) {
            throw new IllegalStateException();
        } else if (this.prev.owner) {
            int byteCount = this.limit - this.pos;
            if (byteCount <= (8192 - this.prev.limit) + (this.prev.shared ? 0 : this.prev.pos)) {
                writeTo(this.prev, byteCount);
                pop();
                SegmentPool.recycle(this);
            }
        }
    }

    public void writeTo(Segment sink, int byteCount) {
        if (sink.owner) {
            if (sink.limit + byteCount > 8192) {
                if (sink.shared) {
                    throw new IllegalArgumentException();
                } else if ((sink.limit + byteCount) - sink.pos > 8192) {
                    throw new IllegalArgumentException();
                } else {
                    System.arraycopy(sink.data, sink.pos, sink.data, 0, sink.limit - sink.pos);
                    sink.limit -= sink.pos;
                    sink.pos = 0;
                }
            }
            System.arraycopy(this.data, this.pos, sink.data, sink.limit, byteCount);
            sink.limit += byteCount;
            this.pos += byteCount;
            return;
        }
        throw new IllegalArgumentException();
    }
}
