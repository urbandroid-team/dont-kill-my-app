package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PowerSaverInfoDialog;
import net2.lingala.zip4j.util.InternalZipConstants;

public class BatterySaver extends Function {
    private static final String KEY_IS_BATTERY_SAVER_POLICY_INIT = "is_battery_saver_policy_init";
    private int mClusterNumber;
    String mErrMsg = "";
    String[] mExtremeModeCpuLimitSpeedList = null;
    private PowerManager mPowerManger;

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            if (BatterySaver.this.mPowerManger != null) {
                BatterySaver.this.mPowerManger.setPowerSaveMode(true);
            }
            if (BatterySaver.this.isProductConfigErr()) {
                Intent i = new Intent("com.evenwell.powersaving.g3.POWER_SAVER_INFO_DIALOG");
                i.putExtra(PowerSaverInfoDialog.POWER_SAVER_DIALOG_INFO, "Power saver product config error !\n" + BatterySaver.this.mErrMsg);
                i.setFlags(268435456);
                BatterySaver.this.mContext.startActivity(i);
            }
        }

        public void onRestore() {
            if (BatterySaver.this.mPowerManger != null) {
                BatterySaver.this.mPowerManger.setPowerSaveMode(false);
            }
        }
    }

    public BatterySaver(Context context) {
        super(context);
        getCpuLimitConfigValue();
        boolean isPolicyInited = PowerSavingUtils.GetPreferencesStatus(this.mContext, KEY_IS_BATTERY_SAVER_POLICY_INIT);
        Log.i("Function", "[BatterySaver]: isPolicyInited = " + isPolicyInited);
        if (!isPolicyInited) {
            Log.i("Function", "[BatterySaver]: init policy");
            initBatterySaverPolicy();
            if (initCpuLimitValue()) {
                PowerSavingUtils.SetPreferencesStatus(this.mContext, KEY_IS_BATTERY_SAVER_POLICY_INIT, true);
            }
        }
        this.mPowerManger = (PowerManager) context.getSystemService("power");
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.BATTERY_SAVER;
    }

    protected String getDBKey() {
        return LPMDB.BATTERY_SAVER;
    }

    public boolean getEnabled() {
        if (this.mPowerManger != null) {
            return this.mPowerManger.isPowerSaveMode();
        }
        return false;
    }

    protected void setEnable(String value) {
    }

    public void bootHandling(int mode) {
    }

    private void initBatterySaverPolicy() {
        PowerSavingUtils.setSettingsProvider(this.mContext, "battery_saver_constants", "vibration_disabled=false,gps_mode=0");
    }

    private boolean initCpuLimitValue() {
        Log.i("Function", "[BatterySaver]: initCpuLimitValue()");
        if (isProductConfigErr()) {
            return false;
        }
        String cpuCoreValueString = "";
        for (int i = 0; i < this.mClusterNumber; i++) {
            if (cpuCoreValueString.equals("")) {
                cpuCoreValueString = String.valueOf(i * 4) + ":" + this.mExtremeModeCpuLimitSpeedList[i];
            } else {
                cpuCoreValueString = cpuCoreValueString + InternalZipConstants.ZIP_FILE_SEPARATOR + String.valueOf(i * 4) + ":" + this.mExtremeModeCpuLimitSpeedList[i];
            }
            Log.i("Function", "[BatterySaver]: cpuCoreValueString = " + cpuCoreValueString);
        }
        String settingString = null;
        if (!cpuCoreValueString.equals("")) {
            settingString = "cpufreq-i=" + cpuCoreValueString + ",cpufreq-n=" + cpuCoreValueString;
        }
        Log.i("Function", "[BatterySaver]: settingString = " + settingString);
        if (settingString == null) {
            return false;
        }
        PowerSavingUtils.setSettingsProvider(this.mContext, "battery_saver_device_specific_constants", settingString);
        return true;
    }

    private void getCpuLimitConfigValue() {
        this.mExtremeModeCpuLimitSpeedList = PowerSavingUtils.getExtremeModeCpuLimitSpeedList();
        if (this.mExtremeModeCpuLimitSpeedList != null) {
            for (int i = 0; i < this.mExtremeModeCpuLimitSpeedList.length; i++) {
                Log.i("Function", "[BatterySaver]: mExtremeModeCpuLimitSpeedList[" + i + "] = " + this.mExtremeModeCpuLimitSpeedList[i]);
            }
            this.mClusterNumber = this.mExtremeModeCpuLimitSpeedList.length;
        }
    }

    private boolean isProductConfigErr() {
        Log.i("Function", "[BatterySaver]: isProductConfigErr()");
        this.mErrMsg = "";
        if (this.mExtremeModeCpuLimitSpeedList == null) {
            Log.i("Function", "[BatterySaver]: config error -> Empty value exist");
            this.mErrMsg = "[CpuLimit]: Empty value exist";
            return true;
        } else if (this.mExtremeModeCpuLimitSpeedList == null || !this.mExtremeModeCpuLimitSpeedList[0].contains("0x")) {
            Log.i("Function", "[BatterySaver]: isProductConfigErr() - false");
            return false;
        } else {
            Log.i("Function", "[BatterySaver]: config error -> Old version config");
            this.mErrMsg = "[CpuLimit]: Old version config";
            return true;
        }
    }
}
