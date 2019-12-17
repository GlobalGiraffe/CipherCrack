package mnh.game.ciphercrack.language;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class LanguageTest {

    @Test
    public void testLettersOrderedByFrequency() {
        List<Character> letters = Language.instanceOf("English").lettersOrderedByFrequency();
        assertEquals("Freq Order count", 26, letters.size());
        assertEquals("Freq Order most", 'E', (char)letters.get(0));
        assertEquals("Freq Order second", 'T', (char)letters.get(1));
        assertEquals("Freq Order second last", 'Q', (char)letters.get(24));
        assertEquals("Freq Order last", 'Z', (char)letters.get(25));
    }

    @Test
    public void testInfrequentLetters() {
        String letters = Language.instanceOf("English").getInfrequentLetters();
        assertTrue("InfrequentLetters", letters.contains("ZQX"));
    }

    @Test
    public void testDictionaryLoad() {
        Dictionary dict = Language.instanceOf("English").getDictionary();
        assertNotNull("Dictionary null", dict);
        // if fails, we've been adding to TEST dictionary!
        assertEquals("Dictionary size", 11046, dict.size());
        assertTrue("Dictionary contains 'the'", dict.contains("THE"));
        assertTrue("Dictionary contains 'scuttled'", dict.contains("SCUTTLED")); // last word

        // first has no dictionary, second is not a language
        // ah, but now German has a dictionary
        //dict = Language.instanceOf("German").getDictionary();
        //assertNull("Dictionary not exist = null", dict);
        dict = Language.instanceOf("Martian").getDictionary(); // should load English
        assertNotNull("Dictionary default not null", dict);

        Set<Character> singleLetterWords = dict.getSingleLetterWords();
        assertNotNull("SingleLetter not null", singleLetterWords);
        assertEquals("SingleLetter size", 2, singleLetterWords.size());
        assertTrue("SingleLetter has A", singleLetterWords.contains('A'));
        assertTrue("SingleLetter has I", singleLetterWords.contains('I'));
    }

    @Test
    public void testFrequencyOf() {
        // fetch specific frequencies
        Language german = Language.instanceOf("German");
        double freq = german.frequencyOf("H");
        assertEquals("FreqOf H in German", 4.11f, freq, 1e-3f);
        freq = german.frequencyOf("%");
        assertEquals("FreqOf % in German", 0.00f, freq, 1e-3f);
        freq = german.frequencyOf("ER");
        assertEquals("FreqOf ER in German", 3.90f, freq, 1e-3f);
        freq = german.frequencyOf("QA");
        assertEquals("FreqOf QA in German", 0.00f, freq, 1e-3f);
        freq = german.frequencyOf("EIN");
        assertEquals("FreqOf EIN in German", 0.83f, freq, 1e-3f);
        freq = german.frequencyOf("XYZ");
        assertEquals("FreqOf XYZ in German", 0.00f, freq, 1e-3f);

        Language english = Language.instanceOf("English");
        freq = english.frequencyOf("H");
        assertEquals("FreqOf H in English", 6.094f, freq, 1e-3f);
        freq = english.frequencyOf("%");
        assertEquals("FreqOf % in English", 0.00f, freq, 1e-3f);
        freq = english.frequencyOf("AN");
        assertEquals("FreqOf AN in English", 1.61f, freq, 1e-3f);
        freq = english.frequencyOf("QA");
        assertEquals("FreqOf QA in English", 0.00f, freq, 1e-3f);
        freq = english.frequencyOf("THE");
        assertEquals("FreqOf THE in English", 1.81f, freq, 1e-3f);
        freq = english.frequencyOf("UAT");
        assertEquals("FreqOf UAT in English", 0.00f, freq, 1e-3f);
    }
}
