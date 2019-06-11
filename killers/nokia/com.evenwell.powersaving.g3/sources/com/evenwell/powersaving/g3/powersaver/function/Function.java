package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserManager;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;

public abstract class Function {
    private static final String BACK_UP_CLOSE_FUNCTION_SETTINGS = "power_saving_lpm_backup_file";
    protected static final boolean DBG = true;
    protected static final String TAG = "Function";
    protected boolean isClose = false;
    protected Context mContext;
    private eventListener mListener = null;
    private SharedPreferences mSharedPreferences;

    interface eventListener {
        void onClose();

        void onRestore();
    }

    protected abstract String getDBKey();

    public abstract boolean getEnabled();

    protected abstract String getRefBackUpFileKey();

    protected abstract void setEnable(String str);

    protected void setListener(eventListener listener) {
        this.mListener = listener;
    }

    public Function(Context context) {
        this.mContext = context;
        this.mSharedPreferences = this.mContext.getSharedPreferences("power_saving_lpm_backup_file", 0);
    }

    public void saveCurrentStateToBackUpFile() {
        String key = getRefBackUpFileKey();
        String value = "";
        if (SWITCHER.KEEP.equals(getValueFromDB())) {
            value = SWITCHER.KEEP;
        } else {
            value = LpmUtils.BooleanToString_NoKeep(getEnabled());
        }
        Log.i(TAG, "savePreference : " + key + " = " + value);
        savePreference(key, value);
    }

    public String readFromBackFile() {
        String key = getRefBackUpFileKey();
        Log.i(TAG, "key = " + key);
        if (!containPreference(key)) {
            return null;
        }
        String value = readPreference(key);
        Log.i(TAG, "readFromBackFile : " + key + " = " + value);
        return value;
    }

    public String getValueFromDB() {
        String value = PowerSavingUtils.getStringItemFromDB(this.mContext, getDBKey());
        Log.i(TAG, "getValueFromDB : " + getDBKey() + " = " + value);
        return value;
    }

    protected boolean containPreference(String key) {
        return this.mSharedPreferences.contains(key);
    }

    protected String readPreference(String key) {
        return this.mSharedPreferences.getString(key, null);
    }

    protected void savePreference(String key, String value) {
        this.mSharedPreferences.edit().putString(key, value).commit();
    }

    protected void removePreference(String key) {
        this.mSharedPreferences.edit().remove(key).commit();
    }

    public void release() {
    }

    public void close() {
        this.isClose = true;
        if (this.mListener != null) {
            this.mListener.onClose();
        }
        Log.i(TAG, getClass().getSimpleName() + " close");
    }

    public void restore() {
        this.isClose = false;
        if (this.mListener != null) {
            this.mListener.onRestore();
        }
        Log.i(TAG, getClass().getSimpleName() + " restore");
    }

    public void bootHandling(int mode) {
    }

    protected boolean hasUserRestriction(String userRestriction) {
        return ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction(userRestriction);
    }
}
