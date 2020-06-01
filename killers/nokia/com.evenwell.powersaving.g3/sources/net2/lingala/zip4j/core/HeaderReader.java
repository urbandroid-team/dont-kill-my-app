package net2.lingala.zip4j.core;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.model.AESExtraDataRecord;
import net2.lingala.zip4j.model.CentralDirectory;
import net2.lingala.zip4j.model.DigitalSignature;
import net2.lingala.zip4j.model.EndCentralDirRecord;
import net2.lingala.zip4j.model.ExtraDataRecord;
import net2.lingala.zip4j.model.FileHeader;
import net2.lingala.zip4j.model.LocalFileHeader;
import net2.lingala.zip4j.model.Zip64EndCentralDirLocator;
import net2.lingala.zip4j.model.Zip64EndCentralDirRecord;
import net2.lingala.zip4j.model.Zip64ExtendedInfo;
import net2.lingala.zip4j.model.ZipModel;
import net2.lingala.zip4j.util.InternalZipConstants;
import net2.lingala.zip4j.util.Raw;
import net2.lingala.zip4j.util.Zip4jUtil;

public class HeaderReader {
    private RandomAccessFile zip4jRaf = null;
    private ZipModel zipModel;

    public HeaderReader(RandomAccessFile zip4jRaf) {
        this.zip4jRaf = zip4jRaf;
    }

    public ZipModel readAllHeaders() throws ZipException {
        return readAllHeaders(null);
    }

    public ZipModel readAllHeaders(String fileNameCharset) throws ZipException {
        this.zipModel = new ZipModel();
        this.zipModel.setFileNameCharset(fileNameCharset);
        this.zipModel.setEndCentralDirRecord(readEndOfCentralDirectoryRecord());
        this.zipModel.setZip64EndCentralDirLocator(readZip64EndCentralDirLocator());
        if (this.zipModel.isZip64Format()) {
            this.zipModel.setZip64EndCentralDirRecord(readZip64EndCentralDirRec());
            if (this.zipModel.getZip64EndCentralDirRecord() == null || this.zipModel.getZip64EndCentralDirRecord().getNoOfThisDisk() <= 0) {
                this.zipModel.setSplitArchive(false);
            } else {
                this.zipModel.setSplitArchive(true);
            }
        }
        this.zipModel.setCentralDirectory(readCentralDirectory());
        return this.zipModel;
    }

    private EndCentralDirRecord readEndOfCentralDirectoryRecord() throws ZipException {
        if (this.zip4jRaf == null) {
            throw new ZipException("random access file was null", 3);
        }
        try {
            byte[] ebs = new byte[4];
            long pos = this.zip4jRaf.length() - 22;
            EndCentralDirRecord endCentralDirRecord = new EndCentralDirRecord();
            int counter = 0;
            long pos2 = pos;
            while (true) {
                pos = pos2 - 1;
                this.zip4jRaf.seek(pos2);
                counter++;
                if (((long) Raw.readLeInt(this.zip4jRaf, ebs)) != InternalZipConstants.ENDSIG && counter <= 3000) {
                    pos2 = pos;
                }
            }
            if (((long) Raw.readIntLittleEndian(ebs, 0)) != InternalZipConstants.ENDSIG) {
                throw new ZipException("zip headers not found. probably not a zip file");
            }
            byte[] intBuff = new byte[4];
            byte[] shortBuff = new byte[2];
            endCentralDirRecord.setSignature(InternalZipConstants.ENDSIG);
            readIntoBuff(this.zip4jRaf, shortBuff);
            endCentralDirRecord.setNoOfThisDisk(Raw.readShortLittleEndian(shortBuff, 0));
            readIntoBuff(this.zip4jRaf, shortBuff);
            endCentralDirRecord.setNoOfThisDiskStartOfCentralDir(Raw.readShortLittleEndian(shortBuff, 0));
            readIntoBuff(this.zip4jRaf, shortBuff);
            endCentralDirRecord.setTotNoOfEntriesInCentralDirOnThisDisk(Raw.readShortLittleEndian(shortBuff, 0));
            readIntoBuff(this.zip4jRaf, shortBuff);
            endCentralDirRecord.setTotNoOfEntriesInCentralDir(Raw.readShortLittleEndian(shortBuff, 0));
            readIntoBuff(this.zip4jRaf, intBuff);
            endCentralDirRecord.setSizeOfCentralDir(Raw.readIntLittleEndian(intBuff, 0));
            readIntoBuff(this.zip4jRaf, intBuff);
            endCentralDirRecord.setOffsetOfStartOfCentralDir(Raw.readLongLittleEndian(getLongByteFromIntByte(intBuff), 0));
            readIntoBuff(this.zip4jRaf, shortBuff);
            int commentLength = Raw.readShortLittleEndian(shortBuff, 0);
            endCentralDirRecord.setCommentLength(commentLength);
            if (commentLength > 0) {
                byte[] commentBuf = new byte[commentLength];
                readIntoBuff(this.zip4jRaf, commentBuf);
                endCentralDirRecord.setComment(new String(commentBuf));
                endCentralDirRecord.setCommentBytes(commentBuf);
            } else {
                endCentralDirRecord.setComment(null);
            }
            if (endCentralDirRecord.getNoOfThisDisk() > 0) {
                this.zipModel.setSplitArchive(true);
            } else {
                this.zipModel.setSplitArchive(false);
            }
            return endCentralDirRecord;
        } catch (IOException e) {
            throw new ZipException("Probably not a zip file or a corrupted zip file", e, 4);
        }
    }

    private CentralDirectory readCentralDirectory() throws ZipException {
        if (this.zip4jRaf == null) {
            throw new ZipException("random access file was null", 3);
        } else if (this.zipModel.getEndCentralDirRecord() == null) {
            throw new ZipException("EndCentralRecord was null, maybe a corrupt zip file");
        } else {
            try {
                int signature;
                CentralDirectory centralDirectory = new CentralDirectory();
                ArrayList fileHeaderList = new ArrayList();
                EndCentralDirRecord endCentralDirRecord = this.zipModel.getEndCentralDirRecord();
                long offSetStartCentralDir = endCentralDirRecord.getOffsetOfStartOfCentralDir();
                int centralDirEntryCount = endCentralDirRecord.getTotNoOfEntriesInCentralDir();
                if (this.zipModel.isZip64Format()) {
                    offSetStartCentralDir = this.zipModel.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
                    centralDirEntryCount = (int) this.zipModel.getZip64EndCentralDirRecord().getTotNoOfEntriesInCentralDir();
                }
                this.zip4jRaf.seek(offSetStartCentralDir);
                byte[] intBuff = new byte[4];
                byte[] shortBuff = new byte[2];
                byte[] longBuff = new byte[8];
                for (int i = 0; i < centralDirEntryCount; i++) {
                    FileHeader fileHeader = new FileHeader();
                    readIntoBuff(this.zip4jRaf, intBuff);
                    signature = Raw.readIntLittleEndian(intBuff, 0);
                    if (((long) signature) != InternalZipConstants.CENSIG) {
                        throw new ZipException("Expected central directory entry not found (#" + (i + 1) + ")");
                    }
                    fileHeader.setSignature(signature);
                    readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setVersionMadeBy(Raw.readShortLittleEndian(shortBuff, 0));
                    readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setVersionNeededToExtract(Raw.readShortLittleEndian(shortBuff, 0));
                    readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setFileNameUTF8Encoded((Raw.readShortLittleEndian(shortBuff, 0) & 2048) != 0);
                    int firstByte = shortBuff[0];
                    if ((firstByte & 1) != 0) {
                        fileHeader.setEncrypted(true);
                    }
                    fileHeader.setGeneralPurposeFlag((byte[]) shortBuff.clone());
                    fileHeader.setDataDescriptorExists((firstByte >> 3) == 1);
                    readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setCompressionMethod(Raw.readShortLittleEndian(shortBuff, 0));
                    readIntoBuff(this.zip4jRaf, intBuff);
                    fileHeader.setLastModFileTime(Raw.readIntLittleEndian(intBuff, 0));
                    readIntoBuff(this.zip4jRaf, intBuff);
                    fileHeader.setCrc32((long) Raw.readIntLittleEndian(intBuff, 0));
                    fileHeader.setCrcBuff((byte[]) intBuff.clone());
                    readIntoBuff(this.zip4jRaf, intBuff);
                    fileHeader.setCompressedSize(Raw.readLongLittleEndian(getLongByteFromIntByte(intBuff), 0));
                    readIntoBuff(this.zip4jRaf, intBuff);
                    fileHeader.setUncompressedSize(Raw.readLongLittleEndian(getLongByteFromIntByte(intBuff), 0));
                    readIntoBuff(this.zip4jRaf, shortBuff);
                    int fileNameLength = Raw.readShortLittleEndian(shortBuff, 0);
                    fileHeader.setFileNameLength(fileNameLength);
                    readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setExtraFieldLength(Raw.readShortLittleEndian(shortBuff, 0));
                    readIntoBuff(this.zip4jRaf, shortBuff);
                    int fileCommentLength = Raw.readShortLittleEndian(shortBuff, 0);
                    fileHeader.setFileComment(new String(shortBuff));
                    readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setDiskNumberStart(Raw.readShortLittleEndian(shortBuff, 0));
                    readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setInternalFileAttr((byte[]) shortBuff.clone());
                    readIntoBuff(this.zip4jRaf, intBuff);
                    fileHeader.setExternalFileAttr((byte[]) intBuff.clone());
                    readIntoBuff(this.zip4jRaf, intBuff);
                    fileHeader.setOffsetLocalHeader(Raw.readLongLittleEndian(getLongByteFromIntByte(intBuff), 0) & InternalZipConstants.ZIP_64_LIMIT);
                    if (fileNameLength > 0) {
                        String fileName;
                        byte[] fileNameBuf = new byte[fileNameLength];
                        readIntoBuff(this.zip4jRaf, fileNameBuf);
                        if (Zip4jUtil.isStringNotNullAndNotEmpty(this.zipModel.getFileNameCharset())) {
                            fileName = new String(fileNameBuf, this.zipModel.getFileNameCharset());
                        } else {
                            fileName = Zip4jUtil.decodeFileName(fileNameBuf, fileHeader.isFileNameUTF8Encoded());
                        }
                        if (fileName == null) {
                            throw new ZipException("fileName is null when reading central directory");
                        }
                        if (fileName.indexOf(":" + System.getProperty("file.separator")) >= 0) {
                            fileName = fileName.substring(fileName.indexOf(":" + System.getProperty("file.separator")) + 2);
                        }
                        fileHeader.setFileName(fileName);
                        boolean z = fileName.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR) || fileName.endsWith("\\");
                        fileHeader.setDirectory(z);
                    } else {
                        fileHeader.setFileName(null);
                    }
                    readAndSaveExtraDataRecord(fileHeader);
                    readAndSaveZip64ExtendedInfo(fileHeader);
                    readAndSaveAESExtraDataRecord(fileHeader);
                    if (fileCommentLength > 0) {
                        byte[] fileCommentBuf = new byte[fileCommentLength];
                        readIntoBuff(this.zip4jRaf, fileCommentBuf);
                        fileHeader.setFileComment(new String(fileCommentBuf));
                    }
                    fileHeaderList.add(fileHeader);
                }
                centralDirectory.setFileHeaders(fileHeaderList);
                DigitalSignature digitalSignature = new DigitalSignature();
                readIntoBuff(this.zip4jRaf, intBuff);
                signature = Raw.readIntLittleEndian(intBuff, 0);
                if (((long) signature) == InternalZipConstants.DIGSIG) {
                    digitalSignature.setHeaderSignature(signature);
                    readIntoBuff(this.zip4jRaf, shortBuff);
                    int sizeOfData = Raw.readShortLittleEndian(shortBuff, 0);
                    digitalSignature.setSizeOfData(sizeOfData);
                    if (sizeOfData > 0) {
                        byte[] sigDataBuf = new byte[sizeOfData];
                        readIntoBuff(this.zip4jRaf, sigDataBuf);
                        digitalSignature.setSignatureData(new String(sigDataBuf));
                    }
                }
                return centralDirectory;
            } catch (Throwable e) {
                throw new ZipException(e);
            }
        }
    }

    private void readAndSaveExtraDataRecord(FileHeader fileHeader) throws ZipException {
        if (this.zip4jRaf == null) {
            throw new ZipException("invalid file handler when trying to read extra data record");
        } else if (fileHeader == null) {
            throw new ZipException("file header is null");
        } else {
            int extraFieldLength = fileHeader.getExtraFieldLength();
            if (extraFieldLength > 0) {
                fileHeader.setExtraDataRecords(readExtraDataRecords(extraFieldLength));
            }
        }
    }

    private void readAndSaveExtraDataRecord(LocalFileHeader localFileHeader) throws ZipException {
        if (this.zip4jRaf == null) {
            throw new ZipException("invalid file handler when trying to read extra data record");
        } else if (localFileHeader == null) {
            throw new ZipException("file header is null");
        } else {
            int extraFieldLength = localFileHeader.getExtraFieldLength();
            if (extraFieldLength > 0) {
                localFileHeader.setExtraDataRecords(readExtraDataRecords(extraFieldLength));
            }
        }
    }

    private ArrayList readExtraDataRecords(int extraFieldLength) throws ZipException {
        if (extraFieldLength <= 0) {
            return null;
        }
        try {
            byte[] extraFieldBuf = new byte[extraFieldLength];
            this.zip4jRaf.read(extraFieldBuf);
            int counter = 0;
            ArrayList extraDataList = new ArrayList();
            while (counter < extraFieldLength) {
                ExtraDataRecord extraDataRecord = new ExtraDataRecord();
                extraDataRecord.setHeader((long) Raw.readShortLittleEndian(extraFieldBuf, counter));
                counter += 2;
                int sizeOfRec = Raw.readShortLittleEndian(extraFieldBuf, counter);
                if (sizeOfRec + 2 > extraFieldLength) {
                    sizeOfRec = Raw.readShortBigEndian(extraFieldBuf, counter);
                    if (sizeOfRec + 2 > extraFieldLength) {
                        break;
                    }
                }
                extraDataRecord.setSizeOfData(sizeOfRec);
                counter += 2;
                if (sizeOfRec > 0) {
                    byte[] data = new byte[sizeOfRec];
                    System.arraycopy(extraFieldBuf, counter, data, 0, sizeOfRec);
                    extraDataRecord.setData(data);
                }
                counter += sizeOfRec;
                extraDataList.add(extraDataRecord);
            }
            if (extraDataList.size() <= 0) {
                return null;
            }
            return extraDataList;
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    private Zip64EndCentralDirLocator readZip64EndCentralDirLocator() throws ZipException {
        if (this.zip4jRaf == null) {
            throw new ZipException("invalid file handler when trying to read Zip64EndCentralDirLocator");
        }
        try {
            Zip64EndCentralDirLocator zip64EndCentralDirLocator = new Zip64EndCentralDirLocator();
            setFilePointerToReadZip64EndCentralDirLoc();
            byte[] intBuff = new byte[4];
            byte[] longBuff = new byte[8];
            readIntoBuff(this.zip4jRaf, intBuff);
            int signature = Raw.readIntLittleEndian(intBuff, 0);
            if (((long) signature) == InternalZipConstants.ZIP64ENDCENDIRLOC) {
                this.zipModel.setZip64Format(true);
                zip64EndCentralDirLocator.setSignature((long) signature);
                readIntoBuff(this.zip4jRaf, intBuff);
                zip64EndCentralDirLocator.setNoOfDiskStartOfZip64EndOfCentralDirRec(Raw.readIntLittleEndian(intBuff, 0));
                readIntoBuff(this.zip4jRaf, longBuff);
                zip64EndCentralDirLocator.setOffsetZip64EndOfCentralDirRec(Raw.readLongLittleEndian(longBuff, 0));
                readIntoBuff(this.zip4jRaf, intBuff);
                zip64EndCentralDirLocator.setTotNumberOfDiscs(Raw.readIntLittleEndian(intBuff, 0));
                return zip64EndCentralDirLocator;
            }
            this.zipModel.setZip64Format(false);
            return null;
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    private Zip64EndCentralDirRecord readZip64EndCentralDirRec() throws ZipException {
        if (this.zipModel.getZip64EndCentralDirLocator() == null) {
            throw new ZipException("invalid zip64 end of central directory locator");
        }
        long offSetStartOfZip64CentralDir = this.zipModel.getZip64EndCentralDirLocator().getOffsetZip64EndOfCentralDirRec();
        if (offSetStartOfZip64CentralDir < 0) {
            throw new ZipException("invalid offset for start of end of central directory record");
        }
        try {
            this.zip4jRaf.seek(offSetStartOfZip64CentralDir);
            Zip64EndCentralDirRecord zip64EndCentralDirRecord = new Zip64EndCentralDirRecord();
            byte[] shortBuff = new byte[2];
            byte[] intBuff = new byte[4];
            byte[] longBuff = new byte[8];
            readIntoBuff(this.zip4jRaf, intBuff);
            int signature = Raw.readIntLittleEndian(intBuff, 0);
            if (((long) signature) != InternalZipConstants.ZIP64ENDCENDIRREC) {
                throw new ZipException("invalid signature for zip64 end of central directory record");
            }
            zip64EndCentralDirRecord.setSignature((long) signature);
            readIntoBuff(this.zip4jRaf, longBuff);
            zip64EndCentralDirRecord.setSizeOfZip64EndCentralDirRec(Raw.readLongLittleEndian(longBuff, 0));
            readIntoBuff(this.zip4jRaf, shortBuff);
            zip64EndCentralDirRecord.setVersionMadeBy(Raw.readShortLittleEndian(shortBuff, 0));
            readIntoBuff(this.zip4jRaf, shortBuff);
            zip64EndCentralDirRecord.setVersionNeededToExtract(Raw.readShortLittleEndian(shortBuff, 0));
            readIntoBuff(this.zip4jRaf, intBuff);
            zip64EndCentralDirRecord.setNoOfThisDisk(Raw.readIntLittleEndian(intBuff, 0));
            readIntoBuff(this.zip4jRaf, intBuff);
            zip64EndCentralDirRecord.setNoOfThisDiskStartOfCentralDir(Raw.readIntLittleEndian(intBuff, 0));
            readIntoBuff(this.zip4jRaf, longBuff);
            zip64EndCentralDirRecord.setTotNoOfEntriesInCentralDirOnThisDisk(Raw.readLongLittleEndian(longBuff, 0));
            readIntoBuff(this.zip4jRaf, longBuff);
            zip64EndCentralDirRecord.setTotNoOfEntriesInCentralDir(Raw.readLongLittleEndian(longBuff, 0));
            readIntoBuff(this.zip4jRaf, longBuff);
            zip64EndCentralDirRecord.setSizeOfCentralDir(Raw.readLongLittleEndian(longBuff, 0));
            readIntoBuff(this.zip4jRaf, longBuff);
            zip64EndCentralDirRecord.setOffsetStartCenDirWRTStartDiskNo(Raw.readLongLittleEndian(longBuff, 0));
            long extDataSecSize = zip64EndCentralDirRecord.getSizeOfZip64EndCentralDirRec() - 44;
            if (extDataSecSize > 0) {
                byte[] extDataSecRecBuf = new byte[((int) extDataSecSize)];
                readIntoBuff(this.zip4jRaf, extDataSecRecBuf);
                zip64EndCentralDirRecord.setExtensibleDataSector(extDataSecRecBuf);
            }
            return zip64EndCentralDirRecord;
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    private void readAndSaveZip64ExtendedInfo(FileHeader fileHeader) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("file header is null in reading Zip64 Extended Info");
        } else if (fileHeader.getExtraDataRecords() != null && fileHeader.getExtraDataRecords().size() > 0) {
            Zip64ExtendedInfo zip64ExtendedInfo = readZip64ExtendedInfo(fileHeader.getExtraDataRecords(), fileHeader.getUncompressedSize(), fileHeader.getCompressedSize(), fileHeader.getOffsetLocalHeader(), fileHeader.getDiskNumberStart());
            if (zip64ExtendedInfo != null) {
                fileHeader.setZip64ExtendedInfo(zip64ExtendedInfo);
                if (zip64ExtendedInfo.getUnCompressedSize() != -1) {
                    fileHeader.setUncompressedSize(zip64ExtendedInfo.getUnCompressedSize());
                }
                if (zip64ExtendedInfo.getCompressedSize() != -1) {
                    fileHeader.setCompressedSize(zip64ExtendedInfo.getCompressedSize());
                }
                if (zip64ExtendedInfo.getOffsetLocalHeader() != -1) {
                    fileHeader.setOffsetLocalHeader(zip64ExtendedInfo.getOffsetLocalHeader());
                }
                if (zip64ExtendedInfo.getDiskNumberStart() != -1) {
                    fileHeader.setDiskNumberStart(zip64ExtendedInfo.getDiskNumberStart());
                }
            }
        }
    }

    private void readAndSaveZip64ExtendedInfo(LocalFileHeader localFileHeader) throws ZipException {
        if (localFileHeader == null) {
            throw new ZipException("file header is null in reading Zip64 Extended Info");
        } else if (localFileHeader.getExtraDataRecords() != null && localFileHeader.getExtraDataRecords().size() > 0) {
            Zip64ExtendedInfo zip64ExtendedInfo = readZip64ExtendedInfo(localFileHeader.getExtraDataRecords(), localFileHeader.getUncompressedSize(), localFileHeader.getCompressedSize(), -1, -1);
            if (zip64ExtendedInfo != null) {
                localFileHeader.setZip64ExtendedInfo(zip64ExtendedInfo);
                if (zip64ExtendedInfo.getUnCompressedSize() != -1) {
                    localFileHeader.setUncompressedSize(zip64ExtendedInfo.getUnCompressedSize());
                }
                if (zip64ExtendedInfo.getCompressedSize() != -1) {
                    localFileHeader.setCompressedSize(zip64ExtendedInfo.getCompressedSize());
                }
            }
        }
    }

    private Zip64ExtendedInfo readZip64ExtendedInfo(ArrayList extraDataRecords, long unCompressedSize, long compressedSize, long offsetLocalHeader, int diskNumberStart) throws ZipException {
        for (int i = 0; i < extraDataRecords.size(); i++) {
            ExtraDataRecord extraDataRecord = (ExtraDataRecord) extraDataRecords.get(i);
            if (extraDataRecord != null && extraDataRecord.getHeader() == 1) {
                Zip64ExtendedInfo zip64ExtendedInfo = new Zip64ExtendedInfo();
                byte[] byteBuff = extraDataRecord.getData();
                if (extraDataRecord.getSizeOfData() > 0) {
                    byte[] longByteBuff = new byte[8];
                    byte[] intByteBuff = new byte[4];
                    int counter = 0;
                    boolean valueAdded = false;
                    if ((65535 & unCompressedSize) == 65535 && 0 < extraDataRecord.getSizeOfData()) {
                        System.arraycopy(byteBuff, 0, longByteBuff, 0, 8);
                        zip64ExtendedInfo.setUnCompressedSize(Raw.readLongLittleEndian(longByteBuff, 0));
                        counter = 0 + 8;
                        valueAdded = true;
                    }
                    if ((65535 & compressedSize) == 65535 && counter < extraDataRecord.getSizeOfData()) {
                        System.arraycopy(byteBuff, counter, longByteBuff, 0, 8);
                        zip64ExtendedInfo.setCompressedSize(Raw.readLongLittleEndian(longByteBuff, 0));
                        counter += 8;
                        valueAdded = true;
                    }
                    if ((65535 & offsetLocalHeader) == 65535 && counter < extraDataRecord.getSizeOfData()) {
                        System.arraycopy(byteBuff, counter, longByteBuff, 0, 8);
                        zip64ExtendedInfo.setOffsetLocalHeader(Raw.readLongLittleEndian(longByteBuff, 0));
                        counter += 8;
                        valueAdded = true;
                    }
                    if ((65535 & diskNumberStart) == 65535 && counter < extraDataRecord.getSizeOfData()) {
                        System.arraycopy(byteBuff, counter, intByteBuff, 0, 4);
                        zip64ExtendedInfo.setDiskNumberStart(Raw.readIntLittleEndian(intByteBuff, 0));
                        counter += 8;
                        valueAdded = true;
                    }
                    if (valueAdded) {
                        return zip64ExtendedInfo;
                    }
                }
                return null;
            }
        }
        return null;
    }

    private void setFilePointerToReadZip64EndCentralDirLoc() throws ZipException {
        try {
            byte[] ebs = new byte[4];
            long pos = this.zip4jRaf.length() - 22;
            while (true) {
                long pos2 = pos - 1;
                this.zip4jRaf.seek(pos);
                if (((long) Raw.readLeInt(this.zip4jRaf, ebs)) == InternalZipConstants.ENDSIG) {
                    this.zip4jRaf.seek(((((this.zip4jRaf.getFilePointer() - 4) - 4) - 8) - 4) - 4);
                    return;
                }
                pos = pos2;
            }
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    public LocalFileHeader readLocalFileHeader(FileHeader fileHeader) throws ZipException {
        if (fileHeader == null || this.zip4jRaf == null) {
            throw new ZipException("invalid read parameters for local header");
        }
        long locHdrOffset = fileHeader.getOffsetLocalHeader();
        if (fileHeader.getZip64ExtendedInfo() != null && fileHeader.getZip64ExtendedInfo().getOffsetLocalHeader() > 0) {
            locHdrOffset = fileHeader.getOffsetLocalHeader();
        }
        if (locHdrOffset < 0) {
            throw new ZipException("invalid local header offset");
        }
        try {
            this.zip4jRaf.seek(locHdrOffset);
            LocalFileHeader localFileHeader = new LocalFileHeader();
            byte[] shortBuff = new byte[2];
            byte[] intBuff = new byte[4];
            byte[] longBuff = new byte[8];
            readIntoBuff(this.zip4jRaf, intBuff);
            int sig = Raw.readIntLittleEndian(intBuff, 0);
            if (((long) sig) != InternalZipConstants.LOCSIG) {
                throw new ZipException("invalid local header signature for file: " + fileHeader.getFileName());
            }
            localFileHeader.setSignature(sig);
            int length = 0 + 4;
            readIntoBuff(this.zip4jRaf, shortBuff);
            localFileHeader.setVersionNeededToExtract(Raw.readShortLittleEndian(shortBuff, 0));
            length += 2;
            readIntoBuff(this.zip4jRaf, shortBuff);
            localFileHeader.setFileNameUTF8Encoded((Raw.readShortLittleEndian(shortBuff, 0) & 2048) != 0);
            int firstByte = shortBuff[0];
            if ((firstByte & 1) != 0) {
                localFileHeader.setEncrypted(true);
            }
            localFileHeader.setGeneralPurposeFlag(shortBuff);
            length += 2;
            String binary = Integer.toBinaryString(firstByte);
            if (binary.length() >= 4) {
                localFileHeader.setDataDescriptorExists(binary.charAt(3) == '1');
            }
            readIntoBuff(this.zip4jRaf, shortBuff);
            localFileHeader.setCompressionMethod(Raw.readShortLittleEndian(shortBuff, 0));
            length += 2;
            readIntoBuff(this.zip4jRaf, intBuff);
            localFileHeader.setLastModFileTime(Raw.readIntLittleEndian(intBuff, 0));
            length += 4;
            readIntoBuff(this.zip4jRaf, intBuff);
            localFileHeader.setCrc32((long) Raw.readIntLittleEndian(intBuff, 0));
            localFileHeader.setCrcBuff((byte[]) intBuff.clone());
            length += 4;
            readIntoBuff(this.zip4jRaf, intBuff);
            localFileHeader.setCompressedSize(Raw.readLongLittleEndian(getLongByteFromIntByte(intBuff), 0));
            length += 4;
            readIntoBuff(this.zip4jRaf, intBuff);
            localFileHeader.setUncompressedSize(Raw.readLongLittleEndian(getLongByteFromIntByte(intBuff), 0));
            length += 4;
            readIntoBuff(this.zip4jRaf, shortBuff);
            int fileNameLength = Raw.readShortLittleEndian(shortBuff, 0);
            localFileHeader.setFileNameLength(fileNameLength);
            length += 2;
            readIntoBuff(this.zip4jRaf, shortBuff);
            int extraFieldLength = Raw.readShortLittleEndian(shortBuff, 0);
            localFileHeader.setExtraFieldLength(extraFieldLength);
            length += 2;
            if (fileNameLength > 0) {
                byte[] fileNameBuf = new byte[fileNameLength];
                readIntoBuff(this.zip4jRaf, fileNameBuf);
                String fileName = Zip4jUtil.decodeFileName(fileNameBuf, localFileHeader.isFileNameUTF8Encoded());
                if (fileName == null) {
                    throw new ZipException("file name is null, cannot assign file name to local file header");
                }
                if (fileName.indexOf(":" + System.getProperty("file.separator")) >= 0) {
                    fileName = fileName.substring(fileName.indexOf(":" + System.getProperty("file.separator")) + 2);
                }
                localFileHeader.setFileName(fileName);
                length = fileNameLength + 30;
            } else {
                localFileHeader.setFileName(null);
            }
            readAndSaveExtraDataRecord(localFileHeader);
            localFileHeader.setOffsetStartOfData(((long) (length + extraFieldLength)) + locHdrOffset);
            localFileHeader.setPassword(fileHeader.getPassword());
            readAndSaveZip64ExtendedInfo(localFileHeader);
            readAndSaveAESExtraDataRecord(localFileHeader);
            if (localFileHeader.isEncrypted() && localFileHeader.getEncryptionMethod() != 99) {
                if ((firstByte & 64) == 64) {
                    localFileHeader.setEncryptionMethod(1);
                } else {
                    localFileHeader.setEncryptionMethod(0);
                }
            }
            if (localFileHeader.getCrc32() <= 0) {
                localFileHeader.setCrc32(fileHeader.getCrc32());
                localFileHeader.setCrcBuff(fileHeader.getCrcBuff());
            }
            if (localFileHeader.getCompressedSize() <= 0) {
                localFileHeader.setCompressedSize(fileHeader.getCompressedSize());
            }
            if (localFileHeader.getUncompressedSize() <= 0) {
                localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());
            }
            return localFileHeader;
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    private void readAndSaveAESExtraDataRecord(FileHeader fileHeader) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("file header is null in reading Zip64 Extended Info");
        } else if (fileHeader.getExtraDataRecords() != null && fileHeader.getExtraDataRecords().size() > 0) {
            AESExtraDataRecord aesExtraDataRecord = readAESExtraDataRecord(fileHeader.getExtraDataRecords());
            if (aesExtraDataRecord != null) {
                fileHeader.setAesExtraDataRecord(aesExtraDataRecord);
                fileHeader.setEncryptionMethod(99);
            }
        }
    }

    private void readAndSaveAESExtraDataRecord(LocalFileHeader localFileHeader) throws ZipException {
        if (localFileHeader == null) {
            throw new ZipException("file header is null in reading Zip64 Extended Info");
        } else if (localFileHeader.getExtraDataRecords() != null && localFileHeader.getExtraDataRecords().size() > 0) {
            AESExtraDataRecord aesExtraDataRecord = readAESExtraDataRecord(localFileHeader.getExtraDataRecords());
            if (aesExtraDataRecord != null) {
                localFileHeader.setAesExtraDataRecord(aesExtraDataRecord);
                localFileHeader.setEncryptionMethod(99);
            }
        }
    }

    private AESExtraDataRecord readAESExtraDataRecord(ArrayList extraDataRecords) throws ZipException {
        if (extraDataRecords == null) {
            return null;
        }
        int i = 0;
        while (i < extraDataRecords.size()) {
            ExtraDataRecord extraDataRecord = (ExtraDataRecord) extraDataRecords.get(i);
            if (extraDataRecord == null || extraDataRecord.getHeader() != 39169) {
                i++;
            } else if (extraDataRecord.getData() == null) {
                throw new ZipException("corrput AES extra data records");
            } else {
                AESExtraDataRecord aesExtraDataRecord = new AESExtraDataRecord();
                aesExtraDataRecord.setSignature(39169);
                aesExtraDataRecord.setDataSize(extraDataRecord.getSizeOfData());
                byte[] aesData = extraDataRecord.getData();
                aesExtraDataRecord.setVersionNumber(Raw.readShortLittleEndian(aesData, 0));
                byte[] vendorIDBytes = new byte[2];
                System.arraycopy(aesData, 2, vendorIDBytes, 0, 2);
                aesExtraDataRecord.setVendorID(new String(vendorIDBytes));
                aesExtraDataRecord.setAesStrength(aesData[4] & 255);
                aesExtraDataRecord.setCompressionMethod(Raw.readShortLittleEndian(aesData, 5));
                return aesExtraDataRecord;
            }
        }
        return null;
    }

    private byte[] readIntoBuff(RandomAccessFile zip4jRaf, byte[] buf) throws ZipException {
        try {
            if (zip4jRaf.read(buf, 0, buf.length) != -1) {
                return buf;
            }
            throw new ZipException("unexpected end of file when reading short buff");
        } catch (Throwable e) {
            throw new ZipException("IOException when reading short buff", e);
        }
    }

    private byte[] getLongByteFromIntByte(byte[] intByte) throws ZipException {
        if (intByte == null) {
            throw new ZipException("input parameter is null, cannot expand to 8 bytes");
        } else if (intByte.length != 4) {
            throw new ZipException("invalid byte length, cannot expand to 8 bytes");
        } else {
            return new byte[]{intByte[0], intByte[1], intByte[2], intByte[3], (byte) 0, (byte) 0, (byte) 0, (byte) 0};
        }
    }
}
