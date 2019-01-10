package net2.lingala.zip4j.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import net2.lingala.zip4j.crypto.AESDecrypter;
import net2.lingala.zip4j.crypto.IDecrypter;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.unzip.UnzipEngine;

public class PartInputStream extends BaseInputStream {
    private byte[] aesBlockByte = new byte[16];
    private int aesBytesReturned = 0;
    private long bytesRead;
    private int count = -1;
    private IDecrypter decrypter;
    private boolean isAESEncryptedFile = false;
    private long length;
    private byte[] oneByteBuff = new byte[1];
    private RandomAccessFile raf;
    private UnzipEngine unzipEngine;

    public PartInputStream(RandomAccessFile raf, long start, long len, UnzipEngine unzipEngine) {
        boolean z = true;
        this.raf = raf;
        this.unzipEngine = unzipEngine;
        this.decrypter = unzipEngine.getDecrypter();
        this.bytesRead = 0;
        this.length = len;
        if (!(unzipEngine.getFileHeader().isEncrypted() && unzipEngine.getFileHeader().getEncryptionMethod() == 99)) {
            z = false;
        }
        this.isAESEncryptedFile = z;
    }

    public int available() {
        long amount = this.length - this.bytesRead;
        if (amount > 2147483647L) {
            return Integer.MAX_VALUE;
        }
        return (int) amount;
    }

    public int read() throws IOException {
        if (this.bytesRead >= this.length) {
            return -1;
        }
        if (this.isAESEncryptedFile) {
            if (this.aesBytesReturned == 0 || this.aesBytesReturned == 16) {
                if (read(this.aesBlockByte) == -1) {
                    return -1;
                }
                this.aesBytesReturned = 0;
            }
            byte[] bArr = this.aesBlockByte;
            int i = this.aesBytesReturned;
            this.aesBytesReturned = i + 1;
            return bArr[i] & 255;
        } else if (read(this.oneByteBuff, 0, 1) != -1) {
            return this.oneByteBuff[0] & 255;
        } else {
            return -1;
        }
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (((long) len) > this.length - this.bytesRead) {
            len = (int) (this.length - this.bytesRead);
            if (len == 0) {
                checkAndReadAESMacBytes();
                return -1;
            }
        }
        if ((this.unzipEngine.getDecrypter() instanceof AESDecrypter) && this.bytesRead + ((long) len) < this.length && len % 16 != 0) {
            len -= len % 16;
        }
        synchronized (this.raf) {
            this.count = this.raf.read(b, off, len);
            if (this.count < len && this.unzipEngine.getZipModel().isSplitArchive()) {
                this.raf.close();
                this.raf = this.unzipEngine.startNextSplitFile();
                if (this.count < 0) {
                    this.count = 0;
                }
                int newlyRead = this.raf.read(b, this.count, len - this.count);
                if (newlyRead > 0) {
                    this.count += newlyRead;
                }
            }
        }
        if (this.count > 0) {
            if (this.decrypter != null) {
                try {
                    this.decrypter.decryptData(b, off, this.count);
                } catch (ZipException e) {
                    throw new IOException(e.getMessage());
                }
            }
            this.bytesRead += (long) this.count;
        }
        if (this.bytesRead >= this.length) {
            checkAndReadAESMacBytes();
        }
        return this.count;
    }

    private void checkAndReadAESMacBytes() throws IOException {
        if (this.isAESEncryptedFile && this.decrypter != null && (this.decrypter instanceof AESDecrypter) && ((AESDecrypter) this.decrypter).getStoredMac() == null) {
            byte[] macBytes = new byte[10];
            int readLen = this.raf.read(macBytes);
            if (readLen != 10) {
                if (this.unzipEngine.getZipModel().isSplitArchive()) {
                    this.raf.close();
                    this.raf = this.unzipEngine.startNextSplitFile();
                    readLen += this.raf.read(macBytes, readLen, 10 - readLen);
                } else {
                    throw new IOException("Error occured while reading stored AES authentication bytes");
                }
            }
            ((AESDecrypter) this.unzipEngine.getDecrypter()).setStoredMac(macBytes);
        }
    }

    public long skip(long amount) throws IOException {
        if (amount < 0) {
            throw new IllegalArgumentException();
        }
        if (amount > this.length - this.bytesRead) {
            amount = this.length - this.bytesRead;
        }
        this.bytesRead += amount;
        return amount;
    }

    public void close() throws IOException {
        this.raf.close();
    }

    public void seek(long pos) throws IOException {
        this.raf.seek(pos);
    }

    public UnzipEngine getUnzipEngine() {
        return this.unzipEngine;
    }
}
