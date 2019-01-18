package android.support.v4.view;

import android.os.Build.VERSION;
import android.view.ViewConfiguration;

public final class ViewConfigurationCompat {
    static final ViewConfigurationVersionImpl IMPL;

    interface ViewConfigurationVersionImpl {
        int getScaledPagingTouchSlop(ViewConfiguration viewConfiguration);

        boolean hasPermanentMenuKey(ViewConfiguration viewConfiguration);
    }

    static class BaseViewConfigurationVersionImpl implements ViewConfigurationVersionImpl {
        BaseViewConfigurationVersionImpl() {
        }

        public int getScaledPagingTouchSlop(ViewConfiguration config) {
            return config.getScaledTouchSlop();
        }

        public boolean hasPermanentMenuKey(ViewConfiguration config) {
            return true;
        }
    }

    static class FroyoViewConfigurationVersionImpl extends BaseViewConfigurationVersionImpl {
        FroyoViewConfigurationVersionImpl() {
        }

        public int getScaledPagingTouchSlop(ViewConfiguration config) {
            return ViewConfigurationCompatFroyo.getScaledPagingTouchSlop(config);
        }
    }

    static class HoneycombViewConfigurationVersionImpl extends FroyoViewConfigurationVersionImpl {
        HoneycombViewConfigurationVersionImpl() {
        }

        public boolean hasPermanentMenuKey(ViewConfiguration config) {
            return false;
        }
    }

    static class IcsViewConfigurationVersionImpl extends HoneycombViewConfigurationVersionImpl {
        IcsViewConfigurationVersionImpl() {
        }

        public boolean hasPermanentMenuKey(ViewConfiguration config) {
            return ViewConfigurationCompatICS.hasPermanentMenuKey(config);
        }
    }

    static {
        if (VERSION.SDK_INT >= 14) {
            IMPL = new IcsViewConfigurationVersionImpl();
        } else if (VERSION.SDK_INT >= 11) {
            IMPL = new HoneycombViewConfigurationVersionImpl();
        } else if (VERSION.SDK_INT >= 8) {
            IMPL = new FroyoViewConfigurationVersionImpl();
        } else {
            IMPL = new BaseViewConfigurationVersionImpl();
        }
    }

    public static int getScaledPagingTouchSlop(ViewConfiguration config) {
        return IMPL.getScaledPagingTouchSlop(config);
    }

    public static boolean hasPermanentMenuKey(ViewConfiguration config) {
        return IMPL.hasPermanentMenuKey(config);
    }

    private ViewConfigurationCompat() {
    }
}
