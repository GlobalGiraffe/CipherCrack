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
 * Test out the Vigenere Cipher code
 */
@RunWith(JUnit4.class)
public class PermutationTest {

    private static final String defaultAlphabet = Settings.DEFAULT_ALPHABET;
    private final Permutation cipher = new Permutation(null);

    @Test
    public void testConvertKeywordToColumnsGood() {
        String keyword = "ZEBRA";
        int[] expected = new int[]{4, 2, 1, 3, 0};
        int[] result = cipher.convertKeywordToColumns(keyword);
        assertEquals("Convert Keyword Len", expected.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals("Convert Keyword " + i, expected[i], result[i]);
        }
    }

    @Test
    public void testConvertKeywordToColumnsBad() {
        int[] result = cipher.convertKeywordToColumns(null);
        assertNull("ConvertBad Null", result);
        result = cipher.convertKeywordToColumns("");
        assertNull("ConvertBad Empty", result);
        result = cipher.convertKeywordToColumns("A1BD0");
        assertNull("ConvertBad Non-alpha", result);
    }

    @Test
    public void testConvertKeywordToColumnsRepeat() {
        int[] result = cipher.convertKeywordToColumns("FLIPPER");
        assertNull("ConvertRepeat", result);
    }

    @Test
    public void testEncodeDecodeKeyword() {
        // example from http://practicalcryptography.com/ciphers/columnar-transposition-cipher/
        String plainText = "defend the east wall of the castle";
        String key = "GERMAN";
        String expectedCipherText = "nededfahteselwtloactfeahXtseXl";
        Directives p = new Directives();
        int[] perm = cipher.convertKeywordToColumns(key);
        p.setPermutation(perm);
        p.setReadAcross(true);
        p.setAlphabet(defaultAlphabet);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encode: params okay", reason);
        String encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Classic", expectedCipherText, encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Classic", plainText.replaceAll("\\W","")+"XX", decoded);
    }

    @Test
    public void testEncodeDecodeIntsAcross() {
        // encode and then decode a mixed case cipher
        int[] perm = new int[] {3,0,1,2};
        String plainText = "DEFENDTHEEASTWALLOFTHECASTLE";
        // DEFE
        // NDTH
        // EEAS
        // TWAL
        // LOFT
        // HECA
        // STLE
        String expectedCipherText = "EDEFHNDTSEEALTWATLOFAHECESTL";
        Directives p = new Directives();
        p.setPermutation(perm);
        p.setAlphabet(defaultAlphabet);
        p.setReadAcross(true);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encode: params okay", reason);
        String encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Classic", expectedCipherText, encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Classic", plainText, decoded);
    }

    @Test
    public void testEncodeDecodeIntsDown() {
        // encode and then decode a mixed case cipher
        int[] perm = new int[] {3,0,1,2};
        String plainText = "DEFENDTHEEASTWALLOFTHECASTLE";
        // DEFE
        // NDTH
        // EEAS
        // TWAL
        // LOFT
        // HECA
        // STLE
        // encoded is then:
        // EHSLTAE
        // DNETLHS
        // EDEWOET
        // FTAAFCL
        // inverse would be 1,2,3,0, giving
        String expectedCipherText = "EHSLTAEDNETLHSEDEWOETFTAAFCL";
        Directives p = new Directives();
        p.setPermutation(perm);
        p.setAlphabet(defaultAlphabet);
        p.setReadAcross(false); // read down the columns
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encode: params okay", reason);
        String encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Classic", expectedCipherText, encoded);
        String decoded = cipher.decode(encoded, p);
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
        p.setAlphabet("D");
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: alphabet empty", "Alphabet is empty or too short", reason);
        p.setAlphabet(defaultAlphabet);

        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: missing perm", "Permutation is not valid", reason);
        p.setPermutation(new int[0]);
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: empty perm", "Permutation is not valid", reason);
        p.setPermutation(new int[] {0,2,1,2}); // 2 is twice
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: extra entry", "Permutation element 2 is repeated", reason);
        p.setPermutation(new int[] {3,1,2});  // missing 0, 3 should not be there
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: too big #1", "Permutation element 3 is too large", reason);

        p.setPermutation(new int[] {3,1,2,0,9});    // 9 should not be there
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: too big #2", "Permutation element 9 is too large", reason);

        p.setPermutation(new int[] {2,3,1,0});
        reason = cipher.canParametersBeSet(p); // now all good for encode/decode
        assertNull("BadParam: encode okay", reason);

        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p); // Crack: still missing cribs
        assertEquals("BadParam: cribs missing", "Some cribs must be provided", reason);
        p.setCribs("");
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: cribs empty", "Some cribs must be provided", reason);
        p.setCribs("vostok,sputnik,saturn");
        reason = cipher.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: cribs empty", "Language must be provided", reason);
        p.setLanguage(Language.instanceOf("English"));
        reason = cipher.canParametersBeSet(p);
        assertNull("BadParam: crack dict okay", reason);
    }

    @Test
    public void testCrackBruteSuccess() {
        // attempt Brute Force crack of Permutation cipher looking for cribs in all permutations (up to 9)
        int[] perm = new int[] { 0, 2, 4, 3, 1 };
        String plainText = "Call me Ishmael. Some years ago — never mind how long precisely — having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen, and regulating the circulation. Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people’s hats off — then, I account it high time to get to sea as soon as I can. This is my substitute for pistol and ball. With a philosophical flourish Cato throws himself upon his sword; I quietly take to the ship. There is nothing surprising in this. If they but knew it, almost all men in their degree, some time or other, cherish very nearly the same feelings towards the ocean with me.X".replaceAll("\\W","");
        Directives p = new Directives();
        p.setPermutation(perm);
        p.setAlphabet(defaultAlphabet);
        p.setCrackMethod(CrackMethod.NONE);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Crack Success: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("Crack Encoding", cipherText);

        // now attempt the crack of the text via brute force
        p.setPermutation(null);
        p.setAlphabet(defaultAlphabet);
        p.setCribs("ishmael,ocean");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertNull("Crack Success: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = Permutation.permutationToString(result.getDirectives().getPermutation());
        System.out.println("Keyword "+decodeKeyword);
        assertTrue("Crack Success", result.isSuccess());
        assertEquals("Crack Cipher", cipherText, result.getCipherText());
        assertEquals("Crack Text", plainText, result.getPlainText());
        assertEquals("Crack Permutation", "0,2,4,3,1", decodeKeyword);
        assertNotNull("Crack Explain", explain);
    }

    @Test
    public void testCrackBruteFail() {
        // attempt Brute Force crack of Permutation cipher looking for cribs in all permutations (up to 9)
        // but fails as cribs are wrong
        int[] permutation = new int[] {4,2,3,1,0};
        String plainText = "Call me Ishmael. Some years ago — never mind how long precisely — having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen, and regulating the circulation. Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people’s hats off — then, I account it high time to get to sea as soon as I can. This is my substitute for pistol and ball. With a philosophical flourish Cato throws himself upon his sword; I quietly take to the ship. There is nothing surprising in this. If they but knew it, almost all men in their degree, some time or other, cherish very nearly the same feelings towards the ocean with me.X";
        Directives p = new Directives();
        p.setPermutation(permutation);
        p.setAlphabet(defaultAlphabet);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackFail: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackFail Encoding", cipherText);

        // now attempt the crack of the text via Brute Force, will fail die to bad cribs
        p.setPermutation(null);
        p.setAlphabet(defaultAlphabet);
        p.setCribs("banana,plantation");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertNull("CrackSuccess: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        assertFalse("CrackFail Success", result.isSuccess());
        assertEquals("CrackFail Cipher", cipherText, result.getCipherText());
        assertNull("CrackFail Text", result.getPlainText());
        assertNull("CrackFail Permutation", result.getDirectives());
        assertNotNull("CrackFail Explain", explain);
    }

    @Test
    public void testCrackDictSuccess() {
        // attempt dictionary crack of Permutation cipher and succeeds with good cribs
        String keyword = "DISCOUNTER";
        String expectedPermutation = "3,0,8,1,6,4,9,2,7,5";
        String plainText = "Mr. and Mrs. Dursley, of number four, Privet Drive, were proud to say that they were perfectly normal, thank you very much. " +
                "They were the last people you'd expect to be involved in anything strange or mysterious, because they just didn't hold with such nonsense.";
        Directives p = new Directives();
        int[] encodePerm = cipher.convertKeywordToColumns(keyword);
        p.setPermutation(encodePerm);
        p.setAlphabet(defaultAlphabet);
        p.setReadAcross(true);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackDictSuccess: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackDictSuccess: Encoding", cipherText);

        // now attempt the crack of the text via Dictionary
        p.setPermutation(null);
        p.setAlphabet(defaultAlphabet);
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("privet,drive,normal");
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = cipher.canParametersBeSet(p);
        assertNull("CrackDictSuccess: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        int[] decodePermutation = result.getDirectives().getPermutation();
        System.out.println("Permutation "+Permutation.permutationToString(decodePermutation));
        assertTrue("CrackDict success", result.isSuccess());
        assertEquals("CrackDict Cipher", cipherText, result.getCipherText());
        assertEquals("CrackDict Text", plainText.replaceAll("\\W","")+"XXX", result.getPlainText());
        assertEquals("CrackDict Keyword", keyword, decodeKeyword);
        assertEquals("CrackDict Permutation", expectedPermutation, Permutation.permutationToString(decodePermutation));
        assertNotNull("CrackDict Explain", explain);
        assertTrue("CrackDict Explain start", explain.startsWith("Success"));
    }

    @Test
    public void testCrackDictFail() {
        // attempt dictionary crack of Permutation cipher and fails with bad cribs
        String keyword = "TUMOR";
        String plainText = "Mr. and Mrs. Dursley, of number four, Privet Drive, were proud to say that they were perfectly normal, thank you very much. " +
                "They were the last people you'd expect to be involved in anything strange or mysterious, because they just didn't hold with such nonsense.";
        Directives p = new Directives();
        int[] encodePerm = cipher.convertKeywordToColumns(keyword);
        p.setPermutation(encodePerm);
        p.setAlphabet(defaultAlphabet);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackDictSuccess: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackDictSuccess: Encoding", cipherText);

        // now attempt the crack of the text via Dictionary
        p.setPermutation(null);
        p.setAlphabet(defaultAlphabet);
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("banana,republic");   // this is why it fails
        //p.setCribs("dursley");   // with this, it would work
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = cipher.canParametersBeSet(p);
        assertNull("CrackDictSuccess: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        assertFalse("CrackDict success", result.isSuccess());
        assertEquals("CrackDict Cipher Text", cipherText, result.getCipherText());
        assertNull("CrackDict Plain Text", result.getPlainText());
        assertNull("CrackDict Directives", result.getDirectives());
        assertNotNull("CrackDict Explain", explain);
        assertTrue("CrackDict Explain start", explain.startsWith("Fail"));
    }

    @Test
    public void testDescription() {
        String desc = cipher.getCipherDescription();
        assertNotNull("Description", desc);
        assertTrue("Description content", desc.contains("Permutation"));
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setAlphabet(defaultAlphabet);
        p.setPermutation(new int[] {1,3,2,0});
        p.setReadAcross(true);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = cipher.getInstanceDescription();
        assertNotNull("Instance Description across", desc);
        assertEquals("Instance Description across", "Permutation cipher (1,3,2,0:across)", desc);
        p.setReadAcross(false);
        reason = cipher.canParametersBeSet(p);
        assertNull("Null reason", reason);
        desc = cipher.getInstanceDescription();
        assertNotNull("Instance Description down", desc);
        assertEquals("Instance Description down", "Permutation cipher (1,3,2,0:down)", desc);
    }
}
