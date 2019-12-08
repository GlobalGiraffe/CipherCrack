package mnh.game.ciphercrack.util;

public enum KeywordExtend {
    EXTEND_FIRST,  // extend from the first in the alphabet
    EXTEND_MIN,    // extend from the lowest letter in the key so far
    EXTEND_MAX,    // extend from the highest letter in the key so far
    EXTEND_LAST,   // extend from the final letter in the key so far
    EXTEND_NONE    // use as-is
}
