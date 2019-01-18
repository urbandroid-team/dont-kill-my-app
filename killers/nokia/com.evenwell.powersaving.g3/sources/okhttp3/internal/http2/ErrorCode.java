package okhttp3.internal.http2;

public enum ErrorCode {
    NO_ERROR(0),
    PROTOCOL_ERROR(1),
    INTERNAL_ERROR(2),
    FLOW_CONTROL_ERROR(3),
    REFUSED_STREAM(7),
    CANCEL(8);
    
    public final int httpCode;

    private ErrorCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public static ErrorCode fromHttp2(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.httpCode == code) {
                return errorCode;
            }
        }
        return null;
    }
}
