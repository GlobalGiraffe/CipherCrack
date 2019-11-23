package mnh.game.ciphercrack.cipher;

import android.content.Context;
import android.os.Parcel;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.TreeMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.util.Climb;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.KeywordExtend;
import mnh.game.ciphercrack.util.Settings;
import mnh.game.ciphercrack.staticanalysis.StaticAnalysis;

public class KeywordSubstitution extends Cipher {

    // when the radio button changes, adjust the full keyword to match new method
    private static final View.OnClickListener EXTEND_BUTTON_CLICK_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View rootView = v.getRootView();
            adjustFullKeyword(rootView);
        }
    };

    private static final View.OnClickListener KEYWORD_ON_CLICK_DELETE = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText k = v.getRootView().findViewById(R.id.extra_substitution_keyword);
            k.setText("");
        }
    };

    private String keyword = "";

    KeywordSubstitution(Context context) { super(context, "Substitution"); }

    // used to send a cipher to a service
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(keyword);
    }
    @Override
    public void unpack(Parcel in) {
        super.unpack(in);
        keyword = in.readString();
    }

    /**
     * Describe what this cipher does
     * @return a description of this cipher
     */
    @Override
    public String getCipherDescription() {
        return "The Keyword Substitution cipher is a monoalphabetic substitution cipher where each letter of the plain text maps to the same cipher letter, but not in any clear sequence. " +
                "To encode a message take the first plain letter, calculate its ordinal (0-25), add it to the ordinal of the first keyword letter taking the modulo 26 to give the encoded letter, repeating as necessary.\n"+
                "To decode a message, the inverse is performed: the ordinal of each cipher letter is taken in turn, the ordinal of the first key letter is subtracted modulo 26 to give the ordinal of the plain letter.\n"+
                "This cipher retains the frequency of the letters of the source alphabet, i.e. since E is most common we look at the most common cipher letter and assume that has been mapped from E. With sufficient cipher text, following this process with some of the common letters and examining for cribs one can usually decode the message.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher ("+(keyword==null?"n/a":keyword)+")";
    }

    /**
     * Look at the keyword and extend method on screen and apply the extension
     * with the current alphabet to produce a new FULL keyword
     * This is to keep it updated as the user changes them
     * @param rootView the view being adjusted
     */
    private static void adjustFullKeyword(View rootView) {
        EditText keywordView = rootView.findViewById(R.id.extra_substitution_keyword);
        KeywordExtend keywordExtend = getKeywordExtend(rootView);
        String alphabet = Settings.instance().getString(rootView.getContext(),R.string.pref_alphabet_plain);
        String fullKeyword = applyKeywordExtend(keywordExtend, keywordView.getText().toString(), alphabet);

        TextView fullKeywordView = rootView.findViewById(R.id.extra_substitution_full_keyword);
        fullKeywordView.setText(fullKeyword);
    }

    /**
     * Determine whether the directives are valid for this cipher type, and sets if they are
     * @param dirs the directives to be checked and set
     * @return return the reason for being invalid, or null if the directives ARE valid
     */
    @Override
    public String canParametersBeSet(Directives dirs) {
        String reason = super.canParametersBeSet(dirs);
        if (reason != null)
            return reason;
        String keywordValue = dirs.getKeyword();
        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == null || crackMethod == CrackMethod.NONE) {
            if (keywordValue == null || keywordValue.length() < 2)
                return "Keyword is empty or too short";
            String alphabet = dirs.getAlphabet();
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
        } else { // crack via dictionary or word-count => need language and cribs
            // this the the only crack possible
            String cribs = dirs.getCribs();
            if (cribs == null || cribs.length() == 0)
                return "Some cribs must be provided";

            if (crackMethod != CrackMethod.WORD_COUNT && crackMethod != CrackMethod.DICTIONARY)
                return "Invalid crack method";
            Language lang = dirs.getLanguage();
            if (lang == null)
                return "Language must be provided";
            if (lang.getDictionary() == null)
                return "No "+lang.getName()+" dictionary is defined";
        }
        keyword = keywordValue;
        return null;
    }

    /**
     * Looking at the radio buttons, determine the method of keyword extension to be used
     * @param v the view containing the buttons
     * @return the keyword extension type to use, based on the buttons
     */
    private static KeywordExtend getKeywordExtend(View v) {
        KeywordExtend extend;
        RadioButton r = v.findViewById(R.id.extra_substitution_button_first);
        if (r.isChecked()) {
            extend = KeywordExtend.EXTEND_FIRST; // first
        } else {
            r = v.findViewById(R.id.extra_substitution_button_min);
            if (r.isChecked()) {
                extend = KeywordExtend.EXTEND_MIN; // min
            } else {
                r = v.findViewById(R.id.extra_substitution_button_max);
                if (r.isChecked()) {
                    extend = KeywordExtend.EXTEND_MAX; // max
                } else {
                    extend = KeywordExtend.EXTEND_LAST; // last
                }
            }
        }
        return extend;
    }

    @Override
    public void addExtraControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
        // this extracts the layout from the XML resource
        super.addExtraControls(context, layout, R.layout.extra_substitution);

        // Create this:
        // Keyword:
        // [----------------------] EditText
        // Keyword Extend from
        // [o] Min Letter [o] Max letter [o] Final letter      RadioButtons
        // [---EXTENDED KEYWORD---] TextView
        //
        // The first can be 1-26, the second 0-26

        // when the radio button or keyword text changes, adjust the full-keyword
        final TextWatcher TEXT_CHANGED_LISTENER = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { adjustFullKeyword(layout.getRootView()); }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        EditText keywordEditText = layout.findViewById(R.id.extra_substitution_keyword);
        keywordEditText.addTextChangedListener(TEXT_CHANGED_LISTENER);
        // ensure input is in capitals, not too long, and only letters in alphabet
        InputFilter[] editFilters = keywordEditText.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 3];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        // ensures capitals
        newFilters[editFilters.length] = new InputFilter.AllCaps();
        // limits text length to size of alphabet
        newFilters[editFilters.length+1] = new InputFilter.LengthFilter(alphabet.length());
        // ensures we type only what's in the alphabet
        newFilters[editFilters.length+2] = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                // only allow chars in the alphabet to be added
                for (int i = start; i < end; i++) {
                    String letter = String.valueOf(source.charAt(i));
                    if (!alphabet.contains(letter)) {
                        return "";
                    }
                }
                return null;
            }
        };
        keywordEditText.setFilters(newFilters);

        // ensure we 'delete' the keyword text when the delete button is pressed
        Button keywordDelete = layout.findViewById(R.id.extra_substitution_keyword_delete);
        keywordDelete.setOnClickListener(KEYWORD_ON_CLICK_DELETE);

        // when the radio buttons clicked, recalculate the full key
        layout.findViewById(R.id.extra_substitution_button_first).setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);
        layout.findViewById(R.id.extra_substitution_button_min).setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);
        layout.findViewById(R.id.extra_substitution_button_max).setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);
        layout.findViewById(R.id.extra_substitution_button_last).setOnClickListener(EXTEND_BUTTON_CLICK_LISTENER);
        adjustFullKeyword(layout);
     }

    @Override
    public void fetchExtraControls(LinearLayout layout, Directives dirs) {
        TextView keywordField = layout.findViewById(R.id.extra_substitution_full_keyword);
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
     * Add crack controls for this cipher: type of crack to be done
     * @param context the context
     * @param layout the layout to add any crack controls to
     * @param alphabet the current alphabet
     * @return true if controls added, else false
     */
    @Override
    public boolean addCrackControls(AppCompatActivity context, LinearLayout layout, String alphabet) {
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
        layout.addView(crackLabel);
        layout.addView(crackButtonGroup);

        return true;
    }

    /**
     * Fetch the details of the extra crack controls for this cipher
     * @param layout the layout that could contains some crack controls
     * @param dirs the directives to add to
     * @return the crack method to be used
     */
    @Override
    public CrackMethod fetchCrackControls(LinearLayout layout, Directives dirs) {
        // locate the kind of crack we've been asked to do
        RadioButton dictButton = layout.findViewById(ID_BUTTON_DICTIONARY);
        return (dictButton.isChecked()) ? CrackMethod.DICTIONARY : CrackMethod.WORD_COUNT;
    }

    /**
     * Since Keyed Substitution is symmetrical if we swap the keyword and alphabet, we just
     *   provide one routine to do this
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
     * @param paddingChars the characters to ignore as padding
     * @return a suggested keyword which matches letter frequencies
     */
    String formFrequencySuggestedKeyword(String cipherText, String alphabet, Language language, String paddingChars) {
        // we need the frequencies of letters in current language
        List<Character> freqOfLetters = language.lettersOrderedByFrequency();
        Map<Character, Integer> cipherFreq = StaticAnalysis.collectFrequencyAllInAlphabet(cipherText, true, alphabet, paddingChars);

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
    private CrackResult crackWordCount(String cipherText, Directives dirs, int crackId) {
        String alphabet = dirs.getAlphabet();
        String cribString = dirs.getCribs();
        String paddingChars = dirs.getPaddingChars();
        Language language = dirs.getLanguage();
        CrackMethod crackMethod = dirs.getCrackMethod();

        Properties crackProps = new Properties();
        crackProps.setProperty(Climb.CLIMB_ALPHABET, alphabet);
        crackProps.setProperty(Climb.CLIMB_LANGUAGE, language.getName());
        crackProps.setProperty(Climb.CLIMB_CRIBS, cribString);

        String startKeyword = formFrequencySuggestedKeyword(cipherText, alphabet, language, paddingChars);
        crackProps.setProperty(Climb.CLIMB_START_KEYWORD, startKeyword);

        // do a short one (5000 iterations overall) to try to get close (ish)
        // short texts are best cracked with low temperature to start with:
        // < 800 => 1, 800 - 1599 => 2, 1600 - 2399 => 3, etc
        int startTemperature = cipherText.length()/600 + 1;
        crackProps.setProperty(Climb.CLIMB_TEMPERATURE, String.valueOf(startTemperature));
        crackProps.setProperty(Climb.CLIMB_CYCLES, String.valueOf(5000/startTemperature));
        CrackResults.updatePercentageDirectly(crackId, 1);

        // returns true if found all cribs, unlikely on first scan
        String firstActivity = "";
        boolean success = Climb.doSimulatedAnealing(cipherText, this, crackProps, crackId);
        Log.i("CipherCrack", "Cracking "+getCipherName()+" Climb, finished first pass");
        if (!success) {
            // scan with higher temperature, should not wander too far
            CrackResults.updatePercentageDirectly(crackId, 50);
            firstActivity = crackProps.getProperty(Climb.CLIMB_ACTIVITY);
            crackProps.setProperty(Climb.CLIMB_START_KEYWORD, crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD));
            crackProps.setProperty(Climb.CLIMB_TEMPERATURE, String.valueOf(8));
            crackProps.setProperty(Climb.CLIMB_CYCLES, String.valueOf(500));
            if (!Climb.doSimulatedAnealing(cipherText, this, crackProps, crackId)) {
                // even this did not work, give up
                String explain = "Fail: Searched for largest word match but did not find cribs ["
                        + cribString + "], best key was "
                        + crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD)
                        + "\n"
                        + firstActivity
                        + crackProps.getProperty(Climb.CLIMB_ACTIVITY);
                keyword = "";
                return new CrackResult(crackMethod, this, cipherText, explain, crackProps.getProperty(Climb.CLIMB_BEST_DECODE));
            }
        }
        // one or other of the simulated anealing above worked, report back
        keyword = crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD);
        dirs.setKeyword(keyword);
        String plainText = crackProps.getProperty(Climb.CLIMB_BEST_DECODE);
        String explain = "Success: Searched for largest word match and found all cribs ["
                + cribString + "] with key "
                + crackProps.getProperty(Climb.CLIMB_BEST_KEYWORD) + "\n"
                + firstActivity
                + crackProps.getProperty(Climb.CLIMB_ACTIVITY);
        return new CrackResult(crackMethod, this, dirs, cipherText, plainText, explain);
    }

    /**
     * Crack a substitution cipher using all words in the dictionary as seeds for the keyword.
     * Does not work if the keyword was not formed from a word in the dictionary
     * @param cipherText the text to be cracked
     * @param dirs directives we need: ALPHABET, CRIBS, LANGUAGE. On return the result will
     *              also include EXPLAIN and (if successfully cracked) DECODE_KEYWORD
     * @return the result of the crack attempt
     */
    private CrackResult crackDictionary(String cipherText, Directives dirs, int crackId) {
        String alphabet = dirs.getAlphabet();
        String cribString = dirs.getCribs();
        Language language = dirs.getLanguage();
        CrackMethod crackMethod = dirs.getCrackMethod();

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
        int wordsRead = 0;
        for (String word : dict) {
            if (wordsRead++ % 200 == 199) {
                Log.i("CipherCrack", "Cracking Substitution Dict: " + wordsRead + " words tried");
                CrackResults.updatePercentageDirectly(crackId, 100*wordsRead/dict.size());
            }
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
                                    .append(" which decoded to text starting ")
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
                                    .append(" which decoded to REVERSE text starting ")
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
            keyword = validKeyword;
            dirs.setKeyword(validKeyword);
            String explainString = "Success: Searched using "
                    + dict.size()
                    + " dictionary words as keys and found all cribs ["
                    + cribString
                    + "]\n"
                    + explain.toString();
            return new CrackResult(crackMethod, this, dirs, cipherText, validDecode, explainString);
        }
        keyword = "";
        String explainString = "Fail: Searched using "
                + dict.size()
                + " dictionary words as keys but did not find cribs ["
                + cribString + "]\n";
        return new CrackResult(crackMethod, this, cipherText, explainString);
    }

    /**
     * Decode a text using Keyed Substitution cipher with the given keyword
     * @param cipherText the text to be decoded
     * @param dirs a group of directories that define how the cipher will work, especially KEYWORD
     * @return the result of the crack attempt
     */
    @Override
    public CrackResult crack(String cipherText, Directives dirs, int crackId) {

        CrackMethod crackMethod = dirs.getCrackMethod();
        if (crackMethod == CrackMethod.WORD_COUNT) {
            // crack by simulated anealing and measuring number of real word letters in the text
            return crackWordCount(cipherText, dirs, crackId);

        } else {
            // crack type via using dictionary entries as potential keywords
            return crackDictionary(cipherText, dirs, crackId);
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
        text = text.toUpperCase().replaceAll("\\s","");
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
