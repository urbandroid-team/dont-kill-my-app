package com.evenwell.powersaving.g3.exception;

import android.content.Context;
import android.os.IDeviceIdleController;
import android.os.IDeviceIdleController.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArraySet;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PowerWhitelistBackend {
    private static final String DEVICE_IDLE_SERVICE = "deviceidle";
    private static final String TAG = "[PowerSavingAppG3]PowerWhitelistBackend";
    private static PowerWhitelistBackend mInstance = null;
    private List<String> cts;
    private Context ctx;
    private IDeviceIdleController mDeviceIdleService = null;
    private Set<String> mSysWhitelistedApps = new ArraySet();

    public static PowerWhitelistBackend getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new PowerWhitelistBackend(ctx);
        }
        return mInstance;
    }

    private PowerWhitelistBackend(Context ctx) {
        this.ctx = ctx;
        this.mDeviceIdleService = Stub.asInterface(ServiceManager.getService(DEVICE_IDLE_SERVICE));
        refreshList();
        this.cts = Arrays.asList(ctx.getResources().getStringArray(C0321R.array.cts_app));
    }

    public synchronized int getDozeWhitelistSize() {
        return this.mSysWhitelistedApps.size();
    }

    public synchronized boolean isDozeWhitelisted(String pkg) {
        return this.mSysWhitelistedApps.contains(pkg);
    }

    public synchronized void add(String pkg) {
        try {
            if (!this.cts.contains(pkg)) {
                this.mDeviceIdleService.addPowerSaveWhitelistApp(pkg);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to reach IDeviceIdleController", e);
        }
    }

    public synchronized void remove(String pkg) {
        try {
            if (!this.cts.contains(pkg)) {
                this.mDeviceIdleService.removePowerSaveWhitelistApp(pkg);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to reach IDeviceIdleController", e);
        }
    }

    public synchronized void refreshList() {
        int i = 0;
        synchronized (this) {
            this.mSysWhitelistedApps.clear();
            try {
                for (String app : this.mDeviceIdleService.getFullPowerWhitelist()) {
                    this.mSysWhitelistedApps.add(app);
                }
                String[] sysWhitelistedApps = this.mDeviceIdleService.getSystemPowerWhitelist();
                int length = sysWhitelistedApps.length;
                while (i < length) {
                    this.mSysWhitelistedApps.add(sysWhitelistedApps[i]);
                    i++;
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to reach IDeviceIdleController", e);
            }
        }
        return;
    }

    public synchronized Set<String> geDozeWhiteList() {
        return cloneApp(this.mSysWhitelistedApps);
    }

    private synchronized Set<String> cloneApp(Set<String> apps) {
        Set<String> cloneApps;
        cloneApps = new ArraySet();
        cloneApps.addAll(apps);
        return cloneApps;
    }
}
