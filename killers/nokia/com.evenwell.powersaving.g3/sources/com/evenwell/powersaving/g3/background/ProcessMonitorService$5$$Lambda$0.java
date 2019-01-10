package com.evenwell.powersaving.g3.background;

import android.os.Bundle;
import com.evenwell.powersaving.g3.background.ProcessMonitorService.C03335;

final /* synthetic */ class ProcessMonitorService$5$$Lambda$0 implements Runnable {
    private final C03335 arg$1;
    private final Bundle arg$2;

    ProcessMonitorService$5$$Lambda$0(C03335 c03335, Bundle bundle) {
        this.arg$1 = c03335;
        this.arg$2 = bundle;
    }

    public void run() {
        this.arg$1.lambda$processStart$0$ProcessMonitorService$5(this.arg$2);
    }
}
