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
 * Test out the Morse Code 'cipher'
 */
@RunWith(JUnit4.class)
public class MorseTest {

    private static final Morse morse = new Morse(null);

    @Test
    public void testIncorrectProperties() {
        // ensure we get a warning if bad parameters set
        Directives p = new Directives();
        p.setAlphabet(null);
        String reason = morse.canParametersBeSet(p);
        assertEquals("BadParam Alpha missing", "Alphabet is empty or too short", reason);
        p.setAlphabet("");
        reason = morse.canParametersBeSet(p);
        assertEquals("BadParam Alpha empty", "Alphabet is empty or too short", reason);
        p.setAlphabet("A");
        reason = morse.canParametersBeSet(p);
        assertEquals("BadParam Alpha too short", "Alphabet is empty or too short", reason);

        p.setAlphabet(Settings.DEFAULT_ALPHABET);
        reason = morse.canParametersBeSet(p);
        assertEquals("BadParam missing symbols", "Too few symbols", reason);
        p.setDigits("");
        reason = morse.canParametersBeSet(p);
        assertEquals("BadParam empty symbols", "Too few symbols", reason);
        p.setDigits("0");
        reason = morse.canParametersBeSet(p);
        assertEquals("BadParam short symbols", "Too few symbols", reason);
        p.setDigits("012");
        reason = morse.canParametersBeSet(p);
        assertEquals("BadParam long symbols", "Too many symbols (3)", reason);
        p.setDigits("00");
        reason = morse.canParametersBeSet(p);
        assertEquals("BadParam dupe symbols", "Character 0 is duplicated in the symbols", reason);

        p.setDigits("AB");
        p.setSeparator("B");
        reason = morse.canParametersBeSet(p);
        assertEquals("BadParam ", "Separator contains a symbol", reason);
        p.setSeparator("/");
        reason = morse.canParametersBeSet(p);
        assertNull("BadParam all okay", reason);

        p.setCrackMethod(CrackMethod.WORD_COUNT);
        reason = morse.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid crack method", reason);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = morse.canParametersBeSet(p);
        assertEquals("BadParam cribs null", "Some cribs must be provided", reason);
        p.setCribs("");
        reason = morse.canParametersBeSet(p);
        assertEquals("BadParam cribs null", "Some cribs must be provided", reason);
        p.setCribs("home,truth");
        reason = morse.canParametersBeSet(p);
        assertNull("BadParam all okay crack", reason);
    }

    @Test
    public void testDefaultAlphabet() {
        // encode and then decode a mixed case cipher
        Directives p = new Directives();
        p.setDigits("01");
        p.setSeparator("/");
        String reason = morse.canParametersBeSet(p);
        assertNull("Encoding other digits", reason);
        String encoded = morse.encode("Hello", p);
        assertEquals("Encoding default Alphabet mixed case", "0000/0/0100/0100/111", encoded);
        String decoded = morse.decode(encoded, p);
        assertEquals("Decoding default Alphabet mixed case", "HELLO", decoded);
    }

    @Test
    public void testSeps() {
        Directives p = new Directives();
        p.setDigits(".-");
        p.setSeparator(":=:");
        p.setNumberSize(4);
        String reason = morse.canParametersBeSet(p);
        assertNull("Encoding other digits", reason);
        String encoded = morse.encode("won't this be fun", p);
        assertEquals("Encoding default no sep size 4", ".--:=:---:=:-.:=:-:=:-:=:....:=:..:=:...:=:-...:=:.:=:..-.:=:..-:=:-.", encoded);
        String decoded = morse.decode(encoded, p);
        assertEquals("Decoding default no sep size 4", "WONTTHISBEFUN", decoded);
        p.setSeparator("\n");
        reason = morse.canParametersBeSet(p);
        assertNull("Encoding other digits", reason);
        encoded = morse.encode("Life Is For the Living", p);
        assertEquals("Encoding default sep: size 4", ".-..\n..\n..-.\n.\n..\n...\n..-.\n---\n.-.\n-\n" +
                "....\n.\n.-..\n..\n...-\n..\n-.\n--.", encoded);
        decoded = morse.decode(encoded, p);
        assertEquals("Decoding default sep: size 4", "LIFEISFORTHELIVING", decoded);
        p.setDigits("AB");
        p.setSeparator("!");
        reason = morse.canParametersBeSet(p);
        assertNull("Encoding other digits", reason);
        encoded = morse.encode("Born 2 drink mild", p);
        assertEquals("Encoding default sep! size 0", "BAAA!BBB!ABA!BA!AABBB!BAA!ABA!AA!BA!BAB!BB!AA!ABAA!BAA", encoded);
        decoded = morse.decode(encoded, p);
        assertEquals("Decoding default sep! size 0", "BORN2DRINKMILD", decoded);
    }

    // TODO test for Binary Crack success when we have it working

    @Test
    public void testCrackFail() {
        String cipherText = ".-/.../--./..-/./--/..../.-.-";
        Directives p = new Directives();
        p.setCribs("presumably");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        String reason = morse.canParametersBeSet(p);
        assertNull("CrackFail morse reason", reason);

        CrackResult result = morse.crack(cipherText, p, 0);
        assertFalse("Crack morse fail", result.isSuccess());
        assertNull("Crack morse fail text", result.getPlainText());
        assertEquals("Crack morse fail cipher text", cipherText, result.getCipherText());
        assertNull("Crack morse fail digits", result.getDirectives());
        assertNotNull("Crack morse fail explain", result.getExplain());
        assertTrue("Crack morse fail start", result.getExplain().startsWith("No crack"));
        // depends on prior run: assertEquals("Crack morse fail cipher name", "Morse cipher ([], size=n/a)", result.getCipher().getInstanceDescription());
        assertEquals("Crack morse crack method", CrackMethod.BRUTE_FORCE, result.getCrackMethod());
    }

    @Test
    public void testDescription() {
        String desc = morse.getCipherDescription();
        assertNotNull("Description", desc);
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setDigits("01");
        String reason = morse.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = morse.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Morse cipher ([01],sep= )", desc);

        p.setDigits(".-");
        p.setSeparator("/");
        reason = morse.canParametersBeSet(p);
        assertNull("Null reason", reason);
        desc = morse.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Morse cipher ([.-],sep=/)", desc);
    }
}