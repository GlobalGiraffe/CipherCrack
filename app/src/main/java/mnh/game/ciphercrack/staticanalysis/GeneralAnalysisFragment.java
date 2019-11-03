package mnh.game.ciphercrack.staticanalysis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    public GeneralAnalysisFragment(AnalysisActivity analysis, String text, String alphabet) {
        this.analysis = analysis;
        this.text = text;
        this.alphabet = alphabet;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis_general, container, false);
        // gather stats and place on the screen
        String stats = gatherGeneralStats(text);
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
        Map<Character, Integer> freqAsIs = analysis.getFreqAsIs();
        Map<Character, Integer> freqUpper = analysis.getFreqUpper();
        int countUniqueAsIsAlphabetic = freqAsIs.keySet().size();
        int countUniqueUpperAlphabetic = freqUpper.keySet().size();

        // calculate how likely it is to see same letter twice - helps decide cipher scheme
        double ioc = analysis.getIOC();

        // now gather our findings into some text
        StringBuilder s = new StringBuilder(1000);
        s.append("Count all chars: ").append(text.length()).append("\n");
        s.append("Count non-padding chars: ").append(countNonPadding).append("\n");
        s.append("Count alphabetic letters: ").append(countAlphabetic).append("\n");
        s.append("Count unique alphabetic: ").append(countUniqueAsIsAlphabetic).append("\n");
        s.append("Count unique upper-case alphabetic: ").append(countUniqueUpperAlphabetic).append("\n");
        s.append("Index of Coincidence (IOC): ").append(String.format(Locale.getDefault(),"%7.6f",ioc)).append("\n");
        s.append("Expected IOC for ").append(languageName).append(": ")
                .append(String.format(Locale.getDefault(),"%7.6f",language.getExpectedIOC())).append("\n");

        // now show the cyclic IOC, which may indicate Vigenere keyword length
        double[] iocCycles = analysis.getIOCCycles();
        for(int cycle=1; cycle < iocCycles.length; cycle++) {
            s.append("Cycle: ").append(cycle).append(", IOC: ")
                    .append(String.format(Locale.getDefault(),"%7.6f",iocCycles[cycle]));
            if (ioc > language.getExpectedIOC() * StaticAnalysis.IOC_SIGNIFICANCE_PERCENTAGE) {
                s.append(" <= poss key length");
            }
            s.append("\n");
        }
        return s.toString();
    }


}
