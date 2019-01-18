package okhttp3.internal.tls;

import android.support.v4.view.MotionEventCompat;
import com.evenwell.powersaving.g3.utils.PSConst.DC.TIME.VALUE;
import javax.security.auth.x500.X500Principal;

final class DistinguishedNameParser {
    private int beg;
    private char[] chars;
    private int cur;
    private final String dn;
    private int end;
    private final int length = this.dn.length();
    private int pos;

    public DistinguishedNameParser(X500Principal principal) {
        this.dn = principal.getName("RFC2253");
    }

    private String nextAT() {
        while (this.pos < this.length && this.chars[this.pos] == ' ') {
            this.pos++;
        }
        if (this.pos == this.length) {
            return null;
        }
        this.beg = this.pos;
        this.pos++;
        while (this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] != ' ') {
            this.pos++;
        }
        if (this.pos >= this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        this.end = this.pos;
        if (this.chars[this.pos] == ' ') {
            while (this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] == ' ') {
                this.pos++;
            }
            if (this.chars[this.pos] != '=' || this.pos == this.length) {
                throw new IllegalStateException("Unexpected end of DN: " + this.dn);
            }
        }
        this.pos++;
        while (this.pos < this.length && this.chars[this.pos] == ' ') {
            this.pos++;
        }
        if (this.end - this.beg > 4 && this.chars[this.beg + 3] == '.' && ((this.chars[this.beg] == 'O' || this.chars[this.beg] == 'o') && ((this.chars[this.beg + 1] == 'I' || this.chars[this.beg + 1] == 'i') && (this.chars[this.beg + 2] == 'D' || this.chars[this.beg + 2] == 'd')))) {
            this.beg += 4;
        }
        return new String(this.chars, this.beg, this.end - this.beg);
    }

    private String quotedAV() {
        this.pos++;
        this.beg = this.pos;
        this.end = this.beg;
        while (this.pos != this.length) {
            if (this.chars[this.pos] == '\"') {
                this.pos++;
                while (this.pos < this.length && this.chars[this.pos] == ' ') {
                    this.pos++;
                }
                return new String(this.chars, this.beg, this.end - this.beg);
            }
            if (this.chars[this.pos] == '\\') {
                this.chars[this.end] = getEscaped();
            } else {
                this.chars[this.end] = this.chars[this.pos];
            }
            this.pos++;
            this.end++;
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private String hexAV() {
        if (this.pos + 4 >= this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        int hexLen;
        this.beg = this.pos;
        this.pos++;
        while (this.pos != this.length && this.chars[this.pos] != '+' && this.chars[this.pos] != ',' && this.chars[this.pos] != ';') {
            if (this.chars[this.pos] == ' ') {
                this.end = this.pos;
                this.pos++;
                while (this.pos < this.length && this.chars[this.pos] == ' ') {
                    this.pos++;
                }
                hexLen = this.end - this.beg;
                if (hexLen >= 5 || (hexLen & 1) == 0) {
                    throw new IllegalStateException("Unexpected end of DN: " + this.dn);
                }
                byte[] encoded = new byte[(hexLen / 2)];
                int p = this.beg + 1;
                for (int i = 0; i < encoded.length; i++) {
                    encoded[i] = (byte) getByte(p);
                    p += 2;
                }
                return new String(this.chars, this.beg, hexLen);
            }
            if (this.chars[this.pos] >= 'A' && this.chars[this.pos] <= 'F') {
                char[] cArr = this.chars;
                int i2 = this.pos;
                cArr[i2] = (char) (cArr[i2] + 32);
            }
            this.pos++;
        }
        this.end = this.pos;
        hexLen = this.end - this.beg;
        if (hexLen >= 5) {
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private String escapedAV() {
        this.beg = this.pos;
        this.end = this.pos;
        while (this.pos < this.length) {
            char[] cArr;
            int i;
            switch (this.chars[this.pos]) {
                case ' ':
                    this.cur = this.end;
                    this.pos++;
                    cArr = this.chars;
                    i = this.end;
                    this.end = i + 1;
                    cArr[i] = ' ';
                    while (this.pos < this.length && this.chars[this.pos] == ' ') {
                        cArr = this.chars;
                        i = this.end;
                        this.end = i + 1;
                        cArr[i] = ' ';
                        this.pos++;
                    }
                    if (this.pos != this.length && this.chars[this.pos] != ',' && this.chars[this.pos] != '+' && this.chars[this.pos] != ';') {
                        break;
                    }
                    return new String(this.chars, this.beg, this.cur - this.beg);
                    break;
                case MotionEventCompat.AXIS_GENERIC_12 /*43*/:
                case MotionEventCompat.AXIS_GENERIC_13 /*44*/:
                case ';':
                    return new String(this.chars, this.beg, this.end - this.beg);
                case '\\':
                    cArr = this.chars;
                    i = this.end;
                    this.end = i + 1;
                    cArr[i] = getEscaped();
                    this.pos++;
                    break;
                default:
                    cArr = this.chars;
                    i = this.end;
                    this.end = i + 1;
                    cArr[i] = this.chars[this.pos];
                    this.pos++;
                    break;
            }
        }
        return new String(this.chars, this.beg, this.end - this.beg);
    }

    private char getEscaped() {
        this.pos++;
        if (this.pos == this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        switch (this.chars[this.pos]) {
            case ' ':
            case '\"':
            case '#':
            case MotionEventCompat.AXIS_GENERIC_6 /*37*/:
            case '*':
            case MotionEventCompat.AXIS_GENERIC_12 /*43*/:
            case MotionEventCompat.AXIS_GENERIC_13 /*44*/:
            case ';':
            case VALUE.TIME_CONVERT /*60*/:
            case '=':
            case '>':
            case '\\':
            case '_':
                return this.chars[this.pos];
            default:
                return getUTF8();
        }
    }

    private char getUTF8() {
        int res = getByte(this.pos);
        this.pos++;
        if (res < 128) {
            return (char) res;
        }
        if (res < 192 || res > 247) {
            return '?';
        }
        int count;
        if (res <= 223) {
            count = 1;
            res &= 31;
        } else if (res <= 239) {
            count = 2;
            res &= 15;
        } else {
            count = 3;
            res &= 7;
        }
        for (int i = 0; i < count; i++) {
            this.pos++;
            if (this.pos == this.length || this.chars[this.pos] != '\\') {
                return '?';
            }
            this.pos++;
            int b = getByte(this.pos);
            this.pos++;
            if ((b & 192) != 128) {
                return '?';
            }
            res = (res << 6) + (b & 63);
        }
        return (char) res;
    }

    private int getByte(int position) {
        if (position + 1 >= this.length) {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        }
        int b1 = this.chars[position];
        if (b1 >= 48 && b1 <= 57) {
            b1 -= 48;
        } else if (b1 >= 97 && b1 <= 102) {
            b1 -= 87;
        } else if (b1 < 65 || b1 > 70) {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        } else {
            b1 -= 55;
        }
        int b2 = this.chars[position + 1];
        if (b2 >= 48 && b2 <= 57) {
            b2 -= 48;
        } else if (b2 >= 97 && b2 <= 102) {
            b2 -= 87;
        } else if (b2 < 65 || b2 > 70) {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        } else {
            b2 -= 55;
        }
        return (b1 << 4) + b2;
    }

    public String findMostSpecific(String attributeType) {
        this.pos = 0;
        this.beg = 0;
        this.end = 0;
        this.cur = 0;
        this.chars = this.dn.toCharArray();
        String attType = nextAT();
        if (attType == null) {
            return null;
        }
        do {
            String attValue = "";
            if (this.pos == this.length) {
                return null;
            }
            switch (this.chars[this.pos]) {
                case '\"':
                    attValue = quotedAV();
                    break;
                case '#':
                    attValue = hexAV();
                    break;
                case MotionEventCompat.AXIS_GENERIC_12 /*43*/:
                case MotionEventCompat.AXIS_GENERIC_13 /*44*/:
                case ';':
                    break;
                default:
                    attValue = escapedAV();
                    break;
            }
            if (attributeType.equalsIgnoreCase(attType)) {
                return attValue;
            }
            if (this.pos >= this.length) {
                return null;
            }
            if (this.chars[this.pos] == ',' || this.chars[this.pos] == ';' || this.chars[this.pos] == '+') {
                this.pos++;
                attType = nextAT();
            } else {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            }
        } while (attType != null);
        throw new IllegalStateException("Malformed DN: " + this.dn);
    }
}
