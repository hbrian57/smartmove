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

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity  {

    private User user; // L'utilisateur de l'application
    private Double displayedCarbonFootprint; // L'empreinte carbone affichée
    ImageView navToEquivalent; // L'image qui permet de naviguer vers la page d'empreinte carbone

    //Data du fichier config
    private boolean geolocSwitch;
    private Integer positionUpdateDelay;
    private Integer numberOfPositionsInSegments;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readConfigFile();
        setContentView(R.layout.activity_dashboard);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                PackageManager.PERMISSION_GRANTED);
        user = new User();
        //add 3 challenges for testing
        for (int i = 0; i < 3; i++) {
            user.getNewChallenge();
        }
        navToEquivalent = findViewById(R.id.imageViewNavToCarbonEquivalent);
        navToEquivalent.setOnClickListener(v -> {
            clickOpenCarbonEquivalentPage();
        });
        navToEquivalent.setVisibility(View.VISIBLE);
        displayCarbonFootprint();
        displayChallenges();
        AndroidUserTracker trackerAndroid = new AndroidUserTracker(this);
        GeolocpvtUserTracker trackerGeoloc = new GeolocpvtUserTracker(this);
        // La clock qui gère le tracker
        TrackerClock tracker;
        if (geolocSwitch) {
            tracker = new TrackerClock(trackerGeoloc, positionUpdateDelay);
        } else {
            tracker = new TrackerClock(trackerAndroid, positionUpdateDelay);
        }

    }

    private void readConfigFile() {
        //Open the config file from raw ressource
        InputStream inputStream = getResources().openRawResource(R.raw.config);
        Yaml yaml = new Yaml();
        //Load the file into a map
        Map<String,Object> configResult = (Map) yaml.load(inputStream);
        for (Map.Entry<String,Object> entry : configResult.entrySet()) {
            //Get the key and the value
            String key = entry.getKey();
            Object value = entry.getValue();
            switch(key) {
                case "geoloc":
                    geolocSwitch = (boolean) value;
                    break;
                case "positionUpdateClockDelay" :
                    positionUpdateDelay = (Integer) value;
                    break;
                case "numberOfPositionsInSegment":
                    numberOfPositionsInSegments = (Integer) value;
                    break;
            }
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
     * Method to send the user to the view of the carbon equivalent.
     */
    private void clickOpenCarbonEquivalentPage(){
        Intent intent = new Intent(this, ActivityCarbonEquivalent.class);
        intent.putExtra(getString(R.string.displayed_carbon_footprint), displayedCarbonFootprint);
        startActivity(intent);;
    }

    /**
     * Methode to display the carbon footprint.
     */
    private void displayCarbonFootprint(){
       // Double carbonFootprint = user.calculateCurrentWeekCarbonFootprint();
        displayedCarbonFootprint = 3.0;
        TextView textView = findViewById(R.id.impactTextDashboard);
        textView.setText(displayedCarbonFootprint + getString(R.string.display_message_after_CO2footprint));
    }
}
