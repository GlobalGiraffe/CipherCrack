package mnh.game.ciphercrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import mnh.game.ciphercrack.cipher.Affine;
import mnh.game.ciphercrack.cipher.Atbash;
import mnh.game.ciphercrack.cipher.Beaufort;
import mnh.game.ciphercrack.cipher.Caesar;
import mnh.game.ciphercrack.cipher.Cipher;
import mnh.game.ciphercrack.cipher.KeywordSubstitution;
import mnh.game.ciphercrack.cipher.Permutation;
import mnh.game.ciphercrack.cipher.Railfence;
import mnh.game.ciphercrack.cipher.Rot13;
import mnh.game.ciphercrack.cipher.Vigenere;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.util.CrackMethod;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;
import mnh.game.ciphercrack.util.Settings;

public class CrackActivity extends AppCompatActivity {

    private Cipher cipher = null;
    private String cipherName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crack);

        // put the text on the screen
        String inputText = getIntent().getStringExtra("TEXT");
        TextView textView = findViewById(R.id.crack_inputtext);
        textView.setText(inputText);

        Toolbar toolbar = findViewById(R.id.crack_toolbar);
        cipherName = getIntent().getStringExtra("CIPHER");
        toolbar.setTitle(cipherName);
        setSupportActionBar(toolbar);

        String alphabet = Settings.instance().getString(this, R.string.pref_alphabet_plain);

        // where the extra controls go
        LinearLayout layoutExtra = findViewById(R.id.crack_extra_controls);

        switch (cipherName) {
            case "Caesar":
                cipher = new Caesar(this);
                break;
            case "ROT13":
                cipher = new Rot13(this);
                break;
            case "Affine":
                cipher = new Affine(this);
                break;
            case "Vigenere":
                cipher = new Vigenere(this);
                break;
            case "Keyword Substitution":
                cipher = new KeywordSubstitution(this);
                break;
            case "Atbash":
                cipher = new Atbash(this);
                break;
            case "Beaufort":
                cipher = new Beaufort(this);
                break;
            case "Railfence":
                cipher = new Railfence(this);
                break;
            case "Permutation":
                cipher = new Permutation(this);
                break;
            case "Hill":
                // TODO cipher = new Hill(this);
                break;
            case "Bifid":
                // TODO cipher = new Bifid(this);
                break;
            case "Playfair":
                // TODO cipher = new Playfair(this);
                break;
        }
        cipher.layoutExtraControls(this, layoutExtra, alphabet);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crack_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_crack_settings:
                showSettings(null);
                return true;
           case R.id.action_crack_encode:
                performEncode(null);
                return true;
            case R.id.action_crack_decode:
                performDecode(null);
                return true;
            case R.id.action_crack_crack:
                performCrack(null);
                return true;
            case R.id.action_crack_info:
                showCipherDescription(null);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    // Settings button pressed - show settings
    private void showSettings(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    /**
     * User wants to see what the cipher is all about, show a dialog box describing it
     * @param view the pressed button
     */
    private void showCipherDescription(View view) {
        if (cipher == null) {
            Toast.makeText(this, "Cipher "+cipherName+" has no description yet", Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Cipher: "+cipherName);
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
     * Fetch the text from the screen for encoding, decoding or cracking
     * @return the text from the screen
     */
    private String getInputText() {
        TextView textView = findViewById(R.id.crack_inputtext);
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
    private Directives setCipherDirectives() {
        // always add the alphabet, language and cribs
        Directives dirs = getDefaultDirectives();

        // now add properties specific to this cipher
        LinearLayout layoutExtra = findViewById(R.id.crack_extra_controls);
        cipher.fetchExtraControls(layoutExtra, dirs);
        return dirs;
    }

    private Intent getResultIntent(final String text, final String resultText,
                                   final String explainText, final Cipher cipher) {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("TEXT", text);
        i.putExtra("RESULT", resultText);
        i.putExtra("CIPHER", cipherName);
        i.putExtra("EXPLAIN", explainText);
        i.putExtra("CIPHER_DESCRIPTION", cipher.getInstanceDescription());
        return i;
    }

    /**
     * User wants to encode the text, so get the parameters for the cipher from the screen and
     * kick off the encoding, show the results at the end
     * @param view the button being clicked
     */
    private void performEncode(View view) {
        // if we have implemented the cipher code...
        if (cipher == null) {
            Toast.makeText(this, "Not yet able to encode the " + cipherName + " cipher.", Toast.LENGTH_LONG).show();
        } else {
            // depending on the cipher type we set up required properties from the screen
            // this also has added alphabet, language and cribs from Settings
            Directives cipherDirectives = setCipherDirectives();
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
                    cipherText = "Unable to encode text using " + cipherName + " cipher";
                }
                Intent i = getResultIntent(inputText, cipherText, "", cipher);
                startActivity(i);
            }
        }
    }

    /**
     * User wants to decode the text, so get the directives for the cipher from the screen and
     * kick off the decoding, show the results at the end
     * @param view the button being clicked
     */
    private void performDecode(View view) {
        // if we have implemented the cipher code...
        if (cipher == null) {
            Toast.makeText(this, "Not yet able to decode the " + cipherName + " cipher.", Toast.LENGTH_LONG).show();
        } else {
            // depending on the cipher type we set up required properties from the screen
            // this also sets default ones from Settings: alphabet, language, cribs
            Directives cipherDirectives = setCipherDirectives();
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
                    plainText = "Unable to decode this text using " + cipherName + " cipher";
                }
                Intent i = getResultIntent(inputText, plainText, "", cipher);
                startActivity(i);
            }
        }
    }

    /**
     * User wants to crack the text, with no key or details of how the cipher is configured.
     * Get the directives for the cipher from the screen and kick off the crack process
     * suitable for this cipher and show the results at the end
     * @param view the button being clicked
     */
    private void performCrack(View view) {
        // if we have implemented the cipher code...
        if (cipher == null) {
            Toast.makeText(this, "Not yet able to crack the " + cipherName + " cipher.", Toast.LENGTH_LONG).show();
        } else {
            // depending on the cipher type we set up required properties from the screen
            // this also sets default ones from Settings: alphabet, language, cribs
            Directives cipherDirectives = setCipherDirectives();
            CrackMethod crackMethod = cipher.getCrackMethod(findViewById(R.id.crack_extra_controls));
            cipherDirectives.setCrackMethod(crackMethod);

            // check the parameters are acceptable for decoding
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
                String explain = result.getExplain()+"Completed in "+duration+"ms\n";
                String resultText = result.isSuccess()
                        ? result.getPlainText()
                        : ("Unable to crack this text using " + cipherName
                            + " cipher and cribs [" + cipherDirectives.getCribs() + "].");
                Intent i = getResultIntent(inputText, resultText, explain, cipher);
                startActivity(i);
            }
        }
    }

    /**
     * If user goes back to prior screen, send the text back -- it may have been changed
     */
    @Override
    public void onBackPressed() {
        // when leaving this screen pass the possibly-edited text back to original screen
        EditText inputTextView = findViewById(R.id.crack_inputtext);
        String inputText = inputTextView.getText().toString();
        Intent intent = new Intent();
        intent.putExtra("INPUT_TEXT", inputText);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
