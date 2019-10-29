package mnh.game.ciphercrack.util;

/**
 * Class used to pass results back from a crack attempt
 * On failure, only cipherText and explain is set
 * On success, plainText and directives are also set
 */
public class CrackResult {

    private Directives directives = null;
    private String plainText = null;
    private String cipherText;
    private String explain;
    private boolean isSuccess;
    private long milliseconds;

    // this is called for a successful crack
    public CrackResult(Directives directives, String cipherText, String plainText, String explain) {
        this.directives = directives;
        this.plainText = plainText;
        this.cipherText = cipherText;
        this.explain = explain;
        this.isSuccess = true;
    }

    // this is called for an unsuccessful crack
    public CrackResult(String cipherText, String explain) {
        this.cipherText = cipherText;
        this.explain = explain;
        this.isSuccess = false;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    public String getCipherText() {
        return cipherText;
    }

    public void setCipherText(String cipherText) {
        this.cipherText = cipherText;
    }

    public Directives getDirectives() {
        return directives;
    }

    public void setDirectives(Directives directives) {
        this.directives = directives;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
