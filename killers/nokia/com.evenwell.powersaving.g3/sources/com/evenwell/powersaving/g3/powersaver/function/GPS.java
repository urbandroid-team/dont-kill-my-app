package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.text.TextUtils;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;

public class GPS extends Function {

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            GPS.this.setEnable(GPS.this.getValueFromDB());
        }

        public void onRestore() {
            String value = GPS.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                GPS.this.setEnable(value);
            }
        }
    }

    public GPS(Context context) {
        super(context);
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.GPS;
    }

    protected String getDBKey() {
        return LPMDB.GPS;
    }

    public boolean getEnabled() {
        return LpmUtils.GetGPSEnable(this.mContext);
    }

    protected void setEnable(String value) {
        LpmUtils.SetGpsEnable(this.mContext, value);
    }
}
