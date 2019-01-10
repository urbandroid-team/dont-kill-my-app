package com.evenwell.powersaving.g3.exception;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import java.util.ArrayList;
import java.util.List;

public class PowerSaverExceptionAdapterForCN extends BaseAdapter {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;
    private int mActionIconID = 0;
    private int mAppNameColor = 0;
    private Context mContext;
    private AlertDialog mDialog;
    private final LayoutInflater mInflater;
    private SelectionListener mListenerForBAM;
    private SelectionListener mListenerForDisauto;
    private List<PowerSaverExceptionAppInfoItem> mShowList = new ArrayList();

    /* renamed from: com.evenwell.powersaving.g3.exception.PowerSaverExceptionAdapterForCN$3 */
    class C03673 implements OnKeyListener {
        C03673() {
        }

        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
            PowerSaverExceptionAdapterForCN.this.mDialog.dismiss();
            return true;
        }
    }

    public interface SelectionListener {
        void onSelectionModeChange(int i, boolean z);
    }

    private static class ViewHolder {
        private CheckBox appBAM;
        private TextView appContent;
        private CheckBox appDisauto;
        private ImageView appIcon;
        private TextView appName;

        private ViewHolder() {
        }
    }

    public void setSelectionListenerForBAM(SelectionListener listener) {
        this.mListenerForBAM = listener;
    }

    public void setSelectionListenerForDisauto(SelectionListener listener) {
        this.mListenerForDisauto = listener;
    }

    public PowerSaverExceptionAdapterForCN(Context context) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mActionIconID = C0321R.drawable.fih_ps_add_button_light;
        this.mAppNameColor = context.getResources().getColor(C0321R.color.black);
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
        Log.i(TAG, "bindView Position = " + position);
        bindView(rowView, position);
        return rowView;
    }

    private View newView(ViewGroup parent) {
        View rowView = this.mInflater.inflate(C0321R.layout.lockscreen_app_list_for_cn, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.appIcon = (ImageView) rowView.findViewById(C0321R.id.appIcon);
        holder.appName = (TextView) rowView.findViewById(C0321R.id.appName);
        holder.appContent = (TextView) rowView.findViewById(C0321R.id.appNameProtect);
        holder.appBAM = (CheckBox) rowView.findViewById(C0321R.id.appCheckBoxhWidgetBAM);
        holder.appDisauto = (CheckBox) rowView.findViewById(C0321R.id.appCheckBoxhWidgetDisAuto);
        rowView.setTag(holder);
        return rowView;
    }

    public void bindView(View rowView, int position) {
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        final int mP = position;
        Log.i(TAG, "Position = " + position);
        if (this.mShowList != null && this.mShowList.size() > 0) {
            PowerSaverExceptionAppInfoItem ListItemInfo = (PowerSaverExceptionAppInfoItem) this.mShowList.get(position);
            if (BackgroundPolicyExecutor.getInstance(this.mContext).isWhitelisted(ListItemInfo.GetPackageName())) {
                Log.i(TAG, "[LockScreenAppProtectAdapterForCN] [ " + position + " ] " + ListItemInfo.GetPackageName());
                holder.appBAM.setOnCheckedChangeListener(null);
                holder.appBAM.setChecked(true);
            } else {
                holder.appBAM.setOnCheckedChangeListener(null);
                holder.appBAM.setChecked(false);
            }
            if (BackgroundPolicyExecutor.getInstance(this.mContext).isInDisautoList(ListItemInfo.GetPackageName())) {
                Log.i(TAG, "[DisautoAppProtectAdapterForCN] [ " + position + " ] " + ListItemInfo.GetPackageName());
                holder.appDisauto.setOnCheckedChangeListener(null);
                holder.appDisauto.setChecked(false);
            } else {
                holder.appDisauto.setOnCheckedChangeListener(null);
                holder.appDisauto.setChecked(true);
            }
            if (ListItemInfo.mHighConsumption) {
                holder.appContent.setText(this.mContext.getResources().getString(C0321R.string.fih_power_saving_doze_high_consumption));
                holder.appContent.setVisibility(0);
            } else {
                holder.appContent.setVisibility(8);
            }
            holder.appIcon.setVisibility(0);
            holder.appIcon.setImageDrawable(ListItemInfo.GetIcon());
            holder.appName.setTextColor(this.mAppNameColor);
            holder.appName.setText(ListItemInfo.GetAppName());
            holder.appName.setMaxLines(1);
            holder.appName.setEllipsize(TruncateAt.END);
            holder.appBAM.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                /* renamed from: com.evenwell.powersaving.g3.exception.PowerSaverExceptionAdapterForCN$1$1 */
                class C03611 implements OnClickListener {
                    C03611() {
                    }

                    public void onClick(DialogInterface dialog, int which) {
                        if (PowerSaverExceptionAdapterForCN.this.mListenerForBAM != null) {
                            PowerSaverExceptionAdapterForCN.this.mListenerForBAM.onSelectionModeChange(mP, true);
                        }
                    }
                }

                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    holder.appBAM.setOnCheckedChangeListener(null);
                    buttonView.setChecked(!isChecked);
                    Log.i(PowerSaverExceptionAdapterForCN.TAG, "[LockScreenAppProtectAdapterForCN] appBAM onCheckedChanged " + mP + ",isChecked : " + isChecked);
                    String message = "";
                    if (isChecked) {
                        message = PowerSaverExceptionAdapterForCN.this.mContext.getString(C0321R.string.alert_message_bam_on);
                    } else {
                        message = PowerSaverExceptionAdapterForCN.this.mContext.getString(C0321R.string.alert_message_bam_off);
                    }
                    PowerSaverExceptionAdapterForCN.this.showAlertDialog(message, new C03611(), new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            boolean z;
                            CompoundButton compoundButton = buttonView;
                            if (isChecked) {
                                z = false;
                            } else {
                                z = true;
                            }
                            compoundButton.setChecked(z);
                            if (PowerSaverExceptionAdapterForCN.this.mListenerForBAM != null) {
                                PowerSaverExceptionAdapterForCN.this.mListenerForBAM.onSelectionModeChange(mP, false);
                            }
                        }
                    });
                }
            });
            holder.appDisauto.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                /* renamed from: com.evenwell.powersaving.g3.exception.PowerSaverExceptionAdapterForCN$2$1 */
                class C03641 implements OnClickListener {
                    C03641() {
                    }

                    public void onClick(DialogInterface dialog, int which) {
                        if (PowerSaverExceptionAdapterForCN.this.mListenerForDisauto != null) {
                            PowerSaverExceptionAdapterForCN.this.mListenerForDisauto.onSelectionModeChange(mP, true);
                        }
                    }
                }

                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    holder.appDisauto.setOnCheckedChangeListener(null);
                    buttonView.setChecked(!isChecked);
                    Log.i(PowerSaverExceptionAdapterForCN.TAG, "[DisautoAppProtectAdapterForCN] appDisauto onCheckedChanged " + mP + ",isChecked : " + isChecked);
                    String message = "";
                    if (isChecked) {
                        message = PowerSaverExceptionAdapterForCN.this.mContext.getString(C0321R.string.alert_message_auto_activate_on);
                    } else {
                        message = PowerSaverExceptionAdapterForCN.this.mContext.getString(C0321R.string.alert_message_auto_activate_off);
                    }
                    PowerSaverExceptionAdapterForCN.this.showAlertDialog(message, new C03641(), new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            boolean z;
                            CompoundButton compoundButton = buttonView;
                            if (isChecked) {
                                z = false;
                            } else {
                                z = true;
                            }
                            compoundButton.setChecked(z);
                            if (PowerSaverExceptionAdapterForCN.this.mListenerForBAM != null) {
                                PowerSaverExceptionAdapterForCN.this.mListenerForBAM.onSelectionModeChange(mP, false);
                            }
                        }
                    });
                }
            });
        }
    }

    public void setApplist(List<PowerSaverExceptionAppInfoItem> ShowList) {
        this.mShowList.clear();
        this.mShowList.addAll(ShowList);
    }

    private void showAlertDialog(String message, OnClickListener positiveButtonListener, OnClickListener negativeButtonListener) {
        Log.i(TAG, "showAlertDialog");
        Builder builder = new Builder(this.mContext);
        builder.setMessage(message);
        builder.setPositiveButton(C0321R.string.power_saving_dialog_ok, positiveButtonListener);
        builder.setNegativeButton(C0321R.string.fih_power_saving_request_dialog_cancel, negativeButtonListener);
        this.mDialog = builder.create();
        this.mDialog.setCanceledOnTouchOutside(true);
        this.mDialog.setCancelable(true);
        this.mDialog.show();
        this.mDialog.setOnKeyListener(new C03673());
    }
}
