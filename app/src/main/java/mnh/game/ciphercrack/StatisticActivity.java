package mnh.game.ciphercrack;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.Settings;
import mnh.game.ciphercrack.util.StaticAnalysis;

public class StatisticActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // get what we will need from parcel sent, place on the screen
        String text = getIntent().getStringExtra("TEXT");
        String alphabet = Settings.instance().getString(this, getString(R.string.pref_alphabet_plain));

        Toolbar toolbar = findViewById(R.id.stats_toolbar);
        toolbar.setTitle(R.string.analysis);
        setSupportActionBar(toolbar);

        // gather stats
        String stats = gatherStats(text, alphabet);
        TextView textView = findViewById(R.id.stats_text);
        textView.setText(stats);
    }

    private String gatherStats(String text, String alphabet) {
        // work out which language we should use
        String languageName = Settings.instance().getString(this, getString(R.string.pref_language));
        Language language = Language.instanceOf(languageName);

        // we will use this to check for presence in the alphabet
        //String textUpper = text.toUpperCase();

        // we keep track of counts of letters converted to upper case
        //  and also counts of letters in original form - less useful statistically
        int countAlphabetic = StaticAnalysis.countAlphabetic(text, alphabet);
        HashMap<Character, Integer> freqAsIs = StaticAnalysis.collectFrequency(text, false, alphabet);
        HashMap<Character, Integer> freqUpper = StaticAnalysis.collectFrequency(text, true, alphabet);
        int countUniqueUpperAlphabetic = freqUpper.keySet().size();
        int countUniqueAsIsAlphabetic = freqAsIs.keySet().size();

        // calculate how likely it is to see same letter twice - helps decide cipher scheme
        double ioc = StaticAnalysis.getIOC(freqUpper, countAlphabetic, alphabet);

        // TODO: calc IOC for sections at a time (for Vigenere key length suggestion)

        // now gather our findings into some text
        StringBuilder s = new StringBuilder(1000);
        s.append("Alphabetic letters: ").append(countAlphabetic).append("\n");
        s.append("Unique Letters: ").append(countUniqueAsIsAlphabetic).append("\n");
        s.append("Unique Letters when made upper: ").append(countUniqueUpperAlphabetic).append("\n");
        s.append("Index of Coincidence (IOC): ").append(String.format(Locale.getDefault(),"%7.6f",ioc)).append("\n");
        s.append("Expected IOC for ").append(languageName).append(": ").append(String.format(Locale.getDefault(),"%7.6f",language.getExpectedIOC())).append("\n");
        for (int i=0; i < alphabet.length(); i++) {
            char textChar = alphabet.charAt(i);
            Integer freq = freqUpper.get(textChar);
            freq = (freq == null) ? 0 : freq ;
            float percent = (100.0f * freq)/countAlphabetic;
            s.append(alphabet.charAt(i))
                    .append(": freq=")
                    .append(freq)
                    .append(", %=")
                    .append(String.format(Locale.getDefault(),"%4.2f",percent))
                    .append(", norm=")
                    .append(String.format(Locale.getDefault(),"%4.2f",language.frequencyOf(textChar)))
                    .append("\n");
        }
        return s.toString();
    }

    public void showFrequency(View view) {
        // TODO: Show frequency of single letters/symbols
    }
    public void showBigraphFrequency(View view) {
        // TODO: Show frequency of pairs of letters/symbols
    }
    public void showTrigraphFrequency(View view) {
        // TODO: Show frequency of triples of letters/symbols
    }
}