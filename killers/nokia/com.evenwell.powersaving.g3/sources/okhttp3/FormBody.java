package okhttp3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.BufferedSink;

public final class FormBody extends RequestBody {
    private static final MediaType CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded");
    private final List<String> encodedNames;
    private final List<String> encodedValues;

    public static final class Builder {
        private final List<String> names = new ArrayList();
        private final List<String> values = new ArrayList();

        public Builder add(String name, String value) {
            this.names.add(HttpUrl.canonicalize(name, " \"':;<=>@[]^`{}|/\\?#&!$(),~", false, false, true, true));
            this.values.add(HttpUrl.canonicalize(value, " \"':;<=>@[]^`{}|/\\?#&!$(),~", false, false, true, true));
            return this;
        }

        public Builder addEncoded(String name, String value) {
            this.names.add(HttpUrl.canonicalize(name, " \"':;<=>@[]^`{}|/\\?#&!$(),~", true, false, true, true));
            this.values.add(HttpUrl.canonicalize(value, " \"':;<=>@[]^`{}|/\\?#&!$(),~", true, false, true, true));
            return this;
        }

        public FormBody build() {
            return new FormBody(this.names, this.values);
        }
    }

    FormBody(List<String> encodedNames, List<String> encodedValues) {
        this.encodedNames = Util.immutableList((List) encodedNames);
        this.encodedValues = Util.immutableList((List) encodedValues);
    }

    public int size() {
        return this.encodedNames.size();
    }

    public String encodedName(int index) {
        return (String) this.encodedNames.get(index);
    }

    public String name(int index) {
        return HttpUrl.percentDecode(encodedName(index), true);
    }

    public String encodedValue(int index) {
        return (String) this.encodedValues.get(index);
    }

    public String value(int index) {
        return HttpUrl.percentDecode(encodedValue(index), true);
    }

    public MediaType contentType() {
        return CONTENT_TYPE;
    }

    public long contentLength() {
        return writeOrCountBytes(null, true);
    }

    public void writeTo(BufferedSink sink) throws IOException {
        writeOrCountBytes(sink, false);
    }

    private long writeOrCountBytes(BufferedSink sink, boolean countBytes) {
        Buffer buffer;
        if (countBytes) {
            buffer = new Buffer();
        } else {
            buffer = sink.buffer();
        }
        int size = this.encodedNames.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                buffer.writeByte(38);
            }
            buffer.writeUtf8((String) this.encodedNames.get(i));
            buffer.writeByte(61);
            buffer.writeUtf8((String) this.encodedValues.get(i));
        }
        if (!countBytes) {
            return 0;
        }
        long byteCount = buffer.size();
        buffer.clear();
        return byteCount;
    }
}
