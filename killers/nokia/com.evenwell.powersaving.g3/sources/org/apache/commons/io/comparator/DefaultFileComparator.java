package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class DefaultFileComparator extends AbstractFileComparator implements Serializable {
    public static final Comparator<File> DEFAULT_COMPARATOR = new DefaultFileComparator();
    public static final Comparator<File> DEFAULT_REVERSE = new ReverseComparator(DEFAULT_COMPARATOR);
    private static final long serialVersionUID = 3260141861365313518L;

    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public int compare(File file1, File file2) {
        return file1.compareTo(file2);
    }
}
