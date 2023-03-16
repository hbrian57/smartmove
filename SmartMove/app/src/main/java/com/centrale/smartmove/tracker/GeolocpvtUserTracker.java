package com.centrale.smartmove.tracker;

import static android.content.Context.LOCATION_SERVICE;

import android.annotation.SuppressLint;
import android.location.Criteria;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.gogpsproject.Utils;
import org.gogpsproject.ephemeris.BeidouEphemeris;
import org.gogpsproject.ephemeris.GNSSEphemeris;
import org.gogpsproject.ephemeris.GalileoEphemeris;
import org.gogpsproject.ephemeris.GlonassEphemeris;
import org.gogpsproject.ephemeris.GpsEphemeris;
import org.gogpsproject.ephemeris.KeplerianEphemeris;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import fr.ifsttar.geoloc.geoloclib.Coordinates;
import fr.ifsttar.geoloc.geoloclib.LatLngAlt;
import fr.ifsttar.geoloc.geoloclib.Options;
import fr.ifsttar.geoloc.geoloclib.computations.GNSSPositioning;

public class GeolocpvtUserTracker extends UserTracker{
    private double longitude;
    private double latitude;
    private double altitude;
    private GNSSPositioning positionTracker;
    private Map<Integer, Map<Integer, Map<Integer,GnssNavigationMessage>>> mMapMessages;
    private GnssMeasurementsEvent.Callback mGnssMeasurementsEventCallback;
    private GnssNavigationMessage.Callback mGnssNavigationMessageCallback;
    private OnNmeaMessageListener nmeaMessageListener;
    private GnssMeasurementsEvent mGnssMeasurementsEvent;
    private HashMap<String, GNSSEphemeris> satelliteEphemeris;
    private LocationManager mLocationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationListener locationListener;
    private GNSSPositioning gnssPositioning;

    private int rstId = 0;

    Options processingOptions;
    Coordinates approxUserCoord;
    Coordinates userCoord;


    @SuppressLint("MissingPermission")
    public GeolocpvtUserTracker(AppCompatActivity activity) {
        mLocationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }
    @Override
    public double getLongitude() {
        return longitude;
    }
    @Override
    public double getLatitude() {
        return latitude;
    }


    private Options getProcessingOptions() {
        Options options = new Options();
        options.setDualFrequencyEnabled(false);
        options.setIonofreeEnabled(false);
        options.setPppEnabled(false);
        options.setMonoFrequencyEnabled(true);
        options.setSppEnabled(true);
        options.setCutoffAngle(0);
        options.setCutoffNoise(0f);
        options.setDynamicMode(true);
        options.setIonoCorrEnabled(false);

        Vector<Integer> systemsEnabled = new Vector<>();
        systemsEnabled.add(GnssStatus.CONSTELLATION_GPS);
        systemsEnabled.add(GnssStatus.CONSTELLATION_GALILEO);
        systemsEnabled.add(GnssStatus.CONSTELLATION_BEIDOU);
        //systemsEnabled.add(GnssStatus.CONSTELLATION_GLONASS);

        options.setSystemsEnabled(systemsEnabled);
        return options;
    }

    /**
     * Get the satellite positions from the navigation message.
     */
    private void getSatPositionFromNavigationMessage()
    {

        if(satelliteEphemeris == null)
        {
            satelliteEphemeris = new HashMap<>();
        }

        /*getting GNSS satellite positions from the navigation message*/
        if(!mMapMessages.isEmpty()){
            if(mMapMessages.containsKey(GnssNavigationMessage.TYPE_GPS_L1CA)){
                /*we recuperate satellite id, subFrameId and navigation message from GPS in another map*/
                Map<Integer, Map<Integer, GnssNavigationMessage>> mMapMessagesGPS =
                        new HashMap<>(mMapMessages.get(GnssNavigationMessage.TYPE_GPS_L1CA));

                /*we iterate this new map by satellite id*/
                for(Map.Entry<Integer,Map<Integer,GnssNavigationMessage>> entry : mMapMessagesGPS.entrySet()){
                    /*if we have already all subframes to reconstruct data*/
                    if(entry.getValue().containsKey(1) && entry.getValue().containsKey(2) && entry.getValue().containsKey(3) && entry.getValue().containsKey(4)){
                        /*we save subframes needed for reconstructing in local variables*/
                        GnssNavigationMessage subframe1 = entry.getValue().get(1);
                        GnssNavigationMessage subframe2 = entry.getValue().get(2);
                        GnssNavigationMessage subframe3 = entry.getValue().get(3);
                        GnssNavigationMessage subframe4 = entry.getValue().get(4);
                        /*we reconstruct ephemeris and we save then in our vector*/
                        KeplerianEphemeris ephemerisGPSL1 = new GpsEphemeris(subframe1,subframe2,subframe3,subframe4);

                        /*we initialize object to calculate satellite position ans we add it to our SatellitePositionGPS vector*/
                        satelliteEphemeris.put(Utils.getFormattedSatIndex(GnssStatus.CONSTELLATION_GPS, entry.getKey()), ephemerisGPSL1);
                    }
                }
            }
            //entry.getValue().containsKey(0) &&
            if(mMapMessages.containsKey(GnssNavigationMessage.TYPE_GAL_I)){
                /*we recuperate satellite id, subFrameId and navigation message from Galileo in another map*/
                Map<Integer, Map<Integer, GnssNavigationMessage>> mMapMessagesGalileo =
                        new HashMap<>(mMapMessages.get(GnssNavigationMessage.TYPE_GAL_I));

                /*we iterate this new map by satellite id*/
                for(Map.Entry<Integer,Map<Integer,GnssNavigationMessage>> entry : mMapMessagesGalileo.entrySet()){
                    /*if we have already all pages to reconstruct data*/
                    if(entry.getValue().containsKey(1) && entry.getValue().containsKey(2) && entry.getValue().containsKey(3) && entry.getValue().containsKey(4) && entry.getValue().containsKey(5)){
                        /*we save pages needed for reconstructing in local variables*/
                        GnssNavigationMessage word1 = entry.getValue().get(1);
                        GnssNavigationMessage word2 = entry.getValue().get(2);
                        GnssNavigationMessage word3 = entry.getValue().get(3);
                        GnssNavigationMessage word4 = entry.getValue().get(4);
                        GnssNavigationMessage word5 = entry.getValue().get(5);
                        /*we reconstruct ephemeris and save them in our vector*/
                        KeplerianEphemeris ephemerisGALE1 = new GalileoEphemeris(word1, word2, word3, word4, word5);

                        /*we initialize object to calculate satellite position and add it to our SatellitePositionGPS vector*/
                        satelliteEphemeris.put(Utils.getFormattedSatIndex(GnssStatus.CONSTELLATION_GALILEO, entry.getKey()), ephemerisGALE1);
                    }
                }
            }
            if(mMapMessages.containsKey(GnssNavigationMessage.TYPE_BDS_D1)){
                /*we recuperate satellite id, subFrameId and navigation message from Beidou in another map*/
                Map<Integer, Map<Integer, GnssNavigationMessage>> mMapMessagesBeidou =
                        new HashMap<>(mMapMessages.get(GnssNavigationMessage.TYPE_BDS_D1));

                /*we iterate this new map by satellite id*/
                for(Map.Entry<Integer,Map<Integer,GnssNavigationMessage>> entry : mMapMessagesBeidou.entrySet()){
                    /*if we have already all subframes to reconstruct data*/
                    if(entry.getValue().containsKey(1) && entry.getValue().containsKey(2) && entry.getValue().containsKey(3)){
                        /*we save subframes needed for reconstructing in local variables*/
                        GnssNavigationMessage subframe1 = entry.getValue().get(1);
                        GnssNavigationMessage subframe2 = entry.getValue().get(2);
                        GnssNavigationMessage subframe3 = entry.getValue().get(3);
                        /*we reconstruct ephemeris and we save them in our vector*/
                        KeplerianEphemeris ephemerisBDSL1 = new BeidouEphemeris(subframe1,subframe2,subframe3);

                        /*we initialize object to calculate satellite position and we add it to our SatellitePositionGPS vector*/
                        satelliteEphemeris.put(Utils.getFormattedSatIndex(GnssStatus.CONSTELLATION_BEIDOU, entry.getKey()), ephemerisBDSL1);
                    }
                }
            }
            if(mMapMessages.containsKey(GnssNavigationMessage.TYPE_GLO_L1CA)) {
                /*we recuperate satellite id, subFrameId and navigation message from GLONASS in another map*/
                Map<Integer, Map<Integer, GnssNavigationMessage>> mMapMessagesGlonass =
                        new HashMap<>(mMapMessages.get(GnssNavigationMessage.TYPE_GLO_L1CA));

                /*we iterate this new map by satellite id*/
                for (Map.Entry<Integer, Map<Integer, GnssNavigationMessage>> entry : mMapMessagesGlonass.entrySet()) {
                    /*if we have already all subframes to reconstruct data*/
                    if (entry.getValue().containsKey(1) && entry.getValue().containsKey(2) && entry.getValue().containsKey(3) && entry.getValue().containsKey(4) && entry.getValue().containsKey(5)) {
                        /*we save subframes needed for reconstructing in local variables*/
                        GnssNavigationMessage subframe1 = entry.getValue().get(1);
                        GnssNavigationMessage subframe2 = entry.getValue().get(2);
                        GnssNavigationMessage subframe3 = entry.getValue().get(3);
                        GnssNavigationMessage subframe4 = entry.getValue().get(4);

                        GnssNavigationMessage subframe5 = entry.getValue().get(5); /*only needed for N4 to reconstruct date of acquisition*/
                        /*we reconstruct ephemeris and we save then in our vector*/
                        GlonassEphemeris ephemerisGlonassL1 = new GlonassEphemeris(subframe1, subframe2, subframe3, subframe4, subframe5);


                        /*we initialize object to calculate satellite position ans we add it to our SatellitePositionGPS vector*/
                        satelliteEphemeris.put(Utils.getFormattedSatIndex(GnssStatus.CONSTELLATION_GLONASS, entry.getKey()), ephemerisGlonassL1);
                    }
                }
            }
        }
    }

    /**
     * Handler of new GNSS measurement event
     * Outcome: Position estimate is extracted from the algorithm
     */
    private void gnssMeasurementsHandler() throws Exception {
        /*Checking if first event*/
        if(gnssPositioning == null)
        {
            gnssPositioning = new GNSSPositioning();
        }

        if(gnssPositioning.getPosition() == null)
        {
            if(approxUserCoord == null)
            {
                /*start from [0, 0, 0] -> Absolute Positioning*/
                approxUserCoord = new Coordinates(0.0, 0.0, 0.0);
            }

            gnssPositioning.setApproxPosition(approxUserCoord);
        }

        /* Refreshing the computations option at every epoch*/
        processingOptions = getProcessingOptions();
        gnssPositioning.refreshOptions(processingOptions);

        /* Get satellites positions, via broadcast navigation message*/
        getSatPositionFromNavigationMessage();

        /*Refreshing with last satellite ephemeris and measurements*/
        gnssPositioning.refreshEphemeris(satelliteEphemeris);
        gnssPositioning.refreshMeasurements(mGnssMeasurementsEvent);

        /*Compute a position with current data*/
        //check if manual reset has been required
        if (rstId == 1)
        {
            gnssPositioning.resetCondition(true);
            rstId = 0;
        }
        else
        {
            gnssPositioning.resetCondition(false);
        }

        userCoord = gnssPositioning.computeUserPosition();

        /*If results null, something wrong happened and we are discarding the previous measurements*/
        if(userCoord == null)
        {
            gnssPositioning = new GNSSPositioning();
        }
        /* Otherwise, we update our time variable*/
        else
        {
            if(gnssPositioning.getGnssObservationAllSats().isEmpty())
            {
                return;
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    public void refreshPosition() throws Exception {
        userCoord = gnssPositioning.getPosition();
        gnssMeasurementsHandler();
        LatLngAlt coordinates = userCoord.getLatLngAlt();
        latitude = coordinates.getLatitude();
        longitude = coordinates.getLongitude();
        altitude = coordinates.getAltitude();
    }

}
