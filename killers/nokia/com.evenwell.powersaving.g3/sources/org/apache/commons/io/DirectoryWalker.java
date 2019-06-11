package org.apache.commons.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public abstract class DirectoryWalker<T> {
    private final int depthLimit;
    private final FileFilter filter;

    public static class CancelException extends IOException {
        private static final long serialVersionUID = 1347339620135041008L;
        private final int depth;
        private final File file;

        public CancelException(File file, int depth) {
            this("Operation Cancelled", file, depth);
        }

        public CancelException(String message, File file, int depth) {
            super(message);
            this.file = file;
            this.depth = depth;
        }

        public File getFile() {
            return this.file;
        }

        public int getDepth() {
            return this.depth;
        }
    }

    protected DirectoryWalker() {
        this(null, -1);
    }

    protected DirectoryWalker(FileFilter filter, int depthLimit) {
        this.filter = filter;
        this.depthLimit = depthLimit;
    }

    protected DirectoryWalker(IOFileFilter directoryFilter, IOFileFilter fileFilter, int depthLimit) {
        if (directoryFilter == null && fileFilter == null) {
            this.filter = null;
        } else {
            if (directoryFilter == null) {
                directoryFilter = TrueFileFilter.TRUE;
            }
            if (fileFilter == null) {
                fileFilter = TrueFileFilter.TRUE;
            }
            directoryFilter = FileFilterUtils.makeDirectoryOnly(directoryFilter);
            fileFilter = FileFilterUtils.makeFileOnly(fileFilter);
            this.filter = FileFilterUtils.or(directoryFilter, fileFilter);
        }
        this.depthLimit = depthLimit;
    }

    protected final void walk(File startDirectory, Collection<T> results) throws IOException {
        if (startDirectory == null) {
            throw new NullPointerException("Start Directory is null");
        }
        try {
            handleStart(startDirectory, results);
            walk(startDirectory, 0, results);
            handleEnd(results);
        } catch (CancelException cancel) {
            handleCancelled(startDirectory, results, cancel);
        }
    }

    private void walk(File directory, int depth, Collection<T> results) throws IOException {
        checkIfCancelled(directory, depth, results);
        if (handleDirectory(directory, depth, results)) {
            handleDirectoryStart(directory, depth, results);
            int childDepth = depth + 1;
            if (this.depthLimit < 0 || childDepth <= this.depthLimit) {
                checkIfCancelled(directory, depth, results);
                File[] childFiles = filterDirectoryContents(directory, depth, this.filter == null ? directory.listFiles() : directory.listFiles(this.filter));
                if (childFiles == null) {
                    handleRestricted(directory, childDepth, results);
                } else {
                    for (File childFile : childFiles) {
                        if (childFile.isDirectory()) {
                            walk(childFile, childDepth, results);
                        } else {
                            checkIfCancelled(childFile, childDepth, results);
                            handleFile(childFile, childDepth, results);
                            checkIfCancelled(childFile, childDepth, results);
                        }
                    }
                }
            }
            handleDirectoryEnd(directory, depth, results);
        }
        checkIfCancelled(directory, depth, results);
    }

    protected final void checkIfCancelled(File file, int depth, Collection<T> results) throws IOException {
        if (handleIsCancelled(file, depth, results)) {
            throw new CancelException(file, depth);
        }
    }

    protected boolean handleIsCancelled(File file, int depth, Collection<T> collection) throws IOException {
        return false;
    }

    protected void handleCancelled(File startDirectory, Collection<T> collection, CancelException cancel) throws IOException {
        throw cancel;
    }

    protected void handleStart(File startDirectory, Collection<T> collection) throws IOException {
    }

    protected boolean handleDirectory(File directory, int depth, Collection<T> collection) throws IOException {
        return true;
    }

    protected void handleDirectoryStart(File directory, int depth, Collection<T> collection) throws IOException {
    }

    protected File[] filterDirectoryContents(File directory, int depth, File[] files) throws IOException {
        return files;
    }

    protected void handleFile(File file, int depth, Collection<T> collection) throws IOException {
    }

    protected void handleRestricted(File directory, int depth, Collection<T> collection) throws IOException {
    }

    protected void handleDirectoryEnd(File directory, int depth, Collection<T> collection) throws IOException {
    }

    protected void handleEnd(Collection<T> collection) throws IOException {
    }
}
