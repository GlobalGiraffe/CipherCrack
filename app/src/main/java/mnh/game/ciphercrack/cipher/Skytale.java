package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.transform.RemoveNonAlphabetic;
import mnh.game.ciphercrack.transform.Transform;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.CrackState;
import mnh.game.ciphercrack.util.Directives;

/**
 * Class that contains methods to assist with Skytale Cipher operations
 * This transposition cipher comes from a time when a wrapped cloth around a staff carried a message
 */
public class Skytale extends Cipher {

    // maximum number of letters in a cycle around the staff
    static final int MAX_CYCLE_LENGTH = 1000; // need some limit to avoid going forever during crack

    private final Transform removeNonAlphabetic = new RemoveNonAlphabetic();
    private int cycleLength = -1;

    Skytale(Context context) { super(context, "Skytale"); }

    // used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(cycleLength);
    }
    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        cycleLength = in.readInt();
    }

    /**
     * Describe what this cipher does
     * @return a description for this cipher, split across lines and paragraphs
     */
    @Override
    public String getCipherDescription() {
        return "The Skytale cipher is a transposition cipher where letters were written on a cloth around a stick, the diameter of the stick would determine how many letters are in a complete cycle.\n" +
                "To encode a message with cycle length of 7 the first letter is at position 0, the next at position 7, the next at position 14, and so on. When one 7th of the message has been written this way, start again with the next letter at position 1, then 8, then 15, etc.\n" +
                "To decipher, choose letters at positions 0, 7, 14, to the end, then 1, 8, 15, and so on\n\n"+
                "This can be broken using brute force by trying with different cycle lengths and looking for cribs - there are only a limited number of cycle lengths possible.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher ("+(cycleLength == -1 ? "n/a" : "cycleLength="+cycleLength)+")";
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
        int cycleLength = dirs.getCycleLength();
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            if (cycleLength < 2 || cycleLength > MAX_CYCLE_LENGTH)
                return "Cycle length: " + cycleLength + " must be between 2 and " + MAX_CYCLE_LENGTH;
        } else {
            if (crackMethod != CrackMethod.BRUTE_FORCE)
                return "Invalid crack method";
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
        }
        this.cycleLength = cycleLength;
        return null;
    }


    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_skytale);

        // create an array of possible cycle lengths for the user to choose from: 2 to large number
        Integer[] cycleLengthArray = new Integer[MAX_CYCLE_LENGTH-2];
        for (int i = 0; i < MAX_CYCLE_LENGTH-2; i++) {
            cycleLengthArray[i] = i+2;
        }

        // Create an ArrayAdapter and default layout for the spinner
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, cycleLengthArray);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Create a spinner and apply the adapter to it
        Spinner spinner = layout.findViewById(R.id.extra_skytale_spinner);
        spinner.setAdapter(adapter);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        Spinner spinner = layout.findViewById(R.id.extra_skytale_spinner);
        int cycleLength = (int)spinner.getSelectedItem();
        dirs.setCycleLength(cycleLength);
    }

    // we don't add any extra controls, but we allow change of cribs
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        return true;
    }

    /**
     * Encode a text using Skytale cipher with the given cycle size
     * @param plainText the text to be encoded
     * @param dirs a group of directives, we need CYCLE LENGTH (int) only
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        int cycleLength = dirs.getCycleLength();
        plainText = removeNonAlphabetic.apply(context, plainText);

        int numberOfCycles = (plainText.length()+(cycleLength-1))/cycleLength;
        int lengthOfAllCycles = numberOfCycles * cycleLength;
        int lettersAlongStick = lengthOfAllCycles / cycleLength;

        // allocate a char array to hold the cipher text
        char[] cipherArray = new char[lengthOfAllCycles];
        Arrays.fill(cipherArray, ' ');

        for (int pos=0; pos < plainText.length(); pos++) {

            // thisCycle = how much we have turned the stick by:
            // first few letters on cycle 0, then next set on cycle 1, etc
            int thisCycle = pos / lettersAlongStick;
            int posOnStick = pos % lettersAlongStick;
            cipherArray[posOnStick * cycleLength + thisCycle] = plainText.charAt(pos);
        }
        return String.valueOf(cipherArray);
    }

    /**
     * Decode a cipher text that was encoded with Skytale and the given cycle length
     * @param cipherText the text to be decoded
     * @param dirs a group of directives, we need CYCLE LENGTH (int)
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        int cycleLength = dirs.getCycleLength();
        StringBuilder[] lines = new StringBuilder[cycleLength];
        for (int i = 0; i < cycleLength; i++) {
            lines[i] = new StringBuilder();
        }

        // now cycle through the text, adding to correct string builder
        for (int pos=0; pos < cipherText.length(); pos++) {
            lines[pos % cycleLength].append(cipherText.charAt(pos));
        }

        // now append these cyclic strings to one final string
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < cycleLength; i++) {
            result.append(lines[i]);
        }
        return result.toString().trim();
    }

    /**
     * Crack a Skytale cipher by checking all cycle lengths and looking for cribs
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        String reverseCipherText = new StringBuilder(cipherText).reverse().toString();
        int maxCycleLength = Math.min(MAX_CYCLE_LENGTH, cipherText.length()/2);
        for (int currentCycleLength=2; currentCycleLength < maxCycleLength; currentCycleLength++) {
            if (CrackResults.isCancelled(crackId))
                return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Crack cancelled", CrackState.CANCELLED);
            CrackResults.updateProgressDirectly(crackId, currentCycleLength+" cycles  of "+maxCycleLength+": "+100*currentCycleLength/maxCycleLength+"% complete");

            dirs.setCycleLength(currentCycleLength);
            String plainText = decode(cipherText, dirs);
            if (Cipher.containsAllCribs(plainText, cribSet)) {
                cycleLength = currentCycleLength;
                String explain = "Success: Brute Force: tried possible cycle lengths from 2 to "
                        + maxCycleLength
                        + " looking for the cribs ["
                        + cribString
                        + "] in the decoded text and found them with "
                        + currentCycleLength
                        + " cycle length.\n";
                return new CrackResult(dirs.getCrackMethod(), this, dirs, cipherText, plainText, explain);
            }
            plainText = decode(reverseCipherText, dirs);
            if (Cipher.containsAllCribs(plainText, cribSet)) {
                cycleLength = currentCycleLength;
                String explain = "Success: Brute Force REVERSE: tried possible cycle lengths from 2 to "
                        + maxCycleLength
                        + " looking for the cribs ["
                        + cribString
                        + "] in the decoded REVERSE text and found them with "
                        + currentCycleLength
                        + " cycle length.\n";
                return new CrackResult(dirs.getCrackMethod(), this, dirs, cipherText, plainText, explain);
            }
        }
        cycleLength = -1;
        dirs.setCycleLength(cycleLength);
        String explain = "Fail: Brute force approach: tried possible cycle lengths from 2 to "
                + maxCycleLength
                + " looking for the cribs ["
                + cribString
                + "] in the decoded text but did not find them.\n";
        return new CrackResult(dirs.getCrackMethod(), this, cipherText, explain);
    }
}
