package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;

/**
 * Class that contains methods to assist with Railfence Cipher operations
 * This transposition cipher places letters on rails in a zig-zag fashion
 *   and then reads one rail at a time
 */
public class Railfence extends Cipher {

    static final int MAX_RAILS = 20; // need some limit to avoid going forever

    private int rails = -1;

    Railfence(Context context) { super(context, "Railfence"); }

    /**
     * Describe what this cipher does
     * @return a description for this cipher, split across lines and paragraphs
     */
    @Override
    public String getCipherDescription() {
        return "The Railfence cipher is a transposition cipher where letters are written out in a zig-zag across multiple railfences and then cipher text read along the rails. " +
                "For example, if we have 3 rails then 'THISISASECRETMESSAGE' is encoded as:\n" +
                "T   I   E   T   S\n" +
                " H S S S C E M S A E\n" +
                "  I   A   R   E   G\n" +
                "To decipher, the number of rails is known and the above is reconstructed from the cipher text.\n\n"+
                "This can be broken by trying with a number of different rails and looking for cribs - there are only a limited number of rails possible.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher (rails="+rails+")";
    }

    /**
     * Determine whether the directives are valid for this cipher type, and sets if they are
     * @param dirs the directives to be checked and set
     * @return return the reason for being invalid, or null if the directives ARE valid (and set)
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            int rails = dirs.getRails();
            if (rails < 2 || rails > MAX_RAILS)
                return "Number of rails: " + rails + " must be between 2 and " + MAX_RAILS;
            this.rails = rails;
        } else {
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
        }
        return null;
    }


    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_railfence);

        // create an array of possible rails for the user to choose from: 2 to 20
        Integer[] railArray = new Integer[MAX_RAILS-2];
        for (int i = 0; i < MAX_RAILS-2; i++) {
            railArray[i] = i+2;
        }

        // Create an ArrayAdapter and default layout for the spinner
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, railArray);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Create a spinner and apply the adapter to it
        Spinner spinner = layout.findViewById(R.id.extra_railfence_spinner);
        spinner.setAdapter(adapter);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        Spinner spinner = layout.findViewById(R.id.extra_railfence_spinner);
        int rails = (int) spinner.getSelectedItem();
        dirs.setRails(rails);
    }

    /**
     * Encode a text using Railfence cipher with the given number of rails
     * @param plainText the text to be encoded
     * @param dirs a group of directives, we need RAILS (int) only
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        int rails = dirs.getRails();

        // allocate a number of string builders
        StringBuilder[] railText = new StringBuilder[rails];
        int railMaxLength = 2 * plainText.length()/rails + 2;
        for (int rail=0; rail < rails; rail++) {
            railText[rail] = new StringBuilder(railMaxLength);
        }
        // go through the plain text, placing each letter at the next spot on the right rail
        int currentRail = 0; // start on the top rail
        int railDirection = -1; // assume we've just finished moving up and now need to go down
        for (int i=0; i < plainText.length(); i++) {
            char plainLetter = plainText.charAt(i);
            railText[currentRail].append(plainLetter);
            // change direction if on the top or bottom rail - zig-zag
            if (currentRail==0 || currentRail == rails-1) {
                railDirection = -railDirection;
            }
            currentRail += railDirection;
        }
        // now we have built the rails, append them one-by-one to give the result
        StringBuilder result = new StringBuilder(plainText.length());
        for (StringBuilder text : railText) {
            result.append(text);
        }
        return result.toString();
    }

    /**
     * Decode a cipher text that was encoded with Railfence and the given number of rails
     * @param cipherText the text to be decoded
     * @param dirs a group of directives, we need RAILS (int)
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        int railCount = dirs.getRails();
        int period = (railCount * 2) - 2;
        int extra = cipherText.length() % period; // how many rails need to be a bit longer
        int[] lengthRail = new int[railCount];
        // basic length (floored) - later we may make some rails 1 char longer, based on extra
        int baseRailLength = cipherText.length() / period;
        // DFNTEATALEEDHESWL
        // D F N T E A T A L
        //  E E D H E S W L

        // work out base length of first and last rail
        lengthRail[0] = lengthRail[railCount-1] = baseRailLength;
        // work out base lengths of the middle rails - twice as big
        for (int railItem=1; railItem < railCount-1; railItem++) {
            lengthRail[railItem] = 2*baseRailLength;
        }
        // increment the lengths of some rails of text is not multiple of rails
        // first down
        for (int railItem=0; railItem < railCount && extra != 0; railItem++) {
            lengthRail[railItem]++;
            extra--;
        }
        // and then back up, if necessary
        for (int railItem=railCount-2; railItem > 0 && extra != 0; railItem--) {
            lengthRail[railItem]++;
            extra--;
        }

        // now start to build up the text on the rails
        int[] railUsed = new int[railCount];
        String[] railText = new String[railCount];
        for (int railItem=0, pos=0; railItem < railCount; pos += lengthRail[railItem], railItem++) {
            railText[railItem] = cipherText.substring(pos, pos+lengthRail[railItem]);
            railUsed[railItem] = 0; // zeroing as well need this in the next loop
        }

        // now read the zig-zags to get the plain text
        StringBuilder result = new StringBuilder(cipherText.length());
        int direction = -1;
        for (int railItem=0, pos=0; pos < cipherText.length(); pos++) {
            result.append(railText[railItem].charAt(railUsed[railItem]));
            railUsed[railItem]++;
            if (railItem == 0 || railItem == railCount-1) {
                direction = -direction;
            }
            railItem += direction;
        }
        return result.toString();
    }

    /**
     * Crack a Railfence cipher by checking all rails and looking for cribs
     * @param cipherText the text to try to crack
     * @param dirs the directives with alphabet and cribs
     * @return the result of the crack attempt
     */
    public CrackResult crack(String cipherText, Directives dirs) {
        String cribString = dirs.getCribs();
        Set<String> cribSet = Cipher.getCribSet(cribString);
        int maxRails = Math.min(MAX_RAILS, cipherText.length()/2);
        for (int currentRail=2; currentRail < maxRails; currentRail++) {
            dirs.setRails(currentRail);
            String plainText = decode(cipherText, dirs);
            if (Cipher.containsAllCribs(plainText, cribSet)) {
                String explain = "Success: Brute force approach: tried possible rails from 2 to "
                        + maxRails
                        + " looking for the cribs ["
                        + cribString
                        + "] in the decoded text and found them with "
                        + currentRail
                        + " rails.";
                return new CrackResult(dirs, cipherText, plainText, explain);
            }
        }
        String explain = "Fail: Brute force approach: tried possible rails from 2 to "
                + maxRails
                + " looking for the cribs ["
                + cribString
                + "] in the decoded text but did not find them.";
        return new CrackResult(cipherText, explain);
    }
}
