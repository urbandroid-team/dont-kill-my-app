package com.evenwell.powersaving.g3.pushservice;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.pushservice.PushServiceUtils.CHECK_CP_REASON;
import com.evenwell.powersaving.g3.pushservice.PushServiceUtils.REGISTER_DEVICE_KEY;
import com.evenwell.powersaving.g3.pushservice.PushServiceUtils.RESPONSE_CODE;
import com.evenwell.powersaving.g3.retrofit.ApiClient;
import com.evenwell.powersaving.g3.retrofit.CheckCpPost;
import com.evenwell.powersaving.g3.retrofit.DevicePost;
import com.evenwell.powersaving.g3.retrofit.DevicePost.ModelInfo;
import com.evenwell.powersaving.g3.retrofit.DeviceResponse;
import com.evenwell.powersaving.g3.retrofit.RegisterDevicePost;
import com.evenwell.powersaving.g3.retrofit.UpdateResultPost;
import com.evenwell.powersaving.g3.utils.PSConst.SERVICE_START.PSSPREF;

public class PullServerCommand {
    public static final String TAG = "[PowerSavingAppG3]PullServerCommand";
    private ApiClient mApiClient;
    private Context mContext;
    private String version_dafult = "1";

    public PullServerCommand(Context context) {
        this.mContext = context;
        this.mApiClient = new ApiClient(this.mContext);
        if (PowerSavingUtils.GetPreferencesStatusString(this.mContext, PackageCategory.BLACK_LIST.getValue()) == null) {
            PowerSavingUtils.SetPreferencesStatus(this.mContext, PackageCategory.BLACK_LIST.getValue(), this.version_dafult);
        }
        if (PowerSavingUtils.GetPreferencesStatusString(this.mContext, PackageCategory.WHITE_LIST.getValue()) == null) {
            PowerSavingUtils.SetPreferencesStatus(this.mContext, PackageCategory.WHITE_LIST.getValue(), this.version_dafult);
        }
        if (PowerSavingUtils.GetPreferencesStatusInt(this.mContext, PSSPREF.POLLING_INTERVAL) == -1) {
            PowerSavingUtils.SetPreferencesStatus(this.mContext, PSSPREF.POLLING_INTERVAL, PollingService.POLLING_INTERVAL);
        }
    }

    public boolean CheckCP(PackageCategory category, String reason) {
        CheckCpPost checkCpPost = new CheckCpPost(this.mContext, category.getValue(), reason);
        Log.d(TAG, "CheckCP :" + checkCpPost);
        return this.mApiClient.CheckCP(checkCpPost);
    }

    public void RegisterDevice(PackageCategory category) {
        RegisterDevicePost registerDeviceRequest = new RegisterDevicePost(this.mContext, category.getValue(), String.valueOf(PowerSavingUtils.GetPreferencesStatusInt(this.mContext, PSSPREF.POLLING_INTERVAL)));
        Log.d(TAG, "RegisterDevice :" + registerDeviceRequest);
        setDeviceConfig(registerDeviceRequest);
        this.mApiClient.RegisterDevice(registerDeviceRequest);
    }

    private void setDeviceConfig(RegisterDevicePost mRegisterDeviceRequest) {
        PowerSavingUtils.SetPreferencesStatus(this.mContext, REGISTER_DEVICE_KEY.app_name, this.mContext.getPackageName());
        PowerSavingUtils.SetPreferencesStatus(this.mContext, REGISTER_DEVICE_KEY.fingerprint, mRegisterDeviceRequest.fingerprint);
        PowerSavingUtils.SetPreferencesStatus(this.mContext, mRegisterDeviceRequest.category, mRegisterDeviceRequest.version);
        PowerSavingUtils.SetPreferencesStatus(this.mContext, REGISTER_DEVICE_KEY.device_project, mRegisterDeviceRequest.device_project);
        PowerSavingUtils.SetPreferencesStatus(this.mContext, REGISTER_DEVICE_KEY.device_model, mRegisterDeviceRequest.device_model);
        PowerSavingUtils.SetPreferencesStatus(this.mContext, "device_id", mRegisterDeviceRequest.device_id);
        PowerSavingUtils.SetPreferencesStatus(this.mContext, REGISTER_DEVICE_KEY.device_version, mRegisterDeviceRequest.device_version);
        PowerSavingUtils.SetPreferencesStatus(this.mContext, REGISTER_DEVICE_KEY.device_sub_version, mRegisterDeviceRequest.device_sub_version);
        PowerSavingUtils.SetPreferencesStatus(this.mContext, REGISTER_DEVICE_KEY.device_imei, mRegisterDeviceRequest.device_imei);
        PowerSavingUtils.SetPreferencesStatus(this.mContext, REGISTER_DEVICE_KEY.device_skuid, mRegisterDeviceRequest.device_skuid);
    }

    public boolean checkRegisterDevice() {
        ModelInfo modelInfo = DevicePost.GetModelInfo();
        String proj_name = modelInfo.project;
        String device_id = VERSION.SDK_INT >= 9 ? Build.getSerial() : "";
        String ver = modelInfo.version;
        String sub_ver = modelInfo.subVersion;
        String model = modelInfo.model;
        boolean result = isSameValue(PowerSavingUtils.GetPreferencesStatusString(this.mContext, REGISTER_DEVICE_KEY.device_project), proj_name);
        if (result) {
            Log.d(TAG, "project name is different");
            return result;
        }
        String old_device_id = PowerSavingUtils.GetPreferencesStatusString(this.mContext, "device_id");
        result = isSameValue(old_device_id, device_id);
        if (result) {
            Log.d(TAG, "device id is different");
            return result;
        }
        String old_ver = PowerSavingUtils.GetPreferencesStatusString(this.mContext, REGISTER_DEVICE_KEY.device_version);
        result = isSameValue(old_device_id, device_id);
        if (result) {
            Log.d(TAG, "device version is different");
            return result;
        }
        String old_sub_ver = PowerSavingUtils.GetPreferencesStatusString(this.mContext, REGISTER_DEVICE_KEY.device_sub_version);
        result = isSameValue(old_sub_ver, sub_ver);
        if (result) {
            Log.d(TAG, "device sub version is different");
            return result;
        }
        String old_imei = PowerSavingUtils.GetPreferencesStatusString(this.mContext, REGISTER_DEVICE_KEY.device_imei);
        String ime = RegisterDevicePost.getIMEI(this.mContext);
        result = isSameValue(old_sub_ver, sub_ver);
        if (result) {
            Log.d(TAG, "device imei is different");
            return result;
        }
        result = isSameValue(PowerSavingUtils.GetPreferencesStatusString(this.mContext, REGISTER_DEVICE_KEY.device_model), model);
        if (result) {
            Log.d(TAG, "device model is different");
            return result;
        }
        result = isSameValue(PowerSavingUtils.GetPreferencesStatusString(this.mContext, REGISTER_DEVICE_KEY.device_skuid), DevicePost.getSKUID());
        if (!result) {
            return false;
        }
        Log.d(TAG, "device skuid is different");
        return result;
    }

    private boolean isSameValue(String oldValue, String newValue) {
        if (TextUtils.isEmpty(oldValue) || TextUtils.isEmpty(newValue) || !oldValue.equals(newValue)) {
            return true;
        }
        return false;
    }

    public boolean updateResult(String strBody) {
        boolean isSuccess = false;
        try {
            DeviceResponse res = this.mApiClient.getCheckCPResponse(strBody);
            CheckCpPost checkCpPost = new CheckCpPost(this.mContext, res.category, CHECK_CP_REASON.push_request);
            if (res != null) {
                UpdateResultPost updateResultPost = this.mApiClient.getUpdateResultPost(res, checkCpPost);
                if (updateResultPost.status == null) {
                    updateResultPost.status = this.mApiClient.getResult(RESPONSE_CODE.success);
                    isSuccess = true;
                }
                this.mApiClient.UpdateResult(updateResultPost);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }
}
