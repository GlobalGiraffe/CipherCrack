package mnh.game.ciphercrack.transform;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReverseWordsTest extends TestCase {

    private static final Transform transform = new ReverseWords();

    @Test
    public void testReverseWordsBadParameter() {
        String result = transform.apply(null, null);
        assertNull("Rev Words null", result);
    }

    @Test
    public void testReverseWordsSomeText() {
        String result = transform.apply(null, "All for one, one for all");
        assertEquals("Rev Words normal1", "llA rof ,eno eno rof lla", result);
        result = transform.apply(null, "there is a house: in New Orleans");
        assertEquals("Rev Words normal2", "ereht si a :esuoh ni weN snaelrO", result);
        result = transform.apply(null, "");
        assertEquals("Rev Words empty", "", result);
        result = transform.apply(null, "wibble-wobble-whatsit");
        assertEquals("Rev Words no padding", "tistahw-elbbow-elbbiw", result);
        result = transform.apply(null, "a  study   of  ciphers    and  their solution  ");
        assertEquals("Rev Words multiple padding", "a  yduts   fo  srehpic    dna  rieht noitulos  ", result);
    }
}
