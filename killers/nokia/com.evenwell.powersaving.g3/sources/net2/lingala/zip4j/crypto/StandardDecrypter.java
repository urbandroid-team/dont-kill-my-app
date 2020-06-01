package net2.lingala.zip4j.crypto;

import net2.lingala.zip4j.crypto.engine.ZipCryptoEngine;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.model.FileHeader;

public class StandardDecrypter implements IDecrypter {
    private byte[] crc = new byte[4];
    private FileHeader fileHeader;
    private ZipCryptoEngine zipCryptoEngine;

    public StandardDecrypter(FileHeader fileHeader, byte[] headerBytes) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("one of more of the input parameters were null in StandardDecryptor");
        }
        this.fileHeader = fileHeader;
        this.zipCryptoEngine = new ZipCryptoEngine();
        init(headerBytes);
    }

    public int decryptData(byte[] buff) throws ZipException {
        return decryptData(buff, 0, buff.length);
    }

    public int decryptData(byte[] buff, int start, int len) throws ZipException {
        if (start < 0 || len < 0) {
            throw new ZipException("one of the input parameters were null in standard decrpyt data");
        }
        int i = 0;
        while (i < len) {
            try {
                int val = (this.zipCryptoEngine.decryptByte() ^ (buff[i] & 255)) & 255;
                this.zipCryptoEngine.updateKeys((byte) val);
                buff[i] = (byte) val;
                i++;
            } catch (Throwable e) {
                throw new ZipException(e);
            }
        }
        return len;
    }

    public void init(byte[] headerBytes) throws ZipException {
        byte[] crcBuff = this.fileHeader.getCrcBuff();
        this.crc[3] = (byte) (crcBuff[3] & 255);
        this.crc[2] = (byte) ((crcBuff[3] >> 8) & 255);
        this.crc[1] = (byte) ((crcBuff[3] >> 16) & 255);
        this.crc[0] = (byte) ((crcBuff[3] >> 24) & 255);
        if (this.crc[2] > (byte) 0 || this.crc[1] > (byte) 0 || this.crc[0] > (byte) 0) {
            throw new IllegalStateException("Invalid CRC in File Header");
        } else if (this.fileHeader.getPassword() == null || this.fileHeader.getPassword().length <= 0) {
            throw new ZipException("Wrong password!", 5);
        } else {
            this.zipCryptoEngine.initKeys(this.fileHeader.getPassword());
            try {
                int result = headerBytes[0];
                for (int i = 0; i < 12; i++) {
                    this.zipCryptoEngine.updateKeys((byte) (this.zipCryptoEngine.decryptByte() ^ result));
                    if (i + 1 != 12) {
                        result = headerBytes[i + 1];
                    }
                }
            } catch (Throwable e) {
                throw new ZipException(e);
            }
        }
    }
}
