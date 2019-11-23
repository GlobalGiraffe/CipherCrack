package mnh.game.ciphercrack;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.staticanalysis.AnalysisProvider;
import mnh.game.ciphercrack.staticanalysis.AnalysisTabAdapter;
import mnh.game.ciphercrack.staticanalysis.BigramFrequencyFragment;
import mnh.game.ciphercrack.staticanalysis.FrequencyFragment;
import mnh.game.ciphercrack.staticanalysis.GeneralAnalysisFragment;
import mnh.game.ciphercrack.staticanalysis.LetterFrequencyFragment;
import mnh.game.ciphercrack.staticanalysis.SuggestCipherFragment;
import mnh.game.ciphercrack.staticanalysis.TrigramFrequencyFragment;
import mnh.game.ciphercrack.util.Settings;
import mnh.game.ciphercrack.staticanalysis.StaticAnalysis;

public class AnalysisActivity extends AppCompatActivity implements AnalysisProvider {

    // calculations we've done once and can be used by fragments
    private int countAlphabetic;
    private int countNonPadding;
    private Map<Character, Integer> freqAllInclPadding;  // all, including space
    private Map<Character, Integer> freqAllNonPadding;   // all, excluding space
    private Map<Character, Integer> freqAlphaAsIs;       // all alpha chars
    private Map<Character, Integer> freqAlphaUpper;
    private double ioc;
    private double[] iocCycles;
    private boolean isAllNumeric;

    // the frequency objects are referred to when re-ordering columns
    private LetterFrequencyFragment letterFrequency;
    private BigramFrequencyFragment bigramFrequency;
    private TrigramFrequencyFragment trigramFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        Toolbar toolbar = findViewById(R.id.static_analysis_toolbar);
        toolbar.setTitle(R.string.analysis);
        setSupportActionBar(toolbar);

        // get what we will need from the parcel sent
        String text = getIntent().getStringExtra("TEXT");
        String alphabet = Settings.instance().getString(this, getString(R.string.pref_alphabet_plain));
        String paddingChars = Settings.instance().getString(this, getString(R.string.pref_padding_chars));
        Language language = Language.instanceOf(Settings.instance().getString(this, getString(R.string.pref_language)));

        ViewPager viewPager = findViewById(R.id.static_analysis_pager);
        TabLayout tabLayout = findViewById(R.id.static_analysis_tab_layout);
        AnalysisTabAdapter adapter = new AnalysisTabAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        SuggestCipherFragment suggestCipher = new SuggestCipherFragment(this, language);
        GeneralAnalysisFragment generalAnalysis = new GeneralAnalysisFragment(this, text, alphabet);
        letterFrequency = new LetterFrequencyFragment(text);
        bigramFrequency = new BigramFrequencyFragment(text);
        trigramFrequency = new TrigramFrequencyFragment(text);
        adapter.addFragment(suggestCipher, getString(R.string.suggest));
        adapter.addFragment(generalAnalysis, getString(R.string.general));
        adapter.addFragment(letterFrequency, getString(R.string.letters));
        adapter.addFragment(bigramFrequency, getString(R.string.bigrams));
        adapter.addFragment(trigramFrequency, getString(R.string.trigrams));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1); // general analysis
        tabLayout.setupWithViewPager(viewPager);

        // gather initial analysis
        isAllNumeric = StaticAnalysis.isAllNumeric(text);
        countAlphabetic = StaticAnalysis.countAlphabetic(text, alphabet);
        countNonPadding = StaticAnalysis.countNonPadding(text, paddingChars);
        freqAllInclPadding = StaticAnalysis.collectFrequency(text, true, false, alphabet, "");
        freqAllNonPadding = StaticAnalysis.collectFrequency(text, true, false, alphabet, paddingChars);
        freqAlphaAsIs = StaticAnalysis.collectFrequency(text, false, false, alphabet, paddingChars);
        freqAlphaUpper = StaticAnalysis.collectFrequency(text, false, true, alphabet, paddingChars);
        ioc = StaticAnalysis.calculateIOC(freqAlphaUpper, countAlphabetic, alphabet);

        iocCycles = StaticAnalysis.getCyclicIOC(text, this, alphabet, paddingChars);
    }

    // allow fragments in the tabs to get the analysis we've done
    @Override
    public int getCountAlphabetic() { return countAlphabetic; }
    @Override
    public int getCountNonPadding() { return countNonPadding; }
    @Override
    public Map<Character, Integer> getFreqAll() { return freqAllInclPadding; }
    @Override
    public Map<Character, Integer> getFreqNonPadding() { return freqAllNonPadding; }
    @Override
    public Map<Character, Integer> getFreqAlphaAsIs() { return freqAlphaAsIs; }
    @Override
    public Map<Character, Integer> getFreqAlphaUpper() { return freqAlphaUpper; }
    @Override
    public double getIOC() { return ioc; }
    @Override
    public double[] getIOCCycles() { return iocCycles; }
    @Override
    public boolean isAllNumeric() { return isAllNumeric; }

    /**
     * User clicks letter column heading: sort the frequency columns in this view
     * @param clickedView the view (column heading) that was clicked
     */
    public void sortLetterByColumn(View clickedView) {
        FrequencyFragment.ColumnOrder oldOrder = letterFrequency.getColumnOrder();
        switch (clickedView.getId()) {
            case R.id.freq_letter_count:
                letterFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.COUNT_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.COUNT_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.COUNT_HIGH_TO_LOW);
                break;
            case R.id.freq_letter_gram:
                letterFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.GRAM_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.GRAM_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.GRAM_HIGH_TO_LOW);
                break;
            case R.id.freq_letter_normal:
                letterFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.NORMAL_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.NORMAL_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.NORMAL_HIGH_TO_LOW);
                break;
            case R.id.freq_letter_percent:
                letterFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.PERCENT_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.PERCENT_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.PERCENT_HIGH_TO_LOW);
                break;
        }
    }

    /**
     * User clicks bigram column heading: sort the frequency columns in this view
     * @param clickedView the view (column heading) that was clicked
     */
    public void sortBigramByColumn(View clickedView) {
        FrequencyFragment.ColumnOrder oldOrder = bigramFrequency.getColumnOrder();
        switch (clickedView.getId()) {
            case R.id.freq_bigram_count:
                bigramFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.COUNT_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.COUNT_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.COUNT_HIGH_TO_LOW);
                break;
            case R.id.freq_bigram_gram:
                bigramFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.GRAM_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.GRAM_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.GRAM_HIGH_TO_LOW);
                break;
            case R.id.freq_bigram_normal:
                bigramFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.NORMAL_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.NORMAL_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.NORMAL_HIGH_TO_LOW);
                break;
            case R.id.freq_bigram_percent:
                bigramFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.PERCENT_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.PERCENT_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.PERCENT_HIGH_TO_LOW);
                break;
        }
    }

    /**
     * User clicks trigram column heading: sort the frequency columns in this view
     * @param clickedView the view (column heading) that was clicked
     */
    public void sortTrigramByColumn(View clickedView) {
        FrequencyFragment.ColumnOrder oldOrder = trigramFrequency.getColumnOrder();
        switch (clickedView.getId()) {
            case R.id.freq_trigram_count:
                trigramFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.COUNT_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.COUNT_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.COUNT_HIGH_TO_LOW);
                break;
            case R.id.freq_trigram_gram:
                trigramFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.GRAM_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.GRAM_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.GRAM_HIGH_TO_LOW);
                break;
            case R.id.freq_trigram_normal:
                trigramFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.NORMAL_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.NORMAL_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.NORMAL_HIGH_TO_LOW);
                break;
            case R.id.freq_trigram_percent:
                trigramFrequency.populateGramFrequency(oldOrder == FrequencyFragment.ColumnOrder.PERCENT_HIGH_TO_LOW
                        ? FrequencyFragment.ColumnOrder.PERCENT_LOW_TO_HIGH : FrequencyFragment.ColumnOrder.PERCENT_HIGH_TO_LOW);
                break;
        }
    }
}