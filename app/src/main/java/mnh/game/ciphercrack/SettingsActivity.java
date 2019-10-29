package mnh.game.ciphercrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Allow user to change preferences
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment())
                .commit();
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.application_settings, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.application_settings);
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            sp.registerOnSharedPreferenceChangeListener(this);
        }

        public void onResume() {
            super.onResume();
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            sp.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(sp, getString(R.string.pref_cribs));
            onSharedPreferenceChanged(sp, getString(R.string.pref_alphabet_cipher));
            onSharedPreferenceChanged(sp, getString(R.string.pref_alphabet_plain));
            onSharedPreferenceChanged(sp, getString(R.string.pref_language));
            onSharedPreferenceChanged(sp, getString(R.string.pref_padding_chars));
        }

        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        /**
         * If a preference changes, we may want to adjust the summary to allow users to easily
         * see what the preference now represents
         * @param sp the preference object that was changed
         * @param key the key of the preference
         */
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            // get some items we may use...
            Preference p = findPreference(key);
            String defaultAlphabet = getString(R.string.default_alphabet);

            // check each preference key that may need summary changed
            if (key.equals(getString(R.string.pref_cribs)))
                p.setSummary("Cribs in use: "+ sp.getString(key, "<none>"));
            if (key.equals(getString(R.string.pref_padding_chars))) {
                String padding = sp.getString(key, "<none>");
                if (padding != null && padding.equals(" ")) {
                    p.setSummary("Default padding with space");
                } else {
                    p.setSummary("Custom Padding: " + padding);
                }
            }
            if (key.equals(getString(R.string.pref_language)))
                p.setSummary(sp.getString(key, ""));
            if (key.equals(getString(R.string.pref_alphabet_plain))) {
                if (defaultAlphabet.equals(sp.getString(key, defaultAlphabet)))
                    p.setSummary("Default");
                else
                    p.setSummary("Custom");
            }
            if (key.equals(getString(R.string.pref_alphabet_cipher))) {
                if (defaultAlphabet.equals(sp.getString(key, defaultAlphabet)))
                    p.setSummary("Default");
                else
                    p.setSummary("Custom");
            }
        }
    }
}