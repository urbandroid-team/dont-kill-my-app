package org2.apache.commons.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

@Deprecated
public class CopyUtils {
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public static void copy(byte[] input, OutputStream output) throws IOException {
        output.write(input);
    }

    public static void copy(byte[] input, Writer output) throws IOException {
        copy(new ByteArrayInputStream(input), output);
    }

    public static void copy(byte[] input, Writer output, String encoding) throws IOException {
        copy(new ByteArrayInputStream(input), output, encoding);
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        while (true) {
            int n = input.read(buffer);
            if (-1 == n) {
                return count;
            }
            output.write(buffer, 0, n);
            count += n;
        }
    }

    public static int copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[4096];
        int count = 0;
        while (true) {
            int n = input.read(buffer);
            if (-1 == n) {
                return count;
            }
            output.write(buffer, 0, n);
            count += n;
        }
    }

    public static void copy(InputStream input, Writer output) throws IOException {
        copy(new InputStreamReader(input), output);
    }

    public static void copy(InputStream input, Writer output, String encoding) throws IOException {
        copy(new InputStreamReader(input, encoding), output);
    }

    public static void copy(Reader input, OutputStream output) throws IOException {
        Writer out = new OutputStreamWriter(output);
        copy(input, out);
        out.flush();
    }

    public static void copy(String input, OutputStream output) throws IOException {
        Reader in = new StringReader(input);
        Writer out = new OutputStreamWriter(output);
        copy(in, out);
        out.flush();
    }

    public static void copy(String input, Writer output) throws IOException {
        output.write(input);
    }
}
