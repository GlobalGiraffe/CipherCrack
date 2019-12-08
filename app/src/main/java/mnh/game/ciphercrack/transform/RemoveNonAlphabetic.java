package mnh.game.ciphercrack.transform;

import android.content.Context;

import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.util.Settings;

/**
 * Remove all characters except those in the alphabet
 * The alphabet is defined in the application preferences
 */
public class RemoveNonAlphabetic extends Transform {

    @Override
    public String apply(Context context, String text) {
        if (text == null)
            return null;
        // what are the alphabetic characters? Context == null => unit test
        String alphabet = (context == null)
                ? Settings.DEFAULT_ALPHABET
                : Settings.instance().getString(context, R.string.pref_alphabet_cipher);

        // rebuild the string with only alphabetical characters
        StringBuilder result = new StringBuilder(text.length());
        for (int pos=0; pos < text.length(); pos++) {
            char c = text.charAt(pos);
            // only append if char is in the alphabet
            if (alphabet.indexOf(Character.toUpperCase(c)) >= 0)
                result.append(c);
        }
        return result.toString();
    }
}
