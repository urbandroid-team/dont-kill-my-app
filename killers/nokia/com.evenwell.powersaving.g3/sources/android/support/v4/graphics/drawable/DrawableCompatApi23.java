package android.support.v4.graphics.drawable;

import android.graphics.drawable.Drawable;

class DrawableCompatApi23 {
    DrawableCompatApi23() {
    }

    public static void setLayoutDirection(Drawable drawable, int layoutDirection) {
        drawable.setLayoutDirection(layoutDirection);
    }

    public static int getLayoutDirection(Drawable drawable) {
        return drawable.getLayoutDirection();
    }
}
