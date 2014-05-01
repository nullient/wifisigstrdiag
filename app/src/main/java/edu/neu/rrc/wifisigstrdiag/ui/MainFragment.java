
package edu.neu.rrc.wifisigstrdiag.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import edu.neu.rrc.wifisigstrdiag.R;

public class MainFragment extends Fragment {

    private static final String NUWAVE_SSID = "NUwave";
    private static final int LEVEL = 100;

    @InjectView(R.id.ssid) EditText mSsid;
    @InjectView(R.id.go) Button mGo;
    @InjectView(R.id.results) TextView mResults;

    WifiManager mWifiManager;

    private boolean mRequestedScan = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWifiManager = (WifiManager) getActivity().getSystemService(Activity.WIFI_SERVICE);
        ButterKnife.inject(this, getView());
        getActivity().registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        mGo.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        mSsid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                // n/a
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                // n/a
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mGo.setEnabled(!mSsid.getText().toString().trim().isEmpty());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        getActivity().unregisterReceiver(mWifiReceiver);
    }

    @OnClick(R.id.go)
    public void onGoPressed() {
        if (!mWifiManager.isWifiEnabled()) {
            Toast.makeText(getActivity(), "Please enable Wi-Fi!", Toast.LENGTH_LONG).show();
        } else {
            mRequestedScan = true;
            if (mWifiManager.startScan()) {
                mGo.setEnabled(false);
                mGo.setText(R.string.measure_disabled);
            } else {
                mRequestedScan = false;
                Toast.makeText(getActivity(), "Could not start Wi-Fi scan. Please try again later.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @OnClick(R.id.label)
    public void onSsidLabelPressed() {
        mSsid.setText(NUWAVE_SSID);
    }

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // return if this wasn't our request
            if (!mRequestedScan)
                return;

            mGo.setEnabled(true);
            mGo.setText(R.string.measure);

            for (ScanResult result : mWifiManager.getScanResults()) {
                if (NUWAVE_SSID.equals(result.SSID)) {
                    StringBuilder results = new StringBuilder();
                    results.append("SSID: ");
                    results.append(result.SSID);
                    results.append("\nBSSID: ");
                    results.append(result.BSSID);
                    results.append("\nFrequency: ");
                    results.append(result.frequency);
                    results.append("\nLevel (RSSI): ");
                    results.append(result.level);
                    results.append("\nLevel (out of " + LEVEL + "): ");
                    results.append(WifiManager.calculateSignalLevel(result.level, LEVEL));

                    mResults.setText(results.toString());
                    return;
                }
            }
            mResults.setText("Could not find a Wi-Fi signal for " + NUWAVE_SSID + ".");
        }
    };
}
