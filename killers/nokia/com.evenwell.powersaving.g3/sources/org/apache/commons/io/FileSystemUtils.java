package org.apache.commons.io;

import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class FileSystemUtils {
    private static final String DF;
    private static final int INIT_PROBLEM = -1;
    private static final FileSystemUtils INSTANCE = new FileSystemUtils();
    private static final int OS;
    private static final int OTHER = 0;
    private static final int POSIX_UNIX = 3;
    private static final int UNIX = 2;
    private static final int WINDOWS = 1;

    static {
        String dfPath = "df";
        int os;
        try {
            String osName = System.getProperty("os.name");
            if (osName == null) {
                throw new IOException("os.name not found");
            }
            osName = osName.toLowerCase(Locale.ENGLISH);
            if (osName.contains("windows")) {
                os = 1;
            } else if (osName.contains("linux") || osName.contains("mpe/ix") || osName.contains("freebsd") || osName.contains("irix") || osName.contains("digital unix") || osName.contains("unix") || osName.contains("mac os x")) {
                os = 2;
            } else if (osName.contains("sun os") || osName.contains("sunos") || osName.contains("solaris")) {
                os = 3;
                dfPath = "/usr/xpg4/bin/df";
            } else if (osName.contains("hp-ux") || osName.contains("aix")) {
                os = 3;
            } else {
                os = 0;
            }
            OS = os;
            DF = dfPath;
        } catch (Exception e) {
            os = -1;
        }
    }

    @Deprecated
    public static long freeSpace(String path) throws IOException {
        return INSTANCE.freeSpaceOS(path, OS, false, -1);
    }

    public static long freeSpaceKb(String path) throws IOException {
        return freeSpaceKb(path, -1);
    }

    public static long freeSpaceKb(String path, long timeout) throws IOException {
        return INSTANCE.freeSpaceOS(path, OS, true, timeout);
    }

    public static long freeSpaceKb() throws IOException {
        return freeSpaceKb(-1);
    }

    public static long freeSpaceKb(long timeout) throws IOException {
        return freeSpaceKb(new File(".").getAbsolutePath(), timeout);
    }

    long freeSpaceOS(String path, int os, boolean kb, long timeout) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }
        switch (os) {
            case 0:
                throw new IllegalStateException("Unsupported operating system");
            case 1:
                return kb ? freeSpaceWindows(path, timeout) / 1024 : freeSpaceWindows(path, timeout);
            case 2:
                return freeSpaceUnix(path, kb, false, timeout);
            case 3:
                return freeSpaceUnix(path, kb, true, timeout);
            default:
                throw new IllegalStateException("Exception caught when determining operating system");
        }
    }

    long freeSpaceWindows(String path, long timeout) throws IOException {
        path = FilenameUtils.normalize(path, false);
        if (path.length() > 0 && path.charAt(0) != '\"') {
            path = "\"" + path + "\"";
        }
        List<String> lines = performCommand(new String[]{"cmd.exe", "/C", "dir /a /-c " + path}, Integer.MAX_VALUE, timeout);
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = (String) lines.get(i);
            if (line.length() > 0) {
                return parseDir(line, path);
            }
        }
        throw new IOException("Command line 'dir /-c' did not return any info for path '" + path + "'");
    }

    long parseDir(String line, String path) throws IOException {
        int bytesStart = 0;
        int bytesEnd = 0;
        int j = line.length() - 1;
        while (j >= 0) {
            if (Character.isDigit(line.charAt(j))) {
                bytesEnd = j + 1;
                break;
            }
            j--;
        }
        while (j >= 0) {
            char c = line.charAt(j);
            if (!Character.isDigit(c) && c != ',' && c != '.') {
                bytesStart = j + 1;
                break;
            }
            j--;
        }
        if (j < 0) {
            throw new IOException("Command line 'dir /-c' did not return valid info for path '" + path + "'");
        }
        StringBuilder buf = new StringBuilder(line.substring(bytesStart, bytesEnd));
        int k = 0;
        while (k < buf.length()) {
            if (buf.charAt(k) == ',' || buf.charAt(k) == '.') {
                int k2 = k - 1;
                buf.deleteCharAt(k);
                k = k2;
            }
            k++;
        }
        return parseBytes(buf.toString(), path);
    }

    long freeSpaceUnix(String path, boolean kb, boolean posix, long timeout) throws IOException {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Path must not be empty");
        }
        String flags = "-";
        if (kb) {
            flags = flags + "k";
        }
        if (posix) {
            flags = flags + "P";
        }
        List<String> lines = performCommand(flags.length() > 1 ? new String[]{DF, flags, path} : new String[]{DF, path}, 3, timeout);
        if (lines.size() < 2) {
            throw new IOException("Command line '" + DF + "' did not return info as expected " + "for path '" + path + "'- response was " + lines);
        }
        StringTokenizer tok = new StringTokenizer((String) lines.get(1), SYMBOLS.SPACE);
        if (tok.countTokens() >= 4) {
            tok.nextToken();
        } else if (tok.countTokens() != 1 || lines.size() < 3) {
            throw new IOException("Command line '" + DF + "' did not return data as expected " + "for path '" + path + "'- check path is valid");
        } else {
            tok = new StringTokenizer((String) lines.get(2), SYMBOLS.SPACE);
        }
        tok.nextToken();
        tok.nextToken();
        return parseBytes(tok.nextToken(), path);
    }

    long parseBytes(String freeSpace, String path) throws IOException {
        try {
            long bytes = Long.parseLong(freeSpace);
            if (bytes >= 0) {
                return bytes;
            }
            throw new IOException("Command line '" + DF + "' did not find free space in response " + "for path '" + path + "'- check path is valid");
        } catch (NumberFormatException ex) {
            throw new IOException("Command line '" + DF + "' did not return numeric data as expected " + "for path '" + path + "'- check path is valid", ex);
        }
    }

    List<String> performCommand(String[] cmdAttribs, int max, long timeout) throws IOException {
        InterruptedException ex;
        Throwable th;
        List<String> lines = new ArrayList(20);
        Process proc = null;
        InputStream in = null;
        OutputStream out = null;
        InputStream err = null;
        Reader reader = null;
        try {
            Thread monitor = ThreadMonitor.start(timeout);
            proc = openProcess(cmdAttribs);
            in = proc.getInputStream();
            out = proc.getOutputStream();
            err = proc.getErrorStream();
            Reader inr = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));
            try {
                for (String line = inr.readLine(); line != null && lines.size() < max; line = inr.readLine()) {
                    lines.add(line.toLowerCase(Locale.ENGLISH).trim());
                }
                proc.waitFor();
                ThreadMonitor.stop(monitor);
                if (proc.exitValue() != 0) {
                    throw new IOException("Command line returned OS error code '" + proc.exitValue() + "' for command " + Arrays.asList(cmdAttribs));
                } else if (lines.isEmpty()) {
                    throw new IOException("Command line did not return any info for command " + Arrays.asList(cmdAttribs));
                } else {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                    IOUtils.closeQuietly(err);
                    IOUtils.closeQuietly(inr);
                    if (proc != null) {
                        proc.destroy();
                    }
                    return lines;
                }
            } catch (InterruptedException e) {
                ex = e;
                reader = inr;
                try {
                    throw new IOException("Command line threw an InterruptedException for command " + Arrays.asList(cmdAttribs) + " timeout=" + timeout, ex);
                } catch (Throwable th2) {
                    th = th2;
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                    IOUtils.closeQuietly(err);
                    IOUtils.closeQuietly(reader);
                    if (proc != null) {
                        proc.destroy();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = inr;
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                IOUtils.closeQuietly(err);
                IOUtils.closeQuietly(reader);
                if (proc != null) {
                    proc.destroy();
                }
                throw th;
            }
        } catch (InterruptedException e2) {
            ex = e2;
            throw new IOException("Command line threw an InterruptedException for command " + Arrays.asList(cmdAttribs) + " timeout=" + timeout, ex);
        }
    }

    Process openProcess(String[] cmdAttribs) throws IOException {
        return Runtime.getRuntime().exec(cmdAttribs);
    }
}
