package mnh.game.ciphercrack.transform;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Remove punctuation chars from a string
 */
public class RemovePunctuation extends Transform {

    private static final Pattern pattern = Pattern.compile("\\p{Punct}", 0);

    // this takes 8ms
    public String applySplit(Context context, String text) {
        if (text == null)
            return null;
        StringBuilder result = new StringBuilder(text.length());
        String[] items = pattern.split(text);
        for (String item : items) {
            result.append(item);
        }
        return result.toString();
    }

    // this takes 17ms
    public String applyScan(Context context, String text) {
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

    // this takes 5-6ms
    @Override
    public String apply(Context context, String text) {
        if (text == null)
            return null;
        StringBuilder result = new StringBuilder(text.length());
        Matcher m = pattern.matcher(text);
        int startPos = 0;
        while (m.find()) {
            result.append(text.substring(startPos, m.start()));
            startPos = m.end();
        }
        result.append(text.substring(startPos));
        return result.toString();
    }
}
