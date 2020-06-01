package net2.lingala.zip4j.core;

import com.fihtdc.backuptool.BackupRestoreService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.io.ZipInputStream;
import net2.lingala.zip4j.model.FileHeader;
import net2.lingala.zip4j.model.UnzipParameters;
import net2.lingala.zip4j.model.ZipModel;
import net2.lingala.zip4j.model.ZipParameters;
import net2.lingala.zip4j.progress.ProgressMonitor;
import net2.lingala.zip4j.unzip.Unzip;
import net2.lingala.zip4j.util.ArchiveMaintainer;
import net2.lingala.zip4j.util.InternalZipConstants;
import net2.lingala.zip4j.util.Zip4jUtil;
import net2.lingala.zip4j.zip.ZipEngine;

public class ZipFile {
    private String file;
    private String fileNameCharset;
    private boolean isEncrypted;
    private BackupRestoreService mService;
    private ZipEngine mZipEngine;
    private int mode;
    private ProgressMonitor progressMonitor;
    private boolean runInThread;
    private ZipModel zipModel;

    public ZipFile(String zipFile) throws ZipException {
        this(new File(zipFile));
    }

    public ZipFile(File zipFile) throws ZipException {
        if (zipFile == null) {
            throw new ZipException("Input zip file parameter is not null", 1);
        }
        this.file = zipFile.getPath();
        this.mode = 2;
        this.progressMonitor = new ProgressMonitor();
        this.runInThread = false;
    }

    public void setSerivce(BackupRestoreService service) {
        this.mService = service;
    }

    public void setCancel(boolean cancel) {
        this.mZipEngine.setCancel(cancel);
    }

    public int getSuccessCount() {
        return this.mZipEngine.getSuccessCount();
    }

    public void createZipFile(File sourceFile, ZipParameters parameters) throws ZipException {
        ArrayList sourceFileList = new ArrayList();
        sourceFileList.add(sourceFile);
        createZipFile(sourceFileList, parameters, false, -1);
    }

    public void createZipFile(File sourceFile, ZipParameters parameters, boolean splitArchive, long splitLength) throws ZipException {
        ArrayList sourceFileList = new ArrayList();
        sourceFileList.add(sourceFile);
        createZipFile(sourceFileList, parameters, splitArchive, splitLength);
    }

    public void createZipFile(ArrayList sourceFileList, ZipParameters parameters) throws ZipException {
        createZipFile(sourceFileList, parameters, false, -1);
    }

    public void createZipFile(ArrayList sourceFileList, ZipParameters parameters, boolean splitArchive, long splitLength) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(this.file)) {
            throw new ZipException("zip file path is empty");
        } else if (Zip4jUtil.checkFileExists(this.file)) {
            throw new ZipException("zip file: " + this.file + " already exists. To add files to existing zip file use addFile method");
        } else if (sourceFileList == null) {
            throw new ZipException("input file ArrayList is null, cannot create zip file");
        } else if (Zip4jUtil.checkArrayListTypes(sourceFileList, 1)) {
            createNewZipModel();
            this.zipModel.setSplitArchive(splitArchive);
            this.zipModel.setSplitLength(splitLength);
            addFiles(sourceFileList, parameters);
        } else {
            throw new ZipException("One or more elements in the input ArrayList is not of type File");
        }
    }

    public void createZipFileFromFolder(String folderToAdd, ZipParameters parameters, boolean splitArchive, long splitLength) throws ZipException {
        if (Zip4jUtil.isStringNotNullAndNotEmpty(folderToAdd)) {
            createZipFileFromFolder(new File(folderToAdd), parameters, splitArchive, splitLength);
            return;
        }
        throw new ZipException("folderToAdd is empty or null, cannot create Zip File from folder");
    }

    public void createZipFileFromFolder(File folderToAdd, ZipParameters parameters, boolean splitArchive, long splitLength) throws ZipException {
        if (folderToAdd == null) {
            throw new ZipException("folderToAdd is null, cannot create zip file from folder");
        } else if (parameters == null) {
            throw new ZipException("input parameters are null, cannot create zip file from folder");
        } else if (Zip4jUtil.checkFileExists(this.file)) {
            throw new ZipException("zip file: " + this.file + " already exists. To add files to existing zip file use addFolder method");
        } else {
            createNewZipModel();
            this.zipModel.setSplitArchive(splitArchive);
            if (splitArchive) {
                this.zipModel.setSplitLength(splitLength);
            }
            addFolder(folderToAdd, parameters, false);
        }
    }

    public void addFile(File sourceFile, ZipParameters parameters) throws ZipException {
        ArrayList sourceFileList = new ArrayList();
        sourceFileList.add(sourceFile);
        addFiles(sourceFileList, parameters);
    }

    public void addFiles(ArrayList sourceFileList, ZipParameters parameters) throws ZipException {
        checkZipModel();
        if (this.zipModel == null) {
            throw new ZipException("internal error: zip model is null");
        } else if (sourceFileList == null) {
            throw new ZipException("input file ArrayList is null, cannot add files");
        } else if (!Zip4jUtil.checkArrayListTypes(sourceFileList, 1)) {
            throw new ZipException("One or more elements in the input ArrayList is not of type File");
        } else if (parameters == null) {
            throw new ZipException("input parameters are null, cannot add files to zip");
        } else if (this.progressMonitor.getState() == 1) {
            throw new ZipException("invalid operation - Zip4j is in busy state");
        } else if (Zip4jUtil.checkFileExists(this.file) && this.zipModel.isSplitArchive()) {
            throw new ZipException("Zip file already exists. Zip file format does not allow updating split/spanned files");
        } else {
            ZipEngine zipEngine = new ZipEngine(this.zipModel);
            zipEngine.setService(this.mService);
            zipEngine.addFiles(sourceFileList, parameters, this.progressMonitor, this.runInThread);
        }
    }

    public void addFolder(String path, ZipParameters parameters) throws ZipException {
        if (Zip4jUtil.isStringNotNullAndNotEmpty(path)) {
            addFolder(new File(path), parameters);
            return;
        }
        throw new ZipException("input path is null or empty, cannot add folder to zip file");
    }

    public void addFolder(File path, ZipParameters parameters) throws ZipException {
        if (path == null) {
            throw new ZipException("input path is null, cannot add folder to zip file");
        } else if (parameters == null) {
            throw new ZipException("input parameters are null, cannot add folder to zip file");
        } else {
            addFolder(path, parameters, true);
        }
    }

    private void addFolder(File path, ZipParameters parameters, boolean checkSplitArchive) throws ZipException {
        checkZipModel();
        if (this.zipModel == null) {
            throw new ZipException("internal error: zip model is null");
        } else if (checkSplitArchive && this.zipModel.isSplitArchive()) {
            throw new ZipException("This is a split archive. Zip file format does not allow updating split/spanned files");
        } else {
            this.mZipEngine = new ZipEngine(this.zipModel);
            this.mZipEngine.setService(this.mService);
            this.mZipEngine.addFolderToZip(path, parameters, this.progressMonitor, this.runInThread);
        }
    }

    public void addStream(InputStream inputStream, ZipParameters parameters) throws ZipException {
        if (inputStream == null) {
            throw new ZipException("inputstream is null, cannot add file to zip");
        } else if (parameters == null) {
            throw new ZipException("zip parameters are null");
        } else {
            setRunInThread(false);
            checkZipModel();
            if (this.zipModel == null) {
                throw new ZipException("internal error: zip model is null");
            } else if (Zip4jUtil.checkFileExists(this.file) && this.zipModel.isSplitArchive()) {
                throw new ZipException("Zip file already exists. Zip file format does not allow updating split/spanned files");
            } else {
                new ZipEngine(this.zipModel).addStreamToZip(inputStream, parameters);
            }
        }
    }

    private void readZipInfo() throws ZipException {
        Throwable e;
        Throwable th;
        if (!Zip4jUtil.checkFileExists(this.file)) {
            throw new ZipException("zip file does not exist");
        } else if (!Zip4jUtil.checkFileReadAccess(this.file)) {
            throw new ZipException("no read access for the input zip file");
        } else if (this.mode != 2) {
            throw new ZipException("Invalid mode");
        } else {
            RandomAccessFile raf = null;
            try {
                RandomAccessFile raf2 = new RandomAccessFile(new File(this.file), InternalZipConstants.READ_MODE);
                try {
                    if (this.zipModel == null) {
                        this.zipModel = new HeaderReader(raf2).readAllHeaders(this.fileNameCharset);
                        if (this.zipModel != null) {
                            this.zipModel.setZipFile(this.file);
                        }
                    }
                    if (raf2 != null) {
                        try {
                            raf2.close();
                        } catch (IOException e2) {
                        }
                    }
                } catch (FileNotFoundException e3) {
                    e = e3;
                    raf = raf2;
                    try {
                        throw new ZipException(e);
                    } catch (Throwable th2) {
                        th = th2;
                        if (raf != null) {
                            try {
                                raf.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    raf = raf2;
                    if (raf != null) {
                        raf.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                throw new ZipException(e);
            }
        }
    }

    public void extractAll(String destPath) throws ZipException {
        extractAll(destPath, null);
    }

    public void extractAll(String destPath, UnzipParameters unzipParameters) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(destPath)) {
            throw new ZipException("output path is null or invalid");
        } else if (Zip4jUtil.checkOutputFolder(destPath)) {
            if (this.zipModel == null) {
                readZipInfo();
            }
            if (this.zipModel == null) {
                throw new ZipException("Internal error occurred when extracting zip file");
            } else if (this.progressMonitor.getState() == 1) {
                throw new ZipException("invalid operation - Zip4j is in busy state");
            } else {
                new Unzip(this.zipModel).extractAll(unzipParameters, destPath, this.progressMonitor, this.runInThread);
            }
        } else {
            throw new ZipException("invalid output path");
        }
    }

    public void extractFile(FileHeader fileHeader, String destPath) throws ZipException {
        extractFile(fileHeader, destPath, null);
    }

    public void extractFile(FileHeader fileHeader, String destPath, UnzipParameters unzipParameters) throws ZipException {
        extractFile(fileHeader, destPath, unzipParameters, null);
    }

    public void extractFile(FileHeader fileHeader, String destPath, UnzipParameters unzipParameters, String newFileName) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("input file header is null, cannot extract file");
        } else if (Zip4jUtil.isStringNotNullAndNotEmpty(destPath)) {
            readZipInfo();
            if (this.progressMonitor.getState() == 1) {
                throw new ZipException("invalid operation - Zip4j is in busy state");
            }
            fileHeader.setService(this.mService);
            fileHeader.extractFile(this.zipModel, destPath, unzipParameters, newFileName, this.progressMonitor, this.runInThread);
        } else {
            throw new ZipException("destination path is empty or null, cannot extract file");
        }
    }

    public void extractFile(String fileName, String destPath) throws ZipException {
        extractFile(fileName, destPath, null);
    }

    public void extractFile(String fileName, String destPath, UnzipParameters unzipParameters) throws ZipException {
        extractFile(fileName, destPath, unzipParameters, null);
    }

    public void extractFile(String fileName, String destPath, UnzipParameters unzipParameters, String newFileName) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
            throw new ZipException("file to extract is null or empty, cannot extract file");
        } else if (Zip4jUtil.isStringNotNullAndNotEmpty(destPath)) {
            readZipInfo();
            FileHeader fileHeader = Zip4jUtil.getFileHeader(this.zipModel, fileName);
            if (fileHeader == null) {
                throw new ZipException("file header not found for given file name, cannot extract file");
            } else if (this.progressMonitor.getState() == 1) {
                throw new ZipException("invalid operation - Zip4j is in busy state");
            } else {
                fileHeader.setService(this.mService);
                fileHeader.extractFile(this.zipModel, destPath, unzipParameters, newFileName, this.progressMonitor, this.runInThread);
            }
        } else {
            throw new ZipException("destination string path is empty or null, cannot extract file");
        }
    }

    public void setPassword(String password) throws ZipException {
        if (Zip4jUtil.isStringNotNullAndNotEmpty(password)) {
            setPassword(password.toCharArray());
            return;
        }
        throw new NullPointerException();
    }

    public void setPassword(char[] password) throws ZipException {
        if (this.zipModel == null) {
            readZipInfo();
            if (this.zipModel == null) {
                throw new ZipException("Zip Model is null");
            }
        }
        if (this.zipModel.getCentralDirectory() == null || this.zipModel.getCentralDirectory().getFileHeaders() == null) {
            throw new ZipException("invalid zip file");
        }
        int i = 0;
        while (i < this.zipModel.getCentralDirectory().getFileHeaders().size()) {
            if (this.zipModel.getCentralDirectory().getFileHeaders().get(i) != null && ((FileHeader) this.zipModel.getCentralDirectory().getFileHeaders().get(i)).isEncrypted()) {
                ((FileHeader) this.zipModel.getCentralDirectory().getFileHeaders().get(i)).setPassword(password);
            }
            i++;
        }
    }

    public List getFileHeaders() throws ZipException {
        readZipInfo();
        if (this.zipModel == null || this.zipModel.getCentralDirectory() == null) {
            return null;
        }
        return this.zipModel.getCentralDirectory().getFileHeaders();
    }

    public FileHeader getFileHeader(String fileName) throws ZipException {
        if (Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
            readZipInfo();
            if (this.zipModel == null || this.zipModel.getCentralDirectory() == null) {
                return null;
            }
            return Zip4jUtil.getFileHeader(this.zipModel, fileName);
        }
        throw new ZipException("input file name is emtpy or null, cannot get FileHeader");
    }

    public boolean isEncrypted() throws ZipException {
        if (this.zipModel == null) {
            readZipInfo();
            if (this.zipModel == null) {
                throw new ZipException("Zip Model is null");
            }
        }
        if (this.zipModel.getCentralDirectory() == null || this.zipModel.getCentralDirectory().getFileHeaders() == null) {
            throw new ZipException("invalid zip file");
        }
        ArrayList fileHeaderList = this.zipModel.getCentralDirectory().getFileHeaders();
        for (int i = 0; i < fileHeaderList.size(); i++) {
            FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
            if (fileHeader != null && fileHeader.isEncrypted()) {
                this.isEncrypted = true;
                break;
            }
        }
        return this.isEncrypted;
    }

    public boolean isSplitArchive() throws ZipException {
        if (this.zipModel == null) {
            readZipInfo();
            if (this.zipModel == null) {
                throw new ZipException("Zip Model is null");
            }
        }
        return this.zipModel.isSplitArchive();
    }

    public void removeFile(String fileName) throws ZipException {
        if (Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
            if (this.zipModel == null && Zip4jUtil.checkFileExists(this.file)) {
                readZipInfo();
            }
            if (this.zipModel.isSplitArchive()) {
                throw new ZipException("Zip file format does not allow updating split/spanned files");
            }
            FileHeader fileHeader = Zip4jUtil.getFileHeader(this.zipModel, fileName);
            if (fileHeader == null) {
                throw new ZipException("could not find file header for file: " + fileName);
            }
            removeFile(fileHeader);
            return;
        }
        throw new ZipException("file name is empty or null, cannot remove file");
    }

    public void removeFile(FileHeader fileHeader) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("file header is null, cannot remove file");
        }
        if (this.zipModel == null && Zip4jUtil.checkFileExists(this.file)) {
            readZipInfo();
        }
        if (this.zipModel.isSplitArchive()) {
            throw new ZipException("Zip file format does not allow updating split/spanned files");
        }
        ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
        archiveMaintainer.initProgressMonitorForRemoveOp(this.zipModel, fileHeader, this.progressMonitor);
        archiveMaintainer.removeZipFile(this.zipModel, fileHeader, this.progressMonitor, this.runInThread);
    }

    public void mergeSplitFiles(File outputZipFile) throws ZipException {
        if (outputZipFile == null) {
            throw new ZipException("outputZipFile is null, cannot merge split files");
        } else if (outputZipFile.exists()) {
            throw new ZipException("output Zip File already exists");
        } else {
            checkZipModel();
            if (this.zipModel == null) {
                throw new ZipException("zip model is null, corrupt zip file?");
            }
            ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
            archiveMaintainer.initProgressMonitorForMergeOp(this.zipModel, this.progressMonitor);
            archiveMaintainer.mergeSplitZipFiles(this.zipModel, outputZipFile, this.progressMonitor, this.runInThread);
        }
    }

    public void setComment(String comment) throws ZipException {
        if (comment == null) {
            throw new ZipException("input comment is null, cannot update zip file");
        } else if (Zip4jUtil.checkFileExists(this.file)) {
            readZipInfo();
            if (this.zipModel == null) {
                throw new ZipException("zipModel is null, cannot update zip file");
            } else if (this.zipModel.getEndCentralDirRecord() == null) {
                throw new ZipException("end of central directory is null, cannot set comment");
            } else {
                new ArchiveMaintainer().setComment(this.zipModel, comment);
            }
        } else {
            throw new ZipException("zip file does not exist, cannot set comment for zip file");
        }
    }

    public String getComment() throws ZipException {
        return getComment(null);
    }

    public String getComment(String encoding) throws ZipException {
        if (encoding == null) {
            if (Zip4jUtil.isSupportedCharset(InternalZipConstants.CHARSET_COMMENTS_DEFAULT)) {
                encoding = InternalZipConstants.CHARSET_COMMENTS_DEFAULT;
            } else {
                encoding = InternalZipConstants.CHARSET_DEFAULT;
            }
        }
        if (Zip4jUtil.checkFileExists(this.file)) {
            checkZipModel();
            if (this.zipModel == null) {
                throw new ZipException("zip model is null, cannot read comment");
            } else if (this.zipModel.getEndCentralDirRecord() == null) {
                throw new ZipException("end of central directory record is null, cannot read comment");
            } else if (this.zipModel.getEndCentralDirRecord().getCommentBytes() == null || this.zipModel.getEndCentralDirRecord().getCommentBytes().length <= 0) {
                return null;
            } else {
                try {
                    return new String(this.zipModel.getEndCentralDirRecord().getCommentBytes(), encoding);
                } catch (Throwable e) {
                    throw new ZipException(e);
                }
            }
        }
        throw new ZipException("zip file does not exist, cannot read comment");
    }

    private void checkZipModel() throws ZipException {
        if (this.zipModel != null) {
            return;
        }
        if (Zip4jUtil.checkFileExists(this.file)) {
            readZipInfo();
        } else {
            createNewZipModel();
        }
    }

    private void createNewZipModel() {
        this.zipModel = new ZipModel();
        this.zipModel.setZipFile(this.file);
        this.zipModel.setFileNameCharset(this.fileNameCharset);
    }

    public void setFileNameCharset(String charsetName) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(charsetName)) {
            throw new ZipException("null or empty charset name");
        } else if (Zip4jUtil.isSupportedCharset(charsetName)) {
            this.fileNameCharset = charsetName;
        } else {
            throw new ZipException("unsupported charset: " + charsetName);
        }
    }

    public ZipInputStream getInputStream(FileHeader fileHeader) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("FileHeader is null, cannot get InputStream");
        }
        checkZipModel();
        if (this.zipModel != null) {
            return new Unzip(this.zipModel).getInputStream(fileHeader);
        }
        throw new ZipException("zip model is null, cannot get inputstream");
    }

    public boolean isValidZipFile() {
        try {
            readZipInfo();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ArrayList getSplitZipFiles() throws ZipException {
        checkZipModel();
        return Zip4jUtil.getSplitZipFiles(this.zipModel);
    }

    public ProgressMonitor getProgressMonitor() {
        return this.progressMonitor;
    }

    public boolean isRunInThread() {
        return this.runInThread;
    }

    public void setRunInThread(boolean runInThread) {
        this.runInThread = runInThread;
    }

    public File getFile() {
        return new File(this.file);
    }
}
