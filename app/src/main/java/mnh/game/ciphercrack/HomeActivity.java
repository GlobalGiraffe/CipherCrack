package mnh.game.ciphercrack;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import mnh.game.ciphercrack.cipher.Cipher;
import mnh.game.ciphercrack.language.Language;
import mnh.game.ciphercrack.services.CrackResults;
import mnh.game.ciphercrack.services.CrackService;
import mnh.game.ciphercrack.transform.Clear;
import mnh.game.ciphercrack.transform.LowerCase;
import mnh.game.ciphercrack.transform.RemoveNonAlphabetic;
import mnh.game.ciphercrack.transform.RemovePadding;
import mnh.game.ciphercrack.transform.RemovePunctuation;
import mnh.game.ciphercrack.transform.Reverse;
import mnh.game.ciphercrack.transform.ReverseWords;
import mnh.game.ciphercrack.transform.SplitByWords;
import mnh.game.ciphercrack.transform.SwapRowsAndCols;
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
    // For consistent logging
    private static final String TAG = "CrackHome";

    // when going to new Activities, on return this is used to get the results back
    private static final int CAMERA_REQUEST_CODE = 1001;
    // used for a background process to pass a result back to this home screen
    private static final String BACKGROUND_CRACK_RESULT = "BGCrackResult";

    // used to save and restore state across application restarts
    private static final String STATE_INPUT_TEXT = "IT";
    private static final String STATE_SPINNER_POSITION = "SP";
    private static final String STATE_CRACK_RESULTS = "CR";

    // keep track of history of edits, in case user wants to Undo
    private static final Stack<String> priorEdits = new Stack<>();

    // queue and executor for processing background Crack requests
    private final BlockingQueue<Runnable> executorQueue = new ArrayBlockingQueue<>(5, true);
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 3, 10, TimeUnit.SECONDS, executorQueue);

    // used to receive messages from simple background Runnable
    private Handler crackResultHandler;

    // local cipher
    private Cipher cipher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.i(TAG, "onCreate");

        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

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

        // restore state - text, cipher name, prior results
        if (savedInstanceState != null) { // null means this is first time called
            String stateInputText = savedInstanceState.getString(STATE_INPUT_TEXT);
            if (stateInputText != null)
                ((EditText) findViewById(R.id.home_entry_text)).setText(stateInputText);
            int spinnerPosition = savedInstanceState.getInt(STATE_SPINNER_POSITION);
            spinner.setSelection(spinnerPosition);
            // recover the list of prior runs
            List<CrackResult> r = savedInstanceState.getParcelableArrayList(STATE_CRACK_RESULTS);
            CrackResults.setResults(r);
        }

        // this allows background Runnable tasks (not services) to send messages to the main UI thread
        crackResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String textMessageFromBackground = msg.getData().getString(BACKGROUND_CRACK_RESULT);
                Toast.makeText(HomeActivity.this, textMessageFromBackground, Toast.LENGTH_LONG).show();
            }
        };
    }

    // used if we allow background Runnable, not services
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(CrackResults.mReceiver, new IntentFilter(CrackService.CRACK_SERVICE_MESSAGE));
        Log.i(TAG, "onStart");
    }

    // used if we allow background Runnable, not services
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(CrackResults.mReceiver);
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    /**
     * Called when the state is to be squirreled away
     * @param outState a bundle that will be passed back to onCreate later
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the state: text in the view, the type of cipher, list of previous runs
        Spinner spinner = findViewById(R.id.home_spinner);
        outState.putString(STATE_INPUT_TEXT, getInputText());
        outState.putInt(STATE_SPINNER_POSITION, spinner.getSelectedItemPosition());
        ArrayList<CrackResult> r = new ArrayList<>(CrackResults.crackResults.size());
        r.addAll(CrackResults.crackResults);
        outState.putParcelableArrayList(STATE_CRACK_RESULTS, r);
        Log.i(TAG, "onSaveInstanceState, type: "+spinner.getSelectedItem().toString());
    }

    /**
     * Called when the state is to be reinstated
     * @param inState a bundle that was created by a call to onSaveInstanceState
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle inState) {
        super.onRestoreInstanceState(inState);
        String stateInputText = inState.getString(STATE_INPUT_TEXT);
        if (stateInputText != null)
            ((EditText) findViewById(R.id.home_entry_text)).setText(stateInputText);
        Spinner spinner = findViewById(R.id.home_spinner);
        int spinnerPosition = inState.getInt(STATE_SPINNER_POSITION);
        spinner.setSelection(spinnerPosition);
        // recover the list of prior runs
        List<CrackResult> r = inState.getParcelableArrayList(STATE_CRACK_RESULTS);
        CrackResults.setResults(r);
        Log.i(TAG, "onRestoreInstanceState: type: "+spinner.getSelectedItem().toString());
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

    private void setInputText(int stringResourceId) {
        EditText entryText = findViewById(R.id.home_entry_text);
        entryText.setText(getString(stringResourceId));
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
            case R.id.action_home_edit_split_words:
                applyEdit(new SplitByWords(), textField);
                return true;
            case R.id.action_home_edit_reverse_words:
                applyEdit(new ReverseWords(), textField);
                return true;
            case R.id.action_home_edit_swap_rows_and_cols:
                applyEdit(new SwapRowsAndCols(), textField);
                return true;
            case R.id.action_home_cipher_challenge_2001_1:
                setInputText(R.string.cipher_challenge_2001_1);
                return true;
            case R.id.action_home_cipher_challenge_2001_2:
                setInputText(R.string.cipher_challenge_2001_2);
                return true;
            case R.id.action_home_cipher_challenge_2001_3:
                setInputText(R.string.cipher_challenge_2001_3);
                return true;
            case R.id.action_home_cipher_challenge_2001_4:
                setInputText(R.string.cipher_challenge_2001_4);
                return true;
            case R.id.action_home_cipher_challenge_2001_5:
                setInputText(R.string.cipher_challenge_2001_5);
                return true;
            case R.id.action_home_cipher_challenge_2002_00:
                setInputText(R.string.cipher_challenge_2002_00);
                return true;
            case R.id.action_home_cipher_challenge_2002_01:
                setInputText(R.string.cipher_challenge_2002_01);
                return true;
            case R.id.action_home_cipher_challenge_2002_02:
                setInputText(R.string.cipher_challenge_2002_02);
                return true;
            case R.id.action_home_cipher_challenge_2002_03:
                setInputText(R.string.cipher_challenge_2002_03);
                return true;
            case R.id.action_home_cipher_challenge_2002_04:
                setInputText(R.string.cipher_challenge_2002_04);
                return true;
            case R.id.action_home_cipher_challenge_2002_05:
                setInputText(R.string.cipher_challenge_2002_05);
                return true;
            case R.id.action_home_cipher_challenge_2002_06:
                setInputText(R.string.cipher_challenge_2002_06);
                return true;
            case R.id.action_home_cipher_challenge_2002_07:
                setInputText(R.string.cipher_challenge_2002_07);
                return true;
            case R.id.action_home_cipher_challenge_2002_08a:
                setInputText(R.string.cipher_challenge_2002_08a);
                return true;
            case R.id.action_home_cipher_challenge_2002_08b:
                setInputText(R.string.cipher_challenge_2002_08b);
                return true;
            case R.id.action_home_cipher_challenge_2002_09:
                setInputText(R.string.cipher_challenge_2002_09);
                return true;
            case R.id.action_home_cipher_challenge_2002_10:
                setInputText(R.string.cipher_challenge_2002_10);
                return true;
            case R.id.action_home_cipher_challenge_2002_11:
                setInputText(R.string.cipher_challenge_2002_11);
                return true;
            case R.id.action_home_cipher_challenge_2002_12:
                setInputText(R.string.cipher_challenge_2002_12);
                return true;
            case R.id.action_home_cipher_challenge_2002_13:
                setInputText(R.string.cipher_challenge_2002_13);
                return true;
            case R.id.action_home_cipher_challenge_2002_14:
                setInputText(R.string.cipher_challenge_2002_14);
                return true;
            case R.id.action_home_cipher_challenge_2002_15:
                setInputText(R.string.cipher_challenge_2002_15);
                return true;
            case R.id.action_home_cipher_challenge_2002_16:
                setInputText(R.string.cipher_challenge_2002_16);
                return true;
            case R.id.action_home_cipher_challenge_2003_00:
                setInputText(R.string.cipher_challenge_2003_0);
                return true;
            case R.id.action_home_cipher_challenge_2003_01:
                setInputText(R.string.cipher_challenge_2003_1);
                return true;
            case R.id.action_home_cipher_challenge_2003_02:
                setInputText(R.string.cipher_challenge_2003_2);
                return true;
            case R.id.action_home_cipher_challenge_2003_03:
                setInputText(R.string.cipher_challenge_2003_3);
                return true;
            case R.id.action_home_cipher_challenge_2003_04:
                setInputText(R.string.cipher_challenge_2003_4);
                return true;
            case R.id.action_home_cipher_challenge_2003_05:
                setInputText(R.string.cipher_challenge_2003_5);
                return true;
            case R.id.action_home_cipher_challenge_2003_06:
                setInputText(R.string.cipher_challenge_2003_6);
                return true;
            case R.id.action_home_cipher_challenge_2003_07:
                setInputText(R.string.cipher_challenge_2003_7);
                return true;
            case R.id.action_home_cipher_challenge_2003_08:
                setInputText(R.string.cipher_challenge_2003_8);
                return true;
            case R.id.action_home_cipher_challenge_2004_00:
                setInputText(R.string.cipher_challenge_2004_0);
                return true;
            case R.id.action_home_cipher_challenge_2004_1A:
                setInputText(R.string.cipher_challenge_2004_1A);
                return true;
            case R.id.action_home_cipher_challenge_2004_1B:
                setInputText(R.string.cipher_challenge_2004_1B);
                return true;
            case R.id.action_home_cipher_challenge_2004_2A:
                setInputText(R.string.cipher_challenge_2004_2A);
                return true;
            case R.id.action_home_cipher_challenge_2004_2B:
                setInputText(R.string.cipher_challenge_2004_2B);
                return true;
            case R.id.action_home_cipher_challenge_2004_3A:
                setInputText(R.string.cipher_challenge_2004_3A);
                return true;
            case R.id.action_home_cipher_challenge_2004_3B:
                setInputText(R.string.cipher_challenge_2004_3B);
                return true;
            case R.id.action_home_cipher_challenge_2004_4A:
                setInputText(R.string.cipher_challenge_2004_4A);
                return true;
            case R.id.action_home_cipher_challenge_2004_4B:
                setInputText(R.string.cipher_challenge_2004_4B);
                return true;
            case R.id.action_home_cipher_challenge_2004_5A:
                setInputText(R.string.cipher_challenge_2004_5A);
                return true;
            case R.id.action_home_cipher_challenge_2004_5B:
                setInputText(R.string.cipher_challenge_2004_5B);
                return true;
            case R.id.action_home_cipher_challenge_2004_6A:
                setInputText(R.string.cipher_challenge_2004_6A);
                return true;
            case R.id.action_home_cipher_challenge_2004_6B:
                setInputText(R.string.cipher_challenge_2004_6B);
                return true;
            case R.id.action_home_cipher_challenge_2004_7A:
                setInputText(R.string.cipher_challenge_2004_7A);
                return true;
            case R.id.action_home_cipher_challenge_2004_7B:
                setInputText(R.string.cipher_challenge_2004_7B);
                return true;
            case R.id.action_home_cipher_challenge_2004_8A:
                setInputText(R.string.cipher_challenge_2004_8A);
                return true;
            case R.id.action_home_cipher_challenge_2004_8B:
                setInputText(R.string.cipher_challenge_2004_8B);
                return true;
            case R.id.action_home_cipher_challenge_2006_1A:
                setInputText(R.string.cipher_challenge_2006_1A);
                return true;
            case R.id.action_home_cipher_challenge_2006_1B:
                setInputText(R.string.cipher_challenge_2006_1B);
                return true;
            case R.id.action_home_cipher_challenge_2006_2A:
                setInputText(R.string.cipher_challenge_2006_2A);
                return true;
            case R.id.action_home_cipher_challenge_2006_2B:
                setInputText(R.string.cipher_challenge_2006_2B);
                return true;
            case R.id.action_home_cipher_challenge_2006_3A:
                setInputText(R.string.cipher_challenge_2006_3A);
                return true;
            case R.id.action_home_cipher_challenge_2006_3B:
                setInputText(R.string.cipher_challenge_2006_3B);
                return true;
            case R.id.action_home_cipher_challenge_2006_4A:
                setInputText(R.string.cipher_challenge_2006_4A);
                return true;
            case R.id.action_home_cipher_challenge_2006_4B:
                setInputText(R.string.cipher_challenge_2006_4B);
                return true;
            case R.id.action_home_cipher_challenge_2006_5A:
                setInputText(R.string.cipher_challenge_2006_5A);
                return true;
            case R.id.action_home_cipher_challenge_2006_5B:
                setInputText(R.string.cipher_challenge_2006_5B);
                return true;
            case R.id.action_home_cipher_challenge_2006_6A:
                setInputText(R.string.cipher_challenge_2006_6A);
                return true;
            case R.id.action_home_cipher_challenge_2006_6B:
                setInputText(R.string.cipher_challenge_2006_6B);
                return true;
            case R.id.action_home_cipher_challenge_2006_7A:
                setInputText(R.string.cipher_challenge_2006_7A);
                return true;
            case R.id.action_home_cipher_challenge_2006_7B:
                setInputText(R.string.cipher_challenge_2006_7B);
                return true;
            case R.id.action_home_cipher_challenge_2006_8A:
                setInputText(R.string.cipher_challenge_2006_8A);
                return true;
            case R.id.action_home_cipher_challenge_2006_8B:
                setInputText(R.string.cipher_challenge_2006_8B);
                return true;
            case R.id.action_home_cipher_challenge_2007_1A:
                setInputText(R.string.cipher_challenge_2007_1A);
                return true;
            case R.id.action_home_cipher_challenge_2007_1B:
                setInputText(R.string.cipher_challenge_2007_1B);
                return true;
            case R.id.action_home_cipher_challenge_2007_2A:
                setInputText(R.string.cipher_challenge_2007_2A);
                return true;
            case R.id.action_home_cipher_challenge_2007_2B:
                setInputText(R.string.cipher_challenge_2007_2B);
                return true;
            case R.id.action_home_cipher_challenge_2007_3A:
                setInputText(R.string.cipher_challenge_2007_3A);
                return true;
            case R.id.action_home_cipher_challenge_2007_3B:
                setInputText(R.string.cipher_challenge_2007_3B);
                return true;
            case R.id.action_home_cipher_challenge_2007_4A:
                setInputText(R.string.cipher_challenge_2007_4A);
                return true;
            case R.id.action_home_cipher_challenge_2007_4B:
                setInputText(R.string.cipher_challenge_2007_4B);
                return true;
            case R.id.action_home_cipher_challenge_2007_5A:
                setInputText(R.string.cipher_challenge_2007_5A);
                return true;
            case R.id.action_home_cipher_challenge_2007_5B:
                setInputText(R.string.cipher_challenge_2007_5B);
                return true;
            case R.id.action_home_cipher_challenge_2007_6A:
                setInputText(R.string.cipher_challenge_2007_6A);
                return true;
            case R.id.action_home_cipher_challenge_2007_6B:
                setInputText(R.string.cipher_challenge_2007_6B);
                return true;
            case R.id.action_home_cipher_challenge_2007_7A:
                setInputText(R.string.cipher_challenge_2007_7A);
                return true;
            case R.id.action_home_cipher_challenge_2007_7B:
                setInputText(R.string.cipher_challenge_2007_7B);
                return true;
            case R.id.action_home_cipher_challenge_2007_8A:
                setInputText(R.string.cipher_challenge_2007_8A);
                return true;
            case R.id.action_home_cipher_challenge_2007_8B:
                setInputText(R.string.cipher_challenge_2007_8B);
                return true;
            case R.id.action_home_cipher_challenge_2009_0A:
                setInputText(R.string.cipher_challenge_2009_0A);
                return true;
            case R.id.action_home_cipher_challenge_2009_0B:
                setInputText(R.string.cipher_challenge_2009_0B);
                return true;
            case R.id.action_home_cipher_challenge_2009_1A:
                setInputText(R.string.cipher_challenge_2009_1A);
                return true;
            case R.id.action_home_cipher_challenge_2009_1B:
                setInputText(R.string.cipher_challenge_2009_1B);
                return true;
            case R.id.action_home_cipher_challenge_2009_2A:
                setInputText(R.string.cipher_challenge_2009_2A);
                return true;
            case R.id.action_home_cipher_challenge_2009_2B:
                setInputText(R.string.cipher_challenge_2009_2B);
                return true;
            case R.id.action_home_cipher_challenge_2009_3A:
                setInputText(R.string.cipher_challenge_2009_3A);
                return true;
            case R.id.action_home_cipher_challenge_2009_3B:
                setInputText(R.string.cipher_challenge_2009_3B);
                return true;
            case R.id.action_home_cipher_challenge_2009_4A:
                setInputText(R.string.cipher_challenge_2009_4A);
                return true;
            case R.id.action_home_cipher_challenge_2009_4B:
                setInputText(R.string.cipher_challenge_2009_4B);
                return true;
            case R.id.action_home_cipher_challenge_2009_5A:
                setInputText(R.string.cipher_challenge_2009_5A);
                return true;
            case R.id.action_home_cipher_challenge_2009_5B:
                setInputText(R.string.cipher_challenge_2009_5B);
                return true;
            case R.id.action_home_cipher_challenge_2009_6A:
                setInputText(R.string.cipher_challenge_2009_6A);
                return true;
            case R.id.action_home_cipher_challenge_2009_6B:
                setInputText(R.string.cipher_challenge_2009_6B);
                return true;
            case R.id.action_home_cipher_challenge_2009_7A:
                setInputText(R.string.cipher_challenge_2009_7A);
                return true;
            case R.id.action_home_cipher_challenge_2009_7B:
                setInputText(R.string.cipher_challenge_2009_7B);
                return true;
            case R.id.action_home_cipher_challenge_2009_8A:
                setInputText(R.string.cipher_challenge_2009_8A);
                return true;
            case R.id.action_home_cipher_challenge_2009_8B:
                setInputText(R.string.cipher_challenge_2009_8B);
                return true;
            case R.id.action_home_cipher_challenge_2010_1A:
                setInputText(R.string.cipher_challenge_2010_1A);
                return true;
            case R.id.action_home_cipher_challenge_2010_1B:
                setInputText(R.string.cipher_challenge_2010_1B);
                return true;
            case R.id.action_home_cipher_challenge_2010_2A:
                setInputText(R.string.cipher_challenge_2010_2A);
                return true;
            case R.id.action_home_cipher_challenge_2010_2B:
                setInputText(R.string.cipher_challenge_2010_2B);
                return true;
            case R.id.action_home_cipher_challenge_2010_3A:
                setInputText(R.string.cipher_challenge_2010_3A);
                return true;
            case R.id.action_home_cipher_challenge_2010_3B:
                setInputText(R.string.cipher_challenge_2010_3B);
                return true;
            case R.id.action_home_cipher_challenge_2010_4A:
                setInputText(R.string.cipher_challenge_2010_4A);
                return true;
            case R.id.action_home_cipher_challenge_2010_4B:
                setInputText(R.string.cipher_challenge_2010_4B);
                return true;
            case R.id.action_home_cipher_challenge_2010_5A:
                setInputText(R.string.cipher_challenge_2010_5A);
                return true;
            case R.id.action_home_cipher_challenge_2010_5B:
                setInputText(R.string.cipher_challenge_2010_5B);
                return true;
            case R.id.action_home_cipher_challenge_2010_6A:
                setInputText(R.string.cipher_challenge_2010_6A);
                return true;
            case R.id.action_home_cipher_challenge_2010_6B:
                setInputText(R.string.cipher_challenge_2010_6B);
                return true;
            case R.id.action_home_cipher_challenge_2010_7A:
                setInputText(R.string.cipher_challenge_2010_7A);
                return true;
            case R.id.action_home_cipher_challenge_2010_7B:
                setInputText(R.string.cipher_challenge_2010_7B);
                return true;
            case R.id.action_home_cipher_challenge_2010_8A:
                setInputText(R.string.cipher_challenge_2010_8A);
                return true;
            case R.id.action_home_cipher_challenge_2010_8B:
                setInputText(R.string.cipher_challenge_2010_8B);
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
        String paddingChars = settings.getString(this, R.string.pref_padding_chars);
        dirs.setPaddingChars(paddingChars);
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
        // always add the alphabet, language and default cribs
        Directives dirs = getDefaultDirectives();

        // now add properties specific to this cipher
        CrackMethod crackMethod = cipher.fetchCrackControls(layoutExtra, dirs);

        // if there even is a layout (some ciphers have no popup), see if it has cribs in it
        if (layoutExtra != null) {
            EditText cribsField = layoutExtra.getRootView().findViewById(R.id.popup_crack_cribs);
            if (cribsField != null) {
                dirs.setCribs(cribsField.getText().toString());
            }
        }
        dirs.setCrackMethod(crackMethod);
        return dirs;
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
                Intent i = ResultActivity.getResultIntent(this, inputText, cipherText, "", cipher, cipherDirectives);
                startActivityForResult(i, ResultActivity.RESULTS_REQUEST_CODE);
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
                Intent i = ResultActivity.getResultIntent(this, inputText, plainText, "", cipher, cipherDirectives);
                startActivityForResult(i, ResultActivity.RESULTS_REQUEST_CODE);
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

            // add cribs
            String cribs = Settings.instance().getString(this, R.string.pref_cribs);
            EditText cribsText = popupLayout.findViewById(R.id.popup_crack_cribs);
            cribsText.setText(cribs);

            // add extra controls if needed
            PopupWindow popup = new PopupWindow(this);
            String alphabet = Settings.instance().getString(this, R.string.pref_alphabet_plain);
            LinearLayout extraLayout = popupLayout.findViewById(R.id.popup_crack_extra_layout);
            boolean controlsNeeded = cipher.addCrackControls(this, extraLayout, alphabet);
            if (controlsNeeded) {
                popup.setContentView(popupLayout);
                popup.setWidth(800);
                popup.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                popup.setFocusable(true);
                Button cancelButton = popupLayout.findViewById(R.id.popup_crack_button_cancel);
                Button crackButton = popupLayout.findViewById(R.id.popup_crack_button_crack);
                crackButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.dismiss();
                        actuallyDoCrackUsingService(extraLayout);
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.dismiss();
                    }
                });
                popup.showAtLocation(popupLayout, Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, 100);
            } else {
                actuallyDoCrackUsingService(null);
            }
        }
    }

    public void actuallyDoCrackUsingRunnable(LinearLayout extraLayout) {
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
            Runnable runCrack = new Runnable() {
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    CrackResult result = cipher.crack(inputText, cipherDirectives, 0);
                    long duration = System.currentTimeMillis() - start;
                    result.setMilliseconds(duration);
                    CrackResults.crackResults.add(result);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString(BACKGROUND_CRACK_RESULT, "Crack "+cipher.getCipherName()+(result.isSuccess()?": successful":": failed"));
                    msg.setData(bundle);
                    crackResultHandler.sendMessage(msg);
                }
            };
            executor.execute(runCrack);
            Toast.makeText(this,"Cracking "+cipher.getCipherName()+" in the background", Toast.LENGTH_SHORT).show();
        }
    }

    private void actuallyDoCrackUsingService(LinearLayout extraLayout) {
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
            CrackService.startActionCrack(this, cipher.getCipherName(), inputText, cipherDirectives);
            Toast.makeText(this,"Cracking "+cipher.getCipherName()+" via a service", Toast.LENGTH_SHORT).show();
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
        // encode / decode called Results screen, may get copy of data back
        if (requestCode == ResultActivity.RESULTS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String resultString = data.getStringExtra("RESULT_TEXT");
                EditText editText = findViewById(R.id.home_entry_text);
                editText.setText(resultString);
            }
        }
        // crack started, result went to the list view, when opened in Result user copied text
        // this sent result to list view, which sends result back to here
        if (requestCode == ComputeResultListActivity.RESULT_LIST_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String resultString = data.getStringExtra("RESULT_TEXT");
                EditText editText = findViewById(R.id.home_entry_text);
                editText.setText(resultString);
            }
        }
    }
}