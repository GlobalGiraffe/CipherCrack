package mnh.game.ciphercrack.transform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test out the UpperCase Transform
 */
@RunWith(JUnit4.class)
public class UpperCaseTest {

    private static final Transform transform = new UpperCase();

    @Test
    public void testUpperCase() {
        String result = transform.apply(null, "ABc DE FHg");
        assertEquals("Lower", "ABC DE FHG", result);
        result = transform.apply(null, "");
        assertEquals("Lower empty", "", result);
        result = transform.apply(null, null);
        assertNull("Lower null", result);
    }
}