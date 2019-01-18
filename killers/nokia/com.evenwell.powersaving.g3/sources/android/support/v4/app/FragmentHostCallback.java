package android.support.v4.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public abstract class FragmentHostCallback<E> extends FragmentContainer {
    private final Activity mActivity;
    private SimpleArrayMap<String, LoaderManager> mAllLoaderManagers;
    private boolean mCheckedForLoaderManager;
    final Context mContext;
    final FragmentManagerImpl mFragmentManager;
    private final Handler mHandler;
    private LoaderManagerImpl mLoaderManager;
    private boolean mLoadersStarted;
    private boolean mRetainLoaders;
    final int mWindowAnimations;

    @Nullable
    public abstract E onGetHost();

    public FragmentHostCallback(Context context, Handler handler, int windowAnimations) {
        this(null, context, handler, windowAnimations);
    }

    FragmentHostCallback(FragmentActivity activity) {
        this(activity, activity, activity.mHandler, 0);
    }

    FragmentHostCallback(Activity activity, Context context, Handler handler, int windowAnimations) {
        this.mFragmentManager = new FragmentManagerImpl();
        this.mActivity = activity;
        this.mContext = context;
        this.mHandler = handler;
        this.mWindowAnimations = windowAnimations;
    }

    public void onDump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
    }

    public boolean onShouldSaveFragmentState(Fragment fragment) {
        return true;
    }

    public LayoutInflater onGetLayoutInflater() {
        return (LayoutInflater) this.mContext.getSystemService("layout_inflater");
    }

    public void onSupportInvalidateOptionsMenu() {
    }

    public void onStartActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
        onStartActivityFromFragment(fragment, intent, requestCode, null);
    }

    public void onStartActivityFromFragment(Fragment fragment, Intent intent, int requestCode, @Nullable Bundle options) {
        if (requestCode != -1) {
            throw new IllegalStateException("Starting activity with a requestCode requires a FragmentActivity host");
        }
        this.mContext.startActivity(intent);
    }

    public void onRequestPermissionsFromFragment(@NonNull Fragment fragment, @NonNull String[] permissions, int requestCode) {
    }

    public boolean onShouldShowRequestPermissionRationale(@NonNull String permission) {
        return false;
    }

    public boolean onHasWindowAnimations() {
        return true;
    }

    public int onGetWindowAnimations() {
        return this.mWindowAnimations;
    }

    @Nullable
    public View onFindViewById(int id) {
        return null;
    }

    public boolean onHasView() {
        return true;
    }

    Activity getActivity() {
        return this.mActivity;
    }

    Context getContext() {
        return this.mContext;
    }

    Handler getHandler() {
        return this.mHandler;
    }

    FragmentManagerImpl getFragmentManagerImpl() {
        return this.mFragmentManager;
    }

    LoaderManagerImpl getLoaderManagerImpl() {
        if (this.mLoaderManager != null) {
            return this.mLoaderManager;
        }
        this.mCheckedForLoaderManager = true;
        this.mLoaderManager = getLoaderManager("(root)", this.mLoadersStarted, true);
        return this.mLoaderManager;
    }

    void inactivateFragment(String who) {
        if (this.mAllLoaderManagers != null) {
            LoaderManagerImpl lm = (LoaderManagerImpl) this.mAllLoaderManagers.get(who);
            if (lm != null && !lm.mRetaining) {
                lm.doDestroy();
                this.mAllLoaderManagers.remove(who);
            }
        }
    }

    void onAttachFragment(Fragment fragment) {
    }

    boolean getRetainLoaders() {
        return this.mRetainLoaders;
    }

    void doLoaderStart() {
        if (!this.mLoadersStarted) {
            this.mLoadersStarted = true;
            if (this.mLoaderManager != null) {
                this.mLoaderManager.doStart();
            } else if (!this.mCheckedForLoaderManager) {
                this.mLoaderManager = getLoaderManager("(root)", this.mLoadersStarted, false);
                if (!(this.mLoaderManager == null || this.mLoaderManager.mStarted)) {
                    this.mLoaderManager.doStart();
                }
            }
            this.mCheckedForLoaderManager = true;
        }
    }

    void doLoaderStop(boolean retain) {
        this.mRetainLoaders = retain;
        if (this.mLoaderManager != null && this.mLoadersStarted) {
            this.mLoadersStarted = false;
            if (retain) {
                this.mLoaderManager.doRetain();
            } else {
                this.mLoaderManager.doStop();
            }
        }
    }

    void doLoaderRetain() {
        if (this.mLoaderManager != null) {
            this.mLoaderManager.doRetain();
        }
    }

    void doLoaderDestroy() {
        if (this.mLoaderManager != null) {
            this.mLoaderManager.doDestroy();
        }
    }

    void reportLoaderStart() {
        if (this.mAllLoaderManagers != null) {
            int i;
            int N = this.mAllLoaderManagers.size();
            LoaderManagerImpl[] loaders = new LoaderManagerImpl[N];
            for (i = N - 1; i >= 0; i--) {
                loaders[i] = (LoaderManagerImpl) this.mAllLoaderManagers.valueAt(i);
            }
            for (i = 0; i < N; i++) {
                LoaderManagerImpl lm = loaders[i];
                lm.finishRetain();
                lm.doReportStart();
            }
        }
    }

    LoaderManagerImpl getLoaderManager(String who, boolean started, boolean create) {
        if (this.mAllLoaderManagers == null) {
            this.mAllLoaderManagers = new SimpleArrayMap();
        }
        LoaderManagerImpl lm = (LoaderManagerImpl) this.mAllLoaderManagers.get(who);
        if (lm != null) {
            lm.updateHostController(this);
            return lm;
        } else if (!create) {
            return lm;
        } else {
            lm = new LoaderManagerImpl(who, this, started);
            this.mAllLoaderManagers.put(who, lm);
            return lm;
        }
    }

    SimpleArrayMap<String, LoaderManager> retainLoaderNonConfig() {
        boolean retainLoaders = false;
        if (this.mAllLoaderManagers != null) {
            int i;
            int N = this.mAllLoaderManagers.size();
            LoaderManagerImpl[] loaders = new LoaderManagerImpl[N];
            for (i = N - 1; i >= 0; i--) {
                loaders[i] = (LoaderManagerImpl) this.mAllLoaderManagers.valueAt(i);
            }
            for (i = 0; i < N; i++) {
                LoaderManagerImpl lm = loaders[i];
                if (lm.mRetaining) {
                    retainLoaders = true;
                } else {
                    lm.doDestroy();
                    this.mAllLoaderManagers.remove(lm.mWho);
                }
            }
        }
        if (retainLoaders) {
            return this.mAllLoaderManagers;
        }
        return null;
    }

    void restoreLoaderNonConfig(SimpleArrayMap<String, LoaderManager> loaderManagers) {
        this.mAllLoaderManagers = loaderManagers;
    }

    void dumpLoaders(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.print(prefix);
        writer.print("mLoadersStarted=");
        writer.println(this.mLoadersStarted);
        if (this.mLoaderManager != null) {
            writer.print(prefix);
            writer.print("Loader Manager ");
            writer.print(Integer.toHexString(System.identityHashCode(this.mLoaderManager)));
            writer.println(":");
            this.mLoaderManager.dump(prefix + "  ", fd, writer, args);
        }
    }
}
