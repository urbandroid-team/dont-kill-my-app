package android.support.v4.text;

import android.os.Build.VERSION;
import java.util.Locale;

public final class ICUCompat {
    private static final ICUCompatImpl IMPL;

    interface ICUCompatImpl {
        String maximizeAndGetScript(Locale locale);
    }

    static class ICUCompatImplBase implements ICUCompatImpl {
        ICUCompatImplBase() {
        }

        public String maximizeAndGetScript(Locale locale) {
            return null;
        }
    }

    static class ICUCompatImplIcs implements ICUCompatImpl {
        ICUCompatImplIcs() {
        }

        public String maximizeAndGetScript(Locale locale) {
            return ICUCompatIcs.maximizeAndGetScript(locale);
        }
    }

    static class ICUCompatImplLollipop implements ICUCompatImpl {
        ICUCompatImplLollipop() {
        }

        public String maximizeAndGetScript(Locale locale) {
            return ICUCompatApi23.maximizeAndGetScript(locale);
        }
    }

    static {
        int version = VERSION.SDK_INT;
        if (version >= 21) {
            IMPL = new ICUCompatImplLollipop();
        } else if (version >= 14) {
            IMPL = new ICUCompatImplIcs();
        } else {
            IMPL = new ICUCompatImplBase();
        }
    }

    public static String maximizeAndGetScript(Locale locale) {
        return IMPL.maximizeAndGetScript(locale);
    }

    private ICUCompat() {
    }
}
