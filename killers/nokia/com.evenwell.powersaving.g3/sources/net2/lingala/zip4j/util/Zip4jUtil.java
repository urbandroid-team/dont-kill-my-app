package net2.lingala.zip4j.util;

import android.support.v4.media.TransportMediator;
import android.text.TextUtils;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import net2.lingala.zip4j.exception.ZipException;
import net2.lingala.zip4j.model.FileHeader;
import net2.lingala.zip4j.model.ZipModel;

public class Zip4jUtil {
    public static boolean isStringNotNullAndNotEmpty(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return true;
    }

    public static boolean checkOutputFolder(String path) throws ZipException {
        if (isStringNotNullAndNotEmpty(path)) {
            File file = new File(path);
            if (!file.exists()) {
                try {
                    file.mkdirs();
                    if (!file.isDirectory()) {
                        throw new ZipException("output folder is not valid");
                    } else if (!file.canWrite()) {
                        throw new ZipException("no write access to destination folder");
                    }
                } catch (Exception e) {
                    throw new ZipException("Cannot create destination folder");
                }
            } else if (!file.isDirectory()) {
                throw new ZipException("output folder is not valid");
            } else if (!file.canWrite()) {
                throw new ZipException("no write access to output folder");
            }
            return true;
        }
        throw new ZipException(new NullPointerException("output path is null"));
    }

    public static boolean checkFileReadAccess(String path) throws ZipException {
        if (!isStringNotNullAndNotEmpty(path)) {
            throw new ZipException("path is null");
        } else if (checkFileExists(path)) {
            try {
                return new File(path).canRead();
            } catch (Exception e) {
                throw new ZipException("cannot read zip file");
            }
        } else {
            throw new ZipException("file does not exist: " + path);
        }
    }

    public static boolean checkFileWriteAccess(String path) throws ZipException {
        if (!isStringNotNullAndNotEmpty(path)) {
            throw new ZipException("path is null");
        } else if (checkFileExists(path)) {
            try {
                return new File(path).canWrite();
            } catch (Exception e) {
                throw new ZipException("cannot read zip file");
            }
        } else {
            throw new ZipException("file does not exist: " + path);
        }
    }

    public static boolean checkFileExists(String path) throws ZipException {
        if (isStringNotNullAndNotEmpty(path)) {
            return checkFileExists(new File(path));
        }
        throw new ZipException("path is null");
    }

    public static boolean checkFileExists(File file) throws ZipException {
        if (file != null) {
            return file.exists();
        }
        throw new ZipException("cannot check if file exists: input file is null");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }

    public static void setFileReadOnly(File file) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null. cannot set read only file attribute");
        } else if (file.exists()) {
            file.setReadOnly();
        }
    }

    public static void setFileHidden(File file) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null. cannot set hidden file attribute");
        } else if (isWindows() && file.exists()) {
            try {
                Runtime.getRuntime().exec("attrib +H \"" + file.getAbsolutePath() + "\"");
            } catch (IOException e) {
            }
        }
    }

    public static void setFileArchive(File file) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null. cannot set archive file attribute");
        } else if (isWindows() && file.exists()) {
            try {
                if (file.isDirectory()) {
                    Runtime.getRuntime().exec("attrib +A \"" + file.getAbsolutePath() + "\"");
                } else {
                    Runtime.getRuntime().exec("attrib +A \"" + file.getAbsolutePath() + "\"");
                }
            } catch (IOException e) {
            }
        }
    }

    public static void setFileSystemMode(File file) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null. cannot set archive file attribute");
        } else if (isWindows() && file.exists()) {
            try {
                Runtime.getRuntime().exec("attrib +S \"" + file.getAbsolutePath() + "\"");
            } catch (IOException e) {
            }
        }
    }

    public static long getLastModifiedFileTime(File file, TimeZone timeZone) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null, cannot read last modified file time");
        } else if (file.exists()) {
            return file.lastModified();
        } else {
            throw new ZipException("input file does not exist, cannot read last modified file time");
        }
    }

    public static String getFileNameFromFilePath(File file) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null, cannot get file name");
        } else if (file.isDirectory()) {
            return null;
        } else {
            return file.getName();
        }
    }

    public static long getFileLengh(String file) throws ZipException {
        if (isStringNotNullAndNotEmpty(file)) {
            return getFileLengh(new File(file));
        }
        throw new ZipException("invalid file name");
    }

    public static long getFileLengh(File file) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null, cannot calculate file length");
        } else if (file.isDirectory()) {
            return -1;
        } else {
            return file.length();
        }
    }

    public static long javaToDosTime(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        int year = cal.get(1);
        if (year < 1980) {
            return 2162688;
        }
        return (long) (((((((year - 1980) << 25) | ((cal.get(2) + 1) << 21)) | (cal.get(5) << 16)) | (cal.get(11) << 11)) | (cal.get(12) << 5)) | (cal.get(13) >> 1));
    }

    public static long dosToJavaTme(int dosTime) {
        int sec = (dosTime & 31) * 2;
        int min = (dosTime >> 5) & 63;
        int hrs = (dosTime >> 11) & 31;
        int day = (dosTime >> 16) & 31;
        int mon = ((dosTime >> 21) & 15) - 1;
        int year = ((dosTime >> 25) & TransportMediator.KEYCODE_MEDIA_PAUSE) + 1980;
        Calendar cal = Calendar.getInstance();
        cal.set(year, mon, day, hrs, min, sec);
        cal.set(14, 0);
        return cal.getTime().getTime();
    }

    public static FileHeader getFileHeader(ZipModel zipModel, String fileName) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot determine file header for fileName: " + fileName);
        } else if (isStringNotNullAndNotEmpty(fileName)) {
            FileHeader fileHeader = getFileHeaderWithExactMatch(zipModel, fileName);
            if (fileHeader != null) {
                return fileHeader;
            }
            fileName = fileName.replaceAll("\\\\", InternalZipConstants.ZIP_FILE_SEPARATOR);
            fileHeader = getFileHeaderWithExactMatch(zipModel, fileName);
            if (fileHeader == null) {
                return getFileHeaderWithExactMatch(zipModel, fileName.replaceAll(InternalZipConstants.ZIP_FILE_SEPARATOR, "\\\\"));
            }
            return fileHeader;
        } else {
            throw new ZipException("file name is null, cannot determine file header for fileName: " + fileName);
        }
    }

    public static FileHeader getFileHeaderWithExactMatch(ZipModel zipModel, String fileName) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("zip model is null, cannot determine file header with exact match for fileName: " + fileName);
        } else if (!isStringNotNullAndNotEmpty(fileName)) {
            throw new ZipException("file name is null, cannot determine file header with exact match for fileName: " + fileName);
        } else if (zipModel.getCentralDirectory() == null) {
            throw new ZipException("central directory is null, cannot determine file header with exact match for fileName: " + fileName);
        } else if (zipModel.getCentralDirectory().getFileHeaders() == null) {
            throw new ZipException("file Headers are null, cannot determine file header with exact match for fileName: " + fileName);
        } else if (zipModel.getCentralDirectory().getFileHeaders().size() <= 0) {
            return null;
        } else {
            ArrayList fileHeaders = zipModel.getCentralDirectory().getFileHeaders();
            for (int i = 0; i < fileHeaders.size(); i++) {
                FileHeader fileHeader = (FileHeader) fileHeaders.get(i);
                String fileNameForHdr = fileHeader.getFileName();
                if (isStringNotNullAndNotEmpty(fileNameForHdr) && fileName.equalsIgnoreCase(fileNameForHdr)) {
                    return fileHeader;
                }
            }
            return null;
        }
    }

    public static int getIndexOfFileHeader(ZipModel zipModel, FileHeader fileHeader) throws ZipException {
        if (zipModel == null || fileHeader == null) {
            throw new ZipException("input parameters is null, cannot determine index of file header");
        } else if (zipModel.getCentralDirectory() == null) {
            throw new ZipException("central directory is null, ccannot determine index of file header");
        } else if (zipModel.getCentralDirectory().getFileHeaders() == null) {
            throw new ZipException("file Headers are null, cannot determine index of file header");
        } else if (zipModel.getCentralDirectory().getFileHeaders().size() <= 0) {
            return -1;
        } else {
            String fileName = fileHeader.getFileName();
            if (isStringNotNullAndNotEmpty(fileName)) {
                ArrayList fileHeaders = zipModel.getCentralDirectory().getFileHeaders();
                for (int i = 0; i < fileHeaders.size(); i++) {
                    String fileNameForHdr = ((FileHeader) fileHeaders.get(i)).getFileName();
                    if (isStringNotNullAndNotEmpty(fileNameForHdr) && fileName.equalsIgnoreCase(fileNameForHdr)) {
                        return i;
                    }
                }
                return -1;
            }
            throw new ZipException("file name in file header is empty or null, cannot determine index of file header");
        }
    }

    public static ArrayList getFilesInDirectoryRec(File path, boolean readHiddenFiles) throws ZipException {
        if (path == null) {
            throw new ZipException("input path is null, cannot read files in the directory");
        }
        ArrayList result = new ArrayList();
        List filesDirs = Arrays.asList(path.listFiles());
        if (path.canRead()) {
            for (int i = 0; i < filesDirs.size(); i++) {
                File file = (File) filesDirs.get(i);
                if (file.isHidden() && !readHiddenFiles) {
                    break;
                }
                result.add(file);
                if (file.isDirectory()) {
                    result.addAll(getFilesInDirectoryRec(file, readHiddenFiles));
                }
            }
        }
        return result;
    }

    public static String getZipFileNameWithoutExt(String zipFile) throws ZipException {
        if (isStringNotNullAndNotEmpty(zipFile)) {
            String tmpFileName = zipFile;
            if (zipFile.indexOf(System.getProperty("file.separator")) >= 0) {
                tmpFileName = zipFile.substring(zipFile.lastIndexOf(System.getProperty("file.separator")));
            }
            if (tmpFileName.indexOf(".") > 0) {
                return tmpFileName.substring(0, tmpFileName.lastIndexOf("."));
            }
            return tmpFileName;
        }
        throw new ZipException("zip file name is empty or null, cannot determine zip file name");
    }

    public static byte[] convertCharset(String str) throws ZipException {
        try {
            String charSet = detectCharSet(str);
            if (charSet.equals(InternalZipConstants.CHARSET_CP850)) {
                return str.getBytes(InternalZipConstants.CHARSET_CP850);
            }
            if (charSet.equals(InternalZipConstants.CHARSET_UTF8)) {
                return str.getBytes(InternalZipConstants.CHARSET_UTF8);
            }
            return str.getBytes();
        } catch (UnsupportedEncodingException e) {
            return str.getBytes();
        } catch (Throwable e2) {
            throw new ZipException(e2);
        }
    }

    public static String decodeFileName(byte[] data, boolean isUTF8) {
        if (!isUTF8) {
            return getCp850EncodedString(data);
        }
        try {
            return new String(data, InternalZipConstants.CHARSET_UTF8);
        } catch (UnsupportedEncodingException e) {
            return new String(data);
        }
    }

    public static String getCp850EncodedString(byte[] data) {
        try {
            return new String(data, InternalZipConstants.CHARSET_CP850);
        } catch (UnsupportedEncodingException e) {
            return new String(data);
        }
    }

    public static String getAbsoluteFilePath(String filePath) throws ZipException {
        if (isStringNotNullAndNotEmpty(filePath)) {
            return new File(filePath).getAbsolutePath();
        }
        throw new ZipException("filePath is null or empty, cannot get absolute file path");
    }

    public static boolean checkArrayListTypes(ArrayList sourceList, int type) throws ZipException {
        if (sourceList == null) {
            throw new ZipException("input arraylist is null, cannot check types");
        } else if (sourceList.size() <= 0) {
            return true;
        } else {
            boolean invalidFound = false;
            int i;
            switch (type) {
                case 1:
                    for (i = 0; i < sourceList.size(); i++) {
                        if (!(sourceList.get(i) instanceof File)) {
                            invalidFound = true;
                            break;
                        }
                    }
                    break;
                case 2:
                    for (i = 0; i < sourceList.size(); i++) {
                        if (!(sourceList.get(i) instanceof String)) {
                            invalidFound = true;
                            break;
                        }
                    }
                    break;
            }
            if (invalidFound) {
                return false;
            }
            return true;
        }
    }

    public static String detectCharSet(String str) throws ZipException {
        if (str == null) {
            throw new ZipException("input string is null, cannot detect charset");
        }
        try {
            if (str.equals(new String(str.getBytes(InternalZipConstants.CHARSET_CP850), InternalZipConstants.CHARSET_CP850))) {
                return InternalZipConstants.CHARSET_CP850;
            }
            if (str.equals(new String(str.getBytes(InternalZipConstants.CHARSET_UTF8), InternalZipConstants.CHARSET_UTF8))) {
                return InternalZipConstants.CHARSET_UTF8;
            }
            return InternalZipConstants.CHARSET_DEFAULT;
        } catch (UnsupportedEncodingException e) {
            return InternalZipConstants.CHARSET_DEFAULT;
        } catch (Exception e2) {
            return InternalZipConstants.CHARSET_DEFAULT;
        }
    }

    public static int getEncodedStringLength(String str) throws ZipException {
        if (isStringNotNullAndNotEmpty(str)) {
            return getEncodedStringLength(str, detectCharSet(str));
        }
        throw new ZipException("input string is null, cannot calculate encoded String length");
    }

    public static int getEncodedStringLength(String str, String charset) throws ZipException {
        if (!isStringNotNullAndNotEmpty(str)) {
            throw new ZipException("input string is null, cannot calculate encoded String length");
        } else if (isStringNotNullAndNotEmpty(charset)) {
            ByteBuffer byteBuffer;
            try {
                if (charset.equals(InternalZipConstants.CHARSET_CP850)) {
                    byteBuffer = ByteBuffer.wrap(str.getBytes(InternalZipConstants.CHARSET_CP850));
                } else if (charset.equals(InternalZipConstants.CHARSET_UTF8)) {
                    byteBuffer = ByteBuffer.wrap(str.getBytes(InternalZipConstants.CHARSET_UTF8));
                } else {
                    byteBuffer = ByteBuffer.wrap(str.getBytes(charset));
                }
            } catch (UnsupportedEncodingException e) {
                byteBuffer = ByteBuffer.wrap(str.getBytes());
            } catch (Throwable e2) {
                throw new ZipException(e2);
            }
            return byteBuffer.limit();
        } else {
            throw new ZipException("encoding is not defined, cannot calculate string length");
        }
    }

    public static boolean isSupportedCharset(String charset) throws ZipException {
        if (isStringNotNullAndNotEmpty(charset)) {
            try {
                String str = new String("a".getBytes(), charset);
                return true;
            } catch (UnsupportedEncodingException e) {
                return false;
            } catch (Throwable e2) {
                throw new ZipException(e2);
            }
        }
        throw new ZipException("charset is null or empty, cannot check if it is supported");
    }

    public static ArrayList getSplitZipFiles(ZipModel zipModel) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("cannot get split zip files: zipmodel is null");
        } else if (zipModel.getEndCentralDirRecord() == null) {
            return null;
        } else {
            ArrayList retList = new ArrayList();
            String currZipFile = zipModel.getZipFile();
            String zipFileName = new File(currZipFile).getName();
            if (!isStringNotNullAndNotEmpty(currZipFile)) {
                throw new ZipException("cannot get split zip files: zipfile is null");
            } else if (zipModel.isSplitArchive()) {
                int numberOfThisDisk = zipModel.getEndCentralDirRecord().getNoOfThisDisk();
                if (numberOfThisDisk == 0) {
                    retList.add(currZipFile);
                    return retList;
                }
                for (int i = 0; i <= numberOfThisDisk; i++) {
                    if (i == numberOfThisDisk) {
                        retList.add(zipModel.getZipFile());
                    } else {
                        String fileExt = ".z0";
                        if (i > 9) {
                            fileExt = ".z";
                        }
                        retList.add(zipFileName.indexOf(".") >= 0 ? currZipFile.substring(0, currZipFile.lastIndexOf(".")) : currZipFile + fileExt + (i + 1));
                    }
                }
                return retList;
            } else {
                retList.add(currZipFile);
                return retList;
            }
        }
    }

    public static String getRelativeFileName(String file, String rootFolderInZip, String rootFolderPath) throws ZipException {
        if (isStringNotNullAndNotEmpty(file)) {
            String fileName;
            if (isStringNotNullAndNotEmpty(rootFolderPath)) {
                String rootFolderFileRef = new File(rootFolderPath).getPath();
                if (!rootFolderFileRef.endsWith(InternalZipConstants.FILE_SEPARATOR)) {
                    rootFolderFileRef = rootFolderFileRef + InternalZipConstants.FILE_SEPARATOR;
                }
                String tmpFileName = file.substring(rootFolderFileRef.length());
                if (tmpFileName.startsWith(System.getProperty("file.separator"))) {
                    tmpFileName = tmpFileName.substring(1);
                }
                File tmpFile = new File(file);
                if (tmpFile.isDirectory()) {
                    tmpFileName = tmpFileName.replaceAll("\\\\", InternalZipConstants.ZIP_FILE_SEPARATOR) + InternalZipConstants.ZIP_FILE_SEPARATOR;
                } else {
                    tmpFileName = tmpFileName.substring(0, tmpFileName.lastIndexOf(tmpFile.getName())).replaceAll("\\\\", InternalZipConstants.ZIP_FILE_SEPARATOR) + tmpFile.getName();
                }
                fileName = tmpFileName;
            } else {
                File relFile = new File(file);
                if (relFile.isDirectory()) {
                    fileName = relFile.getName() + InternalZipConstants.ZIP_FILE_SEPARATOR;
                } else {
                    fileName = getFileNameFromFilePath(new File(file));
                }
            }
            if (isStringNotNullAndNotEmpty(rootFolderInZip)) {
                fileName = rootFolderInZip + fileName;
            }
            if (isStringNotNullAndNotEmpty(fileName)) {
                return fileName;
            }
            throw new ZipException("Error determining file name");
        }
        throw new ZipException("input file path/name is empty, cannot calculate relative file name");
    }

    public static long[] getAllHeaderSignatures() {
        return new long[]{InternalZipConstants.LOCSIG, 134695760, InternalZipConstants.CENSIG, InternalZipConstants.ENDSIG, InternalZipConstants.DIGSIG, InternalZipConstants.ARCEXTDATREC, 134695760, InternalZipConstants.ZIP64ENDCENDIRLOC, InternalZipConstants.ZIP64ENDCENDIRREC, 1, 39169};
    }
}
