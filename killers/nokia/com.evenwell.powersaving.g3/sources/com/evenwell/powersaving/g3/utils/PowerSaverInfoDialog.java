package com.evenwell.powersaving.g3.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.utils.PSConst.Function;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;

public class PowerSaverInfoDialog extends Activity {
    public static final String POWER_SAVER_DIALOG_INFO = "power_saver_dialog_info";
    private static String TAG = TAG.PSLOG;
    public final String DEFAULT_MSG = "Power saver product config error !";
    private final int LEGALTERM = Function.PW;
    private Context mContext;
    private ContextThemeWrapper themedContext;

    /* renamed from: com.evenwell.powersaving.g3.utils.PowerSaverInfoDialog$1 */
    class C04191 implements OnClickListener {
        C04191() {
        }

        public void onClick(DialogInterface dialog, int which) {
            PowerSaverInfoDialog.this.finish();
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.utils.PowerSaverInfoDialog$2 */
    class C04202 implements OnKeyListener {
        C04202() {
        }

        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
            if (keyCode == 4) {
                PowerSaverInfoDialog.this.finish();
            }
            return true;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.themedContext = new ContextThemeWrapper(this, 16974130);
        this.mContext = this;
        setContentView(C0321R.layout.handler_layout);
        if (PSUtils.isCNModel(this.mContext)) {
            setRequestedOrientation(1);
        }
        String msg = getIntent().getStringExtra(POWER_SAVER_DIALOG_INFO);
        if (msg == null) {
            msg = "Power saver product config error !";
        }
        showDialog(msg);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        getWindow().setFlags(67108864, 67108864);
    }

    public void onBackPressed() {
        finish();
    }

    private void showDialog(String msg) {
        Builder builder = new Builder(this.themedContext);
        builder.setTitle("Notice");
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new C04191());
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setOnKeyListener(new C04202());
    }
}
