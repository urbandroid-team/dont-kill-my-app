package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserManager;
import android.util.Log;
import java.util.LinkedHashSet;
import java.util.Set;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.Function */
public abstract class Function {
    private static final int CHECK_TIMES = 100;
    private static final String CLOSE_FUNCTION_SETTINGS = "close_function_settings";
    private static final int DELAY_TIME = 300;
    protected static final String TAG = "Function";
    private final boolean mCloseFunctionValue;
    protected Context mContext;
    private Function mFunction;
    private SharedPreferences mSharedPreferences;

    public abstract boolean get();

    public abstract void set(boolean z);

    public Function(Context context) {
        this(context, false, null);
    }

    public Function(Context context, Function function) {
        this(context, false, function);
    }

    public Function(Context context, boolean closeFunctionValue) {
        this(context, closeFunctionValue, null);
    }

    public Function(Context context, boolean closeFunctionValue, Function function) {
        this.mFunction = null;
        this.mContext = context;
        this.mCloseFunctionValue = closeFunctionValue;
        this.mSharedPreferences = this.mContext.getSharedPreferences(CLOSE_FUNCTION_SETTINGS, 0);
        this.mFunction = function;
    }

    private void save() {
        String key = getClass().getSimpleName();
        if (!containPreference(key)) {
            savePreference(key, get());
        }
    }

    public void close() {
        if (!forceIgnore()) {
            if (this.mFunction != null) {
                this.mFunction.close();
            }
            String key = getClass().getSimpleName();
            if (get() != this.mCloseFunctionValue) {
                save();
                boolean value = get();
                if (setValueWithCheckTimes(this.mCloseFunctionValue, 100)) {
                    Log.i(TAG, key + " to " + this.mCloseFunctionValue + " from " + value);
                } else {
                    Log.i(TAG, "after delay 30000ms," + key + "is still " + value + ", it may need more time to check.");
                }
            }
        }
    }

    private boolean setValueWithCheckTimes(boolean value, int checkTimes) {
        set(value);
        for (int i = 0; i < checkTimes; i++) {
            if (value == get()) {
                return true;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void restore() {
        String key = getClass().getSimpleName();
        if (containPreference(key)) {
            boolean defaultValue = readPreference(key);
            if (get() != defaultValue) {
                boolean value = get();
                if (setValueWithCheckTimes(defaultValue, 100)) {
                    Log.i(TAG, key + " to " + defaultValue + " from " + value);
                } else {
                    Log.i(TAG, "after delay 30000ms," + key + "is still " + value + ", it may need more time to check.");
                }
            }
            removePreference(key);
        }
        if (this.mFunction != null) {
            this.mFunction.restore();
        }
    }

    public boolean activated() {
        return containPreference(getClass().getSimpleName());
    }

    protected boolean containPreference(String key) {
        return this.mSharedPreferences.contains(key);
    }

    protected boolean readPreference(String key) {
        return this.mSharedPreferences.getBoolean(key, false);
    }

    protected void savePreference(String key, boolean value) {
        this.mSharedPreferences.edit().putBoolean(key, value).commit();
    }

    protected void removePreference(String key) {
        this.mSharedPreferences.edit().remove(key).commit();
    }

    protected void savePreference(String key, Set<String> values) {
        this.mSharedPreferences.edit().putStringSet(key, values).commit();
    }

    protected Set<String> readPreferenceSet(String key) {
        return this.mSharedPreferences.getStringSet(key, new LinkedHashSet());
    }

    public boolean forceIgnore() {
        if (this.mFunction != null) {
            return this.mFunction.forceIgnore();
        }
        return false;
    }

    public void release() {
        if (this.mFunction != null) {
            this.mFunction.release();
        }
    }

    protected boolean hasUserRestriction(String userRestriction) {
        return ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction(userRestriction);
    }
}
