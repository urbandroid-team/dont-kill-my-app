package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkPolicyManager;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;

public class LimitBackgroundData extends Function {
    private final NetworkPolicyManager mPolicyManager;

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            if (SWITCHER.ON.equals(LimitBackgroundData.this.getValueFromDB())) {
                LimitBackgroundData.this.setEnable(SWITCHER.OFF);
            } else {
                LimitBackgroundData.this.setEnable(SWITCHER.ON);
            }
        }

        public void onRestore() {
            String value = LimitBackgroundData.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                LimitBackgroundData.this.setEnable(value);
            }
        }
    }

    public LimitBackgroundData(Context context) {
        super(context);
        setListener(new Listener());
        this.mPolicyManager = NetworkPolicyManager.from(context);
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.BACKGROUND_DATA;
    }

    protected String getDBKey() {
        return LPMDB.BACKGROUND_DATA;
    }

    public boolean getEnabled() {
        return this.mPolicyManager.getRestrictBackground();
    }

    protected void setEnable(String value) {
        if (!SWITCHER.KEEP.equals(value)) {
            if (hasUserRestriction("no_config_tethering")) {
                Log.i("Function", "hasUserRestriction UserManager.DISALLOW_CONFIG_TETHERING, do not change Data Saver state.");
                return;
            }
            boolean enabled = getEnabled();
            if (SWITCHER.ON.equals(value)) {
                enabled = true;
            } else {
                enabled = false;
            }
            new Intent(ACTION.ACTION_NOW_IN_LPM).putExtra(EXTRA.IN_LPM, enabled);
            Log.i("Function", "LimitBackgroundData SendIntentToFrameworkForLPM() enabled =" + enabled);
            if (SWITCHER.ON.equals(value)) {
                Log.i("Function", "[LpmUtils] SendIntentToFrameworkForLPM() mLPMBD = ON");
                this.mPolicyManager.setRestrictBackground(true);
            } else if (SWITCHER.OFF.equals(value)) {
                Log.i("Function", "[LpmUtils] SendIntentToFrameworkForLPM() mLPMBD = OFF");
                this.mPolicyManager.setRestrictBackground(false);
            } else {
                Log.i("Function", "[LpmUtils] SendIntentToFrameworkForLPM() mLPMBD = KEEP");
            }
        }
    }
}
