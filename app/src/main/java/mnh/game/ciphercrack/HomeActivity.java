package mnh.game.ciphercrack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;
import java.util.Stack;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.drawerlayout.widget.DrawerLayout;
import mnh.game.ciphercrack.cipher.Cipher;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.transform.Clear;
import mnh.game.ciphercrack.transform.LowerCase;
import mnh.game.ciphercrack.transform.RemoveNonAlphabetic;
import mnh.game.ciphercrack.transform.RemovePadding;
import mnh.game.ciphercrack.transform.RemovePunctuation;
import mnh.game.ciphercrack.transform.Reverse;
import mnh.game.ciphercrack.transform.Transform;
import mnh.game.ciphercrack.transform.UpperCase;
import mnh.game.ciphercrack.util.BottomNavigationListener;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.Settings;

/**
 * Start screen for the Cipher Crack application
 */
public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // when going to new Activities, on return this is used to get the results back
    private static final int CAMERA_REQUEST_CODE = 1001;

    // keep track of history of edits, in case user wants to Undo
    private static final Stack<String> priorEdits = new Stack<>();

    private Cipher cipher;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout drawerLayout;
    private String[] drawerItemTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        // the bottom navigation bar
        BottomNavigationView bottom = findViewById(R.id.home_bottom_navigation);
        //bottom.setSelectedItemId(R.id.bottom_cipher);
        bottom.setOnNavigationItemSelectedListener(new BottomNavigationListener(this, R.id.home_entry_text));

        // the drop-down list of ciphers
        Spinner spinner = findViewById(R.id.home_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cipher_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    /**
     * Called when the cipher spinner has nothing selected - no cipher chosen
     * @param parent the spinner
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        toolbar.setTitle(R.string.app_name);
        cipher = null;
    }

    /**
     * This is called when the spinner for the cipher type is clicked
     * @param parent the spinner
     * @param view the item in the spinner selected
     * @param position the position in the spinner we selected
     * @param id the id of the view item
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // we only care about the spinner for cipher name
        if (parent.getId() == R.id.home_spinner) {
            Spinner cipherSpinner = (Spinner) parent;
            String cipherName = cipherSpinner.getSelectedItem().toString();
            Toolbar toolbar = findViewById(R.id.home_toolbar);
            toolbar.setTitle(cipherName);

            // where the extra controls go
            LinearLayout layoutExtra = findViewById(R.id.home_extra_controls);
            layoutExtra.removeAllViews();
            cipher = Cipher.instanceOf(cipherName, this);
            if (cipher != null) {
                String alphabet = Settings.instance().getString(this, R.string.pref_alphabet_plain);
                cipher.addExtraControls(this, layoutExtra, alphabet);
            }
            LinearLayout layout = findViewById(R.id.home_main_layout);
            layout.invalidate(); // redraw
        }
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
        EditText textField = findViewById(R.id.home_entry_text); // used for Edits
        switch (item.getItemId()) {
            case R.id.action_home_info:
                showCipherDescription(null);
                return true;
            case R.id.action_home_camera:
                textFromCamera(null);
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
            case R.id.action_home_edit_keep_alphabetic:
                applyEdit(new RemoveNonAlphabetic(), textField);
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
     * User wants to see what the cipher is all about, show a dialog box describing it
     * @param view the pressed button
     */
    private void showCipherDescription(View view) {
        if (cipher == null) {
            Toast.makeText(this, "Cipher has no description yet", Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Cipher: "+cipher.getCipherName());
            alert.setMessage("Description");

            // Create TextView to show the main description
            final TextView viewWithText = new TextView(this);
            String cipherDescription = cipher.getCipherDescription();
            viewWithText.setText(cipherDescription);
            viewWithText.setPadding(10,10,10,10);
            alert.setView(viewWithText);
            alert.setPositiveButton(getString(R.string.close), null);
            alert.show();
        }
    }

    /**
     * Apply a user-requested transform, e.g. remove padding or convert to upper case
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

    // Obtain text from a camera - open separate view
    private void textFromCamera(View view) {
        Intent i = new Intent(this, CameraActivity.class);
        startActivityForResult(i, CAMERA_REQUEST_CODE);
    }

    /**
     * Fetch the text from the screen for encoding, decoding or cracking
     * @return the text from the screen
     */
    private String getInputText() {
        TextView textView = findViewById(R.id.home_entry_text);
        return textView.getText().toString();
    }

    /**
     * Gather the directives needed by any action: Alphabet and the Text to be worked on
     * @return the basic set of directives
     */
    private Directives getDefaultDirectives() {
        Directives dirs = new Directives();
        Settings settings = Settings.instance();
        String alphabet = settings.getString(this, R.string.pref_alphabet_plain);
        dirs.setAlphabet(alphabet);
        String languageName = settings.getString(this, R.string.pref_language);
        dirs.setLanguage(Language.instanceOf(languageName));
        String cribs = settings.getString(this, R.string.pref_cribs);
        dirs.setCribs(cribs);
        return dirs;
    }

    /**
     * We're about to encode or decode, so read items from the screen specific for this cipher
     * @return the default directives and the specific directives for this cipher from the screen
     */
    private Directives setEncodeDecodeDirectives(LinearLayout layoutExtra) {
        // always add the alphabet, language and cribs
        Directives dirs = getDefaultDirectives();

        // now add properties specific to this cipher
        cipher.fetchExtraControls(layoutExtra, dirs);
        return dirs;
    }

    /**
     * We're about to crack, so read items from the crack screen specific for this cipher
     * @return the default directives and the specific crack directives for this cipher from the screen
     */
    private Directives setCrackDirectives(LinearLayout layoutExtra) {
        // always add the alphabet, language and cribs
        Directives dirs = getDefaultDirectives();

        // now add properties specific to this cipher
        CrackMethod crackMethod = cipher.fetchCrackControls(layoutExtra, dirs);
        dirs.setCrackMethod(crackMethod);
        return dirs;
    }

    /**
     * Get the intent for opening the Result screen
     * @param text the text to as original
     * @param resultText what to show as the encoded/decoded text
     * @param explainText for crack only, the explain text as to how we cracked
     * @param cipher the name of the cipher we used
     * @return the Intent that can launch the result screen suitably
     */
    private Intent getResultIntent(final String text, final String resultText,
                                   final String explainText, final Cipher cipher,
                                   final Directives dirs) {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra(ResultActivity.EXTRA_TEXT, text);
        i.putExtra(ResultActivity.EXTRA_RESULT, resultText);
        i.putExtra(ResultActivity.EXTRA_EXPLAIN, explainText);
        i.putExtra(ResultActivity.EXTRA_CIPHER, cipher.getCipherName());
        i.putExtra(ResultActivity.EXTRA_DIRECTIVES, dirs);
        return i;
    }

    /**
     * User wants to encode the text, so get the parameters for the cipher from the screen and
     * kick off the encoding, show the results at the end
     * @param view the button being clicked
     */
    public void performEncode(View view) {
        // if we have implemented the cipher code...
        if (cipher == null) {
            Toast.makeText(this, "Not yet able to encode this cipher.", Toast.LENGTH_LONG).show();
        } else {
            // depending on the cipher type we set up required properties from the screen
            // this also has added alphabet, language and cribs from Settings
            LinearLayout layoutExtra = findViewById(R.id.home_extra_controls);
            Directives cipherDirectives = setEncodeDecodeDirectives(layoutExtra);
            cipherDirectives.setCrackMethod(CrackMethod.NONE);

            // check the parameters are acceptable for encoding
            String reason = cipher.canParametersBeSet(cipherDirectives);
            if (reason != null) {
                Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
            } else {
                // do the encoding in the correct cipher, obtain text result
                String inputText = getInputText();
                String cipherText = cipher.encode(inputText, cipherDirectives);
                if (cipherText == null) {
                    cipherText = "Unable to encode text using " + cipher.getCipherName() + " cipher";
                }
                Intent i = getResultIntent(inputText, cipherText, "", cipher, cipherDirectives);
                startActivity(i);
            }
        }
    }

    /**
     * User wants to decode the text, so get the directives for the cipher from the screen and
     * kick off the decoding, show the results at the end
     * @param view the button being clicked
     */
    public void performDecode(View view) {
        // if we have implemented the cipher code...
        if (cipher == null) {
            Toast.makeText(this, "Not yet able to decode this cipher.", Toast.LENGTH_LONG).show();
        } else {
            // depending on the cipher type we set up required properties from the screen
            // this also sets default ones from Settings: alphabet, language, cribs
            LinearLayout layoutExtra = findViewById(R.id.home_extra_controls);
            Directives cipherDirectives = setEncodeDecodeDirectives(layoutExtra);
            cipherDirectives.setCrackMethod(CrackMethod.NONE);

            // check the parameters are acceptable for decoding
            String reason = cipher.canParametersBeSet(cipherDirectives);
            if (reason != null) {
                Toast.makeText(this, reason, Toast.LENGTH_LONG).show();

            } else {
                // do the decoding in the correct cipher, obtain text result
                String inputText = getInputText();
                String plainText = cipher.decode(inputText, cipherDirectives);
                if (plainText == null) {
                    plainText = "Unable to decode this text using " + cipher.getCipherName() + " cipher";
                }
                Intent i = getResultIntent(inputText, plainText, "", cipher, cipherDirectives);
                startActivity(i);
            }
        }
    }

    /**
     * User wants to crack the cipher of the text, show a dialog box asking for any crack options
     * for this cipher, and then do the cracking
     * @param view the pressed button
     */
    public void performCrack(View view) {
        if (cipher == null) {
            Toast.makeText(this, "Cipher cannot be cracked", Toast.LENGTH_LONG).show();
        } else {
            LinearLayout viewGroup = findViewById(R.id.popup_crack_main_layout);
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupLayout = inflater.inflate(R.layout.popup_crack, viewGroup);
            LinearLayout extraLayout = popupLayout.findViewById(R.id.popup_crack_extra_layout);

            PopupWindow popup = new PopupWindow(this);
            String alphabet = Settings.instance().getString(this, R.string.pref_alphabet_plain);
            boolean controlsAdded = cipher.addCrackControls(this, extraLayout, alphabet);
            if (controlsAdded) {
                popup.setContentView(popupLayout);
                popup.setWidth(900);
                popup.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                popup.setFocusable(true);
                Button cancelButton = popupLayout.findViewById(R.id.popup_crack_button_cancel);
                Button crackButton = popupLayout.findViewById(R.id.popup_crack_button_crack);
                crackButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.dismiss();
                        actuallyDoCrack(extraLayout);
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.dismiss();
                    }
                });
                popup.showAtLocation(popupLayout, Gravity.CENTER, 0, 0);
            } else {
                actuallyDoCrack(null);
            }
        }
    }

    public void actuallyDoCrack(LinearLayout extraLayout) {
        // depending on the cipher type we set up required properties from the screen
        // this also sets default ones from Settings: alphabet, language, cribs
        Directives cipherDirectives = setCrackDirectives(extraLayout);

        // check the parameters are acceptable for cracking
        String reason = cipher.canParametersBeSet(cipherDirectives);
        if (reason != null) {
            Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
        } else {

            // do the cracking now and see send result to the Results view
            String inputText = getInputText();
            long start = System.currentTimeMillis();
            CrackResult result = cipher.crack(inputText, cipherDirectives);
            long duration = System.currentTimeMillis() - start;
            result.setMilliseconds(duration);
            String explain = result.getExplain()+"Completed in "
                    + String.format(Locale.getDefault(),"%3.2f",duration/1000.0)
                    + "secs\n";
            String resultText = result.isSuccess()
                    ? result.getPlainText()
                    : ("Unable to crack this text using " + cipher.getCipherName()
                    + " cipher and cribs [" + cipherDirectives.getCribs() + "].");
            Intent i = getResultIntent(inputText, resultText, explain, cipher, cipherDirectives);
            startActivity(i);
        }
    }

    // child activity has finished and perhaps sent result back
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // camera view was opened - what did users take picture of?
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String capturedString = data.getStringExtra("CAMERA_TEXT");
                EditText editText = findViewById(R.id.home_entry_text);
                editText.setText(capturedString);
            }
        }
    }
}