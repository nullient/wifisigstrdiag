package edu.neu.rrc.wifisigstrdiag.ui;

import android.app.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import butterknife.*;
import edu.neu.rrc.wifisigstrdiag.*;

public class AboutFragment extends Fragment {

    @InjectView(R.id.version) TextView mVersion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
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
