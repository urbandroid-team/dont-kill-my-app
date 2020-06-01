package com.evenwell.powersaving.g3;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.PowerManager;
import android.util.Log;
import com.evenwell.powersaving.g3.background.BAMMode;
import com.evenwell.powersaving.g3.background.BackgroundCleanService;
import com.evenwell.powersaving.g3.background.CheckDBService;
import com.evenwell.powersaving.g3.background.GPSManagerService;
import com.evenwell.powersaving.g3.background.PMSMode;
import com.evenwell.powersaving.g3.exception.BMS;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.exception.DefaultWhiteListService;
import com.evenwell.powersaving.g3.exception.HighPower;
import com.evenwell.powersaving.g3.lpm.LpmObserverUtils;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.p000e.doze.CloseFunction;
import com.evenwell.powersaving.g3.p000e.doze.EDozeService;
import com.evenwell.powersaving.g3.powersaver.GrayscaleModeTileService;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.EXTRA_NAME;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.LATEST_EVENT_EXTRA;
import com.evenwell.powersaving.g3.pushservice.PackageCategory;
import com.evenwell.powersaving.g3.pushservice.PollingService;
import com.evenwell.powersaving.g3.pushservice.PullServerCommand;
import com.evenwell.powersaving.g3.utils.DozeUtil;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.INTENT.FUNCTION;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.INTENT.FUNCTION.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.PARM;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.PACKAGE_NAME;
import com.evenwell.powersaving.g3.utils.PSConst.SERVICE_START.PSSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.PSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SS.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.io.File;
import java.util.ArrayList;
import net2.lingala.zip4j.util.InternalZipConstants;
import org.apache.commons.io.FileUtils;

public class PowerSavingReceiver extends BroadcastReceiver {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;
    private static Intent mPullService;
    Context mContext;
    private PullServerCommand mPullServerCommand;

    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        String action = intent.getAction();
        Log.i(TAG, "[PowerSavingReceiver] onReceive action = " + action + ",CN Model = " + PSUtils.isCNModel(this.mContext));
        BMS bms;
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            File blackListFile = new File(this.mContext.getFilesDir(), PackageCategory.BLACK_LIST.getValue());
            if (blackListFile.exists()) {
                HighPower highPowerListInLocal = new HighPower(blackListFile);
                File blackListFileInSystem = new File(FILENAME.POWER_SAVING_DEFAULT_BLACK_LIST);
                if (!highPowerListInLocal.isNewerVersion(new HighPower(blackListFileInSystem))) {
                    Log.d(TAG, "copy newer black list from system/etc to file dir");
                    try {
                        FileUtils.copyFile(blackListFileInSystem, new File(this.mContext.getFilesDir() + InternalZipConstants.ZIP_FILE_SEPARATOR + PackageCategory.BLACK_LIST.getValue()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.d(TAG, "copy black list from system/etc to file dir");
                try {
                    FileUtils.copyFile(new File(FILENAME.POWER_SAVING_DEFAULT_BLACK_LIST), new File(this.mContext.getFilesDir() + InternalZipConstants.ZIP_FILE_SEPARATOR + PackageCategory.BLACK_LIST.getValue()));
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            if (context.getResources().getBoolean(C0321R.bool.polling_service) && PSUtils.isCNModel(this.mContext)) {
                mPullService = new Intent(this.mContext, PollingService.class);
                mPullService.putExtra(PollingService.EXTRA_KEY_ACTION, PollingService.ACTION_REGISTER_DEVICE);
                this.mContext.startService(mPullService);
                mPullService = new Intent(this.mContext, PollingService.class);
                mPullService.putExtra(PollingService.EXTRA_KEY_ACTION, PollingService.ACTION_DETECT_PULL_TIME);
                this.mContext.startService(mPullService);
            }
            PowerSavingUtils.SetPreferencesStatus(context, PSSPREF.IS_BOOT_COMPLETE, true);
            Log.i(TAG, "[PowerSavingReceiver] onReceive ACTION_BOOT_COMPLETED= ");
            PowerSavingUtils.SetServiceStartReason(this.mContext, 0);
            PowerSavingUtils.checkDefaultValueInDB(this.mContext);
            PowerSavingUtils.setHotspotstate(this.mContext, false);
            if (!PowerSavingUtils.IsFirstTimeTrigger(this.mContext)) {
                PowerSavingUtils.FirstTimeTrigger(this.mContext);
            }
            if (!PowerSavingUtils.isSupportScreenPolicy(this.mContext) && PowerSavingUtils.GetScreenPolicyEnable(this.mContext)) {
                Log.i(TAG, "Not support screen policy. reset default value to false.");
                PowerSavingUtils.setBooleanItemToDB(this.mContext, PSDB.SCREEN_POLICY, false);
            }
            if (PowerSavingUtils.isSupportDozeMode(this.mContext) && PowerSavingUtils.GetPWEnable(this.mContext)) {
                Log.i(TAG, "Not support PW. reset default value to false.");
                PowerSavingUtils.setBooleanItemToDB(this.mContext, PSDB.PW, false);
            }
            LpmObserverUtils.UpdateValueToBackUpSharedPreferences(this.mContext, LPMSPREF.WIFI_HOTSPOT, LpmUtils.BooleanToString_NoKeep(false));
            this.mContext.startService(new Intent(this.mContext, CheckDBService.class));
            bms = BMS.getInstance(context);
            Log.i(TAG, "bamMode=" + new BAMMode(this.mContext).modeToString());
            Log.i(TAG, "pmsMode=" + new PMSMode(this.mContext).modeToString());
            if (!this.mContext.getResources().getBoolean(C0321R.bool.is_e1m_Device) || !PSUtils.enableTestFunction()) {
                this.mContext.startService(new Intent(this.mContext, BackgroundCleanService.class));
                PowerSavingUtils.setProcessMonitorServiceEnable(context, true);
            } else if (bms.getBMSValue()) {
                PowerSavingUtils.setProcessMonitorServiceEnable(context, true);
                this.mContext.startService(new Intent(this.mContext, BackgroundCleanService.class));
            }
            this.mContext.startService(new Intent(this.mContext, DefaultWhiteListService.class));
            if (!PSUtils.isCNModel(this.mContext)) {
                AudioManager mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
                if (!((PowerManager) this.mContext.getSystemService("power")).isPowerSaveMode()) {
                    try {
                        mAudioManager.setParameters("display_enableHDRkey=1;display_enableHDR=0");
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                }
            }
            if (CloseFunction.deepFunctionSize(context) > 0 || CloseFunction.lightFunctionSize(context) > 0) {
                context.startService(new Intent(context, EDozeService.class));
            }
            Log.d(TAG, "[PowerSavingReceiver] DozeUtil.setDozeStateTimeout() : " + DozeUtil.setDozeStateTimeout(context));
            PowerSavingUtils.clearAlarmRecordsInDoze(context);
            if (this.mContext.getResources().getBoolean(C0321R.bool.apply_grayscale_mode_tile) && PowerSavingUtils.isSupportAmoledDisplay()) {
                Log.d(TAG, "[PowerSavingReceiver] ApplyGrayscaleTile & SupportAmoledDisplay");
                this.mContext.getPackageManager().setComponentEnabledSetting(new ComponentName(this.mContext, GrayscaleModeTileService.class), 1, 1);
                Intent intent2 = new Intent("quicksetting.tile.refresh");
                intent2.setPackage(PACKAGE_NAME.SYSTEM_UI);
                this.mContext.sendBroadcast(intent2);
            }
            if (PSUtils.isCNModel(this.mContext)) {
                this.mContext.startService(new Intent(this.mContext, PowerSavingController.class));
                Log.d(TAG, "[PowerSavingReceiver] start PowerSavingController : ");
            }
            BackgroundPolicyExecutor.getInstance(context).addAppToDozeWhiteList(context.getPackageName());
            if (!new File(this.mContext.getFilesDir(), PackageCategory.BLACK_LIST.getValue()).exists()) {
                Log.d(TAG, "copy black list from system/etc to file dir");
                try {
                    FileUtils.copyFile(new File(FILENAME.POWER_SAVING_DEFAULT_BLACK_LIST), new File(this.mContext.getFilesDir() + InternalZipConstants.ZIP_FILE_SEPARATOR + PackageCategory.BLACK_LIST.getValue()));
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
            PowerSavingUtils.setSettingsProvider(context, PARM.KEY_BACKGROUND_EXECUTION_ENABLED, context.getResources().getString(C0321R.string.background_execution_enabled));
            this.mContext.startService(new Intent(this.mContext, GPSManagerService.class));
        } else if (action.equals(ACTION.ACTION_HOTSPOT_STATUS)) {
            int mHotspotStatus = intent.getIntExtra("counter", -1);
            Log.i(TAG, "Receive hotspot intent, Curren user, " + mHotspotStatus);
            if (mHotspotStatus == 0) {
                PowerSavingUtils.setHotspotstate(this.mContext, false);
            } else if (mHotspotStatus >= 1) {
                PowerSavingUtils.setHotspotstate(this.mContext, true);
            } else {
                Log.i(TAG, "[SmartSwitch] ACTION_HOTSPOT_STATUS get error !!");
            }
        } else if (action.equals("android.net.conn.TETHER_STATE_CHANGED")) {
            ArrayList<String> mActive = intent.getStringArrayListExtra("tetherArray");
            if (mActive != null) {
                Log.i(TAG, "Receive tether state changed, current active: " + mActive.size());
                if (mActive.size() == 0) {
                    PowerSavingUtils.setTetherState(this.mContext, false);
                } else if (mActive.size() >= 1) {
                    PowerSavingUtils.setTetherState(this.mContext, true);
                } else {
                    PowerSavingUtils.setTetherState(this.mContext, false);
                }
            }
        } else if (action.equals("android.intent.action.MY_PACKAGE_REPLACED")) {
            Log.i(TAG, "receive ACTION_MY_PACKAGE_REPLACED");
            if (PSUtils.isCNModel(this.mContext) && context.getResources().getBoolean(C0321R.bool.polling_service)) {
                this.mPullServerCommand = new PullServerCommand(this.mContext);
                if (this.mPullServerCommand.checkRegisterDevice()) {
                    this.mPullServerCommand.RegisterDevice(PackageCategory.BLACK_LIST);
                    this.mPullServerCommand.RegisterDevice(PackageCategory.WHITE_LIST);
                }
                mPullService = new Intent(this.mContext, PollingService.class);
                mPullService.putExtra(PollingService.EXTRA_KEY_ACTION, PollingService.ACTION_DETECT_PULL_TIME);
                this.mContext.startService(mPullService);
            }
            PowerSavingUtils.SetServiceStartReason(this.mContext, 0);
            PowerSavingUtils.checkDefaultValueInDB(this.mContext);
            if (!PowerSavingUtils.isSupportScreenPolicy(this.mContext) && PowerSavingUtils.GetScreenPolicyEnable(this.mContext)) {
                Log.i(TAG, "Not support screen policy. reset default value to false.");
                PowerSavingUtils.setBooleanItemToDB(this.mContext, PSDB.SCREEN_POLICY, false);
            }
            if (PowerSavingUtils.isSupportDozeMode(this.mContext) && PowerSavingUtils.GetPWEnable(this.mContext)) {
                Log.i(TAG, "Not support PW. reset default value to false.");
                PowerSavingUtils.setBooleanItemToDB(this.mContext, PSDB.PW, false);
            }
            if (PSUtils.isCNModel(this.mContext)) {
                this.mContext.startService(new Intent(this.mContext, PowerSavingController.class));
            }
            bms = BMS.getInstance(context);
            if (!this.mContext.getResources().getBoolean(C0321R.bool.is_e1m_Device) || !PSUtils.enableTestFunction()) {
                this.mContext.startService(new Intent(this.mContext, BackgroundCleanService.class));
                PowerSavingUtils.setProcessMonitorServiceEnable(context, true);
            } else if (bms.getBMSValue()) {
                PowerSavingUtils.setProcessMonitorServiceEnable(context, true);
                this.mContext.startService(new Intent(this.mContext, BackgroundCleanService.class));
            }
            if (CloseFunction.deepFunctionSize(context) > 0 || CloseFunction.lightFunctionSize(context) > 0) {
                context.startService(new Intent(context, EDozeService.class));
            }
            context.startService(new Intent(context, DefaultWhiteListService.class));
            context.startService(new Intent(context, GPSManagerService.class));
        } else if (action.equals(FUNCTION.ACTION.ACTION_START_SERVICE)) {
            Log.i(TAG, "[PowerSavingReceiver] receive com.fihtdc.powersaving.start_powersaver");
            if (!PowerSavingUtils.isCharging(this.mContext) && PSUtils.isCNModel(this.mContext)) {
                Log.i(TAG, "[PowerSavingReceiver] mEnable = " + intent.getIntExtra(EXTRA.POWERSAVER_ENABLE, -1));
                Intent StartServiceIntent = new Intent(this.mContext, PowerSavingController.class);
                bundle = intent.getExtras();
                if (bundle != null) {
                    Log.i(TAG, "[PowerSavingReceiver] bundle != null");
                    StartServiceIntent.putExtras(bundle);
                    StartServiceIntent.putExtra(EXTRA_NAME.MODE, 1);
                    StartServiceIntent.putExtra(EXTRA_NAME.LATEST_EVENT, LATEST_EVENT_EXTRA.MANUAL);
                } else {
                    Log.i(TAG, "[PowerSavingReceiver] bundle == null");
                }
                this.mContext.startService(StartServiceIntent);
            }
        } else if (!action.equals(FUNCTION.ACTION.ACTION_START_SUBITEM)) {
        } else {
            if (PowerSavingUtils.GetPowerSavingModeEnable(this.mContext)) {
                Log.i(TAG, "[PowerSavingReceiver] Receive com.fihtdc.powersaving.start_subitem");
                bundle = intent.getExtras();
                if (bundle != null) {
                    PowerSavingUtils.SetFunctionByOtherAPK(this.mContext, bundle);
                    return;
                }
                return;
            }
            Log.i(TAG, "[PowerSavingReceiver] Receive com.fihtdc.powersaving.start_subitem, but powersaver = off ,so return");
        }
    }
}
