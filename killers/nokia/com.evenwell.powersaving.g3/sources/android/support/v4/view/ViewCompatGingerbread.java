package android.support.v4.view;

import android.view.View;

class ViewCompatGingerbread {
    ViewCompatGingerbread() {
    }

    public static int getOverScrollMode(View v) {
        return v.getOverScrollMode();
    }

    public static void setOverScrollMode(View v, int mode) {
        v.setOverScrollMode(mode);
    }
}
