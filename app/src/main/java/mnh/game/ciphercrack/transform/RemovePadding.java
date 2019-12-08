package mnh.game.ciphercrack.transform;

import android.content.Context;

import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.util.Settings;

/**
 * Remove padding chars from a string - padding is defined in the application preferences
 */
public class RemovePadding extends Transform {

    @Override
    public String apply(Context context, String text) {
        if (text == null)
            return null;
        // what are the padding characters? Context == null => unit test
        String paddingChars = (context == null)
                ? Settings.DEFAULT_PADDING_CHARS
                : Settings.instance().getString(context, R.string.pref_padding_chars);

        // rebuild the string without any padding characters
        StringBuilder result = new StringBuilder(text.length());
        for (int pos=0; pos < text.length(); pos++) {
            char c = text.charAt(pos);
            // only append if char is not in the set of padding chars
            if (paddingChars.indexOf(c) < 0)
                result.append(c);
        }
        return result.toString();
    }
}
