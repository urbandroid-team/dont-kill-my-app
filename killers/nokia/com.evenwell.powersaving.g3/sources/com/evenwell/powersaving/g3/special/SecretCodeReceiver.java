package com.evenwell.powersaving.g3.special;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SecretCodeReceiver extends BroadcastReceiver {
    String TAG = "SecretCodeReceiver";

    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SECRET_CODE".equals(intent.getAction())) {
            Intent activity = new Intent(context, SecretActivity.class);
            activity.setFlags(268468224);
            context.startActivity(activity);
            Log.d(this.TAG, "onReceive and then start SecretActivity");
        }
    }
}
