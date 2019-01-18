package com.evenwell.powersaving.g3.pushservice;

public class PushServiceUtils {

    public static final class CHECK_CP_REASON {
        public static final String fingerprint_changed = "fingerprint_changed";
        public static final String push_request = "push_request";
        public static final String reqular_polling = "reqular_polling";
    }

    public static final class HEADER {
        public static final String AccessKeyId = "AccessKeyId";
        public static final String ContentType = "Content-Type";
        public static final String Signature = "Signature";
        public static final String SignatureMethod = "SignatureMethod";
        public static final String SignatureNonce = "SignatureNonce";
        public static final String SignatureVersion = "SignatureVersion";
        public static final String Timestamp = "Timestamp";
        public static final String Version = "Version";
    }

    public static final class HEADER_VALUE {
        public static final String AccessKeyId = "1493258691";
        public static final String SignatureMethod = "HMAC-SHA1";
        public static final String SignatureVersion = "1.0";
        public static final String Version = "v1";
    }

    public static final class REGISTER_DEVICE_KEY {
        public static final String app_name = "app_name";
        public static final String category = "category";
        public static final String device_id = "device_id";
        public static final String device_imei = "device_imei";
        public static final String device_model = "device_model";
        public static final String device_project = "device_project";
        public static final String device_skuid = "device_skuid";
        public static final String device_sub_version = "device_sub_version";
        public static final String device_version = "device_version";
        public static final String fingerprint = "fingerprint";
        public static final String regular_polling_interval = "regular_polling_interval";
        public static final String version = "version";
    }

    public static final class RESPONSE_CODE {
        public static final int access_denied = 403;
        public static final int internal_erorr = 500;
        public static final int invalid_request = 400;
        public static final int invalid_signature = 403;
        public static final int package_not_found = 404;
        public static final int signature_not_found = 401;
        public static final int success = 200;
    }

    public static final class UPDATE_RESULT_VALUE {
        public static final String check_response_time = "check_response_time";
        public static final String device_id = "device_id";
        public static final String download_retry_count = "download_retry_count";
        public static final String download_speed = "download_speed";
        public static final String package_id = "package_id";
        public static final String statistics = "statistics";
        public static final String status = "status";
        public static final String success_components = "success_components";
    }
}
