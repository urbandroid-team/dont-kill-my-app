package com.evenwell.powersaving.g3.retrofit;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class DevicePost {
    private static final String SKUID_PATH = "hidden/data/CDALog/ID_Final";
    private static final String TAG = "DevicePost";
    public String app_name;
    public String category;
    public String device_id;
    public String device_model;
    public String device_project;
    public String device_skuid;
    public String device_sub_version;
    public String device_version;
    public String fingerprint;
    public String version = getVersion();

    public static class ModelInfo {
        public String model;
        public String project;
        public String subVersion;
        public String version;

        public String toString() {
            return "project = " + this.project + ",model = " + this.model + ",version = " + this.version + ",subVersion = " + this.subVersion;
        }
    }

    public DevicePost(Context context) {
        ModelInfo info = GetModelInfo();
        this.app_name = context.getPackageName();
        this.fingerprint = getFingerPrint(context);
        this.device_project = info.project;
        this.device_model = info.model;
        this.device_skuid = getSKUID();
        this.device_id = VERSION.SDK_INT >= 9 ? Build.getSerial() : "";
        this.device_version = info.version;
        this.device_sub_version = info.subVersion;
    }

    public static String getSKUID() {
        String prop_skuid = "";
        prop_skuid = SystemProperties.get("ro.cda.skuid.id_final", null);
        if (prop_skuid != null) {
            Log.d(TAG, "getSKUID from property skuid = " + prop_skuid);
            return prop_skuid;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(SKUID_PATH));
            String skuid = reader.readLine();
            reader.close();
            Log.d(TAG, "getSKUID skuid = " + skuid);
            return skuid;
        } catch (Exception ex) {
            Log.d(TAG, "Can not get SKU");
            ex.printStackTrace();
            return null;
        }
    }

    public String toString() {
        return "app_name: " + this.app_name + ",fingerprint: " + this.fingerprint + ",version: " + this.version + ",device_project: " + this.device_project + ",device_model: " + this.device_model + ",device_skuid: " + this.device_skuid + ",device_id: " + this.device_id + ",device_version: " + this.device_version + ",device_sub_version: " + this.device_sub_version;
    }

    public static ModelInfo GetModelInfo() {
        Exception e;
        Throwable th;
        BufferedReader reader = null;
        ModelInfo info = new ModelInfo();
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(FILENAME.POWER_SAVING_GET_DEVICE_SUBVERSION), 256);
            try {
                String[] infos = reader2.readLine().split("-");
                info.project = infos[0].split(",")[1];
                info.model = infos[3];
                info.version = infos[1];
                info.subVersion = infos[4].split("\\.")[0];
                PSUtils.closeSilently(reader2);
                reader = reader2;
            } catch (Exception e2) {
                e = e2;
                reader = reader2;
                try {
                    e.printStackTrace();
                    PSUtils.closeSilently(reader);
                    return info;
                } catch (Throwable th2) {
                    th = th2;
                    PSUtils.closeSilently(reader);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                PSUtils.closeSilently(reader);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            PSUtils.closeSilently(reader);
            return info;
        }
        return info;
    }

    public static String getVersion() {
        return "0001";
    }

    public static String getFingerPrint(Context context) {
        return "mcc_" + String.valueOf(getSIMMCC(context));
    }

    public static int getSIMMCC(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        List<SubscriptionInfo> activeSubs = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
        if (activeSubs == null || activeSubs.isEmpty()) {
            String networkOperator = tm.getNetworkOperator();
            if (networkOperator == null || networkOperator.length() == 0) {
                return 0;
            }
            int mcc = Integer.parseInt(networkOperator.substring(0, 3));
            Log.i(TAG, "No Sim mcc = " + mcc);
            return mcc;
        }
        mcc = ((SubscriptionInfo) activeSubs.get(0)).getMcc();
        Log.w(TAG, "Has Sim mcc = " + mcc);
        return mcc;
    }
}
