package com.evenwell.powersaving.g3.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.LATEST_EVENT_EXTRA;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.ServiceBinder;
import com.evenwell.powersaving.g3.utils.PSConst.Function;

public class PowerSaverRequestDialog extends Activity {
    public static final int DEFAULT_LEVEL = 15;
    public static final int MODE_EXTREME = 1;
    public static final int MODE_NORMAL = 0;
    public static final String POWER_SAVER_MODE_REQUEST = "power_saver_mode_request";
    public static final String POWER_SAVER_MODE_REQUEST_LEVEL = "power_saver_mode_request_level";
    private static String TAG = "PowerSaverRequestDialog";
    private final int LEGALTERM = Function.PW;
    private ServiceConnection conn = new C04211();
    private boolean mBound = false;
    private Context mContext;
    private AlertDialog mDialog;
    private PowerManager mPowerManager;
    private final BroadcastReceiver mReceiver = new C04255();
    private int mRequestMode;
    ServiceBinder mbinder = null;
    private ContextThemeWrapper themedContext;

    /* renamed from: com.evenwell.powersaving.g3.utils.PowerSaverRequestDialog$1 */
    class C04211 implements ServiceConnection {
        C04211() {
        }

        public void onServiceDisconnected(ComponentName name) {
            PowerSaverRequestDialog.this.mBound = false;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            PowerSaverRequestDialog.this.mbinder = (ServiceBinder) service;
            PowerSaverRequestDialog.this.mBound = true;
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.utils.PowerSaverRequestDialog$2 */
    class C04222 implements OnClickListener {
        C04222() {
        }

        public void onClick(DialogInterface dialog, int which) {
            if (PowerSaverRequestDialog.this.mBound) {
                if (PowerSaverRequestDialog.this.mRequestMode == 0) {
                    PowerSaverRequestDialog.this.mbinder.getService().applyNormalMode("apply by user");
                } else if (PowerSaverRequestDialog.this.mRequestMode == 1) {
                    PowerSaverRequestDialog.this.mbinder.getService().applyExtremeMode("apply by user");
                }
                PowerSaverRequestDialog.this.mbinder.getService().updateApplyEventStatusForOtherUI(LATEST_EVENT_EXTRA.LOW_POWER);
            }
            PowerSaverRequestDialog.this.finish();
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.utils.PowerSaverRequestDialog$3 */
    class C04233 implements OnClickListener {
        C04233() {
        }

        public void onClick(DialogInterface dialog, int which) {
            if (PowerSaverRequestDialog.this.mBound && PowerSaverRequestDialog.this.mRequestMode == 0 && PowerSaverRequestDialog.this.mbinder.getService().getCurentMode() == 1) {
                Log.i(PowerSaverRequestDialog.TAG, "previous mode = Extreme");
                PowerSaverRequestDialog.this.mbinder.getService().applyInAactiveMode();
            }
            PowerSaverRequestDialog.this.finish();
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.utils.PowerSaverRequestDialog$4 */
    class C04244 implements OnKeyListener {
        C04244() {
        }

        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
            if (keyCode == 4) {
                PowerSaverRequestDialog.this.finish();
            }
            return true;
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.utils.PowerSaverRequestDialog$5 */
    class C04255 extends BroadcastReceiver {
        C04255() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                PowerSaverRequestDialog.this.mContext = context;
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    int mPlugged = intent.getIntExtra("plugged", -1);
                    int mStatus = intent.getIntExtra("status", -1);
                    if (mPlugged != 1 && mPlugged != 2 && mPlugged != 4) {
                        return;
                    }
                    if (mStatus == 2 || mStatus == 5) {
                        PowerSaverRequestDialog.this.finish();
                    }
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.themedContext = new ContextThemeWrapper(this, 16974130);
        this.mContext = this;
        setContentView(C0321R.layout.handler_layout);
        this.mPowerManager = (PowerManager) getSystemService("power");
        this.mRequestMode = getIntent().getIntExtra(POWER_SAVER_MODE_REQUEST, 0);
        showReqDialog(getIntent().getIntExtra(POWER_SAVER_MODE_REQUEST_LEVEL, 15));
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onStop() {
        super.onStop();
        if (this.mPowerManager.isInteractive()) {
            releaseResource();
        }
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        getWindow().setFlags(67108864, 67108864);
        bindService(new Intent(this, PowerSavingController.class), this.conn, 1);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        registerReceiver(this.mReceiver, filter);
    }

    public void onBackPressed() {
        finish();
    }

    private void showReqDialog(int level) {
        Builder builder = new Builder(this.themedContext);
        String title = getString(C0321R.string.fih_power_saving_request_dialog_title_2);
        String normal_msg = String.format(getString(C0321R.string.fih_power_saving_request_dialog_msg_2), new Object[]{Integer.valueOf(level)});
        String extreme_msg = String.format(getString(C0321R.string.fih_power_saving_request_dialog_msg_2), new Object[]{Integer.valueOf(level)});
        builder.setTitle(title);
        if (this.mRequestMode == 0) {
            builder.setMessage(normal_msg);
        } else {
            builder.setMessage(extreme_msg);
        }
        builder.setPositiveButton(C0321R.string.fih_power_saving_request_dialog_enable, new C04222());
        builder.setNegativeButton(C0321R.string.fih_power_saving_request_dialog_cancel, new C04233());
        this.mDialog = builder.create();
        this.mDialog.setCanceledOnTouchOutside(false);
        this.mDialog.show();
        this.mDialog.setOnKeyListener(new C04244());
    }

    private void releaseResource() {
        try {
            if (this.mBound) {
                unbindService(this.conn);
            }
            try {
                if (this.mReceiver != null) {
                    unregisterReceiver(this.mReceiver);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (this.mDialog != null) {
                this.mDialog.dismiss();
                this.mDialog = null;
                finish();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
