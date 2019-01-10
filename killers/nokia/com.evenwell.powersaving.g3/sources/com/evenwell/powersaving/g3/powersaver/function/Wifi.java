package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.database.ContentObserver;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.lpm.LpmObserverUtils;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;

public class Wifi extends Function {
    private ContentObserver mWiFiSettingObserver;
    private boolean setbySelf;

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            Wifi.this.setEnable(Wifi.this.getValueFromDB());
            Wifi.this.setbySelf = true;
            Log.i("Function", "Wifi registerContentObserver");
            Wifi.this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_on"), true, Wifi.this.mWiFiSettingObserver);
        }

        public void onRestore() {
            Wifi.this.mContext.getContentResolver().unregisterContentObserver(Wifi.this.mWiFiSettingObserver);
            String value = Wifi.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                Wifi.this.setEnable(value);
            }
            Log.i("Function", "Wifi unregisterContentObserver");
        }
    }

    private class WiFiSettingObserver extends ContentObserver {
        public WiFiSettingObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            boolean bWifienable = Wifi.this.getEnabled();
            if (!Wifi.this.setbySelf) {
                Wifi.this.savePreference(Wifi.this.getRefBackUpFileKey(), LpmUtils.BooleanToString_NoKeep(bWifienable));
            }
            Wifi.this.setbySelf = false;
            Log.i("Function", "Wifi enable" + bWifienable);
        }
    }

    public Wifi(Context context) {
        super(context);
        this.mWiFiSettingObserver = null;
        this.setbySelf = false;
        this.mWiFiSettingObserver = new WiFiSettingObserver();
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.WIFI;
    }

    protected String getDBKey() {
        return LPMDB.WIFI;
    }

    public boolean getEnabled() {
        Log.i("Function", "LpmObserverUtils.GetWiFiEnableForSyncBackUpFile(mContext) = " + LpmObserverUtils.GetWiFiEnableForSyncBackUpFile(this.mContext));
        return LpmObserverUtils.GetWiFiEnableForSyncBackUpFile(this.mContext);
    }

    protected void setEnable(String value) {
        LpmUtils.SetWifiEnable(this.mContext, value);
    }

    public void bootHandling(int mode) {
        Log.i("Function", "[Wifi]: bootHandling() mode = " + mode);
        if (mode != -1) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_on"), true, this.mWiFiSettingObserver);
        }
    }
}
