package mnh.game.ciphercrack.transform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test out the Reverse Transform
 */
@RunWith(JUnit4.class)
public class SplitAtTest {

    @Test
    public void testSanitySplit() {
        SplitAt split = new SplitAt();
        assertTrue("Split needs dialog", split.needsDialog());
        split.setAdditionalChars("\n", true);
        split.setRegExpr(" ", false);
        String result = split.apply(null, "");
        assertEquals("Split empty", "", result);
        result = split.apply(null, null);
        assertNull("Split null", result);
    }

    @Test
    public void testBasicSplit() {
        SplitAt split = new SplitAt();
        split.setAdditionalChars("\n", true);
        split.setRegExpr(" ", false);
        String result = split.apply(null, "Now is the time");
        assertEquals("Split Spaces", "Now\nis\nthe\ntime", result);
    }

    @Test
    public void testRegExprXSplit() {
        SplitAt split = new SplitAt();
        split.setRegExpr("X", false);
        split.setAdditionalChars(" ", true);
        String result = split.apply(null, "OnceXUponXAXTime");
        assertEquals("Split X1", "Once Upon A Time", result);
    }

    @Test
    public void testRegExprXSplitBefore() {
        SplitAt split = new SplitAt();
        split.setRegExpr("X", true);
        split.setAdditionalChars(" ", true);
        String result = split.apply(null, "OnceXUponXAXTime");
        assertEquals("Split X Before", "Once XUpon XA XTime", result);
    }

    @Test
    public void testRegExprXSplitAfter() {
        SplitAt split = new SplitAt();
        split.setRegExpr("X", true);
        split.setAdditionalChars("-", false);
        String result = split.apply(null, "OnceXUponXAXTime");
        assertEquals("Split X After", "OnceX-UponX-AX-Time", result);
    }

    @Test
    public void testNumberSplitAfter() {
        SplitAt split = new SplitAt();
        split.setSplitCount(10);
        split.setAdditionalChars(":\n", false);
        String result = split.apply(null, "THSIDIIDJQNWKWJKWDJDWDIWDJKWKDKW");
        assertEquals("Split Num After", "THSIDIIDJQ:\nNWKWJKWDJD:\nWDIWDJKWKD:\nKW", result);
    }
}