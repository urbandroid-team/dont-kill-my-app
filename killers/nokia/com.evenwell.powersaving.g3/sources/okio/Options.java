package okio;

import java.util.AbstractList;
import java.util.RandomAccess;

public final class Options extends AbstractList<ByteString> implements RandomAccess {
    final ByteString[] byteStrings;

    private Options(ByteString[] byteStrings) {
        this.byteStrings = byteStrings;
    }

    public static Options of(ByteString... byteStrings) {
        return new Options((ByteString[]) byteStrings.clone());
    }

    public ByteString get(int i) {
        return this.byteStrings[i];
    }

    public int size() {
        return this.byteStrings.length;
    }
}
