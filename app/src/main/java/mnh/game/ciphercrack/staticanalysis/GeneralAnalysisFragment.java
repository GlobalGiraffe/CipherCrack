package mnh.game.ciphercrack.staticanalysis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.fragment.app.Fragment;
import mnh.game.ciphercrack.AnalysisActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.Settings;

public class GeneralAnalysisFragment extends Fragment {

    private final AnalysisActivity analysis;
    private final String text;
    private final String alphabet;
    private String stats = null;

    public GeneralAnalysisFragment(AnalysisActivity analysis, String text, String alphabet) {
        this.analysis = analysis;
        this.text = text;
        this.alphabet = alphabet;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis_general, container, false);
        if (stats == null)
            stats = gatherGeneralStats(text);
        // gather stats and place on the screen
        TextView textView = view.findViewById(R.id.stats_general_text);
        textView.setText(stats);
        return view;
    }

    private String gatherGeneralStats(String text) {
        // work out which language we should use
        String languageName = Settings.instance().getString(this.getContext(), getString(R.string.pref_language));
        Language language = Language.instanceOf(languageName);

        // we keep track of counts of letters converted to upper case
        //  and also counts of letters in original form - less useful statistically
        int countAlphabetic = analysis.getCountAlphabetic();
        int countNonPadding = analysis.getCountNonPadding();
        Map<Character, Integer> freqAll = analysis.getFreqAll();
        Map<Character, Integer> freqNonPadding = analysis.getFreqNonPadding();
        Map<Character, Integer> freqAlphaAsIs = analysis.getFreqAlphaAsIs();
        Map<Character, Integer> freqAlphaUpper = analysis.getFreqAlphaUpper();
        int countUniqueAsIsAlphabetic = freqAlphaAsIs.keySet().size();
        int countUniqueUpperAlphabetic = freqAlphaUpper.keySet().size();

        // calculate how likely it is to see same letter twice - helps decide cipher scheme
        double ioc = analysis.getIOC();

        // now gather our findings into some text
        StringBuilder s = new StringBuilder(1000);
        s.append("All chars: ").append(text.length()).append("\n");
        s.append("Non-padding chars: ").append(countNonPadding).append("\n");
        s.append("Alphabetic letters: ").append(countAlphabetic).append("\n");
        s.append("Unique symbols incl. padding: ").append(freqAll.size()).append("\n");
        s.append("Unique symbols excl. padding: ").append(freqNonPadding.size()).append("\n");
        s.append("= ");
        List<Character> list = new ArrayList<>(freqNonPadding.size());
        list.addAll(freqNonPadding.keySet());
        Collections.sort(list);
        for(Character c : list) {
            s.append(c);
        }
        s.append("\n");
        s.append("Unique alphabetic: ").append(countUniqueAsIsAlphabetic).append("\n");
        s.append("Unique upper-case alphabetic: ").append(countUniqueUpperAlphabetic).append("\n");
        s.append("= ");
        list.clear();
        list.addAll(freqAlphaUpper.keySet());
        Collections.sort(list);
        for(Character c : list) {
            s.append(c);
        }
        s.append("\n");
        s.append("Missing: ");
        list.clear();
        for(Character c : alphabet.toCharArray()) {
            list.add(c);
        }
        Collections.sort(list);
        for(Character c : list) {
            if (!freqAlphaUpper.keySet().contains(c))
                s.append(c);
        }
        s.append("\n");
        s.append("Index of Coincidence (IOC): ").append(String.format(Locale.getDefault(),"%7.6f",ioc)).append("\n");
        s.append("Expected IOC for ").append(languageName).append(": ")
                .append(String.format(Locale.getDefault(),"%7.6f",language.getExpectedIOC())).append("\n");

        // now show the cyclic IOC, which may indicate Vigenere keyword length
        double[] iocCycles = analysis.getIOCCycles();

        // but we only want to show any 'poss key length' if they're not all > the IOC
        boolean anyLessThanSignificantIOC = false;
        for(int cycle=1; cycle < iocCycles.length; cycle++) {
            if (iocCycles[cycle] <= language.getExpectedIOC() * StaticAnalysis.IOC_SIGNIFICANCE_PERCENTAGE) {
                anyLessThanSignificantIOC = true;
            }
        }
        // now really show the IOC cycles
        for(int cycle=1; cycle < iocCycles.length; cycle++) {
            s.append("Cycle: ").append(cycle).append(", IOC: ")
                    .append(String.format(Locale.getDefault(),"%7.6f",iocCycles[cycle]));
            if (anyLessThanSignificantIOC && iocCycles[cycle] > language.getExpectedIOC() * StaticAnalysis.IOC_SIGNIFICANCE_PERCENTAGE) {
                s.append(" <= poss key length");
            }
            s.append("\n");
        }
        return s.toString();
    }
}
