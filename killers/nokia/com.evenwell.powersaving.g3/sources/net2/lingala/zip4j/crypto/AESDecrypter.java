package net2.lingala.zip4j.crypto;

import com.fihtdc.backuptool.FileOperator;
import java.util.Arrays;
import net2.lingala.zip4j.crypto.PBKDF2.MacBasedPRF;
import net2.lingala.zip4j.crypto.PBKDF2.PBKDF2Engine;
import net2.lingala.zip4j.crypto.PBKDF2.PBKDF2Parameters;
import net2.lingala.zip4j.crypto.engine.AESEngine;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.model.AESExtraDataRecord;
import net2.lingala.zip4j.model.LocalFileHeader;
import net2.lingala.zip4j.util.Raw;

public class AESDecrypter implements IDecrypter {
    private int KEY_LENGTH;
    private int MAC_LENGTH;
    private final int PASSWORD_VERIFIER_LENGTH = 2;
    private int SALT_LENGTH;
    private AESEngine aesEngine;
    private byte[] aesKey;
    private byte[] counterBlock;
    private byte[] derivedPasswordVerifier;
    private byte[] iv;
    private LocalFileHeader localFileHeader;
    private int loopCount = 0;
    private MacBasedPRF mac;
    private byte[] macKey;
    private int nonce = 1;
    private byte[] storedMac;

    public AESDecrypter(LocalFileHeader localFileHeader, byte[] salt, byte[] passwordVerifier) throws ZipException {
        if (localFileHeader == null) {
            throw new ZipException("one of the input parameters is null in AESDecryptor Constructor");
        }
        this.localFileHeader = localFileHeader;
        this.storedMac = null;
        this.iv = new byte[16];
        this.counterBlock = new byte[16];
        init(salt, passwordVerifier);
    }

    private void init(byte[] salt, byte[] passwordVerifier) throws ZipException {
        if (this.localFileHeader == null) {
            throw new ZipException("invalid file header in init method of AESDecryptor");
        }
        AESExtraDataRecord aesExtraDataRecord = this.localFileHeader.getAesExtraDataRecord();
        if (aesExtraDataRecord == null) {
            throw new ZipException("invalid aes extra data record - in init method of AESDecryptor");
        }
        switch (aesExtraDataRecord.getAesStrength()) {
            case 1:
                this.KEY_LENGTH = 16;
                this.MAC_LENGTH = 16;
                this.SALT_LENGTH = 8;
                break;
            case 2:
                this.KEY_LENGTH = 24;
                this.MAC_LENGTH = 24;
                this.SALT_LENGTH = 12;
                break;
            case 3:
                this.KEY_LENGTH = 32;
                this.MAC_LENGTH = 32;
                this.SALT_LENGTH = 16;
                break;
            default:
                throw new ZipException("invalid aes key strength for file: " + this.localFileHeader.getFileName());
        }
        if (this.localFileHeader.getPassword() == null || this.localFileHeader.getPassword().length <= 0) {
            throw new ZipException("empty or null password provided for AES Decryptor");
        }
        byte[] derivedKey = deriveKey(salt, this.localFileHeader.getPassword());
        if (derivedKey == null || derivedKey.length != (this.KEY_LENGTH + this.MAC_LENGTH) + 2) {
            throw new ZipException("invalid derived key");
        }
        this.aesKey = new byte[this.KEY_LENGTH];
        this.macKey = new byte[this.MAC_LENGTH];
        this.derivedPasswordVerifier = new byte[2];
        System.arraycopy(derivedKey, 0, this.aesKey, 0, this.KEY_LENGTH);
        System.arraycopy(derivedKey, this.KEY_LENGTH, this.macKey, 0, this.MAC_LENGTH);
        System.arraycopy(derivedKey, this.KEY_LENGTH + this.MAC_LENGTH, this.derivedPasswordVerifier, 0, 2);
        if (this.derivedPasswordVerifier == null) {
            throw new ZipException("invalid derived password verifier for AES");
        } else if (Arrays.equals(passwordVerifier, this.derivedPasswordVerifier)) {
            this.aesEngine = new AESEngine(this.aesKey);
            this.mac = new MacBasedPRF("HmacSHA1");
            this.mac.init(this.macKey);
        } else {
            throw new ZipException("Wrong Password for file: " + this.localFileHeader.getFileName(), 5);
        }
    }

    public int decryptData(byte[] buff, int start, int len) throws ZipException {
        if (this.aesEngine == null) {
            throw new ZipException("AES not initialized properly");
        }
        int j = start;
        while (j < start + len) {
            int i;
            if (j + 16 <= start + len) {
                i = 16;
            } else {
                i = (start + len) - j;
            }
            try {
                this.loopCount = i;
                this.mac.update(buff, j, this.loopCount);
                Raw.prepareBuffAESIVBytes(this.iv, this.nonce, 16);
                this.aesEngine.processBlock(this.iv, this.counterBlock);
                for (int k = 0; k < this.loopCount; k++) {
                    buff[j + k] = (byte) (buff[j + k] ^ this.counterBlock[k]);
                }
                this.nonce++;
                j += 16;
            } catch (ZipException e) {
                throw e;
            } catch (Throwable e2) {
                throw new ZipException(e2);
            }
        }
        return len;
    }

    public int decryptData(byte[] buff) throws ZipException {
        return decryptData(buff, 0, buff.length);
    }

    private byte[] deriveKey(byte[] salt, char[] password) throws ZipException {
        try {
            return new PBKDF2Engine(new PBKDF2Parameters("HmacSHA1", "ISO-8859-1", salt, FileOperator.MAX_DIR_LENGTH)).deriveKey(password, (this.KEY_LENGTH + this.MAC_LENGTH) + 2);
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    public int getPasswordVerifierLength() {
        return 2;
    }

    public int getSaltLength() {
        return this.SALT_LENGTH;
    }

    public byte[] getCalculatedAuthenticationBytes() {
        return this.mac.doFinal();
    }

    public void setStoredMac(byte[] storedMac) {
        this.storedMac = storedMac;
    }

    public byte[] getStoredMac() {
        return this.storedMac;
    }
}
