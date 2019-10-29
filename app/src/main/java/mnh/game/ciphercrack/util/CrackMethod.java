package mnh.game.ciphercrack.util;

public enum CrackMethod {
    NONE,           // Used for Encoding / Decoding (not Cracking)
    IOC,            // Vigenere mutates keyword of known length and climbs to highest IOC
    BRUTE_FORCE,    // Affine, Caesar, Atbas, Railfence
    DICTIONARY,     // Keyword Substitution checks all words in the dictionary
    WORD_COUNT      // Keyword Substitution uses Simulated Anealing
                    //   measuring fitness with how many letters match English words in the dictionary
}
