package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.Settings;

public class Binary extends Cipher {

    /**
     * Convert 00101 or 00010 to and from letters using binary
     * Could be fixed width digits, or separated by some symbol, e.g 010/11001/...
     */

    private static final int MAX_BINARY_DIGITS = 2;

    private String digits = "";
    private int numberSize = 5;
    private String separator = "";

    Binary(Context context) { super(context, "Binary"); }

    /**
     * Describe what this cipher does
     * @return a description of this cipher
     */
    @Override
    public String getCipherDescription() {
        return "The Binary cipher is a substitution cipher which replaces letters with binary digits, or symbols representing digits. There may be separators between letters, in which case the leading 0s may be missing, e.g. 11/1001/11000/101." +
                "To encode a message take each plain letter convert to a number, e.g. A=>0, B=>1, Z=>25, which is then converted to binary digits, usually 5, e.g. A=>00000, B=>00001, C=>00002, Z=>11001. If separator is used then leading 0s can be removed.\n"+
                "To decode a message, taking any separator into account, the digits are converted to an ordinal number and then to a letter.\n"+
                "This cipher has just 2 symbols representing digits, and usually more representing 0 than 1.\n";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher (["+digits+"]"+((separator.length()!=0)?(",sep="+separator):((numberSize!=0)?(",size="+numberSize):""))+")";
    }

    /**
     * Determine whether the directives are valid for this cipher type, and sets if they are
     * @param dirs the directives to be checked and set
     * @return return the reason for being invalid, or null if the directives ARE valid
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        String alphabet = dirs.getAlphabet();
        if (alphabet == null || alphabet.length() < 2)
            return "Alphabet is empty or too short";

        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            String binaryDigits = dirs.getDigits();
            if (binaryDigits == null || binaryDigits.length() < 2)
                return "Too few digits";
            if (binaryDigits.length() > MAX_BINARY_DIGITS)
                return "Too many digits (" + binaryDigits.length() + ")";

            // letters in the digit list should only occur once and should be in the alphabet
            for (int i = 0; i < binaryDigits.length(); i++) {
                char digit = binaryDigits.charAt(i);
                if (i < binaryDigits.length() - 1) {
                    if (binaryDigits.indexOf(digit, i + 1) >= 0)
                        return "Character " + digit + " is duplicated in the digits";
                }
            }
            digits = binaryDigits;

            String binarySep = dirs.getSeparator();
            if (binarySep == null)
                binarySep = "";
            for (int pos=0; pos < binaryDigits.length(); pos++) {
                if (binarySep.contains(String.valueOf(binaryDigits.charAt(pos)))) {
                    return "Separator contains a digit";
                }
            }
            separator = binarySep;

            if (separator.length() == 0) {
                int size = dirs.getNumberSize();
                if (size <= 0)
                    return "Number size " + size + " too small";
                if (size > 50)
                    return "Number size " + size + " too large";
                numberSize = size;
            } else {
                numberSize = 0;
            }
        } else { // crack via Brute Force, i.e. look at text and work out digits and separator, etc
            Language language = dirs.getLanguage();
            if (language == null)
                return "Language is missing";

            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
        }
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        addExtraControls(context, layout, R.layout.extra_binary);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        EditText digitsField = layout.findViewById(R.id.extra_binary_digits);
        String digits = digitsField.getText().toString();
        this.digits = digits;
        dirs.setDigits(digits);

        EditText numberSizeField = layout.findViewById(R.id.extra_binary_size);
        String numberSize = numberSizeField.getText().toString();
        this.numberSize = Integer.valueOf(numberSize);
        dirs.setNumberSize(this.numberSize);

        EditText separatorField = layout.findViewById(R.id.extra_binary_separator);
        String separator = separatorField.getText().toString();
        this.separator = separator;
        dirs.setSeparator(separator);

        String languageName = Settings.instance().getString(layout.getContext(), R.string.pref_language);
        Language language = Language.instanceOf(languageName);
        dirs.setLanguage(language);
    }

    /**
     * Encode a text using Binary cipher with the given digits
     * @param plainText the text to be encoded
     * @param dirs a group of directives that define how the cipher will work,
     *             especially DIGITS and SEPARATOR
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        String alphabet = dirs.getAlphabet();
        String digits = dirs.getDigits().toUpperCase();
        String separator = dirs.getSeparator().toUpperCase();
        plainText = plainText.toUpperCase();
        StringBuilder result = new StringBuilder(plainText.length()*6);

        // convert the text, one char at a time
        for (int plainPos=0; plainPos < plainText.length(); plainPos++) {
            char plainChar = plainText.charAt(plainPos);
            char plainCharUpper = Character.toUpperCase(plainChar);
            int plainOrdinal = alphabet.indexOf(plainCharUpper); // find this char's pos in alphabet

            // only include in cipherText if the letter is in the alphabet, i.e. drop punctuation/spaces
            if (plainOrdinal >= 0) {
                String binaryShort = Integer.toBinaryString(plainOrdinal);
                if (separator.length() == 0) { // no separator, make n-digits
                    binaryShort = "0000" + binaryShort;
                    binaryShort = binaryShort.substring(binaryShort.length()-5);
                }
                // now convert from 0/1 to the digits user asked for, e.g. A/B or otherwise
                for (int digitPos=0; digitPos < digits.length(); digitPos++) {
                    binaryShort = binaryShort.replace((char)('0'+digitPos),digits.charAt(digitPos));
                }
                // now add to the result string, with separator if needed
                if (plainPos > 0 && separator.length() != 0) {
                    result.append(separator);
                }
                result.append(binaryShort);
            }
        }
        return result.toString();
    }

    /**
     * Decode a text using Binary cipher with given digits and separator
     * @param cipherText the text to be decoded
     * @param dirs a group of directives that define how the cipher will work,
     *             especially DIGITS and SEPARATOR
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        String alphabet = dirs.getAlphabet();
        String digits = dirs.getDigits().toUpperCase();
        cipherText = cipherText.toUpperCase().replace(" ","");
        String separator = dirs.getSeparator();
        StringBuilder result = new StringBuilder(cipherText.length());
        while(cipherText.length() > 0) {
            String seq;
            if (separator.length() > 0) {
                int index = cipherText.indexOf(separator);
                if (index >= 0) {
                    seq = cipherText.substring(0, index);
                    cipherText = cipherText.substring(seq.length()+1);
                } else {
                    seq = cipherText;
                    cipherText = "";
                }
            } else {
                seq = cipherText.substring(0, 5);
                cipherText = cipherText.substring(5);
            }
            // replace the text digits (could be A/B) with 0/1
            for(int digitPos=0; digitPos < digits.length(); digitPos++) {
                seq = seq.replace(digits.charAt(digitPos), (char)('0'+digitPos));
            }
            try {
                int ordinal = Integer.parseInt(seq, 2);
                result.append(alphabet.charAt(ordinal));
            } catch (NumberFormatException ex) {
                result.append("x");
            }
        }
        return result.toString();
    }

    /**
     * Not able to crack this yet
     * @param cipherText the text to be cracked
     * @param directives the controls and parameters for this crack request
     * @return the result of the crack attempt
     */
    public CrackResult crack(String cipherText, Directives directives) {
        return new CrackResult(cipherText, "Fail: Unable to crack "+getCipherName()+" cipher");
    }
}
