
package edu.neu.rrc.wifisigstrdiag.ui;

import android.os.*;
import android.preference.*;

import edu.neu.rrc.wifisigstrdiag.*;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        findPreference(getString(R.string.pref_num_scans_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                try {
                    return Integer.parseInt((String) o) > 0;
                } catch (NumberFormatException nfe) {
                    return false;
                }
            }
        });
    }

}
