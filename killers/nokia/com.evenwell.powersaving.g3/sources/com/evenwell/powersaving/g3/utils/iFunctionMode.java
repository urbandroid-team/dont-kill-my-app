package com.evenwell.powersaving.g3.utils;

import android.content.Context;

public interface iFunctionMode {
    void registerReceiver(Context context);

    void unregisterReceiver(Context context);
}
