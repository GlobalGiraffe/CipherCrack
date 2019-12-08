package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.widget.LinearLayout;

import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;

public class Atbash extends Cipher {

    Atbash(Context context) { super(context, "Atbash"); }

    /**
     * Describe what this cipher does
     * @return a description for this cipher, split across lines and paragraphs
     */
    @Override
    public String getCipherDescription() {
        return "The Atbash cipher is a mono-alphabetic substitution cipher where a=>Z, b=>Y, c=>X, etc.\n\n" +
                "To decipher, the exact same symmetrical mapping takes place.\n\n"+
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
        if (alphabet.length() % 2 != 0)
            return "Alphabet has odd length ("+alphabet.length()+") but needs to be even";

        // are we doing a crack
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
        // Nothing to create - no parameters
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives props) {
        // Nothing to find - no parameters
    }

    /**
     * Encode a text using Atbash cipher
     * @param plainText the text to be encoded
     * @param dirs a group of directives, mainly ALPHABET (string)
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        String alphabet = dirs.getAlphabet();
        StringBuilder sb = new StringBuilder(plainText.length());
        for (int pos=0; pos < plainText.length(); pos++) {
            char nextChar = plainText.charAt(pos);
            char nextCharUpper = Character.toUpperCase(nextChar);
            int ordinal = alphabet.indexOf(nextCharUpper);
            if (ordinal >= 0) {
                char cipherChar = alphabet.charAt(alphabet.length()-ordinal-1);
                sb.append((nextChar == nextCharUpper) ? cipherChar : Character.toLowerCase(cipherChar));
            } else {
                sb.append(nextChar);
            }
        }
        return sb.toString();
    }

    /**
     * Decode a cipher text using Atbash cipher, by just re-doing the encoding (symmetrical)
     * @param cipherText the text to be decoded
     * @param dirs a group of directives, mainly ALPHABET (string)
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        return encode(cipherText, dirs);
    }

    /**
     * Crack a Atbash cipher by simply decoding (there is only one encoding)
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        // we're very fast, but just in case, show some progress
        CrackResults.updateProgressDirectly(crackId, "Started "+getCipherName()+" crack");
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        String plainText = decode(cipherText, dirs);
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (Cipher.containsAllCribs(plainText, cribSet)) {
            String explain = "Success: Brute force approach: Applied Atbash and found all cribs ["
                    + cribString
                    + "].\n";
            return new CrackResult(crackMethod, this, dirs, cipherText, plainText, explain);
        }
        String explain = "Fail: Brute force approach: Applied Atbash bud did not find all cribs ["
                + cribString
                + "].\n";
        return new CrackResult(crackMethod, this, cipherText, explain);
    }
}
