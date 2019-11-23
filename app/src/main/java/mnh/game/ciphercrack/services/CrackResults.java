package mnh.game.ciphercrack.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import mnh.game.ciphercrack.util.CrackResult;

/**
 * Group of cipher results, success, failure, in-progress
 */
public class CrackResults {

    private static final String TAG = "CrackResults";

    // collection of completed crack results
    public static final LinkedList<CrackResult> crackResults = new LinkedList<>();

    // handle messages coming back from the Crack Service
    public static final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String typeOfMessage = intent.getStringExtra(CrackService.MSG_TYPE);
            switch (typeOfMessage) {
                case CrackService.MSG_TYPE_PROGRESS:
                    int crackId = intent.getIntExtra(CrackService.CRACK_ID, -1);
                    CrackResult crackResult = findCrackResult(crackId);
                    if (crackResult != null) {
                        int percentComplete = intent.getIntExtra(CrackService.PERCENT_COMPLETE, 0);
                        Log.i(TAG, "Received message from service: " + typeOfMessage + ", for id=" + crackId + ", percent=" + percentComplete);
                        crackResult.setPercentComplete(percentComplete);
                    //} else {
                    //    Toast.makeText(HomeActivity.this, "Crack Service sent message for unknown id=" + crackId, Toast.LENGTH_LONG).show();
                    }
                    break;
                case CrackService.MSG_TYPE_COMPLETE:
                    CrackResult finalResult = intent.getParcelableExtra(CrackService.CRACK_RESULT);
                    Log.i(TAG, "Received message from service: " + typeOfMessage + ", for id=" + finalResult.getId() + ", success=" + finalResult.isSuccess());
                    //Toast.makeText(HomeActivity.this, "Crack completed: " + (finalResult.isSuccess() ? "success" : "false"), Toast.LENGTH_LONG).show();
                    replaceCrackResult(finalResult);
                    break;
            }
        }
    };


    public static void updatePercentageDirectly(int crackId, int percentage) {
        CrackResult result = findCrackResult(crackId);
        if (result != null) {
            result.setPercentComplete(percentage);
        }
    }

    // locate the id in the list of in-flight and completed items
    private static CrackResult findCrackResult(int id) {
        for (CrackResult result : crackResults) {
            if (result.getId() == id)
                return result;
        }
        return null;
    }

    // replace the result in the list with the actual result
    private static void replaceCrackResult(CrackResult result) {
        int pos = crackResults.indexOf(result);
        if (pos >= 0)
            crackResults.set(pos, result);
    }

    // locate the item to be removed, and remove it
    public static void removeCrackResult(CrackResult crackResult) {
        crackResults.remove(crackResult);
    }

    // clear the list and replace with what has been provided (restoring state)
    public static void setResults(List<CrackResult> results) {
        crackResults.clear();
        crackResults.addAll(results);
    }
}