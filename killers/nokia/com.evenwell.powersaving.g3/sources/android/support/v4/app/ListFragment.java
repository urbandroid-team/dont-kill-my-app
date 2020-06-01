package android.support.v4.app;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ListFragment extends Fragment {
    static final int INTERNAL_EMPTY_ID = 16711681;
    static final int INTERNAL_LIST_CONTAINER_ID = 16711683;
    static final int INTERNAL_PROGRESS_CONTAINER_ID = 16711682;
    ListAdapter mAdapter;
    CharSequence mEmptyText;
    View mEmptyView;
    private final Handler mHandler = new Handler();
    ListView mList;
    View mListContainer;
    boolean mListShown;
    private final OnItemClickListener mOnClickListener = new C01752();
    View mProgressContainer;
    private final Runnable mRequestFocus = new C01741();
    TextView mStandardEmptyView;

    /* renamed from: android.support.v4.app.ListFragment$1 */
    class C01741 implements Runnable {
        C01741() {
        }

        public void run() {
            ListFragment.this.mList.focusableViewAvailable(ListFragment.this.mList);
        }
    }

    /* renamed from: android.support.v4.app.ListFragment$2 */
    class C01752 implements OnItemClickListener {
        C01752() {
        }

        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            ListFragment.this.onListItemClick((ListView) parent, v, position, id);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();
        FrameLayout root = new FrameLayout(context);
        LinearLayout pframe = new LinearLayout(context);
        pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);
        pframe.setOrientation(1);
        pframe.setVisibility(8);
        pframe.setGravity(17);
        pframe.addView(new ProgressBar(context, null, 16842874), new LayoutParams(-2, -2));
        root.addView(pframe, new LayoutParams(-1, -1));
        FrameLayout lframe = new FrameLayout(context);
        lframe.setId(INTERNAL_LIST_CONTAINER_ID);
        TextView tv = new TextView(getActivity());
        tv.setId(INTERNAL_EMPTY_ID);
        tv.setGravity(17);
        lframe.addView(tv, new LayoutParams(-1, -1));
        ListView lv = new ListView(getActivity());
        lv.setId(16908298);
        lv.setDrawSelectorOnTop(false);
        lframe.addView(lv, new LayoutParams(-1, -1));
        root.addView(lframe, new LayoutParams(-1, -1));
        root.setLayoutParams(new LayoutParams(-1, -1));
        return root;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureList();
    }

    public void onDestroyView() {
        this.mHandler.removeCallbacks(this.mRequestFocus);
        this.mList = null;
        this.mListShown = false;
        this.mListContainer = null;
        this.mProgressContainer = null;
        this.mEmptyView = null;
        this.mStandardEmptyView = null;
        super.onDestroyView();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    public void setListAdapter(ListAdapter adapter) {
        boolean z = false;
        boolean hadAdapter;
        if (this.mAdapter != null) {
            hadAdapter = true;
        } else {
            hadAdapter = false;
        }
        this.mAdapter = adapter;
        if (this.mList != null) {
            this.mList.setAdapter(adapter);
            if (!this.mListShown && !hadAdapter) {
                if (getView().getWindowToken() != null) {
                    z = true;
                }
                setListShown(true, z);
            }
        }
    }

    public void setSelection(int position) {
        ensureList();
        this.mList.setSelection(position);
    }

    public int getSelectedItemPosition() {
        ensureList();
        return this.mList.getSelectedItemPosition();
    }

    public long getSelectedItemId() {
        ensureList();
        return this.mList.getSelectedItemId();
    }

    public ListView getListView() {
        ensureList();
        return this.mList;
    }

    public void setEmptyText(CharSequence text) {
        ensureList();
        if (this.mStandardEmptyView == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        this.mStandardEmptyView.setText(text);
        if (this.mEmptyText == null) {
            this.mList.setEmptyView(this.mStandardEmptyView);
        }
        this.mEmptyText = text;
    }

    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }

    private void setListShown(boolean shown, boolean animate) {
        ensureList();
        if (this.mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        } else if (this.mListShown != shown) {
            this.mListShown = shown;
            if (shown) {
                if (animate) {
                    this.mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), 17432577));
                    this.mListContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), 17432576));
                } else {
                    this.mProgressContainer.clearAnimation();
                    this.mListContainer.clearAnimation();
                }
                this.mProgressContainer.setVisibility(8);
                this.mListContainer.setVisibility(0);
                return;
            }
            if (animate) {
                this.mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), 17432576));
                this.mListContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), 17432577));
            } else {
                this.mProgressContainer.clearAnimation();
                this.mListContainer.clearAnimation();
            }
            this.mProgressContainer.setVisibility(0);
            this.mListContainer.setVisibility(8);
        }
    }

    public ListAdapter getListAdapter() {
        return this.mAdapter;
    }

    private void ensureList() {
        if (this.mList == null) {
            View root = getView();
            if (root == null) {
                throw new IllegalStateException("Content view not yet created");
            }
            if (root instanceof ListView) {
                this.mList = (ListView) root;
            } else {
                this.mStandardEmptyView = (TextView) root.findViewById(INTERNAL_EMPTY_ID);
                if (this.mStandardEmptyView == null) {
                    this.mEmptyView = root.findViewById(16908292);
                } else {
                    this.mStandardEmptyView.setVisibility(8);
                }
                this.mProgressContainer = root.findViewById(INTERNAL_PROGRESS_CONTAINER_ID);
                this.mListContainer = root.findViewById(INTERNAL_LIST_CONTAINER_ID);
                View rawListView = root.findViewById(16908298);
                if (rawListView instanceof ListView) {
                    this.mList = (ListView) rawListView;
                    if (this.mEmptyView != null) {
                        this.mList.setEmptyView(this.mEmptyView);
                    } else if (this.mEmptyText != null) {
                        this.mStandardEmptyView.setText(this.mEmptyText);
                        this.mList.setEmptyView(this.mStandardEmptyView);
                    }
                } else if (rawListView == null) {
                    throw new RuntimeException("Your content must have a ListView whose id attribute is 'android.R.id.list'");
                } else {
                    throw new RuntimeException("Content has view with id attribute 'android.R.id.list' that is not a ListView class");
                }
            }
            this.mListShown = true;
            this.mList.setOnItemClickListener(this.mOnClickListener);
            if (this.mAdapter != null) {
                ListAdapter adapter = this.mAdapter;
                this.mAdapter = null;
                setListAdapter(adapter);
            } else if (this.mProgressContainer != null) {
                setListShown(false, false);
            }
            this.mHandler.post(this.mRequestFocus);
        }
    }
}
