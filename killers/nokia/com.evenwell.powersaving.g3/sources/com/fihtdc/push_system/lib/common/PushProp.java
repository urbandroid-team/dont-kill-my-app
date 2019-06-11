package com.fihtdc.push_system.lib.common;

import android.net.Uri;

public class PushProp {
    public static final String ACTION_PUSH_SERVER_CONNECTED = "action.pushlib.server_connected";
    public static final String ACTION_SERVICE_START_PUSH = "action.start_push";
    public static final String ACTION_SERVICE_STOP_PUSH = "action.stop_push";
    public static final String ACTION_XMPP_CONNECTED = "action.pushlib.xmpp_server_connected";
    public static final String ACTION_XMPP_DISCONNECTED = "action.pushlib.xmpp_server_disconnected";
    public static final String[] APP_EXT_KEY_FILTER = new String[]{"Project", "ImageID", "Density", JSON_KEY_ROOT_DEVICE_ID, "Version", "InternalModel", "ImageID", "SubVersion", "MCC", "MNC"};
    @Deprecated
    public static final String ARG_PUSH_ID = "PushId";
    public static final boolean DEBUG = false;
    public static final String JSON_KEY_APP = "PackageBinding";
    public static final String JSON_KEY_APP_EXT = "ExtraInformation";
    public static final String JSON_KEY_APP_EXT_ACCESS_KEY = "AccessKey";
    public static final String JSON_KEY_APP_EXT_APP_ID = "AppId";
    public static final String JSON_KEY_APP_EXT_C2DM_VERSION = "C2DMVersion";
    public static final String JSON_KEY_APP_EXT_CHALLENGE = "Challenge";
    public static final String JSON_KEY_APP_EXT_LISTENER = "Listener";
    public static final String JSON_KEY_APP_EXT_SDK_VERSION = "SDKVersion";
    public static final String JSON_KEY_APP_EXT_SIGNATURE = "Signature";
    public static final String JSON_KEY_APP_PACKAGE_NAME = "PackageName";
    public static final String JSON_KEY_APP_REMOVE_XMPP_CONNECTION = "RemoveXMPPConnection";
    public static final String JSON_KEY_GLOBAL_ACCESS_KEY_ID = "AccessKeyId";
    public static final String JSON_KEY_GLOBAL_SIGNATURE = "Signature";
    public static final String JSON_KEY_GLOBAL_SIGNATURE_METHOD = "SignatureMethod";
    public static final String JSON_KEY_GLOBAL_SIGNATURE_NONCE = "SignatureNonce";
    public static final String JSON_KEY_GLOBAL_SIGNATURE_VERSION = "SignatureVersion";
    public static final String JSON_KEY_GLOBAL_TIMESTAMP = "Timestamp";
    public static final String JSON_KEY_GLOBAL_VERSION = "Version";
    public static final String JSON_KEY_ROOT_CHECK_ACCOUNT = "CheckAccount";
    public static final String JSON_KEY_ROOT_DEVICE_ID = "DeviceID";
    public static final String JSON_KEY_ROOT_PASSWORD = "Credential";
    public static final String JSON_KEY_ROOT_PUSH_PLATFORM = "PushPlatform";
    public static final String JSON_KEY_ROOT_RESOURCE_ID = "ResourceID";
    public static final String KEY_APP_INFO_ACCESS_ID = "key.accessId";
    public static final String KEY_APP_INFO_ACCESS_KEY = "key.accessKey";
    public static final String KEY_APP_INFO_SECRET_kEY = "key.secretKey";
    public static final String LOG_TAG = "FP819";
    @Deprecated
    public static final String METHOD_GET_CONFIG = "GET_config";
    @Deprecated
    public static final String PUSH_AUTHORITY = "fihpush";
    public static final String PUSH_PLATFORM_FCM = "GPush";
    public static final String PUSH_PLATFORM_FIHPUSH = "FIHPush";
    @Deprecated
    public static final Uri PUSH_PROVIDER_URI = Uri.parse("content://fihpush");
    public static final String PUSH_SDK_VERSION = "8.0010.09";
    public static final int PUSH_SDK_VERSION_CODE = 8001009;
}
