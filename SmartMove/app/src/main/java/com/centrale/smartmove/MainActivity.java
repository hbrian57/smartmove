package com.centrale.smartmove;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    /**
     * Method that displays the application's menu view or the CGU view if it has never been accepted.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.smartmove_intext),Context.MODE_PRIVATE);
        int val = sharedPref.getInt(getString(R.string.acceptedCGUtext), 0);
        if (val == 0) {
            Intent intent = new Intent(this, CGUActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this,DashboardActivity.class);
            startActivity(intent);
        }
    }


}