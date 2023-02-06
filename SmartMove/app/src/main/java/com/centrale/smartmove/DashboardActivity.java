package com.centrale.smartmove;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements DisplayHandler {

    DisplayManager displayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        displayManager = new DisplayManager(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                PackageManager.PERMISSION_GRANTED);
    }

    public void onBackPressed() {
        // super.onBackPressed();
        Toast.makeText(this,"Circulez, y'a rien à voir !",Toast.LENGTH_LONG).show();
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibrationEffect1 = VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE);
        vibrator.cancel();
        vibrator.vibrate(vibrationEffect1);
    }


    @Override
    public Map<Integer, View> getEditableObjects() {
        Map<Integer,View> list = new HashMap<>();
        list.put(R.id.impactText, findViewById(R.id.impactText));
        return list;
    }

    public void clickNew(View v){
        setContentView(R.layout.activity_carbon_equivalent);
    }
}
