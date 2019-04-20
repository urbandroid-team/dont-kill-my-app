package com.evenwell.powersaving.g3.background;

import android.content.Context;
import com.evenwell.powersaving.g3.background.BAMMode.Mode;
import com.evenwell.powersaving.g3.utils.PSUtils;

public class PMSMode extends BAMMode {
    public PMSMode(Context context) {
        super(context);
    }

    @Mode
    public int getMode() {
        if (PSUtils.isCNModel(this.mContext)) {
            return 0;
        }
        if (PSUtils.enableTestFunction()) {
            return 4;
        }
        return 1;
    }
}
