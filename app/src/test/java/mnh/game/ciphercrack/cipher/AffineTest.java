package mnh.game.ciphercrack.cipher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
 * Test out the Affine Cipher code
 */
@RunWith(JUnit4.class)
public class AffineTest {

    private static final String defaultAlphabet = Settings.DEFAULT_ALPHABET;
    private static final Affine affine = new Affine(null);

    @Test
    public void testWikiExample() {
        // encode and then decode a mixed case cipher
        Directives dirs = new Directives();
        dirs.setValueA(5);
        dirs.setValueB(8);
        String encoded = affine.encode("Affine Cipher.", dirs);
        assertEquals("Encoding default Alphabet mixed case", "Ihhwvc Swfrcp.", encoded);
        String decoded = affine.decode(encoded, dirs);
        assertEquals("Decoding default Alphabet mixed case", "Affine Cipher.", decoded);
    }

    @Test
    public void testBadParameters() {
        Directives p = new Directives();
        p.setAlphabet(null);
        String reason = affine.canParametersBeSet(p);
        assertEquals("Bad Param Alphabet", "Alphabet is empty or too short", reason);
        reason = affine.canParametersBeSet(p);
        p.setAlphabet("");
        assertEquals("Bad Param Alphabet", "Alphabet is empty or too short", reason);
        p.setAlphabet(defaultAlphabet);
        p.setPaddingChars(null);
        reason = affine.canParametersBeSet(p);
        assertEquals("Bad Param Padding missing", "Set of padding chars is missing", reason);
        p.setPaddingChars(Settings.DEFAULT_PADDING_CHARS);
        p.setValueA(-2);
        reason = affine.canParametersBeSet(p);
        assertEquals("Bad Param ValueB Wrong", "Values for A (-2) and B (0) must be greater than zero", reason);
        p.setValueA(13);
        reason = affine.canParametersBeSet(p);
        //'a' can only be 1, 3, 5, 7, 9, 11, 15, 17, 19, 21, 23, and 25 with alphabet of length 26
        assertEquals("Bad Param ValueB Null", "Values for A (13) and alphabet length (26) are not co-prime", reason);
        p.setValueB(-2);
        reason = affine.canParametersBeSet(p);
        assertEquals("Bad Param ValueB Wrong", "Values for A (13) and B (-2) must be greater than zero", reason);
        p.setValueB(9);
        p.setValueA(12);
        reason = affine.canParametersBeSet(p);
        assertEquals("Bad Param CoPrime", "Values for A (12) and alphabet length (26) are not co-prime", reason);
        p.setValueA(25);
        reason = affine.canParametersBeSet(p);
        assertNull("Null reason", reason); // now all good

        // try for Cracking now
        p = new Directives();
        p.setCrackMethod(CrackMethod.IOC);
        reason = affine.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid crack method", reason);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = affine.canParametersBeSet(p);
        assertEquals("Bad Param Cribs", "Some cribs must be provided", reason);
        p.setCribs("done");
        reason = affine.canParametersBeSet(p);
        assertNull("Null reason", reason);
    }

    @Test
    public void testCrack() {
        String plain = "Now can we see that ALL are fine and dandy.";
        Directives p = new Directives();
        p.setCribs("fine");
        p.setValueA(5);
        p.setValueB(8);
        String encoded = affine.encode(plain, p);
        p.setCribs("are,fine");
        p.setValueA(-1);
        p.setValueB(-1);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        String reason = affine.canParametersBeSet(p);
        assertNull("Affine parameter", reason);
        CrackResult result = affine.crack(encoded, p, 0);
        assertTrue("Crack affine success", result.isSuccess());
        assertEquals("Crack affine text", plain, result.getPlainText());
        assertEquals("Crack affine A", 5, result.getDirectives().getValueA());
        assertEquals("Crack affine B", 8, result.getDirectives().getValueB());
        assertNotNull("Crack affine explain", result.getExplain());
        assertEquals("Crack affine cipher name", "Affine cipher (a=5, b=8)", result.getCipher().getInstanceDescription());
        assertEquals("Crack affine crack method", CrackMethod.BRUTE_FORCE, result.getCrackMethod());
    }

    @Test
    public void testCrackZeroB() {
        String plain = "When all are done then we find it correct.";
        Directives p = new Directives();
        p.setCribs("done");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        String reason = affine.canParametersBeSet(p);
        assertNull("Null reason", reason);
        CrackResult result = affine.crack(plain, p, 0);
        assertTrue("Crack affine success", result.isSuccess());
        assertEquals("Crack affines text", plain, result.getPlainText());
        assertEquals("Crack affine A", 1, result.getDirectives().getValueA());
        assertEquals("Crack affine B", 0, result.getDirectives().getValueB());
        assertNotNull("Crack affine explain", result.getExplain());
        assertEquals("Crack affine cipher name", "Affine cipher (a=1, b=0)", result.getCipher().getInstanceDescription());
        assertEquals("Crack affine crack method", CrackMethod.BRUTE_FORCE, result.getCrackMethod());
    }

    @Test
    public void testCrackFail() {
        String cipherText = "HGEUDKowKWUWYUHWKIOWIJJSIKWIJSKWLpOWOIWNWIUW";
        Directives p = new Directives();
        p.setCribs("done");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        CrackResult result = affine.crack(cipherText, p, 0);
        assertFalse("Crack affine success", result.isSuccess());
        assertNull("CrackFail affine text", result.getPlainText());
        assertNull("CrackFail affine Directives", result.getDirectives());
        assertNotNull("CrackFail affine explain", result.getExplain());
        assertEquals("CrackFail affine cipher name", "Affine cipher (n/a)", result.getCipher().getInstanceDescription());
        assertEquals("CrackFail affine crack method", CrackMethod.BRUTE_FORCE, result.getCrackMethod());
    }

    @Test
    public void testDescription() {
        String desc = affine.getCipherDescription();
        assertNotNull("Description", desc);
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setValueA(15);
        p.setValueB(7);
        String reason = affine.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = affine.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Affine cipher (a=15, b=7)", desc);
    }
}