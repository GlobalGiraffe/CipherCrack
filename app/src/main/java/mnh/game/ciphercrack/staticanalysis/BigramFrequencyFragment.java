package mnh.game.ciphercrack.staticanalysis;

import mnh.game.ciphercrack.R;

public class BigramFrequencyFragment extends FrequencyFragment {

    public BigramFrequencyFragment(String text, String alphabet) {
        super(text, alphabet, 2, R.layout.fragment_bigram_frequency, R.id.freq_bigram_layout);
    }
}
