package com.cianciaruso_cataldo.cnn.image_analyzer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.cianciaruso_cataldo.cnn.image_analyzer.widget.CustomEditTextPreference;
import com.cianciaruso_cataldo.cnn.image_analyzer.R;
import com.cianciaruso_cataldo.cnn.image_analyzer.activity.MainActivity;
import com.cianciaruso_cataldo.cnn.image_analyzer.activity.SettingsActivity;
import com.preference.PowerPreference;
import com.preference.Preference;

import java.util.Arrays;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Context context = getActivity().getApplicationContext();
        Preference preference = PowerPreference.getDefaultFile();

        String animationsPref = getString(R.string.list_prefs_animations);
        String hostPref = getString(R.string.list_prefs_host);

        SwitchPreference animations = (SwitchPreference) findPreference(animationsPref);

        animations.setOnPreferenceChangeListener((pref, newValue) -> {
            preference.put("animations", (boolean) newValue);
            SettingsActivity.is_animations_enabled = (boolean) newValue;
            return true;
        });

        CustomEditTextPreference host = (CustomEditTextPreference) findPreference(hostPref);
        host.setPositiveButtonText("Ok");
        host.setNegativeButtonText("Cancel");
        String address = "";
        for (String s : MainActivity.serverList) {
            address=address.concat(s + "\n");
        }
        host.setSummary(getString(R.string.list_prefs_summ_host)+ address);
        host.setOnPreferenceChangeListener((pref, newValue) -> {
            MainActivity.serverList.clear();
            MainActivity.serverList.addAll(Arrays.asList(((String) newValue).split(";")));
            preference.put("address", (String) newValue);
            String tmp = "";
            for (String s : MainActivity.serverList) {
                tmp=tmp.concat(s + "\n");
            }
            host.setSummary(getString(R.string.list_prefs_summ_host) + tmp);
            return true;
        });

    }


}
