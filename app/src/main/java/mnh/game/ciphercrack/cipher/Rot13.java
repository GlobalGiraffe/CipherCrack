package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.widget.LinearLayout;

import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;

/**
 * Basically this is a Caesar cipher with shift 13
 */
public class Rot13 extends Caesar {

    private static final int ROT13_SHIFT = 13;

    Rot13(Context context) { super(context, "ROT13"); }

    /**
     * Describe what this cipher does
     * @return a description for this cipher, split across lines and paragraphs
     */
    @Override
    public String getCipherDescription() {
        return "The ROT13 cipher is a mono-alphabetic substitution cipher where each letter of the plain text is shifted by 13 places. " +
                "To decipher, the shift is reversed.\n\n"+
                "This is very easy to break since there is only one possible encoding for any piece of text.";
    }

    /**
     * Describe what this cipher does
     * @return a description for this cipher, split across lines and paragraphs
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher";
    }

    /**
     * Determine whether the directives are valid for this cipher type, and sets if they are
     * @param dirs the directives to be checked and set
     * @return return the reason for being invalid, or null if the directives ARE valid (and set)
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        String reason = super.canParametersBeSet(dirs);
        if (reason != null)
            return reason;
        String alphabet = dirs.getAlphabet();
        if (alphabet.length() != 26)
            return "Alphabet has length "+alphabet.length()+" but has to be 26 chars long";
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod != null && crackMethod != CrackMethod.NONE) {
            if (crackMethod != CrackMethod.BRUTE_FORCE)
                return "Invalid crack method";
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
        }
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // Nothing to create - shift is always 13
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        // Nothing to find - shift is always 13
    }

    // we don't add any extra controls, and we don't need to allow change of cribs
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        return false;
    }

    /**
     * Encode a text using ROT13 cipher by calling Caesar with shift=13
     * @param plainText the text to be encoded
     * @param dirs a group of directives, mainly ALPHABET (string)
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        if (dirs.getAlphabet().length() != 26)
            return "";
        dirs.setShift(ROT13_SHIFT);
        return super.encode(plainText, dirs);
    }

    /**
     * Decode a cipher text using ROT13 cipher by calling Caesar with shift 13
     * @param cipherText the text to be decoded
     * @param dirs a group of directives, mainly ALPHABET (string)
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        dirs.setShift(ROT13_SHIFT);
        return super.decode(cipherText, dirs);
    }

    /**
     * Crack a ROT13 cipher by simply reversing (there is only one encoding)
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    @Override
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        String plainText = decode(cipherText, dirs);
        if (Cipher.containsAllCribs(plainText, cribSet)) {
            dirs.setShift(ROT13_SHIFT);
            String explain = "Success: Brute force approach: Applied shift "
                    + ROT13_SHIFT
                    + " and found all cribs.\n";
            return new CrackResult(dirs.getCrackMethod(), this, dirs, cipherText, plainText, explain);
        }
        dirs.setShift(-1);
        String explain = "Fail: Brute force approach: Applied shift "
                + ROT13_SHIFT
                + " looking for the cribs ["
                + cribString
                + "] in the decoded text but did not find them.\n";
        return new CrackResult(dirs.getCrackMethod(), this, cipherText, explain);
    }
}
