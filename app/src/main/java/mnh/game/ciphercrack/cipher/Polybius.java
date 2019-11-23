package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
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

import java.util.HashSet;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
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
                case R.id.extra_polybius_heading_delete:
                    EditText h = v.getRootView().findViewById(R.id.extra_polybius_heading);
                    h.setText("");
                    break;
                case R.id.extra_polybius_replace_delete:
                    EditText r = v.getRootView().findViewById(R.id.extra_polybius_replace);
                    r.setText("");
                    break;
            }
        }
    };

    private String keyword = ""; // represents the grid of letters either 25 or 36 chars long
    private String heading = ""; // represents the column headings, e.g. ABCDE or 123456
    private String replace = ""; // represents which chars should replace others, e.g. J=>I

    Polybius(Context context) {
        super(context, "Polybius");
    }

    // used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(keyword);
        dest.writeString(heading);
        dest.writeString(replace);
    }
    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        keyword = in.readString();
        heading = in.readString();
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
        return getCipherName() + " cipher ("+(keyword==null?"n/a":(keyword + ",heading=" + heading)) + ")";
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
        String headingLetters = dirs.getHeading();
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

            // Check heading letters
            if (headingLetters == null || headingLetters.length() < 3)
                return "Heading is missing or too short";
            if (headingLetters.length() * headingLetters.length() != keywordValue.length())
                return "Heading length must be square-root of keyword length";
            // letters in the code letters should not repeat
            for (int i = 0; i < headingLetters.length() - 1; i++) {
                if (headingLetters.indexOf(headingLetters.charAt(i), i + 1) > 0)
                    return "Symbol " + headingLetters.charAt(i) + " is repeated in the Heading";
            }

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
            // Check heading letters
            if (headingLetters == null || headingLetters.length() < 3)
                return "Heading is missing or too short";
            if (headingLetters.length() > 6)
                return "Heading too long to crack";
            // letters in the code letters should not repeat
            for (int i = 0; i < headingLetters.length() - 1; i++) {
                if (headingLetters.indexOf(headingLetters.charAt(i), i + 1) > 0)
                    return "Symbol " + headingLetters.charAt(i) + " is repeated in the Heading";
            }
        }

        // check the replace field:
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

        heading = headingLetters;
        replace = replaceLetters;
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_polybius);

        // ensure input is in capitals
        EditText keyword = layout.findViewById(R.id.extra_polybius_keyword);
        InputFilter[] editFilters = keyword.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 2];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.AllCaps();   // ensures capitals
        newFilters[editFilters.length + 1] = new InputFilter() {
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
        Button keywordDelete = layout.findViewById(R.id.extra_polybius_keyword_delete);
        keywordDelete.setOnClickListener(POLYBIUS_ON_CLICK_DELETE);

        // ensure input is in capitals
        EditText headingText = layout.findViewById(R.id.extra_polybius_heading);
        editFilters = headingText.getFilters();
        newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.AllCaps();   // ensures capitals
        headingText.setFilters(newFilters);

        // ensure we 'delete' the hading text when the delete button is pressed
        Button headingDelete = layout.findViewById(R.id.extra_polybius_heading_delete);
        headingDelete.setOnClickListener(POLYBIUS_ON_CLICK_DELETE);

        // ensure input is in capitals
        EditText replaceText = layout.findViewById(R.id.extra_polybius_replace);
        editFilters = replaceText.getFilters();
        newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.AllCaps();   // ensures capitals
        replaceText.setFilters(newFilters);

        // ensure we 'delete' the hading text when the delete button is pressed
        Button replaceDelete = layout.findViewById(R.id.extra_polybius_replace_delete);
        replaceDelete.setOnClickListener(POLYBIUS_ON_CLICK_DELETE);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        EditText keywordField = layout.findViewById(R.id.extra_polybius_keyword);
        String keyword = keywordField.getText().toString();
        dirs.setKeyword(keyword);
        EditText headingField = layout.findViewById(R.id.extra_polybius_heading);
        String heading = headingField.getText().toString();
        dirs.setHeading(heading);
        EditText replaceField = layout.findViewById(R.id.extra_polybius_replace);
        String replace = replaceField.getText().toString();
        dirs.setReplace(replace);
    }

    /**
     * Add crack controls for this cipher: type of crack to be done
     *
     * @param context  the context
     * @param layout   the layout to add any crack controls to
     * @param alphabet the current alphabet
     * @return true if controls added, else false
     */
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        TextView headingLabel = new TextView(context);
        headingLabel.setText(context.getString(R.string.heading));
        headingLabel.setTextColor(ContextCompat.getColor(context, R.color.white));
        headingLabel.setLayoutParams(WRAP_CONTENT_BOTH);

        EditText headingText = new EditText(context);
        headingText.setText(context.getString(R.string.default_heading));
        headingText.setPadding(3, 3, 3, 3);
        headingText.setTextColor(ContextCompat.getColor(context, R.color.entry_text_text));
        headingText.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);
        headingText.setId(ID_HEADING_LENGTH);
        headingText.setBackground(context.getDrawable(R.drawable.entry_text_border));
        headingText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

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

        RadioButton bruteForceButton = new RadioButton(context);
        bruteForceButton.setId(ID_BUTTON_BRUTE_FORCE);
        bruteForceButton.setText(context.getString(R.string.crack_brute_force));
        bruteForceButton.setChecked(false);
        bruteForceButton.setEnabled(false);
        bruteForceButton.setTextColor(ContextCompat.getColor(context, R.color.white));
        bruteForceButton.setLayoutParams(WRAP_CONTENT_BOTH);

        RadioGroup crackButtonGroup = new RadioGroup(context);
        crackButtonGroup.check(ID_BUTTON_DICTIONARY);
        crackButtonGroup.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);
        crackButtonGroup.setOrientation(LinearLayout.HORIZONTAL);
        crackButtonGroup.addView(dictButton);
        crackButtonGroup.addView(bruteForceButton);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);
        layout.addView(headingLabel);
        layout.addView(headingText);
        layout.addView(crackLabel);
        layout.addView(crackButtonGroup);

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
        EditText headingField = layout.findViewById(ID_HEADING_LENGTH);
        String headingStr = headingField.getText().toString();
        dirs.setHeading(headingStr);

        // locate the kind of crack we've been asked to do
        RadioButton dictButton = layout.findViewById(ID_BUTTON_DICTIONARY);
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
        String headingUpper = dirs.getHeading().toUpperCase();
        String replaceUpper = (dirs.getReplace() == null) ? "" : dirs.getReplace().toUpperCase();
        int headingLength = headingUpper.length();

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
                int row = posInKeyword / headingLength;
                int col = posInKeyword % headingLength;
                result.append(headingUpper.charAt(row)).append(headingUpper.charAt(col));
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
        String headingUpper = dirs.getHeading().toUpperCase();
        int headingLength = headingUpper.length();

        StringBuilder result = new StringBuilder(cipherText.length());
        for (int i = 0; i < cipherText.length(); ) {
            char cipherChar1 = cipherText.charAt(i);
            char cipherCharUpper1 = Character.toUpperCase(cipherChar1);
            int offset1 = headingUpper.indexOf(cipherCharUpper1);
            // char is not in the heading, or we're at the end - just add this to result
            if (offset1 < 0 || i == cipherText.length() - 1) {
                result.append(cipherChar1);
                i++;
            } else {
                char cipherChar2 = cipherText.charAt(++i);
                char cipherCharUpper2 = Character.toUpperCase(cipherChar2);
                int offset2 = headingUpper.indexOf(cipherCharUpper2);
                // get second char - if this is not in a heading, weird - just add both to output
                if (offset2 < 0) {
                    result.append(cipherChar1).append(cipherChar2);
                    i++;
                } else {
                    // decode this pair into the plain letter in the grid at this row/col
                    result.append(keywordLower.charAt(headingLength * offset1 + offset2));
                    i++;
                }
            }
        }
        return result.toString();
    }

    /**
     * Crack a Polybius cipher by using a dictionary to generate keywords for the Polybius square
     *
     * @param cipherText the text to try to crack
     * @param dirs       the directives with alphabet and cribs
     * @return the results of the crack attempt
     */
    @Override
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        String alphabet = dirs.getAlphabet();
        String cribString = dirs.getCribs();
        CrackMethod crackMethod = dirs.getCrackMethod();
        // this will be used to generate the keywords
        alphabet = alphabet.replaceAll("J","");
        dirs.setReplace("JI");

        // returns the first decode that has all the cribs
        StringBuilder explain = new StringBuilder();
        if (crackMethod == CrackMethod.DICTIONARY) {
            Set<String> cribs = Cipher.getCribSet(cribString);
            Dictionary dict = dirs.getLanguage().getDictionary();
            int wordsRead = 0;
            Set<String> triedKeywords = new HashSet<>(2000);
            for (String word : dict) {
                if (wordsRead++ % 500 == 499) {
                    Log.i("CipherCrack", "Cracking " + getCipherName() + " Dict: " + wordsRead + " words tried");
                    CrackResults.updatePercentageDirectly(crackId, 100 * wordsRead / dict.size());
                }
                word = word.toUpperCase();

                // could be a number of ways of extending a partial keyword
                for (KeywordExtend extendMethod : KeywordExtend.values()) {
                    String fullKeywordForSquare = applyKeywordExtend(extendMethod, word, alphabet);
                    // don't try to decode the cipherText with this keyword if already tried
                    if (!triedKeywords.contains(fullKeywordForSquare)) {
                        triedKeywords.add(fullKeywordForSquare);
                        dirs.setKeyword(fullKeywordForSquare);
                        String plainText = decode(cipherText, dirs);
                        if (Cipher.containsAllCribs(plainText, cribs)) {
                            keyword = fullKeywordForSquare;
                            heading = dirs.getHeading();
                            replace = dirs.getReplace();
                            explain.append("Success: Searched using ")
                                    .append(dict.size())
                                    .append(" dictionary words as keys and found all cribs [")
                                    .append(cribString)
                                    .append("]\n")
                                    .append("Keyword ")
                                    .append(fullKeywordForSquare)
                                    .append(" gave decoded text=")
                                    .append(plainText.substring(0, 60))
                                    .append("\n");
                            return new CrackResult(crackMethod, this, dirs, cipherText, plainText, explain.toString());
                        }
                    }
                }
            }
            dirs.setKeyword(null);
            keyword = null;
            explain.append("Fail: Searched using ")
                    .append(dict.size())
                    .append(" dictionary words as keys but did not find all cribs [")
                    .append(cribString)
                    .append("]\n");
            return new CrackResult(crackMethod, this, cipherText, explain.toString());
        } else {
            return new CrackResult(crackMethod, this, cipherText, "Unable to crack with that method");
        }
    }

}
