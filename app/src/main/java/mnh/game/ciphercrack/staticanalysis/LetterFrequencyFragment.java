package mnh.game.ciphercrack.staticanalysis;

import android.content.Context;

import mnh.game.ciphercrack.R;

public class LetterFrequencyFragment extends FrequencyFragment {

    public LetterFrequencyFragment(Context context, String text) {
        super(context, text, 1, false, R.layout.fragment_letter_frequency, R.id.freq_letter_layout);
    }

}
