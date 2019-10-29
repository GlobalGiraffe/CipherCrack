package mnh.game.ciphercrack.transform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test out the Remove Punctuation Transform
 */
@RunWith(JUnit4.class)
public class RemovePunctuationTest {

    private static final Transform transform = new RemovePunctuation();

    @Test
    public void testRemovePunctuation() {
        String result = transform.apply(null, "A, Message! -for% &you: and; (me)");
        assertEquals("Remove Punctuation", "A Message for you and me", result);
        result = transform.apply(null, "No punctuation here as you can see");
        assertEquals("Remove No Punctuation", "No punctuation here as you can see", result);
        result = transform.apply(null, "");
        assertEquals("Remove Punctuation empty", "", result);
        result = transform.apply(null, null);
        assertNull("Remove Punctuation null", result);
    }
}