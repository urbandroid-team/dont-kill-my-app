package com.fihtdc.push_system.lib.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Process;
import android.os.UserHandle;
import com.fihtdc.backuptool.FileOperator;

public class SystemAppUtil {
    public static ComponentName startService(Context context, Intent intent) {
        if (Process.myUid() == FileOperator.MAX_DIR_LENGTH) {
            return context.startServiceAsUser(intent, UserHandle.CURRENT);
        }
        return context.startService(intent);
    }

    public static boolean bindService(Context context, Intent intent, ServiceConnection svrConnection) {
        if (Process.myUid() == FileOperator.MAX_DIR_LENGTH) {
            return context.bindServiceAsUser(intent, svrConnection, 1, UserHandle.CURRENT);
        }
        return context.bindService(intent, svrConnection, 1);
    }

    public static void startActivity(Context context, Intent intent) {
        if (Process.myUid() == FileOperator.MAX_DIR_LENGTH) {
            context.startActivityAsUser(intent, UserHandle.CURRENT);
        } else {
            context.startActivity(intent);
        }
    }

    public static void sendBroadcast(Context context, Intent intent) {
        if (Process.myUid() == FileOperator.MAX_DIR_LENGTH) {
            context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        } else {
            context.sendBroadcast(intent);
        }
    }
}
