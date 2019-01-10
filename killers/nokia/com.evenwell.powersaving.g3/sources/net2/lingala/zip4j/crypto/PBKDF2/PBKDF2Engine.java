package net2.lingala.zip4j.crypto.PBKDF2;

import android.support.v4.view.ViewCompat;
import net2.lingala.zip4j.util.Raw;

public class PBKDF2Engine {
    protected PBKDF2Parameters parameters;
    protected PRF prf;

    public PBKDF2Engine() {
        this.parameters = null;
        this.prf = null;
    }

    public PBKDF2Engine(PBKDF2Parameters parameters) {
        this.parameters = parameters;
        this.prf = null;
    }

    public PBKDF2Engine(PBKDF2Parameters parameters, PRF prf) {
        this.parameters = parameters;
        this.prf = prf;
    }

    public byte[] deriveKey(char[] inputPassword) {
        return deriveKey(inputPassword, 0);
    }

    public byte[] deriveKey(char[] inputPassword, int dkLen) {
        if (inputPassword == null) {
            throw new NullPointerException();
        }
        assertPRF(Raw.convertCharArrayToByteArray(inputPassword));
        if (dkLen == 0) {
            dkLen = this.prf.getHLen();
        }
        return PBKDF2(this.prf, this.parameters.getSalt(), this.parameters.getIterationCount(), dkLen);
    }

    public boolean verifyKey(char[] inputPassword) {
        byte[] referenceKey = getParameters().getDerivedKey();
        if (referenceKey == null || referenceKey.length == 0) {
            return false;
        }
        byte[] inputKey = deriveKey(inputPassword, referenceKey.length);
        if (inputKey == null || inputKey.length != referenceKey.length) {
            return false;
        }
        for (int i = 0; i < inputKey.length; i++) {
            if (inputKey[i] != referenceKey[i]) {
                return false;
            }
        }
        return true;
    }

    protected void assertPRF(byte[] P) {
        if (this.prf == null) {
            this.prf = new MacBasedPRF(this.parameters.getHashAlgorithm());
        }
        this.prf.init(P);
    }

    public PRF getPseudoRandomFunction() {
        return this.prf;
    }

    protected byte[] PBKDF2(PRF prf, byte[] S, int c, int dkLen) {
        if (S == null) {
            S = new byte[0];
        }
        int hLen = prf.getHLen();
        int l = ceil(dkLen, hLen);
        int r = dkLen - ((l - 1) * hLen);
        byte[] T = new byte[(l * hLen)];
        int ti_offset = 0;
        for (int i = 1; i <= l; i++) {
            _F(T, ti_offset, prf, S, c, i);
            ti_offset += hLen;
        }
        if (r >= hLen) {
            return T;
        }
        byte[] DK = new byte[dkLen];
        System.arraycopy(T, 0, DK, 0, dkLen);
        return DK;
    }

    protected int ceil(int a, int b) {
        int m = 0;
        if (a % b > 0) {
            m = 1;
        }
        return (a / b) + m;
    }

    protected void _F(byte[] dest, int offset, PRF prf, byte[] S, int c, int blockIndex) {
        int hLen = prf.getHLen();
        byte[] U_r = new byte[hLen];
        byte[] U_i = new byte[(S.length + 4)];
        System.arraycopy(S, 0, U_i, 0, S.length);
        INT(U_i, S.length, blockIndex);
        for (int i = 0; i < c; i++) {
            U_i = prf.doFinal(U_i);
            xor(U_r, U_i);
        }
        System.arraycopy(U_r, 0, dest, offset, hLen);
    }

    protected void xor(byte[] dest, byte[] src) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] = (byte) (dest[i] ^ src[i]);
        }
    }

    protected void INT(byte[] dest, int offset, int i) {
        dest[offset + 0] = (byte) (i / ViewCompat.MEASURED_STATE_TOO_SMALL);
        dest[offset + 1] = (byte) (i / 65536);
        dest[offset + 2] = (byte) (i / 256);
        dest[offset + 3] = (byte) i;
    }

    public PBKDF2Parameters getParameters() {
        return this.parameters;
    }

    public void setParameters(PBKDF2Parameters parameters) {
        this.parameters = parameters;
    }

    public void setPseudoRandomFunction(PRF prf) {
        this.prf = prf;
    }
}
