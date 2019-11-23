package mnh.game.ciphercrack.cipher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.Settings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(JUnit4.class)
public class AtbashTest {

    private static final String defaultAlphabet = Settings.DEFAULT_ALPHABET;
    private static final Atbash atbash = new Atbash(null);

    @Test
    public void testDefaultAlphabet() {
        // encode and then decode a mixed case cipher
        Directives p = new Directives();
        String encoded = atbash.encode("Hello", p);
        assertEquals("Encoding default Alphabet mixed case", "Svool", encoded);
        String decoded = atbash.decode(encoded, p);
        assertEquals("Decoding default Alphabet mixed case", "Hello", decoded);
    }

    @Test
    public void testShortAlphabet() {
        // encode and then decode an upper case cipher using a short alphabet
        Directives p = new Directives();
        p.setAlphabet("ABCDEFGH");
        String encoded = atbash.encode("BEAD", p);
        assertEquals("Encoding shorter alphabet", "GDHE", encoded);
        p.setAlphabet("AB");
        encoded = atbash.encode("AB", p);
        assertEquals("Encoding shortest alphabet", "BA", encoded);
    }

    @Test
    public void testBadParameters() {
        Directives p = new Directives();
        p.setAlphabet(null);
        String reason = atbash.canParametersBeSet(p); // alphabet is null
        assertEquals("Encoding null alphabet", "Alphabet is empty or too short", reason);
        p.setAlphabet("");
        reason = atbash.canParametersBeSet(p);
        assertEquals("Encoding null alphabet", "Alphabet is empty or too short", reason);
        p.setAlphabet("ABCDEFG");  // odd letters
        reason = atbash.canParametersBeSet(p);
        assertEquals("Encoding null alphabet", "Alphabet has odd length (7) but needs to be even", reason);
        p.setAlphabet(defaultAlphabet);
        p.setPaddingChars(null);
        reason = atbash.canParametersBeSet(p);
        assertEquals("Encoding missing padding", "Set of padding chars is missing", reason);
        p.setPaddingChars(Settings.DEFAULT_PADDING_CHARS);
        reason = atbash.canParametersBeSet(p);
        assertNull("Encoding params okay", reason);

        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = atbash.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid crack method", reason);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = atbash.canParametersBeSet(p);
        assertEquals("Bad param cribs null", "Some cribs must be provided", reason);
        p.setCribs("");
        reason = atbash.canParametersBeSet(p);
        assertEquals("Bad param missing cribs", "Some cribs must be provided", reason);
        p.setCribs("the,and");
        reason = atbash.canParametersBeSet(p);
        assertNull("Bad param params okay", reason);
    }

    @Test
    public void testCrackSuccess() {
        // can find Crib
        String text = "Yzybolm yfimh.";
        Directives p = new Directives();
        p.setCribs("burn");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        atbash.canParametersBeSet(p);
        CrackResult result = atbash.crack(text, p, 0);
        assertTrue("Crack atbash status", result.isSuccess());
        assertEquals("Crack atbash text", "Babylon burns.", result.getPlainText());
        assertEquals("Crack atbash text", text, result.getCipherText());
        assertNotNull("Crack atbash explain", result.getExplain());
        assertEquals("Crack crack method", CrackMethod.BRUTE_FORCE, result.getCrackMethod());
    }

    @Test
    public void testCrackFail() {
        // can't find Crib
        String cipherText = "Yzybolm yfimh.";
        Directives p = new Directives();
        p.setCribs("the,and");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        String reason = atbash.canParametersBeSet(p);
        assertNull("Atbash parameter", reason);
        CrackResult result = atbash.crack(cipherText, p, 0);
        assertFalse("Crack Fail atbash status", result.isSuccess());
        assertNull("Crack Fail atbash text", result.getPlainText());
        assertEquals("Crack Fail atbash text", cipherText, result.getCipherText());
        assertNotNull("Crack Fail atbash explain", result.getExplain());
        assertEquals("Crack crack method", CrackMethod.BRUTE_FORCE, result.getCrackMethod());
    }

    @Test
    public void testDescription() {
        String desc = atbash.getCipherDescription();
        assertNotNull("Description", desc);
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        String reason = atbash.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = atbash.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Atbash cipher", desc);
    }
}
