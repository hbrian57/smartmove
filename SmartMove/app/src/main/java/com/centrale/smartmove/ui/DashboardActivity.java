package com.centrale.smartmove.ui;

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

import com.centrale.smartmove.R;
import com.centrale.smartmove.tracker.GeolocpvtUserTracker;
import com.centrale.smartmove.tracker.TrackerClock;
import com.centrale.smartmove.models.User;
import com.centrale.smartmove.tracker.AndroidUserTracker;

public class DashboardActivity extends AppCompatActivity  {
    User user;
    Double displayedCarbonFootprint;

    TrackerClock tracker;

    public boolean GeolocSwitch = true; //True makes the app use GeolocPVT, false makes it use Android's default library
    AndroidUserTracker trackerAndroid;
    GeolocpvtUserTracker trackerGeoloc;


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
        trackerAndroid = new AndroidUserTracker(this);
        trackerGeoloc = new GeolocpvtUserTracker(this);
        if (GeolocSwitch) {
            tracker = new TrackerClock(trackerGeoloc);
        } else {
            tracker = new TrackerClock(trackerAndroid);
        }

    }

    /**
     * Method to display a toast when the back button is pressed.
     */
    public void onBackPressed() {
        // super.onBackPressed();
        Toast.makeText(this,getString(R.string.onback_defaultmessage),Toast.LENGTH_LONG).show();
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibrationEffect1 = VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE);
        vibrator.cancel();
        vibrator.vibrate(vibrationEffect1);
    }

    /**
     * Method to display all the ongoing challenges.
     */
    private void displayChallenges() {
        RecyclerView recyclerView = findViewById(R.id.recyclerChallenges);
        RecyclerChallengeAdapter challengeAdapter = new RecyclerChallengeAdapter(this,user.getOnGoingChallenge());
        recyclerView.setAdapter(challengeAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Method to sned the user to the view of the carbon equivalent.
     * @param v the main view
     */
    public void clickOpenCarbonEquivalentPage(View v){
        Intent intent = new Intent(this, ActivityCarbonEquivalent.class);
        intent.putExtra(getString(R.string.displayed_carbon_footprint), displayedCarbonFootprint);
        startActivity(intent);
    }

    /**
     * Methode to display the carbon footprint.
     */
    public void displayCarbonFootprint(){
       // Double carbonFootprint = user.calculateCurrentWeekCarbonFootprint();
        displayedCarbonFootprint = 3.0;
        TextView textView = findViewById(R.id.impactTextDashboard);
        textView.setText(displayedCarbonFootprint + getString(R.string.display_message_after_CO2footprint));
    }

    public void onTileClicked(View v){
        Intent intent = new Intent(this, ChallengeDescriptionActivity.class);
        startActivity(intent);
    }

}
