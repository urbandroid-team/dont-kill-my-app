package okio;

import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import java.nio.charset.Charset;

final class Util {
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private Util() {
    }

    public static void checkOffsetAndCount(long size, long offset, long byteCount) {
        if ((offset | byteCount) < 0 || offset > size || size - offset < byteCount) {
            throw new ArrayIndexOutOfBoundsException(String.format("size=%s offset=%s byteCount=%s", new Object[]{Long.valueOf(size), Long.valueOf(offset), Long.valueOf(byteCount)}));
        }
    }

    public static short reverseBytesShort(short s) {
        int i = s & 65535;
        return (short) (((MotionEventCompat.ACTION_POINTER_INDEX_MASK & i) >>> 8) | ((i & 255) << 8));
    }

    public static int reverseBytesInt(int i) {
        return ((((ViewCompat.MEASURED_STATE_MASK & i) >>> 24) | ((16711680 & i) >>> 8)) | ((MotionEventCompat.ACTION_POINTER_INDEX_MASK & i) << 8)) | ((i & 255) << 24);
    }

    public static long reverseBytesLong(long v) {
        return ((((((((-72057594037927936L & v) >>> 56) | ((71776119061217280L & v) >>> 40)) | ((280375465082880L & v) >>> 24)) | ((1095216660480L & v) >>> 8)) | ((4278190080L & v) << 8)) | ((16711680 & v) << 24)) | ((65280 & v) << 40)) | ((255 & v) << 56);
    }

    public static void sneakyRethrow(Throwable t) {
        sneakyThrow2(t);
    }

    private static <T extends Throwable> void sneakyThrow2(Throwable t) throws Throwable {
        throw t;
    }

    public static boolean arrayRangeEquals(byte[] a, int aOffset, byte[] b, int bOffset, int byteCount) {
        for (int i = 0; i < byteCount; i++) {
            if (a[i + aOffset] != b[i + bOffset]) {
                return false;
            }
        }
        return true;
    }
}
