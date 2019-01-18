package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

public class SizeFileFilter extends AbstractFileFilter implements Serializable {
    private static final long serialVersionUID = 7388077430788600069L;
    private final boolean acceptLarger;
    private final long size;

    public SizeFileFilter(long size) {
        this(size, true);
    }

    public SizeFileFilter(long size, boolean acceptLarger) {
        if (size < 0) {
            throw new IllegalArgumentException("The size must be non-negative");
        }
        this.size = size;
        this.acceptLarger = acceptLarger;
    }

    public boolean accept(File file) {
        boolean smaller;
        if (file.length() < this.size) {
            smaller = true;
        } else {
            smaller = false;
        }
        if (this.acceptLarger) {
            return !smaller;
        } else {
            return smaller;
        }
    }

    public String toString() {
        return super.toString() + "(" + (this.acceptLarger ? ">=" : "<") + this.size + ")";
    }
}
