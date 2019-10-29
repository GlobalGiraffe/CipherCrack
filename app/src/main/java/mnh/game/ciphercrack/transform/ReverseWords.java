package mnh.game.ciphercrack.transform;

import android.content.Context;

import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.util.Settings;

/**
 * Reverse each word in a string in place
 */
public class ReverseWords implements Transform {

    private static final Reverse reverseTransform = new Reverse();

    public String apply(Context context, String text) {
        if (text == null)
            return null;

        // what are the padding characters? Context == null => unit test
        String paddingChars = (context == null)
                ? " "
                : Settings.instance().getString(context, R.string.pref_padding_chars);

        // hold the overall text result
        StringBuilder result = new StringBuilder(text.length());
        // used to hold a word that needs to be reversed
        StringBuilder word = new StringBuilder(100);

        // iterate over the text, look for padding and reverse each word as it is completed
        for (int i=0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (paddingChars.indexOf(c) < 0) {
                word.append(c); // build up the word
            } else {
                if (word.length() > 0) { // we have gathered a word, so reverse it to the result
                    result.append(word.reverse());
                    word.setLength(0);
                }
                result.append(c); // add the padding character
            }
        }
        // the final word will need to be reversed
        if (word.length() > 0) {
            result.append(word.reverse());
        }
        return result.toString();
    }
}
