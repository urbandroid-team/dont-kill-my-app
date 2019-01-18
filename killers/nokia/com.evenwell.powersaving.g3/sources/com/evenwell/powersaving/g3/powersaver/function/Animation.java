package com.evenwell.powersaving.g3.powersaver.function;

import android.content.Context;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;

public class Animation extends Function {
    private static final float CLOSE_ANIMATION = 0.0f;
    private static final int INDEX_TRANSITION_ANIMATION = 1;
    private static final int INDEX_WINDOW_ANIMATION = 0;
    private static final float NORMAL_ANIMATION = 1.0f;
    private IWindowManager mWindowManager = Stub.asInterface(ServiceManager.getService("window"));

    private class Listener implements eventListener {
        private Listener() {
        }

        public void onClose() {
            Animation.this.setEnable(Animation.this.getValueFromDB());
        }

        public void onRestore() {
            String value = Animation.this.readFromBackFile();
            if (!TextUtils.isEmpty(value)) {
                Animation.this.setEnable(value);
            }
        }
    }

    public Animation(Context context) {
        super(context);
        setListener(new Listener());
    }

    protected String getRefBackUpFileKey() {
        return LPMSPREF.ANIMATION;
    }

    protected String getDBKey() {
        return LPMDB.ANIMATION;
    }

    public boolean getEnabled() {
        return !this.isClose;
    }

    protected void setEnable(String value) {
        if (!SWITCHER.KEEP.equals(value)) {
            boolean enabled;
            if (SWITCHER.ON.equals(value)) {
                enabled = true;
            } else {
                enabled = false;
            }
            if (enabled) {
                try {
                    this.mWindowManager.setAnimationScale(1, NORMAL_ANIMATION);
                    this.mWindowManager.setAnimationScale(0, NORMAL_ANIMATION);
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
            }
            this.mWindowManager.setAnimationScale(1, 0.0f);
            this.mWindowManager.setAnimationScale(0, 0.0f);
        }
    }
}
