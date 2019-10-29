package mnh.game.ciphercrack.transform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test out the Remove Padding Transform
 */
@RunWith(JUnit4.class)
public class RemovePaddingTest {

    private static final Transform transform = new RemovePadding();

    @Test
    public void testRemovePadding() {
        String result = transform.apply(null, "AB cD E FH g");
        assertEquals("Remove Padding", "ABcDEFHg", result);
        result = transform.apply(null, "");
        assertEquals("Remove Padding empty", "", result);
        result = transform.apply(null, null);
        assertNull("Remove Padding null", result);
    }
}