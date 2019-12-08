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
 * Test out the Hill Cipher code
 */
@RunWith(JUnit4.class)
public class HillTest {

    private static final String defaultAlphabet = Settings.DEFAULT_ALPHABET;
    private final Hill cipher = new Hill(null);

    @Test
    public void testMultiplyByVector() {
        int[] matrix = new int[]{1, 4, 19, 1};
        int[] vector = new int[]{2, 3};
        int[] result2 = new int[2];
        int[] expected = new int[]{14,15};
        Hill.multiply(matrix, vector, 26, result2);
        assertEquals("Multiply Vector 2 len", expected.length, result2.length);
        for (int i = 0; i < result2.length; i++) {
            assertEquals("Multiply Vector 2: " + i, expected[i], result2[i]);
        }
        matrix = new int[]{1,2,3,2,1,3,2,3,1};
        vector = new int[]{4,5,6};
        int[] result3 = new int[3];
        expected = new int[]{6,5,3};
        Hill.multiply(matrix, vector, 26, result3);
        assertEquals("Multiply Vector 3 len", expected.length, result3.length);
        for (int i = 0; i < result3.length; i++) {
            assertEquals("Multiply Vector 3: " + i, expected[i], result3[i]);
        }
    }

    @Test
    public void testMultiplyByMatrix() {
        int[] matrix = new int[]{1, 4, 3, 7};
        int[] multiplier = new int[]{2, 3, 6, 4};
        int[] result2 = new int[4];
        int[] expected = new int[]{0, 19, 22, 11};
        Hill.multiply(matrix, multiplier, 26, result2);
        assertEquals("Multiply Vector 2 len", expected.length, result2.length);
        for (int i = 0; i < result2.length; i++) {
            assertEquals("Multiply Vector 2: " + i, expected[i], result2[i]);
        }
        matrix = new int[]{1,2,3,2,1,3,2,3,1};
        multiplier = new int[]{2,0,1,3,2,1,5,1,2};
        int[] result3 = new int[9];
        expected = new int[]{23, 7, 9, 22, 5, 9, 18, 7, 7};
        Hill.multiply(matrix, multiplier, 26, result3);
        assertEquals("Multiply Vector 3 len", expected.length, result3.length);
        for (int i = 0; i < result3.length; i++) {
            assertEquals("Multiply Vector 3: " + i, expected[i], result3[i]);
        }
    }

    @Test
    public void testConvertKeywordToMatrixGood() {
        String keyword = "BETA";
        int[] expected = new int[]{1, 4, 19, 0};
        int[] result = Hill.convertKeywordToMatrix(keyword, defaultAlphabet, true);
        assertEquals("Convert Keyword across Len", expected.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals("Convert Keyword across " + i, expected[i], result[i]);
        }
        // check down and then across
        keyword = "ABACADBAD";
        expected = new int[]{0, 2, 1, 1, 0, 0, 0, 3, 3};
        result = Hill.convertKeywordToMatrix(keyword, defaultAlphabet, false);
        assertEquals("Convert Keyword down Len", expected.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals("Convert Keyword down " + i, expected[i], result[i]);
        }
    }

    @Test
    public void testConvertKeywordToMatrixBad() {
        int[] result = Hill.convertKeywordToMatrix(null, defaultAlphabet, true);
        assertNull("ConvertBad Null", result);
        result = Hill.convertKeywordToMatrix("", defaultAlphabet, true);
        assertNull("ConvertBad Empty", result);
        result = Hill.convertKeywordToMatrix("A1BD0", defaultAlphabet, true);
        assertNull("ConvertBad Non-alpha", result);
    }

    @Test
    public void testEncodeDecodeWiki2x2() {
        // example from https://en.wikipedia.org/wiki/Hill_cipher
        String plainText = "Help";
        int[] matrix = new int [] {3,3,2,5};
        String expectedCipherText = "HIAT";
        Directives p = new Directives();
        p.setPermutation(matrix);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encode: Wiki 2x2 params okay", reason);
        String encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Wiki 2x2", expectedCipherText, encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Wiki 2x2", plainText.toUpperCase(), decoded);
    }

    @Test
    public void testEncodeDecodeExample2x2() {
        // example from https://crypto.interactive-maths.com/hill-cipher.html
        String plainText = "SHORTEXAMPLE";
        int[] matrix = new int [] {7,8,11,11};
        String expectedCipherText = "APADJTFTWLFJ";
        Directives p = new Directives();
        p.setPermutation(matrix);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encode: Example 2x2 params okay", reason);
        String encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Example 2x2", expectedCipherText, encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Example 2x2", plainText.toUpperCase(), decoded);
    }

    @Test
    public void testEncodeDecodeExample3x3() {
        // example from https://crypto.interactive-maths.com/hill-cipher.html
        String plainText = "RETREATNOW";
        int[] matrix = Hill.convertKeywordToMatrix("BACKUPABC", defaultAlphabet, true);
        String expectedCipherText = "DPQRQEVKPQLR";
        Directives p = new Directives();
        p.setPermutation(matrix);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encode: Example 3x3 params okay", reason);
        String encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Example 3x3", expectedCipherText, encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Example 3x3", plainText.toUpperCase()+"XX", decoded);
    }

    @Test
    public void testEncodeDecodeWiki3x3() {
        // example from https://en.wikipedia.org/wiki/Hill_cipher
        String plainText = "ACT";
        int[] matrix = Hill.convertKeywordToMatrix("GYBNqkuRp", defaultAlphabet, true);
        String expectedCipherText = "POH";
        Directives p = new Directives();
        p.setPermutation(matrix);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encode: Wiki 3x3 params okay", reason);
        String encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Wiki 3x3", expectedCipherText, encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Wiki 3x3", plainText.toUpperCase(), decoded);

        // now do the second example
        plainText = "CAT";
        expectedCipherText = "FIN";
        encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Wiki 3x3 (b)", expectedCipherText, encoded);
        decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Wiki 3x3 (b)", plainText.toUpperCase(), decoded);
    }

    @Test
    public void testEncodeDecodeCC2015_7B_3x3() {
        // one day we can crack with crib-sliding
        String plainText = "COMRADESW";
        int[] matrix = new int[] { 12, 5, 11, 13, 7, 2, 4, 3, 10 };
        String expectedCipherText = "SSODTUQOE";
        Directives p = new Directives();
        p.setPermutation(matrix);
        String reason = cipher.canParametersBeSet(p);
        assertNull("Encode: Example 3x3 params okay", reason);
        String encoded = cipher.encode(plainText, p);
        assertEquals("Encoding Example 3x3", expectedCipherText, encoded);
        String decoded = cipher.decode(encoded, p);
        assertEquals("Decoding Example 3x3", plainText.toUpperCase(), decoded);
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
        assertEquals("BadParam: missing perm", "Matrix is not valid", reason);
        p.setPermutation(new int[0]);
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: empty perm", "Matrix is not valid", reason);
        p.setPermutation(new int[] {0,2,1,2,9}); // matrix not square
        reason = cipher.canParametersBeSet(p);
        p.setPermutation(new int[] {0,2,1,2,9,8,3,1,12,23,1,0,5,8,19,15}); // can't deal with 4x4 or bigger yet
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: extra entry", "Cannot invert 4x4 or higher matrices yet", reason);
        p.setPermutation(new int[] {3,1,-2,24});  // negative number
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: too big #1", "Matrix element -2 is negative", reason);

        p.setPermutation(new int[] {23,1,12,0,0,1,18,3,29});  // 29 is too large
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: too big #2", "Matrix element 29 is too large", reason);

        p.setPermutation(new int[] {2, 0, 0, 0});
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: not co-prime", "Determinant is 0, matrix is singular and cannot decode", reason);

        p.setPermutation(new int[] {1, 0, 0, 13});
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: not co-prime", "Matrix determinant is 13, cannot decode uniquely", reason);

        p.setPermutation(new int[] {2,3,1,0});
        reason = cipher.canParametersBeSet(p); // now all good for encode/decode
        assertNull("BadParam: encode okay", reason);

        p.setCrackMethod(CrackMethod.IOC);
        reason = cipher.canParametersBeSet(p); // Crack: still missing cribs
        assertEquals("BadParam: cribs missing", "Some cribs must be provided", reason);
        p.setCribs("");
        reason = cipher.canParametersBeSet(p);
        assertEquals("BadParam: cribs empty", "Some cribs must be provided", reason);
        p.setCribs("vostok,sputnik,saturn");
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid crack method", reason);
        p.setCrackMethod(CrackMethod.CRIB_DRAG);
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid Rows and Cols: 0", reason);
        p.setNumberSize(11);
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid Rows and Cols: 11", reason);
        p.setNumberSize(66);
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid Rows and Cols: 66", reason);
        p.setNumberSize(33);
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Crib to drag is missing", reason);
        p.setCribsToDrag("hop");
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Crib to drag is too short", reason);
        p.setCribsToDrag("hello to the world out there, this is just no good, is it");
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Crib to drag is too long", reason);
        p.setCribsToDrag("hello9cribx");
        reason = cipher.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Non-letter (9) in crib to drag", reason);
        p.setCribsToDrag("hellocribxy");
        reason = cipher.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);
        p.setNumberSize(55);
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = cipher.canParametersBeSet(p);
        assertNull("BadParam: crack dict okay", reason);
    }

    // this one can take around 19 seconds but have seen it take 1 min 12 secs to find 7,8,11,11
    @Test
    public void testCrackBruteForceCipherChallenge() {
        String cipherText = "IBWUQ RCIHA QVILU DSARR EAVXD DQCIF XZPLX ZLDFX CIJOD RENUL GKULV NVHJO CSLUC IBDEX MPDRV HJBIX HAMWE AVXEX RXFJJ OEXCS LUHOQ ZGEYW TFKSY EAZEK JTJVF XUJSF SNNPC IALHA MWGFP FJBAZ RBZJY KLZND EQVUQ JHBJK XDCDD RJGCF SVQIA NJXKO MCFJD SQWOH QZDRP JNMIX KZBZF BGVIX IBWOO QWBDB QCIJM XCFOP IXNDC FILFJ LZRBL TQHUU AUMJX GADHA QVQWZ CXZVD SWTFS MQISV IOYTU YZCQW KIKDM EZCQW EQJBU CKZAD OWZXS RKHHA EXEQJ BILYK DBJIH AIOKQ HAIXU CKZIQ CFEXU CQRVX CYMUJ OGGKZ BZDFR LOBFJ DSIFD RKPOG BDCRO CVLNM IXLZS RKHMU KPSOO CXPVH VLXGU GEKVS MJYGP FCDMJ LBJBM UHAWB UCQRW UFOAP YHNDC FWBDB QCAZW BVHYV ANRIX ZNFYK EAVXS MKGEX QOLUH OQICO AZBZY HALYI WQESM ABJZX AZRPO QSBGF WBUUQ IWQWG PIMAR XFJJO EXCSL UHOQZ GESRM JLBJB MUHAW BUCEU IXCSL BXPLX VDGZE CYIKG FXLGL BXPVH IAVXY HULHA MWXZV HVKAN WGNJW BJTDV EOHAW BUCAI YKXYC SIAJV NDRBK ZVHLU HAMWL AANTF RICJP FLUUC KMWQB ZCILG IXYSO QKRVX YHCAU LDHDJ AZIBH AOCDV EQJII ALUSO SBPLU LANLQ IXACO QMINN LUHAS AHAVX WBVNJ IAZMU WOMKH VALLC CSDIA DUCGN DRMUO AZJIB HAIKL LSIPL ULIJY HKSDO QICIP POQSR OBNUV XAPEX QGSML UANHR WUUDV UQZND GFHAY TFXGA TAHAW BUCCO AYTAA CXGIX HAQIS CQVWB UUUCX EQDKH WAOQL ZVNLU UCSXQ VADYT DRCAY KXYNP NUOBV SDRKP HAVXW BANHR JGMXM JFJRI XZDOA YTALZ HAQVX DJOEX HAGRK HKQXG EXHRJ BQWDD YTECY KZWKU WBMIN NVKQH EXQOZ CDRVN MJZGI ONTDB LBEXJ TVKQM WBAZO PIXCA ULMMM IYCGC ZXWUU DJKEX DSGNA LYIEO TFIBH ACFQR CIWQR TIOZC CWPLV XRIIO HBCGC HIOYT MUULH ASBWB TAIBE XHRJB UCYCD RVXOW JIAZU CAIPF APEXC YJOQZ DRJTJ OOWVK YOJBS CADDB JIGMY KLUAZ WUWLA NMINN LUXZR IVXWB AZUCY PADAP KUSGL ULXPS GUYKA YVLVX ZGIXL ACSSR IODBY OBDSA PLJBH AWBHA IXWAI XKQHA IXQYF XTFAZ AZUCQ RDRPA ANJXD BMCIX AZIBV NJOQZ DRRBW TSAEQ JBOPI XIFJJ JBLZW QXPUC QRMUT FXPZX JOXZT XXZIO HAUUO BALGF CNEPU GEYSB DBYNI OIKSW ULJGC WTLGV UUCFR RANPP DRZXA POGAZ HAUUQ GXPXD NNUBA ZCFPL KRSRD RIFEP NJYTX ZVDCF GCTXJ JJBHA AZQIU BUTPL ULIBR XFJJO EXCSL UHOQZ GEAPY HNDCF EXHAC FOCUN PJQRF XSFSR OBDDB DQRWA FXLZJ GYCPL CIGFS VAZRB ZJYKK RULOC TFXPZ JILAN DFTFD SWWXY EXCYW UUDJK EXVHL UHAVX AZKEG HWBIX SNYKL UUCSX ANVHW GVHLU KZWUB DAGYK IANNJ DHHFX YCOCJ ODRGG SFSRQ ZDREO GNFXF EDRLB TFUCO LYHND CFHLF YUPQR TFDSI FDRQO WQLUA LJJPT UCOLY HNDCF BMGAW GDSEG IFADM UJOIX MZIXX ZPZHA MWEAV XAPOG EXQOA FWMMU XDPVW LWBHD RBZJY KDVYP PVBPJ BLXPL XJOQO AVXLB YYJVL ZXZXP NDCFS CYTFX RIKHV XXPJO UCQRQ IOWHB IJGIS RAZUC SKJOM AAUFX UDBZD VANPP KRYYJ KSNJT CFSMF JEBQW OHQZD RVNMJ ZGION TDBTZ DRVXO BQVNF YKEXH ASOGF KQFED RRZDV UUSAE BBPZC MKNNL UFJFE LZWUB QXDJO HASOU LMZKP WBADW DQZSW DBYIV XIBHA QVUDX YSBHA IBHAO CLGVX QWDRZ J";
        String expectedPlainText = "ofcoursethefabulistswereluckythatihadchosenthewrongconfidanteandsensingthedangertheywereinvalentineandproteusknewimmediatelyhowtodiffuseittheyarrangedforyoutobekidnappedfromthetrenchesandsmuggledbacktotheborderundercoverofagasattackihaveneverbeenabletoforgivemyselfforthefactthatmyenemieschosetoattackmebyattackingyounorcanistopthinkingaboutallthoseotheryoungmeninyourregimentwoundedorkilledbytheirownsideinordertostopmeireceivedanofficialtelegramtellingmethatyourcorpshadbeenattackedatdawnandthatyouweremissingandpresumeddeaditwasaccompaniedbyaseparatemessagefromvalentineandproteustellingmethatyouwerealivebutmyjoywasshortlivedasireadontheyhadarrangedmatterssothatyoucouldeasilybefoundandtheyplannedtobrandyouasadeserteriwasalreadyconvincedoftheirskillsindeceptionandpersuasionandthisthreatfilledmeagainwithfearforyouthememoryoftheexecutionihadwitnessedwasstillfreshinmymindandicouldnotbearthethoughtthatyoumightsufferthesamefatemyonlyhopewastofindyoubeforetheycouldfulfilltheirthreatanditravelledthatnighttothefrontinthehopeoffindingacluetoyourlocationarrivingatthefieldhospitaliinterrogatedeveryconsciousmanicouldfindbutitwasoneofthenursesalmosttootiredtospeakwhosetmeonthepathtofindingyousherecalledyoucrashingintothetentcarryingamortallywoundedcompanionandhadtreatedyouforshockandburnsyouignoredherpleastostayinsistingthattherewereotherswhoneededyourhelpandstaggeredoffintothefogriskingeverythingtosaveyourmeneveninthatchaosthemilitarypolicekeptacloseeyeontroopmovementsandwhenishowedthemmyveronacredentialstheytoldmethatmenmatchingthedescriptionofvalentineandproteushadbeenintheneighbourhoodstillunsurewhototrustisearchedforyoualoneineveryabandonedbuildingicouldfindandthreedayslaterifoundyoubandagedandunconsciousinafarmhouseinthewoodstothesouthofthelineyouhadbeenbadlyburnedbythegasandithinkyouhadbeendruggedbymytormentersperhapstheywereshowingaglimmerofempathyforyoursufferingbutitwasmorelikelytohavebeenamethodtopreventyourescapeihoistedyouontomyshouldersandwalkedfifteenmilesbacktothefieldhospitalwhereileftyouinthecareofthenursemissbrittainandlefttoconfronttheconspiratorsnotyetawareofthefulldepthoftheirtreachery";
        Directives p = new Directives();
        p.setPermutation(null);
        p.setNumberSize(22);
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("treachery,fabulists");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackBruteCCSuccess: crack param okay", reason);
        CrackResult result = cipher.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        int[] decodeMatrix = result.getDirectives().getPermutation();
        System.out.println("Matrix "+Hill.matrixToString(decodeMatrix));
        assertTrue("CrackBrute success", result.isSuccess());
        assertEquals("CrackBrute Cipher", cipherText, result.getCipherText());
        assertEquals("CrackBrute Text", expectedPlainText.toUpperCase(), result.getPlainText());
        assertNull("CrackBrute Keyword", decodeKeyword);
        assertEquals("CrackBrute Matrix", "7,8,11,11", Hill.matrixToString(decodeMatrix));
        assertNotNull("CrackBrute Explain", explain);
        assertTrue("CrackBrute Explain start", explain.startsWith("Success"));
        assertEquals("CrackBrute cipher name", "Hill cipher (7,8,11,11)", result.getCipher().getInstanceDescription());
    }

    @Test
    public void testCrackBruteForceSuccess() {
        // attempt dictionary crack of Hill cipher and succeeds with good cribs
        int[] matrixToUse = new int [] {21,9,2,17};
        String plainText = "No ands, ors or buts in the text";
        Directives p = new Directives();
        p.setPermutation(matrixToUse);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackBruteSuccess: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackBruteSuccess: Encoding", cipherText);

        // now attempt the crack of the text via Dictionary
        p.setPermutation(null);
        p.setNumberSize(22);
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("and,the,but");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertNull("CrackBruteSuccess: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        int[] decodeMatrix = result.getDirectives().getPermutation();
        System.out.println("Matrix "+Hill.matrixToString(decodeMatrix));
        assertTrue("CrackBrute success", result.isSuccess());
        assertEquals("CrackBrute Cipher", cipherText, result.getCipherText());
        assertEquals("CrackBrute Text", plainText.replaceAll("\\W","").toUpperCase(), result.getPlainText());
        assertNull("CrackBrute Keyword", decodeKeyword);
        assertEquals("CrackBrute Matrix", Hill.matrixToString(matrixToUse), Hill.matrixToString(decodeMatrix));
        assertNotNull("CrackBrute Explain", explain);
        assertTrue("CrackBrute Explain start", explain.startsWith("Success"));
        assertEquals("CrackBrute cipher name", "Hill cipher (21,9,2,17)", result.getCipher().getInstanceDescription());
    }

    @Test
    @Ignore("Takes ages to run, and does not find the solution")
    public void testCrackBruteForce3x3Success() {
        // attempt dictionary crack of Hill cipher and succeeds with good cribs
        int[] matrixToUse = new int [] {3,10,20,20,9,17,9,4,17};
        String plainText = "No ands, ors or buts in the text";
        Directives p = new Directives();
        p.setPermutation(matrixToUse);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackBruteSuccess: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackBruteSuccess: Encoding", cipherText);
        String decodeText = cipher.decode(cipherText, p);
        assertNotNull("CrackBruteSuccess: Decoding", decodeText);

        // now attempt the crack of the text via Dictionary
        p.setPermutation(null);
        p.setNumberSize(33);
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("and,the,but");
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = cipher.canParametersBeSet(p);
        assertNull("CrackBruteSuccess: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        int[] decodeMatrix = result.getDirectives().getPermutation();
        System.out.println("Matrix "+Hill.matrixToString(decodeMatrix));
        assertTrue("CrackBrute success", result.isSuccess());
        assertEquals("CrackBrute Cipher", cipherText, result.getCipherText());
        assertEquals("CrackBrute Text", plainText.replaceAll("\\W","").toUpperCase(), result.getPlainText());
        assertNull("CrackBrute Keyword", decodeKeyword);
        assertEquals("CrackBrute Matrix", Hill.matrixToString(matrixToUse), Hill.matrixToString(decodeMatrix));
        assertNotNull("CrackBrute Explain", explain);
        assertTrue("CrackBrute Explain start", explain.startsWith("Success"));
        assertEquals("CrackBrute cipher name", "Hill cipher (3,10,20,20,9,17,9,4,17)", result.getCipher().getInstanceDescription());
    }

    @Test
    public void testCrackCribDrag2x2Success() {
        // attempt crib drag crack of Hill cipher and succeeds with good cribs
        int[] matrixToUse = new int [] {7,8,11,11};  // HILL
        String keywordToUse = "HILL";
        String plainText = "We have been persuaded by some that are careful of our safety to take heed how we commit ourselves to armed multitudes, for fear of treachery. But I assure you, I do not desire to live to distrust my faithful and loving people.\n" +
                "Let tyrants fear. I have always so behaved myself that, under God, I have placed my chiefest strength and safeguard in the loyal hearts and good-will of my subjects; and therefore I am come amongst you, as you see, at this time, not for my recreation and disport, but being resolved, in the midst and heat of the battle, to live and die amongst you all; to lay down for my God, and for my kingdom, and my people, my honour and my blood, even in the dust.\n" +
                "I know I have the body of a weak and feeble woman; but I have the heart and stomach of a king, and of a king of England too, and think foul scorn that Parma or Spain, or any prince of Europe, should dare to invade the borders of my realm: to which rather than any dishonour shall grow by me, I myself will take up arms, I myself will be your general, judge, and rewarder of every one of your virtues in the field.\n" +
                "I know already, for your forwardness you have deserved rewards and crowns; and We do assure you on a word of a prince, they shall be duly paid. In the mean time, my lieutenant general shall be in my stead, than whom never prince commanded a more noble or worthy subject; not doubting but by your obedience to my general, by your concord in the camp, and your valour in the field, we shall shortly have a famous victory over these enemies of my God, of my kingdom, and of my people.";
        Directives p = new Directives();
        p.setPermutation(matrixToUse);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackDragSuccess: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackDragSuccess: Encoding", cipherText);
        String decodeText = cipher.decode(cipherText, p);
        assertNotNull("CrackDragSuccess: Decoding", decodeText);

        // now attempt the crack of the text via Dictionary
        p.setPermutation(null);
        p.setNumberSize(22);
        p.setCribsToDrag("weakandfeeblewoman");
        //p.setHeading("BEENPURSUADED");
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("heart,stomach,king");
        p.setCrackMethod(CrackMethod.CRIB_DRAG);
        reason = cipher.canParametersBeSet(p);
        assertNull("CrackDragSuccess: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        int[] decodeMatrix = result.getDirectives().getPermutation();
        System.out.println("Matrix "+Hill.matrixToString(decodeMatrix));
        assertTrue("CrackDrag success", result.isSuccess());
        assertEquals("CrackDrag Cipher", cipherText, result.getCipherText());
        assertEquals("CrackDrag Text", plainText.replaceAll("\\W","").toUpperCase(), result.getPlainText());
        assertEquals("CrackDrag Keyword", keywordToUse, decodeKeyword);
        assertEquals("CrackDrag Matrix", Hill.matrixToString(matrixToUse), Hill.matrixToString(decodeMatrix));
        assertNotNull("CrackDrag Explain", explain);
        assertTrue("CrackDrag Explain start", explain.startsWith("Success"));
        assertEquals("CrackDrag cipher name", "Hill cipher (7,8,11,11)", result.getCipher().getInstanceDescription());
    }

    @Test
    public void testCrackCribDrag3x3Success() {
        // attempt crib drag crack of Hill cipher and succeeds with good cribs
        int[] matrixToUse = new int [] {3,10,20,20,9,17,9,4,17};  // HILL
        String keywordToUse = "DKUUJRJER";
        String plainText = "We have been persuaded by some that are careful of our safety to take heed how we commit ourselves to armed multitudes, for fear of treachery. But I assure you, I do not desire to live to distrust my faithful and loving people.\n" +
                "Let tyrants fear. I have always so behaved myself that, under God, I have placed my chiefest strength and safeguard in the loyal hearts and good-will of my subjects; and therefore I am come amongst you, as you see, at this time, not for my recreation and disport, but being resolved, in the midst and heat of the battle, to live and die amongst you all; to lay down for my God, and for my kingdom, and my people, my honour and my blood, even in the dust.\n" +
                "I know I have the body of a weak and feeble woman; but I have the heart and stomach of a king, and of a king of England too, and think foul scorn that Parma or Spain, or any prince of Europe, should dare to invade the borders of my realm: to which rather than any dishonour shall grow by me, I myself will take up arms, I myself will be your general, judge, and rewarder of every one of your virtues in the field.\n" +
                "I know already, for your forwardness you have deserved rewards and crowns; and We do assure you on a word of a prince, they shall be duly paid. In the mean time, my lieutenant general shall be in my stead, than whom never prince commanded a more noble or worthy subject; not doubting but by your obedience to my general, by your concord in the camp, and your valour in the field, we shall shortly have a famous victory over these enemies of my God, of my kingdom, and of my people.";
        Directives p = new Directives();
        p.setPermutation(matrixToUse);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackDragSuccess: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackDragSuccess: Encoding", cipherText);
        String decodeText = cipher.decode(cipherText, p);
        assertNotNull("CrackDragSuccess: Decoding", decodeText);

        // now attempt the crack of the text via Dictionary
        p.setPermutation(null);
        p.setNumberSize(33);
        p.setCribsToDrag("weakandfeeblewoman");
        //p.setHeading("BEENPURSUADED");
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("heart,stomach,king");
        p.setCrackMethod(CrackMethod.CRIB_DRAG);
        reason = cipher.canParametersBeSet(p);
        assertNull("CrackDragSuccess: crack param okay", reason);

        CrackResult result = cipher.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        int[] decodeMatrix = result.getDirectives().getPermutation();
        System.out.println("Matrix "+Hill.matrixToString(decodeMatrix));
        assertTrue("CrackDrag success", result.isSuccess());
        assertEquals("CrackDrag Cipher", cipherText, result.getCipherText());
        assertEquals("CrackDrag Text", plainText.replaceAll("\\W","").toUpperCase()+"X", result.getPlainText());
        assertEquals("CrackDrag Keyword", keywordToUse, decodeKeyword);
        assertEquals("CrackDrag Matrix", Hill.matrixToString(matrixToUse), Hill.matrixToString(decodeMatrix));
        assertNotNull("CrackDrag Explain", explain);
        assertTrue("CrackDrag Explain start", explain.startsWith("Success"));
        assertEquals("CrackDrag cipher name", "Hill cipher (3,10,20,20,9,17,9,4,17)", result.getCipher().getInstanceDescription());
    }

    @Test
    public void testCrackDictSuccess() {
        // attempt dictionary crack of Hill cipher and succeeds with good cribs
        String keyword = "HILL";
        String expectedMatrix = "7,8,11,11";
        String plainText = "Mr. and Mrs. Dursley, of number four, Privet Drive, were proud to say that they were perfectly normal, thank you very much. " +
                "They were the last people you'd expect to be involved in anything strange or mysterious, because they just didn't hold with such nonsense.";
        Directives p = new Directives();
        int[] encodeMatrix = Hill.convertKeywordToMatrix(keyword, defaultAlphabet, true);
        p.setPermutation(encodeMatrix);
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
        int[] decodeMatrix = result.getDirectives().getPermutation();
        System.out.println("Matrix "+Hill.matrixToString(decodeMatrix));
        assertTrue("CrackDict success", result.isSuccess());
        assertEquals("CrackDict Cipher", cipherText, result.getCipherText());
        assertEquals("CrackDict Text", plainText.replaceAll("\\W","").toUpperCase()+"X", result.getPlainText());
        assertEquals("CrackDict Keyword", keyword, decodeKeyword);
        assertEquals("CrackDict Matrix", expectedMatrix, Hill.matrixToString(decodeMatrix));
        assertNotNull("CrackDict Explain", explain);
        assertTrue("CrackDict Explain start", explain.startsWith("Success"));
        assertEquals("CrackDict cipher name", "Hill cipher (7,8,11,11)", result.getCipher().getInstanceDescription());
    }

    @Test
    public void testCrackDictFail() {
        // attempt dictionary crack of Hill cipher and fails with bad cribs
        String keyword = "HILL";
        String plainText = "Mr. and Mrs. Dursley, of number four, Privet Drive, were proud to say that they were perfectly normal, thank you very much. " +
                "They were the last people you'd expect to be involved in anything strange or mysterious, because they just didn't hold with such nonsense.";
        Directives p = new Directives();
        int[] encodeMatrix = Hill.convertKeywordToMatrix(keyword, defaultAlphabet, true);
        p.setPermutation(encodeMatrix);
        String reason = cipher.canParametersBeSet(p);
        assertNull("CrackDictSuccess: encode param okay", reason);
        String cipherText = cipher.encode(plainText, p);
        assertNotNull("CrackDictSuccess: Encoding", cipherText);

        // now attempt the crack of the text via Dictionary
        p.setPermutation(null);
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("privet,drive,banana"); // wrong cribs
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
        assertEquals("CrackDict cipher name", "Hill cipher (n/a)", result.getCipher().getInstanceDescription());
    }

    @Test
    public void testInvertMatrix3x3() {
        // from: https://unacademy.com/lesson/hill-cipher-example-of-3x3-matrices-decryption-part/V4WFRDBV
        int[] matrix = new int[] {2, 8, 15, 7, 4, 17, 8, 13, 6 };
        int[] inverse = new int[9];
        boolean canInvert = Hill.invertMatrix(matrix, 26, inverse, true);
        assertTrue("Invert Matrix 3x3 success", canInvert);
        assertEquals("Invert 3x3 0", 3, inverse[0]);
        assertEquals("Invert 3x3 1", 7, inverse[1]);
        assertEquals("Invert 3x3 2", 16, inverse[2]);
        assertEquals("Invert 3x3 3", 2, inverse[3]);
        assertEquals("Invert 3x3 4", 6, inverse[4]);
        assertEquals("Invert 3x3 5", 17, inverse[5]);
        assertEquals("Invert 3x3 6", 9, inverse[6]);
        assertEquals("Invert 3x3 7", 8, inverse[7]);
        assertEquals("Invert 3x3 8", 20, inverse[8]);
    }

    @Test
    public void testDescription() {
        String desc = cipher.getCipherDescription();
        assertNotNull("Description", desc);
        assertTrue("Description content", desc.contains("Hill"));
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setPermutation(new int[] {1,13,7,10});
        String reason = cipher.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = cipher.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Hill cipher (1,13,7,10)", desc);
    }
}
