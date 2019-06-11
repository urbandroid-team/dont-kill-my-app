package com.evenwell.powersaving.g3.exception;

import android.content.Context;
import android.util.ArraySet;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.provider.BackDataDb;
import java.util.Set;

public class BackgroundCleanWhitelist {
    private static final String TAG = "[PowerSavingAppG3]BackgroundCleanWhitelist";
    private static BackgroundCleanWhitelist mInstance = null;
    private Context ctx;
    private Set<String> mAllWhitelistedApps = new ArraySet();

    public static BackgroundCleanWhitelist getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new BackgroundCleanWhitelist(ctx);
        }
        return mInstance;
    }

    private BackgroundCleanWhitelist(Context ctx) {
        this.ctx = ctx;
        refreshList();
    }

    public synchronized int getWhitelistSize() {
        return this.mAllWhitelistedApps.size();
    }

    public synchronized boolean isWhitelisted(String pkg) {
        return this.mAllWhitelistedApps.contains(pkg);
    }

    public synchronized void add(String pkg) {
        try {
            PowerSavingUtils.addAppToWhiteList(this.ctx, pkg);
            this.mAllWhitelistedApps.add(pkg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void remove(String pkg) {
        try {
            PowerSavingUtils.removeAppFromWhiteList(this.ctx, pkg);
            this.mAllWhitelistedApps.remove(pkg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void refreshList() {
        this.mAllWhitelistedApps.clear();
        try {
            BackDataDb db = new BackDataDb(this.ctx);
            this.mAllWhitelistedApps = db.getAllWhiteListPkg();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized Set<String> getWhiteList() {
        return cloneApp(this.mAllWhitelistedApps);
    }

    private synchronized Set<String> cloneApp(Set<String> apps) {
        Set<String> cloneApps;
        cloneApps = new ArraySet();
        cloneApps.addAll(apps);
        return cloneApps;
    }
}
