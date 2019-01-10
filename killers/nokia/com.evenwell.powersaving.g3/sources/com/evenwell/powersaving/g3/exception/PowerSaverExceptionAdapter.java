package com.evenwell.powersaving.g3.exception;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import java.util.ArrayList;
import java.util.List;

public class PowerSaverExceptionAdapter extends BaseAdapter {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;
    private List<PowerSaverExceptionAppInfoItem> mBlackList = new ArrayList();
    private Context mContext;
    private final LayoutInflater mInflater;
    private SelectionListener mListener;
    private List<PowerSaverExceptionAppInfoItem> mShowList = new ArrayList();

    public interface SelectionListener {
        void onSelectionModeChange(int i);
    }

    private static class ViewHolder {
        private Switch appActionIcon;
        private TextView appContent;
        private ImageView appIcon;
        private TextView appName;

        private ViewHolder() {
        }
    }

    public void setSelectionListener(SelectionListener listener) {
        this.mListener = listener;
    }

    public PowerSaverExceptionAdapter(Context context) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public int getCount() {
        return this.mShowList.size();
    }

    public Object getItem(int position) {
        return this.mShowList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        if (convertView == null) {
            rowView = newView(parent);
        } else {
            rowView = convertView;
        }
        bindView(rowView, position);
        return rowView;
    }

    private View newView(ViewGroup parent) {
        View rowView = this.mInflater.inflate(C0321R.layout.lockscreen_app_list, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.appIcon = (ImageView) rowView.findViewById(C0321R.id.appIcon);
        holder.appName = (TextView) rowView.findViewById(C0321R.id.appName);
        holder.appContent = (TextView) rowView.findViewById(C0321R.id.appNameProtect);
        holder.appActionIcon = (Switch) rowView.findViewById(C0321R.id.appSwitchWidget);
        rowView.setTag(holder);
        return rowView;
    }

    public void bindView(View rowView, int position) {
        ViewHolder holder = (ViewHolder) rowView.getTag();
        int mP = position;
        if (this.mShowList != null && this.mShowList.size() > 0) {
            PowerSaverExceptionAppInfoItem ListItemInfo = (PowerSaverExceptionAppInfoItem) this.mShowList.get(position);
            holder.appActionIcon.setOnCheckedChangeListener(null);
            holder.appActionIcon.setChecked(this.mBlackList.contains(ListItemInfo));
            if (ListItemInfo.mHighConsumption) {
                holder.appContent.setText(this.mContext.getResources().getString(C0321R.string.fih_power_saving_doze_high_consumption));
                holder.appContent.setVisibility(0);
            } else {
                holder.appContent.setVisibility(8);
            }
            holder.appIcon.setVisibility(0);
            holder.appIcon.setImageDrawable(ListItemInfo.GetIcon());
            holder.appName.setTextColor(ContextCompat.getColor(this.mContext, C0321R.color.black));
            holder.appName.setText(ListItemInfo.GetAppName());
            holder.appName.setMaxLines(1);
            holder.appName.setEllipsize(TruncateAt.END);
            holder.appActionIcon.setOnCheckedChangeListener(new PowerSaverExceptionAdapter$$Lambda$0(this, mP, holder));
        }
    }

    final /* synthetic */ void lambda$bindView$0$PowerSaverExceptionAdapter(int mP, ViewHolder holder, CompoundButton buttonView, boolean isChecked) {
        Log.i(TAG, "[LockScreenAppProtectAdapter] appActionIcon onCheckedChanged " + mP + ",isChecked : " + isChecked);
        holder.appActionIcon.setOnCheckedChangeListener(null);
        if (this.mListener != null) {
            this.mListener.onSelectionModeChange(mP);
        }
    }

    public void setApplist(List<PowerSaverExceptionAppInfoItem> showList, List<PowerSaverExceptionAppInfoItem> blackList) {
        this.mShowList.clear();
        this.mShowList.addAll(showList);
        this.mBlackList.clear();
        this.mBlackList.addAll(blackList);
    }
}
