package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.KeywordExtend;
import mnh.game.ciphercrack.util.Settings;

/**
 * Base class for all ciphers
 *
 *  This is parcelable because we need to pass the Cipher to a Service to do a crack
 *    and some cracks (e.g. Vigenere IOC) need specific values, like keywordLength
 */
abstract public class Cipher implements Parcelable {

    // only allow A-Z, 0-9 and ',' in the keyword field
    static final InputFilter PERM_INPUT_FILTER = new InputFilter() {
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

    // filter to avoid dupes in the input fields
    static final InputFilter NO_DUPE_FILTER = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            // if adding (rather than removing) chars
            if (end-start > 0) {
                // only allow chars if not already in dest
                String destStr = dest.toString();
                String s = "";
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);

                    // if already in dest, but not the bit being replaced, we will discard this letter
                    int posInDest = destStr.indexOf(c);
                    boolean inPartOfDestWeAreNotReplacing = (posInDest >= 0 && (posInDest < dstart || posInDest >= dend));
                    // if already in the source (already scanned and accepted), ignore it
                    if (s.indexOf(c) < 0 && !inPartOfDestWeAreNotReplacing)
                        s += c;
                }
                // if all chars between start and end are now in S, then the source is fine as is
                // if not all there, then return the shorter S string
                return (s.length() == (end-start)) ? null : s ;
            }
            return null;
        }
    };

    // how much text of possible solutions to show when cracking
    public static final int CRACK_PLAIN_LENGTH = 60;

    // build a cipher from a parcel
    public static Cipher instanceOf(Parcel parcel, Context context) {
        String name = parcel.readString();
        Cipher cipher = instanceOf(name, context);
        cipher.unpack(parcel);
        return cipher;
    }

    // build a new empty cipher instance
    @Nullable
    public static Cipher instanceOf(String name, Context context) {
        Cipher cipher = null;
        switch (name) {
            case "Caesar":
                cipher = new Caesar(context);
                break;
            case "ROT13":
                cipher = new Rot13(context);
                break;
            case "Affine":
                cipher = new Affine(context);
                break;
            case "Vigenere":
                cipher = new Vigenere(context);
                break;
            case "Substitution":
                cipher = new KeywordSubstitution(context);
                break;
            case "Atbash":
                cipher = new Atbash(context);
                break;
            case "Beaufort":
                cipher = new Beaufort(context);
                break;
            case "Railfence":
                cipher = new Railfence(context);
                break;
            case "Permutation":
                cipher = new Permutation(context);
                break;
            case "Binary":
                cipher = new Binary(context);
                break;
            case "Skytale":
                cipher = new Skytale(context);
                break;
            case "Polybius":
                cipher = new Polybius(context);
                break;
            case "Hill":
                cipher = new Hill(context);
                break;
            case "Playfair":
                cipher = new Playfair(context);
                break;
            case "Morse":
                cipher = new Morse(context);
                break;
            case "Amsco":
                cipher = new Amsco(context);
                break;
            case "Porta":
                // TODO cipher = new Porta(context);
                break;
            case "Bifid":
                // TODO cipher = new Bifid(context);
                break;
            default:
                Toast.makeText(context, "Unknown cipher: "+name, Toast.LENGTH_LONG).show();
                break;
        }
        return cipher;
    }

    // some ciphers remove padding, etc - need to know what padding is there, what alpha is, etc
    final Context context;
    private final String cipherName;

    Cipher(Context context, String name) { cipherName = name; this.context = context; }

    // used to send a cipher to a service
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cipherName);
    }
    public int describeContents() { return 0; }
    // needed by Parcelable interface, to recreate the passed data
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Cipher createFromParcel(Parcel in) {
            String name = in.readString();
            Cipher cipher = instanceOf(name, null);
            cipher.unpack(in);
            return cipher;
        }
        public Cipher[] newArray(int size) {
            return new Cipher[size];
        }
    };
    // subclasses call this, root class does nothing during unpack
    void unpack(Parcel in) { }


    /**
     * This takes the provided KeywordExtend method and a short keyword and then applies the method
     *  to produce a full-sized keyword with the same length as the alphabet
     * @param extend the method to use to extend the keyword, min, max or last
     * @param keyword the initial keyword, in upper case
     * @param alphabet the alphabet to use
     * @return the resulting keyword with all alphabet letters
     */
    static String applyKeywordExtend(KeywordExtend extend, String keyword, String alphabet) {
        return applyKeywordExtend(extend, keyword, alphabet, "");
    }

    /**
     * This takes the provided KeywordExtend method and a short keyword and then applies the method
     *  to produce a full-sized keyword with the same length as the alphabet, excluding some chars
     *  if required
     * @param extend the method to use to extend the keyword, min, max or last
     * @param keyword the initial keyword, in upper case
     * @param alphabet the alphabet to use
     * @param exclude any letters to be excluded from the keyword, e.g. J for some Polybius
     * @return the resulting keyword with all alphabet letters
     */
    static String applyKeywordExtend(KeywordExtend extend, String keyword, String alphabet, String exclude) {

        // e.g. for 6x6 we may have digits - don't extend, just use as-is
        if (extend == KeywordExtend.EXTEND_NONE) {
            return keyword;
        } else {
            StringBuilder sb = new StringBuilder(alphabet.length());

            // First, we start to form the full keyword by taking each unique letter of the keyword
            SortedSet<Character> charsUsed = new TreeSet<>();
            for (int p = 0; p < keyword.length(); p++) {
                char nextChar = keyword.charAt(p);
                if (alphabet.indexOf(nextChar) >= 0 && exclude.indexOf(nextChar) == -1 && !charsUsed.contains(nextChar)) {
                    sb.append(nextChar);
                    charsUsed.add(nextChar);
                }
            }

            // now we decide using the extend method
            char prevChar = alphabet.charAt(alphabet.length() - 1);
            switch (extend) {
                case EXTEND_FIRST: // already set up above
                    break;
                case EXTEND_MIN:
                    if (!charsUsed.isEmpty())
                        prevChar = charsUsed.first();
                    break;
                case EXTEND_MAX:
                    if (!charsUsed.isEmpty())
                        prevChar = charsUsed.last();
                    break;
                case EXTEND_LAST:
                    if (sb.length() != 0)
                        prevChar = sb.charAt(sb.length() - 1);
                    break;
            }

            // now scan the alphabet for remaining chars
            // used double to save having to go back to the start (% length)
            String doubleAlphabet = alphabet + alphabet;
            int pos = alphabet.indexOf(prevChar) + 1;
            while (pos < doubleAlphabet.length()) {
                char nextChar = doubleAlphabet.charAt(pos);
                if (!charsUsed.contains(nextChar) && exclude.indexOf(nextChar) == -1) {
                    sb.append(nextChar);
                    charsUsed.add(nextChar);
                }
                pos++;
            }
            return sb.toString();
        }
    }

    // return null if parameters can be set, else return the reason why not
    // all ciphers need alphabet and padding chars
    public String canParametersBeSet(Directives directives) {
        String alphabet = directives.getAlphabet();
        if (alphabet == null || alphabet.length() < 2)
            return "Alphabet is empty or too short";
        if (directives.getPaddingChars() == null)
            return "Set of padding chars is missing";
        if (directives.getLanguage() == null)
            return "Missing language";
        return null;
    }

    // return empty string if unsuccessful
    abstract public String encode(String plainText, Directives directives);

    // return empty string if unsuccessful
    abstract public String decode(String cipherText, Directives directives);

    // return empty string if unsuccessful
    abstract public CrackResult crack(String cipherText, Directives directives, int crackId);

    abstract public String getCipherDescription();
    abstract public String getInstanceDescription();

    abstract public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet);
    abstract public void fetchExtraControls(LinearLayout layout, Directives directives);

    /**
     * Add the specific controls for this cipher to the screen, e.g. keyword, shift, etc
     * @param context the activity we're in
     * @param layout the wrapper layout we're to add to
     * @param layoutResource the resource id of the layout for this cipher we want to add
     */
    void addExtraControls(AppCompatActivity context, LinearLayout layout, int layoutResource) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutResource, layout);
        // layout.addView(extraLayout); ... not needed as inflate() does this for us
    }

    /**
     * Default: There are no crack controls for this cipher, can be overridden
     * @param context the context
     * @param layout the layout to add any crack controls to
     * @param cipherText the text to be decoded - used by some ciphers to prepopulate some fields
     * @param language the expected language of the plain text
     * @param alphabet the current alphabet
     * @param paddingChars the currently configured padding chars
     * @return true if controls should be shown, else false
     */
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String cipherText,
                                    Language language, String alphabet, String paddingChars) {
        return false;
    }

    /**
     * There were no crack controls, nothing to add to the directives, can be overridden
     * @param layout the layout that could contains some crack controls
     * @param dirs the directives to add to
     */
    public CrackMethod fetchCrackControls(LinearLayout layout, Directives dirs) {
        return CrackMethod.BRUTE_FORCE;
    }

    /**
     * Inform the name of the cipher, set in constructor
     * @return the name, e.g. Caesar, Atbash, Vigenere, etc.
     */
    public String getCipherName() {
        return cipherName;
    }

    /**
     * Each method that can do hill climb should override this
     * @param text the text whose fitness is to be checked
     * @param dirs any directives the fitness check requires
     * @return a value representing the fitness of the text, bigger is better
     */
    public double getFitness(String text, Directives dirs) {
        throw new UnsupportedOperationException("getFitness not defined for "+this.getClass().getCanonicalName());
    }

    /**
     * Indicate whether the text (ignoring case) contains all the provided cribs
     * @param text the plain text to be checked
     * @param cribs the set of upper-case cribs, e.g. [ "THE", "AND", "HAVE" ]
     * @return true if ALL the cribs are in the text, false otherwise
     */
    public static boolean containsAllCribs(String text, Set<String> cribs) {
        // may use 5-char blocks so we have to remove the whitespace (cr/tab/space)
        String textUpper = text.toUpperCase().replaceAll("\\s", "");
        return normalisedTextHasCribs(textUpper, cribs);
    }

    private static boolean normalisedTextHasCribs(String normalisedText, Set<String> cribs) {
        for (String crib : cribs) {
            if (!normalisedText.contains(crib)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a set of strings representing cribs, in upper case
     * @param cribs the comma-separated list of cribs, e.g. "the,and,have", could have whitespace
     * @return a set of strings in upper case, e.g. [ "THE", "AND", "HAVE" ]
     */
    public static Set<String> getCribSet(String cribs) {
        List<String> listOfCribs = Arrays.asList(cribs.toUpperCase().replaceAll(" ", "").split(","));
        return new HashSet<>(listOfCribs);
    }

    /**
     * Static method to convert a permutation or matrix array to a string
     * @param numbers set if integers, e.g. [1,2,0,3]
     * @return the equivalent matrix as a comma-separated string, e.g. "1,2,0,3"
     */
    @NotNull
    static String numbersToString(int[] numbers) {
        if (numbers == null)
            return "n/a";
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < numbers.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(numbers[i]);
        }
        //return Arrays.asList(matrix).stream().map(n -> n.toString()).collect(Collectors.joining(","));
        return sb.toString();
    }

    /**
     * if the Greatest Common Factor of a and b is not 1 then they're not coprime (relatively prime)
     * in this case more than one plain letter will map to the same cipher letter and we'll not be able to decode
     * Used by Affine and Hill ciphers
     * @param a first int to be checked
     * @param b second int to be checked
     * @return true if the only common divisor of a and b are 1, else false
     */
    static boolean areCoPrimes(int a, int b) {
        if (a == 0) // this doesn't give good Affine mappings, all map to same char
            return false;
        BigInteger gcf = BigInteger.valueOf(a).gcd(BigInteger.valueOf(b));
        return gcf.equals(BigInteger.ONE);
    }

    /**
     * Given a keyword or sequence of integers, check and return set of integers
     * e.g. "BETA" => [3,0,1,2] or "3,2,1,0" => [3,2,1,0]
     * For an alphabetic keyword, remove duplicate letters and then scan them in order, building the
     * integer sequence of ordered distinct columns the keyword represents, e.g. BETA => 3,0,1,2
     * @param entry the text to convert to columns permutation, e.g. ZEBRA => 4,2,1,3,0
     * @param min the min value of the column, can be 0 or 1
     * @return the column permutation represented by the keyword or integers, or null of problem found
     */
    static int[] convertKeywordToColumns(String entry, int min) {
        if (entry == null)
            return null;
        int[] columns;
        if (entry.contains(",")) {
            String[] entries = entry.split(",");
            columns = new int[entries.length];
            int i = 0;
            Set<Integer> integersInKey = new HashSet<>();
            for(String element : entries) {
                try {
                    Integer intValue = Integer.valueOf(element);
                    // check not repeated
                    if (integersInKey.contains(intValue))
                        return null;
                    // check there will be no gaps
                    if (intValue < min || intValue >= entries.length+min)
                        return null;
                    integersInKey.add(intValue);
                    columns[i++] = intValue;
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
        } else {
            // example BETA => 3,0,1,2, ZEBRA => 4,2,1,3,0
            // collect all distinct letters into a Set that we can later navigate
            entry = entry.toUpperCase();
            NavigableSet<Character> charsInKey = new TreeSet<>();
            for (int i = 0; i < entry.length(); i++) {
                char keyChar = entry.charAt(i);
                if (!Character.isAlphabetic(keyChar)) // not a letter
                    return null;
                charsInKey.add(keyChar);
            }
            // means there are repeated letters in the keyword, or keyword is empty
            if (charsInKey.size() == 0 || charsInKey.size() != entry.length())
                return null;

            // now build the resulting list of columns by traversing the tree in alphabetical order
            columns = new int[charsInKey.size()];
            int i = 0;
            for (Character keyChar : charsInKey) {
                int posInEntry = entry.indexOf(keyChar)+min;
                columns[i++] = posInEntry;
            }
        }
        return columns;
    }

    /**
     * Looking at the radio buttons, determine the method of keyword extension to be used
     * @param v the view containing the buttons
     * @return the keyword extension type to use, based on the buttons
     */
    static KeywordExtend getKeywordExtend(View v) {
        KeywordExtend extend = KeywordExtend.EXTEND_MIN;
        RadioGroup r = v.findViewById(R.id.extra_extend_button_group);
        int id = r.getCheckedRadioButtonId();
        switch (id) {
            case R.id.extra_extend_button_first:
                extend = KeywordExtend.EXTEND_FIRST;
                break;
            case R.id.extra_extend_button_min:
                extend = KeywordExtend.EXTEND_MIN;
                break;
            case R.id.extra_extend_button_max:
                extend = KeywordExtend.EXTEND_MAX;
                break;
            case R.id.extra_extend_button_last:
                extend = KeywordExtend.EXTEND_LAST;
                break;
            case R.id.extra_extend_button_none:
                extend = KeywordExtend.EXTEND_NONE;
                break;
        }
        return extend;
    }

    /**
     * Look at the keyword and extend method on screen and apply the extension
     * with the current alphabet to produce a new FULL keyword
     * This is to keep it updated as the user changes them
     * @param rootView the view being adjusted
     */
    static void adjustFullKeyword(View rootView) {
        EditText keywordView = rootView.findViewById(R.id.extra_keyword);
        EditText replaceView = rootView.findViewById(R.id.extra_replace);
        KeywordExtend keywordExtend = getKeywordExtend(rootView);
        String alphabet = Settings.instance().getString(rootView.getContext(),R.string.pref_alphabet_plain);
        String fullKeyword;
        if (replaceView == null) {
            fullKeyword = applyKeywordExtend(keywordExtend, keywordView.getText().toString(), alphabet);
        } else {
            // if there is a 'REPLACE' entry text then use this to exclude certain letters from the generated keyword, like J
            StringBuilder exclude = new StringBuilder();
            String replace = replaceView.getText().toString();
            for(int pos=0; pos < replace.length(); pos+=2)
                exclude.append(replace.charAt(pos));
            fullKeyword = applyKeywordExtend(keywordExtend, keywordView.getText().toString(), alphabet, exclude.toString());
        }

        TextView fullKeywordView = rootView.findViewById(R.id.extra_full_keyword);
        fullKeywordView.setText(fullKeyword);
    }

    /**
     * Count how many letters of real words in the dictionary this text contains
     * @param text the text whose fitness is to be checked
     * @param dirs any directives the fitness check requires
     * @return the number of letters of dictionary words found in the text, larger is more fit
     */
    static double getWordCountFitness(String text, Directives dirs) {
        int lettersFound = 0;
        text = text.toUpperCase().replaceAll("\\s","");
        Dictionary dict = dirs.getLanguage().getDictionary();
        for (String word : dict) {
            if (word.length() > 1) {
                int pos = 0;
                do {
                    pos = text.indexOf(word, pos);
                    if (pos >= 0) {
                        pos += word.length();  // skip past this word to look further
                        lettersFound += word.length(); // fitter because we found the word
                    }
                } while (pos >= 0);
            }
        }
        return (double)lettersFound;
    }

    /**
     * Add some extra filters to an Edit Text field, can be Caps, Max Length and/or custom
     * @param layout the layout containing the field
     * @param fieldId the field ID
     * @param useCaps whether the field should be all Caps
     * @param maxLength whether the filed should have a max length
     * @param extraFilter any custom InputFilter
     */
    static void addInputFilters(LinearLayout layout, int fieldId, boolean useCaps, int maxLength,
                                InputFilter ... extraFilter) {
        int filterCount = 0;
        if (useCaps) filterCount++;
        if (maxLength > 0) filterCount++;
        if (extraFilter != null) filterCount += extraFilter.length;
        if (filterCount == 0) return;

        // find the field and get current filters
        EditText field = layout.findViewById(fieldId);
        InputFilter[] editFilters = field.getFilters();

        // set up array to hold the new filters
        InputFilter[] newFilters = new InputFilter[editFilters.length + filterCount];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        int next = editFilters.length;
        if (useCaps)
            newFilters[next++] = new InputFilter.AllCaps();   // ensures capitals
        if (maxLength > 0)
            newFilters[next++] = new InputFilter.LengthFilter(maxLength); // sets max length
        if (extraFilter != null) {
            for (InputFilter filter : extraFilter) {
                newFilters[next++] = filter;
            }
        }
        field.setFilters(newFilters);
    }
}
