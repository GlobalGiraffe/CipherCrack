package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.CrackState;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.Settings;

/**
 * Class that contains methods to assist with Permutation transposition cipher operations
 * This transposition cipher organises the text into rows (i.e. CR every 'n' chars) and then
 * switches the columns around according to some numbers or to the alphabetical positions in a key
 *   BLUE => 0,3,1,2 (BELU is ordered)
 */
public class Permutation extends Cipher {

    private static final String TAG = "CrackPermutation";

    // only allow A-Z, 0-9 and ',' in the keyword field
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

    // delete the keyword if 'X' is pressed
    private static final View.OnClickListener PERM_ON_CLICK_DELETE = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText k = v.getRootView().findViewById(R.id.extra_permutation_keyword);
            k.setText("");
        }
    };

    /**
     * Given a keyword, remove duplicate letters and then scan then in order building the
     * integer sequence of ordered distinct columns the keyword represents, e.g. BETA => 3,0,1,2
     * @param keyword the keyword to convert to columns permutation, e.g. ZEBRA => 4,2,1,3,0
     * @return the column permutation represented by the keyword
     */
    static int[] convertKeywordToColumns(String keyword) {
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
     * Static method to convert a permutation array to a string
     * @param perm permutation integers, e.g. [1,2,0,3]
     * @return the equivalent permutation as a comma-separated string, e.g. "1,2,0,3"
     */
    @NotNull
    static String permutationToString(int[] perm) {
        return numbersToString(perm);
    }

    int[] permutation = null;
    boolean readAcross = true;
    private int maxCrackColumns;
    private int decodes = 0;
    private int maxDecodes = 0;

    Permutation(Context context) { super(context, "Permutation"); }

    // Part of Parcelable, used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (permutation == null) {
            dest.writeInt(0);
            dest.writeInt(0);
        } else {
            dest.writeInt(permutation.length);
            dest.writeIntArray(permutation);
        }
        dest.writeInt(readAcross ? 1 : 0);
    }

    // Part of Parcelable, used to read a cipher in a service
    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        int permutationSize = in.readInt();
        permutation = new int[permutationSize];
        in.readIntArray(permutation);
        readAcross = (in.readInt() == 1);
    }

    /**
     * Describe what this cipher does
     * @return a description for this cipher, split across lines and paragraphs
     */
    @Override
    public String getCipherDescription() {
        return "The columnar permutation cipher is a transposition cipher where letters are written out in columns, the columns reordered and then cipher text read across. " +
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
                "Which becomes: ITHISSSACETREEMASSEG if reading across, or ISTATSRSHAESICEESEMG if reading down, either of which could be used.\n" +
                "Occasionally to aid remembering the sequence of columns, the permutation numbers are based on the alphabetical sequence of letters in a keyword, e.g. 0,2,3,1,5,4 for keyword ALBION.\n" +
                "To decipher, the process is reversed, or repeated with the permutations being the inverse of the first, i.e. the order of positions in the first key: 1,2,0,4,3\n\n"+
                "A columnar permutation cipher can be cracked by checking all words in a dictionary (around 10k checks) as the keyword and looking for cribs when decoded. " +
                "If this fails then a brute force approach of checking every permutation up to say 9 columns, and looking for cribs. " +
                "After 9 columns (362k possible permutations) the number of checks required grows large and some kind of hill climb with fitness test is needed.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher ("+permutationToString(permutation)+(permutation==null?"":(":"+(readAcross?"across":"down")))+")";
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
        int[] permutation = dirs.getPermutation();
        this.readAcross = dirs.isReadAcross();
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
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
        } else {
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
            if (crackMethod != CrackMethod.DICTIONARY && crackMethod != CrackMethod.BRUTE_FORCE)
                return "Invalid crack method";
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
        this.permutation = permutation;
        return null;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_permutation);

        EditText keywordOrColumns = layout.findViewById(R.id.extra_permutation_keyword);
        // ensure input is in capitals, and "A-Z0-9,"
        InputFilter[] editFilters = keywordOrColumns.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 3];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.AllCaps();   // ensures capitals
        newFilters[editFilters.length+1] = new InputFilter.LengthFilter(200); // limits text length
        newFilters[editFilters.length+2] = PERM_INPUT_FILTER;
        keywordOrColumns.setFilters(newFilters);

        // ensure we 'delete' the keyword text when the delete button is pressed
        Button keywordDelete = layout.findViewById(R.id.extra_permutation_keyword_delete);
        keywordDelete.setOnClickListener(PERM_ON_CLICK_DELETE);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        EditText keywordView = layout.findViewById(R.id.extra_permutation_keyword);
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
        CheckBox useReadAcross = layout.findViewById(R.id.extra_permutation_read_across);
        dirs.setReadAcross(useReadAcross.isChecked());
    }

    // add 2 buttons, one for dictionary crack, one for brute-force
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_permutation_crack);
        return true;
    }

    /**
     * Fetch the details of the extra crack controls for this cipher
     * @param layout the layout that could contains some crack controls
     * @param dirs the directives to add to
     * @return the crack method to be used
     */
    @Override
    public CrackMethod fetchCrackControls(LinearLayout layout, Directives dirs) {
        // locate the kind of crack we've been asked to do
        RadioButton dictButton = layout.findViewById(R.id.crack_button_dictionary);
        return (dictButton.isChecked()) ? CrackMethod.DICTIONARY : CrackMethod.BRUTE_FORCE;
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

        // remove gaps in the input text - but keep punctuation: cf Cipher Challenge 2011 7a
        plainText = plainText.replaceAll("\\s","");

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

    /**
     * Crack a permutation cipher by either Brute Force or Dictionary check
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    @NotNull
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {
        CrackMethod method = dirs.getCrackMethod();
        if (method == CrackMethod.BRUTE_FORCE) {
            return crackBruteForce(cipherText, dirs, crackId);
        } else {
            return crackDictionary(cipherText, dirs, crackId);
        }
    }
    /**
     * Crack a permutation cipher by using words in the dictionary as possible permutations
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    @NotNull
    private CrackResult crackDictionary(String cipherText, Directives dirs, int crackId) {
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        Dictionary dict = dirs.getLanguage().getDictionary();
        CrackMethod crackMethod = dirs.getCrackMethod();
        String reverseCipherText = new StringBuilder(cipherText).reverse().toString();

        int wordsChecked = 0, wordsRead = 0;
        String foundPlainText = null;
        boolean foundReadAcross = false;
        int[] foundPermutation = null;
        StringBuilder explain = new StringBuilder();
        for (String word : dict) {
            if (wordsRead++ % 500 == 499) {
                if (CrackResults.isCancelled(crackId))
                    return new CrackResult(crackMethod, this, cipherText, "Crack cancelled", CrackState.CANCELLED);
                Log.i("CipherCrack", "Cracking Permutation Dict: " + wordsRead + " words tried");
                CrackResults.updateProgressDirectly(crackId, wordsRead+" words of "+dict.size()+": "+100*wordsRead/dict.size()+"% complete");
            }
            String keyword = word.toUpperCase();
            int[] possiblePerm = convertKeywordToColumns(keyword);
            if (possiblePerm != null) {
                wordsChecked++;
                dirs.setPermutation(possiblePerm);
                dirs.setReadAcross(true);
                String plainText = decode(cipherText, dirs);
                if (containsAllCribs(plainText, cribSet)) {
                    dirs.setKeyword(keyword);
                    permutation = possiblePerm;
                    readAcross = dirs.isReadAcross();
                    explain.append("Success: Dictionary scan: tried using all words in the dictionary as keys to form permutations and look for cribs [")
                            .append(cribString)
                            .append("] in the decoded text and found them with ")
                            .append(keyword)
                            .append(", columns: ")
                            .append(permutationToString(possiblePerm))
                            .append(":across\n");
                    foundReadAcross = readAcross;
                    foundPermutation = Arrays.copyOf(possiblePerm, possiblePerm.length);
                    foundPlainText = plainText;
                }
                plainText = decode(reverseCipherText, dirs);
                if (containsAllCribs(plainText, cribSet)) {
                    dirs.setKeyword(keyword);
                    permutation = possiblePerm;
                    readAcross = dirs.isReadAcross();
                    explain.append("Success: Dictionary scan REVERSE: tried using all words in the dictionary as keys to form permutations and look for cribs [")
                            .append(cribString)
                            .append("] in the decoded REVERSE text and found them with ")
                            .append(keyword)
                            .append(", columns: ")
                            .append(permutationToString(possiblePerm))
                            .append(":across\n");
                    foundReadAcross = readAcross;
                    foundPermutation = Arrays.copyOf(possiblePerm, possiblePerm.length);
                    foundPlainText = plainText;
                }
                // did not work with read-across, so try read-down instead
                dirs.setReadAcross(false);
                plainText = decode(cipherText, dirs);
                if (containsAllCribs(plainText, cribSet)) {
                    dirs.setKeyword(keyword);
                    permutation = possiblePerm;
                    readAcross = dirs.isReadAcross();
                    explain.append("Success: Dictionary scan: tried using all words in the dictionary as keys to form permutations and look for cribs [")
                            .append(cribString)
                            .append("] in the decoded text and found them with ")
                            .append(keyword)
                            .append(", columns: ")
                            .append(permutationToString(possiblePerm))
                            .append(":down\n");
                    foundReadAcross = readAcross;
                    foundPermutation = Arrays.copyOf(possiblePerm, possiblePerm.length);
                    foundPlainText = plainText;
                }
                plainText = decode(reverseCipherText, dirs);
                if (containsAllCribs(plainText, cribSet)) {
                    dirs.setKeyword(keyword);
                    permutation = possiblePerm;
                    readAcross = dirs.isReadAcross();
                    explain.append("Success: Dictionary scan REVERSE: tried using all words in the dictionary as keys to form permutations and look for cribs [")
                            .append(cribString)
                            .append("] in the decoded REVERSE text and found them with ")
                            .append(keyword)
                            .append(", columns: ")
                            .append(permutationToString(possiblePerm))
                            .append(":down\n");
                    foundReadAcross = readAcross;
                    foundPermutation = Arrays.copyOf(possiblePerm, possiblePerm.length);
                    foundPlainText = plainText;
                }
            }
        }
        if (foundPlainText != null) {
            dirs.setReadAcross(foundReadAcross);
            dirs.setPermutation(foundPermutation);
            return new CrackResult(crackMethod, this, dirs, cipherText, foundPlainText, explain.toString());
        }
        dirs.setPermutation(null);
        permutation = null;
        String explainNotFound = "Fail: Dictionary scan: tried using "
                + wordsChecked
                + " acceptable words in the dictionary of "
                + dict.size()
                + " words as keys to form permutations and look for cribs ["
                + cribString
                + "] in the decoded text but did not find them.\n";
        return new CrackResult(crackMethod, this, cipherText, explainNotFound);
    }

    /**
     * Take an input set and add all possible permutations to a List, recursively
     * @param start the start in input to commence from, starts at 0 and increases recursively
     * @param input the possible integers in the permutations, e.g. [0,1,2,3,4,5]
     * @param cipherText the cipher text to be decoded
     * @param reverseCipherText the cipher text to be decoded, also to be checked
     * @param cribSet the set of cribs to check for in any decoded text
     * @param dirs the Directives to use to guide the decoding
     * @return the result of a successful crack finding all cribs, or null if crack was not successful
     */
    @Nullable
    private CrackResult checkPossiblePermutations(int start, int[] input, String cipherText, String reverseCipherText, Set<String> cribSet, Directives dirs, int crackId) {
        if (start == input.length) {
            dirs.setPermutation(input);
            if (decodes++ % 2000 == 1999) {
                if (CrackResults.isCancelled(crackId))
                    return new CrackResult(dirs.getCrackMethod(), this, cipherText, "Crack cancelled", CrackState.CANCELLED);
                CrackResults.updateProgressDirectly(crackId, decodes+" decodes of "+maxDecodes+": "+100*decodes/maxDecodes+"% complete");
                Log.i(TAG, "Permutation crack has performed "+decodes+" of "+maxDecodes+", "+(100*decodes/maxDecodes)+"%");
            }
            dirs.setReadAcross(false);
            String plainText = decode(cipherText, dirs);
            boolean containsAllCribs = containsAllCribs(plainText, cribSet);
            if (!containsAllCribs) {
                dirs.setReadAcross(true);
                plainText = decode(cipherText, dirs);
                containsAllCribs = containsAllCribs(plainText, cribSet);
            }
            if (containsAllCribs) {
                String explain = "Success: Brute Force: tried decode all permutations up to "
                        + maxCrackColumns
                        + " columns looking for cribs ["
                        + dirs.getCribs()
                        + "] in the decoded text and found them with "
                        + input.length
                        + " columns: "
                        + Permutation.permutationToString(input)
                        + ":"
                        + (dirs.isReadAcross()?"across":"down")
                        + "\n";
                permutation = input;
                readAcross = dirs.isReadAcross();
                return new CrackResult(dirs.getCrackMethod(), this, dirs, cipherText, plainText, explain);
            }

            // now try checking the decode of the REVERSE text for the cribs
            dirs.setReadAcross(false);
            plainText = decode(reverseCipherText, dirs);
            containsAllCribs = containsAllCribs(plainText, cribSet);
            if (!containsAllCribs) {
                dirs.setReadAcross(true);
                plainText = decode(reverseCipherText, dirs);
                containsAllCribs = containsAllCribs(plainText, cribSet);
            }
            if (containsAllCribs) {
                String explain = "Success: Brute Force REVERSE: tried decode all permutations up to "
                        + maxCrackColumns
                        + " columns looking for cribs ["
                        + dirs.getCribs()
                        + "] in the decoded REVERSE text and found them with "
                        + input.length
                        + " columns: "
                        + Permutation.permutationToString(input)
                        + ":"
                        + (dirs.isReadAcross()?"across":"down")
                        + "\n";
                permutation = input;
                readAcross = dirs.isReadAcross();
                return new CrackResult(dirs.getCrackMethod(), this, dirs, cipherText, plainText, explain);
            }
        } else {
            for (int i = start; i < input.length; i++) {
                // swapping
                int temp = input[i]; input[i] = input[start]; input[start] = temp;
                CrackResult result = checkPossiblePermutations(start + 1, input, cipherText, reverseCipherText, cribSet, dirs, crackId);
                if (result != null)
                    return result;
                temp = input[i]; input[i] = input[start]; input[start] = temp;
            }
        }
        return null;
    }

    /**
     * Create an int array with set number of columns and containing 0..columns-1
     * @param columns the number of columns in the array
     * @return the constructed column array
     */
    private int [] createPermutationColumns(int columns) {
        int[] input = new int[columns];
        for (int a = 0; a < columns; a++) {
            input[a] = a;
        }
        return input;
    }

    /**
     * Crack a permutation cipher by checking all permutations (to a max) and looking for cribs
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    @NotNull
    private CrackResult crackBruteForce(String cipherText, Directives dirs, int crackId) {
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        CrackResults.updateProgressDirectly(crackId, "Calculating permutation limit");
        maxCrackColumns = Integer.valueOf(Settings.instance().getString(context, R.string.pref_limit_railfence_rails, Settings.DEFAULT_LIMIT_PERM_COLS));
        String reverseCipherText = new StringBuilder(cipherText).reverse().toString();

        // calculate the maximum number of decodes we could do
        // 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, etc
        maxDecodes = 1;
        int thisDecodes = 1;
        for (int columns = 1; columns <= maxCrackColumns; columns++) {
            thisDecodes *= columns;
            maxDecodes += thisDecodes;
        }

        // loop through the possible number of columns, trying every combination
        decodes = 0;
        CrackResults.updateProgressDirectly(crackId, "Looking at first permutations");
        for (int columns = 1; columns <= maxCrackColumns; columns++) {
            Log.i("CipherCrack", "Cracking Permutation with " + columns + " columns");
            // do the checking in-line, without creating massive lists of permutations
            // Do the decode/containsAllCribs check in createPossiblePermutations() and if found
            // then return a CrackResult else return null and try next size column
            int[] input = createPermutationColumns(columns);
            CrackResult crackResult = checkPossiblePermutations(0, input, cipherText, reverseCipherText, cribSet, dirs, crackId);
            if (crackResult != null)
                return crackResult;
        }
        dirs.setPermutation(null);
        permutation = null;
        String explain = "Fail: Brute force approach: tried decode all permutations up to "
                + maxCrackColumns
                + " columns looking for cribs ["
                + cribString
                + "] in the decoded text but did not find them.\n";
        return new CrackResult(dirs.getCrackMethod(), this, cipherText, explain);
    }
}
