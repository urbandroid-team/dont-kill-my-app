package com.evenwell.powersaving.g3.exception;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

final /* synthetic */ class PowerSaverExceptionAdapter$$Lambda$0 implements OnCheckedChangeListener {
    private final PowerSaverExceptionAdapter arg$1;
    private final int arg$2;
    private final ViewHolder arg$3;

    PowerSaverExceptionAdapter$$Lambda$0(PowerSaverExceptionAdapter powerSaverExceptionAdapter, int i, ViewHolder viewHolder) {
        this.arg$1 = powerSaverExceptionAdapter;
        this.arg$2 = i;
        this.arg$3 = viewHolder;
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        this.arg$1.lambda$bindView$0$PowerSaverExceptionAdapter(this.arg$2, this.arg$3, compoundButton, z);
    }
}
