package mnh.game.ciphercrack.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    // The singleton instance
    private static Settings instance = null;

    // no one can make an instance but me, and I only want one instance
    private Settings() { }

    public static synchronized Settings instance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public String getString(Context context, String prefName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(prefName, "");
    }

    public String getString(Context context, int prefId) {
        return getString(context, context.getString(prefId));
    }
}
