package com.evenwell.powersaving.g3.powersaver;

import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;

public class GrayscaleModeTileService extends TileService {
    private static final boolean DBG = true;
    private static final String TAG = "GrayscaleModeTileService";
    private boolean enabled = false;
    private ContentObserver mGaryScalModeSettingObserver = null;

    private class GaryScalModeSettingObserver extends ContentObserver {
        public GaryScalModeSettingObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            GrayscaleModeTileService.this.enabled = LpmUtils.getSimulateColorSpaceMode(GrayscaleModeTileService.this.getApplicationContext());
            GrayscaleModeTileService.this.refresh();
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mGaryScalModeSettingObserver = new GaryScalModeSettingObserver();
    }

    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    public void onTileAdded() {
    }

    public void onStartListening() {
        this.enabled = LpmUtils.getSimulateColorSpaceMode(getApplicationContext());
        refresh();
        getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_display_daltonizer_enabled"), true, this.mGaryScalModeSettingObserver);
    }

    public void onStopListening() {
        getContentResolver().unregisterContentObserver(this.mGaryScalModeSettingObserver);
    }

    public void refresh() {
        Tile t = getQsTile();
        if (getQsTile() != null) {
            Icon createWithResource;
            if (this.enabled) {
                createWithResource = Icon.createWithResource(getApplicationContext(), C0321R.drawable.ic_grayscale_mode_on);
            } else {
                createWithResource = Icon.createWithResource(getApplicationContext(), C0321R.drawable.ic_grayscale_mode_off);
            }
            t.setIcon(createWithResource);
            t.setLabel(getString(C0321R.string.fih_power_saving_grayscale_mode_title));
            t.updateTile();
        }
    }

    public void onClick() {
        if (this.enabled) {
            LpmUtils.setMonoChromacyEnabled(getApplicationContext(), SWITCHER.OFF);
            this.enabled = false;
        } else {
            LpmUtils.setMonoChromacyEnabled(getApplicationContext(), SWITCHER.ON);
            this.enabled = true;
        }
        refresh();
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
