package com.evenwell.powersaving.g3.backuptool;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.Settings.System;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.provider.PowerSavingProvider;
import com.evenwell.powersaving.g3.provider.PowerSavingProvider.SettingsColumns;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_KEY;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.TSDB;
import com.fihtdc.backuptool.BackupRestoreService;
import com.fihtdc.backuptool.BackupTool;
import com.fihtdc.backuptool.ZipUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import net2.lingala.zip4j.util.InternalZipConstants;

public class PowerSavingBackupRestoreService extends BackupRestoreService {
    public static final String BACKUP_FILE = "batterySettings.txt";
    private static final String DIVIDER_TAG_BATTERY_LEVEL_PERCENTAGE = "[P]";
    public static final String FOLDER_POWERSAVER = "PowerManagement";
    public static String RestoreFile = "";
    public static final String SEPARATOR = ":";
    private static final String TAG = "PowerSavingBackupRestoreService";
    public static Object mBackupLock = new Object();
    public static boolean openDialog = false;
    private String mBackupFile;
    private int mBackupTotalCount = 0;
    private int mBatteryShowPercent = -1;
    public boolean mCanceled = false;
    private FileOutputStream mOutStream;
    private Cursor mPowerSaverCursor = null;
    private int successCount = 0;

    private enum ProgressState {
        Prepare,
        Write,
        NoSpace
    }

    public Bundle backup(Bundle bundle) {
        Bundle results = new Bundle();
        Log.i(TAG, BackupTool.METHOD_BACKUP);
        String backupPath = bundle.getString(BackupTool.BACKUP_PATH, null);
        String backupZip = bundle.getString(BackupTool.BACKUP_ZIP, null);
        Log.i(TAG, "backupPath =" + backupPath);
        Log.i(TAG, "backupZip =" + backupZip);
        if (checkExternalStoragePermission()) {
            results.putBoolean(BackupTool.CHECK_PERMISSION, true);
            Log.i(TAG, "backup, granted permission.");
        } else {
            openDialog = true;
            if (!false && openDialog) {
                synchronized (mBackupLock) {
                    try {
                        Intent it = new Intent(getApplicationContext(), PowerSavingBackupRestorePermissionActivity.class);
                        it.setFlags(335544320);
                        if (checkReadExternalStoragePermission()) {
                            it.putExtra(EXTRA_KEY.TYPE, 3003);
                        } else {
                            it.putExtra(EXTRA_KEY.TYPE, 3002);
                        }
                        getApplicationContext().startActivity(it);
                        openDialog = true;
                        mBackupLock.wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (checkExternalStoragePermission()) {
                results.putBoolean(BackupTool.CHECK_PERMISSION, true);
                Log.i(TAG, "backup, granted permission.");
            } else {
                results.putBoolean(BackupTool.CHECK_PERMISSION, false);
                results.putInt(BackupTool.BACKUP_COUNT, 0);
                results.putInt(BackupTool.SUCCESS_COUNT, 0);
                Log.i(TAG, "backup, Not granted permission.");
                return results;
            }
        }
        if (backupInit()) {
            results = backupStart(backupPath, backupZip);
            backupEnd(backupPath, backupZip);
        }
        return results;
    }

    public Bundle restore(Bundle bundle) {
        Bundle results = new Bundle();
        Log.i(TAG, BackupTool.METHOD_RESTORE);
        Log.i(TAG, "-- backupPath: " + bundle.getString(BackupTool.BACKUP_PATH, null));
        Log.i(TAG, "-- backupZip: " + bundle.getString(BackupTool.BACKUP_ZIP, null));
        if (checkExternalStoragePermission()) {
            results.putBoolean(BackupTool.CHECK_PERMISSION, true);
            Log.i(TAG, "restore, granted permission.");
        } else {
            openDialog = true;
            if (!false && openDialog) {
                synchronized (mBackupLock) {
                    try {
                        Intent it = new Intent(getApplicationContext(), PowerSavingBackupRestorePermissionActivity.class);
                        it.setFlags(335544320);
                        if (checkReadExternalStoragePermission()) {
                            it.putExtra(EXTRA_KEY.TYPE, 3003);
                        } else {
                            it.putExtra(EXTRA_KEY.TYPE, 3002);
                        }
                        getApplicationContext().startActivity(it);
                        openDialog = true;
                        mBackupLock.wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (checkExternalStoragePermission()) {
                results.putBoolean(BackupTool.CHECK_PERMISSION, true);
                Log.i(TAG, "restore, granted permission.");
            } else {
                results.putBoolean(BackupTool.CHECK_PERMISSION, false);
                results.putInt(BackupTool.RESTORE_COUNT, 0);
                results.putInt(BackupTool.SUCCESS_COUNT, 0);
                Log.i(TAG, "restore, Not granted permission.");
                return results;
            }
        }
        boolean hasbackupZip = restoreInit(bundle);
        results = restoreStart();
        restoreEnd(hasbackupZip);
        return results;
    }

    public Bundle isBackedUp(Bundle bundle) {
        Log.i(TAG, BackupTool.METHOD_IS_BACKEDUP);
        return null;
    }

    public Bundle cancel(Bundle bundle) {
        Log.i(TAG, "cancel()");
        Bundle results = new Bundle();
        this.mCanceled = true;
        return results;
    }

    public Bundle checkPermission(Bundle bundle) {
        Bundle results = new Bundle();
        Log.i(TAG, BackupTool.METHOD_CHECK_PERMISSION);
        if (checkExternalStoragePermission()) {
            results.putBoolean(BackupTool.CHECK_PERMISSION, true);
            Log.i(TAG, "checkPermission, granted permission.");
        } else {
            openDialog = true;
            if (!false && openDialog) {
                synchronized (mBackupLock) {
                    try {
                        Intent it = new Intent(getApplicationContext(), PowerSavingBackupRestorePermissionActivity.class);
                        it.setFlags(335544320);
                        if (checkReadExternalStoragePermission()) {
                            it.putExtra(EXTRA_KEY.TYPE, 3003);
                        } else {
                            it.putExtra(EXTRA_KEY.TYPE, 3002);
                        }
                        getApplicationContext().startActivity(it);
                        openDialog = true;
                        mBackupLock.wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (checkExternalStoragePermission()) {
                results.putBoolean(BackupTool.CHECK_PERMISSION, true);
                Log.i(TAG, "checkPermission, granted permission.");
            } else {
                results.putBoolean(BackupTool.CHECK_PERMISSION, false);
                Log.i(TAG, "checkPermission, Not granted permission.");
            }
        }
        return results;
    }

    public boolean backupInit() {
        boolean result = false;
        try {
            String[] selectionArgs = new String[]{TSDB.TIME_SCHEDULE, TSDB.TIME_SCHEDULE_START_TIME, TSDB.TIME_SCHEDULE_END_TIME};
            this.mPowerSaverCursor = getContentResolver().query(PowerSavingProvider.CONTENT_URI, null, "name IN (?,?,?)", selectionArgs, null);
            if (this.mPowerSaverCursor != null) {
                this.mBackupTotalCount = this.mPowerSaverCursor.getCount();
                Log.i(TAG, "backupInit(): mBackupTotalCount 1 = " + this.mBackupTotalCount);
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.mBatteryShowPercent = System.getInt(getContentResolver(), "status_bar_show_battery_percent", -1);
            Log.i(TAG, "backupInit(): mBatteryShowPercent = " + this.mBatteryShowPercent);
            if (this.mBatteryShowPercent == -1) {
                return result;
            }
            this.mBackupTotalCount++;
            Log.i(TAG, "backupInit(): mBackupTotalCount 2 = " + this.mBackupTotalCount);
            return true;
        } catch (Exception e2) {
            e2.printStackTrace();
            return result;
        }
    }

    public Bundle backupStart(String backupPath, String backupZip) {
        Bundle results = new Bundle();
        Log.i(TAG, "backupStart(), backupPath =" + backupPath);
        Log.i(TAG, "backupStart(), backupZip =" + backupZip);
        if (this.mBackupTotalCount <= 0) {
            Log.i(TAG, "backupStart(): No records.");
        } else {
            if (backupPath == null) {
                backupPath = Environment.getExternalStorageDirectory().getPath() + "/." + FOLDER_POWERSAVER;
            }
            File backupFolder = new File(backupPath, FOLDER_POWERSAVER);
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }
            this.mBackupFile = backupFolder + File.separator + BACKUP_FILE;
            Log.i(TAG, "backupStart(): backup file:" + this.mBackupFile);
            try {
                StringBuffer mSB;
                byte[] buf;
                this.mOutStream = new FileOutputStream(this.mBackupFile);
                if (!(this.mPowerSaverCursor == null || !this.mPowerSaverCursor.moveToFirst() || this.mCanceled)) {
                    mSB = new StringBuffer();
                    mSB.setLength(0);
                    String name = this.mPowerSaverCursor.getString(this.mPowerSaverCursor.getColumnIndex(SettingsColumns.NAME));
                    mSB.append(name + ":" + this.mPowerSaverCursor.getString(this.mPowerSaverCursor.getColumnIndex(SettingsColumns.VALUE)) + "\r\n");
                    if (this.mOutStream != null) {
                        buf = mSB.toString().getBytes();
                        this.mOutStream.write(buf, 0, buf.length);
                        mSB.setLength(0);
                        this.successCount++;
                        updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
                    }
                    while (this.mPowerSaverCursor != null && this.mPowerSaverCursor.moveToNext() && !this.mCanceled) {
                        name = this.mPowerSaverCursor.getString(this.mPowerSaverCursor.getColumnIndex(SettingsColumns.NAME));
                        mSB.append(name + ":" + this.mPowerSaverCursor.getString(this.mPowerSaverCursor.getColumnIndex(SettingsColumns.VALUE)) + "\r\n");
                        if (this.mOutStream != null) {
                            buf = mSB.toString().getBytes();
                            this.mOutStream.write(buf, 0, buf.length);
                            mSB.setLength(0);
                            this.successCount++;
                            updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
                        }
                    }
                }
                if (!(this.mBatteryShowPercent == -1 || this.mCanceled || this.mOutStream == null)) {
                    mSB = new StringBuffer();
                    mSB.setLength(0);
                    mSB.append("[P]\r\n");
                    buf = mSB.toString().getBytes();
                    this.mOutStream.write(buf, 0, buf.length);
                    mSB.setLength(0);
                    mSB.append(String.valueOf(this.mBatteryShowPercent) + "\r\n");
                    buf = mSB.toString().getBytes();
                    this.mOutStream.write(buf, 0, buf.length);
                    mSB.setLength(0);
                    this.successCount++;
                    updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
                }
            } catch (IOException ioe) {
                updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.NoSpace);
                ioe.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "startBackup ,mBackupTotalCount = " + this.mBackupTotalCount + " successCount= " + this.successCount);
            results.putInt(BackupTool.BACKUP_COUNT, this.mBackupTotalCount);
            results.putInt(BackupTool.SUCCESS_COUNT, this.successCount);
        }
        return results;
    }

    public boolean backupEnd(String backupPath, String backupZip) {
        try {
            Log.i(TAG, "backupEnd()");
            if (this.mOutStream != null) {
                Log.i(TAG, "close mOutStream.");
                this.mOutStream.flush();
                this.mOutStream.close();
                this.mOutStream = null;
            }
            if (!(backupZip == null || this.mBackupFile == null)) {
                if (new File(this.mBackupFile).exists()) {
                    File file;
                    ZipUtils.addFiles(backupZip, FOLDER_POWERSAVER, new String[]{this.mBackupFile});
                    if (backupPath == null) {
                        file = new File(Environment.getExternalStorageDirectory().getPath() + "/." + FOLDER_POWERSAVER);
                    } else {
                        file = new File(backupPath);
                    }
                    if (file != null && file.exists()) {
                        DeleteRecursive(file);
                    }
                } else {
                    Log.i(TAG, "Zip target file " + this.mBackupFile + " is not exist.");
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "close mOutStream failed");
            e.printStackTrace();
        }
        if (this.mPowerSaverCursor != null) {
            this.mPowerSaverCursor.close();
            this.mPowerSaverCursor = null;
        }
        return true;
    }

    private void updateProgress(int currentCount, int totalCount, ProgressState state) {
        int progress = (currentCount * 100) / totalCount;
        Bundle args = new Bundle();
        if (state == ProgressState.Write) {
            args.putInt(BackupTool.PROGRESS_STATUS, 2);
        } else if (state == ProgressState.NoSpace) {
            args.putInt(BackupTool.PROGRESS_STATUS, 3);
        }
        args.putInt(BackupTool.PROGRESS_TOTAL_COUNT, totalCount);
        args.putInt(BackupTool.PROGRESS_CURRENT_COUNT, currentCount);
        Log.i(TAG, "updateProgress,totalCount = " + totalCount + " currentCount= " + currentCount);
        updateProgress(progress, args);
    }

    public boolean restoreInit(Bundle bundle) {
        Log.i(TAG, "restoreInit");
        this.mCanceled = false;
        this.successCount = 0;
        String backupPath = bundle.getString(BackupTool.BACKUP_PATH, null);
        Log.i(TAG, "-- backupPath: " + backupPath);
        String backupZip = bundle.getString(BackupTool.BACKUP_ZIP, null);
        Log.i(TAG, "-- backupZip: " + backupZip);
        String path = "";
        boolean backupaszip = false;
        if (backupPath != null && backupPath.length() > 0) {
            path = backupPath;
        } else if (backupZip != null && backupZip.length() > 0) {
            path = Environment.getExternalStorageDirectory().getPath() + InternalZipConstants.ZIP_FILE_SEPARATOR + FOLDER_POWERSAVER;
            new File(path).mkdirs();
            Log.i(TAG, "-- path: " + path);
            try {
                ZipUtils.extractFolder(backupZip, path, FOLDER_POWERSAVER);
            } catch (Exception e) {
                e.printStackTrace();
            }
            backupaszip = true;
        }
        Log.i(TAG, "-- backupaszip: " + backupaszip);
        RestoreFile = path + File.separator + FOLDER_POWERSAVER + File.separator + BACKUP_FILE;
        try {
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(RestoreFile))));
            Log.i(TAG, "restoreInit, mCanceled = " + this.mCanceled);
            while (true) {
                String line = bufferReader.readLine();
                if (line == null || this.mCanceled) {
                    break;
                } else if (!line.equals(DIVIDER_TAG_BATTERY_LEVEL_PERCENTAGE)) {
                    this.mBackupTotalCount++;
                }
            }
            Log.i(TAG, "restoreInit, mBackupTotalCount = " + this.mBackupTotalCount);
        } catch (Exception e2) {
            Log.i(TAG, "restoreInit failed.");
            e2.printStackTrace();
        }
        return backupaszip;
    }

    public Bundle restoreStart() {
        Bundle results = new Bundle();
        try {
            String line;
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(RestoreFile))));
            Log.i(TAG, "restoreStart, mCanceled = " + this.mCanceled);
            int restoreCount = 0;
            while (true) {
                line = bufferReader.readLine();
                if (line != null && !this.mCanceled && !line.equals(DIVIDER_TAG_BATTERY_LEVEL_PERCENTAGE)) {
                    String[] getStr = splitStringWithFirstSymbol(line, ":");
                    String name = getStr[0];
                    String value = getStr[1];
                    Log.i(TAG, "restoreStart, name: " + name + " , value: " + value);
                    PowerSavingUtils.setStringItemToSelfDB(this, name, value);
                    this.successCount++;
                    updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
                    restoreCount++;
                } else if (restoreCount > 0) {
                    PowerSavingBackRestoreUtils.restoreTimeScheduleSetting(this);
                }
            }
            if (restoreCount > 0) {
                PowerSavingBackRestoreUtils.restoreTimeScheduleSetting(this);
            }
            line = bufferReader.readLine();
            if (!(line == null || this.mCanceled)) {
                this.mBatteryShowPercent = Integer.parseInt(line);
                PowerSavingBackRestoreUtils.restoreBatteryShowPercentSetting(this, this.mBatteryShowPercent);
                this.successCount++;
                updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
            }
        } catch (Exception e) {
            Log.i(TAG, "restoreStart failed.");
            e.printStackTrace();
        }
        results.putInt(BackupTool.RESTORE_COUNT, this.mBackupTotalCount);
        results.putInt(BackupTool.SUCCESS_COUNT, this.successCount);
        return results;
    }

    public boolean restoreEnd(boolean backupaszip) {
        Log.d(TAG, "restoreEnd");
        if (backupaszip) {
            try {
                DeleteRecursive(new File(Environment.getExternalStorageDirectory().getPath() + InternalZipConstants.ZIP_FILE_SEPARATOR + FOLDER_POWERSAVER));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void DeleteRecursive(File dir) {
        Log.i(TAG, "DeleteRecursive the top path: " + dir.getPath());
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                File temp = new File(dir, file);
                if (temp.isDirectory()) {
                    Log.i(TAG, "Recursive Call" + temp.getPath());
                    DeleteRecursive(temp);
                } else {
                    Log.i(TAG, "Delete File" + temp.getPath());
                    if (!temp.delete()) {
                        Log.d(TAG, "DELETE FAIL");
                    }
                }
            }
        }
        dir.delete();
    }

    public static String[] splitStringWithFirstSymbol(String line, String symbol) {
        getStr = new String[2];
        int idx = line.indexOf(symbol);
        getStr[0] = line.substring(0, idx);
        getStr[1] = line.substring(idx + 1, line.length());
        return getStr;
    }

    protected synchronized void applySingle(String authority, ContentProviderOperation op) {
        ArrayList<ContentProviderOperation> ops = new ArrayList();
        ops.add(op);
        try {
            getContentResolver().applyBatch(authority, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e2) {
            e2.printStackTrace();
        }
    }

    public boolean checkExternalStoragePermission() {
        return checkReadExternalStoragePermission() && checkWriteExternalStoragePermission();
    }

    public boolean checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            Log.i(TAG, "Not grant read external storage permission.");
            return false;
        }
        Log.i(TAG, "Grant read external storage permission.");
        return true;
    }

    public boolean checkWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            Log.i(TAG, "Not grant write external storage permission.");
            return false;
        }
        Log.i(TAG, "Grant write external storage permission.");
        return true;
    }
}
