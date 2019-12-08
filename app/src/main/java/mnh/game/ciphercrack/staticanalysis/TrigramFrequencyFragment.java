package mnh.game.ciphercrack.staticanalysis;

import android.content.Context;

import mnh.game.ciphercrack.R;

public class TrigramFrequencyFragment extends FrequencyFragment {

    public TrigramFrequencyFragment(Context context, String text) {
        super(context, text, 3, false, R.layout.fragment_trigram_frequency, R.id.freq_trigram_layout);
    }
}
