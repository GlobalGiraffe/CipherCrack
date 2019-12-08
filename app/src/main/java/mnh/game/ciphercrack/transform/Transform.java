package mnh.game.ciphercrack.transform;

import android.content.Context;
import android.widget.EditText;

import mnh.game.ciphercrack.HomeActivity;

/**
 * Interface for all the simple text transformers: split, clear, reverse, upper/lower case, etc
 */
public abstract class Transform {
    public abstract String apply(Context context, String text);
    public boolean needsDialog() { return false; }
    public void presentDialog(HomeActivity context, EditText textField) { }
}
