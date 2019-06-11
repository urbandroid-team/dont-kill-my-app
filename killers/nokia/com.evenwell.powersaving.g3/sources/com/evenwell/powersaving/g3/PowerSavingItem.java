package com.evenwell.powersaving.g3;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;

public final class PowerSavingItem implements Parcelable {
    public static final Creator<PowerSavingItem> CREATOR = new C03181();
    public String m3DSound;
    public String mAutoSync;
    public String mBT;
    public String mBegin;
    public String mGlance;
    public String mGps;
    public String mLPMAnimation;
    public String mLPMBD;
    public String mLPMVibrate;
    public String mMobileData;
    public String mMonochromacy;
    public String mPowerSaverEnable;
    public String mScreenTimeout;
    public String mWifi;
    public String mWifiHotspot;

    /* renamed from: com.evenwell.powersaving.g3.PowerSavingItem$1 */
    static class C03181 implements Creator<PowerSavingItem> {
        C03181() {
        }

        public PowerSavingItem createFromParcel(Parcel p) {
            return new PowerSavingItem(p);
        }

        public PowerSavingItem[] newArray(int size) {
            return new PowerSavingItem[size];
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeString(this.mPowerSaverEnable);
        p.writeString(this.mBegin);
        p.writeString(this.mWifi);
        p.writeString(this.mGps);
        p.writeString(this.mBT);
        p.writeString(this.mMobileData);
        p.writeString(this.m3DSound);
        p.writeString(this.mScreenTimeout);
        p.writeString(this.mLPMAnimation);
        p.writeString(this.mLPMVibrate);
        p.writeString(this.mLPMBD);
        p.writeString(this.mWifiHotspot);
        p.writeString(this.mGlance);
        p.writeString(this.mAutoSync);
        p.writeString(this.mMonochromacy);
    }

    public PowerSavingItem() {
        this.mPowerSaverEnable = null;
        this.mBegin = null;
        this.mWifi = null;
        this.mGps = null;
        this.mBT = null;
        this.mMobileData = null;
        this.m3DSound = null;
        this.mScreenTimeout = null;
        this.mLPMAnimation = null;
        this.mLPMVibrate = null;
        this.mLPMBD = null;
        this.mWifiHotspot = null;
        this.mGlance = null;
        this.mAutoSync = null;
        this.mMonochromacy = null;
        this.mPowerSaverEnable = SWITCHER.KEEP;
        this.mBegin = "30";
        this.mWifi = SWITCHER.KEEP;
        this.mGps = SWITCHER.KEEP;
        this.mBT = SWITCHER.KEEP;
        this.mMobileData = SWITCHER.KEEP;
        this.m3DSound = SWITCHER.KEEP;
        this.mLPMAnimation = SWITCHER.KEEP;
        this.mLPMVibrate = SWITCHER.KEEP;
        this.mLPMBD = SWITCHER.KEEP;
        this.mScreenTimeout = "15000";
        this.mWifiHotspot = SWITCHER.KEEP;
        this.mGlance = SWITCHER.KEEP;
        this.mAutoSync = SWITCHER.KEEP;
        this.mMonochromacy = SWITCHER.OFF;
    }

    public PowerSavingItem(Parcel p) {
        this.mPowerSaverEnable = null;
        this.mBegin = null;
        this.mWifi = null;
        this.mGps = null;
        this.mBT = null;
        this.mMobileData = null;
        this.m3DSound = null;
        this.mScreenTimeout = null;
        this.mLPMAnimation = null;
        this.mLPMVibrate = null;
        this.mLPMBD = null;
        this.mWifiHotspot = null;
        this.mGlance = null;
        this.mAutoSync = null;
        this.mMonochromacy = null;
        this.mPowerSaverEnable = p.readString();
        this.mBegin = p.readString();
        this.mWifi = p.readString();
        this.mGps = p.readString();
        this.mBT = p.readString();
        this.mMobileData = p.readString();
        this.m3DSound = p.readString();
        this.mLPMAnimation = p.readString();
        this.mLPMVibrate = p.readString();
        this.mLPMBD = p.readString();
        this.mScreenTimeout = p.readString();
        this.mWifiHotspot = p.readString();
        this.mGlance = p.readString();
        this.mAutoSync = p.readString();
        this.mMonochromacy = p.readString();
    }
}
