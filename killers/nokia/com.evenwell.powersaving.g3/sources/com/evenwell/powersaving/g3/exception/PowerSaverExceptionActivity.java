package com.evenwell.powersaving.g3.exception;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.exception.layout.FragmentForCN;
import com.evenwell.powersaving.g3.exception.layout.FragmentForWW;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.ACTION;
import com.evenwell.powersaving.g3.utils.PSUtils;

public class PowerSaverExceptionActivity extends Activity {
    public static String EXTRA_DATA_NOTIFICATION = "notification";
    public static String EXTRA_FROM = "from";
    private static String TAG = "ExceptionActivity";
    private Fragment mFragment;
    private FragmentManager manager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0321R.layout.layout_lock_screen_ap_protect);
        this.manager = getFragmentManager();
        String FTAG = "";
        String action = "";
        try {
            if (PSUtils.isCNModel(this)) {
                this.mFragment = new FragmentForCN();
                FTAG = "FragmentForCN";
            } else {
                Intent intent = getIntent();
                if (intent != null) {
                    action = intent.getAction();
                }
                this.mFragment = new FragmentForWW();
                if (ACTION.BAM_TURN_ON.equals(action)) {
                    startServices();
                    Bundle bundle = new Bundle();
                    bundle.putString(EXTRA_FROM, EXTRA_DATA_NOTIFICATION);
                    this.mFragment.setArguments(bundle);
                    setIntent(null);
                }
                FTAG = "FragmentForWW";
                Log.d(TAG, "onCreate() intent :" + intent);
            }
            FragmentTransaction transaction = this.manager.beginTransaction();
            transaction.replace(C0321R.id.center, this.mFragment, FTAG);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onResume() {
        super.onResume();
        String action = "";
        Intent intent = getIntent();
        if (intent != null) {
            if (ACTION.BAM_TURN_ON.equals(intent.getAction())) {
                Log.d(TAG, "onResume() intent : " + intent);
                startServices();
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_FROM, EXTRA_DATA_NOTIFICATION);
                this.mFragment.setArguments(bundle);
                setIntent(null);
                FragmentTransaction transaction = this.manager.beginTransaction();
                transaction.detach(this.mFragment);
                transaction.attach(this.mFragment);
                transaction.commit();
            }
        }
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent " + intent);
        setIntent(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return true;
    }

    private void startServices() {
        BMS.getInstance(this).setBMSValue(true);
        PowerSavingUtils.setProcessMonitorServiceEnable(this, true);
    }
}
