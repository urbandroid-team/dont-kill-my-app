package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.p000e.doze.DataSaverBackend;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.DataSaverBlackList */
public class DataSaverBlackList extends Function {
    private static final String KEY_DATA_SAVER_BLACKLIST = "data_saver_blacklist";
    private DataSaverBackend mDataSaverBackend;

    public DataSaverBlackList(Context context) {
        super(context, true);
        this.mDataSaverBackend = new DataSaverBackend(context);
    }

    public boolean get() {
        return containPreference(KEY_DATA_SAVER_BLACKLIST);
    }

    public void set(boolean value) {
        Set<String> excludeList = getPackageUsingFCMorGCMPackage();
        Log.d("Function", "package using gcm or fcm : " + excludeList);
        this.mDataSaverBackend.refreshBlacklist();
        int uid;
        if (value) {
            Set<String> appsToAddToDataSaverBlackList = new ArraySet();
            Set<String> blackList = new LinkedHashSet();
            BackgroundPolicyExecutor bpe = BackgroundPolicyExecutor.getInstance(this.mContext);
            List<String> bamAppList = bpe.getAppsShowOnBAMUI();
            List<String> bamWhiteList = bpe.getWhiteListApp();
            for (String pkg : bamAppList) {
                if (bamWhiteList.contains(pkg) && !excludeList.contains(pkg)) {
                    appsToAddToDataSaverBlackList.add(pkg);
                    Log.d("Function", "[WW/CN] add " + pkg + " to datasaver black list");
                }
            }
            if (PSUtils.isCNModel(this.mContext)) {
                for (String pkg2 : bamAppList) {
                    if (!(bpe.isInDisautoList(pkg2) || excludeList.contains(pkg2))) {
                        appsToAddToDataSaverBlackList.add(pkg2);
                        Log.d("Function", "[CN] add " + pkg2 + " to datasaver black list");
                    }
                }
            }
            for (String pkgName : appsToAddToDataSaverBlackList) {
                uid = PSUtils.getUid(this.mContext, pkgName);
                if (!(uid == -1 || !UserHandle.isApp(uid) || this.mDataSaverBackend.isBlacklisted(uid))) {
                    this.mDataSaverBackend.setIsBlacklisted(uid, pkgName, true);
                    blackList.add(pkgName);
                }
            }
            savePreference(KEY_DATA_SAVER_BLACKLIST, (Set) blackList);
        } else if (containPreference(KEY_DATA_SAVER_BLACKLIST)) {
            for (String pkgName2 : readPreferenceSet(KEY_DATA_SAVER_BLACKLIST)) {
                uid = PSUtils.getUid(this.mContext, pkgName2);
                if (uid != -1 && this.mDataSaverBackend.isBlacklisted(uid)) {
                    this.mDataSaverBackend.setIsBlacklisted(uid, pkgName2, false);
                }
            }
            removePreference(KEY_DATA_SAVER_BLACKLIST);
        }
    }

    public boolean forceIgnore() {
        if (!super.forceIgnore()) {
            return false;
        }
        Log.i("Function", "do not change data saver state");
        return true;
    }

    private Set<String> getPackageUsingFCMorGCMPackage() {
        Set<String> ret = new ArraySet();
        for (ResolveInfo ri : this.mContext.getPackageManager().queryIntentServices(new Intent("com.google.firebase.MESSAGING_EVENT"), 0)) {
            if (ri.serviceInfo != null) {
                ret.add(ri.serviceInfo.packageName);
            }
        }
        for (ResolveInfo ri2 : this.mContext.getPackageManager().queryIntentServices(new Intent("com.google.android.c2dm.intent.RECEIVE"), 0)) {
            if (ri2.serviceInfo != null) {
                ret.add(ri2.serviceInfo.packageName);
            }
        }
        for (ResolveInfo ri22 : this.mContext.getPackageManager().queryBroadcastReceivers(new Intent("com.google.android.c2dm.intent.RECEIVE"), 0)) {
            if (ri22.activityInfo != null) {
                ret.add(ri22.activityInfo.packageName);
            }
        }
        return ret;
    }
}
