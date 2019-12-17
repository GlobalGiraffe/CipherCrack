package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashSet;
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
import mnh.game.ciphercrack.util.KeywordExtend;

/**
 * Class that contains methods to assist with Playfair Cipher operations
 * This digram substitution cipher codes each letter using an easy to memorise algorithm
 *  S I M P L     SIMPLE EXAMPLE keyword
 *  E X A B C
 *  D F G H K
 *  M O Q R T
 *  U V W Y Z
 */
public class Playfair extends Cipher {

    // when the radio button changes, adjust the full keyword to match new method
    private static final View.OnClickListener EXTEND_BUTTON_CLICK_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View rootView = v.getRootView();
            adjustFullKeyword(rootView);
        }
    };

    // delete the keyword / heading if their 'X' is pressed
    private static final View.OnClickListener PLAYFAIR_ON_CLICK_DELETE = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.extra_playfair_keyword_delete:
                    EditText k = v.getRootView().findViewById(R.id.extra_keyword);
                    k.setText("");
                    break;
                case R.id.extra_playfair_rowscols_delete:
                    EditText h = v.getRootView().findViewById(R.id.extra_playfair_rowscols);
                    h.setText("");
                    break;
                case R.id.extra_playfair_replace_delete:
                    EditText r = v.getRootView().findViewById(R.id.extra_replace);
                    r.setText("");
                    break;
                case R.id.extra_playfair_crack_rows_cols_delete:
                    EditText c = v.getRootView().findViewById(R.id.extra_playfair_crack_layout_rows_cols);
                    c.setText("");
                    break;
            }
        }
    };

    // reassess which fields to see when crack methods chosen
    // we should extend when more Playfair cracks are possible and need different inputs
    private static final View.OnClickListener CRACK_METHOD_ASSESSOR = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LinearLayout lSize = v.getRootView().findViewById(R.id.extra_playfair_crack_layout_rows_cols);
            switch (v.getId()) {
                case R.id.crack_button_dictionary:
                    lSize.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private String keyword = ""; // the grid of letters, usually either 25 or 36 chars long
    private int rowscols = 0;    // how many rows/columns make up the square (56 = 5 rows, 6 columns)
    private String replace = ""; // which chars should replace others, e.g. J=>I

    Playfair(Context context) {
        super(context, "Playfair");
    }

    // used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(keyword);
        dest.writeInt(rowscols);
        dest.writeString(replace);
    }
    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        keyword = in.readString();
        rowscols = in.readInt();
        replace = in.readString();
    }

    /**
     * Describe what this cipher does
     *
     * @return a description of this cipher
     */
    @Override
    public String getCipherDescription() {
        return "The Playfair cipher uses a polybius 'square' (usually 5x5 but can be 6x6 or 5x7) to hold the alphabet in the order denoted by a key.\n" +
                "An example using normal alphabetic ordering omitting the letter J:\n" +
                " M Y R I D     using keyword MYRMIDON\n" +
                " O N A B C\n" +
                " E F G H K\n" +
                " L P Q S T\n" +
                " U V W X Z\n" +
                "The table can have letters in any order and not just alphabetic as shown here. Often they are comprised of a keyword followed by the remaining letters of the alphabet, with a rare letter (J or Z) omitted.\n" +
                "To encode a pair of plain letters, if this is a double letter pair, replace the second repeated letter with X. " +
                "If the letter is not there, then always replace with a specific one, e.g. I is often substituted for J to fit 25 chars into the table. " +
                "Then locate the pair in the table and follow these rules:" +
                "1. If the letters are on different rows and columns, encode using the letters on the same row but other corner of the 'square' formed by the plain letters. For example, BL would encode as OS." +
                "2. If the letters are on the same row, replace them with letters to their immediate right, wrapping around if necessary, e.g. TP is encoded as LQ." +
                "3. If the letters are in the same column, replace them with letters immediately below if required, e.g. EU is encoded as LM." +
                "To decode a pair of letters, reverse the process.\n" +
                "This cipher can be broken similar to a substitution cipher by looking at frequencies of digrams, for example with the above table the coding ER=>GM would generally be more common than others.\n";
    }

    /**
     * Show what this instance is configured to do
     *
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName() + " cipher ("+(keyword==null?"n/a":(keyword + ",size=" + rowscols)) + ")";
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
        int rowsAndCols = dirs.getNumberSize();
        int rows = rowsAndCols / 10;
        int cols = rowsAndCols % 10;
        CrackMethod crackMethod = dirs.getCrackMethod();
        String replaceLetters = (dirs.getReplace() == null) ? "" : dirs.getReplace().toUpperCase();
        dirs.setReplace(replaceLetters);
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            if (rows < 3 || rows > 9 || cols < 3)
                return "Invalid value "+rowsAndCols+" for rows and columns";
            if (keywordValue == null)
                return "Keyword is missing";
            if (keywordValue.length() < rows*cols)
                return "Keyword length is "+keywordValue.length()+", should be "+(rows*cols);
            if (keywordValue.length() > rows*cols)
                return "Keyword is longer than expected size";
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
            // check the replace letters are in or not in the keyword
            if (replaceLetters.length() % 2 != 0)
                return "Invalid replacement length "+replaceLetters.length();
            for (int pos=0; pos < replaceLetters.length(); pos+=2) {
                char ch1 = replaceLetters.charAt(pos);
                if (keywordValue.indexOf(ch1) >= 0)
                    return "Replace symbol "+ch1+" must not be in the keyword";
                char ch2 = replaceLetters.charAt(pos+1);
                if (keywordValue.indexOf(ch2) < 0)
                    return "Replace with symbol "+ch2+" must be in the keyword";
            }

            keyword = keywordValue;
        } else {
            if (crackMethod != CrackMethod.DICTIONARY && crackMethod != CrackMethod.WORD_COUNT)
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
            // Check rows and cols
            if (rows < 3 || rows > 5 || cols < 3 || cols > 5)
                return "Cannot crack with rows and columns: "+rowsAndCols;

            // check to ensure replace field has even length
            if (replaceLetters.length() % 2 != 0)
                return "Invalid replacement length "+replaceLetters.length();
        }

        rowscols = rowsAndCols;
        replace = replaceLetters;
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_playfair);

        // Create this:
        // Keyword:
        // [------------------------------] EditText
        // [SizeRowsCols....]X [Replace..X]
        // Fill: [o] First [o] Min [o] Max [o] Last [o] None      RadioButtons
        // [---EXTENDED KEYWORD-----------] TextView

        // when the radio button or keyword text changes, adjust the full-keyword
        final TextWatcher TEXT_CHANGED_LISTENER = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { Cipher.adjustFullKeyword(layout.getRootView()); }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        EditText keywordEditText = layout.findViewById(R.id.extra_keyword);
        keywordEditText.addTextChangedListener(TEXT_CHANGED_LISTENER);

        // define custom filter to ensure a field only has A-Z and 0-9
        InputFilter ensureAlphNumeric = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                // only allow chars in the alphabet to be added, plus digits (e.g. 6x6 playfair)
                for (int i = start; i < end; i++) {
                    char letter = source.charAt(i);
                    if (alphabet.indexOf(letter) < 0 && !Character.isDigit(letter)) {
                        return "";
                    }
                }
                return null;
            }
        };

        // ensure input is in capitals and alpha-numeric only
        Cipher.addInputFilters(layout, R.id.extra_keyword, true, 0, ensureAlphNumeric, NO_DUPE_FILTER);

        // ensure we 'delete' the keyword text when the delete button is pressed
        Button keywordDelete = layout.findViewById(R.id.extra_playfair_keyword_delete);
        keywordDelete.setOnClickListener(PLAYFAIR_ON_CLICK_DELETE);

        // ensure we 'delete' the rows/cols text when the delete button is pressed
        Button rowsColsDelete = layout.findViewById(R.id.extra_playfair_rowscols_delete);
        rowsColsDelete.setOnClickListener(PLAYFAIR_ON_CLICK_DELETE);

        EditText replaceText = layout.findViewById(R.id.extra_replace);
        replaceText.addTextChangedListener(TEXT_CHANGED_LISTENER);

        // ensure replace input is in capitals
        Cipher.addInputFilters(layout, R.id.extra_replace, true, 0, NO_DUPE_FILTER);

        // ensure we 'delete' the hading text when the delete button is pressed
        Button replaceDelete = layout.findViewById(R.id.extra_playfair_replace_delete);
        replaceDelete.setOnClickListener(PLAYFAIR_ON_CLICK_DELETE);

        // when the radio buttons clicked, recalculate the full key
        layout.findViewById(R.id.extra_extend_button_first).setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);
        layout.findViewById(R.id.extra_extend_button_min).setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);
        layout.findViewById(R.id.extra_extend_button_max).setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);
        layout.findViewById(R.id.extra_extend_button_last).setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);
        adjustFullKeyword(layout);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        TextView keywordField = layout.findViewById(R.id.extra_full_keyword);
        String keyword = keywordField.getText().toString();
        dirs.setKeyword(keyword);
        EditText rowsColsField = layout.findViewById(R.id.extra_playfair_rowscols);
        String rowsColsText = rowsColsField.getText().toString();
        try {
            int rc = Integer.valueOf(rowsColsText);
            dirs.setNumberSize(rc);
        } catch (NumberFormatException ex) {
            dirs.setNumberSize(0);
        }
        EditText replaceField = layout.findViewById(R.id.extra_replace);
        String replace = replaceField.getText().toString();
        dirs.setReplace(replace);
    }

    // add 2 buttons, one for dictionary crack, one for word-count
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String cipherText,
                                    Language language, String alphabet, String paddingChars) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_playfair_crack);

        // ensure we 'delete' the field when the delete button is pressed
        Button sizeDelete = layout.findViewById(R.id.extra_playfair_crack_rows_cols_delete);
        sizeDelete.setOnClickListener(PLAYFAIR_ON_CLICK_DELETE);

        // assess when radio buttons pressed to show what fields needed for each type of crack
        RadioGroup group = layout.findViewById(R.id.extra_playfair_crack_radio_group);
        for (int child = 0; child < group.getChildCount(); child++) {
            RadioButton button = (RadioButton)group.getChildAt(child);
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
        EditText rowsColsField = layout.findViewById(R.id.extra_playfair_crack_rows_cols);
        int rc = Integer.valueOf(rowsColsField.getText().toString());
        dirs.setNumberSize(rc);

        // locate the kind of crack we've been asked to do
        RadioButton dictButton = layout.findViewById(R.id.crack_button_dictionary);
        return (dictButton.isChecked()) ? CrackMethod.DICTIONARY : CrackMethod.WORD_COUNT;
    }

    /**
     * Encode a text using Playfair cipher with the given keyword and rows x cols
     *
     * @param plainText the text to be encoded
     * @param dirs a group of directives that define how the cipher will work, especially KEYWORD and NUMBER SIZE
     * @return the encoded strings
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        String keywordUpper = dirs.getKeyword().toUpperCase();
        int rowsCols = dirs.getNumberSize();
        int rows = rowsCols / 10;
        int cols = rowsCols % 10;
        String replaceUpper = (dirs.getReplace() == null) ? "" : dirs.getReplace().toUpperCase();
        plainText = plainText.replaceAll("\\W","").toUpperCase(); // does not work with padding

        // special case for CC 2012 #12 - seems to add X between even unaligned doubles
        boolean insertBetweenDoubles = dirs.isReadAcross();
        if (insertBetweenDoubles) {
            StringBuilder b = new StringBuilder(plainText.length());
            char prevChar = '.';
            for(int pos=0; pos < plainText.length(); pos++) {
                char thisChar = plainText.charAt(pos);
                if (thisChar==prevChar) {
                    b.append("X");
                }
                b.append(thisChar);
                prevChar = thisChar;
            }
            plainText = b.toString();
        }

        // go through the text, a pair of letters at a time
        StringBuilder result = new StringBuilder(plainText.length());
        for (int pos = 0; pos < plainText.length(); ) {

            // read a letter and deal with non-alpha, replacement, end-of-text
            int posInKeyword1, posInKeyword2;
            char plainCharUpper1, plainCharUpper2;
            do {
                plainCharUpper1 = plainText.charAt(pos++);
                int posInReplace = replaceUpper.indexOf(plainCharUpper1);
                if (posInReplace >= 0 && posInReplace % 2 == 0) {
                    plainCharUpper1 = replaceUpper.charAt(posInReplace + 1);
                }
                posInKeyword1 = keywordUpper.indexOf(plainCharUpper1);
                //if (posInKeyword1 < 0)
                //    result.append(plainChar);
            } while (posInKeyword1 < 0 && pos < plainText.length());
            if (posInKeyword1 < 0) {
                plainCharUpper1 = 'X';
                posInKeyword1 = keywordUpper.indexOf(plainCharUpper1);
            }

            // read a letter and deal with non-alpha, replacement, end-of-text
            do {
                plainCharUpper2 = (pos >= plainText.length() ? 'X' : plainText.charAt(pos++));
                int posInReplace = replaceUpper.indexOf(plainCharUpper2);
                if (posInReplace >= 0 && posInReplace % 2 == 0) {
                    plainCharUpper2 = replaceUpper.charAt(posInReplace + 1);
                }
                posInKeyword2 = keywordUpper.indexOf(plainCharUpper2);
                //if (posInKeyword2 < 0)
                //    result.append(plainChar2);
            } while (posInKeyword2 < 0 && pos < plainText.length());
            if (posInKeyword2 < 0) {
                plainCharUpper2 = 'X';
                posInKeyword2 = keywordUpper.indexOf(plainCharUpper2);
                pos--;
            }

            // duplicate letters - use 'X' for second one
            if (plainCharUpper1 == plainCharUpper2) {
                plainCharUpper2 = 'X';
                posInKeyword2 = keywordUpper.indexOf(plainCharUpper2);
            }

            // now we have 2 chars to encode
            int row1 = posInKeyword1 / rows;
            int col1 = posInKeyword1 % rows;
            int row2 = posInKeyword2 / rows;
            int col2 = posInKeyword2 % rows;
            int row1New, col1New, row2New, col2New;
            if (row1 == row2) {
                col1New = (col1+1)%cols;
                row1New = row1;
                col2New = (col2+1)%cols;
                row2New = row2;
            } else {
                if (col1 == col2) {
                    col1New = col1;
                    row1New = (row1 + 1) % rows;
                    col2New = col2;
                    row2New = (row2 + 1) % rows;
                } else {
                    col1New = col2;
                    row1New = row1;
                    col2New = col1;
                    row2New = row2;
                }
            }
            char cipherChar1 = keywordUpper.charAt(row1New*cols+col1New);
            char cipherChar2 = keywordUpper.charAt(row2New*cols+col2New);
            result.append(cipherChar1).append(cipherChar2);
        }
        return result.toString();
    }

    /**
     * Decode a text using Playfair cipher with the given keyword and heading
     *
     * @param cipherText the text to be decoded
     * @param dirs       a group of directives that define how the cipher will work, especially KEYWORD and NUMBER SIZE
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        String keywordLower = dirs.getKeyword().toLowerCase();
        int rowsCols = dirs.getNumberSize();
        int rows = rowsCols / 10;
        int cols = rowsCols % 10;
        cipherText = cipherText.replaceAll("\\W","").toLowerCase(); // does not work with padding

        StringBuilder result = new StringBuilder(cipherText.length());
        for (int pos = 0; pos < cipherText.length(); ) {

            // read a letter and deal with non-alpha, replacement, end-of-text
            int posInKeyword1, posInKeyword2;
            char plainCharLower1, plainCharLower2;
            do {
                plainCharLower1 = cipherText.charAt(pos++);
                posInKeyword1 = keywordLower.indexOf(plainCharLower1);
                //if (posInKeyword1 < 0)
                //    result.append(plainChar);
            } while (posInKeyword1 < 0 && pos < cipherText.length());
            if (posInKeyword1 < 0) {
                plainCharLower1 = 'X';
                posInKeyword1 = keywordLower.indexOf(plainCharLower1);
            }

            // read a letter and deal with non-alpha, replacement, end-of-text
            do {
                plainCharLower2 = (pos >= cipherText.length() ? 'X' : cipherText.charAt(pos++));
                posInKeyword2 = keywordLower.indexOf(plainCharLower2);
                //if (posInKeyword2 < 0)
                //    result.append(plainChar2);
            } while (posInKeyword2 < 0 && pos < cipherText.length());
            if (posInKeyword2 < 0) {
                plainCharLower2 = 'X';
                posInKeyword2 = keywordLower.indexOf(plainCharLower2);
            }

            // duplicate letters - use 'X' for second one
            if (plainCharLower1 == plainCharLower2) {
                plainCharLower2 = 'X';
                posInKeyword2 = keywordLower.indexOf(plainCharLower2);
            }

            // now we have 2 chars to decode, work out their rows / cols in the table
            int row1 = posInKeyword1 / cols;
            int col1 = posInKeyword1 % cols;
            int row2 = posInKeyword2 / cols;
            int col2 = posInKeyword2 % cols;
            int row1New, col1New, row2New, col2New;
            if (row1 == row2) {
                col1New = (col1-1+cols)%cols;
                row1New = row1;
                col2New = (col2-1+cols)%cols;
                row2New = row2;
            } else {
                if (col1 == col2) {
                    col1New = col1;
                    row1New = (row1-1+rows) % rows;
                    col2New = col2;
                    row2New = (row2-1+rows) % rows;
                } else {
                    col1New = col2;
                    row1New = row1;
                    col2New = col1;
                    row2New = row2;
                }
            }
            int newIndex1 = row1New*cols+col1New;
            int newIndex2 = row2New*cols+col2New;
            char cipherChar1 = keywordLower.charAt(newIndex1);
            char cipherChar2 = keywordLower.charAt(newIndex2);
            result.append(cipherChar1).append(cipherChar2);
        }
        return result.toString();
    }

    /**
     * Crack a Playfair cipher by various means
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
            return crackWordCount(cipherText, dirs, crackId);
        }
    }

    /**
     * Crack a Playfair cipher by using a dictionary to generate keywords for the Playfair square
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

        // this will be used to generate the keywords
        alphabet = alphabet.replaceAll("J","");
        dirs.setReplace("JI");

        // returns the first decode that has all the cribs
        CrackResults.updateProgressDirectly(crackId, "Starting "+getCipherName()+" dictionary crack");
        Set<String> cribs = Cipher.getCribSet(cribString);
        Dictionary dict = dirs.getLanguage().getDictionary();
        Set<String> triedKeywords = new HashSet<>(2000);
        int wordsRead = 0, matchesFound = 0;
        StringBuilder successResult = new StringBuilder()
                .append("Success: Dictionary scan: Searched using ")
                .append(dict.size())
                .append(" dictionary words as keywords, looking for cribs [")
                .append(cribString)
                .append("] in decoded text.\n");
        String foundPlainText = "", foundKeyword = null;
        for (String word : dict) {
            if (wordsRead++ % 200 == 199) {
                if (CrackResults.isCancelled(crackId))
                    return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Crack cancelled", CrackState.CANCELLED);
                Log.i("CipherCrack", "Cracking " + getCipherName() + " Dict: " + wordsRead + " words tried, found="+matchesFound);
                CrackResults.updateProgressDirectly(crackId, wordsRead+" words of "+dict.size()+": "+100*wordsRead/dict.size()+"% complete, found="+matchesFound);
            }
            word = word.toUpperCase();

            // could be a number of ways of extending a partial keyword
            for (KeywordExtend extendMethod : KeywordExtend.values()) {

                // we need the whole square filled in, ignore the None method
                if (extendMethod != KeywordExtend.EXTEND_NONE) {
                    String fullKeywordForSquare = applyKeywordExtend(extendMethod, word, alphabet);
                    fullKeywordForSquare.replaceAll("J", "I");
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
                                rowscols = dirs.getNumberSize();
                                replace = dirs.getReplace();
                                dirs.setKeyword(foundKeyword);
                                return new CrackResult(crackMethod, this, dirs, cipherText, plainText, successResult.toString());
                            } else {
                                matchesFound++;
                                foundKeyword = fullKeywordForSquare;
                                foundPlainText = plainText;
                            }
                        }
                        // now try reverse text
                        if (dirs.considerReverse()) {
                            plainText = decode(reverseCipherText, dirs);
                            if (Cipher.containsAllCribs(plainText, cribs)) {
                                successResult.append("Keyword ")
                                        .append(fullKeywordForSquare)
                                        .append(" gave decoded REVERSE text: ")
                                        .append(plainText.substring(0, Math.min(Cipher.CRACK_PLAIN_LENGTH, plainText.length())))
                                        .append("\n");
                                if (dirs.stopAtFirst()) {
                                    keyword = fullKeywordForSquare;
                                    rowscols = dirs.getNumberSize();
                                    replace = dirs.getReplace();
                                    dirs.setKeyword(foundKeyword);
                                    return new CrackResult(crackMethod, this, dirs, cipherText, plainText, successResult.toString());
                                } else {
                                    matchesFound++;
                                    foundKeyword = fullKeywordForSquare;
                                    foundPlainText = plainText;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (foundPlainText.length() > 0) {
            keyword = foundKeyword;
            rowscols = dirs.getNumberSize();
            replace = dirs.getReplace();
            dirs.setKeyword(foundKeyword);
            return new CrackResult(crackMethod, this, dirs, cipherText, foundPlainText, successResult.toString());
        } else {
            dirs.setKeyword(null);
            keyword = null;
            String failResult = "Fail: Dictionary scan: Searched using "
                    + dict.size()
                    + " dictionary words as keys but did not find all cribs ["
                    + cribString
                    + "].\n";
            return new CrackResult(crackMethod, this, cipherText, failResult);
        }
    }

    /**
     * Crack a Playfair cipher using simulated anealing. Start with a keyword built based on
     * the default alphabet with J removed.
     * Then loop around first with low temperature followed by higher for a few thousand iterations
     * each time mutating the key based on the temperature and measuring fitness by counting how
     * many dictionary word letters can be seen in the decoded text.
     * Slower than dictionary check but can find keyword even if not based on a dictionary work
     * or is from a compound word (like LEONARDO DA VINCI)
     * @param cipherText the text to be cracked
     * @param dirs directives we need: ALPHABET, CRIBS, LANGUAGE. On return the CrackResult will
     *              also include EXPLAIN and (if successfully cracked) DECODE_KEYWORD
     * @return the result of the crack attempt
     */
    private CrackResult crackWordCount(String cipherText, Directives dirs, int crackId) {
        String alphabet = dirs.getAlphabet();
        String cribString = dirs.getCribs();
        Language language = dirs.getLanguage();
        CrackMethod crackMethod = dirs.getCrackMethod();

        Properties crackProps = new Properties();
        crackProps.setProperty(Climb.CLIMB_ALPHABET, alphabet);
        crackProps.setProperty(Climb.CLIMB_LANGUAGE, language.getName());
        crackProps.setProperty(Climb.CLIMB_CRIBS, cribString);

        // assume a 5x5 playfair, with J replaced with I, randomise the alphabet a bit
        String startKeyword = Climb.mutateKey(alphabet.replaceAll("J",""), 14);
        crackProps.setProperty(Climb.CLIMB_START_KEYWORD, startKeyword);
        crackProps.setProperty(Climb.CLIMB_NUMBER_SIZE, String.valueOf(55));

        // we are not starting with a key even close to what we need.
        // do a short one (5000 iterations overall) to try to get close (ish)
        // short texts are best cracked with low temperature to start with:
        // < 400 => 2, 400 - 799 => 3, 800 - 1199 => 4, etc
        int countAlpha=0;
        for (int pos=0; pos < cipherText.length(); pos++) {
            if (alphabet.indexOf(Character.toUpperCase(cipherText.charAt(pos))) >= 0)
                countAlpha++;
        }
        int startTemperature = countAlpha/400 + 2;
        crackProps.setProperty(Climb.CLIMB_TEMPERATURE, String.valueOf(startTemperature));
        crackProps.setProperty(Climb.CLIMB_CYCLES, String.valueOf(5000/startTemperature));
        CrackResults.updateProgressDirectly(crackId, "Started first localised simulated anealing climb");

        // returns true if found all cribs, unlikely on first scan
        String firstActivity = "";
        boolean success = Climb.doSimulatedAnealing(cipherText, this, crackProps, crackId);
        Log.i("CipherCrack", "Cracking "+getCipherName()+" Climb, finished first pass");
        if (CrackResults.isCancelled(crackId))
            return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Crack cancelled", CrackState.CANCELLED);
        // we do another pass if we have not matched cribs
        // use different TEMPERATURE and CYCLES based on certain heuristics
        if (!success) {
            // save first activity (explain) so we can include it in final explain text later
            firstActivity = crackProps.getProperty(Climb.CLIMB_ACTIVITY);

            // scan with higher temperature, should not wander too far
            crackProps.setProperty(Climb.CLIMB_START_KEYWORD, crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD));
            crackProps.setProperty(Climb.CLIMB_TEMPERATURE, String.valueOf(10));
            crackProps.setProperty(Climb.CLIMB_CYCLES, String.valueOf(1000));
            CrackResults.updateProgressDirectly(crackId, "Started second wider simulated anealing climb");
            if (!Climb.doSimulatedAnealing(cipherText, this, crackProps, crackId)) {
                if (CrackResults.isCancelled(crackId))
                    return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Crack cancelled", CrackState.CANCELLED);
                // even this did not work, give up
                String explain = "Fail: Searched for largest word match but did not find cribs ["
                        + cribString + "], best key was "
                        + crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD)
                        + ".\n"
                        + firstActivity
                        + crackProps.getProperty(Climb.CLIMB_ACTIVITY);
                keyword = "";
                return new CrackResult(crackMethod, this, cipherText, explain, crackProps.getProperty(Climb.CLIMB_BEST_DECODE));
            }
        }
        // one or other of the simulated anealing above worked, report back
        keyword = crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD);
        dirs.setKeyword(keyword);
        String plainText = crackProps.getProperty(Climb.CLIMB_BEST_DECODE);
        String explain = "Success: Searched for largest word match and found all cribs ["
                + cribString + "] with key "
                + crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD) + ".\n"
                + firstActivity
                + crackProps.getProperty(Climb.CLIMB_ACTIVITY);
        return new CrackResult(crackMethod, this, dirs, cipherText, plainText, explain);
    }

    /**
     * Count how many letters of real words in the dictionary this text contains
     * @param text the text whose fitness is to be checked
     * @param dirs any directives the fitness check requires
     * @return the number of letters of dictionary words found in the text, larger is more fit
     */
    @Override
    public double getFitness(String text, Directives dirs) {
        return Cipher.getWordCountFitness(text, dirs);
    }
}
