package com.evenwell.powersaving.g3.background;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class TempWhiteList extends ArrayList<String> {
    private static final String apkLockUri = "content://com.android.systemui.recent/lock";
    private static TempWhiteList mInstance;
    private String TAG = "TempWhiteList";
    private List<OnListChangeListener> listeners;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.d(TempWhiteList.this.TAG, "onChange " + uri);
            TempWhiteList.this.mHandler.post(new TempWhiteList$1$$Lambda$0(this));
        }

        final /* synthetic */ void lambda$onChange$0$TempWhiteList$1() {
            synchronized (TempWhiteList.this.mLock) {
                TempWhiteList.this.mTempWhitelist = TempWhiteList.this.getAllLocksApp();
            }
            for (OnListChangeListener listener : TempWhiteList.this.listeners) {
                listener.onChange();
            }
        }
    };
    private Context mContext;
    private Handler mHandler;
    private final Object mLock = new Object();
    private List<String> mTempWhitelist;

    public interface OnListChangeListener {
        void onChange();
    }

    public static TempWhiteList getInstance(Context context) {
        if (mInstance == null) {
            synchronized (TempWhiteList.class) {
                if (mInstance == null) {
                    mInstance = new TempWhiteList(context);
                }
            }
        }
        return mInstance;
    }

    private TempWhiteList(Context context) {
        this.mContext = context.getApplicationContext();
        this.mTempWhitelist = new ArrayList();
        try {
            this.listeners = new ArrayList();
            Log.d(this.TAG, "mTempWhitelist " + this.mTempWhitelist);
            this.mContext.getContentResolver().registerContentObserver(Uri.parse(apkLockUri), true, this.mContentObserver);
            HandlerThread handlerThread = new HandlerThread("TPWL");
            handlerThread.start();
            this.mHandler = new Handler(handlerThread.getLooper());
            this.mHandler.post(new TempWhiteList$$Lambda$0(this));
        } catch (Exception e) {
            Log.d(this.TAG, e.getMessage());
        }
    }

    final /* synthetic */ void lambda$new$0$TempWhiteList() {
        this.mTempWhitelist = getAllLocksApp();
    }

    public List<String> get() {
        List<String> ret = new ArrayList();
        synchronized (this.mLock) {
            ret.addAll(this.mTempWhitelist);
        }
        return ret;
    }

    public void setOnListChangeListener(OnListChangeListener listener) {
        this.listeners.add(listener);
    }

    private List<String> getAllLocksApp() {
        List<String> ret = new ArrayList();
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(Uri.parse(apkLockUri), new String[]{"pkg_name", "use_id", "is_lock"}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int count = cursor.getCount();
                for (int i = 0; i < count; i++) {
                    cursor.moveToPosition(i);
                    String appLockString = cursor.getString(0);
                    if (cursor.getInt(2) == 1) {
                        ret.add(appLockString);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex2) {
            ex2.printStackTrace();
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex22) {
                    ex22.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex222) {
                    ex222.printStackTrace();
                }
            }
        }
        return ret;
    }
}
