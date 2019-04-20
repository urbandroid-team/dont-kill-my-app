package okhttp3.internal.ws;

import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import java.io.Closeable;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.internal.Internal;
import okhttp3.internal.Util;
import okhttp3.internal.connection.StreamAllocation;
import okhttp3.internal.ws.WebSocketReader.FrameCallback;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;

public final class RealWebSocket implements WebSocket, FrameCallback {
    static final /* synthetic */ boolean $assertionsDisabled = (!RealWebSocket.class.desiredAssertionStatus());
    private static final long CANCEL_AFTER_CLOSE_MILLIS = 60000;
    private static final long MAX_QUEUE_SIZE = 16777216;
    private static final List<Protocol> ONLY_HTTP1 = Collections.singletonList(Protocol.HTTP_1_1);
    private Call call;
    private ScheduledFuture<?> cancelFuture;
    private boolean enqueuedClose;
    private ScheduledExecutorService executor;
    private boolean failed;
    private final String key;
    final WebSocketListener listener;
    private final ArrayDeque<Object> messageAndCloseQueue = new ArrayDeque();
    private final Request originalRequest;
    int pingCount;
    int pongCount;
    private final ArrayDeque<ByteString> pongQueue = new ArrayDeque();
    private long queueSize;
    private final Random random;
    private WebSocketReader reader;
    private int receivedCloseCode = -1;
    private String receivedCloseReason;
    private Streams streams;
    private WebSocketWriter writer;
    private final Runnable writerRunnable;

    public static abstract class Streams implements Closeable {
        public final boolean client;
        public final BufferedSink sink;
        public final BufferedSource source;

        public Streams(boolean client, BufferedSource source, BufferedSink sink) {
            this.client = client;
            this.source = source;
            this.sink = sink;
        }
    }

    /* renamed from: okhttp3.internal.ws.RealWebSocket$1 */
    class C00961 implements Runnable {
        C00961() {
        }

        public void run() {
            do {
                try {
                } catch (IOException e) {
                    RealWebSocket.this.failWebSocket(e, null);
                    return;
                }
            } while (RealWebSocket.this.writeOneFrame());
        }
    }

    final class CancelRunnable implements Runnable {
        CancelRunnable() {
        }

        public void run() {
            RealWebSocket.this.cancel();
        }
    }

    static final class Close {
        final long cancelAfterCloseMillis;
        final int code;
        final ByteString reason;

        Close(int code, ByteString reason, long cancelAfterCloseMillis) {
            this.code = code;
            this.reason = reason;
            this.cancelAfterCloseMillis = cancelAfterCloseMillis;
        }
    }

    static final class Message {
        final ByteString data;
        final int formatOpcode;

        Message(int formatOpcode, ByteString data) {
            this.formatOpcode = formatOpcode;
            this.data = data;
        }
    }

    private final class PingRunnable implements Runnable {
        PingRunnable() {
        }

        public void run() {
            RealWebSocket.this.writePingFrame();
        }
    }

    public RealWebSocket(Request request, WebSocketListener listener, Random random) {
        if ("GET".equals(request.method())) {
            this.originalRequest = request;
            this.listener = listener;
            this.random = random;
            byte[] nonce = new byte[16];
            random.nextBytes(nonce);
            this.key = ByteString.of(nonce).base64();
            this.writerRunnable = new C00961();
            return;
        }
        throw new IllegalArgumentException("Request must be GET: " + request.method());
    }

    public Request request() {
        return this.originalRequest;
    }

    public synchronized long queueSize() {
        return this.queueSize;
    }

    public void cancel() {
        this.call.cancel();
    }

    public void connect(OkHttpClient client) {
        client = client.newBuilder().protocols(ONLY_HTTP1).build();
        final int pingIntervalMillis = client.pingIntervalMillis();
        final Request request = this.originalRequest.newBuilder().header("Upgrade", "websocket").header("Connection", "Upgrade").header("Sec-WebSocket-Key", this.key).header("Sec-WebSocket-Version", "13").build();
        this.call = Internal.instance.newWebSocketCall(client, request);
        this.call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) {
                try {
                    RealWebSocket.this.checkResponse(response);
                    StreamAllocation streamAllocation = Internal.instance.streamAllocation(call);
                    streamAllocation.noNewStreams();
                    Streams streams = streamAllocation.connection().newWebSocketStreams(streamAllocation);
                    try {
                        RealWebSocket.this.listener.onOpen(RealWebSocket.this, response);
                        RealWebSocket.this.initReaderAndWriter("OkHttp WebSocket " + request.url().redact(), (long) pingIntervalMillis, streams);
                        streamAllocation.connection().socket().setSoTimeout(0);
                        RealWebSocket.this.loopReader();
                    } catch (Exception e) {
                        RealWebSocket.this.failWebSocket(e, null);
                    }
                } catch (ProtocolException e2) {
                    RealWebSocket.this.failWebSocket(e2, response);
                    Util.closeQuietly((Closeable) response);
                }
            }

            public void onFailure(Call call, IOException e) {
                RealWebSocket.this.failWebSocket(e, null);
            }
        });
    }

    void checkResponse(Response response) throws ProtocolException {
        if (response.code() != 101) {
            throw new ProtocolException("Expected HTTP 101 response but was '" + response.code() + SYMBOLS.SPACE + response.message() + "'");
        }
        String headerConnection = response.header("Connection");
        if ("Upgrade".equalsIgnoreCase(headerConnection)) {
            String headerUpgrade = response.header("Upgrade");
            if ("websocket".equalsIgnoreCase(headerUpgrade)) {
                String headerAccept = response.header("Sec-WebSocket-Accept");
                String acceptExpected = ByteString.encodeUtf8(this.key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").sha1().base64();
                if (!acceptExpected.equals(headerAccept)) {
                    throw new ProtocolException("Expected 'Sec-WebSocket-Accept' header value '" + acceptExpected + "' but was '" + headerAccept + "'");
                }
                return;
            }
            throw new ProtocolException("Expected 'Upgrade' header value 'websocket' but was '" + headerUpgrade + "'");
        }
        throw new ProtocolException("Expected 'Connection' header value 'Upgrade' but was '" + headerConnection + "'");
    }

    public void initReaderAndWriter(String name, long pingIntervalMillis, Streams streams) throws IOException {
        synchronized (this) {
            this.streams = streams;
            this.writer = new WebSocketWriter(streams.client, streams.sink, this.random);
            this.executor = new ScheduledThreadPoolExecutor(1, Util.threadFactory(name, false));
            if (pingIntervalMillis != 0) {
                this.executor.scheduleAtFixedRate(new PingRunnable(), pingIntervalMillis, pingIntervalMillis, TimeUnit.MILLISECONDS);
            }
            if (!this.messageAndCloseQueue.isEmpty()) {
                runWriter();
            }
        }
        this.reader = new WebSocketReader(streams.client, streams.source, this);
    }

    public void loopReader() throws IOException {
        while (this.receivedCloseCode == -1) {
            this.reader.processNextFrame();
        }
    }

    boolean processNextFrame() throws IOException {
        try {
            this.reader.processNextFrame();
            if (this.receivedCloseCode == -1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            failWebSocket(e, null);
            return false;
        }
    }

    void awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException {
        this.executor.awaitTermination((long) timeout, timeUnit);
    }

    void tearDown() throws InterruptedException {
        if (this.cancelFuture != null) {
            this.cancelFuture.cancel(false);
        }
        this.executor.shutdown();
        this.executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    synchronized int pingCount() {
        return this.pingCount;
    }

    synchronized int pongCount() {
        return this.pongCount;
    }

    public void onReadMessage(String text) throws IOException {
        this.listener.onMessage((WebSocket) this, text);
    }

    public void onReadMessage(ByteString bytes) throws IOException {
        this.listener.onMessage((WebSocket) this, bytes);
    }

    public synchronized void onReadPing(ByteString payload) {
        if (!(this.failed || (this.enqueuedClose && this.messageAndCloseQueue.isEmpty()))) {
            this.pongQueue.add(payload);
            runWriter();
            this.pingCount++;
        }
    }

    public synchronized void onReadPong(ByteString buffer) {
        this.pongCount++;
    }

    public void onReadClose(int code, String reason) {
        if (code == -1) {
            throw new IllegalArgumentException();
        }
        Closeable toClose = null;
        synchronized (this) {
            if (this.receivedCloseCode != -1) {
                throw new IllegalStateException("already closed");
            }
            this.receivedCloseCode = code;
            this.receivedCloseReason = reason;
            if (this.enqueuedClose && this.messageAndCloseQueue.isEmpty()) {
                toClose = this.streams;
                this.streams = null;
                if (this.cancelFuture != null) {
                    this.cancelFuture.cancel(false);
                }
                this.executor.shutdown();
            }
        }
        try {
            this.listener.onClosing(this, code, reason);
            if (toClose != null) {
                this.listener.onClosed(this, code, reason);
            }
            Util.closeQuietly(toClose);
        } catch (Throwable th) {
            Util.closeQuietly(toClose);
        }
    }

    public boolean send(String text) {
        if (text != null) {
            return send(ByteString.encodeUtf8(text), 1);
        }
        throw new NullPointerException("text == null");
    }

    public boolean send(ByteString bytes) {
        if (bytes != null) {
            return send(bytes, 2);
        }
        throw new NullPointerException("bytes == null");
    }

    private synchronized boolean send(ByteString data, int formatOpcode) {
        boolean z = false;
        synchronized (this) {
            if (!(this.failed || this.enqueuedClose)) {
                if (this.queueSize + ((long) data.size()) > MAX_QUEUE_SIZE) {
                    close(1001, null);
                } else {
                    this.queueSize += (long) data.size();
                    this.messageAndCloseQueue.add(new Message(formatOpcode, data));
                    runWriter();
                    z = true;
                }
            }
        }
        return z;
    }

    synchronized boolean pong(ByteString payload) {
        boolean z;
        if (this.failed || (this.enqueuedClose && this.messageAndCloseQueue.isEmpty())) {
            z = false;
        } else {
            this.pongQueue.add(payload);
            runWriter();
            z = true;
        }
        return z;
    }

    public boolean close(int code, String reason) {
        return close(code, reason, CANCEL_AFTER_CLOSE_MILLIS);
    }

    synchronized boolean close(int code, String reason, long cancelAfterCloseMillis) {
        boolean z = true;
        synchronized (this) {
            WebSocketProtocol.validateCloseCode(code);
            ByteString reasonBytes = null;
            if (reason != null) {
                reasonBytes = ByteString.encodeUtf8(reason);
                if (((long) reasonBytes.size()) > 123) {
                    throw new IllegalArgumentException("reason.size() > 123: " + reason);
                }
            }
            if (this.failed || this.enqueuedClose) {
                z = false;
            } else {
                this.enqueuedClose = true;
                this.messageAndCloseQueue.add(new Close(code, reasonBytes, cancelAfterCloseMillis));
                runWriter();
            }
        }
        return z;
    }

    private void runWriter() {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.executor != null) {
            this.executor.execute(this.writerRunnable);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean writeOneFrame() throws java.io.IOException {
        /*
        r16 = this;
        r11 = 0;
        r4 = 0;
        r6 = -1;
        r7 = 0;
        r9 = 0;
        monitor-enter(r16);
        r0 = r16;
        r12 = r0.failed;	 Catch:{ all -> 0x006c }
        if (r12 == 0) goto L_0x000e;
    L_0x000c:
        monitor-exit(r16);	 Catch:{ all -> 0x006c }
    L_0x000d:
        return r11;
    L_0x000e:
        r0 = r16;
        r10 = r0.writer;	 Catch:{ all -> 0x006c }
        r0 = r16;
        r12 = r0.pongQueue;	 Catch:{ all -> 0x006c }
        r5 = r12.poll();	 Catch:{ all -> 0x006c }
        r5 = (okio.ByteString) r5;	 Catch:{ all -> 0x006c }
        if (r5 != 0) goto L_0x0045;
    L_0x001e:
        r0 = r16;
        r12 = r0.messageAndCloseQueue;	 Catch:{ all -> 0x006c }
        r4 = r12.poll();	 Catch:{ all -> 0x006c }
        r12 = r4 instanceof okhttp3.internal.ws.RealWebSocket.Close;	 Catch:{ all -> 0x006c }
        if (r12 == 0) goto L_0x006f;
    L_0x002a:
        r0 = r16;
        r6 = r0.receivedCloseCode;	 Catch:{ all -> 0x006c }
        r0 = r16;
        r7 = r0.receivedCloseReason;	 Catch:{ all -> 0x006c }
        r11 = -1;
        if (r6 == r11) goto L_0x0050;
    L_0x0035:
        r0 = r16;
        r9 = r0.streams;	 Catch:{ all -> 0x006c }
        r11 = 0;
        r0 = r16;
        r0.streams = r11;	 Catch:{ all -> 0x006c }
        r0 = r16;
        r11 = r0.executor;	 Catch:{ all -> 0x006c }
        r11.shutdown();	 Catch:{ all -> 0x006c }
    L_0x0045:
        monitor-exit(r16);	 Catch:{ all -> 0x006c }
        if (r5 == 0) goto L_0x0073;
    L_0x0048:
        r10.writePong(r5);	 Catch:{ all -> 0x00a8 }
    L_0x004b:
        r11 = 1;
        okhttp3.internal.Util.closeQuietly(r9);
        goto L_0x000d;
    L_0x0050:
        r0 = r16;
        r12 = r0.executor;	 Catch:{ all -> 0x006c }
        r13 = new okhttp3.internal.ws.RealWebSocket$CancelRunnable;	 Catch:{ all -> 0x006c }
        r0 = r16;
        r13.<init>();	 Catch:{ all -> 0x006c }
        r0 = r4;
        r0 = (okhttp3.internal.ws.RealWebSocket.Close) r0;	 Catch:{ all -> 0x006c }
        r11 = r0;
        r14 = r11.cancelAfterCloseMillis;	 Catch:{ all -> 0x006c }
        r11 = java.util.concurrent.TimeUnit.MILLISECONDS;	 Catch:{ all -> 0x006c }
        r11 = r12.schedule(r13, r14, r11);	 Catch:{ all -> 0x006c }
        r0 = r16;
        r0.cancelFuture = r11;	 Catch:{ all -> 0x006c }
        goto L_0x0045;
    L_0x006c:
        r11 = move-exception;
        monitor-exit(r16);	 Catch:{ all -> 0x006c }
        throw r11;
    L_0x006f:
        if (r4 != 0) goto L_0x0045;
    L_0x0071:
        monitor-exit(r16);	 Catch:{ all -> 0x006c }
        goto L_0x000d;
    L_0x0073:
        r11 = r4 instanceof okhttp3.internal.ws.RealWebSocket.Message;	 Catch:{ all -> 0x00a8 }
        if (r11 == 0) goto L_0x00ad;
    L_0x0077:
        r0 = r4;
        r0 = (okhttp3.internal.ws.RealWebSocket.Message) r0;	 Catch:{ all -> 0x00a8 }
        r11 = r0;
        r3 = r11.data;	 Catch:{ all -> 0x00a8 }
        r4 = (okhttp3.internal.ws.RealWebSocket.Message) r4;	 Catch:{ all -> 0x00a8 }
        r11 = r4.formatOpcode;	 Catch:{ all -> 0x00a8 }
        r12 = r3.size();	 Catch:{ all -> 0x00a8 }
        r12 = (long) r12;	 Catch:{ all -> 0x00a8 }
        r11 = r10.newMessageSink(r11, r12);	 Catch:{ all -> 0x00a8 }
        r8 = okio.Okio.buffer(r11);	 Catch:{ all -> 0x00a8 }
        r8.write(r3);	 Catch:{ all -> 0x00a8 }
        r8.close();	 Catch:{ all -> 0x00a8 }
        monitor-enter(r16);	 Catch:{ all -> 0x00a8 }
        r0 = r16;
        r12 = r0.queueSize;	 Catch:{ all -> 0x00a5 }
        r11 = r3.size();	 Catch:{ all -> 0x00a5 }
        r14 = (long) r11;	 Catch:{ all -> 0x00a5 }
        r12 = r12 - r14;
        r0 = r16;
        r0.queueSize = r12;	 Catch:{ all -> 0x00a5 }
        monitor-exit(r16);	 Catch:{ all -> 0x00a5 }
        goto L_0x004b;
    L_0x00a5:
        r11 = move-exception;
        monitor-exit(r16);	 Catch:{ all -> 0x00a5 }
        throw r11;	 Catch:{ all -> 0x00a8 }
    L_0x00a8:
        r11 = move-exception;
        okhttp3.internal.Util.closeQuietly(r9);
        throw r11;
    L_0x00ad:
        r11 = r4 instanceof okhttp3.internal.ws.RealWebSocket.Close;	 Catch:{ all -> 0x00a8 }
        if (r11 == 0) goto L_0x00c8;
    L_0x00b1:
        r0 = r4;
        r0 = (okhttp3.internal.ws.RealWebSocket.Close) r0;	 Catch:{ all -> 0x00a8 }
        r2 = r0;
        r11 = r2.code;	 Catch:{ all -> 0x00a8 }
        r12 = r2.reason;	 Catch:{ all -> 0x00a8 }
        r10.writeClose(r11, r12);	 Catch:{ all -> 0x00a8 }
        if (r9 == 0) goto L_0x004b;
    L_0x00be:
        r0 = r16;
        r11 = r0.listener;	 Catch:{ all -> 0x00a8 }
        r0 = r16;
        r11.onClosed(r0, r6, r7);	 Catch:{ all -> 0x00a8 }
        goto L_0x004b;
    L_0x00c8:
        r11 = new java.lang.AssertionError;	 Catch:{ all -> 0x00a8 }
        r11.<init>();	 Catch:{ all -> 0x00a8 }
        throw r11;	 Catch:{ all -> 0x00a8 }
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.ws.RealWebSocket.writeOneFrame():boolean");
    }

    void writePingFrame() {
        synchronized (this) {
            if (this.failed) {
                return;
            }
            WebSocketWriter writer = this.writer;
            try {
                writer.writePing(ByteString.EMPTY);
            } catch (IOException e) {
                failWebSocket(e, null);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void failWebSocket(java.lang.Exception r4, okhttp3.Response r5) {
        /*
        r3 = this;
        monitor-enter(r3);
        r1 = r3.failed;	 Catch:{ all -> 0x002c }
        if (r1 == 0) goto L_0x0007;
    L_0x0005:
        monitor-exit(r3);	 Catch:{ all -> 0x002c }
    L_0x0006:
        return;
    L_0x0007:
        r1 = 1;
        r3.failed = r1;	 Catch:{ all -> 0x002c }
        r0 = r3.streams;	 Catch:{ all -> 0x002c }
        r1 = 0;
        r3.streams = r1;	 Catch:{ all -> 0x002c }
        r1 = r3.cancelFuture;	 Catch:{ all -> 0x002c }
        if (r1 == 0) goto L_0x0019;
    L_0x0013:
        r1 = r3.cancelFuture;	 Catch:{ all -> 0x002c }
        r2 = 0;
        r1.cancel(r2);	 Catch:{ all -> 0x002c }
    L_0x0019:
        r1 = r3.executor;	 Catch:{ all -> 0x002c }
        if (r1 == 0) goto L_0x0022;
    L_0x001d:
        r1 = r3.executor;	 Catch:{ all -> 0x002c }
        r1.shutdown();	 Catch:{ all -> 0x002c }
    L_0x0022:
        monitor-exit(r3);	 Catch:{ all -> 0x002c }
        r1 = r3.listener;	 Catch:{ all -> 0x002f }
        r1.onFailure(r3, r4, r5);	 Catch:{ all -> 0x002f }
        okhttp3.internal.Util.closeQuietly(r0);
        goto L_0x0006;
    L_0x002c:
        r1 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x002c }
        throw r1;
    L_0x002f:
        r1 = move-exception;
        okhttp3.internal.Util.closeQuietly(r0);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.ws.RealWebSocket.failWebSocket(java.lang.Exception, okhttp3.Response):void");
    }
}
