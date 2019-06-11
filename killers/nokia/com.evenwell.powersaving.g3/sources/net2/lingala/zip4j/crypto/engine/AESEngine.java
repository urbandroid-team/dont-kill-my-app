package net2.lingala.zip4j.crypto.engine;

import java.lang.reflect.Array;
import net2.lingala.zip4j.exception.ZipException;

public class AESEngine {
    /* renamed from: S */
    private static final byte[] f1S = new byte[]{(byte) 99, (byte) 124, (byte) 119, (byte) 123, (byte) -14, (byte) 107, (byte) 111, (byte) -59, (byte) 48, (byte) 1, (byte) 103, (byte) 43, (byte) -2, (byte) -41, (byte) -85, (byte) 118, (byte) -54, (byte) -126, (byte) -55, (byte) 125, (byte) -6, (byte) 89, (byte) 71, (byte) -16, (byte) -83, (byte) -44, (byte) -94, (byte) -81, (byte) -100, (byte) -92, (byte) 114, (byte) -64, (byte) -73, (byte) -3, (byte) -109, (byte) 38, (byte) 54, (byte) 63, (byte) -9, (byte) -52, (byte) 52, (byte) -91, (byte) -27, (byte) -15, (byte) 113, (byte) -40, (byte) 49, (byte) 21, (byte) 4, (byte) -57, (byte) 35, (byte) -61, (byte) 24, (byte) -106, (byte) 5, (byte) -102, (byte) 7, (byte) 18, Byte.MIN_VALUE, (byte) -30, (byte) -21, (byte) 39, (byte) -78, (byte) 117, (byte) 9, (byte) -125, (byte) 44, (byte) 26, (byte) 27, (byte) 110, (byte) 90, (byte) -96, (byte) 82, (byte) 59, (byte) -42, (byte) -77, (byte) 41, (byte) -29, (byte) 47, (byte) -124, (byte) 83, (byte) -47, (byte) 0, (byte) -19, (byte) 32, (byte) -4, (byte) -79, (byte) 91, (byte) 106, (byte) -53, (byte) -66, (byte) 57, (byte) 74, (byte) 76, (byte) 88, (byte) -49, (byte) -48, (byte) -17, (byte) -86, (byte) -5, (byte) 67, (byte) 77, (byte) 51, (byte) -123, (byte) 69, (byte) -7, (byte) 2, Byte.MAX_VALUE, (byte) 80, (byte) 60, (byte) -97, (byte) -88, (byte) 81, (byte) -93, (byte) 64, (byte) -113, (byte) -110, (byte) -99, (byte) 56, (byte) -11, (byte) -68, (byte) -74, (byte) -38, (byte) 33, (byte) 16, (byte) -1, (byte) -13, (byte) -46, (byte) -51, (byte) 12, (byte) 19, (byte) -20, (byte) 95, (byte) -105, (byte) 68, (byte) 23, (byte) -60, (byte) -89, (byte) 126, (byte) 61, (byte) 100, (byte) 93, (byte) 25, (byte) 115, (byte) 96, (byte) -127, (byte) 79, (byte) -36, (byte) 34, (byte) 42, (byte) -112, (byte) -120, (byte) 70, (byte) -18, (byte) -72, (byte) 20, (byte) -34, (byte) 94, (byte) 11, (byte) -37, (byte) -32, (byte) 50, (byte) 58, (byte) 10, (byte) 73, (byte) 6, (byte) 36, (byte) 92, (byte) -62, (byte) -45, (byte) -84, (byte) 98, (byte) -111, (byte) -107, (byte) -28, (byte) 121, (byte) -25, (byte) -56, (byte) 55, (byte) 109, (byte) -115, (byte) -43, (byte) 78, (byte) -87, (byte) 108, (byte) 86, (byte) -12, (byte) -22, (byte) 101, (byte) 122, (byte) -82, (byte) 8, (byte) -70, (byte) 120, (byte) 37, (byte) 46, (byte) 28, (byte) -90, (byte) -76, (byte) -58, (byte) -24, (byte) -35, (byte) 116, (byte) 31, (byte) 75, (byte) -67, (byte) -117, (byte) -118, (byte) 112, (byte) 62, (byte) -75, (byte) 102, (byte) 72, (byte) 3, (byte) -10, (byte) 14, (byte) 97, (byte) 53, (byte) 87, (byte) -71, (byte) -122, (byte) -63, (byte) 29, (byte) -98, (byte) -31, (byte) -8, (byte) -104, (byte) 17, (byte) 105, (byte) -39, (byte) -114, (byte) -108, (byte) -101, (byte) 30, (byte) -121, (byte) -23, (byte) -50, (byte) 85, (byte) 40, (byte) -33, (byte) -116, (byte) -95, (byte) -119, (byte) 13, (byte) -65, (byte) -26, (byte) 66, (byte) 104, (byte) 65, (byte) -103, (byte) 45, (byte) 15, (byte) -80, (byte) 84, (byte) -69, (byte) 22};
    private static final int[] T0 = new int[]{-1520213050, -2072216328, -1720223762, -1921287178, 234025727, -1117033514, -1318096930, 1422247313, 1345335392, 50397442, -1452841010, 2099981142, 436141799, 1658312629, -424957107, -1703512340, 1170918031, -1652391393, 1086966153, -2021818886, 368769775, -346465870, -918075506, 200339707, -324162239, 1742001331, -39673249, -357585083, -1080255453, -140204973, -1770884380, 1539358875, -1028147339, 486407649, -1366060227, 1780885068, 1513502316, 1094664062, 49805301, 1338821763, 1546925160, -190470831, 887481809, 150073849, -1821281822, 1943591083, 1395732834, 1058346282, 201589768, 1388824469, 1696801606, 1589887901, 672667696, -1583966665, 251987210, -1248159185, 151455502, 907153956, -1686077413, 1038279391, 652995533, 1764173646, -843926913, -1619692054, 453576978, -1635548387, 1949051992, 773462580, 756751158, -1301385508, -296068428, -73359269, -162377052, 1295727478, 1641469623, -827083907, 2066295122, 1055122397, 1898917726, -1752923117, -179088474, 1758581177, 0, 753790401, 1612718144, 536673507, -927878791, -312779850, -1100322092, 1187761037, -641810841, 1262041458, -565556588, -733197160, -396863312, 1255133061, 1808847035, 720367557, -441800113, 385612781, -985447546, -682799718, 1429418854, -1803188975, -817543798, 284817897, 100794884, -2122350594, -263171936, 1144798328, -1163944155, -475486133, -212774494, -22830243, -1069531008, -1970303227, -1382903233, -1130521311, 1211644016, 83228145, -541279133, -1044990345, 1977277103, 1663115586, 806359072, 452984805, 250868733, 1842533055, 1288555905, 336333848, 890442534, 804056259, -513843266, -1567123659, -867941240, 957814574, 1472513171, -223893675, -2105639172, 1195195770, -1402706744, -413311558, 723065138, -1787595802, -1604296512, -1736343271, -783331426, 2145180835, 1713513028, 2116692564, -1416589253, -2088204277, -901364084, 703524551, -742868885, 1007948840, 2044649127, -497131844, 487262998, 1994120109, 1004593371, 1446130276, 1312438900, 503974420, -615954030, 168166924, 1814307912, -463709000, 1573044895, 1859376061, -273896381, -1503501628, -1466855111, -1533700815, 937747667, -1954973198, 854058965, 1137232011, 1496790894, -1217565222, -1936880383, 1691735473, -766620004, -525751991, -1267962664, -95005012, 133494003, 636152527, -1352309302, -1904575756, -374428089, 403179536, -709182865, -2005370640, 1864705354, 1915629148, 605822008, -240736681, -944458637, 1371981463, 602466507, 2094914977, -1670089496, 555687742, -582268010, -591544991, -2037675251, -2054518257, -1871679264, 1111375484, -994724495, -1436129588, -666351472, 84083462, 32962295, 302911004, -1553899070, 1597322602, -111716434, -793134743, -1853454825, 1489093017, 656219450, -1180787161, 954327513, 335083755, -1281845205, 856756514, -1150719534, 1893325225, -1987146233, -1483434957, -1231316179, 572399164, -1836611819, 552200649, 1238290055, -11184726, 2015897680, 2061492133, -1886614525, -123625127, -2138470135, 386731290, -624967835, 837215959, -968736124, -1201116976, -1019133566, -1332111063, 1999449434, 286199582, -877612933, -61582168, -692339859, 974525996};
    private static final int[] rcon = new int[]{1, 2, 4, 8, 16, 32, 64, 128, 27, 54, 108, 216, 171, 77, 154, 47, 94, 188, 99, 198, 151, 53, 106, 212, 179, 125, 250, 239, 197, 145};
    private int C0;
    private int C1;
    private int C2;
    private int C3;
    private int rounds;
    private int[][] workingKey = ((int[][]) null);

    public AESEngine(byte[] key) throws ZipException {
        init(key);
    }

    public void init(byte[] key) throws ZipException {
        this.workingKey = generateWorkingKey(key);
    }

    private int[][] generateWorkingKey(byte[] key) throws ZipException {
        int kc = key.length / 4;
        if ((kc == 4 || kc == 6 || kc == 8) && kc * 4 == key.length) {
            this.rounds = kc + 6;
            int[][] W = (int[][]) Array.newInstance(Integer.TYPE, new int[]{this.rounds + 1, 4});
            int t = 0;
            int i = 0;
            while (i < key.length) {
                W[t >> 2][t & 3] = (((key[i] & 255) | ((key[i + 1] & 255) << 8)) | ((key[i + 2] & 255) << 16)) | (key[i + 3] << 24);
                i += 4;
                t++;
            }
            int k = (this.rounds + 1) << 2;
            i = kc;
            while (i < k) {
                int temp = W[(i - 1) >> 2][(i - 1) & 3];
                if (i % kc == 0) {
                    temp = subWord(shift(temp, 8)) ^ rcon[(i / kc) - 1];
                } else if (kc > 6 && i % kc == 4) {
                    temp = subWord(temp);
                }
                W[i >> 2][i & 3] = W[(i - kc) >> 2][(i - kc) & 3] ^ temp;
                i++;
            }
            return W;
        }
        throw new ZipException("invalid key length (not 128/192/256)");
    }

    public int processBlock(byte[] in, byte[] out) throws ZipException {
        return processBlock(in, 0, out, 0);
    }

    public int processBlock(byte[] in, int inOff, byte[] out, int outOff) throws ZipException {
        if (this.workingKey == null) {
            throw new ZipException("AES engine not initialised");
        } else if (inOff + 16 > in.length) {
            throw new ZipException("input buffer too short");
        } else if (outOff + 16 > out.length) {
            throw new ZipException("output buffer too short");
        } else {
            stateIn(in, inOff);
            encryptBlock(this.workingKey);
            stateOut(out, outOff);
            return 16;
        }
    }

    private final void stateIn(byte[] bytes, int off) {
        int index = off;
        int index2 = index + 1;
        this.C0 = bytes[index] & 255;
        index = index2 + 1;
        this.C0 |= (bytes[index2] & 255) << 8;
        index2 = index + 1;
        this.C0 |= (bytes[index] & 255) << 16;
        index = index2 + 1;
        this.C0 |= bytes[index2] << 24;
        index2 = index + 1;
        this.C1 = bytes[index] & 255;
        index = index2 + 1;
        this.C1 |= (bytes[index2] & 255) << 8;
        index2 = index + 1;
        this.C1 |= (bytes[index] & 255) << 16;
        index = index2 + 1;
        this.C1 |= bytes[index2] << 24;
        index2 = index + 1;
        this.C2 = bytes[index] & 255;
        index = index2 + 1;
        this.C2 |= (bytes[index2] & 255) << 8;
        index2 = index + 1;
        this.C2 |= (bytes[index] & 255) << 16;
        index = index2 + 1;
        this.C2 |= bytes[index2] << 24;
        index2 = index + 1;
        this.C3 = bytes[index] & 255;
        index = index2 + 1;
        this.C3 |= (bytes[index2] & 255) << 8;
        index2 = index + 1;
        this.C3 |= (bytes[index] & 255) << 16;
        index = index2 + 1;
        this.C3 |= bytes[index2] << 24;
    }

    private final void stateOut(byte[] bytes, int off) {
        int i = off;
        int i2 = i + 1;
        bytes[i] = (byte) this.C0;
        i = i2 + 1;
        bytes[i2] = (byte) (this.C0 >> 8);
        i2 = i + 1;
        bytes[i] = (byte) (this.C0 >> 16);
        i = i2 + 1;
        bytes[i2] = (byte) (this.C0 >> 24);
        i2 = i + 1;
        bytes[i] = (byte) this.C1;
        i = i2 + 1;
        bytes[i2] = (byte) (this.C1 >> 8);
        i2 = i + 1;
        bytes[i] = (byte) (this.C1 >> 16);
        i = i2 + 1;
        bytes[i2] = (byte) (this.C1 >> 24);
        i2 = i + 1;
        bytes[i] = (byte) this.C2;
        i = i2 + 1;
        bytes[i2] = (byte) (this.C2 >> 8);
        i2 = i + 1;
        bytes[i] = (byte) (this.C2 >> 16);
        i = i2 + 1;
        bytes[i2] = (byte) (this.C2 >> 24);
        i2 = i + 1;
        bytes[i] = (byte) this.C3;
        i = i2 + 1;
        bytes[i2] = (byte) (this.C3 >> 8);
        i2 = i + 1;
        bytes[i] = (byte) (this.C3 >> 16);
        i = i2 + 1;
        bytes[i2] = (byte) (this.C3 >> 24);
    }

    private final void encryptBlock(int[][] KW) {
        int r0;
        int r1;
        int r2;
        int r;
        int r3;
        this.C0 ^= KW[0][0];
        this.C1 ^= KW[0][1];
        this.C2 ^= KW[0][2];
        this.C3 ^= KW[0][3];
        int r4 = 1;
        while (r4 < this.rounds - 1) {
            r0 = (((T0[this.C0 & 255] ^ shift(T0[(this.C1 >> 8) & 255], 24)) ^ shift(T0[(this.C2 >> 16) & 255], 16)) ^ shift(T0[(this.C3 >> 24) & 255], 8)) ^ KW[r4][0];
            r1 = (((T0[this.C1 & 255] ^ shift(T0[(this.C2 >> 8) & 255], 24)) ^ shift(T0[(this.C3 >> 16) & 255], 16)) ^ shift(T0[(this.C0 >> 24) & 255], 8)) ^ KW[r4][1];
            r2 = (((T0[this.C2 & 255] ^ shift(T0[(this.C3 >> 8) & 255], 24)) ^ shift(T0[(this.C0 >> 16) & 255], 16)) ^ shift(T0[(this.C1 >> 24) & 255], 8)) ^ KW[r4][2];
            r = r4 + 1;
            r3 = (((T0[this.C3 & 255] ^ shift(T0[(this.C0 >> 8) & 255], 24)) ^ shift(T0[(this.C1 >> 16) & 255], 16)) ^ shift(T0[(this.C2 >> 24) & 255], 8)) ^ KW[r4][3];
            this.C0 = (((T0[r0 & 255] ^ shift(T0[(r1 >> 8) & 255], 24)) ^ shift(T0[(r2 >> 16) & 255], 16)) ^ shift(T0[(r3 >> 24) & 255], 8)) ^ KW[r][0];
            this.C1 = (((T0[r1 & 255] ^ shift(T0[(r2 >> 8) & 255], 24)) ^ shift(T0[(r3 >> 16) & 255], 16)) ^ shift(T0[(r0 >> 24) & 255], 8)) ^ KW[r][1];
            this.C2 = (((T0[r2 & 255] ^ shift(T0[(r3 >> 8) & 255], 24)) ^ shift(T0[(r0 >> 16) & 255], 16)) ^ shift(T0[(r1 >> 24) & 255], 8)) ^ KW[r][2];
            r4 = r + 1;
            this.C3 = (((T0[r3 & 255] ^ shift(T0[(r0 >> 8) & 255], 24)) ^ shift(T0[(r1 >> 16) & 255], 16)) ^ shift(T0[(r2 >> 24) & 255], 8)) ^ KW[r][3];
        }
        r0 = (((T0[this.C0 & 255] ^ shift(T0[(this.C1 >> 8) & 255], 24)) ^ shift(T0[(this.C2 >> 16) & 255], 16)) ^ shift(T0[(this.C3 >> 24) & 255], 8)) ^ KW[r4][0];
        r1 = (((T0[this.C1 & 255] ^ shift(T0[(this.C2 >> 8) & 255], 24)) ^ shift(T0[(this.C3 >> 16) & 255], 16)) ^ shift(T0[(this.C0 >> 24) & 255], 8)) ^ KW[r4][1];
        r2 = (((T0[this.C2 & 255] ^ shift(T0[(this.C3 >> 8) & 255], 24)) ^ shift(T0[(this.C0 >> 16) & 255], 16)) ^ shift(T0[(this.C1 >> 24) & 255], 8)) ^ KW[r4][2];
        r = r4 + 1;
        r3 = (((T0[this.C3 & 255] ^ shift(T0[(this.C0 >> 8) & 255], 24)) ^ shift(T0[(this.C1 >> 16) & 255], 16)) ^ shift(T0[(this.C2 >> 24) & 255], 8)) ^ KW[r4][3];
        this.C0 = ((((f1S[r0 & 255] & 255) ^ ((f1S[(r1 >> 8) & 255] & 255) << 8)) ^ ((f1S[(r2 >> 16) & 255] & 255) << 16)) ^ (f1S[(r3 >> 24) & 255] << 24)) ^ KW[r][0];
        this.C1 = ((((f1S[r1 & 255] & 255) ^ ((f1S[(r2 >> 8) & 255] & 255) << 8)) ^ ((f1S[(r3 >> 16) & 255] & 255) << 16)) ^ (f1S[(r0 >> 24) & 255] << 24)) ^ KW[r][1];
        this.C2 = ((((f1S[r2 & 255] & 255) ^ ((f1S[(r3 >> 8) & 255] & 255) << 8)) ^ ((f1S[(r0 >> 16) & 255] & 255) << 16)) ^ (f1S[(r1 >> 24) & 255] << 24)) ^ KW[r][2];
        this.C3 = ((((f1S[r3 & 255] & 255) ^ ((f1S[(r0 >> 8) & 255] & 255) << 8)) ^ ((f1S[(r1 >> 16) & 255] & 255) << 16)) ^ (f1S[(r2 >> 24) & 255] << 24)) ^ KW[r][3];
    }

    private int shift(int r, int shift) {
        return (r >>> shift) | (r << (-shift));
    }

    private int subWord(int x) {
        return (((f1S[x & 255] & 255) | ((f1S[(x >> 8) & 255] & 255) << 8)) | ((f1S[(x >> 16) & 255] & 255) << 16)) | (f1S[(x >> 24) & 255] << 24);
    }
}
