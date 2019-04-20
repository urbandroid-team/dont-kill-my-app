package com.evenwell.powersaving.g3.exception;

import android.content.Context;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.pushservice.PackageCategory;
import com.evenwell.powersaving.g3.utils.PSUtils;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DownloadedWhiteList {
    private static final String TAG = "DownloadWhiteList";
    private Context mContext;
    private WhiteList mWhiteList;

    public static class WhiteList {
        public List<String> app_name = new ArrayList();
        public List<String> version = new ArrayList();
    }

    public DownloadedWhiteList(Context context, File file) {
        this.mWhiteList = readWhiteList(file);
        this.mContext = context;
    }

    private WhiteList readWhiteList(File file) {
        Exception e;
        Throwable th;
        WhiteList whiteList = new WhiteList();
        if (file.exists()) {
            Log.i(TAG, "readWhiteList file: " + file.getPath());
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                try {
                    whiteList = (WhiteList) new Gson().fromJson(reader2, WhiteList.class);
                    PSUtils.closeSilently(reader2);
                    reader = reader2;
                } catch (Exception e2) {
                    e = e2;
                    reader = reader2;
                    try {
                        Log.e(TAG, "readWhiteList Happen exception", e);
                        PSUtils.closeSilently(reader);
                        return whiteList;
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
                Log.e(TAG, "readWhiteList Happen exception", e);
                PSUtils.closeSilently(reader);
                return whiteList;
            }
            return whiteList;
        }
        Log.i(TAG, "white list," + file.getPath() + " does not exist!");
        return whiteList;
    }

    public void updateWhiteList() {
        try {
            String oldWhiteVerion = PowerSavingUtils.GetPreferencesStatusString(this.mContext, PackageCategory.WHITE_LIST.getValue());
            Log.i(TAG, "[checkServerWhilteList]mWhiteList version : " + ((String) this.mWhiteList.version.get(0)) + ",mWhiteList : " + this.mWhiteList.app_name.toString() + ",oldWhiteVerion : " + oldWhiteVerion);
            String[] ver = null;
            if (((String) this.mWhiteList.version.get(0)).contains("_")) {
                ver = ((String) this.mWhiteList.version.get(0)).split("_");
            }
            if (ver != null) {
                String version = ver[ver.length - 1];
                if (oldWhiteVerion == null || Long.valueOf(version).longValue() > Long.valueOf(oldWhiteVerion).longValue()) {
                    List<String> apps = PowerSavingUtils.getLauncherApList(this.mContext);
                    for (int i = 0; i < apps.size(); i++) {
                        if (this.mWhiteList.app_name.contains(apps.get(i))) {
                            BackgroundPolicyExecutor.getInstance(this.mContext).addAppToWhiteList((String) apps.get(i));
                        }
                    }
                    PowerSavingUtils.SetPreferencesStatus(this.mContext, PackageCategory.WHITE_LIST.getValue(), version);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
