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

import static android.view.View.GONE;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Toolbar toolbar = findViewById(R.id.result_toolbar);
        String cipherDescription = getIntent().getStringExtra("CIPHER_DESCRIPTION");
        toolbar.setTitle(cipherDescription);
        setSupportActionBar(toolbar);

        // put the text on the screen
        String text = getIntent().getStringExtra("TEXT");
        TextView textView = findViewById(R.id.result_input_text);
        textView.setText(text);

        // put the result on the screen
        text = getIntent().getStringExtra("RESULT");
        textView = findViewById(R.id.result_result_text);
        textView.setText(text);

        // put the explanation of the result on the screen
        // but only if there is any explanation: only used for Crack
        text = getIntent().getStringExtra("EXPLAIN");
        textView = findViewById(R.id.result_explain_text);
        if (text == null || text.length() == 0) {
            textView.setVisibility(GONE);
        } else {
            textView.setText(text);
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