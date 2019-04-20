package net2.lingala.zip4j.exception;

public class ZipException extends Exception {
    private static final long serialVersionUID = 1;
    private int code = -1;

    public ZipException(String msg) {
        super(msg);
    }

    public ZipException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZipException(String msg, int code) {
        super(msg);
        this.code = code;
    }

    public ZipException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public ZipException(Throwable cause) {
        super(cause);
    }

    public ZipException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
