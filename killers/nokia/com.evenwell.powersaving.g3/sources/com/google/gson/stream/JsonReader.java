package com.google.gson.stream;

import android.support.v4.view.MotionEventCompat;
import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.internal.bind.JsonTreeReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import org.apache.commons.io.ByteOrderMark;

public class JsonReader implements Closeable {
    private static final long MIN_INCOMPLETE_INTEGER = -922337203685477580L;
    private static final char[] NON_EXECUTE_PREFIX = ")]}'\n".toCharArray();
    private static final int NUMBER_CHAR_DECIMAL = 3;
    private static final int NUMBER_CHAR_DIGIT = 2;
    private static final int NUMBER_CHAR_EXP_DIGIT = 7;
    private static final int NUMBER_CHAR_EXP_E = 5;
    private static final int NUMBER_CHAR_EXP_SIGN = 6;
    private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
    private static final int NUMBER_CHAR_NONE = 0;
    private static final int NUMBER_CHAR_SIGN = 1;
    private static final int PEEKED_BEGIN_ARRAY = 3;
    private static final int PEEKED_BEGIN_OBJECT = 1;
    private static final int PEEKED_BUFFERED = 11;
    private static final int PEEKED_DOUBLE_QUOTED = 9;
    private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
    private static final int PEEKED_END_ARRAY = 4;
    private static final int PEEKED_END_OBJECT = 2;
    private static final int PEEKED_EOF = 17;
    private static final int PEEKED_FALSE = 6;
    private static final int PEEKED_LONG = 15;
    private static final int PEEKED_NONE = 0;
    private static final int PEEKED_NULL = 7;
    private static final int PEEKED_NUMBER = 16;
    private static final int PEEKED_SINGLE_QUOTED = 8;
    private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
    private static final int PEEKED_TRUE = 5;
    private static final int PEEKED_UNQUOTED = 10;
    private static final int PEEKED_UNQUOTED_NAME = 14;
    private final char[] buffer = new char[1024];
    private final Reader in;
    private boolean lenient = false;
    private int limit = 0;
    private int lineNumber = 0;
    private int lineStart = 0;
    private int[] pathIndices;
    private String[] pathNames;
    int peeked = 0;
    private long peekedLong;
    private int peekedNumberLength;
    private String peekedString;
    private int pos = 0;
    private int[] stack = new int[32];
    private int stackSize = 0;

    /* renamed from: com.google.gson.stream.JsonReader$1 */
    static class C00551 extends JsonReaderInternalAccess {
        C00551() {
        }

        public void promoteNameToValue(JsonReader reader) throws IOException {
            if (reader instanceof JsonTreeReader) {
                ((JsonTreeReader) reader).promoteNameToValue();
                return;
            }
            int p = reader.peeked;
            if (p == 0) {
                p = reader.doPeek();
            }
            if (p == 13) {
                reader.peeked = 9;
            } else if (p == 12) {
                reader.peeked = 8;
            } else if (p == 14) {
                reader.peeked = 10;
            } else {
                throw new IllegalStateException("Expected a name but was " + reader.peek() + SYMBOLS.SPACE + " at line " + reader.getLineNumber() + " column " + reader.getColumnNumber() + " path " + reader.getPath());
            }
        }
    }

    static {
        JsonReaderInternalAccess.INSTANCE = new C00551();
    }

    public JsonReader(Reader in) {
        int[] iArr = this.stack;
        int i = this.stackSize;
        this.stackSize = i + 1;
        iArr[i] = 6;
        this.pathNames = new String[32];
        this.pathIndices = new int[32];
        if (in == null) {
            throw new NullPointerException("in == null");
        }
        this.in = in;
    }

    public final void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public final boolean isLenient() {
        return this.lenient;
    }

    public void beginArray() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 3) {
            push(1);
            this.pathIndices[this.stackSize - 1] = 0;
            this.peeked = 0;
            return;
        }
        throw new IllegalStateException("Expected BEGIN_ARRAY but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
    }

    public void endArray() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 4) {
            this.stackSize--;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            this.peeked = 0;
            return;
        }
        throw new IllegalStateException("Expected END_ARRAY but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
    }

    public void beginObject() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 1) {
            push(3);
            this.peeked = 0;
            return;
        }
        throw new IllegalStateException("Expected BEGIN_OBJECT but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
    }

    public void endObject() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 2) {
            this.stackSize--;
            this.pathNames[this.stackSize] = null;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            this.peeked = 0;
            return;
        }
        throw new IllegalStateException("Expected END_OBJECT but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
    }

    public boolean hasNext() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        return (p == 2 || p == 4) ? false : true;
    }

    public JsonToken peek() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        switch (p) {
            case 1:
                return JsonToken.BEGIN_OBJECT;
            case 2:
                return JsonToken.END_OBJECT;
            case 3:
                return JsonToken.BEGIN_ARRAY;
            case 4:
                return JsonToken.END_ARRAY;
            case 5:
            case 6:
                return JsonToken.BOOLEAN;
            case 7:
                return JsonToken.NULL;
            case 8:
            case 9:
            case 10:
            case 11:
                return JsonToken.STRING;
            case 12:
            case 13:
            case 14:
                return JsonToken.NAME;
            case 15:
            case 16:
                return JsonToken.NUMBER;
            case 17:
                return JsonToken.END_DOCUMENT;
            default:
                throw new AssertionError();
        }
    }

    int doPeek() throws IOException {
        int peekStack = this.stack[this.stackSize - 1];
        if (peekStack == 1) {
            this.stack[this.stackSize - 1] = 2;
        } else if (peekStack == 2) {
            switch (nextNonWhitespace(true)) {
                case MotionEventCompat.AXIS_GENERIC_13 /*44*/:
                    break;
                case 59:
                    checkLenient();
                    break;
                case 93:
                    this.peeked = 4;
                    return 4;
                default:
                    throw syntaxError("Unterminated array");
            }
        } else if (peekStack == 3 || peekStack == 5) {
            this.stack[this.stackSize - 1] = 4;
            if (peekStack == 5) {
                switch (nextNonWhitespace(true)) {
                    case MotionEventCompat.AXIS_GENERIC_13 /*44*/:
                        break;
                    case 59:
                        checkLenient();
                        break;
                    case 125:
                        this.peeked = 2;
                        return 2;
                    default:
                        throw syntaxError("Unterminated object");
                }
            }
            int c = nextNonWhitespace(true);
            switch (c) {
                case 34:
                    this.peeked = 13;
                    return 13;
                case MotionEventCompat.AXIS_GENERIC_8 /*39*/:
                    checkLenient();
                    this.peeked = 12;
                    return 12;
                case 125:
                    if (peekStack != 5) {
                        this.peeked = 2;
                        return 2;
                    }
                    throw syntaxError("Expected name");
                default:
                    checkLenient();
                    this.pos--;
                    if (isLiteral((char) c)) {
                        this.peeked = 14;
                        return 14;
                    }
                    throw syntaxError("Expected name");
            }
        } else if (peekStack == 4) {
            this.stack[this.stackSize - 1] = 5;
            switch (nextNonWhitespace(true)) {
                case 58:
                    break;
                case 61:
                    checkLenient();
                    if ((this.pos < this.limit || fillBuffer(1)) && this.buffer[this.pos] == '>') {
                        this.pos++;
                        break;
                    }
                default:
                    throw syntaxError("Expected ':'");
            }
        } else if (peekStack == 6) {
            if (this.lenient) {
                consumeNonExecutePrefix();
            }
            this.stack[this.stackSize - 1] = 7;
        } else if (peekStack == 7) {
            if (nextNonWhitespace(false) == -1) {
                this.peeked = 17;
                return 17;
            }
            checkLenient();
            this.pos--;
        } else if (peekStack == 8) {
            throw new IllegalStateException("JsonReader is closed");
        }
        switch (nextNonWhitespace(true)) {
            case 34:
                this.peeked = 9;
                return 9;
            case MotionEventCompat.AXIS_GENERIC_8 /*39*/:
                checkLenient();
                this.peeked = 8;
                return 8;
            case MotionEventCompat.AXIS_GENERIC_13 /*44*/:
            case 59:
                break;
            case 91:
                this.peeked = 3;
                return 3;
            case 93:
                if (peekStack == 1) {
                    this.peeked = 4;
                    return 4;
                }
                break;
            case 123:
                this.peeked = 1;
                return 1;
            default:
                this.pos--;
                int result = peekKeyword();
                if (result != 0) {
                    return result;
                }
                result = peekNumber();
                if (result != 0) {
                    return result;
                }
                if (isLiteral(this.buffer[this.pos])) {
                    checkLenient();
                    this.peeked = 10;
                    return 10;
                }
                throw syntaxError("Expected value");
        }
        if (peekStack == 1 || peekStack == 2) {
            checkLenient();
            this.pos--;
            this.peeked = 7;
            return 7;
        }
        throw syntaxError("Unexpected value");
    }

    private int peekKeyword() throws IOException {
        String keyword;
        int peeking;
        char c = this.buffer[this.pos];
        String keywordUpper;
        if (c == 't' || c == 'T') {
            keyword = "true";
            keywordUpper = "TRUE";
            peeking = 5;
        } else if (c == 'f' || c == 'F') {
            keyword = "false";
            keywordUpper = "FALSE";
            peeking = 6;
        } else if (c != 'n' && c != 'N') {
            return 0;
        } else {
            keyword = "null";
            keywordUpper = "NULL";
            peeking = 7;
        }
        int length = keyword.length();
        int i = 1;
        while (i < length) {
            if (this.pos + i >= this.limit && !fillBuffer(i + 1)) {
                return 0;
            }
            c = this.buffer[this.pos + i];
            if (c != keyword.charAt(i) && c != keywordUpper.charAt(i)) {
                return 0;
            }
            i++;
        }
        if ((this.pos + length < this.limit || fillBuffer(length + 1)) && isLiteral(this.buffer[this.pos + length])) {
            return 0;
        }
        this.pos += length;
        this.peeked = peeking;
        return peeking;
    }

    private int peekNumber() throws IOException {
        char[] buffer = this.buffer;
        int p = this.pos;
        int l = this.limit;
        long value = 0;
        boolean negative = false;
        boolean fitsInLong = true;
        int last = 0;
        int i = 0;
        while (true) {
            if (p + i == l) {
                if (i == buffer.length) {
                    return 0;
                }
                if (fillBuffer(i + 1)) {
                    p = this.pos;
                    l = this.limit;
                } else if (last != 2 && fitsInLong && (value != Long.MIN_VALUE || negative)) {
                    if (!negative) {
                        value = -value;
                    }
                    this.peekedLong = value;
                    this.pos += i;
                    this.peeked = 15;
                    return 15;
                } else if (last == 2 && last != 4 && last != 7) {
                    return 0;
                } else {
                    this.peekedNumberLength = i;
                    this.peeked = 16;
                    return 16;
                }
            }
            char c = buffer[p + i];
            switch (c) {
                case MotionEventCompat.AXIS_GENERIC_12 /*43*/:
                    if (last != 5) {
                        return 0;
                    }
                    last = 6;
                    continue;
                case MotionEventCompat.AXIS_GENERIC_14 /*45*/:
                    if (last == 0) {
                        negative = true;
                        last = 1;
                        continue;
                    } else if (last == 5) {
                        last = 6;
                        break;
                    } else {
                        return 0;
                    }
                case '.':
                    if (last != 2) {
                        return 0;
                    }
                    last = 3;
                    continue;
                case 'E':
                case 'e':
                    if (last != 2 && last != 4) {
                        return 0;
                    }
                    last = 5;
                    continue;
                default:
                    if (c >= '0' && c <= '9') {
                        if (last != 1 && last != 0) {
                            if (last != 2) {
                                if (last != 3) {
                                    if (last != 5 && last != 6) {
                                        break;
                                    }
                                    last = 7;
                                    break;
                                }
                                last = 4;
                                break;
                            } else if (value != 0) {
                                long newValue = (10 * value) - ((long) (c - 48));
                                int i2 = (value > MIN_INCOMPLETE_INTEGER || (value == MIN_INCOMPLETE_INTEGER && newValue < value)) ? 1 : 0;
                                fitsInLong &= i2;
                                value = newValue;
                                break;
                            } else {
                                return 0;
                            }
                        }
                        value = (long) (-(c - 48));
                        last = 2;
                        continue;
                    } else if (isLiteral(c)) {
                        return 0;
                    }
                    break;
            }
            if (last != 2) {
            }
            if (last == 2) {
            }
            this.peekedNumberLength = i;
            this.peeked = 16;
            return 16;
            i++;
        }
    }

    private boolean isLiteral(char c) throws IOException {
        switch (c) {
            case '\t':
            case '\n':
            case '\f':
            case '\r':
            case ' ':
            case MotionEventCompat.AXIS_GENERIC_13 /*44*/:
            case ':':
            case '[':
            case ']':
            case '{':
            case '}':
                break;
            case '#':
            case MotionEventCompat.AXIS_GENERIC_16 /*47*/:
            case ';':
            case '=':
            case '\\':
                checkLenient();
                break;
            default:
                return true;
        }
        return false;
    }

    public String nextName() throws IOException {
        String result;
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 14) {
            result = nextUnquotedValue();
        } else if (p == 12) {
            result = nextQuotedValue('\'');
        } else if (p == 13) {
            result = nextQuotedValue('\"');
        } else {
            throw new IllegalStateException("Expected a name but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
        }
        this.peeked = 0;
        this.pathNames[this.stackSize - 1] = result;
        return result;
    }

    public String nextString() throws IOException {
        String result;
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 10) {
            result = nextUnquotedValue();
        } else if (p == 8) {
            result = nextQuotedValue('\'');
        } else if (p == 9) {
            result = nextQuotedValue('\"');
        } else if (p == 11) {
            result = this.peekedString;
            this.peekedString = null;
        } else if (p == 15) {
            result = Long.toString(this.peekedLong);
        } else if (p == 16) {
            result = new String(this.buffer, this.pos, this.peekedNumberLength);
            this.pos += this.peekedNumberLength;
        } else {
            throw new IllegalStateException("Expected a string but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
        }
        this.peeked = 0;
        int[] iArr = this.pathIndices;
        int i = this.stackSize - 1;
        iArr[i] = iArr[i] + 1;
        return result;
    }

    public boolean nextBoolean() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 5) {
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return true;
        } else if (p == 6) {
            this.peeked = 0;
            int[] iArr2 = this.pathIndices;
            int i2 = this.stackSize - 1;
            iArr2[i2] = iArr2[i2] + 1;
            return false;
        } else {
            throw new IllegalStateException("Expected a boolean but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
        }
    }

    public void nextNull() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 7) {
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return;
        }
        throw new IllegalStateException("Expected null but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
    }

    public double nextDouble() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 15) {
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return (double) this.peekedLong;
        }
        if (p == 16) {
            this.peekedString = new String(this.buffer, this.pos, this.peekedNumberLength);
            this.pos += this.peekedNumberLength;
        } else if (p == 8 || p == 9) {
            this.peekedString = nextQuotedValue(p == 8 ? '\'' : '\"');
        } else if (p == 10) {
            this.peekedString = nextUnquotedValue();
        } else if (p != 11) {
            throw new IllegalStateException("Expected a double but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
        }
        this.peeked = 11;
        double result = Double.parseDouble(this.peekedString);
        if (this.lenient || !(Double.isNaN(result) || Double.isInfinite(result))) {
            this.peekedString = null;
            this.peeked = 0;
            iArr = this.pathIndices;
            i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return result;
        }
        throw new MalformedJsonException("JSON forbids NaN and infinities: " + result + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
    }

    public long nextLong() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 15) {
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return this.peekedLong;
        }
        long parseLong;
        if (p == 16) {
            this.peekedString = new String(this.buffer, this.pos, this.peekedNumberLength);
            this.pos += this.peekedNumberLength;
        } else if (p == 8 || p == 9) {
            this.peekedString = nextQuotedValue(p == 8 ? '\'' : '\"');
            try {
                parseLong = Long.parseLong(this.peekedString);
                this.peeked = 0;
                iArr = this.pathIndices;
                i = this.stackSize - 1;
                iArr[i] = iArr[i] + 1;
                return parseLong;
            } catch (NumberFormatException e) {
            }
        } else {
            throw new IllegalStateException("Expected a long but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
        }
        this.peeked = 11;
        double asDouble = Double.parseDouble(this.peekedString);
        parseLong = (long) asDouble;
        if (((double) parseLong) != asDouble) {
            throw new NumberFormatException("Expected a long but was " + this.peekedString + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
        }
        this.peekedString = null;
        this.peeked = 0;
        iArr = this.pathIndices;
        i = this.stackSize - 1;
        iArr[i] = iArr[i] + 1;
        return parseLong;
    }

    private String nextQuotedValue(char quote) throws IOException {
        char[] buffer = this.buffer;
        StringBuilder builder = new StringBuilder();
        do {
            int p = this.pos;
            int l = this.limit;
            int start = p;
            int p2 = p;
            while (p2 < l) {
                p = p2 + 1;
                char c = buffer[p2];
                if (c == quote) {
                    this.pos = p;
                    builder.append(buffer, start, (p - start) - 1);
                    return builder.toString();
                }
                if (c == '\\') {
                    this.pos = p;
                    builder.append(buffer, start, (p - start) - 1);
                    builder.append(readEscapeCharacter());
                    p = this.pos;
                    l = this.limit;
                    start = p;
                } else if (c == '\n') {
                    this.lineNumber++;
                    this.lineStart = p;
                }
                p2 = p;
            }
            builder.append(buffer, start, p2 - start);
            this.pos = p2;
        } while (fillBuffer(1));
        throw syntaxError("Unterminated string");
    }

    private String nextUnquotedValue() throws IOException {
        StringBuilder builder = null;
        int i = 0;
        while (true) {
            String result;
            if (this.pos + i < this.limit) {
                switch (this.buffer[this.pos + i]) {
                    case '\t':
                    case '\n':
                    case '\f':
                    case '\r':
                    case ' ':
                    case MotionEventCompat.AXIS_GENERIC_13 /*44*/:
                    case ':':
                    case '[':
                    case ']':
                    case '{':
                    case '}':
                        break;
                    case '#':
                    case MotionEventCompat.AXIS_GENERIC_16 /*47*/:
                    case ';':
                    case '=':
                    case '\\':
                        checkLenient();
                        break;
                    default:
                        i++;
                        continue;
                }
            } else if (i >= this.buffer.length) {
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(this.buffer, this.pos, i);
                this.pos += i;
                i = 0;
                if (fillBuffer(1)) {
                }
            } else if (fillBuffer(i + 1)) {
            }
            if (builder == null) {
                result = new String(this.buffer, this.pos, i);
            } else {
                builder.append(this.buffer, this.pos, i);
                result = builder.toString();
            }
            this.pos += i;
            return result;
        }
    }

    private void skipQuotedValue(char quote) throws IOException {
        char[] buffer = this.buffer;
        do {
            int p = this.pos;
            int l = this.limit;
            int p2 = p;
            while (p2 < l) {
                p = p2 + 1;
                char c = buffer[p2];
                if (c == quote) {
                    this.pos = p;
                    return;
                }
                if (c == '\\') {
                    this.pos = p;
                    readEscapeCharacter();
                    p = this.pos;
                    l = this.limit;
                } else if (c == '\n') {
                    this.lineNumber++;
                    this.lineStart = p;
                }
                p2 = p;
            }
            this.pos = p2;
        } while (fillBuffer(1));
        throw syntaxError("Unterminated string");
    }

    private void skipUnquotedValue() throws IOException {
        do {
            int i = 0;
            while (this.pos + i < this.limit) {
                switch (this.buffer[this.pos + i]) {
                    case '\t':
                    case '\n':
                    case '\f':
                    case '\r':
                    case ' ':
                    case MotionEventCompat.AXIS_GENERIC_13 /*44*/:
                    case ':':
                    case '[':
                    case ']':
                    case '{':
                    case '}':
                        break;
                    case '#':
                    case MotionEventCompat.AXIS_GENERIC_16 /*47*/:
                    case ';':
                    case '=':
                    case '\\':
                        checkLenient();
                        break;
                    default:
                        i++;
                }
                this.pos += i;
                return;
            }
            this.pos += i;
        } while (fillBuffer(1));
    }

    public int nextInt() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        int result;
        if (p == 15) {
            result = (int) this.peekedLong;
            if (this.peekedLong != ((long) result)) {
                throw new NumberFormatException("Expected an int but was " + this.peekedLong + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
            }
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return result;
        }
        if (p == 16) {
            this.peekedString = new String(this.buffer, this.pos, this.peekedNumberLength);
            this.pos += this.peekedNumberLength;
        } else if (p == 8 || p == 9) {
            this.peekedString = nextQuotedValue(p == 8 ? '\'' : '\"');
            try {
                result = Integer.parseInt(this.peekedString);
                this.peeked = 0;
                iArr = this.pathIndices;
                i = this.stackSize - 1;
                iArr[i] = iArr[i] + 1;
                return result;
            } catch (NumberFormatException e) {
            }
        } else {
            throw new IllegalStateException("Expected an int but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
        }
        this.peeked = 11;
        double asDouble = Double.parseDouble(this.peekedString);
        result = (int) asDouble;
        if (((double) result) != asDouble) {
            throw new NumberFormatException("Expected an int but was " + this.peekedString + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
        }
        this.peekedString = null;
        this.peeked = 0;
        iArr = this.pathIndices;
        i = this.stackSize - 1;
        iArr[i] = iArr[i] + 1;
        return result;
    }

    public void close() throws IOException {
        this.peeked = 0;
        this.stack[0] = 8;
        this.stackSize = 1;
        this.in.close();
    }

    public void skipValue() throws IOException {
        int count = 0;
        do {
            int p = this.peeked;
            if (p == 0) {
                p = doPeek();
            }
            if (p == 3) {
                push(1);
                count++;
            } else if (p == 1) {
                push(3);
                count++;
            } else if (p == 4) {
                this.stackSize--;
                count--;
            } else if (p == 2) {
                this.stackSize--;
                count--;
            } else if (p == 14 || p == 10) {
                skipUnquotedValue();
            } else if (p == 8 || p == 12) {
                skipQuotedValue('\'');
            } else if (p == 9 || p == 13) {
                skipQuotedValue('\"');
            } else if (p == 16) {
                this.pos += this.peekedNumberLength;
            }
            this.peeked = 0;
        } while (count != 0);
        int[] iArr = this.pathIndices;
        int i = this.stackSize - 1;
        iArr[i] = iArr[i] + 1;
        this.pathNames[this.stackSize - 1] = "null";
    }

    private void push(int newTop) {
        if (this.stackSize == this.stack.length) {
            int[] newStack = new int[(this.stackSize * 2)];
            int[] newPathIndices = new int[(this.stackSize * 2)];
            String[] newPathNames = new String[(this.stackSize * 2)];
            System.arraycopy(this.stack, 0, newStack, 0, this.stackSize);
            System.arraycopy(this.pathIndices, 0, newPathIndices, 0, this.stackSize);
            System.arraycopy(this.pathNames, 0, newPathNames, 0, this.stackSize);
            this.stack = newStack;
            this.pathIndices = newPathIndices;
            this.pathNames = newPathNames;
        }
        int[] iArr = this.stack;
        int i = this.stackSize;
        this.stackSize = i + 1;
        iArr[i] = newTop;
    }

    private boolean fillBuffer(int minimum) throws IOException {
        char[] buffer = this.buffer;
        this.lineStart -= this.pos;
        if (this.limit != this.pos) {
            this.limit -= this.pos;
            System.arraycopy(buffer, this.pos, buffer, 0, this.limit);
        } else {
            this.limit = 0;
        }
        this.pos = 0;
        do {
            int total = this.in.read(buffer, this.limit, buffer.length - this.limit);
            if (total == -1) {
                return false;
            }
            this.limit += total;
            if (this.lineNumber == 0 && this.lineStart == 0 && this.limit > 0 && buffer[0] == ByteOrderMark.UTF_BOM) {
                this.pos++;
                this.lineStart++;
                minimum++;
            }
        } while (this.limit < minimum);
        return true;
    }

    int getLineNumber() {
        return this.lineNumber + 1;
    }

    int getColumnNumber() {
        return (this.pos - this.lineStart) + 1;
    }

    private int nextNonWhitespace(boolean throwOnEof) throws IOException {
        char[] buffer = this.buffer;
        int p = this.pos;
        int l = this.limit;
        while (true) {
            if (p == l) {
                this.pos = p;
                if (fillBuffer(1)) {
                    p = this.pos;
                    l = this.limit;
                } else if (!throwOnEof) {
                    return -1;
                } else {
                    throw new EOFException("End of input at line " + getLineNumber() + " column " + getColumnNumber());
                }
            }
            int p2 = p + 1;
            int c = buffer[p];
            if (c == 10) {
                this.lineNumber++;
                this.lineStart = p2;
                p = p2;
            } else if (c == 32 || c == 13) {
                p = p2;
            } else if (c == 9) {
                p = p2;
            } else if (c == 47) {
                this.pos = p2;
                if (p2 == l) {
                    this.pos--;
                    boolean charsLoaded = fillBuffer(2);
                    this.pos++;
                    if (!charsLoaded) {
                        p = p2;
                        return c;
                    }
                }
                checkLenient();
                switch (buffer[this.pos]) {
                    case '*':
                        this.pos++;
                        if (skipTo("*/")) {
                            p = this.pos + 2;
                            l = this.limit;
                            break;
                        }
                        throw syntaxError("Unterminated comment");
                    case MotionEventCompat.AXIS_GENERIC_16 /*47*/:
                        this.pos++;
                        skipToEndOfLine();
                        p = this.pos;
                        l = this.limit;
                        break;
                    default:
                        p = p2;
                        return c;
                }
            } else if (c == 35) {
                this.pos = p2;
                checkLenient();
                skipToEndOfLine();
                p = this.pos;
                l = this.limit;
            } else {
                this.pos = p2;
                p = p2;
                return c;
            }
        }
    }

    private void checkLenient() throws IOException {
        if (!this.lenient) {
            throw syntaxError("Use JsonReader.setLenient(true) to accept malformed JSON");
        }
    }

    private void skipToEndOfLine() throws IOException {
        char c;
        do {
            if (this.pos < this.limit || fillBuffer(1)) {
                char[] cArr = this.buffer;
                int i = this.pos;
                this.pos = i + 1;
                c = cArr[i];
                if (c == '\n') {
                    this.lineNumber++;
                    this.lineStart = this.pos;
                    return;
                }
            } else {
                return;
            }
        } while (c != '\r');
    }

    private boolean skipTo(String toFind) throws IOException {
        while (true) {
            if (this.pos + toFind.length() > this.limit && !fillBuffer(toFind.length())) {
                return false;
            }
            if (this.buffer[this.pos] == '\n') {
                this.lineNumber++;
                this.lineStart = this.pos + 1;
            } else {
                int c = 0;
                while (c < toFind.length()) {
                    if (this.buffer[this.pos + c] == toFind.charAt(c)) {
                        c++;
                    }
                }
                return true;
            }
            this.pos++;
        }
    }

    public String toString() {
        return getClass().getSimpleName() + " at line " + getLineNumber() + " column " + getColumnNumber();
    }

    public String getPath() {
        StringBuilder result = new StringBuilder().append('$');
        int size = this.stackSize;
        for (int i = 0; i < size; i++) {
            switch (this.stack[i]) {
                case 1:
                case 2:
                    result.append('[').append(this.pathIndices[i]).append(']');
                    break;
                case 3:
                case 4:
                case 5:
                    result.append('.');
                    if (this.pathNames[i] == null) {
                        break;
                    }
                    result.append(this.pathNames[i]);
                    break;
                default:
                    break;
            }
        }
        return result.toString();
    }

    private char readEscapeCharacter() throws IOException {
        if (this.pos != this.limit || fillBuffer(1)) {
            char[] cArr = this.buffer;
            int i = this.pos;
            this.pos = i + 1;
            char escaped = cArr[i];
            switch (escaped) {
                case '\n':
                    this.lineNumber++;
                    this.lineStart = this.pos;
                    break;
                case 'b':
                    return '\b';
                case 'f':
                    return '\f';
                case 'n':
                    return '\n';
                case 'r':
                    return '\r';
                case 't':
                    return '\t';
                case 'u':
                    if (this.pos + 4 <= this.limit || fillBuffer(4)) {
                        char result = '\u0000';
                        int i2 = this.pos;
                        int end = i2 + 4;
                        while (i2 < end) {
                            char c = this.buffer[i2];
                            result = (char) (result << 4);
                            if (c >= '0' && c <= '9') {
                                result = (char) ((c - 48) + result);
                            } else if (c >= 'a' && c <= 'f') {
                                result = (char) (((c - 97) + 10) + result);
                            } else if (c < 'A' || c > 'F') {
                                throw new NumberFormatException("\\u" + new String(this.buffer, this.pos, 4));
                            } else {
                                result = (char) (((c - 65) + 10) + result);
                            }
                            i2++;
                        }
                        this.pos += 4;
                        return result;
                    }
                    throw syntaxError("Unterminated escape sequence");
            }
            return escaped;
        }
        throw syntaxError("Unterminated escape sequence");
    }

    private IOException syntaxError(String message) throws IOException {
        throw new MalformedJsonException(message + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
    }

    private void consumeNonExecutePrefix() throws IOException {
        nextNonWhitespace(true);
        this.pos--;
        if (this.pos + NON_EXECUTE_PREFIX.length <= this.limit || fillBuffer(NON_EXECUTE_PREFIX.length)) {
            int i = 0;
            while (i < NON_EXECUTE_PREFIX.length) {
                if (this.buffer[this.pos + i] == NON_EXECUTE_PREFIX[i]) {
                    i++;
                } else {
                    return;
                }
            }
            this.pos += NON_EXECUTE_PREFIX.length;
        }
    }
}
