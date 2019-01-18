package net2.lingala.zip4j.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.CRC32;
import net2.lingala.zip4j.core.HeaderWriter;
import net2.lingala.zip4j.crypto.AESEncrpyter;
import net2.lingala.zip4j.crypto.IEncrypter;
import net2.lingala.zip4j.crypto.StandardEncrypter;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.model.AESExtraDataRecord;
import net2.lingala.zip4j.model.CentralDirectory;
import net2.lingala.zip4j.model.EndCentralDirRecord;
import net2.lingala.zip4j.model.FileHeader;
import net2.lingala.zip4j.model.LocalFileHeader;
import net2.lingala.zip4j.model.ZipModel;
import net2.lingala.zip4j.model.ZipParameters;
import net2.lingala.zip4j.util.InternalZipConstants;
import net2.lingala.zip4j.util.Raw;
import net2.lingala.zip4j.util.Zip4jUtil;

public class CipherOutputStream extends BaseOutputStream {
    private long bytesWrittenForThisFile = 0;
    protected CRC32 crc = new CRC32();
    private IEncrypter encrypter;
    protected FileHeader fileHeader;
    protected LocalFileHeader localFileHeader;
    protected OutputStream outputStream;
    private byte[] pendingBuffer = new byte[16];
    private int pendingBufferLength = 0;
    private File sourceFile;
    private long totalBytesRead = 0;
    private long totalBytesWritten = 0;
    protected ZipModel zipModel;
    protected ZipParameters zipParameters;

    public CipherOutputStream(OutputStream outputStream, ZipModel zipModel) {
        this.outputStream = outputStream;
        initZipModel(zipModel);
    }

    public void putNextEntry(File file, ZipParameters zipParameters) throws ZipException {
        if (!zipParameters.isSourceExternalStream() && file == null) {
            throw new ZipException("input file is null");
        } else if (zipParameters.isSourceExternalStream() || Zip4jUtil.checkFileExists(file)) {
            if (zipParameters == null) {
                zipParameters = new ZipParameters();
            }
            try {
                this.sourceFile = file;
                this.zipParameters = (ZipParameters) zipParameters.clone();
                if (zipParameters.isSourceExternalStream()) {
                    if (!Zip4jUtil.isStringNotNullAndNotEmpty(this.zipParameters.getFileNameInZip())) {
                        throw new ZipException("file name is empty for external stream");
                    } else if (this.zipParameters.getFileNameInZip().endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR) || this.zipParameters.getFileNameInZip().endsWith("\\")) {
                        this.zipParameters.setEncryptFiles(false);
                        this.zipParameters.setEncryptionMethod(-1);
                        this.zipParameters.setCompressionMethod(0);
                    }
                } else if (this.sourceFile.isDirectory()) {
                    this.zipParameters.setEncryptFiles(false);
                    this.zipParameters.setEncryptionMethod(-1);
                    this.zipParameters.setCompressionMethod(0);
                }
                createFileHeader();
                createLocalFileHeader();
                if (this.zipModel.isSplitArchive() && (this.zipModel.getCentralDirectory() == null || this.zipModel.getCentralDirectory().getFileHeaders() == null || this.zipModel.getCentralDirectory().getFileHeaders().size() == 0)) {
                    byte[] intByte = new byte[4];
                    Raw.writeIntLittleEndian(intByte, 0, 134695760);
                    this.outputStream.write(intByte);
                    this.totalBytesWritten += 4;
                }
                if (this.outputStream instanceof SplitOutputStream) {
                    if (this.totalBytesWritten == 4) {
                        this.fileHeader.setOffsetLocalHeader(4);
                    } else {
                        this.fileHeader.setOffsetLocalHeader(((SplitOutputStream) this.outputStream).getFilePointer());
                    }
                } else if (this.totalBytesWritten == 4) {
                    this.fileHeader.setOffsetLocalHeader(4);
                } else {
                    this.fileHeader.setOffsetLocalHeader(this.totalBytesWritten);
                }
                this.totalBytesWritten += (long) new HeaderWriter().writeLocalFileHeader(this.zipModel, this.localFileHeader, this.outputStream);
                if (this.zipParameters.isEncryptFiles()) {
                    initEncrypter();
                    if (this.encrypter != null) {
                        if (zipParameters.getEncryptionMethod() == 0) {
                            byte[] headerBytes = ((StandardEncrypter) this.encrypter).getHeaderBytes();
                            this.outputStream.write(headerBytes);
                            this.totalBytesWritten += (long) headerBytes.length;
                            this.bytesWrittenForThisFile += (long) headerBytes.length;
                        } else if (zipParameters.getEncryptionMethod() == 99) {
                            byte[] saltBytes = ((AESEncrpyter) this.encrypter).getSaltBytes();
                            byte[] passwordVerifier = ((AESEncrpyter) this.encrypter).getDerivedPasswordVerifier();
                            this.outputStream.write(saltBytes);
                            this.outputStream.write(passwordVerifier);
                            this.totalBytesWritten += (long) (saltBytes.length + passwordVerifier.length);
                            this.bytesWrittenForThisFile += (long) (saltBytes.length + passwordVerifier.length);
                        }
                    }
                }
                this.crc.reset();
            } catch (Throwable e) {
                throw new ZipException(e);
            } catch (ZipException e2) {
                throw e2;
            } catch (Throwable e3) {
                throw new ZipException(e3);
            }
        } else {
            throw new ZipException("input file does not exist");
        }
    }

    private void initEncrypter() throws ZipException {
        if (this.zipParameters.isEncryptFiles()) {
            switch (this.zipParameters.getEncryptionMethod()) {
                case 0:
                    this.encrypter = new StandardEncrypter(this.zipParameters.getPassword(), (this.localFileHeader.getLastModFileTime() & 65535) << 16);
                    return;
                case 99:
                    this.encrypter = new AESEncrpyter(this.zipParameters.getPassword(), this.zipParameters.getAesKeyStrength());
                    return;
                default:
                    throw new ZipException("invalid encprytion method");
            }
        }
        this.encrypter = null;
    }

    private void initZipModel(ZipModel zipModel) {
        if (zipModel == null) {
            this.zipModel = new ZipModel();
        } else {
            this.zipModel = zipModel;
        }
        if (this.zipModel.getEndCentralDirRecord() == null) {
            this.zipModel.setEndCentralDirRecord(new EndCentralDirRecord());
        }
        if (this.zipModel.getCentralDirectory() == null) {
            this.zipModel.setCentralDirectory(new CentralDirectory());
        }
        if (this.zipModel.getCentralDirectory().getFileHeaders() == null) {
            this.zipModel.getCentralDirectory().setFileHeaders(new ArrayList());
        }
        if (this.zipModel.getLocalFileHeaderList() == null) {
            this.zipModel.setLocalFileHeaderList(new ArrayList());
        }
        if ((this.outputStream instanceof SplitOutputStream) && ((SplitOutputStream) this.outputStream).isSplitZipFile()) {
            this.zipModel.setSplitArchive(true);
            this.zipModel.setSplitLength(((SplitOutputStream) this.outputStream).getSplitLength());
        }
        this.zipModel.getEndCentralDirRecord().setSignature(InternalZipConstants.ENDSIG);
    }

    public void write(int bval) throws IOException {
        write(new byte[]{(byte) bval}, 0, 1);
    }

    public void write(byte[] b) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (b.length != 0) {
            write(b, 0, b.length);
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (len != 0) {
            if (this.zipParameters.isEncryptFiles() && this.zipParameters.getEncryptionMethod() == 99) {
                if (this.pendingBufferLength != 0) {
                    if (len >= 16 - this.pendingBufferLength) {
                        System.arraycopy(b, off, this.pendingBuffer, this.pendingBufferLength, 16 - this.pendingBufferLength);
                        encryptAndWrite(this.pendingBuffer, 0, this.pendingBuffer.length);
                        off = 16 - this.pendingBufferLength;
                        len -= off;
                        this.pendingBufferLength = 0;
                    } else {
                        System.arraycopy(b, off, this.pendingBuffer, this.pendingBufferLength, len);
                        this.pendingBufferLength += len;
                        return;
                    }
                }
                if (!(len == 0 || len % 16 == 0)) {
                    System.arraycopy(b, (len + off) - (len % 16), this.pendingBuffer, 0, len % 16);
                    this.pendingBufferLength = len % 16;
                    len -= this.pendingBufferLength;
                }
            }
            if (len != 0) {
                encryptAndWrite(b, off, len);
            }
        }
    }

    private void encryptAndWrite(byte[] b, int off, int len) throws IOException {
        if (this.encrypter != null) {
            try {
                this.encrypter.encryptData(b, off, len);
            } catch (ZipException e) {
                throw new IOException(e.getMessage());
            }
        }
        this.outputStream.write(b, off, len);
        this.totalBytesWritten += (long) len;
        this.bytesWrittenForThisFile += (long) len;
    }

    public void closeEntry() throws IOException, ZipException {
        if (this.pendingBufferLength != 0) {
            encryptAndWrite(this.pendingBuffer, 0, this.pendingBufferLength);
            this.pendingBufferLength = 0;
        }
        if (this.zipParameters.isEncryptFiles() && this.zipParameters.getEncryptionMethod() == 99) {
            if (this.encrypter instanceof AESEncrpyter) {
                this.outputStream.write(((AESEncrpyter) this.encrypter).getFinalMac());
                this.bytesWrittenForThisFile += 10;
                this.totalBytesWritten += 10;
            } else {
                throw new ZipException("invalid encrypter for AES encrypted file");
            }
        }
        this.fileHeader.setCompressedSize(this.bytesWrittenForThisFile);
        this.localFileHeader.setCompressedSize(this.bytesWrittenForThisFile);
        if (this.zipParameters.isSourceExternalStream()) {
            this.fileHeader.setUncompressedSize(this.totalBytesRead);
            if (this.localFileHeader.getUncompressedSize() != this.totalBytesRead) {
                this.localFileHeader.setUncompressedSize(this.totalBytesRead);
            }
        }
        long crc32 = this.crc.getValue();
        if (this.fileHeader.isEncrypted() && this.fileHeader.getEncryptionMethod() == 99) {
            crc32 = 0;
        }
        if (this.zipParameters.isEncryptFiles() && this.zipParameters.getEncryptionMethod() == 99) {
            this.fileHeader.setCrc32(0);
            this.localFileHeader.setCrc32(0);
        } else {
            this.fileHeader.setCrc32(crc32);
            this.localFileHeader.setCrc32(crc32);
        }
        this.zipModel.getLocalFileHeaderList().add(this.localFileHeader);
        this.zipModel.getCentralDirectory().getFileHeaders().add(this.fileHeader);
        this.totalBytesWritten += (long) new HeaderWriter().writeExtendedLocalHeader(this.localFileHeader, this.outputStream);
        this.crc.reset();
        this.bytesWrittenForThisFile = 0;
        this.encrypter = null;
        this.totalBytesRead = 0;
    }

    public void finish() throws IOException, ZipException {
        this.zipModel.getEndCentralDirRecord().setOffsetOfStartOfCentralDir(this.totalBytesWritten);
        new HeaderWriter().finalizeZipFile(this.zipModel, this.outputStream);
    }

    public void close() throws IOException {
        if (this.outputStream != null) {
            this.outputStream.close();
        }
    }

    private void createFileHeader() throws ZipException {
        this.fileHeader = new FileHeader();
        this.fileHeader.setSignature(33639248);
        this.fileHeader.setVersionMadeBy(20);
        this.fileHeader.setVersionNeededToExtract(20);
        if (this.zipParameters.isEncryptFiles() && this.zipParameters.getEncryptionMethod() == 99) {
            this.fileHeader.setCompressionMethod(99);
            this.fileHeader.setAesExtraDataRecord(generateAESExtraDataRecord(this.zipParameters));
        } else {
            this.fileHeader.setCompressionMethod(this.zipParameters.getCompressionMethod());
        }
        if (this.zipParameters.isEncryptFiles()) {
            this.fileHeader.setEncrypted(true);
            this.fileHeader.setEncryptionMethod(this.zipParameters.getEncryptionMethod());
        }
        if (this.zipParameters.isSourceExternalStream()) {
            this.fileHeader.setLastModFileTime((int) Zip4jUtil.javaToDosTime(System.currentTimeMillis()));
            if (Zip4jUtil.isStringNotNullAndNotEmpty(this.zipParameters.getFileNameInZip())) {
                String fileName = this.zipParameters.getFileNameInZip();
            } else {
                throw new ZipException("fileNameInZip is null or empty");
            }
        }
        this.fileHeader.setLastModFileTime((int) Zip4jUtil.javaToDosTime(Zip4jUtil.getLastModifiedFileTime(this.sourceFile, this.zipParameters.getTimeZone())));
        this.fileHeader.setUncompressedSize(this.sourceFile.length());
        fileName = Zip4jUtil.getRelativeFileName(this.sourceFile.getAbsolutePath(), this.zipParameters.getRootFolderInZip(), this.zipParameters.getDefaultFolderPath());
        if (Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
            this.fileHeader.setFileName(fileName);
            if (Zip4jUtil.isStringNotNullAndNotEmpty(this.zipModel.getFileNameCharset())) {
                this.fileHeader.setFileNameLength(Zip4jUtil.getEncodedStringLength(fileName, this.zipModel.getFileNameCharset()));
            } else {
                this.fileHeader.setFileNameLength(Zip4jUtil.getEncodedStringLength(fileName));
            }
            if (this.outputStream instanceof SplitOutputStream) {
                this.fileHeader.setDiskNumberStart(((SplitOutputStream) this.outputStream).getCurrSplitFileCounter());
            } else {
                this.fileHeader.setDiskNumberStart(0);
            }
            int fileAttrs = 0;
            if (!this.zipParameters.isSourceExternalStream()) {
                fileAttrs = getFileAttributes(this.sourceFile);
            }
            this.fileHeader.setExternalFileAttr(new byte[]{(byte) fileAttrs, (byte) 0, (byte) 0, (byte) 0});
            if (this.zipParameters.isSourceExternalStream()) {
                FileHeader fileHeader = this.fileHeader;
                boolean z = fileName.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR) || fileName.endsWith("\\");
                fileHeader.setDirectory(z);
            } else {
                this.fileHeader.setDirectory(this.sourceFile.isDirectory());
            }
            if (this.fileHeader.isDirectory()) {
                this.fileHeader.setCompressedSize(0);
                this.fileHeader.setUncompressedSize(0);
            } else if (!this.zipParameters.isSourceExternalStream()) {
                long fileSize = Zip4jUtil.getFileLengh(this.sourceFile);
                if (this.zipParameters.getCompressionMethod() != 0) {
                    this.fileHeader.setCompressedSize(0);
                } else if (this.zipParameters.getEncryptionMethod() == 0) {
                    this.fileHeader.setCompressedSize(12 + fileSize);
                } else if (this.zipParameters.getEncryptionMethod() == 99) {
                    int saltLength;
                    switch (this.zipParameters.getAesKeyStrength()) {
                        case 1:
                            saltLength = 8;
                            break;
                        case 3:
                            saltLength = 16;
                            break;
                        default:
                            throw new ZipException("invalid aes key strength, cannot determine key sizes");
                    }
                    this.fileHeader.setCompressedSize(((((long) saltLength) + fileSize) + 10) + 2);
                } else {
                    this.fileHeader.setCompressedSize(0);
                }
                this.fileHeader.setUncompressedSize(fileSize);
            }
            if (this.zipParameters.isEncryptFiles() && this.zipParameters.getEncryptionMethod() == 0) {
                this.fileHeader.setCrc32((long) this.zipParameters.getSourceFileCRC());
            }
            byte[] shortByte = new byte[2];
            shortByte[0] = Raw.bitArrayToByte(generateGeneralPurposeBitArray(this.fileHeader.isEncrypted(), this.zipParameters.getCompressionMethod()));
            boolean isFileNameCharsetSet = Zip4jUtil.isStringNotNullAndNotEmpty(this.zipModel.getFileNameCharset());
            if (!(isFileNameCharsetSet && this.zipModel.getFileNameCharset().equalsIgnoreCase(InternalZipConstants.CHARSET_UTF8)) && (isFileNameCharsetSet || !Zip4jUtil.detectCharSet(this.fileHeader.getFileName()).equals(InternalZipConstants.CHARSET_UTF8))) {
                shortByte[1] = (byte) 0;
            } else {
                shortByte[1] = (byte) 8;
            }
            this.fileHeader.setGeneralPurposeFlag(shortByte);
            return;
        }
        throw new ZipException("fileName is null or empty. unable to create file header");
    }

    private void createLocalFileHeader() throws ZipException {
        if (this.fileHeader == null) {
            throw new ZipException("file header is null, cannot create local file header");
        }
        this.localFileHeader = new LocalFileHeader();
        this.localFileHeader.setSignature(67324752);
        this.localFileHeader.setVersionNeededToExtract(this.fileHeader.getVersionNeededToExtract());
        this.localFileHeader.setCompressionMethod(this.fileHeader.getCompressionMethod());
        this.localFileHeader.setLastModFileTime(this.fileHeader.getLastModFileTime());
        this.localFileHeader.setUncompressedSize(this.fileHeader.getUncompressedSize());
        this.localFileHeader.setFileNameLength(this.fileHeader.getFileNameLength());
        this.localFileHeader.setFileName(this.fileHeader.getFileName());
        this.localFileHeader.setEncrypted(this.fileHeader.isEncrypted());
        this.localFileHeader.setEncryptionMethod(this.fileHeader.getEncryptionMethod());
        this.localFileHeader.setAesExtraDataRecord(this.fileHeader.getAesExtraDataRecord());
        this.localFileHeader.setCrc32(this.fileHeader.getCrc32());
        this.localFileHeader.setCompressedSize(this.fileHeader.getCompressedSize());
        this.localFileHeader.setGeneralPurposeFlag((byte[]) this.fileHeader.getGeneralPurposeFlag().clone());
    }

    private int getFileAttributes(File file) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null, cannot get file attributes");
        } else if (!file.exists()) {
            return 0;
        } else {
            if (file.isDirectory()) {
                if (file.isHidden()) {
                    return 18;
                }
                return 16;
            } else if (!file.canWrite() && file.isHidden()) {
                return 3;
            } else {
                if (!file.canWrite()) {
                    return 1;
                }
                if (file.isHidden()) {
                    return 2;
                }
                return 0;
            }
        }
    }

    private int[] generateGeneralPurposeBitArray(boolean isEncrpyted, int compressionMethod) {
        int[] generalPurposeBits = new int[8];
        if (isEncrpyted) {
            generalPurposeBits[0] = 1;
        } else {
            generalPurposeBits[0] = 0;
        }
        if (compressionMethod != 8) {
            generalPurposeBits[1] = 0;
            generalPurposeBits[2] = 0;
        }
        generalPurposeBits[3] = 1;
        return generalPurposeBits;
    }

    private AESExtraDataRecord generateAESExtraDataRecord(ZipParameters parameters) throws ZipException {
        if (parameters == null) {
            throw new ZipException("zip parameters are null, cannot generate AES Extra Data record");
        }
        AESExtraDataRecord aesDataRecord = new AESExtraDataRecord();
        aesDataRecord.setSignature(39169);
        aesDataRecord.setDataSize(7);
        aesDataRecord.setVendorID("AE");
        aesDataRecord.setVersionNumber(2);
        if (parameters.getAesKeyStrength() == 1) {
            aesDataRecord.setAesStrength(1);
        } else if (parameters.getAesKeyStrength() == 3) {
            aesDataRecord.setAesStrength(3);
        } else {
            throw new ZipException("invalid AES key strength, cannot generate AES Extra data record");
        }
        aesDataRecord.setCompressionMethod(parameters.getCompressionMethod());
        return aesDataRecord;
    }

    public void decrementCompressedFileSize(int value) {
        if (value > 0 && ((long) value) <= this.bytesWrittenForThisFile) {
            this.bytesWrittenForThisFile -= (long) value;
        }
    }

    protected void updateTotalBytesRead(int toUpdate) {
        if (toUpdate > 0) {
            this.totalBytesRead += (long) toUpdate;
        }
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public File getSourceFile() {
        return this.sourceFile;
    }
}
