package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends ProxyInputStream {
    private long count;

    public CountingInputStream(InputStream in) {
        super(in);
    }

    public synchronized long skip(long length) throws IOException {
        long skip;
        skip = super.skip(length);
        this.count += skip;
        return skip;
    }

    protected synchronized void afterRead(int n) {
        if (n != -1) {
            this.count += (long) n;
        }
    }

    public int getCount() {
        long result = getByteCount();
        if (result <= 2147483647L) {
            return (int) result;
        }
        throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
    }

    public int resetCount() {
        long result = resetByteCount();
        if (result <= 2147483647L) {
            return (int) result;
        }
        throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
    }

    public synchronized long getByteCount() {
        return this.count;
    }

    public synchronized long resetByteCount() {
        long tmp;
        tmp = this.count;
        this.count = 0;
        return tmp;
    }
}
