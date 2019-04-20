package com.fihtdc.asyncservice;

public class RequestException extends RuntimeException {
    private static final long serialVersionUID = 1;

    public RequestException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RequestException(String detailMessage) {
        super(detailMessage);
    }

    public RequestException(Throwable throwable) {
        super(throwable);
    }
}
