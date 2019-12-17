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

@RunWith(JUnit4.class)
public class RailfenceTest {

    private static final Railfence cipher = new Railfence(null);

    @Test
    public void testBadProperties() {
        // Ensure we get warning if bad parameters set
        Directives p = new Directives();
        p.setAlphabet(null);
        String reason = cipher.canParametersBeSet(p);
        assertEquals("RailBadParam alphabet", "Alphabet is empty or too short", reason);
        p.setAlphabet(Settings.DEFAULT_ALPHABET);
        p.setPaddingChars(null);
        reason = cipher.canParametersBeSet(p);
        assertEquals("RailBadParam", "Set of padding chars is missing", reason);
        p.setPaddingChars(Settings.DEFAULT_PADDING_CHARS);
        reason = cipher.canParametersBeSet(p);
        assertEquals("RailBadParam 0", "Number of rails: 0 must be between 2 and 20", reason);
        p.setRails(-1);
        reason = cipher.canParametersBeSet(p);
        assertEquals("RailBadParam negative", "Number of rails: -1 must be between 2 and 20", reason);
        p.setRails(1);
        reason = cipher.canParametersBeSet(p);
        assertEquals("RailBadParam 1", "Number of rails: 1 must be between 2 and 20", reason);
        p.setRails(20+1);
        reason = cipher.canParametersBeSet(p);
        assertEquals("RailBadParam big", "Number of rails: "+p.getRails()+" must be between 2 and "+20, reason);
        p.setRails(20);
        reason = cipher.canParametersBeSet(p);
        assertNull("RailBadParam encode okay", reason);

        p.setRails(-1);
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid crack method", reason);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertEquals("RailBadParam cribs missing", "Some cribs must be provided", reason);

        p.setCribs("");
        reason = cipher.canParametersBeSet(p);
        assertEquals("RailBadParam cribs empty", "Some cribs must be provided", reason);

        p.setCribs("there,and,back,again");
        reason = cipher.canParametersBeSet(p);
        assertNull("RailBadParam crack okay", reason);
    }

    @Test
    public void testWikiExample() {
        // encode and then decode a mixed case cipher
        Directives p = new Directives();
        p.setRails(3);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encoding Wiki mixed case reason", reason);
        String encoded = cipher.encode("WearediscoveredFleeatonce", p);
        assertEquals("Encoding Wiki mixed case", "WecrlteerdsoeeFeaocaivden", encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Wiki mixed case", "WearediscoveredFleeatonce", decoded);
    }

    @Test
    public void testCrack() {
        String plainText = "Now can we see that ALL are fine and dandy.";
        Directives p = new Directives();
        p.setCribs("fine");
        p.setRails(4);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Crack dirs okay1", reason);

        String cipherText = cipher.encode(plainText, p);
        p.setRails(-1);
        p.setCribs("are,fine");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertNull("Crack dirs okay2", reason);

        CrackResult result = cipher.crack(cipherText, p, 0);
        assertTrue("Crack Railfence status", result.isSuccess());
        assertEquals("Crack Railfence text", plainText, result.getPlainText());
        assertEquals("Crack Railfence cipherText", cipherText, result.getCipherText());
        assertEquals("Crack Railfence rails", 4, result.getDirectives().getRails());
        assertNotNull("Crack Railfence explain", result.getExplain());
        assertEquals("Crack Railfence cipher name", "Railfence cipher (rails=4)", result.getCipher().getInstanceDescription());
        assertEquals("WordCountCrack crack method", CrackMethod.BRUTE_FORCE, result.getCrackMethod());
    }

    @Test
    public void testReverseCrack() {
        String plainText = "Now can we see that ALL are fine and dandy.";
        Directives p = new Directives();
        p.setCribs("fine");
        p.setRails(5);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Crack reverse dirs okay1", reason);

        String cipherText = cipher.encode(plainText, p);
        p.setRails(-1);
        p.setCribs("are,fine");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        p.setConsiderReverse(true);
        reason = cipher.canParametersBeSet(p);
        assertNull("Crack reverse dirs okay2", reason);

        String reverseCipherText = new StringBuilder(cipherText).reverse().toString();
        CrackResult result = cipher.crack(reverseCipherText, p, 0);
        assertTrue("Crack Railfence reverse status", result.isSuccess());
        assertEquals("Crack Railfence reverse text", plainText, result.getPlainText());
        assertEquals("Crack Railfence reverse cipherText", reverseCipherText, result.getCipherText());
        assertEquals("Crack Railfence reverse rails", 5, result.getDirectives().getRails());
        assertNotNull("Crack Railfence reverse explain", result.getExplain());
        assertEquals("Crack Railfence reverse cipher name", "Railfence cipher (rails=5)", result.getCipher().getInstanceDescription());
        assertEquals("WordCountCrack reverse crack method", CrackMethod.BRUTE_FORCE, result.getCrackMethod());
    }

    @Test
    public void testCrackFail() {
        String cipherText = "When all are done then we find it correct.";
        Directives p = new Directives();
        p.setCribs("lioness");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackFail parameter", reason);
        CrackResult result = cipher.crack(cipherText, p, 0);
        assertFalse("CrackFail Railfence status", result.isSuccess());
        assertNull("CrackFail Railfence text", result.getPlainText());
        assertEquals("CrackFail Railfence cipherText", cipherText, result.getCipherText());
        assertNull("CrackFail Railfence rails", result.getDirectives());
        assertNotNull("CrackFail Railfence explain", result.getExplain());
        assertEquals("Crack Railfence cipher name", "Railfence cipher (n/a)", result.getCipher().getInstanceDescription());
        assertEquals("WordCountCrack crack method", CrackMethod.BRUTE_FORCE, result.getCrackMethod());
    }

    @Test
    public void testDescription() {
        String desc = cipher.getCipherDescription();
        assertNotNull("Description", desc);
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setRails(3);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = cipher.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Railfence cipher (rails=3)", desc);
    }
}
