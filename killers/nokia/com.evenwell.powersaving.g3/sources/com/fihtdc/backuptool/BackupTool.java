package com.fihtdc.backuptool;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import java.io.File;
import java.util.List;
import net2.lingala.zip4j.exception.ZipException;
import org2.apache.commons.io.FilenameUtils;

public final class BackupTool {
    public static final String BACKED_UP = "backedUp";
    public static final String BACKUP_COUNT = "backupCount";
    public static final String BACKUP_INFO = "Backup.info";
    public static final String BACKUP_PATH = "backupPath";
    public static final String BACKUP_SERVICE_NAME = "backupServiceName";
    public static final String BACKUP_SETTINGS = "Backup.settings";
    public static final String BACKUP_ZIP = "backupZip";
    public static final String CHECK_PERMISSION = "permissionSuccess";
    public static final String EXT_ZIP = ".zip";
    public static final String LIB_VERSION = "2.02";
    public static final String METHOD_BACKUP = "backup";
    public static final String METHOD_CANCEL = "cancel";
    public static final String METHOD_CHECK_PERMISSION = "checkPermission";
    public static final String METHOD_IS_BACKEDUP = "isBackedUp";
    public static final String METHOD_RESTORE = "restore";
    public static final String PROGRESS_CURRENT_COUNT = "currentCount";
    public static final String PROGRESS_NUMBER_FORMAT = "numberFormat";
    public static final String PROGRESS_PERCENT = "progressPercent";
    public static final int PROGRESS_STATE_NOSPACE = 3;
    public static final int PROGRESS_STATE_PREPARE = 1;
    public static final int PROGRESS_STATE_WRITE = 2;
    public static final String PROGRESS_STATUS = "progressStatus";
    public static final String PROGRESS_TOTAL_COUNT = "totalCount";
    public static final String REBOOT = "reboot";
    public static final String REPLACE = "replace";
    public static final String RESTORE_COUNT = "restoreCount";
    public static final String SERVICE_BACKUP = "com.fihtdc.backup";
    public static final String SESSION_ID = "sessionId";
    public static final String SUCCESS_COUNT = "successCount";
    public static final String VERSION_CODE = "versionCode";
    private static final String ZIP_PASSWORD = "password";

    public static boolean isBackupZip(String backupPath) {
        if (backupPath != null) {
            return backupPath.endsWith(EXT_ZIP);
        }
        return false;
    }

    public static List<ResolveInfo> queryBackupServices(PackageManager pm) {
        return pm.queryIntentServices(new Intent(SERVICE_BACKUP), 0);
    }

    public static String getZipPassword(Bundle task) {
        if (task.containsKey(ZIP_PASSWORD)) {
            return task.getString(ZIP_PASSWORD);
        }
        return null;
    }

    public static boolean containsZipPassword(Bundle task) {
        return task.containsKey(ZIP_PASSWORD);
    }

    public static void putZipPassword(Bundle task, String zipPassword) {
        task.putString(ZIP_PASSWORD, zipPassword);
    }

    public static String removeZipPassword(Bundle task) {
        String value = getZipPassword(task);
        task.remove(ZIP_PASSWORD);
        return value;
    }

    public static String getSessionId(Bundle task) {
        if (task.containsKey(SESSION_ID)) {
            return task.getString(SESSION_ID);
        }
        return null;
    }

    public static void putSessionId(Bundle task, String value) {
        task.putString(SESSION_ID, value);
    }

    public static String getBackupServiceName(Bundle task) {
        if (task.containsKey(BACKUP_SERVICE_NAME)) {
            return task.getString(BACKUP_SERVICE_NAME);
        }
        return null;
    }

    public static void putBackupServiceName(Bundle task, String value) {
        task.putString(BACKUP_SERVICE_NAME, value);
    }

    public static boolean containsBackupServiceName(Bundle task) {
        return task.containsKey(BACKUP_SERVICE_NAME);
    }

    public static boolean containsBackupInfo(String backupPathOrZip) {
        try {
            if ("zip".equalsIgnoreCase(FilenameUtils.getExtension(backupPathOrZip)) && ZipUtils.checkRootFile(backupPathOrZip, BACKUP_INFO)) {
                return true;
            }
            if (new File(backupPathOrZip, BACKUP_INFO).exists()) {
                return true;
            }
            return false;
        } catch (ZipException e) {
            return false;
        }
    }

    private BackupTool() {
    }
}
