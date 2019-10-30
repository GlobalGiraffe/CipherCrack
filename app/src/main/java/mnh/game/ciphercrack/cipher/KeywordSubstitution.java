package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.Climb;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.KeywordExtend;
import mnh.game.ciphercrack.util.Settings;
import mnh.game.ciphercrack.util.StaticAnalysis;

public class KeywordSubstitution extends Cipher {

    /**
     * Look at the keyword and extend method on screen and apply the extension
     * with the current alphabet to produce a new FULL keyword
     * This is to keep it updated as the user changes them
     * @param rootView the view being adjusted
     */
    private static void adjustFullKeyword(View rootView) {
        EditText keywordView = rootView.findViewById(ID_SUBSTITUTION_KEYWORD);
        KeywordExtend keywordExtend = getKeywordExtend(rootView);
        String alphabet = Settings.instance().getString(rootView.getContext(),R.string.pref_alphabet_plain);
        String fullKeyword = applyKeywordExtend(keywordExtend, keywordView.getText().toString(), alphabet);

        TextView fullKeywordView = rootView.findViewById(ID_SUBSTITUTION_FULL_KEYWORD);
        fullKeywordView.setText(fullKeyword);
    }

    // when the radio button changes, adjust the full keyword to match new method
    private static final View.OnClickListener EXTEND_BUTTON_CLICK_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View rootView = v.getRootView();
            adjustFullKeyword(rootView);
        }
    };

    private String keyword = "";

    public KeywordSubstitution(Context context) { super(context); }

    /**
     * Describe what this cipher does
     * @return a description of this cipher
     */
    @Override
    public String getCipherDescription() {
        return "The Keyword substitution cipher is a monoalphabetic substitution cipher where each letter of the plain text maps to the same cipher letter, but not in any clear sequence. " +
                "To encode a message take the first plain letter, calculate its ordinal (0-25), add it to the ordinal of the first keyword letter taking the modulo 26 to give the encoded letter, repeating as necessary.\n"+
                "To decode a message, the inverse is performed: the ordinal of each cipher letter is taken in turn, the ordinal of the first key letter is subtracted modulo 26 to give the ordinal of the plain letter.\n"+
                "This cipher retains the frequency of the letters of the source alphabet, i.e. since E is most commong we look at the most common cipher letter and assume that has been mapped from E. With sufficient cipher text, following this process with some of the common letters and examining for cribs one can usually decode the message.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return "Keyword Substitution cipher (keyword="+keyword+")";
    }

    /**
     * Determine whether the directives are valid for this cipher type, and sets if they are
     * @param dirs the directives to be checked and set
     * @return return the reason for being invalid, or null if the directives ARE valid
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        String alphabet = dirs.getAlphabet();
        if (alphabet == null || alphabet.length() < 2)
            return "Alphabet is empty or too short";

        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            String keywordValue = dirs.getKeyword();
            if (keywordValue == null || keywordValue.length() < 2)
                return "Keyword is empty or too short";
            if (keywordValue.length() != alphabet.length())
                return "Keyword (" + keywordValue.length() + ") and alphabet (" + alphabet.length() + ") must have the same length";

            // letters in the keyword should only occur once and should be in the alphabet
            for (int i = 0; i < keywordValue.length(); i++) {
                char keyChar = keywordValue.charAt(i);
                int posInAlphabet = alphabet.indexOf(keyChar);
                if (posInAlphabet < 0)
                    return "Character " + keyChar + " at offset " + i + " in the keyword is not in the alphabet";
                if (i < keywordValue.length() - 1) {
                    if (keywordValue.indexOf(keyChar, i + 1) >= 0)
                        return "Character " + keyChar + " is present multiple times in the keyword";
                }
            }
            keyword = keywordValue;
        } else { // crack via dictionary or word-count => need language and cribs
            Language language = dirs.getLanguage();
            if (language == null)
                return "Language is missing";

            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";
        }
        return null;
    }

    /**
     * Looking at the radio buttons, determine the method of keyword extension to be used
     * @param v the view containing the buttons
     * @return the keyword extension type to use, based on the buttons
     */
    private static KeywordExtend getKeywordExtend(View v) {
        KeywordExtend extend;
        RadioButton r = v.findViewById(ID_BUTTON_FIRST);
        if (r.isChecked()) {
            extend = KeywordExtend.EXTEND_FIRST; // first
        } else {
            r = v.findViewById(ID_BUTTON_MIN);
            if (r.isChecked()) {
                extend = KeywordExtend.EXTEND_MIN; // min
            } else {
                r = v.findViewById(ID_BUTTON_MAX);
                if (r.isChecked()) {
                    extend = KeywordExtend.EXTEND_MAX; // max
                } else {
                    extend = KeywordExtend.EXTEND_LAST; // last
                }
            }
        }
        return extend;
    }

    /**
     * This takes the keyword extend method and a short keyword and then applies the method
     * to produce a full-sized keyword, same length as the alphabet
     * @param extend the method to use to extend the keyword, min, max or last
     * @param keyword the initial keyword
     * @return the resulting keyword with all alphabet letters
     */
     static String applyKeywordExtend(KeywordExtend extend, String keyword, String alphabet) {
        StringBuilder sb = new StringBuilder(alphabet.length());

        // First, we start to form the full keyword by taking each unique letter of the keyword
        SortedSet<Character> charsUsed = new TreeSet<>();
        for (int p =0; p < keyword.length(); p++) {
            char nextChar = keyword.charAt(p);
            if (!charsUsed.contains(nextChar)) {
                sb.append(nextChar);
                charsUsed.add(nextChar);
            }
        }

        // now we decide using the extend method
        char prevChar = alphabet.charAt(alphabet.length()-1);
        switch (extend) {
            case EXTEND_FIRST: // already set up above
                break;
            case EXTEND_MIN:
                if (!charsUsed.isEmpty())
                    prevChar = charsUsed.first();
                break;
            case EXTEND_MAX:
                if (!charsUsed.isEmpty())
                    prevChar = charsUsed.last();
                break;
            case EXTEND_LAST:
                if (sb.length() != 0)
                    prevChar = sb.charAt(sb.length()-1);
                break;
        }

        // now scan the alphabet for remaining chars
        // used double to save having to go back to the start (% length)
        String doubleAlphabet = alphabet+alphabet;
        int pos = alphabet.indexOf(prevChar)+1;
        while (sb.length() < alphabet.length()) {
            char nextChar = doubleAlphabet.charAt(pos);
            if (!charsUsed.contains(nextChar)) {
                sb.append(nextChar);
                charsUsed.add(nextChar);
            }
            pos++;
        }
        return sb.toString();
    }

    @Override
    public void layoutExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {

        // Create this:
        // Keyword:
        // [--------------------] EditText
        // Keyword Extend from
        // [o] Min Letter [o] Max letter [o] Final letter      RadioButtons
        // [----- RESULT -------] TextView
        //
        // The first can be 1-26, the second 0-26
        TextView keywordLabel = new TextView(context);
        keywordLabel.setText(context.getString(R.string.keyword));
        keywordLabel.setTextColor(ContextCompat.getColor(context, R.color.white));
        keywordLabel.setLayoutParams(WRAP_CONTENT_BOTH);

        // when the radio button changes, adjust the full keyword
        final TextWatcher TEXT_CHANGED_LISTENER = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                adjustFullKeyword(layout.getRootView());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        EditText keyword = new EditText(context);
        keyword.setText("");
        keyword.setTextColor(ContextCompat.getColor(context, R.color.entrytext_text));
        keyword.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);
        keyword.setId(ID_SUBSTITUTION_KEYWORD);
        keyword.setBackground(context.getDrawable(R.drawable.entrytext_border));
        keyword.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        keyword.addTextChangedListener(TEXT_CHANGED_LISTENER);
        // ensure input is in capitals
        InputFilter[] editFilters = keyword.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.AllCaps();   // ensures capitals
        keyword.setFilters(newFilters);

        TextView extendLabel = new TextView(context);
        extendLabel.setText(context.getString(R.string.extend_explain));
        extendLabel.setTextColor(ContextCompat.getColor(context, R.color.white));
        extendLabel.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);

        RadioButton firstButton = new RadioButton(context);
        firstButton.setId(ID_BUTTON_FIRST);
        firstButton.setText(context.getString(R.string.extend_first));
        firstButton.setChecked(false);
        firstButton.setTextColor(ContextCompat.getColor(context, R.color.white));
        firstButton.setLayoutParams(WRAP_CONTENT_BOTH);
        firstButton.setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);

        RadioButton minButton = new RadioButton(context);
        minButton.setId(ID_BUTTON_MIN);
        minButton.setText(context.getString(R.string.extend_min));
        minButton.setChecked(false);
        minButton.setTextColor(ContextCompat.getColor(context, R.color.white));
        minButton.setLayoutParams(WRAP_CONTENT_BOTH);
        minButton.setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);

        RadioButton maxButton = new RadioButton(context);
        maxButton.setId(ID_BUTTON_MAX);
        maxButton.setText(context.getString(R.string.extend_max));
        maxButton.setChecked(true);
        maxButton.setTextColor(ContextCompat.getColor(context, R.color.white));
        maxButton.setLayoutParams(WRAP_CONTENT_BOTH);
        maxButton.setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);

        RadioButton lastButton = new RadioButton(context);
        lastButton.setId(ID_BUTTON_LAST);
        lastButton.setText(context.getString(R.string.extend_last));
        lastButton.setChecked(false);
        lastButton.setTextColor(ContextCompat.getColor(context, R.color.white));
        lastButton.setLayoutParams(WRAP_CONTENT_BOTH);
        lastButton.setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);

        TextView fullKeywordLabel = new TextView(context);
        fullKeywordLabel.setText("[----------------------]");
        fullKeywordLabel.setTextColor(ContextCompat.getColor(context, R.color.white));
        fullKeywordLabel.setId(ID_SUBSTITUTION_FULL_KEYWORD);
        fullKeywordLabel.setLayoutParams(WRAP_CONTENT_BOTH);

        RadioGroup extendButtonGroup = new RadioGroup(context);
        extendButtonGroup.check(ID_BUTTON_MAX);
        extendButtonGroup.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);
        extendButtonGroup.setOrientation(LinearLayout.HORIZONTAL);
        extendButtonGroup.addView(firstButton);
        extendButtonGroup.addView(minButton);
        extendButtonGroup.addView(maxButton);
        extendButtonGroup.addView(lastButton);

        TextView crackLabel = new TextView(context);
        crackLabel.setText(context.getString(R.string.crack_method));
        crackLabel.setTextColor(ContextCompat.getColor(context, R.color.white));
        crackLabel.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);

        RadioButton dictButton = new RadioButton(context);
        dictButton.setId(ID_BUTTON_DICTIONARY);
        dictButton.setText(context.getString(R.string.crack_dictionary));
        dictButton.setChecked(true);
        dictButton.setTextColor(ContextCompat.getColor(context, R.color.white));
        dictButton.setLayoutParams(WRAP_CONTENT_BOTH);

        RadioButton wordCountButton = new RadioButton(context);
        wordCountButton.setId(ID_BUTTON_WORD_COUNT);
        wordCountButton.setText(context.getString(R.string.crack_word_count));
        wordCountButton.setChecked(false);
        wordCountButton.setTextColor(ContextCompat.getColor(context, R.color.white));
        wordCountButton.setLayoutParams(WRAP_CONTENT_BOTH);

        RadioGroup crackButtonGroup = new RadioGroup(context);
        crackButtonGroup.check(ID_BUTTON_DICTIONARY);
        crackButtonGroup.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);
        crackButtonGroup.setOrientation(LinearLayout.HORIZONTAL);
        crackButtonGroup.addView(dictButton);
        crackButtonGroup.addView(wordCountButton);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(MATCH_PARENT_W_WRAP_CONTENT_H);
        layout.addView(keywordLabel);
        layout.addView(keyword);
        layout.addView(extendLabel);
        layout.addView(extendButtonGroup);
        layout.addView(fullKeywordLabel);
        layout.addView(crackLabel);
        layout.addView(crackButtonGroup);
    }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        EditText keywordField = layout.findViewById(ID_SUBSTITUTION_KEYWORD);
        String keyword = keywordField.getText().toString();
        KeywordExtend keywordExtend = getKeywordExtend(layout);

        // build the extended keyword
        String alphabet = Settings.instance().getString(layout.getContext(), R.string.pref_alphabet_plain);
        String fullKeyword = KeywordSubstitution.applyKeywordExtend(keywordExtend, keyword, alphabet);
        dirs.setKeyword(fullKeyword);
        String languageName = Settings.instance().getString(layout.getContext(), R.string.pref_language);
        Language language = Language.instanceOf(languageName);
        dirs.setLanguage(language);
    }

    /**
     * Query the extra layout to find which crack method we're using
     * @param layout layout that may have indication of crack type
     * @return the crack method the user chose
     */
    @Override
    public CrackMethod getCrackMethod(LinearLayout layout) {
        // locate the kind of crack we've been asked to do
        RadioButton dictButton = layout.findViewById(ID_BUTTON_DICTIONARY);
        return (dictButton.isChecked()) ? CrackMethod.DICTIONARY : CrackMethod.WORD_COUNT;
    }

    /**
     * Since Keyed Substitution is symetrical if we swap the keyword and alphabet,
     * provide one routine to do this
     * @param text the text to be encoded / decoded
     * @param alphabet alphabet if encoding, keyword if decoding
     * @param keyword keyword if encoding, alphabet if decoding
     * @return the encoded or decoded text
     */
    private String doKeyedEncodeDecode(String text, String alphabet, String keyword) {
        String keywordUpper = keyword.toUpperCase();
        StringBuilder result = new StringBuilder(text.length());
        for (int i=0; i < text.length(); i++) {
            char plainChar = text.charAt(i);
            char plainCharUpper = Character.toUpperCase(plainChar);
            int plainOrdinal = alphabet.indexOf(plainCharUpper); // find this char's pos in alphabet
            if (plainOrdinal < 0) { // not in alphabet e.g. punctuation, just copy as-is
                result.append(plainChar);
            } else {
                char keyChar = keywordUpper.charAt(plainOrdinal); // find same pos in keyword
                result.append((plainChar == plainCharUpper) ? keyChar : Character.toLowerCase(keyChar));
            }
        }
        return result.toString();
    }

    /**
     * Encode a text using Keyword Substitution cipher with the given keyword
     * @param plainText the text to be encoded
     * @param dirs a group of directives that define how the cipher will work, especially KEYWORD
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        String alphabet = dirs.getAlphabet();
        String keyword = dirs.getKeyword();
        return doKeyedEncodeDecode(plainText, alphabet, keyword);
    }

    /**
     * Decode a text using Keyed Substitution cipher with the given keyword
     * @param cipherText the text to be decoded
     * @param dirs a group of directives that define how the cipher will work, especially KEYWORD
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        String alphabet = dirs.getAlphabet();
        String keyword = dirs.getKeyword();
        return doKeyedEncodeDecode(cipherText, keyword, alphabet);
    }

    /**
     * Find the keyword that most closely matches the letter frequency
     * @param cipherText the text to be decoded
     * @param alphabet the alphabet of the text
     * @param language the language currently in use
     * @return a suggested keyword which matches letter frequencies
     */
    String formFrequencySuggestedKeyword(String cipherText, String alphabet, Language language) {
        // we need the frequencies of letters in current language
        List<Character> freqOfLetters = language.lettersOrderedByFrequency();
        Map<Character, Integer> cipherFreq = StaticAnalysis.collectFrequencyAllInAlphabet(cipherText, true, alphabet);

        TreeMap<Character, Character> suggestedMap = new TreeMap<>();
        for (Character langLetter : freqOfLetters) {
            Character highestCipherLetter = null;
            Integer highestCipherFreq = -1;
            for (Map.Entry<Character, Integer> cipherFreqEntry : cipherFreq.entrySet()) {
                if (cipherFreqEntry.getValue() > highestCipherFreq) {
                    highestCipherFreq = cipherFreqEntry.getValue();
                    highestCipherLetter = cipherFreqEntry.getKey();
                }
            }
            suggestedMap.put(langLetter, highestCipherLetter);
            cipherFreq.remove(highestCipherLetter);
        }
        StringBuilder keyword = new StringBuilder(alphabet.length());
        for (Map.Entry<Character, Character> entry : suggestedMap.entrySet()) {
            keyword.append(entry.getValue());
        }
        return keyword.toString();
    }

    /**
     * Crack a substitution cipher using simulated anealing. Start with a keyword built based on
     * the frequency of cipher letters compared to the frequency of letters in the language. Then
     * loop around first with low temperature and then with higher for a few thousand iterations
     * each time mutating the key based on the temperature and measuring fitness by counting how
     * many dictionary word letters can be seen in the decoded text.
     * Slower than dictionary check but can find keyword even if not based on a dictionary work
     * or is from a compound word (like LEONARDO DA VINCI)
     * @param cipherText the text to be cracked
     * @param dirs directives we need: ALPHABET, CRIBS, LANGUAGE. On return the CrackResult will
     *              also include EXPLAIN and (if successfully cracked) DECODE_KEYWORD
     * @return the result of the crack attempt
     */
    private CrackResult crackWordCount(String cipherText, Directives dirs) {
        String alphabet = dirs.getAlphabet();
        String cribString = dirs.getCribs();
        Language language = dirs.getLanguage();

        Properties crackProps = new Properties();
        crackProps.setProperty(Climb.CLIMB_ALPHABET, alphabet);
        crackProps.setProperty(Climb.CLIMB_LANGUAGE, language.getName());
        crackProps.setProperty(Climb.CLIMB_CRIBS, cribString);

        String startKeyword = formFrequencySuggestedKeyword(cipherText, alphabet, language);
        crackProps.setProperty(Climb.CLIMB_START_KEYWORD, startKeyword);

        // do a short one (5000 iterations overall) to try to get close (ish)
        // short texts are best cracked with low temperature to start with:
        // < 800 => 1, 800 - 1599 => 2, 1600 - 2399 => 3, etc
        int startTemperature = cipherText.length()/600 + 1;
        crackProps.setProperty(Climb.CLIMB_TEMPERATURE, String.valueOf(startTemperature));
        crackProps.setProperty(Climb.CLIMB_CYCLES, String.valueOf(5000/startTemperature));

        // returns true if found all cribs, unlikely on first scan
        String firstActivity = "";
        boolean success = Climb.doSimulatedAnealing(cipherText, this, crackProps);
        if (!success) {
            // scan with higher temperature, should not wander too far
            firstActivity = crackProps.getProperty(Climb.CLIMB_ACTIVITY);
            crackProps.setProperty(Climb.CLIMB_START_KEYWORD, crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD));
            crackProps.setProperty(Climb.CLIMB_TEMPERATURE, String.valueOf(8));
            crackProps.setProperty(Climb.CLIMB_CYCLES, String.valueOf(1500));
            if (!Climb.doSimulatedAnealing(cipherText, this, crackProps)) {
                // even this did not work, give up
                String explain = "Fail: Searched for largest word match but did not find cribs ["
                        + cribString + "], best key was "
                        + crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD) + "\n"
                        + firstActivity
                        + crackProps.getProperty(Climb.CLIMB_ACTIVITY);
                return new CrackResult(cipherText, explain);
            }
        }
        // one or other of the simulated anealing above worked, report back
        dirs.setKeyword(crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD));
        String plainText = crackProps.getProperty(Climb.CLIMB_BEST_DECODE);
        String explain = "Success: Searched for largest word match and found all cribs ["
                + cribString + "] with key "
                + crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD) + "\n"
                + firstActivity
                + crackProps.getProperty(Climb.CLIMB_ACTIVITY);
        return new CrackResult(dirs, cipherText, plainText, explain);
    }

    /**
     * Crack a substitution cipher using all words in the dictionary as seeds for the keyword.
     * Does not work if the keyword was not formed from a word in the dictionary
     * @param cipherText the text to be cracked
     * @param dirs directives we need: ALPHABET, CRIBS, LANGUAGE. On return the result will
     *              also include EXPLAIN and (if successfully cracked) DECODE_KEYWORD
     * @return the result of the crack attempt
     */
    private CrackResult crackDictionary(String cipherText, Directives dirs) {
        String alphabet = dirs.getAlphabet();
        String cribString = dirs.getCribs();
        Language language = dirs.getLanguage();

        // work out our crib set just once
        Set<String> cribs = Cipher.getCribSet(cribString);
        StringBuilder explain = new StringBuilder(1000);
        String validKeyword = "";
        String validDecode = "";

        Directives crackDirs = new Directives();
        crackDirs.setAlphabet(alphabet);

        // look through all words (length > 1) to see which makes a keyword that decodes the text
        // and results in a plain text with all the cribs in it
        Dictionary dict = language.getDictionary();
        Set<String> triedKeywords = new HashSet<>(2000);
        for (String word : dict) {
            if (word.length() > 1) {
                word = word.toUpperCase();

                // could be a number of ways of extending a partial keyword
                for (KeywordExtend extend : KeywordExtend.values()) {
                    String keyword = applyKeywordExtend(extend, word, alphabet);
                    // don't try to decode the cipherText with this keyword if already tried
                    if (!triedKeywords.contains(keyword)) {
                        triedKeywords.add(keyword);
                        crackDirs.setKeyword(keyword);
                        String decoded = decode(cipherText, crackDirs);
                        if (Cipher.containsAllCribs(decoded, cribs)) {
                            explain.append("Using ")
                                    .append(word)
                                    .append(" gave keyword ")
                                    .append(keyword)
                                    .append(" which decoded to text=")
                                    .append(decoded.substring(0, 60))
                                    .append("\n");
                            validKeyword = keyword;
                            validDecode = decoded;
                        }
                        // now do same again with reverse text - could be backwards
                        String decodedReverse = new StringBuilder(decoded).reverse().toString();
                        if (Cipher.containsAllCribs(decodedReverse, cribs)) {
                            explain.append("Using ")
                                    .append(word)
                                    .append(" gave keyword ")
                                    .append(keyword)
                                    .append(" which decoded to REVERSE text=")
                                    .append(decodedReverse.substring(0, 60))
                                    .append("\n");
                            validKeyword = keyword;
                            validDecode = decodedReverse;
                        }
                    }
                }
            }
        }

        // let's see if we found anything
        if (explain.length() > 0) {
            dirs.setKeyword(validKeyword);
            String explainString = "Success: Searched using "
                    + dict.size()
                    + " dictionary words as keys and found all cribs ["
                    + cribString
                    + "]\n"
                    + explain.toString();
            return new CrackResult(dirs, cipherText, validDecode, explainString);
        }

        String explainString = "Fail: Searched using "
                + dict.size()
                + " dictionary words as keys but did not find cribs ["
                + cribString + "]\n";
        return new CrackResult(cipherText, explainString);
    }

    /**
     * Decode a text using Keyed Substitution cipher with the given keyword
     * @param cipherText the text to be decoded
     * @param dirs a group of directories that define how the cipher will work, especially KEYWORD
     * @return the result of the crack attempt
     */
    @Override
    public CrackResult crack(String cipherText, Directives dirs) {
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == CrackMethod.WORD_COUNT) {
            // crack by simulated anealing and measuring number of real word letters in the text
            return crackWordCount(cipherText, dirs);

        } else {
            // crack type via using dictionary entries as potential keywords
            return crackDictionary(cipherText, dirs);
        }
    }

    /**
     * Count how many letters of real words in the dictionary this text contains
     * @param text the text whose fitness is to be checked
     * @param dirs any directives the fitness check requires
     * @return the number of letters of dictionary words found in the text, larger is more fit
     */
    @Override
    public double getFitness(String text, Directives dirs) {
        int lettersFound = 0;
        text = text.toUpperCase();
        Dictionary dict = dirs.getLanguage().getDictionary();
        for (String word : dict) {
            if (word.length() > 1) {
                int pos = 0;
                do {
                    pos = text.indexOf(word, pos);
                    if (pos >= 0) {
                        pos += word.length();  // skip past this word to look further
                        lettersFound += word.length(); // fitter because we found the word
                    }
                } while (pos >= 0);
            }
        }
        return (double)lettersFound;
    }
}
