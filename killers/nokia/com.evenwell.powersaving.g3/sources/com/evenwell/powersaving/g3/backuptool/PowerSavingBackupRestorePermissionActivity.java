package com.evenwell.powersaving.g3.backuptool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_KEY;
import com.evenwell.powersaving.g3.utils.PSConst.PERMISSION.REQUEST_CODE;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.fihtdc.push_system.lib.common.PushMessageContract;

public class PowerSavingBackupRestorePermissionActivity extends Activity {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;
    private static Context mContext;
    private int type = 0;

    /* renamed from: com.evenwell.powersaving.g3.backuptool.PowerSavingBackupRestorePermissionActivity$2 */
    class C03372 implements OnClickListener {
        C03372() {
        }

        public void onClick(DialogInterface dialog, int which) {
            Log.i(PowerSavingBackupRestorePermissionActivity.TAG, "[PowerSavingBackupRestorePermissionActivity]: close permission dialog.");
            PowerSavingBackupRestorePermissionActivity.this.finish();
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.backuptool.PowerSavingBackupRestorePermissionActivity$4 */
    class C03394 implements OnKeyListener {
        C03394() {
        }

        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == 4) {
            }
            return true;
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.i(TAG, "[PowerSavingBackupRestorePermissionActivity]: onCreate()");
        mContext = this;
        Intent intent = getIntent();
        if (intent != null) {
            this.type = intent.getIntExtra(EXTRA_KEY.TYPE, -1);
        }
        Log.i(TAG, "[PowerSavingBackupRestorePermissionActivity] type: " + this.type);
        if (this.type != 3003 && this.type != 3002) {
            Log.i(TAG, "[PowerSavingBackupRestorePermissionActivity]: type is not match, close activity.");
            finish();
        } else if (PowerSavingBackRestoreUtils.getPermissionFirstflag(this)) {
            buildPermissionsRequest(mContext);
        } else {
            ContextThemeWrapper themedContext = new ContextThemeWrapper(mContext, 16974143);
            if (PowerSavingBackRestoreUtils.getPermissionDialogflag(this)) {
                ShowPermissionDialog(themedContext, this.type);
            } else {
                handlePermissionsDeny(themedContext);
            }
        }
    }

    public void onResume() {
        super.onResume();
        Log.i(TAG, "[PowerSavingBackupRestorePermissionActivity]: onResume()");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "[PowerSavingBackupRestorePermissionActivity]: onDestroy()");
        synchronized (PowerSavingBackupRestoreService.mBackupLock) {
            PowerSavingBackupRestoreService.openDialog = false;
            PowerSavingBackupRestoreService.mBackupLock.notify();
        }
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

    private void ShowPermissionDialog(Context context, int type) {
        Log.i(TAG, "[PowerSavingBackupRestorePermissionActivity]: ShowDialog() type: " + type);
        final int mType = type;
        Builder builder = new Builder(context);
        builder.setTitle(C0321R.string.fih_power_saving_notice_dialog_title);
        builder.setMessage(context.getString(C0321R.string.fih_power_saving_permission_msg, new Object[]{context.getResources().getString(C0321R.string.app_label)}));
        builder.setPositiveButton(C0321R.string.fih_power_saving_permission_settings, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(PowerSavingBackupRestorePermissionActivity.TAG, "[PowerSavingBackupRestorePermissionActivity]: launch permission settings page.");
                PowerSavingBackupRestorePermissionActivity.this.LaunchPermissionSettings(PowerSavingBackupRestorePermissionActivity.mContext, mType);
                PowerSavingBackupRestorePermissionActivity.this.finish();
            }
        });
        builder.setNegativeButton(C0321R.string.fih_power_saving_permission_cancel, new C03372());
        AlertDialog ad = builder.create();
        ad.setCanceledOnTouchOutside(false);
        ad.setCancelable(false);
        ad.show();
    }

    private void LaunchPermissionSettings(Context mContext, int type) {
        if (type == 3003 || type == 3002) {
            Intent it = new Intent();
            it.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            it.setData(Uri.fromParts(PushMessageContract.MESSAGE_KEY_PACKAGE_NAME, getPackageName(), null));
            startActivity(it);
            return;
        }
        finish();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "[PowerSavingBackupRestorePermissionActivity]: onRequestPermissionsResult()");
        switch (requestCode) {
            case REQUEST_CODE.READ_EXTERNAL_STORAGE_PERMISSION_REQ_CODE /*3102*/:
            case REQUEST_CODE.WRITE_EXTERNAL_STORAGE_PERMISSION_REQ_CODE /*3103*/:
                PowerSavingBackRestoreUtils.setPermissionFirstflag(this, false);
                if (grantResults.length > 0 && grantResults[0] == 0) {
                    PowerSavingBackRestoreUtils.setPermissionDialogflag(this, false);
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                    PowerSavingBackRestoreUtils.setPermissionDialogflag(this, false);
                } else {
                    PowerSavingBackRestoreUtils.setPermissionDialogflag(this, true);
                }
                finish();
                return;
            default:
                return;
        }
    }

    private void handlePermissionsDeny(final Context mContext) {
        new Builder(mContext).setTitle(getResources().getString(C0321R.string.app_label)).setMessage(getResources().getString(C0321R.string.fih_power_saving_permission_first_time_or_always_deny)).setCancelable(false).setOnKeyListener(new C03394()).setPositiveButton(getResources().getString(C0321R.string.fih_power_saving_dialog_ok), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                PowerSavingBackupRestorePermissionActivity.this.buildPermissionsRequest(mContext);
            }
        }).show();
    }

    private void buildPermissionsRequest(Context mContext) {
        Log.i(TAG, "[PowerSavingBackupRestorePermissionActivity]: buildPermissionsRequest()");
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, REQUEST_CODE.WRITE_EXTERNAL_STORAGE_PERMISSION_REQ_CODE);
    }
}
