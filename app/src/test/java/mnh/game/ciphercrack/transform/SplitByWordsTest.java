package mnh.game.ciphercrack.transform;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import mnh.game.ciphercrack.language.Language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        assertEquals("SplitByWordsDutch", "Rechtdoor gaan, dan naar links", result);
        result = transform.apply(dutch, "Ikbensinds1maandNederlandsaanhetleren.");
        assertEquals("SplitByWordsDutch", "Ik ben sinds 1 maand Nederlands aan het leren.", result);
    }

    @Test
    public void testSplitByWordsGerman() {
        Language german = Language.instanceOf("German");
        String result = transform.apply(german, "IchbinderMeister");
        assertEquals("SplitByWordsGerman", "Ich bin der Meister", result);
        // does not come back exactly the same, e.g. "Schultüte"=>"Sch u l tüte", "die Pause-n" at the end
        String text = "Heute ist der erste Schultag. Lena steht mit ihrer Schultüte vor der Schule. Sandra, Susanne und Paul sind auch da. Die Kinder kennen sich aus dem Kindergarten. Jetzt gehen sie in die gleiche Klasse. Sie freuen sich schon auf den Unterricht. Lena freut sich besonders auf das Rechnen. Sandra und Susanne aufs Schreiben. Und Paul? Paul sagt, er freut sich besonders auf die Pausen";
        result = transform.apply(german, text.replaceAll(" ",""));
        assertTrue("SplitByWordsGerman Longer", result.startsWith("Heute ist der erste Schultag. Lena steht mit ihrer"));
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
    public void testSplitByWordsDNA() {
        // look 3 words ahead
        String result = transform.apply(context, "HEREFUSEDREQUESTSTOPROVIDEADNASAMPLE");
        assertEquals("SplitByWordsPunctuation", "HE REFUSED REQUESTS TO PROVIDE A DNA SAMPLE", result);
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
    public void testOverlook() {
        String result = transform.apply(context, "inclinedtooverlooktherealmightofthemorepowerful");
        assertEquals("Split Overlook", "inclined to overlook the real might of the more powerful", result);
    }

    @Test
    public void testSplitLargeText() {
        String result = transform.apply(context, "charlesisrightthethreatstotheeastaregrowingbythedayinthesouthernregiontheottomansarerestlessandinthenorththethreeemperorsareagrowingthreattoourinfluencetheforeignofficeseemparalysedfocussedentirelyonformingalliancesandunabletodeterminetheirbeststrategyithinktheyaremistakenintheiroutlooktheyfocustoomuchontreatiesandnotenoughonrealpolitikasvonrochauwrotethelawofpowergovernstheworldofstatesjustasthelawofgravitygovernsthephysicalworldandweareperhapstoomuchinclinedtooverlooktherealmightofthemorepowerfulandtheinevitabilityofitspoliticalinfluenceourtasknowisnottochooseafavouredpartnertocontroltheothersthatwayriskswaranalliancewillencourageacounterallianceandadangerousescalationinsteadourstrategyshouldbetomaintainanadmittedlyuneasypeacebetweentheemperorstogethertheyarethebiggestpoliticalthreattoourempirebutasenemiesofoneanothertheyalsothreatenourtradingroutesweneedtoinducethemtoworktogetherwhilepreventingthemfromformingapowerblocagainstourinterestourmostimportantweaponinthisisconfusionandmilddistrustthebestoutcomewouldbetoencourageadysfunctionalalliancebetweenallthreethatmakesithardfortheemperorstofightoneanotherorusandintheottomancrisisithinkisenseanopportunitytodojustthat");
        assertEquals("Split Large Text", "charles is right the threats to thee a stare growing by the day in the southern region the ottoman s are rest less and in the north the three emperors area growing threat to our influence the foreign office seem paralysed focussed entirely on forming alliances and unable to determine their best strategy i think they are mistaken in their outlook they focus too much on treaties and not enough on realpolitik as von r o c h a u wrote the law of power governs the world of states just as the law of gravity governs the physical world and we are perhaps too much inclined to overlook the real might of the more powerful and the inevitability of its political influence our task now is not to choose a favoured partner to control the others that way risks war an alliance will encourage a counter alliance and a dangerous escalation instead our strategy should be to maintain an admittedly uneasy peace between the emperors together they are the biggest political threat to our empire but as enemies of one another they also threaten our trading routes we need to induce them to work together while preventing them from forming a powerbloc against our interest our most important weapon in this is confusion and mild distrust the best outcome would be to encourage a dysfunctional alliance between all three that makes it hard fort he emperors to fight one another or us and in the ottoman crisis i think i sense an opportunity to do just that", result);
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