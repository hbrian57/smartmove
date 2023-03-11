package com.centrale.smartmove.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.centrale.smartmove.R;
import com.centrale.smartmove.debugus4.DebugUS4;

public class MainActivity extends AppCompatActivity {

    /**
     * Method that displays the application's menu view or the CGU view if it has never been accepted.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean DEBUG_SCREEN = true;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.smartmove_intext),Context.MODE_PRIVATE);
        int CGU_accepted = sharedPref.getInt(getString(R.string.acceptedCGUtext), 0);
        if (CGU_accepted == 0) {
            Intent intent = new Intent(this, CGUActivity.class);
            startActivity(intent);
        } else {
            if (DEBUG_SCREEN) {
                Intent intent = new Intent(this, DebugUS4.class);
                startActivity(intent);
                return;
            }
            Intent intent = new Intent(this,DashboardActivity.class);
            startActivity(intent);
        }
    }

}