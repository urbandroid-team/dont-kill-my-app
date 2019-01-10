package com.evenwell.powersaving.g3.exception.layout;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.exception.PowerSaverExceptionAdapterForCN;
import com.evenwell.powersaving.g3.exception.PowerSaverExceptionAdapterForCN.SelectionListener;
import com.evenwell.powersaving.g3.exception.PowerSaverExceptionAppInfoItem;
import com.evenwell.powersaving.g3.provider.WakePathInfo;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentForCN extends Fragment {
    private static final int ADD_ALL_FOR_BAM = 1;
    private static final int ADD_ALL_FOR_DISAUTO = 6;
    private static final int ADD_PACKAGE_FOR_BAM = 4;
    private static final int ADD_PACKAGE_FOR_DISAUTO = 8;
    private static final boolean DBG = true;
    private static final int INIT = 3;
    private static final int NO_CHANGE = 12;
    private static final int ONLY_UPDATE = 0;
    private static final int REMOVE_ALL_FOR_BAM = 2;
    private static final int REMOVE_ALL_FOR_DISAUTO = 7;
    private static final int REMOVE_PACKAGE_FOR_BAM = 5;
    private static final int REMOVE_PACKAGE_FOR_DISAUTO = 9;
    private static String TAG = TAG.PSLOG;
    private static final int UPDATE_BAM = 10;
    private static final int UPDATE_DISAUTO = 11;
    private boolean isFromMulti = false;
    private boolean isMultiWindowFromCreate = false;
    private CheckBox mAllAPCheckBoxBAM;
    private CheckBox mAllAPCheckBoxDisauto;
    private OnCheckedChangeListener mAllAPPBAMListerner = new C03701();
    private OnCheckedChangeListener mAllAPPDisAutoListerner = new C03732();
    private List<PowerSaverExceptionAppInfoItem> mAllAppsList = new ArrayList();
    private ListView mAppListView;
    private PowerSaverExceptionAdapterForCN mAppListViewAdapter;
    SelectionListener mBAMSelectionListener = new C03743();
    private LinearLayout mCheckAllLayout;
    private AlertDialog mDialog;
    private List<PowerSaverExceptionAppInfoItem> mDisautoAppList = new ArrayList();
    SelectionListener mDisautoSelectionListener = new C03754();
    private ProgressDialog mProgressDialog;
    private UpdateListTask mUpdateListTask;
    private List<PowerSaverExceptionAppInfoItem> mWhiteAppsList = new ArrayList();

    /* renamed from: com.evenwell.powersaving.g3.exception.layout.FragmentForCN$1 */
    class C03701 implements OnCheckedChangeListener {
        C03701() {
        }

        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            Log.d(FragmentForCN.TAG, "[FragmentForCN] mAllAPPBAMListerner : " + isChecked);
            buttonView.setChecked(!isChecked);
            String message = "";
            if (isChecked) {
                message = FragmentForCN.this.getActivity().getString(C0321R.string.alert_message_bam_all_on);
            } else {
                try {
                    message = FragmentForCN.this.getActivity().getString(C0321R.string.alert_message_bam_all_off);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            FragmentForCN.this.showAlertDialog(message, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (isChecked) {
                        FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(1));
                        return;
                    }
                    FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(2));
                }
            }, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    boolean z;
                    FragmentForCN.this.mAllAPCheckBoxBAM.setOnCheckedChangeListener(null);
                    CompoundButton compoundButton = buttonView;
                    if (isChecked) {
                        z = false;
                    } else {
                        z = true;
                    }
                    compoundButton.setChecked(z);
                    FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(12));
                }
            });
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.exception.layout.FragmentForCN$2 */
    class C03732 implements OnCheckedChangeListener {
        C03732() {
        }

        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            Log.d(FragmentForCN.TAG, "[FragmentForCN] mAllAPPDisAutoListerner : " + isChecked);
            buttonView.setChecked(!isChecked);
            String message = "";
            if (isChecked) {
                message = FragmentForCN.this.getActivity().getString(C0321R.string.alert_message_auto_activate_all_on);
            } else {
                try {
                    message = FragmentForCN.this.getActivity().getString(C0321R.string.alert_message_auto_activate_all_off);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            FragmentForCN.this.showAlertDialog(message, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (isChecked) {
                        FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(6));
                        return;
                    }
                    FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(7));
                }
            }, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    boolean z;
                    FragmentForCN.this.mAllAPCheckBoxDisauto.setOnCheckedChangeListener(null);
                    CompoundButton compoundButton = buttonView;
                    if (isChecked) {
                        z = false;
                    } else {
                        z = true;
                    }
                    compoundButton.setChecked(z);
                    FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(12));
                }
            });
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.exception.layout.FragmentForCN$3 */
    class C03743 implements SelectionListener {
        C03743() {
        }

        public void onSelectionModeChange(int position, boolean isCheckedChange) {
            Log.i(FragmentForCN.TAG, "[FragmentForCN] mBAMSelectionListener onSelectionModeChange click " + position);
            PowerSaverExceptionAppInfoItem appInfo = (PowerSaverExceptionAppInfoItem) FragmentForCN.this.mAppListView.getItemAtPosition(position);
            if (appInfo != null) {
                Log.i(FragmentForCN.TAG, "[FragmentForCN] mBAMSelectionListener onSelectionModeChange appInfo " + appInfo.GetAppName());
                if (!isCheckedChange) {
                    FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(12), appInfo);
                } else if (FragmentForCN.this.mWhiteAppsList.contains(appInfo)) {
                    FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(5), appInfo);
                } else {
                    FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(4), appInfo);
                }
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.exception.layout.FragmentForCN$4 */
    class C03754 implements SelectionListener {
        C03754() {
        }

        public void onSelectionModeChange(int position, boolean isCheckedChange) {
            Log.i(FragmentForCN.TAG, "[FragmentForCN] mDisautoSelectionListener onSelectionModeChange click " + position);
            PowerSaverExceptionAppInfoItem appInfo = (PowerSaverExceptionAppInfoItem) FragmentForCN.this.mAppListView.getItemAtPosition(position);
            if (appInfo != null) {
                Log.i(FragmentForCN.TAG, "[FragmentForCN] mDisautoSelectionListener onSelectionModeChange appInfo " + appInfo.GetAppName());
                if (!isCheckedChange) {
                    FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(12), appInfo);
                } else if (FragmentForCN.this.mDisautoAppList.contains(appInfo)) {
                    FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(9), appInfo);
                } else {
                    FragmentForCN.this.startUpdateListTask(true, Integer.valueOf(8), appInfo);
                }
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.exception.layout.FragmentForCN$5 */
    class C03765 implements OnKeyListener {
        C03765() {
        }

        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
            FragmentForCN.this.mDialog.dismiss();
            return true;
        }
    }

    private class UpdateListTask extends AsyncTask<Object, Void, Void> {
        private final boolean mShowDialog;

        public UpdateListTask(boolean showDialog) {
            this.mShowDialog = showDialog;
        }

        protected void onPreExecute() {
            if (FragmentForCN.this.mProgressDialog != null) {
                FragmentForCN.this.mProgressDialog.dismiss();
            }
            if (this.mShowDialog) {
                FragmentForCN.this.showDialog();
            }
        }

        protected Void doInBackground(Object... index) {
            try {
                int parameter = ((Integer) index[0]).intValue();
                BackgroundPolicyExecutor BPE = BackgroundPolicyExecutor.getInstance(FragmentForCN.this.getActivity());
                if (parameter == 3) {
                    FragmentForCN.this.mAllAppsList.clear();
                    FragmentForCN.this.mAllAppsList = BPE.getAllApList();
                    List<String> whitelist = BPE.getWhiteListApp();
                    List<String> disautolist = BPE.getDisAutoAppList();
                    FragmentForCN.this.mWhiteAppsList.clear();
                    FragmentForCN.this.mDisautoAppList.clear();
                    for (PowerSaverExceptionAppInfoItem appInfo : FragmentForCN.this.mAllAppsList) {
                        if (whitelist.contains(appInfo.GetPackageName())) {
                            FragmentForCN.this.mWhiteAppsList.add(appInfo);
                        }
                        if (disautolist.contains(appInfo.GetPackageName())) {
                            FragmentForCN.this.mDisautoAppList.add(appInfo);
                        }
                    }
                    Collections.sort(FragmentForCN.this.mAllAppsList);
                    Collections.sort(FragmentForCN.this.mWhiteAppsList);
                    Collections.sort(FragmentForCN.this.mDisautoAppList);
                    FragmentForCN.this.mAllAppsList.removeAll(FragmentForCN.this.mWhiteAppsList);
                    FragmentForCN.this.mAllAppsList.addAll(0, FragmentForCN.this.mWhiteAppsList);
                } else if (parameter == 1) {
                    FragmentForCN.this.mWhiteAppsList.clear();
                    BPE.addAppsToWhiteList(FragmentForCN.this.mAllAppsList);
                    FragmentForCN.this.mWhiteAppsList.addAll(FragmentForCN.this.mAllAppsList);
                } else if (parameter == 2) {
                    BPE.removeAppsFromWhiteList(FragmentForCN.this.mAllAppsList);
                    FragmentForCN.this.mWhiteAppsList.clear();
                } else if (parameter == 5) {
                    item = index[1];
                    FragmentForCN.this.mWhiteAppsList.remove(item);
                    BPE.removeAppFromWhiteList(item.GetPackageName());
                } else if (parameter == 4) {
                    item = (PowerSaverExceptionAppInfoItem) index[1];
                    FragmentForCN.this.mWhiteAppsList.add(item);
                    BPE.addAppToWhiteList(item.GetPackageName());
                } else if (parameter == 6) {
                    FragmentForCN.this.mDisautoAppList.clear();
                    if (BackgroundPolicyExecutor.getInstance(FragmentForCN.this.getActivity()).isCNModel()) {
                        BPE.addAppsToDozeWhiteList(FragmentForCN.this.mAllAppsList);
                    }
                    BPE.removeAppsFromDisAutoList(FragmentForCN.this.mAllAppsList);
                } else if (parameter == 7) {
                    BPE.addAppsToDisAutoList(FragmentForCN.this.mAllAppsList);
                    if (BackgroundPolicyExecutor.getInstance(FragmentForCN.this.getActivity()).isCNModel()) {
                        BPE.removeAppsFromDozeWhiteList(FragmentForCN.this.mAllAppsList);
                    }
                    FragmentForCN.this.mDisautoAppList.clear();
                    FragmentForCN.this.mDisautoAppList.addAll(FragmentForCN.this.mAllAppsList);
                } else if (parameter == 8) {
                    item = (PowerSaverExceptionAppInfoItem) index[1];
                    FragmentForCN.this.mDisautoAppList.add(item);
                    BPE.addAppToDisAutoList(item.GetPackageName());
                    if (BackgroundPolicyExecutor.getInstance(FragmentForCN.this.getActivity()).isCNModel()) {
                        BPE.removeAppFromDozeWhiteList(item.GetPackageName());
                    }
                } else if (parameter == 9) {
                    item = (PowerSaverExceptionAppInfoItem) index[1];
                    FragmentForCN.this.mDisautoAppList.remove(item);
                    BPE.removeAppFromDisAutoList(item.GetPackageName());
                    if (BackgroundPolicyExecutor.getInstance(FragmentForCN.this.getActivity()).isCNModel()) {
                        BPE.addAppToDozeWhiteList(item.GetPackageName());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            FragmentForCN.this.checkWakeUpDB();
            return null;
        }

        protected void onPostExecute(Void result) {
            FragmentForCN.this.mAppListViewAdapter.setApplist(FragmentForCN.this.mAllAppsList);
            FragmentForCN.this.mAppListViewAdapter.notifyDataSetChanged();
            FragmentForCN.this.mAllAPCheckBoxBAM.setOnCheckedChangeListener(null);
            FragmentForCN.this.mAllAPCheckBoxBAM.setChecked(FragmentForCN.this.checkAPAllInWhite());
            FragmentForCN.this.mAllAPCheckBoxBAM.setOnCheckedChangeListener(FragmentForCN.this.mAllAPPBAMListerner);
            FragmentForCN.this.mAllAPCheckBoxDisauto.setOnCheckedChangeListener(null);
            FragmentForCN.this.mAllAPCheckBoxDisauto.setChecked(FragmentForCN.this.checkAPAllInDisauto());
            FragmentForCN.this.mAllAPCheckBoxDisauto.setOnCheckedChangeListener(FragmentForCN.this.mAllAPPDisAutoListerner);
            if (FragmentForCN.this.mProgressDialog != null) {
                FragmentForCN.this.mProgressDialog.dismiss();
            }
        }

        protected void onCancelled() {
            if (FragmentForCN.this.mProgressDialog != null) {
                FragmentForCN.this.mProgressDialog.dismiss();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(C0321R.layout.fragment_lockscreen_frag_for_cn, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            this.mCheckAllLayout = (LinearLayout) getView().findViewById(C0321R.id.check_all_layout);
            Log.i(TAG, "[FragmentForCN] onCreate");
            getActivity().getActionBar().setTitle(C0321R.string.lock_screen_ap_protect_title);
            if (PSUtils.isCNModel(getActivity())) {
                getActivity().setRequestedOrientation(1);
            }
            this.mAppListViewAdapter = new PowerSaverExceptionAdapterForCN(getActivity());
            this.mAppListView = (ListView) getView().findViewById(C0321R.id.applist);
            this.mAppListView.setAdapter(this.mAppListViewAdapter);
            this.mAppListViewAdapter.setSelectionListenerForBAM(this.mBAMSelectionListener);
            this.mAppListViewAdapter.setSelectionListenerForDisauto(this.mDisautoSelectionListener);
            this.mAllAPCheckBoxBAM = (CheckBox) getView().findViewById(C0321R.id.allAppCheckBoxBAM);
            this.mAllAPCheckBoxDisauto = (CheckBox) getView().findViewById(C0321R.id.allAppCheckBoxDisAuto);
            this.mAllAPCheckBoxBAM.setOnCheckedChangeListener(this.mAllAPPBAMListerner);
            this.mAllAPCheckBoxDisauto.setOnCheckedChangeListener(this.mAllAPPDisAutoListerner);
            this.isMultiWindowFromCreate = getActivity().isInMultiWindowMode();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void onDetach() {
        super.onDetach();
    }

    public void onPause() {
        super.onPause();
        Log.i(TAG, "[FragmentForCN] onPause");
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
        stopUpdateListTask();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "[FragmentForCN] onDestroy");
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
        stopUpdateListTask();
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        Log.i(TAG, "[FragmentForCN]onMultiWindowModeChanged : " + isInMultiWindowMode);
        if (!isInMultiWindowMode) {
            showDialog();
            startUpdateListTask(true, Integer.valueOf(0));
            this.isFromMulti = true;
        }
    }

    public void onResume() {
        super.onResume();
        startUpdateListTask(true, Integer.valueOf(3));
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
        this.mProgressDialog = ProgressDialog.show(getActivity(), "", "", true, false);
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

    private boolean checkAPAllInWhite() {
        if (this.mAllAppsList.size() == this.mWhiteAppsList.size()) {
            return true;
        }
        return false;
    }

    private boolean checkAPAllInDisauto() {
        if (this.mDisautoAppList.size() == 0) {
            return true;
        }
        return false;
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

    private void showAlertDialog(String message, OnClickListener positiveButtonListener, OnClickListener negativeButtonListener) {
        try {
            Builder builder = new Builder(getActivity());
            builder.setMessage(message);
            builder.setPositiveButton(C0321R.string.power_saving_dialog_ok, positiveButtonListener);
            builder.setNegativeButton(C0321R.string.fih_power_saving_request_dialog_cancel, negativeButtonListener);
            this.mDialog = builder.create();
            this.mDialog.setCanceledOnTouchOutside(true);
            this.mDialog.setCancelable(true);
            this.mDialog.show();
            this.mDialog.setOnKeyListener(new C03765());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
