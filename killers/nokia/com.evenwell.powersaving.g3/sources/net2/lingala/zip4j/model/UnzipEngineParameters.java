package net2.lingala.zip4j.model;

import java.io.FileOutputStream;
import net2.lingala.zip4j.crypto.IDecrypter;
import net2.lingala.zip4j.unzip.UnzipEngine;

public class UnzipEngineParameters {
    private FileHeader fileHeader;
    private IDecrypter iDecryptor;
    private LocalFileHeader localFileHeader;
    private FileOutputStream outputStream;
    private UnzipEngine unzipEngine;
    private ZipModel zipModel;

    public ZipModel getZipModel() {
        return this.zipModel;
    }

    public void setZipModel(ZipModel zipModel) {
        this.zipModel = zipModel;
    }

    public FileHeader getFileHeader() {
        return this.fileHeader;
    }

    public void setFileHeader(FileHeader fileHeader) {
        this.fileHeader = fileHeader;
    }

    public LocalFileHeader getLocalFileHeader() {
        return this.localFileHeader;
    }

    public void setLocalFileHeader(LocalFileHeader localFileHeader) {
        this.localFileHeader = localFileHeader;
    }

    public IDecrypter getIDecryptor() {
        return this.iDecryptor;
    }

    public void setIDecryptor(IDecrypter decrypter) {
        this.iDecryptor = decrypter;
    }

    public FileOutputStream getOutputStream() {
        return this.outputStream;
    }

    public void setOutputStream(FileOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public UnzipEngine getUnzipEngine() {
        return this.unzipEngine;
    }

    public void setUnzipEngine(UnzipEngine unzipEngine) {
        this.unzipEngine = unzipEngine;
    }
}
