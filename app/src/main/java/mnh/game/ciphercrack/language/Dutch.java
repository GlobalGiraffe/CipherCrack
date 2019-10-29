package mnh.game.ciphercrack.language;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Dutch extends Language {

    // frequency of individual letters
    // https://www.sttmedia.com/characterfrequency-dutch
    private static final Map<Character, Float> freqLetters = Stream.of(new Object[][] {
            { 'A', 7.79f },
            { 'B', 1.38f },
            { 'C', 1.31f },
            { 'D', 5.48f },
            { 'E',19.34f },
            { 'F', 0.74f },
            { 'G', 3.17f },
            { 'H', 3.16f },
            { 'I', 5.04f },
            { 'J', 1.34f },
            { 'K', 2.83f },
            { 'L', 3.85f },
            { 'M', 2.60f },
            { 'N',10.04f },
            { 'O', 5.89f },
            { 'P', 1.51f },
            { 'Q', 0.01f },
            { 'R', 5.70f },
            { 'S', 3.91f },
            { 'T', 6.50f },
            { 'U', 2.15f },
            { 'V', 2.27f },
            { 'W', 1.74f },
            { 'X', 0.05f },
            { 'Y', 0.06f },
            { 'Z', 1.62f }
    }).collect(Collectors.toMap(data -> (Character) data[0], data -> (Float) data[1]));

    // top 10 frequency of pairs of letters
    // https://www.sttmedia.com/syllablefrequency-dutch
    private static final Map<String, Float> freqDigrams10 = Stream.of(new Object[][]{
            {"EN", 6.08f},
            {"DE", 3.28f},
            {"ER", 2.97f},
            {"EE", 2.09f},
            {"AN", 2.05f},
            {"ET", 2.03f},
            {"GE", 1.96f},
            {"TE", 1.93f},
            {"IJ", 1.69f},
            {"AA", 1.66f}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (Float)data[1]));

    // top 10 frequency of trios of letters
    // https://www.sttmedia.com/syllablefrequency-dutch
    private static final Map<String, Float> freqTrigrams10 = Stream.of(new Object[][]{
            {"EEN", 1.40f},
            {"AAR", 1.14f},
            {"HET", 1.08f},
            {"VER", 0.96f},
            {"VAN", 0.92f},
            {"GEN", 0.77f},
            {"OOR", 0.76f},
            {"NDE", 0.71f},
            {"DEN", 0.61f},
            {"CHT", 0.69f}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (Float)data[1]));

    public Dutch() { super("Dutch"); }

    @Override
    public double getExpectedIOC() { return 0.0798; } // https://elec5616.com/static/lectures/2016/03_ciphers.pdf

    @Override
    public String getDictionaryResourceId() { return Dictionary.NONE; }

    @Override
    public Map<Character, Float> getLetterFrequencies() {
        return freqLetters;
    }

    @Override
    public Map<String, Float> getDigramFrequencies() {
        return freqDigrams10;
    }

    @Override
    public Map<String, Float> getTrigramFrequencies() {
        return freqTrigrams10;
    }
}
