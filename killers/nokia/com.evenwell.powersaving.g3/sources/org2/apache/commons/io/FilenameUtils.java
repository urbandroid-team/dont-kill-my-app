package org2.apache.commons.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

public class FilenameUtils {
    public static final char EXTENSION_SEPARATOR = '.';
    public static final String EXTENSION_SEPARATOR_STR = Character.toString('.');
    private static final char OTHER_SEPARATOR;
    private static final char SYSTEM_SEPARATOR = File.separatorChar;
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    static {
        if (isSystemWindows()) {
            OTHER_SEPARATOR = '/';
        } else {
            OTHER_SEPARATOR = '\\';
        }
    }

    static boolean isSystemWindows() {
        return SYSTEM_SEPARATOR == '\\';
    }

    private static boolean isSeparator(char ch) {
        return ch == '/' || ch == '\\';
    }

    public static String normalize(String filename) {
        return doNormalize(filename, SYSTEM_SEPARATOR, true);
    }

    public static String normalize(String filename, boolean unixSeparator) {
        return doNormalize(filename, unixSeparator ? '/' : '\\', true);
    }

    public static String normalizeNoEndSeparator(String filename) {
        return doNormalize(filename, SYSTEM_SEPARATOR, false);
    }

    public static String normalizeNoEndSeparator(String filename, boolean unixSeparator) {
        return doNormalize(filename, unixSeparator ? '/' : '\\', false);
    }

    private static String doNormalize(String filename, char separator, boolean keepSeparator) {
        if (filename == null) {
            return null;
        }
        int length = filename.length();
        if (length == 0) {
            return filename;
        }
        int prefix = getPrefixLength(filename);
        if (prefix < 0) {
            return null;
        }
        int i;
        char[] array = new char[(length + 2)];
        filename.getChars(0, filename.length(), array, 0);
        char otherSeparator = separator == SYSTEM_SEPARATOR ? OTHER_SEPARATOR : SYSTEM_SEPARATOR;
        for (i = 0; i < array.length; i++) {
            if (array[i] == otherSeparator) {
                array[i] = separator;
            }
        }
        boolean lastIsDirectory = true;
        if (array[length - 1] != separator) {
            int size = length + 1;
            array[length] = separator;
            lastIsDirectory = false;
            length = size;
        }
        i = prefix + 1;
        while (i < length) {
            if (array[i] == separator && array[i - 1] == separator) {
                System.arraycopy(array, i, array, i - 1, length - i);
                length--;
                i--;
            }
            i++;
        }
        i = prefix + 1;
        while (i < length) {
            if (array[i] == separator && array[i - 1] == '.' && (i == prefix + 1 || array[i - 2] == separator)) {
                if (i == length - 1) {
                    lastIsDirectory = true;
                }
                System.arraycopy(array, i + 1, array, i - 1, length - i);
                length -= 2;
                i--;
            }
            i++;
        }
        i = prefix + 2;
        while (i < length) {
            if (array[i] == separator && array[i - 1] == '.' && array[i - 2] == '.' && (i == prefix + 2 || array[i - 3] == separator)) {
                if (i == prefix + 2) {
                    return null;
                }
                if (i == length - 1) {
                    lastIsDirectory = true;
                }
                for (int j = i - 4; j >= prefix; j--) {
                    if (array[j] == separator) {
                        System.arraycopy(array, i + 1, array, j + 1, length - i);
                        length -= i - j;
                        i = j + 1;
                        break;
                    }
                }
                System.arraycopy(array, i + 1, array, prefix, length - i);
                length -= (i + 1) - prefix;
                i = prefix + 1;
            }
            i++;
        }
        if (length <= 0) {
            return "";
        }
        if (length <= prefix) {
            return new String(array, 0, length);
        }
        if (lastIsDirectory && keepSeparator) {
            return new String(array, 0, length);
        }
        return new String(array, 0, length - 1);
    }

    public static String concat(String basePath, String fullFilenameToAdd) {
        int prefix = getPrefixLength(fullFilenameToAdd);
        if (prefix < 0) {
            return null;
        }
        if (prefix > 0) {
            return normalize(fullFilenameToAdd);
        }
        if (basePath == null) {
            return null;
        }
        int len = basePath.length();
        if (len == 0) {
            return normalize(fullFilenameToAdd);
        }
        if (isSeparator(basePath.charAt(len - 1))) {
            return normalize(basePath + fullFilenameToAdd);
        }
        return normalize(basePath + '/' + fullFilenameToAdd);
    }

    public static boolean directoryContains(String canonicalParent, String canonicalChild) throws IOException {
        if (canonicalParent == null) {
            throw new IllegalArgumentException("Directory must not be null");
        } else if (canonicalChild == null || IOCase.SYSTEM.checkEquals(canonicalParent, canonicalChild)) {
            return false;
        } else {
            return IOCase.SYSTEM.checkStartsWith(canonicalChild, canonicalParent);
        }
    }

    public static String separatorsToUnix(String path) {
        return (path == null || path.indexOf(92) == -1) ? path : path.replace('\\', '/');
    }

    public static String separatorsToWindows(String path) {
        return (path == null || path.indexOf(47) == -1) ? path : path.replace('/', '\\');
    }

    public static String separatorsToSystem(String path) {
        if (path == null) {
            return null;
        }
        if (isSystemWindows()) {
            return separatorsToWindows(path);
        }
        return separatorsToUnix(path);
    }

    public static int getPrefixLength(String filename) {
        if (filename == null) {
            return -1;
        }
        int len = filename.length();
        if (len == 0) {
            return 0;
        }
        char ch0 = filename.charAt(0);
        if (ch0 == ':') {
            return -1;
        }
        if (len == 1) {
            if (ch0 == '~') {
                return 2;
            }
            if (isSeparator(ch0)) {
                return 1;
            }
            return 0;
        } else if (ch0 == '~') {
            posUnix = filename.indexOf(47, 1);
            posWin = filename.indexOf(92, 1);
            if (posUnix == -1 && posWin == -1) {
                return len + 1;
            }
            if (posUnix == -1) {
                posUnix = posWin;
            }
            if (posWin == -1) {
                posWin = posUnix;
            }
            return Math.min(posUnix, posWin) + 1;
        } else {
            char ch1 = filename.charAt(1);
            if (ch1 == ':') {
                ch0 = Character.toUpperCase(ch0);
                if (ch0 < 'A' || ch0 > 'Z') {
                    return -1;
                }
                if (len == 2 || !isSeparator(filename.charAt(2))) {
                    return 2;
                }
                return 3;
            } else if (isSeparator(ch0) && isSeparator(ch1)) {
                posUnix = filename.indexOf(47, 2);
                posWin = filename.indexOf(92, 2);
                if ((posUnix == -1 && posWin == -1) || posUnix == 2 || posWin == 2) {
                    return -1;
                }
                if (posUnix == -1) {
                    posUnix = posWin;
                }
                if (posWin == -1) {
                    posWin = posUnix;
                }
                return Math.min(posUnix, posWin) + 1;
            } else if (isSeparator(ch0)) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        return Math.max(filename.lastIndexOf(47), filename.lastIndexOf(92));
    }

    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(46);
        if (indexOfLastSeparator(filename) > extensionPos) {
            extensionPos = -1;
        }
        return extensionPos;
    }

    public static String getPrefix(String filename) {
        if (filename == null) {
            return null;
        }
        int len = getPrefixLength(filename);
        if (len < 0) {
            return null;
        }
        if (len > filename.length()) {
            return filename + '/';
        }
        return filename.substring(0, len);
    }

    public static String getPath(String filename) {
        return doGetPath(filename, 1);
    }

    public static String getPathNoEndSeparator(String filename) {
        return doGetPath(filename, 0);
    }

    private static String doGetPath(String filename, int separatorAdd) {
        if (filename == null) {
            return null;
        }
        int prefix = getPrefixLength(filename);
        if (prefix < 0) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        int endIndex = index + separatorAdd;
        if (prefix >= filename.length() || index < 0 || prefix >= endIndex) {
            return "";
        }
        return filename.substring(prefix, endIndex);
    }

    public static String getFullPath(String filename) {
        return doGetFullPath(filename, true);
    }

    public static String getFullPathNoEndSeparator(String filename) {
        return doGetFullPath(filename, false);
    }

    private static String doGetFullPath(String filename, boolean includeSeparator) {
        if (filename == null) {
            return null;
        }
        int prefix = getPrefixLength(filename);
        if (prefix < 0) {
            return null;
        }
        if (prefix < filename.length()) {
            int index = indexOfLastSeparator(filename);
            if (index < 0) {
                return filename.substring(0, prefix);
            }
            int i;
            if (includeSeparator) {
                i = 1;
            } else {
                i = 0;
            }
            int end = index + i;
            if (end == 0) {
                end++;
            }
            return filename.substring(0, end);
        } else if (includeSeparator) {
            return getPrefix(filename);
        } else {
            return filename;
        }
    }

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        return filename.substring(indexOfLastSeparator(filename) + 1);
    }

    public static String getBaseName(String filename) {
        return removeExtension(getName(filename));
    }

    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        }
        return filename.substring(index + 1);
    }

    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        return index != -1 ? filename.substring(0, index) : filename;
    }

    public static boolean equals(String filename1, String filename2) {
        return equals(filename1, filename2, false, IOCase.SENSITIVE);
    }

    public static boolean equalsOnSystem(String filename1, String filename2) {
        return equals(filename1, filename2, false, IOCase.SYSTEM);
    }

    public static boolean equalsNormalized(String filename1, String filename2) {
        return equals(filename1, filename2, true, IOCase.SENSITIVE);
    }

    public static boolean equalsNormalizedOnSystem(String filename1, String filename2) {
        return equals(filename1, filename2, true, IOCase.SYSTEM);
    }

    public static boolean equals(String filename1, String filename2, boolean normalized, IOCase caseSensitivity) {
        if (filename1 != null && filename2 != null) {
            if (normalized) {
                filename1 = normalize(filename1);
                filename2 = normalize(filename2);
                if (filename1 == null || filename2 == null) {
                    throw new NullPointerException("Error normalizing one or both of the file names");
                }
            }
            if (caseSensitivity == null) {
                caseSensitivity = IOCase.SENSITIVE;
            }
            return caseSensitivity.checkEquals(filename1, filename2);
        } else if (filename1 == null && filename2 == null) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isExtension(String filename, String extension) {
        if (filename == null) {
            return false;
        }
        if (extension != null && extension.length() != 0) {
            return getExtension(filename).equals(extension);
        }
        if (indexOfExtension(filename) == -1) {
            return true;
        }
        return false;
    }

    public static boolean isExtension(String filename, String[] extensions) {
        boolean z = true;
        if (filename == null) {
            return false;
        }
        if (extensions == null || extensions.length == 0) {
            if (indexOfExtension(filename) != -1) {
                z = false;
            }
            return z;
        }
        String fileExt = getExtension(filename);
        for (String extension : extensions) {
            if (fileExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExtension(String filename, Collection<String> extensions) {
        boolean z = true;
        if (filename == null) {
            return false;
        }
        if (extensions == null || extensions.isEmpty()) {
            if (indexOfExtension(filename) != -1) {
                z = false;
            }
            return z;
        }
        String fileExt = getExtension(filename);
        for (String extension : extensions) {
            if (fileExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean wildcardMatch(String filename, String wildcardMatcher) {
        return wildcardMatch(filename, wildcardMatcher, IOCase.SENSITIVE);
    }

    public static boolean wildcardMatchOnSystem(String filename, String wildcardMatcher) {
        return wildcardMatch(filename, wildcardMatcher, IOCase.SYSTEM);
    }

    public static boolean wildcardMatch(String filename, String wildcardMatcher, IOCase caseSensitivity) {
        if (filename == null && wildcardMatcher == null) {
            return true;
        }
        if (filename == null || wildcardMatcher == null) {
            return false;
        }
        if (caseSensitivity == null) {
            caseSensitivity = IOCase.SENSITIVE;
        }
        String[] wcs = splitOnTokens(wildcardMatcher);
        boolean anyChars = false;
        int textIdx = 0;
        int wcsIdx = 0;
        Stack<int[]> backtrack = new Stack();
        do {
            if (backtrack.size() > 0) {
                int[] array = (int[]) backtrack.pop();
                wcsIdx = array[0];
                textIdx = array[1];
                anyChars = true;
            }
            while (wcsIdx < wcs.length) {
                if (wcs[wcsIdx].equals("?")) {
                    textIdx++;
                    if (textIdx > filename.length()) {
                        break;
                    }
                    anyChars = false;
                } else if (wcs[wcsIdx].equals("*")) {
                    anyChars = true;
                    if (wcsIdx == wcs.length - 1) {
                        textIdx = filename.length();
                    }
                } else {
                    if (!anyChars) {
                        if (!caseSensitivity.checkRegionMatches(filename, textIdx, wcs[wcsIdx])) {
                            break;
                        }
                    }
                    textIdx = caseSensitivity.checkIndexOf(filename, textIdx, wcs[wcsIdx]);
                    if (textIdx == -1) {
                        break;
                    }
                    if (caseSensitivity.checkIndexOf(filename, textIdx + 1, wcs[wcsIdx]) >= 0) {
                        backtrack.push(new int[]{wcsIdx, caseSensitivity.checkIndexOf(filename, textIdx + 1, wcs[wcsIdx])});
                    }
                    textIdx += wcs[wcsIdx].length();
                    anyChars = false;
                }
                wcsIdx++;
            }
            if (wcsIdx == wcs.length && textIdx == filename.length()) {
                return true;
            }
        } while (backtrack.size() > 0);
        return false;
    }

    static String[] splitOnTokens(String text) {
        if (text.indexOf(63) == -1 && text.indexOf(42) == -1) {
            return new String[]{text};
        }
        char[] array = text.toCharArray();
        ArrayList<String> list = new ArrayList();
        StringBuilder buffer = new StringBuilder();
        int i = 0;
        while (i < array.length) {
            if (array[i] == '?' || array[i] == '*') {
                if (buffer.length() != 0) {
                    list.add(buffer.toString());
                    buffer.setLength(0);
                }
                if (array[i] == '?') {
                    list.add("?");
                } else if (list.isEmpty() || (i > 0 && !((String) list.get(list.size() - 1)).equals("*"))) {
                    list.add("*");
                }
            } else {
                buffer.append(array[i]);
            }
            i++;
        }
        if (buffer.length() != 0) {
            list.add(buffer.toString());
        }
        return (String[]) list.toArray(new String[list.size()]);
    }
}
