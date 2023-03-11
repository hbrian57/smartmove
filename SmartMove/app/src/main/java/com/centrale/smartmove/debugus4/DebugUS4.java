package com.centrale.smartmove.debugus4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

import com.centrale.smartmove.R;
import com.centrale.smartmove.models.User;
import com.centrale.smartmove.tracker.AndroidUserTracker;
import com.centrale.smartmove.tracker.GeolocpvtUserTracker;
import com.centrale.smartmove.tracker.TrackerClock;
import com.centrale.smartmove.tracker.UserTracker;

public class DebugUS4 extends AppCompatActivity implements Observer {
    User user;
    TrackerClock clock;
    UserTracker selectedTracker;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch UISwitch;

    Boolean geolocTracker = false;
    TextView latitudeAndroid;
    TextView longitudeAndroid;
    TextView latitudeGeoloc;
    TextView longitudeGeoloc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_us4);

        latitudeAndroid = findViewById(R.id.latitude_android);
        longitudeAndroid = findViewById(R.id.longitude_android);
        latitudeGeoloc = findViewById(R.id.latitude_geoloc);
        longitudeGeoloc = findViewById(R.id.longitude_geoloc);

        UISwitch = findViewById(R.id.switch_geoloc);

        UISwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            trackerSwitched(isChecked);
            geolocTracker = isChecked;
        });
        selectedTracker = new AndroidUserTracker(this);
        clock = new TrackerClock(selectedTracker);
        trackerSwitched(false);
        clock.addObserver(this);
        user = new User();
    }

    public void trackerSwitched(@NonNull Boolean useGeolocTracker){
        if (useGeolocTracker){
            selectedTracker = new GeolocpvtUserTracker(this);
        } else {
            selectedTracker = new AndroidUserTracker(this);
        }
        clock.setTracker(selectedTracker);
    }

    public void updatePositionAndroid(double latitude, double longitude){
        runOnUiThread(() -> {
            latitudeAndroid.setText(doubleToString(latitude));
            longitudeAndroid.setText(doubleToString(longitude));
        });
    }

    void updatePositionGeoloc(double latitude, double longitude){
        runOnUiThread(() -> {
            latitudeGeoloc.setText(doubleToString(latitude));
            longitudeGeoloc.setText(doubleToString(longitude));
        });
    }

    private String doubleToString(double value){
        return String.valueOf(value);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (!(observable instanceof TrackerClock)) {
            return;
        }
        TrackerClock trackerClock = (TrackerClock) observable;
        if (geolocTracker){
            updatePositionGeoloc(trackerClock.getLatitude(), trackerClock.getLongitude());
        } else {
            updatePositionAndroid(trackerClock.getLatitude(), trackerClock.getLongitude());
        }
    }
}