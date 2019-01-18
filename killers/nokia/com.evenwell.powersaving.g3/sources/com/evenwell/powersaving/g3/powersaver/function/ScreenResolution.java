package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.ScreenResolutionUtil;

public class ScreenResolution extends Function {
    private static final String KEY_DENSITY = "ScreenResolution_density";
    private int defaultDensity = 0;
    private IWindowManager mWm;
    public int rate = 75;

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            ScreenResolution.this.isClose = true;
            ScreenResolution.this.setEnable(ScreenResolution.this.getValueFromDB());
        }

        public void onRestore() {
            ScreenResolution.this.isClose = false;
            String value = ScreenResolution.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                ScreenResolution.this.setEnable(value);
            }
        }
    }

    public ScreenResolution(Context context) {
        super(context);
        setListener(new Listener());
        this.mWm = Stub.asInterface(ServiceManager.checkService("window"));
        try {
            this.defaultDensity = this.mWm.getInitialDisplayDensity(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.SCREEN_RESOLUTION;
    }

    protected String getDBKey() {
        return LPMDB.SCREEN_RESOLUTION;
    }

    public boolean getEnabled() {
        return !this.isClose;
    }

    protected void setEnable(String value) {
        if (!value.equals(SWITCHER.KEEP)) {
            boolean enabled;
            if (value.equals(SWITCHER.ON)) {
                enabled = true;
            } else {
                enabled = false;
            }
            if (enabled) {
                int density = readDensityFromBackFile();
                ScreenResolutionUtil.resetDisplaySize();
                ScreenResolutionUtil.setDensity(density);
                return;
            }
            saveCurrentDensityToBackUpFile();
            ScreenResolutionUtil.changeResoultionByRate(this.mContext, this.rate);
        }
    }

    public void saveCurrentDensityToBackUpFile() {
        try {
            int density = this.mWm.getBaseDisplayDensity(0);
            Log.i("Function", "savePreference : ScreenResolution_density = " + density);
            savePreference(KEY_DENSITY, Integer.toString(density));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int readDensityFromBackFile() {
        int density = 0;
        if (containPreference(KEY_DENSITY)) {
            String value = readPreference(KEY_DENSITY);
            if (value != null) {
                density = Integer.parseInt(value);
            }
            Log.i("Function", "readFromBackFile : ScreenResolution_density = " + density);
        }
        if (density == 0) {
            return this.defaultDensity;
        }
        return density;
    }
}
