package com.fihtdc.asyncservice;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RequestService extends Service implements Runnable, Callback {
    private static final boolean DEBUG = true;
    private static final Object FINISHED = new Object();
    private static final String TAG = "BackupRestoreService/RequestService";
    private static final int THREAD_NUMBER = 5;
    private static final ThreadLocal<Object> THREAD_TASK = new ThreadLocal();
    private static final ThreadFactory sThreadFactory = new C01381();
    private long mCurrentSize;
    private ExecutorService mExecutor = createTaskExecutor();
    private Handler mHandler;
    private Bundle mProgressInfo = new Bundle();
    private Messenger mService;
    private final BlockingQueue<Object> mTaskQueue = new LinkedBlockingQueue();
    private long mTotalSize;

    /* renamed from: com.fihtdc.asyncservice.RequestService$1 */
    static class C01381 implements ThreadFactory {
        private final AtomicInteger mCount = new AtomicInteger(1);

        C01381() {
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + this.mCount.getAndIncrement());
        }
    }

    public Bundle getProgressInfo() {
        return this.mProgressInfo;
    }

    public long getTotalSize() {
        return this.mTotalSize;
    }

    public void setTotalSize(long mTotalSize) {
        this.mTotalSize = mTotalSize;
    }

    public long getCurrentSize() {
        return this.mCurrentSize;
    }

    public void setCurrentSize(long mCurrentSize) {
        this.mCurrentSize = mCurrentSize;
    }

    private ExecutorService createTaskExecutor() {
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue();
        Log.e(TAG, "taskQueue:" + taskQueue + ":----sThreadFactory:" + sThreadFactory + ":----DiscardOldestPolicy:" + new DiscardOldestPolicy());
        return new ThreadPoolExecutor(5, 5, 0, TimeUnit.MILLISECONDS, taskQueue, sThreadFactory, new DiscardOldestPolicy());
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "RequestService - in onCreate: " + this);
        new Thread(this).start();
    }

    public IBinder onBind(Intent intent) {
        this.mHandler = new Handler(this);
        this.mService = new Messenger(this.mHandler);
        return this.mService.getBinder();
    }

    public boolean handleMessage(Message msg) {
        LogUtils.logD(TAG, "handleMessage() -- msg.what: " + msg.what);
        switch (msg.what) {
            case 3:
                Object task = msg.obj;
                Messenger replier = msg.replyTo;
                if (!(task instanceof RequestTask)) {
                    if (task instanceof Bundle) {
                        LogUtils.logD(TAG, "handleMessage() -- MessageType.MSG_ADD_REQUEST | task instanceof Bundle");
                        AsyncService.putReplier((Bundle) task, replier);
                        AsyncService.putVersionCode((Bundle) task);
                        this.mTaskQueue.offer(task);
                        break;
                    }
                }
                LogUtils.logD(TAG, "handleMessage() -- MessageType.MSG_ADD_REQUEST | task instanceof RequestTask");
                ((RequestTask) task).setReplier(replier);
                this.mTaskQueue.offer(task);
                break;
                break;
        }
        return true;
    }

    private void cancelRequest(final Object task) {
        LogUtils.logD(TAG, "cancelRequest(1) -- task: " + task);
        this.mExecutor.execute(new Runnable() {
            public void run() {
                RequestService.THREAD_TASK.set(task);
                if (task instanceof RequestTask) {
                    LogUtils.logD(RequestService.TAG, "cancelRequest(1) -- task instanceof RequestTask");
                    RequestService.this.cancelRequest((RequestTask) task);
                } else if (task instanceof Bundle) {
                    LogUtils.logD(RequestService.TAG, "cancelRequest(1) -- task instanceof Bundle");
                    RequestService.this.cancelRequest((Bundle) task);
                }
                RequestService.THREAD_TASK.remove();
            }
        });
    }

    private void handleRequest(final Object task) {
        LogUtils.logD(TAG, "handleRequest(1) -- task: " + task);
        this.mExecutor.execute(new Runnable() {
            public void run() {
                RequestService.THREAD_TASK.set(task);
                if (task instanceof RequestTask) {
                    LogUtils.logD(RequestService.TAG, "handleRequest(1) -- task instanceof RequestTask");
                    RequestService.this.handleRequest((RequestTask) task);
                } else if (task instanceof Bundle) {
                    LogUtils.logD(RequestService.TAG, "handleRequest(1) -- task instanceof Bundle");
                    RequestService.this.handleRequest((Bundle) task);
                }
                RequestService.THREAD_TASK.remove();
            }
        });
    }

    protected void cancelRequest(Bundle task) {
        doHandleRequest(task, true);
    }

    protected void handleRequest(Bundle task) {
        doHandleRequest(task, false);
    }

    protected void doHandleRequest(Bundle task, boolean cancel) {
        LogUtils.logD(TAG, "handleRequest(2)");
        Message message = new Message();
        try {
            String methodName = AsyncService.getMethodName(task);
            Log.i(TAG, "-- Request: " + methodName + " | Params: " + task);
            if (cancel) {
                LogUtils.logD(TAG, "handleRequest(2) --cancel request");
            } else {
                Bundle requestResults = (Bundle) ReflectUtils.invokeMethodOrThrow(this, methodName, new Class[]{Bundle.class}, new Object[]{task});
                Log.i(TAG, "-- Request: " + methodName + " | Results: " + requestResults);
                LogUtils.logD(TAG, "handleRequest(2) --requestResults: " + requestResults);
                if (requestResults != null) {
                    task.putAll(requestResults);
                }
            }
            message.what = 4;
        } catch (Throwable e) {
            LogUtils.logW(TAG, "handleRequest(2) --send MessageType.MSG_CALLBACK_EXCEPTION");
            AsyncService.putException(task, e);
            message.what = 5;
        }
        try {
            Messenger replier = AsyncService.getReplier(task);
            message.obj = task;
            replier.send(message);
        } catch (RemoteException e2) {
            Log.w(TAG, "Remote exception occurs when reply message to client", e2);
        }
    }

    protected void cancelRequest(RequestTask task) {
        doHandleRequest(task, true);
    }

    protected void handleRequest(RequestTask task) {
        doHandleRequest(task, false);
    }

    protected void doHandleRequest(RequestTask task, boolean cancel) {
        LogUtils.logD(TAG, "handleRequest(3)");
        Message message = new Message();
        try {
            Log.i(TAG, "-- Request: " + task.getMethodName() + " | Params: " + task.getRequestParams());
            if (cancel) {
                Log.i(TAG, "-- Request: " + task.getMethodName() + " cancel request: ");
            } else {
                Object requestResults;
                if (task.getRequestParams() instanceof Bundle) {
                    requestResults = ReflectUtils.invokeMethodOrThrow(this, task.getMethodName(), new Class[]{Bundle.class}, new Object[]{task.getRequestParams()});
                } else {
                    requestResults = ReflectUtils.invokeMethodOrThrow(this, task.getMethodName(), new Class[]{RequestParams.class}, new Object[]{task.getRequestParams()});
                }
                Log.i(TAG, "-- Request: " + task.getMethodName() + " | Results: " + requestResults);
                if (task.getRequestListener() != null) {
                    task.getRequestListener().onHandle(task);
                }
                task.setRequestResults(requestResults);
            }
            message.what = 4;
        } catch (Throwable e) {
            LogUtils.logW(TAG, "handleRequest(3) --send MessageType.MSG_CALLBACK_EXCEPTION");
            task.setException(new RequestException(e));
            message.what = 5;
        }
        try {
            message.obj = task;
            task.getReplier().send(message);
        } catch (RemoteException e2) {
            Log.w(TAG, "Remote exception occurs when reply message to client", e2);
        }
    }

    public void updateProgress(int progress) {
        LogUtils.logD(TAG, "updateProgress(1)");
        updateProgress(progress, null);
    }

    public void updateProgress(int progress, Bundle progressInfo) {
        Messenger replier;
        LogUtils.logD(TAG, "updateProgress(2)");
        try {
            if (getPackageManager().getPackageInfo("com.evenwell.backuptool", 1).versionCode <= 4204003) {
                progressInfo = null;
            }
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }
        Object task = THREAD_TASK.get();
        if (task instanceof Bundle) {
            AsyncService.putProgress((Bundle) task, progress);
            if (progressInfo != null) {
                AsyncService.putProgressInfo((Bundle) task, progressInfo);
            }
            replier = AsyncService.getReplier((Bundle) task);
        } else if (task instanceof RequestTask) {
            ((RequestTask) task).setProgress(progress);
            if (progressInfo != null) {
                ((RequestTask) task).setProgressInfo(progressInfo);
            }
            replier = ((RequestTask) task).getReplier();
        } else {
            LogUtils.logW(TAG, "updateProgress(2)");
            return;
        }
        Message message = new Message();
        message.what = 7;
        message.obj = task;
        try {
            replier.send(message);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException occurs when update progress", e);
        }
    }

    public void run() {
        LogUtils.logD(TAG, "run()");
        while (true) {
            Object task = null;
            try {
                task = this.mTaskQueue.take();
            } catch (InterruptedException e) {
                Log.e(TAG, "RequestService - InterruptedException in take task", e);
            }
            LogUtils.logD(TAG, "run() -- task: " + task);
            if (task != null && task != FINISHED) {
                Log.i(TAG, "RequestService - Handle request task: " + task);
                handleRequest(task);
            }
        }
        if (this.mTaskQueue.size() > 0) {
            Object[] objs = this.mTaskQueue.toArray();
            this.mTaskQueue.clear();
            for (Object obj : objs) {
                if (obj instanceof RequestTask) {
                    cancelRequest(obj);
                } else if (obj instanceof Bundle) {
                    cancelRequest(obj);
                }
            }
        }
        if (this.mExecutor != null) {
            LogUtils.logD(TAG, "run() -- shutdown executor");
            this.mExecutor.shutdown();
        }
    }

    public void onDestroy() {
        LogUtils.logD(TAG, "onDestroy()");
        super.onDestroy();
        this.mTaskQueue.offer(FINISHED);
    }
}
