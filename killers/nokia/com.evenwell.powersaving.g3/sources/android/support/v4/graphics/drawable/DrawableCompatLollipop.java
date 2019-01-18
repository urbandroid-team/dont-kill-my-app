package android.support.v4.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class DrawableCompatLollipop {
    DrawableCompatLollipop() {
    }

    public static void setHotspot(Drawable drawable, float x, float y) {
        drawable.setHotspot(x, y);
    }

    public static void setHotspotBounds(Drawable drawable, int left, int top, int right, int bottom) {
        drawable.setHotspotBounds(left, top, right, bottom);
    }

    public static void setTint(Drawable drawable, int tint) {
        drawable.setTint(tint);
    }

    public static void setTintList(Drawable drawable, ColorStateList tint) {
        drawable.setTintList(tint);
    }

    public static void setTintMode(Drawable drawable, Mode tintMode) {
        drawable.setTintMode(tintMode);
    }

    public static Drawable wrapForTinting(Drawable drawable) {
        if (drawable instanceof DrawableWrapperLollipop) {
            return drawable;
        }
        return new DrawableWrapperLollipop(drawable);
    }

    public static void applyTheme(Drawable drawable, Theme t) {
        drawable.applyTheme(t);
    }

    public static boolean canApplyTheme(Drawable drawable) {
        return drawable.canApplyTheme();
    }

    public static ColorFilter getColorFilter(Drawable drawable) {
        return drawable.getColorFilter();
    }

    public static void inflate(Drawable drawable, Resources res, XmlPullParser parser, AttributeSet attrs, Theme t) throws IOException, XmlPullParserException {
        drawable.inflate(res, parser, attrs, t);
    }
}
