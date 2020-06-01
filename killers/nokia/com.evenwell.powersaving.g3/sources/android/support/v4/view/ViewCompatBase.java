package android.support.v4.view;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.view.ViewParent;
import java.lang.reflect.Field;

class ViewCompatBase {
    private static final String TAG = "ViewCompatBase";
    private static Field sMinHeightField;
    private static boolean sMinHeightFieldFetched;
    private static Field sMinWidthField;
    private static boolean sMinWidthFieldFetched;

    ViewCompatBase() {
    }

    static ColorStateList getBackgroundTintList(View view) {
        return view instanceof TintableBackgroundView ? ((TintableBackgroundView) view).getSupportBackgroundTintList() : null;
    }

    static void setBackgroundTintList(View view, ColorStateList tintList) {
        if (view instanceof TintableBackgroundView) {
            ((TintableBackgroundView) view).setSupportBackgroundTintList(tintList);
        }
    }

    static Mode getBackgroundTintMode(View view) {
        return view instanceof TintableBackgroundView ? ((TintableBackgroundView) view).getSupportBackgroundTintMode() : null;
    }

    static void setBackgroundTintMode(View view, Mode mode) {
        if (view instanceof TintableBackgroundView) {
            ((TintableBackgroundView) view).setSupportBackgroundTintMode(mode);
        }
    }

    static boolean isLaidOut(View view) {
        return view.getWidth() > 0 && view.getHeight() > 0;
    }

    static int getMinimumWidth(View view) {
        if (!sMinWidthFieldFetched) {
            try {
                sMinWidthField = View.class.getDeclaredField("mMinWidth");
                sMinWidthField.setAccessible(true);
            } catch (NoSuchFieldException e) {
            }
            sMinWidthFieldFetched = true;
        }
        if (sMinWidthField != null) {
            try {
                return ((Integer) sMinWidthField.get(view)).intValue();
            } catch (Exception e2) {
            }
        }
        return 0;
    }

    static int getMinimumHeight(View view) {
        if (!sMinHeightFieldFetched) {
            try {
                sMinHeightField = View.class.getDeclaredField("mMinHeight");
                sMinHeightField.setAccessible(true);
            } catch (NoSuchFieldException e) {
            }
            sMinHeightFieldFetched = true;
        }
        if (sMinHeightField != null) {
            try {
                return ((Integer) sMinHeightField.get(view)).intValue();
            } catch (Exception e2) {
            }
        }
        return 0;
    }

    static boolean isAttachedToWindow(View view) {
        return view.getWindowToken() != null;
    }

    static void offsetTopAndBottom(View view, int offset) {
        int currentTop = view.getTop();
        view.offsetTopAndBottom(offset);
        if (offset != 0) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                int absOffset = Math.abs(offset);
                ((View) parent).invalidate(view.getLeft(), currentTop - absOffset, view.getRight(), (view.getHeight() + currentTop) + absOffset);
                return;
            }
            view.invalidate();
        }
    }

    static void offsetLeftAndRight(View view, int offset) {
        int currentLeft = view.getLeft();
        view.offsetLeftAndRight(offset);
        if (offset != 0) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                int absOffset = Math.abs(offset);
                ((View) parent).invalidate(currentLeft - absOffset, view.getTop(), (view.getWidth() + currentLeft) + absOffset, view.getBottom());
                return;
            }
            view.invalidate();
        }
    }
}
