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
 * Test out the Beaufort Cipher code
 */
@RunWith(JUnit4.class)
public class BeaufortTest {

    private static final String defaultAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Beaufort cipher = new Beaufort(null);

    @Test
    public void testClassic() {
        // encode and then decode a mixed case cipher
        String keyword = "FORTIFICATION";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        String encoded = cipher.encode("DEFENDTHEEASTWALLOFTHECASTLE", p);
        assertEquals("Encoding Classic", "CKMPVCPVWPIWUJOGIUAPVWRIWUUK", encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Classic", "DEFENDTHEEASTWALLOFTHECASTLE", decoded);
    }

    @Test
    public void testDickensQuote() {
        String plainText = "There are very few moments in a man's existence when he experiences so much ludicrous distress, or meets with so little charitable commiseration, as when he is in pursuit of his own hat.";
        String cipherText = "Kbyta nbz nytg ioh woyaazl ap k snf'l efcmuoqgy oxjf we ynpjbvepiav ap wiix cyaaatqta aakrnjal, ul yajzl murx ve sajrtj qwilclnrse awsbklelklfeq, ik oxjf we us wa djrkqwu ey bus qrf wij.";
        String keyword = "DICKENS";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        String encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Classic", cipherText, encoded);
        String decoded = cipher.decode(cipherText, p);
        assertEquals("Decoding Classic", plainText, decoded);
    }

    @Test
    public void testBadParameters() {
        Directives p = new Directives();
        String reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: alphabet missing", "Alphabet is empty or too short", reason);
        p.setAlphabet("");
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: alphabet empty", "Alphabet is empty or too short", reason);
        p.setAlphabet(defaultAlphabet);

        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Keyword is empty or too short", reason);
        p.setKeyword("");
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Keyword is empty or too short", reason);
        p.setKeyword("A");
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: short keyword", "Keyword is empty or too short", reason);

        p.setCrackMethod(CrackMethod.NONE);
        p.setKeyword("FORTIFICATION123");    // 1 is not in alphabet
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: not in alphabet", "Character 1 at offset 13 in the keyword is not in the alphabet", reason);

        p.setKeyword("KEYWORD");
        reason = cipher.canParametersBeSet(p); // now all good for encode/decode
        assertNull("BadParam: encode okay", reason);

        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = cipher.canParametersBeSet(p); // Crack: still missing cribs
        assertEquals("BadParam: cribs missing", "Some cribs must be provided", reason);
        p.setCribs("");
        p.setCrackMethod(CrackMethod.IOC);
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: cribs empty", "Some cribs must be provided", reason);
        p.setCribs("vostok,sputnik,saturn");

        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: length 0", "Keyword length is empty, zero or not a positive integer", reason);
        p.setKeywordLength(-1);
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: negative length", "Keyword length is empty, zero or not a positive integer", reason);
        p.setKeywordLength(0);
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: negative length", "Keyword length is empty, zero or not a positive integer", reason);

        p.setKeywordLength(5);
        reason = cipher.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);

        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: missing length", "Missing language", reason);

        p.setLanguage(Language.instanceOf("English"));
        reason = cipher.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);
    }

    @Test
    public void testCrackSuccess() {
        // attempt IOC crack of Vigenere cipher - needs a decent length to get good IOC
        String keyword = "ONTHEROAD";
        String plainText = "I first met Dean not long after my wife and I split up. I had just gotten over a serious illness that I won’t bother to talk about, except that it had something to do with the miserably weary split-up and my feeling that everything was dead. With the coming of Dean Moriarty began the part of my life you could call my life on the road. Before that I’d often dreamed of going West to see the country, always vaguely planning and never taking off. Dean is the perfect guy for the road because he actually was born on the road, when his parents were passing through Salt Lake City in 1926, in a jalopy, on their way to Los Angeles. First reports of him came to me through Chad King, who’d shown me a few letters from him written in a New Mexico reform school. I was tremendously interested in the letters because they so naively and sweetly asked Chad to teach him all about Nietzsche and all the wonderful intellectual things that Chad knew.";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("Crack Encoding", cipherText);

        // now attempt the crack of the text via IOC
        p.setKeyword(null);
        p.setKeywordLength(keyword.length());
        p.setAlphabet(defaultAlphabet);
        p.setCribs("moriarty,mexico");
        p.setCrackMethod(CrackMethod.IOC);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Crack Success reason", reason);
        CrackResult cracked = cipher.crack(cipherText, p);
        assertTrue("Crack Status", cracked.isSuccess());
        assertEquals("Crack Plain Text", plainText, cracked.getPlainText());
        assertEquals("Crack Cipher Text", cipherText, cracked.getCipherText());
        assertEquals("Crack Keyword", keyword, cracked.getDirectives().getKeyword());
        assertNotNull("Crack Explain", cracked.getExplain());
    }

    @Test
    public void testCrackFail() {
        // attempt IOC crack of Vigenere cipher - needs a decent length to get good IOC
        String keyword = "ONTHEROAD";
        String plainText = "I first met Dean not long after my wife and I split up. I had just gotten over a serious illness that I won’t bother to talk about, except that it had something to do with the miserably weary split-up and my feeling that everything was dead. With the coming of Dean Moriarty began the part of my life you could call my life on the road. Before that I’d often dreamed of going West to see the country, always vaguely planning and never taking off. Dean is the perfect guy for the road because he actually was born on the road, when his parents were passing through Salt Lake City in 1926, in a jalopy, on their way to Los Angeles. First reports of him came to me through Chad King, who’d shown me a few letters from him written in a New Mexico reform school. I was tremendously interested in the letters because they so naively and sweetly asked Chad to teach him all about Nietzsche and all the wonderful intellectual things that Chad knew.";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackFail Encoding", cipherText);

        // now attempt the crack of the text via IOC
        p.setKeyword(null);
        p.setKeywordLength(keyword.length());
        p.setAlphabet(defaultAlphabet);
        p.setCribs("banana,plantation");
        p.setCrackMethod(CrackMethod.IOC);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Crack Fail reason", reason);
        CrackResult cracked = cipher.crack(cipherText, p);
        assertFalse("Crack Status", cracked.isSuccess());
        assertNull("CrackFail Text", cracked.getPlainText());
        assertEquals("Crack Cipher Text", cipherText, cracked.getCipherText());
        assertNull("Crack Keyword", cracked.getDirectives());
        assertNotNull("Crack Explain", cracked.getExplain());
    }

    @Test
    public void testDescription() {
        String desc = cipher.getCipherDescription();
        assertNotNull("Description", desc);
        assertTrue("Description content", desc.contains("Beaufort"));
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setKeyword("BROUGHT");
        p.setAlphabet(defaultAlphabet);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = cipher.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Beaufort cipher (keyword=BROUGHT)", desc);
    }
}
