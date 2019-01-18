package android.support.v4.widget;

import android.widget.TextView;

class TextViewCompatJb {
    TextViewCompatJb() {
    }

    static int getMaxLines(TextView textView) {
        return textView.getMaxLines();
    }

    static int getMinLines(TextView textView) {
        return textView.getMinLines();
    }
}
