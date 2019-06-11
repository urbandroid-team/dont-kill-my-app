package net2.lingala.zip4j.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.io.SplitOutputStream;
import net2.lingala.zip4j.model.AESExtraDataRecord;
import net2.lingala.zip4j.model.FileHeader;
import net2.lingala.zip4j.model.LocalFileHeader;
import net2.lingala.zip4j.model.Zip64EndCentralDirLocator;
import net2.lingala.zip4j.model.Zip64EndCentralDirRecord;
import net2.lingala.zip4j.model.ZipModel;
import net2.lingala.zip4j.util.InternalZipConstants;
import net2.lingala.zip4j.util.Raw;
import net2.lingala.zip4j.util.Zip4jUtil;

public class HeaderWriter {
    private final int ZIP64_EXTRA_BUF = 50;

    public int writeLocalFileHeader(ZipModel zipModel, LocalFileHeader localFileHeader, OutputStream outputStream) throws ZipException {
        if (localFileHeader == null) {
            throw new ZipException("input parameters are null, cannot write local file header");
        }
        try {
            ArrayList byteArrayList = new ArrayList();
            byte[] shortByte = new byte[2];
            byte[] intByte = new byte[4];
            byte[] longByte = new byte[8];
            int i = 8;
            byte[] emptyLongByte = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
            Raw.writeIntLittleEndian(intByte, 0, localFileHeader.getSignature());
            copyByteArrayToArrayList(intByte, byteArrayList);
            int headerLength = 0 + 4;
            Raw.writeShortLittleEndian(shortByte, 0, (short) localFileHeader.getVersionNeededToExtract());
            copyByteArrayToArrayList(shortByte, byteArrayList);
            headerLength += 2;
            copyByteArrayToArrayList(localFileHeader.getGeneralPurposeFlag(), byteArrayList);
            headerLength += 2;
            Raw.writeShortLittleEndian(shortByte, 0, (short) localFileHeader.getCompressionMethod());
            copyByteArrayToArrayList(shortByte, byteArrayList);
            headerLength += 2;
            Raw.writeIntLittleEndian(intByte, 0, localFileHeader.getLastModFileTime());
            copyByteArrayToArrayList(intByte, byteArrayList);
            headerLength += 4;
            Raw.writeIntLittleEndian(intByte, 0, (int) localFileHeader.getCrc32());
            copyByteArrayToArrayList(intByte, byteArrayList);
            headerLength += 4;
            boolean writingZip64Rec = false;
            if (50 + localFileHeader.getUncompressedSize() >= InternalZipConstants.ZIP_64_LIMIT) {
                Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
                System.arraycopy(longByte, 0, intByte, 0, 4);
                copyByteArrayToArrayList(intByte, byteArrayList);
                copyByteArrayToArrayList(intByte, byteArrayList);
                zipModel.setZip64Format(true);
                writingZip64Rec = true;
                localFileHeader.setWriteComprSizeInZip64ExtraRecord(true);
            } else {
                Raw.writeLongLittleEndian(longByte, 0, localFileHeader.getCompressedSize());
                System.arraycopy(longByte, 0, intByte, 0, 4);
                copyByteArrayToArrayList(intByte, byteArrayList);
                Raw.writeLongLittleEndian(longByte, 0, localFileHeader.getUncompressedSize());
                System.arraycopy(longByte, 0, intByte, 0, 4);
                copyByteArrayToArrayList(intByte, byteArrayList);
                localFileHeader.setWriteComprSizeInZip64ExtraRecord(false);
            }
            headerLength += 8;
            Raw.writeShortLittleEndian(shortByte, 0, (short) localFileHeader.getFileNameLength());
            copyByteArrayToArrayList(shortByte, byteArrayList);
            headerLength += 2;
            int extraFieldLength = 0;
            if (writingZip64Rec) {
                extraFieldLength = 0 + 20;
            }
            if (localFileHeader.getAesExtraDataRecord() != null) {
                extraFieldLength += 11;
            }
            Raw.writeShortLittleEndian(shortByte, 0, (short) extraFieldLength);
            copyByteArrayToArrayList(shortByte, byteArrayList);
            headerLength += 2;
            if (Zip4jUtil.isStringNotNullAndNotEmpty(zipModel.getFileNameCharset())) {
                byte[] fileNameBytes = localFileHeader.getFileName().getBytes(zipModel.getFileNameCharset());
                copyByteArrayToArrayList(fileNameBytes, byteArrayList);
                headerLength = fileNameBytes.length + 30;
            } else {
                copyByteArrayToArrayList(Zip4jUtil.convertCharset(localFileHeader.getFileName()), byteArrayList);
                headerLength = Zip4jUtil.getEncodedStringLength(localFileHeader.getFileName()) + 30;
            }
            if (writingZip64Rec) {
                Raw.writeShortLittleEndian(shortByte, 0, (short) 1);
                copyByteArrayToArrayList(shortByte, byteArrayList);
                headerLength += 2;
                Raw.writeShortLittleEndian(shortByte, 0, (short) 16);
                copyByteArrayToArrayList(shortByte, byteArrayList);
                headerLength += 2;
                Raw.writeLongLittleEndian(longByte, 0, localFileHeader.getUncompressedSize());
                copyByteArrayToArrayList(longByte, byteArrayList);
                headerLength += 8;
                copyByteArrayToArrayList(emptyLongByte, byteArrayList);
                headerLength += 8;
            }
            if (localFileHeader.getAesExtraDataRecord() != null) {
                AESExtraDataRecord aesExtraDataRecord = localFileHeader.getAesExtraDataRecord();
                Raw.writeShortLittleEndian(shortByte, 0, (short) ((int) aesExtraDataRecord.getSignature()));
                copyByteArrayToArrayList(shortByte, byteArrayList);
                Raw.writeShortLittleEndian(shortByte, 0, (short) aesExtraDataRecord.getDataSize());
                copyByteArrayToArrayList(shortByte, byteArrayList);
                Raw.writeShortLittleEndian(shortByte, 0, (short) aesExtraDataRecord.getVersionNumber());
                copyByteArrayToArrayList(shortByte, byteArrayList);
                copyByteArrayToArrayList(aesExtraDataRecord.getVendorID().getBytes(), byteArrayList);
                copyByteArrayToArrayList(new byte[]{(byte) aesExtraDataRecord.getAesStrength()}, byteArrayList);
                Raw.writeShortLittleEndian(shortByte, 0, (short) aesExtraDataRecord.getCompressionMethod());
                copyByteArrayToArrayList(shortByte, byteArrayList);
            }
            byte[] lhBytes = byteArrayListToByteArray(byteArrayList);
            outputStream.write(lhBytes);
            return lhBytes.length;
        } catch (ZipException e) {
            throw e;
        } catch (Throwable e2) {
            throw new ZipException(e2);
        }
    }

    public int writeExtendedLocalHeader(LocalFileHeader localFileHeader, OutputStream outputStream) throws ZipException, IOException {
        if (localFileHeader == null || outputStream == null) {
            throw new ZipException("input parameters is null, cannot write extended local header");
        }
        ArrayList byteArrayList = new ArrayList();
        byte[] intByte = new byte[4];
        Raw.writeIntLittleEndian(intByte, 0, 134695760);
        copyByteArrayToArrayList(intByte, byteArrayList);
        Raw.writeIntLittleEndian(intByte, 0, (int) localFileHeader.getCrc32());
        copyByteArrayToArrayList(intByte, byteArrayList);
        long compressedSize = localFileHeader.getCompressedSize();
        if (compressedSize >= 2147483647L) {
            compressedSize = 2147483647L;
        }
        Raw.writeIntLittleEndian(intByte, 0, (int) compressedSize);
        copyByteArrayToArrayList(intByte, byteArrayList);
        long uncompressedSize = localFileHeader.getUncompressedSize();
        if (uncompressedSize >= 2147483647L) {
            uncompressedSize = 2147483647L;
        }
        Raw.writeIntLittleEndian(intByte, 0, (int) uncompressedSize);
        copyByteArrayToArrayList(intByte, byteArrayList);
        byte[] extLocHdrBytes = byteArrayListToByteArray(byteArrayList);
        outputStream.write(extLocHdrBytes);
        return extLocHdrBytes.length;
    }

    public void finalizeZipFile(ZipModel zipModel, OutputStream outputStream) throws ZipException {
        if (zipModel == null || outputStream == null) {
            throw new ZipException("input parameters is null, cannot finalize zip file");
        }
        try {
            processHeaderData(zipModel, outputStream);
            long offsetCentralDir = zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
            List headerBytesList = new ArrayList();
            int sizeOfCentralDir = writeCentralDirectory(zipModel, outputStream, headerBytesList);
            if (zipModel.isZip64Format()) {
                if (zipModel.getZip64EndCentralDirRecord() == null) {
                    zipModel.setZip64EndCentralDirRecord(new Zip64EndCentralDirRecord());
                }
                if (zipModel.getZip64EndCentralDirLocator() == null) {
                    zipModel.setZip64EndCentralDirLocator(new Zip64EndCentralDirLocator());
                }
                zipModel.getZip64EndCentralDirLocator().setOffsetZip64EndOfCentralDirRec(((long) sizeOfCentralDir) + offsetCentralDir);
                if (outputStream instanceof SplitOutputStream) {
                    zipModel.getZip64EndCentralDirLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(((SplitOutputStream) outputStream).getCurrSplitFileCounter());
                    zipModel.getZip64EndCentralDirLocator().setTotNumberOfDiscs(((SplitOutputStream) outputStream).getCurrSplitFileCounter() + 1);
                } else {
                    zipModel.getZip64EndCentralDirLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(0);
                    zipModel.getZip64EndCentralDirLocator().setTotNumberOfDiscs(1);
                }
                writeZip64EndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, headerBytesList);
                writeZip64EndOfCentralDirectoryLocator(zipModel, outputStream, headerBytesList);
            }
            writeEndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, headerBytesList);
            writeZipHeaderBytes(zipModel, outputStream, byteArrayListToByteArray(headerBytesList));
        } catch (ZipException e) {
            throw e;
        } catch (Throwable e2) {
            throw new ZipException(e2);
        }
    }

    public void finalizeZipFileWithoutValidations(ZipModel zipModel, OutputStream outputStream) throws ZipException {
        if (zipModel == null || outputStream == null) {
            throw new ZipException("input parameters is null, cannot finalize zip file without validations");
        }
        try {
            List headerBytesList = new ArrayList();
            long offsetCentralDir = zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
            int sizeOfCentralDir = writeCentralDirectory(zipModel, outputStream, headerBytesList);
            if (zipModel.isZip64Format()) {
                if (zipModel.getZip64EndCentralDirRecord() == null) {
                    zipModel.setZip64EndCentralDirRecord(new Zip64EndCentralDirRecord());
                }
                if (zipModel.getZip64EndCentralDirLocator() == null) {
                    zipModel.setZip64EndCentralDirLocator(new Zip64EndCentralDirLocator());
                }
                zipModel.getZip64EndCentralDirLocator().setOffsetZip64EndOfCentralDirRec(((long) sizeOfCentralDir) + offsetCentralDir);
                writeZip64EndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, headerBytesList);
                writeZip64EndOfCentralDirectoryLocator(zipModel, outputStream, headerBytesList);
            }
            writeEndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, headerBytesList);
            writeZipHeaderBytes(zipModel, outputStream, byteArrayListToByteArray(headerBytesList));
        } catch (ZipException e) {
            throw e;
        } catch (Throwable e2) {
            throw new ZipException(e2);
        }
    }

    private void writeZipHeaderBytes(ZipModel zipModel, OutputStream outputStream, byte[] buff) throws ZipException {
        if (buff == null) {
            throw new ZipException("invalid buff to write as zip headers");
        }
        try {
            if ((outputStream instanceof SplitOutputStream) && ((SplitOutputStream) outputStream).checkBuffSizeAndStartNextSplitFile(buff.length)) {
                finalizeZipFile(zipModel, outputStream);
            } else {
                outputStream.write(buff);
            }
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    private void processHeaderData(ZipModel zipModel, OutputStream outputStream) throws ZipException {
        int currSplitFileCounter = 0;
        try {
            if (outputStream instanceof SplitOutputStream) {
                zipModel.getEndCentralDirRecord().setOffsetOfStartOfCentralDir(((SplitOutputStream) outputStream).getFilePointer());
                currSplitFileCounter = ((SplitOutputStream) outputStream).getCurrSplitFileCounter();
            }
            if (zipModel.isZip64Format()) {
                if (zipModel.getZip64EndCentralDirRecord() == null) {
                    zipModel.setZip64EndCentralDirRecord(new Zip64EndCentralDirRecord());
                }
                if (zipModel.getZip64EndCentralDirLocator() == null) {
                    zipModel.setZip64EndCentralDirLocator(new Zip64EndCentralDirLocator());
                }
                zipModel.getZip64EndCentralDirLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(currSplitFileCounter);
                zipModel.getZip64EndCentralDirLocator().setTotNumberOfDiscs(currSplitFileCounter + 1);
            }
            zipModel.getEndCentralDirRecord().setNoOfThisDisk(currSplitFileCounter);
            zipModel.getEndCentralDirRecord().setNoOfThisDiskStartOfCentralDir(currSplitFileCounter);
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    private int writeCentralDirectory(ZipModel zipModel, OutputStream outputStream, List headerBytesList) throws ZipException {
        if (zipModel == null || outputStream == null) {
            throw new ZipException("input parameters is null, cannot write central directory");
        } else if (zipModel.getCentralDirectory() == null || zipModel.getCentralDirectory().getFileHeaders() == null || zipModel.getCentralDirectory().getFileHeaders().size() <= 0) {
            return 0;
        } else {
            int sizeOfCentralDir = 0;
            for (int i = 0; i < zipModel.getCentralDirectory().getFileHeaders().size(); i++) {
                sizeOfCentralDir += writeFileHeader(zipModel, (FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(i), outputStream, headerBytesList);
            }
            return sizeOfCentralDir;
        }
    }

    private int writeFileHeader(ZipModel zipModel, FileHeader fileHeader, OutputStream outputStream, List headerBytesList) throws ZipException {
        if (fileHeader == null || outputStream == null) {
            throw new ZipException("input parameters is null, cannot write local file header");
        }
        try {
            byte[] shortByte = new byte[2];
            byte[] intByte = new byte[4];
            byte[] longByte = new byte[8];
            int i = 2;
            byte[] emptyShortByte = new byte[]{(byte) 0, (byte) 0};
            i = 4;
            byte[] emptyIntByte = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0};
            boolean writeZip64FileSize = false;
            boolean writeZip64OffsetLocalHeader = false;
            Raw.writeIntLittleEndian(intByte, 0, fileHeader.getSignature());
            copyByteArrayToArrayList(intByte, headerBytesList);
            int sizeOfFileHeader = 0 + 4;
            Raw.writeShortLittleEndian(shortByte, 0, (short) fileHeader.getVersionMadeBy());
            copyByteArrayToArrayList(shortByte, headerBytesList);
            sizeOfFileHeader += 2;
            Raw.writeShortLittleEndian(shortByte, 0, (short) fileHeader.getVersionNeededToExtract());
            copyByteArrayToArrayList(shortByte, headerBytesList);
            sizeOfFileHeader += 2;
            copyByteArrayToArrayList(fileHeader.getGeneralPurposeFlag(), headerBytesList);
            sizeOfFileHeader += 2;
            Raw.writeShortLittleEndian(shortByte, 0, (short) fileHeader.getCompressionMethod());
            copyByteArrayToArrayList(shortByte, headerBytesList);
            sizeOfFileHeader += 2;
            Raw.writeIntLittleEndian(intByte, 0, fileHeader.getLastModFileTime());
            copyByteArrayToArrayList(intByte, headerBytesList);
            sizeOfFileHeader += 4;
            Raw.writeIntLittleEndian(intByte, 0, (int) fileHeader.getCrc32());
            copyByteArrayToArrayList(intByte, headerBytesList);
            sizeOfFileHeader += 4;
            if (fileHeader.getCompressedSize() >= InternalZipConstants.ZIP_64_LIMIT || fileHeader.getUncompressedSize() + 50 >= InternalZipConstants.ZIP_64_LIMIT) {
                Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
                System.arraycopy(longByte, 0, intByte, 0, 4);
                copyByteArrayToArrayList(intByte, headerBytesList);
                sizeOfFileHeader += 4;
                copyByteArrayToArrayList(intByte, headerBytesList);
                sizeOfFileHeader += 4;
                writeZip64FileSize = true;
            } else {
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getCompressedSize());
                System.arraycopy(longByte, 0, intByte, 0, 4);
                copyByteArrayToArrayList(intByte, headerBytesList);
                sizeOfFileHeader += 4;
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getUncompressedSize());
                System.arraycopy(longByte, 0, intByte, 0, 4);
                copyByteArrayToArrayList(intByte, headerBytesList);
                sizeOfFileHeader += 4;
            }
            Raw.writeShortLittleEndian(shortByte, 0, (short) fileHeader.getFileNameLength());
            copyByteArrayToArrayList(shortByte, headerBytesList);
            sizeOfFileHeader += 2;
            byte[] offsetLocalHeaderBytes = new byte[4];
            if (fileHeader.getOffsetLocalHeader() > InternalZipConstants.ZIP_64_LIMIT) {
                Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
                System.arraycopy(longByte, 0, offsetLocalHeaderBytes, 0, 4);
                writeZip64OffsetLocalHeader = true;
            } else {
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getOffsetLocalHeader());
                System.arraycopy(longByte, 0, offsetLocalHeaderBytes, 0, 4);
            }
            int extraFieldLength = 0;
            if (writeZip64FileSize || writeZip64OffsetLocalHeader) {
                extraFieldLength = 0 + 4;
                if (writeZip64FileSize) {
                    extraFieldLength += 16;
                }
                if (writeZip64OffsetLocalHeader) {
                    extraFieldLength += 8;
                }
            }
            if (fileHeader.getAesExtraDataRecord() != null) {
                extraFieldLength += 11;
            }
            Raw.writeShortLittleEndian(shortByte, 0, (short) extraFieldLength);
            copyByteArrayToArrayList(shortByte, headerBytesList);
            sizeOfFileHeader += 2;
            copyByteArrayToArrayList(emptyShortByte, headerBytesList);
            sizeOfFileHeader += 2;
            Raw.writeShortLittleEndian(shortByte, 0, (short) fileHeader.getDiskNumberStart());
            copyByteArrayToArrayList(shortByte, headerBytesList);
            sizeOfFileHeader += 2;
            copyByteArrayToArrayList(emptyShortByte, headerBytesList);
            sizeOfFileHeader += 2;
            if (fileHeader.getExternalFileAttr() != null) {
                copyByteArrayToArrayList(fileHeader.getExternalFileAttr(), headerBytesList);
            } else {
                copyByteArrayToArrayList(emptyIntByte, headerBytesList);
            }
            sizeOfFileHeader += 4;
            copyByteArrayToArrayList(offsetLocalHeaderBytes, headerBytesList);
            sizeOfFileHeader += 4;
            if (Zip4jUtil.isStringNotNullAndNotEmpty(zipModel.getFileNameCharset())) {
                byte[] fileNameBytes = fileHeader.getFileName().getBytes(zipModel.getFileNameCharset());
                copyByteArrayToArrayList(fileNameBytes, headerBytesList);
                sizeOfFileHeader = fileNameBytes.length + 46;
            } else {
                copyByteArrayToArrayList(Zip4jUtil.convertCharset(fileHeader.getFileName()), headerBytesList);
                sizeOfFileHeader = Zip4jUtil.getEncodedStringLength(fileHeader.getFileName()) + 46;
            }
            if (writeZip64FileSize || writeZip64OffsetLocalHeader) {
                zipModel.setZip64Format(true);
                Raw.writeShortLittleEndian(shortByte, 0, (short) 1);
                copyByteArrayToArrayList(shortByte, headerBytesList);
                sizeOfFileHeader += 2;
                int dataSize = 0;
                if (writeZip64FileSize) {
                    dataSize = 0 + 16;
                }
                if (writeZip64OffsetLocalHeader) {
                    dataSize += 8;
                }
                Raw.writeShortLittleEndian(shortByte, 0, (short) dataSize);
                copyByteArrayToArrayList(shortByte, headerBytesList);
                sizeOfFileHeader += 2;
                if (writeZip64FileSize) {
                    Raw.writeLongLittleEndian(longByte, 0, fileHeader.getUncompressedSize());
                    copyByteArrayToArrayList(longByte, headerBytesList);
                    sizeOfFileHeader += 8;
                    Raw.writeLongLittleEndian(longByte, 0, fileHeader.getCompressedSize());
                    copyByteArrayToArrayList(longByte, headerBytesList);
                    sizeOfFileHeader += 8;
                }
                if (writeZip64OffsetLocalHeader) {
                    Raw.writeLongLittleEndian(longByte, 0, fileHeader.getOffsetLocalHeader());
                    copyByteArrayToArrayList(longByte, headerBytesList);
                    sizeOfFileHeader += 8;
                }
            }
            if (fileHeader.getAesExtraDataRecord() == null) {
                return sizeOfFileHeader;
            }
            AESExtraDataRecord aesExtraDataRecord = fileHeader.getAesExtraDataRecord();
            Raw.writeShortLittleEndian(shortByte, 0, (short) ((int) aesExtraDataRecord.getSignature()));
            copyByteArrayToArrayList(shortByte, headerBytesList);
            Raw.writeShortLittleEndian(shortByte, 0, (short) aesExtraDataRecord.getDataSize());
            copyByteArrayToArrayList(shortByte, headerBytesList);
            Raw.writeShortLittleEndian(shortByte, 0, (short) aesExtraDataRecord.getVersionNumber());
            copyByteArrayToArrayList(shortByte, headerBytesList);
            copyByteArrayToArrayList(aesExtraDataRecord.getVendorID().getBytes(), headerBytesList);
            copyByteArrayToArrayList(new byte[]{(byte) aesExtraDataRecord.getAesStrength()}, headerBytesList);
            Raw.writeShortLittleEndian(shortByte, 0, (short) aesExtraDataRecord.getCompressionMethod());
            copyByteArrayToArrayList(shortByte, headerBytesList);
            return sizeOfFileHeader + 11;
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    private void writeZip64EndOfCentralDirectoryRecord(ZipModel zipModel, OutputStream outputStream, int sizeOfCentralDir, long offsetCentralDir, List headerBytesList) throws ZipException {
        if (zipModel == null || outputStream == null) {
            throw new ZipException("zip model or output stream is null, cannot write zip64 end of central directory record");
        }
        try {
            byte[] shortByte = new byte[2];
            byte[] emptyShortByte = new byte[]{(byte) 0, (byte) 0};
            byte[] intByte = new byte[4];
            byte[] longByte = new byte[8];
            Raw.writeIntLittleEndian(intByte, 0, 101075792);
            copyByteArrayToArrayList(intByte, headerBytesList);
            Raw.writeLongLittleEndian(longByte, 0, 44);
            copyByteArrayToArrayList(longByte, headerBytesList);
            if (zipModel.getCentralDirectory() == null || zipModel.getCentralDirectory().getFileHeaders() == null || zipModel.getCentralDirectory().getFileHeaders().size() <= 0) {
                copyByteArrayToArrayList(emptyShortByte, headerBytesList);
                copyByteArrayToArrayList(emptyShortByte, headerBytesList);
            } else {
                Raw.writeShortLittleEndian(shortByte, 0, (short) ((FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(0)).getVersionMadeBy());
                copyByteArrayToArrayList(shortByte, headerBytesList);
                Raw.writeShortLittleEndian(shortByte, 0, (short) ((FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(0)).getVersionNeededToExtract());
                copyByteArrayToArrayList(shortByte, headerBytesList);
            }
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getEndCentralDirRecord().getNoOfThisDisk());
            copyByteArrayToArrayList(intByte, headerBytesList);
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getEndCentralDirRecord().getNoOfThisDiskStartOfCentralDir());
            copyByteArrayToArrayList(intByte, headerBytesList);
            int numEntriesOnThisDisk = 0;
            if (zipModel.getCentralDirectory() == null || zipModel.getCentralDirectory().getFileHeaders() == null) {
                throw new ZipException("invalid central directory/file headers, cannot write end of central directory record");
            }
            int numEntries = zipModel.getCentralDirectory().getFileHeaders().size();
            if (zipModel.isSplitArchive()) {
                countNumberOfFileHeaderEntriesOnDisk(zipModel.getCentralDirectory().getFileHeaders(), zipModel.getEndCentralDirRecord().getNoOfThisDisk());
            } else {
                numEntriesOnThisDisk = numEntries;
            }
            Raw.writeLongLittleEndian(longByte, 0, (long) numEntriesOnThisDisk);
            copyByteArrayToArrayList(longByte, headerBytesList);
            Raw.writeLongLittleEndian(longByte, 0, (long) numEntries);
            copyByteArrayToArrayList(longByte, headerBytesList);
            Raw.writeLongLittleEndian(longByte, 0, (long) sizeOfCentralDir);
            copyByteArrayToArrayList(longByte, headerBytesList);
            Raw.writeLongLittleEndian(longByte, 0, offsetCentralDir);
            copyByteArrayToArrayList(longByte, headerBytesList);
        } catch (ZipException zipException) {
            throw zipException;
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    private void writeZip64EndOfCentralDirectoryLocator(ZipModel zipModel, OutputStream outputStream, List headerBytesList) throws ZipException {
        if (zipModel == null || outputStream == null) {
            throw new ZipException("zip model or output stream is null, cannot write zip64 end of central directory locator");
        }
        try {
            byte[] intByte = new byte[4];
            byte[] longByte = new byte[8];
            Raw.writeIntLittleEndian(intByte, 0, 117853008);
            copyByteArrayToArrayList(intByte, headerBytesList);
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getZip64EndCentralDirLocator().getNoOfDiskStartOfZip64EndOfCentralDirRec());
            copyByteArrayToArrayList(intByte, headerBytesList);
            Raw.writeLongLittleEndian(longByte, 0, zipModel.getZip64EndCentralDirLocator().getOffsetZip64EndOfCentralDirRec());
            copyByteArrayToArrayList(longByte, headerBytesList);
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getZip64EndCentralDirLocator().getTotNumberOfDiscs());
            copyByteArrayToArrayList(intByte, headerBytesList);
        } catch (ZipException zipException) {
            throw zipException;
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    private void writeEndOfCentralDirectoryRecord(ZipModel zipModel, OutputStream outputStream, int sizeOfCentralDir, long offsetCentralDir, List headrBytesList) throws ZipException {
        if (zipModel == null || outputStream == null) {
            throw new ZipException("zip model or output stream is null, cannot write end of central directory record");
        }
        try {
            byte[] shortByte = new byte[2];
            byte[] intByte = new byte[4];
            byte[] longByte = new byte[8];
            Raw.writeIntLittleEndian(intByte, 0, (int) zipModel.getEndCentralDirRecord().getSignature());
            copyByteArrayToArrayList(intByte, headrBytesList);
            Raw.writeShortLittleEndian(shortByte, 0, (short) zipModel.getEndCentralDirRecord().getNoOfThisDisk());
            copyByteArrayToArrayList(shortByte, headrBytesList);
            Raw.writeShortLittleEndian(shortByte, 0, (short) zipModel.getEndCentralDirRecord().getNoOfThisDiskStartOfCentralDir());
            copyByteArrayToArrayList(shortByte, headrBytesList);
            if (zipModel.getCentralDirectory() == null || zipModel.getCentralDirectory().getFileHeaders() == null) {
                throw new ZipException("invalid central directory/file headers, cannot write end of central directory record");
            }
            int numEntriesOnThisDisk;
            int numEntries = zipModel.getCentralDirectory().getFileHeaders().size();
            if (zipModel.isSplitArchive()) {
                numEntriesOnThisDisk = countNumberOfFileHeaderEntriesOnDisk(zipModel.getCentralDirectory().getFileHeaders(), zipModel.getEndCentralDirRecord().getNoOfThisDisk());
            } else {
                numEntriesOnThisDisk = numEntries;
            }
            Raw.writeShortLittleEndian(shortByte, 0, (short) numEntriesOnThisDisk);
            copyByteArrayToArrayList(shortByte, headrBytesList);
            Raw.writeShortLittleEndian(shortByte, 0, (short) numEntries);
            copyByteArrayToArrayList(shortByte, headrBytesList);
            Raw.writeIntLittleEndian(intByte, 0, sizeOfCentralDir);
            copyByteArrayToArrayList(intByte, headrBytesList);
            if (offsetCentralDir > InternalZipConstants.ZIP_64_LIMIT) {
                Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
                System.arraycopy(longByte, 0, intByte, 0, 4);
                copyByteArrayToArrayList(intByte, headrBytesList);
            } else {
                Raw.writeLongLittleEndian(longByte, 0, offsetCentralDir);
                System.arraycopy(longByte, 0, intByte, 0, 4);
                copyByteArrayToArrayList(intByte, headrBytesList);
            }
            int commentLength = 0;
            if (zipModel.getEndCentralDirRecord().getComment() != null) {
                commentLength = zipModel.getEndCentralDirRecord().getCommentLength();
            }
            Raw.writeShortLittleEndian(shortByte, 0, (short) commentLength);
            copyByteArrayToArrayList(shortByte, headrBytesList);
            if (commentLength > 0) {
                copyByteArrayToArrayList(zipModel.getEndCentralDirRecord().getCommentBytes(), headrBytesList);
            }
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    public void updateLocalFileHeader(LocalFileHeader localFileHeader, long offset, int toUpdate, ZipModel zipModel, byte[] bytesToWrite, int noOfDisk, SplitOutputStream outputStream) throws ZipException {
        if (localFileHeader == null || offset < 0 || zipModel == null) {
            throw new ZipException("invalid input parameters, cannot update local file header");
        }
        boolean closeFlag = false;
        try {
            SplitOutputStream currOutputStream;
            if (noOfDisk != outputStream.getCurrSplitFileCounter()) {
                File file = new File(zipModel.getZipFile());
                String parentFile = file.getParent();
                String fileNameWithoutExt = Zip4jUtil.getZipFileNameWithoutExt(file.getName());
                String fileName = parentFile + System.getProperty("file.separator");
                if (noOfDisk < 9) {
                    fileName = fileName + fileNameWithoutExt + ".z0" + (noOfDisk + 1);
                } else {
                    fileName = fileName + fileNameWithoutExt + ".z" + (noOfDisk + 1);
                }
                closeFlag = true;
                currOutputStream = new SplitOutputStream(new File(fileName));
            } else {
                currOutputStream = outputStream;
            }
            long currOffset = currOutputStream.getFilePointer();
            if (currOutputStream == null) {
                throw new ZipException("invalid output stream handler, cannot update local file header");
            }
            switch (toUpdate) {
                case 14:
                    currOutputStream.seek(((long) toUpdate) + offset);
                    currOutputStream.write(bytesToWrite);
                    break;
                case 18:
                case 22:
                    updateCompressedSizeInLocalFileHeader(currOutputStream, localFileHeader, offset, (long) toUpdate, bytesToWrite, zipModel.isZip64Format());
                    break;
            }
            if (closeFlag) {
                currOutputStream.close();
            } else {
                outputStream.seek(currOffset);
            }
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    private void updateCompressedSizeInLocalFileHeader(SplitOutputStream outputStream, LocalFileHeader localFileHeader, long offset, long toUpdate, byte[] bytesToWrite, boolean isZip64Format) throws ZipException {
        if (outputStream == null) {
            throw new ZipException("invalid output stream, cannot update compressed size for local file header");
        }
        try {
            if (!localFileHeader.isWriteComprSizeInZip64ExtraRecord()) {
                outputStream.seek(offset + toUpdate);
                outputStream.write(bytesToWrite);
            } else if (bytesToWrite.length != 8) {
                throw new ZipException("attempting to write a non 8-byte compressed size block for a zip64 file");
            } else {
                long zip64CompressedSizeOffset = ((((((((offset + toUpdate) + 4) + 4) + 2) + 2) + ((long) localFileHeader.getFileNameLength())) + 2) + 2) + 8;
                if (toUpdate == 22) {
                    zip64CompressedSizeOffset += 8;
                }
                outputStream.seek(zip64CompressedSizeOffset);
                outputStream.write(bytesToWrite);
            }
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    private void copyByteArrayToArrayList(byte[] byteArray, List arrayList) throws ZipException {
        if (arrayList == null || byteArray == null) {
            throw new ZipException("one of the input parameters is null, cannot copy byte array to array list");
        }
        for (byte b : byteArray) {
            arrayList.add(Byte.toString(b));
        }
    }

    private byte[] byteArrayListToByteArray(List arrayList) throws ZipException {
        if (arrayList == null) {
            throw new ZipException("input byte array list is null, cannot conver to byte array");
        } else if (arrayList.size() <= 0) {
            return null;
        } else {
            byte[] retBytes = new byte[arrayList.size()];
            for (int i = 0; i < arrayList.size(); i++) {
                retBytes[i] = Byte.parseByte((String) arrayList.get(i));
            }
            return retBytes;
        }
    }

    private int countNumberOfFileHeaderEntriesOnDisk(ArrayList fileHeaders, int numOfDisk) throws ZipException {
        if (fileHeaders == null) {
            throw new ZipException("file headers are null, cannot calculate number of entries on this disk");
        }
        int noEntries = 0;
        for (int i = 0; i < fileHeaders.size(); i++) {
            if (((FileHeader) fileHeaders.get(i)).getDiskNumberStart() == numOfDisk) {
                noEntries++;
            }
        }
        return noEntries;
    }
}
