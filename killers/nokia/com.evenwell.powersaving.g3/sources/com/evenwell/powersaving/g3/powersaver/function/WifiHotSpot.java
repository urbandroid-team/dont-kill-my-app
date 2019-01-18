package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.text.TextUtils;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;

public class WifiHotSpot extends Function {

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            WifiHotSpot.this.setEnable(WifiHotSpot.this.getValueFromDB());
        }

        public void onRestore() {
            String value = WifiHotSpot.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                WifiHotSpot.this.setEnable(value);
            }
        }
    }

    public WifiHotSpot(Context context) {
        super(context);
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.WIFI_HOTSPOT;
    }

    protected String getDBKey() {
        return LPMDB.WIFI_HOTSPOT;
    }

    public boolean getEnabled() {
        return LpmUtils.GetWifiAPEnabled(this.mContext);
    }

    protected void setEnable(String value) {
        LpmUtils.SetWifiHotspotEnable(this.mContext, value);
    }
}
