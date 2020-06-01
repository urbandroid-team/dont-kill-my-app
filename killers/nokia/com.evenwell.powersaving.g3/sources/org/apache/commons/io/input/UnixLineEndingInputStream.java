package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

public class UnixLineEndingInputStream extends InputStream {
    private final boolean ensureLineFeedAtEndOfFile;
    private boolean eofSeen = false;
    private boolean slashNSeen = false;
    private boolean slashRSeen = false;
    private final InputStream target;

    public UnixLineEndingInputStream(InputStream in, boolean ensureLineFeedAtEndOfFile) {
        this.target = in;
        this.ensureLineFeedAtEndOfFile = ensureLineFeedAtEndOfFile;
    }

    private int readWithUpdate() throws IOException {
        boolean z = true;
        int target = this.target.read();
        this.eofSeen = target == -1;
        if (!this.eofSeen) {
            boolean z2;
            if (target == 10) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.slashNSeen = z2;
            if (target != 13) {
                z = false;
            }
            this.slashRSeen = z;
        }
        return target;
    }

    public int read() throws IOException {
        boolean previousWasSlashR = this.slashRSeen;
        if (this.eofSeen) {
            return eofGame(previousWasSlashR);
        }
        int target = readWithUpdate();
        if (this.eofSeen) {
            return eofGame(previousWasSlashR);
        }
        if (this.slashRSeen) {
            return 10;
        }
        if (previousWasSlashR && this.slashNSeen) {
            return read();
        }
        return target;
    }

    private int eofGame(boolean previousWasSlashR) {
        if (previousWasSlashR || !this.ensureLineFeedAtEndOfFile || this.slashNSeen) {
            return -1;
        }
        this.slashNSeen = true;
        return 10;
    }

    public void close() throws IOException {
        super.close();
        this.target.close();
    }

    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException("Mark notsupported");
    }
}
