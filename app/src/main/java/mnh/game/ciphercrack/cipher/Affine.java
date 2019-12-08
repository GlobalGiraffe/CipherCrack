package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.CrackState;
import mnh.game.ciphercrack.util.Directives;

/**
 * Class that contains methods to assist with Affine Cipher operations
 * This monoalphabetic substitution cipher shifts each letter within the text by a
 *   fixed amount modulo the number of letters in the alphabet
 */
public class Affine extends Cipher {

    // the cipher text is calculated as (char*a + b)
    private int a = -1, b = -1;

    Affine(Context context) { super(context, "Affine"); }

    // used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(a);
        dest.writeInt(b);
    }
    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        a = in.readInt();
        b = in.readInt();
    }

    /**
     * Describe what this cipher does
     * @return a description of this cipher
     */
    @Override
    public String getCipherDescription() {
        return "The Affine cipher is a monoalphabetic substitution cipher where each letter of the plain text is always mapped to the same letter in the cipher text. " +
                "The mapping is achieved using a mathematical formula: (a.x+b) mod 26, where a and b are co-prime integer constants known to the cipher participants, and x is the ordinal number of the letter in the alphabet, A=0, B=1, C=2, etc. It is important that 'a' and 'b' are co-prime to ensure no two plain letters end up mapped to the same cipher letter.\n\n" +
                "To encode a message each letter is taken, its ordinal (0-25) is calculated, multiplied by 'a', add 'b' and then take the modulo 26 (take remainder after division by the number of letters in the alphabet). The result is treated as the ordinal number of the cipher character.\n"+
                "To decode a message, the inverse is performed, an inverse function is found that reverses the encoding calculation: (a'.(x-b)) mod 26. In practice the decoder can also simply apply the equation to each plain letter to build a map of encodings and just decipher my looking up which plain letter makes the encoded letter.\n\n"+
                "This can generally be broken with some cribs given there are only a small number of values (around 300) of 'a' and 'b' that produce valid distinct mappings.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher ("+(a == -1 ? "n/a" :("a="+a+", b="+b))+")";
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_affine);

        // now attach the values to the spinners
        // create an array of possible values for 'a' and 'b' for Affine cipher
        // The first, for 'a' values, can be 1-[alphabet length], but only if not co-prime
        List<Integer> listOfValueA = new LinkedList<Integer>();
        for (int i = 1; i < alphabet.length(); i++) {
            if (areCoPrimes(i, alphabet.length())) {
                listOfValueA.add(i);
            }
        }
        Integer[] aArray = new Integer[listOfValueA.size()];
        listOfValueA.toArray(aArray);
        // now the second, for 'b' values, can be 0-[alphabet length]
        Integer[] bArray = new Integer[alphabet.length()];
        for (int i = 0; i < alphabet.length(); i++) {
            bArray[i] = i;
        }

        // Create an ArrayAdapter and default layout for the spinner for A
        ArrayAdapter<Integer> adapterA = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, aArray);
        // Specify the layout to use for the list of choices
        adapterA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Create a spinner and apply the adapter to it
        Spinner spinnerA = layout.findViewById(R.id.extra_affine_spinner_a);
        spinnerA.setAdapter(adapterA);

        // Create an ArrayAdapter and default layout for the spinner for B
        ArrayAdapter<Integer> adapterB = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, bArray);
        // Specify the layout to use when the list of choices appears
        adapterB.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinnerB = layout.findViewById(R.id.extra_affine_spinner_b);
        spinnerB.setAdapter(adapterB);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        Spinner affineSpinner = layout.findViewById(R.id.extra_affine_spinner_a);
        int a = (int) affineSpinner.getSelectedItem();
        dirs.setValueA(a);
        affineSpinner = layout.findViewById(R.id.extra_affine_spinner_b);
        int b = (int) affineSpinner.getSelectedItem();
        dirs.setValueB(b);
    }

    // we don't add any extra controls, but we allow change of cribs
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        return true;
    }

    /**
     * Determine whether the directives are valid for this cipher type, and sets if they are
     * @param dirs the directives to be checked and set
     * @return return the reason for being invalid, or null if the directives ARE valid (and are set)
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        String reason = super.canParametersBeSet(dirs);
        if (reason != null)
            return reason;
        int aValue = dirs.getValueA();
        int bValue = dirs.getValueB();

        // are we doing encode/decode or cracking
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            if (aValue < 0 || bValue < 0)
                return "Values for A (" + aValue + ") and B (" + bValue + ") must be greater than zero";
            String alphabet = dirs.getAlphabet();
            if (!areCoPrimes(aValue, alphabet.length()))
                return "Values for A (" + aValue + ") and alphabet length (" + alphabet.length() + ") are not co-prime";
        } else {
            // this the the only crack possible
            if (crackMethod != CrackMethod.BRUTE_FORCE)
                return "Invalid crack method";
            // brute force crack needs cribs
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
        }
        a = aValue;
        b = bValue;
        return null;
    }

    /**
     * Affine encode a single character in a specific alphabet
     * @param input the character to be encoded
     * @param a the 'a' value in (a.x + b)
     * @param b the 'b' value in (a.x + b)
     * @param alphabet the alphabet to use
     * @return the encoded character in same case as input
     */
    private char encodeChar(char input, int a, int b, String alphabet) {
        int symbolIndex = alphabet.indexOf(Character.toUpperCase(input));
        if (symbolIndex < 0) { // not in the alphabet - just leave as is
            return input;
        }
        int newIndex = (a * symbolIndex + b) % alphabet.length();
        boolean isLower = (input >= 'a' && input <= 'z');
        return isLower ? alphabet.toLowerCase().charAt(newIndex)
                : alphabet.toUpperCase().charAt(newIndex);
    }

    /**
     * Affine decode a single character in a specific alphabet
     * @param input the character to be decoded
     * @param letterMap an upper-case map of cipher char to plain char
     * @return the decoded character in same case as input
     */
    private char decodeChar(char input, Map<Character, Character> letterMap) {
        Character plainChar = letterMap.get(Character.toUpperCase(input));
        if (plainChar != null) {
            boolean isLower = (input >= 'a' && input <= 'z');
            return isLower
                    ? Character.toLowerCase(plainChar)
                    : Character.toUpperCase(plainChar);
        } else {
            return input;
        }
    }

    /**
     * Encode a text using Affine cipher with the given a and b values
     * @param plainText the text to be encoded
     * @param dirs a group of directives, we need A (int), B (int) and ALPHABET (string)
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        int a = dirs.getValueA();
        int b = dirs.getValueB();
        String alphabet = dirs.getAlphabet();
        StringBuilder result = new StringBuilder(plainText.length());
        for (int i=0; i < plainText.length(); i++) {
            result.append(encodeChar(plainText.charAt(i), a, b, alphabet));
        }
        return result.toString();
    }

    /**
     * Affine is a monoalphabetic substitution cipher, 1-to-1 mapping for letters for given a/b values
     * This produces a map of cipher->plain
     * @param alphabet the alphabet of possible letters
     * @param a the a value to use for (a.x + b)
     * @param b the b value to use for (a.x + b)
     * @return the map of upper-case ciphered letters to upper-case plain letters
     */
    private static Map<Character, Character> buildCharMapForDecode(String alphabet, int a, int b) {
        Map<Character, Character> letterMap = new HashMap<>();
        for (int pos=0; pos < alphabet.length(); pos++) {
            char plain = alphabet.charAt(pos);
            char encoded = alphabet.charAt( (a*pos + b) % alphabet.length());
            letterMap.put(encoded, plain);
        }
        return letterMap;
    }

    /**
     * Decode a cipher text using Affine cipher assuming it has was encoded with the a and b values
     * @param cipherText the text to be decoded
     * @param dirs what this instance of cipher should do,
     *             we need A_VALUE (int), B_VALUE (int) and ALPHABET (string)
     * @return the encoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        int a = dirs.getValueA();
        int b = dirs.getValueB();
        String alphabet = dirs.getAlphabet();
        Map<Character, Character> letterMap = buildCharMapForDecode(alphabet, a, b);
        StringBuilder result = new StringBuilder(cipherText.length());
        for (int i=0; i < cipherText.length(); i++) {
            char cipherChar = cipherText.charAt(i);
            char plainChar = (letterMap.containsKey(Character.toUpperCase(cipherChar)))
                    ? decodeChar(cipherChar, letterMap)
                    : cipherChar;
            result.append(plainChar);
        }
        return result.toString();
    }

    /**
     * Crack an Affine cipher by checking all a and b values under 30 and looking for cribs
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        String alphabet = dirs.getAlphabet();
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        CrackMethod crackMethod = dirs.getCrackMethod();
        String reverseCipherText = new StringBuilder(cipherText).reverse().toString();

        //Log.i("CRACK", "Trying to crack text "+cipherText.substring(0,20)+", cribs="+cribString);
        for (int a=0; a < alphabet.length(); a++) {
            // only check if 'a' and length of alphabet are co-primes, else could have 2 plain -> 1 cipher letter
            if (areCoPrimes(a, alphabet.length())) {
                // publish progress as a percentage
                if (CrackResults.isCancelled(crackId))
                    return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Crack cancelled", CrackState.CANCELLED);
                CrackResults.updateProgressDirectly(crackId, a+" of "+alphabet.length()+": "+100*a/alphabet.length()+"% complete");
                dirs.setValueA(a);

                // check each value of b up to length of alphabet, doing decode and looking for cribs
                for (int b=0; b < alphabet.length(); b++) {
                    dirs.setValueB(b);
                    String plainText = decode(cipherText, dirs);
                    if (Cipher.containsAllCribs(plainText, cribSet)) {
                        String explain = "Success: Brute Force: tried each possible value of a and b from 0 to "
                                + (alphabet.length() - 1)
                                + " looking for the cribs [" + cribString
                                + "] in the decoded text and found them all with a=" + a + " and b="
                                + b + ".\n";
                        this.a = a;
                        this.b = b;
                        return new CrackResult(crackMethod, this, dirs, cipherText, plainText, explain);
                    }
                    plainText = decode(reverseCipherText, dirs);
                    if (Cipher.containsAllCribs(plainText, cribSet)) {
                        String explain = "Success: Brute Force REVERSE: tried each possible value of a and b from 0 to "
                                + (alphabet.length() - 1)
                                + " looking for the cribs [" + cribString
                                + "] in the decoded REVERSE text and found them all with a=" + a + " and b="
                                + b + ".\n";
                        this.a = a;
                        this.b = b;
                        return new CrackResult(crackMethod, this, dirs, cipherText, plainText, explain);
                    }
                }
            }
        }
        a = b = -1;
        String explain = "Fail: Brute Force: tried each possible value of a and b from 0 to "
                + (alphabet.length() - 1)
                + " looking for the cribs [" + cribString
                + "] in the decoded text but did not find them.\n";
        return new CrackResult(crackMethod, this, cipherText, explain);
    }
}
