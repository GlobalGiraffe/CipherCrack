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
 * Test out the Polybius Cipher code
 */
@RunWith(JUnit4.class)
public class PolybiusTest {

    private final Polybius polybius = new Polybius(null);

    @Test
    public void testCipherChallenge2019_8B() {
        String encoding = "FBGAI AGCFE KEFEK CIAGC FCGAF CIBHD HEFCF AFBFA GDFCH DFEKC IAKCI BGBGC IAHAF EKCFA KAIAG CFBFA GBFBI AFBHE IAGCK CIAFC IBHDF EGAGA FCHDI AHEIA FCKDF CFAIA KCFBF AIAGC FEHEF CICFB FEIAH EFDKA HBHDF CIEKA IDKCH DHEFB HEKEF CFCHA FEKEK CHEHA KAGEF CKCIA GCFBF AGBFC GAIAG CFEHE FCICF BFEIA FEHAH BFBHD FEIDK CHEHE IAFCG DFEKE FDKAG CFBHE KEFEK CIAGC IAGCF EIBHE HEHDG AFEFE KEHEF CFAIA GCFEK CFAGB FEHDI AGCKC IAIDF EHEIA FBHDI BHBID FBIAG CFEKC KDGCI DKCHD IDFEG AFBGB GCIAF BFAFB IAHEH BIBHB HBFEI AHEIA KCIAF EHEID FEGCK CICFE IAHDF BFEKE IAFCK DFCFA IAKCF BFAFB IAFDI BIAFB FAFCI DHDFE KCGEF BHEFE IAGCK CIAID FEKDK CFAFC FAGEK AKEFE GAFEK CIAFB IAIAG CFEID KCKAI DFEGA FCIBG BGCIA GAKCH EKDFB HEHAF DKAIA KCGDF BFAGB FCFAF BIAHE HAFBG BGCIA FBFAF CHBFE FAIDK CHDFC IBHDH BFCGE FBIAF BKDFB KCFAH EGCKC ICFEH EGCHD IBFAG DGAHD FCHAI AGCFE KDFCF AGAGE FBKDI AKCFA KEIAG CFEKA FAFEF EKEKC HDFEK CHEFC FAIAF CKDFC FAGAH DFCFA IAIAG CFEFE ICFBG EFEIC FEFAI AGCFE IAGCH DFEKC IAFCG AFAIB KDGEF EKCHD HAFBH EHEFB GEFEH EFDKC HEFEK EFBFA KDIBF DKCKE FBKEF AFCIA GBFBI CFEIA GCFEH AIAGC FEHAF EIAIA GEFEI AFCGA FBGBG CIAFB FAHEI AFEKC KEIAG CFEKA GCKCI CFEKD FCFAI CFBFA KDFEK EIAGC FEHAH EFEGE ICFEH EIAGC KCIAI AGCFE HAKCK EKEFC KDIAH DFBFA FEFCG AHAIB IAIBK CGEGE KAKCH EHEIB HDFEK EKEFE HEIAH DIBKD IAFBF CFAID FBGEG EHBHD FCIAF EKDIA IBHEK CFAKE IAGCK CIAHB FEKCK DFEKD KCFAF DFEHA KCFBF AIAKC FBFAF EKEIB FAKEF EHDIA GCFEH EGCFE GEIAF EHDFC GAIAG CFEFA IBKDG EFEKC HDIBH AFDHD FEGEG EKCIA GCFEK AKEFC FAFCI AHEFE FEHAI AFCHE FEFEI AGCKC IAIAG CFEGE FCFAG BFEHD IDFEK DFCID FEHDF BFAIA GCFBH EHEGC KCKEF CIDIA GCFEH AFCHD FEIAG CFEIA IDFCH EFBKE FEHEI DFBGE GEFBF AICFE HEIAF BFAIA GCFEH EFEKC IDGAI BGEID FEKCH BFCFA HEFDF EGAFC HDFEG EFCFA GBFBI AIDFB GEGEF DFEFB HAHBF CHEHE FBFDG EFEIA FCGAF BGBGC IAKCI AKCGE GEIDF BIAGC FCIBI AIAGC FEIAG CHDFE KCIAF CGAIA FCIAK CGEKC FAFAF BGCFB GEKCI AFBFC FAKCF AKEID GCFEF AIAGC KCIAK EKCKA KDFCH AFEHE IDFEI DFBGE GEKCG EGEFD FEFEF AHEGE KCICF EKEFD KAIDG CFCFE ICFEH DFBHE HAFCH DFEHD IBIAG CGEFE HEHEG EFCHE FEKCF AKEID FEIDF BGEGE FDFEI AGCFE HEGEK CICFE HEIAG CFEHB HDFBK DFEFC GAIDF BFAFA FBFAG BHAFB GBGCI AFDFE IDFCH DHEFE KCIAG AFBHD HEIAF BGCFC HBFEK EIAGC KCIAI AGCFE HEHBK CKDFE HDKCK DFEID FCIBG EKEGC FEGEH BIBHE IAIBH DFAFB FAGBI AGCFE HAFBH EHEFB GEFEH EGAHD FCHAI DFEKC HBFCF AHEFC GAIDK CHDIA FCKDK CHDHD FBFEH DHEFC GAHBF EKCKD FEKCF AKEGD FEFAF AFEKE KAHEF EFEHA FEKEI AFCGC KCICF EIAGC KCIAF BFAHA FBFAK EIDGC FEFAG CFEHE FEIAG CFBHE HBHDF CGBHD KCHAI AFCGE KCFAK EFCFA IAGCF EHAFC FCFAF DKAID FBFAF AFBFA GBIAG CFEHE HBKCK DFEHD KCKDF EIAGC FEKCK EHAFB FAFBH EIAHD KCIAF BFCFA GCFCH BFEKE IAFCF EHEIA KCFDG EFBHE GCIAG CFEHE IBHBH DFEHA KCKDK AFCGA IAGCF EKDKC HBFBI AKCGE FBHEI AHEKA HEIAF EHAFC ICFEH DKDFC HAHAI BFAFB HEHAK DFCID FBFAG BFCIB HDFEF AFEHA FBFEH EKCFA KEHEF EIAIA FBFAG BFCIB HDGCF EKCHD IAHEK CFAKE HAFBF AKEHE FCFAI AGCFE KDFCF AHCIB FEHEI AFCGA KCFAF EIDGA HDFCF AIAFB FEHDF DIBIA FDHDF EKBGC FAFEI CHEFE FEHAH EIBFA KEFEI AFEHD HDFEK EKCFA KEFEI CFEFA FDFEG AFCHD FEIAG CFEGE KCIBF AKDGC FCGAK CHBFC GEGEF CIEFB IAGCF EKCHA FEHDF BKDKC FAHBI BFDGE FBKDF BHEFD FEGBF BFAFA FBFAG BIAFC GBFEI AFDFC HDFEK EIAGC FEIAG CHDFE KCIAF CGAFA IBKDG EFEKC HDIDK CHDFB HEHDF BHEFB FAGBK CFAKE IAGCF EHBFE KCKDF EHBHD FCIAF EHEIA HEIDG CFBKD GCKCH DFEFB FAIAF EFAKE FEKEI AFCHB HDFEI CFEFA IAFBI AKCHD FEFEF AKDFC IBHDK CGBFB FAGBF CIBHD FEFAF EHAFB FEHEI AFCFD FEGEF BFEIC FEIAG CKCIA IAGCF EKAKD KCFAI DFBFA IDKCH DFBHE IBFAK CICFC FBKEK CFDGE FEIAF CHEIB HDICF BICFE FBIAI DFEHA IBHEI AGAFB GBGCI AFBIA FAFCI DFCFA FCIBH DFCID FAIAF EHDHA HEFDF EGAFC HDFEI AGCFE FAIBK DGEFE KCHDK CHDHE FEFAK CGEHE GBHDF CIDIA FCFCH BFCID FEHDG AIBGE FBGAF CIBHD GEFEK CKEFE HDHEI DFBGE GEFAF CIAKC KDIAF CGAIA GCFEF BHDFC IDFAI CFCGE FBIAF BFCFA IAGCF EFAFB IDFBG EGEHB HDFCI CFCGD FEIAG CFEHD FEHCI BFBHD FEKEK DFCFA GAHDF CFAIA KCIAF BFCFA FBIDF BGEGE KDFCF AICFB FAKDF EIAGC FEHAI AGCKC IAIAG CFEHD IBHEH EFBKC FAHEG CKCIC FEHEK CFDFC IAKCG BFEKE IAGCF EGEIB FAKCH DHBHD FCGBH DKCHA KCFAK EGDFB GEGEF EKEFC HDKCI AIAFE HAHBI AFEKE IAFCG DFBGE GEFCI BHDKC HEIAH DFCFA KCIBI AHEIA GCFEH DIBHE HEFBK CFAHE IDFBG EGEFA FEICF EHDFD FEKCF DGEFE IAFCH BHDFC ICFEI AGCKC IAIAG CFEKA KCHDF EFBFA FAFCK DFEFA IAKCF AKEFC IBHDH BFCGE FBIAF BKDFB KCFAH EKCFA KEGBF EFAFE HDKCG EHEID FBGEG EFDFE GAFCH DKDFE KEIAF CHEIA HDFBG DFEFD KCKDG DIAGC FEHDI BHEHE FBKCF AHEID FBGEG EFEHE KDKCG EKCIA FEFBF AFCHD KEFEH DIAFC FAFCI AGEFC FCGDI DFEKC GDKCF AKEFC FAKDF EIAGC FEKAG CKCIC FEKDF CHAHA FBIAI AFEKE IAGCF EHAHE FEGEI CFEHE IAGCF EHDFE IDFBG EGEFD FEFAF CIAIB HDFAF BFAGB FDKCK DGDIA GCFEI BFAKC ICFCF BKEKC FDGEF EIDKC HDIDF BGEGE GCKCI CFEIA FCFDF EGAFC IBGBG CIAIA FCFBI AHEKD FCFAK DGEIB HEFBF CFA".replaceAll(" ", "");
        String colHeading = "ABCDE";
        String rowHeading = "FGHIK";
        //String keyword = "ABCDEFGHIKLMNOPQRSTUVWXYZ";
        //String keyword = "NOBLEFGHIKMPQRSTUVWXYZACD";
        String keyword = "NIOBEFGHKLMPQRSTUVWXYZACD";
        Directives dirs = new Directives();
        dirs.setColHeading(colHeading);
        dirs.setRowHeading(rowHeading);
        dirs.setKeyword(keyword);
        dirs.setReplace("JI");
        String reason = polybius.canParametersBeSet(dirs);
        assertNull("CipherChallenge2019_8B", reason);
        String decoded = polybius.decode(encoding, dirs);
        System.out.println("DECODED: "+decoded);
        // ifthedeathofoursoninkoreataughtmeanythingitisthatoureffortstocontainthesovietsby  proxy  warsisdoomedasmyloathingofthesoviet...
        assertTrue("Decoding Example", decoded.contains("proxy"));
    }

    // takes around 2 mins to run, depends on size of alphabet, have seen complete in 7 seconds!
    @Test
    public void testDictionaryCrackCC2019_8B() {
        String encoding = "FBGAI AGCFE KEFEK CIAGC FCGAF CIBHD HEFCF AFBFA GDFCH DFEKC IAKCI BGBGC IAHAF EKCFA KAIAG CFBFA GBFBI AFBHE IAGCK CIAFC IBHDF EGAGA FCHDI AHEIA FCKDF CFAIA KCFBF AIAGC FEHEF CICFB FEIAH EFDKA HBHDF CIEKA IDKCH DHEFB HEKEF CFCHA FEKEK CHEHA KAGEF CKCIA GCFBF AGBFC GAIAG CFEHE FCICF BFEIA FEHAH BFBHD FEIDK CHEHE IAFCG DFEKE FDKAG CFBHE KEFEK CIAGC IAGCF EIBHE HEHDG AFEFE KEHEF CFAIA GCFEK CFAGB FEHDI AGCKC IAIDF EHEIA FBHDI BHBID FBIAG CFEKC KDGCI DKCHD IDFEG AFBGB GCIAF BFAFB IAHEH BIBHB HBFEI AHEIA KCIAF EHEID FEGCK CICFE IAHDF BFEKE IAFCK DFCFA IAKCF BFAFB IAFDI BIAFB FAFCI DHDFE KCGEF BHEFE IAGCK CIAID FEKDK CFAFC FAGEK AKEFE GAFEK CIAFB IAIAG CFEID KCKAI DFEGA FCIBG BGCIA GAKCH EKDFB HEHAF DKAIA KCGDF BFAGB FCFAF BIAHE HAFBG BGCIA FBFAF CHBFE FAIDK CHDFC IBHDH BFCGE FBIAF BKDFB KCFAH EGCKC ICFEH EGCHD IBFAG DGAHD FCHAI AGCFE KDFCF AGAGE FBKDI AKCFA KEIAG CFEKA FAFEF EKEKC HDFEK CHEFC FAIAF CKDFC FAGAH DFCFA IAIAG CFEFE ICFBG EFEIC FEFAI AGCFE IAGCH DFEKC IAFCG AFAIB KDGEF EKCHD HAFBH EHEFB GEFEH EFDKC HEFEK EFBFA KDIBF DKCKE FBKEF AFCIA GBFBI CFEIA GCFEH AIAGC FEHAF EIAIA GEFEI AFCGA FBGBG CIAFB FAHEI AFEKC KEIAG CFEKA GCKCI CFEKD FCFAI CFBFA KDFEK EIAGC FEHAH EFEGE ICFEH EIAGC KCIAI AGCFE HAKCK EKEFC KDIAH DFBFA FEFCG AHAIB IAIBK CGEGE KAKCH EHEIB HDFEK EKEFE HEIAH DIBKD IAFBF CFAID FBGEG EHBHD FCIAF EKDIA IBHEK CFAKE IAGCK CIAHB FEKCK DFEKD KCFAF DFEHA KCFBF AIAKC FBFAF EKEIB FAKEF EHDIA GCFEH EGCFE GEIAF EHDFC GAIAG CFEFA IBKDG EFEKC HDIBH AFDHD FEGEG EKCIA GCFEK AKEFC FAFCI AHEFE FEHAI AFCHE FEFEI AGCKC IAIAG CFEGE FCFAG BFEHD IDFEK DFCID FEHDF BFAIA GCFBH EHEGC KCKEF CIDIA GCFEH AFCHD FEIAG CFEIA IDFCH EFBKE FEHEI DFBGE GEFBF AICFE HEIAF BFAIA GCFEH EFEKC IDGAI BGEID FEKCH BFCFA HEFDF EGAFC HDFEG EFCFA GBFBI AIDFB GEGEF DFEFB HAHBF CHEHE FBFDG EFEIA FCGAF BGBGC IAKCI AKCGE GEIDF BIAGC FCIBI AIAGC FEIAG CHDFE KCIAF CGAIA FCIAK CGEKC FAFAF BGCFB GEKCI AFBFC FAKCF AKEID GCFEF AIAGC KCIAK EKCKA KDFCH AFEHE IDFEI DFBGE GEKCG EGEFD FEFEF AHEGE KCICF EKEFD KAIDG CFCFE ICFEH DFBHE HAFCH DFEHD IBIAG CGEFE HEHEG EFCHE FEKCF AKEID FEIDF BGEGE FDFEI AGCFE HEGEK CICFE HEIAG CFEHB HDFBK DFEFC GAIDF BFAFA FBFAG BHAFB GBGCI AFDFE IDFCH DHEFE KCIAG AFBHD HEIAF BGCFC HBFEK EIAGC KCIAI AGCFE HEHBK CKDFE HDKCK DFEID FCIBG EKEGC FEGEH BIBHE IAIBH DFAFB FAGBI AGCFE HAFBH EHEFB GEFEH EGAHD FCHAI DFEKC HBFCF AHEFC GAIDK CHDIA FCKDK CHDHD FBFEH DHEFC GAHBF EKCKD FEKCF AKEGD FEFAF AFEKE KAHEF EFEHA FEKEI AFCGC KCICF EIAGC KCIAF BFAHA FBFAK EIDGC FEFAG CFEHE FEIAG CFBHE HBHDF CGBHD KCHAI AFCGE KCFAK EFCFA IAGCF EHAFC FCFAF DKAID FBFAF AFBFA GBIAG CFEHE HBKCK DFEHD KCKDF EIAGC FEKCK EHAFB FAFBH EIAHD KCIAF BFCFA GCFCH BFEKE IAFCF EHEIA KCFDG EFBHE GCIAG CFEHE IBHBH DFEHA KCKDK AFCGA IAGCF EKDKC HBFBI AKCGE FBHEI AHEKA HEIAF EHAFC ICFEH DKDFC HAHAI BFAFB HEHAK DFCID FBFAG BFCIB HDFEF AFEHA FBFEH EKCFA KEHEF EIAIA FBFAG BFCIB HDGCF EKCHD IAHEK CFAKE HAFBF AKEHE FCFAI AGCFE KDFCF AHCIB FEHEI AFCGA KCFAF EIDGA HDFCF AIAFB FEHDF DIBIA FDHDF EKBGC FAFEI CHEFE FEHAH EIBFA KEFEI AFEHD HDFEK EKCFA KEFEI CFEFA FDFEG AFCHD FEIAG CFEGE KCIBF AKDGC FCGAK CHBFC GEGEF CIEFB IAGCF EKCHA FEHDF BKDKC FAHBI BFDGE FBKDF BHEFD FEGBF BFAFA FBFAG BIAFC GBFEI AFDFC HDFEK EIAGC FEIAG CHDFE KCIAF CGAFA IBKDG EFEKC HDIDK CHDFB HEHDF BHEFB FAGBK CFAKE IAGCF EHBFE KCKDF EHBHD FCIAF EHEIA HEIDG CFBKD GCKCH DFEFB FAIAF EFAKE FEKEI AFCHB HDFEI CFEFA IAFBI AKCHD FEFEF AKDFC IBHDK CGBFB FAGBF CIBHD FEFAF EHAFB FEHEI AFCFD FEGEF BFEIC FEIAG CKCIA IAGCF EKAKD KCFAI DFBFA IDKCH DFBHE IBFAK CICFC FBKEK CFDGE FEIAF CHEIB HDICF BICFE FBIAI DFEHA IBHEI AGAFB GBGCI AFBIA FAFCI DFCFA FCIBH DFCID FAIAF EHDHA HEFDF EGAFC HDFEI AGCFE FAIBK DGEFE KCHDK CHDHE FEFAK CGEHE GBHDF CIDIA FCFCH BFCID FEHDG AIBGE FBGAF CIBHD GEFEK CKEFE HDHEI DFBGE GEFAF CIAKC KDIAF CGAIA GCFEF BHDFC IDFAI CFCGE FBIAF BFCFA IAGCF EFAFB IDFBG EGEHB HDFCI CFCGD FEIAG CFEHD FEHCI BFBHD FEKEK DFCFA GAHDF CFAIA KCIAF BFCFA FBIDF BGEGE KDFCF AICFB FAKDF EIAGC FEHAI AGCKC IAIAG CFEHD IBHEH EFBKC FAHEG CKCIC FEHEK CFDFC IAKCG BFEKE IAGCF EGEIB FAKCH DHBHD FCGBH DKCHA KCFAK EGDFB GEGEF EKEFC HDKCI AIAFE HAHBI AFEKE IAFCG DFBGE GEFCI BHDKC HEIAH DFCFA KCIBI AHEIA GCFEH DIBHE HEFBK CFAHE IDFBG EGEFA FEICF EHDFD FEKCF DGEFE IAFCH BHDFC ICFEI AGCKC IAIAG CFEKA KCHDF EFBFA FAFCK DFEFA IAKCF AKEFC IBHDH BFCGE FBIAF BKDFB KCFAH EKCFA KEGBF EFAFE HDKCG EHEID FBGEG EFDFE GAFCH DKDFE KEIAF CHEIA HDFBG DFEFD KCKDG DIAGC FEHDI BHEHE FBKCF AHEID FBGEG EFEHE KDKCG EKCIA FEFBF AFCHD KEFEH DIAFC FAFCI AGEFC FCGDI DFEKC GDKCF AKEFC FAKDF EIAGC FEKAG CKCIC FEKDF CHAHA FBIAI AFEKE IAGCF EHAHE FEGEI CFEHE IAGCF EHDFE IDFBG EGEFD FEFAF CIAIB HDFAF BFAGB FDKCK DGDIA GCFEI BFAKC ICFCF BKEKC FDGEF EIDKC HDIDF BGEGE GCKCI CFEIA FCFDF EGAFC IBGBG CIAIA FCFBI AHEKD FCFAK DGEIB HEFBF CFA".replaceAll(" ", "");
        // different rows and column headings
        String colHeading = "ABCDE";
        String rowHeading = "FGHIK";
        String keyword = "ABCDEFGHIKLMNOPQRSTUVWXYZ"; // which letters are allowed
        Directives dirs = new Directives();
        dirs.setKeyword(keyword);
        dirs.setColHeading(colHeading);
        dirs.setRowHeading(rowHeading);
        dirs.setReplace("JI");
        dirs.setCribs("the,and,that");
        // this does not achieve a full match, since keyword for CC2019 8B is not in the dictionary, but gets close
        // close keyword is NOBLEFGHIKMPQRSTUVWXYZACD
        // real  keyword is NIOBEFGHKLMPQRSTUVWXYZACD
        dirs.setAlphabet(Settings.DEFAULT_ALPHABET);
        dirs.setLanguage(Language.instanceOf("English"));
        dirs.setCrackMethod(CrackMethod.DICTIONARY);
        String reason = polybius.canParametersBeSet(dirs);
        assertNull("CipherChallenge2019_8B", reason);
        CrackResult result = polybius.crack(encoding, dirs, 0);
        System.out.println("DECODED: "+result.getPlainText());
        System.out.println("EXPLAIN: "+result.getExplain());
//        assertTrue("Decoding Example", decoded.contains("the"));
    }

    @Test
    public void testDropLetter() {
        // encode and then decode 5x5 example found on the web
        String heading = "ABCDE";
        String square = "abcdefghiklmnopqrstuvwxyz";
        String plainText = "the quick brown fox jumps over the lazy dog";
        String expected = "DDBCAE DADEBDACBE ABDBCDEBCC BACDEC BDDECBCEDC CDEAAEDB DDBCAE CAAAEEED ADCDBB";
        Directives p = new Directives();
        p.setColHeading(heading);
        p.setRowHeading(heading);
        p.setKeyword(square);
        p.setReplace("JI");
        String reason = polybius.canParametersBeSet(p);
        assertNull("Drop Letter", reason);
        String encoded = polybius.encode(plainText, p);
        assertEquals("Encoding Example", expected, encoded);
        String decoded = polybius.decode(encoded, p);
        assertEquals("Decoding Example", plainText.replace('j','i'), decoded);
    }

    @Test
    public void testExampleFromWeb() {
        // encode and then decode 5x5 example found on the web
        String heading = "ABCDE";
        String square = "phqgmeaylnofdxkrcvszwbuti";
        String replace = "JI";
        Directives p = new Directives();
        p.setColHeading(heading);
        p.setRowHeading(heading);
        p.setKeyword(square);
        p.setReplace(replace);
        String encoded = polybius.encode("Defend the east wall of the castle", p);
        assertEquals("Encoding Example", "CCBACBBABECC EDABBA BABBDDED EABBBDBD CACB EDABBA DBBBDDEDBDBA", encoded);
        String decoded = polybius.decode(encoded, p);
        assertEquals("Decoding Example", "defend the east wall of the castle", decoded);
    }

    @Test
    public void testAllLettersAZWithNoJ() {
        // encode and then decode 5x5 example found on the web
        String heading = "ABCDE";
        String square = "ABCDEFGHIKLMNOPQRSTUVWXYZ"; // no J
        String replace = "JI";
        Directives p = new Directives();
        p.setHeadings(heading);
        p.setKeyword(square);
        p.setReplace(replace);
        String encoded = polybius.encode("The quick brown fox jumps over the Lazy Dog", p);
        assertEquals("Encoding Example", "DDBCAE DADEBDACBE ABDBCDEBCC BACDEC BDDECBCEDC CDEAAEDB DDBCAE CAAAEEED ADCDBB", encoded);
        String decoded = polybius.decode(encoded, p);
        assertEquals("Decoding Example", "the quick brown fox iumps over the lazy dog", decoded);
    }

    @Test
    public void testAllLettersAZWithNoQ() {
        // encode and then decode 5x5 example found on the web
        String heading = "24680";
        String square = "ABCDEFGHIJKLMNOPRSTUVWXYZ"; // no Q
        String replace = "QK";
        Directives p = new Directives();
        p.setColHeading(heading);
        p.setRowHeading(heading);
        p.setKeyword(square);
        p.setReplace(replace);
        String encoded = polybius.encode("The quick brown fox jumps over the Lazy Dog", p);
        assertEquals("Encoding Example", "884620 6280482662 2484600468 426006 4080668286 60022084 884620 64220008 286044", encoded);
        String decoded = polybius.decode(encoded, p);
        assertEquals("Decoding Example", "the kuick brown fox jumps over the lazy dog", decoded);
    }

    @Test
    public void test6x6Cipher() {
        // encode and then decode some text with a 6x6 polybius
        String heading = "12A45B";
        String square = "abcdefghijklmnopqrstuvwxyz1234567890";
        String plainText = "the quick brown fox jumps over the lazy dog 1 9 28";
        String expected = "422215 A54A2A1A25 12ABAA45A2 1BAA4B 244AA1A441 AA4415AB 422215 2B115251 14AA21 5A B5 54B4";
        Directives p = new Directives();
        p.setHeadings(heading);
        p.setKeyword(square);
        polybius.canParametersBeSet(p);
        String encoded = polybius.encode(plainText, p);
        assertEquals("Encoding Example", expected, encoded);
        String decoded = polybius.decode(encoded, p);
        assertEquals("Decoding Example", plainText, decoded);
    }

    @Test
    public void testCipherChallenge2017_3b() {
        // decode the text
        String heading = "XLCDM";
        String square = "ABCDEFGHIKLMNOPQRSTUVWXYZ";
        Directives p = new Directives();
        p.setHeadings(heading);
        p.setKeyword(square);
        p.setReplace("JI");
        String cipherText = "MLLCL DCXXM LDDDL DDCDD DLDMX MDDLC XXDDD CDMXM DDCDC CLDDM DCDCX MDDDM " +
                "CMDDL CXMLX LDCCX XCXXL XXDDD DCXXM MLLDD DLCXL CDDMX DLDXC XCXXX XDDML XXDDC XLDCC " +
                "LLDCD DDLXM XMDDL CXMLC XXXDC CXMLD DDLCX MDLDD LCXMD DDLCD CDCMD CCCCD DLDDL CXMDD XXXCD " +
                "DLDXC XXCXX XXLLD CXLDD DMDDD CDCDM XXMDL XCCDC LXMDD LCXML CDMLL XMCCD MCLXM DLLDX CXXCX " +
                "XDLDD CXXXD MXXXC CDDXX LLXML XXXXC XMXDX LMDLC LDDCX XDLCL MDMLL DDDLC CDDMD DDDLC XMDCD " +
                "MCMCM CDDLD DCDLX XXLLD LLDXC CDCXX XXXCC XDLCL DDCDD XXCXX MCCDD XMXDD CXCCD DMDDD CXDXM " +
                "LXXMX XDDML CDDMC XXDLC XXMXX MXLXM XMCCL DCCXM MXLDD DXXXL CXXMX XCCXD MLLDD DLCLD DDDDL " +
                "CXMCX CDDCD CCDLX XLDLL DDDXX CCCCL DXXLC XXMXL DCCLL DLXMX XCXLD DCXMX DDDLC XXDDD DLCXM " +
                "LDXCX MCCLD MLXMD LXMDL XMXXX DLDCC LLLCL DDCXC CDCLC LDMCC LDXCX XDDLD CDCCD CXXLL DLLDX " +
                "CCDCX XXXXD LDLXX CCLLX MXDDD CDCMC XXXCC DDLXX XCXDC XMLDC CLXCD DLCLX XDDLD CDCCX XXLCD " +
                "DMDDD DLCXM CMCXX XCCCC XMXDX DLDDC CMCDD CLDDD LDCDC CCDLX DDDLC DCDCM DCDCX MDDDD LDCCL " +
                "LXXDD DLXXC MLDCC DDCDM LLCLD XCLCD DLCXM LDXCX MCCLD LXXMC XCXLD DDLDD CDCDD LDCXC XDMCC " +
                "XCCXX MXXDL LCCDM LDDLC XMDCX XMXXX LLXMD DDLLD XLXMD CLCXX XDCXX MXXDL CCXMX DDDCD XDXMX " +
                "CLDCM LCXMD LCDDM DLXXD LCLMD DCXCC DCLCL DMCCL DXCXX DDLDC DCCDC XXDCD DLCXM MDLCX XXDCC " +
                "CDMLD LLDDD LDCCL LCDLX DDLCX MLDDL CDMLC CLCCD MLXMM XXMDL XMMXX MDLMD XXDLC LMDLC XXDCL " +
                "DDDDC DDDLX XLDDD CDDLD CXXCC XDLDD DDCXM XMCLD CCXLD LMXMC XMDDD CDCLX MDDLC XXDDC DCCXM " +
                "CDDLC LCDDL XMCDL XDDLC XMCXC DXCXX CXDDD LLDXL XMDCC MXMCD CMCXX MDDDM DLCCX MXDDC CDCLX " +
                "MCDCC XMDCL CXMXX XDCXC DMXXM CDDLC LCDCC XMMDL DDDLD DCXXC XCXDD DLXMX XXCLC XMDLM DXLDM " +
                "DDDDL CLDDC XXXCD DDDLC DLXMX XDDXM CCXMX DDDCD DMCCX DXMDL CLLDC CXMDD LCXMX MCLCM LDDLX " +
                "MDCXM CCDDL DDLXM DCMDD CDDXM CLCDL XDCXM XCDMD LXMXC CDCLC LDMCC LDXCX XDDLD CDCCD DLCXM " +
                "XCXXX MDCXX DLXCL DCMLC XMDLC MXMDL LCXXC MDCCL CDDLX MDDLC XXCCD LCDXX XDDCL CXXXD XMCCX " +
                "XXLCX XMXDD DLCXM XMCLC MLDDL XMDDC DLXDM CCXCD DLDCD CCDCX MXCDM DLXMC XMDXX CCXDL DDDDC " +
                "CXCDD CDCML XXDCX XXDLD DCXXD CDDXM DLDDD MDLCC LDCCL LDDLC XXDDC XCDDC DCLDC CDDCD XXMXL " +
                "DXCDD CDDLM DCDCC DDLCX MXLXX DDDDC XXMLX LDXMC XXDXD XMDCX MDLMX XMXDC LCDDL XMDLX MXCCD " +
                "LLCCL DDDLD CDCCD DLCXX CCLDD DDLXM XCXML DMXXM XDCMX MDLLC XXCMD CDCDM XMDDC DCCLD DMDCC " +
                "CXMMX XMDLL MCCXM MLCDD LCMXM DLLCX XCMDC LCXML XXXLD CXXMX DDDCD DMCCX DXMDL DCDDX XCCXD " +
                "DDLCX MXCDM CCCCL DCCLL CDLXD DLCXM CMCXX XCCXL DMDDX MLDDD LCXMD LMLXX MDXDX MDCCM LDDDX " +
                "MDDLC XMMXL DXCDD CDDLM DXXLL DLLDX CCDCX XXXXC CXDDD LCXMC CLDCC DDLCC XXMLL LDCDC CDLXM " +
                "CLXXL DCCXM XDLDC CXDLD DCLLD LXXXC XMCDC CLCXM XXDLL DCCLL DDLCL DDCCC XMMLD CXCCX XXDMX " +
                "DLDDM DCXCX XXMDC XXDLX XDMLL DMDCD DDMDC LLXMD LCLXX CCLDX CDMDC CCXMD LCDXM CLCMX MDLCD " +
                "DLLDD CDCDM XMXDX XDCXM XCDLX MDDCM DLCDX CCXXX CLXXD DLDCD CCXMD CDDXX XLCXL DDCLC LDCCL " +
                "LXXXC LDCML CXMDL DCXCL CCDCD CXDDC DXDXM MXXMC XCDCM XXCCX MMLDC MDDCD DXMCL CDLXL DCLCM " +
                "XMDLL DXXCX XCLDC MLCXM DLDCL DDDML CDDLL MXMXD LDCCD CXMXC DLXMD DLXCD DLCDM XXMDL DDMLX " +
                "MCCDD MDMDX MXXDL DCXDX MMXXM CXCDC MLDCC LLCCX MMLCL XMDDL CCDXD DCDDC DDCXM XCDMD LXMXC " +
                "CDCLC LDMCC LDXCX XDDLD CDCCD CXXXC DLCDD CDCDD LCXMX MCLCM LDDLX MDCDD DMXDM DLDCC LLDDL " +
                "CXMML CDDLL MDCCD LXDDL CXMLL DLXMX MLMDC XXLLX MDCXM DMXCC XLDXD XXCCX DLCMD CMXXD DLDXX " +
                "XXCCX DXMMX XMCCC DCXXD XMDLM LCDDL LMDCL XDLCD CLXXC CXCLD XMCCD DXLXX XLMDC XCDCC DDLCX " +
                "MDCXC LCCDC DCXXD XMMXX MCXCD CMXMX DCCXM MLMLX XMDDC DDCDD CXXLX XMLLD MXXDL XDXCC DCLCL " +
                "DMCCL DXCXX DDLDC DCCDC LXDLC DCLDL CDCLX MDCXM CCXMC LLDXM DCDDL CXMDC XMCCX MMLDC MDDCD " +
                "DXMCL DCMLX MDLXM MLDLL DDDDD XMCCX DCDML CCXXC CXDXD LDDCD DDLLD XLDMD DXMXD LDCCD DLCXM " +
                "XCCDX DXMMC CDXCX CDMCX DDCDD LDMCL CMXMD LLCXX CMDCD DLCXM CLCDD CDDDC DDDLL DXCDD CXMDL " +
                "LDMXX DLXDX MXDXD CDXCD MCLXM CCDDL DCCXX CXCXL CLDDC DDCDD LMDML LCXMC CLDDD MLXXD CLXLD " +
                "CCXXC XCXMD XCCDC LCMCX XMDDX MXDCL XXCCM DMDXM XXDLD CCXXX DDXMD LDDLC XMXMC LCMXM DLCDD " +
                "LXDCD CLLDD DLDXX CCLDD CDCDM XMXDX XCCXM MCXMX CDMDD LDMXX MCDDL XDXMD LMLLC LDXCL CCMDL " +
                "XMLXX XXCXM XDXMM XXMDL MDXCC DCMMD CDLXD DLCXM XCCDX DXMMC DDLCX XDDLD DDMLX XDCDD CDXLX " +
                "MLLDM XXDLX DXMXD XLMDX MMXXM DLMDC XXMLL LDCDC CDDCD DDLCX MCXXX DCDDC LXXCC DDLCX MXDXM " +
                "LXXMX XDDCD LXDDL CXMLD XCXMC CLDXX CCXDD DLCXM DCDML DXCLD XDXMC DLXXL CDDMX DLDXC XCXXC " +
                "MXMDL LCXXC MDCCM DLCDD DXMXC DDXMX DDDLC XMCCL DCCDD LCCXX MLLLD CDCCL XDLCD CLXDL DDCDC " +
                "CDCXD MDDLD CDCCX XLLDL LDXCC DCXXX MLXXD CCXXX DLLLX MCXMD CXXML XDDDD CDLCL DDCCD MLCCX " +
                "DXMMX LDXCX MDCLD CCDDL CXMCM DLCDM XLDCC XCXMD DLCCD DMLLL CLDDD MLXXD CCLXX XDXMX CCXXM " +
                "XXDLD DCDLC LDCLD DLCXX DDDDC DDLXM DDDMD LCCML CDDMC XXDCL XMXXC CXXDD XLXMD CDDXD LDDCL " +
                "LDLXX XCXMX XCCXD XXDDM LCDDL DCDDX DXMXX DDLCL DCCXX XLDLX MXXLM MLLDD DLCDD DLXXX DLDDD " +
                "LDCDC CDDLC XMCCL DCCDD LCDLX XLDDC XMXDX XCCXM MLDCD DXXCC XDXXD LXDDD LCXMC XXMLL LDCDC " +
                "CMLXX DCXMM CLDCX XMXDL DCCXD LDDCL LDLXX XCXMD DCDXM CCXDD MDLXM DDLCX MLCXX DLXDX XCCXD " +
                "XDXXC CLLXM DLCDD MDCML CDDLL MCDLX DCDMX LXDDM LDCCL LDDLC XMXCX XCXXM XDCDC CLDLD LDCCD " +
                "DLCXM XLCXX MXXLM MLLDC XXDXM DLCCX MDCDC XMDCC DLXXC XXCXX MXDCD CCLDX XDCDM XMDDC DCCLD " +
                "DMDCC DDLXD XMDLX MXDDD LCXMC CLDCC DDLCD DCDCL XXDLX CLCDD CDXMX LCDDL XXXCD MCLML LCLDX " +
                "CLCDD LCXMM DMLXM DLXMD DCDCL XXLMX MDDLC XMLDD LXLXX DCXML XCDDL DLXXL DXDDC LDCCD DCDXC " +
                "XXCXX MXDCD CCLDX XDDLC XMDMC CDCCM CDLMX MCCDL XMXXD CCDCC LXCDD LDDLC XMLDD LXMMC LDCXX " +
                "MMLXX DCDDL CXXDD LDCCD DXMCX CXLDL LXMCC XCXMD LXMCM CDDLD DDCDC DMLLL LXMDC DDXMX DDDLC " +
                "XXDDD DLCLD DCMLX XDCML LCXMD LXMDD LCXMM DCLLD LLLCD DLXLD CCXDD DLCXM LDDLC XCDDC DDXXD " +
                "XDMLD CXXXD DLCCD DCXMM LLCCD MLCDD MCXXD DLXMX XXDCD CCCLD MDCDD LXCDC XCXCD MLCLM DLXXX " +
                "LDDDL CLXDM CXDCC XXXMX XMDDL DDLCD MLLCC DLXXX XCXMD CXXLL DLXXM XXMDD XXDCL MCMXM DLLCX " +
                "XCMDC DDLCX MCXXX DLLLX MDCDD DDCDX DXXDD XMXXD CLCXM DDDLX XMXXM CXDCD DCDXC CDCCX CXMXX " +
                "CXDDL CXMLX CDDMD LDDLC XCLCX XCMDD XMDLC DLXDD LCLDD CDDDL XXLLL DXCDD XXCXX M";
        String decoded = polybius.decode(cipherText.replaceAll(" ",""), p);
        assertTrue("Decoding CipherText1", decoded.contains("troops"));
        assertTrue("Decoding CipherText2", decoded.contains("understand"));
        assertTrue("Decoding CipherText3", decoded.contains("perhaps"));
        assertTrue("Decoding CipherText4", decoded.contains("communication"));
        assertTrue("Decoding CipherText5", decoded.contains("tragic"));
    }

    @Test
    public void testBadParameters() {
        Directives p = new Directives();
        p.setAlphabet(null);
        String reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: alphabet missing", "Alphabet is empty or too short", reason);
        p.setAlphabet("");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: alphabet empty", "Alphabet is empty or too short", reason);
        p.setAlphabet("D");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: alphabet empty", "Alphabet is empty or too short", reason);
        p.setAlphabet(Settings.DEFAULT_ALPHABET);
        reason = polybius.canParametersBeSet(p); // keyword is null
        assertEquals("BadParam: empty keyword", "Keyword is empty or too short", reason);
        p.setKeyword("");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Keyword is empty or too short", reason);
        p.setKeyword("ABCDEFGHIJ");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: short keyword", "Keyword length must be a square number", reason);
        p.setKeyword("ABC#EFGHIJKLMNOPQRSTUVWXY"); // why is # there!
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: short keyword", "Symbol # at offset 3 in the keyword is not in the alphabet", reason);
        p.setKeyword("ABCDEFGHIJKLMNOPQRSHUVWXY"); // H is repeated
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Symbol H is repeated in the keyword", reason);

        p.setKeyword("ABCDEFGHIJKLMNOPQRSTUVWXY"); // missing Z - but 5x5 is okay
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Col heading is missing or too short", reason);
        p.setHeadings("");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Col heading is missing or too short", reason);
        p.setHeadings("ABDE"); // should be 5x5
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: short keyword", "Heading lengths should multiply to give keyword length", reason);
        p.setHeadings("ABCDC"); // C is repeated
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Symbol C is repeated in the Col heading", reason);

        p.setHeadings("12345");
        p.setReplace("X");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: replace len", "Invalid replacement length 1", reason);
        p.setReplace("Z@");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: replace len", "Replace with symbol @ must be in the keyword", reason);
        p.setReplace("BZ");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: replace len", "Replace symbol B must not be in the keyword", reason);
        p.setReplace("ZX");
        reason = polybius.canParametersBeSet(p); // now all good for encode/decode
        assertNull("BadParam: encode okay", reason);

        p.setCrackMethod(CrackMethod.IOC);
        reason = polybius.canParametersBeSet(p);
        assertEquals("Bad Param Method", "Invalid crack method", reason);
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = polybius.canParametersBeSet(p); // Crack: still missing cribs
        assertEquals("BadParam: cribs missing", "Some cribs must be provided", reason);
        p.setCribs("vostok,sputnik,saturn");
        p.setLanguage(null);
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: missing language", "Missing language", reason);
        p.setLanguage(Language.instanceOf("English"));

        p.setHeadings("");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: length 0", "Col heading is missing or too short", reason);
        p.setHeadings("123456789");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: negative length", "Col heading is too long", reason);
        p.setHeadings("12345");
        reason = polybius.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);

        p.setLanguage(Language.instanceOf("English"));
        reason = polybius.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);
    }

    @Test
    public void testCrackDictSuccess() {
        // attempt dictionary crack of Polybius cipher and succeeds with good cribs
        String keyword = "ENRGYABCDFHIKLMOPQSTUVWXZ";  // ENERGY, 5x5
        String heading = "ABCDE";
        String plainText = "I must not fear. Fear is the mind-killer. Fear is the little-death that brings total obliteration. I will face my fear. I will permit it to pass over me and through me. And when it has gone past I will turn the inner eye to see its path. Where the fear has gone there will be nothing. Only I will remain.";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setHeadings(heading);
        String reason = polybius.canParametersBeSet(p);
        assertNull("CrackDictSuccess: encode param okay", reason);
        String cipherText = polybius.encode(plainText, p);
        assertNotNull("CrackDictSuccess: Encoding", cipherText);

        String decodedText = polybius.decode(cipherText, p);
        assertNotNull("CrackDictSuccess: Decoding", decodedText);

        // now attempt the crack of the text via Dictionary
        p.setKeyword(null);
        p.setHeadings("ABCDE");
        p.setCribs("total,fear,killer");
        p.setLanguage(Language.instanceOf("English"));
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = polybius.canParametersBeSet(p);
        assertNull("CrackDictSuccess: crack param okay", reason);

        CrackResult result = polybius.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        assertTrue("CrackDict success", result.isSuccess());
        assertEquals("CrackDict Cipher", cipherText, result.getCipherText());
        assertEquals("CrackDict Text", plainText.toLowerCase(), result.getPlainText());
        assertEquals("CrackDict Keyword", keyword, decodeKeyword);
        assertNotNull("CrackDict Explain", explain);
        assertEquals("CrackDict cipher name", "Polybius cipher (ENRGYABCDFHIKLMOPQSTUVWXZ,cols=ABCDE,rows=ABCDE)", result.getCipher().getInstanceDescription());
        assertEquals("CrackDict crack method", CrackMethod.DICTIONARY, result.getCrackMethod());
    }

    @Test
    public void testCrackDictReverseSuccess() {
        // attempt dictionary crack of Polybius cipher and succeeds with good cribs
        String keyword = "ENRGYABCDFHIKLMOPQSTUVWXZ";  // ENERGY, 5x5
        String heading = "ABCDE";
        String plainText = "I must not fear. Fear is the mind-killer. Fear is the little-death that brings total obliteration. I will face my fear. I will permit it to pass over me and through me. And when it has gone past I will turn the inner eye to see its path. Where the fear has gone there will be nothing. Only I will remain.";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setHeadings(heading);
        String reason = polybius.canParametersBeSet(p);
        assertNull("CrackDictSuccess: encode param okay", reason);
        String cipherText = polybius.encode(new StringBuilder(plainText).reverse().toString(), p);
        assertNotNull("CrackDictSuccess: Encoding", cipherText);

        String decodedText = polybius.decode(cipherText, p);
        assertNotNull("CrackDictSuccess: Decoding", decodedText);

        // now attempt the crack of the text via Dictionary
        p.setKeyword(null);
        p.setHeadings("ABCDE");
        p.setCribs("total,fear,killer");
        p.setLanguage(Language.instanceOf("English"));
        p.setCrackMethod(CrackMethod.DICTIONARY);
        p.setConsiderReverse(true);
        reason = polybius.canParametersBeSet(p);
        assertNull("CrackDictSuccess: crack param okay", reason);

        CrackResult result = polybius.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        String decodeKeyword = result.getDirectives().getKeyword();
        System.out.println("Keyword "+decodeKeyword);
        assertTrue("CrackDict success", result.isSuccess());
        assertEquals("CrackDict Cipher", cipherText, result.getCipherText());
        assertEquals("CrackDict Text", plainText.toLowerCase(), result.getPlainText());
        assertEquals("CrackDict Keyword", keyword, decodeKeyword);
        assertNotNull("CrackDict Explain", explain);
        assertEquals("CrackDict cipher name", "Polybius cipher (ENRGYABCDFHIKLMOPQSTUVWXZ,cols=ABCDE,rows=ABCDE)", result.getCipher().getInstanceDescription());
        assertEquals("CrackDict crack method", CrackMethod.DICTIONARY, result.getCrackMethod());
    }

    @Test
    public void testCrackDictFail() {
        // attempt dictionary crack of Vigenere cipher but fails as keyword (MOBYDICK) is not in the dictionary
        String keyword = "ENRGYABCDFHIKLMOPQSTUVWXZ";  // ENERGY, 5x5
        String heading = "ABCDE";
        String plainText = "I must not fear. Fear is the mind-killer. Fear is the little-death that brings total obliteration. I will face my fear. I will permit it to pass over me and through me. And when it has gone past I will turn the inner eye to see its path. Where the fear has gone there will be nothing. Only I will remain.";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setHeadings(heading);
        String reason = polybius.canParametersBeSet(p);
        assertNull("CrackDictFail encode param okay", reason);
        String cipherText = polybius.encode(plainText, p);
        assertNotNull("CrackDictFail: Encoding", cipherText);

        // now attempt the crack of the text via Dictionary
        p.setKeyword(null);
        p.setHeadings("ABCDE");
        p.setLanguage(Language.instanceOf("English"));
        p.setCribs("purse,november,ocean"); // bad cribs => that's why it fails
        p.setCrackMethod(CrackMethod.DICTIONARY);
        reason = polybius.canParametersBeSet(p);
        assertNull("CrackDictFail: crack param okay", reason);

        CrackResult result = polybius.crack(cipherText, p, 0);
        System.out.println("Decoded "+result.getPlainText());
        String explain = result.getExplain();
        System.out.println("Explain "+explain);
        assertFalse("CrackDict Fail success", result.isSuccess());
        assertNull("CrackDict Fail Text", result.getPlainText());
        assertEquals("CrackDict Fail Cipher", cipherText, result.getCipherText());
        assertNull("CrackDict Fail Keyword", result.getDirectives());
        assertNotNull("CrackDict Fail Explain", explain);
        assertEquals("CrackDict Fail cipher name", "Polybius cipher (n/a)", result.getCipher().getInstanceDescription());
        assertEquals("CrackDict Fail crack method", CrackMethod.DICTIONARY, result.getCrackMethod());
    }

    @Test
    public void testDescription() {
        String desc = polybius.getCipherDescription();
        assertNotNull("Description", desc);
        assertTrue("Description content", desc.contains("Polybius cipher"));
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setKeyword("AFTERBCDGHIKLMNOPQSUVWXYZ");
        p.setColHeading("ABCDE");
        p.setRowHeading("FGHIJ");
        String reason = polybius.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = polybius.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Polybius cipher (AFTERBCDGHIKLMNOPQSUVWXYZ,cols=ABCDE,rows=FGHIJ)", desc);
    }
}
