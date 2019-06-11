package com.evenwell.powersaving.g3;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;
import com.evenwell.powersaving.g3.background.PowerSavingNotificationListenerService;
import com.evenwell.powersaving.g3.background.ProcessMonitorService;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.p000e.doze.record.AlarmRecord;
import com.evenwell.powersaving.g3.provider.BackDataDb;
import com.evenwell.powersaving.g3.provider.BackDataDb.SaveData;
import com.evenwell.powersaving.g3.provider.PowerSavingProvider;
import com.evenwell.powersaving.g3.provider.PowerSavingProvider.SettingsColumns;
import com.evenwell.powersaving.g3.provider.WakePathInfo;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.INTENT.FUNCTION;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.PARM;
import com.evenwell.powersaving.g3.utils.PSConst.CPU.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.CPU.INTENT.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMPARM;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_KEY;
import com.evenwell.powersaving.g3.utils.PSConst.PACKAGE_NAME;
import com.evenwell.powersaving.g3.utils.PSConst.PERMISSION.TYPE;
import com.evenwell.powersaving.g3.utils.PSConst.SERVICE_START.PSSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.DCDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.PROCESS_MONITOR;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.PSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.PWDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.SSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.TSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SS.SSPARM;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.evenwell.powersaving.g3.utils.PSUtils;
import com.evenwell.powersaving.g3.utils.ProjectInfo;
import com.fihtdc.backuptool.FileOperator;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net2.lingala.zip4j.util.InternalZipConstants;
import org.xmlpull.v1.XmlPullParser;

public class PowerSavingUtils {
    private static final boolean DBG = true;
    public static final String LOGCONFIG_PATH = (Environment.getDataDirectory().toString() + "/logs/LogConfig");
    private static String TAG = TAG.PSLOG;
    private static final String WIDGET_STATE_FILENAME = "appwidgets.xml";
    private static String mXmlStr = "";

    /* renamed from: com.evenwell.powersaving.g3.PowerSavingUtils$1 */
    static class C03191 implements OnClickListener {
        C03191() {
        }

        public void onClick(DialogInterface dialog, int which) {
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.PowerSavingUtils$2 */
    static class C03202 implements OnClickListener {
        C03202() {
        }

        public void onClick(DialogInterface dialog, int which) {
        }
    }

    public static boolean IsFirstTimeTrigger(Context mContext) {
        if (IsUseNewMethodToStoreSettings(mContext)) {
            return isSettingExistInSelfDB(mContext, TSDB.TIME_SCHEDULE_START_TIME);
        }
        if (System.getString(mContext.getContentResolver(), PSDB.SCREEN_POLICY) == null) {
            return false;
        }
        return true;
    }

    public static void FirstTimeTrigger(Context ctx) {
        Log.i(TAG, "[PowerSavingUtils] FirstTimeTrigger: Not Has Shared Preferences, so new Shared Preferences");
        ProjectInfo.ProjectInfo(ctx);
    }

    public static void SetServiceStartReason(Context ctx, int value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putInt(PSSPREF.SERVICE_START_REASON, value);
        editor.commit();
    }

    public static int CheckServiceStatus(Context ctx) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getInt(PSSPREF.SERVICE_START_REASON, 3);
    }

    public static void setPowerSavingModeUiStatus(Context ctx, int value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putInt(PSSPREF.PS_MODE_UI_STATUS, value);
        editor.commit();
    }

    public static int getPowerSavingModeUiStatus(Context ctx) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getInt(PSSPREF.PS_MODE_UI_STATUS, 0);
    }

    public static void SetRememberChoiceStatus(Context ctx, boolean value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putBoolean(PSSPREF.IS_SHOW_INSTALL_DIALOG, value);
        editor.commit();
    }

    public static boolean GetRememberChoiceStatus(Context ctx) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getBoolean(PSSPREF.IS_SHOW_INSTALL_DIALOG, false);
    }

    public static void SendIntentToCPUPolicy(Context ctx, int Type) {
        Intent NoticeIntent = new Intent(ACTION.ACTION_CPU_POLICY);
        NoticeIntent.putExtra(EXTRA.PS_NOTICE_PARM, Type);
        ctx.sendBroadcast(NoticeIntent);
    }

    public static void triggerBacklightChange(Context ctx, boolean isChecked) {
        String mBrightness = LpmUtils.GetBacklight(ctx);
        Log.i(TAG, "[PowerSavingUtils] getBrightnessformPhone = " + mBrightness);
        if (mBrightness != LPMPARM.AUTOBRIGHTNESS) {
            int brightness = Integer.parseInt(mBrightness);
            if (isChecked) {
                brightness++;
            } else {
                brightness--;
            }
            if (brightness < 20) {
                brightness = 23;
            } else if (brightness > 255) {
                brightness = 252;
            }
            System.putInt(ctx.getContentResolver(), "screen_brightness", brightness);
        }
    }

    public static void SendNotification(Context mContext, int type) {
        String mSummary;
        int icon = 0;
        String mTitle = null;
        String mTicker = null;
        Class<?> mGoToClass = null;
        int mNotificationID = 0;
        Builder builder = new Builder(mContext);
        if (type == NOTIFICATION.PS_MODE) {
            mTitle = mContext.getResources().getString(C0321R.string.fih_power_saving_enabled_notify_2);
            icon = C0321R.drawable.ic_powersaver_powersaver;
            if (checkCurrentCheckItem(mContext)) {
                mSummary = mContext.getResources().getString(C0321R.string.fih_power_saving_apply_notify_msg);
                mTicker = mContext.getResources().getString(C0321R.string.fih_power_saving_apply_ticker_msg);
                Toast.makeText(mContext, mTicker, 0).show();
            } else {
                mSummary = "";
                mTicker = mTitle;
            }
            mGoToClass = MainActivity.class;
            mNotificationID = NOTIFICATION.PS_MODE;
        }
        mSummary = mContext.getResources().getString(C0321R.string.fih_power_saving_enabled_notify_summary);
        builder.setSmallIcon(icon);
        builder.setContentTitle(mTitle);
        builder.setContentText(mSummary);
        builder.setTicker(mTicker);
        builder.setWhen(System.currentTimeMillis());
        builder.setOngoing(true);
        Intent viewConvIntent = new Intent(mContext, mGoToClass);
        viewConvIntent.setFlags(335544320);
        builder.setContentIntent(PendingIntent.getActivity(mContext, 0, viewConvIntent, 0));
        ((NotificationManager) mContext.getSystemService("notification")).notify(mNotificationID, builder.build());
    }

    public static void CancelNotification(Context mContext, int NotificationID) {
        ((NotificationManager) mContext.getSystemService("notification")).cancel(NotificationID);
    }

    public static void NotificationPSUpdate(Context mContext, boolean isLastChecked) {
        int i = 1;
        if (GetPowerSavingModeEnable(mContext)) {
            int i2;
            int sum = 0;
            if (HasCPUPolicyAPK(mContext)) {
                if (getBooleanItemFromoDB(mContext, PSDB.CPU_POLICY)) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                sum = 0 + i2;
            }
            if (isSupportScreenPolicy(mContext)) {
                if (getBooleanItemFromoDB(mContext, PSDB.SCREEN_POLICY)) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                sum += i2;
            }
            if (getBooleanItemFromoDB(mContext, PSDB.DATA_CONNECTION)) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            sum += i2;
            if (!isSupportDozeMode(mContext)) {
                if (getBooleanItemFromoDB(mContext, PSDB.PW)) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                sum += i2;
            }
            if (getBooleanItemFromoDB(mContext, PSDB.LPM)) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            sum += i2;
            if (isSupportSmartSwitch()) {
                if (!getBooleanItemFromoDB(mContext, PSDB.SS)) {
                    i = 0;
                }
                sum += i;
            }
            Log.i(TAG, "[PowerSavingObserver] NotificationPSUpdate " + sum + " item applied");
            switch (sum) {
                case 0:
                    SendNotification(mContext, NOTIFICATION.PS_MODE);
                    return;
                case 1:
                    if (isLastChecked) {
                        SendNotification(mContext, NOTIFICATION.PS_MODE);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public static void ShowDialog(Context context, int messageResId, int titleID) {
        Log.i(TAG, "[PowerSavingUtils] ShowDialog() -ShowDialog");
        int titleResId = titleID;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleResId);
        builder.setMessage(messageResId);
        builder.setPositiveButton(C0321R.string.fih_power_saving_dialog_ok, new C03191());
        AlertDialog ad = builder.create();
        ad.setCanceledOnTouchOutside(false);
        ad.show();
    }

    public static void ShowDialog(Context context, int dialogID) {
        Log.i(TAG, "[PowerSavingUtils] ShowDialog() -ShowDialog");
        int messageResId = C0321R.string.app_label;
        switch (dialogID) {
            case 2002:
                messageResId = C0321R.string.fih_power_saving_data_turn_on_dialog_msg;
                break;
            case 2003:
                messageResId = C0321R.string.fih_power_saving_pw_turn_on_dialog_msg;
                break;
            case 2004:
                messageResId = C0321R.string.fih_power_saving_lpm_dialog_msg;
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(C0321R.string.fih_power_saving_notice_dialog_title);
        builder.setMessage(messageResId);
        builder.setPositiveButton(C0321R.string.fih_power_saving_dialog_ok, new C03202());
        AlertDialog ad = builder.create();
        ad.getWindow().setType(2003);
        ad.setCanceledOnTouchOutside(false);
        ad.show();
    }

    public static void setBooleanItemToDB(Context ctx, String key, boolean value) {
        if (key != null) {
            Log.i(TAG, "[PowerSavingUtils] setBooleanItemToDB " + key + " = " + value);
            if (IsUseNewMethodToStoreSettings(ctx)) {
                setBooleanItemToSelfDB(ctx, key, value);
            } else {
                System.putInt(ctx.getContentResolver(), key, value ? 1 : 0);
            }
        }
    }

    public static boolean getBooleanItemFromoDB(Context ctx, String key) {
        if (key == null) {
            return false;
        }
        if (IsUseNewMethodToStoreSettings(ctx)) {
            return getBooleanItemFromSelfDB(ctx, key);
        }
        if (System.getInt(ctx.getContentResolver(), key, 0) != 0) {
            return true;
        }
        return false;
    }

    public static void setStringItemToDB(Context mContext, String key, String value) {
        if (key != null) {
            Log.i(TAG, "[PowerSavingUtils] setStringItemToDB key = " + key + " value = " + value);
            if (IsUseNewMethodToStoreSettings(mContext)) {
                setStringItemToSelfDB(mContext, key, value);
            } else {
                System.putString(mContext.getContentResolver(), key, value);
            }
        }
    }

    public static String getStringItemFromDB(Context mContext, String key) {
        String GetValue;
        if (IsUseNewMethodToStoreSettings(mContext)) {
            GetValue = getStringItemFromSelfDB(mContext, key);
            if (CorrectedValue(mContext, key, GetValue)) {
                return GetValue;
            }
            return getStringItemFromSelfDB(mContext, key);
        }
        GetValue = System.getString(mContext.getContentResolver(), key);
        if (CorrectedValue(mContext, key, GetValue)) {
            return GetValue;
        }
        return System.getString(mContext.getContentResolver(), key);
    }

    public static void SetPowerSavingModeEnable(Context ctx, boolean value) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            setStringItemToSelfDB(ctx, PSDB.MAIN, value ? "1" : SYMBOLS.ZERO);
        } else {
            System.putInt(ctx.getContentResolver(), PSDB.MAIN, value ? 1 : 0);
        }
    }

    public static boolean GetPowerSavingModeEnable(Context ctx) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            return "1".equals(getStringItemFromSelfDB(ctx, PSDB.MAIN));
        }
        if (System.getInt(ctx.getContentResolver(), PSDB.MAIN, 0) != 0) {
            return true;
        }
        return false;
    }

    public static void SetCPUPolicyEnable(Context ctx, boolean value) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            setStringItemToSelfDB(ctx, PSDB.CPU_POLICY, value ? "1" : SYMBOLS.ZERO);
        } else {
            System.putInt(ctx.getContentResolver(), PSDB.CPU_POLICY, value ? 1 : 0);
        }
    }

    public static boolean GetCPUPolicyEnable(Context ctx) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            return "1".equals(getStringItemFromSelfDB(ctx, PSDB.CPU_POLICY));
        }
        if (System.getInt(ctx.getContentResolver(), PSDB.CPU_POLICY, 0) != 0) {
            return true;
        }
        return false;
    }

    public static void SetScreenPolicyEnable(Context ctx, boolean value) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            setStringItemToSelfDB(ctx, PSDB.SCREEN_POLICY, value ? "1" : SYMBOLS.ZERO);
        } else {
            System.putInt(ctx.getContentResolver(), PSDB.SCREEN_POLICY, value ? 1 : 0);
        }
    }

    public static boolean GetScreenPolicyEnable(Context ctx) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            return "1".equals(getStringItemFromSelfDB(ctx, PSDB.SCREEN_POLICY));
        }
        if (System.getInt(ctx.getContentResolver(), PSDB.SCREEN_POLICY, 0) != 0) {
            return true;
        }
        return false;
    }

    public static void SetDataConnectionEnable(Context ctx, boolean value) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            setStringItemToSelfDB(ctx, PSDB.DATA_CONNECTION, value ? "1" : SYMBOLS.ZERO);
        } else {
            System.putInt(ctx.getContentResolver(), PSDB.DATA_CONNECTION, value ? 1 : 0);
        }
    }

    public static boolean GetDataConnectionEnable(Context ctx) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            return "1".equals(getStringItemFromSelfDB(ctx, PSDB.DATA_CONNECTION));
        }
        if (System.getInt(ctx.getContentResolver(), PSDB.DATA_CONNECTION, 0) != 0) {
            return true;
        }
        return false;
    }

    public static void SetPWEnable(Context ctx, boolean value) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            setStringItemToSelfDB(ctx, PSDB.PW, value ? "1" : SYMBOLS.ZERO);
        } else {
            System.putInt(ctx.getContentResolver(), PSDB.PW, value ? 1 : 0);
        }
    }

    public static boolean GetPWEnable(Context ctx) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            return "1".equals(getStringItemFromSelfDB(ctx, PSDB.PW));
        }
        if (System.getInt(ctx.getContentResolver(), PSDB.PW, 0) != 0) {
            return true;
        }
        return false;
    }

    public static void SetLPMEnable(Context ctx, boolean value) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            setStringItemToSelfDB(ctx, PSDB.LPM, value ? "1" : SYMBOLS.ZERO);
        } else {
            System.putInt(ctx.getContentResolver(), PSDB.LPM, value ? 1 : 0);
        }
    }

    public static boolean GetLPMEnable(Context ctx) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            return "1".equals(getStringItemFromSelfDB(ctx, PSDB.LPM));
        }
        if (System.getInt(ctx.getContentResolver(), PSDB.LPM, 0) != 0) {
            return true;
        }
        return false;
    }

    public static void SetSSEnable(Context ctx, boolean value) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            setStringItemToSelfDB(ctx, PSDB.SS, value ? "1" : SYMBOLS.ZERO);
        } else {
            System.putInt(ctx.getContentResolver(), PSDB.SS, value ? 1 : 0);
        }
    }

    public static boolean GetSSEnable(Context ctx) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            return "1".equals(getStringItemFromSelfDB(ctx, PSDB.SS));
        }
        if (System.getInt(ctx.getContentResolver(), PSDB.SS, 0) != 0) {
            return true;
        }
        return false;
    }

    public static boolean HasCPUPolicyAPK(Context ctx) {
        if (ProjectInfo.IsSupportCPUPolicy(ctx)) {
            return true;
        }
        return false;
    }

    private static boolean CorrectedValue(Context ctx, String mKey, String Value) {
        boolean mCorrect = false;
        String DefalutValue;
        if (mKey.equals(LPMDB.BEGIN)) {
            if (Value != null) {
                return true;
            }
            DefalutValue = ProjectInfo.getConfigFromExternal(ctx, mKey);
            if (DefalutValue == null) {
                DefalutValue = ProjectInfo.getConfigString(ctx, DBItemtransferInternelConfigItem(mKey));
            }
            setStringItemToDB(ctx, mKey, DefalutValue);
            return false;
        } else if (mKey.equals(LPMDB.WIFI) || mKey.equals(LPMDB.BT) || mKey.equals(LPMDB.GPS) || mKey.equals(LPMDB.MOBILE_DATA) || mKey.equals(LPMDB.D3_SOUND) || mKey.equals(LPMDB.ANIMATION) || mKey.equals(LPMDB.VIBRATION) || mKey.equals(LPMDB.BACKGROUND_DATA) || mKey.equals(LPMDB.WIFI_HOTSPOT) || mKey.equals(LPMDB.AUTOSYNC) || mKey.equals(LPMDB.MONOCHROMACY) || mKey.equals(LPMDB.DATA_CONNECTION) || mKey.equals(LPMDB.SMART_SWITCH) || mKey.equals(LPMDB.SCREEN_LIGHT) || mKey.equals(LPMDB.GLANCE) || mKey.equals(LPMDB.EXTREME) || mKey.equals("powersaving_db_power_saving_mode") || mKey.equals(LPMDB.BAM) || mKey.equals(LPMDB.SCREEN_RESOLUTION) || mKey.equals(LPMDB.CPU_LIMIT) || mKey.equals(LPMDB.BATTERY_SAVER)) {
            if (Value != null && (Value.equals(SWITCHER.KEEP) || Value.equals(SWITCHER.OFF))) {
                return true;
            }
            DefalutValue = ProjectInfo.getConfigFromExternal(ctx, mKey);
            if (DefalutValue == null) {
                DefalutValue = ProjectInfo.getConfigString(ctx, DBItemtransferInternelConfigItem(mKey));
            }
            setStringItemToDB(ctx, mKey, DefalutValue);
            return false;
        } else if (mKey.equals(LPMDB.SCREEN_TIMEOUT)) {
            String[] lpm_screen_timeout_items = ctx.getResources().getStringArray(C0321R.array.fih_power_saving_lpm_screen_timeout_values);
            if (Value != null) {
                for (Object equals : lpm_screen_timeout_items) {
                    if (Value.equals(equals)) {
                        mCorrect = true;
                        break;
                    }
                }
            }
            if (mCorrect) {
                return mCorrect;
            }
            DefalutValue = ProjectInfo.getConfigFromExternal(ctx, mKey);
            if (DefalutValue == null) {
                DefalutValue = ProjectInfo.getConfigString(ctx, DBItemtransferInternelConfigItem(mKey));
            }
            setStringItemToDB(ctx, mKey, DefalutValue);
            return mCorrect;
        } else if (mKey.equals(PWDB.PW_PW_WHITELIST)) {
            return true;
        } else {
            if (mKey.equals(DCDB.PW_DATA_WHITELIST)) {
                return true;
            }
            if (mKey.equals(DCDB.PW_DATA_STARTTIME) || mKey.equals(DCDB.PW_DATA_ENDTIME)) {
                if (Value != null && Value.contains(":")) {
                    return true;
                }
                DefalutValue = ProjectInfo.getConfigFromExternal(ctx, mKey);
                if (DefalutValue == null) {
                    DefalutValue = ProjectInfo.getConfigString(ctx, DBItemtransferInternelConfigItem(mKey));
                }
                setStringItemToDB(ctx, mKey, DefalutValue);
                return false;
            } else if (mKey.equals(DCDB.PW_DATA_ALWAYSON)) {
                if (Value != null && (Value.equals(SWITCHER.ON) || Value.equals(SWITCHER.OFF))) {
                    return true;
                }
                DefalutValue = ProjectInfo.getConfigFromExternal(ctx, mKey);
                if (DefalutValue == null) {
                    DefalutValue = ProjectInfo.getConfigString(ctx, DBItemtransferInternelConfigItem(mKey));
                }
                setStringItemToDB(ctx, mKey, DefalutValue);
                return false;
            } else if (mKey.equals(PWDB.PW_PW_TIME)) {
                if (Value != null) {
                    return true;
                }
                DefalutValue = ProjectInfo.getConfigFromExternal(ctx, mKey);
                if (DefalutValue == null) {
                    DefalutValue = ProjectInfo.getConfigString(ctx, DBItemtransferInternelConfigItem(mKey));
                }
                setStringItemToDB(ctx, mKey, DefalutValue);
                return false;
            } else if (mKey.equals(TSDB.TIME_SCHEDULE) || mKey.equals(TSDB.TIME_SCHEDULE_MODE) || mKey.equals(TSDB.TIME_SCHEDULE_START_TIME) || mKey.equals(TSDB.TIME_SCHEDULE_END_TIME)) {
                if (Value != null) {
                    return true;
                }
                DefalutValue = ProjectInfo.getConfigFromExternal(ctx, mKey);
                if (DefalutValue == null) {
                    DefalutValue = ProjectInfo.getConfigString(ctx, DBItemtransferInternelConfigItem(mKey));
                }
                setStringItemToDB(ctx, mKey, DefalutValue);
                return false;
            } else if (!mKey.equals(PROCESS_MONITOR.SWITCH_NAME)) {
                return false;
            } else {
                if (Value != null) {
                    return true;
                }
                DefalutValue = ProjectInfo.getConfigFromExternal(ctx, mKey);
                if (DefalutValue == null) {
                    DefalutValue = ProjectInfo.getConfigString(ctx, DBItemtransferInternelConfigItem(mKey));
                }
                setStringItemToDB(ctx, mKey, DefalutValue);
                return false;
            }
        }
    }

    public static int DBItemtransferInternelConfigItem(String mKey) {
        if (mKey.equals(PSDB.MAIN)) {
            return C0321R.bool.powersaving_db_main;
        }
        if (mKey.equals(PSDB.CPU_POLICY)) {
            return C0321R.bool.powersaving_db_cpu_policy;
        }
        if (mKey.equals(PSDB.SCREEN_POLICY)) {
            return C0321R.bool.powersaving_db_screen_policy;
        }
        if (mKey.equals(PSDB.DATA_CONNECTION)) {
            return C0321R.bool.powersaving_db_data_connection;
        }
        if (mKey.equals(PSDB.PW)) {
            return C0321R.bool.powersaving_db_periodic_wakeup;
        }
        if (mKey.equals(PSDB.LPM)) {
            return C0321R.bool.powersaving_db_lpm;
        }
        if (mKey.equals(LPMDB.BEGIN)) {
            return C0321R.string.powersaving_db_power_saving_begin;
        }
        if (mKey.equals(LPMDB.WIFI)) {
            return C0321R.string.powersaving_db_wifi;
        }
        if (mKey.equals(LPMDB.BT)) {
            return C0321R.string.powersaving_db_bt;
        }
        if (mKey.equals(LPMDB.GPS)) {
            return C0321R.string.powersaving_db_gps;
        }
        if (mKey.equals(LPMDB.MOBILE_DATA)) {
            return C0321R.string.powersaving_db_mobile_data;
        }
        if (mKey.equals(LPMDB.D3_SOUND)) {
            return C0321R.string.powersaving_db_3d_sound;
        }
        if (mKey.equals(LPMDB.ANIMATION)) {
            return C0321R.string.powersaving_db_animation;
        }
        if (mKey.equals(LPMDB.VIBRATION)) {
            return C0321R.string.powersaving_db_limit_vibrate;
        }
        if (mKey.equals(LPMDB.BACKGROUND_DATA)) {
            return C0321R.string.powersaving_db_restrict_bd;
        }
        if (mKey.equals(LPMDB.SCREEN_TIMEOUT)) {
            return C0321R.string.powersaving_db_screen_timeout;
        }
        if (mKey.equals(DCDB.PW_DATA_ALWAYSON)) {
            return C0321R.string.powersaving_db_dc_alwayson;
        }
        if (mKey.equals(DCDB.PW_DATA_STARTTIME)) {
            return C0321R.string.powersaving_db_dc_start_time;
        }
        if (mKey.equals(DCDB.PW_DATA_ENDTIME)) {
            return C0321R.string.powersaving_db_dc_end_time;
        }
        if (mKey.equals(DCDB.PW_DATA_WHITELIST)) {
            return C0321R.string.powersaving_db_dc_white_list;
        }
        if (mKey.equals(PWDB.PW_PW_WHITELIST)) {
            return C0321R.string.powersaving_db_pw_white_list;
        }
        if (mKey.equals(PWDB.PW_PW_HIDELIST)) {
            return C0321R.string.powersaving_db_pw_hide_list;
        }
        if (mKey.equals(PWDB.PW_PW_TIME)) {
            return C0321R.string.powersaving_db_pw_time;
        }
        if (mKey.equals(LPMDB.WIFI_HOTSPOT)) {
            return C0321R.string.powersaving_db_wifihotspot;
        }
        if (mKey.equals(LPMDB.CPU_LIMIT)) {
            return C0321R.string.powersaving_db_cpu_limit;
        }
        if (mKey.equals(TSDB.TIME_SCHEDULE)) {
            return C0321R.string.powersaving_db_time_schedule;
        }
        if (mKey.equals(TSDB.TIME_SCHEDULE_MODE)) {
            return C0321R.string.powersaving_db_time_schedule_mode;
        }
        if (mKey.equals(TSDB.TIME_SCHEDULE_START_TIME)) {
            return C0321R.string.powersaving_db_time_schedule_start_time;
        }
        if (mKey.equals(TSDB.TIME_SCHEDULE_END_TIME)) {
            return C0321R.string.powersaving_db_time_schedule_end_time;
        }
        if (mKey.equals(LPMDB.AUTOSYNC)) {
            return C0321R.string.powersaving_db_autosync;
        }
        if (mKey.equals(LPMDB.MONOCHROMACY)) {
            return C0321R.string.powersaving_db_monochromacy;
        }
        if (mKey.equals(LPMDB.DATA_CONNECTION)) {
            return C0321R.string.powersaving_db_data_connection_new;
        }
        if (mKey.equals(LPMDB.SMART_SWITCH)) {
            return C0321R.string.powersaving_db_smart_switch;
        }
        if (mKey.equals(LPMDB.SCREEN_LIGHT)) {
            return C0321R.string.powersaving_db_screen_light;
        }
        if (mKey.equals(LPMDB.GLANCE)) {
            return C0321R.string.powersaving_db_glance;
        }
        if (mKey.equals(LPMDB.EXTREME)) {
            return C0321R.string.powersaving_db_power_saving_extreme;
        }
        if (mKey.equals("powersaving_db_power_saving_mode")) {
            return C0321R.string.powersaving_db_power_saving_mode;
        }
        if (mKey.equals(LPMDB.BAM)) {
            return C0321R.string.powersaving_db_screen_bam;
        }
        if (mKey.equals(LPMDB.SCREEN_RESOLUTION)) {
            return C0321R.string.powersaving_db_screen_resolution;
        }
        if (mKey.equals(PROCESS_MONITOR.SWITCH_NAME)) {
            return C0321R.string.powersaving_db_process_monitor;
        }
        if (mKey.equals(LPMDB.BATTERY_SAVER)) {
            return C0321R.string.powersaving_db_battery_saver;
        }
        return 0;
    }

    public static void CheckG2ToG3Item(Context mContext, String key) {
        getStringItemFromDB(mContext, key);
    }

    public static boolean GetWiFiEnableByDB(Context mContext) {
        return ((WifiManager) mContext.getSystemService(SSPARM.WIFI)).isWifiEnabled();
    }

    public static boolean checkCurrentCheckItem(Context mContext) {
        boolean isCPUOn = false;
        if (HasCPUPolicyAPK(mContext)) {
            isCPUOn = getBooleanItemFromoDB(mContext, PSDB.CPU_POLICY);
        }
        boolean isSCREENOn = false;
        if (isSupportScreenPolicy(mContext)) {
            isSCREENOn = getBooleanItemFromoDB(mContext, PSDB.SCREEN_POLICY);
        }
        boolean isDCOn = getBooleanItemFromoDB(mContext, PSDB.DATA_CONNECTION);
        boolean isPWOn = false;
        if (!isSupportDozeMode(mContext)) {
            isPWOn = getBooleanItemFromoDB(mContext, PSDB.PW);
        }
        boolean isLPMOn = getBooleanItemFromoDB(mContext, PSDB.LPM);
        boolean isSSOn = false;
        if (isSupportSmartSwitch()) {
            isSSOn = getBooleanItemFromoDB(mContext, PSDB.SS);
        }
        if (isCPUOn || isSCREENOn || isDCOn || isPWOn || isLPMOn || isSSOn) {
            return false;
        }
        return true;
    }

    public static void setHotspotstate(Context ctx, boolean value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putBoolean(SSPARM.HOTSPOTSTATE, value);
        editor.commit();
    }

    public static boolean getHotspotstate(Context ctx) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getBoolean(SSPARM.HOTSPOTSTATE, false);
    }

    public static boolean getConfigBoolean(Context ctx, int mKey) {
        return ctx.getResources().getBoolean(mKey);
    }

    public static String getConfigString(Context ctx, int mKey) {
        return ctx.getResources().getString(mKey);
    }

    public static void checkDefaultValueInDB(Context ctx) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            if (getStringItemFromDB(ctx, PSDB.SS) == null) {
                setStringItemToDB(ctx, PSDB.SS, getConfigBoolean(ctx, C0321R.bool.powersaving_db_ss) ? "1" : SYMBOLS.ZERO);
            }
        } else if (System.getInt(ctx.getContentResolver(), PSDB.SS, -1) == -1) {
            setBooleanItemToDB(ctx, PSDB.SS, getConfigBoolean(ctx, C0321R.bool.powersaving_db_ss));
        }
        if (getStringItemFromDB(ctx, SSDB.WIFI_TIMEOUT) == null) {
            setStringItemToDB(ctx, SSDB.WIFI_TIMEOUT, getConfigString(ctx, C0321R.string.powersaving_db_wifi_timeout));
        }
        if (getStringItemFromDB(ctx, SSDB.HOTSPOT_TIMEOUT) == null) {
            setStringItemToDB(ctx, SSDB.HOTSPOT_TIMEOUT, getConfigString(ctx, C0321R.string.powersaving_db_hotspot_timeout));
        }
        if (getStringItemFromDB(ctx, SSDB.WIFI) == null) {
            setStringItemToDB(ctx, SSDB.WIFI, getConfigString(ctx, C0321R.string.powersaving_db_ss_wifi));
        }
        if (getStringItemFromDB(ctx, SSDB.HOTSPOT) == null) {
            setStringItemToDB(ctx, SSDB.HOTSPOT, getConfigString(ctx, C0321R.string.powersaving_db_ss_hotspot));
        }
        if (getStringItemFromDB(ctx, DCDB.PW_DATA_DETECTTIME) == null) {
            setStringItemToDB(ctx, DCDB.PW_DATA_DETECTTIME, getConfigString(ctx, C0321R.string.powersaving_db_dc_detect_time));
        }
        if (getStringItemFromDB(ctx, LPMDB.BATTERY_INTENT_MIN_INTERVAL) == null) {
            setStringItemToDB(ctx, LPMDB.BATTERY_INTENT_MIN_INTERVAL, getConfigString(ctx, C0321R.string.powersaving_db_battery_change_intent_min_interval));
        }
    }

    public static boolean isPackageExist(Context context, Intent intent) {
        try {
            List<ResolveInfo> activitiesList = context.getPackageManager().queryIntentActivities(intent, 65536);
            if (activitiesList == null || activitiesList.isEmpty()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.d(TAG, "isPackageExist exception");
            e.printStackTrace();
            return false;
        }
    }

    public static void setTetherState(Context ctx, boolean value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putBoolean(PARM.TETHER, value);
        editor.commit();
    }

    public static boolean getTetherState(Context ctx) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getBoolean(PARM.TETHER, false);
    }

    public static boolean checkDeviceIsVKYOrVK3() {
        String mProduct = Build.DEVICE;
        Log.i(TAG, "Product ID: " + mProduct);
        if (mProduct == null || (!mProduct.equals(PARM.VKY) && !mProduct.equals(PARM.VK3))) {
            return false;
        }
        return true;
    }

    public static boolean checkAndroidVersion() {
        Log.i(TAG, "Android Version: " + VERSION.SDK_INT);
        if (VERSION.SDK_INT <= 18) {
            return true;
        }
        return false;
    }

    public static boolean isSupportSmartSwitch() {
        if (checkDeviceIsVKYOrVK3() && checkAndroidVersion()) {
            return false;
        }
        return true;
    }

    public static void setAlarmToCheckService(Context mContext) {
        String mServiceDetectOn = getStringItemFromDB(mContext, PSDB.SERVICE_DETECT);
        if (mServiceDetectOn == null) {
            String mDefaultValue = mContext.getResources().getString(C0321R.string.powersaving_db_service_detect);
            setStringItemToDB(mContext, PSDB.SERVICE_DETECT, mDefaultValue);
            mServiceDetectOn = mDefaultValue;
        }
        if (mServiceDetectOn == null || SWITCHER.OFF.equals(mServiceDetectOn)) {
            Log.i(TAG, "Service detect disable.");
            return;
        }
        int detect_service_time;
        String detect_time = getStringItemFromDB(mContext, PSDB.SERVICE_DETECT_TIME);
        if (detect_time != null) {
            detect_service_time = Integer.valueOf(detect_time).intValue();
        } else {
            detect_time = mContext.getResources().getString(C0321R.string.powersaving_db_service_detect_time);
            if (detect_time == null || detect_time.equals("")) {
                detect_service_time = 180000;
            } else {
                detect_service_time = Integer.valueOf(detect_time).intValue();
            }
            setStringItemToDB(mContext, PSDB.SERVICE_DETECT_TIME, String.valueOf(detect_service_time));
        }
        ((AlarmManager) mContext.getSystemService("alarm")).set(1, System.currentTimeMillis() + ((long) detect_service_time), PendingIntent.getBroadcast(mContext, 1, new Intent("com.fihtdc.powersaving.period_check_service"), 134217728));
    }

    public static int findPID() {
        int pid = 0;
        try {
            pid = Process.myPid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pid;
    }

    public static void setAlarmToRestartService(Context mContext) {
        ((AlarmManager) mContext.getSystemService("alarm")).set(1, System.currentTimeMillis() + ((long) FileOperator.MAX_DIR_LENGTH), PendingIntent.getBroadcast(mContext, 1, new Intent("com.fihtdc.powersaving.period_check_service"), 134217728));
    }

    public static boolean isShowSSHotSpot(Context mContext) {
        String mValue = ProjectInfo.getConfigFromExternal(mContext, SSDB.SHOW_HOTSPOT);
        if (mValue == null) {
            mValue = ProjectInfo.getConfigString(mContext, C0321R.string.powersaving_db_ss_show_hotspot);
            if (mValue == null) {
                Log.i(TAG, "External config and internal config do not have tag, return value depend on system property");
                if (isMtkPlatform(mContext)) {
                    return false;
                }
                return true;
            } else if (mValue.equals("NO")) {
                return false;
            } else {
                return true;
            }
        } else if (mValue.equals("NO")) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isMtkPlatform(Context mContext) {
        boolean bMTKPlatform = true;
        String productBoard = Build.BOARD;
        if (!TextUtils.isEmpty(productBoard)) {
            productBoard = productBoard.trim();
            String mChipName = ProjectInfo.getConfigFromExternal(mContext, PSDB.QCOM_CHIP_NAME);
            String[] mChipNameArray;
            if (mChipName == null) {
                Log.i(TAG, "External mChipName is Null!!");
                mChipName = ProjectInfo.getConfigString(mContext, C0321R.string.powersaving_db_qcom_chip_name);
                if (mChipName == null) {
                    Log.i(TAG, "Internal mChipName is Null!!");
                    return false;
                }
                mChipNameArray = mChipName.split(SYMBOLS.SEMICOLON);
                for (String startsWith : mChipNameArray) {
                    if (productBoard.startsWith(startsWith)) {
                        bMTKPlatform = false;
                        break;
                    }
                    bMTKPlatform = true;
                }
            } else {
                mChipNameArray = mChipName.split(SYMBOLS.SEMICOLON);
                for (String startsWith2 : mChipNameArray) {
                    if (productBoard.startsWith(startsWith2)) {
                        bMTKPlatform = false;
                        break;
                    }
                    bMTKPlatform = true;
                }
            }
        }
        return bMTKPlatform;
    }

    public static void cancelAlarmToCheckService(Context mContext) {
        ((AlarmManager) mContext.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(mContext, 1, new Intent("com.fihtdc.powersaving.period_check_service"), 134217728));
    }

    public static boolean IsUseNewMethodToStartService(Context mContext) {
        String mStartServiceMethod = ProjectInfo.getConfigFromExternal(mContext, PARM.START_SERVICE_METHOD);
        if (mStartServiceMethod != null && mStartServiceMethod.equals(PARM.START_SERVICE_USE_INTENT)) {
            return true;
        }
        mStartServiceMethod = ProjectInfo.getConfigString(mContext, C0321R.string.powersaving_start_service_method);
        if (mStartServiceMethod == null || !mStartServiceMethod.equals(PARM.START_SERVICE_USE_INTENT)) {
            return false;
        }
        return true;
    }

    public static void SetFunctionByOtherAPK(Context mContext, Bundle mBundle) {
        if (mBundle != null) {
            int mSubEnable;
            boolean mNowStatus;
            if (GetPowerSavingModeEnable(mContext)) {
                Log.i(TAG, "[PowerSavingUtils]:[SetFunctionByOtherAPK] powersaver is ON");
            } else {
                Log.i(TAG, "[PowerSavingUtils]:[SetFunctionByOtherAPK]  powersaver is OFF");
            }
            if (mBundle.containsKey(FUNCTION.EXTRA.CPU_ENABLE)) {
                mSubEnable = mBundle.getInt(FUNCTION.EXTRA.CPU_ENABLE, -1);
                mNowStatus = GetCPUPolicyEnable(mContext);
                if (mSubEnable == 1 && !mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn CPU = ON");
                    SetCPUPolicyEnable(mContext, true);
                } else if (mSubEnable == 0 && mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn CPU = OFF");
                    SetCPUPolicyEnable(mContext, false);
                }
            }
            if (mBundle.containsKey(FUNCTION.EXTRA.SCREEN_ENABLE)) {
                mSubEnable = mBundle.getInt(FUNCTION.EXTRA.SCREEN_ENABLE, -1);
                mNowStatus = GetScreenPolicyEnable(mContext);
                if (mSubEnable == 1 && !mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn SCREEN POLICY = ON");
                    SetScreenPolicyEnable(mContext, true);
                } else if (mSubEnable == 0 && mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn SCREEN POLICY = OFF");
                    SetScreenPolicyEnable(mContext, false);
                }
            }
            if (mBundle.containsKey(FUNCTION.EXTRA.DC_ENABLE)) {
                mSubEnable = mBundle.getInt(FUNCTION.EXTRA.DC_ENABLE, -1);
                mNowStatus = GetDataConnectionEnable(mContext);
                if (mSubEnable == 1 && !mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn DC = ON");
                    SetDataConnectionEnable(mContext, true);
                } else if (mSubEnable == 0 && mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn DC = OFF");
                    SetDataConnectionEnable(mContext, false);
                }
            }
            if (mBundle.containsKey(FUNCTION.EXTRA.PW_ENABLE)) {
                mSubEnable = mBundle.getInt(FUNCTION.EXTRA.PW_ENABLE, -1);
                mNowStatus = GetPWEnable(mContext);
                if (mSubEnable == 1 && !mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn PW = ON");
                    SetPWEnable(mContext, true);
                } else if (mSubEnable == 0 && mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn PW = OFF");
                    SetPWEnable(mContext, false);
                }
            }
            if (mBundle.containsKey(FUNCTION.EXTRA.LPM_ENABLE)) {
                mSubEnable = mBundle.getInt(FUNCTION.EXTRA.LPM_ENABLE, -1);
                mNowStatus = GetLPMEnable(mContext);
                if (mSubEnable == 1 && !mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn LPM = ON");
                    SetLPMEnable(mContext, true);
                } else if (mSubEnable == 0 && mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn LPM = OFF");
                    SetLPMEnable(mContext, false);
                }
            }
            if (mBundle.containsKey(FUNCTION.EXTRA.SS_ENABLE)) {
                mSubEnable = mBundle.getInt(FUNCTION.EXTRA.SS_ENABLE, -1);
                mNowStatus = GetSSEnable(mContext);
                if (mSubEnable == 1 && !mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn SS = ON");
                    SetSSEnable(mContext, true);
                } else if (mSubEnable == 0 && mNowStatus) {
                    Log.i(TAG, "[PowerSavingUtils]: [SetFunctionByOtherAPK] other apk turn SS = OFF");
                    SetSSEnable(mContext, false);
                }
            }
        }
    }

    public static void SetMobileDataEnable(Context mContext, boolean enabled) {
        TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService("phone");
        if (enabled) {
            try {
                mTelephonyManager.enableDataConnectivity();
                return;
            } catch (Exception e) {
                Log.i(TAG, "[PowerSavingUtils]: [SetMobileDataEnable] fail : ");
                e.printStackTrace();
                return;
            }
        }
        mTelephonyManager.disableDataConnectivity();
    }

    public static boolean GetMobileDataEnable(Context mContext) {
        return ((TelephonyManager) mContext.getSystemService("phone")).getDataEnabled();
    }

    public static void setLPMHotspotstate(Context ctx, boolean value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putBoolean(LPMSPREF.WIFI_HOTSPOT, value);
        editor.commit();
    }

    public static boolean getLPMHotspotstate(Context ctx) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getBoolean(LPMSPREF.WIFI_HOTSPOT, false);
    }

    public static boolean checkPermission(Context mContext, int type) {
        boolean isGranted = false;
        Log.i(TAG, "[PowerSavingUtils]: [checkPermission] type is :" + type);
        switch (type) {
            case TYPE.SYSTEM_ALERT_WINDOW /*3000*/:
                isGranted = Settings.canDrawOverlays(mContext);
                break;
            case TYPE.WRITE_SETTINGS /*3001*/:
                isGranted = System.canWrite(mContext);
                break;
            default:
                Log.i(TAG, "[PowerSavingUtils]: [checkPermission] type is not match.");
                break;
        }
        Log.i(TAG, "[PowerSavingUtils]: [checkPermission] the permission is :" + isGranted);
        return isGranted;
    }

    public static void ShowPermissionDialog(Context mContext, int type, int function, boolean isChecked, int triggerFrom) {
        Log.i(TAG, "[PowerSavingUtils]: [ShowPermissionDialog] type: " + type + ", function: " + function + ", isChecked: " + isChecked + ", triggerFrom: " + triggerFrom);
        Intent it = new Intent(mContext, AlertActivity.class);
        it.setFlags(268468224);
        it.putExtra(EXTRA_KEY.TYPE, type);
        it.putExtra("function", function);
        it.putExtra("isChecked", isChecked);
        it.putExtra("triggerFrom", triggerFrom);
        mContext.startActivity(it);
    }

    public static void ShowPermissionNotification(Context mContext, int type, int function, int triggerFrom) {
        int icon = 0;
        String mTitle = null;
        String mSummary = null;
        String mTicker = null;
        Class<?> mGoToClass = null;
        int mNotificationID = 0;
        Builder builder = new Builder(mContext);
        Log.i(TAG, "[PowerSavingUtils]: [ShowPermissionNotification] type: " + type + ", function: " + function + ", triggerFrom: " + triggerFrom);
        if (function == 2012) {
            mTitle = mContext.getResources().getString(C0321R.string.fih_power_saving_permission_lpm_notification_title, new Object[]{mContext.getResources().getString(C0321R.string.app_label)});
            icon = C0321R.drawable.ic_powersaver_powersaver;
            mSummary = mContext.getResources().getString(C0321R.string.fih_power_saving_permission_lpm_notification_summary);
            mTicker = mTitle;
            mGoToClass = AlertActivity.class;
            mNotificationID = 2002;
        } else if (function == 2013) {
            mTitle = mContext.getResources().getString(C0321R.string.fih_power_saving_permission_lpm_notification_title, new Object[]{mContext.getResources().getString(C0321R.string.app_label)});
            icon = C0321R.drawable.ic_powersaver_powersaver;
            mSummary = mContext.getResources().getString(C0321R.string.fih_power_saving_permission_lpm_notification_summary);
            mTicker = mTitle;
            mGoToClass = AlertActivity.class;
            mNotificationID = 2002;
        }
        builder.setSmallIcon(icon);
        builder.setContentTitle(mTitle);
        builder.setContentText(mSummary);
        builder.setTicker(mTicker);
        builder.setWhen(System.currentTimeMillis());
        builder.setOngoing(false);
        Intent viewConvIntent = new Intent(mContext, mGoToClass);
        viewConvIntent.setFlags(335544320);
        viewConvIntent.putExtra(EXTRA_KEY.TYPE, type);
        viewConvIntent.putExtra("function", function);
        viewConvIntent.putExtra("triggerFrom", triggerFrom);
        builder.setContentIntent(PendingIntent.getActivity(mContext, 0, viewConvIntent, 0));
        ((NotificationManager) mContext.getSystemService("notification")).notify(mNotificationID, builder.build());
    }

    public static boolean isNeedChangeWlan(Context mContext) {
        boolean needChangeWlan = false;
        try {
            Resources settingRes = mContext.getPackageManager().getResourcesForApplication("com.android.settings");
            int resID = settingRes.getIdentifier("wifi_settings_title", "string", "com.android.settings");
            String str = settingRes.getString(resID);
            if (str != null && str.equals("WLAN")) {
                return true;
            }
            AssetManager assets = settingRes.getAssets();
            DisplayMetrics metrics = settingRes.getDisplayMetrics();
            Configuration config = new Configuration(settingRes.getConfiguration());
            Locale defaultLocale = config.locale;
            config.setLocale(new Locale("en"));
            settingRes.updateConfiguration(config, null);
            str = new Resources(assets, metrics, config).getString(resID);
            if (str != null && str.equals("WLAN")) {
                needChangeWlan = true;
            }
            config.setLocale(defaultLocale);
            settingRes.updateConfiguration(config, null);
            return needChangeWlan;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            String mModel = Build.DISPLAY;
            for (CharSequence contains : PARM.WLAN_MODEL) {
                if (mModel.contains(contains)) {
                    needChangeWlan = true;
                    break;
                }
            }
            if (!needChangeWlan) {
                return needChangeWlan;
            }
            Log.i(TAG, "Model: " + mModel);
            return needChangeWlan;
        }
    }

    public static Resources getSettingsResource(Context mContext) {
        Resources settingRes = null;
        try {
            settingRes = mContext.getPackageManager().getResourcesForApplication("com.android.settings");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return settingRes;
    }

    public static int getSettingsResourceID(Context mContext, String resName) {
        int resID = -1;
        try {
            resID = mContext.getPackageManager().getResourcesForApplication("com.android.settings").getIdentifier(resName, "string", "com.android.settings");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return resID;
    }

    public static String getSettingsResourceStringValue(Context mContext, String resName) {
        String value = "";
        try {
            Resources settingRes = mContext.getPackageManager().getResourcesForApplication("com.android.settings");
            int resID = settingRes.getIdentifier(resName, "string", "com.android.settings");
            if (resID != 0) {
                value = settingRes.getString(resID);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return value;
    }

    public static boolean isSupportScreenPolicy(Context context) {
        return ProjectInfo.isSupportScreenPolicy(context);
    }

    public static boolean isSupportDozeMode(Context context) {
        if (!ProjectInfo.isSupportDozeConfig()) {
            Log.i(TAG, "PowerSavingUtils: isSupportDozeConfig()= No");
            return false;
        } else if (context.getResources().getBoolean(C0321R.bool.powersaving_support_doze_mode) && context.getResources().getBoolean(C0321R.bool.powersaving_support_doze_mode_model)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSupportTGGUI(Context mContext) {
        return ProjectInfo.isSupportTGGUI(mContext);
    }

    public static boolean isSupportAmoledDisplay() {
        return ProjectInfo.isSupportAmoledConfig();
    }

    public static String[] getNormalModeCpuLimitSpeedList() {
        return ProjectInfo.getNormalModeCpuLimitSpeedList();
    }

    public static String[] getExtremeModeCpuLimitSpeedList() {
        return ProjectInfo.getExtremeModeCpuLimitSpeedList();
    }

    public static String[] getCpuLimitOpcode1List() {
        return ProjectInfo.getCpuLimitOpcode1List();
    }

    public static String[] getCpuLimitOpcode2List() {
        return ProjectInfo.getCpuLimitOpcode2List();
    }

    public static String[] getNormalModeSaveTimeList() {
        return ProjectInfo.getNormalModeSaveTimeList();
    }

    public static String[] getExtremeModeSaveTimeList() {
        return ProjectInfo.getExtremeModeSaveTimeList();
    }

    public static boolean isSettingExistInSelfDB(Context mContext, String key) {
        boolean ret = false;
        if (key == null) {
            Log.i(TAG, "[PowerSavingUtils] isSettingExistInSelfDB key is null");
            return 0;
        }
        String selection = "name=?";
        String[] selectionArgs = new String[]{key};
        Uri content_uri = PowerSavingProvider.CONTENT_URI;
        if (key != null) {
            content_uri = PowerSavingProvider.getUriFor(key);
        }
        Cursor cursor = mContext.getContentResolver().query(content_uri, null, selection, selectionArgs, null);
        if (cursor != null) {
            ret = cursor.moveToFirst();
            cursor.close();
        } else {
            Log.i(TAG, "[PowerSavingUtils] isDataExistSelfDB cursor is null, key is " + key);
        }
        return ret;
    }

    public static void setStoreMethod(Context mContext, String value) {
        Editor editor = mContext.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putString(PARM.STORE_SETTINGS_METHOD, value);
        editor.commit();
    }

    public static String getStoreMethod(Context mContext) {
        return mContext.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getString(PARM.STORE_SETTINGS_METHOD, "");
    }

    public static void setBooleanItemToSelfDB(Context mContext, String key, boolean value) {
        ContentValues values = new ContentValues();
        values.put(SettingsColumns.NAME, key);
        values.put(SettingsColumns.VALUE, value ? "1" : SYMBOLS.ZERO);
        String selection = "name=?";
        String[] selectionArgs = new String[]{key};
        Uri content_uri = PowerSavingProvider.CONTENT_URI;
        if (key != null) {
            content_uri = PowerSavingProvider.getUriFor(key);
        }
        if (mContext.getContentResolver().update(content_uri, values, selection, selectionArgs) == 0) {
            Log.i(TAG, "[PowerSavingUtils] setBooleanItemToDB uri: " + mContext.getContentResolver().insert(PowerSavingProvider.CONTENT_URI, values).toString());
        }
    }

    public static boolean getBooleanItemFromSelfDB(Context mContext, String key) {
        String selection = "name=?";
        String[] selectionArgs = new String[]{key};
        boolean ret = false;
        Uri content_uri = PowerSavingProvider.CONTENT_URI;
        if (key != null) {
            content_uri = PowerSavingProvider.getUriFor(key);
        }
        Cursor cursor = mContext.getContentResolver().query(content_uri, null, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(SettingsColumns.VALUE);
                if (index >= 0 && "1".equals(cursor.getString(index))) {
                    ret = true;
                }
            }
            cursor.close();
        } else {
            Log.i(TAG, "[PowerSavingUtils] getBooleanItemFromoSelfDB cursor is null, key is " + key);
        }
        return ret;
    }

    public static void setStringItemToSelfDB(Context mContext, String key, String value) {
        ContentValues values = new ContentValues();
        values.put(SettingsColumns.NAME, key);
        values.put(SettingsColumns.VALUE, value);
        String selection = "name=?";
        String[] selectionArgs = new String[]{key};
        Uri content_uri = PowerSavingProvider.CONTENT_URI;
        if (key != null) {
            content_uri = PowerSavingProvider.getUriFor(key);
        }
        int update_count = 0;
        try {
            update_count = mContext.getContentResolver().update(content_uri, values, selection, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (update_count == 0) {
            try {
                Log.i(TAG, "[PowerSavingUtils] setStringItemToSelfDB uri: " + mContext.getContentResolver().insert(PowerSavingProvider.CONTENT_URI, values).toString());
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public static String getStringItemFromSelfDB(Context mContext, String key) {
        String selection = "name=?";
        String[] selectionArgs = new String[]{key};
        String ret = null;
        Uri content_uri = PowerSavingProvider.CONTENT_URI;
        if (key != null) {
            content_uri = PowerSavingProvider.getUriFor(key);
        }
        Cursor cursor = mContext.getContentResolver().query(content_uri, null, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(SettingsColumns.VALUE);
                if (index >= 0) {
                    ret = cursor.getString(index);
                }
            }
            cursor.close();
        } else {
            Log.i(TAG, "[PowerSavingUtils] getStringItemFromSelfDB cursor is null, key is " + key);
        }
        return ret;
    }

    public static boolean IsUseNewMethodToStoreSettings(Context mContext) {
        if (PARM.STORE_IN_SELF_DB.equals(getStoreMethod(mContext))) {
            return true;
        }
        boolean ret = false;
        String mStoreMethod = ProjectInfo.getConfigFromExternal(mContext, PARM.STORE_SETTINGS_METHOD);
        if (mStoreMethod == null || !mStoreMethod.equals(PARM.STORE_IN_SELF_DB)) {
            mStoreMethod = ProjectInfo.getConfigString(mContext, C0321R.string.powersaving_store_settings_method);
            if (mStoreMethod != null && mStoreMethod.equals(PARM.STORE_IN_SELF_DB)) {
                ret = System.getString(mContext.getContentResolver(), PSDB.SCREEN_POLICY) == null;
            }
        } else {
            ret = System.getString(mContext.getContentResolver(), PSDB.SCREEN_POLICY) == null;
        }
        if (ret) {
            setStoreMethod(mContext, PARM.STORE_IN_SELF_DB);
            return ret;
        }
        setStoreMethod(mContext, PARM.STORE_IN_SETTINGS_DB);
        return ret;
    }

    public static void SetExceptionNotice(Context ctx, boolean value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putBoolean(PSSPREF.IS_SHOW_EXCEPTION_DIALOG, value);
        editor.commit();
    }

    public static boolean GetRExceptionNoticeStatus(Context ctx) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getBoolean(PSSPREF.IS_SHOW_EXCEPTION_DIALOG, false);
    }

    public static void SetCheckDataBaseStatus(Context ctx, boolean value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putBoolean(PSSPREF.IS_CHECK_DISAUTO_WAKEUP_DATABASE, value);
        editor.commit();
    }

    public static boolean GetCheckDataBaseStatus(Context ctx) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getBoolean(PSSPREF.IS_CHECK_DISAUTO_WAKEUP_DATABASE, false);
    }

    public static String GetPreferencesStatusString(Context ctx, String prefKey) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getString(prefKey, null);
    }

    public static void SetPreferencesStatus(Context ctx, String prefKey, String value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putString(prefKey, value);
        editor.commit();
    }

    public static void removePreferneceStatus(Context ctx, String prefKey) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.remove(prefKey);
        editor.commit();
    }

    public static boolean GetPreferencesStatus(Context ctx, String prefKey) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getBoolean(prefKey, false);
    }

    public static void SetPreferencesStatus(Context ctx, String prefKey, boolean value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putBoolean(prefKey, value);
        editor.commit();
    }

    public static int GetPreferencesStatusInt(Context ctx, String prefKey) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getInt(prefKey, -1);
    }

    public static void SetPreferencesStatus(Context ctx, String prefKey, int value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putInt(prefKey, value);
        editor.commit();
    }

    public static long GetPreferencesStatusLong(Context ctx, String prefKey) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getLong(prefKey, -1);
    }

    public static void SetPreferencesStatus(Context ctx, String prefKey, long value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putLong(prefKey, value);
        editor.commit();
    }

    public static boolean contaionPreferences(Context ctx, String prefKey) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).contains(prefKey);
    }

    public static void SetBatteryLevel(Context ctx, int value) {
        Editor editor = ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        editor.putInt(PSSPREF.BATTERY_LEVEL, value);
        editor.commit();
    }

    public static int GetBatteryLevel(Context ctx) {
        return ctx.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getInt(PSSPREF.BATTERY_LEVEL, 100);
    }

    public static void addToDisAutoStart(Context ctx, String pck) {
        BackDataDb db = new BackDataDb(ctx);
        db.addToDisAutoStart(pck);
        db.close();
    }

    public static void saveToDisAutoStartDb(Context context, List<String> packageList) {
        BackDataDb db = new BackDataDb(context);
        db.addToDisAutoStart((List) packageList);
        db.close();
    }

    public static void deleteFromDisAutoStartDb(Context context, String packageName) {
        BackDataDb db = new BackDataDb(context);
        db.deleteFromDisAutoStart(packageName);
        db.close();
    }

    public static void deleteFromDisAutoStartDb(Context context, List<String> pkgList) {
        BackDataDb db = new BackDataDb(context);
        db.deleteFromDisAutoStart((List) pkgList);
        db.close();
    }

    public static void setForbidStatu(Context context, long id, boolean isForbidden) {
        BackDataDb db = new BackDataDb(context);
        db.setForbidStatu(id, isForbidden);
        db.close();
    }

    public static void addAppToWhiteList(Context context, String pkgName) {
        BackDataDb db = new BackDataDb(context);
        db.addAppToWhiteList(pkgName);
        db.close();
    }

    public static void addAppToWhiteList(Context context, List<String> pkgList) {
        BackDataDb db = new BackDataDb(context);
        db.addAppToWhiteList((List) pkgList);
        db.close();
    }

    public static void removeAppFromWhiteList(Context context, String pkgName) {
        BackDataDb db = new BackDataDb(context);
        db.removeAppFromWhiteList(pkgName);
        db.close();
    }

    public static void removeAppFromWhiteList(Context context, List<String> pkgList) {
        BackDataDb db = new BackDataDb(context);
        db.removeAppFromWhiteList((List) pkgList);
        db.close();
    }

    public static List<WakePathInfo> getWakeList(Context context) {
        BackDataDb db = new BackDataDb(context);
        List<WakePathInfo> wakeList = db.queryWakeUpInfo();
        db.close();
        return wakeList;
    }

    public static List<String> getBlackList(Context context) {
        BackDataDb db = new BackDataDb(context);
        List<String> blackList = db.getAllDisAutoStartPkg();
        db.close();
        return blackList;
    }

    public static String getDefaultLauncher(PackageManager pm) {
        ComponentName currentDefaultHome = pm.getHomeActivities(new ArrayList());
        if (currentDefaultHome == null) {
            return "";
        }
        String defaultHome = currentDefaultHome.getPackageName();
        Log.d(TAG, "getDefaultLauncher: " + defaultHome);
        return defaultHome;
    }

    private static AtomicFile getSavedStateFile(int userId, String fileName) {
        return new AtomicFile(new File(Environment.getUserSystemDirectory(userId), fileName));
    }

    public static List<String> getWidgetPackageName(String launcher) {
        String hostTag;
        List<String> pTagList;
        String widgetPkg;
        List<Map<String, String>> providerList = new ArrayList();
        List<Map<String, String>> hostList = new ArrayList();
        List<Map<String, String>> groupList = new ArrayList();
        List<String> pkgList = new ArrayList();
        try {
            AtomicFile file = getSavedStateFile(UserHandle.myUserId(), WIDGET_STATE_FILENAME);
            if (file != null) {
                InputStream stream = file.openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, null);
                while (parser != null) {
                    int type = parser.next();
                    if (type == 2) {
                        String tag = parser.getName();
                        String pkg;
                        String tagAttribute;
                        if ("p".equals(tag)) {
                            pkg = parser.getAttributeValue(null, "pkg");
                            String cl = parser.getAttributeValue(null, "cl");
                            tagAttribute = parser.getAttributeValue(null, SaveData.TAG);
                            Map<String, String> provider = new HashMap();
                            provider.put(tagAttribute, pkg);
                            providerList.add(provider);
                        } else if ("h".equals(tag)) {
                            pkg = parser.getAttributeValue(null, "pkg");
                            tagAttribute = parser.getAttributeValue(null, SaveData.TAG);
                            Map<String, String> host = new HashMap();
                            host.put(pkg, tagAttribute);
                            hostList.add(host);
                        } else if ("g".equals(tag)) {
                            hostTag = parser.getAttributeValue(null, "h");
                            String providerString = parser.getAttributeValue(null, "p");
                            Map<String, String> group = new HashMap();
                            group.put(hostTag, providerString);
                            groupList.add(group);
                        }
                    }
                    if (type == 1) {
                        break;
                    }
                }
                hostTag = "";
                for (Map<String, String> hMap : hostList) {
                    if (hMap.containsKey(launcher)) {
                        hostTag = (String) hMap.get(launcher);
                        break;
                    }
                }
                pTagList = new ArrayList();
                for (Map<String, String> gMap : groupList) {
                    if (gMap.containsKey(hostTag)) {
                        pTagList.add(gMap.get(hostTag));
                    }
                }
                for (Map<String, String> pMap : providerList) {
                    for (String pTag : pTagList) {
                        if (pMap.containsKey(pTag)) {
                            widgetPkg = (String) pMap.get(pTag);
                            if (!pkgList.contains(widgetPkg)) {
                                pkgList.add(widgetPkg);
                                Log.d(TAG, "widget pkg----" + widgetPkg);
                            }
                        }
                    }
                }
                return pkgList;
            }
            Log.d(TAG, "file == null ");
            return pkgList;
        } catch (Exception e) {
            e = e;
        } catch (Exception e2) {
            e = e2;
        } catch (Exception e22) {
            e = e22;
        } catch (Exception e222) {
            e = e222;
        } catch (Exception e2222) {
            e = e2222;
        }
        Exception e3;
        Log.d(TAG, "failed parsing " + e3);
        hostTag = "";
        for (Map<String, String> hMap2 : hostList) {
            if (hMap2.containsKey(launcher)) {
                hostTag = (String) hMap2.get(launcher);
                break;
            }
        }
        pTagList = new ArrayList();
        for (Map<String, String> gMap2 : groupList) {
            if (gMap2.containsKey(hostTag)) {
                pTagList.add(gMap2.get(hostTag));
            }
        }
        for (Map<String, String> pMap2 : providerList) {
            for (String pTag2 : pTagList) {
                if (pMap2.containsKey(pTag2)) {
                    widgetPkg = (String) pMap2.get(pTag2);
                    if (!pkgList.contains(widgetPkg)) {
                        pkgList.add(widgetPkg);
                        Log.d(TAG, "widget pkg----" + widgetPkg);
                    }
                }
            }
        }
        return pkgList;
    }

    public static List<String> getLauncherApList(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(0);
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService("activity");
        List<String> apps = new ArrayList();
        List<String> system_app_list;
        if (BackgroundPolicyExecutor.getInstance(context).isCNModel()) {
            system_app_list = new ArrayList();
        } else {
            system_app_list = PSUtils.getSystemApps(context);
        }
        for (ApplicationInfo packageInfo : packages) {
            if (!(pm.getLaunchIntentForPackage(packageInfo.packageName) == null || system_app_list.contains(packageInfo.packageName))) {
                apps.add(packageInfo.packageName);
            }
        }
        return apps;
    }

    public static List<String> getAllApList(Context context) {
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(0);
        List<String> apps = new ArrayList();
        for (ApplicationInfo packageInfo : packages) {
            apps.add(packageInfo.packageName);
        }
        return apps;
    }

    public static boolean isLauncherAP(Context context, String pkg) {
        boolean ans;
        if (context.getPackageManager().getLaunchIntentForPackage(pkg) != null) {
            ans = true;
        } else {
            ans = false;
        }
        Log.i(TAG, "[PowerSavingUtils] isLauncherAP = " + pkg + "," + ans);
        return ans;
    }

    public static boolean isAppHideOnBamUi(Context context, String pkg) {
        List<String> system_app_list;
        if (BackgroundPolicyExecutor.getInstance(context).isCNModel()) {
            system_app_list = BackgroundPolicyExecutor.getInstance(context).getWhiteListApp(32);
        } else {
            system_app_list = PSUtils.getSystemApps(context);
        }
        if (!system_app_list.contains(pkg) && isLauncherAP(context, pkg)) {
            return false;
        }
        return true;
    }

    public static String getSettingsProvider(Context context, String property) {
        String name = "";
        name = Global.getString(context.getContentResolver(), property);
        if (name == null) {
            return null;
        }
        if (name == null || !name.equalsIgnoreCase("")) {
            Log.i("[PowerSavingAppG3]", "getSettingsProvider : " + name);
            return name;
        }
        Log.i("[PowerSavingAppG3]", "no  getSettingsProvider ");
        return null;
    }

    public static void setSettingsProvider(Context context, String property, String name) {
        ContentResolver resolver = context.getContentResolver();
        Log.i("[PowerSavingAppG3]", "setSettingsProvider property " + property + ",name : " + name);
        if (name != null && !name.equalsIgnoreCase("")) {
            Log.i("[PowerSavingAppG3]", "setSettingsProvider success " + Global.putString(resolver, property, name));
        }
    }

    public static boolean isCharging(Context context) {
        int plugged = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra("plugged", -1);
        if (plugged == 1 || plugged == 2) {
            return true;
        }
        return false;
    }

    public static void addForceStoppedApp(Context context, String pkgName) {
        BackDataDb db = new BackDataDb(context);
        db.insertProcessWasForceStopped(pkgName);
        db.close();
    }

    public static void insertStopSyncAdapterInfo(Context context, String setStop, String syncAdapterTypeInfo) {
        BackDataDb db = new BackDataDb(context);
        db.insertStopSyncAdapterInfo(setStop, syncAdapterTypeInfo);
        db.close();
    }

    public static boolean getProcessMonitorEnableFromDB(Context ctx) {
        if (IsUseNewMethodToStoreSettings(ctx)) {
            return "1".equals(getStringItemFromDB(ctx, PROCESS_MONITOR.SWITCH_NAME));
        }
        if (System.getInt(ctx.getContentResolver(), PROCESS_MONITOR.SWITCH_NAME, 0) != 0) {
            return true;
        }
        return false;
    }

    private static void setProcessMonitorServiceEnableToDB(Context ctx, boolean enable) {
        String key = PROCESS_MONITOR.SWITCH_NAME;
        String strEnable = "1";
        String strDisable = SYMBOLS.ZERO;
        String value = "";
        if (enable) {
            value = strEnable;
        } else {
            value = strDisable;
        }
        setStringItemToDB(ctx, key, value);
    }

    public static boolean canStartProcessMonitorService(Context context) {
        try {
            Class test = Class.forName("android.app.IFihProcessListener$Stub");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void setProcessMonitorServiceEnable(Context context, boolean enable) {
        boolean bcanStartProcessMonitorService = canStartProcessMonitorService(context);
        Log.d(TAG, "canStartProcessMonitorService : " + bcanStartProcessMonitorService);
        if (bcanStartProcessMonitorService) {
            if (enable) {
                context.startService(new Intent(context, ProcessMonitorService.class));
                Log.d(TAG, "start ProcessMonitorService");
            } else {
                context.stopService(new Intent(context, ProcessMonitorService.class));
                Log.d(TAG, "stop ProcessMonitorService");
            }
        }
        setProcessMonitorServiceEnableToDB(context, enable);
    }

    public static List<String> getAlarmRecordsInDoze(Context context) {
        List<String> ret = new ArrayList();
        List<String> alarmRecordWhiteList = new ArrayList();
        alarmRecordWhiteList.addAll(Arrays.asList(context.getResources().getStringArray(C0321R.array.alarm_record_white_list)));
        alarmRecordWhiteList.add(context.getApplicationContext().getPackageName());
        Cursor cursor = context.getContentResolver().query(Uri.parse(AlarmRecord.ALARM_IN_DOZE_URI), new String[]{"pkg_name"}, "tag= ? and (wakeup_alarms > ? or non_wakeup_alarms > ?)", new String[]{AlarmRecord.ALARM_DIFF, String.valueOf(0), String.valueOf(0)}, null);
        if (cursor != null) {
            try {
                int pkgIndex = cursor.getColumnIndex("pkg_name");
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(pkgIndex);
                    if (!alarmRecordWhiteList.contains(pkgName)) {
                        Log.i(TAG, "getAlarmRecordsInLightDoze add pkgName = " + pkgName);
                        if (!ret.contains(pkgName)) {
                            ret.add(pkgName);
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        }
        if (ret.contains(PACKAGE_NAME.SYSTEM_UI) && isDozePusleAOD(context)) {
            Log.d(TAG, "DozePusleAOD" + isDozePusleAOD(context) + ",remove " + PACKAGE_NAME.SYSTEM_UI);
            ret.remove(PACKAGE_NAME.SYSTEM_UI);
        }
        return ret;
    }

    public static void clearAlarmRecordsInDoze(Context context) {
        try {
            context.getContentResolver().delete(Uri.parse(AlarmRecord.ALARM_IN_DOZE_URI), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enableNotificationListener(Context context) {
        Log.i(TAG, "enableNotificationListener()");
        String notificationListenerList = Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(notificationListenerList)) {
            String[] names = notificationListenerList.split(":");
            boolean bFound = false;
            for (String unflattenFromString : names) {
                ComponentName cn = ComponentName.unflattenFromString(unflattenFromString);
                if (cn != null && TextUtils.equals("com.evenwell.powersaving.g3", cn.getPackageName())) {
                    bFound = true;
                }
            }
            if (!bFound) {
                notificationListenerList = notificationListenerList + ":" + "com.evenwell.powersaving.g3" + InternalZipConstants.ZIP_FILE_SEPARATOR + PowerSavingNotificationListenerService.class.getName();
                Log.i(TAG, "notificationListenerList = " + notificationListenerList);
                Secure.putString(context.getContentResolver(), "enabled_notification_listeners", notificationListenerList);
            }
        }
    }

    public static void disableNotificationListener(Context context) {
        Log.i(TAG, "disableNotificationListener()");
        String notificationListenerList = Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(notificationListenerList)) {
            String psListenerComponent = ":com.evenwell.powersaving.g3/" + PowerSavingNotificationListenerService.class.getName();
            if (notificationListenerList.indexOf(psListenerComponent) != -1) {
                Log.i(TAG, "notificationListenerList = " + notificationListenerList);
                String newNotificationListenerList = notificationListenerList.replaceAll(psListenerComponent, "");
                Log.i(TAG, "newNotificationListenerList = " + newNotificationListenerList);
                Secure.putString(context.getContentResolver(), "enabled_notification_listeners", newNotificationListenerList);
            }
        }
    }

    public static void insertTimeStampToRestartServiceTable(Context context, String ServiceName) {
        BackDataDb db = new BackDataDb(context);
        db.insertTimeStampToServiceRestartTable(ServiceName);
        db.close();
    }

    public static boolean isDozePusleAOD(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        if (System.getInt(context.getContentResolver(), "glance_aod_pulsing_enabled", 0) != 1) {
            z = false;
        }
        return z;
    }

    public static boolean hasMethod(Class<?> klass, String methodName, Class<?>... paramTypes) {
        try {
            klass.getDeclaredMethod(methodName, paramTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean isDBGcfgtoolEnabled() {
        try {
            String dbg = SystemProperties.get("ro.config.dbgcfgtool_config", "");
            if (dbg == null || dbg.equals("")) {
                return false;
            }
            int config = Integer.valueOf(dbg).intValue();
            if (config == 0 || config == 1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isLogConfigExist() {
        return new File(LOGCONFIG_PATH).exists();
    }

    public static void startProcessMonitorServiceWithAction(Context context, String action) {
        boolean bcanStartProcessMonitorService = canStartProcessMonitorService(context);
        Log.d(TAG, "canStartProcessMonitorService : " + bcanStartProcessMonitorService);
        if (bcanStartProcessMonitorService) {
            Intent intent = new Intent(context, ProcessMonitorService.class);
            intent.setAction(action);
            context.startService(intent);
            Log.d(TAG, "start ProcessMonitorService");
        }
        setProcessMonitorServiceEnableToDB(context, true);
    }

    public static boolean isDBGToolStartToWriteLog(String path) {
        try {
            FileInputStream stream = new FileInputStream(path);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            while (parser != null) {
                int type = parser.next();
                if (type == 2) {
                    if ("LoggerConfig".equals(parser.getName())) {
                        if (FUNCTION.EXTRA.POWERSAVER_ENABLE.equals(parser.getAttributeValue(null, SettingsColumns.NAME))) {
                            if ("1".equals(parser.nextText())) {
                                return true;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                if (type == 1) {
                    break;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "failed parsing " + e);
        }
        return false;
    }
}
