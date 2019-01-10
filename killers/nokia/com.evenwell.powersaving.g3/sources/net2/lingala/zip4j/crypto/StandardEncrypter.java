package net2.lingala.zip4j.crypto;

import java.util.Random;
import net2.lingala.zip4j.crypto.engine.ZipCryptoEngine;
import net2.lingala.zip4j.exception.ZipException;

public class StandardEncrypter implements IEncrypter {
    private byte[] headerBytes;
    private ZipCryptoEngine zipCryptoEngine;

    public StandardEncrypter(char[] password, int crc) throws ZipException {
        if (password == null || password.length <= 0) {
            throw new ZipException("input password is null or empty in standard encrpyter constructor");
        }
        this.zipCryptoEngine = new ZipCryptoEngine();
        this.headerBytes = new byte[12];
        init(password, crc);
    }

    private void init(char[] password, int crc) throws ZipException {
        if (password == null || password.length <= 0) {
            throw new ZipException("input password is null or empty, cannot initialize standard encrypter");
        }
        this.zipCryptoEngine.initKeys(password);
        this.headerBytes = generateRandomBytes(12);
        this.zipCryptoEngine.initKeys(password);
        this.headerBytes[11] = (byte) (crc >>> 24);
        this.headerBytes[10] = (byte) (crc >>> 16);
        if (this.headerBytes.length < 12) {
            throw new ZipException("invalid header bytes generated, cannot perform standard encryption");
        }
        encryptData(this.headerBytes);
    }

    public int encryptData(byte[] buff) throws ZipException {
        if (buff != null) {
            return encryptData(buff, 0, buff.length);
        }
        throw new NullPointerException();
    }

    public int encryptData(byte[] buff, int start, int len) throws ZipException {
        if (len < 0) {
            throw new ZipException("invalid length specified to decrpyt data");
        }
        int i = start;
        while (i < start + len) {
            try {
                buff[i] = encryptByte(buff[i]);
                i++;
            } catch (Throwable e) {
                throw new ZipException(e);
            }
        }
        return len;
    }

    protected byte encryptByte(byte val) {
        byte temp_val = (byte) ((this.zipCryptoEngine.decryptByte() & 255) ^ val);
        this.zipCryptoEngine.updateKeys(val);
        return temp_val;
    }

    protected byte[] generateRandomBytes(int size) throws ZipException {
        if (size <= 0) {
            throw new ZipException("size is either 0 or less than 0, cannot generate header for standard encryptor");
        }
        byte[] buff = new byte[size];
        Random rand = new Random();
        for (int i = 0; i < buff.length; i++) {
            buff[i] = encryptByte((byte) rand.nextInt(256));
        }
        return buff;
    }

    public byte[] getHeaderBytes() {
        return this.headerBytes;
    }
}
