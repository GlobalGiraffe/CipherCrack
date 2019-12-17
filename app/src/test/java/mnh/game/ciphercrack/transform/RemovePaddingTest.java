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
    public void testRemovePaddingSimple() {
        String result = transform.apply(null, "AB cD E FH g");
        assertEquals("Remove Padding simple", "ABcDEFHg", result);
    }

    @Test
    public void testRemovePaddingCC2003() {
        // the default padding is <space>, <tab> and \n
        String result = transform.apply(null, "Agatha Highfield's Journal\nSouthampton, September 1911\n\n MHWTR B LATEE LXM LTBE");
        assertEquals("Remove Padding CC2003", "AgathaHighfield'sJournalSouthampton,September1911MHWTRBLATEELXMLTBE", result);
    }

    @Test
    public void testRemovePaddingEmpty() {
        String result = transform.apply(null, "");
        assertEquals("Remove Padding empty", "", result);
        result = transform.apply(null, null);
        assertNull("Remove Padding null", result);
    }
}