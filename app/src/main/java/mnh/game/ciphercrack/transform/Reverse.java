package mnh.game.ciphercrack.transform;

import android.content.Context;

/**
 * Reverse the characters in the string
 */
public class Reverse extends Transform {

    @Override
    public String apply(Context context, String text) {
        if (text == null)
            return null;
        return new StringBuilder(text).reverse().toString();
    }
}
