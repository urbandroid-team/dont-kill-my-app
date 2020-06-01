package okhttp3.internal.ws;

import com.evenwell.powersaving.g3.utils.PSConst.DC.TIME.VALUE;
import com.fihtdc.backuptool.FileOperator;
import okio.ByteString;

public final class WebSocketProtocol {
    static final String ACCEPT_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    static final int B0_FLAG_FIN = 128;
    static final int B0_FLAG_RSV1 = 64;
    static final int B0_FLAG_RSV2 = 32;
    static final int B0_FLAG_RSV3 = 16;
    static final int B0_MASK_OPCODE = 15;
    static final int B1_FLAG_MASK = 128;
    static final int B1_MASK_LENGTH = 127;
    static final int CLOSE_ABNORMAL_TERMINATION = 1006;
    static final int CLOSE_CLIENT_GOING_AWAY = 1001;
    static final long CLOSE_MESSAGE_MAX = 123;
    static final int CLOSE_NO_STATUS_CODE = 1005;
    static final int CLOSE_PROTOCOL_EXCEPTION = 1002;
    static final int OPCODE_BINARY = 2;
    static final int OPCODE_CONTINUATION = 0;
    static final int OPCODE_CONTROL_CLOSE = 8;
    static final int OPCODE_CONTROL_PING = 9;
    static final int OPCODE_CONTROL_PONG = 10;
    static final int OPCODE_FLAG_CONTROL = 8;
    static final int OPCODE_TEXT = 1;
    static final long PAYLOAD_BYTE_MAX = 125;
    static final int PAYLOAD_LONG = 127;
    static final int PAYLOAD_SHORT = 126;
    static final long PAYLOAD_SHORT_MAX = 65535;

    static void toggleMask(byte[] buffer, long byteCount, byte[] key, long frameBytesRead) {
        int keyLength = key.length;
        int i = 0;
        while (((long) i) < byteCount) {
            buffer[i] = (byte) (buffer[i] ^ key[(int) (frameBytesRead % ((long) keyLength))]);
            i++;
            frameBytesRead++;
        }
    }

    static String closeCodeExceptionMessage(int code) {
        if (code < FileOperator.MAX_DIR_LENGTH || code >= VALUE.DETECT_WAKE_UP_TIME) {
            return "Code must be in range [1000,5000): " + code;
        }
        if ((code < 1004 || code > CLOSE_ABNORMAL_TERMINATION) && (code < 1012 || code > 2999)) {
            return null;
        }
        return "Code " + code + " is reserved and may not be used.";
    }

    static void validateCloseCode(int code) {
        String message = closeCodeExceptionMessage(code);
        if (message != null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static String acceptHeader(String key) {
        return ByteString.encodeUtf8(key + ACCEPT_MAGIC).sha1().base64();
    }

    private WebSocketProtocol() {
        throw new AssertionError("No instances.");
    }
}
