package mnh.game.ciphercrack.util;

import android.content.Context;
import android.os.Parcel;

import org.junit.Test;

import androidx.test.platform.app.InstrumentationRegistry;
import mnh.game.ciphercrack.cipher.Cipher;
import mnh.game.ciphercrack.cipher.Vigenere;
import mnh.game.ciphercrack.language.Language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
//@RunWith(AndroidJUnit4.class)
public class CrackResultParcelableTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("mnh.game.ciphercrack", appContext.getPackageName());
    }

    @Test
    public void testParcelableCrackResult() {
        // create and fill in a crack result object
        Vigenere cipher = (Vigenere)Cipher.instanceOf("Vigenere",null);
        Directives dirs = new Directives();
        dirs.setKeyword("CAPTAIN");
        dirs.setCribs("the");
        dirs.setLanguage(Language.instanceOf("English"));
        dirs.setAlphabet(Settings.DEFAULT_ALPHABET);
        String cipherText = "AHDHEUEHH";
        String plainText = "IAMTHEONE";
        String explain = "explanation";
        CrackResult cr = new CrackResult(CrackMethod.NONE, cipher, dirs, cipherText, plainText, explain);
        cr.setMilliseconds(10102L);

        // duplicate the CrackResult via parcelling and unparcelling
        Parcel parcel = Parcel.obtain();
        cr.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        CrackResult cr2 = new CrackResult(parcel);

        // now check new and old are the same
        assertNotNull("ParcelCR Not Null", cr2);
        assertEquals("ParcelCR getId",cr.getId(), cr2.getId());
        assertEquals("ParcelCR getPlainText",cr.getPlainText(), cr2.getPlainText());
        assertEquals("ParcelCR getCipherText",cr.getCipherText(), cr2.getCipherText());
        assertEquals("ParcelCR getExplain",cr.getExplain(), cr2.getExplain());
        assertEquals("ParcelCR getMilliseconds",cr.getMilliseconds(), cr2.getMilliseconds());
        assertEquals("ParcelCR getDirsKeyword",cr.getDirectives().getKeyword(), cr2.getDirectives().getKeyword());
        assertEquals("ParcelCR getDirsCribs",cr.getDirectives().getCribs(), cr2.getDirectives().getCribs());
        assertEquals("ParcelCR getDirsLanguage",cr.getDirectives().getLanguage(), cr2.getDirectives().getLanguage());
        assertEquals("ParcelCR getDirsAlphabet",cr.getDirectives().getAlphabet(), cr2.getDirectives().getAlphabet());
        assertEquals("ParcelCR getDirsMethod",cr.getDirectives().getCrackMethod(), cr2.getDirectives().getCrackMethod());
        assertEquals("ParcelCR isSuccess",cr.isSuccess(), cr2.isSuccess());
        assertEquals("ParcelCR getCipher",cr.getCipher().getInstanceDescription(), cr2.getCipher().getInstanceDescription());
    }
}
