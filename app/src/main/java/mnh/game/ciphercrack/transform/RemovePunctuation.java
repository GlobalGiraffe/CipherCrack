package mnh.game.ciphercrack.transform;

import android.content.Context;
import java.util.regex.Pattern;

/**
 * Remove punctuation chars from a string
 */
public class RemovePunctuation extends Transform {

    @Override
    public String apply(Context context, String text) {
        if (text == null)
            return null;
        StringBuilder result = new StringBuilder(text.length());
        for (int pos=0; pos < text.length(); pos++) {
            String c = text.substring(pos, pos+1);
            // only append if char is not punctuation
            if (!Pattern.matches("\\p{Punct}", c))
                result.append(c);
        }
        return result.toString();
    }
}
