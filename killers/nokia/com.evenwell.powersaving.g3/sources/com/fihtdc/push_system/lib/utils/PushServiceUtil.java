package com.fihtdc.push_system.lib.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.fihtdc.backuptool.FileOperator;
import com.fihtdc.push_system.lib.FihPushServiceProxy;
import com.fihtdc.push_system.lib.app.FihPushReceiveServiceProxy;
import com.fihtdc.push_system.lib.common.PushProp;
import com.fihtdc.push_system.lib.common.PushUtil;
import com.fihtdc.push_system.lib.service.ApplicationBinding;
import com.fihtdc.push_system.lib.service.CommonConfig;
import dalvik.system.PathClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net2.lingala.zip4j.util.InternalZipConstants;
import org.json.JSONObject;

public class PushServiceUtil {
    public static final String ACTION_APP_RECEIVER = "com.fihtdc.push_system.lib.app.PUSH_RECEIVER";
    public static final String ACTION_PUSH_SERVICE = "com.fihtdc.push_system.lib.FihPushService";
    private static final String META_DATA_DEBUG_SERVER_ADDR = "dev-server-addr";
    private static final String PATH_SHARED_LIBRARY = "/system/framework/PushLibrary.jar";
    private static final String TAG = "FP819.PushServiceUtil";

    /* renamed from: com.fihtdc.push_system.lib.utils.PushServiceUtil$1 */
    static class C01101 implements Comparator<ResolveInfo> {
        C01101() {
        }

        public int compare(ResolveInfo o1, ResolveInfo o2) {
            return o1.serviceInfo.packageName.compareTo(o2.serviceInfo.packageName);
        }
    }

    protected static List<ResolveInfo> queryPushServiceList(Context context) {
        return queryPushServiceList(context, null);
    }

    protected static List<ResolveInfo> queryPushServiceList(Context context, String pushServerAddr) {
        if (pushServerAddr == null) {
            pushServerAddr = getPushServerAddr(context);
        }
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentServices(new Intent(ACTION_PUSH_SERVICE), 1152);
        if (resolveInfoList == null || resolveInfoList.size() == 0) {
            Log.e(TAG, "queryPushServiceList(): error, no any active push service");
        } else {
            Iterator<ResolveInfo> iter = resolveInfoList.iterator();
            while (iter.hasNext()) {
                if (!TextUtils.equals(pushServerAddr, getPushServerAddr(context, ((ResolveInfo) iter.next()).serviceInfo))) {
                    iter.remove();
                }
            }
            Collections.sort(resolveInfoList, new C01101());
        }
        return resolveInfoList;
    }

    public static Intent getPushService(Context context) {
        ServiceInfo service = getActivePushService(context);
        if (service != null) {
            Log.v(TAG, "getPushService(): actived: " + service.packageName);
            return getServiceIntent(service);
        }
        ServiceInfo selector = getHighestVersionService(context);
        if (selector == null && !GcmUtils.isPushAgentApp(context)) {
            Log.i(TAG, "getPushService(): fail to get selector");
            enablePushService(context, true);
            selector = getHighestVersionService(context);
        }
        if (selector == null) {
            Log.i(TAG, "getPushService(): still fail to get selector");
            return null;
        }
        Intent pushService = null;
        try {
            pushService = callChoosePushService(context, selector.applicationInfo.packageName);
            Log.v(TAG, "getPushService(): " + pushService);
            return pushService;
        } catch (Exception e) {
            e.printStackTrace();
            return pushService;
        }
    }

    public static void enablePushService(Context context, boolean enabled) {
        int state = 1;
        Log.d(TAG, "enablePushService(): " + enabled);
        if (!enabled) {
            state = 2;
        }
        try {
            ServiceInfo si = getPushServiceInfo(context);
            if (si != null) {
                context.getPackageManager().setComponentEnabledSetting(new ComponentName(si.packageName, si.name), state, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(24)
    public static ServiceInfo getPushServiceInfo(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent query = new Intent(ACTION_PUSH_SERVICE);
        int flag;
        if (VERSION.SDK_INT <= 23) {
            flag = 512;
        } else {
            flag = 512;
        }
        for (ResolveInfo ri : pm.queryIntentServices(query, flag)) {
            if (context.getPackageName().equals(ri.serviceInfo.packageName)) {
                return ri.serviceInfo;
            }
        }
        return null;
    }

    private static Intent callChoosePushService(Context context, String selectorPackageName) {
        try {
            return (Intent) Class.forName("com.fihtdc.push_system.lib.utils.PushServiceUtil", true, new PathClassLoader(getLibrarySrcDir(context.getPackageManager().getApplicationInfo(selectorPackageName, 1152)), ClassLoader.getSystemClassLoader())).getDeclaredMethod("choosePushService", new Class[]{Context.class}).invoke(null, new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @TargetApi(23)
    public static Intent choosePushService(Context context) {
        if (GcmUtils.isPushAgentExist(context)) {
            ServiceInfo pushAgentService = GcmUtils.getPushAgentService(context);
            if (pushAgentService != null) {
                return getServiceIntent(pushAgentService);
            }
            Log.i(TAG, "choosePushService(): PushAgent do not have push service, return null service");
            return null;
        }
        List<ResolveInfo> pushServiceList = queryPushServiceList(context);
        Intent selectedService = null;
        int selectedVersion = -1;
        for (ResolveInfo ri : pushServiceList) {
            ServiceInfo si = ri.serviceInfo;
            if (si.applicationInfo.uid == FileOperator.MAX_DIR_LENGTH) {
                selectedService = getServiceIntent(si);
                Log.i(TAG, "choosePushService(): choose system uid: " + selectedService);
                break;
            } else if (VERSION.SDK_INT >= 23 && ((PowerManager) context.getSystemService("power")).isIgnoringBatteryOptimizations(si.applicationInfo.packageName)) {
                int thisServiceVersion = getPushServiceVersion(ri.serviceInfo.applicationInfo);
                if (thisServiceVersion > selectedVersion) {
                    selectedService = getServiceIntent(si);
                    selectedVersion = thisServiceVersion;
                    Log.i(TAG, "choosePushService(): choose Doze white list: " + selectedService);
                }
            }
        }
        if (selectedService != null) {
            return selectedService;
        }
        selectedService = getServiceIntent(((ResolveInfo) pushServiceList.get(0)).serviceInfo);
        Log.i(TAG, "choosePushService(): choose first: " + selectedService);
        return selectedService;
    }

    static ServiceInfo getHighestVersionService(Context context) {
        ServiceInfo service = null;
        List<ResolveInfo> resolveInfoList = queryPushServiceList(context);
        int maxVersion = -1;
        for (ResolveInfo ri : resolveInfoList) {
            int serviceVersion = getPushServiceVersion(ri.serviceInfo.applicationInfo);
            if (serviceVersion > maxVersion) {
                service = ri.serviceInfo;
                maxVersion = serviceVersion;
            }
        }
        if (service != null) {
            Log.d(TAG, "getHighestServiceVersion(): select service " + service.packageName + ", ver=" + maxVersion);
            return service;
        } else if (resolveInfoList == null || resolveInfoList.size() == 0) {
            Log.d(TAG, "getHighestServiceVersion(): No any Service avaliable");
            return service;
        } else {
            service = ((ResolveInfo) resolveInfoList.get(0)).serviceInfo;
            Log.d(TAG, "getHighestServiceVersion(): select first service " + service.packageName);
            return service;
        }
    }

    public static int getPushServiceVersion(ApplicationInfo info) {
        int version = -1;
        try {
            if (!(useFrameworkLibrary(info) || info.metaData == null)) {
                version = info.metaData.getInt("FihPushSdk.Version", -1);
                Log.v(TAG, "getPushServiceVersion(" + info.packageName + "):# " + version);
                if (version > 0) {
                    return version;
                }
            }
            Object objVersion = Class.forName("com.fihtdc.push_system.lib.common.PushProp", true, new PathClassLoader(getLibrarySrcDir(info), ClassLoader.getSystemClassLoader())).getDeclaredField("PUSH_SDK_VERSION_CODE").get(null);
            Log.v(TAG, "getPushServiceVersion(" + info.packageName + "):## " + objVersion);
            if (objVersion != null) {
                version = ((Integer) objVersion).intValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    private static String getLibrarySrcDir(ApplicationInfo info) {
        if (useFrameworkLibrary(info)) {
            return PATH_SHARED_LIBRARY;
        }
        return info.sourceDir;
    }

    private static boolean useFrameworkLibrary(ApplicationInfo info) {
        String[] files = info.sharedLibraryFiles;
        if (files == null || files.length <= 0) {
            return false;
        }
        for (String file : files) {
            if (PATH_SHARED_LIBRARY.equals(file)) {
                return true;
            }
        }
        return false;
    }

    public static ServiceInfo getActivePushService(Context context) {
        return getActivePushService(context, null);
    }

    public static ServiceInfo getActivePushService(Context context, String debugServer) {
        ServiceInfo defaultService = null;
        long createdTime = Long.MAX_VALUE;
        try {
            List<ResolveInfo> resolveInfoList = queryPushServiceList(context, debugServer);
            List<RunningServiceInfo> amRunningServiceList = ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE);
            for (ResolveInfo ri : resolveInfoList) {
                ServiceInfo si = ri.serviceInfo;
                RunningServiceInfo rsi = findRunningInfo(amRunningServiceList, si);
                if (rsi != null) {
                    if (createdTime == Long.MAX_VALUE) {
                        defaultService = si;
                        createdTime = rsi.activeSince;
                        Log.v(TAG, "getActivePushService(): 1 " + defaultService.packageName + "--" + rsi.activeSince);
                    } else {
                        ServiceInfo serviceNotDefault;
                        Log.v(TAG, "getActivePushService(): 2 def:  " + defaultService.packageName + "--" + createdTime);
                        Log.v(TAG, "getActivePushService(): 2 this: " + si.packageName + "--" + rsi.activeSince);
                        if (rsi.activeSince < createdTime) {
                            serviceNotDefault = defaultService;
                            defaultService = si;
                            createdTime = rsi.activeSince;
                        } else {
                            serviceNotDefault = si;
                        }
                        Log.e(TAG, "getActivePushService(): Service run at same time. Default=" + defaultService.packageName + ", stop " + serviceNotDefault.packageName);
                        final Intent service = getServiceIntent(serviceNotDefault);
                        final Context context2 = context;
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    new FihPushServiceProxy(context2, service).shutdown();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, "shutdown-" + service).start();
                    }
                }
            }
        } catch (Exception e) {
            if (e.getClass().getName().contains("DeadSystemException")) {
                Process.killProcess(Process.myPid());
            }
            e.printStackTrace();
        }
        return defaultService;
    }

    public static String getPushServerAddr(Context context) {
        try {
            ServiceInfo si = getPushServiceInfo(context);
            if (si == null) {
                return getPushServerAddr(context, getCurrentPushReceiver(context));
            }
            return getPushServerAddr(context, si);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getPushServerAddr(Context context, ServiceInfo si) {
        String serverAddr = null;
        if (!(si == null || si.metaData == null)) {
            serverAddr = si.metaData.getString(META_DATA_DEBUG_SERVER_ADDR);
            if (serverAddr != null && serverAddr.endsWith(InternalZipConstants.ZIP_FILE_SEPARATOR)) {
                serverAddr = serverAddr.substring(0, serverAddr.length() - 1);
            }
        }
        if (serverAddr == null) {
            serverAddr = CommonConfig.getDefaultPushServerAddr(context);
        }
        return serverAddr.toLowerCase();
    }

    public static ServiceInfo getCurrentPushReceiver(Context context) {
        Intent intent = new Intent(ACTION_APP_RECEIVER);
        intent.setPackage(context.getPackageName());
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentServices(intent, 128);
        if (resolveInfoList == null || resolveInfoList.size() <= 0) {
            return null;
        }
        return ((ResolveInfo) resolveInfoList.get(0)).serviceInfo;
    }

    private static RunningServiceInfo findRunningInfo(List<RunningServiceInfo> amRunningServiceList, ServiceInfo si) {
        for (RunningServiceInfo rsi : amRunningServiceList) {
            if (si.packageName.equals(rsi.service.getPackageName()) && si.name.equals(rsi.service.getClassName())) {
                return rsi;
            }
        }
        return null;
    }

    public static boolean containPushSerivce(Context context, int uid) {
        for (ResolveInfo ri : getPushReceiverList(context)) {
            if (ri.serviceInfo.applicationInfo.uid == uid) {
                return true;
            }
        }
        return false;
    }

    public static Intent getServiceIntent(ServiceInfo serviceInfo) {
        Intent intent = new Intent();
        intent.setClassName(serviceInfo.packageName, serviceInfo.name);
        return intent;
    }

    public static ArrayList<ApplicationBinding> getApplicationList(Context context) {
        ArrayList<ApplicationBinding> appBindingList = new ArrayList();
        List<ResolveInfo> resolveInfoList = getPushReceiverList(context);
        if (resolveInfoList.size() == 0) {
            return null;
        }
        for (ResolveInfo info : resolveInfoList) {
            ApplicationBinding appBinding = resolveInfoToApplicationBinding(context, info);
            if (appBinding != null) {
                appBindingList.add(appBinding);
            }
        }
        return appBindingList;
    }

    public static List<ResolveInfo> getPushReceiverList(Context context) {
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentServices(new Intent(ACTION_APP_RECEIVER), 128);
        if (resolveInfoList == null || resolveInfoList.size() == 0) {
            return new ArrayList();
        }
        String pushServerAddr = getPushServerAddr(context);
        Iterator<ResolveInfo> iter = resolveInfoList.iterator();
        while (iter.hasNext()) {
            if (!TextUtils.equals(pushServerAddr, getPushServerAddr(context, ((ResolveInfo) iter.next()).serviceInfo))) {
                iter.remove();
            }
        }
        return resolveInfoList;
    }

    public static ApplicationBinding resolveInfoToApplicationBinding(Context context, ResolveInfo info) {
        ApplicationBinding appBinding = new ApplicationBinding();
        appBinding.packageName = info.serviceInfo.packageName;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(info.serviceInfo.packageName, 0);
            if (!PushUtil.canAutoStart(context, ai.uid, info.serviceInfo.packageName)) {
                PushUtil.grantAutoStartAppops(context, ai.uid, info.serviceInfo.packageName);
            }
            Intent service = getServiceIntent(info.serviceInfo);
            Log.v(TAG, "> resolveInfoToApplicationBinding(): getPushInfos " + info.serviceInfo.packageName);
            Bundle infos = new FihPushReceiveServiceProxy(context, service).getPushInfos();
            Log.v(TAG, "< resolveInfoToApplicationBinding(): getPushInfos " + info.serviceInfo.packageName);
            if (infos != null) {
                for (String key : infos.keySet()) {
                    if (Arrays.asList(PushProp.APP_EXT_KEY_FILTER).contains(key)) {
                        Object val = infos.get(key);
                        if (val != null) {
                            appBinding.extraInfo.put(key, val.toString());
                        }
                    }
                }
            }
            Log.v(TAG, "> resolveInfoToApplicationBinding(): getApplicationInfo " + info.serviceInfo.packageName);
            Bundle appInfos = new FihPushReceiveServiceProxy(context, service).getApplicationInfo();
            Log.v(TAG, "< resolveInfoToApplicationBinding(): getApplicationInfo " + info.serviceInfo.packageName);
            if (addApplicationInfo(context, appBinding, appInfos)) {
                return appBinding;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ApplicationBinding getApplicationBinding(Context context, String packageName) {
        List<ResolveInfo> resolveInfoList = getPushReceiverList(context);
        if (resolveInfoList.size() == 0) {
            return null;
        }
        for (ResolveInfo rInfo : resolveInfoList) {
            if (rInfo.serviceInfo.packageName.equals(packageName)) {
                return resolveInfoToApplicationBinding(context, rInfo);
            }
        }
        return null;
    }

    private static boolean addApplicationInfo(Context context, ApplicationBinding appBinding, Bundle appInfos) {
        String accessId = appInfos.getString(PushProp.KEY_APP_INFO_ACCESS_ID);
        String accessKey = appInfos.getString(PushProp.KEY_APP_INFO_ACCESS_KEY);
        String secretKey = appInfos.getString(PushProp.KEY_APP_INFO_SECRET_kEY);
        String challenge = getRandomString(32);
        String signature = getSignature(accessId, secretKey, challenge);
        if (TextUtils.isEmpty(accessId)) {
            Log.e(TAG, "addApplicationInfo(): error, " + appBinding.packageName + " accessId is empty");
            return false;
        } else if (TextUtils.isEmpty(accessKey)) {
            Log.e(TAG, "addApplicationInfo(): error, " + appBinding.packageName + " accessKey is empty");
            return false;
        } else if (TextUtils.isEmpty(secretKey)) {
            Log.e(TAG, "addApplicationInfo(): error, " + appBinding.packageName + " secretKey is empty");
            return false;
        } else if (TextUtils.isEmpty(signature)) {
            Log.e(TAG, "addApplicationInfo(): error, " + appBinding.packageName + " cannnot generate signature");
            return false;
        } else {
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(appBinding.packageName, 0);
                appBinding.extraInfo.put("ApkVersionCode", String.valueOf(pInfo.versionCode));
                appBinding.extraInfo.put("ApkVersion", pInfo.versionName);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            appBinding.extraInfo.put("MobileDevice", Build.DEVICE);
            appBinding.extraInfo.put("MobileProduct", Build.PRODUCT);
            appBinding.extraInfo.put("MobileBrand", Build.BRAND);
            appBinding.extraInfo.put("AndroidSDK", String.valueOf(VERSION.SDK_INT));
            appBinding.extraInfo.put(PushProp.JSON_KEY_APP_EXT_APP_ID, accessId);
            appBinding.extraInfo.put(PushProp.JSON_KEY_APP_EXT_ACCESS_KEY, accessKey);
            appBinding.extraInfo.put(PushProp.JSON_KEY_APP_EXT_CHALLENGE, challenge);
            appBinding.extraInfo.put("Signature", signature);
            return true;
        }
    }

    private static String getSignature(String accessId, String secretKey, String challenge) {
        try {
            return sha1(secretKey, accessId + ":" + challenge);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String sha1(String keyString, String message) throws Exception {
        SecretKeySpec key = new SecretKeySpec(keyString.getBytes("UTF-8"), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(key);
        return new String(Base64.encodeToString(mac.doFinal(message.getBytes("UTF-8")), 2));
    }

    public static String getRandomString(int strSize) {
        return getRandomString(strSize, false);
    }

    public static String getRandomString(int strSize, boolean digitOnly) {
        String randomLetterList;
        if (digitOnly) {
            randomLetterList = "0123456789";
        } else {
            randomLetterList = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-";
        }
        StringBuilder sb = new StringBuilder(strSize);
        Random random = new Random();
        for (int i = 0; i < strSize; i++) {
            sb.append(randomLetterList.charAt((int) (random.nextFloat() * ((float) randomLetterList.length()))));
        }
        return sb.toString();
    }

    public static boolean isFihPushEnabled(Context context) {
        if (isDeviceAllowPush()) {
            return true;
        }
        return false;
    }

    public static boolean isDeviceAllowPush() {
        JSONObject config = CommonConfig.readConfig();
        if (config != null) {
            try {
                if (config.has("PushEnabledStatus") && "disable".equalsIgnoreCase(config.getString("PushEnabledStatus"))) {
                    Log.w(TAG, "isDeviceAllowPush(): Not allow to run push service PushEnabledStatus is disable");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
