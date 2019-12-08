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
        String result = transform.apply(context, "mynameisCharlieandIliveintheWoods");
        assertEquals("SplitByWordsSimple", "my name is Charlie and I live in the Woods", result);
    }

    @Test
    public void testSplitByWordsDutch() {
        Language dutch = Language.instanceOf("Dutch");
        String result = transform.apply(dutch, "Rechtdoorgaan,dannaarlinks");
        assertEquals("SplitByWordsSimple", "Recht doorgaan, dan naar links", result);
        result = transform.apply(dutch, "Ikbensinds1maandNederlandsaanhetleren.");
        assertEquals("SplitByWordsSimple", "Ik ben sinds 1 maand Nederlands aan het leren.", result);
    }

    @Test
    public void testSplitByWordsPunctuation() {
        String result = transform.apply(context, "Youhavebanana,apple,meatandfruit;Ihavechips!");
        assertEquals("SplitByWordsPunctuation", "You have banana, apple, meat and fruit; I have chips!", result);
    }

    @Test
    public void testSplitByWordsListens() {
        String result = transform.apply(context, "NOONELISTENSICOULDNTGETANYTHINGMOREOUTOFHIM");
        assertEquals("SplitByWordsPunctuation", "NO ONE LISTENS I COULDNT GET ANYTHING MORE OUT OF HIM", result);
    }

    @Test
    public void testSplitByWordsOneLetterWord() {
        String result;
        result = transform.apply(context, "itcanonlybeamatteroftimebeforethis");
        assertNotNull("SplitByWordsSingleLetter1", result);
        assertEquals("SplitByWordsSingleLetter1", "it can only be a matter of time before this", result);
        result = transform.apply(context, "RequirestheuseofaSecretKeyandtraditionally");
        assertEquals("SplitByWordsSingleLetter2", "Requires the use of a Secret Key and traditionally", result);
        result = transform.apply(context, "INHIBITMYAMBITIONIMOSTCERTAINLYWILLNOT");
        assertEquals("SplitByWordsSingleLetter3", "INHIBIT MY AMBITION I MOST CERTAINLY WILL NOT", result);
        result = transform.apply(context, "Iwenttothesweetshopformylunch");
        assertEquals("SplitByWordsSingleLetter4", "I went to the sweet shop for my lunch", result);
        result = transform.apply(context, "WeCanFindANumberOfUses");
        assertEquals("SplitByWordsSingleLetter5", "We Can Find A Number Of Uses", result);
        result = transform.apply(context, "anDthiSiSnotaGAMEratheraSeriousUndertaking");
        assertEquals("SplitByWordsSingleLetter6", "anD thiS iS not a GAME rather a Serious Undertaking", result);
    }

    @Test
    public void testSplitByWordsSpaces() {
        String result = transform.apply(context, "where there's a will, there's a way.");
        assertEquals("SplitByWordsSpaces", "where there's a will, there's a way.", result);
    }

    @Test
    public void testSplitByWordsEdge() {
        String result = transform.apply(context, null);
        assertNull("SplitByWords null", result);
        result = transform.apply(context, "");
        assertEquals("SplitByWords empty", "", result);
    }
}