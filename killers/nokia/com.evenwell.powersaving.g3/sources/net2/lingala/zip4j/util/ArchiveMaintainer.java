package net2.lingala.zip4j.util;

import android.support.v4.media.session.PlaybackStateCompat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import net2.lingala.zip4j.core.HeaderReader;
import net2.lingala.zip4j.core.HeaderWriter;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.io.SplitOutputStream;
import net2.lingala.zip4j.model.FileHeader;
import net2.lingala.zip4j.model.ZipModel;
import net2.lingala.zip4j.progress.ProgressMonitor;

public class ArchiveMaintainer {
    public HashMap removeZipFile(ZipModel zipModel, FileHeader fileHeader, ProgressMonitor progressMonitor, boolean runInThread) throws ZipException {
        if (runInThread) {
            final ZipModel zipModel2 = zipModel;
            final FileHeader fileHeader2 = fileHeader;
            final ProgressMonitor progressMonitor2 = progressMonitor;
            new Thread(InternalZipConstants.THREAD_NAME) {
                public void run() {
                    try {
                        ArchiveMaintainer.this.initRemoveZipFile(zipModel2, fileHeader2, progressMonitor2);
                        progressMonitor2.endProgressMonitorSuccess();
                    } catch (ZipException e) {
                    }
                }
            }.start();
            return null;
        }
        HashMap retMap = initRemoveZipFile(zipModel, fileHeader, progressMonitor);
        progressMonitor.endProgressMonitorSuccess();
        return retMap;
    }

    public HashMap initRemoveZipFile(ZipModel zipModel, FileHeader fileHeader, ProgressMonitor progressMonitor) throws ZipException {
        ZipException e;
        Throwable th;
        Throwable e2;
        if (fileHeader == null || zipModel == null) {
            throw new ZipException("input parameters is null in maintain zip file, cannot remove file from archive");
        }
        File zipFile = null;
        RandomAccessFile inputStream = null;
        String tmpZipFileName = null;
        HashMap retMap = new HashMap();
        OutputStream outputStream;
        try {
            int indexOfFileHeader = Zip4jUtil.getIndexOfFileHeader(zipModel, fileHeader);
            if (indexOfFileHeader < 0) {
                throw new ZipException("file header not found in zip model, cannot remove file");
            } else if (zipModel.isSplitArchive()) {
                throw new ZipException("This is a split archive. Zip file format does not allow updating split/spanned files");
            } else {
                File file;
                tmpZipFileName = zipModel.getZipFile() + (System.currentTimeMillis() % 1000);
                File tmpFile = new File(tmpZipFileName);
                while (tmpFile.exists()) {
                    tmpZipFileName = zipModel.getZipFile() + (System.currentTimeMillis() % 1000);
                    tmpFile = new File(tmpZipFileName);
                }
                outputStream = new SplitOutputStream(new File(tmpZipFileName));
                try {
                    file = new File(zipModel.getZipFile());
                } catch (ZipException e3) {
                    e = e3;
                    try {
                        progressMonitor.endProgressMonitorError(e);
                        throw e;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e4) {
                                throw new ZipException("cannot close input stream or output stream when trying to delete a file from zip file");
                            }
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (null != null) {
                            restoreFileName(zipFile, tmpZipFileName);
                        } else {
                            new File(tmpZipFileName).delete();
                        }
                        throw th;
                    }
                } catch (Exception e5) {
                    e2 = e5;
                    progressMonitor.endProgressMonitorError(e2);
                    throw new ZipException(e2);
                }
                try {
                    inputStream = createFileHandler(zipModel, InternalZipConstants.READ_MODE);
                    if (new HeaderReader(inputStream).readLocalFileHeader(fileHeader) == null) {
                        throw new ZipException("invalid local file header, cannot remove file from archive");
                    }
                    long offsetLocalFileHeader = fileHeader.getOffsetLocalHeader();
                    if (!(fileHeader.getZip64ExtendedInfo() == null || fileHeader.getZip64ExtendedInfo().getOffsetLocalHeader() == -1)) {
                        offsetLocalFileHeader = fileHeader.getZip64ExtendedInfo().getOffsetLocalHeader();
                    }
                    long offsetEndOfCompressedFile = -1;
                    long offsetStartCentralDir = zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
                    if (zipModel.isZip64Format() && zipModel.getZip64EndCentralDirRecord() != null) {
                        offsetStartCentralDir = zipModel.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
                    }
                    ArrayList fileHeaderList = zipModel.getCentralDirectory().getFileHeaders();
                    if (indexOfFileHeader == fileHeaderList.size() - 1) {
                        offsetEndOfCompressedFile = offsetStartCentralDir - 1;
                    } else {
                        FileHeader nextFileHeader = (FileHeader) fileHeaderList.get(indexOfFileHeader + 1);
                        if (nextFileHeader != null) {
                            offsetEndOfCompressedFile = nextFileHeader.getOffsetLocalHeader() - 1;
                            if (!(nextFileHeader.getZip64ExtendedInfo() == null || nextFileHeader.getZip64ExtendedInfo().getOffsetLocalHeader() == -1)) {
                                offsetEndOfCompressedFile = nextFileHeader.getZip64ExtendedInfo().getOffsetLocalHeader() - 1;
                            }
                        }
                    }
                    if (offsetLocalFileHeader < 0 || offsetEndOfCompressedFile < 0) {
                        throw new ZipException("invalid offset for start and end of local file, cannot remove file");
                    }
                    if (indexOfFileHeader == 0) {
                        if (zipModel.getCentralDirectory().getFileHeaders().size() > 1) {
                            copyFile(inputStream, outputStream, 1 + offsetEndOfCompressedFile, offsetStartCentralDir, progressMonitor);
                        }
                    } else if (indexOfFileHeader == fileHeaderList.size() - 1) {
                        copyFile(inputStream, outputStream, 0, offsetLocalFileHeader, progressMonitor);
                    } else {
                        copyFile(inputStream, outputStream, 0, offsetLocalFileHeader, progressMonitor);
                        copyFile(inputStream, outputStream, 1 + offsetEndOfCompressedFile, offsetStartCentralDir, progressMonitor);
                    }
                    if (progressMonitor.isCancelAllTasks()) {
                        progressMonitor.setResult(3);
                        progressMonitor.setState(0);
                        retMap = null;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e6) {
                                throw new ZipException("cannot close input stream or output stream when trying to delete a file from zip file");
                            }
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (null != null) {
                            restoreFileName(file, tmpZipFileName);
                        } else {
                            new File(tmpZipFileName).delete();
                        }
                    } else {
                        zipModel.getEndCentralDirRecord().setOffsetOfStartOfCentralDir(((SplitOutputStream) outputStream).getFilePointer());
                        zipModel.getEndCentralDirRecord().setTotNoOfEntriesInCentralDir(zipModel.getEndCentralDirRecord().getTotNoOfEntriesInCentralDir() - 1);
                        zipModel.getEndCentralDirRecord().setTotNoOfEntriesInCentralDirOnThisDisk(zipModel.getEndCentralDirRecord().getTotNoOfEntriesInCentralDirOnThisDisk() - 1);
                        zipModel.getCentralDirectory().getFileHeaders().remove(indexOfFileHeader);
                        int i = indexOfFileHeader;
                        while (i < zipModel.getCentralDirectory().getFileHeaders().size()) {
                            long offsetLocalHdr = ((FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(i)).getOffsetLocalHeader();
                            if (!(((FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(i)).getZip64ExtendedInfo() == null || ((FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(i)).getZip64ExtendedInfo().getOffsetLocalHeader() == -1)) {
                                offsetLocalHdr = ((FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(i)).getZip64ExtendedInfo().getOffsetLocalHeader();
                            }
                            ((FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(i)).setOffsetLocalHeader((offsetLocalHdr - (offsetEndOfCompressedFile - offsetLocalFileHeader)) - 1);
                            i++;
                        }
                        new HeaderWriter().finalizeZipFile(zipModel, outputStream);
                        retMap.put(InternalZipConstants.OFFSET_CENTRAL_DIR, Long.toString(zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir()));
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e7) {
                                throw new ZipException("cannot close input stream or output stream when trying to delete a file from zip file");
                            }
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (true) {
                            restoreFileName(file, tmpZipFileName);
                        } else {
                            new File(tmpZipFileName).delete();
                        }
                    }
                    return retMap;
                } catch (ZipException e8) {
                    e = e8;
                    zipFile = file;
                    progressMonitor.endProgressMonitorError(e);
                    throw e;
                } catch (Exception e9) {
                    e2 = e9;
                    zipFile = file;
                    progressMonitor.endProgressMonitorError(e2);
                    throw new ZipException(e2);
                } catch (Throwable th3) {
                    th = th3;
                    zipFile = file;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (null != null) {
                        new File(tmpZipFileName).delete();
                    } else {
                        restoreFileName(zipFile, tmpZipFileName);
                    }
                    throw th;
                }
            }
        } catch (Throwable e1) {
            throw new ZipException(e1);
        } catch (ZipException e10) {
            e = e10;
            outputStream = null;
            progressMonitor.endProgressMonitorError(e);
            throw e;
        } catch (Exception e11) {
            e2 = e11;
            outputStream = null;
            progressMonitor.endProgressMonitorError(e2);
            throw new ZipException(e2);
        } catch (Throwable th4) {
            th = th4;
            outputStream = null;
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (null != null) {
                restoreFileName(zipFile, tmpZipFileName);
            } else {
                new File(tmpZipFileName).delete();
            }
            throw th;
        }
    }

    private void restoreFileName(File zipFile, String tmpZipFileName) throws ZipException {
        if (!zipFile.delete()) {
            throw new ZipException("cannot delete old zip file");
        } else if (!new File(tmpZipFileName).renameTo(zipFile)) {
            throw new ZipException("cannot rename modified zip file");
        }
    }

    private void copyFile(RandomAccessFile inputStream, OutputStream outputStream, long start, long end, ProgressMonitor progressMonitor) throws ZipException {
        if (inputStream == null || outputStream == null) {
            throw new ZipException("input or output stream is null, cannot copy file");
        } else if (start < 0) {
            throw new ZipException("starting offset is negative, cannot copy file");
        } else if (end < 0) {
            throw new ZipException("end offset is negative, cannot copy file");
        } else if (start > end) {
            throw new ZipException("start offset is greater than end offset, cannot copy file");
        } else if (start != end) {
            if (progressMonitor.isCancelAllTasks()) {
                progressMonitor.setResult(3);
                progressMonitor.setState(0);
                return;
            }
            try {
                byte[] buff;
                inputStream.seek(start);
                long bytesRead = 0;
                long bytesToRead = end - start;
                if (end - start < PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM) {
                    buff = new byte[((int) (end - start))];
                } else {
                    buff = new byte[4096];
                }
                while (true) {
                    int readLen = inputStream.read(buff);
                    if (readLen != -1) {
                        outputStream.write(buff, 0, readLen);
                        progressMonitor.updateWorkCompleted((long) readLen);
                        if (progressMonitor.isCancelAllTasks()) {
                            progressMonitor.setResult(3);
                            return;
                        }
                        bytesRead += (long) readLen;
                        if (bytesRead == bytesToRead) {
                            return;
                        }
                        if (((long) buff.length) + bytesRead > bytesToRead) {
                            buff = new byte[((int) (bytesToRead - bytesRead))];
                        }
                    } else {
                        return;
                    }
                }
            } catch (Throwable e) {
                throw new ZipException(e);
            } catch (Throwable e2) {
                throw new ZipException(e2);
            }
        }
    }

    private RandomAccessFile createFileHandler(ZipModel zipModel, String mode) throws ZipException {
        if (zipModel == null || !Zip4jUtil.isStringNotNullAndNotEmpty(zipModel.getZipFile())) {
            throw new ZipException("input parameter is null in getFilePointer, cannot create file handler to remove file");
        }
        try {
            return new RandomAccessFile(new File(zipModel.getZipFile()), mode);
        } catch (Throwable e) {
            throw new ZipException(e);
        }
    }

    public void mergeSplitZipFiles(ZipModel zipModel, File outputZipFile, ProgressMonitor progressMonitor, boolean runInThread) throws ZipException {
        if (runInThread) {
            final ZipModel zipModel2 = zipModel;
            final File file = outputZipFile;
            final ProgressMonitor progressMonitor2 = progressMonitor;
            new Thread(InternalZipConstants.THREAD_NAME) {
                public void run() {
                    try {
                        ArchiveMaintainer.this.initMergeSplitZipFile(zipModel2, file, progressMonitor2);
                    } catch (ZipException e) {
                    }
                }
            }.start();
            return;
        }
        initMergeSplitZipFile(zipModel, outputZipFile, progressMonitor);
    }

    private void initMergeSplitZipFile(ZipModel zipModel, File outputZipFile, ProgressMonitor progressMonitor) throws ZipException {
        ZipException e;
        if (zipModel == null) {
            e = new ZipException("one of the input parameters is null, cannot merge split zip file");
            progressMonitor.endProgressMonitorError(e);
            throw e;
        } else if (zipModel.isSplitArchive()) {
            OutputStream outputStream = null;
            RandomAccessFile inputStream = null;
            ArrayList fileSizeList = new ArrayList();
            long totBytesWritten = 0;
            boolean splitSigRemoved = false;
            try {
                int totNoOfSplitFiles = zipModel.getEndCentralDirRecord().getNoOfThisDisk();
                if (totNoOfSplitFiles <= 0) {
                    throw new ZipException("corrupt zip model, archive not a split zip file");
                }
                outputStream = prepareOutputStreamForMerge(outputZipFile);
                for (int i = 0; i <= totNoOfSplitFiles; i++) {
                    inputStream = createSplitZipFileHandler(zipModel, i);
                    int start = 0;
                    Long end = new Long(inputStream.length());
                    if (i == 0 && zipModel.getCentralDirectory() != null && zipModel.getCentralDirectory().getFileHeaders() != null && zipModel.getCentralDirectory().getFileHeaders().size() > 0) {
                        byte[] buff = new byte[4];
                        inputStream.seek(0);
                        inputStream.read(buff);
                        if (((long) Raw.readIntLittleEndian(buff, 0)) == 134695760) {
                            start = 4;
                            splitSigRemoved = true;
                        }
                    }
                    if (i == totNoOfSplitFiles) {
                        end = new Long(zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir());
                    }
                    copyFile(inputStream, outputStream, (long) start, end.longValue(), progressMonitor);
                    totBytesWritten += end.longValue() - ((long) start);
                    if (progressMonitor.isCancelAllTasks()) {
                        progressMonitor.setResult(3);
                        progressMonitor.setState(0);
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e2) {
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                                return;
                            } catch (IOException e3) {
                                return;
                            }
                        }
                        return;
                    }
                    fileSizeList.add(end);
                    inputStream.close();
                }
                ZipModel newZipModel = (ZipModel) zipModel.clone();
                newZipModel.getEndCentralDirRecord().setOffsetOfStartOfCentralDir(totBytesWritten);
                updateSplitZipModel(newZipModel, fileSizeList, splitSigRemoved);
                new HeaderWriter().finalizeZipFileWithoutValidations(newZipModel, outputStream);
                progressMonitor.endProgressMonitorSuccess();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e4) {
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (IOException e6) {
            } catch (Throwable e7) {
                progressMonitor.endProgressMonitorError(e7);
                throw new ZipException(e7);
            } catch (Throwable e72) {
                progressMonitor.endProgressMonitorError(e72);
                throw new ZipException(e72);
            } catch (Throwable th) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e8) {
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e9) {
                    }
                }
            }
        } else {
            e = new ZipException("archive not a split zip file");
            progressMonitor.endProgressMonitorError(e);
            throw e;
        }
    }

    private RandomAccessFile createSplitZipFileHandler(ZipModel zipModel, int partNumber) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot create split file handler");
        } else if (partNumber < 0) {
            throw new ZipException("invlaid part number, cannot create split file handler");
        } else {
            try {
                String partFile;
                String curZipFile = zipModel.getZipFile();
                if (partNumber == zipModel.getEndCentralDirRecord().getNoOfThisDisk()) {
                    partFile = zipModel.getZipFile();
                } else if (partNumber >= 9) {
                    partFile = curZipFile.substring(0, curZipFile.lastIndexOf(".")) + ".z" + (partNumber + 1);
                } else {
                    partFile = curZipFile.substring(0, curZipFile.lastIndexOf(".")) + ".z0" + (partNumber + 1);
                }
                File tmpFile = new File(partFile);
                if (Zip4jUtil.checkFileExists(tmpFile)) {
                    return new RandomAccessFile(tmpFile, InternalZipConstants.READ_MODE);
                }
                throw new ZipException("split file does not exist: " + partFile);
            } catch (Throwable e) {
                throw new ZipException(e);
            } catch (Throwable e2) {
                throw new ZipException(e2);
            }
        }
    }

    private OutputStream prepareOutputStreamForMerge(File outFile) throws ZipException {
        if (outFile == null) {
            throw new ZipException("outFile is null, cannot create outputstream");
        }
        try {
            return new FileOutputStream(outFile);
        } catch (Throwable e) {
            throw new ZipException(e);
        } catch (Throwable e2) {
            throw new ZipException(e2);
        }
    }

    private void updateSplitZipModel(ZipModel zipModel, ArrayList fileSizeList, boolean splitSigRemoved) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot update split zip model");
        }
        zipModel.setSplitArchive(false);
        updateSplitFileHeader(zipModel, fileSizeList, splitSigRemoved);
        updateSplitEndCentralDirectory(zipModel);
        if (zipModel.isZip64Format()) {
            updateSplitZip64EndCentralDirLocator(zipModel, fileSizeList);
            updateSplitZip64EndCentralDirRec(zipModel, fileSizeList);
        }
    }

    private void updateSplitFileHeader(ZipModel zipModel, ArrayList fileSizeList, boolean splitSigRemoved) throws ZipException {
        try {
            if (zipModel.getCentralDirectory() == null) {
                throw new ZipException("corrupt zip model - getCentralDirectory, cannot update split zip model");
            }
            int fileHeaderCount = zipModel.getCentralDirectory().getFileHeaders().size();
            int splitSigOverhead = 0;
            if (splitSigRemoved) {
                splitSigOverhead = 4;
            }
            for (int i = 0; i < fileHeaderCount; i++) {
                long offsetLHToAdd = 0;
                for (int j = 0; j < ((FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(i)).getDiskNumberStart(); j++) {
                    offsetLHToAdd += ((Long) fileSizeList.get(j)).longValue();
                }
                ((FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(i)).setOffsetLocalHeader((((FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(i)).getOffsetLocalHeader() + offsetLHToAdd) - ((long) splitSigOverhead));
                ((FileHeader) zipModel.getCentralDirectory().getFileHeaders().get(i)).setDiskNumberStart(0);
            }
        } catch (ZipException e) {
            throw e;
        } catch (Throwable e2) {
            throw new ZipException(e2);
        }
    }

    private void updateSplitEndCentralDirectory(ZipModel zipModel) throws ZipException {
        if (zipModel == null) {
            try {
                throw new ZipException("zip model is null - cannot update end of central directory for split zip model");
            } catch (ZipException e) {
                throw e;
            } catch (Throwable e2) {
                throw new ZipException(e2);
            }
        } else if (zipModel.getCentralDirectory() == null) {
            throw new ZipException("corrupt zip model - getCentralDirectory, cannot update split zip model");
        } else {
            zipModel.getEndCentralDirRecord().setNoOfThisDisk(0);
            zipModel.getEndCentralDirRecord().setNoOfThisDiskStartOfCentralDir(0);
            zipModel.getEndCentralDirRecord().setTotNoOfEntriesInCentralDir(zipModel.getCentralDirectory().getFileHeaders().size());
            zipModel.getEndCentralDirRecord().setTotNoOfEntriesInCentralDirOnThisDisk(zipModel.getCentralDirectory().getFileHeaders().size());
        }
    }

    private void updateSplitZip64EndCentralDirLocator(ZipModel zipModel, ArrayList fileSizeList) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot update split Zip64 end of central directory locator");
        } else if (zipModel.getZip64EndCentralDirLocator() != null) {
            zipModel.getZip64EndCentralDirLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(0);
            long offsetZip64EndCentralDirRec = 0;
            for (int i = 0; i < fileSizeList.size(); i++) {
                offsetZip64EndCentralDirRec += ((Long) fileSizeList.get(i)).longValue();
            }
            zipModel.getZip64EndCentralDirLocator().setOffsetZip64EndOfCentralDirRec(zipModel.getZip64EndCentralDirLocator().getOffsetZip64EndOfCentralDirRec() + offsetZip64EndCentralDirRec);
            zipModel.getZip64EndCentralDirLocator().setTotNumberOfDiscs(1);
        }
    }

    private void updateSplitZip64EndCentralDirRec(ZipModel zipModel, ArrayList fileSizeList) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot update split Zip64 end of central directory record");
        } else if (zipModel.getZip64EndCentralDirRecord() != null) {
            zipModel.getZip64EndCentralDirRecord().setNoOfThisDisk(0);
            zipModel.getZip64EndCentralDirRecord().setNoOfThisDiskStartOfCentralDir(0);
            zipModel.getZip64EndCentralDirRecord().setTotNoOfEntriesInCentralDirOnThisDisk((long) zipModel.getEndCentralDirRecord().getTotNoOfEntriesInCentralDir());
            long offsetStartCenDirWRTStartDiskNo = 0;
            for (int i = 0; i < fileSizeList.size(); i++) {
                offsetStartCenDirWRTStartDiskNo += ((Long) fileSizeList.get(i)).longValue();
            }
            zipModel.getZip64EndCentralDirRecord().setOffsetStartCenDirWRTStartDiskNo(zipModel.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo() + offsetStartCenDirWRTStartDiskNo);
        }
    }

    public void setComment(ZipModel zipModel, String comment) throws ZipException {
        SplitOutputStream splitOutputStream;
        HeaderWriter headerWriter;
        SplitOutputStream outputStream;
        Throwable e;
        Throwable th;
        if (comment == null) {
            throw new ZipException("comment is null, cannot update Zip file with comment");
        } else if (zipModel == null) {
            throw new ZipException("zipModel is null, cannot update Zip file with comment");
        } else {
            String encodedComment = comment;
            byte[] commentBytes = comment.getBytes();
            int commentLength = comment.length();
            if (Zip4jUtil.isSupportedCharset(InternalZipConstants.CHARSET_COMMENTS_DEFAULT)) {
                try {
                    String encodedComment2 = new String(comment.getBytes(InternalZipConstants.CHARSET_COMMENTS_DEFAULT), InternalZipConstants.CHARSET_COMMENTS_DEFAULT);
                    try {
                        commentBytes = encodedComment2.getBytes(InternalZipConstants.CHARSET_COMMENTS_DEFAULT);
                        commentLength = encodedComment2.length();
                        encodedComment = encodedComment2;
                    } catch (UnsupportedEncodingException e2) {
                        encodedComment = encodedComment2;
                        encodedComment = comment;
                        commentBytes = comment.getBytes();
                        commentLength = comment.length();
                        if (commentLength > 65535) {
                            throw new ZipException("comment length exceeds maximum length");
                        }
                        zipModel.getEndCentralDirRecord().setComment(encodedComment);
                        zipModel.getEndCentralDirRecord().setCommentBytes(commentBytes);
                        zipModel.getEndCentralDirRecord().setCommentLength(commentLength);
                        splitOutputStream = null;
                        try {
                            headerWriter = new HeaderWriter();
                            outputStream = new SplitOutputStream(zipModel.getZipFile());
                            try {
                                if (zipModel.isZip64Format()) {
                                    outputStream.seek(zipModel.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo());
                                } else {
                                    outputStream.seek(zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir());
                                }
                                headerWriter.finalizeZipFileWithoutValidations(zipModel, outputStream);
                                if (outputStream == null) {
                                    try {
                                        outputStream.close();
                                    } catch (IOException e3) {
                                        return;
                                    }
                                }
                            } catch (FileNotFoundException e4) {
                                e = e4;
                                splitOutputStream = outputStream;
                                try {
                                    throw new ZipException(e);
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (splitOutputStream != null) {
                                        try {
                                            splitOutputStream.close();
                                        } catch (IOException e5) {
                                        }
                                    }
                                    throw th;
                                }
                            } catch (IOException e6) {
                                e = e6;
                                splitOutputStream = outputStream;
                                throw new ZipException(e);
                            } catch (Throwable th3) {
                                th = th3;
                                splitOutputStream = outputStream;
                                if (splitOutputStream != null) {
                                    splitOutputStream.close();
                                }
                                throw th;
                            }
                        } catch (FileNotFoundException e7) {
                            e = e7;
                            throw new ZipException(e);
                        } catch (IOException e8) {
                            e = e8;
                            throw new ZipException(e);
                        }
                    }
                } catch (UnsupportedEncodingException e9) {
                    encodedComment = comment;
                    commentBytes = comment.getBytes();
                    commentLength = comment.length();
                    if (commentLength > 65535) {
                        zipModel.getEndCentralDirRecord().setComment(encodedComment);
                        zipModel.getEndCentralDirRecord().setCommentBytes(commentBytes);
                        zipModel.getEndCentralDirRecord().setCommentLength(commentLength);
                        splitOutputStream = null;
                        headerWriter = new HeaderWriter();
                        outputStream = new SplitOutputStream(zipModel.getZipFile());
                        if (zipModel.isZip64Format()) {
                            outputStream.seek(zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir());
                        } else {
                            outputStream.seek(zipModel.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo());
                        }
                        headerWriter.finalizeZipFileWithoutValidations(zipModel, outputStream);
                        if (outputStream == null) {
                            outputStream.close();
                        }
                    }
                    throw new ZipException("comment length exceeds maximum length");
                }
            }
            if (commentLength > 65535) {
                throw new ZipException("comment length exceeds maximum length");
            }
            zipModel.getEndCentralDirRecord().setComment(encodedComment);
            zipModel.getEndCentralDirRecord().setCommentBytes(commentBytes);
            zipModel.getEndCentralDirRecord().setCommentLength(commentLength);
            splitOutputStream = null;
            headerWriter = new HeaderWriter();
            outputStream = new SplitOutputStream(zipModel.getZipFile());
            if (zipModel.isZip64Format()) {
                outputStream.seek(zipModel.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo());
            } else {
                outputStream.seek(zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir());
            }
            headerWriter.finalizeZipFileWithoutValidations(zipModel, outputStream);
            if (outputStream == null) {
                outputStream.close();
            }
        }
    }

    public void initProgressMonitorForRemoveOp(ZipModel zipModel, FileHeader fileHeader, ProgressMonitor progressMonitor) throws ZipException {
        if (zipModel == null || fileHeader == null || progressMonitor == null) {
            throw new ZipException("one of the input parameters is null, cannot calculate total work");
        }
        progressMonitor.setCurrentOperation(2);
        progressMonitor.setFileName(fileHeader.getFileName());
        progressMonitor.setTotalWork(calculateTotalWorkForRemoveOp(zipModel, fileHeader));
        progressMonitor.setState(1);
    }

    private long calculateTotalWorkForRemoveOp(ZipModel zipModel, FileHeader fileHeader) throws ZipException {
        return Zip4jUtil.getFileLengh(new File(zipModel.getZipFile())) - fileHeader.getCompressedSize();
    }

    public void initProgressMonitorForMergeOp(ZipModel zipModel, ProgressMonitor progressMonitor) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot calculate total work for merge op");
        }
        progressMonitor.setCurrentOperation(4);
        progressMonitor.setFileName(zipModel.getZipFile());
        progressMonitor.setTotalWork(calculateTotalWorkForMergeOp(zipModel));
        progressMonitor.setState(1);
    }

    private long calculateTotalWorkForMergeOp(ZipModel zipModel) throws ZipException {
        long totSize = 0;
        if (zipModel.isSplitArchive()) {
            int totNoOfSplitFiles = zipModel.getEndCentralDirRecord().getNoOfThisDisk();
            String curZipFile = zipModel.getZipFile();
            for (int i = 0; i <= totNoOfSplitFiles; i++) {
                String partFile;
                if (0 == zipModel.getEndCentralDirRecord().getNoOfThisDisk()) {
                    partFile = zipModel.getZipFile();
                } else if (0 >= 9) {
                    partFile = curZipFile.substring(0, curZipFile.lastIndexOf(".")) + ".z" + 1;
                } else {
                    partFile = curZipFile.substring(0, curZipFile.lastIndexOf(".")) + ".z0" + 1;
                }
                totSize += Zip4jUtil.getFileLengh(new File(partFile));
            }
        }
        return totSize;
    }
}
