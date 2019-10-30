package mnh.game.ciphercrack.staticanalysis;

import mnh.game.ciphercrack.R;

public class TrigramFrequencyFragment extends FrequencyFragment {

    public TrigramFrequencyFragment(String text, String alphabet) {
        super(text, alphabet, 3, R.layout.fragment_trigram_frequency, R.id.freq_trigram_layout);
    }
}
