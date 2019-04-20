package net2.lingala.zip4j.unzip;

import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import com.fihtdc.backuptool.BackupTool;
import java.io.File;
import java.util.ArrayList;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.io.ZipInputStream;
import net2.lingala.zip4j.model.CentralDirectory;
import net2.lingala.zip4j.model.FileHeader;
import net2.lingala.zip4j.model.UnzipParameters;
import net2.lingala.zip4j.model.ZipModel;
import net2.lingala.zip4j.progress.ProgressMonitor;
import net2.lingala.zip4j.util.InternalZipConstants;
import net2.lingala.zip4j.util.Zip4jUtil;

public class Unzip {
    private ZipModel zipModel;

    public Unzip(ZipModel zipModel) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("ZipModel is null");
        }
        this.zipModel = zipModel;
    }

    public void extractAll(UnzipParameters unzipParameters, String outPath, ProgressMonitor progressMonitor, boolean runInThread) throws ZipException {
        CentralDirectory centralDirectory = this.zipModel.getCentralDirectory();
        if (centralDirectory == null || centralDirectory.getFileHeaders() == null) {
            throw new ZipException("invalid central directory in zipModel");
        }
        final ArrayList fileHeaders = centralDirectory.getFileHeaders();
        progressMonitor.setCurrentOperation(1);
        progressMonitor.setTotalWork(calculateTotalWork(fileHeaders));
        progressMonitor.setState(1);
        if (runInThread) {
            final UnzipParameters unzipParameters2 = unzipParameters;
            final ProgressMonitor progressMonitor2 = progressMonitor;
            final String str = outPath;
            new Thread(InternalZipConstants.THREAD_NAME) {
                public void run() {
                    try {
                        Unzip.this.initExtractAll(fileHeaders, unzipParameters2, progressMonitor2, str);
                        progressMonitor2.endProgressMonitorSuccess();
                    } catch (ZipException e) {
                    }
                }
            }.start();
            return;
        }
        initExtractAll(fileHeaders, unzipParameters, progressMonitor, outPath);
    }

    private void initExtractAll(ArrayList fileHeaders, UnzipParameters unzipParameters, ProgressMonitor progressMonitor, String outPath) throws ZipException {
        for (int i = 0; i < fileHeaders.size(); i++) {
            initExtractFile((FileHeader) fileHeaders.get(i), outPath, unzipParameters, null, progressMonitor);
            if (progressMonitor.isCancelAllTasks()) {
                progressMonitor.setResult(3);
                progressMonitor.setState(0);
                return;
            }
        }
    }

    public void extractFile(FileHeader fileHeader, String outPath, UnzipParameters unzipParameters, String newFileName, ProgressMonitor progressMonitor, boolean runInThread) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("fileHeader is null");
        }
        progressMonitor.setCurrentOperation(1);
        progressMonitor.setTotalWork(fileHeader.getCompressedSize());
        progressMonitor.setState(1);
        progressMonitor.setPercentDone(0);
        progressMonitor.setFileName(fileHeader.getFileName());
        if (runInThread) {
            final FileHeader fileHeader2 = fileHeader;
            final String str = outPath;
            final UnzipParameters unzipParameters2 = unzipParameters;
            final String str2 = newFileName;
            final ProgressMonitor progressMonitor2 = progressMonitor;
            new Thread(InternalZipConstants.THREAD_NAME) {
                public void run() {
                    try {
                        Unzip.this.initExtractFile(fileHeader2, str, unzipParameters2, str2, progressMonitor2);
                        progressMonitor2.endProgressMonitorSuccess();
                    } catch (ZipException e) {
                    }
                }
            }.start();
            return;
        }
        initExtractFile(fileHeader, outPath, unzipParameters, newFileName, progressMonitor);
        progressMonitor.endProgressMonitorSuccess();
    }

    private void initExtractFile(FileHeader fileHeader, String outPath, UnzipParameters unzipParameters, String newFileName, ProgressMonitor progressMonitor) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("fileHeader is null");
        }
        try {
            progressMonitor.setFileName(fileHeader.getFileName());
            if (!outPath.endsWith(InternalZipConstants.FILE_SEPARATOR)) {
                outPath = outPath + InternalZipConstants.FILE_SEPARATOR;
            }
            if (fileHeader.isDirectory()) {
                String fileName = fileHeader.getFileName();
                if (Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
                    File file = new File(outPath + fileName);
                    if (!file.exists()) {
                        file.mkdirs();
                        return;
                    }
                    return;
                }
                return;
            }
            checkOutputDirectoryStructure(fileHeader, outPath, newFileName);
            if (!fileHeader.getFileName().equals(BackupTool.BACKUP_INFO)) {
                checkFileExists(fileHeader, outPath);
            }
            UnzipEngine unzipEngine = new UnzipEngine(this.zipModel, fileHeader);
            unzipEngine.setService(fileHeader.getService());
            unzipEngine.unzipFile(progressMonitor, outPath, newFileName, unzipParameters);
        } catch (Throwable e) {
            progressMonitor.endProgressMonitorError(e);
            throw new ZipException(e);
        } catch (ZipException e2) {
            progressMonitor.endProgressMonitorError(e2);
            throw e2;
        } catch (Throwable e3) {
            progressMonitor.endProgressMonitorError(e3);
            throw new ZipException(e3);
        } catch (Throwable e32) {
            progressMonitor.endProgressMonitorError(e32);
            throw new ZipException(e32);
        }
    }

    private static void checkFileExists(FileHeader fileHeader, String outPath) {
        String fileName = fileHeader.getFileName();
        File file = new File(outPath + fileName);
        String strExt = getExtFromFilename(file.getName());
        if (strExt == null || strExt.length() == 0) {
            strExt = "";
        } else {
            strExt = "." + strExt;
        }
        int i = 1;
        String destPath = fileName;
        while (file.exists()) {
            int i2 = i + 1;
            destPath = fileName.substring(0, fileName.lastIndexOf(InternalZipConstants.ZIP_FILE_SEPARATOR)) + InternalZipConstants.ZIP_FILE_SEPARATOR + (getNameFromFilename(file.getName()) + SYMBOLS.SPACE + i + strExt);
            file = new File(outPath + destPath);
            i = i2;
        }
        fileHeader.setFileName(destPath);
    }

    private static String getExtFromFilename(String filename) {
        if (filename == null) {
            return "";
        }
        int dotPosition = filename.lastIndexOf(46);
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    private static String getNameFromFilename(String filename) {
        if (filename == null) {
            return "";
        }
        int dotPosition = filename.lastIndexOf(46);
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return filename;
    }

    public ZipInputStream getInputStream(FileHeader fileHeader) throws ZipException {
        return new UnzipEngine(this.zipModel, fileHeader).getInputStream();
    }

    private void checkOutputDirectoryStructure(FileHeader fileHeader, String outPath, String newFileName) throws ZipException {
        if (fileHeader == null || !Zip4jUtil.isStringNotNullAndNotEmpty(outPath)) {
            throw new ZipException("Cannot check output directory structure...one of the parameters was null");
        }
        String fileName = fileHeader.getFileName();
        if (Zip4jUtil.isStringNotNullAndNotEmpty(newFileName)) {
            fileName = newFileName;
        }
        if (Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
            try {
                File parentDirFile = new File(new File(outPath + fileName).getParent());
                if (!parentDirFile.exists()) {
                    parentDirFile.mkdirs();
                }
            } catch (Throwable e) {
                throw new ZipException(e);
            }
        }
    }

    private long calculateTotalWork(ArrayList fileHeaders) throws ZipException {
        if (fileHeaders == null) {
            throw new ZipException("fileHeaders is null, cannot calculate total work");
        }
        long totalWork = 0;
        for (int i = 0; i < fileHeaders.size(); i++) {
            FileHeader fileHeader = (FileHeader) fileHeaders.get(i);
            if (fileHeader.getZip64ExtendedInfo() == null || fileHeader.getZip64ExtendedInfo().getUnCompressedSize() <= 0) {
                totalWork += fileHeader.getCompressedSize();
            } else {
                totalWork += fileHeader.getZip64ExtendedInfo().getCompressedSize();
            }
        }
        return totalWork;
    }
}
