package mnh.game.ciphercrack.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import mnh.game.ciphercrack.cipher.Cipher;
import mnh.game.ciphercrack.util.CrackResult;
import mnh.game.ciphercrack.util.Directives;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * This one is used to perform a background crack operation
 */
public class CrackService extends IntentService {
    // used for logging consistently
    private static final String TAG = "CrackService";

    // used to pass messages back to main app
    public static final String CRACK_SERVICE_MESSAGE = "CrackServiceMsg";
    public static final String MSG_TYPE = "MsgType"; // one of progress or complete
    public static final String CRACK_ID = "CrackId";

    public static final String MSG_TYPE_PROGRESS = "Progress"; // will have PERCENT_COMPLETE int
    public static final String MSG_TYPE_COMPLETE = "Complete"; // will have CRACK_RESULT
    public static final String PERCENT_COMPLETE = "PercentComplete"; // key for an int
    public static final String CRACK_RESULT = "CrackResult"; // key for a CrackResult

    // IntentService can perform these actions
    private static final String ACTION_CRACK = "mnh.game.ciphercrack.services.action.CRACK";

    // Parameters for the Crack Action
    private static final String EXTRA_CIPHER_NAME = "mnh.game.ciphercrack.services.extra.CIPHER_NAME";
    private static final String EXTRA_INPUT_TEXT = "mnh.game.ciphercrack.services.extra.INPUT_TEXT";
    private static final String EXTRA_DIRECTIVES = "mnh.game.ciphercrack.services.extra.DIRECTIVES";

    public CrackService() {
        super("CrackService");
    }

    /**
     * Starts this service to perform action Crack with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionCrack(Context context, String cipherName, String inputText, Directives dirs) {
        Intent intent = new Intent(context, CrackService.class);
        intent.setAction(ACTION_CRACK);
        intent.putExtra(EXTRA_CIPHER_NAME, cipherName);
        intent.putExtra(EXTRA_INPUT_TEXT, inputText);
        intent.putExtra(EXTRA_DIRECTIVES, dirs);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CRACK.equals(action)) {
                final String cipherName = intent.getStringExtra(EXTRA_CIPHER_NAME);
                final String inputText = intent.getStringExtra(EXTRA_INPUT_TEXT);
                final Directives dirs = intent.getParcelableExtra(EXTRA_DIRECTIVES);
                final Cipher cipher = Cipher.instanceOf(cipherName, null);
                if (cipher != null && dirs != null) {
                    cipher.canParametersBeSet(dirs);
                }
                handleActionCrack(cipher, inputText, dirs);
            }
        }
    }

    /**
     * Handle Crack action in the provided background thread with the provided parameters.
     */
    private void handleActionCrack(Cipher cipher, String inputText, Directives dirs) {
        Log.i(TAG, "handleActionCrack is starting");
        long start = System.currentTimeMillis();
        CrackResult cr = new CrackResult(dirs.getCrackMethod(), cipher, inputText, "Not yet complete");
        cr.setPercentComplete(1);
        CrackResults.crackResults.addFirst(cr);

        // get on with the crack action
        CrackResult result = cipher.crack(inputText, dirs, cr.getId());

        // The result now goes into the array of results at cr
        CrackResults.updatePercentageDirectly(cr.getId(), 100);
        long duration = System.currentTimeMillis() - start;
        cr.setMilliseconds(duration);
        cr.setFields(result);
        Log.i(TAG, "handleActionCrack is complete");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "handleActionCrack is created");
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG, "handleActionCrack is started, id="+startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "handleActionCrack is destroyed");
    }
}
