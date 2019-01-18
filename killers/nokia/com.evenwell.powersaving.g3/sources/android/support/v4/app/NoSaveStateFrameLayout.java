package android.support.v4.app;

import android.content.Context;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

class NoSaveStateFrameLayout extends FrameLayout {
    static ViewGroup wrap(View child) {
        NoSaveStateFrameLayout wrapper = new NoSaveStateFrameLayout(child.getContext());
        LayoutParams childParams = child.getLayoutParams();
        if (childParams != null) {
            wrapper.setLayoutParams(childParams);
        }
        child.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        wrapper.addView(child);
        return wrapper;
    }

    public NoSaveStateFrameLayout(Context context) {
        super(context);
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }
}
