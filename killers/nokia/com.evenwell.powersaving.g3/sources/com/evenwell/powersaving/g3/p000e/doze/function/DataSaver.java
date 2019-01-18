package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.Context;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.p000e.doze.DataSaverBackend;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.LinkedHashSet;
import java.util.Set;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.DataSaver */
public class DataSaver extends Function {
    private static final String KEY_DATA_SAVER_WHITELIST = "data_saver_whitelist";
    private DataSaverBackend mDataSaverBackend;

    public DataSaver(Context context) {
        super(context, true, new BluetoothHotSpot(context, new WifiHotSpot(context)));
        this.mDataSaverBackend = new DataSaverBackend(context);
    }

    public boolean get() {
        return this.mDataSaverBackend.isDataSaverEnabled();
    }

    public void set(boolean value) {
        int i = 0;
        this.mDataSaverBackend.setDataSaverEnabled(value);
        this.mDataSaverBackend.refreshWhitelist();
        String pkgName;
        int uid;
        if (value) {
            Set<String> whitelist = new LinkedHashSet();
            String[] dataSaverWhiteList = this.mContext.getResources().getStringArray(C0321R.array.data_saver_whitelist);
            int length = dataSaverWhiteList.length;
            while (i < length) {
                pkgName = dataSaverWhiteList[i];
                uid = PSUtils.getUid(this.mContext, pkgName);
                if (!(uid == -1 || this.mDataSaverBackend.isWhitelisted(uid))) {
                    this.mDataSaverBackend.setIsWhitelisted(uid, pkgName, true);
                    whitelist.add(pkgName);
                }
                i++;
            }
            savePreference(KEY_DATA_SAVER_WHITELIST, (Set) whitelist);
        } else if (containPreference(KEY_DATA_SAVER_WHITELIST)) {
            for (String pkgName2 : readPreferenceSet(KEY_DATA_SAVER_WHITELIST)) {
                uid = PSUtils.getUid(this.mContext, pkgName2);
                if (uid != -1 && this.mDataSaverBackend.isWhitelisted(uid)) {
                    this.mDataSaverBackend.setIsWhitelisted(uid, pkgName2, false);
                }
            }
            removePreference(KEY_DATA_SAVER_WHITELIST);
        }
    }

    public boolean forceIgnore() {
        if (!super.forceIgnore()) {
            return false;
        }
        Log.i("Function", "do not change data saver state");
        return true;
    }
}
