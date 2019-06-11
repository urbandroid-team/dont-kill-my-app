package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.database.ContentObserver;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.exception.BMS;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;

public class BAMCleaner extends Function {
    private BAMSettingObserver mBAMSettingObserver = null;
    private BMS mBMS = null;

    private class BAMSettingObserver extends ContentObserver {
        public BAMSettingObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            boolean bBAMenable = BAMCleaner.this.getEnabled();
            Log.i("Function", "bBAMenable = " + bBAMenable);
            BAMCleaner.this.savePreference(BAMCleaner.this.getRefBackUpFileKey(), LpmUtils.BooleanToString_NoKeep(bBAMenable));
        }
    }

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            BAMCleaner.this.setEnable(BAMCleaner.this.getValueFromDB());
            BAMCleaner.this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(BMS.KEY_ORIGINAL_BMS_SETTINGS), true, BAMCleaner.this.mBAMSettingObserver);
        }

        public void onRestore() {
            BAMCleaner.this.mContext.getContentResolver().unregisterContentObserver(BAMCleaner.this.mBAMSettingObserver);
            String value = BAMCleaner.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                BAMCleaner.this.setEnable(value);
            }
        }
    }

    public BAMCleaner(Context context) {
        super(context);
        this.mBMS = BMS.getInstance(context);
        setListener(new Listener());
        this.mBAMSettingObserver = new BAMSettingObserver();
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.BAM;
    }

    protected String getDBKey() {
        return LPMDB.BAM;
    }

    public boolean getEnabled() {
        Log.i("Function", "BMS getEnabled = " + this.mBMS.getBMSValue());
        return this.mBMS.getBMSValue();
    }

    protected void setEnable(String value) {
        if (!SWITCHER.KEEP.equals(value)) {
            if (SWITCHER.ON.equals(value)) {
                Log.i("Function", "BAM set enable : " + value);
                this.mBMS.setBMSValue(true);
            } else if (SWITCHER.OFF.equals(value)) {
                Log.i("Function", "BAM set enable : " + value);
                this.mBMS.setBMSValue(false);
            }
        }
    }

    public void bootHandling(int mode) {
        Log.i("Function", "[BAMCleaner]: bootHandling() mode = " + mode);
        if (mode != -1) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(BMS.KEY_ORIGINAL_BMS_SETTINGS), true, this.mBAMSettingObserver);
        }
    }
}
