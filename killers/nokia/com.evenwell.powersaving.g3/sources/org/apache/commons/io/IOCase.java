package org.apache.commons.io;

import java.io.Serializable;

public enum IOCase implements Serializable {
    SENSITIVE("Sensitive", true),
    INSENSITIVE("Insensitive", false);
    
    private static final long serialVersionUID = -6343169151696340687L;
    private final String name;
    private final transient boolean sensitive;

    static {
        boolean z;
        SENSITIVE = new IOCase("SENSITIVE", 0, "Sensitive", true);
        INSENSITIVE = new IOCase("INSENSITIVE", 1, "Insensitive", false);
        String str = "SYSTEM";
        String str2 = "System";
        if (FilenameUtils.isSystemWindows()) {
            z = false;
        } else {
            z = true;
        }
        SYSTEM = new IOCase(str, 2, str2, z);
        $VALUES = new IOCase[]{SENSITIVE, INSENSITIVE, SYSTEM};
    }

    public static IOCase forName(String name) {
        for (IOCase ioCase : values()) {
            if (ioCase.getName().equals(name)) {
                return ioCase;
            }
        }
        throw new IllegalArgumentException("Invalid IOCase name: " + name);
    }

    private IOCase(String name, boolean sensitive) {
        this.name = name;
        this.sensitive = sensitive;
    }

    private Object readResolve() {
        return forName(this.name);
    }

    public String getName() {
        return this.name;
    }

    public boolean isCaseSensitive() {
        return this.sensitive;
    }

    public int checkCompareTo(String str1, String str2) {
        if (str1 != null && str2 != null) {
            return this.sensitive ? str1.compareTo(str2) : str1.compareToIgnoreCase(str2);
        } else {
            throw new NullPointerException("The strings must not be null");
        }
    }

    public boolean checkEquals(String str1, String str2) {
        if (str1 != null && str2 != null) {
            return this.sensitive ? str1.equals(str2) : str1.equalsIgnoreCase(str2);
        } else {
            throw new NullPointerException("The strings must not be null");
        }
    }

    public boolean checkStartsWith(String str, String start) {
        return str.regionMatches(!this.sensitive, 0, start, 0, start.length());
    }

    public boolean checkEndsWith(String str, String end) {
        int endLen = end.length();
        return str.regionMatches(!this.sensitive, str.length() - endLen, end, 0, endLen);
    }

    public int checkIndexOf(String str, int strStartIndex, String search) {
        int endIndex = str.length() - search.length();
        if (endIndex >= strStartIndex) {
            for (int i = strStartIndex; i <= endIndex; i++) {
                if (checkRegionMatches(str, i, search)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean checkRegionMatches(String str, int strStartIndex, String search) {
        return str.regionMatches(!this.sensitive, strStartIndex, search, 0, search.length());
    }

    public String toString() {
        return this.name;
    }
}
