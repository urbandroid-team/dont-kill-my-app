package net2.lingala.zip4j.zip;

import android.os.Bundle;
import com.fihtdc.backuptool.BackupRestoreService;
import com.fihtdc.backuptool.BackupTool;
import com.fihtdc.backuptool.FileOperator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.io.SplitOutputStream;
import net2.lingala.zip4j.io.ZipOutputStream;
import net2.lingala.zip4j.model.EndCentralDirRecord;
import net2.lingala.zip4j.model.FileHeader;
import net2.lingala.zip4j.model.ZipModel;
import net2.lingala.zip4j.model.ZipParameters;
import net2.lingala.zip4j.progress.ProgressMonitor;
import net2.lingala.zip4j.util.ArchiveMaintainer;
import net2.lingala.zip4j.util.CRCUtil;
import net2.lingala.zip4j.util.InternalZipConstants;
import net2.lingala.zip4j.util.Zip4jUtil;

public class ZipEngine {
    private boolean mCanceled = false;
    private BackupRestoreService mService;
    private int mSuccessCount = 0;
    private ZipModel zipModel;

    public ZipEngine(ZipModel zipModel) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null in ZipEngine constructor");
        }
        this.zipModel = zipModel;
    }

    public void setService(BackupRestoreService service) {
        this.mService = service;
    }

    public void setCancel(boolean cancel) {
        this.mCanceled = cancel;
    }

    public int getSuccessCount() {
        return this.mSuccessCount;
    }

    public void addFiles(ArrayList fileList, ZipParameters parameters, ProgressMonitor progressMonitor, boolean runInThread) throws ZipException {
        if (fileList == null || parameters == null) {
            throw new ZipException("one of the input parameters is null when adding files");
        } else if (fileList.size() <= 0) {
            throw new ZipException("no files to add");
        } else {
            progressMonitor.setTotalWork(calculateTotalWork(fileList, parameters));
            progressMonitor.setCurrentOperation(0);
            progressMonitor.setState(1);
            progressMonitor.setResult(1);
            if (runInThread) {
                final ArrayList arrayList = fileList;
                final ZipParameters zipParameters = parameters;
                final ProgressMonitor progressMonitor2 = progressMonitor;
                new Thread(InternalZipConstants.THREAD_NAME) {
                    public void run() {
                        try {
                            ZipEngine.this.initAddFiles(arrayList, zipParameters, progressMonitor2);
                        } catch (ZipException e) {
                        }
                    }
                }.start();
                return;
            }
            initAddFiles(fileList, parameters, progressMonitor);
        }
    }

    private void initAddFiles(ArrayList fileList, ZipParameters parameters, ProgressMonitor progressMonitor) throws ZipException {
        ZipException e;
        Throwable e2;
        Throwable th;
        Bundle progressInfo;
        if (fileList == null || parameters == null) {
            throw new ZipException("one of the input parameters is null when adding files");
        } else if (fileList.size() <= 0) {
            throw new ZipException("no files to add");
        } else {
            if (this.zipModel.getEndCentralDirRecord() == null) {
                this.zipModel.setEndCentralDirRecord(createEndOfCentralDirectoryRecord());
            }
            ZipOutputStream outputStream = null;
            InputStream inputStream = null;
            try {
                int i;
                int progress;
                checkParameters(parameters);
                removeFilesIfExists(fileList, parameters, progressMonitor);
                boolean isZipFileAlreadExists = Zip4jUtil.checkFileExists(this.zipModel.getZipFile());
                OutputStream splitOutputStream = new SplitOutputStream(new File(this.zipModel.getZipFile()), this.zipModel.getSplitLength());
                ZipOutputStream zipOutputStream = new ZipOutputStream(splitOutputStream, this.zipModel);
                if (isZipFileAlreadExists) {
                    try {
                        if (this.zipModel.getEndCentralDirRecord() == null) {
                            throw new ZipException("invalid end of central directory record");
                        }
                        splitOutputStream.seek(this.zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir());
                    } catch (ZipException e3) {
                        e = e3;
                        outputStream = zipOutputStream;
                    } catch (Exception e4) {
                        e2 = e4;
                        outputStream = zipOutputStream;
                    } catch (Throwable th2) {
                        th = th2;
                        outputStream = zipOutputStream;
                    }
                }
                long totalFileSize = 0;
                int totalCount = 0;
                int currentCount = 0;
                for (i = 0; i < fileList.size(); i++) {
                    File file = (File) fileList.get(i);
                    totalFileSize += FileOperator.getFileLength(file.getAbsolutePath());
                    if (!file.isDirectory()) {
                        totalCount++;
                    }
                }
                if (this.mService != null) {
                    this.mService.setTotalSize(totalFileSize);
                }
                byte[] readBuff = new byte[4096];
                this.mSuccessCount = 0;
                if (this.mService != null) {
                    progress = (int) (100.0d * 0.0d);
                    progressInfo = this.mService.getProgressInfo();
                    progressInfo.putInt(BackupTool.PROGRESS_TOTAL_COUNT, totalCount);
                    progressInfo.putInt(BackupTool.PROGRESS_CURRENT_COUNT, 0);
                    this.mService.updateProgress(progress, progressInfo);
                }
                i = 0;
                InputStream inputStream2 = null;
                while (i < fileList.size() && !this.mCanceled) {
                    try {
                        ZipParameters fileParameters = (ZipParameters) parameters.clone();
                        progressMonitor.setFileName(((File) fileList.get(i)).getAbsolutePath());
                        if (!((File) fileList.get(i)).isDirectory()) {
                            if (fileParameters.isEncryptFiles() && fileParameters.getEncryptionMethod() == 0) {
                                progressMonitor.setCurrentOperation(3);
                                fileParameters.setSourceFileCRC((int) CRCUtil.computeFileCRC(((File) fileList.get(i)).getAbsolutePath(), progressMonitor));
                                progressMonitor.setCurrentOperation(0);
                            }
                            if (Zip4jUtil.getFileLengh((File) fileList.get(i)) == 0) {
                                fileParameters.setCompressionMethod(0);
                            }
                        }
                        zipOutputStream.putNextEntry((File) fileList.get(i), fileParameters);
                        if (((File) fileList.get(i)).isDirectory()) {
                            zipOutputStream.closeEntry();
                            inputStream = inputStream2;
                        } else {
                            long totalSize;
                            long currentSize;
                            double percent;
                            inputStream = new FileInputStream((File) fileList.get(i));
                            long totalReadCount = 0;
                            int iLoopCount = 0;
                            while (true) {
                                int readLen = inputStream.read(readBuff);
                                if (readLen == -1) {
                                    break;
                                }
                                zipOutputStream.write(readBuff, 0, readLen);
                                progressMonitor.updateWorkCompleted((long) readLen);
                                totalReadCount += (long) readLen;
                                iLoopCount++;
                                if (iLoopCount % 10 == 0) {
                                    if (this.mService != null) {
                                        totalSize = this.mService.getTotalSize();
                                        currentSize = this.mService.getCurrentSize() + totalReadCount;
                                        this.mService.setCurrentSize(currentSize);
                                        percent = ((double) currentSize) / ((double) totalSize);
                                        progress = (int) (100.0d * percent);
                                        progressInfo = this.mService.getProgressInfo();
                                        progressInfo.putInt(BackupTool.PROGRESS_STATUS, 2);
                                        progressInfo.putDouble(BackupTool.PROGRESS_PERCENT, percent);
                                        this.mService.updateProgress(progress, progressInfo);
                                    }
                                    totalReadCount = 0;
                                }
                            }
                            if (!(this.mService == null || totalReadCount == 0)) {
                                totalSize = this.mService.getTotalSize();
                                currentSize = this.mService.getCurrentSize() + totalReadCount;
                                this.mService.setCurrentSize(currentSize);
                                percent = ((double) currentSize) / ((double) totalSize);
                                progress = (int) (100.0d * percent);
                                progressInfo = this.mService.getProgressInfo();
                                progressInfo.putInt(BackupTool.PROGRESS_STATUS, 2);
                                progressInfo.putDouble(BackupTool.PROGRESS_PERCENT, percent);
                                this.mService.updateProgress(progress, progressInfo);
                            }
                            if (this.mService != null) {
                                progressInfo = this.mService.getProgressInfo();
                                progress = (int) (100.0d * progressInfo.getDouble(BackupTool.PROGRESS_PERCENT));
                                currentCount++;
                                progressInfo.putInt(BackupTool.PROGRESS_CURRENT_COUNT, currentCount);
                                this.mService.updateProgress(progress, progressInfo);
                            }
                            zipOutputStream.closeEntry();
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            this.mSuccessCount++;
                        }
                        i++;
                        inputStream2 = inputStream;
                    } catch (ZipException e5) {
                        e = e5;
                        inputStream = inputStream2;
                        outputStream = zipOutputStream;
                    } catch (Exception e6) {
                        e2 = e6;
                        inputStream = inputStream2;
                        outputStream = zipOutputStream;
                    } catch (Throwable th3) {
                        th = th3;
                        inputStream = inputStream2;
                        outputStream = zipOutputStream;
                    }
                }
                zipOutputStream.finish();
                progressMonitor.endProgressMonitorSuccess();
                if (inputStream2 != null) {
                    try {
                        inputStream2.close();
                    } catch (IOException e7) {
                    }
                }
                if (zipOutputStream != null) {
                    try {
                        zipOutputStream.close();
                        inputStream = inputStream2;
                        outputStream = zipOutputStream;
                        return;
                    } catch (IOException e8) {
                        inputStream = inputStream2;
                        outputStream = zipOutputStream;
                        return;
                    }
                }
                outputStream = zipOutputStream;
            } catch (ZipException e9) {
                e = e9;
                try {
                    progressMonitor.endProgressMonitorError(e);
                    throw e;
                } catch (Throwable th4) {
                    th = th4;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e10) {
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e11) {
                        }
                    }
                    throw th;
                }
            } catch (Exception e12) {
                e2 = e12;
                progressMonitor.endProgressMonitorError(e2);
                e2.printStackTrace();
                if (e2.getMessage() != null) {
                    if (!(e2.getMessage().indexOf("No space left") == -1 || this.mService == null)) {
                        progressInfo = this.mService.getProgressInfo();
                        progressInfo.putInt(BackupTool.PROGRESS_STATUS, 3);
                        this.mService.updateProgress((int) (100.0d * progressInfo.getDouble(BackupTool.PROGRESS_PERCENT, 0.0d)), progressInfo);
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e13) {
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                            return;
                        } catch (IOException e14) {
                            return;
                        }
                    }
                    return;
                }
                throw new ZipException(e2);
            }
        }
    }

    public void addStreamToZip(InputStream inputStream, ZipParameters parameters) throws ZipException {
        ZipException e;
        Throwable th;
        Throwable e2;
        if (inputStream == null || parameters == null) {
            throw new ZipException("one of the input parameters is null, cannot add stream to zip");
        }
        ZipOutputStream zipOutputStream = null;
        try {
            checkParameters(parameters);
            boolean isZipFileAlreadExists = Zip4jUtil.checkFileExists(this.zipModel.getZipFile());
            SplitOutputStream splitOutputStream = new SplitOutputStream(new File(this.zipModel.getZipFile()), this.zipModel.getSplitLength());
            ZipOutputStream outputStream = new ZipOutputStream(splitOutputStream, this.zipModel);
            if (isZipFileAlreadExists) {
                try {
                    if (this.zipModel.getEndCentralDirRecord() == null) {
                        throw new ZipException("invalid end of central directory record");
                    }
                    splitOutputStream.seek(this.zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir());
                } catch (ZipException e3) {
                    e = e3;
                    zipOutputStream = outputStream;
                    try {
                        throw e;
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } catch (Exception e4) {
                    e2 = e4;
                    zipOutputStream = outputStream;
                    throw new ZipException(e2);
                } catch (Throwable th3) {
                    th = th3;
                    zipOutputStream = outputStream;
                    if (zipOutputStream != null) {
                        try {
                            zipOutputStream.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            }
            byte[] readBuff = new byte[4096];
            outputStream.putNextEntry(null, parameters);
            if (!parameters.getFileNameInZip().endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR) && !parameters.getFileNameInZip().endsWith("\\")) {
                while (true) {
                    int readLen = inputStream.read(readBuff);
                    if (readLen != -1) {
                        outputStream.write(readBuff, 0, readLen);
                    }
                }
                outputStream.closeEntry();
                outputStream.finish();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e6) {
                        return;
                    }
                }
            }
            outputStream.closeEntry();
            outputStream.finish();
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (ZipException e7) {
            e = e7;
            throw e;
        } catch (Exception e8) {
            e2 = e8;
            throw new ZipException(e2);
        }
    }

    public void addFolderToZip(File file, ZipParameters parameters, ProgressMonitor progressMonitor, boolean runInThread) throws ZipException {
        if (file == null || parameters == null) {
            throw new ZipException("one of the input parameters is null, cannot add folder to zip");
        } else if (!Zip4jUtil.checkFileExists(file.getAbsolutePath())) {
            throw new ZipException("input folder does not exist");
        } else if (!file.isDirectory()) {
            throw new ZipException("input file is not a folder, user addFileToZip method to add files");
        } else if (Zip4jUtil.checkFileReadAccess(file.getAbsolutePath())) {
            String rootFolderPath = parameters.isIncludeRootFolder() ? file.getAbsolutePath() != null ? file.getAbsoluteFile().getParentFile() != null ? file.getAbsoluteFile().getParentFile().getAbsolutePath() : "" : file.getParentFile() != null ? file.getParentFile().getAbsolutePath() : "" : file.getAbsolutePath();
            parameters.setDefaultFolderPath(rootFolderPath);
            ArrayList fileList = Zip4jUtil.getFilesInDirectoryRec(file, parameters.isReadHiddenFiles());
            if (parameters.isIncludeRootFolder()) {
                if (fileList == null) {
                    fileList = new ArrayList();
                }
                fileList.add(file);
            }
            addFiles(fileList, parameters, progressMonitor, runInThread);
        } else {
            throw new ZipException("cannot read folder: " + file.getAbsolutePath());
        }
    }

    private void checkParameters(ZipParameters parameters) throws ZipException {
        if (parameters == null) {
            throw new ZipException("cannot validate zip parameters");
        } else if (parameters.getCompressionMethod() != 0 && parameters.getCompressionMethod() != 8) {
            throw new ZipException("unsupported compression type");
        } else if (parameters.getCompressionMethod() == 8 && parameters.getCompressionLevel() < 0 && parameters.getCompressionLevel() > 9) {
            throw new ZipException("invalid compression level. compression level dor deflate should be in the range of 0-9");
        } else if (!parameters.isEncryptFiles()) {
            parameters.setAesKeyStrength(-1);
            parameters.setEncryptionMethod(-1);
        } else if (parameters.getEncryptionMethod() != 0 && parameters.getEncryptionMethod() != 99) {
            throw new ZipException("unsupported encryption method");
        } else if (parameters.getPassword() == null || parameters.getPassword().length <= 0) {
            throw new ZipException("input password is empty or null");
        }
    }

    private void removeFilesIfExists(ArrayList fileList, ZipParameters parameters, ProgressMonitor progressMonitor) throws ZipException {
        if (this.zipModel != null && this.zipModel.getCentralDirectory() != null && this.zipModel.getCentralDirectory().getFileHeaders() != null && this.zipModel.getCentralDirectory().getFileHeaders().size() > 0) {
            RandomAccessFile outputStream = null;
            for (int i = 0; i < fileList.size(); i++) {
                FileHeader fileHeader = Zip4jUtil.getFileHeader(this.zipModel, Zip4jUtil.getRelativeFileName(((File) fileList.get(i)).getAbsolutePath(), parameters.getRootFolderInZip(), parameters.getDefaultFolderPath()));
                if (fileHeader != null) {
                    if (outputStream != null) {
                        outputStream.close();
                        outputStream = null;
                    }
                    ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
                    progressMonitor.setCurrentOperation(2);
                    HashMap retMap = archiveMaintainer.initRemoveZipFile(this.zipModel, fileHeader, progressMonitor);
                    if (progressMonitor.isCancelAllTasks()) {
                        progressMonitor.setResult(3);
                        progressMonitor.setState(0);
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                                return;
                            } catch (IOException e) {
                                return;
                            }
                        }
                        return;
                    }
                    try {
                        progressMonitor.setCurrentOperation(0);
                        if (outputStream == null) {
                            outputStream = prepareFileOutputStream();
                            if (!(retMap == null || retMap.get(InternalZipConstants.OFFSET_CENTRAL_DIR) == null)) {
                                long offsetCentralDir = Long.parseLong((String) retMap.get(InternalZipConstants.OFFSET_CENTRAL_DIR));
                                if (offsetCentralDir >= 0) {
                                    outputStream.seek(offsetCentralDir);
                                } else {
                                    continue;
                                }
                            }
                        } else {
                            continue;
                        }
                    } catch (NumberFormatException e2) {
                        throw new ZipException("NumberFormatException while parsing offset central directory. Cannot update already existing file header");
                    } catch (Exception e3) {
                        throw new ZipException("Error while parsing offset central directory. Cannot update already existing file header");
                    } catch (Throwable e4) {
                        try {
                            throw new ZipException(e4);
                        } catch (Throwable th) {
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e5) {
                                }
                            }
                        }
                    }
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e6) {
                }
            }
        }
    }

    private RandomAccessFile prepareFileOutputStream() throws ZipException {
        String outPath = this.zipModel.getZipFile();
        if (Zip4jUtil.isStringNotNullAndNotEmpty(outPath)) {
            try {
                File outFile = new File(outPath);
                if (!outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdirs();
                }
                return new RandomAccessFile(outFile, InternalZipConstants.WRITE_MODE);
            } catch (Throwable e) {
                throw new ZipException(e);
            }
        }
        throw new ZipException("invalid output path");
    }

    private EndCentralDirRecord createEndOfCentralDirectoryRecord() {
        EndCentralDirRecord endCentralDirRecord = new EndCentralDirRecord();
        endCentralDirRecord.setSignature(InternalZipConstants.ENDSIG);
        endCentralDirRecord.setNoOfThisDisk(0);
        endCentralDirRecord.setTotNoOfEntriesInCentralDir(0);
        endCentralDirRecord.setTotNoOfEntriesInCentralDirOnThisDisk(0);
        endCentralDirRecord.setOffsetOfStartOfCentralDir(0);
        return endCentralDirRecord;
    }

    private long calculateTotalWork(ArrayList fileList, ZipParameters parameters) throws ZipException {
        if (fileList == null) {
            throw new ZipException("file list is null, cannot calculate total work");
        }
        long totalWork = 0;
        int i = 0;
        while (i < fileList.size()) {
            if ((fileList.get(i) instanceof File) && ((File) fileList.get(i)).exists()) {
                if (parameters.isEncryptFiles() && parameters.getEncryptionMethod() == 0) {
                    totalWork += Zip4jUtil.getFileLengh((File) fileList.get(i)) * 2;
                } else {
                    totalWork += Zip4jUtil.getFileLengh((File) fileList.get(i));
                }
                if (!(this.zipModel.getCentralDirectory() == null || this.zipModel.getCentralDirectory().getFileHeaders() == null || this.zipModel.getCentralDirectory().getFileHeaders().size() <= 0)) {
                    FileHeader fileHeader = Zip4jUtil.getFileHeader(this.zipModel, Zip4jUtil.getRelativeFileName(((File) fileList.get(i)).getAbsolutePath(), parameters.getRootFolderInZip(), parameters.getDefaultFolderPath()));
                    if (fileHeader != null) {
                        totalWork += Zip4jUtil.getFileLengh(new File(this.zipModel.getZipFile())) - fileHeader.getCompressedSize();
                    }
                }
            }
            i++;
        }
        return totalWork;
    }
}
