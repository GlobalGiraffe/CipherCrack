package mnh.game.ciphercrack.cipher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class CipherTest {

    @Test
    public void testSetAndGetCribs() {
        String text = "How I have th e best of t hem all";
        String cribString = " how, the ";
        Set<String> cribs = Cipher.getCribSet(cribString);
        assertEquals("Cribset Size", 2, cribs.size());
        assertTrue("Cribset First", cribs.contains("HOW"));
        assertTrue("Cribset Second", cribs.contains("THE"));
        boolean present = Cipher.containsAllCribs(text, cribs);
        assertTrue("Contains all cribs with case/spaces", present);
    }

}
