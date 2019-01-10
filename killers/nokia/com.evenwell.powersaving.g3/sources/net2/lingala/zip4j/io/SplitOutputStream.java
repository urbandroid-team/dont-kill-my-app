package net2.lingala.zip4j.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.util.InternalZipConstants;
import net2.lingala.zip4j.util.Raw;
import net2.lingala.zip4j.util.Zip4jUtil;

public class SplitOutputStream extends OutputStream {
    private long bytesWrittenForThisPart;
    private int currSplitFileCounter;
    private File outFile;
    private RandomAccessFile raf;
    private long splitLength;
    private File zipFile;

    public SplitOutputStream(String name) throws FileNotFoundException, ZipException {
        this(Zip4jUtil.isStringNotNullAndNotEmpty(name) ? new File(name) : null);
    }

    public SplitOutputStream(File file) throws FileNotFoundException, ZipException {
        this(file, -1);
    }

    public SplitOutputStream(String name, long splitLength) throws FileNotFoundException, ZipException {
        this(!Zip4jUtil.isStringNotNullAndNotEmpty(name) ? new File(name) : null, splitLength);
    }

    public SplitOutputStream(File file, long splitLength) throws FileNotFoundException, ZipException {
        if (splitLength < 0 || splitLength >= 65536) {
            this.raf = new RandomAccessFile(file, InternalZipConstants.WRITE_MODE);
            this.splitLength = splitLength;
            this.outFile = file;
            this.zipFile = file;
            this.currSplitFileCounter = 0;
            this.bytesWrittenForThisPart = 0;
            return;
        }
        throw new ZipException("split length less than minimum allowed split length of 65536 Bytes");
    }

    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (len > 0) {
            if (this.splitLength == -1) {
                this.raf.write(b, off, len);
                this.bytesWrittenForThisPart += (long) len;
            } else if (this.splitLength < 65536) {
                throw new IOException("split length less than minimum allowed split length of 65536 Bytes");
            } else if (this.bytesWrittenForThisPart >= this.splitLength) {
                startNextSplitFile();
                this.raf.write(b, off, len);
                this.bytesWrittenForThisPart = (long) len;
            } else if (this.bytesWrittenForThisPart + ((long) len) <= this.splitLength) {
                this.raf.write(b, off, len);
                this.bytesWrittenForThisPart += (long) len;
            } else if (isHeaderData(b)) {
                startNextSplitFile();
                this.raf.write(b, off, len);
                this.bytesWrittenForThisPart = (long) len;
            } else {
                this.raf.write(b, off, (int) (this.splitLength - this.bytesWrittenForThisPart));
                startNextSplitFile();
                this.raf.write(b, ((int) (this.splitLength - this.bytesWrittenForThisPart)) + off, (int) (((long) len) - (this.splitLength - this.bytesWrittenForThisPart)));
                this.bytesWrittenForThisPart = ((long) len) - (this.splitLength - this.bytesWrittenForThisPart);
            }
        }
    }

    private void startNextSplitFile() throws IOException {
        try {
            File currSplitFile;
            String zipFileWithoutExt = Zip4jUtil.getZipFileNameWithoutExt(this.outFile.getName());
            String zipFileName = this.zipFile.getAbsolutePath();
            if (this.currSplitFileCounter < 9) {
                currSplitFile = new File(this.outFile.getParent() + System.getProperty("file.separator") + zipFileWithoutExt + ".z0" + (this.currSplitFileCounter + 1));
            } else {
                currSplitFile = new File(this.outFile.getParent() + System.getProperty("file.separator") + zipFileWithoutExt + ".z" + (this.currSplitFileCounter + 1));
            }
            this.raf.close();
            if (currSplitFile.exists()) {
                throw new IOException("split file: " + currSplitFile.getName() + " already exists in the current directory, cannot rename this file");
            } else if (this.zipFile.renameTo(currSplitFile)) {
                this.zipFile = new File(zipFileName);
                this.raf = new RandomAccessFile(this.zipFile, InternalZipConstants.WRITE_MODE);
                this.currSplitFileCounter++;
            } else {
                throw new IOException("cannot rename newly created split file");
            }
        } catch (ZipException e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean isHeaderData(byte[] buff) {
        if (buff == null || buff.length < 4) {
            return false;
        }
        int signature = Raw.readIntLittleEndian(buff, 0);
        long[] allHeaderSignatures = Zip4jUtil.getAllHeaderSignatures();
        if (allHeaderSignatures == null || allHeaderSignatures.length <= 0) {
            return false;
        }
        int i = 0;
        while (i < allHeaderSignatures.length) {
            if (allHeaderSignatures[i] != 134695760 && allHeaderSignatures[i] == ((long) signature)) {
                return true;
            }
            i++;
        }
        return false;
    }

    public boolean checkBuffSizeAndStartNextSplitFile(int bufferSize) throws ZipException {
        if (bufferSize < 0) {
            throw new ZipException("negative buffersize for checkBuffSizeAndStartNextSplitFile");
        } else if (isBuffSizeFitForCurrSplitFile(bufferSize)) {
            return false;
        } else {
            try {
                startNextSplitFile();
                this.bytesWrittenForThisPart = 0;
                return true;
            } catch (Throwable e) {
                throw new ZipException(e);
            }
        }
    }

    public boolean isBuffSizeFitForCurrSplitFile(int bufferSize) throws ZipException {
        if (bufferSize < 0) {
            throw new ZipException("negative buffersize for isBuffSizeFitForCurrSplitFile");
        } else if (this.splitLength < 65536 || this.bytesWrittenForThisPart + ((long) bufferSize) <= this.splitLength) {
            return true;
        } else {
            return false;
        }
    }

    public void seek(long pos) throws IOException {
        this.raf.seek(pos);
    }

    public void close() throws IOException {
        if (this.raf != null) {
            this.raf.close();
        }
    }

    public void flush() throws IOException {
    }

    public long getFilePointer() throws IOException {
        return this.raf.getFilePointer();
    }

    public boolean isSplitZipFile() {
        return this.splitLength != -1;
    }

    public long getSplitLength() {
        return this.splitLength;
    }

    public int getCurrSplitFileCounter() {
        return this.currSplitFileCounter;
    }
}
