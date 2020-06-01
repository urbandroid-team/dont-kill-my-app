package net2.lingala.zip4j.model;

import com.fihtdc.backuptool.BackupRestoreService;
import java.util.ArrayList;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.progress.ProgressMonitor;
import net2.lingala.zip4j.unzip.Unzip;
import net2.lingala.zip4j.util.InternalZipConstants;
import net2.lingala.zip4j.util.Zip4jUtil;

public class FileHeader {
    private AESExtraDataRecord aesExtraDataRecord;
    private long compressedSize;
    private int compressionMethod;
    private long crc32 = 0;
    private byte[] crcBuff;
    private boolean dataDescriptorExists;
    private int diskNumberStart;
    private int encryptionMethod = -1;
    private byte[] externalFileAttr;
    private ArrayList extraDataRecords;
    private int extraFieldLength;
    private String fileComment;
    private int fileCommentLength;
    private String fileName;
    private int fileNameLength;
    private boolean fileNameUTF8Encoded;
    private byte[] generalPurposeFlag;
    private byte[] internalFileAttr;
    private boolean isDirectory;
    private boolean isEncrypted;
    private int lastModFileTime;
    private long offsetLocalHeader;
    private char[] password;
    private BackupRestoreService service;
    private int signature;
    private long uncompressedSize = 0;
    private int versionMadeBy;
    private int versionNeededToExtract;
    private Zip64ExtendedInfo zip64ExtendedInfo;

    public void setService(BackupRestoreService service) {
        this.service = service;
    }

    public BackupRestoreService getService() {
        return this.service;
    }

    public int getSignature() {
        return this.signature;
    }

    public void setSignature(int signature) {
        this.signature = signature;
    }

    public int getVersionMadeBy() {
        return this.versionMadeBy;
    }

    public void setVersionMadeBy(int versionMadeBy) {
        this.versionMadeBy = versionMadeBy;
    }

    public int getVersionNeededToExtract() {
        return this.versionNeededToExtract;
    }

    public void setVersionNeededToExtract(int versionNeededToExtract) {
        this.versionNeededToExtract = versionNeededToExtract;
    }

    public byte[] getGeneralPurposeFlag() {
        return this.generalPurposeFlag;
    }

    public void setGeneralPurposeFlag(byte[] generalPurposeFlag) {
        this.generalPurposeFlag = generalPurposeFlag;
    }

    public int getCompressionMethod() {
        return this.compressionMethod;
    }

    public void setCompressionMethod(int compressionMethod) {
        this.compressionMethod = compressionMethod;
    }

    public int getLastModFileTime() {
        return this.lastModFileTime;
    }

    public void setLastModFileTime(int lastModFileTime) {
        this.lastModFileTime = lastModFileTime;
    }

    public long getCrc32() {
        return this.crc32 & InternalZipConstants.ZIP_64_LIMIT;
    }

    public void setCrc32(long crc32) {
        this.crc32 = crc32;
    }

    public long getCompressedSize() {
        return this.compressedSize;
    }

    public void setCompressedSize(long compressedSize) {
        this.compressedSize = compressedSize;
    }

    public long getUncompressedSize() {
        return this.uncompressedSize;
    }

    public void setUncompressedSize(long uncompressedSize) {
        this.uncompressedSize = uncompressedSize;
    }

    public int getFileNameLength() {
        return this.fileNameLength;
    }

    public void setFileNameLength(int fileNameLength) {
        this.fileNameLength = fileNameLength;
    }

    public int getExtraFieldLength() {
        return this.extraFieldLength;
    }

    public void setExtraFieldLength(int extraFieldLength) {
        this.extraFieldLength = extraFieldLength;
    }

    public int getFileCommentLength() {
        return this.fileCommentLength;
    }

    public void setFileCommentLength(int fileCommentLength) {
        this.fileCommentLength = fileCommentLength;
    }

    public int getDiskNumberStart() {
        return this.diskNumberStart;
    }

    public void setDiskNumberStart(int diskNumberStart) {
        this.diskNumberStart = diskNumberStart;
    }

    public byte[] getInternalFileAttr() {
        return this.internalFileAttr;
    }

    public void setInternalFileAttr(byte[] internalFileAttr) {
        this.internalFileAttr = internalFileAttr;
    }

    public byte[] getExternalFileAttr() {
        return this.externalFileAttr;
    }

    public void setExternalFileAttr(byte[] externalFileAttr) {
        this.externalFileAttr = externalFileAttr;
    }

    public long getOffsetLocalHeader() {
        return this.offsetLocalHeader;
    }

    public void setOffsetLocalHeader(long offsetLocalHeader) {
        this.offsetLocalHeader = offsetLocalHeader;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileComment() {
        return this.fileComment;
    }

    public void setFileComment(String fileComment) {
        this.fileComment = fileComment;
    }

    public boolean isDirectory() {
        return this.isDirectory;
    }

    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public void extractFile(ZipModel zipModel, String outPath, ProgressMonitor progressMonitor, boolean runInThread) throws ZipException {
        extractFile(zipModel, outPath, null, progressMonitor, runInThread);
    }

    public void extractFile(ZipModel zipModel, String outPath, UnzipParameters unzipParameters, ProgressMonitor progressMonitor, boolean runInThread) throws ZipException {
        extractFile(zipModel, outPath, unzipParameters, null, progressMonitor, runInThread);
    }

    public void extractFile(ZipModel zipModel, String outPath, UnzipParameters unzipParameters, String newFileName, ProgressMonitor progressMonitor, boolean runInThread) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("input zipModel is null");
        } else if (!Zip4jUtil.checkOutputFolder(outPath)) {
            throw new ZipException("Invalid output path");
        } else if (this == null) {
            throw new ZipException("invalid file header");
        } else {
            new Unzip(zipModel).extractFile(this, outPath, unzipParameters, newFileName, progressMonitor, runInThread);
        }
    }

    public boolean isEncrypted() {
        return this.isEncrypted;
    }

    public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public int getEncryptionMethod() {
        return this.encryptionMethod;
    }

    public void setEncryptionMethod(int encryptionMethod) {
        this.encryptionMethod = encryptionMethod;
    }

    public char[] getPassword() {
        return this.password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public byte[] getCrcBuff() {
        return this.crcBuff;
    }

    public void setCrcBuff(byte[] crcBuff) {
        this.crcBuff = crcBuff;
    }

    public ArrayList getExtraDataRecords() {
        return this.extraDataRecords;
    }

    public void setExtraDataRecords(ArrayList extraDataRecords) {
        this.extraDataRecords = extraDataRecords;
    }

    public boolean isDataDescriptorExists() {
        return this.dataDescriptorExists;
    }

    public void setDataDescriptorExists(boolean dataDescriptorExists) {
        this.dataDescriptorExists = dataDescriptorExists;
    }

    public Zip64ExtendedInfo getZip64ExtendedInfo() {
        return this.zip64ExtendedInfo;
    }

    public void setZip64ExtendedInfo(Zip64ExtendedInfo zip64ExtendedInfo) {
        this.zip64ExtendedInfo = zip64ExtendedInfo;
    }

    public AESExtraDataRecord getAesExtraDataRecord() {
        return this.aesExtraDataRecord;
    }

    public void setAesExtraDataRecord(AESExtraDataRecord aesExtraDataRecord) {
        this.aesExtraDataRecord = aesExtraDataRecord;
    }

    public boolean isFileNameUTF8Encoded() {
        return this.fileNameUTF8Encoded;
    }

    public void setFileNameUTF8Encoded(boolean fileNameUTF8Encoded) {
        this.fileNameUTF8Encoded = fileNameUTF8Encoded;
    }
}
