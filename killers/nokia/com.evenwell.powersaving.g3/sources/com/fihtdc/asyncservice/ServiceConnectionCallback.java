package com.fihtdc.asyncservice;

import android.content.ComponentName;
import android.os.IBinder;

public interface ServiceConnectionCallback {
    void onServiceConnected(ComponentName componentName, IBinder iBinder);

    void onServiceDisconnected(ComponentName componentName);
}
