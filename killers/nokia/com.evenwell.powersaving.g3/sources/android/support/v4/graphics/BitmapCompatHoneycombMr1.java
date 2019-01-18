package android.support.v4.graphics;

import android.graphics.Bitmap;

class BitmapCompatHoneycombMr1 {
    BitmapCompatHoneycombMr1() {
    }

    static int getAllocationByteCount(Bitmap bitmap) {
        return bitmap.getByteCount();
    }
}
