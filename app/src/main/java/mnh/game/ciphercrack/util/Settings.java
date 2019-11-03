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
    public static final String DEFAULT_CRIBS = "the,and,have";
    public static final String DEFAULT_LANGUAGE = "English";
    public static final String DEFAULT_PADDING_CHARS = " ";
    public static final Map<String, String> defaultSettings = new HashMap<>();
    static {
        defaultSettings.put("pref_cribs", DEFAULT_CRIBS);
        defaultSettings.put("pref_alphabet_plain", DEFAULT_ALPHABET);
        defaultSettings.put("pref_alphabet_cipher", DEFAULT_ALPHABET);
        defaultSettings.put("pref_language", DEFAULT_LANGUAGE);
        defaultSettings.put("pref_padding_chars", DEFAULT_PADDING_CHARS);
    }

    // no one can make an instance but me, and I only want one instance
    private Settings() { }

    public static synchronized Settings instance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public String getString(Context context, String prefName, String defaultString) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(prefName, defaultString);
    }

    public String getString(Context context, String prefName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultString = defaultSettings.get(prefName);
        return prefs.getString(prefName, (defaultString != null) ? defaultString : "");
    }

    public String getString(Context context, int prefId) {
        return getString(context, context.getString(prefId));
    }
}
