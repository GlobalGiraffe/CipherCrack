package mnh.game.ciphercrack.staticanalysis;

import java.util.List;
import java.util.Map;

public interface AnalysisProvider {

    // allow fragments in the tabs to get the analysis we've done
    int getCountAlphabetic();
    int getCountNonPadding();
    Map<Character, Integer> getFreqAll();
    Map<Character, Integer> getFreqNonPadding();
    Map<Character, Integer> getFreqAlphaAsIs();
    Map<Character, Integer> getFreqAlphaUpper();
    List<FrequencyEntry> getTrigramFrequency();
    List<FrequencyEntry> getBigramFrequency();
    List<FrequencyEntry> getAlignedBigramFrequency();
    List<FrequencyEntry> getLetterFrequency();
    double getIOC();
    double[] getIOCCycles();
    boolean isAllNumeric();
}
