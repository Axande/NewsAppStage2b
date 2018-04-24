package com.example.andrei.newsappstage2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Andrei on 23.04.2018.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }

    public static class NewsPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        SharedPreferences.Editor editor;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            //set the editor for updating the page preference
            editor = getActivity().getPreferences(MODE_PRIVATE).edit();

            Preference prPageToLoad = findPreference(getString(R.string.settings_page_size_to_load));
            bindPreferenceSummaryToValue(prPageToLoad);

            Preference prCategory = findPreference(getString(R.string.settings_category));
            bindPreferenceSummaryToValue(prCategory);

            Preference prFind = findPreference(getString(R.string.settings_find));
            bindPreferenceSummaryToValue(prFind);

            Preference orderBy = findPreference(getString(R.string.settings_order));
            bindPreferenceSummaryToValue(orderBy);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            String stringValue = o.toString();
            preference.setSummary(stringValue);

            //reset the page when a change in the settings is done
            editor.putString(getString(R.string.settings_page), "1");
            editor.apply();

            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }
    }
}