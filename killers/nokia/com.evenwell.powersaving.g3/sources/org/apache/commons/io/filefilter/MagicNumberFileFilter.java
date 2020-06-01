package org.apache.commons.io.filefilter;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import net2.lingala.zip4j.util.InternalZipConstants;
import org.apache.commons.io.IOUtils;

public class MagicNumberFileFilter extends AbstractFileFilter implements Serializable {
    private static final long serialVersionUID = -547733176983104172L;
    private final long byteOffset;
    private final byte[] magicNumbers;

    public MagicNumberFileFilter(byte[] magicNumber) {
        this(magicNumber, 0);
    }

    public MagicNumberFileFilter(String magicNumber) {
        this(magicNumber, 0);
    }

    public MagicNumberFileFilter(String magicNumber, long offset) {
        if (magicNumber == null) {
            throw new IllegalArgumentException("The magic number cannot be null");
        } else if (magicNumber.isEmpty()) {
            throw new IllegalArgumentException("The magic number must contain at least one byte");
        } else if (offset < 0) {
            throw new IllegalArgumentException("The offset cannot be negative");
        } else {
            this.magicNumbers = magicNumber.getBytes(Charset.defaultCharset());
            this.byteOffset = offset;
        }
    }

    public MagicNumberFileFilter(byte[] magicNumber, long offset) {
        if (magicNumber == null) {
            throw new IllegalArgumentException("The magic number cannot be null");
        } else if (magicNumber.length == 0) {
            throw new IllegalArgumentException("The magic number must contain at least one byte");
        } else if (offset < 0) {
            throw new IllegalArgumentException("The offset cannot be negative");
        } else {
            this.magicNumbers = new byte[magicNumber.length];
            System.arraycopy(magicNumber, 0, this.magicNumbers, 0, magicNumber.length);
            this.byteOffset = offset;
        }
    }

    public boolean accept(File file) {
        Throwable th;
        boolean z = false;
        if (file != null && file.isFile() && file.canRead()) {
            Closeable randomAccessFile = null;
            try {
                byte[] fileBytes = new byte[this.magicNumbers.length];
                Closeable randomAccessFile2 = new RandomAccessFile(file, InternalZipConstants.READ_MODE);
                try {
                    randomAccessFile2.seek(this.byteOffset);
                    if (randomAccessFile2.read(fileBytes) != this.magicNumbers.length) {
                        IOUtils.closeQuietly(randomAccessFile2);
                    } else {
                        z = Arrays.equals(this.magicNumbers, fileBytes);
                        IOUtils.closeQuietly(randomAccessFile2);
                    }
                } catch (IOException e) {
                    randomAccessFile = randomAccessFile2;
                    IOUtils.closeQuietly(randomAccessFile);
                    return z;
                } catch (Throwable th2) {
                    th = th2;
                    randomAccessFile = randomAccessFile2;
                    IOUtils.closeQuietly(randomAccessFile);
                    throw th;
                }
            } catch (IOException e2) {
                IOUtils.closeQuietly(randomAccessFile);
                return z;
            } catch (Throwable th3) {
                th = th3;
                IOUtils.closeQuietly(randomAccessFile);
                throw th;
            }
        }
        return z;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append("(");
        builder.append(new String(this.magicNumbers, Charset.defaultCharset()));
        builder.append(",");
        builder.append(this.byteOffset);
        builder.append(")");
        return builder.toString();
    }
}
