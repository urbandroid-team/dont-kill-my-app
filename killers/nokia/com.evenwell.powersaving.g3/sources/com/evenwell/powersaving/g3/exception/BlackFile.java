package com.evenwell.powersaving.g3.exception;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlackFile {
    public static final String BAM_BLACK_FILE_VERSION = "BAM_BLACK_FILE_VERSION";
    private static final boolean DBG = true;
    public static final String DISAUTO_BLACK_FILE_VERSION = "DISAUTO_BLACK_FILE_VERSION";
    private static final String TAG = "[PowerSavingAppG3]BlackFile";
    private static BlackFile mInstance = null;
    private BlackList mBAMBlackList;
    private Context mContext;
    private BlackList mDisautoBlackList = new BlackList();

    public class BlackList extends ArrayList<String> {
        public boolean isNeedToRefresh;
        public int version;

        private BlackList() {
            this.version = 0;
            this.isNeedToRefresh = false;
        }
    }

    public static BlackFile getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new BlackFile(ctx);
        }
        return mInstance;
    }

    private BlackFile(Context ctx) {
        this.mContext = ctx;
        SharedPreferences prefStatus = this.mContext.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0);
        int oldDisautoBlackListVersion = prefStatus.getInt(DISAUTO_BLACK_FILE_VERSION, -999);
        int oldBAMBlackListVersion = prefStatus.getInt(BAM_BLACK_FILE_VERSION, -999);
        int newDisautoFileVersion = 7001106;
        try {
            newDisautoFileVersion = getVersion(C0321R.string.disauto_black_list_version);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (newDisautoFileVersion > oldDisautoBlackListVersion) {
            this.mDisautoBlackList.isNeedToRefresh = true;
        }
        this.mDisautoBlackList.addAll(getBlackList(C0321R.array.disauto_black_list));
        prefStatus.edit().putInt(DISAUTO_BLACK_FILE_VERSION, newDisautoFileVersion).commit();
        Log.d(TAG, "[checkDB] mDisautoBlackList " + this.mDisautoBlackList);
        Log.d(TAG, "[checkDB] mDisautoBlackList.isNeedToRefresh " + this.mDisautoBlackList.isNeedToRefresh);
        this.mBAMBlackList = new BlackList();
        int newBAMFileVersion = 7001106;
        try {
            newBAMFileVersion = getVersion(C0321R.string.bam_black_list_version);
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
        if (newBAMFileVersion > oldBAMBlackListVersion) {
            this.mBAMBlackList.isNeedToRefresh = true;
        }
        this.mBAMBlackList.addAll(getBlackList(C0321R.array.bam_black_list));
        prefStatus.edit().putInt(BAM_BLACK_FILE_VERSION, newBAMFileVersion).commit();
        Log.d(TAG, "[checkDB] mBAMBlackList " + this.mBAMBlackList);
        Log.d(TAG, "[checkDB] mBAMBlackList.isNeedToRefresh " + this.mBAMBlackList.isNeedToRefresh);
    }

    public BlackList getDisautoBlackList() {
        return this.mDisautoBlackList;
    }

    public BlackList getBAMBlackList() {
        return this.mBAMBlackList;
    }

    private List<String> getBlackList(int resourceId) {
        return Arrays.asList(this.mContext.getResources().getStringArray(resourceId));
    }

    private int getVersion(int resourceId) {
        return Integer.valueOf(this.mContext.getResources().getString(resourceId).replace(".", "")).intValue();
    }
}
