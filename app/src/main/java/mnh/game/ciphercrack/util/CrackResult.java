package mnh.game.ciphercrack.util;

import android.os.Parcel;
import android.os.Parcelable;

import mnh.game.ciphercrack.cipher.Cipher;

/**
 * Class used to pass results back from a crack attempt
 * On failure, only cipherText and explain is set
 * On success, plainText and directives are also set
 */
public class CrackResult implements Parcelable {

    private final int id;
    private CrackMethod crackMethod;
    private Cipher cipher;
    private String cipherText;
    private String explain;
    private boolean isSuccess;
    private String plainText = null;
    private Directives directives = null;
    private long milliseconds = 0L;
    private String progress = "";
    private CrackState crackState;

    // used to assign unique values to a crack result
    private static int masterId = 10;

    // needed by Parcelable interface, to recreate the passed data
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CrackResult createFromParcel(Parcel in) {
            return new CrackResult(in);
        }
        public CrackResult[] newArray(int size) {
            return new CrackResult[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    // this is called for a successful crack
    public CrackResult(CrackMethod crackMethod, Cipher cipher, Directives directives, String cipherText, String plainText, String explain) {
        this.id = getNextId();
        this.crackMethod = crackMethod;
        this.cipher = cipher;
        this.directives = directives;
        this.plainText = plainText;
        this.cipherText = cipherText;
        this.explain = explain;
        this.crackState = CrackState.COMPLETE;
        this.isSuccess = true;
    }

    // this is called for an unsuccessful crack or a cancellation
    public CrackResult(CrackMethod crackMethod, Cipher cipher, String cipherText, String explain) {
        this.id = getNextId();
        this.crackMethod = crackMethod;
        this.cipher = cipher;
        this.cipherText = cipherText;
        this.explain = explain;
        this.crackState = CrackState.COMPLETE;
        this.isSuccess = false;
    }

    // this is called for an unsuccessful crack, where the best result may be of use, e.g. IOC crack
    public CrackResult(CrackMethod crackMethod, Cipher cipher, String cipherText, String explain, String bestDecode) {
        this.id = getNextId();
        this.crackMethod = crackMethod;
        this.cipher = cipher;
        this.cipherText = cipherText;
        this.explain = explain;
        this.plainText = bestDecode;
        this.crackState = CrackState.COMPLETE;
        this.isSuccess = false;
    }

    // this is called when creating a brand new crack at the start of the CrackService call
    public CrackResult(CrackMethod crackMethod, Cipher cipher, Directives dirs, String cipherText, String explain, CrackState state) {
        this.id = getNextId();
        this.crackMethod = crackMethod;
        this.cipher = cipher;
        this.directives = dirs;
        this.cipherText = cipherText;
        this.explain = explain;
        this.crackState = state;
        this.isSuccess = false;
    }

    // this is called when a running crack detects it's being cancelled
    public CrackResult(CrackMethod crackMethod, Cipher cipher, String cipherText, String explain, CrackState state) {
        this.id = getNextId();
        this.crackMethod = crackMethod;
        this.cipher = cipher;
        this.cipherText = cipherText;
        this.explain = explain;
        this.crackState = state;
        this.isSuccess = false;
    }

    public CrackResult(Parcel in) {
        crackMethod = CrackMethod.valueOf(in.readString());
        crackState = CrackState.valueOf(in.readString());
        cipher = in.readParcelable(Cipher.class.getClassLoader());
        cipherText = in.readString();
        plainText = in.readString();
        explain = in.readString();
        progress = in.readString();
        isSuccess = (in.readInt() == 1);
        id = in.readInt();
        directives = in.readParcelable(Directives.class.getClassLoader());
        milliseconds = in.readLong();
    }

    // once a result is ready, we need to replace it in the list
    public void setFields(CrackResult result) {
        crackMethod = result.getCrackMethod();
        crackState = result.getCrackState();
        plainText = result.getPlainText();
        cipherText = result.getCipherText();
        cipher = result.getCipher();
        explain = result.getExplain();
        isSuccess = result.isSuccess();
        directives = result.getDirectives();
        milliseconds = result.getMilliseconds();
        progress = result.getProgress();
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeString(crackMethod.name());
        p.writeString(crackState.name());
        p.writeParcelable(cipher, 0);
        p.writeString(cipherText);
        p.writeString(plainText);
        p.writeString(explain);
        p.writeString(progress);
        p.writeInt(isSuccess?1:0);
        p.writeInt(id);
        p.writeParcelable(directives, 0);
        p.writeLong(milliseconds);
    }

    private synchronized int getNextId() {
        return masterId++;
    }

    public int getId() {
        return id;
    }
    public CrackMethod getCrackMethod() { return crackMethod; }
    public CrackState getCrackState() { return crackState; }
    public Cipher getCipher() {
        return cipher;
    }
    public Directives getDirectives() {
        return directives;
    }
    public String getCipherText() { return cipherText; }
    public String getPlainText() { return plainText; }
    public String getExplain() {
        return explain;
    }
    public long getMilliseconds() {
        return milliseconds;
    }
    public boolean isSuccess() {
        return isSuccess;
    }
    public String getProgress() { return progress; }

    public void setMilliseconds(long milliseconds) { this.milliseconds = milliseconds; }
    public void setProgress(String progress) { this.progress = progress; }
    public void setCrackState(CrackState state) { this.crackState = state; }
    // these are probably never used - above methods probably suffice
    /*
    public void setExplain(String explain) { this.explain = explain; }
    public void setPlainText(String plainText) { this.plainText = plainText; }
    public void setCipherText(String cipherText) { this.cipherText = cipherText; }
    public void setDirectives(Directives directives) { this.directives = directives; }
    public void setSuccess(boolean isSuccess) { this.isSuccess = isSuccess; }
     */

    /**
     * Has to be overridden as we've overridden equals - use the same (id) field
     * @return a hash code such that any CrackResult with the same id has the same hashCode
     */
    @Override
    public int hashCode() {
        return 31 * 17 + id;
    }

    /**
     * Two Crack Results are equal if they are the same instance or have the same 'id'
     * @param other the crack result to compare this one too
     * @return true if the two results are equal
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CrackResult))
            return false;
        if (other == this)
            return true;
        return ((CrackResult)other).getId() == this.id;
    }
}
