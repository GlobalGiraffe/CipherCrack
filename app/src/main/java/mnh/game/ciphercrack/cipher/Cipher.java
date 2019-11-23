package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.KeywordExtend;

/**
 * Base class for all ciphers
 *
 *  This is parcelable because we need to pass the Cipher to a Service to do a crack
 *    and some cracks (e.g. Vigenere IOC) need specific values, like keywordLength
 */
abstract public class Cipher implements Parcelable {

    // used to locate programmatically added controls on the screen
    static final int ID_VIGENERE_LENGTH = 9021;

    static final int ID_HEADING_LENGTH = 9031;

    static final int ID_BUTTON_DICTIONARY = 9040;
    static final int ID_BUTTON_WORD_COUNT = 9041;
    static final int ID_BUTTON_IOC = 9042;
    static final int ID_BUTTON_BRUTE_FORCE = 9043;

    static final LinearLayout.LayoutParams WRAP_CONTENT_BOTH = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);

    static final LinearLayout.LayoutParams MATCH_PARENT_W_WRAP_CONTENT_H = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);

    // some ciphers remove padding, etc - need to know what padding is there, what alpha is, etc
    final Context context;
    private final String cipherName;

    // build a cipher from a parcel
    @Nullable
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
                // TODO cipher = new Hill(this);
                break;
            case "Bifid":
                // TODO cipher = new Bifid(this);
                break;
            case "Playfair":
                // TODO cipher = new Playfair(this);
                break;
            default:
                Toast.makeText(context, "Unknown cipher: "+name, Toast.LENGTH_LONG).show();
                break;
        }
        return cipher;
    }

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
     * @return the resulting keyword with all alphabet letters
     */
    static String applyKeywordExtend(KeywordExtend extend, String keyword, String alphabet) {
        StringBuilder sb = new StringBuilder(alphabet.length());

        // First, we start to form the full keyword by taking each unique letter of the keyword
        SortedSet<Character> charsUsed = new TreeSet<>();
        for (int p =0; p < keyword.length(); p++) {
            char nextChar = keyword.charAt(p);
            if (!charsUsed.contains(nextChar)) {
                sb.append(nextChar);
                charsUsed.add(nextChar);
            }
        }

        // now we decide using the extend method
        char prevChar = alphabet.charAt(alphabet.length()-1);
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
                    prevChar = sb.charAt(sb.length()-1);
                break;
        }

        // now scan the alphabet for remaining chars
        // used double to save having to go back to the start (% length)
        String doubleAlphabet = alphabet+alphabet;
        int pos = alphabet.indexOf(prevChar)+1;
        while (sb.length() < alphabet.length()) {
            char nextChar = doubleAlphabet.charAt(pos);
            if (!charsUsed.contains(nextChar)) {
                sb.append(nextChar);
                charsUsed.add(nextChar);
            }
            pos++;
        }
        return sb.toString();
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
     * @param alphabet the current alphabet
     * @return true if controls should be shown, else false
     */
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
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
        for (String crib : cribs) {
            if (!textUpper.contains(crib)) {
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
}
