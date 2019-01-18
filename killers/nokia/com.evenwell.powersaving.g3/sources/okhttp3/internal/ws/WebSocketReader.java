package okhttp3.internal.ws;

import android.support.v4.media.TransportMediator;
import java.io.EOFException;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.Timeout;

final class WebSocketReader {
    boolean closed;
    long frameBytesRead;
    final FrameCallback frameCallback;
    long frameLength;
    final boolean isClient;
    boolean isControlFrame;
    boolean isFinalFrame;
    boolean isMasked;
    final byte[] maskBuffer = new byte[8192];
    final byte[] maskKey = new byte[4];
    int opcode;
    final BufferedSource source;

    public interface FrameCallback {
        void onReadClose(int i, String str);

        void onReadMessage(String str) throws IOException;

        void onReadMessage(ByteString byteString) throws IOException;

        void onReadPing(ByteString byteString);

        void onReadPong(ByteString byteString);
    }

    WebSocketReader(boolean isClient, BufferedSource source, FrameCallback frameCallback) {
        if (source == null) {
            throw new NullPointerException("source == null");
        } else if (frameCallback == null) {
            throw new NullPointerException("frameCallback == null");
        } else {
            this.isClient = isClient;
            this.source = source;
            this.frameCallback = frameCallback;
        }
    }

    void processNextFrame() throws IOException {
        readHeader();
        if (this.isControlFrame) {
            readControlFrame();
        } else {
            readMessageFrame();
        }
    }

    private void readHeader() throws IOException {
        Timeout readByte;
        boolean z = true;
        TimeUnit timeUnit = null;
        if (this.closed) {
            throw new IOException("closed");
        }
        boolean z2;
        long timeoutBefore = this.source.timeout().timeoutNanos();
        this.source.timeout().clearTimeout();
        try {
            readByte = this.source.readByte();
            int b0 = readByte & 255;
        } finally {
            Timeout timeout = readByte;
            z = this.source.timeout();
            timeUnit = TimeUnit.NANOSECONDS;
            z.timeout(timeoutBefore, timeUnit);
        }
        if (readByte != null) {
            z2 = z;
        } else {
            z2 = timeUnit;
        }
        this.isFinalFrame = z2;
        if ((b0 & 8) != 0) {
            z2 = z;
        } else {
            z2 = timeUnit;
        }
        this.isControlFrame = z2;
        if (!this.isControlFrame || this.isFinalFrame) {
            boolean reservedFlag1;
            if ((b0 & 64) != 0) {
                reservedFlag1 = z;
            } else {
                reservedFlag1 = timeUnit;
            }
            boolean reservedFlag2;
            if ((b0 & 32) != 0) {
                reservedFlag2 = z;
            } else {
                reservedFlag2 = timeUnit;
            }
            boolean reservedFlag3;
            if ((b0 & 16) != 0) {
                reservedFlag3 = z;
            } else {
                reservedFlag3 = timeUnit;
            }
            if (reservedFlag1 || reservedFlag2 || reservedFlag3) {
                throw new ProtocolException("Reserved flags are unsupported.");
            }
            int b1 = this.source.readByte() & 255;
            if ((b1 & 128) == 0) {
                z = timeUnit;
            }
            this.isMasked = z;
            if (this.isMasked == this.isClient) {
                String str;
                if (this.isClient) {
                    str = "Server-sent frames must not be masked.";
                } else {
                    str = "Client-sent frames must be masked.";
                }
                throw new ProtocolException(str);
            }
            this.frameLength = (long) (b1 & TransportMediator.KEYCODE_MEDIA_PAUSE);
            if (this.frameLength == 126) {
                this.frameLength = ((long) this.source.readShort()) & 65535;
            } else if (this.frameLength == 127) {
                this.frameLength = this.source.readLong();
                if (this.frameLength < 0) {
                    throw new ProtocolException("Frame length 0x" + Long.toHexString(this.frameLength) + " > 0x7FFFFFFFFFFFFFFF");
                }
            }
            this.frameBytesRead = 0;
            if (this.isControlFrame && this.frameLength > 125) {
                throw new ProtocolException("Control frame must be less than 125B.");
            } else if (this.isMasked) {
                this.source.readFully(this.maskKey);
                return;
            } else {
                return;
            }
        }
        throw new ProtocolException("Control frames must be final.");
    }

    private void readControlFrame() throws IOException {
        Buffer buffer = new Buffer();
        if (this.frameBytesRead < this.frameLength) {
            if (this.isClient) {
                this.source.readFully(buffer, this.frameLength);
            } else {
                while (this.frameBytesRead < this.frameLength) {
                    int read = this.source.read(this.maskBuffer, 0, (int) Math.min(this.frameLength - this.frameBytesRead, (long) this.maskBuffer.length));
                    if (read == -1) {
                        throw new EOFException();
                    }
                    WebSocketProtocol.toggleMask(this.maskBuffer, (long) read, this.maskKey, this.frameBytesRead);
                    buffer.write(this.maskBuffer, 0, read);
                    this.frameBytesRead += (long) read;
                }
            }
        }
        switch (this.opcode) {
            case 8:
                int code = 1005;
                String reason = "";
                long bufferSize = buffer.size();
                if (bufferSize == 1) {
                    throw new ProtocolException("Malformed close payload length of 1.");
                }
                if (bufferSize != 0) {
                    code = buffer.readShort();
                    reason = buffer.readUtf8();
                    String codeExceptionMessage = WebSocketProtocol.closeCodeExceptionMessage(code);
                    if (codeExceptionMessage != null) {
                        throw new ProtocolException(codeExceptionMessage);
                    }
                }
                this.frameCallback.onReadClose(code, reason);
                this.closed = true;
                return;
            case 9:
                this.frameCallback.onReadPing(buffer.readByteString());
                return;
            case 10:
                this.frameCallback.onReadPong(buffer.readByteString());
                return;
            default:
                throw new ProtocolException("Unknown control opcode: " + Integer.toHexString(this.opcode));
        }
    }

    private void readMessageFrame() throws IOException {
        int opcode = this.opcode;
        if (opcode == 1 || opcode == 2) {
            Buffer message = new Buffer();
            readMessage(message);
            if (opcode == 1) {
                this.frameCallback.onReadMessage(message.readUtf8());
                return;
            } else {
                this.frameCallback.onReadMessage(message.readByteString());
                return;
            }
        }
        throw new ProtocolException("Unknown opcode: " + Integer.toHexString(opcode));
    }

    void readUntilNonControlFrame() throws IOException {
        while (!this.closed) {
            readHeader();
            if (this.isControlFrame) {
                readControlFrame();
            } else {
                return;
            }
        }
    }

    private void readMessage(Buffer sink) throws IOException {
        while (!this.closed) {
            long read;
            if (this.frameBytesRead == this.frameLength) {
                if (!this.isFinalFrame) {
                    readUntilNonControlFrame();
                    if (this.opcode != 0) {
                        throw new ProtocolException("Expected continuation opcode. Got: " + Integer.toHexString(this.opcode));
                    } else if (this.isFinalFrame && this.frameLength == 0) {
                        return;
                    }
                }
                return;
            }
            long toRead = this.frameLength - this.frameBytesRead;
            if (this.isMasked) {
                read = (long) this.source.read(this.maskBuffer, 0, (int) Math.min(toRead, (long) this.maskBuffer.length));
                if (read == -1) {
                    throw new EOFException();
                }
                WebSocketProtocol.toggleMask(this.maskBuffer, read, this.maskKey, this.frameBytesRead);
                sink.write(this.maskBuffer, 0, (int) read);
            } else {
                read = this.source.read(sink, toRead);
                if (read == -1) {
                    throw new EOFException();
                }
            }
            this.frameBytesRead += read;
        }
        throw new IOException("closed");
    }
}
