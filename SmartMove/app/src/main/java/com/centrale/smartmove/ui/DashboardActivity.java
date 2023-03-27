package com.centrale.smartmove.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.centrale.smartmove.*;
import com.centrale.smartmove.models.Challenge;
import com.centrale.smartmove.models.ChallengeGoal;
import com.centrale.smartmove.models.GoalType;
import com.centrale.smartmove.models.TransportType;
import com.centrale.smartmove.models.Trip;
import com.centrale.smartmove.tracker.GeolocpvtUserTracker;
import com.centrale.smartmove.tracker.TrackerClock;
import com.centrale.smartmove.models.User;
import com.centrale.smartmove.tracker.AndroidUserTracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class DashboardActivity extends AppCompatActivity implements Observer {

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
        setContentView(R.layout.activity_dashboard);
        readConfigFile();
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                PackageManager.PERMISSION_GRANTED);

        //CREATION DE L'UTILISATEUR
        user = new User();
        user.setChallengeListOfCoach(getChallengeListFromResource());
        //If the save exists, we load it
        SharedPreferences preferences = getSharedPreferences(getString(R.string.SMARTMOVE_PREFS), Context.MODE_PRIVATE);
        System.out.println("SaveFile found : " + preferences.contains(getString(R.string.SMARTMOVE_USER)));
        if (preferences.contains(getString(R.string.SMARTMOVE_USER))) {
            System.out.println("SaveFile : " + preferences.getString(getString(R.string.SMARTMOVE_USER), null));
            String json = preferences.getString(getString(R.string.SMARTMOVE_USER), null);
            try {
                user.loadFromSave(new JSONObject(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        //if the user has less than 3 challenges, we add some
        System.out.println(user.getOnGoingChallenge().size());
        if (user.getOnGoingChallenge().size() <= 3) {
            for (int i = 0; i < 3 - user.getOnGoingChallenge().size(); i++) {
                user.getNewChallenge();
            }
        }

        // LISTENER POUR LE BOUTON DE NAVIGATION VERS LA PAGE D'EMPREINTE CARBONE EQUIVALENTE
        navToEquivalent = findViewById(R.id.imageViewNavToCarbonEquivalent);
        navToEquivalent.setOnClickListener(v -> {
            clickOpenCarbonEquivalentPage();
        });
        navToEquivalent.setVisibility(View.VISIBLE);

        // AFFICHAGE UI
        displayCarbonFootprint();
        displayChallenges();

        // CREATION DU TRACKER
        AndroidUserTracker trackerAndroid = new AndroidUserTracker(this);
        GeolocpvtUserTracker trackerGeoloc = new GeolocpvtUserTracker(this);
        TrackerClock tracker;
        if (geolocSwitch) {
            tracker = new TrackerClock(trackerGeoloc, positionUpdateDelay);
        } else {
            tracker = new TrackerClock(trackerAndroid, positionUpdateDelay);
        }
        tracker.addObserver(this);
    }

    private void readConfigFile() {
        //Open the config file from raw ressource
        InputStream inputStream = getResources().openRawResource(R.raw.config);
        //Create a new Yaml object
        Yaml yaml = new Yaml();
        //Load the file into a map
        Map<String, Object> configResult = (Map) yaml.load(inputStream);
        for (Map.Entry<String, Object> entry : configResult.entrySet()) {
            //Get the key and the value
            String key = entry.getKey();
            Object value = entry.getValue();
            switch (key) {
                case "geoloc":
                    geolocSwitch = (boolean) value;
                    break;
                case "positionUpdateClockDelay":
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
        Toast.makeText(this, getString(R.string.onback_defaultmessage), Toast.LENGTH_LONG).show();
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
        RecyclerChallengeAdapter challengeAdapter = new RecyclerChallengeAdapter(this, user.getOnGoingChallenge());
        recyclerView.setAdapter(challengeAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Method to send the user to the view of the carbon equivalent.
     */
    private void clickOpenCarbonEquivalentPage() {
        Intent intent = new Intent(this, ActivityCarbonEquivalent.class);
        intent.putExtra(getString(R.string.displayed_carbon_footprint), displayedCarbonFootprint);
        startActivity(intent);
    }

    /**
     * Methode to display the carbon footprint.
     */
    private void displayCarbonFootprint() {
        // Double carbonFootprint = user.calculateCurrentWeekCarbonFootprint()
        double carbonFootprint = 0;
        for (Trip t : user.getUserTrips()) {
            carbonFootprint += t.getTotalCarbonFootprint();
        }
        displayedCarbonFootprint = carbonFootprint;
        TextView textView = findViewById(R.id.impactTextDashboard);
        textView.setText(displayedCarbonFootprint + getString(R.string.display_message_after_CO2footprint));
    }


    @NonNull
    private ArrayList<Challenge> getChallengeListFromResource() {
        ArrayList<Challenge> challengeList = new ArrayList<>();
        //lecture des lignes
        try {
            InputStream locationdeschallenges = getResources().openRawResource(R.raw.listechallenges);
            BufferedReader reader = new BufferedReader(new InputStreamReader(locationdeschallenges));
            StringBuilder jsonStringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonStringBuilder.append(line);
            }
            reader.close();
            locationdeschallenges.close();
            //creation de l'arraylist de challenge
            JSONObject json = new JSONObject(jsonStringBuilder.toString());
            JSONArray challenges = json.getJSONArray("challenges");

            for (int i = 0; i < challenges.length(); i++) {
                JSONObject currentchall = challenges.getJSONObject(i);
                Challenge challenge = new Challenge(
                        currentchall.getString("title"), currentchall.getString("short_text"), currentchall.getString("long_text")
                );
                switch (currentchall.getInt("goal_type")) {
                    case 0:
                        challenge.setGoalType(GoalType.DISTANCE_COVERED);
                        break;
                    case 1:
                        challenge.setGoalType(GoalType.NUMBER_OF_TRIPS);
                        break;
                }
                switch (currentchall.getString("transport_type")) {
                    case "bike":
                        challenge.setTransportType(TransportType.BIKE);
                        break;
                    case "foot":
                        challenge.setTransportType(TransportType.WALKING);
                        break;
                }
                switch (currentchall.getString("icon")) {
                    case "bike":
                        challenge.setIcon(R.drawable.ic_bike);
                        break;
                    case "walking":
                        challenge.setIcon(R.drawable.ic_walking);
                        break;
                }
                challengeList.add(challenge);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return challengeList;

    }


    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof TrackerClock) {
            TrackerClock trackerClock = (TrackerClock) o;
            try {
                user.newPositionObtained(trackerClock.getPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onPause() {
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.SMARTMOVE_PREFS), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.SMARTMOVE_USER), user.getSaveFormat().toString());
        //editor.putString(getString(R.string.SMARTMOVE_USER), (new User()).getSaveFormat().toString());
        editor.apply();
        super.onPause();
    }
}