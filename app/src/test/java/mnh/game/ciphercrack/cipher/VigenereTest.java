package mnh.game.ciphercrack.cipher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test out the Vigenere Cipher code
 */
@RunWith(JUnit4.class)
public class VigenereTest {

    private static final String defaultAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Vigenere vigenere = new Vigenere(null);

    @Test
    public void testClassic() {
        // encode and then decode a mixed case cipher
        String keyword = "FORTIFICATION";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        String encoded = vigenere.encode("DEFENDTHEEASTWALLOFTHECASTLE", p);
        assertEquals("Encoding Classic", "ISWXVIBJEXIGGBOCEWKBJEVIGGQS", encoded);
        String decoded = vigenere.decode(encoded, p);
        assertEquals("Decoding Classic", "DEFENDTHEEASTWALLOFTHECASTLE", decoded);
    }

    @Test
    public void testBadParameters() {
        Directives p = new Directives();
        String reason = vigenere.canParametersBeSet(p);
        assertEquals("BadParam: alphabet missing", "Alphabet is empty or too short", reason);
        p.setAlphabet("");
        reason = vigenere.canParametersBeSet(p);
        assertEquals("BadParam: alphabet empty", "Alphabet is empty or too short", reason);
        p.setAlphabet("D");
        reason = vigenere.canParametersBeSet(p);
        assertEquals("BadParam: alphabet empty", "Alphabet is empty or too short", reason);
        p.setAlphabet(defaultAlphabet);

        reason = vigenere.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Keyword is empty or too short", reason);
        p.setKeyword("");
        reason = vigenere.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Keyword is empty or too short", reason);
        p.setKeyword("A");
        reason = vigenere.canParametersBeSet(p);
        assertEquals("BadParam: short keyword", "Keyword is empty or too short", reason);

        p.setKeyword("FORTIFICATION123");    // 1 is not in alphabet
        reason = vigenere.canParametersBeSet(p);
        assertEquals("BadParam: not in alphabet", "Character 1 at offset 13 in the keyword is not in the alphabet", reason);

        p.setKeyword("KEYWORD");
        reason = vigenere.canParametersBeSet(p); // now all good for encode/decode
        assertNull("BadParam: encode okay", reason);

        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = vigenere.canParametersBeSet(p); // Crack: still missing cribs
        assertEquals("BadParam: cribs missing", "Some cribs must be provided", reason);
        p.setCribs("");
        p.setCrackMethod(CrackMethod.IOC);
        reason = vigenere.canParametersBeSet(p);
        assertEquals("BadParam: cribs empty", "Some cribs must be provided", reason);
        p.setCribs("vostok,sputnik,saturn");

        reason = vigenere.canParametersBeSet(p);
        assertEquals("BadParam: length 0", "Keyword length is empty, zero or not a positive integer", reason);
        p.setKeywordLength(-2);
        reason = vigenere.canParametersBeSet(p);
        assertEquals("BadParam: negative length", "Keyword length is empty, zero or not a positive integer", reason);
        p.setKeywordLength(5);
        reason = vigenere.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);

        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = vigenere.canParametersBeSet(p);
        assertEquals("BadParam: empty length", "Missing language", reason);

        p.setLanguage(Language.instanceOf("English"));
        reason = vigenere.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);
    }

    @Test
    public void testCrackIOCSuccess() {
        // attempt IOC crack of Vigenere cipher - needs a decent length to get good IOC
        String keyword = "MOBYDICK";
        String plainText = "Call me Ishmael. Some years ago — never mind how long precisely — having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen, and regulating the circulation. Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people’s hats off — then, I account it high time to get to sea as soon as I can. This is my substitute for pistol and ball. With a philosophical flourish Cato throws himself upon his sword; I quietly take to the ship. There is nothing surprising in this. If they but knew it, almost all men in their degree, some time or other, cherish very nearly the same feelings towards the ocean with me.";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        p.setCrackMethod(CrackMethod.NONE);
        String reason = vigenere.canParametersBeSet(p);
        assertNull("CrackIOCSuccess: encode param okay", reason);
        String cipherText = vigenere.encode(plainText, p);
        assertNotNull("Crack Encoding", cipherText);

        // now attempt the crack of the text via IOC
        p.setKeyword(null);
        p.setAlphabet(defaultAlphabet);
        p.setKeywordLength(keyword.length());
        p.setCribs("ishmael,ocean");
        p.setCrackMethod(CrackMethod.IOC);
        reason = vigenere.canParametersBeSet(p);
        assertNull("CrackIOCSuccess: crack param okay", reason);

        CrackResult result = vigenere.crack(cipherText, p);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        assertTrue("Crack Success", result.isSuccess());
        assertEquals("Crack Cipher", cipherText, result.getCipherText());
        assertEquals("Crack Text", plainText, result.getPlainText());
        assertEquals("Crack Keyword", keyword, decodeKeyword);
        assertNotNull("Crack Explain", explain);
    }

    @Test
    public void testCrackIOCFail() {
        // attempt IOC crack of Vigenere cipher but fails as cribs wrong
        String keyword = "MOBYDICK";
        String plainText = "Call me Ishmael. Some years ago — never mind how long precisely — having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen, and regulating the circulation. Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people’s hats off — then, I account it high time to get to sea as soon as I can. This is my substitute for pistol and ball. With a philosophical flourish Cato throws himself upon his sword; I quietly take to the ship. There is nothing surprising in this. If they but knew it, almost all men in their degree, some time or other, cherish very nearly the same feelings towards the ocean with me.";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        String reason = vigenere.canParametersBeSet(p);
        assertNull("CrackIOCFail: encode param okay", reason);
        String cipherText = vigenere.encode(plainText, p);
        assertNotNull("CrackIOC Encoding", cipherText);

        // now attempt the crack of the text via IOC
        p.setKeyword(null);
        p.setKeywordLength(keyword.length());
        p.setAlphabet(defaultAlphabet);
        p.setCribs("banana,plantation");
        p.setCrackMethod(CrackMethod.IOC);
        reason = vigenere.canParametersBeSet(p);
        assertNull("CrackIOCSuccess: crack param okay", reason);

        CrackResult result = vigenere.crack(cipherText, p);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        assertFalse("CrackFail Success", result.isSuccess());
        assertEquals("CrackFail Cipher", cipherText, result.getCipherText());
        assertNull("CrackFail Text", result.getPlainText());
        assertNull("CrackFail Keyword", result.getDirectives());
        assertNotNull("CrackFail Explain", explain);
    }

    @Test
    public void testCrackDictSuccess() {
        // attempt dictionary crack of Vigenere cipher and succeeds with good cribs
        String keyword = "EXCITED";
        String plainText = "Call me Ishmael. Some years ago — never mind how long precisely — having little or no money in my purse, and nothing particular to interest me on shore, " +
                "I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen, and regulating the circulation. " +
                "Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, " +
                "that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people’s hats off — then, I account it high time to get to sea as soon as I can. " +
                "This is my substitute for pistol and ball. With a philosophical flourish Cato throws himself upon his sword; I quietly take to the ship. There is nothing surprising in this. " +
                "If they but knew it, almost all men in their degree, some time or other, cherish very nearly the same feelings towards the ocean with me.";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        String reason = vigenere.canParametersBeSet(p);
        assertNull("CrackDictSuccess: encode param okay", reason);
        String cipherText = vigenere.encode(plainText, p);
        assertNotNull("CrackDictSuccess: Encoding", cipherText);

        // now attempt the crack of the text via Dictionary
        p.setKeyword(null);
        p.setAlphabet(defaultAlphabet);
        p.setCribs("purse,november,ocean");
        p.setLanguage(Language.instanceOf("English"));
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = vigenere.canParametersBeSet(p);
        assertNull("CrackDictSuccess: crack param okay", reason);

        CrackResult result = vigenere.crack(cipherText, p);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        assertTrue("CrackDict success", result.isSuccess());
        assertEquals("CrackDict Cipher", cipherText, result.getCipherText());
        assertEquals("CrackDict Text", plainText, result.getPlainText());
        assertEquals("CrackDict Keyword", keyword, decodeKeyword);
        assertNotNull("CrackDict Explain", explain);
    }

    @Test
    public void testCrackDictFail() {
        // attempt dictionary crack of Vigenere cipher but fails as cribs wrong
        String keyword = "MOBYDICK";
        String plainText = "Call me Ishmael. Some years ago — never mind how long precisely — having little or no money in my purse, and nothing particular to interest me on shore, " +
                "I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen, and regulating the circulation. " +
                "Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, " +
                "that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people’s hats off — then, I account it high time to get to sea as soon as I can. " +
                "This is my substitute for pistol and ball. With a philosophical flourish Cato throws himself upon his sword; I quietly take to the ship. There is nothing surprising in this. " +
                "If they but knew it, almost all men in their degree, some time or other, cherish very nearly the same feelings towards the ocean with me.";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        String reason = vigenere.canParametersBeSet(p);
        assertNull("CrackDictSuccess: encode param okay", reason);
        String cipherText = vigenere.encode(plainText, p);
        assertNotNull("CrackDictSuccess: Encoding", cipherText);

        // now attempt the crack of the text via Dictionary
        p.setKeyword(null);
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("purse,november,ocean");
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = vigenere.canParametersBeSet(p);
        assertNull("CrackDictSuccess: crack param okay", reason);

        CrackResult result = vigenere.crack(cipherText, p);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        assertFalse("CrackDict Fail success", result.isSuccess());
        assertNull("CrackDict Fail Text", result.getPlainText());
        assertEquals("CrackDict Fail Cipher", cipherText, result.getCipherText());
        assertNull("CrackDict Fail Keyword", result.getDirectives());
        assertNotNull("CrackDict Fail Explain", explain);
    }

    @Test
    public void testDescription() {
        String desc = vigenere.getCipherDescription();
        assertNotNull("Description", desc);
        assertTrue("Description content", desc.contains("Vigenere"));
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setAlphabet(defaultAlphabet);
        p.setKeyword("BROUGHT");
        String reason = vigenere.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = vigenere.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Vigenere cipher (keyword=BROUGHT)", desc);
    }
}
