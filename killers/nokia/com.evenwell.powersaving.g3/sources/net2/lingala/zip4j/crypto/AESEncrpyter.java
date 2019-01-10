package net2.lingala.zip4j.crypto;

import com.fihtdc.backuptool.FileOperator;
import java.util.Random;
import net2.lingala.zip4j.crypto.PBKDF2.MacBasedPRF;
import net2.lingala.zip4j.crypto.PBKDF2.PBKDF2Engine;
import net2.lingala.zip4j.crypto.PBKDF2.PBKDF2Parameters;
import net2.lingala.zip4j.crypto.engine.AESEngine;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.util.Raw;

public class AESEncrpyter implements IEncrypter {
    private int KEY_LENGTH;
    private int MAC_LENGTH;
    private final int PASSWORD_VERIFIER_LENGTH = 2;
    private int SALT_LENGTH;
    private AESEngine aesEngine;
    private byte[] aesKey;
    private byte[] counterBlock;
    private byte[] derivedPasswordVerifier;
    private boolean finished;
    private byte[] iv;
    private int keyStrength;
    private int loopCount = 0;
    private MacBasedPRF mac;
    private byte[] macKey;
    private int nonce = 1;
    private char[] password;
    private byte[] saltBytes;

    public AESEncrpyter(char[] password, int keyStrength) throws ZipException {
        if (password == null || password.length == 0) {
            throw new ZipException("input password is empty or null in AES encrypter constructor");
        } else if (keyStrength == 1 || keyStrength == 3) {
            this.password = password;
            this.keyStrength = keyStrength;
            this.finished = false;
            this.counterBlock = new byte[16];
            this.iv = new byte[16];
            init();
        } else {
            throw new ZipException("Invalid key strength in AES encrypter constructor");
        }
    }

    private void init() throws ZipException {
        switch (this.keyStrength) {
            case 1:
                this.KEY_LENGTH = 16;
                this.MAC_LENGTH = 16;
                this.SALT_LENGTH = 8;
                break;
            case 3:
                this.KEY_LENGTH = 32;
                this.MAC_LENGTH = 32;
                this.SALT_LENGTH = 16;
                break;
            default:
                throw new ZipException("invalid aes key strength, cannot determine key sizes");
        }
        this.saltBytes = generateSalt(this.SALT_LENGTH);
        byte[] keyBytes = deriveKey(this.saltBytes, this.password);
        if (keyBytes == null || keyBytes.length != (this.KEY_LENGTH + this.MAC_LENGTH) + 2) {
            throw new ZipException("invalid key generated, cannot decrypt file");
        }
        this.aesKey = new byte[this.KEY_LENGTH];
        this.macKey = new byte[this.MAC_LENGTH];
        this.derivedPasswordVerifier = new byte[2];
        System.arraycopy(keyBytes, 0, this.aesKey, 0, this.KEY_LENGTH);
        System.arraycopy(keyBytes, this.KEY_LENGTH, this.macKey, 0, this.MAC_LENGTH);
        System.arraycopy(keyBytes, this.KEY_LENGTH + this.MAC_LENGTH, this.derivedPasswordVerifier, 0, 2);
        this.aesEngine = new AESEngine(this.aesKey);
        this.mac = new MacBasedPRF("HmacSHA1");
        this.mac.init(this.macKey);
    }

    private byte[] deriveKey(byte[] salt, char[] password) throws ZipException {
        try {
            return new PBKDF2Engine(new PBKDF2Parameters("HmacSHA1", "ISO-8859-1", salt, FileOperator.MAX_DIR_LENGTH)).deriveKey(password, (this.KEY_LENGTH + this.MAC_LENGTH) + 2);
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    public int encryptData(byte[] buff) throws ZipException {
        if (buff != null) {
            return encryptData(buff, 0, buff.length);
        }
        throw new ZipException("input bytes are null, cannot perform AES encrpytion");
    }

    public int encryptData(byte[] buff, int start, int len) throws ZipException {
        if (this.finished) {
            throw new ZipException("AES Encrypter is in finished state (A non 16 byte block has already been passed to encrypter)");
        }
        if (len % 16 != 0) {
            this.finished = true;
        }
        int j = start;
        while (j < start + len) {
            this.loopCount = j + 16 <= start + len ? 16 : (start + len) - j;
            Raw.prepareBuffAESIVBytes(this.iv, this.nonce, 16);
            this.aesEngine.processBlock(this.iv, this.counterBlock);
            for (int k = 0; k < this.loopCount; k++) {
                buff[j + k] = (byte) (buff[j + k] ^ this.counterBlock[k]);
            }
            this.mac.update(buff, j, this.loopCount);
            this.nonce++;
            j += 16;
        }
        return len;
    }

    private static byte[] generateSalt(int size) throws ZipException {
        if (size == 8 || size == 16) {
            int rounds = 0;
            if (size == 8) {
                rounds = 2;
            }
            if (size == 16) {
                rounds = 4;
            }
            byte[] salt = new byte[size];
            for (int j = 0; j < rounds; j++) {
                int i = new Random().nextInt();
                salt[(j * 4) + 0] = (byte) (i >> 24);
                salt[(j * 4) + 1] = (byte) (i >> 16);
                salt[(j * 4) + 2] = (byte) (i >> 8);
                salt[(j * 4) + 3] = (byte) i;
            }
            return salt;
        }
        throw new ZipException("invalid salt size, cannot generate salt");
    }

    public byte[] getFinalMac() {
        byte[] macBytes = new byte[10];
        System.arraycopy(this.mac.doFinal(), 0, macBytes, 0, 10);
        return macBytes;
    }

    public byte[] getDerivedPasswordVerifier() {
        return this.derivedPasswordVerifier;
    }

    public void setDerivedPasswordVerifier(byte[] derivedPasswordVerifier) {
        this.derivedPasswordVerifier = derivedPasswordVerifier;
    }

    public byte[] getSaltBytes() {
        return this.saltBytes;
    }

    public void setSaltBytes(byte[] saltBytes) {
        this.saltBytes = saltBytes;
    }

    public int getSaltLength() {
        return this.SALT_LENGTH;
    }

    public int getPasswordVeriifierLength() {
        return 2;
    }
}
