package android.support.v4.widget;

import android.graphics.drawable.Drawable;
import android.widget.CompoundButton;

class CompoundButtonCompatApi23 {
    CompoundButtonCompatApi23() {
    }

    static Drawable getButtonDrawable(CompoundButton button) {
        return button.getButtonDrawable();
    }
}
