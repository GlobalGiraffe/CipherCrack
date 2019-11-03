package mnh.game.ciphercrack.language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeMap;

import mnh.game.ciphercrack.util.Settings;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toList;

public abstract class Language {

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
                    language = (Language) languageClass.newInstance();
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

    abstract protected String getDictionaryResourceId();
    abstract protected Map<String, Float> getLetterFrequencies();
    abstract protected Map<String, Float> getBigramFrequencies();
    abstract protected Map<String, Float> getTrigramFrequencies();
    abstract public double getExpectedIOC();

    // some languages with other letters may override this
    public String getAlphabet() {
        return Settings.DEFAULT_ALPHABET;
    }

    public String getName() {
        return name;
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
        LinkedList<Character> orderedChars = new LinkedList<>();
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
        /** Java 8 Streams do not run on SDK 23 (needs 24)
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
            String dictResourceId = getDictionaryResourceId();
            if (Dictionary.NONE.equals(dictResourceId)) {
                return;
            }
            InputStream is = null;
            ClassLoader cl = getClass().getClassLoader();
            if (cl != null) {
                is = cl.getResourceAsStream(dictResourceId);
                if (is == null) {
                    is = cl.getResourceAsStream("raw/"+dictResourceId);
                }
            }
            if (is == null) {
                is = getClass().getResourceAsStream(dictResourceId);
            }
            if (is == null) {
                is = getClass().getResourceAsStream("raw/"+dictResourceId);
            }
            if (is == null) {
                return; // could not locate the dictionary files
            }
            Dictionary dict = new Dictionary();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    dict.add(line.trim().toUpperCase());
                }
            } catch (IOException ex) {
                return;
            }
            dictionary = dict;
        }
    }

}
