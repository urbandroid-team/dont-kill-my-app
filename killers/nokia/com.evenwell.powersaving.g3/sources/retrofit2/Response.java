package retrofit2;

import com.evenwell.powersaving.g3.pushservice.PushServiceUtils.RESPONSE_CODE;
import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response.Builder;
import okhttp3.ResponseBody;

public final class Response<T> {
    private final T body;
    private final ResponseBody errorBody;
    private final okhttp3.Response rawResponse;

    public static <T> Response<T> success(T body) {
        return success((Object) body, new Builder().code(RESPONSE_CODE.success).message("OK").protocol(Protocol.HTTP_1_1).request(new Request.Builder().url("http://localhost/").build()).build());
    }

    public static <T> Response<T> success(T body, Headers headers) {
        if (headers != null) {
            return success((Object) body, new Builder().code(RESPONSE_CODE.success).message("OK").protocol(Protocol.HTTP_1_1).headers(headers).request(new Request.Builder().url("http://localhost/").build()).build());
        }
        throw new NullPointerException("headers == null");
    }

    public static <T> Response<T> success(T body, okhttp3.Response rawResponse) {
        if (rawResponse == null) {
            throw new NullPointerException("rawResponse == null");
        } else if (rawResponse.isSuccessful()) {
            return new Response(rawResponse, body, null);
        } else {
            throw new IllegalArgumentException("rawResponse must be successful response");
        }
    }

    public static <T> Response<T> error(int code, ResponseBody body) {
        if (code >= RESPONSE_CODE.invalid_request) {
            return error(body, new Builder().code(code).protocol(Protocol.HTTP_1_1).request(new Request.Builder().url("http://localhost/").build()).build());
        }
        throw new IllegalArgumentException("code < 400: " + code);
    }

    public static <T> Response<T> error(ResponseBody body, okhttp3.Response rawResponse) {
        if (body == null) {
            throw new NullPointerException("body == null");
        } else if (rawResponse == null) {
            throw new NullPointerException("rawResponse == null");
        } else if (!rawResponse.isSuccessful()) {
            return new Response(rawResponse, null, body);
        } else {
            throw new IllegalArgumentException("rawResponse should not be successful response");
        }
    }

    private Response(okhttp3.Response rawResponse, T body, ResponseBody errorBody) {
        this.rawResponse = rawResponse;
        this.body = body;
        this.errorBody = errorBody;
    }

    public okhttp3.Response raw() {
        return this.rawResponse;
    }

    public int code() {
        return this.rawResponse.code();
    }

    public String message() {
        return this.rawResponse.message();
    }

    public Headers headers() {
        return this.rawResponse.headers();
    }

    public boolean isSuccessful() {
        return this.rawResponse.isSuccessful();
    }

    public T body() {
        return this.body;
    }

    public ResponseBody errorBody() {
        return this.errorBody;
    }

    public String toString() {
        return this.rawResponse.toString();
    }
}
