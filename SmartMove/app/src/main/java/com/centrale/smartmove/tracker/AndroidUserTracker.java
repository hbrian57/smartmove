package com.centrale.smartmove.tracker;

import static android.content.Context.LOCATION_SERVICE;

import android.annotation.SuppressLint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AndroidUserTracker extends UserTracker implements LocationListener {

    //Attributes------------------------------------------------------------------------------------
    /**
     * double
     */
    private double longitude;

    /**
     * double
     */
    private double latitude;

    //Getters and Setters---------------------------------------------------------------------------

    /**
     * Getter
     * @return double longitude
     */
    @Override
    public double getLongitude() {
        return longitude;
    }

    /**
     * Getter
     * @return double latitude
     */
    @Override
    public double getLatitude() {
        return latitude;
    }

    //Methods---------------------------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    public AndroidUserTracker(AppCompatActivity activity) {
        //Initialisation des variables
        longitude = 0;
        latitude = 0;
        //Création du LocationManager pour récupérer la position
        LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String locationProvider = locationManager.getBestProvider(criteria, false);
        //register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(locationProvider, 0, 0, this);
        Location locationTest = locationManager.getLastKnownLocation(locationProvider);
        if (locationTest != null) {
            System.out.println("Provider " + locationProvider + " has been selected. (TODO, passer en FusedLocationProviderClient)");
            onLocationChanged(locationTest);
        } else {
            System.out.println("Location is null");
        }


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
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

    //----------------------------------------------------------------------------------------------
}