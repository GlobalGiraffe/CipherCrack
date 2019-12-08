package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;

public class Morse extends Cipher {

    /**
     * Convert between the dots and dashes of Morse code and text
     * Could be separated by some symbol: ..-/..-./...
     * Could be 0 and 1s: 001/0010/000
     * A => '.-', B => '-...', C => '-.-.', D => '-..', E => '.',
     * F => '..-.', G => '--.', H => '....', I => '..', J => '.---',
     * K => '-.-', L => '.-..', M => '--', N => '-.', O => '---',
     * P => '.--.', Q => '--.-', R => '.-.', S => '...', T => '-',
     * U => '..-', V => '...-', W => '.--', X => '-..-', Y => '-.--',
     * Z => '--..',
     * '1' => '.----', '2' => '..---', '3' => '...--', '4' => '....-',
     * '5' => '.....', '6' => '-....', '7' => '--...', '8' => '---..',
     * '9' => '----.', '0' => '-----',
     */
    private static Map<Character, String> encoding = new HashMap<>(30);
    private static Map<String, Character> decoding = new HashMap<>(30);

    static {
        encoding.put('A',".-");
        encoding.put('B',"-...");
        encoding.put('C',"-.-.");
        encoding.put('D',"-..");
        encoding.put('E',".");
        encoding.put('F',"..-.");
        encoding.put('G',"--.");
        encoding.put('H',"....");
        encoding.put('I',"..");
        encoding.put('J',".---");
        encoding.put('K',"-.-");
        encoding.put('L',".-..");
        encoding.put('M',"--");
        encoding.put('N',"-.");
        encoding.put('O',"---");
        encoding.put('P',".--.");
        encoding.put('Q',"--.-");
        encoding.put('R',".-.");
        encoding.put('S',"...");
        encoding.put('T',"-");
        encoding.put('U',"..-");
        encoding.put('V',"...-");
        encoding.put('W',".--");
        encoding.put('X',"-..-");
        encoding.put('Y',"-.--");
        encoding.put('Z',"--..");
        encoding.put('0',"-----");
        encoding.put('1',".----");
        encoding.put('2',"..---");
        encoding.put('3',"...--");
        encoding.put('4',"....-");
        encoding.put('5',".....");
        encoding.put('6',"-....");
        encoding.put('7',"--...");
        encoding.put('8',"---..");
        encoding.put('9',"----.");
        for(Map.Entry<Character, String> entry : encoding.entrySet()) {
            decoding.put(entry.getValue(),entry.getKey());
        }
    }

    private static final int MAX_MORSE_SYMBOLS = 2;
    private static final String MORSE_SYMBOLS = ".-";

    private String symbols = ".-";
    private String separator = " ";

    Morse(Context context) { super(context, "Morse"); }

    // used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(symbols);
        dest.writeString(separator);
    }
    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        symbols = in.readString();
        separator = in.readString();
    }

    /**
     * Describe what this cipher does
     * @return a description of this cipher
     */
    @Override
    public String getCipherDescription() {
        return "The Morse Code is an encoding of letters into a sequence of short and long pulses (dots and dashes) for telegraph transmission but also employed on radio, between ships and other uses. Each letter is encoded by between 1 and 5 symbols and so there needs to be separators between letters to show where one starts and another finishes, e.g. ...././.-../---." +
                "To encode a message take each plain letter and convert to the correct form using the symbols provided, e.g. A=> 01 or .- or AB, adding separators between letters. Punctuation is normally discarded.\n"+
                "To decode a message, taking any separator into account, the symbols are converted back to the letters using the reverse coding.\n"+
                "Morse is not a strict cipher, rather a coding since the key and algorithm are well known.\n";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher (["+symbols+"]"+((separator.length()!=0)?(",sep="+separator):"")+")";
    }

    /**
     * Determine whether the directives are valid for this cipher type, and sets if they are
     * @param dirs the directives to be checked and set
     * @return return the reason for being invalid, or null if the directives ARE valid
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        String reason = super.canParametersBeSet(dirs);
        if (reason != null)
            return reason;
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            String morseSymbols = dirs.getDigits();
            if (morseSymbols == null || morseSymbols.length() < 2)
                return "Too few symbols";
            if (morseSymbols.length() > MAX_MORSE_SYMBOLS)
                return "Too many symbols (" + morseSymbols.length() + ")";

            // letters in the digit list should only occur once and should be in the alphabet
            for (int i = 0; i < morseSymbols.length(); i++) {
                char digit = morseSymbols.charAt(i);
                if (i < morseSymbols.length() - 1) {
                    if (morseSymbols.indexOf(digit, i + 1) >= 0)
                        return "Character " + digit + " is duplicated in the symbols";
                }
            }
            symbols = morseSymbols;

            String morseSep = dirs.getSeparator();
            if (morseSep == null || morseSep.length() == 0)
                morseSep = " ";
            for (int pos=0; pos < morseSymbols.length(); pos++) {
                if (morseSep.contains(String.valueOf(morseSymbols.charAt(pos)))) {
                    return "Separator contains a symbol";
                }
            }
            separator = morseSep;

        } else { // crack via Brute Force, i.e. look at text and work out symbols and separator, etc
            // this the the only crack possible
            if (crackMethod != CrackMethod.BRUTE_FORCE)
                return "Invalid crack method";
            // brute force crack needs cribs
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
        }
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        addExtraControls(context, layout, R.layout.extra_morse);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        EditText symbolsField = layout.findViewById(R.id.extra_morse_symbols);
        String symbolsStr = symbolsField.getText().toString();
        symbols = symbolsStr;
        dirs.setDigits(symbolsStr);

        EditText separatorField = layout.findViewById(R.id.extra_morse_separator);
        String separatorStr = separatorField.getText().toString();
        separator = separatorStr;
        dirs.setSeparator(separatorStr);
    }

    // we don't add any extra controls, but we allow change of cribs
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        return true;
    }

    /**
     * Encode a text into Morse code with the given symbols
     * @param plainText the text to be encoded
     * @param dirs a group of directives that define how the coding will work,
     *             especially SYMBOLS and SEPARATOR
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        String symbols = dirs.getDigits().toUpperCase();
        String separatorStr = dirs.getSeparator().toUpperCase();
        plainText = plainText.replaceAll("[^A-Za-z0-9]","").toUpperCase(); // just keep upper-case alphabetic
        StringBuilder result = new StringBuilder(plainText.length()*6);

        // convert the text, one char at a time
        for (int plainPos=0; plainPos < plainText.length(); plainPos++) {
            if (result.length() > 0)
                result.append(separatorStr);
            char plainCharUpper = Character.toUpperCase(plainText.charAt(plainPos));
            String codeString = encoding.get(plainCharUpper);
            if (codeString == null) { // character not in the Morse alphabet
                result.append("<").append(plainCharUpper).append(">");
            } else {
                for(char dotDash : codeString.toCharArray()) {
                    result.append(symbols.charAt(MORSE_SYMBOLS.indexOf(dotDash)));
                }
            }
        }
        return result.toString();
    }

    /**
     * Decode a text using Morse Code with given symbols and separator
     * @param cipherText the text to be decoded
     * @param dirs a group of directives that define how the cipher will work,
     *             especially SYMBOLS and SEPARATOR
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        String separatorStr = dirs.getSeparator();
        String symbolsStr = dirs.getDigits().toUpperCase();

        // remove all but the separators and the symbols
        cipherText = cipherText.replaceAll("[^"+separatorStr+symbolsStr+"]","").toUpperCase();

        // scan the text, separated by the separator
        StringBuilder result = new StringBuilder(cipherText.length());
        String[] codedLetters = cipherText.split(separatorStr);
        for (String codedLetter : codedLetters) {
            if (codedLetter.length() == 0) { // could be 2 separators together - perhaps between words
                result.append(" ");
            } else {
                char[] chars = codedLetter.toCharArray();
                boolean badSymbol = false;
                for (int pos = 0; pos < chars.length; pos++) {
                    int ordinal = symbolsStr.indexOf(chars[pos]);
                    if (ordinal < 0) {
                        badSymbol = true;
                        break;
                    }
                    chars[pos] = MORSE_SYMBOLS.charAt(ordinal);
                }
                if (badSymbol) {
                    result.append("[").append(codedLetter).append("]");
                } else {
                    String morseSequence = String.valueOf(chars);
                    Character decodedLetter = decoding.get(morseSequence);
                    if (decodedLetter == null) {
                        if (morseSequence.equals("----")) // special case we've seen sometimes
                            result.append("CH");
                        else
                            result.append("{").append(codedLetter).append("}");
                    } else {
                        result.append(decodedLetter);
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * Not able to crack this yet
     * @param cipherText the text to be cracked
     * @param dirs the controls and parameters for this crack request
     * @return the result of the crack attempt
     */
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        // TODO: Morse Crack: suggest looking for most common symbol => ".", next most common => "-", and third is separator, look for cribs, or words
        return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Fail: Not yet able to crack "+getCipherName()+" cipher.\n");
    }
}
