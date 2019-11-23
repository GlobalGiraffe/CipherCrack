package mnh.game.ciphercrack.staticanalysis;

import java.util.Map;

public interface AnalysisProvider {

    // allow fragments in the tabs to get the analysis we've done
    int getCountAlphabetic();
    int getCountNonPadding();
    Map<Character, Integer> getFreqAll();
    Map<Character, Integer> getFreqNonPadding();
    Map<Character, Integer> getFreqAlphaAsIs();
    Map<Character, Integer> getFreqAlphaUpper();
    double getIOC();
    double[] getIOCCycles();
    boolean isAllNumeric();
}
