package okhttp3.internal.http2;

import java.io.IOException;
import java.util.List;
import okio.BufferedSource;

public interface PushObserver {
    public static final PushObserver CANCEL = new C00941();

    /* renamed from: okhttp3.internal.http2.PushObserver$1 */
    class C00941 implements PushObserver {
        C00941() {
        }

        public boolean onRequest(int streamId, List<Header> list) {
            return true;
        }

        public boolean onHeaders(int streamId, List<Header> list, boolean last) {
            return true;
        }

        public boolean onData(int streamId, BufferedSource source, int byteCount, boolean last) throws IOException {
            source.skip((long) byteCount);
            return true;
        }

        public void onReset(int streamId, ErrorCode errorCode) {
        }
    }

    boolean onData(int i, BufferedSource bufferedSource, int i2, boolean z) throws IOException;

    boolean onHeaders(int i, List<Header> list, boolean z);

    boolean onRequest(int i, List<Header> list);

    void onReset(int i, ErrorCode errorCode);
}
