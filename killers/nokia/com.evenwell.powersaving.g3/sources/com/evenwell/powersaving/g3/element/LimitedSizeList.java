package com.evenwell.powersaving.g3.element;

import java.util.ArrayList;

public class LimitedSizeList<E> extends ArrayList<E> {
    private int mSize;

    public LimitedSizeList(int size) {
        this.mSize = size;
    }

    public boolean add(E e) {
        if (contains(e)) {
            remove(e);
        }
        boolean r = super.add(e);
        keepLatestElement(this.mSize);
        return r;
    }

    public void keepLatestElement(int size) {
        if (size() > size) {
            super.removeRange(0, size() - size);
        }
    }
}
