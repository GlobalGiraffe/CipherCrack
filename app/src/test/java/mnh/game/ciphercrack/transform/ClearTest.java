package mnh.game.ciphercrack.transform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

/**
 * Test out the Clear Transform
 */
@RunWith(JUnit4.class)
public class ClearTest {

    private static final Transform transform = new Clear();

    @Test
    public void testClear() {
        String result = transform.apply(null, "Jdkwodnkoowjnwbilg;hd.QHO");
        assertEquals("Clear text", "", result);
        result = transform.apply(null, "");
        assertEquals("Clear empty", "", result);
        result = transform.apply(null, null);
        assertEquals("Clear null", "", result);
    }
}