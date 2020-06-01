package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;

public class Vibrate extends Function {

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            Vibrate.this.isClose = true;
            Vibrate.this.setEnable(Vibrate.this.getValueFromDB());
        }

        public void onRestore() {
            Vibrate.this.isClose = false;
            String value = Vibrate.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                Vibrate.this.setEnable(value);
            }
        }
    }

    public Vibrate(Context context) {
        super(context);
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.VIBRATION;
    }

    protected String getDBKey() {
        return LPMDB.VIBRATION;
    }

    public boolean getEnabled() {
        return !this.isClose;
    }

    protected void setEnable(String value) {
        if (!SWITCHER.KEEP.equals(value)) {
            boolean enabled;
            if (SWITCHER.ON.equals(value)) {
                enabled = true;
            } else {
                enabled = false;
            }
            Intent NoticeIntent = new Intent(ACTION.ACTION_NOW_IN_LPM);
            NoticeIntent.putExtra(EXTRA.IN_LPM, enabled);
            Log.i("Function", "Vibrate SendIntentToFrameworkForLPM() enabled =" + enabled);
            if (SWITCHER.ON.equals(value)) {
                Log.i("Function", "[LpmUtils] SendIntentToFrameworkForLPM() mLPMVibrate = ON");
                NoticeIntent.putExtra(EXTRA.IN_LPM_VIBRATE, 1);
            } else if (SWITCHER.OFF.equals(value)) {
                Log.i("Function", "[LpmUtils] SendIntentToFrameworkForLPM() mLPMVibrate = OFF");
                NoticeIntent.putExtra(EXTRA.IN_LPM_VIBRATE, 0);
            } else {
                Log.i("Function", "[LpmUtils] SendIntentToFrameworkForLPM() mLPMVibrate = KEEP");
            }
            this.mContext.sendBroadcast(NoticeIntent);
        }
    }

    public void bootHandling(int mode) {
        Log.i("Function", "[Vibrate]: bootHandling() mode = " + mode);
        if (mode != -1) {
            setEnable(getValueFromDB());
        }
    }
}
