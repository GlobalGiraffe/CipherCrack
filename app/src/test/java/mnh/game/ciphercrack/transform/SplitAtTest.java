package mnh.game.ciphercrack.transform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test out the SplitAt Transform, used to split at a repeating position or regular expression
 */
@RunWith(JUnit4.class)
public class SplitAtTest {

    @Test
    public void testSanitySplit() {
        SplitAt split = new SplitAt();
        assertTrue("Split needs dialog", split.needsDialog());
        split.setSplitAt(0, " ", false, "\n", false);
        String result = split.apply(null, "");
        assertEquals("Split empty", "", result);
        result = split.apply(null, null);
        assertNull("Split null", result);
    }

    @Test
    public void testBasicSplit() {
        SplitAt split = new SplitAt();
        // split at spaces, don't keep space, replace with '\n' before the place where RE was
        split.setSplitAt(0, " ", false, "\n", false);
        String result = split.apply(null, "Now is the time");
        assertEquals("Split Spaces", "Now\nis\nthe\ntime", result);
    }

    @Test
    public void testRegExprXSplit() {
        SplitAt split = new SplitAt();
        // split at "X", don't keep the "X", replace with <space> before the place where X was
        split.setSplitAt(0, "X", false, " ", false);
        String result = split.apply(null, "OnceXUponXAXTime");
        assertEquals("Split X1", "Once Upon A Time", result);
    }

    @Test
    public void testRegExprXSplitBefore() {
        SplitAt split = new SplitAt();
        // split at "X", do keep the "X", replace with <space> before the place where X was
        split.setSplitAt(0, "X", true, " ", false);
        String result = split.apply(null, "OnceXUponXAXTime");
        assertEquals("Split X Before", "Once XUpon XA XTime", result);
    }

    @Test
    public void testRegExprXSplitAfter() {
        SplitAt split = new SplitAt();
        // split at "X", do keep the "X", replace with "-" AFTER the place where X was
        split.setSplitAt(0, "X", true, " ", true);
        String result = split.apply(null, "OnceXUponXAXTime\n\n");
        assertEquals("Split X After", "OnceX UponX AX Time\n\n", result);
    }

    @Test
    public void testNumberSplitAfter() {
        SplitAt split = new SplitAt();
        // split every 10 chars, at that point put in a ":\n" AFTER at each split
        split.setSplitAt(10, null, true, ":\n", false);
        String result = split.apply(null, "THSIDIIDJQNWKWJKWDJDWDIWDJKWKDKW");
        assertEquals("Split Num After", "THSIDIIDJQ:\nNWKWJKWDJD:\nWDIWDJKWKD:\nKW", result);
    }

    @Test
    public void testAdjacentREs() {
        SplitAt split = new SplitAt();
        // split every 10 chars, at that point put in a ":\n" AFTER at each split
        split.setSplitAt(-1, "XY", false, "A", false);
        String result = split.apply(null, "I like XY but I like XYXY more!");
        assertEquals("Split Adjacent RE", "I like A but I like AA more!", result);
    }

    @Test
    public void testLargeNumber() {
        SplitAt split = new SplitAt();
        // split every 200 chars, at that point put in a ":\n" AFTER that place
        // this means no split, as input is too short
        split.setSplitAt(200, null, true, ":\n", false);
        String result = split.apply(null, "THSIDIIDJQNWKWJKWDJDWDIWDJKWKDKW");
        assertEquals("Split Large Number", "THSIDIIDJQNWKWJKWDJDWDIWDJKWKDKW", result);
    }

    @Test
    public void testBothProvided() {
        SplitAt split = new SplitAt();
        // split at '.', replace with '!'
        split.setSplitAt(10, "\\.", false, "!", true);
        String result = split.apply(null, "There is no time like now. Take it away. Goodbye?");
        assertEquals("Split Both", "There is no time like now! Take it away! Goodbye?", result);
    }

    @Test
    public void testRegExpDoesNotExist() {
        SplitAt split = new SplitAt();
        // split at 'O', replace with 'o'
        split.setSplitAt(0, "O", false, "o", true);
        String result = split.apply(null, "There is no time like now. Take it away. Goodbye?");
        // no change
        assertEquals("Split RegExp not there", "There is no time like now. Take it away. Goodbye?", result);
    }

    @Test
    public void testRealRegExpr() {
        SplitAt split = new SplitAt();
        // split at '[0-9]', replace with 'x'
        split.setSplitAt(0, "[0-9]", false, "x", true);
        String result = split.apply(null, "When I have 201 then 329 will be better than 4.");
        // no change
        assertEquals("Split RealRegExpr", "When I have xxx then xxx will be better than x.", result);
    }

    @Test
    public void testMultiMatchRE() {
        SplitAt split = new SplitAt();
        // split at '[P-Z]', replace with '=[P-Z]'
        split.setSplitAt(0, "[O-Z]+", true, "=", false);
        String result = split.apply(null, "Who, Why, PLACE and ROSES.");
        // no change
        assertEquals("Split RealRegExpr", "=Who, =Why, =PLACE and =ROSE=S.", result);
    }

    @Test
    public void testMultiMatchRePunctuation() {
        SplitAt split = new SplitAt();
        // split at punctuation, replace with '_:_'
        split.setSplitAt(0, "\\p{Punct}", false, "_:_", true);
        String result = split.apply(null, "Who, What, Where, When. And How!");
        // no change
        assertEquals("Split RealRegExpr", "Who_:_ What_:_ Where_:_ When_:_ And How_:_", result);
    }
}