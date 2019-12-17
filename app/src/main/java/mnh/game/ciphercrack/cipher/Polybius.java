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

import java.util.HashSet;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.CrackState;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.KeywordExtend;

/**
 * Class that contains methods to assist with Polybius Cipher operations
 * This digram substitution cipher codes each letter as it's row/column position in a grid
 *    A B C D E
 * A  H O T E L
 * B  A B C D F
 * C  G I K M N
 * D  P Q R S U
 * E  V W X Y Z
 */
public class Polybius extends Cipher {

    // delete the keyword / heading if their 'X' is pressed
    private static final View.OnClickListener POLYBIUS_ON_CLICK_DELETE = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.extra_polybius_keyword_delete:
                    EditText k = v.getRootView().findViewById(R.id.extra_polybius_keyword);
                    k.setText("");
                    break;
                case R.id.extra_polybius_col_heading_delete:
                    EditText ch = v.getRootView().findViewById(R.id.extra_polybius_col_heading);
                    ch.setText("");
                    break;
                case R.id.extra_polybius_row_heading_delete:
                    EditText rh = v.getRootView().findViewById(R.id.extra_polybius_row_heading);
                    rh.setText("");
                    break;
                case R.id.extra_polybius_replace_delete:
                    EditText r = v.getRootView().findViewById(R.id.extra_replace);
                    r.setText("");
                    break;
                case R.id.extra_polybius_crack_col_heading_delete:
                    EditText cch = v.getRootView().findViewById(R.id.extra_polybius_crack_col_heading);
                    cch.setText("");
                    break;
                case R.id.extra_polybius_crack_row_heading_delete:
                    EditText crh = v.getRootView().findViewById(R.id.extra_polybius_crack_row_heading);
                    crh.setText("");
                    break;
            }
        }
    };

    // reassess which fields to see when crack methods chosen
    // extend when more crack methods are added to this cipher
    private static final View.OnClickListener CRACK_METHOD_ASSESSOR = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LinearLayout lColHeading = v.getRootView().findViewById(R.id.extra_polybius_crack_col_heading_layout);
            LinearLayout lRowHeading = v.getRootView().findViewById(R.id.extra_polybius_crack_row_heading_layout);
            switch (v.getId()) {
                case R.id.crack_button_dictionary:
                case R.id.crack_button_brute_force:
                    lColHeading.setVisibility(View.VISIBLE);
                    lRowHeading.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private String keyword = ""; // represents the grid of letters either 25 or 36 chars long
    private String colHeading = ""; // represents the column headings, e.g. ABCDE or 123456
    private String rowHeading = ""; // represents the column headings, e.g. ABCDE or 123456
    private String replace = ""; // represents which chars should replace others, e.g. J=>I

    Polybius(Context context) {
        super(context, "Polybius");
    }

    // used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(keyword);
        dest.writeString(colHeading);
        dest.writeString(rowHeading);
        dest.writeString(replace);
    }

    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        keyword = in.readString();
        colHeading = in.readString();
        rowHeading = in.readString();
        replace = in.readString();
    }

    /**
     * Describe what this cipher does
     *
     * @return a description of this cipher
     */
    @Override
    public String getCipherDescription() {
        return "The Polybius cipher uses a polybius square (usually 5x5 but can be 6x6) to hold the alphabet and unique column and row headings.\n" +
                "An example using normal alphabetic ordering omitting the letter J with column and row headings of 12345 would be:\n" +
                "  1 2 3 4 5\n" +
                "1 A B C D E\n" +
                "2 F G H I K\n" +
                "3 L M N O P\n" +
                "4 Q R S T U\n" +
                "5 V W X Y Z\n" +
                "The column headings can be any letters or digits, the table can have letters in any order not just alphabetic as shown here. Often they are comprised of a keyword followed by the remaining letters of the alphabet, with a rare letter (J or Z) omitted.\n" +
                "To encode a plain letter, locate it in the table and read off the row and column headings, e.g. letter C is encoded as 13, U is encoded as 45. If the letter is not there, then always replace with a specific one, e.g. I is often substituted for J to fit 25 chars into the table.\n" +
                "To decode a pair of letters, use them to locate the row and column and read off the plain letter, e.g. 34 decodes to O, 11 decodes to A. Any letter missing in the table (usually a rare one) is decoded incorrectly, e.g. J will be decoded as I.\n" +
                "This cipher can be broken similar to a substitution cipher by looking at frequencies of digrams, for example with the above table the coding 15 would generally be more common than others as it represents E.\n";
    }

    /**
     * Show what this instance is configured to do
     *
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName() + " cipher (" + (keyword == null ? "n/a" : (keyword + ",cols=" + colHeading + ",rows=" + rowHeading)) + ")";
    }

    /**
     * Determine whether a column or row heading looks reasonable
     *
     * @param type           either 'Col' or 'Row'
     * @param headingLetters the letters to be checked, e.g. ABCDE or 12345
     * @return return the reason for being invalid, or null if the properties ARE valid
     */
    private String canHeadingBeSet(String type, String headingLetters) {
        if (headingLetters == null || headingLetters.length() < 3)
            return type + " heading is missing or too short";
        if (headingLetters.length() > 8)
            return type + " heading is too long";
        // letters in the code letters should not repeat
        for (int i = 0; i < headingLetters.length() - 1; i++) {
            if (headingLetters.indexOf(headingLetters.charAt(i), i + 1) > 0)
                return "Symbol " + headingLetters.charAt(i) + " is repeated in the " + type + " heading";
        }
        return null;
    }

    /**
     * Determine whether the properties are valid for this cipher type, and sets if they are
     *
     * @param dirs the properties to be checked and set
     * @return return the reason for being invalid, or null if the properties ARE valid
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        String reason = super.canParametersBeSet(dirs);
        if (reason != null)
            return reason;
        String keywordValue = dirs.getKeyword();
        String colHeadingLetters = dirs.getColHeading();
        String rowHeadingLetters = dirs.getRowHeading();
        CrackMethod crackMethod = dirs.getCrackMethod();
        String replaceLetters = (dirs.getReplace() == null) ? "" : dirs.getReplace().toUpperCase();
        dirs.setReplace(replaceLetters);
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            if (keywordValue == null || keywordValue.length() < 4)
                return "Keyword is empty or too short";
            if (keywordValue.length() != 9 && keywordValue.length() != 16 && keywordValue.length() != 25 && keywordValue.length() != 36)
                return "Keyword length must be a square number";
            // letters in the keyword should be in the alphabet
            keywordValue = keywordValue.toUpperCase();
            for (int i = 0; i < keywordValue.length(); i++) {
                if (dirs.getAlphabet().indexOf(keywordValue.charAt(i)) < 0)
                    return "Symbol " + keywordValue.charAt(i) + " at offset " + i + " in the keyword is not in the alphabet";
            }
            // letters in the keyword should not repeat
            for (int i = 0; i < keywordValue.length() - 1; i++) {
                if (keywordValue.indexOf(keywordValue.charAt(i), i + 1) > 0)
                    return "Symbol " + keywordValue.charAt(i) + " is repeated in the keyword";
            }

            // Check heading letters have decent lengths and do not contain repeats
            reason = canHeadingBeSet("Col", colHeadingLetters);
            if (reason != null)
                return reason;
            reason = canHeadingBeSet("Row", rowHeadingLetters);
            if (reason != null)
                return reason;
            if (colHeadingLetters.length() * rowHeadingLetters.length() != keywordValue.length())
                return "Heading lengths should multiply to give keyword length";

            keyword = keywordValue;
        } else {
            if (crackMethod != CrackMethod.DICTIONARY)
                return "Invalid crack method";
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";

            // for the dictionary
            Language lang = dirs.getLanguage();
            if (lang == null)
                return "Language must be provided";
            if (lang.getDictionary() == null) {
                return "No " + lang.getName() + " dictionary is defined";
            }
            // Check heading letters have decent lengths and do not contain repeats
            reason = canHeadingBeSet("Col", colHeadingLetters);
            if (reason != null)
                return reason;
            reason = canHeadingBeSet("Row", rowHeadingLetters);
            if (reason != null)
                return reason;
        }

        // check the replace field:
        if (replaceLetters.length() % 2 != 0)
            return "Invalid replacement length " + replaceLetters.length();
        for (int pos = 0; pos < replaceLetters.length(); pos += 2) {
            char ch1 = replaceLetters.charAt(pos);
            if (keywordValue.indexOf(ch1) >= 0)
                return "Replace symbol " + ch1 + " must not be in the keyword";
            char ch2 = replaceLetters.charAt(pos + 1);
            if (keywordValue.indexOf(ch2) < 0)
                return "Replace with symbol " + ch2 + " must be in the keyword";
        }

        colHeading = colHeadingLetters;
        rowHeading = rowHeadingLetters;
        replace = replaceLetters;
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_polybius);

        // custom filter to ensure a field is alphabetic
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
        Cipher.addInputFilters(layout, R.id.extra_polybius_keyword, true, 50, ensureAlphabetic, NO_DUPE_FILTER);

        // ensure we 'delete' the keyword text when the delete button is pressed
        Button keywordDelete = layout.findViewById(R.id.extra_polybius_keyword_delete);
        keywordDelete.setOnClickListener(POLYBIUS_ON_CLICK_DELETE);

        // ensure input is in capitals
        Cipher.addInputFilters(layout, R.id.extra_polybius_col_heading, true, 10, NO_DUPE_FILTER);
        Cipher.addInputFilters(layout, R.id.extra_polybius_row_heading, true, 10, NO_DUPE_FILTER);

        // ensure we 'delete' the heading text when the delete button is pressed
        Button colHeadingDelete = layout.findViewById(R.id.extra_polybius_col_heading_delete);
        colHeadingDelete.setOnClickListener(POLYBIUS_ON_CLICK_DELETE);

        // ensure we 'delete' the heading text when the delete button is pressed
        Button rowHeadingDelete = layout.findViewById(R.id.extra_polybius_row_heading_delete);
        rowHeadingDelete.setOnClickListener(POLYBIUS_ON_CLICK_DELETE);

        // ensure extra replace input is in capitals
        Cipher.addInputFilters(layout, R.id.extra_replace, true, 10, NO_DUPE_FILTER);

        // ensure we 'delete' the replace text when the delete button is pressed
        Button replaceDelete = layout.findViewById(R.id.extra_polybius_replace_delete);
        replaceDelete.setOnClickListener(POLYBIUS_ON_CLICK_DELETE);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        EditText keywordField = layout.findViewById(R.id.extra_polybius_keyword);
        String keyword = keywordField.getText().toString();
        dirs.setKeyword(keyword);
        EditText colHeadingField = layout.findViewById(R.id.extra_polybius_col_heading);
        String colHeading = colHeadingField.getText().toString();
        dirs.setColHeading(colHeading);
        EditText rowHeadingField = layout.findViewById(R.id.extra_polybius_row_heading);
        String rowHeading = rowHeadingField.getText().toString();
        dirs.setRowHeading(rowHeading);
        EditText replaceField = layout.findViewById(R.id.extra_replace);
        String replace = replaceField.getText().toString();
        dirs.setReplace(replace);
    }

    // add 2 buttons, one for dictionary crack, one for brute-force
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String cipherText,
                                    Language language, String alphabet, String paddingChars) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_polybius_crack);

        // ensure col/row heading input is in capitals
        Cipher.addInputFilters(layout, R.id.extra_polybius_crack_col_heading, true, 10, NO_DUPE_FILTER);
        Cipher.addInputFilters(layout, R.id.extra_polybius_crack_row_heading, true, 10, NO_DUPE_FILTER);

        // ensure we 'delete' the field when the delete button is pressed
        Button sizeDelete = layout.findViewById(R.id.extra_polybius_crack_col_heading_delete);
        sizeDelete.setOnClickListener(POLYBIUS_ON_CLICK_DELETE);
        Button cribsDragDelete = layout.findViewById(R.id.extra_polybius_crack_row_heading_delete);
        cribsDragDelete.setOnClickListener(POLYBIUS_ON_CLICK_DELETE);

        // assess when radio buttons pressed to show what fields needed for each type of crack
        RadioGroup group = layout.findViewById(R.id.extra_polybius_crack_radio_group);
        for (int child = 0; child < group.getChildCount(); child++) {
            RadioButton button = (RadioButton) group.getChildAt(child);
            button.setOnClickListener(CRACK_METHOD_ASSESSOR);
        }
        CRACK_METHOD_ASSESSOR.onClick(layout.findViewById(group.getCheckedRadioButtonId()));
        return true;
    }

    /**
     * Fetch the details of the extra crack controls for this cipher
     *
     * @param layout the layout that could contains some crack controls
     * @param dirs   the directives to add to
     * @return the crack method to use
     */
    @Override
    public CrackMethod fetchCrackControls(LinearLayout layout, Directives dirs) {
        EditText colHeadingField = layout.findViewById(R.id.extra_polybius_crack_col_heading);
        String colHeadingStr = colHeadingField.getText().toString();
        dirs.setColHeading(colHeadingStr);

        EditText rowHeadingField = layout.findViewById(R.id.extra_polybius_crack_row_heading);
        String rowHeadingStr = rowHeadingField.getText().toString();
        dirs.setRowHeading(rowHeadingStr);

        // locate the kind of crack we've been asked to do
        RadioButton dictButton = layout.findViewById(R.id.crack_button_dictionary);
        return (dictButton.isChecked()) ? CrackMethod.DICTIONARY : CrackMethod.BRUTE_FORCE;
    }

    /**
     * Encode a text using Polybius cipher with the given keyword and heading
     *
     * @param plainText the text to be encoded
     * @param dirs      a group of directives that define how the cipher will work, especially KEYWORD and HEADING
     * @return the encoded strings
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        String keywordUpper = dirs.getKeyword().toUpperCase();
        String colHeadingUpper = dirs.getColHeading().toUpperCase();
        String rowHeadingUpper = dirs.getRowHeading().toUpperCase();
        int colHeadingLength = colHeadingUpper.length();
        String replaceUpper = (dirs.getReplace() == null) ? "" : dirs.getReplace().toUpperCase();

        StringBuilder result = new StringBuilder(plainText.length());
        for (int pos = 0; pos < plainText.length(); pos++) {
            char plainChar = plainText.charAt(pos);
            char plainCharUpper = Character.toUpperCase(plainChar);
            int posInKeyword = keywordUpper.indexOf(plainCharUpper);
            if (posInKeyword < 0) {
                int posInReplace = replaceUpper.indexOf(plainCharUpper);
                if (posInReplace >= 0 && posInReplace % 2 == 0) {
                    char replacementChar = replaceUpper.charAt(posInReplace + 1);
                    posInKeyword = keywordUpper.indexOf(replacementChar);
                }
            }
            // if not in the keyword or no replacement char or replacement char not in keyword
            // then just copy the char to the output, could be punctuation or space
            if (posInKeyword < 0) {
                result.append(plainChar);
            } else {
                int row = posInKeyword / colHeadingLength;
                int col = posInKeyword % colHeadingLength;
                result.append(rowHeadingUpper.charAt(row)).append(colHeadingUpper.charAt(col));
            }
        }
        return result.toString();
    }

    /**
     * Decode a text using Polybius cipher with the given keyword and heading
     *
     * @param cipherText the text to be decoded
     * @param dirs       a group of directives that define how the cipher will work, especially KEYWORD and HEADING
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        String keywordLower = dirs.getKeyword().toLowerCase();
        String colHeadingUpper = dirs.getColHeading().toUpperCase();
        String rowHeadingUpper = dirs.getRowHeading().toUpperCase();
        int colHeadingLength = colHeadingUpper.length();

        // decode does not work with padding
        // but tests assume it does... so make following code skip non-heading chars
        // cipherText = cipherText.replaceAll("\\s","");

        StringBuilder result = new StringBuilder(cipherText.length());
        for (int i = 0; i < cipherText.length(); ) {

            // find first character from the cipher text
            char cipherChar1 = cipherText.charAt(i++);
            char cipherCharUpper1 = Character.toUpperCase(cipherChar1);
            int offset1 = rowHeadingUpper.indexOf(cipherCharUpper1);
            // char is not in the heading (e.g. space), or we're at the end - just add this to result
            while (offset1 < 0 && i < cipherText.length()) {
                result.append(cipherChar1);
                if (i < cipherText.length()) {
                    cipherChar1 = cipherText.charAt(i++);
                    cipherCharUpper1 = Character.toUpperCase(cipherChar1);
                    offset1 = rowHeadingUpper.indexOf(cipherCharUpper1);
                }
            }
            int offset2 = -1;
            if (i < cipherText.length()) {
                // get second character from the cipher text
                char cipherChar2 = cipherText.charAt(i++);
                char cipherCharUpper2 = Character.toUpperCase(cipherChar2);
                offset2 = colHeadingUpper.indexOf(cipherCharUpper2);
                // get second char - if this is not in a heading, weird - just add both to output
                while (offset2 < 0 && i < cipherText.length()) {
                    result.append(cipherChar2);
                    if (i < cipherText.length()) {
                        cipherChar2 = cipherText.charAt(i++);
                        cipherCharUpper2 = Character.toUpperCase(cipherChar2);
                        offset2 = colHeadingUpper.indexOf(cipherCharUpper2);
                    }
                }
                if (offset1 >= 0 && offset2 >= 0) {
                    // decode this pair into the plain letter in the grid at this row/col
                    result.append(keywordLower.charAt(colHeadingLength * offset1 + offset2));
                }
            } else {
                result.append(cipherChar1);
            }
        }
        return result.toString();
    }

    /**
     * Crack a Polybius cipher by various means
     *
     * @param cipherText the text to try to crack
     * @param dirs       the directives with alphabet and cribs
     * @return the results of the crack attempt
     */
    @Override
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == CrackMethod.DICTIONARY) {
            return crackDictionary(cipherText, dirs, crackId);
        } else {
            // TODO - use Word Count Simulated Anealing crack
            return new CrackResult(crackMethod, this, cipherText, "Unable to crack with that method");
        }
    }

    /**
     * Crack a Polybius cipher by using a dictionary to generate keywords for the Polybius square
     *
     * @param cipherText the text to try to crack
     * @param dirs       the directives with alphabet and cribs
     * @return the results of the crack attempt
     */
    private CrackResult crackDictionary(String cipherText, Directives dirs, int crackId) {
        String alphabet = dirs.getAlphabet();
        String cribString = dirs.getCribs();
        CrackMethod crackMethod = dirs.getCrackMethod();
        String reverseCipherText = new StringBuilder(cipherText).reverse().toString();
        StringBuilder reverser = new StringBuilder(cipherText.length());

        // this will be used to generate the keywords
        alphabet = alphabet.replaceAll("J", "");
        dirs.setReplace("JI");

        // returns the first decode that has all the cribs
        CrackResults.updateProgressDirectly(crackId, "Starting " + getCipherName() + " dictionary crack");
        Set<String> cribs = Cipher.getCribSet(cribString);
        Dictionary dict = dirs.getLanguage().getDictionary();
        Set<String> triedKeywords = new HashSet<>(2000);
        int wordsRead = 0, matchesFound = 0;
        String foundPlainText = "", foundKeyword = null;
        StringBuilder successResult = new StringBuilder()
                .append("Success: Dictionary scan: Searched using ")
                .append(dict.size())
                .append(" dictionary words as keywords looking for cribs [")
                .append(cribString)
                .append("] in the decoded text.\n");
        for (String word : dict) {
            if (wordsRead++ % 200 == 199) {
                if (CrackResults.isCancelled(crackId))
                    return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Crack cancelled", CrackState.CANCELLED);
                Log.i("CipherCrack", "Cracking " + getCipherName() + " Dict: " + wordsRead + " words tried, found=" + matchesFound);
                CrackResults.updateProgressDirectly(crackId, wordsRead + " words of " + dict.size() + ": " + 100 * wordsRead / dict.size() + "% complete, found=" + matchesFound);
            }
            word = word.toUpperCase();

            // could be a number of ways of extending a partial keyword
            for (KeywordExtend extendMethod : KeywordExtend.values()) {

                // we need the whole square filled in, ignore the None method
                if (extendMethod != KeywordExtend.EXTEND_NONE) {
                    String fullKeywordForSquare = applyKeywordExtend(extendMethod, word, alphabet);

                    // don't try to decode the cipherText with this keyword if already tried
                    if (!triedKeywords.contains(fullKeywordForSquare)) {
                        triedKeywords.add(fullKeywordForSquare);
                        dirs.setKeyword(fullKeywordForSquare);
                        String plainText = decode(cipherText, dirs);
                        if (Cipher.containsAllCribs(plainText, cribs)) {
                            successResult.append("Keyword ")
                                    .append(fullKeywordForSquare)
                                    .append(" gave decoded text: ")
                                    .append(plainText.substring(0, Math.min(Cipher.CRACK_PLAIN_LENGTH, plainText.length())))
                                    .append("\n");
                            if (dirs.stopAtFirst()) {
                                keyword = fullKeywordForSquare;
                                colHeading = dirs.getColHeading();
                                rowHeading = dirs.getRowHeading();
                                replace = dirs.getReplace();
                                dirs.setKeyword(foundKeyword);
                                return new CrackResult(crackMethod, this, dirs, cipherText, plainText, successResult.toString());
                            } else {
                                matchesFound++;
                                foundPlainText = plainText;
                                foundKeyword = fullKeywordForSquare;
                            }
                        }
                        // now try reverse text
                        if (dirs.considerReverse()) {
                            reverser.setLength(0);
                            String reversePlainText = reverser.append(plainText).reverse().toString();
                            if (Cipher.containsAllCribs(reversePlainText, cribs)) {
                                successResult.append("Keyword ")
                                        .append(fullKeywordForSquare)
                                        .append(" gave REVERSE decoded text: ")
                                        .append(reversePlainText.substring(0, Math.min(Cipher.CRACK_PLAIN_LENGTH, reversePlainText.length())))
                                        .append("\n");
                                if (dirs.stopAtFirst()) {
                                    keyword = fullKeywordForSquare;
                                    colHeading = dirs.getColHeading();
                                    rowHeading = dirs.getRowHeading();
                                    replace = dirs.getReplace();
                                    dirs.setKeyword(foundKeyword);
                                    return new CrackResult(crackMethod, this, dirs, cipherText, reversePlainText, successResult.toString());
                                } else {
                                    matchesFound++;
                                    foundPlainText = reversePlainText;
                                    foundKeyword = fullKeywordForSquare;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (foundPlainText.length() > 0) {
            keyword = foundKeyword;
            colHeading = dirs.getColHeading();
            rowHeading = dirs.getRowHeading();
            replace = dirs.getReplace();
            dirs.setKeyword(foundKeyword);
            return new CrackResult(crackMethod, this, dirs, cipherText, foundPlainText, successResult.toString());
        }
        dirs.setKeyword(null);
        keyword = colHeading = rowHeading = null;
        String failResult = "Fail: Dictionary scan: Searched using "
                + dict.size()
                + " dictionary words as keywords looking for all cribs ["
                + cribString
                + "] but did not fund them.\n";
        return new CrackResult(crackMethod, this, cipherText, failResult);
    }
}
