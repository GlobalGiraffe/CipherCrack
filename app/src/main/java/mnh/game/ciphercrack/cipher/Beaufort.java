package mnh.game.ciphercrack.cipher;

import android.content.Context;

import mnh.game.ciphercrack.util.Directives;

/**
 * Class that contains methods to assist with Beaufort Cipher operations
 * This poly-alphabetic cipher shifts each letter within the text by a
 *   an amount based on a keyword
 * Many parts (screen layout, crack algorithm, etc) are identical to Vigenere,
 * so just extend that one and override where necessary
 * Use this tableau:
 * - use plain letter to find the column
 * - scan down to the key letter
 * - look left to read the enciphered letter from the first column
 *     A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
 *     ---------------------------------------------------
 * A   A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
 * B   B C D E F G H I J K L M N O P Q R S T U V W X Y Z A
 * C   C D E F G H I J K L M N O P Q R S T U V W X Y Z A B
 * D   D E F G H I J K L M N O P Q R S T U V W X Y Z A B C
 * E   E F G H I J K L M N O P Q R S T U V W X Y Z A B C D
 * F   F G H I J K L M N O P Q R S T U V W X Y Z A B C D E
 * G   G H I J K L M N O P Q R S T U V W X Y Z A B C D E F
 * H   H I J K L M N O P Q R S T U V W X Y Z A B C D E F G
 * I   I J K L M N O P Q R S T U V W X Y Z A B C D E F G H
 * J   J K L M N O P Q R S T U V W X Y Z A B C D E F G H I
 * K   K L M N O P Q R S T U V W X Y Z A B C D E F G H I J
 * L   L M N O P Q R S T U V W X Y Z A B C D E F G H I J K
 * M   M N O P Q R S T U V W X Y Z A B C D E F G H I J K L
 * N   N O P Q R S T U V W X Y Z A B C D E F G H I J K L M
 * O   O P Q R S T U V W X Y Z A B C D E F G H I J K L M N
 * P   P Q R S T U V W X Y Z A B C D E F G H I J K L M N O
 * Q   Q R S T U V W X Y Z A B C D E F G H I J K L M N O P
 * R   R S T U V W X Y Z A B C D E F G H I J K L M N O P Q
 * S   S T U V W X Y Z A B C D E F G H I J K L M N O P Q R
 * T   T U V W X Y Z A B C D E F G H I J K L M N O P Q R S
 * U   U V W X Y Z A B C D E F G H I J K L M N O P Q R S T
 * V   V W X Y Z A B C D E F G H I J K L M N O P Q R S T U
 * W   W X Y Z A B C D E F G H I J K L M N O P Q R S T U V
 * X   X Y Z A B C D E F G H I J K L M N O P Q R S T U V W
 * Y   Y Z A B C D E F G H I J K L M N O P Q R S T U V W X
 * Z   Z A B C D E F G H I J K L M N O P Q R S T U V W X Y
 */
public class Beaufort extends Vigenere {

    Beaufort(Context context) { super(context, "Beaufort"); }

    /**
     * Describe what this cipher is and does
     * @return a description of this cipher
     */
    @Override
    public String getCipherDescription() {
        return "The Beaufort cipher is a poly-alphabetic substitution cipher where each letter of the plain text can be mapped to many different letters in the cipher text, depending on the position of the letter in the message. " +
                "A tableau is used where each of 26 columns contain the 26 letters of the alphabet in a different position. As the message is encoded, each column in turn is used to perform a Caesar cipher encoding.\n" +
                "To encrypt, first choose the plaintext character from the top row of the tableau; call this column P. Secondly, travel down column P to the corresponding key letter K. Finally, move directly left from the key letter to the left edge of the tableau, the cipher text encryption of plaintext P with key K will be there.\n"+
                "As Beaufort is a reciprocal cipher and so to decode a message the same steps are used as for encoding.\n"+
                "This cipher can be broken by looking at IOC values for different possible keyword sizes, the one with IOC close to the target language will indicate keyword length. With the help of some cribs, particularly ones longer than the keyword length, the keyword can be slowly discovered.";
    }

    /**
     * Show what this instance is configured to do
     * @return a string showing how this instance of the cipher has been configured
     */
    @Override
    public String getInstanceDescription() {
        return getCipherName()+" cipher ("+(keyword==null?"n/a":keyword)+")";
    }

    /**
     * Encode a text using Beaufort cipher with the given keyword
     * @param plainText the text to be encoded
     * @param dirs a group of directives that define how the cipher will work, especially KEYWORD
     * @return the encoded string
     */
    @Override
    public String encode(String plainText, Directives dirs) {
        String alphabet = dirs.getAlphabet();
        String keyword = dirs.getKeyword();
        int keywordLength = keyword.length();
        String keywordUpper = keyword.toUpperCase();

        StringBuilder result = new StringBuilder(plainText.length());
        for (int i=0, keyPos=0; i < plainText.length(); i++) {
            char plainChar = plainText.charAt(i);
            char plainCharUpper = Character.toUpperCase(plainChar);
            // find the column that has the plain letter at the top
            int plainColumn = alphabet.indexOf(plainCharUpper);
            if (plainColumn < 0) {
                // letter is not in alphabet, just copy to result, key does not advance
                result.append(plainChar);
            } else {
                // work out how far we need to go down this column
                char keyLetter = keywordUpper.charAt(keyPos);
                keyPos = (keyPos+1) % keywordLength;
                int keyRow = alphabet.indexOf(keyLetter);
                int cipherOrd = keyRow - plainColumn;
                if (cipherOrd < 0)
                    cipherOrd += alphabet.length();
                char cipherChar = alphabet.charAt(cipherOrd);
                if (Character.isLowerCase(plainChar))
                    cipherChar = Character.toLowerCase(cipherChar);
                result.append(cipherChar);
            }
        }
        return result.toString();
    }

    /**
     * Decode a text using Beaufort cipher with the given keyword.
     * As Beaufort is a reciprocal cipher we can just reuse the encode method.
     * @param cipherText the text to be decoded
     * @param dirs a group of directives that define how the cipher will work, especially KEYWORD
     * @return the decoded string
     */
    @Override
    public String decode(String cipherText, Directives dirs) {
        return encode(cipherText, dirs);
    }
}
