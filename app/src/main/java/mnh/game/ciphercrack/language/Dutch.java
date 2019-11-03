package mnh.game.ciphercrack.language;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Dutch extends Language {

    // frequency of individual letters
    // TODO - adjust to be like English with static initialisers - to run on old phones
    // https://www.sttmedia.com/characterfrequency-dutch
    private static final Map<String, Float> freqLetters = Stream.of(new Object[][] {
            { "A", 7.79f },
            { "B", 1.38f },
            { "C", 1.31f },
            { "D", 5.48f },
            { "E",19.34f },
            { "F", 0.74f },
            { "G", 3.17f },
            { "H", 3.16f },
            { "I", 5.04f },
            { "J", 1.34f },
            { "K", 2.83f },
            { "L", 3.85f },
            { "M", 2.60f },
            { "N",10.04f },
            { "O", 5.89f },
            { "P", 1.51f },
            { "Q", 0.01f },
            { "R", 5.70f },
            { "S", 3.91f },
            { "T", 6.50f },
            { "U", 2.15f },
            { "V", 2.27f },
            { "W", 1.74f },
            { "X", 0.05f },
            { "Y", 0.06f },
            { "Z", 1.62f }
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Float) data[1]));

    // top 10 frequency of pairs of letters
    // https://www.sttmedia.com/syllablefrequency-dutch
    private static final Map<String, Float> freqBigrams20 = Stream.of(new Object[][]{
            {"EN", 6.08f},
            {"DE", 3.28f},
            {"ER", 2.97f},
            {"EE", 2.09f},
            {"AN", 2.05f},
            {"ET", 2.03f},
            {"GE", 1.96f},
            {"TE", 1.93f},
            {"IJ", 1.69f},
            {"AA", 1.66f},
            {"EL", 1.47f},
            {"IN", 1.46f},
            {"HE", 1.44f},
            {"IE", 1.39f},
            {"CH", 1.18f},
            {"AR", 1.17f},
            {"OO", 1.06f},
            {"ST", 1.05f},
            {"LE", 1.04f},
            {"ND", 1.03f}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (Float)data[1]));

    // top 10 frequency of trios of letters
    // https://www.sttmedia.com/syllablefrequency-dutch
    private static final Map<String, Float> freqTrigrams20 = Stream.of(new Object[][]{
            {"EEN", 1.40f},
            {"AAR", 1.14f},
            {"HET", 1.08f},
            {"VER", 0.96f},
            {"VAN", 0.92f},
            {"GEN", 0.77f},
            {"OOR", 0.76f},
            {"NDE", 0.71f},
            {"DEN", 0.61f},
            {"CHT", 0.69f},

            {"NIE", 0.67f},
            {"ING", 0.67f},
            {"TEN", 0.63f},
            {"SCH", 0.61f},
            {"EER", 0.61f},
            {"DER", 0.59f},
            {"DAT", 0.59f},
            {"IET", 0.58f},
            {"STE", 0.53f},
            {"AND", 0.50f}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (Float)data[1]));

    public Dutch() { super("Dutch"); }

    @Override
    public double getExpectedIOC() { return 0.0798; } // https://elec5616.com/static/lectures/2016/03_ciphers.pdf

    @Override
    public String getDictionaryResourceId() { return Dictionary.NONE; }

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
