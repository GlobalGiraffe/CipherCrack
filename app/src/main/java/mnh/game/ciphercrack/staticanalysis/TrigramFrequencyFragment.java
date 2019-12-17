package mnh.game.ciphercrack.staticanalysis;

import android.content.Context;

import mnh.game.ciphercrack.R;

public class TrigramFrequencyFragment extends FrequencyFragment {

    public TrigramFrequencyFragment(Context context, String text, boolean aligned, int overallLayoutId, int tableLayoutId) {
        super(context, text, 3, aligned, overallLayoutId, tableLayoutId);
    }
}
