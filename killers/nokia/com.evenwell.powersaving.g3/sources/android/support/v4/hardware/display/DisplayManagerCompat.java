package android.support.v4.hardware.display;

import android.content.Context;
import android.os.Build.VERSION;
import android.view.Display;
import android.view.WindowManager;
import java.util.WeakHashMap;

public abstract class DisplayManagerCompat {
    public static final String DISPLAY_CATEGORY_PRESENTATION = "android.hardware.display.category.PRESENTATION";
    private static final WeakHashMap<Context, DisplayManagerCompat> sInstances = new WeakHashMap();

    private static class JellybeanMr1Impl extends DisplayManagerCompat {
        private final Object mDisplayManagerObj;

        public JellybeanMr1Impl(Context context) {
            this.mDisplayManagerObj = DisplayManagerJellybeanMr1.getDisplayManager(context);
        }

        public Display getDisplay(int displayId) {
            return DisplayManagerJellybeanMr1.getDisplay(this.mDisplayManagerObj, displayId);
        }

        public Display[] getDisplays() {
            return DisplayManagerJellybeanMr1.getDisplays(this.mDisplayManagerObj);
        }

        public Display[] getDisplays(String category) {
            return DisplayManagerJellybeanMr1.getDisplays(this.mDisplayManagerObj, category);
        }
    }

    private static class LegacyImpl extends DisplayManagerCompat {
        private final WindowManager mWindowManager;

        public LegacyImpl(Context context) {
            this.mWindowManager = (WindowManager) context.getSystemService("window");
        }

        public Display getDisplay(int displayId) {
            Display display = this.mWindowManager.getDefaultDisplay();
            return display.getDisplayId() == displayId ? display : null;
        }

        public Display[] getDisplays() {
            return new Display[]{this.mWindowManager.getDefaultDisplay()};
        }

        public Display[] getDisplays(String category) {
            return category == null ? getDisplays() : new Display[0];
        }
    }

    public abstract Display getDisplay(int i);

    public abstract Display[] getDisplays();

    public abstract Display[] getDisplays(String str);

    DisplayManagerCompat() {
    }

    public static DisplayManagerCompat getInstance(Context context) {
        DisplayManagerCompat instance;
        synchronized (sInstances) {
            instance = (DisplayManagerCompat) sInstances.get(context);
            if (instance == null) {
                if (VERSION.SDK_INT >= 17) {
                    instance = new JellybeanMr1Impl(context);
                } else {
                    instance = new LegacyImpl(context);
                }
                sInstances.put(context, instance);
            }
        }
        return instance;
    }
}
