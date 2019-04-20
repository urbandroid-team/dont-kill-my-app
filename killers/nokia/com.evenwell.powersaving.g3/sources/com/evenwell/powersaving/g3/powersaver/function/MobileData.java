package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.text.TextUtils;
import com.evenwell.powersaving.g3.lpm.LpmObserverUtils;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;

public class MobileData extends Function {

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            MobileData.this.setEnable(MobileData.this.getValueFromDB());
        }

        public void onRestore() {
            String value = MobileData.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                MobileData.this.setEnable(value);
            }
        }
    }

    public MobileData(Context context) {
        super(context);
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.MOBILE_DATA;
    }

    protected String getDBKey() {
        return LPMDB.MOBILE_DATA;
    }

    public boolean getEnabled() {
        return LpmObserverUtils.GetMobileEnableForSyncBackUpFile(this.mContext);
    }

    protected void setEnable(String value) {
        LpmUtils.SetMobileDataEnable(this.mContext, value);
    }
}
