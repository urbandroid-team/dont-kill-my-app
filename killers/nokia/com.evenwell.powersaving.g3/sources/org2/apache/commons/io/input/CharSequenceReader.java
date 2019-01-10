package org2.apache.commons.io.input;

import java.io.Reader;
import java.io.Serializable;

public class CharSequenceReader extends Reader implements Serializable {
    private final CharSequence charSequence;
    private int idx;
    private int mark;

    public CharSequenceReader(CharSequence charSequence) {
        if (charSequence == null) {
            charSequence = "";
        }
        this.charSequence = charSequence;
    }

    public void close() {
        this.idx = 0;
        this.mark = 0;
    }

    public void mark(int readAheadLimit) {
        this.mark = this.idx;
    }

    public boolean markSupported() {
        return true;
    }

    public int read() {
        if (this.idx >= this.charSequence.length()) {
            return -1;
        }
        CharSequence charSequence = this.charSequence;
        int i = this.idx;
        this.idx = i + 1;
        return charSequence.charAt(i);
    }

    public int read(char[] array, int offset, int length) {
        if (this.idx >= this.charSequence.length()) {
            return -1;
        }
        if (array == null) {
            throw new NullPointerException("Character array is missing");
        } else if (length < 0 || offset < 0 || offset + length > array.length) {
            throw new IndexOutOfBoundsException("Array Size=" + array.length + ", offset=" + offset + ", length=" + length);
        } else {
            int count = 0;
            for (int i = 0; i < length; i++) {
                int c = read();
                if (c == -1) {
                    return count;
                }
                array[offset + i] = (char) c;
                count++;
            }
            return count;
        }
    }

    public void reset() {
        this.idx = this.mark;
    }

    public long skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException("Number of characters to skip is less than zero: " + n);
        } else if (this.idx >= this.charSequence.length()) {
            return -1;
        } else {
            int dest = (int) Math.min((long) this.charSequence.length(), ((long) this.idx) + n);
            int count = dest - this.idx;
            this.idx = dest;
            return (long) count;
        }
    }

    public String toString() {
        return this.charSequence.toString();
    }
}
