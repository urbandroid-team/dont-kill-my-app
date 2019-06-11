package com.evenwell.powersaving.g3.utils;

public final class PSConst {
    public static final int ONOFF_BASE = 0;

    public static final class COMMON {

        public static final class INTENT {

            public static final class FUNCTION {

                public static final class ACTION {
                    public static final String ACTION_BAM_STATUS = "com.evenwell.action.powersaving.ACTION_BAM_STATUS";
                    public static final String ACTION_START_SERVICE = "com.fihtdc.powersaving.start_powersaver";
                    public static final String ACTION_START_SUBITEM = "com.fihtdc.powersaving.start_subitem";
                }

                public static final class EXTRA {
                    public static final String APP_ALL_WHITE = "app_all_white";
                    public static final String CPU_ENABLE = "CPU";
                    public static final String DC_ENABLE = "DC";
                    public static final String LPM_ENABLE = "LPM";
                    public static final String POWERSAVER_ENABLE = "Enable";
                    public static final String PW_ENABLE = "PW";
                    public static final String SCREEN_ENABLE = "Screen";
                    public static final String SS_ENABLE = "SS";
                }

                public static final class EXTRA_DATA {
                    public static final int OFF = 0;
                    public static final int ON = 1;
                }
            }
        }

        public static final class PARM {
            public static final String CHINA = "CHINA";
            public static final String DISABLED_SYNC_ADAPTER_TYPE_INFO_LIST = "disabled_sync_adapter_type_info_list";
            public static final String IS_SYNC_ADAPTER_CLOSE_SET = "is_sync_adapter_close_set";
            public static final String KEY_BACKGROUND_EXECUTION_ENABLED = "background_execution_enabled";
            public static final String KEY_PS_KEEP_MANUAL_ON = "power_saver_keep_manual_on";
            public static final String KEY_PS_RESTORE_GPS_MODE = "power_saver_restore_gps_mode";
            public static final String LOCAL = "local";
            public static final String NONE = "NONE";
            public static final String START_SERVICE_METHOD = "powersaving_start_service_method";
            public static final String START_SERVICE_USE_INTENT = "start_use_intent";
            public static final String STORE_IN_SELF_DB = "store_in_self_db";
            public static final String STORE_IN_SETTINGS_DB = "store_in_settings_db";
            public static final String STORE_SETTINGS_METHOD = "powersaving_store_settings_method";
            public static final String TETHER = "TETHERING";
            public static final String VK3 = "VK3";
            public static final String VKY = "VKY";
            public static final String[] WLAN_MODEL = new String[]{"10CN", "15CN", "100C", "100T", "100A", "000A"};
        }
    }

    public static final class CPU {

        public static final class CPU_APK {
            public static final String APK42 = "com.evenwell.CPUFreqSuppression";
            public static final String APK43 = "com.evenwell.cpudaemon";
        }

        public static final class CPU_POLICY {
            public static final int ASOD_LOOSE = 0;
            public static final int ASOD_STRICT = 1;
            public static final int ASOD_VERY_STRICT = 2;
        }

        public static final class INTENT {

            public static final class ACTION {
                public static final String ACTION_CPU_POLICY = "com.fihtdc.action.powersaving.notice";
            }

            public static final class EXTRA {
                public static final String PS_NOTICE_PARM = "NoticeInfo";
            }
        }
    }

    public static final class DC {

        public static final class DCPARM {
            public static final String ALLLIST = "app_list";
            public static final String ALLOWLIST = "allow_list";
            public static final int POSTDELAY_MESSAGE = 4001;
            public static final int SCREENON_MESSAGE = 4002;
            public static final int TIME_LENGTH = 1;

            public static final class ALARMTYPE {
                public static final String END = "END";
                public static final String START = "START";
            }

            public static final class BLOCKTYPE {
                public static final int BLOCK_TYPE_3G = 2;
                public static final int BLOCK_TYPE_WIFI = 3;
            }

            public static final class DCMODE {
                public static final int ALLOW_LIST = 0;
                public static final int APP_LIST = 1;
            }

            public static final class PENDING_INTENT_TYPE {
                public static final int PI_END_ALARM = 3003;
                public static final int PI_START_ALARM = 3002;
            }

            public static final class XMLTAG {
                public static final String BLOCK_3G = "Black-List-3G";
                public static final String BLOCK_WIFI = "Black-List-WIFI";
            }
        }

        public static final class DCPREF {
            public static final String DATAACTIVITYSTATE = "dataActivityState";
            public static final String ISDCTIME = "isDCTime";
            public static final String MOBILE = "mobile";
            public static final String RXPACK = "RXPACK";
            public static final String TXPACK = "TXPACK";
            public static final String WIFI = "WiFi";
        }

        public static final class INTENT {

            public static final class ACTION {
                public static final String ACTION_DC_APPLY_TIME = "com.fihtdc.action.powersaving.dc.apply_time";
                public static final String ACTION_DC_DO_SCREEN_OFF_ACTION = "com.fihtdc.action.powersaving.dc.screen_off_action";
            }

            public static final class EXTRA {
                public static final String TIME_KEY = "alarmtype";
            }
        }

        public static final class TIME {

            public static final class TIME_FORMAT {
                public static final int HOUR = 0;
                public static final int MINUTE = 1;
            }

            public static final class VALUE {
                public static final int DETECT_PACKET_TIME = 60000;
                public static final int DETECT_WAKE_UP_TIME = 5000;
                public static final int SCREEN_OFF_TIME = 360000;
                public static final long SCREEN_OFF_WAIT_TIME = 3000;
                public static final long SCREEN_ON_WAIT_TIME = 0;
                public static final int SLEEP_TIME = 30000;
                public static final int TIME_CONVERT = 60;
            }
        }
    }

    public static final class DEBUG {
        public static final boolean DBG = true;
    }

    public static final class DialogID {
        public static final int DC_SWITCH_DIALOG = 2002;
        public static final int INSTALL_AP_WHEN_DC_AND_PW_ON_DIALOG = 2007;
        public static final int INSTALL_AP_WHEN_DC_ON_DIALOG = 2005;
        public static final int INSTALL_AP_WHEN_PW_ON_DIALOG = 2006;
        public static final int LPM_SWITCH_DIALOG = 2004;
        public static final int PS_SWITCH_DIALOG = 2014;
        public static final int PW_SWITCH_DIALOG = 2003;
    }

    public static final class FILENAME {
        public static final String DATAUSAGEXML = "/data/system/DataUsage.xml";
        public static final String NEW_ADD_PRELOAD_APP_STATUS_FILE = "new_add_preload_app_status";
        public static String POWER_SAVING_DEFAULT_BLACK_LIST = "/system/etc/PowerDrainList";
        public static String POWER_SAVING_DEFAULT_EXTERNAL_CFG_FILE = "/system/etc/PowerSavingG3Cfg.xml";
        public static String POWER_SAVING_DEFAULT_EXTERNAL_PRODUCT_CFG_FILE = "/system/etc/PowerSavingProductCfg.xml";
        public static final String POWER_SAVING_GET_DEVICE_SUBVERSION = "/proc/fver";
        public static final String POWER_SAVING_GET_PROJECT_NAME = "/proc/devmodel";
        public static final String POWER_SAVING_PKGS_CLOSE_GPS_FILE = "apps_gps_ignore";
        public static final String PREF_POWER_SAVING_DATA_CONNECTION_FILE = "power_saving_data_con_file";
        public static final String PREF_POWER_SAVING_LPM_AND_DC_APPLY_FILE = "power_saving_lpm_and_dc_apply_file";
        public static final String PREF_POWER_SAVING_LPM_BACKUP_FILE = "power_saving_lpm_backup_file";
        public static final String PREF_POWER_SAVING_STATUS_FILE = "power_saving_status_file";
    }

    public static final class Function {
        public static final int CPU_POLICY = 2008;
        public static final int DC = 2011;
        public static final int LPM = 2012;
        public static final int PW = 2010;
        public static final int SCREEN_POLICY = 2009;
        public static final int SS = 2013;
    }

    public static final class LPM {

        public static final class INTENT {

            public static final class ACTION {
                public static final String ACTION_LPM_BACKGROUND_DATA_CHANGED = "com.fihtdc.action.background_data_changed";
                public static final String ACTION_LPM_RECHECK_BATTERY_STATUS = "com.fihtdc.action.powersaving.lpm.recheck_battery_status";
                public static final String ACTION_LPM_SMART_AMP_MODE_CHANGED = "com.fihtdc.action.ACTION_SMART_AMP_MODE_CHANGED";
                public static final String ACTION_LPM_STILL_SETTING = "com.fihtdc.action.powersaving.lpm.still.setting";
                public static final String ACTION_LPM_UPDATE_SCHEDULE = "com.fihtdc.action.powersaving.lpm.update_schedule";
                public static final String ACTION_NOW_IN_LPM = "com.fihtdc.action.powersaving.now_in_lpm";
            }

            public static final class EXTRA {
                public static final String IN_LPM = "in_lpm";
                public static final String IN_LPM_ANIMATION = "LpmAnimation";
                public static final String IN_LPM_BD = "LpmBackgroundData";
                public static final String IN_LPM_SMART_AMP_MODE = "com.fihtdc.extra.SMART_AMP_MODE";
                public static final String IN_LPM_VIBRATE = "LpmVibration";
                public static final String LPM_STILL_SETTING = "lpm_still_setting";
            }
        }

        public static final class LPMPARM {
            public static final String AUTOBRIGHTNESS = "AUTO";
            public static final int BATTERY_EXCESS = 5;
            public static final int DEFAULT_BACKLIGHT = 100;
            public static final int DEFAULT_TIMEOUT = 30000;
            public static final int MAXIMUM_BACKLIGHT = 255;
            public static final int MINIMUM_BACKLIGHT = 20;

            public static final class MESSAGE {
                public static final int APPLY_AGAIN_FINISH = 3;
                public static final int APPLY_FINISH = 0;
                public static final int RESTORE_FINISH = 1;
                public static final int RESTORE_FINISH_WHEN_RESTART = 2;
            }
        }

        public static final class LPMSPREF {
            public static final String ANIMATION = "lpm_animation";
            public static final String AUTOSYNC = "lpm_autosync";
            public static final String BACKGROUND_DATA = "lpm_background_data";
            public static final String BAM = "lpm_bam";
            public static final String BATTERY_SAVER = "lpm_battery_saver";
            public static final String BT = "lpm_bt";
            public static final String CPU_LIMIT = "lpm_cpu_limit";
            public static final String D3_SOUND = "lpm_3d_sound";
            public static final String GLANCE = "lpm_glance";
            public static final String GPS = "lpm_gps";
            public static final String MOBILE_DATA = "lpm_mobile_data";
            public static final String MONOCHROMACY = "lpm_monochromacy";
            public static final String SCREEN_LIGHT = "lpm_screen_light";
            public static final String SCREEN_RESOLUTION = "lpm__screen_resolution";
            public static final String SCREEN_TIMEOUT = "lpm_screen_timeout";
            public static final String VIBRATION = "lpm_vibrate";
            public static final String WIFI = "lpm_wifi";
            public static final String WIFI_HOTSPOT = "lpm_wifi_hotspot";
        }
    }

    public static final class LPM_AND_DC_APPLY {

        public static final class INTENT {

            public static final class ACTION {
                public static final String ACTION_DC_APPLY_END_BUT_STILL_HAS_LPM = "com.fihtdc.action.powersaving.dc.applay.end";
                public static final String ACTION_LPM_APPLY_END_BUT_STILL_HAS_DC = "com.fihtdc.action.powersaving.lpm.applay.end";
            }
        }

        public static final class ITEM {
            public static final int DC = 2;
            public static final int LPM = 1;
            public static final int NONE = 0;
        }

        public static final class PREF {
            public static final String IS_DC_APPLY = "is_DC_Apply";
            public static final String IS_LPM_APPLY = "is_LPM_Apply";
            public static final String WHO_FRIST = "Who_first";
        }
    }

    public static final class NOTIFICATION {
        public static final int BAM_NOTIFY = 2004;
        public static final int LPM_MODE = 2001;
        public static final int LPM_MODE_PERMISSION = 2002;
        public static final int PS_MODE = 2000;
        public static final int SMART_SWITCH_PERMISSION = 2003;

        public static final class ACTION {
            public static final String BAM_NEVER_SHOW = "com.evenwell.action.powersaving.ACTION_BAM_NEVER_SHOW";
            public static final String BAM_NOTIFICATION_EVENT = "com.evenwell.action.powersaving.ACTION_BAM_NOTIFICATION_EVENT";
            public static final String BAM_TURN_ON = "com.evenwell.action.powersaving.ACTION_BAM_TURN_ON";
        }

        public static final class EXTRA_DATA {
            public static final String CLICK_NOTI = "click_noti";
            public static final String NEVER_SHOW = "never_show";
            public static final String REMOVE = "remove";
            public static final String SHOW = "show";
            public static final String TURN_ON = "turn_on";
        }

        public static final class EXTRA_KEY {
            public static final String SHOW_COUNT = "show_count";
            public static final String TYPE = "type";
        }

        public static final class PREF {
            public static final String FIRST_NOTIFY_TIME = "notification_first_notify_time";
            public static final String NEVER_SHOW = "notification_never_show";
            public static final String SHOW_COUNT = "notification_show_count";
        }
    }

    public static final class PACKAGENAME {
        public static final String POWERMONITOR = "com.evenwell.PowerMonitor";
        public static final String POWERSAVING = "com.evenwell.powersaving.g3";
    }

    public static class PACKAGE_NAME {
        public static final String SYSTEM_UI = "com.android.systemui";
    }

    public static final class PERMISSION {

        public static final class REQUEST_CODE {
            public static final int READ_EXTERNAL_STORAGE_PERMISSION_REQ_CODE = 3102;
            public static final int SYSTEM_ALERT_WINDOW_PERMISSION_REQ_CODE = 3100;
            public static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQ_CODE = 3103;
            public static final int WRITE_SETTINGS_PERMISSION_REQ_CODE = 3101;
        }

        public static final class TRIGGER_FROM {
            public static final int BATTERY_CHANGED_INTENT = 0;
            public static final int POWERSAVER_COUNTDOWN_THREAD_SS_HOTSPOT = 11;
            public static final int POWERSAVER_COUNTDOWN_THREAD_SS_WIFI = 10;
            public static final int POWERSAVER_UI_DC_SWITCH = 6;
            public static final int POWERSAVER_UI_LPM_SCHEDULE = 13;
            public static final int POWERSAVER_UI_LPM_SWITCH = 8;
            public static final int POWERSAVER_UI_MAIN_DC_CHECKBOX = 2;
            public static final int POWERSAVER_UI_MAIN_LPM_CHECKBOX = 4;
            public static final int POWERSAVER_UI_MAIN_PW_CHECKBOX = 3;
            public static final int POWERSAVER_UI_MAIN_SS_CHECKBOX = 5;
            public static final int POWERSAVER_UI_MAIN_SWITCH = 1;
            public static final int POWERSAVER_UI_PW_SWITCH = 7;
            public static final int POWERSAVER_UI_SS_SWITCH = 9;
            public static final int RESTART_SERVICE_OR_BOOT_COMPLETED = 12;
        }

        public static final class TYPE {
            public static final int READ_EXTERNAL_STORAGE = 3002;
            public static final int SYSTEM_ALERT_WINDOW = 3000;
            public static final int WRITE_EXTERNAL_STORAGE = 3003;
            public static final int WRITE_SETTINGS = 3001;
        }
    }

    public static final class PW {

        public static final class PWPARM {
            public static final String ALLLIST = "app_list";
            public static final String ALLOWLIST = "allow_list";

            public static final class EMAIL_APK {
                public static final String ANDROID_EMAIL = "com.android.email";
                public static final String ANDROID_EMAIL_EXCHANGE = "com.android.exchange";
            }

            public static final class EMAIL_CHECK_MODE {
                public static final int EXIT_ALLOW = 1;
                public static final int IN_ALLOW = 0;
            }

            public static final class PWMODE {
                public static final int ALLOW_LIST = 0;
                public static final int APP_LIST = 1;
            }
        }
    }

    public static final class SERVICE_START {

        public static final class PSSPREF {
            public static final String BATTERY_LEVEL = "BatteryLevel";
            public static final String DISAUTO_VERSION = "disauto_version";
            public static final String DONE_VERSION = "doze_version";
            public static final String IS_BOOT_COMPLETE = "CheckBootComplete";
            public static final String IS_CHECK_DISAUTO_WAKEUP_DATABASE = "CheckDisAuto_WakeupDatabase";
            public static final String IS_REFRESH = "is_refresh";
            public static final String IS_SHOW_EXCEPTION_DIALOG = "ShowExceptionDailog";
            public static final String IS_SHOW_INSTALL_DIALOG = "ShowInstallDailog";
            public static final String POLLING_INTERVAL = "pollingInterval";
            public static final String PS_MODE_UI_STATUS = "ps_mode_ui_status";
            public static final String PULL_SERVER_TIME = "PullServerTime";
            public static final String SERVICE_START_REASON = "SERVICE_START_REASON";
        }

        public static final class REASON {
            public static final int BOOT_COMPLETED = 0;
            public static final int NORMAL_START = 2;
            public static final int SERVICE_CRASH = 1;
            public static final int UNKNOWN = 3;
        }
    }

    public static final class SETTINGDB {

        public static final class DATAUSAGEDB {
            public static final String APPDATAUSAGE = "AppDataUsage";
        }

        public static final class DCDB {
            public static final String PW_DATA_ALWAYSON = "powersaving_db_dc_alwayson";
            public static final String PW_DATA_DETECTTIME = "powersaving_db_dc_detect_time";
            public static final String PW_DATA_ENDTIME = "powersaving_db_dc_end_time";
            public static final String PW_DATA_HIDELIST = "powersaving_db_dc_hide_list";
            public static final String PW_DATA_STARTTIME = "powersaving_db_dc_start_time";
            public static final String PW_DATA_WHITELIST = "powersaving_db_dc_white_list";
        }

        public static final class FUNCTIONDB {
            public static final String ADD_ANIMATION = "ps_lpm_animation";
            public static final String ADD_BACKGROUND_DATA = "ps_lpm_background_data";
        }

        public static final class LPMDB {
            public static final String ANIMATION = "powersaving_db_lpm_animation";
            public static final String AUTOSYNC = "powersaving_db_autosync";
            public static final String BACKGROUND_DATA = "powersaving_db_lpm_background_data";
            public static final String BAM = "powersaving_db_screen_bam";
            public static final String BATTERY_INTENT_MIN_INTERVAL = "powersaving_db_battery_change_intent_min_interval";
            public static final String BATTERY_SAVER = "powersaving_db_battery_saver";
            public static final String BEGIN = "powersaving_db_power_saving_begin";
            public static final String BT = "powersaving_db_bt";
            public static final String CHANGE = "powersaving_db_power_saving_change";
            public static final String CPU_LIMIT = "powersaving_db_cpu_limit";
            public static final String D3_SOUND = "powersaving_db_3d_sound";
            public static final String DATA_CONNECTION = "powersaving_db_data_connection_new";
            public static final String EXTREME = "powersaving_db_power_saving_extreme";
            public static final String GLANCE = "powersaving_db_glance";
            public static final String GPS = "powersaving_db_gps";
            public static final String HOTSPOT_STATE = "powersaving_db_hotspot_state";
            public static final String MOBILE_DATA = "powersaving_db_mobile_data";
            public static final String MODE = "powersaving_db_power_saving_mode";
            public static final String MONOCHROMACY = "powersaving_db_monochromacy";
            public static final String SCREEN_LIGHT = "powersaving_db_screen_light";
            public static final String SCREEN_RESOLUTION = "powersaving_db_screen_resolution";
            public static final String SCREEN_TIMEOUT = "powersaving_db_screen_timeout";
            public static final String SMART_SWITCH = "powersaving_db_smart_switch";
            public static final String VIBRATION = "powersaving_db_lpm_vibrate";
            public static final String WIFI = "powersaving_db_wifi";
            public static final String WIFI_HOTSPOT = "powersaving_db_wifi_hotspot";
        }

        public static class PROCESS_MONITOR {
            public static final String SWITCH_NAME = "powersaving_db_process_monitor";
        }

        public static final class PSDB {
            public static final String AMOLED_DISPLAY = "powersaving_amoled_display";
            public static final String CPU_LIMIT_OPCODE_1_LIST = "powersaving_cpu_limit_opcode_1_list";
            public static final String CPU_LIMIT_OPCODE_2_LIST = "powersaving_cpu_limit_opcode_2_list";
            public static final String CPU_POLICY = "powersaving_db_cpu_policy";
            public static final String DATA_CONNECTION = "powersaving_db_data_connection";
            public static final String EXTREME_MODE_CPU_LIMIT_SPEED_LIST = "powersaving_extreme_mode_cpu_limit_speed_list";
            public static final String EXTREME_MODE_SAVE_TIME_LIST = "powersaving_extreme_mode_save_time_list";
            public static final String IS_NOT_SUPPORT_SCREEN_POLICY = "powersaving_not_support_screen_policy";
            public static final String IS_SUPPORT_CPU_POLICY = "powersaving_db_support_cpu";
            public static final String IS_SUPPORT_DOZE_MODE = "powersaving_support_doze_mode";
            public static final String IS_SUPPORT_LPA = "powersaving_db_support_lpa";
            public static final String IS_SUPPORT_TG_GUI = "powersaving_support_tg_gui";
            public static final String LPM = "powersaving_db_lpm";
            public static final String MAIN = "powersaving_db_main";
            public static final String NORMAL_MODE_CPU_LIMIT_SPEED_LIST = "powersaving_normal_mode_cpu_limit_speed_list";
            public static final String NORMAL_MODE_SAVE_TIME_LIST = "powersaving_normal_mode_save_time_list";
            public static final String POWER_SAVING_CONTROLLER_STATE = "powersaving_db_power_saving_mode";
            public static final String PS_SERVER = "powersaving_server";
            public static final String PW = "powersaving_db_periodic_wakeup";
            public static final String QCOM_CHIP_NAME = "powersaving_db_qcom_chip_name";
            public static final String SCREEN_POLICY = "powersaving_db_screen_policy";
            public static final String SERVICE_DETECT = "powersaving_db_service_detect";
            public static final String SERVICE_DETECT_TIME = "powersaving_db_service_detect_time";
            public static final String SHOW_BAM_PREFERENCE = "powersaving_show_bam";
            public static final String SS = "powersaving_db_ss";
            public static final String START_SERVICE_DELAY_TIME = "powersaving_start_service_delay_time";
        }

        public static final class PWDB {
            public static final String PW_PW_HIDELIST = "powersaving_db_pw_hide_list";
            public static final String PW_PW_REMOVELIST = "powersaving_db_pw_remove_list";
            public static final String PW_PW_TIME = "powersaving_db_pw_time";
            public static final String PW_PW_WHITELIST = "powersaving_db_pw_white_list";
        }

        public static final class SSDB {
            public static final String HOTSPOT = "powersaving_db_ss_hotspot";
            public static final String HOTSPOT_TIMEOUT = "powersaving_db_hotspot_timeout";
            public static final String SHOW_HOTSPOT = "powersaving_db_ss_show_hotspot";
            public static final String WIFI = "powersaving_db_ss_wifi";
            public static final String WIFI_TIMEOUT = "powersaving_db_wifi_timeout";
        }

        public static final class TSDB {
            public static final String TIME_SCHEDULE = "powersaving_db_time_schedule";
            public static final String TIME_SCHEDULE_END_TIME = "powersaving_db_time_schedule_end_time";
            public static final String TIME_SCHEDULE_MODE = "powersaving_db_time_schedule_mode";
            public static final String TIME_SCHEDULE_START_TIME = "powersaving_db_time_schedule_start_time";
        }
    }

    public static final class SS {

        public static final class INTENT {

            public static final class ACTION {
                public static final String ACTION_CHECK_HOTSPOT = "com.fihtdc.action.powersaving.ss.checkhotspot";
                public static final String ACTION_CHECK_WIFI = "com.fihtdc.action.powersaving.ss.checkwifi";
                public static final String ACTION_HOTSPOT_STATUS = "com.fihtdc.wifihotspot.connected.status";
            }
        }

        public static final class SSPARM {
            public static final String HOTSPOT = "hotspot";
            public static final String HOTSPOTSTATE = "hotspotstate";
            public static final String WIFI = "wifi";
        }
    }

    public static final class SWITCHER {
        public static final String KEEP = "KEEP";
        public static final String OFF = "OFF";
        public static final String ON = "ON";
    }

    public static final class SYMBOLS {
        public static final String COLON = ":";
        public static final String PERCENT = "%";
        public static final String SEMICOLON = ";";
        public static final String SPACE = " ";
        public static final String ZERO = "0";
    }

    public static final class SYSTEMUI_EXECUTE_PS {

        public static final class INTENT {

            public static final class ACTION {
                public static final String ACTION_SYSTEMUI_EXECUTE = "com.fihtdc.action.powersaving.systemui.execute.ps.update_ui";
            }
        }
    }

    public static class SettingPreference {
        public static final String ENABLE_BLACK_LIST = "enable_black_list";
    }

    public static final class TAG {
        public static final String PSLOG = "PowerSavingAppG3";
    }

    public static final class Theme {
        public static final String FIH_White_Theme = "Settings.Theme";
        public static final String Theme_DeviceDefault_Light = "Theme.DeviceDefault.Light";
        public static final String Theme_Holo_Light = "Theme.Holo.Light";
        public static final String Theme_Light = "Theme.Light";
    }
}
