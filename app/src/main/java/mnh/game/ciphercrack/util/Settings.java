package mnh.game.ciphercrack.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public class Settings {

    // The singleton instance
    private static Settings instance = null;

    public static final String DEFAULT_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String DEFAULT_LANGUAGE = "English";
    public static final String DEFAULT_PADDING_CHARS = " \t\n";
    public static final String DEFAULT_LIMIT_GRAMS = "40";
    public static final String DEFAULT_LIMIT_PERM_COLS = "9";
    public static final String DEFAULT_LIMIT_RAILFENCE_RAILS = "20";
    private static final String DEFAULT_CRIBS = "the,and,have";
    private static final Map<String, String> defaultSettings = new HashMap<>();

    static {
        defaultSettings.put("pref_cribs", DEFAULT_CRIBS);
        defaultSettings.put("pref_alphabet_plain", DEFAULT_ALPHABET);
        defaultSettings.put("pref_alphabet_cipher", DEFAULT_ALPHABET);
        defaultSettings.put("pref_language", DEFAULT_LANGUAGE);
        defaultSettings.put("pref_padding_chars", DEFAULT_PADDING_CHARS);
        defaultSettings.put("pref_limit_grams", DEFAULT_LIMIT_GRAMS);
        defaultSettings.put("pref_limit_perm_brute_force_cols", DEFAULT_LIMIT_PERM_COLS);
        defaultSettings.put("pref_limit_railfence_rails", DEFAULT_LIMIT_RAILFENCE_RAILS);
    }

    // no one can make an instance but me, and I only want one instance
    private Settings() {
    }

    public static synchronized Settings instance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public String getString(Context context, String prefName, String defaultString) {
        if (context != null) { // could be in unit tests
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString(prefName, defaultString);
        } else {
            return defaultString;
        }
    }

    public String getString(Context context, String prefName) {
        String defaultString = defaultSettings.get(prefName);
        if (context != null) { // could be in unit tests
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString(prefName, (defaultString != null) ? defaultString : "");
        } else {
            return (defaultString != null) ? defaultString : "";
        }
    }

    public String getString(Context context, int prefId) {
        if (context != null) {
            return getString(context, context.getString(prefId));
        } else {
            return "";
        }
    }

    public String getString(Context context, int prefId, String defaultString) {
        if (context != null) {
            return getString(context, context.getString(prefId), defaultString);
        } else {
            return defaultString;
        }
    }
}
