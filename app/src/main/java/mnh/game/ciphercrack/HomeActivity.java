package mnh.game.ciphercrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Stack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import mnh.game.ciphercrack.transform.Clear;
import mnh.game.ciphercrack.transform.LowerCase;
import mnh.game.ciphercrack.transform.RemovePadding;
import mnh.game.ciphercrack.transform.RemovePunctuation;
import mnh.game.ciphercrack.transform.Reverse;
import mnh.game.ciphercrack.transform.Transform;
import mnh.game.ciphercrack.transform.UpperCase;

/**
 * Start screen for the Cipher Crack application
 */
public class HomeActivity extends AppCompatActivity {

    private static final int CRACK_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 1001;

    private static final Stack<String> priorEdits = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        Spinner spinner = findViewById(R.id.home_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cipher_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_home_edit_undo);
        if (item != null) {
            item.setEnabled(!priorEdits.empty());
        }
        return true;
    }

    /**
     * Called when someone clicks on an Options menu item
     * @param item the menu that was clicked
     * @return true if this method dealt with the call
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        EditText textField = findViewById(R.id.home_entrytext); // used for Edits
        switch (item.getItemId()) {
            case R.id.action_home_crack:
                this.performCrack(null);
                return true;
            case R.id.action_home_settings:
                showSettings(null);
                return true;
            case R.id.action_home_camera:
                textFromCamera(null);
                return true;
            case R.id.action_home_analysis:
                this.showStatistics(null);
                return true;
            case R.id.action_home_edit_undo:
                if (!priorEdits.empty()) {
                    textField.setText(priorEdits.pop());
                    invalidateOptionsMenu();
                }
                return true;
            case R.id.action_home_edit_remove_padding:
                applyEdit(new RemovePadding(), textField);
                return true;
            case R.id.action_home_edit_remove_punctuation:
                applyEdit(new RemovePunctuation(), textField);
                return true;
            case R.id.action_home_edit_uppercase:
                applyEdit(new UpperCase(), textField);
                return true;
            case R.id.action_home_edit_lowercase:
                applyEdit(new LowerCase(), textField);
                return true;
            case R.id.action_home_edit_reverse:
                applyEdit(new Reverse(), textField);
                return true;
            case R.id.action_home_edit_clear:
                applyEdit(new Clear(), textField);
                return true;
            case R.id.action_home_edit_reverse_words:
                Toast.makeText(this, "Reverse Words action not yet possible", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_home_edit_reflect_col_row:
                Toast.makeText(this, "Reflection of columns and rows not yet possible", Toast.LENGTH_LONG).show();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Apply a user-requested transform
     * @param transform the transform the user wants to do
     * @param textField the resulting text after transform
     */
    private void applyEdit(Transform transform, EditText textField) {
        String entryText = textField.getText().toString();
        String editedText = transform.apply(this, entryText);
        textField.setText(editedText);
        priorEdits.push(entryText);
        invalidateOptionsMenu();
    }

    // Crack the text on the screen
    private void performCrack(View view) {
        Intent i = new Intent(this, CrackActivity.class);
        EditText textField = findViewById(R.id.home_entrytext);
        Spinner cipherSpinner = findViewById(R.id.home_spinner);
        i.putExtra("TEXT", textField.getText().toString());
        i.putExtra("CIPHER", cipherSpinner.getSelectedItem().toString());
        startActivityForResult(i, CRACK_REQUEST_CODE);
    }

    // Show stats for the text on the screen
    private void showStatistics(View view) {
        Intent i = new Intent(this, StatisticActivity.class);
        EditText textField = findViewById(R.id.home_entrytext);
        i.putExtra("TEXT", textField.getText().toString());
        startActivity(i);
    }

    // Settings button pressed - show settings
    private void showSettings(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    // Obtain text from a camera - open separate view
    private void textFromCamera(View view) {
        Intent i = new Intent(this, CameraActivity.class);
        startActivityForResult(i, CAMERA_REQUEST_CODE);
    }

    // child activity has finished and perhaps sent result back
    protected void ActivityResult(int requestCode, int resultCode, Intent data) {

        // camera view was opened - what did users take picture of?
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String capturedString = data.getStringExtra("CAMERA_TEXT");
                EditText editText = findViewById(R.id.home_entrytext);
                editText.setText(capturedString);
            }
        }
        // if Crack view was opened - users may have adjusted the text
        if (requestCode == CRACK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String capturedString = data.getStringExtra("INPUT_TEXT");
                EditText editText = findViewById(R.id.home_entrytext);
                editText.setText(capturedString);
            }
        }
    }
}