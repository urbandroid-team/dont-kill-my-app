package com.evenwell.powersaving.g3.utils;

import android.content.Context;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.DCDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.PSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.PWDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.SSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.TSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

public class ProjectInfo {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;
    private static final String mPowerSavingDefaultCfgFile = FILENAME.POWER_SAVING_DEFAULT_EXTERNAL_CFG_FILE;
    private static final String mPowerSavingDefaultProductCfgFile = FILENAME.POWER_SAVING_DEFAULT_EXTERNAL_PRODUCT_CFG_FILE;

    public static void ProjectInfo(Context ctx) {
        try {
            if (!ReadExternalDefaultCfgFile(ctx)) {
                Log.i(TAG, "ProjectInfo: ReadExternalDefaultCfgFile fail,so load apk internel cfg value ");
                ReadInternalDefaultCfgFile(ctx);
            }
        } catch (Exception e) {
            Log.i(TAG, "ProjectInfo: error ", e);
        }
    }

    private static boolean ReadExternalDefaultCfgFile(Context ctx) {
        boolean mFailRead = false;
        Element root = GetCfgXmlFile();
        Log.i(TAG, "ProjectInfo: ReadExternalDefaultCfgFile()");
        if (root != null) {
            WriteConfigFromExternaltoDB(ctx, root, PSDB.MAIN);
            WriteConfigFromExternaltoDB(ctx, root, PSDB.CPU_POLICY);
            WriteConfigFromExternaltoDB(ctx, root, PSDB.SCREEN_POLICY);
            WriteConfigFromExternaltoDB(ctx, root, PSDB.DATA_CONNECTION);
            WriteConfigFromExternaltoDB(ctx, root, PSDB.PW);
            WriteConfigFromExternaltoDB(ctx, root, PSDB.LPM);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.BEGIN);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.WIFI);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.BT);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.GPS);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.MOBILE_DATA);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.D3_SOUND);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.ANIMATION);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.VIBRATION);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.BACKGROUND_DATA);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.SCREEN_TIMEOUT);
            WriteConfigFromExternaltoDB(ctx, root, DCDB.PW_DATA_ALWAYSON);
            WriteConfigFromExternaltoDB(ctx, root, DCDB.PW_DATA_STARTTIME);
            WriteConfigFromExternaltoDB(ctx, root, DCDB.PW_DATA_ENDTIME);
            WriteConfigFromExternaltoDB(ctx, root, DCDB.PW_DATA_WHITELIST);
            WriteConfigFromExternaltoDB(ctx, root, PWDB.PW_PW_TIME);
            WriteConfigFromExternaltoDB(ctx, root, PWDB.PW_PW_WHITELIST);
            WriteConfigFromExternaltoDB(ctx, root, PWDB.PW_PW_HIDELIST);
            WriteConfigFromExternaltoDB(ctx, root, PWDB.PW_PW_REMOVELIST);
            WriteConfigFromExternaltoDB(ctx, root, PSDB.SS);
            WriteConfigFromExternaltoDB(ctx, root, SSDB.WIFI_TIMEOUT);
            WriteConfigFromExternaltoDB(ctx, root, SSDB.HOTSPOT_TIMEOUT);
            WriteConfigFromExternaltoDB(ctx, root, SSDB.WIFI);
            WriteConfigFromExternaltoDB(ctx, root, SSDB.HOTSPOT);
            WriteConfigFromExternaltoDB(ctx, root, DCDB.PW_DATA_DETECTTIME);
            WriteConfigFromExternaltoDB(ctx, root, PSDB.SERVICE_DETECT);
            WriteConfigFromExternaltoDB(ctx, root, PSDB.SERVICE_DETECT_TIME);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.WIFI_HOTSPOT);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.BATTERY_INTENT_MIN_INTERVAL);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.MONOCHROMACY);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.AUTOSYNC);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.GLANCE);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.SCREEN_LIGHT);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.SMART_SWITCH);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.DATA_CONNECTION);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.EXTREME);
            WriteConfigFromExternaltoDB(ctx, root, "powersaving_db_power_saving_mode");
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.SCREEN_RESOLUTION);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.BAM);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.CPU_LIMIT);
            WriteConfigFromExternaltoDB(ctx, root, TSDB.TIME_SCHEDULE);
            WriteConfigFromExternaltoDB(ctx, root, TSDB.TIME_SCHEDULE_MODE);
            WriteConfigFromExternaltoDB(ctx, root, TSDB.TIME_SCHEDULE_START_TIME);
            WriteConfigFromExternaltoDB(ctx, root, TSDB.TIME_SCHEDULE_END_TIME);
            WriteConfigFromExternaltoDB(ctx, root, LPMDB.BATTERY_SAVER);
        } else {
            Log.i(TAG, "ProjectInfo: ReadExternalDefaultCfgFile() [root ==null] ");
            mFailRead = true;
        }
        if (mFailRead) {
            return false;
        }
        return true;
    }

    private static Element GetCfgXmlFile() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(new File(mPowerSavingDefaultCfgFile))).getDocumentElement();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "ProjectInfo: GetCfgXmlFile() [file not found]");
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private static Element GetProductCfgXmlFile() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(new File(mPowerSavingDefaultProductCfgFile))).getDocumentElement();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "ProjectInfo: GetProductCfgXmlFile() [file not found]");
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private static void ReadInternalDefaultCfgFile(Context ctx) {
        boolean mbValue;
        PowerSavingUtils.setBooleanItemToDB(ctx, PSDB.MAIN, getConfigBoolean(ctx, C0321R.bool.powersaving_db_main));
        if (PowerSavingUtils.HasCPUPolicyAPK(ctx)) {
            mbValue = getConfigBoolean(ctx, C0321R.bool.powersaving_db_cpu_policy);
        } else {
            mbValue = false;
        }
        PowerSavingUtils.setBooleanItemToDB(ctx, PSDB.CPU_POLICY, mbValue);
        if (PowerSavingUtils.isSupportScreenPolicy(ctx)) {
            mbValue = getConfigBoolean(ctx, C0321R.bool.powersaving_db_screen_policy);
        } else {
            mbValue = false;
        }
        PowerSavingUtils.setBooleanItemToDB(ctx, PSDB.SCREEN_POLICY, mbValue);
        PowerSavingUtils.setBooleanItemToDB(ctx, PSDB.DATA_CONNECTION, getConfigBoolean(ctx, C0321R.bool.powersaving_db_data_connection));
        if (PowerSavingUtils.isSupportDozeMode(ctx)) {
            mbValue = false;
        } else {
            mbValue = getConfigBoolean(ctx, C0321R.bool.powersaving_db_periodic_wakeup);
        }
        PowerSavingUtils.setBooleanItemToDB(ctx, PSDB.PW, mbValue);
        PowerSavingUtils.setBooleanItemToDB(ctx, PSDB.LPM, getConfigBoolean(ctx, C0321R.bool.powersaving_db_lpm));
        if (PowerSavingUtils.isSupportSmartSwitch()) {
            mbValue = getConfigBoolean(ctx, C0321R.bool.powersaving_db_ss);
        } else {
            mbValue = false;
        }
        PowerSavingUtils.setBooleanItemToDB(ctx, PSDB.SS, mbValue);
        PowerSavingUtils.setStringItemToDB(ctx, DCDB.PW_DATA_ALWAYSON, getConfigString(ctx, C0321R.string.powersaving_db_dc_alwayson));
        PowerSavingUtils.setStringItemToDB(ctx, DCDB.PW_DATA_STARTTIME, getConfigString(ctx, C0321R.string.powersaving_db_dc_start_time));
        PowerSavingUtils.setStringItemToDB(ctx, DCDB.PW_DATA_ENDTIME, getConfigString(ctx, C0321R.string.powersaving_db_dc_end_time));
        PowerSavingUtils.setStringItemToDB(ctx, DCDB.PW_DATA_WHITELIST, getConfigString(ctx, C0321R.string.powersaving_db_dc_white_list));
        PowerSavingUtils.setStringItemToDB(ctx, PWDB.PW_PW_TIME, getConfigString(ctx, C0321R.string.powersaving_db_pw_time));
        PowerSavingUtils.setStringItemToDB(ctx, PWDB.PW_PW_WHITELIST, getConfigString(ctx, C0321R.string.powersaving_db_pw_white_list));
        PowerSavingUtils.setStringItemToDB(ctx, PWDB.PW_PW_HIDELIST, getConfigString(ctx, C0321R.string.powersaving_db_pw_hide_list));
        PowerSavingUtils.setStringItemToDB(ctx, PWDB.PW_PW_REMOVELIST, getConfigString(ctx, C0321R.string.powersaving_db_pw_remove_list));
        PowerSavingUtils.setStringItemToDB(ctx, LPMDB.MOBILE_DATA, getConfigString(ctx, C0321R.string.powersaving_db_mobile_data));
        PowerSavingUtils.CheckG2ToG3Item(ctx, LPMDB.BEGIN);
        PowerSavingUtils.CheckG2ToG3Item(ctx, LPMDB.WIFI);
        PowerSavingUtils.CheckG2ToG3Item(ctx, LPMDB.BT);
        PowerSavingUtils.CheckG2ToG3Item(ctx, LPMDB.GPS);
        PowerSavingUtils.CheckG2ToG3Item(ctx, LPMDB.D3_SOUND);
        PowerSavingUtils.CheckG2ToG3Item(ctx, LPMDB.SCREEN_TIMEOUT);
        PowerSavingUtils.CheckG2ToG3Item(ctx, LPMDB.ANIMATION);
        PowerSavingUtils.CheckG2ToG3Item(ctx, LPMDB.VIBRATION);
        PowerSavingUtils.CheckG2ToG3Item(ctx, LPMDB.BACKGROUND_DATA);
        PowerSavingUtils.CheckG2ToG3Item(ctx, LPMDB.WIFI_HOTSPOT);
        PowerSavingUtils.setStringItemToDB(ctx, SSDB.WIFI_TIMEOUT, getConfigString(ctx, C0321R.string.powersaving_db_wifi_timeout));
        PowerSavingUtils.setStringItemToDB(ctx, SSDB.HOTSPOT_TIMEOUT, getConfigString(ctx, C0321R.string.powersaving_db_hotspot_timeout));
        PowerSavingUtils.setStringItemToDB(ctx, SSDB.WIFI, getConfigString(ctx, C0321R.string.powersaving_db_ss_wifi));
        PowerSavingUtils.setStringItemToDB(ctx, SSDB.HOTSPOT, getConfigString(ctx, C0321R.string.powersaving_db_ss_hotspot));
        PowerSavingUtils.setStringItemToDB(ctx, DCDB.PW_DATA_DETECTTIME, getConfigString(ctx, C0321R.string.powersaving_db_dc_detect_time));
        PowerSavingUtils.setStringItemToDB(ctx, PSDB.SERVICE_DETECT, getConfigString(ctx, C0321R.string.powersaving_db_service_detect));
        PowerSavingUtils.setStringItemToDB(ctx, PSDB.SERVICE_DETECT_TIME, getConfigString(ctx, C0321R.string.powersaving_db_service_detect_time));
        PowerSavingUtils.setStringItemToDB(ctx, LPMDB.BATTERY_INTENT_MIN_INTERVAL, getConfigString(ctx, C0321R.string.powersaving_db_battery_change_intent_min_interval));
    }

    public static boolean getConfigBoolean(Context ctx, int mKey) {
        return ctx.getResources().getBoolean(mKey);
    }

    public static String getConfigString(Context ctx, int mKey) {
        return ctx.getResources().getString(mKey);
    }

    public static String getConfigFromExternal(Context ctx, String mKey, String defaultValue) {
        String value = getConfigFromExternal(ctx, mKey);
        return value == null ? defaultValue : value;
    }

    public static String getConfigFromExternal(Context ctx, String mKey) {
        Element root = GetCfgXmlFile();
        if (root == null) {
            return null;
        }
        try {
            return root.getElementsByTagName(mKey).item(0).getFirstChild().getNodeValue();
        } catch (Exception e) {
            Log.i(TAG, "Can not get " + mKey + " value!!");
            return null;
        }
    }

    private static void WriteConfigFromExternaltoDB(Context ctx, Element root, String mKey) {
        String mValue;
        if (mKey.equals(PSDB.MAIN) || mKey.equals(PSDB.CPU_POLICY) || mKey.equals(PSDB.SCREEN_POLICY) || mKey.equals(PSDB.DATA_CONNECTION) || mKey.equals(PSDB.PW) || mKey.equals(PSDB.LPM) || mKey.equals(PSDB.SS)) {
            mValue = root.getElementsByTagName(mKey).item(0).getFirstChild().getNodeValue();
            if (mKey.equals(PSDB.CPU_POLICY)) {
                if (!PowerSavingUtils.HasCPUPolicyAPK(ctx)) {
                    PowerSavingUtils.setBooleanItemToDB(ctx, mKey, false);
                    return;
                }
            } else if (mKey.equals(PSDB.SCREEN_POLICY)) {
                if (!PowerSavingUtils.isSupportScreenPolicy(ctx)) {
                    PowerSavingUtils.setBooleanItemToDB(ctx, mKey, false);
                    return;
                }
            } else if (mKey.equals(PSDB.SS)) {
                if (!PowerSavingUtils.isSupportSmartSwitch()) {
                    PowerSavingUtils.setBooleanItemToDB(ctx, mKey, false);
                    return;
                }
            } else if (mKey.equals(PSDB.PW) && PowerSavingUtils.isSupportDozeMode(ctx)) {
                Log.i(TAG, "ProjectInfo: WriteConfigFromExternaltoDB SupportDozeMode set : " + mKey + " false ");
                PowerSavingUtils.setBooleanItemToDB(ctx, mKey, false);
                return;
            }
            if (mValue == null) {
                return;
            }
            if (Integer.valueOf(mValue).intValue() != 0) {
                PowerSavingUtils.setBooleanItemToDB(ctx, mKey, true);
            } else {
                PowerSavingUtils.setBooleanItemToDB(ctx, mKey, false);
            }
        } else if (mKey.equals(LPMDB.BEGIN) || mKey.equals(LPMDB.WIFI) || mKey.equals(LPMDB.BT) || mKey.equals(LPMDB.GPS) || mKey.equals(LPMDB.MOBILE_DATA) || mKey.equals(LPMDB.D3_SOUND) || mKey.equals(LPMDB.ANIMATION) || mKey.equals(LPMDB.VIBRATION) || mKey.equals(LPMDB.BACKGROUND_DATA) || mKey.equals(LPMDB.SCREEN_TIMEOUT) || mKey.equals(LPMDB.WIFI_HOTSPOT) || mKey.equals(LPMDB.AUTOSYNC) || mKey.equals(LPMDB.MONOCHROMACY) || mKey.equals(LPMDB.GLANCE) || mKey.equals(LPMDB.SCREEN_LIGHT) || mKey.equals(LPMDB.SMART_SWITCH) || mKey.equals(LPMDB.DATA_CONNECTION) || mKey.equals(LPMDB.EXTREME) || mKey.equals("powersaving_db_power_saving_mode") || mKey.equals(LPMDB.BAM) || mKey.equals(LPMDB.SCREEN_RESOLUTION) || mKey.equals(LPMDB.CPU_LIMIT)) {
            try {
                mValue = root.getElementsByTagName(mKey).item(0).getFirstChild().getNodeValue();
                Log.i(TAG, "ProjectInfo: WriteConfigFromExternaltoDB() Value =" + mValue);
                if (mValue != null) {
                    PowerSavingUtils.setStringItemToDB(ctx, mKey, mValue);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.i(TAG, "ProjectInfo: WriteConfigFromExternaltoDB(), external config null, key = " + mKey);
                mValue = getConfigString(ctx, PowerSavingUtils.DBItemtransferInternelConfigItem(mKey));
                if (mValue != null) {
                    Log.i(TAG, "ProjectInfo: WriteConfigFromExternaltoDB(), set internal value, key = " + mKey);
                    PowerSavingUtils.setStringItemToDB(ctx, mKey, mValue);
                }
            }
        } else if (mKey.equals(TSDB.TIME_SCHEDULE) || mKey.equals(TSDB.TIME_SCHEDULE_MODE) || mKey.equals(TSDB.TIME_SCHEDULE_START_TIME) || mKey.equals(TSDB.TIME_SCHEDULE_END_TIME)) {
            mValue = root.getElementsByTagName(mKey).item(0).getFirstChild().getNodeValue();
            if (mValue != null) {
                PowerSavingUtils.setStringItemToDB(ctx, mKey, mValue);
            }
        } else if (mKey.equals(DCDB.PW_DATA_ALWAYSON) || mKey.equals(DCDB.PW_DATA_STARTTIME) || mKey.equals(DCDB.PW_DATA_ENDTIME) || mKey.equals(PWDB.PW_PW_TIME) || mKey.equals(SSDB.WIFI_TIMEOUT) || mKey.equals(SSDB.HOTSPOT_TIMEOUT) || mKey.equals(SSDB.WIFI) || mKey.equals(SSDB.HOTSPOT) || mKey.equals(LPMDB.BATTERY_INTENT_MIN_INTERVAL) || mKey.equals(DCDB.PW_DATA_DETECTTIME) || mKey.equals(PSDB.SERVICE_DETECT) || mKey.equals(PSDB.SERVICE_DETECT_TIME)) {
            mValue = root.getElementsByTagName(mKey).item(0).getFirstChild().getNodeValue();
            if (mValue != null) {
                PowerSavingUtils.setStringItemToDB(ctx, mKey, mValue);
            }
        } else if (mKey.equals(DCDB.PW_DATA_WHITELIST) || mKey.equals(PWDB.PW_PW_WHITELIST) || mKey.equals(PWDB.PW_PW_HIDELIST) || mKey.equals(PWDB.PW_PW_REMOVELIST)) {
            try {
                mValue = root.getElementsByTagName(mKey).item(0).getFirstChild().getNodeValue();
                Log.i(TAG, "ProjectInfo: WriteConfigFromExternaltoDB() Value =" + mValue);
                PowerSavingUtils.setStringItemToDB(ctx, mKey, mValue);
            } catch (NullPointerException e2) {
                e2.printStackTrace();
                mValue = SYMBOLS.SPACE;
                Log.i(TAG, "ProjectInfo: WriteConfigFromExternaltoDB() Value = null and write null");
                PowerSavingUtils.setStringItemToDB(ctx, mKey, mValue);
            }
        } else if (mKey.equals(LPMDB.BATTERY_SAVER)) {
            mValue = getConfigString(ctx, PowerSavingUtils.DBItemtransferInternelConfigItem(mKey));
            if (mValue != null) {
                Log.i(TAG, "ProjectInfo: WriteConfigFromExternaltoDB(), set internal value, key = " + mKey);
                PowerSavingUtils.setStringItemToDB(ctx, mKey, mValue);
            }
        }
    }

    public static String GetProjectName() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(FILENAME.POWER_SAVING_GET_PROJECT_NAME), 256);
            String mProjectStr = reader.readLine();
            reader.close();
            return mProjectStr;
        } catch (IOException e) {
            return "";
        } catch (Throwable th) {
            reader.close();
        }
    }

    public static String GetDeviceSubVersion() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(FILENAME.POWER_SAVING_GET_DEVICE_SUBVERSION), 256);
            String mProjectStr = reader.readLine();
            reader.close();
            try {
                if (mProjectStr.contains("-")) {
                    String[] str = mProjectStr.split("-");
                    if (str != null && str.length > 0) {
                        for (int i = 0; i < str.length; i++) {
                            if (str[i].contains(".")) {
                                int index = str[i].lastIndexOf(".");
                                if (index > 0) {
                                    mProjectStr = str[i].substring(0, index);
                                    break;
                                }
                            }
                        }
                    }
                }
                Log.i(TAG, "ProjectInfo: GetDeviceSubVersion : " + mProjectStr);
                return mProjectStr;
            } catch (Exception e) {
                return "";
            }
        } catch (IOException e2) {
            return "";
        } catch (Throwable th) {
            reader.close();
        }
    }

    public static boolean IsSupportLPA(Context ctx) {
        String ProjectName = GetProjectName();
        if (ProjectName != null) {
            Element root = GetCfgXmlFile();
            String[] mSupportProject;
            if (root != null) {
                mSupportProject = root.getElementsByTagName(PSDB.IS_SUPPORT_LPA).item(0).getFirstChild().getNodeValue().split(SYMBOLS.SEMICOLON);
                if (mSupportProject != null) {
                    for (Object equals : mSupportProject) {
                        if (ProjectName.equals(equals)) {
                            Log.i(TAG, "ProjectInfo: IsSupportLPA()= Yes");
                            return true;
                        }
                    }
                }
            } else {
                mSupportProject = getConfigString(ctx, C0321R.string.powersaving_db_support_lpa).split(SYMBOLS.SEMICOLON);
                if (mSupportProject != null) {
                    for (Object equals2 : mSupportProject) {
                        if (ProjectName.equals(equals2)) {
                            Log.i(TAG, "ProjectInfo: IsSupportLPA()= Yes");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static String[] getNormalModeCpuLimitSpeedList() {
        String[] limitSpeedList = null;
        Element root = GetProductCfgXmlFile();
        if (root != null) {
            try {
                limitSpeedList = root.getElementsByTagName(PSDB.NORMAL_MODE_CPU_LIMIT_SPEED_LIST).item(0).getFirstChild().getNodeValue().split(SYMBOLS.SEMICOLON);
            } catch (Exception e) {
                Log.i(TAG, "Exception: " + e);
            }
        }
        return limitSpeedList;
    }

    public static String[] getExtremeModeCpuLimitSpeedList() {
        String[] limitSpeedList = null;
        Element root = GetProductCfgXmlFile();
        if (root != null) {
            try {
                limitSpeedList = root.getElementsByTagName(PSDB.EXTREME_MODE_CPU_LIMIT_SPEED_LIST).item(0).getFirstChild().getNodeValue().split(SYMBOLS.SEMICOLON);
            } catch (Exception e) {
                Log.i(TAG, "Exception: " + e);
            }
        }
        return limitSpeedList;
    }

    public static String[] getCpuLimitOpcode1List() {
        String[] opcodeList = null;
        Element root = GetProductCfgXmlFile();
        if (root != null) {
            try {
                opcodeList = root.getElementsByTagName(PSDB.CPU_LIMIT_OPCODE_1_LIST).item(0).getFirstChild().getNodeValue().split(SYMBOLS.SEMICOLON);
            } catch (Exception e) {
                Log.i(TAG, "Exception: " + e);
            }
        }
        return opcodeList;
    }

    public static String[] getCpuLimitOpcode2List() {
        String[] opcodeList = null;
        Element root = GetProductCfgXmlFile();
        if (root != null) {
            try {
                opcodeList = root.getElementsByTagName(PSDB.CPU_LIMIT_OPCODE_2_LIST).item(0).getFirstChild().getNodeValue().split(SYMBOLS.SEMICOLON);
            } catch (Exception e) {
                Log.i(TAG, "Exception: " + e);
            }
        }
        return opcodeList;
    }

    public static String[] getNormalModeSaveTimeList() {
        String[] saveTimeList = null;
        Element root = GetProductCfgXmlFile();
        if (root != null) {
            try {
                saveTimeList = root.getElementsByTagName(PSDB.NORMAL_MODE_SAVE_TIME_LIST).item(0).getFirstChild().getNodeValue().split(SYMBOLS.SEMICOLON);
            } catch (Exception e) {
                Log.i(TAG, "Exception: " + e);
            }
        }
        return saveTimeList;
    }

    public static String[] getExtremeModeSaveTimeList() {
        String[] saveTimeList = null;
        Element root = GetProductCfgXmlFile();
        if (root != null) {
            try {
                saveTimeList = root.getElementsByTagName(PSDB.EXTREME_MODE_SAVE_TIME_LIST).item(0).getFirstChild().getNodeValue().split(SYMBOLS.SEMICOLON);
            } catch (Exception e) {
                Log.i(TAG, "Exception: " + e);
            }
        }
        return saveTimeList;
    }

    public static boolean IsSupportCPUPolicy(Context ctx) {
        String ProjectName = GetProjectName();
        if (ProjectName != null) {
            try {
                Element root = GetCfgXmlFile();
                String[] mSupportProject;
                if (root != null) {
                    mSupportProject = root.getElementsByTagName(PSDB.IS_SUPPORT_CPU_POLICY).item(0).getFirstChild().getNodeValue().split(SYMBOLS.SEMICOLON);
                    if (mSupportProject != null) {
                        for (Object equals : mSupportProject) {
                            if (ProjectName.equals(equals)) {
                                Log.i(TAG, "ProjectInfo: IsSupportCPUPolicy()= Yes");
                                Log.i(TAG, "ProjectName: ProjectName = " + ProjectName);
                                return true;
                            }
                        }
                    }
                } else {
                    mSupportProject = getConfigString(ctx, C0321R.string.powersaving_db_support_cpu).split(SYMBOLS.SEMICOLON);
                    if (mSupportProject != null) {
                        for (Object equals2 : mSupportProject) {
                            if (ProjectName.equals(equals2)) {
                                Log.i(TAG, "ProjectInfo: IsSupportCPUPolicy()= Yes");
                                Log.i(TAG, "ProjectName: ProjectName = " + ProjectName);
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, "Exception: " + e);
                return false;
            }
        }
        return false;
    }

    public static boolean isSupportScreenPolicy(Context ctx) {
        String ProjectName = GetProjectName();
        if (ProjectName != null) {
            try {
                Element root = GetCfgXmlFile();
                String[] mNotSupportProject;
                if (root != null) {
                    mNotSupportProject = root.getElementsByTagName(PSDB.IS_NOT_SUPPORT_SCREEN_POLICY).item(0).getFirstChild().getNodeValue().split(SYMBOLS.SEMICOLON);
                    if (mNotSupportProject != null) {
                        for (Object equals : mNotSupportProject) {
                            if (ProjectName.equals(equals)) {
                                Log.i(TAG, "ProjectInfo: IsSupportScreenPolicy()= No");
                                return false;
                            }
                        }
                    }
                } else {
                    mNotSupportProject = getConfigString(ctx, C0321R.string.powersaving_not_support_screen_policy).split(SYMBOLS.SEMICOLON);
                    if (mNotSupportProject != null) {
                        for (Object equals2 : mNotSupportProject) {
                            if (ProjectName.equals(equals2)) {
                                Log.i(TAG, "ProjectInfo: IsSupportScreenPolicy()= No");
                                return false;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                return true;
            }
        }
        return true;
    }

    public static boolean isSupportTGGUI(Context ctx) {
        String ProjectName = GetProjectName();
        if (ProjectName != null) {
            try {
                Element root = GetCfgXmlFile();
                String[] mNotSupportProject;
                if (root != null) {
                    mNotSupportProject = root.getElementsByTagName(PSDB.IS_SUPPORT_TG_GUI).item(0).getFirstChild().getNodeValue().split(SYMBOLS.SEMICOLON);
                    if (mNotSupportProject != null) {
                        for (Object equals : mNotSupportProject) {
                            if (ProjectName.equals(equals)) {
                                Log.i(TAG, "ProjectInfo: IsSupportTGGUI()= Yes");
                                return true;
                            }
                        }
                    }
                } else {
                    mNotSupportProject = getConfigString(ctx, C0321R.string.powersaving_support_tg_gui).split(SYMBOLS.SEMICOLON);
                    if (mNotSupportProject != null) {
                        for (Object equals2 : mNotSupportProject) {
                            if (ProjectName.equals(equals2)) {
                                Log.i(TAG, "ProjectInfo: IsSupportTGGUI()= Yes");
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isSupportDozeConfig() {
        Element root = GetCfgXmlFile();
        if (root == null || root.getElementsByTagName(PSDB.IS_SUPPORT_DOZE_MODE).getLength() <= 0 || !root.getElementsByTagName(PSDB.IS_SUPPORT_DOZE_MODE).item(0).getFirstChild().getNodeValue().equalsIgnoreCase(SWITCHER.ON)) {
            return false;
        }
        Log.i(TAG, "ProjectInfo: isSupportDozeConfig()= Yes");
        return true;
    }

    public static boolean isSupportAmoledConfig() {
        Element root = GetProductCfgXmlFile();
        if (root == null) {
            return false;
        }
        try {
            if (root.getElementsByTagName(PSDB.AMOLED_DISPLAY).getLength() <= 0 || !root.getElementsByTagName(PSDB.AMOLED_DISPLAY).item(0).getFirstChild().getNodeValue().equalsIgnoreCase("true")) {
                return false;
            }
            Log.i(TAG, "ProjectInfo: isSupportAmoledConfig()= Yes");
            return true;
        } catch (Exception e) {
            Log.i(TAG, "Exception: " + e);
            return false;
        }
    }

    public static String getServerConfig() {
        Element root = GetCfgXmlFile();
        if (root == null || root.getElementsByTagName(PSDB.PS_SERVER).getLength() <= 0) {
            return null;
        }
        return root.getElementsByTagName(PSDB.PS_SERVER).item(0).getFirstChild().getNodeValue();
    }

    public static String getDozeTimeoutConfig(String key) {
        Element root = GetCfgXmlFile();
        String value = "";
        if (root == null || root.getElementsByTagName(key).getLength() <= 0) {
            return value;
        }
        try {
            return root.getElementsByTagName(key).item(0).getFirstChild().getNodeValue();
        } catch (Exception e) {
            return "";
        }
    }
}
