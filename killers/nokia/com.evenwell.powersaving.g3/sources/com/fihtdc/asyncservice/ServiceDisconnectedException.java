package com.fihtdc.asyncservice;

public class ServiceDisconnectedException extends RuntimeException {
    private static final long serialVersionUID = 7863522038361194526L;

    public ServiceDisconnectedException(String detailMessage) {
        super(detailMessage);
    }

    public ServiceDisconnectedException(Throwable throwable) {
        super(throwable);
    }

    public ServiceDisconnectedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
