package mnh.game.ciphercrack.util;

import android.util.Log;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import mnh.game.ciphercrack.cipher.Caesar;
import mnh.game.ciphercrack.cipher.Cipher;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.services.CrackResults;

/**
 * Tools used to crack ciphers using various kinds of climb or anealing, measuring fitness
 */
public class Climb {

    private static final String TAG = "CrackClimb";

    // used to detect most likely key once IOC hill climb has completed
    // look for the key with least number of these letters
    private static final String INFREQUENT_LETTERS = "ZQXJK";

    // inputs
    public static final String CLIMB_ALPHABET = "climb-alphabet";
    public static final String CLIMB_LANGUAGE = "climb-language";
    public static final String CLIMB_PADDING_CHARS = "climb-padding";
    public static final String CLIMB_CRIBS = "climb-cribs";
    public static final String CLIMB_START_KEYWORD = "climb-start-keyword";
    public static final String CLIMB_NUMBER_SIZE = "climb-number-size";
    public static final String CLIMB_TEMPERATURE = "climb-temperature";
    public static final String CLIMB_CYCLES = "climb-cycles";

    // outputs
    public static final String CLIMB_ACTIVITY = "climb-activity";
    public static final String CLIMB_BEST_KEYWORD = "climb-best-keyword";
    public static final String CLIMB_BEST_DECODE = "climb-best-decode";

    /**
     * Used for breaking Vigenere and Beaufort ciphers.
     * Rotate each letter in a candidate key of set length
     * checking the IOC of the decoded text and keeping the best,
     * moving from 0.041 towards 0.0667 or more
     * once the maximum IOC is reached perform parallel shift each position on the candidate key
     * to find if any of the 26 keys have all the cribs
     * @param cipherText the cipher text to be cracked
     * @param cipher the type of cipher - generally Vigenere or Beaufort
     * @param props properties used in the cipher: alphabet, seed keyword and cribs
     * @param crackId the identifier for the crack attempt we're doing, used to update progress
     * @return true if successful, and the properties will contain the best keyword,
     * decoded text and measure, with explanation of what has been done, otherwise false
     */
    public static boolean doClimb(String cipherText, Cipher cipher, Properties props, int crackId) {
        String alphabet = props.getProperty(Climb.CLIMB_ALPHABET);
        String startKey = props.getProperty(Climb.CLIMB_START_KEYWORD);
        String cribString = props.getProperty(Climb.CLIMB_CRIBS);
        String paddingChars = props.getProperty(Climb.CLIMB_PADDING_CHARS);
        Set<String> cribs = Cipher.getCribSet(cribString);
        Language language = Language.instanceOf(props.getProperty(Climb.CLIMB_LANGUAGE));
        String reverseCipherText = new StringBuilder(cipherText).reverse().toString();

        // nothing found yet
        props.remove(Climb.CLIMB_BEST_KEYWORD);
        props.remove(Climb.CLIMB_BEST_DECODE);

        char[] bestKey = new char[0];
        String bestDecode = "";

        // properties for doing the decode
        Directives dirs = new Directives();
        dirs.setAlphabet(alphabet);
        dirs.setLanguage(language);
        dirs.setPaddingChars(paddingChars);

        double bestMeasure = -1.0;
        boolean finished = false;
        int iteration = 0;
        StringBuilder activity = new StringBuilder("Perform a hill climb for ")
                .append(cipher.getCipherName())
                .append(" cipher with start key ")
                .append(startKey)
                .append(".\n");

        // count how many passes we do, for reporting progress
        int pass = 0;

        // keep going until we did a whole loop with no change being made
        String msg = "Starting first pass of hill climb";
        CrackResults.updateProgressDirectly(crackId, msg);
        Log.i(TAG, msg);
        while (!finished) {
            pass++;

            char[] dynamicKey = startKey.toCharArray();

            // mutate each letter in the key in turn
            for (int pos=0; pos < dynamicKey.length; pos++) {

                // try every possible char in this pos, one at a time
                for (int ordinal=0; ordinal < alphabet.length(); ordinal++) {
                    dynamicKey[pos] = alphabet.charAt(ordinal);

                    // decode and measure fitness
                    dirs.setKeyword(String.valueOf(dynamicKey));
                    String plain = cipher.decode(cipherText, dirs);
                    double measure = cipher.getFitness(plain, dirs);
                    if (measure > bestMeasure) {
                        activity.append("Key ")
                                .append(String.valueOf(dynamicKey))
                                .append(" improves measure to ")
                                .append(String.format(Locale.getDefault(), "%7.6f", measure))
                                .append(", text=")
                                .append(plain.substring(0,Math.min(plain.length(),50)))
                                .append(".\n");
                        bestKey = Arrays.copyOf(dynamicKey,dynamicKey.length);
                        bestDecode = plain;
                        bestMeasure = measure;
                    }
                    iteration++;
                }
                dynamicKey = Arrays.copyOf(bestKey, bestKey.length);
            }

            // see if anything changed after the above 2 loops, if not, we're done
            finished = startKey.equals(String.valueOf(dynamicKey));
            startKey = String.valueOf(bestKey); // prepare to go around again

            // drop out of the climb if we've been cancelled
            if (CrackResults.isCancelled(crackId)) {
                props.setProperty(Climb.CLIMB_BEST_KEYWORD, String.valueOf(bestKey));
                props.setProperty(Climb.CLIMB_BEST_DECODE, bestDecode);
                props.setProperty(Climb.CLIMB_ACTIVITY, activity.toString()+"Climb was cancelled.\n");
                return false;
            }

            // Can't tell when it'll be done, but show some progress
            msg = "Completed hill climb pass "+pass+", bestMeasure="+bestMeasure+", bestKey="+startKey;
            CrackResults.updateProgressDirectly(crackId, msg);
            Log.i(TAG,msg);
        }

        // report the best key/measure we found overall
        activity.append("Found best key ")
                .append(String.valueOf(bestKey))
                .append(" with best measure ")
                .append(String.format(Locale.getDefault(), "%7.6f", bestMeasure))
                .append(" after ")
                .append(iteration)
                .append(" iterations.\n");
        props.setProperty(Climb.CLIMB_BEST_KEYWORD, String.valueOf(bestKey));
        props.setProperty(Climb.CLIMB_BEST_DECODE, bestDecode);

        // now find the one with plain containing the cribs, if not use the one already found
        CrackResults.updateProgressDirectly(crackId, "Completed hill climb passes, best key: "+String.valueOf(bestKey)+", checking for cribs.");

        // this key is probably a cyclic-shift of the real one, check all for the cribs
        Directives checkCribsDirs = new Directives();
        checkCribsDirs.setAlphabet(alphabet);
        char[] candidateKey = new char[bestKey.length];
        // we also find the shift with least count of ZQXJK, in case cribs not found
        String leastInfrequentLetterKey = "";
        String leastInfrequentLetterDecode = "";
        int leastInfrequentLetterCount = Integer.MAX_VALUE;
        for (int shift=0; shift < alphabet.length(); shift++) {
            // create a key with from the base with this shift
            for (int keyPos=0; keyPos < bestKey.length; keyPos++) {
                candidateKey[keyPos] = Caesar.encodeChar(bestKey[keyPos], shift, alphabet);
            }

            // apply the decode and look for cribs
            checkCribsDirs.setKeyword(String.valueOf(candidateKey));
            String plain = cipher.decode(cipherText, checkCribsDirs);
            boolean foundCribs = Cipher.containsAllCribs(plain, cribs);
            if (foundCribs) {
                activity.append("Shifted letters equally and found all cribs using keyword ")
                        .append(String.valueOf(candidateKey))
                        .append(".\n");
                props.setProperty(Climb.CLIMB_BEST_KEYWORD, String.valueOf(candidateKey));
                props.setProperty(Climb.CLIMB_BEST_DECODE, plain);
                props.setProperty(Climb.CLIMB_ACTIVITY, activity.toString());
                return true;
            }
            // try also in reverse - only works if text length is multiple of key length
            String plainReverse = cipher.decode(reverseCipherText, checkCribsDirs);
            foundCribs = Cipher.containsAllCribs(plainReverse, cribs);
            if (foundCribs) {
                activity.append("Shifted letters equally and found all cribs in REVERSE text using keyword ")
                        .append(String.valueOf(candidateKey))
                        .append(".\n");
                props.setProperty(Climb.CLIMB_BEST_KEYWORD, String.valueOf(candidateKey));
                props.setProperty(Climb.CLIMB_BEST_DECODE, plainReverse);
                props.setProperty(Climb.CLIMB_ACTIVITY, activity.toString());
                return true;
            }

            // see if this plain text has the LEAST number of infrequent chars, which could help if cribs are wrong
            int infrequentLetterCount = 0;
            for (int i = 0; i < plain.length(); i++) {
                char ch = plain.charAt(i);
                if (INFREQUENT_LETTERS.indexOf(ch) >= 0) {
                    infrequentLetterCount++;
                }
            }
            if (infrequentLetterCount < leastInfrequentLetterCount) {
                leastInfrequentLetterCount = infrequentLetterCount;
                leastInfrequentLetterKey = String.valueOf(candidateKey);
                leastInfrequentLetterDecode = plain;
            }
        }

        activity.append("Shifted letters equally but could not find all cribs; keyword ")
                .append(leastInfrequentLetterKey)
                .append(" has the fewest ZQXJK letters, deciphering to ")
                .append(leastInfrequentLetterDecode.substring(0,Math.min(60, leastInfrequentLetterDecode.length()-1)))
                .append(".\n");
        props.setProperty(Climb.CLIMB_BEST_KEYWORD, leastInfrequentLetterKey);
        props.setProperty(Climb.CLIMB_BEST_DECODE, leastInfrequentLetterDecode);
        props.setProperty(Climb.CLIMB_ACTIVITY, activity.toString());
        return false;
    }

    /**
     * Swap around 'temp' letters in the startKey, returning a new key
     * @param startKey the initial key value
     * @param temp how many letters to swap around
     * @return the new key with the letters swapped around
     */
    public static String mutateKey(String startKey, int temp) {
        char[] newKey = startKey.toCharArray();
        int first, second;
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int maxRand = startKey.length();
        for (int i=0; i < temp; i++) {
            first = rand.nextInt(0, maxRand);
            do {
                second = rand.nextInt(0, maxRand);
            } while (first == second);
            char swap = newKey[first]; newKey[first] = newKey[second]; newKey[second] = swap;
        }
        return String.valueOf(newKey);
    }

    /**
     * Apply Simulated Anealing where we do a hill climb reducing the temperature as we go
     * @param text the text to be analysed and decoded
     * @param cipher the cipher being used (e.g. Keyword Substitution)
     * @param props the properties required for this climb
     * @param crackId the identifier for the crack attempt we're doing, used to update progress
     * @return true if all cribs were found in the best decode we could achieve
     */
    public static boolean doSimulatedAnealing(String text, Cipher cipher, Properties props, int crackId) {
        String alphabet = props.getProperty(Climb.CLIMB_ALPHABET);
        String startKey = props.getProperty(Climb.CLIMB_START_KEYWORD);
        String cribString = props.getProperty(Climb.CLIMB_CRIBS);
        String languageName = props.getProperty(Climb.CLIMB_LANGUAGE);
        int numberSize = props.getProperty(Climb.CLIMB_NUMBER_SIZE) == null
                ? 0 : Integer.parseInt(props.getProperty(Climb.CLIMB_NUMBER_SIZE));
        int startTemp = Integer.parseInt(props.getProperty(Climb.CLIMB_TEMPERATURE));
        int cycles = Integer.parseInt(props.getProperty(Climb.CLIMB_CYCLES));
        Set<String> cribs = Cipher.getCribSet(cribString);

        // nothing found yet
        props.remove(Climb.CLIMB_BEST_KEYWORD);
        props.remove(Climb.CLIMB_BEST_DECODE);

        // properties for doing the decode
        Directives decodeDirs = new Directives();
        decodeDirs.setAlphabet(alphabet);
        decodeDirs.setLanguage(Language.instanceOf(languageName));

        int iteration = 0;
        String msg = "Started simulated anealing (" + startTemp + "x" + cycles + ") for "+cipher.getCipherName() + " with start key " + startKey + ".\n";
        CrackResults.updateProgressDirectly(crackId, msg);
        StringBuilder activity = new StringBuilder(msg);

        // measure the start key's fitness - that's our starting point
        decodeDirs.setKeyword(startKey);
        decodeDirs.setNumberSize(numberSize);
        String bestKey = startKey;
        String bestDecode = cipher.decode(text, decodeDirs);
        double bestMeasure = cipher.getFitness(bestDecode, decodeDirs);

        // keep going until we did a whole loop with no change being made
        String dynamicKey = startKey;
        for (int temp = startTemp; temp > 0; temp--) {

            // this does thousands of checks, mutating key as we go
            for (int cycle = 0; cycle < cycles; cycle++) {

                if (cycle % 100 == 0) {
                    // drop out of the crack attempt if we've been cancelled
                    if (CrackResults.isCancelled(crackId)) {
                        props.setProperty(Climb.CLIMB_BEST_KEYWORD, String.valueOf(bestKey));
                        props.setProperty(Climb.CLIMB_BEST_DECODE, bestDecode);
                        props.setProperty(Climb.CLIMB_ACTIVITY, activity.toString()+"Simulated Anealing was cancelled.\n");
                        return false;
                    }

                    msg = "Annealing temperature=" + temp + ", cycle=" + cycle + " of " + cycles + ", best measure=" + bestMeasure + ", best key=" + bestKey;
                    CrackResults.updateProgressDirectly(crackId, msg);
                    Log.i(TAG, msg);
                }

                String trialKey = mutateKey(dynamicKey, temp);

                // decode and measure fitness
                decodeDirs.setKeyword(trialKey);
                String plain = cipher.decode(text, decodeDirs);
                double measure = cipher.getFitness(plain, decodeDirs);
                double probability = 0.0; //bestMeasure*(temp-1)*0.01;
                if (measure > bestMeasure
                        || (bestMeasure - measure) < probability) {
                    activity.append("Key ")
                            .append(trialKey)
                            .append(" improves measure to ")
                            .append(String.format(Locale.getDefault(), "%7.6f", measure))
                            .append(", text=")
                            .append(plain.substring(0, Math.min(plain.length(), 50)))
                            .append(".\n");
                    bestKey = trialKey;
                    dynamicKey = trialKey;
                    bestDecode = plain;
                    bestMeasure = measure;
                }
                iteration++;
            }
        }

        // report the best key/measure we found overall
        activity.append("Found best key ")
                .append(bestKey)
                .append(" with best measure ")
                .append(String.format(Locale.getDefault(), "%7.6f", bestMeasure))
                .append(" after ")
                .append(iteration)
                .append(" iterations.\n");
        Log.i(TAG,"Completed simulated anealing after "+iteration+" iterations, bestMeasure="+bestMeasure+", bestKey="+bestKey);

        // now find the one with plain containing the cribs, if not use the one already found
        props.setProperty(Climb.CLIMB_BEST_KEYWORD, bestKey);
        props.setProperty(Climb.CLIMB_BEST_DECODE, bestDecode);

        boolean foundCribs = Cipher.containsAllCribs(bestDecode, cribs);
        if (foundCribs) {
            activity.append("Decoded text contains all cribs.\n");
            props.setProperty(Climb.CLIMB_ACTIVITY, activity.toString());
            return true;
        }
        activity.append("Decoded text did not contain all cribs.\n");
        props.setProperty(Climb.CLIMB_ACTIVITY, activity.toString());
        return false;
    }
}
