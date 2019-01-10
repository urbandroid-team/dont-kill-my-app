package com.fihtdc.backuptool;

import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import com.fihtdc.asyncservice.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import net2.lingala.zip4j.util.InternalZipConstants;

public class FileOperator {
    private static final boolean DEBUG = true;
    public static final int MAX_DIR_LENGTH = 1000;
    private static final int MSG_FILELIST_MSG_PASTE_CANCEL = 203;
    private static final int MSG_FILELIST_MSG_PASTE_FAILED = 202;
    private static final int MSG_FILELIST_MSG_PASTE_FINISH = 201;
    private static final String TAG = "FileOperator";
    private final int FILE_OPERATION_FAILED = 1;
    private final int FILE_OPERATION_FAILED_ORG_PATH = 3;
    private final int FILE_OPERATION_SUCCESS = 0;
    private final int FILE_OPERATION_WRONG_PARAMETER = 2;
    private boolean mCancel = false;
    private ArrayList<String> mCurFileNameList = new ArrayList();
    private int mCurrentCount = 0;
    private long mCurrentSize = 0;
    private Bundle mProgressInfo;
    private ArrayList<String> mRootFolder = null;
    private BackupRestoreService mService;
    private int mSuccessCount = 0;
    private long mTotalSize = 0;

    private class PasteAsyncTask {
        private String mPath;
        private int m_iResult = 0;

        public PasteAsyncTask(String strPath, Handler handler) {
            this.mPath = strPath;
        }

        public void execute() {
            doInBackground(new Void[0]);
            onPostExecute(null);
        }

        protected Void doInBackground(Void... params) {
            if (FileOperator.this.mCurFileNameList.size() != 0) {
                FileOperator.this.mTotalSize = 0;
                Iterator it = FileOperator.this.mCurFileNameList.iterator();
                while (it.hasNext()) {
                    FileOperator.this.mTotalSize = FileOperator.this.mTotalSize + FileOperator.getFileLength((String) it.next());
                }
                Log.d("55", "TOTAL SIZE: [" + FileOperator.this.mTotalSize + "]");
                FileOperator.this.mProgressInfo.putInt(BackupTool.PROGRESS_STATUS, 2);
                FileOperator.this.mProgressInfo.putInt(BackupTool.PROGRESS_TOTAL_COUNT, FileOperator.this.mCurFileNameList.size());
                FileOperator.this.mProgressInfo.putInt(BackupTool.PROGRESS_CURRENT_COUNT, 0);
                FileOperator.this.mService.updateProgress(0, FileOperator.this.mProgressInfo);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FileOperator.this.mCurrentSize = 0;
                FileOperator.this.mCurrentCount = 0;
                it = FileOperator.this.mCurFileNameList.iterator();
                while (it.hasNext()) {
                    String f = (String) it.next();
                    if (!FileOperator.this.mCancel) {
                        this.m_iResult = FileOperator.this.CopyFile(new File(f), this.mPath);
                        if (this.m_iResult != 0) {
                            break;
                        }
                        FileOperator.this.mSuccessCount = FileOperator.this.mSuccessCount + 1;
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            FileOperator.this.clear();
        }
    }

    public interface StatusListener {
        void onComplete(Bundle bundle);

        void updateProgress(int i, Bundle bundle);
    }

    public BackupRestoreService getService() {
        return this.mService;
    }

    public FileOperator(BackupRestoreService service) {
        this.mService = service;
        this.mProgressInfo = this.mService.getProgressInfo();
        this.mTotalSize = this.mService.getTotalSize();
    }

    public void copyFileList(ArrayList<String> files, String toPath) {
        this.mSuccessCount = 0;
        synchronized (this.mCurFileNameList) {
            this.mCurFileNameList.clear();
            if (files != null) {
                this.mCurFileNameList.addAll(files);
            }
        }
        PasteThread(toPath, null);
    }

    public void setRootFolder(ArrayList<String> root) {
        this.mRootFolder = root;
    }

    public int getSuccessCount() {
        return this.mSuccessCount;
    }

    private boolean PasteThread(String path, Handler handler) {
        new PasteAsyncTask(path, handler).execute();
        return true;
    }

    private int CopyFile(File file, String dest) {
        if (file == null || dest == null) {
            Log.e(TAG, "CopyFile: null parameter");
            return 2;
        }
        String destFile = copyFile(file.getAbsolutePath(), dest);
        this.mCurrentCount++;
        this.mProgressInfo.putInt(BackupTool.PROGRESS_CURRENT_COUNT, this.mCurrentCount);
        this.mService.updateProgress((int) (100.0d * this.mProgressInfo.getDouble(BackupTool.PROGRESS_PERCENT, 0.0d)), this.mProgressInfo);
        if (destFile == null) {
            return 1;
        }
        Log.e("seanli", "file: " + destFile);
        MediaScannerConnection.scanFile(this.mService.getApplicationContext(), new String[]{destFile}, null, null);
        return 0;
    }

    public void clear() {
        synchronized (this.mCurFileNameList) {
            this.mCurFileNameList.clear();
        }
    }

    public static String makePath(String path1, String path2) {
        if (path1.endsWith(File.separator)) {
            return path1 + path2;
        }
        return path1 + File.separator + path2;
    }

    private static String getExtFromFilename(String filename) {
        if (filename == null) {
            return "";
        }
        int dotPosition = filename.lastIndexOf(46);
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    private static String getNameFromFilename(String filename) {
        if (filename == null) {
            return "";
        }
        int dotPosition = filename.lastIndexOf(46);
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return filename;
    }

    private int isMaxFileName(String strName) {
        if (strName == null) {
            return -1;
        }
        return strName.getBytes().length - 255;
    }

    public static long getFileLength(String path) {
        if (path == null) {
            return 0;
        }
        File curFile = new File(path);
        if (curFile.isFile()) {
            return curFile.length();
        }
        ArrayList<String> folderArray = new ArrayList();
        long size = 0 + curFile.length();
        folderArray.add(curFile.getAbsolutePath());
        while (!folderArray.isEmpty()) {
            File[] files = new File((String) folderArray.remove(0)).listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.exists() && file.isFile()) {
                        size += file.length();
                    } else if (file.exists() && file.isDirectory()) {
                        folderArray.add(file.getAbsolutePath());
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }

    private String copyFile(String src, String dest) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        File file = new File(src);
        if (!file.exists() || file.isDirectory() || file.isHidden()) {
            LogUtils.logV(TAG, "copyFile: file not exist or is directory, " + src);
            return null;
        }
        FileInputStream fi = null;
        FileOutputStream fo = null;
        boolean bHaveExceptin = false;
        String destPath = null;
        double percent;
        try {
            FileInputStream fi2 = new FileInputStream(file);
            try {
                int i;
                if (this.mRootFolder != null) {
                    String root = getRootFolder(src, this.mRootFolder);
                    if (root != null) {
                        dest = dest + src.substring(root.length(), src.lastIndexOf(InternalZipConstants.ZIP_FILE_SEPARATOR));
                    }
                }
                File destPlace = new File(dest);
                if (!destPlace.exists()) {
                    destPlace.mkdirs();
                }
                destPath = makePath(dest, file.getName());
                File destFile = new File(destPath);
                String strExt = getExtFromFilename(file.getName());
                if (strExt == null || TextUtils.isEmpty(strExt)) {
                    strExt = "";
                    i = 1;
                } else {
                    strExt = "." + strExt;
                    i = 1;
                }
                while (destFile.exists()) {
                    int i2 = i + 1;
                    destPath = makePath(dest, getNameFromFilename(file.getName()) + SYMBOLS.SPACE + i + strExt);
                    destFile = new File(destPath);
                    i = i2;
                }
                if (isMaxFileName(destFile.getName()) > 0) {
                    LogUtils.logE(TAG, "Can not create file with Long name");
                    if (null != null) {
                        try {
                            new File(destPath).delete();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    if (fi2 != null) {
                        fi2.close();
                    }
                    if (fo != null) {
                        fo.close();
                    }
                    return null;
                } else if (destFile.createNewFile()) {
                    FileOutputStream fileOutputStream = new FileOutputStream(destFile);
                    try {
                        int progress;
                        long freeHeapSize = getFreeHeapSize();
                        if (freeHeapSize > 1048576) {
                            freeHeapSize = 1048576;
                        }
                        int ReadCount = (int) freeHeapSize;
                        Log.d(TAG, "ReadCount: [" + ReadCount + "]");
                        byte[] buffer = new byte[ReadCount];
                        int iLoopCount = 0;
                        long totalReadCount = 0;
                        while (true) {
                            int read = fi2.read(buffer, 0, ReadCount);
                            if (read == -1) {
                                break;
                            }
                            fileOutputStream.write(buffer, 0, read);
                            totalReadCount += (long) read;
                            iLoopCount++;
                            if (iLoopCount % 10 == 0) {
                                this.mCurrentSize = this.mService.getCurrentSize() + totalReadCount;
                                this.mService.setCurrentSize(this.mCurrentSize);
                                percent = ((double) this.mCurrentSize) / ((double) this.mTotalSize);
                                progress = (int) (100.0d * percent);
                                if (this.mService != null) {
                                    this.mProgressInfo.putDouble(BackupTool.PROGRESS_PERCENT, percent);
                                    this.mService.updateProgress(progress, this.mProgressInfo);
                                }
                                totalReadCount = 0;
                            }
                        }
                        if (!(this.mService == null || totalReadCount == 0)) {
                            this.mCurrentSize = this.mService.getCurrentSize() + totalReadCount;
                            this.mService.setCurrentSize(this.mCurrentSize);
                            percent = ((double) this.mCurrentSize) / ((double) this.mTotalSize);
                            progress = (int) (100.0d * percent);
                            this.mProgressInfo.putDouble(BackupTool.PROGRESS_PERCENT, percent);
                            this.mService.updateProgress(progress, this.mProgressInfo);
                        }
                        if (null != null) {
                            try {
                                new File(destPath).delete();
                            } catch (IOException e32) {
                                e32.printStackTrace();
                                return destPath;
                            }
                        }
                        if (fi2 != null) {
                            fi2.close();
                        }
                        if (fileOutputStream == null) {
                            return destPath;
                        }
                        fileOutputStream.close();
                        return destPath;
                    } catch (FileNotFoundException e4) {
                        e2 = e4;
                        fo = fileOutputStream;
                        fi = fi2;
                        try {
                            Log.e(TAG, "copyFile: file not found, " + src);
                            bHaveExceptin = true;
                            e2.printStackTrace();
                            LogUtils.logD(TAG, "copyFile() -- catch exception: send no space progress");
                            percent = this.mProgressInfo.getDouble(BackupTool.PROGRESS_PERCENT, 0.0d);
                            this.mProgressInfo.putInt(BackupTool.PROGRESS_STATUS, 3);
                            this.mService.updateProgress((int) (100.0d * percent), this.mProgressInfo);
                            if (1 != null) {
                                try {
                                    new File(destPath).delete();
                                } catch (IOException e322) {
                                    e322.printStackTrace();
                                    return null;
                                }
                            }
                            if (fi != null) {
                                fi.close();
                            }
                            if (fo != null) {
                                fo.close();
                            }
                            return null;
                        } catch (Throwable th2) {
                            th = th2;
                            if (bHaveExceptin) {
                                try {
                                    new File(destPath).delete();
                                } catch (IOException e3222) {
                                    e3222.printStackTrace();
                                    throw th;
                                }
                            }
                            if (fi != null) {
                                fi.close();
                            }
                            if (fo != null) {
                                fo.close();
                            }
                            throw th;
                        }
                    } catch (IOException e5) {
                        e3222 = e5;
                        fo = fileOutputStream;
                        fi = fi2;
                        bHaveExceptin = true;
                        Log.e(TAG, "copyFile: " + e3222.toString());
                        e3222.printStackTrace();
                        LogUtils.logD(TAG, "copyFile() -- catch exception: send no space progress");
                        percent = this.mProgressInfo.getDouble(BackupTool.PROGRESS_PERCENT, 0.0d);
                        this.mProgressInfo.putInt(BackupTool.PROGRESS_STATUS, 3);
                        this.mService.updateProgress((int) (100.0d * percent), this.mProgressInfo);
                        if (1 != null) {
                            try {
                                new File(destPath).delete();
                            } catch (IOException e32222) {
                                e32222.printStackTrace();
                                return null;
                            }
                        }
                        if (fi != null) {
                            fi.close();
                        }
                        if (fo != null) {
                            fo.close();
                        }
                        return null;
                    } catch (Throwable th3) {
                        th = th3;
                        fo = fileOutputStream;
                        fi = fi2;
                        if (bHaveExceptin) {
                            new File(destPath).delete();
                        }
                        if (fi != null) {
                            fi.close();
                        }
                        if (fo != null) {
                            fo.close();
                        }
                        throw th;
                    }
                } else {
                    if (null != null) {
                        try {
                            new File(destPath).delete();
                        } catch (IOException e322222) {
                            e322222.printStackTrace();
                        }
                    }
                    if (fi2 != null) {
                        fi2.close();
                    }
                    if (fo != null) {
                        fo.close();
                    }
                    return null;
                }
            } catch (FileNotFoundException e6) {
                e2 = e6;
                fi = fi2;
                Log.e(TAG, "copyFile: file not found, " + src);
                bHaveExceptin = true;
                e2.printStackTrace();
                LogUtils.logD(TAG, "copyFile() -- catch exception: send no space progress");
                percent = this.mProgressInfo.getDouble(BackupTool.PROGRESS_PERCENT, 0.0d);
                this.mProgressInfo.putInt(BackupTool.PROGRESS_STATUS, 3);
                this.mService.updateProgress((int) (100.0d * percent), this.mProgressInfo);
                if (1 != null) {
                    new File(destPath).delete();
                }
                if (fi != null) {
                    fi.close();
                }
                if (fo != null) {
                    fo.close();
                }
                return null;
            } catch (IOException e7) {
                e322222 = e7;
                fi = fi2;
                bHaveExceptin = true;
                Log.e(TAG, "copyFile: " + e322222.toString());
                e322222.printStackTrace();
                LogUtils.logD(TAG, "copyFile() -- catch exception: send no space progress");
                percent = this.mProgressInfo.getDouble(BackupTool.PROGRESS_PERCENT, 0.0d);
                this.mProgressInfo.putInt(BackupTool.PROGRESS_STATUS, 3);
                this.mService.updateProgress((int) (100.0d * percent), this.mProgressInfo);
                if (1 != null) {
                    new File(destPath).delete();
                }
                if (fi != null) {
                    fi.close();
                }
                if (fo != null) {
                    fo.close();
                }
                return null;
            } catch (Throwable th4) {
                th = th4;
                fi = fi2;
                if (bHaveExceptin) {
                    new File(destPath).delete();
                }
                if (fi != null) {
                    fi.close();
                }
                if (fo != null) {
                    fo.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e2 = e8;
            Log.e(TAG, "copyFile: file not found, " + src);
            bHaveExceptin = true;
            e2.printStackTrace();
            LogUtils.logD(TAG, "copyFile() -- catch exception: send no space progress");
            percent = this.mProgressInfo.getDouble(BackupTool.PROGRESS_PERCENT, 0.0d);
            this.mProgressInfo.putInt(BackupTool.PROGRESS_STATUS, 3);
            this.mService.updateProgress((int) (100.0d * percent), this.mProgressInfo);
            if (1 != null) {
                new File(destPath).delete();
            }
            if (fi != null) {
                fi.close();
            }
            if (fo != null) {
                fo.close();
            }
            return null;
        } catch (IOException e9) {
            e322222 = e9;
            bHaveExceptin = true;
            Log.e(TAG, "copyFile: " + e322222.toString());
            e322222.printStackTrace();
            LogUtils.logD(TAG, "copyFile() -- catch exception: send no space progress");
            percent = this.mProgressInfo.getDouble(BackupTool.PROGRESS_PERCENT, 0.0d);
            this.mProgressInfo.putInt(BackupTool.PROGRESS_STATUS, 3);
            this.mService.updateProgress((int) (100.0d * percent), this.mProgressInfo);
            if (1 != null) {
                new File(destPath).delete();
            }
            if (fi != null) {
                fi.close();
            }
            if (fo != null) {
                fo.close();
            }
            return null;
        }
    }

    private String getRootFolder(String src, ArrayList<String> rootList) {
        String rootFolder = null;
        Iterator it = rootList.iterator();
        while (it.hasNext()) {
            String root = (String) it.next();
            if (src.startsWith(root)) {
                rootFolder = root;
            }
        }
        return rootFolder;
    }

    private static long getFreeHeapSize() {
        return Runtime.getRuntime().freeMemory();
    }

    public void setCancel(boolean cancel) {
        this.mCancel = cancel;
    }

    public boolean isCanceled() {
        return this.mCancel;
    }
}
