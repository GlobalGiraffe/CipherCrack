package mnh.game.ciphercrack.util;

import org.jetbrains.annotations.NotNull;

public enum CrackMethod {
    NONE,           // Used for Encoding / Decoding (not Cracking)
    IOC,            // Vigenere mutates keyword of known length and climbs to highest IOC
    BRUTE_FORCE,    // Affine, Caesar, Atbash, Railfence
    DICTIONARY,     // Keyword Substitution checks all words in the dictionary
    CRIB_DRAG,      // Slide a crib along the cipher text to determine the key at each point
    WORD_COUNT;     // Keyword Substitution uses Simulated Anealing
                    //   measuring fitness with how many letters match English words in the dictionary


    @Override
    @NotNull
    public String toString() {
        String result = super.toString();
        switch (this) {
            case NONE:          result = "None"; break;
            case IOC:           result = "IOC Climb"; break;
            case BRUTE_FORCE:   result = "Brute Force"; break;
            case DICTIONARY:    result = "Dictionary Scan"; break;
            case CRIB_DRAG:     result = "Crib Drag"; break;
            case WORD_COUNT:    result = "Simulated Anealing Word Count"; break;
        }
        return result;
    }

}
