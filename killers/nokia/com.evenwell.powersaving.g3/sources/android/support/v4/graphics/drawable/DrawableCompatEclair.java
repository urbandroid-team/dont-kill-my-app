package android.support.v4.graphics.drawable;

import android.graphics.drawable.Drawable;

class DrawableCompatEclair {
    DrawableCompatEclair() {
    }

    public static Drawable wrapForTinting(Drawable drawable) {
        if (drawable instanceof DrawableWrapperEclair) {
            return drawable;
        }
        return new DrawableWrapperEclair(drawable);
    }
}
