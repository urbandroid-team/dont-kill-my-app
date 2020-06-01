package com.evenwell.powersaving.g3.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import com.evenwell.powersaving.g3.background.BackgroundCleanUtil;
import com.evenwell.powersaving.g3.utils.PSConst.SS.SSPARM;

public class WifiManagerUtils {
    public static final int INVALID_ID = -1;

    public static int getWifiApNumClients(@NonNull Context context) {
        int num = -1;
        try {
            if (has_getWifiApNumClients_API()) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(SSPARM.WIFI);
                num = ((Integer) wifiManager.getClass().getDeclaredMethod("getWifiApNumClients", new Class[0]).invoke(wifiManager, new Object[0])).intValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    public static boolean has_getWifiApNumClients_API() {
        return BackgroundCleanUtil.hasMethod(WifiManager.class, "getWifiApNumClients", new Class[0]);
    }
}
