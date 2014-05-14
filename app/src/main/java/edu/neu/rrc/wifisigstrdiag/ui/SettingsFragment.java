
package edu.neu.rrc.wifisigstrdiag.ui;

import android.content.pm.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import android.widget.*;

import butterknife.*;
import edu.neu.rrc.wifisigstrdiag.*;

public class SettingsFragment extends PreferenceFragment {

    @InjectView(R.id.version) TextView mVersion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, getActivity());

        String version = "???";
        try {
            version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mVersion.setText(getString(R.string.version, version));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
