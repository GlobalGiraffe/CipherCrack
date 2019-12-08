package mnh.game.ciphercrack.staticanalysis;

import android.content.Context;

public class BigramFrequencyFragment extends FrequencyFragment {

    public BigramFrequencyFragment(Context context, String text, boolean aligned, int overallLayoutId, int tableLayoutId) {
        super(context, text, 2, aligned, overallLayoutId, tableLayoutId);
    }
}
