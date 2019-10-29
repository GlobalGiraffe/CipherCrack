package mnh.game.ciphercrack.util;

import mnh.game.ciphercrack.language.Language;

public class Directives {

    // used across many ciphers
    private Language language;
    private String alphabet;
    private String cribs;

    private int shift;                  // used for Caesar, ROT13
    private int valueA;                 // used for Affine
    private int valueB;
    private int rails;                  // used for Railfence
    private int keywordLength;          // used for vigenere
    private String keyword;             // used for vigenere and keyword substitution
    private int[] permutation;          // use for permutation cipher
    private boolean readAcross;         // read final answer across columns or down (permutation)

    private CrackMethod crackMethod;    // used only when cracking any cipher

    public Directives() { }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    public void setCribs(String cribs) {
        this.cribs = cribs;
    }

    public void setValueA(int valueA) {
        this.valueA = valueA;
    }

    public void setValueB(int valueB) {
        this.valueB = valueB;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }

    public void setRails(int rails) { this.rails = rails; }

    public void setPermutation(int[] permutation) { this. permutation = permutation; }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setKeywordLength(int keywordLength) {
        this.keywordLength = keywordLength;
    }

    public void setCrackMethod(CrackMethod crackMethod) {
        this.crackMethod = crackMethod;
    }

    public void setReadAcross(boolean readAcross) { this.readAcross = readAcross; }

    public Language getLanguage() {
        return language;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public String getCribs() {
        return cribs;
    }

    public CrackMethod getCrackMethod() {
        return crackMethod;
    }

    public int getValueA() {
        return valueA;
    }

    public int getValueB() {
        return valueB;
    }

    public int getShift() {
        return shift;
    }

    public int getRails() { return rails; }

    public int[] getPermutation() { return permutation; }

    public String getKeyword() {
        return keyword;
    }

    public int getKeywordLength() {
        return keywordLength;
    }

    public boolean isReadAcross() {
        return readAcross;
    }
}
