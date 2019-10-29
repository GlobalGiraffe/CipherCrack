package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;

abstract public class Cipher {

    // used to locate programmatically added controls on the screen
    static final int ID_CAESAR_SPINNER = 9001;

    static final int ID_AFFINE_A_SPINNER = 9010;
    static final int ID_AFFINE_B_SPINNER = 9011;

    static final int ID_VIGENERE_KEYWORD = 9020;
    static final int ID_VIGENERE_LENGTH = 9021;

    static final int ID_SUBSTITUTION_KEYWORD = 9030;
    static final int ID_SUBSTITUTION_FULL_KEYWORD = 9031;
    static final int ID_BUTTON_FIRST = 9032;
    static final int ID_BUTTON_MIN = 9033;
    static final int ID_BUTTON_MAX = 9034;
    static final int ID_BUTTON_LAST = 9035;

    static final int ID_BUTTON_DICTIONARY = 9040;
    static final int ID_BUTTON_WORD_COUNT = 9041;
    static final int ID_BUTTON_IOC = 9042;
    static final int ID_BUTTON_BRUTE_FORCE = 9043;

    static final int ID_RAILFENCE_SPINNER = 9050;

    static final int ID_PERMUTATION_KEYWORD = 9060;
    static final int ID_PERMUTATION_READ_ACROSS = 9061;

    static final LinearLayout.LayoutParams WRAP_CONTENT_BOTH = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);

    static final LinearLayout.LayoutParams MATCH_PARENT_W_WRAP_CONTENT_H = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);

    Cipher(Context context) { }

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

    abstract public void layoutExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet);
    abstract public void fetchExtraControls(LinearLayout layout, Directives directives);

    /**
     * Returns default crack method (BRUTE_FORCE), should be overridden by ciphers that use other methods
     * @param layout layout that may have indication of crack type
     * @return the default crack method (BRUTE_FORCE), but can be overridden in cipher classes
     */
    public CrackMethod getCrackMethod(LinearLayout layout) {
        return CrackMethod.BRUTE_FORCE;
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
