package mnh.game.ciphercrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import mnh.game.ciphercrack.cipher.Cipher;
import mnh.game.ciphercrack.transform.SplitByWords;
import mnh.game.ciphercrack.util.Directives;

import static android.view.View.GONE;

public class ResultActivity extends AppCompatActivity {

    public static final int RESULTS_REQUEST_CODE = 1000;

    private static final String EXTRA_TEXT = "Txt";
    private static final String EXTRA_RESULT = "Res";
    private static final String EXTRA_EXPLAIN = "Exp";
    private static final String EXTRA_CIPHER = "Cyp";
    private static final String EXTRA_DIRECTIVES = "Dir";

    private Cipher cipher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String text = getIntent().getStringExtra(EXTRA_TEXT);
        String result = getIntent().getStringExtra(EXTRA_RESULT);
        String explain  = getIntent().getStringExtra(EXTRA_EXPLAIN);
        cipher = getIntent().getParcelableExtra(EXTRA_CIPHER);
        // we use dto need these, not any more
        //Directives dirs = getIntent().getParcelableExtra(EXTRA_DIRECTIVES);

        Toolbar toolbar = findViewById(R.id.result_toolbar);
        toolbar.setTitle(cipher.getInstanceDescription());
        setSupportActionBar(toolbar);

        // put the text on the screen
        TextView textView = findViewById(R.id.result_input_text);
        textView.setText(text);

        // put the result on the screen
        textView = findViewById(R.id.result_result_text);
        textView.setText(result);

        // put the explanation of the result on the screen
        // but only if there is any explanation: only used for Crack
        textView = findViewById(R.id.result_explain_text);
        if (explain == null || explain.length() == 0) {
            textView.setVisibility(GONE);
        } else {
            textView.setText(explain);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.result_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // show settings
        if (item.getItemId() == R.id.action_result_settings) {
            showSettings(null);
            return true;
        }
        // copy the result text back to the main screen
        if (item.getItemId() == R.id.action_result_copy) {
            TextView textView = findViewById(R.id.result_result_text);
            String result = textView.getText().toString();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("RESULT_TEXT", result);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            return true;
        }
        // split the text on the results box into separate words
        if (item.getItemId() == R.id.action_result_split_words) {
            TextView textView = findViewById(R.id.result_result_text);
            String result = textView.getText().toString();
            String newResult = new SplitByWords().apply(this, result);
            textView.setText(newResult);
            return true;
        }
        // create an email with these results in the body
        if (item.getItemId() == R.id.action_result_email) {
            TextView textView = findViewById(R.id.result_input_text);
            String input = textView.getText().toString();
            textView = findViewById(R.id.result_result_text);
            String result = textView.getText().toString();
            textView = findViewById(R.id.result_explain_text);
            String explain = textView.getText().toString();

            Intent i = new Intent(Intent.ACTION_SEND);

            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_SUBJECT, "Crack with cipher: "+cipher.getInstanceDescription());
            i.putExtra(Intent.EXTRA_TEXT, "Input:\n"+input+"\n\nResult:\n"+result+"\n\nExplain\n"+explain+"\n");
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
            }
            return true;
        }

        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    // Settings button pressed - show settings
    private void showSettings(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    /**
     * Get the intent for opening the Result screen
     * @param context the activity initiating the intent
     * @param text the text to as original
     * @param resultText what to show as the encoded/decoded text
     * @param explainText for crack only, the explain text as to how we cracked
     * @param cipher the name of the cipher we used
     * @return the Intent that can launch the result screen suitably
     */
    public static Intent getResultIntent(AppCompatActivity context, final String text, final String resultText,
                                          final String explainText, final Cipher cipher,
                                          final Directives dirs) {
        Intent i = new Intent(context, ResultActivity.class);
        i.putExtra(ResultActivity.EXTRA_TEXT, text);
        i.putExtra(ResultActivity.EXTRA_RESULT, resultText);
        i.putExtra(ResultActivity.EXTRA_EXPLAIN, explainText);
        i.putExtra(ResultActivity.EXTRA_CIPHER, cipher);
        i.putExtra(ResultActivity.EXTRA_DIRECTIVES, dirs);
        return i;
    }
}