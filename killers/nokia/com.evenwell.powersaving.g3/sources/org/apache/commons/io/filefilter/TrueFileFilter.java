package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

public class TrueFileFilter implements IOFileFilter, Serializable {
    public static final IOFileFilter INSTANCE = TRUE;
    public static final IOFileFilter TRUE = new TrueFileFilter();
    private static final long serialVersionUID = 8782512160909720199L;

    protected TrueFileFilter() {
    }

    public boolean accept(File file) {
        return true;
    }

    public boolean accept(File dir, String name) {
        return true;
    }
}
