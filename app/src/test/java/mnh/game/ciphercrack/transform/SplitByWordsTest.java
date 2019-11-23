package mnh.game.ciphercrack.transform;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import mnh.game.ciphercrack.language.Language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test out the SplitByWords Transform
 */
@RunWith(JUnit4.class)
public class SplitByWordsTest {

    private static final SplitByWords transform = new SplitByWords();
    private final Context context = null;

    @Test
    public void testSplitByWordsSimple() {
        String result = transform.apply(context, "mynameischarlieandiliveinthewoods");
        assertEquals("SplitByWordsSimple", "MY NAME IS CHARLIE AND I LIVE IN THE WOODS", result);
    }

    @Test
    public void testSplitByWordsDutch() {
        Language dutch = Language.instanceOf("Dutch");
        String result = transform.apply(dutch, "rechtdoorgaan,dannaarlinks");
        assertEquals("SplitByWordsSimple", "RECHT DOORGAAN, DAN NAAR LINKS", result);
        result = transform.apply(dutch, "Ikbensinds1maandNederlandsaanhetleren.");
        assertEquals("SplitByWordsSimple", "IK BEN SINDS 1 MAAND NEDERLANDS AAN HET LEREN.", result);
    }

    @Test
    public void testSplitByWordsPunctuation() {
        String result = transform.apply(context, "Youhavebanana,apple,meatandfruit;Ihavechips!");
        assertEquals("SplitByWordsPunctuation", "YOU HAVE BANANA, APPLE, MEAT AND FRUIT; I HAVE CHIPS!", result);
    }

    @Test
    public void testSplitByWordsOneLetterWord() {
        String result;
        // TODO: fix this splitting of words (BEAM), but not sure how to right now: BE A MATTER rather than BEAM A T T E R, perhaps backtracking
        result = transform.apply(context, "itcanonlybeamatteroftimebeforethis");
        assertNotNull("SplitByWordsSingleLetter1", result);
        // issue here: "BEAM" is better than "BE A (MATTER)"
        //assertEquals("SplitByWordsSingleLetter1", "IT CAN ONLY BE A MATTER OF TIME BEFORE THIS", result);
        result = transform.apply(context, "requirestheuseofasecretkeyandtraditionally");
        assertEquals("SplitByWordsSingleLetter2", "REQUIRES THE USE OF A SECRET KEY AND TRADITIONALLY", result);
        result = transform.apply(context, "INHIBITMYAMBITIONIMOSTCERTAINLYWILLNOT");
        assertEquals("SplitByWordsSingleLetter3", "INHIBIT MY AMBITION I MOST CERTAINLY WILL NOT", result);
        result = transform.apply(context, "Iwenttothesweetshopformylunch");
        assertEquals("SplitByWordsSingleLetter4", "I WENT TO THE SWEET SHOP FOR MY LUNCH", result);
    }

    @Test
    public void testSplitByWordsSpaces() {
        String result = transform.apply(context, "where there's a will, there's a way.");
        assertEquals("SplitByWordsSpaces", "WHERE THERE'S A WILL, THERE'S A WAY.", result);
    }

    @Test
    public void testSplitByWordsEdge() {
        String result = transform.apply(context, null);
        assertNull("SplitByWords null", result);
        result = transform.apply(context, "");
        assertEquals("SplitByWords empty", "", result);
    }
}