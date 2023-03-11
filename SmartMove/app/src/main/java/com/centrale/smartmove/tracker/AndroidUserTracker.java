package com.centrale.smartmove.tracker;

import static android.content.Context.LOCATION_SERVICE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import java.util.List;

public class AndroidUserTracker extends UserTracker implements LocationListener {
    double longitude;
    double latitude;
    double altitude;
    LocationManager locationManager;
    String locationProvider;

    @SuppressLint("MissingPermission")
    public AndroidUserTracker(AppCompatActivity activity) {
        //Initialisation des variables
        longitude = 0;
        latitude = 0;
        //Création du LocationManager pour récupérer la position
        locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        locationProvider = locationManager.getBestProvider(criteria, false);
        //register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(locationProvider, 0, 0, this);
        Location locationTest = locationManager.getLastKnownLocation(locationProvider);
        if (locationTest != null) {
            System.out.println("Provider " + locationProvider + " has been selected.");
            onLocationChanged(locationTest);
        } else {
            System.out.println("Location is null");
        }


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        altitude = location.getAltitude();
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
    @Override
    public double getLongitude() {
        return longitude;
    }
    @Override
    public double getLatitude() {
        return latitude;
    }
    @Override
    public double getAltitude() {
        return altitude;
    }

    @Override
    public String getFormattedName() {
        return "Tracker ANDROID";
    }
}