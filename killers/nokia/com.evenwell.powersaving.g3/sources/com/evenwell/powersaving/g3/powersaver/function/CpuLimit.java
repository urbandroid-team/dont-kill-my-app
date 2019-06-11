package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PowerSaverInfoDialog;

public class CpuLimit extends Function {
    private int mClusterNumber;
    String[] mCpuLimitOpcode1List;
    String[] mCpuLimitOpcode2List;
    String mErrMsg;
    String[] mExtremeModeCpuLimitSpeedList;
    String[] mNormalModeCpuLimitSpeedList;
    PowerManager mPowerManager;
    private ContentObserver mPowerSavingModeObserver;

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            CpuLimit.this.setEnable(CpuLimit.this.getValueFromDB());
            Log.i("Function", "[CpuLimit]: PowerSavingMode registerContentObserver");
            CpuLimit.this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.evenwell.powersaving.g3/settings/powersaving_db_power_saving_mode"), true, CpuLimit.this.mPowerSavingModeObserver);
        }

        public void onRestore() {
            CpuLimit.this.mContext.getContentResolver().unregisterContentObserver(CpuLimit.this.mPowerSavingModeObserver);
            String value = CpuLimit.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                CpuLimit.this.setEnable(value);
            }
            Log.i("Function", "[CpuLimit]: PowerSavingMode unregisterContentObserver");
        }
    }

    private class PowerSavingModeObserver extends ContentObserver {
        public PowerSavingModeObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            int mode = CpuLimit.this.getPowerSavingModeStatus();
            Log.i("Function", "[CpuLimit]: Mode status = " + mode);
            if (mode != -1) {
                CpuLimit.this.setEnable(CpuLimit.this.getValueFromDB());
            }
        }
    }

    public CpuLimit(Context context) {
        super(context);
        this.mPowerSavingModeObserver = null;
        this.mNormalModeCpuLimitSpeedList = null;
        this.mExtremeModeCpuLimitSpeedList = null;
        this.mCpuLimitOpcode1List = null;
        this.mCpuLimitOpcode2List = null;
        this.mErrMsg = "";
        this.mPowerManager = null;
        this.mPowerSavingModeObserver = new PowerSavingModeObserver();
        setListener(new Listener());
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        initConfigList();
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.CPU_LIMIT;
    }

    protected String getDBKey() {
        return LPMDB.CPU_LIMIT;
    }

    public boolean getEnabled() {
        return this.isClose;
    }

    public void bootHandling(int mode) {
        Log.i("Function", "[CpuLimit]: bootHandling() mode = " + mode);
        if (mode != -1) {
            setEnable(SWITCHER.ON);
            this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.evenwell.powersaving.g3/settings/powersaving_db_power_saving_mode"), true, this.mPowerSavingModeObserver);
        }
    }

    private int getPowerSavingModeStatus() {
        int mode = 0;
        String str = PowerSavingUtils.getStringItemFromSelfDB(this.mContext, "powersaving_db_power_saving_mode");
        if (str != null) {
            mode = Integer.valueOf(str).intValue();
        }
        Log.i("Function", "[CpuLimit]: getPowerSavingModeStatus = " + mode);
        return mode;
    }

    private int getPowerSavingMode() {
        int mode = 0;
        String str = PowerSavingUtils.getStringItemFromSelfDB(this.mContext, "powersaving_db_power_saving_mode");
        if (str != null) {
            mode = Integer.valueOf(str).intValue();
        }
        if (mode <= 0) {
            mode = 0;
        }
        Log.i("Function", "[CpuLimit]: getPowerSavingMode = " + mode);
        return mode;
    }

    protected void setEnable(String value) {
        if (!value.equals(SWITCHER.KEEP)) {
            boolean enabled;
            if (value.equals(SWITCHER.ON)) {
                enabled = true;
            } else {
                enabled = false;
            }
            Log.i("Function", "[CpuLimit]: setEnable = " + enabled);
            if (!enabled) {
                releaseMaxSpeed();
            } else if (isProductConfigErr()) {
                Intent intent2 = new Intent("com.evenwell.powersaving.g3.POWER_SAVER_INFO_DIALOG");
                intent2.putExtra(PowerSaverInfoDialog.POWER_SAVER_DIALOG_INFO, "Power saver product config error !\n" + this.mErrMsg);
                intent2.setFlags(268435456);
                this.mContext.startActivity(intent2);
            } else {
                releaseMaxSpeed();
                limitMaxSpeed(getPowerSavingMode());
            }
        }
    }

    private void initConfigList() {
        int i;
        Log.i("Function", "[CpuLimit]: initConfigList()");
        this.mNormalModeCpuLimitSpeedList = PowerSavingUtils.getNormalModeCpuLimitSpeedList();
        if (this.mNormalModeCpuLimitSpeedList != null) {
            for (i = 0; i < this.mNormalModeCpuLimitSpeedList.length; i++) {
                Log.i("Function", "[CpuLimit]: mNormalModeCpuLimitSpeedList[" + i + "] = " + this.mNormalModeCpuLimitSpeedList[i]);
            }
        }
        this.mExtremeModeCpuLimitSpeedList = PowerSavingUtils.getExtremeModeCpuLimitSpeedList();
        if (this.mExtremeModeCpuLimitSpeedList != null) {
            for (i = 0; i < this.mExtremeModeCpuLimitSpeedList.length; i++) {
                Log.i("Function", "[CpuLimit]: mExtremeModeCpuLimitSpeedList[" + i + "] = " + this.mExtremeModeCpuLimitSpeedList[i]);
            }
        }
        this.mCpuLimitOpcode1List = PowerSavingUtils.getCpuLimitOpcode1List();
        if (this.mCpuLimitOpcode1List != null) {
            for (i = 0; i < this.mCpuLimitOpcode1List.length; i++) {
                Log.i("Function", "[CpuLimit]: mCpuLimitOpcode1List[" + i + "] = " + this.mCpuLimitOpcode1List[i]);
            }
        }
        this.mCpuLimitOpcode2List = PowerSavingUtils.getCpuLimitOpcode2List();
        if (this.mCpuLimitOpcode2List != null) {
            for (i = 0; i < this.mCpuLimitOpcode2List.length; i++) {
                Log.i("Function", "[CpuLimit]: mCpuLimitOpcode2List[" + i + "] = " + this.mCpuLimitOpcode2List[i]);
            }
        }
        if (this.mNormalModeCpuLimitSpeedList != null) {
            this.mClusterNumber = this.mNormalModeCpuLimitSpeedList.length;
        }
    }

    private void limitMaxSpeed(int mode) {
        Log.i("Function", "[CpuLimit]: limitMaxSpeed()");
        for (int i = 0; i < this.mClusterNumber; i++) {
            int[] list = new int[]{0, 0};
            list[0] = Integer.parseInt(this.mCpuLimitOpcode2List[i].substring(2), 16);
            if (mode == 1) {
                Log.i("Function", "[CpuLimit]: limitMaxSpeed() MODE_EXTREME");
                list[1] = Integer.parseInt(this.mExtremeModeCpuLimitSpeedList[i].substring(2), 16);
            } else {
                Log.i("Function", "[CpuLimit]: limitMaxSpeed() MODE_NORMAL");
                list[1] = Integer.parseInt(this.mNormalModeCpuLimitSpeedList[i].substring(2), 16);
            }
        }
    }

    private void releaseMaxSpeed() {
        Log.i("Function", "[CpuLimit]: releaseMaxSpeed()");
    }

    private boolean isProductConfigErr() {
        Log.i("Function", "[CpuLimit]: isProductConfigErr()");
        this.mErrMsg = "";
        if (this.mNormalModeCpuLimitSpeedList == null || this.mExtremeModeCpuLimitSpeedList == null || this.mCpuLimitOpcode1List == null || this.mCpuLimitOpcode2List == null) {
            Log.i("Function", "[CpuLimit]: config error -> Empty value exist");
            this.mErrMsg = "[CpuLimit]: Empty value exist";
            return true;
        } else if (this.mNormalModeCpuLimitSpeedList[0].equals("NA") || this.mExtremeModeCpuLimitSpeedList[0].equals("NA") || this.mCpuLimitOpcode1List[0].equals("NA") || this.mCpuLimitOpcode2List[0].equals("NA")) {
            Log.i("Function", "[CpuLimit]: config error -> NA value exist");
            this.mErrMsg = "[CpuLimit]: NA value exist";
            return true;
        } else {
            int len = this.mNormalModeCpuLimitSpeedList.length;
            if (this.mExtremeModeCpuLimitSpeedList.length == len && this.mCpuLimitOpcode1List.length == len && this.mCpuLimitOpcode2List.length == len) {
                Log.i("Function", "[CpuLimit]: isProductConfigErr() - false");
                return false;
            }
            Log.i("Function", "[CpuLimit]: config error -> CPU limit items : no same length on all list");
            this.mErrMsg = "[CpuLimit]: no same length on all list";
            return true;
        }
    }
}
