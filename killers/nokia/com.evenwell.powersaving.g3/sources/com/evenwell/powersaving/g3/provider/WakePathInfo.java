package com.evenwell.powersaving.g3.provider;

import android.graphics.drawable.Drawable;

public class WakePathInfo {
    public long id;
    public boolean isForbidden;
    public String mCallAppName;
    public Drawable mCallIcon;
    public String mCallPackageName;
    public int mForbiddenNum;
    public long mLastWakeTime;
    public String mPackageName;
    public int mWakeTime;
}
