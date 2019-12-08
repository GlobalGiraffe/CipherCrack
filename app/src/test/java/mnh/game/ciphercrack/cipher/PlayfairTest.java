package mnh.game.ciphercrack.cipher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.KeywordExtend;
import mnh.game.ciphercrack.util.Settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test out the Playfair Cipher code
 */
@RunWith(JUnit4.class)
public class PlayfairTest {

    private final Playfair playfair = new Playfair(null);

    @Test
    public void testEncodeDecodeFromCipherChallenge() {
        // 2002 - cipher 12
        String key = "SHERLOCK HOLMES";   //
        String plainText = "I believe this cipher would be of greater utility if we included numbers.\n" +
                "It occurs to me that we should extend the alphabet to include the numerals,\n" +
                "and this would lend itself most conveniently to a six by six grid. That\n" +
                "would also avoid the need to arbitrarily concatenate two letters in one\n" +
                "cell, an altogether more satisfactory solution, I am sure you will agree.\n" +
                "\n" +
                "With regard to our turning machine: bad news, I am afraid. My friend close\n" +
                "to the French embassy has been making enquiries about CD and E Spion.\n" +
                "Information is hard to obtain, but it seems that the French are constructing\n" +
                "an automatic enciphering machine that bears some marked resemblance to our\n" +
                "designs. It is an electromechanical device, and like ours, has two rotating\n" +
                "wheels that move forward each time a letter is punched into the machine.\n" +
                "From what I gather, these wheels can be interchanged--a marked improvement\n" +
                "on our own more fixed design. I have not been able to obtain further details\n" +
                "of the internal construction, but I will make further enquiries. The most\n" +
                "alarming detail which has so far come to my attention is the fact that\n" +
                "operating the machine can both encipher and decipher a text using the same\n" +
                "settings. This suggests strongly that, like our design, the French machine\n" +
                "embodies the principle of the electrical reflector, though I am unable to\n" +
                "understand how they could have obtained this idea, given that we retrieved\n" +
                "the only copy of the design drawings from that fiend E Spion. It all appears\n" +
                "to be a bit of an enigma.\n" +
                "\n" +
                "The news from Afghanistan and the progress of General Keane at the head of\n" +
                "the army of the Indus has set me to consider how we might make our machine\n" +
                "portable for use by the military. Messages could be passed along a veritable\n" +
                "web of electric telegraph lines, but it would greatly help if small letters\n" +
                "illuminated by the power of electricity could replace the large letter\n" +
                "indicator mechanism. Today this seems but an idle dream.a";
        String expectedEncoding = "WI RS AL TL QE WL NX QS RL DP VR JW SK GJ LR GA RL VU YN AV LY DX LA OK RV GS YO BU AR LH AV CK FQ BM EP CN KA EX PA RH SC VR GS AQ LK GP ER IE QS BI KA AQ NW OK RV GS QE LK BU RL IE EW OY QE WL DP VR YS LK YW PE RS JC PO QK CO TL VY LK VE GV KW LW AI DL WA JE WY QE GA DP VR GW SH KW PN WY QE LK HA SG PK BE IW UE BE YN FN CO KX AK KI AK PA NS KA AQ RL LW OC KL KH HI EI KI EV KD KA ER MU MS RH GA WL GX KQ MS DL NS VU WN VY BK RP LR DN PB YN IH GE LR AH XW QE LR EG SJ PK WC BM UV LM YV JK XK LX KL IB YO SA LW BK XG EB WY NJ JH AL OY NH PO KA KP ER JH LK QC RK IB HW LD EX RW HA LK KB NA KY LK TV BL AL EW WM VU OF IK GS OW WN VY CY MS KB VA CO WL EX SJ PK WC AU BW MI VU AV HR AH OR QE GA AQ ER JH LK QC BE HK CO EP MB KQ YV EG KI VU CN GA XN LK NX QS RL YV JK XK LX KL QE GA AR BE HW OP KR KB EM SG LR HR UJ EI OK KA CW MP SJ RH AY OL AV WL IK RS HK UE CN HK EX VY KX SY LT XN KG OY NY TK MP LH EX EP DP SM AG VA KY XS HA RS EP EX UK NP HG MS XB SJ KG QC VA KR IE KA AQ RL WL QV OK ER YW KV KP ER KB QC YV HG SM OB EX VA EG QE RL QE RH SA ER AH SH KX MI LA KV RL QC IK EK GW KB EM SG BN US NP RK LK PK OC BM PD ON MS HG WA SG WF RH AY VY EX TL OC UA HA LK BI SR PK WC AU BW CY BM QE RL GS AG YN OP GQ ER YV AK LM IE KC OL UE QM VA CO JB VA XW HI RN GT HG BM QE RL LK TV BL AL EP ER NC EP IE BE NB KY GS AG YN XS XN CF EX HW OP GX HM CN KA CN GI QA AK KV WN VY EP ER GX KQ AQ EX PK TS EB VA KY QE RK XK LX KL KX MI KP ER OK WV ER EB OY WF HK WV ER EB AK AQ PR YV EA ER EW KR HR QA VA KY EP LX HW RP FA EK EP HW EP SM KY NL QE GA NY TK MP SJ RH AY KV ER JH LK QC KB QC YV HA RK WM YW RH QE ST LB OK WV SR CD QE HA RS HK UE XN IE LR YH HK PK EU SC TJ LX BK VM BI SR PK VM GS LH AG OY SC AP ER FN MP SY EX TL MW AG YV SG QE WL WY KG YA TL KV EX PA RL KA LB LT SG QE SK VN FN PW DN GQ ER GS LW YK JS BX YV DE JH CN QE GA YX LK GS OW WN VY AG HI EI QW TS BE EP MW KG IW PK GX KL VY JK GA ER KL DO JH CN XG FE IK WL AG KI OY QE ST SM JE RH WH CD EK KL EB EN KG KL GA AQ ER ER WG CD QE KG MU DN GQ ER YV JP HE WE WH KA KR PK KC OL WY RL SC XA AS NB FE UK GT SK BM KB QC YV ST MS AG IR HG MS PR RA GV ER NB NY AG LJ KR HW EW EK HO MP SY AR TW HW HR GW SN KY IT RL AV BI SR AS WM GH SR KQ LB KQ RS KE EB QS NY KL RW VU AV DP VR FJ LR GA NL ER SV XY RO IE IH IH KA AQ RL LW HI RV NB KI AK JW GV ER WP AS SM GH SR KQ LB NX VG KC VR JS ST EI KH QE RS BE EK SR QA AK LB OY XN GA MS KR QC IK WL KU PS IG QE WL WH HA RK RW VU IK WY SR JS KG KB".replaceAll(" ","");

        String fullKey = Cipher.applyKeywordExtend(KeywordExtend.EXTEND_LAST, key, Settings.DEFAULT_ALPHABET, "X");
        //assertEquals("Ensure keyword extended correctly", "SHERLOCKMNPQTUVWXABIDFGJY", fullKey);
        fullKey = "SHERLOCKMNPQTUVWXABIDFGJY";

        Directives p = new Directives();
        p.setKeyword(fullKey);
        p.setNumberSize(55);
        p.setReplace("ZX");
        p.setReadAcross(true);
        String reason = playfair.canParametersBeSet(p);
        assertNull("Check parameters encode", reason);
        String encoded = playfair.encode(plainText, p);
        assertEquals("Encoding CC Example", expectedEncoding, encoded);
        String decoded = playfair.decode(encoded, p);
        assertEquals("Decoding CC Example", plainText.toLowerCase().replaceAll("[ x:,.\n]|-",""), decoded.replaceAll("x",""));
    }

    // takes around 8 seconds
    @Test
    public void testCrackCipherChallenge2() {
        String cipherText = "GRLZE HNRAO TNNPM PVKUG UFLIG YBRPN OSTUH KMGLT TUBRS YUGGN OAFXD ALVEI EQGRN UGRLO LNGAL UMHGR LHLIM AZGBA AYEIM PGRLD KELRT QNPRN ZHGAT ULGHM EUPLU QRATP LPKIT YGRHB GYUNM PPMER TYIOU GANAL MPBVT ADAEI GDUZA RKTPO AOLTU FKZRL UGEPP LADTF ADKEB LUGRA VDPLH KMGRL RAEPR DKEUI XLPMF ADGKH IOTQO AEPUH MHAPK GGTRU TAHZF YBNEP GRUGN ACXOU TUMRG YGRNI IEAMP LHLIN VBAOP MMYOK TGYGG TREAY GTYDG AAMAY GYBRS YUGGN ADKEB LUGRA GRELU GATBP TYKOH GUHTA HZFYB NRPTY MAUMN LPNNL KOOKT GYGGT UGOAY GGTZC GTNPV DXMKO DEEXA KQFIZ RARLK OTAHZ FYBNL PBVDA SNGTP NMGRM ARKGA TRATH BNEPA ORLUN BRKEN PIZRA UNUGR LRDER EHRAM VKOLY FBEIN GHMAD ARVDA LUGRA RDSVT ADATU UOBAA GLYGT OIARE DPNIO UFPVG AQYLY GTNGA OEIEQ GRHBG YUNBL NKEXE ICDAG RENAY AUGEP TAHZF YBNLI NBGUB RYATU MRGTU GATBP TYKOH CDBMC HUNSG RNKMG HZTUA DPNIO EPUGA NAOIE IPAMY EUNUA KRQHR DIELR SPEPR EKMGT LTQXG RUGRA UGGEY DARTO BIERU HTOVK FLAMT NUNUA KRQHR DFWGD QUBIE RHMEH RANAP LSYUG EPTUE BANTP OMOIY KEPDE GTSEK MLTDA LPMPA DARTA HOBVT LYAOK OGRAB LNKEX NPTLL NTQMU PMFAG YBRSY UGLGL IMAVN TYGTL REQUO MREQG RNIIE AMPLT OPNRM TGMHU GLNBR LKLPY GARLW LDAPK GMPYG FBOKS PDYRE EROIT HGUUH ATHUR QGDUG ATBPT YKOHC PUUGE PTAHZ FYBNL PVBPN GTGDZ KARUG KOUGR GBVTV ARTPE SVDTU BRSYU GATBP TYKOC YLPAR UPEOR LUNHL EIGDE IAKTM KZKOU ZAUMU TLXLP MIOKV MAEAG YZHTY IUDAM PUNHK UHAKI EYGUG RDGQP ANPNA UMHMZ HDAYG VSRAU TKOAR QFPOT NUGGN AREPZ KDOXM UHUGL GEPGR AEEIL KXENA PUGTE IUTVK FLDFI OPNOT GYKHI NIAGT KBLGU HUGDY ARQGG RECOA AFPUO BMREK UOIUG YBRUT GRVKF LAFLG UZKEU GRGUZ TLKON ITYIL EUARB RPFLY GTUGU HUZHB EXTLD AFBRL EIEQG RILYG AODEP LKEZK DOUMB LEUUG RARGB AAGLY GTBDP ERMHT RLEIA QLPTL EQMZU ZLZAR TUUHE PKITY RDNSG ROUPT ANUTR EGBLP ARUPE ORLUN NPULM RRDPL TUEBK OBTMU SGUHU EMZIU PMTYA DMTNP UGRLM PHKYA EZMHN IARMH PLADQ XUHQK IOGDD ERLGA BOKEV BTGFX KOUGG NIORK AMRAU FINAG AOTNP LBRSV TVHKK GRSPM IOUNE INUGR AUDAU NHMZH GTSPO ANAUF RAHME HRANA PLARU ESREG WSPOP MYGEI RKZHL YTAEU QXUZE LGABR ALUAL TTLEQ KRGYD GSYUG LGADQ RMBYD AKEKN UDEAR DLWNO IYQBR SYUGM PVDTU GRZKD OMHUG GNGRS YUNBL NKEXV KUGZN ZHTAL OMPPM IOKVU ZELAM NDUNU ALTGT UNTGM HUGLT DYUHM QUNZN PRKEE INUGR EUNUG RHSOH RALPL IDAMI MSEGT ULYLN EILKE NADPN UPLRH MEZUZ LUPQU IEUKO UZDZU HOCAN RFNPE RLZ";

        String expectedDecode = ("Herr Goering,\n" +
                "\n" +
                "It is with shame that I must confess that the girl Sara and the other members of her family have vanished, and despite our best efforts at persuasion, the Ghetto is silent on their disappearance. Our enquiries showed that Sara’s grandfather was a confederate and possible co-conspirator of Vincenzo Peruggia, the thief who stole the Mona Lisa from Paris in Nineteen Eleven. We believe that the grandfather held the painting for Peruggia until some time in Nineteen Thirteen, when it was finally discovered in Peruggia’s apartment in Florence. Peruggia tried to hand it over to the dealer Geri in exchange for a reward. There appears to have been no relationship between Geri and the Ghetto family and we believe that Peruggia might have stolen the painting back from them in frustration at their inability to produce an adequate likeness. Whether they were unable or unwilling to produce acceptable forgeries at that stage is unknown. A talent like Sara’s is rare, perhaps even in her family. It seems possible that the family intended to hold the Mona Lisa until one of them had mastered da Vinci’s technique well enough to reproduce the painting but that Peruggia’s patience wore thin. The happy result was that the painting was returned to France and is now in our possession.\n" +
                "\n" +
                "I will return to Paris to coordinate the activities of our art experts in rescuing the great works for the Fatherland. My lieutenants will continue to comb Venice for the wretched girl, but I hold no hope that she will be found.\n" +
                "\n" +
                "The house in Montmartre has been thoroughly searched and the materials and works of art there have been catalogued and passed to our restoration team. The house itself has returned to its role as a staging post for troops in transit.\n" +
                "\n" +
                "The discovery of more of Sara’s work, concealed behind panels in the gondolier’s home, brings a happy conclusion to another part of our enquiries. Her forgeries are truly exquisite and our experts would be hard pressed to detect the fraud if we did not already know that this was the work of the Ghetto family.\n" +
                "\n" +
                "With your permission I would like to present one of these works to you, and another to the Fuhrer as a mark of my esteem and my gratitude for your support in our work.\n" +
                "\n" +
                "Heil Hitler.\n").replaceAll("[\n x.,'’]|-","").toLowerCase();
        // now attempt the crack of the text via Dictionary
        Directives p = new Directives();
        p.setKeyword(null);
        p.setNumberSize(55);
        p.setReadAcross(false);
        p.setCribs("the,and,gondolier");
        p.setReplace("JI");
        p.setLanguage(Language.instanceOf("English"));
        p.setCrackMethod(CrackMethod.DICTIONARY);
        String reason = playfair.canParametersBeSet(p);
        assertNull("CrackDictCCSuccess: crack param okay", reason);

        CrackResult result = playfair.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        assertTrue("CrackDictCC success", result.isSuccess());
        assertEquals("CrackDictCC Cipher", cipherText, result.getCipherText());
        assertEquals("CrackDictCC Text", expectedDecode, result.getPlainText().replaceAll("x",""));
        assertEquals("CrackDictCC Keyword", "ADLERBCFGHIKMNOPQSTUVWXYZ", decodeKeyword);
        assertNotNull("CrackDictCC Explain", explain);
        assertEquals("CrackDictCC cipher name", "Playfair cipher (ADLERBCFGHIKMNOPQSTUVWXYZ,size=55)", result.getCipher().getInstanceDescription());
        assertEquals("CrackDictCC crack method", CrackMethod.DICTIONARY, result.getCrackMethod());
    }

    // takes around 2 seconds
    @Test
    public void testCrackDictFailLittleDorrit() {
        String plainText = "Thirty years ago, Marseilles lay burning in the sun, one day. A blazing sun upon a fierce August day was no greater rarity in southern France then, than at any other time, before or since. Everything in Marseilles, and about Marseilles, had stared at the fervid sky, and been stared at in return, until a staring habit had become universal there. Strangers were stared out of countenance by staring white houses, staring white walls, staring white streets, staring tracts of arid road, staring hills from which verdure was burnt away. The only things to be seen not fixedly staring and glaring were the vines drooping under their load of grapes. These did occasionally wink a little, as the hot air barely moved their faint leaves.";
        // BRAMLEY APPLES -- not in the dictionary
        String fullKey = Cipher.applyKeywordExtend(KeywordExtend.EXTEND_FIRST,"BRAMLEYAPPLES",Settings.DEFAULT_ALPHABET,"J");

        Directives p = new Directives();
        p.setKeyword(fullKey);
        p.setNumberSize(55);
        p.setReplace("JI");
        p.setReadAcross(true);
        String reason = playfair.canParametersBeSet(p);
        assertNull("Check parameters CrackFail", reason);
        String encoded = playfair.encode(plainText, p);
        assertNotNull("Encoding CrackFail encode", encoded);
        String decoded = playfair.decode(encoded, p);
        assertEquals("Decoding", plainText.replaceAll("[ ,.x]","").toLowerCase(), decoded.replaceAll("x",""));

        // try a crack via Dictionary, expect to fail - bad cribs and key not in dictionary
        p.setKeyword(null);
        p.setNumberSize(55);
        p.setCribs("the,and,marseilles");
        p.setReplace("JI");
        p.setLanguage(Language.instanceOf("English"));
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = playfair.canParametersBeSet(p);
        assertNull("CrackDictCCSuccess: crack param okay", reason);

        CrackResult result = playfair.crack(encoded, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        assertFalse("CrackFail success", result.isSuccess());
        assertEquals("CrackFail Cipher", encoded, result.getCipherText());
        assertNull("CrackFail Text", result.getPlainText());
        assertNull("CrackFail Directives", result.getDirectives());
        assertNotNull("CrackFail Explain", explain);
        assertEquals("CrackFail cipher name", "Playfair cipher (n/a)", result.getCipher().getInstanceDescription());
        assertEquals("CrackFail crack method", CrackMethod.DICTIONARY, result.getCrackMethod());
    }

    // takes up to 2 minutes, sometimes works, sometimes not
    @Test
    public void testCrackWordCountSuccess() {
        String plainText = "Thirty years ago, Marseilles lay burning in the sun, one day. A blazing sun upon a fierce August day was no greater rarity in southern France then, than at any other time, before or since. Everything in Marseilles, and about Marseilles, had stared at the fervid sky, and been stared at in return, until a staring habit had become universal there. Strangers were stared out of countenance by staring white houses, staring white walls, staring white streets, staring tracts of arid road, staring hills from which verdure was burnt away. The only things to be seen not fixedly staring and glaring were the vines drooping under their load of grapes. These did occasionally wink a little, as the hot air barely moved their faint leaves.";
        // BRAMLEY APPLES -- not in the dictionary
        String fullKey = Cipher.applyKeywordExtend(KeywordExtend.EXTEND_FIRST,"BRAMLEYAPPLES",Settings.DEFAULT_ALPHABET,"J");

        Directives p = new Directives();
        p.setKeyword(fullKey);
        p.setNumberSize(55);
        p.setReplace("JI");
        p.setReadAcross(true);
        String reason = playfair.canParametersBeSet(p);
        assertNull("Check parameters CrackFail", reason);
        String encoded = playfair.encode(plainText, p);
        assertNotNull("Encoding CrackFail encode", encoded);
        String decoded = playfair.decode(encoded, p);
        assertEquals("Decoding", plainText.replaceAll("[ ,.x]","").toLowerCase(), decoded.replaceAll("x",""));

        // try a crack via Dictionary, expect to fail - bad cribs and key not in dictionary
        p.setKeyword(null);
        p.setNumberSize(55);
        p.setCribs("the,and,burning");
        p.setReplace("JI");
        p.setLanguage(Language.instanceOf("English"));
        p.setCrackMethod(CrackMethod.WORD_COUNT);
        reason = playfair.canParametersBeSet(p);
        assertNull("CrackDictCCSuccess: crack param okay", reason);
        System.out.println("Expecting Key "+fullKey);

        CrackResult result = playfair.crack(encoded, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        assertEquals("CrackFail Cipher", encoded, result.getCipherText());
        assertNotNull("CrackFail Explain", explain);
        assertTrue("CrackFail success", result.isSuccess());
        assertEquals("CrackFail Text", decoded, result.getPlainText());
        assertNotNull("CrackFail Directives", result.getDirectives());
        assertEquals("CrackFail Keyword", fullKey, result.getDirectives().getKeyword());
        assertEquals("CrackFail cipher name", "Playfair cipher ("+fullKey+",size=55)", result.getCipher().getInstanceDescription());
        assertEquals("CrackFail crack method", CrackMethod.WORD_COUNT, result.getCrackMethod());
    }

    @Test
    public void testDropLetter() {
        // encode and then decode 5x5 example found on the web
        String square = "abcdefghiklmnopqrstuvwxyz";
        String plainText = "the quick brown fox jumps over the lazy dog";
        String expected = "SIAUTKEHGWMYLHNYKTNLTNZASUKCQFVZITHW";
        Directives p = new Directives();
        p.setKeyword(square);
        p.setNumberSize(55);
        p.setReplace("JI");
        String reason = playfair.canParametersBeSet(p);
        assertNull("Check parameters encode", reason);
        String encoded = playfair.encode(plainText, p);
        assertEquals("Encoding Example", expected, encoded);
        String decoded = playfair.decode(encoded, p);
        assertEquals("Decoding Example", plainText.replace('j','i').replaceAll(" ","")+"x", decoded);
    }

    @Test
    public void testExampleFromWeb() {
        // encode and then decode 5x5 example found on the web
        int rowsCols = 55;
        String square = "zgptfoihmuwdrcnykeqaxvsbl";
        String replace = "JI";
        Directives p = new Directives();
        p.setNumberSize(55);
        p.setKeyword(square);
        p.setReplace(replace);
        String reason = playfair.canParametersBeSet(p);
        assertNull("Check parameters encode", reason);
        String encoded = playfair.encode("Defend the east wall of the castle", p);
        assertEquals("Encoding Example", "RKPAWRPMYSELZCLFXUZFRSNQBPSA", encoded);
                                       // defendtheeast
        String decoded = playfair.decode(encoded, p);
        assertEquals("Decoding Example", "defendthexastwallofthecastle", decoded);
    }

    @Test
    public void testAllLettersAZWithNoJ() {
        // encode and then decode 5x5 using all letters (not J) in key, and all letters in a message
        int rowsCols = 55;
        String square = "ABCDEFGHIKLMNOPQRSTUVWXYZ"; // no J
        String replace = "JI";
        Directives p = new Directives();
        p.setNumberSize(rowsCols);
        p.setKeyword(square);
        p.setReplace(replace);
        String reason = playfair.canParametersBeSet(p);
        assertNull("Check parameters encode", reason);
        String encoded = playfair.encode("The quick brown fox jumps over the Lazy Dog", p);
        assertEquals("Encoding Example", "SIAUTKEHGWMYLHNYKTNLTNZASUKCQFVZITHW", encoded);
        String decoded = playfair.decode(encoded, p);
        assertEquals("Decoding Example", "thequickbrownfoxiumpsoverthelazydogx", decoded);
    }

    @Test
    public void testAllLettersAZWithNoQ() {
        // encode and then decode 5x5 example found on the web
        int rowsCols = 55;
        String square = "ABCDEFGHIJKLMNOPRSTUVWXYZ"; // no Q
        String replace = "QK";
        Directives p = new Directives();
        p.setNumberSize(rowsCols);
        p.setKeyword(square);
        p.setReplace(replace);
        String reason = playfair.canParametersBeSet(p);
        assertNull("Check parameters encode", reason);
        String encoded = playfair.encode("The quick brown fox jumps over the Lazy Dog", p);
        assertEquals("Encoding Example", "SIAOTJAMGWLZKIMZOZKSUMZASUJCKBVZENHW", encoded);
        String decoded = playfair.decode(encoded, p);
        assertEquals("Decoding Example", "thekuickbrownfoxjumpsoverthelazydogx", decoded);
    }

    @Test
    public void test6x6Cipher() {
        // encode and then decode some text with a 6x6 playfair
        int rowsCols = 66;
        String square = "a8bcde1fgh2ijkl34mno5pqrs6tuv7wx9yz0";
        String replace = "JI";
        String plainText = "the quick brown fox jumps over the lazy dog 1 9 28";
        String expectedEncode = "UGDR7H83E5NXO168H73R6N7D57ICJB0Z8QHFZGF8";
        String expectedDecode = "thequickbrownfoxiumpsoverthelazydog1928x";
        Directives p = new Directives();
        p.setNumberSize(rowsCols);
        p.setKeyword(square);
        p.setReplace(replace);
        playfair.canParametersBeSet(p);
        String encoded = playfair.encode(plainText, p);
        assertEquals("Encoding Example", expectedEncode, encoded);
        String decoded = playfair.decode(encoded, p);
        assertEquals("Decoding Example", expectedDecode, decoded);
    }

    @Test
    public void testBadParameters() {
        Directives p = new Directives();
        p.setAlphabet(null);
        String reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: alphabet missing", "Alphabet is empty or too short", reason);
        p.setAlphabet("");
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: alphabet empty", "Alphabet is empty or too short", reason);
        p.setAlphabet("D");
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: alphabet empty", "Alphabet is empty or too short", reason);
        p.setAlphabet(Settings.DEFAULT_ALPHABET);

        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: rows cols wrong", "Invalid value 0 for rows and columns", reason);
        p.setNumberSize(22);
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Invalid value 22 for rows and columns", reason);
        p.setNumberSize(102);// too big
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: short keyword", "Invalid value 102 for rows and columns", reason);
        p.setNumberSize(66); // not right for keyword - should have 36 chars
        p.setKeyword("ABCDEFGHIJKLMNOPQRSTUVWXY"); // missing Z
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Keyword is empty or too short", reason);
        p.setNumberSize(44); // not right for keyword - should have 16 chars
        p.setKeyword("ABCDEFGHIJKLMNOPQRSTUVWXY"); // missing Z
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Keyword is longer than expected size", reason);
        p.setNumberSize(55);
        p.setKeyword(null);

        reason = playfair.canParametersBeSet(p); // keyword is null
        assertEquals("BadParam: empty keyword", "Keyword is empty or too short", reason);
        p.setKeyword("");
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Keyword is empty or too short", reason);
        p.setKeyword("ABCDEFGHIJ");
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: short keyword", "Keyword is empty or too short", reason);
        p.setKeyword("ABC#EFGHIJKLMNOPQRSTUVWXY"); // why is # there!
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: short keyword", "Symbol # at offset 3 in the keyword is not in the alphabet", reason);
        p.setKeyword("ABCDEFGHIJKLMNOPQRSHUVWXY"); // H is repeated
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Symbol H is repeated in the keyword", reason);
        p.setKeyword("ABCDEFGHIJKLMNOPQRSTUVWXY"); // H is repeated
        p.setNumberSize(44);
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Keyword is longer than expected size", reason);

        p.setNumberSize(55);
        p.setKeyword("ABCDEFGHIJKLMNOPQRSTUVWXY"); // missing Z - but 5x5 is okay
        p.setReplace("Z");
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: replace len", "Invalid replacement length 1", reason);
        p.setReplace("Z@");
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: replace len", "Replace with symbol @ must be in the keyword", reason);
        p.setReplace("BZ");
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: replace len", "Replace symbol B must not be in the keyword", reason);
        p.setReplace("ZX");
        reason = playfair.canParametersBeSet(p); // now all good for encode/decode
        assertNull("BadParam: encode okay", reason);

        p.setCrackMethod(CrackMethod.IOC);
        reason = playfair.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid crack method", reason);
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = playfair.canParametersBeSet(p); // Crack: still missing cribs
        assertEquals("BadParam: cribs missing", "Some cribs must be provided", reason);
        p.setCribs("vostok,sputnik,saturn");
        p.setLanguage(null);
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: missing language", "Missing language", reason);
        p.setLanguage(Language.instanceOf("German"));
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: missing language", "No German dictionary is defined", reason);
        p.setLanguage(Language.instanceOf("English"));

        p.setNumberSize(0);
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: row/col 0", "Cannot crack with rows and columns: 0", reason);
        p.setNumberSize(99);
        reason = playfair.canParametersBeSet(p);
        assertEquals("BadParam: negative length", "Cannot crack with rows and columns: 99", reason);
        p.setNumberSize(55);
        reason = playfair.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);

        p.setLanguage(Language.instanceOf("English"));
        reason = playfair.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);
    }

    @Test
    public void testCrackDictSuccess() {
        // attempt dictionary crack of Vigenere cipher and succeeds with good cribs
        String keyword = "ENRGYABCDFHIKLMOPQSTUVWXZ";  // ENERGY, 5x5
        int rowsCols = 55;
        String plainText = "I must not fear. Fear is the mind-killer. Fear is the little-death that brings total obliteration.";
        String expectedDecode = "imustnotfearfearisthemindkillerfearisthelitxledeaththatbringstotalobliteration";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setNumberSize(rowsCols);
        String reason = playfair.canParametersBeSet(p);
        assertNull("CrackDictSuccess: encode param okay", reason);
        String cipherText = playfair.encode(plainText, p);
        assertNotNull("CrackDictSuccess: Encoding", cipherText);

        String decodedText = playfair.decode(cipherText, p);
        assertNotNull("CrackDictSuccess: Decoding", decodedText);

        // now attempt the crack of the text via Dictionary
        p.setKeyword(null);
        p.setNumberSize(rowsCols);
        p.setCribs("total,fear,killer");
        p.setLanguage(Language.instanceOf("English"));
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = playfair.canParametersBeSet(p);
        assertNull("CrackDictSuccess: crack param okay", reason);

        CrackResult result = playfair.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        assertTrue("CrackDict success", result.isSuccess());
        assertEquals("CrackDict Cipher", cipherText, result.getCipherText());
        assertEquals("CrackDict Text", expectedDecode, result.getPlainText());
        assertEquals("CrackDict Keyword", keyword, decodeKeyword);
        assertNotNull("CrackDict Explain", explain);
        assertEquals("CrackDict cipher name", "Playfair cipher (ENRGYABCDFHIKLMOPQSTUVWXZ,size=55)", result.getCipher().getInstanceDescription());
        assertEquals("CrackDict crack method", CrackMethod.DICTIONARY, result.getCrackMethod());
    }

    @Test
    public void testCrackDictFail() {
        // attempt dictionary crack of Vigenere cipher but fails as keyword (MOBYDICK) is not in the dictionary
        String keyword = "ENRGYABCDFHIKLMOPQSTUVWXZ";  // ENERGY, 5x5
        int rowsCols = 55;
        String plainText = "I must not fear. Fear is the mind-killer. Fear is the little-death that brings total obliteration. I will face my fear. I will permit it to pass over me and through me. And when it has gone past I will turn the inner eye to see its path. Where the fear has gone there will be nothing. Only I will remain.";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setNumberSize(rowsCols);
        String reason = playfair.canParametersBeSet(p);
        assertNull("CrackDictFail encode param okay", reason);
        String cipherText = playfair.encode(plainText, p);
        assertNotNull("CrackDictFail: Encoding", cipherText);

        // now attempt the crack of the text via Dictionary
        p.setKeyword(null);
        p.setNumberSize(rowsCols);
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("purse,november,ocean"); // bad cribs => that's why it fails
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = playfair.canParametersBeSet(p);
        assertNull("CrackDictFail: crack param okay", reason);

        CrackResult result = playfair.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        assertFalse("CrackDict Fail success", result.isSuccess());
        assertNull("CrackDict Fail Text", result.getPlainText());
        assertEquals("CrackDict Fail Cipher", cipherText, result.getCipherText());
        assertNull("CrackDict Fail Keyword", result.getDirectives());
        assertNotNull("CrackDict Fail Explain", explain);
        assertEquals("CrackDict Fail cipher name", "Playfair cipher (n/a)", result.getCipher().getInstanceDescription());
        assertEquals("CrackDict Fail crack method", CrackMethod.DICTIONARY, result.getCrackMethod());
    }

    @Test
    public void testDescription() {
        String desc = playfair.getCipherDescription();
        assertNotNull("Description", desc);
        assertTrue("Description content", desc.contains("Playfair cipher"));
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setKeyword("AFTERBCDGHIKLMNOPQSUVWXYZ");
        p.setNumberSize(55);
        String reason = playfair.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = playfair.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Playfair cipher (AFTERBCDGHIKLMNOPQSUVWXYZ,size=55)", desc);
    }
}
