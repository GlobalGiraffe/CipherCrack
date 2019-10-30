package mnh.game.ciphercrack.language;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class English extends Language {

    // frequency of individual letters
    private static final Map<String, Float> freqLetters = Stream.of(new Object[][] {
            { "A", 8.167f },
            { "B", 1.492f },
            { "C", 2.782f },
            { "D", 4.253f },
            { "E",12.702f },
            { "F", 2.228f },
            { "G", 2.015f },
            { "H", 6.094f },
            { "I", 6.966f },
            { "J", 0.153f },
            { "K", 0.772f },
            { "L", 4.025f },
            { "M", 2.406f },
            { "N", 6.749f },
            { "O", 7.507f },
            { "P", 1.929f },
            { "Q", 0.095f },
            { "R", 5.987f },
            { "S", 6.327f },
            { "T", 9.056f },
            { "U", 2.758f },
            { "V", 0.978f },
            { "W", 2.360f },
            { "X", 0.150f },
            { "Y", 1.974f },
            { "Z", 0.074f }
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Float) data[1]));

    // top 10 frequency of pairs of letters
    // http://practicalcryptography.com/cryptanalysis/letter-frequencies-various-languages/english-letter-frequencies/
    private static final Map<String, Float> freqBigrams20 = Stream.of(new Object[][]{
            {"TH", 2.71f},
            {"HE", 2.33f},
            {"IN", 2.03f},
            {"ER", 1.78f},
            {"AN", 1.61f},
            {"RE", 1.41f},
            {"ES", 1.32f},
            {"ON", 1.32f},
            {"ST", 1.25f},
            {"NT", 1.17f},
            {"EN", 1.13f},
            {"AT", 1.12f},
            {"ED", 1.08f},
            {"ND", 1.07f},
            {"TO", 1.07f},
            {"OR", 1.06f},
            {"EA", 1.00f},
            {"TI", 0.99f},
            {"AR", 0.98f},
            {"TE", 0.98f}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (Float)data[1]));

    // top 10 frequency of trios of letters
    // http://practicalcryptography.com/cryptanalysis/letter-frequencies-various-languages/english-letter-frequencies/
    private static final Map<String, Float> freqTrigrams20 = Stream.of(new Object[][]{
            {"THE", 1.81f},
            {"AND", 0.73f},
            {"ING", 0.72f},
            {"ENT", 0.42f},
            {"ION", 0.42f},
            {"HER", 0.36f},
            {"FOR", 0.34f},
            {"THA", 0.33f},
            {"NTH", 0.33f},
            {"INT", 0.32f},

            {"ERE", 0.31f},
            {"TIO", 0.31f},
            {"TER", 0.30f},
            {"EST", 0.28f},
            {"ERS", 0.28f},
            {"ATI", 0.26f},
            {"HATs", 0.26f},
            {"ATE", 0.25f},
            {"ALL", 0.25f},
            {"ETH", 0.24f}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (Float)data[1]));

    public English() { super("English"); }

    @Override
    public double getExpectedIOC() { return 0.066895; } // https://elec5616.com/static/lectures/2016/03_ciphers.pdf

    @Override
    public String getDictionaryResourceId() { return "dictionary_english"; }

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
