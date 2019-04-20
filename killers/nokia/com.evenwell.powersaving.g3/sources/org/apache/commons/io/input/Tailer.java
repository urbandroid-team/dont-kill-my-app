package org.apache.commons.io.input;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Tailer implements Runnable {
    private static final int DEFAULT_BUFSIZE = 4096;
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private static final int DEFAULT_DELAY_MILLIS = 1000;
    private static final String RAF_MODE = "r";
    private final Charset cset;
    private final long delayMillis;
    private final boolean end;
    private final File file;
    private final byte[] inbuf;
    private final TailerListener listener;
    private final boolean reOpen;
    private volatile boolean run;

    public Tailer(File file, TailerListener listener) {
        this(file, listener, 1000);
    }

    public Tailer(File file, TailerListener listener, long delayMillis) {
        this(file, listener, delayMillis, false);
    }

    public Tailer(File file, TailerListener listener, long delayMillis, boolean end) {
        this(file, listener, delayMillis, end, 4096);
    }

    public Tailer(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen) {
        this(file, listener, delayMillis, end, reOpen, 4096);
    }

    public Tailer(File file, TailerListener listener, long delayMillis, boolean end, int bufSize) {
        this(file, listener, delayMillis, end, false, bufSize);
    }

    public Tailer(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufSize) {
        this(file, DEFAULT_CHARSET, listener, delayMillis, end, reOpen, bufSize);
    }

    public Tailer(File file, Charset cset, TailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufSize) {
        this.run = true;
        this.file = file;
        this.delayMillis = delayMillis;
        this.end = end;
        this.inbuf = new byte[bufSize];
        this.listener = listener;
        listener.init(this);
        this.reOpen = reOpen;
        this.cset = cset;
    }

    public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end, int bufSize) {
        return create(file, listener, delayMillis, end, false, bufSize);
    }

    public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufSize) {
        return create(file, DEFAULT_CHARSET, listener, delayMillis, end, reOpen, bufSize);
    }

    public static Tailer create(File file, Charset charset, TailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufSize) {
        Tailer tailer = new Tailer(file, charset, listener, delayMillis, end, reOpen, bufSize);
        Thread thread = new Thread(tailer);
        thread.setDaemon(true);
        thread.start();
        return tailer;
    }

    public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end) {
        return create(file, listener, delayMillis, end, 4096);
    }

    public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen) {
        return create(file, listener, delayMillis, end, reOpen, 4096);
    }

    public static Tailer create(File file, TailerListener listener, long delayMillis) {
        return create(file, listener, delayMillis, false);
    }

    public static Tailer create(File file, TailerListener listener) {
        return create(file, listener, 1000, false);
    }

    public File getFile() {
        return this.file;
    }

    protected boolean getRun() {
        return this.run;
    }

    public long getDelay() {
        return this.delayMillis;
    }

    public void run() {
        Closeable randomAccessFile;
        InterruptedException e;
        Exception e2;
        long last = 0;
        long position = 0;
        RandomAccessFile reader = null;
        while (getRun() && reader == null) {
            try {
                try {
                    randomAccessFile = new RandomAccessFile(this.file, "r");
                } catch (FileNotFoundException e3) {
                    this.listener.fileNotFound();
                    Object obj = reader;
                }
                if (randomAccessFile == null) {
                    try {
                        Thread.sleep(this.delayMillis);
                        reader = randomAccessFile;
                    } catch (InterruptedException e4) {
                        e = e4;
                    } catch (Exception e5) {
                        e2 = e5;
                    }
                } else {
                    position = this.end ? this.file.length() : 0;
                    last = this.file.lastModified();
                    randomAccessFile.seek(position);
                    reader = randomAccessFile;
                }
            } catch (InterruptedException e6) {
                e = e6;
                randomAccessFile = reader;
            } catch (Exception e7) {
                e2 = e7;
                randomAccessFile = reader;
            } catch (Throwable th) {
                Throwable th2 = th;
                randomAccessFile = reader;
            }
        }
        while (getRun()) {
            boolean newer = FileUtils.isFileNewer(this.file, last);
            long length = this.file.length();
            if (length < position) {
                this.listener.fileRotated();
                Closeable save = reader;
                try {
                    randomAccessFile = new RandomAccessFile(this.file, "r");
                    try {
                        readLines(save);
                    } catch (Exception ioe) {
                        this.listener.handle(ioe);
                    }
                    position = 0;
                    try {
                        IOUtils.closeQuietly(save);
                        reader = randomAccessFile;
                    } catch (FileNotFoundException e8) {
                        this.listener.fileNotFound();
                        reader = randomAccessFile;
                    }
                } catch (FileNotFoundException e9) {
                    obj = reader;
                    this.listener.fileNotFound();
                    reader = randomAccessFile;
                }
            } else {
                RandomAccessFile reader2;
                if (length > position) {
                    position = readLines(reader);
                    last = this.file.lastModified();
                } else if (newer) {
                    reader.seek(0);
                    position = readLines(reader);
                    last = this.file.lastModified();
                }
                if (this.reOpen) {
                    IOUtils.closeQuietly((Closeable) reader);
                }
                Thread.sleep(this.delayMillis);
                if (getRun() && this.reOpen) {
                    randomAccessFile = new RandomAccessFile(this.file, "r");
                    randomAccessFile.seek(position);
                } else {
                    reader2 = reader;
                }
                reader = reader2;
            }
        }
        IOUtils.closeQuietly((Closeable) reader);
        randomAccessFile = reader;
        return;
        try {
            Thread.currentThread().interrupt();
            stop(e);
            IOUtils.closeQuietly(randomAccessFile);
            return;
        } catch (Throwable th3) {
            th2 = th3;
            IOUtils.closeQuietly(randomAccessFile);
            throw th2;
        }
        stop(e2);
        IOUtils.closeQuietly(randomAccessFile);
    }

    private void stop(Exception e) {
        this.listener.handle(e);
        stop();
    }

    public void stop() {
        this.run = false;
    }

    private long readLines(RandomAccessFile reader) throws IOException {
        OutputStream lineBuf = new ByteArrayOutputStream(64);
        long pos = reader.getFilePointer();
        long rePos = pos;
        boolean seenCR = false;
        while (getRun()) {
            int num = reader.read(this.inbuf);
            if (num != -1) {
                for (int i = 0; i < num; i++) {
                    byte ch = this.inbuf[i];
                    switch (ch) {
                        case (byte) 10:
                            seenCR = false;
                            this.listener.handle(new String(lineBuf.toByteArray(), this.cset));
                            lineBuf.reset();
                            rePos = (((long) i) + pos) + 1;
                            break;
                        case (byte) 13:
                            if (seenCR) {
                                lineBuf.write(13);
                            }
                            seenCR = true;
                            break;
                        default:
                            if (seenCR) {
                                seenCR = false;
                                this.listener.handle(new String(lineBuf.toByteArray(), this.cset));
                                lineBuf.reset();
                                rePos = (((long) i) + pos) + 1;
                            }
                            lineBuf.write(ch);
                            break;
                    }
                }
                pos = reader.getFilePointer();
            } else {
                IOUtils.closeQuietly(lineBuf);
                reader.seek(rePos);
                if (this.listener instanceof TailerListenerAdapter) {
                    ((TailerListenerAdapter) this.listener).endOfFileReached();
                }
                return rePos;
            }
        }
        IOUtils.closeQuietly(lineBuf);
        reader.seek(rePos);
        if (this.listener instanceof TailerListenerAdapter) {
            ((TailerListenerAdapter) this.listener).endOfFileReached();
        }
        return rePos;
    }
}
