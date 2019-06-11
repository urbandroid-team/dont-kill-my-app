package com.evenwell.powersaving.g3.special;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.PSDB;

public class SecretActivity extends PreferenceActivity implements OnPreferenceChangeListener {
    private String KEY_PREF_SHOW_BAM = "key_bam_preference";
    private String TAG = "SecretActivity";
    private SwitchPreference mSwitchBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0321R.layout.activity_secret);
        addPreferencesFromResource(C0321R.xml.secert_activity_preference);
        this.mSwitchBar = (SwitchPreference) findPreference(this.KEY_PREF_SHOW_BAM);
    }

    protected void onResume() {
        super.onResume();
        boolean isChecked = false;
        try {
            isChecked = PowerSavingUtils.getBooleanItemFromoDB(this, PSDB.SHOW_BAM_PREFERENCE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.mSwitchBar.setChecked(isChecked);
        this.mSwitchBar.setOnPreferenceChangeListener(this);
    }

    protected void onPause() {
        super.onPause();
        this.mSwitchBar.setOnPreferenceChangeListener(null);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isChecked = ((Boolean) newValue).booleanValue();
        Log.d(this.TAG, "onPreferenceChange checked = " + isChecked);
        try {
            PowerSavingUtils.setBooleanItemToDB(this, PSDB.SHOW_BAM_PREFERENCE, isChecked);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }
}
