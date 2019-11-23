package mnh.game.ciphercrack.transform;

import android.content.Context;

/**
 * Remove all characters in the text
 */
public class Clear implements Transform {

    @Override
    public String apply(Context context, String text) {
        return "";
    }
}
