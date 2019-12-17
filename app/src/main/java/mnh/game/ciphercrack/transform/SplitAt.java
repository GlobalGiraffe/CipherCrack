package mnh.game.ciphercrack.transform;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mnh.game.ciphercrack.HomeActivity;
import mnh.game.ciphercrack.R;

/**
 * Present user with dialog to define a way to split the text at:
 * - a regular expression locates the text where split will happen
 * - every 'n' chars
 * Optionally insert chars (before or after split point)
 */
public class SplitAt extends Transform {

    // delete the field if 'X' is pressed
    private static final View.OnClickListener SPLITAT_ON_CLICK_DELETE = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.split_at_add_chars_delete:
                    EditText ch = v.getRootView().findViewById(R.id.split_at_add_chars);
                    ch.setText("");
                    break;
                case R.id.split_at_char_position_delete:
                    EditText cp = v.getRootView().findViewById(R.id.split_at_char_position);
                    cp.setText("");
                    break;
                case R.id.split_at_regular_expression_delete:
                    EditText re = v.getRootView().findViewById(R.id.split_at_regular_expression);
                    re.setText("");
                    break;
            }
        }
    };

    // regular expression for text at which the split should take place
    private String splitPointRegExpr;

    // if true, the regular expression is still there in the output
    private boolean keepRegExpr = false;

    // if not using regular expression, split at certain columns
    private int splitEveryCountChars = 0;

    // what chars should be added into the split point
    private String additionalChars = "";

    // do we add extra chars before the regular expression, or after
    private boolean extraGoesAfter = true;

    // setter used for unit testing without a dialog
    void setSplitAt(int count, String regExpr, boolean keep, String additionalChars, boolean extraGoesAfter) {
        this.splitEveryCountChars = count;
        this.additionalChars = additionalChars;
        this.extraGoesAfter = extraGoesAfter;
        this.splitPointRegExpr = regExpr;
        this.keepRegExpr = keep;
    }

    @Override
    public boolean needsDialog() { return true; }

    @Override
    public void presentDialog(HomeActivity activity, EditText textField) {
        // show a dialog with some controls
        LinearLayout viewGroup = activity.findViewById(R.id.transform_split_at_layout);
        LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupLayout = inflater.inflate(R.layout.transform_split_at, viewGroup);
        PopupWindow popup = new PopupWindow(activity);
        popup.setContentView(popupLayout);
        popup.setWidth(820);
        popup.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);

        // ensure we 'delete' the various texts when the delete button is pressed
        Button deleteButton = popupLayout.findViewById(R.id.split_at_add_chars_delete);
        deleteButton.setOnClickListener(SPLITAT_ON_CLICK_DELETE);
        deleteButton = popupLayout.findViewById(R.id.split_at_char_position_delete);
        deleteButton.setOnClickListener(SPLITAT_ON_CLICK_DELETE);
        deleteButton = popupLayout.findViewById(R.id.split_at_regular_expression_delete);
        deleteButton.setOnClickListener(SPLITAT_ON_CLICK_DELETE);

        Button cancelButton = popupLayout.findViewById(R.id.transform_button_cancel);
        Button crackButton = popupLayout.findViewById(R.id.transform_button_apply);
        crackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SplitAt.this.gatherDialogChoices(popup);
                popup.dismiss();
                activity.applyEdit(SplitAt.this, textField);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
        popup.showAtLocation(popupLayout, Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, 100);
    }

    private void gatherDialogChoices(PopupWindow popup) {
        EditText addCharField = popup.getContentView().findViewById(R.id.split_at_add_chars);
        EditText regExprField = popup.getContentView().findViewById(R.id.split_at_regular_expression);
        CheckBox keepBox = popup.getContentView().findViewById(R.id.split_at_keep);
        CheckBox afterBox = popup.getContentView().findViewById(R.id.split_at_is_after);
        EditText numericSplitField = popup.getContentView().findViewById(R.id.split_at_char_position);

        additionalChars = addCharField.getText().toString();
        splitPointRegExpr = regExprField.getText().toString();
        keepRegExpr = keepBox.isChecked();
        extraGoesAfter = afterBox.isChecked();
        try {
            int num = Integer.parseInt(numericSplitField.getText().toString());
            splitEveryCountChars = num;
        } catch (NumberFormatException ex) {
            splitEveryCountChars = 0;
        }
    }

    @Override
    public String apply(Context context, String text) {
        if (text == null)
            return null;
        String charsToAdd = (additionalChars == null || additionalChars.length() == 0) ? "\n" : additionalChars;
        StringBuilder results = new StringBuilder();

        // do the split, but sanity check the parameters before use
        if (splitPointRegExpr == null || splitPointRegExpr.length() == 0) {
            // splitting every 'n' chars
            if (splitEveryCountChars <= 0)
                return null;
            if (splitEveryCountChars > text.length())
                return text;
            for (int pos = 0; pos < text.length(); pos += splitEveryCountChars) {
                int endPos = pos + splitEveryCountChars;
                String splitText = text.substring(pos, endPos >= text.length() ? text.length() : endPos);
                if (pos != 0)
                    results.append(charsToAdd);
                results.append(splitText);
            }
        } else {
            // split at a regular expression
            // we want to keep (or not) the reg expr, splitting just before, or just after it
            Pattern pattern = Pattern.compile(splitPointRegExpr);   // the pattern to search for
            Matcher matcher = pattern.matcher(text);
            int startPoint = 0;
            StringBuilder section = new StringBuilder();
            while (matcher.find()) {
                section.setLength(0);
                if (keepRegExpr) { // we want the reg expr in the output
                    // if splitting before, don't include the RE match
                    if (extraGoesAfter) {
                        // text before-RE + RE + Add
                        // do include the RE match since we're splitting before and keeping
                        section.append(text.substring(startPoint, matcher.end()))
                                .append(charsToAdd);
                    } else {
                        // text before-RE + Add + RE
                        section.append(text.substring(startPoint, matcher.start()))
                                .append(charsToAdd)
                                .append(text.substring(matcher.start(),matcher.end()));
                    }
                } else {
                    // before-RE + Add
                    section.append(text.substring(startPoint, matcher.start()))
                            .append(charsToAdd);
                }
                startPoint = matcher.end();
                results.append(section.toString());
            }
            if (startPoint < text.length()) {
                results.append(text.substring(startPoint));
            }
        }

        return results.toString();
    }
}
