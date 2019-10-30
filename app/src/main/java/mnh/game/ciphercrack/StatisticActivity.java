package mnh.game.ciphercrack;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import mnh.game.ciphercrack.staticanalysis.AnalysisTabAdapter;
import mnh.game.ciphercrack.staticanalysis.BigramFrequencyFragment;
import mnh.game.ciphercrack.staticanalysis.GeneralAnalysisFragment;
import mnh.game.ciphercrack.staticanalysis.LetterFrequencyFragment;
import mnh.game.ciphercrack.staticanalysis.TrigramFrequencyFragment;
import mnh.game.ciphercrack.util.Settings;

public class StatisticActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Toolbar toolbar = findViewById(R.id.static_analysis_toolbar);
        toolbar.setTitle(R.string.analysis);
        setSupportActionBar(toolbar);

        // get what we will need from the parcel sent
        String text = getIntent().getStringExtra("TEXT");
        String alphabet = Settings.instance().getString(this, getString(R.string.pref_alphabet_plain));

        ViewPager viewPager = findViewById(R.id.static_analysis_pager);
        TabLayout tabLayout = findViewById(R.id.static_analysis_tab_layout);
        AnalysisTabAdapter adapter = new AnalysisTabAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        // TODO adapter.addFragment(new StaticSuggestFragment(text, alphabet), "Suggest");
        adapter.addFragment(new GeneralAnalysisFragment(text, alphabet), "General");
        adapter.addFragment(new LetterFrequencyFragment(text, alphabet), "Letters");
        adapter.addFragment(new BigramFrequencyFragment(text, alphabet), "Bigrams");
        adapter.addFragment(new TrigramFrequencyFragment(text, alphabet), "Trigrams");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        tabLayout.setupWithViewPager(viewPager);
    }

        /*
    public void showBigraphFrequency(View view) {
        // TODO: Show frequency of pairs of letters/symbols
    }
    public void showTrigraphFrequency(View view) {
        // TODO: Show frequency of triples of letters/symbols
    }
    */
}