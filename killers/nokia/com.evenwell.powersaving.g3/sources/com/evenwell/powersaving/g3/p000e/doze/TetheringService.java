package com.evenwell.powersaving.g3.p000e.doze;

import android.app.Service;

/* renamed from: com.evenwell.powersaving.g3.e.doze.TetheringService */
public abstract class TetheringService extends Service {
    public abstract int TetheringSize();

    public abstract boolean isTetheringOn();

    public abstract void setTethering(boolean z);
}
