package mnh.game.ciphercrack.staticanalysis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import mnh.game.ciphercrack.AnalysisActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.language.Language;

/**
 * This is the screen that shows which cipher is likely, using static analysis of the text
 */
public class SuggestCipherFragment extends Fragment {

    private final AnalysisProvider analysis;
    private final Language language;

    public SuggestCipherFragment(AnalysisActivity analysis, Language language) {
        this.analysis = analysis;
        this.language = language;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis_suggest, container, false);
        // gather analysis and place on the screen
        String stats = StaticAnalysis.produceSuggestionsForCipher(analysis, language);
        TextView textView = view.findViewById(R.id.stats_suggest_text);
        textView.setText(stats);
        return view;
    }

}
