package mnh.game.ciphercrack.staticanalysis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.Settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test out the Static Analysis methods
 */
@RunWith(JUnit4.class)
public class StaticAnalysisTest {

    private static final String defaultAlphabet = Settings.DEFAULT_ALPHABET;
    private static final String defaultPadding = Settings.DEFAULT_PADDING_CHARS;

    @Test
    public void testFactors() {
        int[] factors = StaticAnalysis.factorsOf(7);
        assertEquals("Factors 7 size", 2, factors.length);
        assertEquals("Factors 7#1", 1, factors[0]);
        assertEquals("Factors 7#2", 7, factors[1]);

        factors = StaticAnalysis.factorsOf(12);
        assertEquals("Factors 12 size", 6, factors.length);
        assertEquals("Factors 12#1", 1, factors[0]);
        assertEquals("Factors 12#2", 2, factors[1]);
        assertEquals("Factors 12#3", 3, factors[2]);
        assertEquals("Factors 12#4", 4, factors[3]);
        assertEquals("Factors 12#5", 6, factors[4]);
        assertEquals("Factors 12#6", 12, factors[5]);

        factors = StaticAnalysis.factorsOf(-1);
        assertEquals("Factors -1 size", 0, factors.length);

        factors = StaticAnalysis.factorsOf(0);
        assertEquals("Factors 0 size", 0, factors.length);

        factors = StaticAnalysis.factorsOf(1);
        assertEquals("Factors 1 size", 1, factors.length);
        assertEquals("Factors 1#1", 1, factors[0]);

        factors = StaticAnalysis.factorsOf(2);
        assertEquals("Factors 2 size", 2, factors.length);
        assertEquals("Factors 2#1", 1, factors[0]);
        assertEquals("Factors 2#2", 2, factors[1]);

        factors = StaticAnalysis.factorsOf(13);
        assertEquals("Factors 13 size", 2, factors.length);
        assertEquals("Factors 13#1", 1, factors[0]);
        assertEquals("Factors 13#2", 13, factors[1]);
    }

    @Test
    public void testIsAllNumeric() {
        boolean isAllNumeric = StaticAnalysis.isAllNumeric("826203 38387 27617\n29292 2829\n1019");
        assertTrue("IsAllNumeric#1", isAllNumeric);
        isAllNumeric = StaticAnalysis.isAllNumeric("01 203 04 281");
        assertTrue("IsAllNumeric#2", isAllNumeric);

        isAllNumeric = StaticAnalysis.isAllNumeric("01 20A 04 281");
        assertFalse("IsAllNumeric#3", isAllNumeric);
        isAllNumeric = StaticAnalysis.isAllNumeric("01 20 04 281%");
        assertFalse("IsAllNumeric#4", isAllNumeric);
        isAllNumeric = StaticAnalysis.isAllNumeric("01-20 04 281");
        assertFalse("IsAllNumeric#5", isAllNumeric);
        isAllNumeric = StaticAnalysis.isAllNumeric("01 20!04 281");
        assertFalse("IsAllNumeric#6", isAllNumeric);

        isAllNumeric = StaticAnalysis.isAllNumeric(null);
        assertFalse("IsAllNumeric#null", isAllNumeric);
    }

    @Test
    public void testFrequency() {
        Map<Character, Integer> mapAllAsIsPlusSpace = StaticAnalysis.collectFrequency("Ab%cd!Abc. ABa", true, false, defaultAlphabet, "");
        assertEquals("Frequency Upper size", 10, mapAllAsIsPlusSpace.size());  // A B a b c d % ! . <space>
        Map<Character, Integer> mapAllAsIs = StaticAnalysis.collectFrequency("Ab%cd!Abc. ABa", true, false, defaultAlphabet, defaultPadding);
        assertEquals("Frequency Upper size", 9, mapAllAsIs.size());  // A B a b c d % ! .
        Map<Character, Integer> mapAllUpper = StaticAnalysis.collectFrequency("Ab%cd!Abc. ABa", true, true, defaultAlphabet, defaultPadding);
        assertEquals("Frequency Upper size", 7, mapAllUpper.size());  // A B C D % ! .
        Map<Character, Integer> mapAsIs = StaticAnalysis.collectFrequency("A! bc & dAbc, ABa.", false, false, defaultAlphabet, defaultPadding);
        assertEquals("Frequency AsIs size", 6, mapAsIs.size());       // A B a b c d
        Map<Character, Integer> mapUpper = StaticAnalysis.collectFrequency("Ab%cd!Abc. ABa", false, true, defaultAlphabet, defaultPadding);
        assertEquals("Frequency Upper size", 4, mapUpper.size());     // A B C D
    }

    @Test
    public void testFrequencyAndIOC() {
        String randomSampleFromWeb = "VVQGY TVVVK ALURW FHQAC MMVLE HUCAT WFHHI PLXHV UWSCI GINCM\n" +
                "UHNHQ RMSUI MHWZO DXTNA EKVVQ GYTVV QPHXI NWCAB ASYYM TKSZR\n" +
                "CXWRP RFWYH XYGFI PSBWK QAMZY BXJQQ ABJEM TCHQS NAEKV VQGYT\n" +
                "VVPCA QPBSL URQUC VMVPQ UTMML VHWDH NFIKJ CPXMY EIOCD TXBJW\n" +
                "KQGAN";
        Map<Character, Integer> mapAllIncludePadding = StaticAnalysis.collectFrequency(randomSampleFromWeb, true, false, defaultAlphabet, "");
        assertEquals("FrequencyIOC size1", 28, mapAllIncludePadding.size());
        // 28 is 26 alpha chars, including <space> and CR
        Map<Character, Integer> mapAllAsIs = StaticAnalysis.collectFrequency(randomSampleFromWeb, true, false, defaultAlphabet, defaultPadding);
        assertEquals("FrequencyIOC size1", 26, mapAllAsIs.size());
        // 26 alpha chars, <space> and CR has been removed
        Map<Character, Integer> mapAllUpper = StaticAnalysis.collectFrequency(randomSampleFromWeb, true, true, defaultAlphabet, defaultPadding);
        assertEquals("FrequencyIOC size2", 26, mapAllUpper.size());
        Map<Character, Integer> mapAsIs = StaticAnalysis.collectFrequency(randomSampleFromWeb, false, false, defaultAlphabet, defaultPadding);
        assertEquals("FrequencyIOC size3", 26, mapAsIs.size());
        Map<Character, Integer> mapUpper = StaticAnalysis.collectFrequency(randomSampleFromWeb, false, true, defaultAlphabet, defaultPadding);
        assertEquals("FrequencyIOC size4", 26, mapUpper.size());
        int countAlpha = StaticAnalysis.countAlphabetic(randomSampleFromWeb, defaultAlphabet);
        assertEquals("FrequencyIOC alpha count", 205, countAlpha);
        double ioc = StaticAnalysis.calculateIOC(mapUpper, countAlpha, defaultAlphabet);
        assertEquals("FrequencyIOC ioc", 0.041989, ioc, 1e-4);
        String sampleString = "NOW WHERE DID I PUT THAT SHAWL, BY BONNY BOY?";
        ioc = StaticAnalysis.calculateIOC(sampleString, defaultAlphabet, defaultPadding);
        assertEquals("FrequencyIOC ioc", 0.044563, ioc, 1e-4);
    }

    @Test
    public void testAlphaCount() {
        int count = StaticAnalysis.countAlphabetic("ABC def, GHI: jkl MNO pqr", defaultAlphabet);
        assertEquals("Count Mixed Text", 18, count);
        String text = "ABC def, GHI: jkl MNO pqr";
        count = StaticAnalysis.countAlphabetic(text+text, defaultAlphabet);
        assertEquals("Count Double Text", 36, count);
    }

    @Test
    public void testAlphaCountReducedAlphabet() {
        int count = StaticAnalysis.countAlphabetic("ABC def, GHI: jkl MNO pqr", "ABCDEFG");
        assertEquals("Count Mixed Text", 7, count);
        String text = "ABC def, GHI: jkl MNO pqr";
        count = StaticAnalysis.countAlphabetic(text+text, "STUVWXYZ");
        assertEquals("Count Double Text", 0, count);
    }

    @Test
    public void testAlphaCountEmpty() {
        int count = StaticAnalysis.countAlphabetic("", "ABCDEFG");
        assertEquals("Count Empty Text", 0, count);
        count = StaticAnalysis.countAlphabetic(null, "STUVWXYZ");
        assertEquals("Count Null Text", 0, count);
    }

    @Test
    public void testNonPaddingCount() {
        int count = StaticAnalysis.countNonPadding("ABC% def, GHI: jkl MNO pqr", " ");
        assertEquals("Count NP Mixed Text", 21, count);
        String text = "ABC def, GHI: jkl MNO pqr-|";
        count = StaticAnalysis.countNonPadding(text+text, " ");
        assertEquals("Count NP Double Text", 44, count);
        count = StaticAnalysis.countNonPadding("", " ");
        assertEquals("Count NP Empty", 0, count);
        count = StaticAnalysis.countNonPadding(null, " ");
        assertEquals("Count NP null", 0, count);
        count = StaticAnalysis.countNonPadding("albatros", "ab");
        assertEquals("Count NP pad=ab", 5, count);
    }

    @Test
    public void testGetIOC() {
        double delta = 1e-3;
        Language english = Language.instanceOf("English");
        String englishLikeSampleFromWeb = "WKHUH DUHWZ RZDBV RIFRQ VWUXF WLQJD VRIWZ DUHGH VLJQR QHZDB\n" +
                "LVWRP DNHLW VRVLP SOHWK DWWKH UHDUH REYLR XVOBQ RGHIL FLHQF\n" +
                "LHVDQ GWKHR WKHUZ DBLVW RPDNH LWVRF RPSOL FDWHG WKDWW KHUHD\n" +
                "UHQRR EYLRX VGHIL FLHQF LHVWK HILUV WPHWK RGLVI DUPRU HGLII\n" +
                "LFXOW";
        double ioc = StaticAnalysis.calculateIOC(englishLikeSampleFromWeb, english.getAlphabet(), defaultPadding);
        assertEquals("IOC English Text 1", 0.06810, ioc, delta);

        String simpleMessage = "WHENTHECLOCKSTRIKESTWELVEATTACK";
        ioc = StaticAnalysis.calculateIOC(simpleMessage, english.getAlphabet(), defaultPadding);
        assertEquals("IOC English Text 2", 0.06667, ioc, delta);
        assertEquals("IOC English Language", english.getExpectedIOC(), ioc, delta);

        String dutchMessage = "Als u informatie wilt over uw aanvraag (voorwaarden, benodigde documenten, beslistermijn, vragen over correspondentie) raden wij u eerst aan om onze website te raadplegen. Als u aanvullende vragen hebt, belt u ons algemene informatienummer. Hebt u een brief ontvangen van de medewerker die uw aanvraag behandelt? Dan kunt u met specifieke vragen over uw lopende procedure direct contact opnemen met deze medewerker. Bijvoorbeeld wanneer u bepaalde documenten niet tijdig kunt inleveren. U vindt het telefoonnummer van de behandelend medewerker in deze brieven onder het kopje ‘Contactpersoon’";
        Language dutch = Language.instanceOf("Dutch");
        ioc = StaticAnalysis.calculateIOC(dutchMessage, dutch.getAlphabet(), defaultPadding);
        assertEquals("IOC Dutch Language", dutch.getExpectedIOC(), ioc, 4e-3);
    }

    @Test
    public void testGetIOCEmpty() {
        double delta = 1e-10;
        double ioc = StaticAnalysis.calculateIOC("", defaultAlphabet, defaultPadding);
        assertEquals("IOC Empty Text", 0.0, ioc, delta);
        ioc = StaticAnalysis.calculateIOC(null, defaultAlphabet, defaultPadding);
        assertEquals("IOC Null Text", 0.0, ioc, delta);
    }

    @Test
    public void testGetCyclicIOC() {
        String text = "AHHDIOWPLKQJJSUPOQJKKKQJAIOAIWFSXWWVWPWPWKWLNMXMZBCTDHYWFEWVEJDIUDHEWKIDUEENEGDPQNMNOZGQOGAZCCXRFQPWUEHRNCOMDHGETYEHJEPOPQWVXPGSURJQ";
        double[] ioc = StaticAnalysis.getCyclicIOC(text, null, defaultAlphabet, defaultPadding);
        assertEquals("CyclicIOC Size ", 30, ioc.length);
    }

    @Test
    public void testCollectGramFrequency() {
        String text = "ABC DE FGA";
        Map<String, Integer> grams = StaticAnalysis.collectGramFrequency(text, 1, null);
        assertEquals("Collect Gram Freq ", 7, grams.size()); // A is twice
        assertEquals("Collect Gram A", Integer.valueOf(2), grams.get("A"));
        assertEquals("Collect Gram B", Integer.valueOf(1), grams.get("B"));
        assertEquals("Collect Gram F", Integer.valueOf(1), grams.get("F"));
        assertNull("Collect Gram B", grams.get("Z"));

        text = "AB AB FA";
        grams = StaticAnalysis.collectGramFrequency(text, 2, null);
        assertEquals("Collect Gram Freq ", 4, grams.size()); // A is twice
        assertEquals("Collect Gram AB", Integer.valueOf(2), grams.get("AB"));
        assertEquals("Collect Gram BA", Integer.valueOf(1), grams.get("BA"));
        assertEquals("Collect Gram BF", Integer.valueOf(1), grams.get("BF"));
        assertEquals("Collect Gram FA", Integer.valueOf(1), grams.get("FA"));
        assertNull("Collect Gram FB", grams.get("FB"));
    }
}