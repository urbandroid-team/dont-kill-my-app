package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

public class CanWriteFileFilter extends AbstractFileFilter implements Serializable {
    public static final IOFileFilter CANNOT_WRITE = new NotFileFilter(CAN_WRITE);
    public static final IOFileFilter CAN_WRITE = new CanWriteFileFilter();
    private static final long serialVersionUID = 5132005214688990379L;

    protected CanWriteFileFilter() {
    }

    public boolean accept(File file) {
        return file.canWrite();
    }
}
