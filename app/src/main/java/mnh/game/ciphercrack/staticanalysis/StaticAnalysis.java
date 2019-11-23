package mnh.game.ciphercrack.staticanalysis;

import android.content.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.transform.RemoveNonAlphabetic;
import mnh.game.ciphercrack.transform.Transform;

public class StaticAnalysis {

    // how close to language IOC we have to be to be significant
    // used to indicate transposition/monoalphabetic ciphers
    static final double IOC_SIGNIFICANCE_PERCENTAGE = 0.84;

    // how may cycles to analyse
    private static final int MAX_IOC_CYCLES_TO_ANALYSE = 30;

    // used during bigram and trigram analysis
    private static final Transform removeAllButAlphabetic = new RemoveNonAlphabetic();

    /**
     * Compute the frequency of letters in a string
     * @param text the text to be analysed
     * @param includeNonAlpha if true then non-alphabetic (punctuation) characters are included in the map
     * @param useUpper if true then letters are converted to upper case before counting, else
     *                 lower and upper case are treated as distinct
     * @param alphabet the alphabet to use, we only count letters in the alphabet
     * @param paddingChars padding chars to exclude from the map, can be empty string to not exclude any
     * @return a map of the count for each character
     */
    public static Map<Character, Integer> collectFrequency(String text, boolean includeNonAlpha, boolean useUpper,
                                                           String alphabet, String paddingChars) {
        HashMap<Character, Integer> frequency = new HashMap<>(alphabet.length());
        if (text != null) {
            String textToUse = useUpper ? text.toUpperCase() : text;

            // scan the symbols in the text to collect frequency
            for (int i = 0; i < text.length(); i++) {

                // check the letter is in the alphabet, if not, we don't count frequency (punctuation)
                char letter = textToUse.charAt(i);

                // ignore padding chars -- to gather including padding we pass in empty string
                if (!paddingChars.contains(String.valueOf(letter))) {

                    // we decide here whether to include non-alpha and alphabetic based on what was asked for
                    if (includeNonAlpha || alphabet.indexOf(Character.toUpperCase(letter)) >= 0) {
                        // keep track of how many times the letter occurs
                        Integer count = frequency.get(letter);
                        if (count == null) {
                            frequency.put(letter, 1);
                        } else {
                            frequency.put(letter, count + 1);
                        }
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
    static HashMap<String, Integer> collectGramFrequency(String text, int gramSize, Context context) {
        HashMap<String, Integer> frequency = new HashMap<>(500);
        if (text != null) {

            // just keep the alpha/numeric characters, make upper case
            String textToUse = removeAllButAlphabetic.apply(context, text.toUpperCase());

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
     * @param paddingChars the characters to ignore as padding
     * @return the map of characters to counts, for ALL letters in the alphabet
     */
    public static Map<Character, Integer> collectFrequencyAllInAlphabet(String text, boolean useUpper, String alphabet, String paddingChars) {
        Map<Character, Integer> freq = collectFrequency(text, false, useUpper, alphabet, paddingChars);
        for (int pos=0; pos < alphabet.length(); pos++) {
            // Java8: simply use: freq.putIfAbsent(alphabet.charAt(pos), 0);
            char thisChar = alphabet.charAt(pos);
            if (!freq.containsKey(thisChar))
                freq.put(thisChar, 0);
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
     * @param paddingChars the characters to treat as padding
     * @return the index of coincidence
     */
    public static double calculateIOC(String text, String alphabet, String paddingChars) {
        Map<Character, Integer> frequency = collectFrequency(text, false, true, alphabet, paddingChars);
        int alphaCount = countAlphabetic(text, alphabet);
        return calculateIOC(frequency, alphaCount, alphabet);
    }

    /**
     * Calculate Index of Coincidence from the hash map of counts
     * @param frequency the map of character counts
     * @param alphaCount the count of significant alphabetic characters in the text
     * @param alphabet the possible letters in the alphabet
     * @return the index of coincidence
     */
    public static double calculateIOC(Map<Character, Integer> frequency, int alphaCount, String alphabet) {
        if (alphaCount == 0) {
            return 0.0;
        } else {
            int calc = 0;
            for (int i = 0; i < alphabet.length(); i++) {
                Integer freq = frequency.get(alphabet.charAt(i));
                if (freq != null) {
                    calc += freq * (freq - 1);
                }
            }
            return ((double) calc) / (alphaCount * (alphaCount - 1));
        }
    }

    /**
     * Examine cycles in the text to see if IOC for these cycles could indicate Vigenere keyword length
     * @param text the text to be analysed
     * @param context the context for getting padding settings
     * @param alphabet the list of valid letters in the alphabet
     * @param paddingChars the characters to treat as padding
     * @return the IOC for various cycles
     */
    public static double[] getCyclicIOC(String text, Context context, String alphabet, String paddingChars) {
        // we need to remove all chars (punctuation, spaces, etc) leaving chars in the alphabet
        String textJustAlpha = removeAllButAlphabetic.apply(context,text.toUpperCase());

        // these will hold the cyclic strings
        int maxCycles = Math.min(textJustAlpha.length(), MAX_IOC_CYCLES_TO_ANALYSE);
        StringBuilder[] cycleStrings = new StringBuilder[maxCycles];
        double[] cycleIOC = new double[maxCycles];
        for (int pos=0; pos < maxCycles; pos++) {
            cycleStrings[pos] = new StringBuilder();
            cycleIOC[pos] = 0.0;
        }

        // check all cycles from
        for(int cycleSize=1; cycleSize < maxCycles; cycleSize++) {
            // clear the cyclic strings
            for (int pos=0; pos < cycleSize; pos++) {
                cycleStrings[pos].setLength(0);
            }
            // form the string with this cycle size by scanning whole input text
            for (int pos=0; pos < textJustAlpha.length(); pos++) {
                cycleStrings[pos%cycleSize].append(textJustAlpha.charAt(pos));
            }
            double overallIOC = 0.0;
            for (int pos=0; pos < cycleSize; pos++) {
                overallIOC += calculateIOC(cycleStrings[pos].toString(), alphabet, paddingChars);
            }
            cycleIOC[cycleSize] = overallIOC / cycleSize;
        }
        return cycleIOC;
    }

    /**
     * Determine if the only non-whitespace chars in the text are numbers
     * @param text the text to be analysed
     * @return true if the only non-whitespace are digits 0-9
s     */
    public static boolean isAllNumeric(String text) {
        if (text == null)
            return false;
        for (int i=0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (!Character.isWhitespace(ch) && !Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculate factors of a number, including 1 and the number itself
     * @param num the number whose factors are needed
     * @return the array of factors
     */
    static int[] factorsOf(int num) {
        if (num <= 0)
            return new int[0];
        if (num == 1)
            return new int[] {1};
        int[] factors = new int[Math.abs(num)/2+1];
        int factCount = 0;
        for (int i = 1; i <= num / 2; i++) {
            if (num % i == 0) {
                factors[factCount++] = i;
            }
        }
        factors[factCount++] = num;
        return Arrays.copyOf(factors, factCount);
    }

    /**
     * Look at the text and suggest the cipher(s) that could be used to decode it
     * @param analysis something where analysis has been done and can make it available
     * @param language the language we think the plain text is in
     * @return suggestions for what the cipher could be
     */
    static String produceSuggestionsForCipher(AnalysisProvider analysis, Language language) {

        StringBuilder sb = new StringBuilder();

        Map<Character, Integer> freq = analysis.getFreqAlphaUpper();
        int distinctSymbols = freq.size();
        Set<Character> chars = freq.keySet();
        if (distinctSymbols == 2) {
            Character[] symbols = chars.toArray(new Character[2]);
            sb.append("The text has only 2 distinct symbols: ")
                    .append(symbols[0])
                    .append(" and ")
                    .append(symbols[1])
                    .append(" which would indicate Binary, Morse Code or Baconian ciphers.\n");
        } else {
            // if only digits then could
            if (distinctSymbols <= 10) {
                if (analysis.isAllNumeric()) {
                    sb.append("There are ")
                            .append(distinctSymbols)
                            .append(" distinct symbols and all numeric, which could indicate a book cipher.");
                } else {
                    if (distinctSymbols == 5) {
                        if (chars.contains('A') && chars.contains('D') && chars.contains('F')
                                && chars.contains('G') && chars.contains('X')) {
                            sb.append("The text has only A, D, F, G and X, which indicates an ADFGX cipher.\n");
                        } else {
                            sb.append("The text has only 5 distinct symbols ")
                                    .append("which would indicate a 5x5 Polybius Square.\n");
                        }
                    }
                    if (distinctSymbols == 6) {
                        if (chars.contains('A') && chars.contains('D') && chars.contains('F')
                                && chars.contains('G') && chars.contains('V') && chars.contains('X')) {
                            sb.append("The text has only A, D, F, G, V and X, which indicates an ADFGVX cipher.\n");
                        } else {
                            sb.append("The text has just 6 distinct symbols ")
                                    .append("which could indicate a 6x6 Polybius square.\n");
                        }
                    }
                }
            } else {
                if (distinctSymbols == 25) {
                    sb.append("There are 25 distinct letters, ")
                            .append("which could suggest a 5x5 square cipher such as ")
                            .append("Playfair, Foursquare or Bifid, with one letter omitted.\n");
                }
                if (distinctSymbols == 36) {
                    sb.append("There are 36 distinct symbols, ")
                            .append("which strongly suggests a 6x6 square cipher such as Playfair.\n");
                }
                // determine if transposition / mono-alphabetic substitution cipher vs. poly-alphabetic
                boolean couldBeTransposition = true;
                boolean couldBeMonoAlpha = true;
                boolean couldBePolyAlpha = true;
                double ioc = analysis.getIOC();
                sb.append("The text has Index of Coincidence (IOC) ")
                        .append(String.format(Locale.getDefault(), "%7.6f", ioc))
                        .append(", while the default for ")
                        .append(language.getName())
                        .append(" is ")
                        .append(String.format(Locale.getDefault(), "%7.6f. ", language.getExpectedIOC()));

                // Around : 0.067 * 0.905  - i.e. 90.5% of expected IOC
                if (ioc > language.getExpectedIOC() * StaticAnalysis.IOC_SIGNIFICANCE_PERCENTAGE) {
                    // similar to the IOC of the language => transposition or mono-alphabetic cipher
                    sb.append("This similarity indicates a mono-alphabetic or transposition cipher.\n");

                    // count Z, K, Q, X and J - if low, would suggest Transposition
                    // count E, T, A, O and N - if high, would suggest Transposition
                    Integer countQ = freq.get('Q');
                    Integer countZ = freq.get('Z');
                    Integer countJ = freq.get('J');
                    Integer countK = freq.get('K');
                    Integer countX = freq.get('X');
                    int lowFreqCounts =
                              (countJ != null ? countJ : 0)
                            + (countQ != null ? countQ : 0)
                            + (countK != null ? countK : 0)
                            + (countX != null ? countX : 0)
                            + (countZ != null ? countZ : 0);
                    double lowFreqPercent = lowFreqCounts / (double)analysis.getCountAlphabetic();
                    Integer countA = freq.get('A');
                    Integer countE = freq.get('E');
                    Integer countI = freq.get('I');
                    Integer countN = freq.get('N');
                    Integer countO = freq.get('O');
                    Integer countT = freq.get('T');
                    Integer countU = freq.get('U');
                    int highFreqCounts =
                            (countE != null ? countE : 0)
                                    + (countT != null ? countT : 0)
                                    + (countA != null ? countA : 0)
                                    + (countO != null ? countO : 0)
                                    + (countN != null ? countN : 0);
                    double highFreqPercent = highFreqCounts / (double)analysis.getCountAlphabetic();
                    sb.append("The percentage of ZQKXJ in the text is ")
                            .append(String.format(Locale.getDefault(), "%4.2f%%", lowFreqPercent*100.0))
                            .append(" and the percentage of ETAON in the text is ")
                            .append(String.format(Locale.getDefault(), "%4.2f%%", highFreqPercent*100.0));
                    int likelyTransposition = 0;
                    if (lowFreqPercent < 0.025 && highFreqPercent > 0.4) {  // 5% for low, 40% for high
                        sb.append(" which indicates a Transposition cipher, such as Railfence, Permutation, Route or Skytale.\n");
                        likelyTransposition++;
                    } else {
                        sb.append(" which is more evenly spread than a Transposition cipher would have.\n");
                        likelyTransposition--;
                    }

                    int vowelCounts =
                            (countA != null ? countA : 0)
                                    + (countE != null ? countE : 0)
                                    + (countI != null ? countI : 0)
                                    + (countO != null ? countO : 0)
                                    + (countU != null ? countU : 0);
                    int vowelPercent = (int)(100.0f * vowelCounts / (float)analysis.getCountAlphabetic());
                    sb.append("Vowels occupy ").append(vowelPercent).append("% of the text");
                    if (vowelPercent > 35 && vowelPercent < 50) {
                        sb.append(", which is high enough to make a Transposition cipher likely.\n");
                        likelyTransposition++;
                    } else {
                        if (vowelPercent >= 50) {
                            sb.append(", which is uncommonly high!\n");
                            likelyTransposition--;
                        } else {
                            sb.append(", which is lower than expected by a Transposition cipher.\n");
                            likelyTransposition--;
                        }
                    }

                    // see if the most frequent letter matches the language's frequent letter
                    char frequentChar = 'A';
                    int frequentCount = 0;
                    for (Map.Entry<Character,Integer> entry : freq.entrySet()) {
                        if (entry.getValue() > frequentCount) {
                            frequentChar = entry.getKey();
                            frequentCount = entry.getValue();
                        }
                    }

                    // see if the text frequent char matches the frequent char in language
                    List<Character> orderedLetters = language.lettersOrderedByFrequency();
                    if (frequentChar == orderedLetters.get(0)) {
                        sb.append("The most frequent letter is ")
                                .append(frequentChar)
                                .append(", matching the most frequent letter in ")
                                .append(language.getName())
                                .append(", which suggests a Transposition cipher.\n");
                        likelyTransposition++;
                    } else {
                        sb.append("The most frequent letter is ")
                                .append(frequentChar)
                                .append(", but in ")
                                .append(language.getName())
                                .append(" the most frequent letter is ")
                                .append(orderedLetters.get(0))
                                .append(", which suggests a substitution cipher.\n");
                        likelyTransposition--;
                    }

                    // TODO - check monogram frequency vs expected frequency
                    // i.e. check that many (over 75%) of the chars with freq>1% are withing 10% of target language
                    // if so then likely to be Transposition, some other checks also in Trello

                    if (likelyTransposition > 0) {
                        // TODO - can we tell which TYPE of Transposition cipher?
                        int[] factors = factorsOf(analysis.getCountAlphabetic());
                        sb.append("The text length is ")
                                .append(analysis.getCountAlphabetic())
                                .append(", which has ")
                                .append(factors.length)
                                .append(" factors: ");
                        for (int factor : factors) {
                            if (factor == analysis.getCountAlphabetic()) {
                                sb.append(" and ");
                            } else {
                                if (factor != 1) {
                                    sb.append(", ");
                                }
                            }
                            sb.append(factor);
                        }
                        sb.append(". ");
                        if (factors.length > 2) {
                            sb.append("This may suggest the Skytale cipher cycle size or column transposition keyword length.\n");
                        } else {
                            sb.append("This does not help with a Skytale cycle size or column transposition keyword length.\n");
                        }

                    } else { // not transposition

                        // find top and second most frequent letters in cipher text
                        Map.Entry<Character, Integer> top = null, second = null;
                        for(Map.Entry<Character,Integer> entry : freq.entrySet()) {
                            if (top == null || entry.getValue() > top.getValue()) {
                                second = top;
                                top = entry;
                            } else {
                                if (second == null) {
                                    second = entry;
                                }
                            }
                        }
                        char topChar = top.getKey();
                        char secondChar = second.getKey();
                        int cipherDiff = (secondChar - topChar) + ((secondChar > topChar) ? 0 : 26);

                        // now get difference between most frequent 2 letters in the language
                        List<Character> languageFreq = language.lettersOrderedByFrequency();
                        char topLang = languageFreq.get(0);
                        char secondLang = languageFreq.get(1);
                        int languageDiff = (secondLang - topLang) + ((secondLang > topLang) ? 0 : 26);

                        // see if the distance between letters matches the language
                        sb.append("The gap between most frequent letter (")
                                .append(topChar)
                                .append(") and second most frequent (")
                                .append(secondChar)
                                .append(") is ")
                                .append(cipherDiff);
                        if (cipherDiff == languageDiff) {
                            sb.append(", which matches the difference between the top letter in ")
                                    .append(language.getName())
                                    .append(" (")
                                    .append(topLang)
                                    .append(") and second letter (")
                                    .append(secondLang)
                                    .append("). This provides a high confidence of a Caesar, Atbash or ROT13 cipher.\n");
                        } else {
                            sb.append(", which differs from the gap between the top letter in ")
                                    .append(language.getName())
                                    .append(" (")
                                    .append(topLang)
                                    .append(") and second letter (")
                                    .append(secondLang)
                                    .append("). This provides a high confidence of Affine or Substitution cipher.\n");
                        }
                    }
                } else {
                    // low IOC so likely to be a poly-alphabetic cipher
                    sb.append("This difference indicates a poly-alphabetic cipher.\n");

                    // look for cyclic IOC that is close to the language norm
                    double[] cyclicIOC = analysis.getIOCCycles();
                    int firstSignificantCycle = 0, multiples = 0, noise = 0;
                    for (int cycle = 2; cycle < cyclicIOC.length; cycle++) {
                        if (cyclicIOC[cycle] > language.getExpectedIOC() * StaticAnalysis.IOC_SIGNIFICANCE_PERCENTAGE) {

                            // this is close, if first time, record it, later we'll check for multiples
                            if (firstSignificantCycle == 0) {
                                firstSignificantCycle = cycle;
                            } else {
                                if (cycle % firstSignificantCycle == 0) {
                                    multiples++;
                                } else {
                                    noise++;
                                }
                            }
                        }
                    }

                    if (firstSignificantCycle > 0 && multiples > 2 && noise < 2) {
                        sb.append("Checking for periodic IOC peaks shows likely period of ")
                                .append(firstSignificantCycle)
                                .append(" which indicates a keyword poly-alphabetic cipher such as ")
                                .append("Vigenere, Beaufort, Portia or Gronsfeld with that keyword length.\n");
                    } else {
                        sb.append("Checking for periodic IOC peaks shows no significant pattern ")
                                .append("which excludes Vigenere, Beaufort, Portia or Gronsfeld ciphers, ")
                                .append(" but could indicate a Playfair, Hill, Autokey, Running Key or one-time pad cipher.\n");
                        if (analysis.getCountAlphabetic() % 2 != 0) {
                            sb.append("The text contains an odd number (")
                                    .append(analysis.getCountAlphabetic())
                                    .append(") of alphabetic letters which rules out bi-graphic ciphers ")
                                    .append("such as 2x2 Hill, Playfair and Foursquare ciphers.\n");
                        } else {
                            sb.append("The text contains an even number (")
                                    .append(analysis.getCountAlphabetic())
                                    .append(") of alphabetic letters ")
                                    .append("which could indicate 2x2 Hill, Playfair or Foursquare ciphers.\n");
                        }
                    }

                    if (analysis.getCountAlphabetic()%2 != 0) {
                        sb.append("There are an odd (")
                                .append(analysis.getCountAlphabetic())
                                .append(") number of symbols, which rules out Polybius based ciphers (Playfair, two- and four-square).\n");
                    }
                }
            }
        }
        return sb.toString();
    }

}
