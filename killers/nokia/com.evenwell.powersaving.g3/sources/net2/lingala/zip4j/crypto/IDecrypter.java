package net2.lingala.zip4j.crypto;

import net2.lingala.zip4j.exception.ZipException;

public interface IDecrypter {
    int decryptData(byte[] bArr) throws ZipException;

    int decryptData(byte[] bArr, int i, int i2) throws ZipException;
}
