package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.CrackState;
import mnh.game.ciphercrack.util.Directives;

/**
 * Class that contains methods to assist with Caesar Cipher operations
 * This monoalphabetic substitution cipher shifts each letter within the text by a
 *   fixed amount modulo the number of letters in the alphabet
 */
public class Caesar extends Cipher {

    int shift = -1;

    Caesar(Context context) { super(context, "Caesar"); }

    // needed for subclasses (ROT13)
    Caesar(Context context, String name) { super(context, name); }

    // used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(shift);
    }
    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        shift = in.readInt();
    }

    /**
     * Describe what this cipher does
     * @return a description for this cipher, split across lines and paragraphs
     */
    @Override
    public String getCipherDescription() {
        return "The Caesar cipher is a mono-alphabetic substitution cipher where each letter of the plain text is shifted by a fixed amount. " +
                "For example, if a shift of 3 is used then plain text 'a' will map to the 3rd letter after: 'D', 'b' will become 'E', and so on.\n" +
                "To decipher, the shift is reversed, so with shift of 3, 'E' becomes 'b', 'D' becomes 'a' and so on.\n\n"+
                "This is relatively easy to break since for each cipher text there are only 25 possible decipherments.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher ("+((shift == -1) ? "n/a" : "shift="+shift)+")";
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
        CrackMethod crackMethod = dirs.getCrackMethod();
        int shift = dirs.getShift();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            String alphabet = dirs.getAlphabet();
            if (shift < 0 || shift >= alphabet.length())
                return "Shift value " + shift + " incorrect, should be between 0 and " + (alphabet.length() - 1);
        } else {
            // this the the only crack possible
            if (crackMethod != CrackMethod.BRUTE_FORCE)
                return "Invalid crack method";
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
        }
        this.shift = shift;
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_caesar);

        // create an array of possible cipher shifts for the user to choose from
        Integer[] shiftArray = new Integer[alphabet.length()];
        for (int i = 0; i < alphabet.length(); i++) {
            shiftArray[i] = i;
        }

        // Create an ArrayAdapter and default layout for the spinner
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, shiftArray);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Create a spinner and apply the adapter to it
        Spinner spinner = layout.findViewById(R.id.extra_caesar_spinner);
        spinner.setAdapter(adapter);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        Spinner spinner = layout.findViewById(R.id.extra_caesar_spinner);
        int shift = (int)spinner.getSelectedItem();
        dirs.setShift(shift);
    }

    // we don't add any extra controls, but we allow change of cribs
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String cipherText,
                                    Language language, String alphabet, String paddingChars) {
        return true;
    }

    /**
     * Caesar encode a single character in a specific alphabet
     * @param input the character to be encoded
     * @param shift the number of characters to shift by
     * @param alphabet the alphabet to use
     * @return the encoded character
     */
    public static char encodeChar(char input, int shift, String alphabet) {
        int pos = alphabet.indexOf(Character.toUpperCase(input));
        if (pos < 0) { // not in the alphabet - just leave as is
            return input;
        }
        pos = (pos+shift)%alphabet.length();
        boolean isLower = (input >= 'a' && input <= 'z');
        return isLower ? alphabet.toLowerCase().charAt(pos)
                : alphabet.toUpperCase().charAt(pos);
    }

    /**
     * Caesar decode a single character in a specific alphabet
     * @param input the character to be decoded
     * @param shift the number of characters that was used to shift by during encoding
     * @param alphabet the alphabet to use
     * @return the decoded character
     */
    static char decodeChar(char input, int shift, String alphabet) {
        int pos = alphabet.indexOf(Character.toUpperCase(input));
        if (pos < 0) { // not in the alphabet - just leave as is
            return input;
        }
        // Java8 : pos = Math.floorMod((pos-shift),alphabet.length());
        pos = pos-shift;
        if (pos < 0)
            pos += alphabet.length();

        boolean isLower = (input >= 'a' && input <= 'z');
        return isLower ? alphabet.toLowerCase().charAt(pos)
                : alphabet.toUpperCase().charAt(pos);
    }

    /**
     * Encode a text using Caesar cipher with the given shift
     * @param plainText the text to be encoded
     * @param dirs a group of directives, we need SHIFT (int) and ALPHABET (string)
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        int shift = dirs.getShift();
        String alphabet = dirs.getAlphabet();
        StringBuilder result = new StringBuilder(plainText.length());
        for (int i=0; i < plainText.length(); i++) {
            result.append(encodeChar(plainText.charAt(i), shift, alphabet));
        }
        return result.toString();
    }

    /**
     * Decode a cipher text using Caesar cipher assuming it has was encoded with the given shift
     * @param cipherText the text to be decoded
     * @param dirs a group of directives, we need SHIFT (int) and ALPHABET (string)
     * @return the encoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        int shift = dirs.getShift();
        String alphabet = dirs.getAlphabet();
        StringBuilder result = new StringBuilder(cipherText.length());
        for (int i=0; i < cipherText.length(); i++) {
            result.append(decodeChar(cipherText.charAt(i), shift, alphabet));
        }
        return result.toString();
    }

    /**
     * Crack a Caesar cipher by checking all shifts and looking for cribs
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the cracked text (and directives has RESULT_SHIFT) or "" if unable to crack
     */
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        String alphabet = dirs.getAlphabet();
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        CrackMethod crackMethod = dirs.getCrackMethod();
        String reverseCipherText = new StringBuilder(cipherText).reverse().toString();

        StringBuilder resultSuccess = new StringBuilder()
                .append("Success Brute Force: tried each possible Caesar shift from 0 to ")
                .append((alphabet.length()-1))
                .append(" looking for cribs [")
                .append(cribString)
                .append("] in the decoded text.\n");
        String foundPlainText = "";
        int foundShift = -1;
        for (int shift=0; shift < alphabet.length(); shift++) {
            if (CrackResults.isCancelled(crackId))
                return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Crack cancelled", CrackState.CANCELLED);
            CrackResults.updateProgressDirectly(crackId, shift+" shifts of "+alphabet.length()+": "+100*shift/alphabet.length()+"% complete");
            dirs.setShift(shift);
            String plainText = decode(cipherText, dirs);
            if (Cipher.containsAllCribs(plainText, cribSet)) {
                this.shift = shift;
                String explain = "Found all cribs with shift "
                        + shift
                        + ", starts: "
                        + plainText.substring(0, Math.min(Cipher.CRACK_PLAIN_LENGTH, plainText.length()))
                        + "\n";
                resultSuccess.append(explain);
                if (dirs.stopAtFirst()) {
                    dirs.setShift(shift);
                    return new CrackResult(crackMethod, this, dirs, cipherText, plainText, resultSuccess.toString());
                } else {
                    foundShift = shift;
                    foundPlainText = plainText;
                }
            }

            // look in decoded reverse text for cribs
            if (dirs.considerReverse()) {
                plainText = decode(reverseCipherText, dirs);
                if (Cipher.containsAllCribs(plainText, cribSet)) {
                    this.shift = shift;
                    String explain = "Found all cribs in REVERSE text with shift "
                            + shift
                            + ", starts: "
                            + plainText.substring(0, Math.min(Cipher.CRACK_PLAIN_LENGTH, plainText.length()))
                            + "\n";
                    resultSuccess.append(explain);
                    if (dirs.stopAtFirst()) {
                        dirs.setShift(shift);
                        return new CrackResult(crackMethod, this, dirs, cipherText, plainText, explain);
                    } else {
                        foundShift = shift;
                        foundPlainText = plainText;
                    }
                }
            }
        }
        if (foundPlainText.length() > 0) {
            dirs.setShift(foundShift);
            shift = foundShift;
            return new CrackResult(crackMethod, this, dirs, cipherText, foundPlainText, resultSuccess.toString());
        }
        dirs.setShift(-1);
        this.shift = -1;
        String explain = "Fail: Brute Force: tried each possible Caesar shift from 0 to "
                + (alphabet.length()-1)
                + " looking for the cribs ["
                + cribString
                + "] in the decoded text but did not find them.\n";
        return new CrackResult(crackMethod, this, cipherText, explain);
    }
}
