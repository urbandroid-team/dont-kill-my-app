package org.apache.commons.io;

import java.io.File;
import java.io.IOException;

public class FileExistsException extends IOException {
    private static final long serialVersionUID = 1;

    public FileExistsException(String message) {
        super(message);
    }

    public FileExistsException(File file) {
        super("File " + file + " exists");
    }
}
