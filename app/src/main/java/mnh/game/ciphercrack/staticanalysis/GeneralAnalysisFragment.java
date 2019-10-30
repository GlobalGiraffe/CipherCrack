package mnh.game.ciphercrack.staticanalysis;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

import androidx.fragment.app.Fragment;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.transform.RemovePadding;
import mnh.game.ciphercrack.transform.RemovePunctuation;
import mnh.game.ciphercrack.util.Settings;
import mnh.game.ciphercrack.util.StaticAnalysis;

public class GeneralAnalysisFragment extends Fragment {

    private final String text;
    private final String alphabet;
    private final RemovePadding removePadding = new RemovePadding();
    private final RemovePunctuation removePunctuation = new RemovePunctuation();

    public GeneralAnalysisFragment(String text, String alphabet) {
        this.text = text;
        this.alphabet = alphabet;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis_general, container, false);
        // gather stats and place on the screen
        String stats = gatherGeneralStats(text, alphabet);
        TextView textView = view.findViewById(R.id.stats_general_text);
        textView.setText(stats);
        return view;
    }

    private String gatherGeneralStats(String text, String alphabet) {
        // work out which language we should use
        String languageName = Settings.instance().getString(this.getContext(), getString(R.string.pref_language));
        Language language = Language.instanceOf(languageName);
        String paddingChars = Settings.instance().getString(this.getContext(), getString(R.string.pref_padding_chars));

        // we keep track of counts of letters converted to upper case
        //  and also counts of letters in original form - less useful statistically
        int countAlphabetic = StaticAnalysis.countAlphabetic(text, alphabet);
        int countNonPadding = StaticAnalysis.countNonPadding(text, paddingChars);
        HashMap<Character, Integer> freqAsIs = StaticAnalysis.collectFrequency(text, false, alphabet);
        HashMap<Character, Integer> freqUpper = StaticAnalysis.collectFrequency(text, true, alphabet);
        int countUniqueAsIsAlphabetic = freqAsIs.keySet().size();
        int countUniqueUpperAlphabetic = freqUpper.keySet().size();

        // calculate how likely it is to see same letter twice - helps decide cipher scheme
        double ioc = StaticAnalysis.getIOC(freqUpper, countAlphabetic, alphabet);

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
        String textToUse = removePadding.apply(getContext(),removePunctuation.apply(getContext(),text.toUpperCase()));
        int maxCycles = Math.min(textToUse.length(), 30);

        // these will hold the cyclic strings
        StringBuilder[] cycleStrings = new StringBuilder[maxCycles];
        for (int pos=0; pos < maxCycles; pos++) {
            cycleStrings[pos] = new StringBuilder();
        }

        for(int cycleSize=2; cycleSize < maxCycles; cycleSize++) {
            // clear the cyclic strings
            for (int pos=0; pos < cycleSize; pos++) {
                cycleStrings[pos].setLength(0);
            }
            // form the string with this cycle size by scanning whole input text
            for (int pos=0; pos < textToUse.length(); pos++) {
                cycleStrings[pos%cycleSize].append(textToUse.charAt(pos));
            }
            double overallIOC = 0.0;
            for (int pos=0; pos < cycleSize; pos++) {
                overallIOC += StaticAnalysis.getIOC(cycleStrings[pos].toString(), cycleStrings[pos].length(), alphabet);
            }
            ioc = overallIOC / cycleSize;
            s.append("Cycle: ").append(cycleSize).append(", IOC: ")
                    .append(String.format(Locale.getDefault(),"%7.6f",ioc));
            if (ioc > language.getExpectedIOC()*.95f) {
                s.append(" <= poss key length");
            }
            s.append("\n");
        }
        return s.toString();
    }


}
