package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Properties;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.Climb;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.staticanalysis.StaticAnalysis;

/**
 * Class that contains methods to assist with Vigenere Cipher operations
 * This poly-alphabetic cipher shifts each letter within the text by a
 *   an amount based on a keyword
 */
public class Vigenere extends Cipher {

    // delete the keyword if 'X' is pressed
    private static final View.OnClickListener VIGENERE_ON_CLICK_DELETE = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText k = v.getRootView().findViewById(R.id.extra_vigenere_keyword);
            k.setText("");
        }
    };

    String keyword = "";

    Vigenere(Context context) { super(context, "Vigenere"); }

    // needed for subclasses (Beaufort)
    Vigenere(Context context, String name) { super(context, name); }

    /**
     * Describe what this cipher does
     * @return a description of this cipher
     */
    @Override
    public String getCipherDescription() {
        return "The Vigenere cipher is a polyalphabtic substitution cipher where each letter of the plain text can be mapped to many different letters in the cipher text, depending on the position of the letter in the message. " +
                "A tableau is used where each of 26 columns contain the 26 letters of the alphabet in a different position. As the message is encoded, each column in turn is used to perform a Caesar cipher encoding.\n" +
                "To encode a message take the next plain letter, calculate its ordinal (0-25), count down that many letters in the column whose position is the plain letter's position in the text modulus the number of columns. The result is treated as the ordinal number of the cipher character.\n"+
                "To decode a message, the inverse is performed: each cipher letter is taken in turn, it is looked up in the column that is the position in the text modulo the number of columns, and the position in that column gives the ordinal of the plain letter.\n"+
                "This cipher can be broken by looking at IOC values for different possible keyword sizes, the one with IOC close to the target language will indicate keyword length. With the help of some cribs, particularly ones longer than the keyword length, the keyword can be slowly discovered.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher (keyword="+keyword+")";
    }

    /**
     * Determine whether the properties are valid for this cipher type, and sets if they are
     * @param dirs the properties to be checked and set
     * @return return the reason for being invalid, or null if the properties ARE valid
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        String alphabet = dirs.getAlphabet();
        if (alphabet == null || alphabet.length() < 2)
            return "Alphabet is empty or too short";

        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            String keywordValue = dirs.getKeyword();
            if (keywordValue == null || keywordValue.length() < 2)
                return "Keyword is empty or too short";
            // letters in the keyword should be in the alphabet
            for (int i = 0; i < keywordValue.length(); i++) {
                if (alphabet.indexOf(keywordValue.charAt(i)) < 0)
                    return "Character " + keywordValue.charAt(i) + " at offset " + i + " in the keyword is not in the alphabet";
            }
            keyword = keywordValue;
        } else {
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";

            // to crack Vigenere (IOC climb) we need a keyword length too
            if (crackMethod == CrackMethod.IOC) {
                int keywordLength = dirs.getKeywordLength();
                if (keywordLength <= 0)
                    return "Keyword length is empty, zero or not a positive integer";
            }
            // to crack Vigenere via Dictionary we need a Language
            if (crackMethod == CrackMethod.DICTIONARY) {
                Language language = dirs.getLanguage();
                if (language == null)
                    return "Missing language";
            }
        }
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_vigenere);

        // ensure input is in capitals
        EditText keyword = layout.findViewById(R.id.extra_vigenere_keyword);
        InputFilter[] editFilters = keyword.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 2];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.AllCaps();   // ensures capitals
        newFilters[editFilters.length+1] = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                // only allow chars in the alphabet to be added
                for (int i = start; i < end; i++) {
                    String letter = String.valueOf(source.charAt(i));
                    if (!alphabet.contains(letter)) {
                        return "";
                    }
                }
                return null;
            }
        };
        keyword.setFilters(newFilters);

        // ensure we 'delete' the keyword text when the delete button is pressed
        Button keywordDelete = layout.findViewById(R.id.extra_vigenere_keyword_delete);
        keywordDelete.setOnClickListener(VIGENERE_ON_CLICK_DELETE);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        EditText keywordField = layout.findViewById(R.id.extra_vigenere_keyword);
        String keyword = keywordField.getText().toString();
        dirs.setKeyword(keyword);
    }

    /**
     * Add crack controls for this cipher: type of crack to be done
     * @param context the context
     * @param layout the layout to add any crack controls to
     * @param alphabet the current alphabet
     * @return true if controls added, else false
     */
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        TextView lengthLabel = new TextView(context);
        lengthLabel.setText(context.getString(R.string.length));
        lengthLabel.setTextColor(ContextCompat.getColor(context, R.color.white));
        lengthLabel.setLayoutParams(WRAP_CONTENT_BOTH);

        EditText keywordLength = new EditText(context);
        keywordLength.setText("");
        keywordLength.setPadding(3,3,3,3);
        keywordLength.setTextColor(ContextCompat.getColor(context, R.color.entry_text_text));
        keywordLength.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);
        keywordLength.setId(ID_VIGENERE_LENGTH);
        keywordLength.setBackground(context.getDrawable(R.drawable.entry_text_border));
        keywordLength.setInputType(InputType.TYPE_CLASS_NUMBER);

        TextView crackLabel = new TextView(context);
        crackLabel.setText(context.getString(R.string.crack_method));
        crackLabel.setTextColor(ContextCompat.getColor(context, R.color.white));
        crackLabel.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);

        RadioButton dictButton = new RadioButton(context);
        dictButton.setId(ID_BUTTON_DICTIONARY);
        dictButton.setText(context.getString(R.string.crack_dictionary));
        dictButton.setChecked(true);
        dictButton.setTextColor(ContextCompat.getColor(context, R.color.white));
        dictButton.setLayoutParams(WRAP_CONTENT_BOTH);

        RadioButton iocButton = new RadioButton(context);
        iocButton.setId(ID_BUTTON_IOC);
        iocButton.setText(context.getString(R.string.crack_ioc));
        iocButton.setChecked(false);
        iocButton.setTextColor(ContextCompat.getColor(context, R.color.white));
        iocButton.setLayoutParams(WRAP_CONTENT_BOTH);

        RadioGroup crackButtonGroup = new RadioGroup(context);
        crackButtonGroup.check(ID_BUTTON_DICTIONARY);
        crackButtonGroup.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);
        crackButtonGroup.setOrientation(LinearLayout.HORIZONTAL);
        crackButtonGroup.addView(dictButton);
        crackButtonGroup.addView(iocButton);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);
        layout.addView(lengthLabel);
        layout.addView(keywordLength);
        layout.addView(crackLabel);
        layout.addView(crackButtonGroup);

        return true;
    }

    /**
     * Fetch the details of the extra crack controls for this cipher
     * @param layout the layout that could contains some crack controls
     * @param dirs the directives to add to
     * @return the crack method to use
     */
    public CrackMethod fetchCrackControls(LinearLayout layout, Directives dirs) {
        dirs.setKeywordLength(-2);
        EditText keywordLengthField = layout.findViewById(ID_VIGENERE_LENGTH);
        String keywordLengthStr = keywordLengthField.getText().toString();
        if (keywordLengthStr.length() == 0)
            dirs.setKeywordLength(-1);
        try {
            int keywordLength = Integer.valueOf(keywordLengthStr);
            if (keywordLength <= 0)
                dirs.setKeywordLength(-1);
            else
                dirs.setKeywordLength(keywordLength);
        } catch (NumberFormatException ex) {
            dirs.setKeywordLength(-1);
        }

        // locate the kind of crack we've been asked to do
        RadioButton dictButton = layout.findViewById(ID_BUTTON_DICTIONARY);
        return (dictButton.isChecked()) ? CrackMethod.DICTIONARY : CrackMethod.WORD_COUNT;
    }

    /**
     * Encode a text using Vigenere cipher with the given keyword
     * @param plainText the text to be encoded
     * @param dirs a group of directives that define how the cipher will work, especially KEYWORD
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        String alphabet = dirs.getAlphabet();
        String keyword = dirs.getKeyword();
        int keywordLength = keyword.length();
        String keywordUpper = keyword.toUpperCase();

        StringBuilder result = new StringBuilder(plainText.length());
        for (int i=0, keyPos=0; i < plainText.length(); i++) {
            char plainChar = plainText.charAt(i);
            char plainCharUpper = (char)((plainChar >= 'a' && plainChar <= 'z')
                ? (plainChar - ('a'-'A')) : plainChar);
            if (alphabet.indexOf(plainCharUpper) < 0) {
                result.append(plainChar);
            } else {
                int keyLetter = keywordUpper.charAt(keyPos);
                keyPos = (keyPos+1) % keywordLength;
                int keyShift = alphabet.indexOf(keyLetter);
                result.append(Caesar.encodeChar(plainChar, keyShift, alphabet));
            }
        }
        return result.toString();
    }

    /**
     * Decode a text using Vigenere cipher with the given keyword
     * @param cipherText the text to be decoded
     * @param dirs a group of directives that define how the cipher will work, especially KEYWORD
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        String alphabet = dirs.getAlphabet();
        String keyword = dirs.getKeyword();
        int keywordLength = keyword.length();
        String keywordUpper = keyword.toUpperCase();

        StringBuilder result = new StringBuilder(cipherText.length());
        for (int i=0, keyPos=0; i < cipherText.length(); i++) {
            char cipherChar = cipherText.charAt(i);
            char cipherCharUpper = (char)((cipherChar >= 'a' && cipherChar <= 'z')
                    ? (cipherChar - ('a'-'A')) : cipherChar);
            if (alphabet.indexOf(cipherCharUpper) < 0) {
                result.append(cipherChar);
            } else {
                int keyLetter = keywordUpper.charAt(keyPos);
                keyPos = (keyPos+1) % keywordLength;
                int keyShift = alphabet.indexOf(keyLetter);
                result.append(Caesar.decodeChar(cipherChar, keyShift, alphabet));
            }
        }
        return result.toString();
    }

    /**
     * Crack a Vigenere cipher by checking all shifts, climbing based on IOC and when best IOC found
     * we take that keyword and look through all equally shifted keys for the cribs
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the results of the crack attempt
     */
    @Override
    public CrackResult crack(String cipherText, Directives dirs) {
        String alphabet = dirs.getAlphabet();
        String cribString = dirs.getCribs();
        CrackMethod crackMethod = dirs.getCrackMethod();

        // returns best decode if found all cribs
        StringBuilder explain = new StringBuilder();
        if (crackMethod == CrackMethod.IOC) {

            // build a candidate keyword, all 'A's to start with
            int keywordLength = dirs.getKeywordLength();
            StringBuilder sbKeyword = new StringBuilder(alphabet.length());
            for (int pos=0; pos < keywordLength; pos++) {
                sbKeyword.append(alphabet.charAt(0));
            }

            Properties crackProps = new Properties();
            crackProps.setProperty(Climb.CLIMB_ALPHABET, alphabet);
            crackProps.setProperty(Climb.CLIMB_CRIBS, cribString);
            crackProps.setProperty(Climb.CLIMB_START_KEYWORD, sbKeyword.toString());

            if (Climb.doClimb(cipherText, this, crackProps)) {
                dirs.setKeyword(crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD));
                explain.append("Success: Searched for best IOC and found all cribs [")
                        .append(cribString)
                        .append("] with key ")
                        .append(crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD)).append("\n")
                        .append(crackProps.getProperty(Climb.CLIMB_ACTIVITY));
                return new CrackResult(dirs, cipherText, crackProps.getProperty(Climb.CLIMB_BEST_DECODE), explain.toString());
            } else { // did not find all cribs, return failed result
                explain.append("Fail: Searched for best IOC, but did not find cribs [")
                        .append(cribString)
                        .append("], best key was ")
                        .append(crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD))
                        .append("\n")
                        .append(crackProps.getProperty(Climb.CLIMB_ACTIVITY));
                return new CrackResult(cipherText, explain.toString());
            }
        } else { // do dictionary search
            Set<String> cribs = Cipher.getCribSet(cribString);
            Dictionary dict = dirs.getLanguage().getDictionary();
            int wordsRead = 0;
            for (String word : dict) {
                if (wordsRead++ % 1000 == 0)
                    Log.i("CipherCrack", "Cracking "+getCipherName()+" Dict: "+wordsRead+" words tried");
                word = word.toUpperCase();
                dirs.setKeyword(word);
                String plainText = decode(cipherText, dirs);
                if (Cipher.containsAllCribs(plainText, cribs)) {
                    explain.append("Success: Searched using ")
                            .append(dict.size())
                            .append(" dictionary words as keys and found all cribs [")
                            .append(cribString)
                            .append("]\n")
                            .append("Keyword ")
                            .append(word)
                            .append(" gave decoded text=")
                            .append(plainText.substring(0, 60))
                            .append("\n");
                    return new CrackResult(dirs, cipherText, plainText, explain.toString());
                }
            }
            dirs.setKeyword(null);
            explain.append("Fail: Searched using ")
                    .append(dict.size())
                    .append(" dictionary words as keys but did not find all cribs [")
                    .append(cribString)
                    .append("]\n");
            return new CrackResult(cipherText, explain.toString());
        }
    }

    /**
     * Fitness for vigenere (and beaufort) is via checking IOC and aiming high
     * @param text the text whose fitness is to be checked
     * @param dirs directives that may be required for the check
     * @return a measure of fitness, higher is better, more fit
     */
    @Override
    public double getFitness(String text, Directives dirs) {
        return StaticAnalysis.calculateIOC(text, dirs.getAlphabet());
    }
}
