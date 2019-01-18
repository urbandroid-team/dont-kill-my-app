package net2.lingala.zip4j.model;

import java.util.TimeZone;
import net2.lingala.zip4j.util.InternalZipConstants;
import net2.lingala.zip4j.util.Zip4jUtil;

public class ZipParameters implements Cloneable {
    private int aesKeyStrength = -1;
    private int compressionLevel;
    private int compressionMethod = 8;
    private String defaultFolderPath;
    private boolean encryptFiles = false;
    private int encryptionMethod = -1;
    private String fileNameInZip;
    private boolean includeRootFolder = true;
    private boolean isSourceExternalStream;
    private char[] password;
    private boolean readHiddenFiles = true;
    private String rootFolderInZip;
    private int sourceFileCRC;
    private TimeZone timeZone = TimeZone.getDefault();

    public int getCompressionMethod() {
        return this.compressionMethod;
    }

    public void setCompressionMethod(int compressionMethod) {
        this.compressionMethod = compressionMethod;
    }

    public boolean isEncryptFiles() {
        return this.encryptFiles;
    }

    public void setEncryptFiles(boolean encryptFiles) {
        this.encryptFiles = encryptFiles;
    }

    public int getEncryptionMethod() {
        return this.encryptionMethod;
    }

    public void setEncryptionMethod(int encryptionMethod) {
        this.encryptionMethod = encryptionMethod;
    }

    public int getCompressionLevel() {
        return this.compressionLevel;
    }

    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    public boolean isReadHiddenFiles() {
        return this.readHiddenFiles;
    }

    public void setReadHiddenFiles(boolean readHiddenFiles) {
        this.readHiddenFiles = readHiddenFiles;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public char[] getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        if (password != null) {
            setPassword(password.toCharArray());
        }
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public int getAesKeyStrength() {
        return this.aesKeyStrength;
    }

    public void setAesKeyStrength(int aesKeyStrength) {
        this.aesKeyStrength = aesKeyStrength;
    }

    public boolean isIncludeRootFolder() {
        return this.includeRootFolder;
    }

    public void setIncludeRootFolder(boolean includeRootFolder) {
        this.includeRootFolder = includeRootFolder;
    }

    public String getRootFolderInZip() {
        return this.rootFolderInZip;
    }

    public void setRootFolderInZip(String rootFolderInZip) {
        if (Zip4jUtil.isStringNotNullAndNotEmpty(rootFolderInZip)) {
            if (!(rootFolderInZip.endsWith("\\") || rootFolderInZip.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR))) {
                rootFolderInZip = rootFolderInZip + InternalZipConstants.FILE_SEPARATOR;
            }
            rootFolderInZip = rootFolderInZip.replaceAll("\\\\", InternalZipConstants.ZIP_FILE_SEPARATOR);
        }
        this.rootFolderInZip = rootFolderInZip;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public int getSourceFileCRC() {
        return this.sourceFileCRC;
    }

    public void setSourceFileCRC(int sourceFileCRC) {
        this.sourceFileCRC = sourceFileCRC;
    }

    public String getDefaultFolderPath() {
        return this.defaultFolderPath;
    }

    public void setDefaultFolderPath(String defaultFolderPath) {
        this.defaultFolderPath = defaultFolderPath;
    }

    public String getFileNameInZip() {
        return this.fileNameInZip;
    }

    public void setFileNameInZip(String fileNameInZip) {
        this.fileNameInZip = fileNameInZip;
    }

    public boolean isSourceExternalStream() {
        return this.isSourceExternalStream;
    }

    public void setSourceExternalStream(boolean isSourceExternalStream) {
        this.isSourceExternalStream = isSourceExternalStream;
    }
}
