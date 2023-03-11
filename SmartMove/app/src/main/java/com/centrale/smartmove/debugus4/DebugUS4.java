package com.centrale.smartmove.debugus4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.security.Permission;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.view.View;

import android.Manifest;
import com.centrale.smartmove.R;
import com.centrale.smartmove.models.User;
import com.centrale.smartmove.tracker.AndroidUserTracker;
import com.centrale.smartmove.tracker.GeolocpvtUserTracker;
import com.centrale.smartmove.tracker.TrackerClock;
import com.centrale.smartmove.tracker.UserTracker;


import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;



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
    Button centerMapButton;

    MapView mapView;
    IMapController mapController;
    Marker marker;


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


        centerMapButton = findViewById(R.id.center_map_on_user);
        centerMapButton.setOnClickListener(v -> {
            GeoPoint startPoint = new GeoPoint(clock.getLatitude(), clock.getLongitude());
            mapController.animateTo(startPoint);
        });

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        TextView statusPermission = findViewById(R.id.status_permission);
        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        permissionGranted = permissionGranted && ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
        permissionGranted = permissionGranted && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
        permissionGranted = permissionGranted && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        permissionGranted = permissionGranted && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted) {
            statusPermission.setText("OK");
        } else {
            statusPermission.setText("NON ACCORDÉE");
        }

        mapView = findViewById(R.id.mapViewPositions);
        mapController = mapView.getController();
        //mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        GeoPoint startPoint = new GeoPoint(47.218371, -1.553621);
        mapController.setCenter(startPoint);


        selectedTracker = new AndroidUserTracker(this);
        clock = new TrackerClock(selectedTracker);
        trackerSwitched(false);
        clock.addObserver(this);
        user = new User();
        //make a point on the map at Nantes, France
        addPositionOnMap(47.218371, -1.553621);
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
        if (observable instanceof TrackerClock) {
            TrackerClock trackerClock = (TrackerClock) observable;
            if (geolocTracker) {
                updatePositionGeoloc(trackerClock.getLatitude(), trackerClock.getLongitude());
            } else {
                updatePositionAndroid(trackerClock.getLatitude(), trackerClock.getLongitude());
            }
            addPositionOnMap(trackerClock.getLatitude(), trackerClock.getLongitude());
        }
    }






    public void addPositionOnMap(double latitude, double longitude) {
        Marker newMarker = new Marker(mapView);
        newMarker.setPosition(new GeoPoint(latitude, longitude));
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        newMarker.setIcon(ResourcesCompat.getDrawable(Objects.requireNonNull(getResources()), R.drawable.red_marker, null));
        mapView.getOverlays().add(newMarker);
    }
}