package mnh.game.ciphercrack.staticanalysis;

import mnh.game.ciphercrack.R;

public class TrigramFrequencyFragment extends FrequencyFragment {

    public TrigramFrequencyFragment(String text) {
        super(text, 3, R.layout.fragment_trigram_frequency, R.id.freq_trigram_layout);
    }
}
