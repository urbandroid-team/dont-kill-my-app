package net2.lingala.zip4j.crypto;

import net2.lingala.zip4j.exception.ZipException;

public interface IEncrypter {
    int encryptData(byte[] bArr) throws ZipException;

    int encryptData(byte[] bArr, int i, int i2) throws ZipException;
}
