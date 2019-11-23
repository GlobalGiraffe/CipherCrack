package mnh.game.ciphercrack.staticanalysis;

import mnh.game.ciphercrack.R;

public class LetterFrequencyFragment extends FrequencyFragment {

    public LetterFrequencyFragment(String text) {
        super(text, 1, R.layout.fragment_letter_frequency, R.id.freq_letter_layout);
    }

}
