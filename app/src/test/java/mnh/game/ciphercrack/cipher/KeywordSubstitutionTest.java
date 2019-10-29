package mnh.game.ciphercrack.cipher;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import mnh.game.ciphercrack.language.Dictionary;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.KeywordExtend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test out the Keyword Substitution Cipher code
 */
@RunWith(JUnit4.class)
public class KeywordSubstitutionTest {

    private static final String defaultAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Language defaultLanguage = Language.instanceOf("English");

    private final KeywordSubstitution keySub = new KeywordSubstitution(null);

    @Test
    public void testApplyKeywordExtend() {
        String fullKeyword = KeywordSubstitution.applyKeywordExtend(KeywordExtend.EXTEND_LAST, "BOOST", defaultAlphabet);
        assertEquals("ExtendLast", "BOSTUVWXYZACDEFGHIJKLMNPQR", fullKeyword);
    }

    @Test
    public void testZebras() {
        // encode and then decode a mixed case cipher
        //          --    ABCDEFGHIJKLMNOPQRSTUVWXYZ
        String keyword = "ZEBRASCDFGHIJKLMNOPQTUVWXY";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        String reason = keySub.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String encoded = keySub.encode("flee at once. we are discovered!", p);
        assertEquals("Encoding ZEBRAS", "siaa zq lkba. va zoa rfpbluaoar!", encoded);
        String decoded = keySub.decode(encoded, p);
        assertEquals("Decoding ZEBRAS", "flee at once. we are discovered!", decoded);
    }

    @Test
    @Ignore("Word Count Crack takes 90+ seconds")
    public void testSubstitutionCrackWordCount() {
        // encode and then crack a large piece of text
        //                abcdefghijklmnopqrstuvwxyz
        //                SIBGTREHWQMZPCNAFVDJLYKXOU <= start
        //                *   *   *  * *  *  *     * = 7 !!!
        String keyword = "SALVTIONWXYZBCDEFGHJKMPQRU";
        //                SFBGTOEDWQYZPCNAIVHJUMKXRL
        //                *   *   * ** *    ** *  *  = 10 w/probability (8x1000)
        //                SIBGTREHWQMZPCDAFLNJOYKXVU
        //                *   *   *  * ** *  *     * = 9 [3x3000]
        //                SIBVTREKWQMZPCNAFGDJLYHXOU
        //                *  **   *  * *  ** *     * = 9 [2x3000]

        // National Cipher Challeng 4B 2013
        String plainText = "iwasnearlycaughtlastnighthecametocheckonmeandijusthadtimetoturnthecanvasagainstthewallintheshadowsiamnotsurewhathesawbutikeptmyhandsbehindmybacktohidetheredpigmentihadbeenworkingandhesaidnothingheseemstohavesoftenedbutiknowicannottrustanyonehereshemustnotbediscoveredhereortheywilltakeawaymylasthopeofrescuingherandreturninghertotheghettomyplanstoescapeareoflessimportancebutmytraveldocumentsarenowcompletethepapersandinkswerehardtoacquirebutiexcusedmuchofitbyexplainingthatineededtosketchtosharpenmyskillswhichhaddeterioratedinthecampiamsavingsuchfoodasmightlastmostlyhardbreadandhardcheeseagainstthedayswhenihopetorunfromthisplacenowiwillneedtofindawaytostealmoneytopayformyjourneymyplacehighintheatticgivesmeaviewofthecitywhichhasallowedmetomakeamaptoguidemeonthemoonlessnightwheniwillfinallyrunandikeepthemapwithherandwiththisdiaryundertheboardsiwillliveandiwillbefreeandsowillshe";
        String cipherText = "WPSHC TSGZR LSKON JZSHJ CWONJ NTLSB TJDLN TLYDC BTSCV WXKHJ NSVJW BTJDJ KGCJN TLSCM SHSOS WCHJJ NTPSZ ZWCJN THNSV DPHWS BCDJH KGTPN SJNTH SPAKJ WYTEJ BRNSC VHATN WCVBR ASLYJ DNWVT JNTGT VEWOB TCJWN SVATT CPDGY WCOSC VNTHS WVCDJ NWCON THTTB HJDNS MTHDI JTCTV AKJWY CDPWL SCCDJ JGKHJ SCRDC TNTGT HNTBK HJCDJ ATVWH LDMTG TVNTG TDGJN TRPWZ ZJSYT SPSRB RZSHJ NDETD IGTHL KWCON TGSCV GTJKG CWCON TGJDJ NTONT JJDBR EZSCH JDTHL SETSG TDIZT HHWBE DGJSC LTAKJ BRJGS MTZVD LKBTC JHSGT CDPLD BEZTJ TJNTE SETGH SCVWC YHPTG TNSGV JDSLF KWGTA KJWTQ LKHTV BKLND IWJAR TQEZS WCWCO JNSJW CTTVT VJDHY TJLNJ DHNSG ETCBR HYWZZ HPNWL NNSVV TJTGW DGSJT VWCJN TLSBE WSBHS MWCOH KLNID DVSHB WONJZ SHJBD HJZRN SGVAG TSVSC VNSGV LNTTH TSOSW CHJJN TVSRH PNTCW NDETJ DGKCI GDBJN WHEZS LTCDP WPWZZ CTTVJ DIWCV SPSRJ DHJTS ZBDCT RJDES RIDGB RXDKG CTRBR EZSLT NWONW CJNTS JJWLO WMTHB TSMWT PDIJN TLWJR PNWLN NSHSZ ZDPTV BTJDB SYTSB SEJDO KWVTB TDCJN TBDDC ZTHHC WONJP NTCWP WZZIW CSZZR GKCSC VWYTT EJNTB SEPWJ NNTGS CVPWJ NJNWH VWSGR KCVTG JNTAD SGVHW PWZZZ WMTSC VWPWZ ZATIG TTSCV HDPWZ ZHNT".replaceAll("\\W","");

        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        p.setLanguage(defaultLanguage);
        String reason = keySub.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String encoded = keySub.encode(plainText, p);
        assertNotNull("Encoding text null", encoded);
        assertEquals("Encoding text value", cipherText, encoded.toUpperCase());

        // do this to load the English dictionary
        keySub.canParametersBeSet(p);
        Dictionary dict = defaultLanguage.getDictionary();
        assertNotNull("Load dictionary", dict);

        // now crack it
        Directives cp = new Directives();
        cp.setLanguage(defaultLanguage);
        cp.setAlphabet(defaultAlphabet);
        cp.setCribs("caught,allowed,diary");
        cp.setCrackMethod(CrackMethod.WORD_COUNT);
        reason = keySub.canParametersBeSet(cp);
        assertNull("Null reason", reason);
        CrackResult result = keySub.crack(cipherText, cp);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        assertTrue("WordCountCrack Explain Status", result.isSuccess());
        assertTrue("WordCountCrack Explain Success", explain.startsWith("Success"));
        assertEquals("WordCountCrack Text", plainText, result.getPlainText().toLowerCase());
        assertEquals("WordCountCrack CipherText", cipherText, result.getCipherText());
        assertEquals("WordCountCrack Keyword", keyword, result.getDirectives().getKeyword());
        assertNotNull("WordCountCrack Explain", explain);
    }

    @Test
    @Ignore("Word Count Crack2 takes 200+ seconds")
    public void testSubstitutionCrackWordCount2() {
        // encode and then crack a large piece of text
        String keyword = "ALFREDOSTUVWXYZBCGHIJKMNPQ";
        String keywordAlt = "ALFREDOSTCVWXYZBUGHIJKMNPQ";

        // National Cipher Challeng 5A 2013
        String plainText = "philthisisthelastofsarahsdiaryentriesanditisatoughonetodecryptihaventhadtodecryptavigenereforawhileandialmostdidntgetthisoneatleastittellsusthatshegotawaybutbeyondthatiamnotsurewhathappenedwheredidshegodidthepaintinggowithherandifitdidhowcomeitisnowbackinthelouvrediddanielorthessofficerfollowherwhowashedidhefindherthetrailisgoingcoldandweneedanewideaiwaswonderingiftheremightbesomethinginthessanglethoseguyswerenothingifnotthoroughdoyouhaveaccesstoanysspapersfromthatpartofparisinthewarmaybetheyknewwhatwasgoingoniamheadingdowntotalktoalfredogerihewasthedealerperuggiashowedthemonalisatobackinmaybeheknowssomethingaboutsarasfamilyitsalongshotbutunlessyoucomeupwithsomethingfromthenaziwarrecorditisallwehaveanynewsonthexrayharry";
        String cipherText = "BSTWI STHTH ISEWA HIZDH AGASH RTAGP EYIGT EHAYR TITHA IZJOS ZYEIZ REFGP BITSA KEYIS ARIZR EFGPB IAKTO EYEGE DZGAM STWEA YRTAW XZHIR TRYIO EIIST HZYEA IWEAH ITIIE WWHJH ISAIH SEOZI AMAPL JILEP ZYRIS AITAX YZIHJ GEMSA ISABB EYERM SEGER TRHSE OZRTR ISEBA TYITY OOZMT ISSEG AYRTD TIRTR SZMFZ XETIT HYZML AFVTY ISEWZ JKGER TRRAY TEWZG ISEHH ZDDTF EGDZW WZMSE GMSZM AHSER TRSED TYRSE GISEI GATWT HOZTY OFZWR AYRME YEERA YEMTR EATMA HMZYR EGTYO TDISE GEXTO SILEH ZXEIS TYOTY ISEHH AYOWE ISZHE OJPHM EGEYZ ISTYO TDYZI ISZGZ JOSRZ PZJSA KEAFF EHHIZ AYPHH BABEG HDGZX ISAIB AGIZD BAGTH TYISE MAGXA PLEIS EPVYE MMSAI MAHOZ TYOZY TAXSE ARTYO RZMYI ZIAWV IZAWD GERZO EGTSE MAHIS EREAW EGBEG JOOTA HSZME RISEX ZYAWT HAIZL AFVTY XAPLE SEVYZ MHHZX EISTY OALZJ IHAGA HDAXT WPTIH AWZYO HSZIL JIJYW EHHPZ JFZXE JBMTI SHZXE ISTYO DGZXI SEYAQ TMAGG EFZGR TITHA WWMES AKEAY PYEMH ZYISE NGAPS AGGP".replaceAll("\\W", "");

        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        p.setLanguage(defaultLanguage);

        // do this to load the English dictionary
        String reason = keySub.canParametersBeSet(p);
        assertNull("ParametersSet", reason);
        Dictionary dict = defaultLanguage.getDictionary();
        assertNotNull("Load dictionary", dict);

        // now crack it
        Directives cp = new Directives();
        cp.setLanguage(defaultLanguage);
        cp.setAlphabet(defaultAlphabet);
        cp.setCrackMethod(CrackMethod.WORD_COUNT);
        cp.setCribs("paris,xray,painting");
        reason = keySub.canParametersBeSet(cp);
        assertNull("Null reason", reason);
        CrackResult result = keySub.crack(cipherText, cp);
        System.out.println("Decoded " + result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain " + explain);
        Directives outDirs = result.getDirectives();
        String decodeKeyword;
        if (outDirs != null) {
             decodeKeyword = outDirs.getKeyword();
            System.out.println("Keyword: " + decodeKeyword);
        } else {
            System.out.println("Keyword not available as crack failed");
            decodeKeyword = "<n/a>";
        }
        assertNotNull("WordCountCrack Explain", explain);
        assertTrue("WordCountCrack Explain Success", explain.startsWith("Success"));
        assertTrue("WordCountCrack Explain Status", result.isSuccess());
        assertEquals("WordCountCrack Text", plainText, result.getPlainText().toLowerCase());
        assertEquals("WordCountCrack Text", cipherText, result.getCipherText());
        assertTrue("WordCountCrack Keyword", decodeKeyword.equals(keyword) || decodeKeyword.equals(keywordAlt));
    }

    @Test
    public void testSubstitutionCrackDict() {
        // encode and then crack a large piece of text
        String keyword = "SALVTIONWXYZBCDEFGHJKMPQRU";

        // National Cipher Challeng 4B 2013
        String plainText = "iwasnearlycaughtlastnighthecametocheckonmeandijusthadtimetoturnthecanvasagainstthewallintheshadowsiamnotsurewhathesawbutikeptmyhandsbehindmybacktohidetheredpigmentihadbeenworkingandhesaidnothingheseemstohavesoftenedbutiknowicannottrustanyonehereshemustnotbediscoveredhereortheywilltakeawaymylasthopeofrescuingherandreturninghertotheghettomyplanstoescapeareoflessimportancebutmytraveldocumentsarenowcompletethepapersandinkswerehardtoacquirebutiexcusedmuchofitbyexplainingthatineededtosketchtosharpenmyskillswhichhaddeterioratedinthecampiamsavingsuchfoodasmightlastmostlyhardbreadandhardcheeseagainstthedayswhenihopetorunfromthisplacenowiwillneedtofindawaytostealmoneytopayformyjourneymyplacehighintheatticgivesmeaviewofthecitywhichhasallowedmetomakeamaptoguidemeonthemoonlessnightwheniwillfinallyrunandikeepthemapwithherandwiththisdiaryundertheboardsiwillliveandiwillbefreeandsowillshe";
        String cipherText = "WPSHC TSGZR LSKON JZSHJ CWONJ NTLSB TJDLN TLYDC BTSCV WXKHJ NSVJW BTJDJ KGCJN TLSCM SHSOS WCHJJ NTPSZ ZWCJN THNSV DPHWS BCDJH KGTPN SJNTH SPAKJ WYTEJ BRNSC VHATN WCVBR ASLYJ DNWVT JNTGT VEWOB TCJWN SVATT CPDGY WCOSC VNTHS WVCDJ NWCON THTTB HJDNS MTHDI JTCTV AKJWY CDPWL SCCDJ JGKHJ SCRDC TNTGT HNTBK HJCDJ ATVWH LDMTG TVNTG TDGJN TRPWZ ZJSYT SPSRB RZSHJ NDETD IGTHL KWCON TGSCV GTJKG CWCON TGJDJ NTONT JJDBR EZSCH JDTHL SETSG TDIZT HHWBE DGJSC LTAKJ BRJGS MTZVD LKBTC JHSGT CDPLD BEZTJ TJNTE SETGH SCVWC YHPTG TNSGV JDSLF KWGTA KJWTQ LKHTV BKLND IWJAR TQEZS WCWCO JNSJW CTTVT VJDHY TJLNJ DHNSG ETCBR HYWZZ HPNWL NNSVV TJTGW DGSJT VWCJN TLSBE WSBHS MWCOH KLNID DVSHB WONJZ SHJBD HJZRN SGVAG TSVSC VNSGV LNTTH TSOSW CHJJN TVSRH PNTCW NDETJ DGKCI GDBJN WHEZS LTCDP WPWZZ CTTVJ DIWCV SPSRJ DHJTS ZBDCT RJDES RIDGB RXDKG CTRBR EZSLT NWONW CJNTS JJWLO WMTHB TSMWT PDIJN TLWJR PNWLN NSHSZ ZDPTV BTJDB SYTSB SEJDO KWVTB TDCJN TBDDC ZTHHC WONJP NTCWP WZZIW CSZZR GKCSC VWYTT EJNTB SEPWJ NNTGS CVPWJ NJNWH VWSGR KCVTG JNTAD SGVHW PWZZZ WMTSC VWPWZ ZATIG TTSCV HDPWZ ZHNT".replaceAll("\\W","");

        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setAlphabet(defaultAlphabet);
        p.setLanguage(defaultLanguage);
        String encoded = keySub.encode(plainText, p);
        assertNotNull("DictEncoding text null", encoded);
        assertEquals("DictEncoding text value", cipherText, encoded.toUpperCase());

        // do this to load the English dictionary
        String reason = keySub.canParametersBeSet(p);
        assertNull("CanParametersBeSet", reason);
        Dictionary dict = defaultLanguage.getDictionary();
        assertNotNull("DictLoad dictionary", dict);

        // now crack it using Dictionary words as possible keywords
        Directives cp = new Directives();
        cp.setLanguage(defaultLanguage);
        cp.setAlphabet(defaultAlphabet);
        cp.setCribs("caught,allowed,diary");
        cp.setCrackMethod(CrackMethod.DICTIONARY);
        reason = keySub.canParametersBeSet(cp);
        assertNull("Null reason", reason);
        CrackResult result = keySub.crack(cipherText, cp);
        //System.out.println("Decoded "+decoded);
        String explain = result.getExplain();
        //System.out.println("Explain "+explain);
        assertNotNull("DictionaryCrack Explain", explain);
        assertTrue("DictionaryCrack Explain Success", explain.startsWith("Success"));
        assertTrue("DictionaryCrack Explain Status", result.isSuccess());
        assertEquals("DictionaryCrack Text", plainText, result.getPlainText().toLowerCase());
        assertEquals("DictionaryCrack CipherText", cipherText, result.getCipherText());
        assertEquals("DictionaryCrack Keyword", keyword, result.getDirectives().getKeyword());
    }

    @Test
    public void testExtendEmptySeedKeyword() {
        String fullKeyword = KeywordSubstitution.applyKeywordExtend(KeywordExtend.EXTEND_MIN, "", defaultAlphabet);
        assertEquals("Empty Keyword Min", defaultAlphabet, fullKeyword);
        fullKeyword = KeywordSubstitution.applyKeywordExtend(KeywordExtend.EXTEND_MAX, "", defaultAlphabet);
        assertEquals("Empty Keyword Max", defaultAlphabet, fullKeyword);
        fullKeyword = KeywordSubstitution.applyKeywordExtend(KeywordExtend.EXTEND_LAST, "", defaultAlphabet);
        assertEquals("Empty Keyword Last", defaultAlphabet, fullKeyword);
        fullKeyword = KeywordSubstitution.applyKeywordExtend(KeywordExtend.EXTEND_FIRST, "", defaultAlphabet);
        assertEquals("Empty Keyword First", defaultAlphabet, fullKeyword);
    }

    @Test
    public void testBadDirectives() {
        Directives p = new Directives();
        String reason = keySub.canParametersBeSet(p);
        assertEquals("BadParam: alphabet missing", "Alphabet is empty or too short", reason);
        p.setAlphabet("");
        reason = keySub.canParametersBeSet(p);
        assertEquals("BadParam: alphabet blank", "Alphabet is empty or too short", reason);
        p.setAlphabet("A");
        reason = keySub.canParametersBeSet(p);
        assertEquals("BadParam: alphabet short", "Alphabet is empty or too short", reason);
        p.setAlphabet(defaultAlphabet);
        reason = keySub.canParametersBeSet(p);
        assertEquals("BadParam: keyword  missing", "Keyword is empty or too short", reason);
        p.setKeyword("");
        reason = keySub.canParametersBeSet(p);
        assertEquals("BadParam: keyword  missing", "Keyword is empty or too short", reason);
        p.setKeyword("H");
        reason = keySub.canParametersBeSet(p);
        assertEquals("BadParam: keyword  missing", "Keyword is empty or too short", reason);

        p.setKeyword("ZEBRASCDFGHIJKLMNOPTUVWXY"); // Q is missing
        reason = keySub.canParametersBeSet(p);
        assertEquals("BadParam: different size alphabets", "Keyword (25) and alphabet (26) must have the same length", reason);

        p.setKeyword("ABCDEFGHIJKLMN!OQRSTUVWXYZ");    // ! is not in alphabet
        reason = keySub.canParametersBeSet(p);
        assertEquals("PBadParam: not in alphabet", "Character ! at offset 14 in the keyword is not in the alphabet", reason);

        p.setKeyword("ZEBRASCDFGHIJKLMNOPDTUVWXY"); // D is twice
        reason = keySub.canParametersBeSet(p);
        assertEquals("BadParam: repeat char", "Character D is present multiple times in the keyword", reason);
        p.setKeyword("ZEBRASCDFGHIJKLMNOPQTUVWXY"); // all good now
        reason = keySub.canParametersBeSet(p);
        assertNull("BadParam all okay", reason);

        // cracking
        p.setKeyword(null);
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = keySub.canParametersBeSet(p);
        assertEquals("BadParam: no lang", "Language is missing", reason);
        p.setLanguage(defaultLanguage);

        reason = keySub.canParametersBeSet(p);
        assertEquals("BadParam: cribs missing", "Some cribs must be provided", reason);
        p.setCribs("");
        p.setCrackMethod(CrackMethod.WORD_COUNT);
        reason = keySub.canParametersBeSet(p);
        assertEquals("BadParam: cribs empty", "Some cribs must be provided", reason);
        p.setCribs("mona,lisa,overdrive");
        reason = keySub.canParametersBeSet(p);
        assertNull("BadParam all okay crack", reason);
    }

    @Test
    public void testDescription() {
        String desc = keySub.getCipherDescription();
        assertNotNull("Description", desc);
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setAlphabet(defaultAlphabet);
        p.setKeyword("ZYXWAUTSROPQMNLKJIGHEFDCBV");
        p.setLanguage(defaultLanguage);
        String reason = keySub.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = keySub.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Keyword Substitution cipher (keyword=ZYXWAUTSROPQMNLKJIGHEFDCBV)", desc);
    }

    @Test
    public void testFormSuggestedKeyword() {
        String cipherText = "Abcdejdjdhiijdj;j;p;oefiheohf;fefHEFOIHEF;ohfwo;hefw;OFEGHFEh'PEHF'pewhffh'";
        String keyword = keySub.formFrequencySuggestedKeyword(cipherText, defaultAlphabet, defaultLanguage);
        assertNotNull("Form Suggested Keyword", keyword);
        assertEquals("Form Suggested Keyword", "ESCAFMNPJVUBKDORYWIHGTLXQZ", keyword);
    }

}
