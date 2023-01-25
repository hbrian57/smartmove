package com.centrale.smartmove;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class BootstrapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferences sharedPref = getSharedPreferences("SMARTMOVE",Context.MODE_PRIVATE);
        int val = sharedPref.getInt("acceptedGCU", 0);
        if (val == 0) {
            Intent intent = new Intent(this,ScrollingActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this,DashboardActivity.class);
            startActivity(intent);
        }
    }


}