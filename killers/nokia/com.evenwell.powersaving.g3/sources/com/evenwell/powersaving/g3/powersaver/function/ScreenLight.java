package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;

public class ScreenLight extends Function {

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            ScreenLight.this.setEnable(ScreenLight.this.getValueFromDB());
        }

        public void onRestore() {
            String value = ScreenLight.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                ScreenLight.this.setEnable(value);
            }
        }
    }

    public ScreenLight(Context context) {
        super(context);
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.SCREEN_LIGHT;
    }

    protected String getDBKey() {
        return LPMDB.SCREEN_LIGHT;
    }

    public boolean getEnabled() {
        return !this.isClose;
    }

    protected void setEnable(String value) {
        if (!SWITCHER.KEEP.equals(value)) {
            boolean isDecreaseBRIGHTNESS = false;
            if (SWITCHER.ON.equals(value)) {
                isDecreaseBRIGHTNESS = true;
            }
            Log.i("Function", "isDecreaseBRIGHTNESS = " + isDecreaseBRIGHTNESS);
        }
    }
}
