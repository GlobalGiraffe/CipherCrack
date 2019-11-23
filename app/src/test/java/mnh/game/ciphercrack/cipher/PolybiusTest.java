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
public class PolybiusTest {

    private final Polybius polybius = new Polybius(null);

    @Test
    public void testDropLetter() {
        // encode and then decode 5x5 example found on the web
        String heading = "ABCDE";
        String square = "abcdefghiklmnopqrstuvwxyz";
        String plainText = "the quick brown fox jumps over the lazy dog";
        String expected = "DDBCAE DADEBDACBE ABDBCDEBCC BACDEC BDDECBCEDC CDEAAEDB DDBCAE CAAAEEED ADCDBB";
        Directives p = new Directives();
        p.setHeading(heading);
        p.setKeyword(square);
        p.setReplace("JI");
        polybius.canParametersBeSet(p);
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
        p.setHeading(heading);
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
        p.setHeading(heading);
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
        p.setHeading(heading);
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
        String plainText = "the quick brown fox jumps over the lazy dog 1 9 2 8";
        String expected = "422215A54A2A1A2512ABAA45A21BAA4B244AA1A441AA4415AB4222152B11525114AA215AB554B4";
        Directives p = new Directives();
        p.setHeading(heading);
        p.setKeyword(square);
        polybius.canParametersBeSet(p);
        String encoded = polybius.encode(plainText, p);
        assertEquals("Encoding Example", expected, encoded.replaceAll(" ", ""));
        String decoded = polybius.decode(encoded, p);
        assertEquals("Decoding Example", plainText, decoded);
    }

    @Test
    public void testCipherChallenge2017_3b() {
        // decode the text
        String heading = "XLCDM";
        String square = "ABCDEFGHIKLMNOPQRSTUVWXYZ";
        Directives p = new Directives();
        p.setHeading(heading);
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
        assertEquals("BadParam: empty keyword", "Heading is missing or too short", reason);
        p.setHeading("");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Heading is missing or too short", reason);
        p.setHeading("ABDE"); // should be 5x5
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: short keyword", "Heading length must be square-root of keyword length", reason);
        p.setHeading("ABCDC"); // C is repeated
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: empty keyword", "Symbol C is repeated in the Heading", reason);

        p.setHeading("12345");
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
        p.setLanguage(Language.instanceOf("German"));
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: missing language", "No German dictionary is defined", reason);
        p.setLanguage(Language.instanceOf("English"));

        p.setHeading("");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: length 0", "Heading is missing or too short", reason);
        p.setHeading("1234567");
        reason = polybius.canParametersBeSet(p);
        assertEquals("BadParam: negative length", "Heading too long to crack", reason);
        p.setHeading("12345");
        reason = polybius.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);

        p.setLanguage(Language.instanceOf("English"));
        reason = polybius.canParametersBeSet(p);
        assertNull("BadParam: crack okay", reason);
    }

    @Test
    public void testCrackDictSuccess() {
        // attempt dictionary crack of Vigenere cipher and succeeds with good cribs
        String keyword = "ENRGYABCDFHIKLMOPQSTUVWXZ";  // ENERGY, 5x5
        String heading = "ABCDE";
        String plainText = "I must not fear. Fear is the mind-killer. Fear is the little-death that brings total obliteration. I will face my fear. I will permit it to pass over me and through me. And when it has gone past I will turn the inner eye to see its path. Where the fear has gone there will be nothing. Only I will remain.";
        Directives p = new Directives();
        p.setKeyword(keyword);
        p.setHeading(heading);
        String reason = polybius.canParametersBeSet(p);
        assertNull("CrackDictSuccess: encode param okay", reason);
        String cipherText = polybius.encode(plainText, p);
        assertNotNull("CrackDictSuccess: Encoding", cipherText);

        String decodedText = polybius.decode(cipherText, p);
        assertNotNull("CrackDictSuccess: Decoding", decodedText);

        // now attempt the crack of the text via Dictionary
        p.setKeyword(null);
        p.setHeading("ABCDE");
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
        assertEquals("CrackDict cipher name", "Polybius cipher (ENRGYABCDFHIKLMOPQSTUVWXZ,heading=ABCDE)", result.getCipher().getInstanceDescription());
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
        p.setHeading(heading);
        String reason = polybius.canParametersBeSet(p);
        assertNull("CrackDictFail encode param okay", reason);
        String cipherText = polybius.encode(plainText, p);
        assertNotNull("CrackDictFail: Encoding", cipherText);

        // now attempt the crack of the text via Dictionary
        p.setKeyword(null);
        p.setHeading("ABCDE");
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
        p.setHeading("ABCDE");
        String reason = polybius.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = polybius.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Polybius cipher (AFTERBCDGHIKLMNOPQSUVWXYZ,heading=ABCDE)", desc);
    }
}
