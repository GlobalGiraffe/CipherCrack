package mnh.game.ciphercrack.cipher;

import android.os.Parcel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

//@RunWith(AndroidJUnit4.class)
public class CipherParcelableTest {

    @Test
    public void testVigenereParcelable() {
        Vigenere cipher = (Vigenere)Cipher.instanceOf("Vigenere", null);
        assert cipher != null;
        cipher.keyword = "HELLO";
        Parcel parcel = Parcel.obtain();
        cipher.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Vigenere cipher2 = (Vigenere)Cipher.instanceOf(parcel, null);
        assert cipher2 != null;
        assertEquals("Vigenere Description","Vigenere cipher (HELLO)", cipher2.getInstanceDescription());
        assertEquals("Vigenere Keyword", cipher.keyword, cipher2.keyword);
    }

    @Test
    public void testVigenereParcelableEmpty() {
        Vigenere cipher = (Vigenere)Cipher.instanceOf("Vigenere", null);
        assert cipher != null;
        cipher.keyword = "";
        Parcel parcel = Parcel.obtain();
        cipher.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Vigenere cipher2 = (Vigenere)Cipher.instanceOf(parcel, null);
        assert cipher2 != null;
        assertEquals("Vigenere Empty Description","Vigenere cipher ()", cipher2.getInstanceDescription());
        assertEquals("Vigenere Empty Keyword",cipher.keyword, cipher2.keyword);
    }

    @Test
    public void testVigenereParcelableNull() {
        Vigenere cipher = (Vigenere)Cipher.instanceOf("Vigenere", null);
        assert cipher != null;
        cipher.keyword = null;
        Parcel parcel = Parcel.obtain();
        cipher.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Vigenere cipher2 = (Vigenere)Cipher.instanceOf(parcel, null);
        assert cipher2 != null;
        assertEquals("Vigenere Null Description","Vigenere cipher (n/a)", cipher2.getInstanceDescription());
        assertNull("Vigenere Null Keyword", cipher2.keyword);
    }

    @Test
    public void testPermutationParcelable() {
        Permutation cipher = (Permutation)Cipher.instanceOf("Permutation", null);
        assert cipher != null;
        cipher.permutation = new int[4];
        cipher.permutation[0] = 1;
        cipher.permutation[1] = 2;
        cipher.permutation[2] = 0;
        cipher.permutation[3] = 3;
        cipher.readAcross = true;
        Parcel parcel = Parcel.obtain();
        cipher.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Permutation cipher2 = (Permutation)Cipher.instanceOf(parcel, null);
        assert cipher2 != null;
        assertEquals("Permutation Description","Permutation cipher (1,2,0,3:across)", cipher2.getInstanceDescription());
        assertTrue("Permutation Across", cipher2.readAcross);
    }

    @Test
    public void testPermutationParcelableEmpty() {
        Permutation cipher = (Permutation)Cipher.instanceOf("Permutation", null);
        assert cipher != null;
        cipher.permutation = new int[0];
        cipher.readAcross = false;
        Parcel parcel = Parcel.obtain();
        cipher.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Permutation cipher2 = (Permutation)Cipher.instanceOf(parcel, null);
        assert cipher2 != null;
        assertEquals("Permutation Empty Description","Permutation cipher (:down)", cipher2.getInstanceDescription());
        assertFalse("Permutation Empty Across", cipher2.readAcross);
    }

    @Test
    public void testPermutationParcelableNull() {
        Permutation cipher = (Permutation)Cipher.instanceOf("Permutation", null);
        assert cipher != null;
        cipher.permutation = null;
        cipher.readAcross = true;
        Parcel parcel = Parcel.obtain();
        cipher.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Permutation cipher2 = (Permutation)Cipher.instanceOf(parcel, null);
        assert cipher2 != null;
        assertEquals("Permutation Null Description","Permutation cipher (:across)", cipher2.getInstanceDescription());
        assertTrue("Permutation Null Across", cipher2.readAcross);
    }

    @Test
    public void testCaesarParcelable() {
        Caesar cipher = (Caesar)Cipher.instanceOf("Caesar", null);
        assert cipher != null;
        cipher.shift = 18;
        Parcel parcel = Parcel.obtain();
        cipher.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Caesar cipher2 = (Caesar)Cipher.instanceOf(parcel, null);
        assert cipher2 != null;
        assertEquals("Caesar Description","Caesar cipher (shift=18)", cipher2.getInstanceDescription());
        assertEquals("Caesar Shift", cipher.shift, cipher2.shift);
    }

    @Test
    public void testCaesarParcelableNull() {
        Caesar cipher = (Caesar)Cipher.instanceOf("Caesar", null);
        assert cipher != null;
        cipher.shift = -1;
        Parcel parcel = Parcel.obtain();
        cipher.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Caesar cipher2 = (Caesar)Cipher.instanceOf(parcel, null);
        assert cipher2 != null;
        assertEquals("Caesar Null Description","Caesar cipher (n/a)", cipher2.getInstanceDescription());
        assertEquals("Caesar Null Shift", cipher.shift, cipher2.shift);
    }
}
