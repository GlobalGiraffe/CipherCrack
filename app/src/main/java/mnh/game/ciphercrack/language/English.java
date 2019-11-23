package mnh.game.ciphercrack.language;

import java.util.HashMap;
import java.util.Map;

public class English extends Language {

    // frequency of individual letters
    private static final Map<String, Float> freqLetters = new HashMap<>(26);
    static {
        freqLetters.put("A", 8.167f);
        freqLetters.put("B", 1.492f);
        freqLetters.put("C", 2.782f);
        freqLetters.put("D", 4.253f);
        freqLetters.put("E", 12.702f);
        freqLetters.put("F", 2.228f);
        freqLetters.put("G", 2.015f);
        freqLetters.put("H", 6.094f);
        freqLetters.put("I", 6.966f);
        freqLetters.put("J", 0.153f);
        freqLetters.put("K", 0.772f);
        freqLetters.put("L", 4.025f);
        freqLetters.put("M", 2.406f);
        freqLetters.put("N", 6.749f);
        freqLetters.put("O", 7.507f);
        freqLetters.put("P", 1.929f);
        freqLetters.put("Q", 0.095f);
        freqLetters.put("R", 5.987f);
        freqLetters.put("S", 6.327f);
        freqLetters.put("T", 9.056f);
        freqLetters.put("U", 2.758f);
        freqLetters.put("V", 0.978f);
        freqLetters.put("W", 2.360f);
        freqLetters.put("X", 0.150f);
        freqLetters.put("Y", 1.974f);
        freqLetters.put("Z", 0.074f);
    }

    // top 10 frequency of pairs of letters
    // http://practicalcryptography.com/cryptanalysis/letter-frequencies-various-languages/english-letter-frequencies/
    private static final Map<String, Float> freqBigrams20 = new HashMap<>(20);
    static {
        freqBigrams20.put("TH", 2.71f);
        freqBigrams20.put("HE", 2.33f);
        freqBigrams20.put("IN", 2.03f);
        freqBigrams20.put("ER", 1.78f);
        freqBigrams20.put("AN", 1.61f);
        freqBigrams20.put("RE", 1.41f);
        freqBigrams20.put("ES", 1.32f);
        freqBigrams20.put("ON", 1.32f);
        freqBigrams20.put("ST", 1.25f);
        freqBigrams20.put("NT", 1.17f);
        freqBigrams20.put("EN", 1.13f);
        freqBigrams20.put("AT", 1.12f);
        freqBigrams20.put("ED", 1.08f);
        freqBigrams20.put("ND", 1.07f);
        freqBigrams20.put("TO", 1.07f);
        freqBigrams20.put("OR", 1.06f);
        freqBigrams20.put("EA", 1.00f);
        freqBigrams20.put("TI", 0.99f);
        freqBigrams20.put("AR", 0.98f);
        freqBigrams20.put("TE", 0.98f);
    }

    // top 10 frequency of trios of letters
    // http://practicalcryptography.com/cryptanalysis/letter-frequencies-various-languages/english-letter-frequencies/
    private static final Map<String, Float> freqTrigrams20 = new HashMap<>(20);
    static {
        freqTrigrams20.put("THE", 1.81f);
        freqTrigrams20.put("AND", 0.73f);
        freqTrigrams20.put("ING", 0.72f);
        freqTrigrams20.put("ENT", 0.42f);
        freqTrigrams20.put("ION", 0.42f);
        freqTrigrams20.put("HER", 0.36f);
        freqTrigrams20.put("FOR", 0.34f);
        freqTrigrams20.put("THA", 0.33f);
        freqTrigrams20.put("NTH", 0.33f);
        freqTrigrams20.put("INT", 0.32f);

        freqTrigrams20.put("ERE", 0.31f);
        freqTrigrams20.put("TIO", 0.31f);
        freqTrigrams20.put("TER", 0.30f);
        freqTrigrams20.put("EST", 0.28f);
        freqTrigrams20.put("ERS", 0.28f);
        freqTrigrams20.put("ATI", 0.26f);
        freqTrigrams20.put("HATs", 0.26f);
        freqTrigrams20.put("ATE", 0.25f);
        freqTrigrams20.put("ALL", 0.25f);
        freqTrigrams20.put("ETH", 0.24f);
    }

    public English() { super("English"); }

    @Override
    public double getExpectedIOC() { return 0.066895; } // https://elec5616.com/static/lectures/2016/03_ciphers.pdf

    @Override
    public String getDictionaryResourceName() { return "dictionary_english"; }

    @Override
    public Map<String, Float> getLetterFrequencies() {
        return freqLetters;
    }

    @Override
    public Map<String, Float> getBigramFrequencies() {
        return freqBigrams20;
    }

    @Override
    public Map<String, Float> getTrigramFrequencies() {
        return freqTrigrams20;
    }
}
