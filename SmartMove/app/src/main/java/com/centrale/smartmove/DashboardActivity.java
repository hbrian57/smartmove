package com.centrale.smartmove;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardActivity extends AppCompatActivity  {
    User user;
    Double carbonFootprintDisplayed;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                PackageManager.PERMISSION_GRANTED);
        ImageView navToHere = findViewById(R.id.imageViewNavToCarbonEquivalent);
        navToHere.setVisibility(View.VISIBLE);


        user = new User();
        //add 3 challenges for testing
        for (int i = 0; i < 3; i++) {
            user.getNewChallenge();
        }
        displayCarbonFootprint();
        displayChallenges();
    }

    public void onBackPressed() {
        // super.onBackPressed();
        Toast.makeText(this,"Circulez, y'a rien à voir !",Toast.LENGTH_LONG).show();
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibrationEffect1 = VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE);
        vibrator.cancel();
        vibrator.vibrate(vibrationEffect1);
    }

    private void displayChallenges() {
        RecyclerView recyclerView = findViewById(R.id.recyclerChallenges);
        RecyclerChallengeAdapter challengeAdapter = new RecyclerChallengeAdapter(this,user.getOnGoingChallenge());
        recyclerView.setAdapter(challengeAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void clickOpenCarbonEquivalentPage(View v){
        Intent intent = new Intent(this,ActivityCarbonEquivalent.class);
        intent.putExtra("carbonFootprintDisplayed", carbonFootprintDisplayed);
        startActivity(intent);;
    }

    public void displayCarbonFootprint(){
       // Double carbonFootprint = user.calculateCurrentWeekCarbonFootprint();
        carbonFootprintDisplayed = 3.0;
        TextView textView = findViewById(R.id.impactTextDashboard);
        textView.setText(carbonFootprintDisplayed + " tonnes de CO2eq");
    }
}
