package com.evenwell.powersaving.g3.powersaver;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.IStateChangeListener;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.LATEST_EVENT_EXTRA;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.ServiceBinder;

public class PowerSavingTileService extends TileService {
    private static final boolean DBG = true;
    private static final String TAG = "PowerSavingTileService";
    private ServiceConnection conn = new C03911();
    private boolean enabled = false;
    private IStateChangeListener listener = new C03932();
    private boolean mBound = false;
    private Handler mHandler;
    private int mLowPowerMode = -1;
    private ServiceBinder mbinder = null;

    /* renamed from: com.evenwell.powersaving.g3.powersaver.PowerSavingTileService$1 */
    class C03911 implements ServiceConnection {
        C03911() {
        }

        public void onServiceDisconnected(ComponentName name) {
            PowerSavingTileService.this.mBound = false;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            PowerSavingTileService.this.mbinder = (ServiceBinder) service;
            PowerSavingTileService.this.mBound = true;
            PowerSavingTileService.this.mbinder.getService().registerStateChangeListener(PowerSavingTileService.this.listener);
            PowerSavingTileService.this.mLowPowerMode = PowerSavingTileService.this.mbinder.getService().getCurentMode();
            if (PowerSavingTileService.this.mLowPowerMode == -1) {
                PowerSavingTileService.this.enabled = false;
            } else {
                PowerSavingTileService.this.enabled = true;
            }
            PowerSavingTileService.this.refresh();
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.powersaver.PowerSavingTileService$2 */
    class C03932 implements IStateChangeListener {

        /* renamed from: com.evenwell.powersaving.g3.powersaver.PowerSavingTileService$2$1 */
        class C03921 implements Runnable {
            C03921() {
            }

            public void run() {
                PowerSavingTileService.this.refresh();
            }
        }

        C03932() {
        }

        public void onChange(int mode) {
            Log.d(PowerSavingTileService.TAG, "onChange mode : " + mode);
            if (PowerSavingTileService.this.mBound && PowerSavingTileService.this.mbinder.getService() != null && PowerSavingTileService.this.mLowPowerMode != mode) {
                if (mode == -1) {
                    PowerSavingTileService.this.enabled = false;
                } else {
                    PowerSavingTileService.this.enabled = true;
                }
                PowerSavingTileService.this.mLowPowerMode = mode;
                PowerSavingTileService.this.mHandler.post(new C03921());
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mHandler = new Handler();
    }

    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    public void onTileAdded() {
    }

    public void onStartListening() {
        bindService(new Intent(this, PowerSavingController.class), this.conn, 1);
    }

    public void onStopListening() {
        if (this.mBound) {
            if (this.mbinder.getService() != null) {
                this.mbinder.getService().unregisterStateChangeListener(this.listener);
            }
            unbindService(this.conn);
        }
    }

    public void refresh() {
        Tile t = getQsTile();
        if (getQsTile() != null) {
            Icon createWithResource;
            if (this.enabled) {
                createWithResource = Icon.createWithResource(getApplicationContext(), C0321R.drawable.ic_tile_power_saving_on);
            } else {
                createWithResource = Icon.createWithResource(getApplicationContext(), C0321R.drawable.ic_tile_power_saving_off);
            }
            t.setIcon(createWithResource);
            t.setLabel(getString(C0321R.string.fih_power_saving_power_saver_title_2));
            t.updateTile();
        }
    }

    public void onClick() {
        if (this.mBound && !PowerSavingUtils.isCharging(this) && this.mbinder.getService() != null) {
            this.mLowPowerMode = this.mbinder.getService().getCurentMode();
            if (this.mLowPowerMode == -1) {
                this.mbinder.getService().updateEventStatusForQS();
                this.mbinder.getService().updateApplyEventStatusForOtherUI(LATEST_EVENT_EXTRA.MANUAL);
                this.mbinder.getService().applyExtremeMode("apply by quicksettings");
                this.enabled = true;
            } else {
                this.mbinder.getService().updateEventStatusForQS();
                this.mbinder.getService().applyInAactiveMode("apply by quicksettings");
                this.enabled = false;
            }
            refresh();
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
