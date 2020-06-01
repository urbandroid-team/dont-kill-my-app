package net2.lingala.zip4j.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.model.ZipModel;
import net2.lingala.zip4j.model.ZipParameters;

public class DeflaterOutputStream extends CipherOutputStream {
    private byte[] buff = new byte[4096];
    protected Deflater deflater = new Deflater();
    private boolean firstBytesRead = false;

    public DeflaterOutputStream(OutputStream outputStream, ZipModel zipModel) {
        super(outputStream, zipModel);
    }

    public void putNextEntry(File file, ZipParameters zipParameters) throws ZipException {
        super.putNextEntry(file, zipParameters);
        if (zipParameters.getCompressionMethod() == 8) {
            this.deflater.reset();
            if ((zipParameters.getCompressionLevel() < 0 || zipParameters.getCompressionLevel() > 9) && zipParameters.getCompressionLevel() != -1) {
                throw new ZipException("invalid compression level for deflater. compression level should be in the range of 0-9");
            }
            this.deflater.setLevel(zipParameters.getCompressionLevel());
        }
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    private void deflate() throws IOException {
        int len = this.deflater.deflate(this.buff, 0, this.buff.length);
        if (len > 0) {
            if (this.deflater.finished()) {
                if (len != 4) {
                    if (len < 4) {
                        decrementCompressedFileSize(4 - len);
                        return;
                    }
                    len -= 4;
                } else {
                    return;
                }
            }
            if (this.firstBytesRead) {
                super.write(this.buff, 0, len);
                return;
            }
            super.write(this.buff, 2, len - 2);
            this.firstBytesRead = true;
        }
    }

    public void write(int bval) throws IOException {
        write(new byte[]{(byte) bval}, 0, 1);
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        if (this.zipParameters.getCompressionMethod() != 8) {
            super.write(buf, off, len);
            return;
        }
        this.deflater.setInput(buf, off, len);
        while (!this.deflater.needsInput()) {
            deflate();
        }
    }

    public void closeEntry() throws IOException, ZipException {
        if (this.zipParameters.getCompressionMethod() == 8) {
            if (!this.deflater.finished()) {
                this.deflater.finish();
                while (!this.deflater.finished()) {
                    deflate();
                }
            }
            this.firstBytesRead = false;
        }
        super.closeEntry();
    }

    public void finish() throws IOException, ZipException {
        super.finish();
    }
}
