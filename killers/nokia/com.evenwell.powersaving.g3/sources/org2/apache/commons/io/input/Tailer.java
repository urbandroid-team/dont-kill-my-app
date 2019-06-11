package org2.apache.commons.io.input;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import net2.lingala.zip4j.util.InternalZipConstants;
import org2.apache.commons.io.FileUtils;
import org2.apache.commons.io.IOUtils;

public class Tailer implements Runnable {
    private final long delay;
    private final boolean end;
    private final File file;
    private final TailerListener listener;
    private volatile boolean run;

    public Tailer(File file, TailerListener listener) {
        this(file, listener, 1000);
    }

    public Tailer(File file, TailerListener listener, long delay) {
        this(file, listener, delay, false);
    }

    public Tailer(File file, TailerListener listener, long delay, boolean end) {
        this.run = true;
        this.file = file;
        this.delay = delay;
        this.end = end;
        this.listener = listener;
        listener.init(this);
    }

    public static Tailer create(File file, TailerListener listener, long delay, boolean end) {
        Tailer tailer = new Tailer(file, listener, delay, end);
        Thread thread = new Thread(tailer);
        thread.setDaemon(true);
        thread.start();
        return tailer;
    }

    public static Tailer create(File file, TailerListener listener, long delay) {
        return create(file, listener, delay, false);
    }

    public static Tailer create(File file, TailerListener listener) {
        return create(file, listener, 1000, false);
    }

    public File getFile() {
        return this.file;
    }

    public long getDelay() {
        return this.delay;
    }

    public void run() {
        Closeable randomAccessFile;
        Exception e;
        long last = 0;
        long position = 0;
        RandomAccessFile reader = null;
        while (this.run && reader == null) {
            try {
                try {
                    randomAccessFile = new RandomAccessFile(this.file, InternalZipConstants.READ_MODE);
                } catch (FileNotFoundException e2) {
                    this.listener.fileNotFound();
                    Object obj = reader;
                }
                if (randomAccessFile == null) {
                    try {
                        Thread.sleep(this.delay);
                        reader = randomAccessFile;
                    } catch (InterruptedException e3) {
                        reader = randomAccessFile;
                    }
                } else {
                    try {
                        position = this.end ? this.file.length() : 0;
                        last = System.currentTimeMillis();
                        randomAccessFile.seek(position);
                        reader = randomAccessFile;
                    } catch (Exception e4) {
                        e = e4;
                    }
                }
            } catch (Exception e5) {
                e = e5;
                randomAccessFile = reader;
            } catch (Throwable th) {
                Throwable th2 = th;
                randomAccessFile = reader;
            }
        }
        while (this.run) {
            long length = this.file.length();
            if (length < position) {
                this.listener.fileRotated();
                Closeable save = reader;
                try {
                    randomAccessFile = new RandomAccessFile(this.file, InternalZipConstants.READ_MODE);
                    position = 0;
                    try {
                        IOUtils.closeQuietly(save);
                        reader = randomAccessFile;
                    } catch (FileNotFoundException e6) {
                        this.listener.fileNotFound();
                        reader = randomAccessFile;
                    }
                } catch (FileNotFoundException e7) {
                    randomAccessFile = reader;
                    this.listener.fileNotFound();
                    reader = randomAccessFile;
                }
            } else {
                if (length > position) {
                    last = System.currentTimeMillis();
                    position = readLines(reader);
                } else if (FileUtils.isFileNewer(this.file, last)) {
                    reader.seek(0);
                    last = System.currentTimeMillis();
                    position = readLines(reader);
                }
                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException e8) {
                }
            }
        }
        IOUtils.closeQuietly((Closeable) reader);
        randomAccessFile = reader;
        return;
        try {
            this.listener.handle(e);
            IOUtils.closeQuietly(randomAccessFile);
        } catch (Throwable th3) {
            th2 = th3;
            IOUtils.closeQuietly(randomAccessFile);
            throw th2;
        }
    }

    public void stop() {
        this.run = false;
    }

    private long readLines(RandomAccessFile reader) throws IOException {
        long pos = reader.getFilePointer();
        String line = readLine(reader);
        while (line != null) {
            pos = reader.getFilePointer();
            this.listener.handle(line);
            line = readLine(reader);
        }
        reader.seek(pos);
        return pos;
    }

    private String readLine(RandomAccessFile reader) throws IOException {
        StringBuffer sb = new StringBuffer();
        boolean seenCR = false;
        while (true) {
            int ch = reader.read();
            if (ch != -1) {
                switch (ch) {
                    case 10:
                        return sb.toString();
                    case 13:
                        seenCR = true;
                        break;
                    default:
                        if (seenCR) {
                            sb.append('\r');
                            seenCR = false;
                        }
                        sb.append((char) ch);
                        break;
                }
            }
            return null;
        }
    }
}
