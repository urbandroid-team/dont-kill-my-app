package com.evenwell.powersaving.g3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.evenwell.powersaving.g3.lpm.LowPowerMode;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.ss.SsUtils;
import com.evenwell.powersaving.g3.utils.PSConst.Function;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_KEY;
import com.evenwell.powersaving.g3.utils.PSConst.PERMISSION.REQUEST_CODE;
import com.evenwell.powersaving.g3.utils.PSConst.PERMISSION.TYPE;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;

public class AlertActivity extends Activity {
    private static final boolean DBG = true;
    private static final int INIT_FINISH = 2001;
    private static String TAG = TAG.PSLOG;
    private static Context mContext;
    private int function = 0;
    private boolean isChecked = false;
    private int triggerFrom = -1;
    private int type = 0;

    /* renamed from: com.evenwell.powersaving.g3.AlertActivity$2 */
    class C03052 implements OnClickListener {
        C03052() {
        }

        public void onClick(DialogInterface dialog, int which) {
            Log.i(AlertActivity.TAG, "[AlertActivity]: close permission dialog.");
            AlertActivity.this.finish();
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.i(TAG, "[AlertActivity]: onCreate()");
        mContext = this;
        Intent intent = getIntent();
        if (intent != null) {
            this.type = intent.getIntExtra(EXTRA_KEY.TYPE, -1);
            this.function = intent.getIntExtra("function", -1);
            this.isChecked = intent.getBooleanExtra("isChecked", false);
            this.triggerFrom = intent.getIntExtra("triggerFrom", -1);
        }
        Log.i(TAG, "[AlertActivity] type: " + this.type + ", function: " + this.function + ", isChecked: " + this.isChecked + ", triggerFrom: " + this.triggerFrom);
        ContextThemeWrapper themedContext = new ContextThemeWrapper(mContext, 16974143);
        if (this.type == TYPE.SYSTEM_ALERT_WINDOW) {
            ShowPermissionDialog(themedContext, this.type, this.function);
        } else if (this.type == TYPE.WRITE_SETTINGS) {
            ShowPermissionDialog(themedContext, this.type, this.function);
        } else {
            Log.i(TAG, "[AlertActivity]: type is not match, close activity.");
            finish();
        }
    }

    public void onResume() {
        super.onResume();
        Log.i(TAG, "[AlertActivity]: onResume()");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "[AlertActivity]: onDestroy()");
    }

    public void onStop() {
        super.onStop();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean z = true;
        Log.i(TAG, "[AlertActivity]: onActivityResult() requestCode: " + requestCode + " , resultCode: " + resultCode);
        if (requestCode == REQUEST_CODE.SYSTEM_ALERT_WINDOW_PERMISSION_REQ_CODE) {
            Log.i(TAG, "[AlertActivity] onActivityResult() allow system alert permission: " + Settings.canDrawOverlays(mContext));
            if (Settings.canDrawOverlays(this)) {
                Log.i(TAG, "[AlertActivity]: SYSTEM_ALERT_WINDOW permission granted");
                if (this.function == Function.DC) {
                    PowerSavingUtils.SetDataConnectionEnable(mContext, true);
                } else if (this.function == Function.PW) {
                    PowerSavingUtils.SetPWEnable(mContext, true);
                }
            } else {
                Log.i(TAG, "[AlertActivity]: SYSTEM_ALERT_WINDOW permission not granted");
            }
            finish();
        } else if (requestCode == REQUEST_CODE.WRITE_SETTINGS_PERMISSION_REQ_CODE) {
            Log.i(TAG, "[AlertActivity] onActivityResult() allow modify settings permission: " + System.canWrite(mContext));
            if (System.canWrite(mContext)) {
                Log.i(TAG, "[AlertActivity]: WRITE_SETTINGS permission granted, triggerFrom: " + this.triggerFrom);
                PowerSavingUtils.CancelNotification(mContext, 2002);
                if (this.triggerFrom == 8 || this.triggerFrom == 4) {
                    PowerSavingUtils.SetLPMEnable(mContext, this.isChecked);
                } else if (this.triggerFrom == 9 || this.triggerFrom == 5) {
                    PowerSavingUtils.SetSSEnable(mContext, this.isChecked);
                } else if (this.triggerFrom == 11) {
                    isEnable = PowerSavingUtils.GetSSEnable(mContext);
                    Log.i(TAG, "[AlertActivity]: Smart Switch enable: " + isEnable);
                    if (isEnable) {
                        Log.i(TAG, "[AlertActivity]: Smart Switch, turn off Wi-Fi hotspot.");
                        SsUtils.setWifiApEnabled(mContext, false);
                    }
                } else if (this.triggerFrom == 0) {
                    isEnable = PowerSavingUtils.GetLPMEnable(mContext);
                    Log.i(TAG, "[AlertActivity]: Low power mode enable: " + isEnable);
                    if (isEnable) {
                        mContext.sendBroadcast(new Intent(ACTION.ACTION_LPM_RECHECK_BATTERY_STATUS));
                    }
                } else if (this.triggerFrom == 12) {
                    LpmUtils.RestoreSettingsToPhone(mContext);
                } else if (this.triggerFrom == 13) {
                    String mChange = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.CHANGE);
                    PowerSavingUtils.setStringItemToDB(mContext, LPMDB.BEGIN, mChange);
                    LowPowerMode.mThreshold = Integer.parseInt(mChange);
                    mContext.sendBroadcast(new Intent(ACTION.ACTION_LPM_UPDATE_SCHEDULE));
                }
            } else {
                Log.i(TAG, "[AlertActivity]: WRITE_SETTINGS permission not granted, triggerFrom: " + this.triggerFrom);
                Context context;
                if (this.triggerFrom == 8 || this.triggerFrom == 4) {
                    context = mContext;
                    if (this.isChecked) {
                        z = false;
                    }
                    PowerSavingUtils.SetLPMEnable(context, z);
                } else if (this.triggerFrom == 1) {
                    context = mContext;
                    if (this.isChecked) {
                        z = false;
                    }
                    PowerSavingUtils.SetPowerSavingModeEnable(context, z);
                } else if (this.triggerFrom == 9 || this.triggerFrom == 5) {
                    context = mContext;
                    if (this.isChecked) {
                        z = false;
                    }
                    PowerSavingUtils.SetSSEnable(context, z);
                } else if (!(this.triggerFrom == 11 || this.triggerFrom == 0 || this.triggerFrom == 12 || this.triggerFrom != 13)) {
                }
            }
            finish();
        }
    }

    private void ShowPermissionDialog(Context context, int type, int function) {
        Log.i(TAG, "[AlertActivity]: ShowDialog() type: " + type + " function: " + function);
        final int mType = type;
        int messageResId = C0321R.string.app_label;
        switch (function) {
            case Function.PW /*2010*/:
                messageResId = C0321R.string.fih_power_saving_permission_msg;
                break;
            case Function.DC /*2011*/:
                messageResId = C0321R.string.fih_power_saving_permission_msg;
                break;
            case Function.LPM /*2012*/:
                messageResId = C0321R.string.fih_power_saving_permission_msg;
                break;
            case Function.SS /*2013*/:
                messageResId = C0321R.string.fih_power_saving_permission_msg;
                break;
        }
        Builder builder = new Builder(context);
        builder.setTitle(C0321R.string.fih_power_saving_notice_dialog_title);
        builder.setMessage(context.getString(messageResId, new Object[]{context.getResources().getString(C0321R.string.app_label)}));
        builder.setPositiveButton(C0321R.string.fih_power_saving_permission_settings, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(AlertActivity.TAG, "[AlertActivity]: launch permission settings page.");
                AlertActivity.this.LaunchPermissionSettings(AlertActivity.mContext, mType);
            }
        });
        builder.setNegativeButton(C0321R.string.fih_power_saving_permission_cancel, new C03052());
        AlertDialog ad = builder.create();
        ad.setCanceledOnTouchOutside(false);
        ad.setCancelable(false);
        ad.show();
    }

    private void LaunchPermissionSettings(Context mContext, int type) {
        if (type == TYPE.SYSTEM_ALERT_WINDOW) {
            startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + mContext.getPackageName())), REQUEST_CODE.SYSTEM_ALERT_WINDOW_PERMISSION_REQ_CODE);
        } else if (type == TYPE.WRITE_SETTINGS) {
            startActivityForResult(new Intent("android.settings.action.MANAGE_WRITE_SETTINGS", Uri.parse("package:" + getPackageName())), REQUEST_CODE.WRITE_SETTINGS_PERMISSION_REQ_CODE);
        } else {
            finish();
        }
    }
}
