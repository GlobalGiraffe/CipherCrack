package mnh.game.ciphercrack.staticanalysis;

import mnh.game.ciphercrack.AnalysisActivity;
import mnh.game.ciphercrack.R;

public class BigramFrequencyFragment extends FrequencyFragment {

    public BigramFrequencyFragment(AnalysisActivity analysis, String text, String alphabet) {
        super(analysis, text, alphabet, 2, R.layout.fragment_bigram_frequency, R.id.freq_bigram_layout);
    }
}
