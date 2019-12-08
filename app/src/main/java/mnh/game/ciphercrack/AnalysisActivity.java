package mnh.game.ciphercrack;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.staticanalysis.AnalysisProvider;
import mnh.game.ciphercrack.staticanalysis.AnalysisTabAdapter;
import mnh.game.ciphercrack.staticanalysis.BigramFrequencyFragment;
import mnh.game.ciphercrack.staticanalysis.ColumnOrder;
import mnh.game.ciphercrack.staticanalysis.FrequencyEntry;
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
    private BigramFrequencyFragment alignedBigramFrequency; // used for Polybius-square ciphers
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

        // set up the TAB view
        ViewPager viewPager = findViewById(R.id.static_analysis_pager);
        TabLayout tabLayout = findViewById(R.id.static_analysis_tab_layout);
        AnalysisTabAdapter adapter = new AnalysisTabAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        // now create the tab fragments one by one
        // order is important, do the "suggest" one last as it relies on the others
        // do this last as it relies on data from the others
        SuggestCipherFragment suggestCipher = new SuggestCipherFragment(this, language);
        adapter.addFragment(suggestCipher, getString(R.string.suggest));
        GeneralAnalysisFragment generalAnalysis = new GeneralAnalysisFragment(this, text, alphabet);
        adapter.addFragment(generalAnalysis, getString(R.string.general));
        letterFrequency = new LetterFrequencyFragment(this, text);
        adapter.addFragment(letterFrequency, getString(R.string.letters));
        bigramFrequency = new BigramFrequencyFragment(this, text, false, R.layout.fragment_bigram_frequency, R.id.freq_bigram_layout);
        adapter.addFragment(bigramFrequency, getString(R.string.bigrams));
        // if text length is multiple of 2, also gather aligned bigrams
        if (countAlphabetic % 2 == 0) {
            alignedBigramFrequency = new BigramFrequencyFragment(this, text, true, R.layout.fragment_aligned_bigram_frequency, R.id.freq_aligned_bigram_layout);
            adapter.addFragment(alignedBigramFrequency, getString(R.string.aligned_bigrams));
        }
        trigramFrequency = new TrigramFrequencyFragment(this, text);
        adapter.addFragment(trigramFrequency, getString(R.string.trigrams));

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1); // general analysis
        tabLayout.setupWithViewPager(viewPager);
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
    public List<FrequencyEntry> getBigramFrequency() { return bigramFrequency.getFrequenciesOfThisGram(); }
    @Override
    public List<FrequencyEntry> getAlignedBigramFrequency() { return alignedBigramFrequency == null ? null : alignedBigramFrequency.getFrequenciesOfThisGram(); }
    @Override
    public List<FrequencyEntry> getTrigramFrequency() { return trigramFrequency.getFrequenciesOfThisGram(); }
    @Override
    public List<FrequencyEntry> getLetterFrequency() { return letterFrequency.getFrequenciesOfThisGram(); }
    @Override
    public double getIOC() { return ioc; }
    @Override
    public double[] getIOCCycles() { return iocCycles; }
    @Override
    public boolean isAllNumeric() { return isAllNumeric; }

    /**
     * User clicks bigram column heading: sort the frequency columns in this view
     * @param clickedView the view (column heading) that was clicked
     */
    public void sortByColumn(View clickedView) {
        ColumnOrder oldBigramOrder = bigramFrequency.getColumnOrder();
        ColumnOrder oldAlignedBigramOrder = alignedBigramFrequency != null ? alignedBigramFrequency.getColumnOrder() : ColumnOrder.COUNT_HIGH_TO_LOW;
        ColumnOrder oldLetterOrder = letterFrequency.getColumnOrder();
        ColumnOrder oldTrigramOrder = trigramFrequency.getColumnOrder();
        switch (clickedView.getId()) {
            case R.id.freq_letter_count:
                letterFrequency.populateGramFrequency(oldLetterOrder == ColumnOrder.COUNT_HIGH_TO_LOW
                        ? ColumnOrder.COUNT_LOW_TO_HIGH : ColumnOrder.COUNT_HIGH_TO_LOW);
                break;
            case R.id.freq_letter_gram:
                letterFrequency.populateGramFrequency(oldLetterOrder == ColumnOrder.GRAM_HIGH_TO_LOW
                        ? ColumnOrder.GRAM_LOW_TO_HIGH : ColumnOrder.GRAM_HIGH_TO_LOW);
                break;
            case R.id.freq_letter_normal:
                letterFrequency.populateGramFrequency(oldLetterOrder == ColumnOrder.NORMAL_HIGH_TO_LOW
                        ? ColumnOrder.NORMAL_LOW_TO_HIGH : ColumnOrder.NORMAL_HIGH_TO_LOW);
                break;
            case R.id.freq_letter_percent:
                letterFrequency.populateGramFrequency(oldLetterOrder == ColumnOrder.PERCENT_HIGH_TO_LOW
                        ? ColumnOrder.PERCENT_LOW_TO_HIGH : ColumnOrder.PERCENT_HIGH_TO_LOW);
                break;

            case R.id.freq_bigram_count:
                bigramFrequency.populateGramFrequency(oldBigramOrder == ColumnOrder.COUNT_HIGH_TO_LOW
                        ? ColumnOrder.COUNT_LOW_TO_HIGH : ColumnOrder.COUNT_HIGH_TO_LOW);
                break;
            case R.id.freq_bigram_gram:
                bigramFrequency.populateGramFrequency(oldBigramOrder == ColumnOrder.GRAM_HIGH_TO_LOW
                        ? ColumnOrder.GRAM_LOW_TO_HIGH : ColumnOrder.GRAM_HIGH_TO_LOW);
                break;
            case R.id.freq_bigram_normal:
                bigramFrequency.populateGramFrequency(oldBigramOrder == ColumnOrder.NORMAL_HIGH_TO_LOW
                        ? ColumnOrder.NORMAL_LOW_TO_HIGH : ColumnOrder.NORMAL_HIGH_TO_LOW);
                break;
            case R.id.freq_bigram_percent:
                bigramFrequency.populateGramFrequency(oldBigramOrder == ColumnOrder.PERCENT_HIGH_TO_LOW
                        ? ColumnOrder.PERCENT_LOW_TO_HIGH : ColumnOrder.PERCENT_HIGH_TO_LOW);
                break;

            case R.id.freq_aligned_bigram_count:
                alignedBigramFrequency.populateGramFrequency(oldAlignedBigramOrder == ColumnOrder.COUNT_HIGH_TO_LOW
                        ? ColumnOrder.COUNT_LOW_TO_HIGH : ColumnOrder.COUNT_HIGH_TO_LOW);
                break;
            case R.id.freq_aligned_bigram_gram:
                alignedBigramFrequency.populateGramFrequency(oldAlignedBigramOrder == ColumnOrder.GRAM_HIGH_TO_LOW
                        ? ColumnOrder.GRAM_LOW_TO_HIGH : ColumnOrder.GRAM_HIGH_TO_LOW);
                break;
            case R.id.freq_aligned_bigram_normal:
                alignedBigramFrequency.populateGramFrequency(oldAlignedBigramOrder == ColumnOrder.NORMAL_HIGH_TO_LOW
                        ? ColumnOrder.NORMAL_LOW_TO_HIGH : ColumnOrder.NORMAL_HIGH_TO_LOW);
                break;
            case R.id.freq_aligned_bigram_percent:
                alignedBigramFrequency.populateGramFrequency(oldAlignedBigramOrder == ColumnOrder.PERCENT_HIGH_TO_LOW
                        ? ColumnOrder.PERCENT_LOW_TO_HIGH : ColumnOrder.PERCENT_HIGH_TO_LOW);
                break;

            case R.id.freq_trigram_count:
                trigramFrequency.populateGramFrequency(oldTrigramOrder == ColumnOrder.COUNT_HIGH_TO_LOW
                        ? ColumnOrder.COUNT_LOW_TO_HIGH : ColumnOrder.COUNT_HIGH_TO_LOW);
                break;
            case R.id.freq_trigram_gram:
                trigramFrequency.populateGramFrequency(oldTrigramOrder == ColumnOrder.GRAM_HIGH_TO_LOW
                        ? ColumnOrder.GRAM_LOW_TO_HIGH : ColumnOrder.GRAM_HIGH_TO_LOW);
                break;
            case R.id.freq_trigram_normal:
                trigramFrequency.populateGramFrequency(oldTrigramOrder == ColumnOrder.NORMAL_HIGH_TO_LOW
                        ? ColumnOrder.NORMAL_LOW_TO_HIGH : ColumnOrder.NORMAL_HIGH_TO_LOW);
                break;
            case R.id.freq_trigram_percent:
                trigramFrequency.populateGramFrequency(oldTrigramOrder == ColumnOrder.PERCENT_HIGH_TO_LOW
                        ? ColumnOrder.PERCENT_LOW_TO_HIGH : ColumnOrder.PERCENT_HIGH_TO_LOW);
                break;
        }
    }
}