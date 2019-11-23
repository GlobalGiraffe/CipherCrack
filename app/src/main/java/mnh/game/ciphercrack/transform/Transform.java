package mnh.game.ciphercrack.transform;

import android.content.Context;

/**
 * Interface for all the simple text transformers: split, clear, reverse, upper/lower case, etc
 */
public interface Transform {
    String apply(Context context, String text);
}
