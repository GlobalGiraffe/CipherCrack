package mnh.game.ciphercrack.util;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import mnh.game.ciphercrack.transform.RemovePadding;
import mnh.game.ciphercrack.transform.RemovePunctuation;
import mnh.game.ciphercrack.transform.Transform;

public class StaticAnalysis {

    // used during bigram and trigram analysis
    private static final Transform removePadding = new RemovePadding();
    private static final Transform removePunctuation = new RemovePunctuation();

    /**
     * Compute the frequency of letters in a string
     * @param text the text to be analysed
     * @param useUpper if true then letters are converted to upper case before counting, else
     *                 lower and upper case are treated as distinct
     * @param alphabet the alphabet to use, we only count letters in the alphabet
     * @return a map of the count for each character
     */
    public static HashMap<Character, Integer> collectFrequency(String text, boolean useUpper, String alphabet) {
        HashMap<Character, Integer> frequency = new HashMap<>(alphabet.length());
        if (text != null) {
            String textToUse = useUpper ? text.toUpperCase() : text;

            // scan the symbols in the text to collect frequency
            for (int i = 0; i < text.length(); i++) {

                // check the letter is in the alphabet, if not, we don't count frequency (punctuation)
                char letter = textToUse.charAt(i);
                if (alphabet.indexOf(Character.toUpperCase(letter)) >= 0) {
                    // we want to see how many times the letter occurs regardless of case
                    Integer count = frequency.get(letter);
                    if (count == null) {
                        frequency.put(letter, 1);
                    } else {
                        frequency.put(letter, count + 1);
                    }
                }
            }
        }
        return frequency;
    }

    /**
     * Compute the frequency of bi-grams or tri-grams
     * @param text the text to be analysed
     * @param gramSize the number of chars in a gram (2 or 3)
     * @param context the context in which we're running, used to determine punctuation
     * @return a map of the count for each gram
     */
    public static HashMap<String, Integer> collectGramFrequency(String text, int gramSize, Context context) {
        HashMap<String, Integer> frequency = new HashMap<>(500);
        if (text != null) {

            // just keep the alpha/numeric characters, make upper case
            String textToUse = removePunctuation.apply(context, removePadding.apply(context, text.toUpperCase()));

            // scan the symbols in the text to collect frequency of grams
            for (int i = 0; i <= textToUse.length()-gramSize; i++) {

                // get the bi- or tri-gram
                String gram = textToUse.substring(i, i+gramSize);

                // we want to see how many times the letter occurs regardless of case
                Integer count = frequency.get(gram);
                if (count == null) {
                    frequency.put(gram, 1);
                } else {
                    frequency.put(gram, count + 1);
                }
            }
        }
        return frequency;
    }

    /**
     * Return the frequency of all letters in the alphabet
     * @param text the text whose letters should be analysed
     * @param useUpper use upper case letters only
     * @param alphabet the alphabet in use
     * @return the map of characters to counts, for ALL letters in the alphabet
     */
    public static HashMap<Character, Integer> collectFrequencyAllInAlphabet(String text, boolean useUpper, String alphabet) {
        HashMap<Character, Integer> freq = collectFrequency(text, useUpper, alphabet);
        for (int pos=0; pos < alphabet.length(); pos++) {
            freq.putIfAbsent(alphabet.charAt(pos), 0);
        }
        return freq;
    }

    /**
     * Count the number of symbols in the text that are part of the alphabet
     * @param text the text to be scanned for letters
     * @param alphabet the valid upper case letters in the alphabet
     * @return the number of upper/lower case letters in the text that are in the alphabet
     */
    public static int countAlphabetic(String text, String alphabet) {
        int count = 0;
        if (text != null) {
            String textToUse = text.toUpperCase();

            // scan the symbols in the text to collect number in the alphabet
            for (int i = 0; i < text.length(); i++) {

                // check the letter is in the alphabet, if not, we don't count it (e.g. punctuation)
                char letter = textToUse.charAt(i);
                if (alphabet.indexOf(Character.toUpperCase(letter)) >= 0) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Count the number of symbols in the text that are not padding characters
     * @param text the text to be scanned for letters
     * @param paddingChars the characters to treat as padding
     * @return the number of non-padding symbols in the text
     */
    public static int countNonPadding(String text, String paddingChars) {
        int count = 0;
        if (text != null) {
            // scan the symbols in the text to collect number that are non-padding
            for (int i = 0; i < text.length(); i++) {

                // check the letter is padding, if it is then we don't count it
                char ch = text.charAt(i);
                if (paddingChars.indexOf(ch) < 0) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Calculate IOC from the hash map of counts
     * @param text the text whose IOC is to be calculated
     * @param alphabet the possible letters in the alphabet
     * @return the index of coincidence
     */
    public static double getIOC(String text, String alphabet) {
        Map<Character, Integer> frequency = collectFrequency(text, true, alphabet);
        int alphabeticCount = countAlphabetic(text, alphabet);
        return getIOC(frequency, alphabeticCount, alphabet);
    }

    /**
     * Calculate Index of Coincidence from the hash map of counts
     * @param frequency the map of character counts
     * @param alphabet the possible letters in the alphabet
     * @return the index of coincidence
     */
    public static double getIOC(Map<Character, Integer> frequency, int textLen, String alphabet) {
        if (textLen == 0) {
            return 0.0;
        } else {
            int calc = 0;
            for (int i = 0; i < alphabet.length(); i++) {
                Integer freq = frequency.get(alphabet.charAt(i));
                if (freq != null) {
                    calc += freq * (freq - 1);
                }
            }
            return ((double) calc) / (textLen * (textLen - 1));
        }
    }

    /**
     * Calculate Index of Coincidence from the hash map of counts
     * @param text the text whose IOC is to be calculated
     * @param alphabet the possible letters in the alphabet
     * @return the index of coincidence
     */
    public static double getIOC(String text, int textLen, String alphabet) {
        HashMap<Character, Integer> freqUpper = StaticAnalysis.collectFrequency(text, true, alphabet);
        return getIOC(freqUpper, textLen, alphabet);
    }
}
