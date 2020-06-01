package com.evenwell.powersaving.g3.pushservice;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.exception.DownloadedWhiteList;
import com.evenwell.powersaving.g3.pushservice.PushServiceUtils.CHECK_CP_REASON;
import com.evenwell.powersaving.g3.pushservice.PushServiceUtils.REGISTER_DEVICE_KEY;
import com.evenwell.powersaving.g3.utils.PSConst.SERVICE_START.PSSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.fihtdc.push_system.lib.app.FihPushReceiveService;
import com.fihtdc.push_system.lib.common.PushMessageContract;
import java.io.File;
import org.json.JSONObject;

public class PushReceiverService extends FihPushReceiveService {
    public static final String ACCESS_ID = "4958551037881081807";
    public static final String ACCESS_KEY = "2wi1m4d1f161828gyxgih7b7hs9v7x0w";
    public static final String KEY_CHECK_NOW = "CheckNow";
    public static final String KEY_PUSH_CHECK_RESULT = "PushCheckResult";
    public static final String KEY_UPDATE_DEVICE_SETTING = "UpdateDeviceSetting";
    public static final String SECRET_kEY = "n4e442381kas99i72xp6055oheq6nn0v";
    private static String TAG = TAG.PSLOG;
    private PullServerCommand mPullServerCommand;
    private boolean pullBlackSuccess = false;
    private boolean pullWhiteSuccess = false;

    public String getAccessId() {
        return ACCESS_ID;
    }

    public String getAccessKey() {
        return ACCESS_KEY;
    }

    public String getSecretKey() {
        return SECRET_kEY;
    }

    public int getDefaultNotificationSmallIcon() {
        return 0;
    }

    public Bundle getPushInfos() {
        return null;
    }

    public boolean newPushMessage(Bundle arg0) {
        Log.i(TAG, "[PushReceiverService]: newPushMessage() " + arg0);
        this.mPullServerCommand = new PullServerCommand(this);
        String command = "";
        String body = "";
        for (String key : arg0.keySet()) {
            Log.i(TAG, "key:" + key + "     value:" + arg0.get(key).toString());
            if (key.equalsIgnoreCase(PushMessageContract.MESSAGE_KEY_TITLE)) {
                command = arg0.get(key).toString();
            } else if (key.equalsIgnoreCase(PushMessageContract.MESSAGE_KEY_CONTENT)) {
                body = arg0.get(key).toString();
            }
        }
        if (command.equalsIgnoreCase(KEY_CHECK_NOW)) {
            this.pullBlackSuccess = this.mPullServerCommand.CheckCP(PackageCategory.BLACK_LIST, CHECK_CP_REASON.push_request);
            this.pullWhiteSuccess = this.mPullServerCommand.CheckCP(PackageCategory.WHITE_LIST, CHECK_CP_REASON.push_request);
            if (this.pullWhiteSuccess) {
                new DownloadedWhiteList(this, new File(getFilesDir(), PackageCategory.WHITE_LIST.getValue())).updateWhiteList();
                Log.i(TAG, "[PushReceiverService]: success");
            } else {
                Log.i(TAG, "[PushReceiverService]: false ");
            }
        } else if (command.equalsIgnoreCase(KEY_PUSH_CHECK_RESULT)) {
            Log.i(TAG, "[PushReceiverService]: KEY_PUSH_CHECK_RESULT value : " + body);
            if (this.mPullServerCommand.updateResult(body)) {
                new DownloadedWhiteList(this, new File(getFilesDir(), PackageCategory.WHITE_LIST.getValue())).updateWhiteList();
                Log.i(TAG, "[PushReceiverService]: success");
            } else {
                Log.i(TAG, "[PushReceiverService]: false ");
            }
        } else if (command.equalsIgnoreCase(KEY_UPDATE_DEVICE_SETTING)) {
            try {
                String value = new JSONObject(body).getString(REGISTER_DEVICE_KEY.regular_polling_interval);
                Log.i(TAG, "[PushReceiverService]: KEY_UPDATE_DEVICE_SETTING value : " + value);
                PowerSavingUtils.SetPreferencesStatus((Context) this, PSSPREF.POLLING_INTERVAL, Integer.valueOf(value).intValue());
                this.mPullServerCommand.RegisterDevice(PackageCategory.BLACK_LIST);
                this.mPullServerCommand.RegisterDevice(PackageCategory.WHITE_LIST);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
