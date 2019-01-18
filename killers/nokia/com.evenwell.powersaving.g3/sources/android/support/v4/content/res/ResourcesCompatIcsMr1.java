package android.support.v4.content.res;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;

class ResourcesCompatIcsMr1 {
    ResourcesCompatIcsMr1() {
    }

    public static Drawable getDrawableForDensity(Resources res, int id, int density) throws NotFoundException {
        return res.getDrawableForDensity(id, density);
    }
}
