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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.fragment.app.Fragment;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.Settings;

public class FrequencyFragment extends Fragment {

    private final String text;
    private final int gramSize;         // 1 for letter, 2 for bigram, 3 for trigram
    private final boolean aligned;      // whether to do only aligned grams, i.e. not overlapping
    private final int overallLayoutId;  // xml file containing the main view
    private final int tableLayoutId;    // layout withing the view containing the table

    private TableLayout tableLayout;
    // which column/direction are we ordering by
    private ColumnOrder ordering;
    private List<FrequencyEntry> frequenciesOfThisGram;

    FrequencyFragment(Context context, String text, int gramSize, boolean aligned, int overallLayoutId, int tableLayoutId) {
        super();
        this.text = text;
        this.gramSize = gramSize;
        this.aligned = aligned;
        this.overallLayoutId = overallLayoutId;
        this.tableLayoutId = tableLayoutId;
        this.ordering = ColumnOrder.COUNT_HIGH_TO_LOW;

        // gather frequencies for the bi-grams
        frequenciesOfThisGram = gatherGramFrequency(context);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(overallLayoutId, container, false);
        tableLayout = view.findViewById(tableLayoutId);

        // place them on the screen, default ordering
        populateGramFrequency(ordering);
        return view;
    }

    /**
     * In the text provided, count the number of instances of each sequence of chars of length gramSize
     * @param context current context, used to remove spaces and punctuation
     * @return a list of pairs of grams and their frequencies
     */
    private List<FrequencyEntry> gatherGramFrequency(Context context) {

        Language language = Language.instanceOf(Settings.instance().getString(context, R.string.pref_language));

        // find the bigrams in the condensed upper-case text, with count for each
        Map<String, Integer> freqUpper = StaticAnalysis.collectGramFrequency(text, gramSize, aligned, context);

        // work out how many grams in total there are
        int countGrams = 0;
        for(Map.Entry<String, Integer> entry : freqUpper.entrySet()) {
            countGrams += entry.getValue();
        }

        // now create the list of grams with associated stats (count, %, normal %)
        List<FrequencyEntry> grams = new ArrayList<>(freqUpper.size());
        for (Map.Entry<String, Integer> entry : freqUpper.entrySet()) {
            grams.add(new FrequencyEntry(entry.getKey(),
                                        entry.getValue(),
                                        (100.0f * entry.getValue())/countGrams,
                                        language.frequencyOf(entry.getKey())));
        }
        return grams;
    }

    /**
     * Put the pre-gathered frequencies in the table with the specified ordering
     * @param order the ordering needed for this display of the grams
     */
    public void populateGramFrequency(ColumnOrder order) {
        // set the new order that we're using now
        ordering = order;

        // determine if this is high to low or low to high
        boolean highToLow = (order == ColumnOrder.COUNT_HIGH_TO_LOW
                          || order == ColumnOrder.GRAM_HIGH_TO_LOW
                          || order == ColumnOrder.PERCENT_HIGH_TO_LOW
                          || order == ColumnOrder.NORMAL_HIGH_TO_LOW);

        // create the right comparator for the sort we're about to do
        Comparator<FrequencyEntry> comp;
        switch (order) {
            case COUNT_HIGH_TO_LOW:
            case COUNT_LOW_TO_HIGH:
                comp = new Comparator<FrequencyEntry>() {
                    public int compare(FrequencyEntry a, FrequencyEntry b) {
                        int result = Integer.compare(a.getCount(), b.getCount());
                        return highToLow ? -result : result;
                    }
                };
                break;
            case GRAM_HIGH_TO_LOW:
            case GRAM_LOW_TO_HIGH:
                comp = new Comparator<FrequencyEntry>() {
                    public int compare(FrequencyEntry a, FrequencyEntry b) {
                        int result = a.getGram().compareTo(b.getGram());
                        return highToLow ? -result : result;
                    }
                };
                break;
            case PERCENT_HIGH_TO_LOW:
            case PERCENT_LOW_TO_HIGH:
                comp = new Comparator<FrequencyEntry>() {
                    public int compare(FrequencyEntry a, FrequencyEntry b) {
                        int result = Float.compare(a.getPercent(), b.getPercent());
                        return highToLow ? -result : result;
                    }
                };
                break;
            default:
                comp = new Comparator<FrequencyEntry>() {
                    public int compare(FrequencyEntry a, FrequencyEntry b) {
                        int result = Float.compare(a.getNormal(), b.getNormal());
                        return highToLow ? -result : result;
                    }
                };
                break;
        }

        // perform the SORT with the comparator we just created
        Collections.sort(frequenciesOfThisGram, comp);

        // remove all old views except the heading and separator (line across screen)
        while (tableLayout.getChildCount() > 2) {
            View childView = tableLayout.getChildAt(2);
            tableLayout.removeView(childView);
        }

        // show the results of the frequency analysis in the TableView
        Context context = tableLayout.getContext();
        int maxGramsInView = Integer.valueOf(Settings.instance().getString(context, R.string.pref_limit_grams));
        for (int i=0; i < Math.min(maxGramsInView, frequenciesOfThisGram.size()); i++) {
            FrequencyEntry entry = frequenciesOfThisGram.get(i);
            String gramText = entry.getGram();
            int gramCount = entry.getCount();
            float gramPercent = entry.getPercent();
            float gramNormal = entry.getNormal();
            TableRow tr = new TableRow(context);
            TextView view = new TextView(context);
            view.setPadding(3,3,3,3);
            view.setText(gramText);
            view.setGravity(Gravity.START);
            tr.addView(view);
            view = new TextView(context);
            view.setPadding(3,3,3,3);
            view.setText(String.valueOf(gramCount));
            view.setGravity(Gravity.START);
            tr.addView(view);
            view = new TextView(context);
            view.setPadding(3,3,3,3);
            view.setText(String.format(Locale.getDefault(),"%4.2f",gramPercent));
            view.setGravity(Gravity.START);
            tr.addView(view);
            view = new TextView(context);
            view.setPadding(3,3,3,3);
            view.setText(String.format(Locale.getDefault(),"%4.2f",gramNormal));
            view.setGravity(Gravity.START);
            tr.addView(view);
            tableLayout.addView(tr);
        }
    }

    public ColumnOrder getColumnOrder() { return ordering; }

    public List<FrequencyEntry> getFrequenciesOfThisGram() { return frequenciesOfThisGram; }
}
