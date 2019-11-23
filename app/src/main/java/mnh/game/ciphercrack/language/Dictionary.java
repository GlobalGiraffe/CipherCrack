package mnh.game.ciphercrack.language;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A dictionary is a hash set of upper-case trimmed words in a particular language
 */
public class Dictionary extends HashSet<String> {

    // reduce number of times we need to resize the hash map
    private static final int DEFAULT_INITIAL_SIZE = 3500;

    // used for those languages that have single-letter words, like 'A' and 'I' in English
    private Set<Character> singleLetterWords = null;

    // Optimises the splitting of words, see SplitByWords class
    // so whole dictionary is not scanned for each word to be split
    private Map<String, List<String>> mapOf2LetterPrefixes = null;

    // a dictionary will have thousands of entries, so ensure we ALWAYS specify size
    Dictionary() { super(DEFAULT_INITIAL_SIZE); }

    /**
     * Given an input stream to a resource file containing a word per line, read in the words
     * and add to the current dictionary, replacing the current contents
     * @param dictStream the stream of words, one per line
     * @return true if dictionary loaded successfully, false otherwise
     */
    boolean load(InputStream dictStream) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(dictStream))) {
            String line;
            singleLetterWords = new HashSet<>(3);
            while ((line = br.readLine()) != null) {
                String wordToBeAdded = line.trim().toUpperCase();
                if (wordToBeAdded.length() > 0) {
                    add(wordToBeAdded);
                    if (wordToBeAdded.length() == 1) {
                        singleLetterWords.add(wordToBeAdded.charAt(0));
                    }
                }
            }
        } catch (IOException ex) {
            Log.i("Dictionary", "Unable to load dictionary: "+ex.getMessage());
            this.clear();
            return false; // can't be loaded, keep it null
        }
        return true;
    }

    /**
     * Construct a map of 2-char prefixes to the list of words with that prefix.
     * This is used to quickly find and split words.
     * Short words like A and I will be in a map with key "A" or "I", no padding
     * Map is constructed on first call, subsequent ones re-use the same map
     * @return the map of prefixes to words
     */
    public synchronized Map<String,List<String>> getMapOf2LetterPrefixes() {
        if (mapOf2LetterPrefixes == null) {
            Map<String, List<String>> map = new HashMap<>(1000);
            for (String word : this) {
                // take first 2 chars of the word
                // words like 'a' and 'i' (spanish: 'y' and 'o') are used as-is
                String prefix = word.substring(0, Math.min(word.length(), 2));
                List<String> list = map.get(prefix);
                if (list == null) { // first time we've seen this prefix
                    list = new LinkedList<>();
                    map.put(prefix, list);
                }
                list.add(word);
            }
            mapOf2LetterPrefixes = map;
        }
        return mapOf2LetterPrefixes;
    }

    public Set<Character> getSingleLetterWords() {
        return singleLetterWords;
    }
}
