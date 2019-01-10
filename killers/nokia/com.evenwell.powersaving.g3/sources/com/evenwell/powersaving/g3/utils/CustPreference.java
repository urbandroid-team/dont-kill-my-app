package com.evenwell.powersaving.g3.utils;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;

public class CustPreference extends Preference {
    private static String TAG = TAG.PSLOG;
    private Context mContext;

    public CustPreference(Context context) {
        super(context);
        this.mContext = context;
    }

    public CustPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public CustPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public void onBindView(View view) {
        super.onBindView(view);
        int padWeight = (int) TypedValue.applyDimension(1, 56.0f, this.mContext.getResources().getDisplayMetrics());
        int padHeight = (int) TypedValue.applyDimension(1, 3.0f, this.mContext.getResources().getDisplayMetrics());
        TextView title = (TextView) view.findViewById(16908310);
        if (title != null) {
            title.setPadding(padWeight, padHeight, title.getPaddingRight(), title.getPaddingBottom());
        }
        TextView summary = (TextView) view.findViewById(16908304);
        if (summary != null) {
            summary.setPadding(padWeight, summary.getPaddingTop(), summary.getPaddingRight(), padHeight);
        }
    }
}
