package mnh.game.ciphercrack.util;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import mnh.game.ciphercrack.R;
import mnh.game.ciphercrack.SettingsActivity;
import mnh.game.ciphercrack.AnalysisActivity;

/**
 * Take action when the buttons at the bottom of the screen are pressed
 */
public class BottomNavigationListener implements BottomNavigationView.OnNavigationItemSelectedListener {

    private AppCompatActivity activity;

    public BottomNavigationListener(AppCompatActivity activity) { this.activity = activity; }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.bottom_analysis:
                Intent i = new Intent(activity, AnalysisActivity.class);
                EditText textField = activity.findViewById(R.id.home_entrytext);
                i.putExtra("TEXT", textField.getText().toString());
                activity.startActivity(i);
                return true;
            case R.id.bottom_cipher:
                break;
            case R.id.bottom_history:
                break;
            case R.id.bottom_info:
                break;
            case R.id.bottom_settings:
                Intent si = new Intent(activity, SettingsActivity.class);
                activity.startActivity(si);
                return true;
        }
        return false;
    }
}
