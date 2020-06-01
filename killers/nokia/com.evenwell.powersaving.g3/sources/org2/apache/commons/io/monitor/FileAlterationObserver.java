package org2.apache.commons.io.monitor;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org2.apache.commons.io.FileUtils;
import org2.apache.commons.io.IOCase;
import org2.apache.commons.io.comparator.NameFileComparator;

public class FileAlterationObserver implements Serializable {
    private final Comparator<File> comparator;
    private final FileFilter fileFilter;
    private final List<FileAlterationListener> listeners;
    private final FileEntry rootEntry;

    public FileAlterationObserver(String directoryName) {
        this(new File(directoryName));
    }

    public FileAlterationObserver(String directoryName, FileFilter fileFilter) {
        this(new File(directoryName), fileFilter);
    }

    public FileAlterationObserver(String directoryName, FileFilter fileFilter, IOCase caseSensitivity) {
        this(new File(directoryName), fileFilter, caseSensitivity);
    }

    public FileAlterationObserver(File directory) {
        this(directory, (FileFilter) null);
    }

    public FileAlterationObserver(File directory, FileFilter fileFilter) {
        this(directory, fileFilter, (IOCase) null);
    }

    public FileAlterationObserver(File directory, FileFilter fileFilter, IOCase caseSensitivity) {
        this(new FileEntry(directory), fileFilter, caseSensitivity);
    }

    protected FileAlterationObserver(FileEntry rootEntry, FileFilter fileFilter, IOCase caseSensitivity) {
        this.listeners = new CopyOnWriteArrayList();
        if (rootEntry == null) {
            throw new IllegalArgumentException("Root entry is missing");
        } else if (rootEntry.getFile() == null) {
            throw new IllegalArgumentException("Root directory is missing");
        } else {
            this.rootEntry = rootEntry;
            this.fileFilter = fileFilter;
            if (caseSensitivity == null || caseSensitivity.equals(IOCase.SYSTEM)) {
                this.comparator = NameFileComparator.NAME_SYSTEM_COMPARATOR;
            } else if (caseSensitivity.equals(IOCase.INSENSITIVE)) {
                this.comparator = NameFileComparator.NAME_INSENSITIVE_COMPARATOR;
            } else {
                this.comparator = NameFileComparator.NAME_COMPARATOR;
            }
        }
    }

    public File getDirectory() {
        return this.rootEntry.getFile();
    }

    public FileFilter getFileFilter() {
        return this.fileFilter;
    }

    public void addListener(FileAlterationListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    public void removeListener(FileAlterationListener listener) {
        if (listener != null) {
            do {
            } while (this.listeners.remove(listener));
        }
    }

    public Iterable<FileAlterationListener> getListeners() {
        return this.listeners;
    }

    public void initialize() throws Exception {
        this.rootEntry.refresh(this.rootEntry.getFile());
        File[] files = listFiles(this.rootEntry.getFile());
        FileEntry[] children = files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_ENTRIES;
        for (int i = 0; i < files.length; i++) {
            children[i] = createFileEntry(this.rootEntry, files[i]);
        }
        this.rootEntry.setChildren(children);
    }

    public void destroy() throws Exception {
    }

    public void checkAndNotify() {
        for (FileAlterationListener listener : this.listeners) {
            listener.onStart(this);
        }
        File rootFile = this.rootEntry.getFile();
        if (rootFile.exists()) {
            checkAndNotify(this.rootEntry, this.rootEntry.getChildren(), listFiles(rootFile));
        } else if (this.rootEntry.isExists()) {
            checkAndNotify(this.rootEntry, this.rootEntry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
        }
        for (FileAlterationListener listener2 : this.listeners) {
            listener2.onStop(this);
        }
    }

    private void checkAndNotify(FileEntry parent, FileEntry[] previous, File[] files) {
        int c = 0;
        FileEntry[] current = files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_ENTRIES;
        for (FileEntry entry : previous) {
            while (c < files.length && this.comparator.compare(entry.getFile(), files[c]) > 0) {
                current[c] = createFileEntry(parent, files[c]);
                doCreate(current[c]);
                c++;
            }
            if (c >= files.length || this.comparator.compare(entry.getFile(), files[c]) != 0) {
                checkAndNotify(entry, entry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
                doDelete(entry);
            } else {
                doMatch(entry, files[c]);
                checkAndNotify(entry, entry.getChildren(), listFiles(files[c]));
                current[c] = entry;
                c++;
            }
        }
        while (c < files.length) {
            current[c] = createFileEntry(parent, files[c]);
            doCreate(current[c]);
            c++;
        }
        parent.setChildren(current);
    }

    private FileEntry createFileEntry(FileEntry parent, File file) {
        FileEntry entry = parent.newChildInstance(file);
        entry.refresh(file);
        File[] files = listFiles(file);
        FileEntry[] children = files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_ENTRIES;
        for (int i = 0; i < files.length; i++) {
            children[i] = createFileEntry(entry, files[i]);
        }
        entry.setChildren(children);
        return entry;
    }

    private void doCreate(FileEntry entry) {
        for (FileAlterationListener listener : this.listeners) {
            if (entry.isDirectory()) {
                listener.onDirectoryCreate(entry.getFile());
            } else {
                listener.onFileCreate(entry.getFile());
            }
        }
        for (FileEntry aChildren : entry.getChildren()) {
            doCreate(aChildren);
        }
    }

    private void doMatch(FileEntry entry, File file) {
        if (entry.refresh(file)) {
            for (FileAlterationListener listener : this.listeners) {
                if (entry.isDirectory()) {
                    listener.onDirectoryChange(file);
                } else {
                    listener.onFileChange(file);
                }
            }
        }
    }

    private void doDelete(FileEntry entry) {
        for (FileAlterationListener listener : this.listeners) {
            if (entry.isDirectory()) {
                listener.onDirectoryDelete(entry.getFile());
            } else {
                listener.onFileDelete(entry.getFile());
            }
        }
    }

    private File[] listFiles(File file) {
        File[] children = null;
        if (file.isDirectory()) {
            children = this.fileFilter == null ? file.listFiles() : file.listFiles(this.fileFilter);
        }
        if (children == null) {
            children = FileUtils.EMPTY_FILE_ARRAY;
        }
        if (this.comparator != null && children.length > 1) {
            Arrays.sort(children, this.comparator);
        }
        return children;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[file='");
        builder.append(getDirectory().getPath());
        builder.append('\'');
        if (this.fileFilter != null) {
            builder.append(", ");
            builder.append(this.fileFilter.toString());
        }
        builder.append(", listeners=");
        builder.append(this.listeners.size());
        builder.append("]");
        return builder.toString();
    }
}
