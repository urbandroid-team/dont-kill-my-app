package net2.lingala.zip4j.crypto.PBKDF2;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MacBasedPRF implements PRF {
    protected int hLen;
    protected Mac mac;
    protected String macAlgorithm;

    public MacBasedPRF(String macAlgorithm) {
        this.macAlgorithm = macAlgorithm;
        try {
            this.mac = Mac.getInstance(macAlgorithm);
            this.hLen = this.mac.getMacLength();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public MacBasedPRF(String macAlgorithm, String provider) {
        this.macAlgorithm = macAlgorithm;
        try {
            this.mac = Mac.getInstance(macAlgorithm, provider);
            this.hLen = this.mac.getMacLength();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e2) {
            throw new RuntimeException(e2);
        }
    }

    public byte[] doFinal(byte[] M) {
        return this.mac.doFinal(M);
    }

    public byte[] doFinal() {
        return this.mac.doFinal();
    }

    public int getHLen() {
        return this.hLen;
    }

    public void init(byte[] P) {
        try {
            this.mac.init(new SecretKeySpec(P, this.macAlgorithm));
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(byte[] U) {
        try {
            this.mac.update(U);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(byte[] U, int start, int len) {
        try {
            this.mac.update(U, start, len);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }
}
