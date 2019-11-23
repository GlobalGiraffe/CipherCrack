package mnh.game.ciphercrack.transform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test out the Swap Rows and Cols Transform
 */
@RunWith(JUnit4.class)
public class SwapRowsAndColsTest {

    private static final Transform transform = new SwapRowsAndCols();

    @Test
    public void testSwapRowsAndCols() {
        String result = transform.apply(null, "ABCDE\nFGHIJ\nKLMNO\n");
        assertEquals("SwapRowsAndCols #1", "AFK\nBGL\nCHM\nDIN\nEJO\n", result);
    }

    @Test
    public void testSwapRowsAndColsUneven() {
        String result = transform.apply(null, "ABCD\nFGHIJ\nKLM\n");
        assertEquals("SwapRowsAndCols #1", "AFK\nBGL\nCHM\nDI \n J \n", result);
    }

    @Test
    public void testSwapRowsAndColsEdges() {
        String result = transform.apply(null, "");
        assertEquals("Remove Punctuation empty", "", result);
        result = transform.apply(null, null);
        assertNull("Remove Punctuation null", result);
    }
}