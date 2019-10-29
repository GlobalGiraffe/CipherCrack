package mnh.game.ciphercrack.language;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

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
    public void testDictionaryLoad() {
        Dictionary dict = Language.instanceOf("English").getDictionary();
        assertNotNull("Dictionary null", dict);
        assertEquals("Dictionary size", 11035, dict.size());
        assertTrue("Dictonary contains 'the'", dict.contains("THE"));

        // first has no dictionary, second is not a language
        dict = Language.instanceOf("Dutch").getDictionary();
        assertNull("Dictionary not exist = null", dict);
        dict = Language.instanceOf("Martian").getDictionary(); // should load English
        assertNotNull("Dictionary default not null", dict);
    }
}
