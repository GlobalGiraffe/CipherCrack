package mnh.game.ciphercrack.language;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class German extends Language {

    private static final String GERMAN_ALPHABET = "AÄBCDEFGHIJKLMNOÖPQRSßTUÜVWXYZ";

    // frequency of individual letters
    // http://practicalcryptography.com/cryptanalysis/letter-frequencies-various-languages/german-letter-frequencies/
    private static final Map<String, Float> freqLetters = Stream.of(new Object[][] {
            { "A", 6.34f },
            { "B", 2.21f },
            { "C", 2.71f },
            { "D", 4.92f },
            { "E",15.99f },
            { "F", 1.80f },
            { "G", 3.02f },
            { "H", 4.11f },
            { "I", 7.60f },
            { "J", 0.27f },
            { "K", 1.50f },
            { "L", 3.72f },
            { "M", 2.75f },
            { "N", 9.59f },
            { "O", 2.75f },
            { "P", 1.06f },
            { "Q", 0.04f },
            { "R", 7.71f },
            { "S", 6.41f },
            { "T", 6.43f },
            { "U", 3.76f },
            { "V", 0.94f },
            { "W", 1.40f },
            { "X", 0.07f },
            { "Y", 0.13f },
            { "Z", 1.22f },
            { "Ä", 0.54f },
            { "Ö", 0.24f },
            { "Ü", 0.63f },
            { "ß", 0.15f }
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Float) data[1]));

    // top 10 frequency of pairs of letters
    // http://practicalcryptography.com/cryptanalysis/letter-frequencies-various-languages/german-letter-frequencies/
    private static final Map<String, Float> freqBigrams20 = Stream.of(new Object[][]{
            {"ER", 3.90f},
            {"EN", 3.61f},
            {"CH", 2.36f},
            {"DE", 2.31f},
            {"EI", 1.98f},
            {"TE", 1.98f},
            {"IN", 1.71f},
            {"ND", 1.68f},
            {"IE", 1.48f},
            {"GE", 1.45f},
            {"ST", 1.21f},
            {"NE", 1.19f},
            {"BE", 1.17f},
            {"ES", 1.17f},
            {"UN", 1.13f},
            {"RE", 1.11f},
            {"AN", 1.07f},
            {"HE", 0.89f},
            {"AU", 0.89f},
            {"NG", 0.87f}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (Float)data[1]));

    // top 10 frequency of trios of letters
    // http://practicalcryptography.com/cryptanalysis/letter-frequencies-various-languages/german-letter-frequencies/
    private static final Map<String, Float> freqTrigrams20 = Stream.of(new Object[][]{
            {"DER", 1.04f},
            {"EIN", 0.83f},
            {"SCH", 0.76f},
            {"ICH", 0.75f},
            {"NDE", 0.72f},
            {"DIE", 0.62f},
            {"CHE", 0.58f},
            {"DEN", 0.56f},
            {"TEN", 0.51f},
            {"UND", 0.48f},

            {"INE", 0.48f},
            {"TER", 0.44f},
            {"GEN", 0.44f},
            {"END", 0.44f},
            {"ERS", 0.42f},
            {"STE", 0.42f},
            {"CHT", 0.41f},
            {"UNG", 0.39f},
            {"DAS", 0.38f},
            {"ERE", 0.38f}
    }).collect(Collectors.toMap(data -> (String)data[0], data -> (Float)data[1]));

    public German() { super("German"); }

    @Override
        public String getAlphabet() { return GERMAN_ALPHABET; } // 30 letters

    @Override
    public double getExpectedIOC() { return 0.076667; } // https://elec5616.com/static/lectures/2016/03_ciphers.pdf

    @Override
    public String getDictionaryResourceId() { return Dictionary.NONE; }

    @Override
    public Map<String, Float> getLetterFrequencies() { return freqLetters; }

    @Override
    public Map<String, Float> getBigramFrequencies() { return freqBigrams20; }

    @Override
    public Map<String, Float> getTrigramFrequencies() { return freqTrigrams20; }
}
