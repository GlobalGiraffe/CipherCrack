package mnh.game.ciphercrack;

import android.os.Looper;

import org.junit.Before;
import org.junit.Test;

import androidx.test.filters.SmallTest;

//@RunWith(AndroidJUnit4.class)
@SmallTest
public class HomeActivityTest {

    @Before
    public void prepareLooper() {
        Looper.prepare();
    }

    @Test
    public void testHomeActivity() {
       HomeActivity homeActivity = new HomeActivity();
       // homeActivity.performDecode(null);
    }

}
