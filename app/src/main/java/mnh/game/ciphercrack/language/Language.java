package mnh.game.ciphercrack.language;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mnh.game.ciphercrack.staticanalysis.FrequencyEntry;
import mnh.game.ciphercrack.util.Settings;

public abstract class Language {

    private static final String NO_DICTIONARY = ""; // indicates the language has no dictionary available

    // keep track of created Languages, we just want one of each so dictionary only loaded once
    private static final HashMap<String, Language> instances = new HashMap<>();

    // instance variables
    private Dictionary dictionary = null;
    private final String name;

    // don't instantiate this root class, but subclasses can call this
    Language(String name) {
        this.name = name;
    }

    /**
     * Return an instance of a language object, for letter frequencies and dictionaries
     * Reuse if already allocated - each language has one instance (one dictionary loaded)
     * @param name a name such as English or German
     * @return the language object that provides static details of the named language
     */
    public static synchronized Language instanceOf(String name) {
        Language language = instances.get(name);
        if (language == null) {
            try {
                Package packageName = Language.class.getPackage();
                if (packageName != null) {
                    Class languageClass = Class.forName(packageName.getName() + "." + name);
                    language = (Language)languageClass.newInstance();
                    instances.put(name, language);
                    language.loadDictionary();
                } else {
                    language = null;
                }
            } catch (NoClassDefFoundError | ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                if (!Settings.DEFAULT_LANGUAGE.equals(name)) { // avoid infinite loop
                    language = instanceOf(Settings.DEFAULT_LANGUAGE);
                } else {
                    throw new RuntimeException("Default Language could not be used", ex);
                }
            }
        }
        return language;
    }

    // the name of the dictionary raw resource, e.g. "english_dictionary"
    abstract public Map<String, Float> getLetterFrequencies();
    abstract public Map<String, Float> getBigramFrequencies();
    abstract public Map<String, Float> getTrigramFrequencies();
    abstract public double getExpectedIOC();
    abstract public String getInfrequentLetters();

    // some languages with other letters may override this
    public String getAlphabet() {
        return Settings.DEFAULT_ALPHABET;
    }

    public String getName() {
        return name;
    }

    // default is "no dictionary available", language subclasses will override
    String getDictionaryResourceName() { return Language.NO_DICTIONARY; }

    /**
     * Determine which is the most frequent gram in a list
     * @param grams the map of grams, e.g. from a Language
     * @return the entry that has the highest percentage
     */
    public static String mostFrequentGram(Map<String, Float> grams) {
        Float mostFrequentPercent = null;
        String mostFrequentGram = null;
        for (Map.Entry<String, Float> gram : grams.entrySet()) {
            if (mostFrequentPercent == null) {
                mostFrequentPercent = gram.getValue();
                mostFrequentGram = gram.getKey();
            } else {
                if (gram.getValue() > mostFrequentPercent) {
                    mostFrequentPercent = gram.getValue();
                    mostFrequentGram = gram.getKey();
                }
            }
        }
        return mostFrequentGram;
    }

    /**
     * Compute the sorted list of letters based on their frequency, low to high
     * @return the high-to-low list of letters based on their frequency
     * E and T may be first while Q or Z will probably be last
     */
    public List<Character> lettersOrderedByFrequency() {
        Map<String, Float> freqLetterStrings = getLetterFrequencies();
        Map<Character, Float> freqLetters = new HashMap<>();
        for (Map.Entry<String,Float> entry : freqLetterStrings.entrySet()) {
            freqLetters.put(entry.getKey().charAt(0), entry.getValue());
        }
        // now create ordered list of letters, sorted based on the float value, high-to-low
        List<Character> orderedChars = new LinkedList<>();
        for(Map.Entry<Character, Float> entry : freqLetters.entrySet()) {
            char thisChar = entry.getKey();
            float thisValue = entry.getValue();
            boolean added = false;
            // find position in the sorted list where this char will get added
            for (int pos=0; pos < orderedChars.size(); pos++) {
                if (thisValue > freqLetters.get(orderedChars.get(pos))) {
                    orderedChars.add(pos, thisChar);
                    added = true;
                    break;
                }
            }
            if (!added) {
                orderedChars.add(thisChar);
            }
        }
        /* Java 8 Streams do not run on SDK 23 (needs 24)
        List<Character> orderedChars = freqLetters
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .map(Map.Entry::getKey)
                .map(a -> a.charAt(0))
                .collect(toList());
         Collections.reverse(chars);
         **/
        return orderedChars;
    }

    /**
     * Given short string, return the usual frequency of that string in the language
     * @param gram the gram whose frequency is required
     * @return the string's frequency or 0.0 if not recorded in the map
     */
    public float frequencyOf(String gram) {
        Map<String,Float> frequencies;
        if (gram.length() == 3) {
            frequencies = getTrigramFrequencies();
        } else {
            if (gram.length() == 2) {
                frequencies = getBigramFrequencies();
            } else {
                frequencies = getLetterFrequencies();
            }
        }
        Float value = frequencies.get(gram.toUpperCase());
        if (value == null) {
            return 0.0f;
        }
        return value;
    }

    /**
     * Return the dictionary for this language, assumes already loaded
     * @return the dictionary or null if the language has none, or not yet loaded
     */
    public Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * Load the dictionary if necessary, it will be set to null if not found or can't be loaded
     */
    private void loadDictionary() {
        if (dictionary == null) {
            String dictResourceName = getDictionaryResourceName();
            if (Language.NO_DICTIONARY.equals(dictResourceName)) {
                return;
            }
            InputStream dictStream = null;
            ClassLoader cl = getClass().getClassLoader();
            if (cl != null) {
                dictStream = cl.getResourceAsStream(dictResourceName);
                if (dictStream == null) {
                    dictStream = cl.getResourceAsStream("raw/"+dictResourceName);
                }
            }
            if (dictStream == null) {
                dictStream = getClass().getResourceAsStream(dictResourceName);
            }
            if (dictStream == null) {
                dictStream = getClass().getResourceAsStream("raw/"+dictResourceName);
            }
            if (dictStream == null) {
                return; // could not locate the dictionary files
            }
            Dictionary dict = new Dictionary();
            if (dict.load(dictStream))
                dictionary = dict;
        }
    }
}
