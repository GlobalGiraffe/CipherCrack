package mnh.game.ciphercrack.transform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test out the Remove Punctuation Transform
 */
@RunWith(JUnit4.class)
public class RemovePunctuationTest {

    private static final RemovePunctuation transform = new RemovePunctuation();

    @Test
    public void testRemovePunctuation() {
        String result = transform.apply(null, "A, Message! -for% &you: and; (me)");
        assertEquals("Remove Punctuation 1", "A Message for you and me", result);
        result = transform.apply(null, "A, Message! -for% &you: and; (me) okay");
        assertEquals("Remove Punctuation 2", "A Message for you and me okay", result);
        result = transform.apply(null, "!*^%$,.:;");
        assertEquals("Remove Punctuation 3", "", result);
        result = transform.apply(null, "!*A^%$,B.:C;");
        assertEquals("Remove Punctuation 4", "ABC", result);
        String inputText = "No punctuation here as you can see";
        result = transform.apply(null, inputText);
        assertEquals("Remove No Punctuation", inputText, result);
    }

    @Test
    public void testRemovePunctuationLong() {
        String plainText = "Call me Ishmael. Some years ago — never mind how long precisely - having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen, and regulating the circulation. Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people’s hats off — then, I account it high time to get to sea as soon as I can. This is my substitute for pistol and ball. With a philosophical flourish Cato throws himself upon his sword; I quietly take to the ship. There is nothing surprising in this. If they but knew it, almost all men in their degree, some time or other, cherish very nearly the same feelings towards the ocean with me.";
        String result = transform.apply(null, plainText);
        //assertEquals("RemovePunc Long bigdash", -1, result.indexOf("—"));
        assertEquals("RemovePunc Long comma", -1, result.indexOf(","));
        assertEquals("RemovePunc Long comma", -1, result.indexOf("-"));
        assertEquals("RemovePunc Long dot", -1, result.indexOf("."));
        assertEquals("RemovePunc Long SC", -1, result.indexOf(";"));
        assertEquals("RemovePunc Long QM", -1, result.indexOf("?"));
        assertEquals("RemovePunc Long a", 1, result.indexOf("a"));
        assertEquals("RemovePunc Long S", 16, result.indexOf("S"));
    }

    @Test
    public void testRemoveBadInput() {
        String result = transform.apply(null, "");
        assertEquals("Remove Punctuation empty", "", result);
        result = transform.apply(null, null);
        assertNull("Remove Punctuation null", result);
    }

}