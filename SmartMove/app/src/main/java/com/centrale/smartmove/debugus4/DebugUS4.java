package com.centrale.smartmove.debugus4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import android.Manifest;
import com.centrale.smartmove.*;
import com.centrale.smartmove.models.TimestampedPosition;
import com.centrale.smartmove.models.Trip;
import com.centrale.smartmove.models.TripSegment;
import com.centrale.smartmove.models.User;
import com.centrale.smartmove.tracker.AndroidUserTracker;
import com.centrale.smartmove.tracker.GeolocpvtUserTracker;
import com.centrale.smartmove.tracker.TrackerClock;
import com.centrale.smartmove.tracker.UserTracker;


import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;



/**
 * Debug activity for the US4
 */
public class DebugUS4 extends AppCompatActivity implements Observer {

    Boolean drawMarkers = false;
    Boolean drawSelfPosition = true;

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

    final private Integer positionUpdateDelay = 1000;

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

        Button displayTripButton = findViewById(R.id.DisplayTrip);
        displayTripButton.setOnClickListener(v -> {
            //delete all the previous Polylines
            mapView.getOverlays().removeIf(overlay -> overlay instanceof Polyline);
            displayCurrentTripOnMap();
//            displayDummyPolyline();
        });


        Button forceNewTripButton = findViewById(R.id.forceNewTrip);
        forceNewTripButton.setOnClickListener(v -> {
            user.forceNewTrip();
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
        mapController.setZoom(18.0);
        GeoPoint startPoint = new GeoPoint(47.218371, -1.553621);
        mapController.setCenter(startPoint);

        //add the automatic polyline
        displayAutomaticPolyline();


        selectedTracker = new AndroidUserTracker(this);
        clock = new TrackerClock(selectedTracker, positionUpdateDelay);
        trackerSwitched(false);
        clock.addObserver(this);
        user = new User();
        //make a point on the map at Nantes, France
        //addPositionOnMap(47.218371, -1.553621);
    }

    private void displayDummyPolyline() {
        System.out.print("Displaying dummy polyline");
        ArrayList<GeoPoint> points = new ArrayList<>();
        points.add(new GeoPoint(47.237641177, -1.5867024));
        //add a few random points in the area
        points.add(new GeoPoint(47.237541177, -1.5865024));
        points.add(new GeoPoint(47.237441177, -1.5862024));

        Polyline polyline = new Polyline();
        polyline.setPoints(points);
        mapView.getOverlays().add(polyline);
        mapView.invalidate();

    }

    /**
     * Method called when the user tracker is switched
     * @param useGeolocTracker true if the geoloc tracker is used, false if the android tracker is used
     */
    public void trackerSwitched(@NonNull Boolean useGeolocTracker){
        if (useGeolocTracker){
            selectedTracker = new GeolocpvtUserTracker(this);
        } else {
            selectedTracker = new AndroidUserTracker(this);
        }
        clock.setTracker(selectedTracker);
    }

    /**
     * Update the position in the display table for the android tracker
     * @param latitude latitude of the position
     * @param longitude longitude of the position
     */
    public void updatePositionAndroid(double latitude, double longitude){
        runOnUiThread(() -> {
            latitudeAndroid.setText(doubleToString(latitude));
            longitudeAndroid.setText(doubleToString(longitude));
        });
    }

    /**
     * Update the position in the display table for the geoloc tracker
     * @param latitude latitude of the position
     * @param longitude longitude of the position
     */
    void updatePositionGeoloc(double latitude, double longitude){
        runOnUiThread(() -> {
            latitudeGeoloc.setText(doubleToString(latitude));
            longitudeGeoloc.setText(doubleToString(longitude));
        });
    }

    /**
     * Convert a double to a string
     * @param value double to convert
     * @return string of the double
     */
    private String doubleToString(double value){
        return String.valueOf(value);
    }

    //Méthode héritée de Observer, appelée à chaque fois qu'un objet observé est modifié (et que c'est notifié)
    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof TrackerClock) {
            TrackerClock trackerClock = (TrackerClock) observable;
            if (geolocTracker) {
                updatePositionGeoloc(trackerClock.getLatitude(), trackerClock.getLongitude());
            } else {
                updatePositionAndroid(trackerClock.getLatitude(), trackerClock.getLongitude());
            }
            if (drawMarkers) {
                addPositionOnMap(trackerClock.getLatitude(), trackerClock.getLongitude());
            }
            if (drawSelfPosition) {
                //Remove all the previous markers
                mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker);
                addPositionOnMap(trackerClock.getLatitude(), trackerClock.getLongitude());

            }
            try {
                user.newPositionObtained(trackerClock.getPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
            addNewPointToPolyline(trackerClock.getLatitude(), trackerClock.getLongitude());
            //Run the method on the UI thread
            runOnUiThread(this::updateInfosFromUserChange);
        }
    }

    private void updateInfosFromUserChange() {
        TextView numberOfSegmentsInTrip = findViewById(R.id.number_of_segments);
        numberOfSegmentsInTrip.setText(String.valueOf(user.getCurrentTrip().getNumberOfSegments()));
        TextView numberOfPositionsInSegment = findViewById(R.id.number_of_positions);
        numberOfPositionsInSegment.setText(String.valueOf(user.getCurrentTrip().getCurrentSegment().getNumberOfPositions()));
        TextView transportType = findViewById(R.id.computedTransportType);
        transportType.setText(user.getCurrentTrip().getCurrentSegment().getTransportType().toString());
        TextView speed = findViewById(R.id.computedSpeed);
        try {
            speed.setText(String.valueOf(user.getCurrentTrip().getCurrentSegment().calculateMeanVelocity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayCurrentTripOnMap() {
        ArrayList<GeoPoint> points = new ArrayList<>();
        //System.out.println("displayCurrentTripOnMap");
        Trip currentTrip = user.getCurrentTrip();
        Polyline polyline = new Polyline();
        for (TripSegment segment : currentTrip.getTripSegments()) {
            for (TimestampedPosition position : segment.getPositionList()) {
                points.add(new GeoPoint(position.getLatitude(), position.getLongitude()));
            }
        }
        polyline.setPoints(points);
        mapView.getOverlayManager().add(polyline);
        mapView.invalidate();
    }

    Polyline automaticPolyline = new Polyline();
    ArrayList<GeoPoint> automaticPoints = new ArrayList<>();
    private void displayAutomaticPolyline() {
        automaticPolyline.setPoints(automaticPoints);
        mapView.getOverlayManager().add(automaticPolyline);
    }
    private void addNewPointToPolyline(double latitude, double longitude) {
        automaticPoints.add(new GeoPoint(latitude, longitude));
        automaticPolyline.setPoints(automaticPoints);
        mapView.invalidate();
    }

    /**
     * Add a marker on the map
     * @param latitude latitude of the marker
     * @param longitude longitude of the marker
     */
    public void addPositionOnMap(double latitude, double longitude) {
        Marker newMarker = new Marker(mapView);
        newMarker.setPosition(new GeoPoint(latitude, longitude));
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        newMarker.setIcon(ResourcesCompat.getDrawable(Objects.requireNonNull(getResources()), R.drawable.red_marker, null));
        mapView.getOverlays().add(newMarker);
        mapView.invalidate();
    }
}