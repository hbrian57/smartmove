package fr.ifsttar.geoloc.geoloclib.location_PVT;

import android.location.Location;

import java.util.Vector;

public class LocationAccuracy {
    private Vector<Double> mLocationPvt;

    public void getPvtMeasurement(Location location){
        mLocationPvt = new Vector<>();

        mLocationPvt.add(location.getLatitude());
        mLocationPvt.add(location.getLongitude());
        mLocationPvt.add(location.getAltitude());
        mLocationPvt.add((double)location.getAccuracy());
        mLocationPvt.add((double)location.getVerticalAccuracyMeters());
        mLocationPvt.add((double)location.getBearing());
        mLocationPvt.add((double)location.getBearingAccuracyDegrees());
        mLocationPvt.add((double)location.getSpeed());
        mLocationPvt.add((double)location.getSpeedAccuracyMetersPerSecond());

    }

    public Vector<Double> getAccuracy(){
        return mLocationPvt;
    }

    public void refresh(){
        mLocationPvt.removeAllElements();
    }
}
