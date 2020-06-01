package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.database.ContentObserver;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.glance.GlanceUtil;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;

public class Glance extends Function {
    private GlanceSettingObserver mGlanceSettingObserver = null;
    private boolean setbySelf = false;

    private class GlanceSettingObserver extends ContentObserver {
        public GlanceSettingObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            boolean bGlanceEnable = Glance.this.getEnabled();
            if (!Glance.this.setbySelf) {
                Glance.this.savePreference(Glance.this.getRefBackUpFileKey(), LpmUtils.BooleanToString_NoKeep(bGlanceEnable));
            }
            Glance.this.setbySelf = false;
            Log.i("Function", "Glance enable" + bGlanceEnable);
        }
    }

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            if (GlanceUtil.isSupportGlanceMode(Glance.this.mContext.getPackageManager())) {
                Glance.this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(GlanceUtil.DOZE_ENABLED), true, Glance.this.mGlanceSettingObserver);
                Glance.this.setbySelf = true;
            }
            Glance.this.setEnable(Glance.this.getValueFromDB());
        }

        public void onRestore() {
            if (GlanceUtil.isSupportGlanceMode(Glance.this.mContext.getPackageManager())) {
                Glance.this.mContext.getContentResolver().unregisterContentObserver(Glance.this.mGlanceSettingObserver);
            }
            String value = Glance.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                Glance.this.setEnable(value);
            }
        }
    }

    public Glance(Context context) {
        super(context);
        setListener(new Listener());
        this.mGlanceSettingObserver = new GlanceSettingObserver();
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.GLANCE;
    }

    protected String getDBKey() {
        return LPMDB.GLANCE;
    }

    public boolean getEnabled() {
        return GlanceUtil.getGlanceModeEnable(this.mContext);
    }

    protected void setEnable(String value) {
        GlanceUtil.setGlanceModeEnable(this.mContext, value);
    }

    public void bootHandling(int mode) {
        Log.i("Function", "[Glance]: bootHandling() mode = " + mode);
        if (mode != -1 && GlanceUtil.isSupportGlanceMode(this.mContext.getPackageManager())) {
            this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(GlanceUtil.DOZE_ENABLED), true, this.mGlanceSettingObserver);
        }
    }
}
