package mnh.game.ciphercrack.cipher;

import org.junit.Ignore;
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
 * Test out the Amsco Cipher code
 */
@RunWith(JUnit4.class)
public class AmscoTest {

    private final Amsco cipher = new Amsco(null);

    @Test
    public void testConvertKeywordToColumnsGood() {
        String keyword = "BETA";
        int[] expected = new int[]{3,0,1,2};
        int[] result = Amsco.convertKeywordToColumns(keyword,0);
        assertEquals("Convert Keyword Len", expected.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals("Convert Keyword " + i, expected[i], result[i]);
        }
    }

    @Test
    public void testConvertNumbersToColumnsGood() {
        String integers = "0,2,1,4,3";
        int[] expected = new int[]{0,2,1,4,3};
        int[] result = Amsco.convertKeywordToColumns(integers,0);
        assertEquals("Convert Integers Len", expected.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals("Convert Integers " + i, expected[i], result[i]);
        }
    }

    @Test
    public void testConvertKeywordToColumnsBad() {
        int[] result = Amsco.convertKeywordToColumns(null,0);
        assertNull("ConvertBad Null", result);
        result = Amsco.convertKeywordToColumns("",0);
        assertNull("ConvertBad Empty", result);
        result = Amsco.convertKeywordToColumns("A1BD0",0);
        assertNull("ConvertBad Non-alpha", result);
        result = Amsco.convertKeywordToColumns("1,2,1,0",0);
        assertNull("ConvertBad Repeat", result);
        result = Amsco.convertKeywordToColumns("0,3,2",0);
        assertNull("ConvertBad Gap", result);
        result = Amsco.convertKeywordToColumns("-1,1,0",0);
        assertNull("ConvertBad Negative", result);
    }

    @Test
    public void testConvertKeywordToColumnsRepeat() {
        int[] result = Amsco.convertKeywordToColumns("FLIPPER",0);
        assertNull("ConvertRepeat", result);
    }

    @Test
    public void testEncodeDecodeKeyword_1_2() {
        // example from https://www.thonky.com/kryptos/amsco-cipher
        String plainText = "Whoever has made a voyage up the Hudson must remember the Kaatskill mountains.".toUpperCase();
        String key = "2,4,0,3,1";
        String expectedCipherText = "EMAAEHUMBALMNREAUDSRRTSUNWHAVPTOEMHKITVEDGEUSTEATOSHOSOYHNMEEKLAI";
        Directives p = new Directives();
        int[] perm = Amsco.convertKeywordToColumns(key,0);
        p.setPermutation(perm);
        p.setCharsPerCell(new int[] {1,2});
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encode: params okay", reason);
        String encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Classic", expectedCipherText, encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Classic", plainText.replaceAll("\\W",""), decoded);
    }

    @Test
    public void testEncodeDecodeIncomplete_2_1() {
        // encode and then decode a mixed case cipher
        // https://www.cryptogram.org/downloads/aca.info/ciphers/Amsco.pdf
        int[] perm = new int[] {1,3,2,0,4};
        String plainText = "Incomplete columnar with alternating single letters and digraphs.".replaceAll("[ .]","");
        String expectedCipherText = "cecrteglenphplutnanteiomowirsitddsIntnalinesaalemhatglrgr";
        Directives p = new Directives();
        p.setPermutation(perm);
        p.setCharsPerCell(new int[]{2,1});
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encode: params okay", reason);
        String encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Classic", expectedCipherText, encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Classic", plainText, decoded);
    }

    @Test
    public void testCipherChallenge2014_7B() {
        String plainText = "phasesevenweapproachedthecablejunctionundercoverofnightwithnautilusatanelevationofthreefeettowingseahorsetostarboardcommsinterceptionshowedthatweremainedundetectedandseahorsewasdeployedatoperatingdepththevariouslayersofarmouredprotectionwereremovedfromthecableandasexpectedoncethesteeljacketwasremovedtheotherlayersprovidedlittleresistancethediversenteredthewaterandcutintothecoretoinserttheopticalrepeaterslinkingthembacktothemaninthemiddleunitwhichwaspoweredupandfullytestedinitialtestsshowedthatitwasoperatingasexpectedandthreekeyshavealreadybeenrecoveredfromtheomanitransmissionswithdaylightapproachingtheremainingtestswerepostponedforthefollowingnightandtheshipreturnedtodeeperwaterswhereitremainedatlowdeckheightthediverswereleftatseahorsetodecompressslowlyandwillberecoveredtomorrowoncethefinaltestshavebeenconcluded".toUpperCase();
        String expectedCipherText = "ANWAE CNNDR TWTAN IREOA HSRDN TIEER DCTEW AYERE VAAAR PIOER OBSED ESCRE HLAOI TITHS DTRIN OSEPE PLTHT NIDTW SDULE DLHOT PEATE RHAAR EDEOA IODTA HREGE ROTHW HTSUR ETEEI NWIGI RESET POWIC OOONI TSEUD SEECH ACTEO FIILE ONEWI OTACT EODTE UNEAH SEDAP TRYEM RONMO MLEXO NTKEM EOYVI TSTEE NHANT RERTI EINEO TNDLH POPLY ITEWI TRSED EEVDY CFRMN SNAYP INMTE ENEEI NAHIN EPRIT EDEHV ELEAO RELLL VMOCN AHNCE PHERO HJUUV EHAUT ATHTT ETOAS IPOWW NEEDS ELOPG DESLF EDTER FCAAT EHJAS DTRPR LESEE RETET ECNEO RRSGC KAMII WAEFU TIASH AONGC THSRE NREHT RSTHH ACEIN WTPRL OGHET ODAER ALOEE DEATS OMLDW EDTWE FSBEL EVPDT EONOI GNSAV FTEGS EBOMC EHATI ETNRS PTONT HUSOR ECRED ENDCE TLWAE HESED RNCVE RACUH OIHAL EINAE MEUNH ERDES TTSTA SIPED EYLEE EMTII SIIGO THNTS SFOLN ITRET RWHEM TKHHS WTORC SSNER EROHT EENCS APEBL IRCNT HULEO FENRS ROMRN SHMAD DAODE ATIHI OROUT WEVTH APECE ETOVT ERDLE ADITE WDOTT TTCAT KMBHT HEICW ANTNI SEDWA TXANK EABOV OANMS WLPRG AISPO DFOGN DPEDE SWRDA CTTRE FHDES YABER RETLA VOD".replaceAll("\\W","");
        String key = "1,2,0,4,3";
        Directives p = new Directives();
        int[] perm = Amsco.convertKeywordToColumns(key,0);
        p.setPermutation(perm);
        p.setCharsPerCell(new int[] {2,1});
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
        p.setAlphabet(null);
        String reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: alphabet missing", "Alphabet is empty or too short", reason);
        p.setAlphabet("");
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: alphabet empty", "Alphabet is empty or too short", reason);
        p.setAlphabet("D");
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: alphabet empty", "Alphabet is empty or too short", reason);

        p.setAlphabet(Settings.DEFAULT_ALPHABET);
        p.setPaddingChars(null);
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: missing padding", "Set of padding chars is missing", reason);

        p.setPaddingChars(Settings.DEFAULT_PADDING_CHARS);
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
        p.setPermutation(new int[] {3,1,2,0,5});    // 5 should not be there
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: too big #2", "Permutation element 5 is too large", reason);

        p.setPermutation(new int[] {3,1,2,0,4});
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: too big #2", "Chars per Cell is not valid", reason);

        p.setCharsPerCell(new int[] {1,-1});
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: too big #2", "Chars per Cell element -1 is too small", reason);
        p.setCharsPerCell(new int[] {1,1});
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: too big #2", "Chars per Cell element 1 is repeated", reason);

        p.setCharsPerCell(new int[] {3,1});
        reason = cipher.canParametersBeSet(p); // now all good for encode/decode
        assertNull("BadParam: encode okay", reason);

        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = cipher.canParametersBeSet(p); // Crack: still missing cribs
        assertEquals("BadParam: cribs missing", "Some cribs must be provided", reason);
        p.setCribs("");
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: cribs empty", "Some cribs must be provided", reason);
        p.setCribs("vostok,sputnik,saturn");
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid crack method", reason);
        p.setCrackMethod(CrackMethod.WORD_COUNT);
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid crack method", reason);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertNull("BadParam: crack dict okay", reason);
    }

    @Test
    @Ignore("Not yet implemented Crack")
    public void testCrackBruteSuccess() {
        // attempt Brute Force crack of Permutation cipher looking for cribs in all permutations (up to 9)
        int[] perm = new int[] { 0, 2, 4, 3, 1 };
        String plainText = "Call me Ishmael. Some years ago — never mind how long precisely — having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen, and regulating the circulation. Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people’s hats off — then, I account it high time to get to sea as soon as I can. This is my substitute for pistol and ball. With a philosophical flourish Cato throws himself upon his sword; I quietly take to the ship. There is nothing surprising in this. If they but knew it, almost all men in their degree, some time or other, cherish very nearly the same feelings towards the ocean with me.X".replaceAll("\\W","");
        Directives p = new Directives();
        p.setPermutation(perm);
        p.setCrackMethod(CrackMethod.NONE);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Crack Success: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("Crack Encoding", cipherText);

        // now attempt the crack of the text via brute force
        p.setPermutation(null);
        p.setCribs("ishmael,ocean");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertNull("Crack Success: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = Cipher.numbersToString(result.getDirectives().getPermutation());
        System.out.println("Keyword "+decodeKeyword);
        assertTrue("Crack Success", result.isSuccess());
        assertEquals("Crack Cipher", cipherText, result.getCipherText());
        assertEquals("Crack Text", plainText, result.getPlainText());
        assertEquals("Crack Permutation", "0,2,4,3,1", decodeKeyword);
        assertNotNull("Crack Explain", explain);
        assertEquals("Crack cipher name", "Permutation cipher (0,2,4,3,1:down)", result.getCipher().getInstanceDescription());
    }

    @Test
    @Ignore("Not yet implemented Crack")
    public void testCrackBruteReverseSuccess() {
        // attempt Brute Force crack of Permutation cipher looking for cribs in all permutations (up to 9)
        int[] perm = new int[] { 0, 2, 4, 3, 1 };
        String plainText = "Call me Ishmael. Some years ago — never mind how long precisely — having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen, and regulating the circulation. Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people’s hats off — then, I account it high time to get to sea as soon as I can. This is my substitute for pistol and ball. With a philosophical flourish Cato throws himself upon his sword; I quietly take to the ship. There is nothing surprising in this. If they but knew it, almost all men in their degree, some time or other, cherish very nearly the same feelings towards the ocean with me.X".replaceAll("\\W","");
        Directives p = new Directives();
        p.setPermutation(perm);
        p.setCrackMethod(CrackMethod.NONE);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Crack Success: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("Crack Encoding", cipherText);

        // now attempt the crack of the text via brute force
        p.setPermutation(null);
        p.setCribs("ishmael,ocean");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        p.setConsiderReverse(true);
        reason = cipher.canParametersBeSet(p);
        assertNull("Crack Success: crack param okay", reason);

        String reverseText = new StringBuilder(cipherText).reverse().toString();

        CrackResult result = cipher.crack(reverseText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = Amsco.numbersToString(result.getDirectives().getPermutation());
        System.out.println("Keyword "+decodeKeyword);
        assertTrue("Crack Success", result.isSuccess());
        assertEquals("Crack Cipher", reverseText, result.getCipherText());
        assertEquals("Crack Text", plainText, result.getPlainText());
        assertEquals("Crack Permutation", "0,2,4,3,1", decodeKeyword);
        assertNotNull("Crack Explain", explain);
        assertTrue("Crack Explain", explain.contains("REVERSE"));
        assertEquals("Crack cipher name", "Permutation cipher (0,2,4,3,1:down)", result.getCipher().getInstanceDescription());
    }

    // this one takes around 32 seconds with max column permutations = 9
    @Test
    @Ignore("Not yet implemented Crack")
    public void testCrackBruteFail() {
        // attempt Brute Force crack of Permutation cipher looking for cribs in all permutations (up to 9)
        // but fails as cribs are wrong
        int[] permutation = new int[] {4,2,3,1,0};
        String plainText = "Call me Ishmael. Some years ago — never mind how long precisely — having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen, and regulating the circulation. Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people’s hats off — then, I account it high time to get to sea as soon as I can.";
        Directives p = new Directives();
        p.setPermutation(permutation);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackFail: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackFail Encoding", cipherText);

        // now attempt the crack of the text via Brute Force, will fail die to bad cribs
        p.setPermutation(null);
        p.setCribs("banana,plantation");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertNull("CrackSuccess: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        assertFalse("CrackFail Success", result.isSuccess());
        assertEquals("CrackFail Cipher", cipherText, result.getCipherText());
        assertNull("CrackFail Text", result.getPlainText());
        assertNull("CrackFail Permutation", result.getDirectives());
        assertNotNull("CrackFail Explain", explain);
        assertEquals("CrackFail cipher name", "Permutation cipher (n/a)", result.getCipher().getInstanceDescription());
    }

    @Test
    @Ignore("Not yet implemented Crack")
    public void testCrackDictSuccess() {
        // attempt dictionary crack of Permutation cipher and succeeds with good cribs
        String keyword = "DISCOUNTER";
        String expectedPermutation = "3,0,8,1,6,4,9,2,7,5";
        String plainText = "Mr. and Mrs. Dursley, of number four, Privet Drive, were proud to say that they were perfectly normal, thank you very much. " +
                "They were the last people you'd expect to be involved in anything strange or mysterious, because they just didn't hold with such nonsense.";
        Directives p = new Directives();
        int[] encodePerm = Cipher.convertKeywordToColumns(keyword,0);
        p.setPermutation(encodePerm);
        p.setReadAcross(true);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackDictSuccess: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackDictSuccess: Encoding", cipherText);

        // now attempt the crack of the text via Dictionary
        p.setPermutation(null);
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("privet,drive,normal");
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = cipher.canParametersBeSet(p);
        assertNull("CrackDictSuccess: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        int[] decodePermutation = result.getDirectives().getPermutation();
        System.out.println("Permutation "+Amsco.numbersToString(decodePermutation));
        assertTrue("CrackDict success", result.isSuccess());
        assertEquals("CrackDict Cipher", cipherText, result.getCipherText());
        assertEquals("CrackDict Text", plainText+"XXXXXXXX", result.getPlainText());
        assertEquals("CrackDict Keyword", keyword, decodeKeyword);
        assertEquals("CrackDict Permutation", expectedPermutation, Cipher.numbersToString(decodePermutation));
        assertNotNull("CrackDict Explain", explain);
        assertTrue("CrackDict Explain start", explain.startsWith("Success"));
        assertEquals("CrackDict cipher name", "Permutation cipher (3,0,8,1,6,4,9,2,7,5:across)", result.getCipher().getInstanceDescription());
    }

    @Test
    @Ignore("Not yet implemented Crack")
    public void testCrackDictFail() {
        // attempt dictionary crack of Permutation cipher and fails with bad cribs
        String keyword = "TUMOR";
        String plainText = "Mr. and Mrs. Dursley, of number four, Privet Drive, were proud to say that they were perfectly normal, thank you very much. " +
                "They were the last people you'd expect to be involved in anything strange or mysterious, because they just didn't hold with such nonsense.";
        Directives p = new Directives();
        int[] encodePerm = Cipher.convertKeywordToColumns(keyword,0);
        p.setPermutation(encodePerm);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackDictSuccess: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackDictSuccess: Encoding", cipherText);

        // now attempt the crack of the text via Dictionary
        p.setPermutation(null);
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("banana,republic");   // this is why it fails
        //p.setCribs("dursley");   // with this, it would work
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = cipher.canParametersBeSet(p);
        assertNull("CrackDictSuccess: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        assertFalse("CrackDict success", result.isSuccess());
        assertEquals("CrackDict Cipher Text", cipherText, result.getCipherText());
        assertNull("CrackDict Plain Text", result.getPlainText());
        assertNull("CrackDict Directives", result.getDirectives());
        assertNotNull("CrackDict Explain", explain);
        assertTrue("CrackDict Explain start", explain.startsWith("Fail"));
        assertEquals("CrackDict cipher name", "Permutation cipher (n/a)", result.getCipher().getInstanceDescription());
    }

    @Test
    public void testDescription() {
        String desc = cipher.getCipherDescription();
        assertNotNull("Description", desc);
        assertTrue("Description content", desc.contains("Amsco"));
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setPermutation(new int[] {3,1,2,0});
        p.setCharsPerCell(new int[] {1,3,2});
        String reason = cipher.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = cipher.getInstanceDescription();
        assertNotNull("Instance Description across", desc);
        assertEquals("Instance Description across", "Amsco cipher (3,1,2,0:1,3,2)", desc);
    }
}
