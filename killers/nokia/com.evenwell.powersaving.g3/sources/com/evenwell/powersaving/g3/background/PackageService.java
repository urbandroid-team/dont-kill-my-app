package com.evenwell.powersaving.g3.background;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.appops.UpdateBackgroundOpsService;
import com.evenwell.powersaving.g3.appops.UpdateBootCompleteService;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.p000e.doze.EDozeService;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.PARM;
import com.evenwell.powersaving.g3.utils.PSUtils;
import com.fihtdc.push_system.lib.common.PushMessageContract;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PackageService extends Service {
    private static final boolean DBG = true;
    private static final String TAG = "PackageService";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private List<String> mExemptPrefix;
    private BroadcastReceiver mReceiver = new C03271();

    /* renamed from: com.evenwell.powersaving.g3.background.PackageService$1 */
    class C03271 extends BroadcastReceiver {
        C03271() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.i(PackageService.TAG, "onReceive action = " + intent.getAction() + ", EXTRA_REPLACING = " + intent.getBooleanExtra("android.intent.extra.REPLACING", false));
            String action = intent.getAction();
            boolean isPackageReplaced = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
            String packageName;
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                packageName = getPackageName(intent);
                Log.i(PackageService.TAG, "add packageName = " + packageName);
                if (packageName != null) {
                    PackageService.this.checkCTS(packageName);
                    if (!isPackageReplaced) {
                        if (BackgroundPolicyExecutor.getInstance(context).isCNModel()) {
                            List<String> list = BackgroundPolicyExecutor.getInstance(context).getWhiteListApp(72);
                            boolean hasExemptPrefix = false;
                            for (String prefix : PackageService.this.mExemptPrefix) {
                                if (packageName.contains(prefix)) {
                                    hasExemptPrefix = true;
                                }
                            }
                            if (list.contains(packageName) || hasExemptPrefix) {
                                BackgroundPolicyExecutor.getInstance(context).addAppToWhiteList(packageName);
                            } else {
                                BackgroundPolicyExecutor.getInstance(context).removeAppFromWhiteList(packageName);
                            }
                            if (BackgroundPolicyExecutor.getInstance(context).isInDisautoWhiteList(packageName) || hasExemptPrefix) {
                                Log.d(PackageService.TAG, "addAppToDozeWhiteList " + packageName);
                                BackgroundPolicyExecutor.getInstance(context).addAppToDozeWhiteList(packageName);
                                return;
                            }
                            Log.d(PackageService.TAG, "addAppToDisAutoList " + packageName);
                            BackgroundPolicyExecutor.getInstance(context).addAppToDisAutoList(packageName);
                            return;
                        }
                        try {
                            PSUtils.packageAddOnWW(context, packageName);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                packageName = getPackageName(intent);
                Log.i(PackageService.TAG, "remove packageName = " + packageName);
                if (packageName != null && !isPackageReplaced) {
                    BackgroundPolicyExecutor.getInstance(context).removeAppFromWhiteList(packageName);
                    if (BackgroundPolicyExecutor.getInstance(context).isCNModel()) {
                        BackgroundPolicyExecutor.getInstance(context).removeAppFromDozeWhiteList(packageName);
                    }
                    BackgroundPolicyExecutor.getInstance(context).removeAppFromDisAutoList(packageName);
                }
            } else if (!"android.intent.action.PACKAGE_REPLACED".equals(action)) {
            }
        }

        private String getPackageName(Intent intent) {
            Uri uri = intent.getData();
            return uri != null ? uri.getSchemeSpecificPart() : null;
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.background.PackageService$2 */
    class C03282 implements Runnable {
        WeakReference<PackageService> wr = new WeakReference(PackageService.this);

        C03282() {
        }

        public void run() {
            if (this.wr.get() != null) {
                try {
                    PowerSavingUtils.insertTimeStampToRestartServiceTable((Context) this.wr.get(), "PS");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        Log.i(TAG, "onCreate()");
        Intent updateBootCompleteService = new Intent(this, UpdateBootCompleteService.class);
        updateBootCompleteService.setAction(UpdateBootCompleteService.UPDATE_ALL_APPS_BC);
        startService(updateBootCompleteService);
        Intent updateBackgroundOpsService = new Intent(this, UpdateBackgroundOpsService.class);
        updateBackgroundOpsService.setAction(UpdateBackgroundOpsService.UPDATE_ALL_APPS_BG);
        startService(updateBackgroundOpsService);
        registerReceiver();
        this.mExemptPrefix = BackgroundPolicyExecutor.getInstance(this).getExemptPrefix();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if (intent == null) {
            this.executorService.execute(new C03282());
        }
        return 1;
    }

    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        unregisterReceiver();
        this.executorService.shutdownNow();
    }

    private void registerReceiver() {
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme(PushMessageContract.MESSAGE_KEY_PACKAGE_NAME);
        registerReceiver(this.mReceiver, packageFilter);
    }

    private void unregisterReceiver() {
        try {
            unregisterReceiver(this.mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkCTS(String packageName) {
        if (!PSUtils.isCTS() && BackgroundPolicyExecutor.getInstance(this).getWhiteListApp(8).contains(packageName)) {
            updateCTSFlag();
        }
        checkCTSEx(packageName);
    }

    private void checkCTSEx(String packageName) {
        if (!PSUtils.isCTS()) {
            String[] ctsAppEx = getResources().getStringArray(C0321R.array.cts_app_ex);
            if (ctsAppEx != null) {
                for (String regex : ctsAppEx) {
                    if (packageName.matches(regex)) {
                        updateCTSFlag();
                        return;
                    }
                }
            }
        }
    }

    private void updateCTSFlag() {
        PSUtils.setCTS(true);
        startService(new Intent(this, EDozeService.class));
        PowerSavingUtils.setSettingsProvider(this, PARM.KEY_BACKGROUND_EXECUTION_ENABLED, "false");
    }
}
