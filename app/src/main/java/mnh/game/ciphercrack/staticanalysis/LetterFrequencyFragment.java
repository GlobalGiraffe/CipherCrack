package mnh.game.ciphercrack.staticanalysis;

import mnh.game.ciphercrack.AnalysisActivity;
import mnh.game.ciphercrack.R;

public class LetterFrequencyFragment extends FrequencyFragment {

    public LetterFrequencyFragment(AnalysisActivity analysis, String text, String alphabet) {
        super(analysis, text, alphabet, 1, R.layout.fragment_letter_frequency, R.id.freq_letter_layout);
    }

}
