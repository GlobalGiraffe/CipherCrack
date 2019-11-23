package mnh.game.ciphercrack.staticanalysis;

import mnh.game.ciphercrack.R;

public class BigramFrequencyFragment extends FrequencyFragment {

    public BigramFrequencyFragment(String text) {
        super(text, 2, R.layout.fragment_bigram_frequency, R.id.freq_bigram_layout);
    }
}
