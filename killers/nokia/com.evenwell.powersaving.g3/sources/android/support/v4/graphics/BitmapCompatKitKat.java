package android.support.v4.graphics;

import android.graphics.Bitmap;

class BitmapCompatKitKat {
    BitmapCompatKitKat() {
    }

    static int getAllocationByteCount(Bitmap bitmap) {
        return bitmap.getAllocationByteCount();
    }
}
