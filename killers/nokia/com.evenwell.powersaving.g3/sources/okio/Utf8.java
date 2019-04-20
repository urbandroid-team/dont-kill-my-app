package okio;

public final class Utf8 {
    private Utf8() {
    }

    public static long size(String string) {
        return size(string, 0, string.length());
    }

    public static long size(String string, int beginIndex, int endIndex) {
        if (string == null) {
            throw new IllegalArgumentException("string == null");
        } else if (beginIndex < 0) {
            throw new IllegalArgumentException("beginIndex < 0: " + beginIndex);
        } else if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        } else if (endIndex > string.length()) {
            throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string.length());
        } else {
            long result = 0;
            int i = beginIndex;
            while (i < endIndex) {
                int c = string.charAt(i);
                if (c < 128) {
                    result++;
                    i++;
                } else if (c < 2048) {
                    result += 2;
                    i++;
                } else if (c < 55296 || c > 57343) {
                    result += 3;
                    i++;
                } else {
                    int low = i + 1 < endIndex ? string.charAt(i + 1) : 0;
                    if (c > 56319 || low < 56320 || low > 57343) {
                        result++;
                        i++;
                    } else {
                        result += 4;
                        i += 2;
                    }
                }
            }
            return result;
        }
    }
}
