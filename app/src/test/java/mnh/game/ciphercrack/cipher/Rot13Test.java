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
public class Rot13Test {

    private static final Rot13 rot13 = new Rot13(null);

    @Test
    public void testBadParameters() {
        Directives p = new Directives();
        p.setAlphabet(null);
        String reason = rot13.canParametersBeSet(p); // alphabet is null
        assertEquals("Encoding null alphabet", "Alphabet is empty or too short", reason);
        p.setAlphabet("");
        reason = rot13.canParametersBeSet(p);
        assertEquals("Encoding null alphabet", "Alphabet is empty or too short", reason);
        p.setAlphabet(Settings.DEFAULT_ALPHABET);
        reason = rot13.canParametersBeSet(p);
        assertNull("Encoding params okay", reason);

        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = rot13.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid crack method", reason);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = rot13.canParametersBeSet(p);
        assertEquals("Crack cribs null", "Some cribs must be provided", reason);
        p.setCribs("");
        reason = rot13.canParametersBeSet(p);
        assertEquals("Crack missing cribs", "Some cribs must be provided", reason);
        p.setCribs("the,and");
        reason = rot13.canParametersBeSet(p);
        assertNull("Crack params okay", reason);
    }

    @Test
    public void testDefaultAlphabet() {
        // encode and then decode a mixed case cipher
        Directives p = new Directives();
        String encoded = rot13.encode("Hello", p);
        assertEquals("Encoding default Alphabet mixed case", "Uryyb", encoded);
        String decoded = rot13.decode(encoded, p);
        assertEquals("Decoding default Alphabet mixed case", "Hello", decoded);
    }

    @Test
    public void testBadAlphabet() {
        // encode and then decode an upper case cipher using a short alphabet
        Directives p = new Directives();
        p.setAlphabet("ABCDEFGHIJKLMNOPQRSTUVWXY");  // no Z
        String encoded = rot13.encode("AOUYIEAOEY", p);
        assertEquals("Alphabet too short", "", encoded);
        p.setAlphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ1");  // extra 1
        encoded = rot13.encode("AOUYIEAOEY", p);
        assertEquals("Alphabet too long", "", encoded);
    }

    @Test
    public void testCrackSuccess() {
        String cipherText = "Gur fxl nobir gur cbeg jnf gur pbybe bs gryrivfvba, gharq gb n qrnq punaary.";
        String plainText = "The sky above the port was the color of television, tuned to a dead channel.";
        Directives p = new Directives();
        p.setCribs("the,dead");
        CrackResult result = rot13.crack(cipherText, p, 0);
        assertTrue("Crack rot13 success", result.isSuccess());
        assertEquals("Crack rot13 plain", plainText, result.getPlainText());
        assertEquals("Crack rot13 cipher", cipherText, result.getCipherText());
        assertEquals("Crack rot13 shift", 13, result.getDirectives().getShift());
        assertNotNull("Crack 13 explain", result.getExplain());
    }

    @Test
    public void testCrackFail() {
        String cipherText = "Gur fxl nobir gur cbeg jnf gur pbybe bs gryrivfvba, gharq gb n qrnq punaary.";
        Directives p = new Directives();
        p.setCribs("the,and");
        CrackResult result = rot13.crack(cipherText, p, 0);
        assertFalse("Crack rot13 fail", result.isSuccess());
        assertNull("CrackFail rot13 text", result.getPlainText());
        assertEquals("CrackFail rot13 cipher", cipherText, result.getCipherText());
        assertNull("CrackFail rot13 shift", result.getDirectives());
        assertNotNull("CrackFail rot13 explain", result.getExplain());
    }

    @Test
    public void testDescription() {
        String desc = rot13.getCipherDescription();
        assertNotNull("Description", desc);
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        String reason = rot13.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = rot13.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "ROT13 cipher", desc);
    }
}
