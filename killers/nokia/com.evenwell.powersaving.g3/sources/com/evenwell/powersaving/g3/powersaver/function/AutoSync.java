package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.text.TextUtils;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;

public class AutoSync extends Function {
    private String currentState = "";

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            AutoSync.this.setEnable(AutoSync.this.getValueFromDB());
        }

        public void onRestore() {
            String value = AutoSync.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                AutoSync.this.setEnable(value);
            }
        }
    }

    public AutoSync(Context context) {
        super(context);
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.AUTOSYNC;
    }

    protected String getDBKey() {
        return LPMDB.AUTOSYNC;
    }

    public boolean getEnabled() {
        return LpmUtils.getAutoSyncEnabled(this.mContext);
    }

    protected void setEnable(String value) {
        LpmUtils.setAutoSyncEnabled(this.mContext, value);
    }
}
