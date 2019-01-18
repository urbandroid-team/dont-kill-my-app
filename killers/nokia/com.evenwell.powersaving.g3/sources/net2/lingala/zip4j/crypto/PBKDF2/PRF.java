package net2.lingala.zip4j.crypto.PBKDF2;

interface PRF {
    byte[] doFinal(byte[] bArr);

    int getHLen();

    void init(byte[] bArr);
}
