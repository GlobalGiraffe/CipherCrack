package mnh.game.ciphercrack.staticanalysis;

/**
 * A class to hold the frequencies of letters, bigrams and trigrams that go in the tabbed view
 */
public class FrequencyEntry implements Comparable<FrequencyEntry> {

    private final String gram;
    private final int count;
    private final float percent;
    private final float normal;

    FrequencyEntry(String gram, int count, float percent, float normal) {
        this.gram = gram;
        this.count = count;
        this.percent = percent;
        this.normal = normal;
    }

    public String getGram() { return gram; }
    public int getCount() { return count; }
    public float getPercent() { return percent; }
    public float getNormal() { return normal; }

    public int compareTo(FrequencyEntry other) {
        return 0;
    }
}