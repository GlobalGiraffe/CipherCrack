package mnh.game.ciphercrack.transform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test out the Remove All But Alphabetic Transform
 */
@RunWith(JUnit4.class)
public class RemoveNonAlphabeticTest {

    private static final Transform transform = new RemoveNonAlphabetic();

    @Test
    public void testRemoveNonAlphabetic() {
        String result = transform.apply(null, "A, Message! -for% &you: and; (me)");
        assertEquals("RemoveNonAlpha punctuation", "AMessageforyouandme", result);
        result = transform.apply(null, "No punctuation here\nAs you can\nsee");
        assertEquals("RemoveNonAlpha no punctuation", "NopunctuationhereAsyoucansee", result);
        result = transform.apply(null, "");
        assertEquals("RemoveNonAlpha empty", "", result);
        result = transform.apply(null, null);
        assertNull("RemoveNonAlpha null", result);
    }
}