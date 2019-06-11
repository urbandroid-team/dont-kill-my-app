package com.fihtdc.backuptool;

import android.media.MediaScannerConnection;
import android.os.Bundle;
import com.fihtdc.asyncservice.LogUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import net2.lingala.zip4j.core.ZipFile;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.model.FileHeader;
import net2.lingala.zip4j.model.ZipParameters;
import net2.lingala.zip4j.util.InternalZipConstants;
import org2.apache.commons.io.FileUtils;

public final class ZipUtils {
    public static final String TEMP_FOLDER = ".tmp";
    private static final ThreadLocal<Object> THREAD_PASSWORD = new ThreadLocal();
    private boolean mCancel = false;
    private int mSuccessCount = 0;
    private ZipFile mZip = null;

    public static class ZipList {
        public final List<String> listFiles;
        public final long totalSize;

        public ZipList(List<String> list, long size) {
            this.listFiles = list;
            this.totalSize = size;
        }
    }

    public void setCancel(boolean cancel) {
        if (this.mZip != null) {
            this.mZip.setCancel(cancel);
        }
        this.mCancel = cancel;
    }

    public int getSuccessCount() {
        if (this.mZip != null) {
            return this.mZip.getSuccessCount();
        }
        return this.mSuccessCount;
    }

    public static boolean addFiles(BackupRestoreService service, String zipFile, String rootFolder, ArrayList<String> srcFiles) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        } else if (srcFiles == null) {
            return false;
        } else {
            if (srcFiles.size() == 0) {
                return true;
            }
            ZipParameters parameters = getDefaultZipParameters();
            if (rootFolder != null) {
                parameters.setRootFolderInZip(rootFolder);
            }
            ZipFile zip = new ZipFile(zipFile);
            zip.setSerivce(service);
            zip.addFiles(toFileList((ArrayList) srcFiles), parameters);
            return true;
        }
    }

    private static ArrayList<File> toFileList(ArrayList<String> files) {
        ArrayList<File> fileList = new ArrayList(files.size());
        Iterator it = files.iterator();
        while (it.hasNext()) {
            fileList.add(new File((String) it.next()));
        }
        return fileList;
    }

    public static boolean addFiles(String zipFile, String rootFolder, String... srcFiles) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        }
        if (srcFiles.length != 0) {
            int length = srcFiles.length;
            int i = 0;
            while (i < length) {
                String fileStr = srcFiles[i];
                if (new File(fileStr).exists()) {
                    i++;
                } else {
                    throw new ZipException("zip file: " + fileStr + " not exists. Pls check the file list first before zip them!");
                }
            }
            ZipParameters parameters = getDefaultZipParameters();
            if (rootFolder != null) {
                parameters.setRootFolderInZip(rootFolder);
            }
            new ZipFile(zipFile).addFiles(toFileList(srcFiles), parameters);
        }
        return true;
    }

    private static ArrayList<File> toFileList(String... files) {
        ArrayList<File> fileList = new ArrayList(files.length);
        for (String file : files) {
            fileList.add(new File(file));
        }
        return fileList;
    }

    private static ZipParameters getDefaultZipParameters() {
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(8);
        parameters.setCompressionLevel(5);
        if (getPassword() != null) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(0);
            parameters.setPassword(getPassword());
        }
        return parameters;
    }

    public static boolean addFolder(String zipFile, String rootFolder, String srcFolder) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        }
        if (srcFolder != null) {
            ZipParameters parameters = getDefaultZipParameters();
            parameters.setIncludeRootFolder(false);
            if (rootFolder != null) {
                parameters.setRootFolderInZip(rootFolder);
            }
            new ZipFile(zipFile).addFolder(new File(srcFolder), parameters);
        }
        return true;
    }

    public boolean addFolder(BackupRestoreService service, String zipFile, String rootFolder, String srcFolder) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        }
        if (srcFolder != null) {
            ZipParameters parameters = getDefaultZipParameters();
            parameters.setIncludeRootFolder(false);
            if (rootFolder != null) {
                parameters.setRootFolderInZip(rootFolder);
            }
            this.mZip = new ZipFile(zipFile);
            this.mZip.setSerivce(service);
            this.mZip.addFolder(new File(srcFolder), parameters);
        }
        return true;
    }

    public static boolean extractFiles(String zipFile, String destFolder, String... srcFiles) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        } else if (destFolder == null) {
            throw new IllegalArgumentException("Destination folder cannot be empty.");
        } else {
            ZipFile zip = new ZipFile(zipFile);
            if (zip.isValidZipFile() && zip.isEncrypted()) {
                zip.setPassword(getPassword());
            }
            new File(destFolder).mkdirs();
            for (String srcFile : srcFiles) {
                zip.extractFile(srcFile, destFolder);
            }
            return true;
        }
    }

    public static boolean extractFiles(BackupRestoreService service, String zipFile, String destFolder, String... srcFiles) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        } else if (destFolder == null) {
            throw new IllegalArgumentException("Destination folder cannot be empty.");
        } else {
            ZipFile zip = new ZipFile(zipFile);
            if (zip.isValidZipFile() && zip.isEncrypted()) {
                zip.setPassword(getPassword());
            }
            new File(destFolder).mkdirs();
            zip.setSerivce(service);
            for (String srcFile : srcFiles) {
                zip.extractFile(srcFile, destFolder);
            }
            return true;
        }
    }

    public boolean extractFolder(BackupRestoreService service, String zipFile, String destFolder, String srcFolder) throws ZipException {
        try {
            return extractFolder(service, zipFile, destFolder, srcFolder, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean extractFolder(String zipFile, String destFolder, String srcFolder) throws ZipException {
        try {
            return extractFolder(zipFile, destFolder, srcFolder, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean extractFolder(String zipFile, String destFolder, String srcFolder, boolean includeSrcFolder) throws ZipException, IOException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        } else if (destFolder == null) {
            throw new IllegalArgumentException("Destination folder cannot be empty.");
        } else {
            ZipFile zip = new ZipFile(zipFile);
            if (zip.isValidZipFile() && zip.isEncrypted()) {
                zip.setPassword(getPassword());
            }
            if (srcFolder.contains("\\")) {
                srcFolder = srcFolder.replace('\\', '/');
            }
            if (!srcFolder.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR)) {
                srcFolder = srcFolder + '/';
            }
            if (!includeSrcFolder) {
                destFolder = new File(destFolder, TEMP_FOLDER).toString();
            }
            new File(destFolder).mkdirs();
            List<?> fileHeaderList = zip.getFileHeaders();
            for (int i = 0; i < fileHeaderList.size(); i++) {
                FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                if (srcFolder == null) {
                    zip.extractFile(fileHeader, destFolder);
                } else if (fileHeader.getFileName().startsWith(srcFolder)) {
                    zip.extractFile(fileHeader, destFolder);
                }
            }
            if (!includeSrcFolder) {
                File srcDir = new File(destFolder, srcFolder);
                if (srcDir.exists() && srcDir.isDirectory()) {
                    for (File srcFile : srcDir.listFiles()) {
                        FileUtils.moveToDirectory(srcFile, new File(destFolder).getParentFile(), true);
                    }
                }
                FileUtils.deleteQuietly(new File(destFolder));
            }
            return true;
        }
    }

    public static boolean extractFolder(String zipFile, String destFolder, String srcFolder, boolean includeSrcFolder, boolean override) throws ZipException, IOException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        } else if (destFolder == null) {
            throw new IllegalArgumentException("Destination folder cannot be empty.");
        } else {
            ZipFile zip = new ZipFile(zipFile);
            if (zip.isValidZipFile() && zip.isEncrypted()) {
                zip.setPassword(getPassword());
            }
            if (srcFolder.contains("\\")) {
                srcFolder = srcFolder.replace('\\', '/');
            }
            if (!srcFolder.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR)) {
                srcFolder = srcFolder + '/';
            }
            if (!includeSrcFolder) {
                destFolder = new File(destFolder, TEMP_FOLDER).toString();
            }
            new File(destFolder).mkdirs();
            List<?> fileHeaderList = zip.getFileHeaders();
            for (int i = 0; i < fileHeaderList.size(); i++) {
                FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                if (srcFolder == null) {
                    zip.extractFile(fileHeader, destFolder);
                } else if (fileHeader.getFileName().startsWith(srcFolder)) {
                    zip.extractFile(fileHeader, destFolder);
                }
            }
            if (!includeSrcFolder) {
                File srcDir = new File(destFolder, srcFolder);
                if (srcDir.exists() && srcDir.isDirectory()) {
                    for (File srcFile : srcDir.listFiles()) {
                        if (!override) {
                            FileUtils.moveToDirectory(srcFile, new File(destFolder).getParentFile(), true);
                        } else if (srcFile.isDirectory()) {
                            FileUtils.copyDirectoryToDirectory(srcFile, new File(destFolder).getParentFile());
                            FileUtils.deleteDirectory(srcFile);
                        } else {
                            FileUtils.copyFileToDirectory(srcFile, new File(destFolder).getParentFile());
                            srcFile.delete();
                        }
                    }
                }
                FileUtils.deleteQuietly(new File(destFolder));
            }
            return true;
        }
    }

    public boolean extractFolder(BackupRestoreService service, String zipFile, String destFolder, String srcFolder, boolean includeSrcFolder) throws ZipException, IOException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        } else if (destFolder == null) {
            throw new IllegalArgumentException("Destination folder cannot be empty.");
        } else {
            int i;
            FileHeader fileHeader;
            ZipFile zipFile2 = new ZipFile(zipFile);
            if (zipFile2.isValidZipFile() && zipFile2.isEncrypted()) {
                zipFile2.setPassword(getPassword());
            }
            if (srcFolder.contains("\\")) {
                srcFolder = srcFolder.replace('\\', '/');
            }
            if (!srcFolder.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR)) {
                srcFolder = srcFolder + '/';
            }
            if (!includeSrcFolder) {
                destFolder = new File(destFolder, TEMP_FOLDER).toString();
            }
            new File(destFolder).mkdirs();
            List<?> fileHeaderList = zipFile2.getFileHeaders();
            zipFile2.setSerivce(service);
            int totalSize = 0;
            int totalCount = 0;
            for (i = 0; i < fileHeaderList.size(); i++) {
                fileHeader = (FileHeader) fileHeaderList.get(i);
                if (!fileHeader.isDirectory()) {
                    if (fileHeader.getFileName().startsWith(srcFolder)) {
                        totalCount++;
                    }
                    totalSize = (int) (((FileHeader) fileHeaderList.get(i)).getUncompressedSize() + ((long) totalSize));
                }
            }
            service.setTotalSize((long) totalSize);
            LogUtils.logD("ZipUtils", "extractFolder() --totalSize: " + totalSize);
            Bundle progressInfo = service.getProgressInfo();
            progressInfo.putInt(BackupTool.PROGRESS_STATUS, 2);
            progressInfo.putInt(BackupTool.PROGRESS_TOTAL_COUNT, totalCount);
            progressInfo.putInt(BackupTool.PROGRESS_CURRENT_COUNT, 0);
            service.updateProgress(0, progressInfo);
            int currentCount = 0;
            for (i = 0; i < fileHeaderList.size() && !this.mCancel; i++) {
                fileHeader = (FileHeader) fileHeaderList.get(i);
                if (srcFolder == null || fileHeader.getFileName().startsWith(srcFolder)) {
                    zipFile2.extractFile(fileHeader, destFolder);
                    if (includeSrcFolder && !fileHeader.isDirectory()) {
                        MediaScannerConnection.scanFile(service.getApplicationContext(), new String[]{destFolder + '/' + fileHeader.getFileName()}, null, null);
                    }
                }
                if (!fileHeader.isDirectory() && fileHeader.getFileName().startsWith(srcFolder)) {
                    this.mSuccessCount++;
                    currentCount++;
                    progressInfo.putInt(BackupTool.PROGRESS_CURRENT_COUNT, currentCount);
                    double percent = progressInfo.getDouble(BackupTool.PROGRESS_PERCENT, 0.0d);
                    LogUtils.logD("ZipUtils", "extractFolder() --percent: " + percent);
                    service.updateProgress((int) (100.0d * percent), progressInfo);
                }
            }
            if (!includeSrcFolder) {
                File file = new File(destFolder, srcFolder);
                if (file.exists() && file.isDirectory()) {
                    for (File srcFile : file.listFiles()) {
                        File finalDestFolder = new File(destFolder).getParentFile();
                        FileUtils.moveToDirectory(srcFile, finalDestFolder, true);
                        File destLoc = new File(finalDestFolder, srcFile.getName());
                        if (destLoc.isDirectory()) {
                            for (File destFile : FileUtils.listFiles(destLoc, null, true)) {
                                MediaScannerConnection.scanFile(service.getApplicationContext(), new String[]{destFile.getAbsolutePath()}, null, null);
                            }
                        } else {
                            MediaScannerConnection.scanFile(service.getApplicationContext(), new String[]{destLoc.getAbsolutePath()}, null, null);
                        }
                    }
                }
                FileUtils.deleteQuietly(new File(destFolder));
            }
            return true;
        }
    }

    public static boolean isWrongPassword(ZipException e) {
        e.printStackTrace();
        return e.getMessage() != null && e.getMessage().indexOf(" - Wrong Password?") >= 0;
    }

    public static boolean checkRootFolder(String zipFile, String rootFolder) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        } else if (rootFolder == null) {
            throw new IllegalArgumentException("Root folder cannot be empty.");
        } else {
            ZipFile zip = new ZipFile(zipFile);
            if (rootFolder.contains("\\")) {
                rootFolder = rootFolder.replace('\\', '/');
            }
            if (!rootFolder.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR)) {
                rootFolder = rootFolder + '/';
            }
            List<?> fileHeaderList = zip.getFileHeaders();
            for (int i = 0; i < fileHeaderList.size(); i++) {
                FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                if (fileHeader.isDirectory() && rootFolder.equals(fileHeader.getFileName())) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean checkRootFile(String zipFile, String rootFile) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        } else if (rootFile == null) {
            throw new IllegalArgumentException("Root file cannot be empty.");
        } else if (new ZipFile(zipFile).getFileHeader(rootFile) != null) {
            return true;
        } else {
            return false;
        }
    }

    public static List<String> listFiles(String zipFile, String rootFolder) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        }
        List<String> files = new ArrayList();
        ZipFile zip = new ZipFile(zipFile);
        if (rootFolder != null) {
            if (rootFolder.contains("\\")) {
                rootFolder = rootFolder.replace('\\', '/');
            }
            if (!rootFolder.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR)) {
                rootFolder = rootFolder + '/';
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (rootFolder == null) {
            rootFolder = "";
        }
        Pattern filePattern = Pattern.compile(stringBuilder.append(rootFolder).append("[^/]+[/]?$").toString());
        List<?> fileHeaderList = zip.getFileHeaders();
        for (int i = 0; i < fileHeaderList.size(); i++) {
            String fileName = ((FileHeader) fileHeaderList.get(i)).getFileName();
            if (filePattern.matcher(fileName).matches()) {
                files.add(fileName);
            }
        }
        return files;
    }

    public static List<String> ListFiles(String zipFile, String rootFolder) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        }
        String str;
        List<String> files = new ArrayList();
        ZipFile zip = new ZipFile(zipFile);
        if (rootFolder != null) {
            if (rootFolder.contains("\\")) {
                rootFolder = rootFolder.replace('\\', '/');
            }
            if (!rootFolder.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR)) {
                rootFolder = rootFolder + '/';
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (rootFolder == null) {
            str = "";
        } else {
            str = rootFolder;
        }
        Pattern filePattern = Pattern.compile(stringBuilder.append(str).append("[^/]+[/]?$").toString());
        List<?> fileHeaderList = zip.getFileHeaders();
        for (int i = 0; i < fileHeaderList.size(); i++) {
            FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
            String fileName = fileHeader.getFileName();
            if (!fileHeader.isDirectory() && fileName.startsWith(rootFolder)) {
                files.add(fileName);
            }
        }
        return files;
    }

    public static List<String> listFolders(String zipFile, String rootFolder) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        }
        String str;
        List<String> folders = new ArrayList();
        ZipFile zip = new ZipFile(zipFile);
        if (rootFolder != null) {
            if (rootFolder.contains("\\")) {
                rootFolder = rootFolder.replace('\\', '/');
            }
            if (!rootFolder.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR)) {
                rootFolder = rootFolder + '/';
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (rootFolder == null) {
            str = "";
        } else {
            str = rootFolder;
        }
        Pattern folderPattern = Pattern.compile(stringBuilder.append(str).append("[^/]+[/].*$").toString());
        List<?> fileHeaderList = zip.getFileHeaders();
        for (int i = 0; i < fileHeaderList.size(); i++) {
            String fileName = ((FileHeader) fileHeaderList.get(i)).getFileName();
            if (folderPattern.matcher(fileName).matches()) {
                String folder;
                if (rootFolder == null) {
                    folder = fileName.substring(0, fileName.indexOf(47) + 1);
                } else {
                    folder = fileName.substring(0, fileName.indexOf(47, rootFolder.length()) + 1);
                }
                if (!(folder == null || folders.contains(folder))) {
                    folders.add(folder);
                }
            }
        }
        return folders;
    }

    public static ZipList ListFolders(String zipFile, String rootFolder) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        }
        String str;
        List<String> folders = new ArrayList();
        ZipFile zip = new ZipFile(zipFile);
        if (rootFolder != null) {
            if (rootFolder.contains("\\")) {
                rootFolder = rootFolder.replace('\\', '/');
            }
            if (!rootFolder.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR)) {
                rootFolder = rootFolder + '/';
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (rootFolder == null) {
            str = "";
        } else {
            str = rootFolder;
        }
        Pattern folderPattern = Pattern.compile(stringBuilder.append(str).append("[^/]+[/].*$").toString());
        List<?> fileHeaderList = zip.getFileHeaders();
        long totalSize = 0;
        for (int i = 0; i < fileHeaderList.size(); i++) {
            String fileName = ((FileHeader) fileHeaderList.get(i)).getFileName();
            if (folderPattern.matcher(fileName).matches()) {
                String folder;
                if (rootFolder == null) {
                    folder = fileName.substring(0, fileName.indexOf(47) + 1);
                } else {
                    folder = fileName.substring(0, fileName.indexOf(47, rootFolder.length()) + 1);
                }
                if (!(folder == null || folders.contains(folder))) {
                    folders.add(folder);
                }
            }
            if (((FileHeader) fileHeaderList.get(i)).isDirectory()) {
                totalSize += ((FileHeader) fileHeaderList.get(i)).getUncompressedSize();
            }
        }
        return new ZipList(folders, totalSize);
    }

    public static boolean listFiles(String zipFile) throws ZipException {
        if (zipFile == null) {
            throw new IllegalArgumentException("Zip file cannot be empty.");
        }
        List<?> fileHeaderList = new ZipFile(zipFile).getFileHeaders();
        for (int i = 0; i < fileHeaderList.size(); i++) {
            FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
            System.out.println("****File Details for: " + fileHeader.getFileName() + "*****");
            System.out.println("Name: " + fileHeader.getFileName());
            System.out.println("Compressed Size: " + fileHeader.getCompressedSize());
            System.out.println("Uncompressed Size: " + fileHeader.getUncompressedSize());
            System.out.println("CRC: " + fileHeader.getCrc32());
            System.out.println("************************************************************");
        }
        return true;
    }

    public static boolean isEncrypted(String zipFile) throws ZipException {
        return new ZipFile(zipFile).isEncrypted();
    }

    public static boolean isValid(String zipFile) throws ZipException {
        return new ZipFile(zipFile).isValidZipFile();
    }

    public static void setPassword(String password) {
        THREAD_PASSWORD.set(password);
    }

    public static String removePassword() {
        String password = (String) THREAD_PASSWORD.get();
        THREAD_PASSWORD.remove();
        return password;
    }

    public static String getPassword() {
        return (String) THREAD_PASSWORD.get();
    }

    public static void main(String... args) throws ZipException {
        setPassword("123456");
        addFiles("D:\\test\\hello.zip", null, "D:/test/a/a.txt");
        addFolder("D:\\test\\helloFolder.zip", null, "D:/test/Zip4jExamples");
        listFiles("D:\\test\\helloFolder.zip");
        extractFiles("D:\\test\\helloFolder.zip", "D:/test/extract/1", "d/.classpath", "d/lib/zip4j_1.3.1.jar", "d/src/net/lingala/zip4j/examples/extract/ExtractAllFilesWithInputStreams.java");
        extractFolder("D:\\test\\helloFolder.zip", "D:/test/extract/2", "d/src/");
        removePassword();
    }
}
