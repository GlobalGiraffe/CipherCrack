package mnh.game.ciphercrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import mnh.game.ciphercrack.cipher.Cipher;
import mnh.game.ciphercrack.util.Directives;

import static android.view.View.GONE;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_TEXT = "Txt";
    public static final String EXTRA_RESULT = "Res";
    public static final String EXTRA_EXPLAIN = "Exp";
    public static final String EXTRA_CIPHER = "Cyp";
    public static final String EXTRA_DIRECTIVES = "Dir";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String text = getIntent().getStringExtra(EXTRA_TEXT);
        String result = getIntent().getStringExtra(EXTRA_RESULT);
        String explain  = getIntent().getStringExtra(EXTRA_EXPLAIN);
        String cipherName = getIntent().getStringExtra(EXTRA_CIPHER);
        Directives dirs = (Directives)getIntent().getParcelableExtra((EXTRA_DIRECTIVES));

        Toolbar toolbar = findViewById(R.id.result_toolbar);
        setSupportActionBar(toolbar);

        // create a cipher object so we can get a description [with parameters to the cipher]
        Cipher cipher = Cipher.instanceOf(cipherName, this);
        if (cipher != null) {
            cipher.canParametersBeSet(dirs);
            toolbar.setTitle(cipher.getCipherDescription());
        }

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
        if (item.getItemId() == R.id.action_result_settings) {
            showSettings(null);
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
}