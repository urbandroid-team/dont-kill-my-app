package com.evenwell.powersaving.g3.backuptool;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.exception.BlackFile;
import com.evenwell.powersaving.g3.exception.PowerSaverExceptionAppInfoItem;
import com.evenwell.powersaving.g3.provider.BackDataDb.SaveData;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_KEY;
import com.fihtdc.backuptool.BackupRestoreService;
import com.fihtdc.backuptool.BackupTool;
import com.fihtdc.backuptool.ZipUtils;
import com.fihtdc.push_system.lib.common.PushMessageContract;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import net2.lingala.zip4j.util.InternalZipConstants;

public class BamBackupRestoreService extends BackupRestoreService {
    public static final String BACKUP_FILE = "restrictedList.txt";
    private static final String DIVIDER_TAG_BAM_BLACKLIST = "[B]";
    private static final String DIVIDER_TAG_BAM_SHAREPREF = "[S]";
    public static final String FOLDER_POWERSAVER = "BackgroundActivityManager";
    public static String RestoreFile = "";
    public static final String SEPARATOR = ":";
    private static final String TAG = "BamBackupRestoreService";
    public static Object mBackupLock = new Object();
    public static boolean openDialog = false;
    private List<PowerSaverExceptionAppInfoItem> mAllAppsList = new ArrayList();
    private int mBAMBlackListVersion;
    private String mBackupFile;
    private int mBackupTotalCount = 0;
    private List<String> mBlackAppList;
    public boolean mCanceled = false;
    private int mDisautoBlackListVersion;
    private Cursor mDisautoCursor = null;
    private FileOutputStream mOutStream;
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
            this.mDisautoCursor = getContentResolver().query(Uri.withAppendedPath(Uri.parse("content://" + "com.evenwell.powersaving.g3.disautoprovider"), PushMessageContract.MESSAGE_KEY_PACKAGE_NAME), null, null, null, null);
            if (this.mDisautoCursor != null) {
                Log.i(TAG, "backupInit(): mDisautoCursor.getCount() = " + this.mDisautoCursor.getCount());
                this.mBackupTotalCount += this.mDisautoCursor.getCount();
                Log.i(TAG, "backupInit(): mBackupTotalCount 1 = " + this.mBackupTotalCount);
                if (this.mDisautoCursor.getCount() > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            List<String> whiteListApp = BackgroundPolicyExecutor.getInstance(getApplicationContext()).getWhiteListApp();
            this.mBlackAppList = PowerSavingUtils.getAllApList(getApplicationContext());
            this.mBlackAppList.removeAll(whiteListApp);
            Log.i(TAG, "backupInit(): mBlackAppList.size() = " + this.mBlackAppList.size());
            this.mBackupTotalCount += this.mBlackAppList.size();
            Log.i(TAG, "backupInit(): mBackupTotalCount 2 = " + this.mBackupTotalCount);
            if (this.mBlackAppList.size() > 0) {
                result = true;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            SharedPreferences prefStatus = getApplicationContext().getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0);
            this.mDisautoBlackListVersion = prefStatus.getInt(BlackFile.DISAUTO_BLACK_FILE_VERSION, -999);
            this.mBAMBlackListVersion = prefStatus.getInt(BlackFile.BAM_BLACK_FILE_VERSION, -999);
            if (this.mDisautoBlackListVersion == -999 || this.mBAMBlackListVersion == -999) {
                return result;
            }
            this.mBackupTotalCount += 2;
            Log.i(TAG, "backupInit(): mBackupTotalCount 3 = " + this.mBackupTotalCount);
            return true;
        } catch (Exception e22) {
            e22.printStackTrace();
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
                if (!(this.mDisautoCursor == null || !this.mDisautoCursor.moveToFirst() || this.mCanceled)) {
                    mSB = new StringBuffer();
                    mSB.setLength(0);
                    String name = this.mDisautoCursor.getString(this.mDisautoCursor.getColumnIndex("pkg_name"));
                    mSB.append(name + ":" + this.mDisautoCursor.getString(this.mDisautoCursor.getColumnIndex(SaveData.MUST_DIS_BOOT)) + "\r\n");
                    if (this.mOutStream != null) {
                        buf = mSB.toString().getBytes();
                        this.mOutStream.write(buf, 0, buf.length);
                        mSB.setLength(0);
                        this.successCount++;
                        updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
                    }
                    while (this.mDisautoCursor != null && this.mDisautoCursor.moveToNext() && !this.mCanceled) {
                        name = this.mDisautoCursor.getString(this.mDisautoCursor.getColumnIndex("pkg_name"));
                        mSB.append(name + ":" + this.mDisautoCursor.getString(this.mDisautoCursor.getColumnIndex(SaveData.MUST_DIS_BOOT)) + "\r\n");
                        if (this.mOutStream != null) {
                            buf = mSB.toString().getBytes();
                            this.mOutStream.write(buf, 0, buf.length);
                            mSB.setLength(0);
                            this.successCount++;
                            updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
                        }
                    }
                }
                if (!(this.mBlackAppList == null || this.mBlackAppList.size() <= 0 || this.mCanceled || this.mOutStream == null)) {
                    mSB = new StringBuffer();
                    mSB.setLength(0);
                    mSB.append("[B]\r\n");
                    buf = mSB.toString().getBytes();
                    this.mOutStream.write(buf, 0, buf.length);
                    mSB.setLength(0);
                    for (String pkg : this.mBlackAppList) {
                        mSB.append(pkg + "\r\n");
                        buf = mSB.toString().getBytes();
                        this.mOutStream.write(buf, 0, buf.length);
                        mSB.setLength(0);
                        this.successCount++;
                        updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
                    }
                }
                if (!(this.mDisautoBlackListVersion == -999 || this.mBAMBlackListVersion == -999 || this.mCanceled || this.mOutStream == null)) {
                    mSB = new StringBuffer();
                    mSB.setLength(0);
                    mSB.append("[S]\r\n");
                    buf = mSB.toString().getBytes();
                    this.mOutStream.write(buf, 0, buf.length);
                    mSB.setLength(0);
                    mSB.append(String.valueOf(this.mDisautoBlackListVersion) + "\r\n");
                    buf = mSB.toString().getBytes();
                    this.mOutStream.write(buf, 0, buf.length);
                    mSB.setLength(0);
                    this.successCount++;
                    updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
                    mSB.append(String.valueOf(this.mBAMBlackListVersion) + "\r\n");
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
        if (this.mDisautoCursor != null) {
            this.mDisautoCursor.close();
            this.mDisautoCursor = null;
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
                } else if (!(line.equals(DIVIDER_TAG_BAM_BLACKLIST) || line.equals(DIVIDER_TAG_BAM_SHAREPREF))) {
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
        String line;
        Editor editor;
        Bundle results = new Bundle();
        BackgroundPolicyExecutor BPE = BackgroundPolicyExecutor.getInstance(this);
        this.mAllAppsList = BPE.getAllApList();
        if (BPE.isCNModel()) {
            BPE.addAppsToDozeWhiteList(this.mAllAppsList);
        }
        BPE.removeAppsFromDisAutoList(this.mAllAppsList);
        BPE.addAppsToWhiteList(this.mAllAppsList);
        Log.i(TAG, "restoreStart, init app list finished");
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(RestoreFile))));
        Log.i(TAG, "restoreStart, mCanceled = " + this.mCanceled);
        List<String> appsList = new ArrayList();
        while (true) {
            line = bufferReader.readLine();
            if (line != null && !this.mCanceled && !line.equals(DIVIDER_TAG_BAM_BLACKLIST)) {
                try {
                    String[] getStr = splitStringWithFirstSymbol(line, ":");
                    String name = getStr[0];
                    Log.i(TAG, "restoreStart, name: " + name + " , value: " + getStr[1]);
                    appsList.add(name);
                    this.successCount++;
                    updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
                } catch (Exception e) {
                    Log.i(TAG, "restoreStart failed.");
                    e.printStackTrace();
                }
            }
        }
        if (appsList.size() > 0) {
            BPE.addAppsPkgToDisAutoList(appsList);
        }
        while (true) {
            line = bufferReader.readLine();
            if (line == null || this.mCanceled || line.equals(DIVIDER_TAG_BAM_SHAREPREF)) {
                editor = getApplicationContext().getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
                line = bufferReader.readLine();
            } else {
                Log.i(TAG, "restoreStart, name: " + line);
                BPE.removeAppFromWhiteList(line);
                this.successCount++;
                updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
            }
        }
        editor = getApplicationContext().getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit();
        line = bufferReader.readLine();
        if (!(line == null || this.mCanceled)) {
            editor.putInt(BlackFile.DISAUTO_BLACK_FILE_VERSION, Integer.parseInt(line)).commit();
            this.successCount++;
            updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
        }
        line = bufferReader.readLine();
        if (!(line == null || this.mCanceled)) {
            editor.putInt(BlackFile.BAM_BLACK_FILE_VERSION, Integer.parseInt(line)).commit();
            this.successCount++;
            updateProgress(this.successCount, this.mBackupTotalCount, ProgressState.Write);
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
