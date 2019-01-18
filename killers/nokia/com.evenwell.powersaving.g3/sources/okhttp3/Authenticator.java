package okhttp3;

import java.io.IOException;

public interface Authenticator {
    public static final Authenticator NONE = new C00561();

    /* renamed from: okhttp3.Authenticator$1 */
    class C00561 implements Authenticator {
        C00561() {
        }

        public Request authenticate(Route route, Response response) {
            return null;
        }
    }

    Request authenticate(Route route, Response response) throws IOException;
}
