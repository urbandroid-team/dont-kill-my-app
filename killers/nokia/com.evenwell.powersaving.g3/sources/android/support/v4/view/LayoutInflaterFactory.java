package android.support.v4.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public interface LayoutInflaterFactory {
    View onCreateView(View view, String str, Context context, AttributeSet attributeSet);
}
