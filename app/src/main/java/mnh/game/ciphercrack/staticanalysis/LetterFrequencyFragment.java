package mnh.game.ciphercrack.staticanalysis;

import mnh.game.ciphercrack.R;

public class LetterFrequencyFragment extends FrequencyFragment {

    public LetterFrequencyFragment(String text, String alphabet) {
        super(text, alphabet, 1, R.layout.fragment_letter_frequency, R.id.freq_letter_layout);
    }

    /*
    private String gatherLetterFrequency(String text, String alphabet) {

        String languageName = Settings.instance().getString(this.getContext(), getString(R.string.pref_language));
        Language language = Language.instanceOf(languageName);

        int countAlphabetic = StaticAnalysis.countAlphabetic(text, alphabet);
        HashMap<Character, Integer> freqUpper = StaticAnalysis.collectFrequency(text, true, alphabet);

        StringBuilder s = new StringBuilder();
        for (int i=0; i < alphabet.length(); i++) {
            char textChar = alphabet.charAt(i);
            Integer freq = freqUpper.get(textChar);
            freq = (freq == null) ? 0 : freq ;
            float percent = (100.0f * freq)/countAlphabetic;
            s.append(alphabet.charAt(i))
                    .append(": freq=")
                    .append(freq)
                    .append(", %=")
                    .append(String.format(Locale.getDefault(),"%4.2f",percent))
                    .append(", norm=")
                    .append(String.format(Locale.getDefault(),"%4.2f",language.frequencyOf(textChar)))
                    .append("\n");
        }
        return s.toString();
    }
*/
}
