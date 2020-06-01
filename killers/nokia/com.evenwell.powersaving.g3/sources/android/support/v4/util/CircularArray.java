package android.support.v4.util;

public final class CircularArray<E> {
    private int mCapacityBitmask;
    private E[] mElements;
    private int mHead;
    private int mTail;

    private void doubleCapacity() {
        int n = this.mElements.length;
        int r = n - this.mHead;
        int newCapacity = n << 1;
        if (newCapacity < 0) {
            throw new RuntimeException("Max array capacity exceeded");
        }
        Object[] a = new Object[newCapacity];
        System.arraycopy(this.mElements, this.mHead, a, 0, r);
        System.arraycopy(this.mElements, 0, a, r, this.mHead);
        this.mElements = a;
        this.mHead = 0;
        this.mTail = n;
        this.mCapacityBitmask = newCapacity - 1;
    }

    public CircularArray() {
        this(8);
    }

    public CircularArray(int minCapacity) {
        if (minCapacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        } else if (minCapacity > 1073741824) {
            throw new IllegalArgumentException("capacity must be <= 2^30");
        } else {
            int arrayCapacity;
            if (Integer.bitCount(minCapacity) != 1) {
                arrayCapacity = Integer.highestOneBit(minCapacity - 1) << 1;
            } else {
                arrayCapacity = minCapacity;
            }
            this.mCapacityBitmask = arrayCapacity - 1;
            this.mElements = new Object[arrayCapacity];
        }
    }

    public void addFirst(E e) {
        this.mHead = (this.mHead - 1) & this.mCapacityBitmask;
        this.mElements[this.mHead] = e;
        if (this.mHead == this.mTail) {
            doubleCapacity();
        }
    }

    public void addLast(E e) {
        this.mElements[this.mTail] = e;
        this.mTail = (this.mTail + 1) & this.mCapacityBitmask;
        if (this.mTail == this.mHead) {
            doubleCapacity();
        }
    }

    public E popFirst() {
        if (this.mHead == this.mTail) {
            throw new ArrayIndexOutOfBoundsException();
        }
        E result = this.mElements[this.mHead];
        this.mElements[this.mHead] = null;
        this.mHead = (this.mHead + 1) & this.mCapacityBitmask;
        return result;
    }

    public E popLast() {
        if (this.mHead == this.mTail) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int t = (this.mTail - 1) & this.mCapacityBitmask;
        E result = this.mElements[t];
        this.mElements[t] = null;
        this.mTail = t;
        return result;
    }

    public void clear() {
        removeFromStart(size());
    }

    public void removeFromStart(int numOfElements) {
        if (numOfElements > 0) {
            if (numOfElements > size()) {
                throw new ArrayIndexOutOfBoundsException();
            }
            int i;
            int end = this.mElements.length;
            if (numOfElements < end - this.mHead) {
                end = this.mHead + numOfElements;
            }
            for (i = this.mHead; i < end; i++) {
                this.mElements[i] = null;
            }
            int removed = end - this.mHead;
            numOfElements -= removed;
            this.mHead = (this.mHead + removed) & this.mCapacityBitmask;
            if (numOfElements > 0) {
                for (i = 0; i < numOfElements; i++) {
                    this.mElements[i] = null;
                }
                this.mHead = numOfElements;
            }
        }
    }

    public void removeFromEnd(int numOfElements) {
        if (numOfElements > 0) {
            if (numOfElements > size()) {
                throw new ArrayIndexOutOfBoundsException();
            }
            int i;
            int start = 0;
            if (numOfElements < this.mTail) {
                start = this.mTail - numOfElements;
            }
            for (i = start; i < this.mTail; i++) {
                this.mElements[i] = null;
            }
            int removed = this.mTail - start;
            numOfElements -= removed;
            this.mTail -= removed;
            if (numOfElements > 0) {
                this.mTail = this.mElements.length;
                int newTail = this.mTail - numOfElements;
                for (i = newTail; i < this.mTail; i++) {
                    this.mElements[i] = null;
                }
                this.mTail = newTail;
            }
        }
    }

    public E getFirst() {
        if (this.mHead != this.mTail) {
            return this.mElements[this.mHead];
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public E getLast() {
        if (this.mHead != this.mTail) {
            return this.mElements[(this.mTail - 1) & this.mCapacityBitmask];
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public E get(int n) {
        if (n >= 0 && n < size()) {
            return this.mElements[(this.mHead + n) & this.mCapacityBitmask];
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int size() {
        return (this.mTail - this.mHead) & this.mCapacityBitmask;
    }

    public boolean isEmpty() {
        return this.mHead == this.mTail;
    }
}
