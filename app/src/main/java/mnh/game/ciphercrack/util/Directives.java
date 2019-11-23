package mnh.game.ciphercrack.util;

import android.os.Parcel;
import android.os.Parcelable;

import mnh.game.ciphercrack.language.Language;

/**
 * Holds the parameters telling an encode, decode or crack operation how to perform
 *
 * This is parcelable because we need to pass the Directives to the Result Activity via an Intent
 */
public class Directives implements Parcelable {

    // used across many ciphers
    private Language language;
    private String alphabet;
    private String cribs;
    private String paddingChars;

    private int shift;                  // used for caesar, ROT13
    private int valueA;                 // used for affine
    private int valueB;
    private int rails;                  // used for railfence, also for cycleLength of skytale cipher
    private int keywordLength;          // used for vigenere
    private String keyword;             // used for vigenere and keyword substitution
    private String digits;              // used for binary, e.g. "01"
    private String separator;           // used for binary between letters, e.g. "/"
                                        // also used for Polybius replace, e.g. J with I
    private String heading;             // used for column/row headings in Polybius square cipher
    private int numberSize;             // used for binary, number of digits making a letter = 5
    private int[] permutation;          // use for permutation cipher
    private boolean readAcross;         // read final answer across columns or down (permutation)
    private CrackMethod crackMethod;    // used only when cracking any cipher
    // if any more, add to Parcel methods below...

    // needed by Parcelable interface, to recreate the passed data
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Directives createFromParcel(Parcel in) {
            return new Directives(in);
        }
        public Directives[] newArray(int size) {
            return new Directives[size];
        }
    };

    // set some defaults
    public Directives() {
        language = Language.instanceOf(Settings.DEFAULT_LANGUAGE);
        alphabet = Settings.DEFAULT_ALPHABET;
        paddingChars = Settings.DEFAULT_PADDING_CHARS;
        crackMethod = CrackMethod.NONE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Directives(Parcel in) {
        String languageName = in.readString();
        language = Language.instanceOf(languageName);
        alphabet = in.readString();
        cribs = in.readString();
        paddingChars = in.readString();
        shift = in.readInt();
        valueA = in.readInt();
        valueB = in.readInt();
        rails = in.readInt();
        keywordLength = in.readInt();
        keyword = in.readString();
        digits = in.readString();
        separator = in.readString();
        heading = in.readString();
        numberSize = in.readInt();
        int permutationSize = in.readInt();
        permutation = new int[permutationSize];
        in.readIntArray(permutation);
        readAcross = in.readInt() == 1;
        String crackText = in.readString();
        crackMethod = CrackMethod.valueOf(crackText);
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        if (language != null)
            p.writeString(language.getName());
        else
            p.writeString("");
        p.writeString(alphabet);
        p.writeString(cribs);
        p.writeString(paddingChars);
        p.writeInt(shift);
        p.writeInt(valueA);
        p.writeInt(valueB);
        p.writeInt(rails);
        p.writeInt(keywordLength);
        p.writeString(keyword);
        p.writeString(digits);
        p.writeString(separator);
        p.writeString(heading);
        p.writeInt(numberSize);
        if (permutation == null) {
            p.writeInt(0);
            p.writeInt(0);
        } else {
            p.writeInt(permutation.length);
            p.writeIntArray(permutation);
        }
        p.writeInt(readAcross?1:0);
        p.writeString(crackMethod == null? CrackMethod.NONE.name() : crackMethod.name());
    }

    public void setLanguage(Language language) { this.language = language; }

    public void setAlphabet(String alphabet) { this.alphabet = alphabet; }

    public void setCribs(String cribs) { this.cribs = cribs; }

    public void setPaddingChars(String paddingChars) { this.paddingChars = paddingChars; }

    public void setValueA(int valueA) { this.valueA = valueA; }

    public void setValueB(int valueB) { this.valueB = valueB; }

    public void setShift(int shift) { this.shift = shift; }

    public void setRails(int rails) { this.rails = rails; }

    public void setPermutation(int[] permutation) { this. permutation = permutation; }

    public void setKeyword(String keyword) { this.keyword = keyword; }

    public void setReplace(String replace) { this.separator = replace; }

    public void setDigits(String digits) { this.digits = digits; }

    public void setSeparator(String separator) { this.separator = separator; }

    public void setHeading(String heading) { this.heading = heading; }

    public void setNumberSize(int numberSize) { this.numberSize = numberSize; }

    public void setKeywordLength(int keywordLength) { this.keywordLength = keywordLength; }

    public void setCrackMethod(CrackMethod crackMethod) { this.crackMethod = crackMethod; }

    public void setReadAcross(boolean readAcross) { this.readAcross = readAcross; }

    public Language getLanguage() { return language; }

    public String getAlphabet() { return alphabet; }

    public String getCribs() { return cribs; }

    public String getPaddingChars() { return paddingChars; }

    public CrackMethod getCrackMethod() { return crackMethod; }

    public int getValueA() { return valueA; }

    public int getValueB() { return valueB; }

    public int getShift() { return shift; }

    public int getRails() { return rails; }

    public int[] getPermutation() { return permutation; }

    public String getKeyword() { return keyword; }

    public int getKeywordLength() { return keywordLength; }

    public String getDigits() { return digits; }

    public String getSeparator() { return separator; }

    public String getHeading() { return heading; }

    public String getReplace() { return separator; }

    public int getNumberSize() { return numberSize; }

    public boolean isReadAcross() { return readAcross; }

}
