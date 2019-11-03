package mnh.game.ciphercrack;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import mnh.game.ciphercrack.staticanalysis.GeneralAnalysisFragment;
import mnh.game.ciphercrack.staticanalysis.LetterFrequencyFragment;
import mnh.game.ciphercrack.staticanalysis.SuggestCipherFragment;
import mnh.game.ciphercrack.staticanalysis.TrigramFrequencyFragment;
import mnh.game.ciphercrack.util.BottomNavigationListener;
import mnh.game.ciphercrack.util.Settings;
import mnh.game.ciphercrack.staticanalysis.StaticAnalysis;

public class AnalysisActivity extends AppCompatActivity implements AnalysisProvider {

    // calculations we've done once and can be used by fragments
    private int countAlphabetic;
    private int countNonPadding;
    private Map<Character, Integer> freqAsIs;
    private Map<Character, Integer> freqUpper;
    private double ioc;
    private double[] iocCycles;
    private boolean isAllNumeric;

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
        adapter.addFragment(new SuggestCipherFragment(this, text, alphabet, language), getString(R.string.suggest));
        adapter.addFragment(new GeneralAnalysisFragment(this, text, alphabet), getString(R.string.general));
        adapter.addFragment(new LetterFrequencyFragment(this, text, alphabet), getString(R.string.letters));
        adapter.addFragment(new BigramFrequencyFragment(this, text, alphabet), getString(R.string.bigrams));
        adapter.addFragment(new TrigramFrequencyFragment(this, text, alphabet), getString(R.string.trigrams));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
        tabLayout.setupWithViewPager(viewPager);

        // the bottom navigation bar
        BottomNavigationView bottom = findViewById(R.id.static_analysis_bottom_navigation);
        bottom.setOnNavigationItemSelectedListener(new BottomNavigationListener(this));
        bottom.setSelectedItemId(R.id.bottom_cipher);

        // gather initial analysis
        isAllNumeric = StaticAnalysis.isAllNumeric(text);
        countAlphabetic = StaticAnalysis.countAlphabetic(text, alphabet);
        countNonPadding = StaticAnalysis.countNonPadding(text, paddingChars);
        freqAsIs = StaticAnalysis.collectFrequency(text, false, alphabet);
        freqUpper = StaticAnalysis.collectFrequency(text, true, alphabet);
        ioc = StaticAnalysis.calculateIOC(freqUpper, countAlphabetic, alphabet);

        iocCycles = StaticAnalysis.getCyclicIOC(text, this, alphabet);
    }

    // allow fragments in the tabs to get the analysis we've done
    @Override
    public int getCountAlphabetic() { return countAlphabetic; }
    @Override
    public int getCountNonPadding() { return countNonPadding; }
    @Override
    public Map<Character, Integer> getFreqAsIs() { return freqAsIs; }
    @Override
    public Map<Character, Integer> getFreqUpper() { return freqUpper; }
    @Override
    public double getIOC() { return ioc; }
    @Override
    public double[] getIOCCycles() { return iocCycles; }
    @Override
    public boolean isAllNumeric() { return isAllNumeric; }
}