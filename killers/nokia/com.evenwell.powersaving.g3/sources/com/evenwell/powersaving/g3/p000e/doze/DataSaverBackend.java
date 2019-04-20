package com.evenwell.powersaving.g3.p000e.doze;

import android.content.Context;
import android.net.NetworkPolicyManager;
import android.util.Log;
import android.util.SparseIntArray;

/* renamed from: com.evenwell.powersaving.g3.e.doze.DataSaverBackend */
public class DataSaverBackend {
    private static final String TAG = "DataSaverBackend";
    private final Context mContext;
    private final NetworkPolicyManager mPolicyManager;
    private SparseIntArray mUidPolicies = new SparseIntArray();

    public DataSaverBackend(Context context) {
        this.mContext = context;
        this.mPolicyManager = NetworkPolicyManager.from(context);
    }

    public boolean isDataSaverEnabled() {
        return this.mPolicyManager.getRestrictBackground();
    }

    public void setDataSaverEnabled(boolean enabled) {
        this.mPolicyManager.setRestrictBackground(enabled);
    }

    public void refreshWhitelist() {
        loadWhitelist();
    }

    public void setIsWhitelisted(int uid, String packageName, boolean whitelisted) {
        int policy = whitelisted ? 4 : 0;
        this.mPolicyManager.setUidPolicy(uid, policy);
        this.mUidPolicies.put(uid, policy);
        if (whitelisted) {
            Log.i(TAG, "Add uid = " + uid + ",packageName = " + packageName + " to white list");
        } else {
            Log.i(TAG, "Remove uid = " + uid + ",packageName = " + packageName + " from white list");
        }
    }

    public boolean isWhitelisted(int uid) {
        return this.mUidPolicies.get(uid, 0) == 4;
    }

    public int getWhitelistedCount() {
        int count = 0;
        for (int i = 0; i < this.mUidPolicies.size(); i++) {
            if (this.mUidPolicies.valueAt(i) == 4) {
                count++;
            }
        }
        return count;
    }

    private void loadWhitelist() {
        for (int uid : this.mPolicyManager.getUidsWithPolicy(4)) {
            this.mUidPolicies.put(uid, 4);
        }
    }

    public void refreshBlacklist() {
        loadBlacklist();
    }

    private void loadBlacklist() {
        for (int uid : this.mPolicyManager.getUidsWithPolicy(1)) {
            this.mUidPolicies.put(uid, 1);
        }
    }

    public boolean isBlacklisted(int uid) {
        return this.mUidPolicies.get(uid, 0) == 1;
    }

    public void setIsBlacklisted(int uid, String packageName, boolean blacklisted) {
        int policy = blacklisted ? 1 : 0;
        this.mPolicyManager.setUidPolicy(uid, policy);
        this.mUidPolicies.put(uid, policy);
        if (blacklisted) {
            Log.i(TAG, "Add uid = " + uid + ",packageName = " + packageName + " to black list");
        } else {
            Log.i(TAG, "Remove uid = " + uid + ",packageName = " + packageName + " from black list");
        }
    }
}
