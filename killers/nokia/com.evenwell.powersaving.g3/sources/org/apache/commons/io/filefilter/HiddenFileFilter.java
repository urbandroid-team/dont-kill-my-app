package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

public class HiddenFileFilter extends AbstractFileFilter implements Serializable {
    public static final IOFileFilter HIDDEN = new HiddenFileFilter();
    public static final IOFileFilter VISIBLE = new NotFileFilter(HIDDEN);
    private static final long serialVersionUID = 8930842316112759062L;

    protected HiddenFileFilter() {
    }

    public boolean accept(File file) {
        return file.isHidden();
    }
}
