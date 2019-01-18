package okhttp3;

import java.nio.charset.Charset;
import okio.ByteString;

public final class Credentials {
    private Credentials() {
    }

    public static String basic(String userName, String password) {
        return basic(userName, password, Charset.forName("ISO-8859-1"));
    }

    public static String basic(String userName, String password, Charset charset) {
        return "Basic " + ByteString.of((userName + ":" + password).getBytes(charset)).base64();
    }
}
