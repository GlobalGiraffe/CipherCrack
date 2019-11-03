package mnh.game.ciphercrack.cipher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.Settings;

import static org.junit.Assert.*;

/**
 * Test out the Caesar Cipher code
 */
@RunWith(JUnit4.class)
public class CaesarTest {

    private static final String defaultAlphabet = Settings.DEFAULT_ALPHABET;
    private static final Caesar caesar = new Caesar(null);

    @Test
    public void testIncorrectProperties() {
        // ensure we get a warning if bad parameters set
        Directives p = new Directives();
        String reason = caesar.canParametersBeSet(p);
        assertEquals("BadParam Alpha missing", "Alphabet is empty or too short", reason);
        p.setAlphabet("");
        reason = caesar.canParametersBeSet(p);
        assertEquals("BadParam Alpha empty", "Alphabet is empty or too short", reason);
        p.setAlphabet("A");
        reason = caesar.canParametersBeSet(p);
        assertEquals("BadParam Alpha too short", "Alphabet is empty or too short", reason);

        p.setAlphabet(defaultAlphabet);
        p.setShift(-1);
        reason = caesar.canParametersBeSet(p);
        assertEquals("BadParam shift negative", "Shift value -1 incorrect, should be between 0 and 25", reason);

        p.setShift(26);
        reason = caesar.canParametersBeSet(p);
        assertEquals("BadParam shift too big", "Shift value 26 incorrect, should be between 0 and 25", reason);
        p.setShift(25);
        reason = caesar.canParametersBeSet(p);
        assertNull("BadParam all okay", reason);

        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        reason = caesar.canParametersBeSet(p);
        assertEquals("BadParam cribs null", "Some cribs must be provided", reason);
        p.setCribs("");
        reason = caesar.canParametersBeSet(p);
        assertEquals("BadParam cribs null", "Some cribs must be provided", reason);
        p.setCribs("home,truth");
        reason = caesar.canParametersBeSet(p);
        assertNull("BadParam all okay crack", reason);
    }

    @Test
    public void testDefaultAlphabet() {
        // encode and then decode a mixed case cipher
        Directives p = new Directives();
        p.setAlphabet(defaultAlphabet);
        p.setShift(3);
        String encoded = caesar.encode("AbCdEfGhIjKlMnOpQrStUvWxYZaBcD", p);
        assertEquals("Encoding default Alphabet mixed case", "DeFgHiJkLmNoPqRsTuVwXyZaBCdEfG", encoded);
        String decoded = caesar.decode(encoded, p);
        assertEquals("Decoding default Alphabet mixed case", "AbCdEfGhIjKlMnOpQrStUvWxYZaBcD", decoded);
    }

    @Test
    public void testShortAlphabet() {
        // encode and then decode an upper case cipher using a short alphabet
        final String shortAlphabet = "AEIOUY";
        Directives p = new Directives();
        p.setAlphabet(shortAlphabet);
        p.setShift(2);
        String reason = caesar.canParametersBeSet(p);
        assertNull("Encoding reason", reason);
        String encoded = caesar.encode("AOUYIEAOEY", p);
        assertEquals("Encoding short Alphabet upper case", "IYAEUOIYOE", encoded);
        p.setShift(3);
        String decoded = caesar.decode(encoded, p);
        assertEquals("Decoding short Alphabet upper case", "YIOUEAYIAU", decoded);
    }

    @Test
    public void testClassic() {
        // as per http://practicalcryptography.com/ciphers/caesar-cipher/
        Directives p = new Directives();
        p.setAlphabet(defaultAlphabet);
        p.setShift(1);
        String reason = caesar.canParametersBeSet(p);
        assertNull("Encoding classic reason", reason);
        String encoded = caesar.encode("defend the east wall of the castle\n", p);
        assertEquals("Encoding classic", "efgfoe uif fbtu xbmm pg uif dbtumf\n", encoded);
        String decoded = caesar.decode(encoded, p);
        assertEquals("Decoding classic", "defend the east wall of the castle\n", decoded);
    }

    @Test
    public void testCrackSuccess() {
        String text = "Urndc. Pnwnaju cqn Qxw. Bra J. Fnuunbunh, T.K., cx Lxvvrbbjah Pnwnaju Vdaajh.\n" +
                "Vh Mnja Bra,\n" +
                "R qjen anlnrenm hxda unccna lxwlnawrwp cqn nbcjkurbqvnwc xo cqn Pdrmnb, jwm Blxenuu rb cx kn\n" +
                "lxwpajcdujcnm xw qrb vxbc nglnuunwc cajrwrwp anprvn. Xwln yaxerbrxwnm, cqn Pdrmnb jan cx sxrw xda vjrw\n" +
                "oxaln jc Kjmjsxi.\n" +
                "Rc rb lunja cx vn cqjc Bxduc'b rwcnwcrxw fruu kn cx ldc xoo xda urwn xo ancanjc jwm rc rb cqnanoxan\n" +
                "wnlnbbjah cqjc fn yanyjan j mnonwbren urwn cx cqn anja jc Cxaanb Enmajb. Cx cqrb nwm Lxuxwnu Ounclqna\n" +
                "fruu kdrum j bnarnb xo mrclqnb, oxacb jwm xcqna nvyujlnvnwcb cx buxf jwh jccjlt dyxw cqjc urwn. Cqnbn fxatb\n" +
                "vdbc kn mduh yaxerbrxwnm kh hxda vnjbdanb cx yaxldan vxwnh oxa kruub dyxw Nwpujwm.\n" +
                "Cx knccna xda xmmb xo bdllnbb, Blxenuu'b Pdrmnb jan cx xapjwrbn j ljvyjrpw xo pdnaruuj jlcrxw, qjaahrwp cqn\n" +
                "Oanwlq oxalnb jc nenah cdaw, jwm qn rb rwbcadlcnm cx ngyjwm qrb wncfxat xo byrnb. Rw cqrb qn fruu fxat frcq\n" +
                "cqn Yxacdpdnbn Karpjmnb lxvvjwmnm kh Knanboxam jwm Qjamrwpn.\n" +
                "Cqn jccjlqnm lxvvdwrljcrxw fjb oxafjamnm cx vn oaxv cqn Byjwrbq pdnaruujb, fqx oxdwm rc rw cqn yxbbnbbrxw\n" +
                "xo j vxbc dwoxacdwjcn vxwt, yanbdvjkuh jw joajwljbjmx. Cqn vnbbjpn anvjrwb luxbnm cx vn, jwm R fxdum kn\n" +
                "pajcnodu ro hxd fxdum lxwenh rcb vnjwrwp jb j vjccna xo cqn qrpqnbc yarxarch. Cqrb vjh yaxynauh kn jwxcqna\n" +
                "cjbt oxa Blxenuu, fqxbn fxat rw cqrb ornum rb wxc dwwxcrlnm jvxwp cqn pnwnaju bcjoo.\n" +
                "Knurnen vn, ncl. Jacqda Fnuunbunh.\n";

        String expected = "Lieut. General the Hon. Sir A. Wellesley, K.B., to Commissary General Murray.\n" +
                "My Dear Sir,\n" +
                "I have received your letter concerning the establishment of the Guides, and Scovell is to be\n" +
                "congratulated on his most excellent training regime. Once provisioned, the Guides are to join our main\n" +
                "force at Badajoz.\n" +
                "It is clear to me that Soult's intention will be to cut off our line of retreat and it is therefore\n" +
                "necessary that we prepare a defensive line to the rear at Torres Vedras. To this end Colonel Fletcher\n" +
                "will build a series of ditches, forts and other emplacements to slow any attack upon that line. These works\n" +
                "must be duly provisioned by your measures to procure money for bills upon England.\n" +
                "To better our odds of success, Scovell's Guides are to organise a campaign of guerilla action, harrying the\n" +
                "French forces at every turn, and he is instructed to expand his network of spies. In this he will work with\n" +
                "the Portuguese Brigades commanded by Beresford and Hardinge.\n" +
                "The attached communication was forwarded to me from the Spanish guerillas, who found it in the possession\n" +
                "of a most unfortunate monk, presumably an afrancasado. The message remains closed to me, and I would be\n" +
                "grateful if you would convey its meaning as a matter of the highest priority. This may properly be another\n" +
                "task for Scovell, whose work in this field is not unnoticed among the general staff.\n" +
                "Believe me, etc. Arthur Wellesley.\n";
        Directives p = new Directives();
        p.setCribs("the,and,have");
        p.setAlphabet(defaultAlphabet);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        String reason = caesar.canParametersBeSet(p);
        assertNull("Crack caesar reason", reason);
        CrackResult result = caesar.crack(text, p);
        assertTrue("Crack caesar state", result.isSuccess());
        assertEquals("Crack caesar text", expected, result.getPlainText());
        assertEquals("Crack caesar cipher text", text, result.getCipherText());
        assertEquals("Crack caesar shift", 9, result.getDirectives().getShift());
        assertNotNull("Crack caesar explain", result.getExplain());
    }

    @Test
    public void testCrackFail() {
        String cipherText = "Urndc. Pnwnaju cqn Qxw. Bra J. Fnuunbunh, T.K., cx Lxvvrbbjah Pnwnaju Vdaajh.\n" +
                "Vh Mnja Bra,\n";
        Directives p = new Directives();
        p.setCribs("presumably");
        p.setAlphabet(defaultAlphabet);
        p.setCrackMethod(CrackMethod.BRUTE_FORCE);
        String reason = caesar.canParametersBeSet(p);
        assertNull("CrackFail caesar reason", reason);

        CrackResult result = caesar.crack(cipherText, p);
        assertFalse("Crack caesar state", result.isSuccess());
        assertNull("Crack caesar text", result.getPlainText());
        assertEquals("Crack caesar cipher text", cipherText, result.getCipherText());
        assertNull("Crack caesar shift", result.getDirectives());
        assertNotNull("Crack caesar explain", result.getExplain());
    }

    @Test
    public void testDescription() {
        String desc = caesar.getCipherDescription();
        assertNotNull("Description", desc);
    }

    @Test
    public void testInstanceDescription() {
        Directives p = new Directives();
        p.setAlphabet(defaultAlphabet);
        p.setShift(21);
        String reason = caesar.canParametersBeSet(p);
        assertNull("Null reason", reason);
        String desc = caesar.getInstanceDescription();
        assertNotNull("Instance Description", desc);
        assertEquals("Instance Description", "Caesar cipher (shift=21)", desc);
    }
}