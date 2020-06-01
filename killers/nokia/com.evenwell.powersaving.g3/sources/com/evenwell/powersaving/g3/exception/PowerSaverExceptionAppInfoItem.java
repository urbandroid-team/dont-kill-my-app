package com.evenwell.powersaving.g3.exception;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public final class PowerSaverExceptionAppInfoItem implements Comparable<PowerSaverExceptionAppInfoItem> {
    String mAppName;
    boolean mHighConsumption;
    Drawable mIcon;
    String mPackageName;
    int mUid;

    public String GetPackageName() {
        return this.mPackageName;
    }

    public Drawable GetIcon() {
        return this.mIcon;
    }

    public String GetAppName() {
        return this.mAppName;
    }

    public int GetUid() {
        return this.mUid;
    }

    public int compareTo(@NonNull PowerSaverExceptionAppInfoItem powerSaverExceptionAppInfoItem) {
        if (GetAppName() == null || powerSaverExceptionAppInfoItem == null || powerSaverExceptionAppInfoItem.GetAppName() == null) {
            return -1;
        }
        return GetAppName().compareToIgnoreCase(powerSaverExceptionAppInfoItem.GetAppName());
    }
}
