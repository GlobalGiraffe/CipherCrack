package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;

/**
 * Class that contains methods to assist with Permutation transposition cipher operations
 * This transposition cipher organises the text into rows (i.e. CR every 'n' chars) and then
 * switches the columns around according to some numbers or to the alphabetical positions in a key
 *   BLUE => 0,3,1,2 (BELU is ordered)
 */
public class Permutation extends Cipher {

    // need some limit when cracking to avoid running too long
    private static final int MAX_CRACK_COLUMNS = 9;

    // only allow A-Z, 0-9 and ','
    private static final InputFilter PERM_INPUT_FILTER = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
        Spanned dest, int dstart, int dend) {
            boolean hasAlpha = false;
            boolean hasDigit = false;
            boolean hasComma = false;
            // check what chars the buffer to be added has
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (!Character.isLetterOrDigit(c) && c != ',') {
                    return "";
                }
                hasAlpha = (hasAlpha || Character.isAlphabetic(c));
                hasDigit = (hasDigit || Character.isDigit(c));
                hasComma = (hasComma || c == ',');
            }
            // if we are adding alpha and the dest already contains digit or ',', don't allow add
            if (hasAlpha && dest.length() > 0
                    && (Character.isDigit(dest.charAt(dest.length()-1))
                     || dest.charAt(dest.length()-1) == ','))
                return "";
            // if we're adding digit or comma and dest already has alpha, don't allow the add
            if ((hasDigit || hasComma) && dest.length() > 0
                    && Character.isAlphabetic(dest.charAt(dest.length()-1)))
                return "";
            return null;
        }
    };

    private int[] permutation = null;
    private boolean readAcross = true;

    public Permutation(Context context) { super(context); }

    /**
     * Describe what this cipher does
     * @return a description for this cipher, split across lines and paragraphs
     */
    @Override
    public String getCipherDescription() {
        return "The Permutation cipher is a transposition cipher where letters are written out in columns, the columns reordered and then cipher text read across. " +
                "For example, if we have 'THISISASECRETMESSAGE' encoded using 2,0,1,4,3:\n" +
                "THISI\n" +
                "SASEC\n" +
                "RETME\n" +
                "SSAGE\n" +
                "The columns are switched, giving:\n" +
                "ITHIS\n" +
                "SSACE\n" +
                "TREEM\n" +
                "ASSEG\n" +
                "Which becomes: ITHISSSACETREEMASSEG\n" +
                "To decipher, the process is reversed, or repeated with the permutations being the order of positions in the first key: 1,2,0,4,3\n\n"+
                "This can be broken by looking for cribs when decoding with all permutations up to 9 columns. After 9 columns the number of possible permutations grows too large and some kind of hill climb with fitness test is needed.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return "Permutation cipher ("+permutationToString(permutation)+":"+(readAcross?"across":"down")+")";
    }

    /**
     * Determine whether the directives are valid for this cipher type, and sets if they are
     * @param dirs the directives to be checked and set
     * @return return the reason for being invalid, or null if the directives ARE valid (and set)
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        String alphabet = dirs.getAlphabet();
        if (alphabet == null || alphabet.length() < 2)
            return "Alphabet is empty or too short";
        CrackMethod crackMethod = dirs.getCrackMethod();
        this.readAcross = dirs.isReadAcross();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            int[] permutation = dirs.getPermutation();
            if (permutation == null || permutation.length == 0)
                return "Permutation is not valid";
            Set<Integer> permSet = new HashSet<>(permutation.length*2);
            for (int perm : permutation) {
                if (perm >= permutation.length)
                    return "Permutation element "+perm+" is too large";
                if (permSet.contains(perm)) {
                    return "Permutation element "+perm+" is repeated";
                }
                permSet.add(perm);
            }
            this.permutation = permutation;
        } else {
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
            if (crackMethod == CrackMethod.DICTIONARY) {
                // for the dictionary
                if (dirs.getLanguage() == null)
                    return "Language must be provided";
            }
        }
        return null;
    }


    @Override
    public void layoutExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {

        TextView keywordLabel = new TextView(context);
        keywordLabel.setText(context.getString(R.string.keyword_or_columns));
        keywordLabel.setTextColor(ContextCompat.getColor(context, R.color.white));
        keywordLabel.setLayoutParams(WRAP_CONTENT_BOTH);

        EditText keywordOrColumns = new EditText(context);
        keywordOrColumns.setText("");
        keywordOrColumns.setTextColor(ContextCompat.getColor(context, R.color.white));
        keywordOrColumns.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);
        keywordOrColumns.setId(ID_PERMUTATION_KEYWORD);
        keywordOrColumns.setBackground(context.getDrawable(R.drawable.entrytext_border));
        keywordOrColumns.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        // ensure input is in capitals, and "A-Z0-9,"
        InputFilter[] editFilters = keywordOrColumns.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 2];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.AllCaps();   // ensures capitals
        newFilters[editFilters.length+1] = PERM_INPUT_FILTER;
        keywordOrColumns.setFilters(newFilters);

        CheckBox checkReadAcross = new CheckBox(context);
        checkReadAcross.setId(ID_PERMUTATION_READ_ACROSS);
        checkReadAcross.setText(context.getString(R.string.use_read_across));
        checkReadAcross.setChecked(true);
        checkReadAcross.setTextColor(ContextCompat.getColor(context, R.color.white));
        checkReadAcross.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);

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
        layout.addView(keywordLabel);
        layout.addView(keywordOrColumns);
        layout.addView(checkReadAcross);
        layout.addView(crackButtonGroup);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        EditText keywordView = layout.findViewById(ID_PERMUTATION_KEYWORD);
        String entry = keywordView.getText().toString();
        int[] columns;
        if (entry.contains(",")) {
            String[] entries = entry.split(",");
            columns = new int[entries.length];
            int i = 0;
            for(String element : entries) {
                try {
                    columns[i++] = Integer.valueOf(element);
                } catch (NumberFormatException ex) {
                    columns = new int[0];
                    break;
                }
            }
        } else {
            columns = convertKeywordToColumns(entry);
        }
        dirs.setPermutation(columns);
        CheckBox useReadAcross = layout.findViewById(ID_PERMUTATION_READ_ACROSS);
        dirs.setReadAcross(useReadAcross.isChecked());
    }

    /**
     * Query the extra layout to find which crack method we're using
     * @param layout layout that may have indication of crack type
     * @return the crack method the user chose
     */
    @Override
    public CrackMethod getCrackMethod(LinearLayout layout) {
        // locate the kind of crack we've been asked to do
        RadioButton dictButton = layout.findViewById(ID_BUTTON_DICTIONARY);
        return (dictButton.isChecked()) ? CrackMethod.DICTIONARY : CrackMethod.BRUTE_FORCE;
    }

    /**
     * Given a keyword, remove duplicate letters and then scan then in order building the
     * integer sequence of ordered distinct columns the keyword represents, e.g. BETA => 3,0,1,2
     * @param keyword the keyword to convert to columns permutation, e.g. ZEBRA => 4,2,1,3,0
     * @return the column permutation represented by the keyword
     */
    int[] convertKeywordToColumns(String keyword) {
        // example BETA => 3,0,1,2, ZEBRA => 4,2,1,3,0
        if (keyword == null)
            return null;
        // collect all distinct letters into a Set that we can later navigate
        keyword = keyword.toUpperCase();
        NavigableSet<Character> charsInKey = new TreeSet<>();
        for (int i=0; i<keyword.length(); i++) {
            char keyChar = keyword.charAt(i);
            if (!Character.isAlphabetic(keyChar)) // not a letter
                return null;
            charsInKey.add(keyChar);
        }
        // means there are repeated letters in the keyword, or keyword is empty
        if (charsInKey.size() == 0 || charsInKey.size() != keyword.length())
            return null;

        // now build the resulting list of columns by traversing the tree in alphabetical order
        int[] columns = new int[charsInKey.size()];
        int i = 0;
        for (Character keyChar : charsInKey) {
            int posInEntry = keyword.indexOf(keyChar);
            columns[i++] = posInEntry;
        }
        return columns;
    }
    /**
     * Encode a text using Permutation cipher with the given column ordering
     * @param plainText the text to be encoded
     * @param dirs a group of directives, we need PERMUTATION (int[]) only
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        int[] permutation = dirs.getPermutation();

        // only works with no gaps in the input text
        plainText = plainText.replaceAll("\\W","");

        // add text to the end to make the right number of columns
        StringBuilder sb = new StringBuilder(plainText);
        while (sb.length() % permutation.length != 0) {
            sb.append("X");
        }
        plainText = sb.toString();

        char[] plainChars = plainText.toCharArray();
        char[] cipherChars = new char[plainText.length()];

        // now read the columns in the right order - first if across
        if (dirs.isReadAcross()) {
            for (int linePos = 0; linePos < plainChars.length; linePos += permutation.length) {
                for (int permPos = 0; permPos < permutation.length; permPos++) {
                    cipherChars[linePos + permPos] = plainChars[linePos + permutation[permPos]];
                }
            }
        } else { // read down the columns in the right ordering of columns
            int cPos = 0;
            for (int perm : permutation) {
                for (int linePos = 0; linePos < plainChars.length; linePos += permutation.length) {
                    cipherChars[cPos++] = plainChars[linePos+perm];
                }
            }
        }
        return String.valueOf(cipherChars);
    }

    /**
     * provide the inverse for a permutation, e.g. 1,2,0,4,3 => 2,0,1,4,3
     * @param perm the permutation we want to find the inverse for
     * @return the inverse permutation
     */
    private int[] invert(int[] perm) {
        int[] inverse = new int[perm.length];
        for (int newPos=0; newPos < perm.length; newPos++) {
            for (int c=0; c < perm.length; c++) {
                if (perm[c] == newPos) {
                    inverse[newPos] = c;
                    break;
                }
            }
        }
        return inverse;
    }

    /**
     * Decode a cipher text using Permutation cipher assuming encoded with given permutation
     * @param cipherText the text to be decoded
     * @param dirs a group of directives, we need PERMUTATION (int[])
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {

        int[] inverse = invert(dirs.getPermutation());
        if (dirs.isReadAcross()) {
            // we take the column order and de-order it (inverse) and then call encode()
            // e.g. 1,2,0,4,3 => 2,0,1,4,3
            Directives newDirs = new Directives();
            newDirs.setAlphabet(dirs.getAlphabet());
            newDirs.setLanguage(dirs.getLanguage());
            newDirs.setPermutation(inverse);
            newDirs.setReadAcross(true);
            // now apply the inverse...
            return encode(cipherText, newDirs);
        } else {
            //String plainText = "DEFENDTHEEASTWALLOFTHECASTLE";
            // DEFE
            // NDTH
            // EEAS
            // TWAL
            // LOFT
            // HECA
            // STLE
            // encoded is then:
            // EHSLTAE
            // DNETLHS
            // EDEWOET
            // FTAAFCL
            // inverse would be 1,2,3,0, we read down again...
            char[] plainText = new char[cipherText.length()];
            int lengthOfRow = cipherText.length()/inverse.length;
            for (int colPos = 0; colPos < lengthOfRow; colPos++) {
                for (int rowPos = 0; rowPos < inverse.length; rowPos++) {
                    plainText[colPos*inverse.length+rowPos] = cipherText.charAt(inverse[rowPos]*lengthOfRow+colPos);
                }
            }
            return String.valueOf(plainText);
        }
    }

    /* TODO - remove this once next one tested
    **
     * Take an input set and add all possible permutations to a List, recursively
     * @param start the start in input to commence from, starts at 0 and increases recursively
     * @param input the possible integers in the permutations, e.g. [0,1,2,3,4,5]
     * @param possiblePerms the List that will get added
    private void createPossiblePermutations(int start, int[] input, List<int[]> possiblePerms) {
        if (start == input.length) {
            possiblePerms.add(Arrays.copyOf(input, input.length));
        } else {
            for (int i = start; i < input.length; i++) {
                // swapping
                int temp = input[i];
                input[i] = input[start];
                input[start] = temp;
                // swap(input[i], input[start]);
                createPossiblePermutations(start + 1, input, possiblePerms);
                // swap(input[i],input[start]);
                temp = input[i];
                input[i] = input[start];
                input[start] = temp;
            }
        }
    }
    */

    /**
     * Crack a permutation cipher by either Brute Force or Dictionary check
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    public CrackResult crack(String cipherText, Directives dirs) {
        CrackMethod method = dirs.getCrackMethod();
        if (method == CrackMethod.BRUTE_FORCE) {
            return crackBruteForce(cipherText, dirs);
        } else {
            return crackDictionary(cipherText, dirs);
        }
    }
    /**
     * Crack a permutation cipher by using words in the dictionary as possible permutations
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    private CrackResult crackDictionary(String cipherText, Directives dirs) {
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        Dictionary dictionary = dirs.getLanguage().getDictionary();
        int wordsChecked = 0;
        for (String word : dictionary) {
            String keyword = word.toUpperCase();
            int[] possiblePerm = convertKeywordToColumns(keyword);
            if (possiblePerm != null) {
                wordsChecked++;
                dirs.setPermutation(possiblePerm);
                dirs.setReadAcross(true);
                String plainText = decode(cipherText, dirs);
                if (containsAllCribs(plainText, cribSet)) {
                    dirs.setKeyword(keyword);
                    String explain = "Success: Dictionary scan: tried using all words in the dictionary as keys to form permutations and look for cribs ["
                            + cribString
                            + "] in the decoded text and found them with "
                            + keyword
                            + ", columns: "
                            + permutationToString(possiblePerm)
                            + ":across\n";
                    return new CrackResult(dirs, cipherText, plainText, explain);
                }
                // did not work with read-across, so try read-down instead
                dirs.setReadAcross(false);
                plainText = decode(cipherText, dirs);
                if (containsAllCribs(plainText, cribSet)) {
                    dirs.setKeyword(keyword);
                    String explain = "Success: Dictionary scan: tried using all words in the dictionary as keys to form permutations and look for cribs ["
                            + cribString
                            + "] in the decoded text and found them with "
                            + keyword
                            + ", columns: "
                            + permutationToString(possiblePerm)
                            + ":down\n";
                    return new CrackResult(dirs, cipherText, plainText, explain);
                }
            }
        }
        String explain = "Fail: Dictionary scan: tried using "
                + wordsChecked
                + " acceptable words in the dictionary of "
                + dictionary.size()
                + " words as keys to form permutations and look for cribs ["
                + cribString
                + "] in the decoded text but did not find them.\n";
        return new CrackResult(cipherText, explain);
    }

    /**
     * Take an input set and add all possible permutations to a List, recursively
     * @param start the start in input to commence from, starts at 0 and increases recursively
     * @param input the possible integers in the permutations, e.g. [0,1,2,3,4,5]
     * @param cipherText the cipher text to be decoded
     * @param cribSet the set of cribs to check for in any decoded text
     * @param dirs the Directives to use to guide the decoding
     */
    private CrackResult checkPossiblePermutations(int start, int[] input, String cipherText, Set<String> cribSet, Directives dirs) {
        if (start == input.length) {
            dirs.setPermutation(input);
            String plainText = decode(cipherText, dirs);
            if (containsAllCribs(plainText, cribSet)) {
                String explain = "Success: Brute force approach: tried decode all permutations up to "
                        + MAX_CRACK_COLUMNS
                        + " columns looking for cribs ["
                        + dirs.getCribs()
                        + "] in the decoded text and found them with "
                        + input.length
                        + " columns: "
                        + Permutation.permutationToString(input)
                        + "\n";
                return new CrackResult(dirs, cipherText, plainText, explain);
            }
        } else {
            for (int i = start; i < input.length; i++) {
                // swapping
                int temp = input[i];
                input[i] = input[start];
                input[start] = temp;
                // swap(input[i], input[start]);
                CrackResult result = checkPossiblePermutations(start + 1, input, cipherText, cribSet, dirs);
                if (result != null)
                    return result;
                // swap(input[i],input[start]);
                temp = input[i];
                input[i] = input[start];
                input[start] = temp;
            }
        }
        return null;
    }

    /**
     * Crack a permutation cipher by checking all permutations (to a max) and looking for cribs
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    private CrackResult crackBruteForce(String cipherText, Directives dirs) {
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);

        for (int columns = 1; columns <= MAX_CRACK_COLUMNS; columns++) {
            int[] input = new int[columns];
            for (int a=0; a < columns; a++) {
                input[a] = a;
            }
            // do the checking in-line, without creating massive lists of permutations
            // Do the decode/containsAllCribs check in createPossiblePermutations() and if found
            // then return a CrackResult else return null and try next size column
            CrackResult crackResult = checkPossiblePermutations(0, input, cipherText, cribSet, dirs);
            if (crackResult != null)
                return crackResult;
        }
        String explain = "Fail: Brute force approach: tried decode all permutations up to "
                + MAX_CRACK_COLUMNS
                + " columns looking for cribs ["
                + cribString
                + "] in the decoded text but did not find them.\n";
        return new CrackResult(cipherText, explain);
    }

    /**
     * Convert a permutation array to a string
     * @param perm permutation integers, e.g. [1,2,0,3]
     * @return the equivalent permutation as a comma-separated string, e.g. "1,2,0,3"
     */
    static String permutationToString(int[] perm) {
        if (perm == null)
            return "null";
        StringBuilder sb = new StringBuilder();
        for (int i=0; i< perm.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(perm[i]);
        }
        //return Arrays.asList(perm).stream().map(n -> n.toString()).collect(Collectors.joining(","));
        return sb.toString();
    }
}
