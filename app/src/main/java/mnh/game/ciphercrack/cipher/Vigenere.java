package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Properties;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.util.Climb;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.CrackState;
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
            switch (v.getId()) {
                case R.id.extra_vigenere_keyword_delete:
                    EditText k = v.getRootView().findViewById(R.id.extra_vigenere_keyword);
                    k.setText("");
                    break;
                case R.id.extra_vigenere_crack_length_delete:
                    EditText l = v.getRootView().findViewById(R.id.extra_vigenere_crack_length);
                    l.setText("");
                    break;
            }
        }
    };

    // reassess which fields to see when crack methods chosen
    private static final View.OnClickListener CRACK_METHOD_ASSESSOR = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LinearLayout lLen = v.getRootView().findViewById(R.id.extra_vigenere_crack_layout_length);
            switch (v.getId()) {
                case R.id.crack_button_dictionary:
                    lLen.setVisibility(View.GONE);
                    break;
                case R.id.crack_button_ioc:
                    lLen.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    String keyword = "";

    Vigenere(Context context) { super(context, "Vigenere"); }

    // needed for subclasses (Beaufort)
    Vigenere(Context context, String name) { super(context, name); }

    // used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(keyword);
    }
    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        keyword = in.readString();
    }

    /**
     * Describe what this cipher does
     * @return a description of this cipher
     */
    @Override
    public String getCipherDescription() {
        return "The Vigenere cipher is a poly-alphabetic substitution cipher where each letter of the plain text can be mapped to many different letters in the cipher text, depending on the position of the letter in the message. " +
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
        return getCipherName()+" cipher ("+(keyword==null?"n/a":keyword)+")";
    }

    /**
     * Determine whether the properties are valid for this cipher type, and sets if they are
     * @param dirs the properties to be checked and set
     * @return return the reason for being invalid, or null if the properties ARE valid
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        String reason = super.canParametersBeSet(dirs);
        if (reason != null)
            return reason;
        String keywordValue = dirs.getKeyword();
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            if (keywordValue == null || keywordValue.length() < 2)
                return "Keyword is empty or too short";
            // letters in the keyword should be in the alphabet
            for (int i = 0; i < keywordValue.length(); i++) {
                if (dirs.getAlphabet().indexOf(keywordValue.charAt(i)) < 0)
                    return "Character " + keywordValue.charAt(i) + " at offset " + i + " in the keyword is not in the alphabet";
            }
        } else {
            String cribs = dirs.getCribs();
            if (crackMethod != CrackMethod.DICTIONARY && crackMethod != CrackMethod.IOC)
                return "Invalid crack method";

            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";

            // to crack Vigenere via IOC climb then we need a keyword length too
            if (crackMethod == CrackMethod.IOC) {
                int keywordLength = dirs.getKeywordLength();
                if (keywordLength <= 0)
                    return "Keyword length is empty, zero or not a positive integer";
            }
            // to crack Vigenere via Dictionary keys we need a dictionary
            if (crackMethod == CrackMethod.DICTIONARY) {
                // for the dictionary
                Language lang = dirs.getLanguage();
                if (lang == null)
                    return "Language must be provided";
                if (lang.getDictionary() == null) {
                    return "No "+lang.getName()+" dictionary is defined";
                }
            }
        }
        keyword = keywordValue;
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_vigenere);

        // custom filter to ensure a field only has alphabetic text
        InputFilter ensureAlphabetic = new InputFilter() {
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

        // ensure input is in capitals
        Cipher.addInputFilters(layout, R.id.extra_vigenere_keyword, true, 0, ensureAlphabetic);

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
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String cipherText,
                                    Language language, String alphabet, String paddingChars) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_vigenere_crack);

        // ensure we 'delete' the field when the delete button is pressed
        Button lengthDelete = layout.findViewById(R.id.extra_vigenere_crack_length_delete);
        lengthDelete.setOnClickListener(VIGENERE_ON_CLICK_DELETE);

        // assess when radio buttons pressed to show what fields needed for each type of crack
        RadioGroup group = layout.findViewById(R.id.extra_vigenere_crack_radio_group);
        for (int child = 0; child < group.getChildCount(); child++) {
            RadioButton button = (RadioButton)group.getChildAt(child);
            button.setOnClickListener(CRACK_METHOD_ASSESSOR);
        }
        CRACK_METHOD_ASSESSOR.onClick(layout.findViewById(group.getCheckedRadioButtonId()));
        return true;
    }

    /**
     * Fetch the details of the extra crack controls for this cipher
     * @param layout the layout that could contains some crack controls
     * @param dirs the directives to add to
     * @return the crack method to use
     */
    @Override
    public CrackMethod fetchCrackControls(LinearLayout layout, Directives dirs) {
        dirs.setKeywordLength(-2);
        EditText keywordLengthField = layout.findViewById(R.id.extra_vigenere_crack_length);
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
        RadioButton dictButton = layout.findViewById(R.id.crack_button_dictionary);
        return (dictButton.isChecked()) ? CrackMethod.DICTIONARY : CrackMethod.IOC;
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
            char plainCharUpper = Character.toUpperCase(plainChar);
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
            char cipherCharUpper = Character.toUpperCase(cipherChar);
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
     * Crack a Vigenere cipher by checking all words in a dictionary as keys, looking for cribs
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @param crackId used to pass progress results around
     * @return the results of the crack attempt
     */
    private CrackResult crackUsingDictionary(String cipherText, Directives dirs, int crackId) {
        CrackResults.updateProgressDirectly(crackId, "Starting "+getCipherName()+" dictionary crack");
        String cribString = dirs.getCribs();
        CrackMethod crackMethod = dirs.getCrackMethod();
        String reverseCipherText = new StringBuilder(cipherText).reverse().toString();

        Set<String> cribs = Cipher.getCribSet(cribString);
        Dictionary dict = dirs.getLanguage().getDictionary();
        int wordsRead = 0, foundCount = 0;
        String foundWord = null, foundPlainText = "";
        StringBuilder successResult = new StringBuilder()
                .append("Success: Dictionary scan: Searched using ")
                .append(dict.size())
                .append(" dictionary words as keywords looking for cribs [")
                .append(cribString)
                .append("].\n");
        for (String word : dict) {
            if (wordsRead++ % 200 == 199) {
                if (CrackResults.isCancelled(crackId))
                    return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Crack cancelled", CrackState.CANCELLED);
                Log.i("CipherCrack", "Cracking " + getCipherName() + " Dict: " + wordsRead + " words tried, found "+foundCount);
                CrackResults.updateProgressDirectly(crackId, wordsRead+" words of "+dict.size()+": "+100*wordsRead/dict.size()+"% complete, found="+foundCount);
            }
            word = word.toUpperCase();
            dirs.setKeyword(word);
            String plainText = decode(cipherText, dirs);
            if (Cipher.containsAllCribs(plainText, cribs)) {
                successResult.append("Keyword ")
                        .append(word)
                        .append(" gave decoded text: ")
                        .append(plainText.substring(0, Math.min(Cipher.CRACK_PLAIN_LENGTH, plainText.length())))
                        .append("\n");
                if (dirs.stopAtFirst()) {
                    keyword = word;
                    return new CrackResult(crackMethod, this, dirs, cipherText, plainText, successResult.toString());
                } else {
                    foundCount++;
                    foundWord = word;
                    foundPlainText = plainText;
                }
            }
            // now try reverse text decoding
            if (dirs.considerReverse()) {
                plainText = decode(reverseCipherText, dirs);
                if (Cipher.containsAllCribs(plainText, cribs)) {
                    successResult.append("Keyword ")
                            .append(word)
                            .append(" gave decoded REVERSE text: ")
                            .append(plainText.substring(0, Math.min(Cipher.CRACK_PLAIN_LENGTH, plainText.length())))
                            .append("\n");
                    if (dirs.stopAtFirst()) {
                        keyword = word;
                        return new CrackResult(crackMethod, this, dirs, cipherText, plainText, successResult.toString());
                    } else {
                        foundCount++;
                        foundWord = word;
                        foundPlainText = plainText;
                    }
                }
            }
        }

        // let's see what we found, could be zero or multiple words
        if (foundPlainText.length() > 0) {
            dirs.setKeyword(foundWord);
            keyword = foundWord;
            return new CrackResult(crackMethod, this, dirs, cipherText, foundPlainText, successResult.toString());
        }

        // nothing found
        dirs.setKeyword(null);
        keyword = null;
        String failResult = "Fail: Searched using "
                + dict.size()
                + " dictionary words as keywords, looking for cribs ["
                + cribString
                + "] but found none.\n";
        return new CrackResult(crackMethod, this, cipherText, failResult);
    }

    /**
     * Crack a Vigenere cipher by checking all shifts, climbing based on IOC and when best IOC found
     * we take that keyword and look through all equally shifted keys for the cribs
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @param crackId used to pass progress results around
     * @return the results of the crack attempt
     */
    private CrackResult crackUsingIndexOfCoincidence(String cipherText, Directives dirs, int crackId) {
        CrackResults.updateProgressDirectly(crackId, "Starting "+getCipherName()+" hill climb using IOC fitness");
        String alphabet = dirs.getAlphabet();
        String cribString = dirs.getCribs();
        String paddingChars = dirs.getPaddingChars();
        CrackMethod crackMethod = dirs.getCrackMethod();
        StringBuilder explain = new StringBuilder();

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
        crackProps.setProperty(Climb.CLIMB_PADDING_CHARS, paddingChars);

        // publisher.publishProgress(crackId, 1);
        if (Climb.doClimb(cipherText, this, crackProps, crackId)) {
            keyword = crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD);
            dirs.setKeyword(keyword);
            explain.append("Success: Searched for best IOC and found all cribs [")
                    .append(cribString)
                    .append("] with key ")
                    .append(crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD)).append("\n")
                    .append(crackProps.getProperty(Climb.CLIMB_ACTIVITY));
            return new CrackResult(crackMethod, this, dirs, cipherText, crackProps.getProperty(Climb.CLIMB_BEST_DECODE), explain.toString());
        } else { // did not find all cribs, return failed result
            if (CrackResults.isCancelled(crackId))
                return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Crack cancelled", CrackState.CANCELLED);
            keyword = null;
            String bestDecode = crackProps.getProperty(Climb.CLIMB_BEST_DECODE);
            explain.append("Fail: Searched for best IOC, but did not find cribs [")
                    .append(cribString)
                    .append("], best key was ")
                    .append(crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD))
                    .append(", which gave text starting: ")
                    .append(bestDecode.substring(0,Math.min(Cipher.CRACK_PLAIN_LENGTH, bestDecode.length())))
                    .append("\n")
                    .append(crackProps.getProperty(Climb.CLIMB_ACTIVITY));
            return new CrackResult(crackMethod, this, cipherText, explain.toString(), bestDecode);
        }
    }

    /**
     * Crack a Vigenere cipher by one of several methods
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @param crackId used to pass progress results around
     * @return the results of the crack attempt
     */
    @Override
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == CrackMethod.IOC) {
            return crackUsingIndexOfCoincidence(cipherText, dirs, crackId);
        } else { // do dictionary search
            return crackUsingDictionary(cipherText, dirs, crackId);
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
        return StaticAnalysis.calculateIOC(text, dirs.getAlphabet(), dirs.getPaddingChars());
    }
}
