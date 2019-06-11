package net2.lingala.zip4j.crypto.engine;

public class ZipCryptoEngine {
    private static final int[] CRC_TABLE = new int[256];
    private final int[] keys = new int[3];

    static {
        for (int i = 0; i < 256; i++) {
            int r = i;
            for (int j = 0; j < 8; j++) {
                if ((r & 1) == 1) {
                    r = (r >>> 1) ^ -306674912;
                } else {
                    r >>>= 1;
                }
            }
            CRC_TABLE[i] = r;
        }
    }

    public void initKeys(char[] password) {
        this.keys[0] = 305419896;
        this.keys[1] = 591751049;
        this.keys[2] = 878082192;
        for (char c : password) {
            updateKeys((byte) (c & 255));
        }
    }

    public void updateKeys(byte charAt) {
        this.keys[0] = crc32(this.keys[0], charAt);
        int[] iArr = this.keys;
        iArr[1] = iArr[1] + (this.keys[0] & 255);
        this.keys[1] = (this.keys[1] * 134775813) + 1;
        this.keys[2] = crc32(this.keys[2], (byte) (this.keys[1] >> 24));
    }

    private int crc32(int oldCrc, byte charAt) {
        return (oldCrc >>> 8) ^ CRC_TABLE[(oldCrc ^ charAt) & 255];
    }

    public byte decryptByte() {
        int temp = this.keys[2] | 2;
        return (byte) (((temp ^ 1) * temp) >>> 8);
    }
}
