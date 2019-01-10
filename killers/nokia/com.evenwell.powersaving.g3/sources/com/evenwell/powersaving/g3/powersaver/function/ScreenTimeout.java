package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.database.ContentObserver;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;

public class ScreenTimeout extends Function {
    private String DEFAULT_TIMEOUT;
    private ContentObserver mScreenTimeoutSettingObserver;

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            ScreenTimeout.this.setEnable(ScreenTimeout.this.getValueFromDB());
            Log.i("Function", "ScreenTimeout registerContentObserver");
            ScreenTimeout.this.mContext.getContentResolver().registerContentObserver(System.getUriFor("screen_off_timeout"), true, ScreenTimeout.this.mScreenTimeoutSettingObserver);
        }

        public void onRestore() {
            ScreenTimeout.this.mContext.getContentResolver().unregisterContentObserver(ScreenTimeout.this.mScreenTimeoutSettingObserver);
            String value = ScreenTimeout.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                ScreenTimeout.this.setEnable(value);
            }
            Log.i("Function", "ScreenTimeout unregisterContentObserver");
        }
    }

    private class ScreenTimeoutSettingObserver extends ContentObserver {
        public ScreenTimeoutSettingObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            String screenTimeout = LpmUtils.GetScreenTimeout(ScreenTimeout.this.mContext);
            Log.i("Function", "savePreference : " + ScreenTimeout.this.getRefBackUpFileKey() + " = " + screenTimeout);
            ScreenTimeout.this.savePreference(ScreenTimeout.this.getRefBackUpFileKey(), screenTimeout);
            Log.i("Function", "Screen timeout on change screenTimeout:" + screenTimeout);
        }
    }

    public ScreenTimeout(Context context) {
        super(context);
        this.mScreenTimeoutSettingObserver = null;
        this.DEFAULT_TIMEOUT = "15000";
        this.mScreenTimeoutSettingObserver = new ScreenTimeoutSettingObserver();
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.SCREEN_TIMEOUT;
    }

    protected String getDBKey() {
        return LPMDB.SCREEN_TIMEOUT;
    }

    public boolean getEnabled() {
        return !this.isClose;
    }

    protected void setEnable(String value) {
        if (!value.equals(SWITCHER.KEEP)) {
            if (value.equals(SWITCHER.ON) || value.equals(SWITCHER.OFF)) {
                Log.i("Function", "skip PSConst.SWITCHER.ON and PSConst.SWITCHER.OFF");
                value = this.DEFAULT_TIMEOUT;
            }
            Log.i("Function", "Timeout = " + value);
            try {
                LpmUtils.SetScreenTimeout(this.mContext, value);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void saveCurrentStateToBackUpFile() {
        String key = getRefBackUpFileKey();
        String value = "";
        if (SWITCHER.KEEP.equals(getValueFromDB())) {
            value = SWITCHER.KEEP;
        } else {
            value = LpmUtils.GetScreenTimeout(this.mContext);
        }
        Log.i("Function", "savePreference : " + key + " = " + value);
        savePreference(key, value);
    }

    public void bootHandling(int mode) {
        Log.i("Function", "[ScreenTimeout]: bootHandling() mode = " + mode);
        if (mode != -1) {
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor("screen_off_timeout"), true, this.mScreenTimeoutSettingObserver);
        }
    }
}
