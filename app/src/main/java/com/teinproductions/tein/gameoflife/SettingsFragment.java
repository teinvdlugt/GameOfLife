package com.teinproductions.tein.gameoflife;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        findPreference(getString(R.string.background_color_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ColorPickerActivity.openActivity(getActivity(), MainActivity
                                .getColor(getActivity(), R.color.default_background_color),
                        getString(R.string.background_color_key));
                return true;
            }
        });
        findPreference(getString(R.string.cell_color_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ColorPickerActivity.openActivity(getActivity(), MainActivity
                                .getColor(getActivity(), R.color.default_cell_color),
                        getString(R.string.cell_color_key));
                return true;
            }
        });
        findPreference(getString(R.string.grid_color_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ColorPickerActivity.openActivity(getActivity(), MainActivity
                                .getColor(getActivity(), R.color.default_grid_color),
                        getString(R.string.grid_color_key));
                return true;
            }
        });
    }
}
