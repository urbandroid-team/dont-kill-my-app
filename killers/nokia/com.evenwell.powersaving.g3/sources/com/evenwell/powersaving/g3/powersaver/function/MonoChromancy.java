package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;

public class MonoChromancy extends Function {

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            MonoChromancy.this.setEnable(MonoChromancy.this.getValueFromDB());
        }

        public void onRestore() {
            String value = MonoChromancy.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                MonoChromancy.this.setEnable(value);
            }
        }
    }

    public MonoChromancy(Context context) {
        super(context);
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.MONOCHROMACY;
    }

    protected String getDBKey() {
        return LPMDB.MONOCHROMACY;
    }

    public boolean getEnabled() {
        boolean enable = LpmUtils.getSimulateColorSpaceMode(this.mContext);
        Log.i("Function", "MonoChromancy enable = " + enable);
        return enable;
    }

    protected void setEnable(String value) {
        Log.i("Function", "MonoChromancy value = " + value);
        LpmUtils.setMonoChromacyEnabled(this.mContext, value);
    }
}
