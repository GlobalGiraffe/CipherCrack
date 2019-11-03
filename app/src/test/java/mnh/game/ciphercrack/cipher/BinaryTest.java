package mnh.game.ciphercrack.cipher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.Settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test out the Binary Cipher code
 */
@RunWith(JUnit4.class)
public class BinaryTest {

    private static final String defaultAlphabet = Settings.DEFAULT_ALPHABET;
    private static final Language defaultLanguage = Language.instanceOf(Settings.DEFAULT_LANGUAGE);
    private static final Binary binary = new Binary(null);

    @Test
    public void testIncorrectProperties() {
        // ensure we get a warning if bad parameters set
        Directives p = new Directives();
        String reason = binary.canParametersBeSet(p);
        assertEquals("BadParam Alpha missing", "Alphabet is empty or too short", reason);
        p.setAlphabet("");
        reason = binary.canParametersBeSet(p);
        assertEquals("BadParam Alpha empty", "Alphabet is empty or too short", reason);
        p.setAlphabet("A");
        reason = binary.canParametersBeSet(p);
        assertEquals("BadParam Alpha too short", "Alphabet is empty or too short", reason);

        p.setAlphabet(defaultAlphabet);
        reason = binary.canParametersBeSet(p);
        assertEquals("BadParam missing digits", "Too few digits", reason);
        p.setDigits("");
        reason = binary.canParametersBeSet(p);
        assertEquals("BadParam empty digits", "Too few digits", reason);
        p.setDigits("0");
        reason = binary.canParametersBeSet(p);
        assertEquals("BadParam short digits", "Too few digits", reason);
        p.setDigits("012");
        reason = binary.canParametersBeSet(p);
        assertEquals("BadParam long digits", "Too many digits (3)", reason);
        p.setDigits("00");
        reason = binary.canParametersBeSet(p);
        assertEquals("BadParam dupe digits", "Character 0 is duplicated in the digits", reason);

        p.setDigits("AB");
        p.setSeparator("B");
        reason = binary.canParametersBeSet(p);
        assertEquals("BadParam ", "Separator contains a digit", reason);
        p.setSeparator("/");
        reason = binary.canParametersBeSet(p);
        assertNull("BadParam all okay", reason);

        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = binary.canParametersBeSet(p);
        assertEquals("BadParam cribs null", "Language is missing", reason);
        p.setLanguage(defaultLanguage);
        reason = binary.canParametersBeSet(p);
        assertEquals("BadParam cribs null", "Some cribs must be provided", reason);
        p.setCribs("");
        reason = binary.canParametersBeSet(p);
        assertEquals("BadParam cribs null", "Some cribs must be provided", reason);
        p.setCribs("home,truth");
        reason = binary.canParametersBeSet(p);
        assertNull("BadParam all okay crack", reason);
    }

    @Test
    public void testDefaultAlphabet() {
        // encode and then decode a mixed case cipher
        Directives p = new Directives();
        p.setAlphabet(defaultAlphabet);
        p.setLanguage(defaultLanguage);
        p.setDigits("01");
        p.setSeparator("");
        String encoded = binary.encode("AbcDz", p);
        assertEquals("Encoding default Alphabet mixed case", "0000000001000100001111001", encoded);
        String decoded = binary.decode(encoded, p);
        assertEquals("Decoding default Alphabet mixed case", "ABCDZ", decoded);
    }

    @Test
    public void testOtherDigitsAlphabet() {
        // encode and then decode a mixed case cipher
        Directives p = new Directives();
        p.setAlphabet(defaultAlphabet);
        p.setDigits("AB");
        p.setSeparator("");
        String reason = binary.canParametersBeSet(p);
        assertNull("Encoding other digits", reason);
        String encoded = binary.encode("A0d C!z.", p);
        assertEquals("Encoding other digits", "AAAAAAAABBAAABABBAAB", encoded);
        String decoded = binary.decode(encoded, p);
        assertEquals("Decoding other digits", "ADCZ", decoded);

        p.setSeparator("/");
        encoded = binary.encode("AdGz", p);
        assertEquals("Encoding other digits and sep", "A/BB/BBA/BBAAB", encoded);
        decoded = binary.decode(encoded, p);
        assertEquals("Decoding other digits and sep", "ADGZ", decoded);
    }

    @Test
    public void testShortAlphabet() {
        // encode and then decode an upper case cipher using a short alphabet
        final String shortAlphabet = "ABCXYZ";
        Directives p = new Directives();
        p.setAlphabet(shortAlphabet);
        p.setDigits("01");
        p.setSeparator("!");
        String reason = binary.canParametersBeSet(p);
        assertNull("Encoding reason", reason);
        String encoded = binary.encode("ABCDEFG:WXYZ", p);
        assertEquals("Encoding short Alphabet", "0!1!10!11!100!101", encoded);
        String decoded = binary.decode(encoded, p);
        assertEquals("Decoding short Alphabet", "ABCXYZ", decoded);
    }

    // TODO test for Binary Crack success when we have it working

    @Test
    public void testCrackFail() {
        String cipherText = "10102120121110\n";
        Directives p = new Directives();
        p.setCribs("presumably");
        p.setAlphabet(defaultAlphabet);
        p.setLanguage(defaultLanguage);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        String reason = binary.canParametersBeSet(p);
        assertNull("CrackFail binary reason", reason);

        CrackResult result = binary.crack(cipherText, p);
        assertFalse("Crack binary", result.isSuccess());
        assertNull("Crack binary text", result.getPlainText());
        assertEquals("Crack binary cipher text", cipherText, result.getCipherText());
        assertNull("Crack binary digits", result.getDirectives());
        assertNotNull("Crack binary explain", result.getExplain());
        assertTrue("Crack binary start", result.getExplain().startsWith("Fail:"));
    }

    @Test
    public void testDescription() {
        String desc = binary.getCipherDescription();
        assertNotNull("Description", desc);
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setAlphabet(defaultAlphabet);
        p.setDigits("01");
        String reason = binary.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = binary.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Binary cipher ([01])", desc);

        p.setSeparator("/");
        reason = binary.canParametersBeSet(p);
        assertNull("Null reason", reason);
        desc = binary.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Binary cipher ([01],sep=/)", desc);
    }
}