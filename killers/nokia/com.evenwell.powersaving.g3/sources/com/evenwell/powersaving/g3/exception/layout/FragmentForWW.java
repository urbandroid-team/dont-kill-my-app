package com.evenwell.powersaving.g3.exception.layout;

import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.appops.UpdateAppOpsHelper;
import com.evenwell.powersaving.g3.background.BackgroundCleanService;
import com.evenwell.powersaving.g3.background.ProcessMonitorService;
import com.evenwell.powersaving.g3.exception.BMS;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.exception.DefaultWhiteListService;
import com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity;
import com.evenwell.powersaving.g3.exception.PowerSaverExceptionAdapter;
import com.evenwell.powersaving.g3.exception.PowerSaverExceptionAdapter.SelectionListener;
import com.evenwell.powersaving.g3.exception.PowerSaverExceptionAppInfoItem;
import com.evenwell.powersaving.g3.provider.WakePathInfo;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentForWW extends Fragment implements SelectionListener {
    private static final int ACTION_FOR_NOTIFICATION = 6;
    private static final int ADD_ALL = 1;
    private static final int ADD_PACKAGE = 4;
    private static final boolean DBG = true;
    private static final int INIT = 3;
    private static final int ONLY_UPDATE = 0;
    private static final int REMOVE_ALL = 2;
    private static final int REMOVE_PACKAGE = 5;
    private static String TAG = TAG.PSLOG;
    private List<PowerSaverExceptionAppInfoItem> mAllAppsList = new ArrayList();
    private ListView mAppListView;
    private PowerSaverExceptionAdapter mAppListViewAdapter;
    private List<PowerSaverExceptionAppInfoItem> mBlackAppsList = new ArrayList();
    private ProgressDialog mProgressDialog;
    private List<PowerSaverExceptionAppInfoItem> mUIBlackList = new ArrayList();
    private UpdateListTask mUpdateListTask;

    /* renamed from: com.evenwell.powersaving.g3.exception.layout.FragmentForWW$1 */
    class C03771 implements OnClickListener {
        C03771() {
        }

        public void onClick(DialogInterface dialog, int which) {
        }
    }

    private class UpdateListTask extends AsyncTask<Object, Void, Void> {
        private final boolean mShowDialog;

        public UpdateListTask(boolean showDialog) {
            this.mShowDialog = showDialog;
        }

        protected void onPreExecute() {
            if (FragmentForWW.this.mProgressDialog != null) {
                FragmentForWW.this.mProgressDialog.dismiss();
            }
            if (this.mShowDialog) {
                FragmentForWW.this.showDialog();
            }
        }

        protected Void doInBackground(Object... index) {
            try {
                int parameter = ((Integer) index[0]).intValue();
                BackgroundPolicyExecutor BPE = BackgroundPolicyExecutor.getInstance(FragmentForWW.this.getActivity());
                if (parameter == 1) {
                    FragmentForWW.this.mBlackAppsList.clear();
                    FragmentForWW.this.mBlackAppsList.addAll(FragmentForWW.this.mAllAppsList);
                    List<PowerSaverExceptionAppInfoItem> whiteAppsList = new ArrayList();
                    List<String> whitelist = BPE.getWhiteListApp(64);
                    for (PowerSaverExceptionAppInfoItem appInfo : FragmentForWW.this.mAllAppsList) {
                        if (whitelist.contains(appInfo.GetPackageName())) {
                            whiteAppsList.add(appInfo);
                        }
                    }
                    FragmentForWW.this.mBlackAppsList.removeAll(whiteAppsList);
                    FragmentForWW.this.addAppToBlackList(FragmentForWW.this.mBlackAppsList);
                    FragmentForWW.this.removeAppFromBlackList((List) whiteAppsList);
                    FragmentForWW.this.mUIBlackList.clear();
                    FragmentForWW.this.mUIBlackList.addAll(FragmentForWW.this.mBlackAppsList);
                } else if (parameter == 2) {
                    FragmentForWW.this.removeAppFromBlackList(FragmentForWW.this.mAllAppsList);
                    FragmentForWW.this.mBlackAppsList.clear();
                    FragmentForWW.this.mUIBlackList.clear();
                } else if (parameter == 3) {
                    FragmentForWW.this.mAllAppsList.clear();
                    FragmentForWW.this.mAllAppsList = BPE.getAllApList();
                    List<String> blacklist = BPE.getDisAutoAppList();
                    FragmentForWW.this.mBlackAppsList.clear();
                    for (PowerSaverExceptionAppInfoItem appInfo2 : FragmentForWW.this.mAllAppsList) {
                        if (blacklist.contains(appInfo2.GetPackageName())) {
                            FragmentForWW.this.mBlackAppsList.add(appInfo2);
                        }
                    }
                    restrictedApps = FragmentForWW.this.getRestirctedApp(FragmentForWW.this.getActivity(), FragmentForWW.this.mAllAppsList);
                    restrictedApps.removeAll(FragmentForWW.this.mBlackAppsList);
                    FragmentForWW.this.mUIBlackList.clear();
                    FragmentForWW.this.mUIBlackList.addAll(restrictedApps);
                    FragmentForWW.this.mUIBlackList.addAll(FragmentForWW.this.mBlackAppsList);
                    FragmentForWW.this.mAllAppsList.removeAll(FragmentForWW.this.mUIBlackList);
                    Collections.sort(FragmentForWW.this.mAllAppsList);
                    Collections.sort(FragmentForWW.this.mUIBlackList);
                    FragmentForWW.this.mAllAppsList.addAll(0, FragmentForWW.this.mUIBlackList);
                } else if (parameter == 5) {
                    item = index[1];
                    FragmentForWW.this.mBlackAppsList.remove(item);
                    FragmentForWW.this.removeAppFromBlackList(item.GetPackageName());
                    FragmentForWW.this.mUIBlackList.remove(item);
                } else if (parameter == 4) {
                    item = (PowerSaverExceptionAppInfoItem) index[1];
                    FragmentForWW.this.mBlackAppsList.add(item);
                    FragmentForWW.this.addAppToBlackList(item.GetPackageName());
                    FragmentForWW.this.mUIBlackList.add(item);
                } else if (parameter == 6) {
                    FragmentForWW.this.mAllAppsList.clear();
                    FragmentForWW.this.mAllAppsList = BPE.getAllApList();
                    List<String> userInstall_whitelistApp = BPE.getWhiteListApp(64);
                    FragmentForWW.this.mBlackAppsList.clear();
                    for (PowerSaverExceptionAppInfoItem appInfo22 : FragmentForWW.this.mAllAppsList) {
                        if (userInstall_whitelistApp.contains(appInfo22.GetPackageName())) {
                            FragmentForWW.this.mBlackAppsList.remove(appInfo22);
                        } else {
                            FragmentForWW.this.mBlackAppsList.add(appInfo22);
                            FragmentForWW.this.addAppToBlackList(appInfo22.GetPackageName());
                        }
                    }
                    restrictedApps = FragmentForWW.this.getRestirctedApp(FragmentForWW.this.getActivity(), FragmentForWW.this.mAllAppsList);
                    restrictedApps.removeAll(FragmentForWW.this.mBlackAppsList);
                    FragmentForWW.this.mUIBlackList.clear();
                    FragmentForWW.this.mUIBlackList.addAll(restrictedApps);
                    FragmentForWW.this.mUIBlackList.addAll(FragmentForWW.this.mBlackAppsList);
                    FragmentForWW.this.mAllAppsList.removeAll(FragmentForWW.this.mUIBlackList);
                    Collections.sort(FragmentForWW.this.mAllAppsList);
                    Collections.sort(FragmentForWW.this.mUIBlackList);
                    FragmentForWW.this.mAllAppsList.addAll(0, FragmentForWW.this.mUIBlackList);
                }
                FragmentForWW.this.checkWakeUpDB();
                boolean bms = !FragmentForWW.this.mBlackAppsList.isEmpty();
                BMS.getInstance(FragmentForWW.this.getActivity()).setBMSValue(bms);
                if (FragmentForWW.this.getActivity().getResources().getBoolean(C0321R.bool.is_e1m_Device) && PSUtils.enableTestFunction()) {
                    if (bms) {
                        PowerSavingUtils.startProcessMonitorServiceWithAction(FragmentForWW.this.getActivity(), ProcessMonitorService.REFRESH_HMD_WHITELIST);
                        FragmentForWW.this.getActivity().startService(new Intent(FragmentForWW.this.getActivity(), BackgroundCleanService.class));
                    } else {
                        PowerSavingUtils.setProcessMonitorServiceEnable(FragmentForWW.this.getActivity(), bms);
                        FragmentForWW.this.getActivity().stopService(new Intent(FragmentForWW.this.getActivity(), BackgroundCleanService.class));
                    }
                    return null;
                }
                Log.d(FragmentForWW.TAG, "startProcessMonitorServiceWithAction");
                PowerSavingUtils.startProcessMonitorServiceWithAction(FragmentForWW.this.getActivity(), ProcessMonitorService.REFRESH_HMD_WHITELIST);
                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        protected void onPostExecute(Void result) {
            if (BMS.getInstance(FragmentForWW.this.getActivity()).getBMSValue()) {
                FragmentForWW.this.showWarningDialogIfFlagDisable(FragmentForWW.this.getActivity());
            }
            FragmentForWW.this.mAppListViewAdapter.setApplist(FragmentForWW.this.mAllAppsList, FragmentForWW.this.mUIBlackList);
            FragmentForWW.this.mAppListViewAdapter.notifyDataSetChanged();
            if (FragmentForWW.this.mProgressDialog != null) {
                FragmentForWW.this.mProgressDialog.dismiss();
            }
        }

        protected void onCancelled() {
            if (FragmentForWW.this.mProgressDialog != null) {
                FragmentForWW.this.mProgressDialog.dismiss();
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(C0321R.layout.fragment_lockscreen_frag_for_ww, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "[FragmentForWW] onCreate");
        try {
            getActivity().getActionBar().setTitle(C0321R.string.lock_screen_ap_protect_title);
            if (PSUtils.isCNModel(getActivity())) {
                getActivity().setRequestedOrientation(1);
            }
            this.mAppListViewAdapter = new PowerSaverExceptionAdapter(getActivity());
            this.mAppListView = (ListView) getView().findViewById(C0321R.id.applist);
            this.mAppListView.setAdapter(this.mAppListViewAdapter);
            this.mAppListViewAdapter.setSelectionListener(this);
            if (!BackgroundPolicyExecutor.getInstance(getActivity()).isCNModel()) {
                getActivity().startService(new Intent(getActivity(), DefaultWhiteListService.class));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onPause() {
        super.onPause();
        Log.i(TAG, "[FragmentForWW] onPause");
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
        stopUpdateListTask();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "[FragmentForWW] onDestroy");
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
        stopUpdateListTask();
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        Log.i(TAG, "[FragmentForWW]onMultiWindowModeChanged : " + isInMultiWindowMode);
        if (!isInMultiWindowMode) {
            showDialog();
            startUpdateListTask(true, Integer.valueOf(0));
        }
    }

    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle == null) {
            startUpdateListTask(true, Integer.valueOf(3));
        } else if (PowerSaverExceptionActivity.EXTRA_DATA_NOTIFICATION.equals(bundle.getString(PowerSaverExceptionActivity.EXTRA_FROM))) {
            startUpdateListTask(true, Integer.valueOf(6));
            setArguments(null);
        }
        try {
            getActivity().getActionBar().setDisplayOptions(16, 16);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showDialog() {
        if (this.mProgressDialog == null) {
            try {
                this.mProgressDialog = ProgressDialog.show(getActivity(), "", "", true, false);
                this.mProgressDialog.setContentView(C0321R.layout.custom_progress_dialog);
                this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                this.mProgressDialog.setCancelable(false);
                this.mProgressDialog.setCanceledOnTouchOutside(false);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            this.mProgressDialog = ProgressDialog.show(getActivity(), "", "", true, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private long getWakeUpId(Context context, String pck) {
        List<WakePathInfo> wakeList = PowerSavingUtils.getWakeList(context);
        for (int i = 0; i < wakeList.size(); i++) {
            if (pck.equalsIgnoreCase(((WakePathInfo) wakeList.get(i)).mPackageName)) {
                return ((WakePathInfo) wakeList.get(i)).id;
            }
        }
        return 0;
    }

    private void stopUpdateListTask() {
        if (this.mUpdateListTask != null && this.mUpdateListTask.getStatus() != Status.FINISHED) {
            this.mUpdateListTask.cancel(true);
        }
    }

    private void startUpdateListTask(boolean showDialog, Object... objects) {
        stopUpdateListTask();
        this.mUpdateListTask = new UpdateListTask(showDialog);
        this.mUpdateListTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, objects);
    }

    private void checkWakeUpDB() {
        try {
            List<String> whitelist = BackgroundPolicyExecutor.getInstance(getActivity()).getWhiteListApp();
            List<WakePathInfo> wakeList = PowerSavingUtils.getWakeList(getActivity());
            for (int i = 0; i < wakeList.size(); i++) {
                if (whitelist.contains(((WakePathInfo) wakeList.get(i)).mPackageName)) {
                    long id = getWakeUpId(getActivity(), ((WakePathInfo) wakeList.get(i)).mPackageName);
                    if (id > 0) {
                        PowerSavingUtils.setForbidStatu(getActivity(), id, false);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onSelectionModeChange(int position) {
        Log.i(TAG, "[FragmentForWW] onSelectionModeChange click " + position);
        PowerSaverExceptionAppInfoItem appInfo = (PowerSaverExceptionAppInfoItem) this.mAppListView.getItemAtPosition(position);
        if (appInfo != null) {
            Log.i(TAG, "[FragmentForWW] onSelectionModeChange appInfo " + appInfo.GetAppName());
            if (this.mUIBlackList.contains(appInfo)) {
                startUpdateListTask(false, Integer.valueOf(5), appInfo);
                return;
            }
            startUpdateListTask(false, Integer.valueOf(4), appInfo);
        }
    }

    private void addAppToBlackList(String pkgName) {
        BackgroundPolicyExecutor BPE = BackgroundPolicyExecutor.getInstance(getActivity());
        BPE.removeAppFromWhiteList(pkgName);
        BPE.addAppToDisAutoList(pkgName);
    }

    private void removeAppFromBlackList(String pkgName) {
        BackgroundPolicyExecutor BPE = BackgroundPolicyExecutor.getInstance(getActivity());
        BPE.addAppToWhiteList(pkgName);
        BPE.removeAppFromDisAutoList(pkgName);
        UpdateAppOpsHelper.updateRunAnyInBackgroundOps(getActivity(), pkgName, 0);
    }

    private void removeAppFromBlackList(List<PowerSaverExceptionAppInfoItem> appsList) {
        BackgroundPolicyExecutor BPE = BackgroundPolicyExecutor.getInstance(getActivity());
        BPE.addAppsToWhiteList(appsList);
        BPE.removeAppsFromDisAutoList(appsList);
        ArrayList pkgNames = new ArrayList();
        for (PowerSaverExceptionAppInfoItem appInfoItem : appsList) {
            pkgNames.add(appInfoItem.GetPackageName());
        }
        UpdateAppOpsHelper.updateRunAnyInBackgroundOps(getActivity(), pkgNames, 0);
    }

    private void addAppToBlackList(List<PowerSaverExceptionAppInfoItem> appsList) {
        BackgroundPolicyExecutor BPE = BackgroundPolicyExecutor.getInstance(getActivity());
        BPE.removeAppsFromWhiteList(appsList);
        BPE.addAppsToDisAutoList(appsList);
    }

    public void showWarningDialogIfFlagDisable(Context context) {
        if (context != null) {
            boolean buildFlag = context.getResources().getBoolean(17956959);
            Log.i(TAG, "config_enableAutoPowerModes=" + buildFlag);
            if (!buildFlag) {
                alertDialogShow(context);
            }
        }
    }

    public void alertDialogShow(Context context) {
        Builder builder = new Builder(context);
        builder.setTitle(C0321R.string.warning);
        builder.setMessage(C0321R.string.set_overlay_config_enableAutoPowerModes);
        builder.setPositiveButton(C0321R.string.power_saving_dialog_ok, new C03771());
        builder.create().show();
    }

    private List<PowerSaverExceptionAppInfoItem> getRestirctedApp(Context context, List<PowerSaverExceptionAppInfoItem> appInfoItems) {
        List<PowerSaverExceptionAppInfoItem> restrictedApps = new ArrayList();
        for (PowerSaverExceptionAppInfoItem appInfoItem : appInfoItems) {
            if (UpdateAppOpsHelper.checkRunAnyInBackgroundOps(context, appInfoItem.GetPackageName()) == 1) {
                restrictedApps.add(appInfoItem);
            }
        }
        return restrictedApps;
    }
}
