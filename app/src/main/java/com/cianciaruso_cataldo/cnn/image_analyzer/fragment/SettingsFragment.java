package com.cianciaruso_cataldo.cnn.image_analyzer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.cianciaruso_cataldo.cnn.image_analyzer.widget.CustomEditTextPreference;
import com.cianciaruso_cataldo.cnn.image_analyzer.R;
import com.cianciaruso_cataldo.cnn.image_analyzer.activity.MainActivity;
import com.cianciaruso_cataldo.cnn.image_analyzer.activity.SettingsActivity;
import com.preference.PowerPreference;
import com.preference.Preference;

import es.dmoral.toasty.Toasty;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Context context = getActivity().getApplicationContext();
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

        Preference preference = PowerPreference.getDefaultFile();

        String animationsPref = getString(R.string.list_prefs_animations);
        String hostPref = getString(R.string.list_prefs_host);
        String portPref = getString(R.string.list_prefs_port);

        SwitchPreference animations = (SwitchPreference) findPreference(animationsPref);

        animations.setOnPreferenceChangeListener((pref, newValue) -> {
            preference.put("animations", (boolean) newValue);
            SettingsActivity.is_animations_enabled = (boolean) newValue;
            return true;
        });

        CustomEditTextPreference host = (CustomEditTextPreference) findPreference(hostPref);
        host.setPositiveButtonText("Ok");
        host.setNegativeButtonText("Cancel");
        host.setSummary(getString(R.string.list_prefs_summ_host) + "\nActual : " + MainActivity.server_address);
        host.setOnPreferenceChangeListener((pref, newValue) -> {
            if (((String) newValue).toLowerCase().startsWith("http://")||((String) newValue).toLowerCase().startsWith("https://")) {
                MainActivity.server_address = (String) newValue;
                preference.put("address", (String) newValue);
            } else {
                MainActivity.server_address = "https://" + newValue;
                preference.put("address", "https://" + newValue);
            }
            host.setSummary(getString(R.string.list_prefs_summ_host) + "\nActual : " + MainActivity.server_address);
            return true;
        });

        CustomEditTextPreference port = (CustomEditTextPreference) findPreference(portPref);
        port.setSummary(getString(R.string.list_prefs_summ_port) + "\nActual : " + MainActivity.port);
        port.setOnPreferenceChangeListener((pref, newValue) -> {
            try {
                Integer.parseInt((String) newValue);
            } catch (NumberFormatException e) {
                Toasty.error(getContext(), "You must enter a valid port number", Toast.LENGTH_SHORT, true).show();
                return false;
            }

            preference.put("port", Integer.parseInt((String) newValue));
            MainActivity.port = (int) Integer.parseInt((String) newValue);
            port.setSummary(getString(R.string.list_prefs_summ_port) + "\nActual : " + MainActivity.port);
            return true;

        });

    }


}
