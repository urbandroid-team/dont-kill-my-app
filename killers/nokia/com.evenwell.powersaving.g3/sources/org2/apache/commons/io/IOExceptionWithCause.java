package org2.apache.commons.io;

import java.io.IOException;

public class IOExceptionWithCause extends IOException {
    private static final long serialVersionUID = 1;

    public IOExceptionWithCause(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public IOExceptionWithCause(Throwable cause) {
        super(cause == null ? null : cause.toString());
        initCause(cause);
    }
}
