package mnh.game.ciphercrack.staticanalysis;

import mnh.game.ciphercrack.AnalysisActivity;
import mnh.game.ciphercrack.R;

public class TrigramFrequencyFragment extends FrequencyFragment {

    public TrigramFrequencyFragment(AnalysisActivity analysis, String text, String alphabet) {
        super(analysis, text, alphabet, 3, R.layout.fragment_trigram_frequency, R.id.freq_trigram_layout);
    }
}
