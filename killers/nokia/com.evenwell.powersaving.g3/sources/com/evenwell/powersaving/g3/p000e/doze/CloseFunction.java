package com.evenwell.powersaving.g3.p000e.doze;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.p000e.doze.function.AutoSync;
import com.evenwell.powersaving.g3.p000e.doze.function.Bluetooth;
import com.evenwell.powersaving.g3.p000e.doze.function.BluetoothScan;
import com.evenwell.powersaving.g3.p000e.doze.function.DataSaver;
import com.evenwell.powersaving.g3.p000e.doze.function.DoubleTap;
import com.evenwell.powersaving.g3.p000e.doze.function.Function;
import com.evenwell.powersaving.g3.p000e.doze.function.GPS;
import com.evenwell.powersaving.g3.p000e.doze.function.Glance;
import com.evenwell.powersaving.g3.p000e.doze.function.SyncAdapter;
import com.evenwell.powersaving.g3.p000e.doze.function.Wifi;
import com.evenwell.powersaving.g3.p000e.doze.function.WifiScan;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/* renamed from: com.evenwell.powersaving.g3.e.doze.CloseFunction */
public class CloseFunction {
    private static final String TAG = "CloseFunction";
    private Context mContext;
    private Map<String, Function> mDeepFunctions = new HashMap();
    private Map<String, Function> mLightFunctions = new HashMap();

    public CloseFunction(Context context) {
        this.mContext = context;
        String[] deepFunctions = context.getResources().getStringArray(C0321R.array.deep_close_function);
        Log.i(TAG, "deepFunctionSettings = " + TextUtils.join(",", deepFunctions));
        addFunctions(deepFunctions, this.mDeepFunctions);
        String[] lightFunctions = context.getResources().getStringArray(C0321R.array.light_close_function);
        Log.i(TAG, "lightFunctionSettings = " + TextUtils.join(",", lightFunctions));
        addFunctions(lightFunctions, this.mLightFunctions);
    }

    private void addFunctions(String[] functionSettings, Map<String, Function> functions) {
        for (String function : functionSettings) {
            if (function.equals("AutoSync")) {
                functions.put(function, new AutoSync(this.mContext));
            } else if (function.equals("Bluetooth")) {
                functions.put(function, new Bluetooth(this.mContext));
            } else if (function.equals("BluetoothScan")) {
                functions.put(function, new BluetoothScan(this.mContext));
            } else if (function.equals("DoubleTap")) {
                functions.put(function, new DoubleTap(this.mContext));
            } else if (function.equals("Glance")) {
                functions.put(function, new Glance(this.mContext));
            } else if (function.equals("GPS")) {
                functions.put(function, new GPS(this.mContext));
            } else if (function.equals("Wifi")) {
                functions.put(function, new Wifi(this.mContext));
            } else if (function.equals("WifiScan")) {
                functions.put(function, new WifiScan(this.mContext));
            } else if (function.equals("DataSaver")) {
                functions.put(function, new DataSaver(this.mContext));
            } else if (function.equals("SyncAdapter")) {
                functions.put(function, new SyncAdapter(this.mContext));
            }
        }
    }

    public void release() {
        for (Entry<String, Function> entry : this.mDeepFunctions.entrySet()) {
            ((Function) entry.getValue()).release();
        }
        for (Entry<String, Function> entry2 : this.mLightFunctions.entrySet()) {
            ((Function) entry2.getValue()).release();
        }
        this.mDeepFunctions.clear();
        this.mLightFunctions.clear();
    }

    public String getApkName() {
        try {
            String apk = this.mContext.getPackageManager().getApplicationInfo(this.mContext.getPackageName(), 0).publicSourceDir;
            Log.i(TAG, "apk = " + apk);
            return apk;
        } catch (Throwable th) {
            return null;
        }
    }

    public void restoreDeepFunction() {
        for (Entry<String, Function> entry : this.mDeepFunctions.entrySet()) {
            ((Function) entry.getValue()).restore();
        }
    }

    public void closeDeepFunction() {
        for (Entry<String, Function> entry : this.mDeepFunctions.entrySet()) {
            ((Function) entry.getValue()).close();
        }
    }

    public void restoreLightFunction() {
        for (Entry<String, Function> entry : this.mLightFunctions.entrySet()) {
            ((Function) entry.getValue()).restore();
        }
    }

    public void closeLightFunction() {
        for (Entry<String, Function> entry : this.mLightFunctions.entrySet()) {
            ((Function) entry.getValue()).close();
        }
    }

    public Function getFunction(String key) {
        Function function = (Function) this.mLightFunctions.get(key);
        if (function == null) {
            return (Function) this.mDeepFunctions.get(key);
        }
        return function;
    }

    public String dump() {
        List<String> lightFunctions = new ArrayList();
        List<String> deeptFunctions = new ArrayList();
        for (Entry<String, Function> entry : this.mLightFunctions.entrySet()) {
            lightFunctions.add(entry.getKey());
        }
        for (Entry<String, Function> entry2 : this.mDeepFunctions.entrySet()) {
            deeptFunctions.add(entry2.getKey());
        }
        return "LightFunction:" + TextUtils.join(",", lightFunctions) + ", DeepFunction:" + TextUtils.join(",", deeptFunctions);
    }

    public static int lightFunctionSize(Context context) {
        String[] lightFunctions = context.getResources().getStringArray(C0321R.array.light_close_function);
        Log.i(TAG, "lightFunctions.length = " + lightFunctions.length);
        return lightFunctions.length;
    }

    public static int deepFunctionSize(Context context) {
        String[] deepFunctions = context.getResources().getStringArray(C0321R.array.deep_close_function);
        Log.i(TAG, "deepFunctions.length = " + deepFunctions.length);
        return deepFunctions.length;
    }
}
