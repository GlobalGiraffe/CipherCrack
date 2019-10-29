package mnh.game.ciphercrack.transform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test out the Reverse Transform
 */
@RunWith(JUnit4.class)
public class ReverseTest {

    private static final Transform transform = new Reverse();

    @Test
    public void testReverse() {
        String result = transform.apply(null, "ABcDEFHg");
        assertEquals("Reverse", "gHFEDcBA", result);
        result = transform.apply(null, "");
        assertEquals("Reverse empty", "", result);
        result = transform.apply(null, null);
        assertNull("Reverse null", result);
    }
}