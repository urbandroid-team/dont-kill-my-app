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

public class BlueTooth extends Function {
    private ContentObserver mBTSettingObserver;
    private boolean setbySelf;

    private class BTSettingObserver extends ContentObserver {
        public BTSettingObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            boolean bBTenable = BlueTooth.this.getEnabled();
            if (!BlueTooth.this.setbySelf) {
                BlueTooth.this.savePreference(BlueTooth.this.getRefBackUpFileKey(), LpmUtils.BooleanToString_NoKeep(bBTenable));
            }
            BlueTooth.this.setbySelf = false;
            Log.i("Function", "BT enable" + bBTenable);
        }
    }

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            Log.i("Function", "BT registerContentObserver");
            BlueTooth.this.setEnable(BlueTooth.this.getValueFromDB());
            BlueTooth.this.setbySelf = true;
            BlueTooth.this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("bluetooth_on"), true, BlueTooth.this.mBTSettingObserver);
        }

        public void onRestore() {
            BlueTooth.this.mContext.getContentResolver().unregisterContentObserver(BlueTooth.this.mBTSettingObserver);
            String value = BlueTooth.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                BlueTooth.this.setEnable(value);
            }
            Log.i("Function", "BT unregisterContentObserver");
        }
    }

    public BlueTooth(Context context) {
        super(context);
        this.mBTSettingObserver = null;
        this.setbySelf = false;
        this.mBTSettingObserver = new BTSettingObserver();
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.BT;
    }

    protected String getDBKey() {
        return LPMDB.BT;
    }

    public boolean getEnabled() {
        return LpmObserverUtils.GetBTEnableForSyncBackUpFile(this.mContext);
    }

    protected void setEnable(String value) {
        LpmUtils.SetBTEnable(this.mContext, value);
    }

    public void bootHandling(int mode) {
        Log.i("Function", "[BlueTooth]: bootHandling() mode = " + mode);
        if (mode != -1) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("bluetooth_on"), true, this.mBTSettingObserver);
        }
    }
}
