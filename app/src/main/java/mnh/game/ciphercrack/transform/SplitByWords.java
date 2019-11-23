package mnh.game.ciphercrack.transform;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.Settings;

/**
 * Split a string of plaintext by using words in the dictionary
 */
public class SplitByWords implements Transform {

    @Override
    public String apply(Context context, String text) {
        // which language and dictionary should we use? Context == null => unit test
        String languageName = (context == null)
                ? Settings.DEFAULT_LANGUAGE
                : Settings.instance().getString(context, R.string.pref_language);
        Language language = Language.instanceOf(languageName);
        return doTextSplit(text, language);
    }

    // used for unit testing where no context is available
    String apply(Language language, String text) {
        return doTextSplit(text, language);
    }

    // helper method to do the split, called from both apply methods
    private String doTextSplit(String text, Language language) {
        if (text == null)
            return null;
        // no dictionary, no splitting possible
        Dictionary dict = language.getDictionary();
        if (dict == null)
            return text;

        // fetch (or create) optimised list of dictionary words, hash-indexed by 2-letter prefixes
        // this means when we has a split point, we only search a hundred possible words starting
        // with the next 2 letters (candidates), rather than all (10K) words in the dictionary
        Map<String, List<String>> wordsWithPrefix = dict.getMapOf2LetterPrefixes();

        // find the likely words in the text and insert a space between them
        Set<Character> singleLetterWords = dict.getSingleLetterWords();

        text = text.toUpperCase();
        StringBuilder result = new StringBuilder(text.length()*2);
        List<String> attempts = new ArrayList<>(2);
        while (text.length() > 0) {
            char firstLetter = text.charAt(0);
            if (Character.isAlphabetic(firstLetter)) {

                // if prefix starts 'A' or 'I' (e.g. AS or IN) then we should
                //  try once with prefix AS/IN/etc and then again with just A/I
                String first2Letters = text.substring(0, Math.min(text.length(), 2));
                attempts.clear();
                attempts.add(first2Letters);
                if (dict.getSingleLetterWords().contains(firstLetter)) {
                    attempts.add(String.valueOf(firstLetter));
                }
                String largestWord = "";
                int largestWordLen = 0, largestWordPairLen = 0;
                for (String prefixAttempt : attempts) {
                    List<String> candidateWords = wordsWithPrefix.get(prefixAttempt);

                    // if some words have this prefix (not XJ, for example)
                    if (candidateWords != null) {
                        for (String word : candidateWords) {
                            if (text.startsWith(word)) {
                                int wordLen = word.length();
                                // this word matches to the end of the text, it will be the best
                                if (text.length() == wordLen || !Character.isAlphabetic(text.charAt(wordLen))) {
                                    largestWord = word;
                                    largestWordLen = wordLen;
                                    largestWordPairLen = wordLen;
                                } else {
                                    // check the next 2 letters after this word are a prefix in the map
                                    int nextPrefixLargestWordLen = 0;
                                    String nextPrefix = text.length() >= wordLen + 2 ? text.substring(wordLen, wordLen + 2) : "";
                                    List<String> nextCandidateWords = wordsWithPrefix.get(nextPrefix);
                                    if (nextCandidateWords != null) {
                                        for (String nextWord : nextCandidateWords) {
                                            int nextWordLen = nextWord.length();
                                            if (nextWordLen > nextPrefixLargestWordLen && text.substring(wordLen).startsWith(nextWord)) {
                                                nextPrefixLargestWordLen = nextWordLen;
                                            }
                                        }
                                    }
                                    // no next word, but could by single-letter word like I/A in English, or Y/O in Spanish
                                    if (nextPrefixLargestWordLen == 0
                                            && singleLetterWords.contains(firstLetter)) {
                                        nextPrefixLargestWordLen = 1;
                                    }
                                    // Better match if combined length exceeds current best combined length
                                    // If the combined length of this pair is *same* as best match so far
                                    // then we decide the best match is that one with longest first-word
                                    // except when current longest word has no second word
                                    // get TRADITIONALLY not TRADITION ALLY
                                    // but we want IN THE not INT HE !!!
                                    int combinedLen = wordLen + nextPrefixLargestWordLen;
                                    if (combinedLen > largestWordPairLen
                                            || (combinedLen == largestWordPairLen
                                             && wordLen < largestWordLen
                                             && largestWordLen != largestWordPairLen)) {
                                        largestWord = word;
                                        largestWordLen = wordLen;
                                        largestWordPairLen = combinedLen;
                                    }
                                }
                            }
                        }
                    }
                }

                // we've tried 1 or 2 prefix attempts, let's see what we found
                if (largestWordLen > 0) {
                    result.append(largestWord);
                    //System.out.println(largestWord + ": ");
                    text = text.substring(largestWordLen);
                } else { // no word found matching the letters at the current spot, output 1 letter
                    result.append(firstLetter);
                    //System.out.println(firstLetter + ": ");
                    text = text.substring(1);
                }
                // append a space if not end of text and next char is not punctuation
                if (text.length() > 0) {
                    if (Character.isAlphabetic(text.charAt(0)) || Character.isDigit(text.charAt(0)))
                        result.append(" ");
                }
            } else { // first letter is not alpha (digit or punctuation), so just append as-is
                result.append(firstLetter);
                //System.out.println(firstLetter + ": ");
                text = text.substring(1);

                // for some punctuation we add space after, for others (", %, start-braces) we don't
                if (text.length() > 0
                        && text.charAt(0) != ' '
                        && (firstLetter == '.'
                         || firstLetter == '!'
                         || firstLetter == '?'
                         || firstLetter == ')'
                         || firstLetter == ']'
                         || firstLetter == '}'
                         || firstLetter == ','
                         || firstLetter == ';'
                         || firstLetter == ':'
                         || Character.isDigit(firstLetter)))
                    result.append(" ");
            }
        }
        return result.toString();
    }
}
