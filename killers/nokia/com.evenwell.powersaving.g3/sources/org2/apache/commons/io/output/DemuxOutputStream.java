package org2.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;

public class DemuxOutputStream extends OutputStream {
    private final InheritableThreadLocal<OutputStream> m_streams = new InheritableThreadLocal();

    public OutputStream bindStream(OutputStream output) {
        OutputStream stream = (OutputStream) this.m_streams.get();
        this.m_streams.set(output);
        return stream;
    }

    public void close() throws IOException {
        OutputStream output = (OutputStream) this.m_streams.get();
        if (output != null) {
            output.close();
        }
    }

    public void flush() throws IOException {
        OutputStream output = (OutputStream) this.m_streams.get();
        if (output != null) {
            output.flush();
        }
    }

    public void write(int ch) throws IOException {
        OutputStream output = (OutputStream) this.m_streams.get();
        if (output != null) {
            output.write(ch);
        }
    }
}
