package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;

abstract public class Cipher {

    // used to locate programmatically added controls on the screen
    static final int ID_VIGENERE_LENGTH = 9021;

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

    private final String cipherName;

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
            case "Keyword Substitution":
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
            case "Hill":
                // TODO cipher = new Hill(this);
                break;
            case "Bifid":
                // TODO cipher = new Bifid(this);
                break;
            case "Playfair":
                // TODO cipher = new Playfair(this);
                break;
        }
        return cipher;
    }

    Cipher(Context context, String name) { cipherName = name; }

    // return null if parameters can be set, else return the reason why not
    // for Encode and Decode the crackMethod will be None
    abstract public String canParametersBeSet(Directives directives);

    // return empty string if unsuccessful
    abstract public String encode(String plainText, Directives directives);

    // return empty string if unsuccessful
    abstract public String decode(String cipherText, Directives directives);

    // return empty string if unsuccessful
    abstract public CrackResult crack(String cipherText, Directives directives);

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
    protected void addExtraControls(AppCompatActivity context, LinearLayout layout, int layoutResource) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View extraLayout = inflater.inflate(layoutResource, layout);
        //layout.addView(extraLayout);
    }

    /**
     * Default: There are no crack controls for this cipher, can be overridden
     * @param context the context
     * @param layout the layout to add any crack controls to
     * @param alphabet the current alphabet
     * @return true if controls added, else false
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
        String textUpper = text.toUpperCase().replaceAll(" ", "");
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
