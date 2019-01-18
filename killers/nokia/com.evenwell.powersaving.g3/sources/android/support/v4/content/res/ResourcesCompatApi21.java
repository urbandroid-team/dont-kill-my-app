package android.support.v4.content.res;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;

class ResourcesCompatApi21 {
    ResourcesCompatApi21() {
    }

    public static Drawable getDrawable(Resources res, int id, Theme theme) throws NotFoundException {
        return res.getDrawable(id, theme);
    }

    public static Drawable getDrawableForDensity(Resources res, int id, int density, Theme theme) throws NotFoundException {
        return res.getDrawableForDensity(id, density, theme);
    }
}
