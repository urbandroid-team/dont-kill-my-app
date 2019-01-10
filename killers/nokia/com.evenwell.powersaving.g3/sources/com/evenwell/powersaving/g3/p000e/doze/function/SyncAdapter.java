package com.evenwell.powersaving.g3.p000e.doze.function;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.SyncAdapterType;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.exception.BMS;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.PARM;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import java.util.Arrays;
import java.util.List;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.SyncAdapter */
public class SyncAdapter extends Function {
    public SyncAdapter(Context context) {
        super(context);
    }

    public boolean get() {
        return false;
    }

    public void set(boolean value) {
    }

    public void close() {
        Log.i("Function", "[SyncAdapter] close()");
        Log.i("Function", "[SyncAdapter] getSyncAdapterCloseSetStatus() = " + getSyncAdapterCloseSetStatus());
        if (!getSyncAdapterCloseSetStatus() && isNeedApply()) {
            setAccountListAutoSyncDisabled(this.mContext);
            setSyncAdapterCloseSetStatus(true);
        }
    }

    public void restore() {
        Log.i("Function", "[SyncAdapter] restore()");
        Log.i("Function", "[SyncAdapter] getSyncAdapterCloseSetStatus() = " + getSyncAdapterCloseSetStatus());
        if (getSyncAdapterCloseSetStatus() && isNeedApply()) {
            restoreAccountListAutoSync(this.mContext);
            setSyncAdapterCloseSetStatus(false);
        }
    }

    private void setSyncAdapterCloseSetStatus(boolean value) {
        Editor editor = this.mContext.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putBoolean(PARM.IS_SYNC_ADAPTER_CLOSE_SET, value);
        editor.commit();
    }

    private boolean getSyncAdapterCloseSetStatus() {
        return this.mContext.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getBoolean(PARM.IS_SYNC_ADAPTER_CLOSE_SET, false);
    }

    private boolean isNeedApply() {
        boolean apply = true;
        boolean isCN = this.mContext.getResources().getBoolean(C0321R.bool.region_cn);
        Log.i("Function", "[SyncAdapter] isCN = " + isCN);
        Log.i("Function", "[SyncAdapter] BAM setting = " + BMS.getInstance(this.mContext).getBMSValue());
        if (!(isCN || BMS.getInstance(this.mContext).getBMSValue())) {
            apply = false;
        }
        Log.i("Function", "[SyncAdapter] isNeedApply() = " + apply);
        return apply;
    }

    public void setAccountListAutoSyncDisabled(Context context) {
        Log.i("Function", "[SyncAdapter] setAccountListAutoSyncDisabled()");
        List<String> pkgList = PowerSavingUtils.getBlackList(context);
        if (pkgList != null) {
            Account[] accounts = AccountManager.get(context).getAccounts();
            String disabledSyncAdapterTypeInfoList = "";
            for (SyncAdapterType type : ContentResolver.getSyncAdapterTypes()) {
                String packageName = getSyncAdapterPackageName(type);
                if (pkgList.contains(packageName)) {
                    Account acc = findAccount(accounts, type.accountType);
                    if (acc != null) {
                        Log.i("Function", "[SyncAdapter] acc != null, sync name=" + acc.name);
                        context.getContentResolver();
                        boolean isAccountSyncAutomatically = ContentResolver.getSyncAutomatically(acc, type.authority);
                        Log.i("Function", "[SyncAdapter] isAccountSyncAutomatically = " + isAccountSyncAutomatically);
                        if (isAccountSyncAutomatically) {
                            Log.i("Function", "[SyncAdapter] disable auto sync >>> sync name=" + acc.name + ", type=" + acc.type + ", auth=" + type.authority);
                            context.getContentResolver();
                            ContentResolver.setSyncAutomatically(acc, type.authority, false);
                            String syncAdapterTypeInfo = packageName + "," + acc.name + "," + acc.type + "," + type.authority;
                            if (disabledSyncAdapterTypeInfoList.equals("")) {
                                disabledSyncAdapterTypeInfoList = disabledSyncAdapterTypeInfoList + syncAdapterTypeInfo;
                            } else {
                                disabledSyncAdapterTypeInfoList = disabledSyncAdapterTypeInfoList + SYMBOLS.SEMICOLON + syncAdapterTypeInfo;
                            }
                            try {
                                PowerSavingUtils.insertStopSyncAdapterInfo(context, "true", syncAdapterTypeInfo);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
            Log.i("Function", "[SyncAdapter] disabledSyncAdapterTypeInfoList = " + disabledSyncAdapterTypeInfoList);
            Editor editor = context.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
            editor.putString(PARM.DISABLED_SYNC_ADAPTER_TYPE_INFO_LIST, disabledSyncAdapterTypeInfoList);
            editor.commit();
        }
    }

    private void restoreAccountListAutoSync(Context context) {
        Log.i("Function", "[SyncAdapter] restoreAccountListAutoSync()");
        String disabledSyncAdapterTypeInfoList = context.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getString(PARM.DISABLED_SYNC_ADAPTER_TYPE_INFO_LIST, "");
        Log.i("Function", "[SyncAdapter] disabledSyncAdapterTypeInfoList = " + disabledSyncAdapterTypeInfoList);
        if (!disabledSyncAdapterTypeInfoList.equals("")) {
            String[] syncAdapterTypeInfoList = null;
            try {
                syncAdapterTypeInfoList = disabledSyncAdapterTypeInfoList.split(SYMBOLS.SEMICOLON);
            } catch (Exception e) {
                Log.i("Function", "[SyncAdapter] Exception: " + e);
            }
            if (syncAdapterTypeInfoList != null) {
                Log.i("Function", "[SyncAdapter] restore syncAdapterTypeInfoList = " + Arrays.toString(syncAdapterTypeInfoList));
                Account[] accounts = AccountManager.get(context).getAccounts();
                for (SyncAdapterType type : ContentResolver.getSyncAdapterTypes()) {
                    String packageName = getSyncAdapterPackageName(type);
                    Account acc = findAccount(accounts, type.accountType);
                    if (acc != null) {
                        String syncAdapterTypeInfo = packageName + "," + acc.name + "," + acc.type + "," + type.authority;
                        if (Arrays.asList(syncAdapterTypeInfoList).contains(syncAdapterTypeInfo)) {
                            Log.i("Function", "[SyncAdapter] restore syncAdapterTypeInfo = " + syncAdapterTypeInfo);
                            context.getContentResolver();
                            boolean isAccountSyncAutomatically = ContentResolver.getSyncAutomatically(acc, type.authority);
                            Log.i("Function", "[SyncAdapter] isAccountSyncAutomatically = " + isAccountSyncAutomatically);
                            if (!isAccountSyncAutomatically) {
                                Log.i("Function", "[SyncAdapter] enable auto sync >>> sync name=" + acc.name + ", type=" + acc.type + ", auth=" + type.authority);
                                context.getContentResolver();
                                ContentResolver.setSyncAutomatically(acc, type.authority, true);
                                try {
                                    PowerSavingUtils.insertStopSyncAdapterInfo(context, "false", syncAdapterTypeInfo);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Account findAccount(Account[] accounts, String accountType) {
        for (Account acc : accounts) {
            if (acc.type.equals(accountType)) {
                return acc;
            }
        }
        return null;
    }

    private String getSyncAdapterPackageName(SyncAdapterType type) {
        String str = null;
        try {
            Object o = Class.forName("android.content.SyncAdapterType").getMethod("getPackageName", new Class[0]).invoke(type, new Object[0]);
            if (o != null) {
                str = o.toString();
            }
        } catch (Exception e) {
            Log.w("Function", "[SyncAdapter] getPackageName() failed.", e);
        }
        return str;
    }
}
