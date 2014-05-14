
package edu.neu.rrc.wifisigstrdiag.ui;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.net.wifi.*;
import android.os.*;
import android.preference.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

import com.google.common.collect.*;
import com.google.common.primitives.*;

import java.util.*;

import butterknife.*;
import edu.neu.rrc.wifisigstrdiag.*;

public class MainFragment extends Fragment {

    enum ResultsAlgorithm {
        AVERAGE, MEDIAN, MIN, MAX
    }

    private static final String NUWAVE_SSID = "NUwave";
    private static final int LEVEL = 100;

    @InjectView(R.id.ssid) AutoCompleteTextView mSsid;
    @InjectView(R.id.go) Button mGo;
    @InjectView(R.id.results) TextView mResults;

    private WifiManager mWifiManager;

    // scan results are grouped into buckets of BSSID and frequency
    private Multimap<Pair<String, Integer>, ScanResult> mScanResults = HashMultimap.create();

    private String mSsidToSearch;
    private int mNumberOfScans = 0;
    private int mCurrentScan = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
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
        mSsid.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    mGo.performClick();
                }
                return true;
            }
        });
        mSsid.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mSsid.isEnabled())
                    mSsid.showDropDown();
                return false;
            }
        });
        mSsid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mGo.performClick();
            }
        });

        if (mCurrentScan >= 1)
            updateUiScanning();
        else
            reenableUi();
        repopulateSsidSuggestions();
        mSsid.dismissDropDown();
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
            // begin the first scan!
            mNumberOfScans = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_num_scans_key), "3"));
            mCurrentScan = 1;
            mSsidToSearch = mSsid.getText().toString();

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSsid.getWindowToken(), 0);
            if (mWifiManager.startScan()) {
                updateUiScanning();
            } else {
                reenableUi();
                Toast.makeText(getActivity(), "Could not start Wi-Fi scan. Please try again later.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @OnClick(R.id.label)
    public void onSsidLabelPressed() {
        mSsid.setText(NUWAVE_SSID);
    }

    private void repopulateSsidSuggestions() {
        List<ScanResult> networks = mWifiManager.getScanResults();

        // not as sexy as Guava's FluentIterable, but w/e
        Set<String> ssids = new HashSet<>();
        ssids.add("*");
        for (ScanResult result : networks) {
            ssids.add(result.SSID);
        }
        String[] ssidsArray = ssids.toArray(new String[ssids.size()]);

        mSsid.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, ssidsArray));
    }

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // return if this wasn't our request
            if (mCurrentScan < 1)
                return;

            // collect our data
            for (ScanResult result : mWifiManager.getScanResults()) {
                if (Utils.matchesWildcard(mSsidToSearch, result.SSID)) {
                    mScanResults.put(Pair.create(result.BSSID, result.frequency), result);
                }
            }
            repopulateSsidSuggestions();

            // start another scan if we need to
            if (mCurrentScan < mNumberOfScans) {
                mCurrentScan++;
                updateUiScanning();

                // if we failed, re-enable the UI
                if (!mWifiManager.startScan()) {
                    reenableUi();
                    Toast.makeText(getActivity(), String.format("Could not start Wi-Fi scan %1$d. Please try again later.", mCurrentScan), Toast.LENGTH_LONG).show();
                }
                return;
            }

            reenableUi();

            String chosenAlgo = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(getString(R.string.pref_results_algo_key), "average");

            // display the results!
            StringBuilder results = new StringBuilder();
            for (Pair<String, Integer> bssidAndFreq : mScanResults.keySet()) {
                Collection<ScanResult> bssidAndFreqResults = mScanResults.get(bssidAndFreq);

                ScanResult firstResult = bssidAndFreqResults.iterator().next();

                // print BSSID
                results.append("\nBSSID: ");
                results.append(firstResult.BSSID);

                // print SSID
                results.append("\nSSID: ");
                results.append(firstResult.SSID);

                // print frequency
                results.append("\nFrequency: ");
                results.append(firstResult.frequency);

                // print number of entries
                results.append("\nNumber of data points: ");
                results.append(bssidAndFreqResults.size());

                // print the algorithm used
                results.append("\nAlgorithm: ");
                results.append(chosenAlgo);

                // compute the level based on the algorithm
                ResultsAlgorithm algo;
                int level;
                try {
                    algo = ResultsAlgorithm.valueOf(chosenAlgo.toUpperCase());
                    switch (algo) {
                        case AVERAGE:
                            // faster to do it this way then map-reduce until Java 8 is supported on Android
                            long levelSum = 0;
                            for (ScanResult s : bssidAndFreqResults) {
                                levelSum += s.level;
                            }
                            level = (int) (levelSum / bssidAndFreqResults.size());
                            break;
                        case MEDIAN:
                            List<ScanResult> ordered = mOrderByIncreasingLevel.sortedCopy(bssidAndFreqResults);
                            // calculate median differently w/even number of entries
                            if ((ordered.size() & 1) == 0) {
                                level = (ordered.get(ordered.size() / 2).level + ordered.get(ordered.size() / 2 + 1).level) / 2;
                            } else {
                                level = ordered.get(ordered.size() / 2).level;
                            }
                            break;
                        case MIN:
                            level = mOrderByIncreasingLevel.min(bssidAndFreqResults).level;
                            break;
                        case MAX:
                            level = mOrderByIncreasingLevel.max(bssidAndFreqResults).level;
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                } catch (IllegalArgumentException ie) {
                    Toast.makeText(getActivity(), String.format("Algorithm %1$s not implemented.", chosenAlgo), Toast.LENGTH_LONG).show();
                    return;
                }

                // print the level
                results.append("\nLevel (dBm): ");
                results.append(level);

                results.append("\n=====\n\n");
            }

            // clear out the data
            mScanResults.clear();

            if (results.length() != 0) {
                mResults.setText(results.toString());
            } else {
                mResults.setText("Could not find a Wi-Fi signal for " + mSsidToSearch + ".");
            }
        }
    };

    private void updateUiScanning() {
        lockOrientation();
        mGo.setEnabled(false);
        mGo.setText(String.format(getString(R.string.measure_in_progress), mCurrentScan, mNumberOfScans));
        mSsid.setEnabled(false);
        mResults.setText("");
    }

    private void reenableUi() {
        unlockOrientation();
        mGo.setEnabled(true);
        mGo.setText(R.string.measure);
        mSsid.setEnabled(true);
        mCurrentScan = -1;
    }

    private Ordering<ScanResult> mOrderByIncreasingLevel = new Ordering<ScanResult>() {
        @Override
        public int compare(ScanResult left, ScanResult right) {
            return Ints.compare(left.level, right.level);
        }
    };

    // http://stackoverflow.com/a/14150037/832776
    private void lockOrientation() {
        int rotation = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        int orientation;
        switch (rotation) {
            case Surface.ROTATION_0:
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_90:
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            default:
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
        }

        getActivity().setRequestedOrientation(orientation);
    }

    private void unlockOrientation() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

}
