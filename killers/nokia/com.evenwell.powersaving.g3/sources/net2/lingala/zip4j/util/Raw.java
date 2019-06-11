package net2.lingala.zip4j.util;

import java.io.DataInput;
import net2.lingala.zip4j.exception.ZipException;

public class Raw {
    public static long readLongLittleEndian(byte[] array, int pos) {
        return ((((((((((((((0 | ((long) (array[pos + 7] & 255))) << 8) | ((long) (array[pos + 6] & 255))) << 8) | ((long) (array[pos + 5] & 255))) << 8) | ((long) (array[pos + 4] & 255))) << 8) | ((long) (array[pos + 3] & 255))) << 8) | ((long) (array[pos + 2] & 255))) << 8) | ((long) (array[pos + 1] & 255))) << 8) | ((long) (array[pos] & 255));
    }

    public static int readLeInt(DataInput di, byte[] b) throws ZipException {
        try {
            di.readFully(b, 0, 4);
            return ((b[0] & 255) | ((b[1] & 255) << 8)) | (((b[2] & 255) | ((b[3] & 255) << 8)) << 16);
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    public static int readShortLittleEndian(byte[] b, int off) {
        return (b[off] & 255) | ((b[off + 1] & 255) << 8);
    }

    public static final short readShortBigEndian(byte[] array, int pos) {
        return (short) ((array[pos + 1] & 255) | ((short) (((short) ((array[pos] & 255) | (short) 0)) << 8)));
    }

    public static int readIntLittleEndian(byte[] b, int off) {
        return ((b[off] & 255) | ((b[off + 1] & 255) << 8)) | (((b[off + 2] & 255) | ((b[off + 3] & 255) << 8)) << 16);
    }

    public static byte[] toByteArray(int in, int outSize) {
        byte[] out = new byte[outSize];
        byte[] intArray = toByteArray(in);
        int i = 0;
        while (i < intArray.length && i < outSize) {
            out[i] = intArray[i];
            i++;
        }
        return out;
    }

    public static byte[] toByteArray(int in) {
        return new byte[]{(byte) in, (byte) (in >> 8), (byte) (in >> 16), (byte) (in >> 24)};
    }

    public static final void writeShortLittleEndian(byte[] array, int pos, short value) {
        array[pos + 1] = (byte) (value >>> 8);
        array[pos] = (byte) (value & 255);
    }

    public static final void writeIntLittleEndian(byte[] array, int pos, int value) {
        array[pos + 3] = (byte) (value >>> 24);
        array[pos + 2] = (byte) (value >>> 16);
        array[pos + 1] = (byte) (value >>> 8);
        array[pos] = (byte) (value & 255);
    }

    public static void writeLongLittleEndian(byte[] array, int pos, long value) {
        array[pos + 7] = (byte) ((int) (value >>> 56));
        array[pos + 6] = (byte) ((int) (value >>> 48));
        array[pos + 5] = (byte) ((int) (value >>> 40));
        array[pos + 4] = (byte) ((int) (value >>> 32));
        array[pos + 3] = (byte) ((int) (value >>> 24));
        array[pos + 2] = (byte) ((int) (value >>> 16));
        array[pos + 1] = (byte) ((int) (value >>> 8));
        array[pos] = (byte) ((int) (255 & value));
    }

    public static byte bitArrayToByte(int[] bitArray) throws ZipException {
        if (bitArray == null) {
            throw new ZipException("bit array is null, cannot calculate byte from bits");
        } else if (bitArray.length != 8) {
            throw new ZipException("invalid bit array length, cannot calculate byte");
        } else if (checkBits(bitArray)) {
            int retNum = 0;
            for (int i = 0; i < bitArray.length; i++) {
                retNum = (int) (((double) retNum) + (Math.pow(2.0d, (double) i) * ((double) bitArray[i])));
            }
            return (byte) retNum;
        } else {
            throw new ZipException("invalid bits provided, bits contain other values than 0 or 1");
        }
    }

    private static boolean checkBits(int[] bitArray) {
        int i = 0;
        while (i < bitArray.length) {
            if (bitArray[i] != 0 && bitArray[i] != 1) {
                return false;
            }
            i++;
        }
        return true;
    }

    public static void prepareBuffAESIVBytes(byte[] buff, int nonce, int length) {
        buff[0] = (byte) nonce;
        buff[1] = (byte) (nonce >> 8);
        buff[2] = (byte) (nonce >> 16);
        buff[3] = (byte) (nonce >> 24);
        buff[4] = (byte) 0;
        buff[5] = (byte) 0;
        buff[6] = (byte) 0;
        buff[7] = (byte) 0;
        buff[8] = (byte) 0;
        buff[9] = (byte) 0;
        buff[10] = (byte) 0;
        buff[11] = (byte) 0;
        buff[12] = (byte) 0;
        buff[13] = (byte) 0;
        buff[14] = (byte) 0;
        buff[15] = (byte) 0;
    }

    public static byte[] convertCharArrayToByteArray(char[] charArray) {
        if (charArray == null) {
            throw new NullPointerException();
        }
        byte[] bytes = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            bytes[i] = (byte) charArray[i];
        }
        return bytes;
    }
}
