package mnh.game.ciphercrack.util;

import android.content.Context;
import android.os.Parcel;

import androidx.test.platform.app.InstrumentationRegistry;
import mnh.game.ciphercrack.language.Language;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
//@RunWith(AndroidJUnit4.class)
public class DirectivesParcelableTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("mnh.game.ciphercrack", appContext.getPackageName());
    }

    @Test
    public void testParcelableDirectives() {
        // create and fill in a set of directives
        Directives dir1 = new Directives();
        dir1.setLanguage(Language.instanceOf("English"));
        dir1.setAlphabet("ABCDEFGHIJKLMNN");
        dir1.setCribs("hello,and,now");
        dir1.setShift(5);
        dir1.setValueA(10);
        dir1.setValueB(-10);
        dir1.setRails(14);
        dir1.setKeywordLength(9);
        dir1.setKeyword("ALBATROS");
        dir1.setDigits("XY");
        dir1.setSeparator("^");
        dir1.setNumberSize(5);
        dir1.setPermutation(new int[] {1,3,4,2,0});
        dir1.setReadAcross(true);
        dir1.setStopAtFirst(true);
        dir1.setConsiderReverse(true);
        dir1.setCrackMethod(CrackMethod.WORD_COUNT);

        // duplicate the directives
        Parcel parcel = Parcel.obtain();
        dir1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Directives dir2 = new Directives(parcel);

        // now check new and old are the same
        assertNotNull("ParcelDir Not Null", dir2);
        assertEquals("ParcelDir Lang",dir1.getLanguage(), dir2.getLanguage());
        assertEquals("ParcelDir Alpha",dir1.getAlphabet(), dir2.getAlphabet());
        assertEquals("ParcelDir Cribs",dir1.getCribs(), dir2.getCribs());
        assertEquals("ParcelDir Shift",dir1.getShift(), dir2.getShift());
        assertEquals("ParcelDir ValueA",dir1.getValueA(), dir2.getValueA());
        assertEquals("ParcelDir ValueB",dir1.getValueB(), dir2.getValueB());
        assertEquals("ParcelDir Rails",dir1.getRails(), dir2.getRails());
        assertEquals("ParcelDir KeywordLength",dir1.getKeywordLength(), dir2.getKeywordLength());
        assertEquals("ParcelDir Digits",dir1.getDigits(), dir2.getDigits());
        assertEquals("ParcelDir Separator",dir1.getSeparator(), dir2.getSeparator());
        assertEquals("ParcelDir NumberSize",dir1.getNumberSize(), dir2.getNumberSize());
        assertEquals("ParcelDir Permutation len",dir1.getPermutation().length, dir2.getPermutation().length);
        assertEquals("ParcelDir Permutation 0",dir1.getPermutation()[0], dir2.getPermutation()[0]);
        assertEquals("ParcelDir Permutation 1",dir1.getPermutation()[1], dir2.getPermutation()[1]);
        assertEquals("ParcelDir Permutation 2",dir1.getPermutation()[2], dir2.getPermutation()[2]);
        assertEquals("ParcelDir Permutation 3",dir1.getPermutation()[3], dir2.getPermutation()[3]);
        assertEquals("ParcelDir Permutation 4",dir1.getPermutation()[4], dir2.getPermutation()[4]);
        assertEquals("ParcelDir ReadAcross",dir1.isReadAcross(), dir2.isReadAcross());
        assertEquals("ParcelDir StopAtFirst",dir1.stopAtFirst(), dir2.stopAtFirst());
        assertEquals("ParcelDir ConsiderReverse",dir1.considerReverse(), dir2.considerReverse());
        assertEquals("ParcelDir CrackMethod",dir1.getCrackMethod(), dir2.getCrackMethod());
    }
}
