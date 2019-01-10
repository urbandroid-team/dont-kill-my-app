package android.support.v4.view;

import android.view.View;

class ViewPropertyAnimatorCompatLollipop {
    ViewPropertyAnimatorCompatLollipop() {
    }

    public static void translationZ(View view, float value) {
        view.animate().translationZ(value);
    }

    public static void translationZBy(View view, float value) {
        view.animate().translationZBy(value);
    }

    /* renamed from: z */
    public static void m14z(View view, float value) {
        view.animate().z(value);
    }

    public static void zBy(View view, float value) {
        view.animate().zBy(value);
    }
}
