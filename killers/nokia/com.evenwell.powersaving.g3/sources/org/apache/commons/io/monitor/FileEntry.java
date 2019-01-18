package org.apache.commons.io.monitor;

import java.io.File;
import java.io.Serializable;

public class FileEntry implements Serializable {
    static final FileEntry[] EMPTY_ENTRIES = new FileEntry[0];
    private static final long serialVersionUID = -2505664948818681153L;
    private FileEntry[] children;
    private boolean directory;
    private boolean exists;
    private final File file;
    private long lastModified;
    private long length;
    private String name;
    private final FileEntry parent;

    public FileEntry(File file) {
        this(null, file);
    }

    public FileEntry(FileEntry parent, File file) {
        if (file == null) {
            throw new IllegalArgumentException("File is missing");
        }
        this.file = file;
        this.parent = parent;
        this.name = file.getName();
    }

    public boolean refresh(File file) {
        long lastModified;
        long j = 0;
        boolean origExists = this.exists;
        long origLastModified = this.lastModified;
        boolean origDirectory = this.directory;
        long origLength = this.length;
        this.name = file.getName();
        this.exists = file.exists();
        boolean z = this.exists && file.isDirectory();
        this.directory = z;
        if (this.exists) {
            lastModified = file.lastModified();
        } else {
            lastModified = 0;
        }
        this.lastModified = lastModified;
        if (this.exists && !this.directory) {
            j = file.length();
        }
        this.length = j;
        if (this.exists == origExists && this.lastModified == origLastModified && this.directory == origDirectory && this.length == origLength) {
            return false;
        }
        return true;
    }

    public FileEntry newChildInstance(File file) {
        return new FileEntry(this, file);
    }

    public FileEntry getParent() {
        return this.parent;
    }

    public int getLevel() {
        return this.parent == null ? 0 : this.parent.getLevel() + 1;
    }

    public FileEntry[] getChildren() {
        return this.children != null ? this.children : EMPTY_ENTRIES;
    }

    public void setChildren(FileEntry[] children) {
        this.children = children;
    }

    public File getFile() {
        return this.file;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLength() {
        return this.length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public boolean isExists() {
        return this.exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public boolean isDirectory() {
        return this.directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }
}
