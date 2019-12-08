package mnh.game.ciphercrack.cipher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import mnh.game.ciphercrack.transform.RemoveNonAlphabetic;
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
public class SkytaleTest {

    private static final Skytale cipher = new Skytale(null);

    @Test
    public void testBadProperties() {
        // Ensure we get warning if bad parameters set
        Directives p = new Directives();
        String reason = cipher.canParametersBeSet(p);
        assertEquals("SkytaleBadParam 0", "Cycle length: 0 must be between 2 and "+Skytale.MAX_CYCLE_LENGTH, reason);
        p.setCycleLength(-1);
        reason = cipher.canParametersBeSet(p);
        assertEquals("SkytaleBadParam negative", "Cycle length: -1 must be between 2 and "+Skytale.MAX_CYCLE_LENGTH, reason);
        p.setCycleLength(1);
        reason = cipher.canParametersBeSet(p);
        assertEquals("SkytaleBadParam 1", "Cycle length: 1 must be between 2 and "+Skytale.MAX_CYCLE_LENGTH, reason);
        p.setCycleLength(Skytale.MAX_CYCLE_LENGTH+1);
        reason = cipher.canParametersBeSet(p);
        assertEquals("SkytaleBadParam big", "Cycle length: "+(Skytale.MAX_CYCLE_LENGTH+1)+" must be between 2 and "+Skytale.MAX_CYCLE_LENGTH, reason);
        p.setCycleLength(20);
        reason = cipher.canParametersBeSet(p);
        assertNull("SkytaleBadParam encode okay", reason);

        p.setCycleLength(-1);
        p.setCrackMethod(CrackMethod.WORD_COUNT);
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid crack method", reason);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertEquals("SkytaleBadParam cribs missing", "Some cribs must be provided", reason);

        p.setCribs("");
        reason = cipher.canParametersBeSet(p);
        assertEquals("SkytaleBadParam cribs empty", "Some cribs must be provided", reason);

        p.setCribs("there,and,back,again");
        reason = cipher.canParametersBeSet(p);
        assertNull("SkytaleBadParam crack okay", reason);
    }

    @Test
    public void testSimpleExample() {
        // encode and then decode a mixed case cipher
        Directives p = new Directives();
        p.setPaddingChars(Settings.DEFAULT_PADDING_CHARS);
        p.setAlphabet(Settings.DEFAULT_ALPHABET);
        p.setCycleLength(3);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encoding Wiki mixed case reason", reason);
        String encoded = cipher.encode("We are discovered Flee at once", p);
        assertEquals("Encoding Wiki mixed case", "WoeevaaetrroeenddciFesl ce ", encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Wiki mixed case", "WearediscoveredFleeatonce", decoded);
    }

    @Test
    public void testFoundExample() {
        // encode and then decode a mixed case cipher
        Directives p = new Directives();
        p.setCycleLength(4);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encoding Found example mixed case reason", reason);
        String encoded = cipher.encode("THIS IS A TEST OF THE SCYTALE CIPHER", p);
        assertEquals("Encoding Found mixed case", "TESIHSCPITYHSOTEIFARSTL AHE TEC ", encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Found mixed case", "THISISATESTOFTHESCYTALECIPHER", decoded);
    }

    @Test
    public void testCrack() {
        String plainText = "Now can we see that ALL are fine and dandy.";
        String expectedDecode = new RemoveNonAlphabetic().apply(null, plainText);
        Directives p = new Directives();
        p.setCribs("fine");
        p.setCycleLength(5);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Crack dirs okay1", reason);

        String cipherText = cipher.encode(plainText, p);
        p.setCycleLength(-1);
        p.setCribs("are,fine");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertNull("Crack dirs okay2", reason);

        CrackResult result = cipher.crack(cipherText, p, 0);
        assertTrue("Crack Skytale status", result.isSuccess());
        assertEquals("Crack Skytale text", expectedDecode, result.getPlainText());
        assertEquals("Crack Skytale cipherText", cipherText, result.getCipherText());
        assertEquals("Crack Skytale cycle", 5, result.getDirectives().getCycleLength());
        assertNotNull("Crack Skytale explain", result.getExplain());
        assertEquals("Crack Skytale cipher name", "Skytale cipher (cycleLength=5)", result.getCipher().getInstanceDescription());
        assertEquals("WordCountCrack crack method", CrackMethod.BRUTE_FORCE, result.getCrackMethod());
    }

    @Test
    public void testCrackFail() {
        String cipherText = "When all are done then we find it correct.";
        Directives p = new Directives();
        p.setCribs("lioness");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Atbash parameter", reason);
        CrackResult result = cipher.crack(cipherText, p, 0);
        assertFalse("CrackFail Skytale status", result.isSuccess());
        assertNull("CrackFail Skytale text", result.getPlainText());
        assertEquals("CrackFail Skytale cipherText", cipherText, result.getCipherText());
        assertNull("CrackFail Skytale cycle", result.getDirectives());
        assertNotNull("CrackFail Skytale explain", result.getExplain());
        assertEquals("Crack Skytale cipher name", "Skytale cipher (n/a)", result.getCipher().getInstanceDescription());
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
        p.setCycleLength(4);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = cipher.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Skytale cipher (cycleLength=4)", desc);
    }
}
