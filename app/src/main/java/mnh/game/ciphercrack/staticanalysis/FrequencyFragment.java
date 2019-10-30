package mnh.game.ciphercrack.staticanalysis;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.fragment.app.Fragment;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.Settings;
import mnh.game.ciphercrack.util.StaticAnalysis;

public class FrequencyFragment extends Fragment {

    private static final int MAX_GRAMS_IN_VIEW = 40;

    private final String text;
    private final String alphabet;
    private final int gramSize;
    private final int overallLayoutId;
    private final int tableLayoutId;

    FrequencyFragment(String text, String alphabet, int gramSize, int overallLayoutId, int tableLayoutId) {
        this.text = text;
        this.alphabet = alphabet;
        this.gramSize = gramSize;
        this.overallLayoutId = overallLayoutId;
        this.tableLayoutId = tableLayoutId;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(overallLayoutId, container, false);
        Context context = view.getContext();

        // gather frequencies for the bi-grams
        List<Map.Entry<String,Integer>> freq = gatherGramFrequency(context, gramSize, text);

        // place them on the screen
        Language language = Language.instanceOf(Settings.instance().getString(context, R.string.pref_language));
        TableLayout tableLayout = view.findViewById(tableLayoutId);
        populateGramFrequency(tableLayout, freq, language);
        return view;
    }

    /**
     * In the text provided, count the number of instances of each sequence of chars of length gramSize
     * @param context current context, used to remove spaces and punctuation
     * @param gramSize how many letters to include in each gram
     * @param text the text being analysed
     * @return a list of pairs of grams and their frequencies
     */
    private List<Map.Entry<String,Integer>> gatherGramFrequency(Context context, int gramSize, String text) {

        // find the bigrams in the condensed upper-case text, with count for each
        HashMap<String, Integer> freqUpper = StaticAnalysis.collectGramFrequency(text, gramSize, context);

        // sort them by count descending, reverse order
        List<Map.Entry<String, Integer>> sortedGrams = new ArrayList<>(freqUpper.entrySet());
        Collections.sort(sortedGrams,
                (Comparator<Map.Entry<String, Integer>> & Serializable)
                        (c1, c2) -> c2.getValue().compareTo(c1.getValue()));

        return sortedGrams;
    }

    private void populateGramFrequency(TableLayout tableLayout, List<Map.Entry<String, Integer>> grams, Language language) {
        // show the results of the frequency analysis in the TableView
        int countGrams = 0;
        for(Map.Entry<String, Integer> entry : grams) {
            countGrams += entry.getValue();
        }
        Context context = tableLayout.getContext();
        for (int i=0; i < Math.min(MAX_GRAMS_IN_VIEW, grams.size()); i++) {
            String gramText = grams.get(i).getKey();
            Integer freq = grams.get(i).getValue();
            freq = (freq == null) ? 0 : freq ;
            float percent = (100.0f * freq)/countGrams;
            TableRow tr = new TableRow(context);
            TextView view = new TextView(context);
            view.setPadding(3,3,3,3);
            view.setText(gramText);
            view.setGravity(Gravity.START);
            tr.addView(view);
            view = new TextView(context);
            view.setPadding(3,3,3,3);
            view.setText(String.valueOf(freq));
            view.setGravity(Gravity.START);
            tr.addView(view);
            view = new TextView(context);
            view.setPadding(3,3,3,3);
            view.setText(String.format(Locale.getDefault(),"%4.2f",percent));
            view.setGravity(Gravity.START);
            tr.addView(view);
            view = new TextView(context);
            view.setPadding(3,3,3,3);
            view.setText(String.format(Locale.getDefault(),"%4.2f",language.frequencyOf(gramText)));
            view.setGravity(Gravity.START);
            tr.addView(view);
            tableLayout.addView(tr);
        }
    }
}
