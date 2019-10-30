package mnh.game.ciphercrack.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

import mnh.game.ciphercrack.language.Language;

import static org.junit.Assert.assertEquals;

/**
 * Test out the Static Analysis methods
 */
@RunWith(JUnit4.class)
public class StaticAnalysisTest {

    private static final String defaultAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Test
    public void testFrequency() {
        Map<Character, Integer> mapUpper = StaticAnalysis.collectFrequency("Ab%cd!Abc. ABa", true, defaultAlphabet);
        assertEquals("Frequency Upper size", 4, mapUpper.size());
        Map<Character, Integer> mapAsIs = StaticAnalysis.collectFrequency("A! bc & dAbc, ABa.", false, defaultAlphabet);
        assertEquals("Frequency AsIs size", 6, mapAsIs.size());
    }

    @Test
    public void testFrequencyAndIOC() {
        String randomSampleFromWeb = "VVQGY TVVVK ALURW FHQAC MMVLE HUCAT WFHHI PLXHV UWSCI GINCM\n" +
                "UHNHQ RMSUI MHWZO DXTNA EKVVQ GYTVV QPHXI NWCAB ASYYM TKSZR\n" +
                "CXWRP RFWYH XYGFI PSBWK QAMZY BXJQQ ABJEM TCHQS NAEKV VQGYT\n" +
                "VVPCA QPBSL URQUC VMVPQ UTMML VHWDH NFIKJ CPXMY EIOCD TXBJW\n" +
                "KQGAN";
        Map<Character, Integer> mapUpper = StaticAnalysis.collectFrequency(randomSampleFromWeb, true, defaultAlphabet);
        assertEquals("FrequencyIOC size", 26, mapUpper.size());
        int countAlpha = StaticAnalysis.countAlphabetic(randomSampleFromWeb, defaultAlphabet);
        assertEquals("FrequencyIOC alpha count", 205, countAlpha);
        double ioc = StaticAnalysis.getIOC(mapUpper, countAlpha, defaultAlphabet);
        assertEquals("FrequencyIOC ioc", 0.041989, ioc, 1e-4);
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
        double ioc = StaticAnalysis.getIOC(englishLikeSampleFromWeb, english.getAlphabet());
        assertEquals("IOC English Text 1", 0.06810, ioc, delta);

        String simpleMessage = "WHENTHECLOCKSTRIKESTWELVEATTACK";
        ioc = StaticAnalysis.getIOC(simpleMessage, english.getAlphabet());
        assertEquals("IOC English Text 2", 0.06667, ioc, delta);
        assertEquals("IOC English Language", english.getExpectedIOC(), ioc, delta);

        String dutchMessage = "Als u informatie wilt over uw aanvraag (voorwaarden, benodigde documenten, beslistermijn, vragen over correspondentie) raden wij u eerst aan om onze website te raadplegen. Als u aanvullende vragen hebt, belt u ons algemene informatienummer. Hebt u een brief ontvangen van de medewerker die uw aanvraag behandelt? Dan kunt u met specifieke vragen over uw lopende procedure direct contact opnemen met deze medewerker. Bijvoorbeeld wanneer u bepaalde documenten niet tijdig kunt inleveren. U vindt het telefoonnummer van de behandelend medewerker in deze brieven onder het kopje ‘Contactpersoon’";
        Language dutch = Language.instanceOf("Dutch");
        ioc = StaticAnalysis.getIOC(dutchMessage, dutch.getAlphabet());
        assertEquals("IOC Dutch Language", dutch.getExpectedIOC(), ioc, 4e-3);
    }

    @Test
    public void testGetIOCEmpty() {
        double delta = 1e-10;
        double ioc = StaticAnalysis.getIOC("", defaultAlphabet);
        assertEquals("IOC Empty Text", 0.0, ioc, delta);
        ioc = StaticAnalysis.getIOC(null, defaultAlphabet);
        assertEquals("IOC Null Text", 0.0, ioc, delta);
    }
}