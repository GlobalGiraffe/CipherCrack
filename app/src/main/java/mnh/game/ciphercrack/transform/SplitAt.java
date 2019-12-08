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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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

    private String splitPointRegExpr;
    private boolean keepRegExpr = false;
    private int splitEveryCountChars = 0;

    private String additionalChars = "";
    private boolean addBefore = true;

    // setters used for unit testing without a dialog
    void setRegExpr(String regExpr, boolean keep) {
        this.splitPointRegExpr = regExpr;
        this.keepRegExpr = keep;
    }
    void setAdditionalChars(String additionalChars, boolean before) {
        this.additionalChars = additionalChars;
        this.addBefore = before;
    }
    void setSplitCount(int count) {
        this.splitEveryCountChars = count;
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
        addBefore = !afterBox.isChecked();
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
        List<String> intermediateRows;
        String charsToAdd = additionalChars;

        // do the split, but sanity check the parameters before use
        if (splitPointRegExpr == null || splitPointRegExpr.length() == 0) {
            // splitting every 'n' chars
            if (splitEveryCountChars <= 0)
                return null;
            if (splitEveryCountChars > text.length())
                return text;
            intermediateRows = new LinkedList<>();
            for (int pos = 0; pos < text.length(); pos += splitEveryCountChars) {
                int endPos = pos + splitEveryCountChars;
                String splitText = text.substring(pos, endPos >= text.length() ? text.length() : endPos);
                intermediateRows.add(splitText);
            }
            if (charsToAdd == null || charsToAdd.length() == 0) {
                charsToAdd = "\n";
            }
        } else {
            // split at a regular expression
            if (!keepRegExpr) {
                intermediateRows = Arrays.asList(text.split(splitPointRegExpr));
            } else {
                // we want to keep the reg expr, splitting just before, or just after it
                Pattern pattern = Pattern.compile(splitPointRegExpr);   // the pattern to search for
                Matcher matcher = pattern.matcher(text);
                intermediateRows = new LinkedList<>();
                int startPoint = 0;
                while (matcher.find()) {
                    // if splitting before, don't include the RE match
                    if (addBefore) {
                        intermediateRows.add(text.substring(startPoint, matcher.start()));
                        startPoint = matcher.start();
                    } else { // do include the RE match since we're splitting AFTER
                        intermediateRows.add(text.substring(startPoint, matcher.end()));
                        startPoint = matcher.end();
                    }
                }
                if (startPoint < text.length()) {
                    intermediateRows.add(text.substring(startPoint, text.length()));
                }
            }
        }

        // now join back up
        StringBuilder sb = new StringBuilder();
        for (String row : intermediateRows) {
            if (sb.length() != 0)
                sb.append(charsToAdd);
            sb.append(row);
        }
        return sb.toString();
    }
}
