package mnh.game.ciphercrack.transform;

import android.content.Context;

/**
 * Convert the characters in the string to upper case
 */
public class UpperCase extends Transform {

    @Override
    public String apply(Context context, String text) {
        if (text == null)
            return null;
        return text.toUpperCase();
    }
}
