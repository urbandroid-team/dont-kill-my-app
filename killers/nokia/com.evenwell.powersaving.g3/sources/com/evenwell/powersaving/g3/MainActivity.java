package com.evenwell.powersaving.g3;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.drawable.ColorDrawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.provider.Settings.Secure;
import android.text.format.Formatter;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import com.android.internal.app.IBatteryStats;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.EXTRA_NAME;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.LATEST_EVENT_EXTRA;
import com.evenwell.powersaving.g3.pushservice.PushServiceUtils.RESPONSE_CODE;
import com.evenwell.powersaving.g3.timeschedule.TimeScheduleUtils;
import com.evenwell.powersaving.g3.timeschedule.TimeScheduler;
import com.evenwell.powersaving.g3.utils.DisplayResolutionUtil;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.INTENT.FUNCTION;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.PARM;
import com.evenwell.powersaving.g3.utils.PSConst.DC.TIME.VALUE;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.PSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PSConst.SYSTEMUI_EXECUTE_PS.INTENT;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.evenwell.powersaving.g3.utils.PSUtils;
import com.evenwell.powersaving.g3.utils.PowerSaverInfoDialog;
import com.evenwell.powersaving.g3.utils.ProjectInfo;
import com.evenwell.powersaving.g3.utils.ScreenResolutionUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net2.lingala.zip4j.util.InternalZipConstants;

public class MainActivity extends PreferenceActivity implements OnPreferenceChangeListener {
    private static final boolean DBG = true;
    private static final int INIT_FINISH = 2001;
    private static boolean InInitStatus = false;
    private static final String KEY_DENSITY = "ScreenResolution_density";
    private static String POWERSAVING_PACKAGENAME = "com.evenwell.powersaving.g3";
    private static final int RESOLUTION_REDUCE_RATE = 75;
    private static String TAG = TAG.PSLOG;
    private static Context mContext;
    private static Handler mHandler = new C03136();
    private static ProgressDialog mProgressDialog;
    private long DELAY_TIME = VALUE.SCREEN_OFF_WAIT_TIME;
    private int HOURS_IN_ONE_DAY = 24;
    private final String KEY_DISPLAY_RESOLUTION = "key_display_resolution";
    private final String KEY_GRAYSCALE_MODE = "key_grayscale_mode";
    private final String KEY_POWER_SAVER = "key_power_saver";
    private final String KEY_REDUCE_RESOLUTION = "key_reduce_resolution";
    private final String KEY_TIME_SCHEDULE_END_TIME = "key_time_schedule_end_time";
    private final String KEY_TIME_SCHEDULE_START_TIME = "key_time_schedule_start_time";
    private final String KEY_TIME_SCHEDULE_SWITCH = "key_time_schedule_switch";
    private final long MS_IN_ONE_DAY = 86400000;
    private int SAVINGTIME_ARRAY_AMOUNT = 20;
    private ImageView batteryImageView = null;
    private String cacheFilePath;
    private IBatteryStats mBatteryInfo = null;
    private IntentFilter mBatteryIntentFilter = null;
    private BroadcastReceiver mBatteryReceiver = new C03125();
    private int mDefaultDensity = 0;
    private Preference mDisplayResolutionPref;
    private Preference mEndTimePref;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private List<Long> mExtremeModeSaveTimeList = new ArrayList();
    private ContentObserver mGrayscaleModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.i(MainActivity.TAG, "[MainActivity] mGrayscaleModeObserver onChange");
            boolean isEnabled = LpmUtils.getSimulateColorSpaceMode(MainActivity.mContext);
            if (isEnabled != MainActivity.this.mGrayscaleModeSwitch.isChecked()) {
                Log.i(MainActivity.TAG, "[MainActivity] mGrayscaleModeSwitch.setChecked : " + isEnabled);
                MainActivity.this.mGrayscaleModeSwitch.setChecked(isEnabled);
            }
        }
    };
    private SwitchPreference mGrayscaleModeSwitch;
    private IntentFilter mIntentFilter = null;
    boolean mIsApplyGrayscaleTile = false;
    private boolean mIsTimeScheduleEnabled = false;
    private int mModeSelection = 1;
    private BroadcastReceiver mReceiver = new C03114();
    private SwitchPreference mReduceResolutionSwitch;
    private Preference mStartTimePref;
    private SwitchPreference mSwitchBar;
    private SwitchPreference mTimeScheduleSwitch;
    private TimeScheduler mTimeScheduler;
    private IWindowManager mWm;
    private BatteryManager mbm = null;
    private Long mtotalSaveTimeInExtremeMode = Long.valueOf(0);
    private Long mtotalSaveTimeInNormalMode = Long.valueOf(0);
    private TextView timeRemainTextView = null;

    /* renamed from: com.evenwell.powersaving.g3.MainActivity$1 */
    class C03071 implements OnPreferenceClickListener {
        C03071() {
        }

        public boolean onPreferenceClick(Preference preference) {
            MainActivity.this.showStartTimePickerDialog();
            return true;
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.MainActivity$2 */
    class C03082 implements OnPreferenceClickListener {
        C03082() {
        }

        public boolean onPreferenceClick(Preference preference) {
            MainActivity.this.showEndTimePickerDialog();
            return true;
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.MainActivity$3 */
    class C03093 implements OnPreferenceClickListener {
        C03093() {
        }

        public boolean onPreferenceClick(Preference preference) {
            MainActivity.this.startActivity(new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$ScreenResolutionActivity")));
            return true;
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.MainActivity$4 */
    class C03114 extends BroadcastReceiver {
        private long PrevReceiveTime = 0;

        /* renamed from: com.evenwell.powersaving.g3.MainActivity$4$1 */
        class C03101 implements Runnable {
            C03101() {
            }

            public void run() {
                MainActivity.this.closeProgressDialog();
            }
        }

        C03114() {
        }

        public void onReceive(Context context, Intent intent) {
            boolean z = false;
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(ACTION.ACTION_LPM_STILL_SETTING)) {
                    boolean mStillSetting = intent.getBooleanExtra(EXTRA.LPM_STILL_SETTING, false);
                    Log.i(MainActivity.TAG, "[MainActivity] mReceiver()-ACTION_POWER_SAVING_STILL_SETTING [Control UI] mStillSetting=" + mStillSetting);
                    if (!mStillSetting) {
                        MainActivity.mHandler.postDelayed(new C03101(), 1500);
                    }
                    MainActivity mainActivity = MainActivity.this;
                    if (!mStillSetting) {
                        z = true;
                    }
                    mainActivity.UpdateUIActive(z);
                } else if (action.equals(INTENT.ACTION.ACTION_SYSTEMUI_EXECUTE)) {
                    MainActivity.this.UpdateUIActive(PowerSavingUtils.GetPowerSavingModeEnable(MainActivity.mContext));
                }
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.MainActivity$5 */
    class C03125 extends BroadcastReceiver {
        private long PrevReceiveTime = 0;

        C03125() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - this.PrevReceiveTime < MainActivity.this.DELAY_TIME) {
                        Log.i(MainActivity.TAG, "[MainActivity]  nowTime : " + nowTime + " PrevReceiveTime : " + this.PrevReceiveTime + " diff " + (nowTime - this.PrevReceiveTime));
                        return;
                    }
                    this.PrevReceiveTime = nowTime;
                    Log.i(MainActivity.TAG, "[MainActivity] Intent.ACTION_BATTERY_CHANGED");
                    int mPlugged = intent.getIntExtra("plugged", -1);
                    int mStatus = intent.getIntExtra("status", -1);
                    int batteryLevel = intent.getIntExtra("level", 0);
                    Log.i(MainActivity.TAG, "[MainActivity] mPlugged = " + mPlugged + ", mStatus = " + mStatus);
                    MainActivity.this.updateBatteryImageView(batteryLevel);
                    if (mPlugged != 1 && mPlugged != 2 && mPlugged != 4) {
                        MainActivity.this.setTimeRemainOnUI();
                    } else if (mStatus == 2 || mStatus == 5) {
                        MainActivity.this.mSwitchBar.setEnabled(false);
                    }
                } else if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action)) {
                    Log.i(MainActivity.TAG, "[MainActivity] Intent.ACTION_POWER_CONNECTED");
                    MainActivity.this.timeRemainTextView.setText("");
                    MainActivity.this.mSwitchBar.setEnabled(false);
                } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                    Log.i(MainActivity.TAG, "[MainActivity] Intent.ACTION_POWER_DISCONNECTED");
                    MainActivity.this.setTimeRemainOnUI();
                    MainActivity.this.mSwitchBar.setEnabled(true);
                }
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.MainActivity$6 */
    static class C03136 extends Handler {
        C03136() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2001:
                    MainActivity.InInitStatus = false;
                    Log.i("[PowerSavingAppG3]", "MainActivity WaitInitFinish() -receive INIT_FINISH");
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.MainActivity$7 */
    class C03147 implements OnTimeSetListener {
        C03147() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String time = String.format("%02d", new Object[]{Integer.valueOf(hourOfDay)}) + ":" + String.format("%02d", new Object[]{Integer.valueOf(minute)});
            MainActivity.this.mStartTimePref.setSummary(time);
            TimeScheduleUtils.setTimeScheduleStartTime(MainActivity.mContext, time);
            MainActivity.this.mTimeScheduler.setStartAlarm();
            PowerSavingUtils.setStringItemToSelfDB(MainActivity.mContext, PowerSavingController.THE_LATEST_EVENT_KEY, LATEST_EVENT_EXTRA.TIME_SCHEDULE);
            MainActivity.this.handleApplyOrDisablePowerSaving_TimeSchedule(MainActivity.this.mModeSelection);
            MainActivity.this.updateEndTimePrefSummary(TimeScheduleUtils.getTimeScheduleEndTime(MainActivity.mContext));
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.MainActivity$8 */
    class C03158 implements OnTimeSetListener {
        C03158() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String time = String.format("%02d", new Object[]{Integer.valueOf(hourOfDay)}) + ":" + String.format("%02d", new Object[]{Integer.valueOf(minute)});
            TimeScheduleUtils.setTimeScheduleEndTime(MainActivity.mContext, time);
            MainActivity.this.mTimeScheduler.setEndAlarm();
            PowerSavingUtils.setStringItemToSelfDB(MainActivity.mContext, PowerSavingController.THE_LATEST_EVENT_KEY, LATEST_EVENT_EXTRA.TIME_SCHEDULE);
            MainActivity.this.handleApplyOrDisablePowerSaving_TimeSchedule(MainActivity.this.mModeSelection);
            MainActivity.this.updateEndTimePrefSummary(time);
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.MainActivity$9 */
    class C03179 implements Runnable {
        C03179() {
        }

        public void run() {
            try {
                Thread.sleep(1000);
                if (MainActivity.this.mBatteryInfo.computeBatteryTimeRemaining() != -1) {
                    final long batteryTimeRemaining = MainActivity.this.mBatteryInfo.computeBatteryTimeRemaining();
                    Log.i(MainActivity.TAG, "[MainActivity]: mBatteryInfo.computeBatteryTimeRemaining() = " + batteryTimeRemaining + ",getTimeFormateString(mBatteryInfo.computeBatteryTimeRemaining()) : " + MainActivity.this.getTimeFormateString(batteryTimeRemaining));
                    MainActivity.mHandler.post(new Runnable() {
                        public void run() {
                            try {
                                if (batteryTimeRemaining > 0) {
                                    MainActivity.this.timeRemainTextView.setText(MainActivity.this.getTimeFormateString(batteryTimeRemaining));
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void onCreate(Bundle icicle) {
        getWindow().setUiOptions(1);
        super.onCreate(icicle);
        Log.i(TAG, "[MainActivity]: onCreate() ");
        mContext = this;
        setContentView(C0321R.layout.main_activity);
        if (PSUtils.isCNModel(mContext)) {
            setRequestedOrientation(1);
        }
        getActionBar().setTitle(C0321R.string.fih_power_saving_power_saver_title_2);
        getActionBar().setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(C0321R.color.battery_bg_color)));
        getActionBar().setElevation(0.0f);
        addPreferencesFromResource(C0321R.xml.main_light);
        getListView().addHeaderView(View.inflate(mContext, C0321R.layout.battery_status_layout, null));
        this.mSwitchBar = (SwitchPreference) findPreference("key_power_saver");
        this.mSwitchBar.setOnPreferenceChangeListener(this);
        if (PowerSavingUtils.isNeedChangeWlan(mContext)) {
            this.mSwitchBar.setSummary(getResources().getString(C0321R.string.fih_power_saving_power_saver_summary_wlan));
        } else {
            this.mSwitchBar.setSummary(getResources().getString(C0321R.string.fih_power_saving_power_saver_summary));
        }
        this.mReduceResolutionSwitch = (SwitchPreference) findPreference("key_reduce_resolution");
        getPreferenceScreen().removePreference(this.mReduceResolutionSwitch);
        this.mGrayscaleModeSwitch = (SwitchPreference) findPreference("key_grayscale_mode");
        this.mIsApplyGrayscaleTile = mContext.getResources().getBoolean(C0321R.bool.apply_grayscale_mode_tile);
        if (this.mIsApplyGrayscaleTile && PowerSavingUtils.isSupportAmoledDisplay()) {
            Log.d(TAG, "[MainActivity] ApplyGrayscaleTile & SupportAmoledDisplay");
            this.mGrayscaleModeSwitch.setOnPreferenceChangeListener(this);
            mContext.getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_display_daltonizer_enabled"), true, this.mGrayscaleModeObserver);
        } else {
            getPreferenceScreen().removePreference(this.mGrayscaleModeSwitch);
        }
        this.mWm = Stub.asInterface(ServiceManager.checkService("window"));
        try {
            this.mDefaultDensity = this.mWm.getInitialDisplayDensity(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.mTimeScheduler = new TimeScheduler(mContext);
        this.mTimeScheduleSwitch = (SwitchPreference) findPreference("key_time_schedule_switch");
        this.mTimeScheduleSwitch.setOnPreferenceChangeListener(this);
        this.mIsTimeScheduleEnabled = TimeScheduleUtils.isTimeScheduleEnabled(mContext);
        this.mTimeScheduleSwitch.setChecked(this.mIsTimeScheduleEnabled);
        this.mStartTimePref = findPreference("key_time_schedule_start_time");
        this.mStartTimePref.setTitle(PowerSavingUtils.getSettingsResourceStringValue(mContext, "zen_mode_start_time"));
        this.mStartTimePref.setSummary(TimeScheduleUtils.getTimeScheduleStartTime(mContext));
        this.mStartTimePref.setOnPreferenceClickListener(new C03071());
        this.mEndTimePref = findPreference("key_time_schedule_end_time");
        this.mEndTimePref.setTitle(PowerSavingUtils.getSettingsResourceStringValue(mContext, "zen_mode_end_time"));
        updateEndTimePrefSummary(TimeScheduleUtils.getTimeScheduleEndTime(mContext));
        this.mEndTimePref.setOnPreferenceClickListener(new C03082());
        this.mDisplayResolutionPref = findPreference("key_display_resolution");
        boolean hasScreenResolutionFeature = mContext.getPackageManager().hasSystemFeature("cust.display.screenresolution.settings");
        Log.i(TAG, "[MainActivity]: hasScreenResolutionFeature = " + hasScreenResolutionFeature);
        if (hasScreenResolutionFeature) {
            String drTitle = PowerSavingUtils.getSettingsResourceStringValue(mContext, "display_screen_resolution");
            String drSummary = PowerSavingUtils.getSettingsResourceStringValue(mContext, "screen_resolution_large_summary");
            if (drTitle.equals("") || drSummary.equals("")) {
                getPreferenceScreen().removePreference(this.mDisplayResolutionPref);
            } else {
                this.mDisplayResolutionPref.setTitle(drTitle);
                if (DisplayResolutionUtil.getCurrentSizeRatio(mContext).equals(DisplayResolutionUtil.SCREEN_RESOLUTION_VALUE_LARGE)) {
                    this.mDisplayResolutionPref.setSummary(DisplayResolutionUtil.getDefaultString(mContext) + drSummary);
                } else {
                    this.mDisplayResolutionPref.setSummary(DisplayResolutionUtil.getSmallString(mContext));
                }
                this.mDisplayResolutionPref.setOnPreferenceClickListener(new C03093());
            }
        } else {
            getPreferenceScreen().removePreference(this.mDisplayResolutionPref);
        }
        boolean isSupportDozeMode = PowerSavingUtils.isSupportDozeMode(mContext);
        Log.i(TAG, "[MainActivity]: isSupportDozeMode() : " + isSupportDozeMode);
        if (isSupportDozeMode && PowerSavingUtils.GetPWEnable(mContext)) {
            PowerSavingUtils.setBooleanItemToDB(mContext, PSDB.PW, false);
        }
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction(ACTION.ACTION_LPM_STILL_SETTING);
        this.mIntentFilter.addAction(INTENT.ACTION.ACTION_SYSTEMUI_EXECUTE);
        registerReceiver(this.mReceiver, this.mIntentFilter);
        this.mBatteryIntentFilter = new IntentFilter();
        this.mBatteryIntentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mBatteryIntentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        this.mBatteryIntentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        this.cacheFilePath = mContext.getCacheDir().getPath() + InternalZipConstants.ZIP_FILE_SEPARATOR;
        if (this.batteryImageView == null) {
            this.batteryImageView = (ImageView) findViewById(C0321R.id.batteryImageView);
        }
        if (this.timeRemainTextView == null) {
            this.timeRemainTextView = (TextView) findViewById(C0321R.id.timeRemain);
        }
        this.mbm = (BatteryManager) getSystemService("batterymanager");
        String[] extremeModeSaveTimeArray = PowerSavingUtils.getExtremeModeSaveTimeList();
        if (extremeModeSaveTimeArray != null) {
            for (String saveTime : extremeModeSaveTimeArray) {
                try {
                    long iTime = Long.parseLong(saveTime);
                    this.mtotalSaveTimeInExtremeMode = Long.valueOf(this.mtotalSaveTimeInExtremeMode.longValue() + (5 * iTime));
                    this.mExtremeModeSaveTimeList.add(Long.valueOf(iTime));
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        }
        checkSaveTimeInProductConfig();
    }

    public void onResume() {
        super.onResume();
        Log.i(TAG, "[MainActivity]: onResume() ");
        getActionBar().setDisplayOptions(16, 16);
        this.mSwitchBar.setChecked(PowerSavingUtils.GetPowerSavingModeEnable(mContext));
        registerReceiver(this.mBatteryReceiver, this.mBatteryIntentFilter);
        UpdateUIActive(PowerSavingUtils.GetPowerSavingModeEnable(mContext));
        updateBatteryImageView(this.mbm.getIntProperty(4));
        if (this.mBatteryInfo == null) {
            this.mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        }
        setTimeRemainOnUI();
        if (PowerSavingUtils.isCharging(mContext)) {
            this.mSwitchBar.setEnabled(false);
        } else {
            this.mSwitchBar.setEnabled(true);
        }
        setStartEndTimeViewEnabled(this.mIsTimeScheduleEnabled);
        if (this.mIsApplyGrayscaleTile && PowerSavingUtils.isSupportAmoledDisplay()) {
            this.mGrayscaleModeSwitch.setChecked(LpmUtils.getSimulateColorSpaceMode(mContext));
        }
    }

    private void setStartEndTimeViewEnabled(boolean enable) {
        this.mStartTimePref.setEnabled(enable);
        this.mEndTimePref.setEnabled(enable);
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(this.mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (this.mIsApplyGrayscaleTile && PowerSavingUtils.isSupportAmoledDisplay()) {
                mContext.getContentResolver().unregisterContentObserver(this.mGrayscaleModeObserver);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void onStop() {
        super.onStop();
        getActionBar().setDisplayOptions(0, 16);
        getActionBar().setCustomView(null);
    }

    public void onSwitchOn() {
        Log.i(TAG, "onSwitchOn");
        WaitInitFinish(2001);
        handleApplyPowerSaving(this.mModeSelection);
    }

    public void onSwitchOff() {
        Log.i(TAG, "onSwitchOff");
        WaitInitFinish(2001);
        handleDisablePowerSaving();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == this.mSwitchBar) {
            if (((Boolean) objValue).booleanValue()) {
                showProgressDialog();
                onSwitchOn();
            } else {
                showProgressDialog();
                onSwitchOff();
                setTimeRemainOnUI();
            }
        } else if (preference == this.mTimeScheduleSwitch) {
            if (((Boolean) objValue).booleanValue()) {
                TimeScheduleUtils.setTimeScheduleEnabled(mContext, true);
                this.mIsTimeScheduleEnabled = true;
                this.mTimeScheduler.setStartAlarm();
                this.mTimeScheduler.setEndAlarm();
                PowerSavingUtils.setStringItemToSelfDB(mContext, PowerSavingController.THE_LATEST_EVENT_KEY, LATEST_EVENT_EXTRA.TIME_SCHEDULE);
                handleApplyPowerSaving_TimeSchedule(this.mModeSelection);
            } else {
                TimeScheduleUtils.setTimeScheduleEnabled(mContext, false);
                this.mIsTimeScheduleEnabled = false;
                this.mTimeScheduler.cancelAlarm();
                handleDisablePowerSaving_TimeSchedule();
            }
            setStartEndTimeViewEnabled(this.mIsTimeScheduleEnabled);
        } else if (preference == this.mReduceResolutionSwitch) {
            if (((Boolean) objValue).booleanValue()) {
                saveCurrentDensity();
                ScreenResolutionUtil.changeResoultionByRate(mContext, RESOLUTION_REDUCE_RATE);
            } else {
                int density = readLastTimeDensity();
                ScreenResolutionUtil.resetDisplaySize();
                ScreenResolutionUtil.setDensity(density);
            }
        } else if (preference == this.mGrayscaleModeSwitch) {
            if (((Boolean) objValue).booleanValue()) {
                LpmUtils.setMonoChromacyEnabled(mContext, SWITCHER.ON);
            } else {
                LpmUtils.setMonoChromacyEnabled(mContext, SWITCHER.OFF);
            }
        }
        return true;
    }

    private void saveCurrentDensity() {
        try {
            int density = this.mWm.getBaseDisplayDensity(0);
            Log.i(TAG, "savePreference : ScreenResolution_density = " + density);
            PowerSavingUtils.SetPreferencesStatus(mContext, KEY_DENSITY, Integer.toString(density));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int readLastTimeDensity() {
        int density = 0;
        if (PowerSavingUtils.contaionPreferences(mContext, KEY_DENSITY)) {
            String value = PowerSavingUtils.GetPreferencesStatusString(mContext, KEY_DENSITY);
            if (value != null) {
                density = Integer.parseInt(value);
            }
            Log.i(TAG, "readPreference : ScreenResolution_density = " + density);
        }
        if (density == 0) {
            return this.mDefaultDensity;
        }
        return density;
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            try {
                mProgressDialog = ProgressDialog.show(mContext, "", "", true, false);
                mProgressDialog.setContentView(C0321R.layout.custom_progress_dialog);
                mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                mProgressDialog.setCancelable(false);
                mProgressDialog.setCanceledOnTouchOutside(false);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        Log.i(TAG, "[MainActivity] progress dialog already exist.");
    }

    protected void onPause() {
        super.onPause();
        closeProgressDialog();
        try {
            unregisterReceiver(this.mBatteryReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateUIActive(boolean isEnable) {
        if (isEnable) {
            this.mSwitchBar.setChecked(PowerSavingUtils.getBooleanItemFromoDB(mContext, PSDB.MAIN));
        }
        boolean mStillSetting = !isEnable;
        boolean mPowerSavingModeEnable = PowerSavingUtils.GetPowerSavingModeEnable(mContext);
        if (!mStillSetting) {
            if (!mPowerSavingModeEnable) {
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void WaitInitFinish(int WhyFinish) {
        InInitStatus = true;
        int mDelayTime = RESPONSE_CODE.internal_erorr;
        String sDelayTime = ProjectInfo.getConfigFromExternal(mContext, PSDB.START_SERVICE_DELAY_TIME);
        if (sDelayTime != null) {
            mDelayTime = Integer.valueOf(sDelayTime).intValue();
        } else {
            sDelayTime = ProjectInfo.getConfigString(mContext, C0321R.string.powersaving_start_service_delay_time);
            if (sDelayTime != null) {
                mDelayTime = Integer.valueOf(sDelayTime).intValue();
            }
        }
        Log.i("[PowerSavingAppG3]", "MainActivity WaitInitFinish() waittime = " + mDelayTime);
        mHandler.removeMessages(WhyFinish);
        Message msg1 = Message.obtain(mHandler);
        if (WhyFinish == 2001) {
            msg1.what = WhyFinish;
        }
        mHandler.sendMessageDelayed(msg1, (long) mDelayTime);
    }

    private void handleApplyPowerSaving(int mode) {
        Intent intent = new Intent(mContext, PowerSavingController.class);
        intent.putExtra(FUNCTION.EXTRA.POWERSAVER_ENABLE, 1);
        intent.putExtra(EXTRA_NAME.MODE, mode);
        intent.putExtra(EXTRA_NAME.LATEST_EVENT, LATEST_EVENT_EXTRA.MANUAL);
        startService(intent);
        setTimeRemainOnUI();
    }

    private void handleDisablePowerSaving() {
        Intent intent = new Intent(mContext, PowerSavingController.class);
        intent.putExtra(FUNCTION.EXTRA.POWERSAVER_ENABLE, 0);
        intent.putExtra(EXTRA_NAME.LATEST_EVENT, LATEST_EVENT_EXTRA.MANUAL);
        startService(intent);
    }

    private void handleApplyPowerSaving_TimeSchedule(int mode) {
        if (PowerSavingUtils.isCharging(mContext)) {
            Log.i(TAG, "[MainActivity] isCharging = true -> do nothing");
        } else if (this.mTimeScheduler.isTimeInterval()) {
            Log.i(TAG, "[MainActivity] current time is IN time schedule interval");
            Log.i(TAG, "[MainActivity] turn on power saving, mode = " + mode);
            if (!PowerSavingUtils.GetPowerSavingModeEnable(mContext)) {
                showProgressDialog();
            }
            Intent intent = new Intent(mContext, PowerSavingController.class);
            intent.putExtra(FUNCTION.EXTRA.POWERSAVER_ENABLE, 1);
            intent.putExtra(EXTRA_NAME.MODE, mode);
            intent.putExtra(EXTRA_NAME.LATEST_EVENT, LATEST_EVENT_EXTRA.TIME_SCHEDULE);
            startService(intent);
            setTimeRemainOnUI();
        } else {
            Log.i(TAG, "[MainActivity] current time is NOT in time schedule interval");
            if (PowerSavingUtils.GetPowerSavingModeEnable(mContext)) {
                Log.i(TAG, "[MainActivity] power saving is already ON by manual");
                PowerSavingUtils.SetPreferencesStatus(mContext, PARM.KEY_PS_KEEP_MANUAL_ON, true);
            }
        }
    }

    private void handleDisablePowerSaving_TimeSchedule() {
        if (!PowerSavingUtils.GetPowerSavingModeEnable(mContext)) {
            Log.i(TAG, "[MainActivity] power saving already disabled");
        } else if (!TimeScheduleUtils.getTheLatestEventFromDB(mContext).equals(LATEST_EVENT_EXTRA.TIME_SCHEDULE)) {
            Log.i(TAG, "[MainActivity] power saving is NOT triggered by time schedule");
        } else if (PowerSavingUtils.GetPreferencesStatus(mContext, PARM.KEY_PS_KEEP_MANUAL_ON)) {
            Log.i(TAG, "[MainActivity] power saving keep manual ON");
            PowerSavingUtils.SetPreferencesStatus(mContext, PARM.KEY_PS_KEEP_MANUAL_ON, false);
        } else {
            Log.i(TAG, "[MainActivity] turn off power saving");
            if (PowerSavingUtils.GetPowerSavingModeEnable(mContext)) {
                showProgressDialog();
            }
            Intent intent = new Intent(mContext, PowerSavingController.class);
            intent.putExtra(FUNCTION.EXTRA.POWERSAVER_ENABLE, 0);
            startService(intent);
            setTimeRemainOnUI();
        }
    }

    private void handleApplyOrDisablePowerSaving_TimeSchedule(int mode) {
        if (PowerSavingUtils.isCharging(mContext)) {
            Log.i(TAG, "[MainActivity] isCharging = true -> do nothing");
        } else if (this.mTimeScheduler.isTimeInterval()) {
            Log.i(TAG, "[MainActivity] current time is IN time schedule interval");
            Log.i(TAG, "[MainActivity] turn on power saving, mode = " + mode);
            if (!PowerSavingUtils.GetPowerSavingModeEnable(mContext)) {
                showProgressDialog();
            }
            intent = new Intent(mContext, PowerSavingController.class);
            intent.putExtra(FUNCTION.EXTRA.POWERSAVER_ENABLE, 1);
            intent.putExtra(EXTRA_NAME.MODE, mode);
            intent.putExtra(EXTRA_NAME.LATEST_EVENT, LATEST_EVENT_EXTRA.TIME_SCHEDULE);
            startService(intent);
            setTimeRemainOnUI();
        } else {
            Log.i(TAG, "[MainActivity] current time is NOT in time schedule interval");
            if (!PowerSavingUtils.GetPowerSavingModeEnable(mContext)) {
                Log.i(TAG, "[MainActivity] power saving already disabled");
            } else if (!TimeScheduleUtils.getTheLatestEventFromDB(mContext).equals(LATEST_EVENT_EXTRA.TIME_SCHEDULE)) {
                Log.i(TAG, "[MainActivity] power saving is NOT triggered by time schedule");
            } else if (PowerSavingUtils.GetPreferencesStatus(mContext, PARM.KEY_PS_KEEP_MANUAL_ON)) {
                Log.i(TAG, "[MainActivity] power saving keep manual ON");
            } else {
                Log.i(TAG, "[MainActivity] turn off power saving");
                if (PowerSavingUtils.GetPowerSavingModeEnable(mContext)) {
                    showProgressDialog();
                }
                intent = new Intent(mContext, PowerSavingController.class);
                intent.putExtra(FUNCTION.EXTRA.POWERSAVER_ENABLE, 0);
                startService(intent);
                setTimeRemainOnUI();
            }
        }
    }

    private void showStartTimePickerDialog() {
        Calendar c = Calendar.getInstance();
        Date time = TimeScheduleUtils.getTimeFromDB(mContext, "start");
        if (time != null) {
            c.setTime(time);
        }
        new TimePickerDialog(this, new C03147(), c.get(11), c.get(12), true).show();
    }

    private void showEndTimePickerDialog() {
        Calendar c = Calendar.getInstance();
        Date time = TimeScheduleUtils.getTimeFromDB(mContext, "end");
        if (time != null) {
            c.setTime(time);
        }
        new TimePickerDialog(this, new C03158(), c.get(11), c.get(12), true).show();
    }

    private void updateEndTimePrefSummary(String time) {
        if (this.mTimeScheduler.isStartEndTimeEql()) {
            String formatTimeStr = "";
            Resources SettingsRes = PowerSavingUtils.getSettingsResource(mContext);
            if (SettingsRes != null) {
                int resID = PowerSavingUtils.getSettingsResourceID(mContext, "zen_mode_end_time_next_day_summary_format");
                if (resID != -1) {
                    formatTimeStr = SettingsRes.getString(resID, new Object[]{time});
                }
            }
            this.mEndTimePref.setSummary(formatTimeStr);
            return;
        }
        this.mEndTimePref.setSummary(time);
    }

    private void updateBatteryImageView(int batteryLevel) {
        int resid = C0321R.drawable.ic_powersaving_battery_image1;
        if (batteryLevel == 0) {
            resid = C0321R.drawable.ic_powersaving_battery_image1;
        } else if (batteryLevel > 0 && batteryLevel < 10) {
            resid = C0321R.drawable.ic_powersaving_battery_image2;
        } else if (batteryLevel >= 10 && batteryLevel < 20) {
            resid = C0321R.drawable.ic_powersaving_battery_image3;
        } else if (batteryLevel >= 20 && batteryLevel < 30) {
            resid = C0321R.drawable.ic_powersaving_battery_image4;
        } else if (batteryLevel >= 30 && batteryLevel < 40) {
            resid = C0321R.drawable.ic_powersaving_battery_image5;
        } else if (batteryLevel >= 40 && batteryLevel < 50) {
            resid = C0321R.drawable.ic_powersaving_battery_image6;
        } else if (batteryLevel >= 50 && batteryLevel < 60) {
            resid = C0321R.drawable.ic_powersaving_battery_image7;
        } else if (batteryLevel >= 60 && batteryLevel < 70) {
            resid = C0321R.drawable.ic_powersaving_battery_image8;
        } else if (batteryLevel >= 70 && batteryLevel < 80) {
            resid = C0321R.drawable.ic_powersaving_battery_image9;
        } else if (batteryLevel >= 80 && batteryLevel < 90) {
            resid = C0321R.drawable.ic_powersaving_battery_image10;
        } else if (batteryLevel >= 90 && batteryLevel <= 100) {
            resid = C0321R.drawable.ic_powersaving_battery_image11;
        }
        this.batteryImageView.setImageResource(resid);
    }

    private String getTimeFormateString(long timeInMs) {
        return getString(C0321R.string.fih_power_saving_battery_Approx, new Object[]{Formatter.formatShortElapsedTime(mContext, timeInMs)});
    }

    private void setTimeRemainOnUI() {
        this.mExecutorService.execute(new C03179());
    }

    private void checkSaveTimeInProductConfig() {
        String[] extremeModeSaveTimeArray = PowerSavingUtils.getExtremeModeSaveTimeList();
        if (extremeModeSaveTimeArray == null) {
            showWarningDialog("[Save time] : Empty value exist(Extreme mode save time).");
        } else if (extremeModeSaveTimeArray.length != 20) {
            showWarningDialog("[Save time] : Amount of Extreme mode save time is wrong.\nPlease fill in 20 numbers.");
        } else {
            try {
                for (String str : extremeModeSaveTimeArray) {
                    Integer.parseInt(str);
                }
            } catch (NumberFormatException e) {
                showWarningDialog("[Save time] : Format of Extreme mode save time is wrong.\n(not all values not in numeric format).");
            }
        }
    }

    private void showWarningDialog(String errMsg) {
        Intent intent = new Intent("com.evenwell.powersaving.g3.POWER_SAVER_INFO_DIALOG");
        intent.putExtra(PowerSaverInfoDialog.POWER_SAVER_DIALOG_INFO, "Power saver product config error !\n" + errMsg);
        mContext.startActivity(intent);
    }
}
