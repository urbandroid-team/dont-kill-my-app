package org.apache.commons.io.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class LockableFileWriter extends Writer {
    private static final String LCK = ".lck";
    private final File lockFile;
    private final Writer out;

    public LockableFileWriter(String fileName) throws IOException {
        this(fileName, false, null);
    }

    public LockableFileWriter(String fileName, boolean append) throws IOException {
        this(fileName, append, null);
    }

    public LockableFileWriter(String fileName, boolean append, String lockDir) throws IOException {
        this(new File(fileName), append, lockDir);
    }

    public LockableFileWriter(File file) throws IOException {
        this(file, false, null);
    }

    public LockableFileWriter(File file, boolean append) throws IOException {
        this(file, append, null);
    }

    @Deprecated
    public LockableFileWriter(File file, boolean append, String lockDir) throws IOException {
        this(file, Charset.defaultCharset(), append, lockDir);
    }

    public LockableFileWriter(File file, Charset encoding) throws IOException {
        this(file, encoding, false, null);
    }

    public LockableFileWriter(File file, String encoding) throws IOException {
        this(file, encoding, false, null);
    }

    public LockableFileWriter(File file, Charset encoding, boolean append, String lockDir) throws IOException {
        file = file.getAbsoluteFile();
        if (file.getParentFile() != null) {
            FileUtils.forceMkdir(file.getParentFile());
        }
        if (file.isDirectory()) {
            throw new IOException("File specified is a directory");
        }
        if (lockDir == null) {
            lockDir = System.getProperty("java.io.tmpdir");
        }
        File lockDirFile = new File(lockDir);
        FileUtils.forceMkdir(lockDirFile);
        testLockDir(lockDirFile);
        this.lockFile = new File(lockDirFile, file.getName() + LCK);
        createLock();
        this.out = initWriter(file, encoding, append);
    }

    public LockableFileWriter(File file, String encoding, boolean append, String lockDir) throws IOException {
        this(file, Charsets.toCharset(encoding), append, lockDir);
    }

    private void testLockDir(File lockDir) throws IOException {
        if (!lockDir.exists()) {
            throw new IOException("Could not find lockDir: " + lockDir.getAbsolutePath());
        } else if (!lockDir.canWrite()) {
            throw new IOException("Could not write to lockDir: " + lockDir.getAbsolutePath());
        }
    }

    private void createLock() throws IOException {
        synchronized (LockableFileWriter.class) {
            if (this.lockFile.createNewFile()) {
                this.lockFile.deleteOnExit();
            } else {
                throw new IOException("Can't write file, lock " + this.lockFile.getAbsolutePath() + " exists");
            }
        }
    }

    private Writer initWriter(File file, Charset encoding, boolean append) throws IOException {
        IOException ex;
        RuntimeException ex2;
        boolean fileExistedAlready = file.exists();
        OutputStream stream = null;
        try {
            OutputStream stream2 = new FileOutputStream(file.getAbsolutePath(), append);
            try {
                return new OutputStreamWriter(stream2, Charsets.toCharset(encoding));
            } catch (IOException e) {
                ex = e;
                stream = stream2;
                IOUtils.closeQuietly(null);
                IOUtils.closeQuietly(stream);
                FileUtils.deleteQuietly(this.lockFile);
                if (!fileExistedAlready) {
                    FileUtils.deleteQuietly(file);
                }
                throw ex;
            } catch (RuntimeException e2) {
                ex2 = e2;
                stream = stream2;
                IOUtils.closeQuietly(null);
                IOUtils.closeQuietly(stream);
                FileUtils.deleteQuietly(this.lockFile);
                if (!fileExistedAlready) {
                    FileUtils.deleteQuietly(file);
                }
                throw ex2;
            }
        } catch (IOException e3) {
            ex = e3;
            IOUtils.closeQuietly(null);
            IOUtils.closeQuietly(stream);
            FileUtils.deleteQuietly(this.lockFile);
            if (fileExistedAlready) {
                FileUtils.deleteQuietly(file);
            }
            throw ex;
        } catch (RuntimeException e4) {
            ex2 = e4;
            IOUtils.closeQuietly(null);
            IOUtils.closeQuietly(stream);
            FileUtils.deleteQuietly(this.lockFile);
            if (fileExistedAlready) {
                FileUtils.deleteQuietly(file);
            }
            throw ex2;
        }
    }

    public void close() throws IOException {
        try {
            this.out.close();
        } finally {
            this.lockFile.delete();
        }
    }

    public void write(int idx) throws IOException {
        this.out.write(idx);
    }

    public void write(char[] chr) throws IOException {
        this.out.write(chr);
    }

    public void write(char[] chr, int st, int end) throws IOException {
        this.out.write(chr, st, end);
    }

    public void write(String str) throws IOException {
        this.out.write(str);
    }

    public void write(String str, int st, int end) throws IOException {
        this.out.write(str, st, end);
    }

    public void flush() throws IOException {
        this.out.flush();
    }
}
