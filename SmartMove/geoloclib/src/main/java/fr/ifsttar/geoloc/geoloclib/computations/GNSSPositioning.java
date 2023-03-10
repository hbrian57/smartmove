///=================================================================================================
// Class GNSSPositioning
//      Originally written by Antoine GRENIER
//      Enhanced and Updated by Aravind RAMESH
//        Date :  11/08/2022
///=================================================================================================
/*
 * Copyright 2018(c) IFSTTAR - TeamGEOLOC
 *
 * This file is part of the GeolocPVT application.
 *
 * GeolocPVT is distributed as a free software in order to build a community of users, contributors,
 * developers who will contribute to the project and ensure the necessary means for its evolution.
 *
 * GeolocPVT is a free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version. Any modification of source code in this
 * LGPL software must also be published under the LGPL license.
 *
 * GeolocPVT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Lesser General Public License along with GeolocPVT.
 * If not, see <https://www.gnu.org/licenses/lgpl.txt/>.
 */
///=================================================================================================


package fr.ifsttar.geoloc.geoloclib.computations;

import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.util.Log;

import org.gogpsproject.Constants;
import org.ejml.simple.SimpleMatrix;
import org.gogpsproject.ephemeris.GNSSEphemeris;
import org.gogpsproject.ephemeris.GlonassEphemeris;
import org.gogpsproject.ephemeris.KeplerianEphemeris;
import org.gogpsproject.ephemeris.SatelliteCodeBiases;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import fr.ifsttar.geoloc.geoloclib.Coordinates;
import fr.ifsttar.geoloc.geoloclib.Options;
import fr.ifsttar.geoloc.geoloclib.Utils;
import fr.ifsttar.geoloc.geoloclib.satellites.GNSSObservation;
import fr.ifsttar.geoloc.geoloclib.satellites.SatellitePositionGNSS;

/**
 * Class GNSSPositioning
 *
 * Handling of computing a GNSS position.
 */
public class GNSSPositioning
{
    /* Variables to store Observations*/
    private HashMap<String, GNSSObservation> gnssObservationAllSats;
    private HashMap<String, GNSSObservation> prevGnssObservations;
    private HashMap<String, GNSSObservation> gnssObservationTrackedSats;

    /* Variables to store Satellites information*/
    private HashMap<String, GNSSEphemeris> gnssEphemerisAllSats;
    //private GnssStatus satStat;

    /* Variables for Computations*/
    //private SPP spp;
    //private SPP firstspp;
    private EnhancedSPP spp;
    //private NewEKF sppEKF;
    private EnhancedLSE staticSPP;
    private EnhancedLSE dynamicSPP;
    private PPP ppp;
    private int nbMeasurements;
    //private int prevObs;

    /* Variables for User information*/
    private Coordinates position;
    private SimpleMatrix posX;
    private SimpleMatrix X_pr;
    private SimpleMatrix P_pr;
    //private Coordinates posFused;
    SimpleMatrix init_cord = new SimpleMatrix(3,1);
    private Options options;
    private boolean optionsChanged;

    /* Variables for Miscellaneous information*/
    private long fullBiasNanosFix;
    private double biasNanosFix;
    private double epochCounter;
    private double gdop;
    //Reset Flag
    private boolean flag;

    //private double xPrec;
    //private double yPrec;
    //private double zPrec;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public GNSSPositioning()
    {
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Refresh measurements with new GNSS Measurement event.
     * @param gnssMeasurementsEvent new GNSS Measurement event.
     */
    public void refreshMeasurements(GnssMeasurementsEvent gnssMeasurementsEvent)
    {
        gnssObservationAllSats = new LinkedHashMap<>();
        gnssObservationTrackedSats = new LinkedHashMap<>();

        /* Parsing incoming measurements*/
        if(gnssMeasurementsEvent != null)
        {
            try
            {
                parseGnssMeasurements(gnssMeasurementsEvent, options);
            }
            catch (Exception e)
            {
                Log.e("ERROR", "Can't parse the measurements.");
                e.printStackTrace();
                return;
            }
        }

        /* Merge satellite position and observations*/
        addSatPosToObservations();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Refresh ephemeris with last ephemeris received.
     * @param _gnssEphemerisAllSats Map with of the satellites ephemeris
     */
    public void refreshEphemeris(HashMap<String, GNSSEphemeris>  _gnssEphemerisAllSats)
    {
        gnssEphemerisAllSats = _gnssEphemerisAllSats;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Refreshing with the current computations options.
     * @param _options New options
     */
    public void refreshOptions(Options _options)
    {
        if(options != null && options == _options)
        {
            optionsChanged = false;
        }
        else
        {
            optionsChanged = true;
        }

        options = _options;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Set approx user position for linearization of the system.
     * @param approxPosition approx user position
     */
    public void setApproxPosition(Coordinates approxPosition)
    {
        position = approxPosition;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Function to facilitate users reset request
     * @param input Reset request from the user
     */
    public void resetCondition(Boolean input){flag = input;}
    /*public void setFusedPosition(Coordinates posFsd)
    {
        posFused = posFsd;
    }*/
    //----------------------------------------------------------------------------------------------

    /**
     * Compute absolute user position [from 0,0,0] with current observation and ephemeris.
     * @return computed coordinates
     */

    public Coordinates computeUserPosition()
    {
        Coordinates userCoord = new Coordinates();
        //Coordinates absCoord = new Coordinates();

        if(nbMeasurements == 0)
        {
            epochCounter = 0;
            Log.i("COMPUTATIONS", "Missing data, can't compute position.");
            return userCoord;
        }

        /*Computation of User Position from [0,0,0]*/
        if(isEnoughSatellites())
        {
            /*Computation Mode*/
            /*Standard Point Positioning using Least Squares or EKF*/
            //Spp is forced. Ppp is not used for the moment (14/10/2022).
            if(options.isSppEnabled())
            {
                /*Static Mode: Least Squares (LSE) with Absolute Positioning*/
                if (!options.isDynamicMode())
                {
                    if (epochCounter == 0 || posX.numRows() > 7)
                    {
                        staticSPP = new EnhancedLSE(options, init_cord);
                    }
                    else
                    {
                        staticSPP = new EnhancedLSE(options, position.getSimpleMatrix());
                    }
                    staticSPP.refreshSatelliteObservations(gnssObservationAllSats);
                    staticSPP.computePosition();
                    posX = staticSPP.getX();
                    userCoord = staticSPP.getPosition();
                    gdop = staticSPP.getGdop();
                }
                else
                {
                    /*Dynamic Mode: Extended Kalman Filter (EKF) with Absolute Positioning Updates*/
                    if (epochCounter == 0 || flag)
                    {
                        dynamicSPP = new EnhancedLSE(options, init_cord);
                        dynamicSPP.refreshSatelliteObservations(gnssObservationAllSats);
                        dynamicSPP.computePosition();
                        posX = dynamicSPP.getX();
                        userCoord = dynamicSPP.getPosition();
                        gdop = dynamicSPP.getGdop();
                    }
                    else if (epochCounter == 1 || posX.numRows() <= 7)
                    {
                        spp = new EnhancedSPP(options, posX, epochCounter);
                        spp.refreshSatelliteObservations(gnssObservationAllSats);
                        spp.computePosition();
                        posX = spp.getX();
                        X_pr = spp.getX_Pred();
                        P_pr = spp.getP_Pred();
                        //prevObs = spp.getPrevObs();
                        userCoord = spp.getPosition();
                        gdop = spp.getGdop();

                        /*Absolute Position for correction*/
                        dynamicSPP = new EnhancedLSE(options, init_cord);
                        dynamicSPP.refreshSatelliteObservations(gnssObservationAllSats);
                        dynamicSPP.computePosition();
                        //absCoord = dynamicSPP.getPosition();
                    }
                    else
                    {
                        spp = new EnhancedSPP(options, posX, epochCounter, X_pr, P_pr);
                        spp.refreshSatelliteObservations(gnssObservationAllSats);
                        spp.computePosition();
                        posX = spp.getX();
                        X_pr = spp.getX_Pred();
                        P_pr = spp.getP_Pred();
                        //prevObs = spp.getPrevObs();
                        userCoord = spp.getPosition();
                        gdop = spp.getGdop();

                        /*Absolute Position for correction*/
                        dynamicSPP = new EnhancedLSE(options, init_cord);
                        dynamicSPP.refreshSatelliteObservations(gnssObservationAllSats);
                        dynamicSPP.computePosition();
                        //absCoord = dynamicSPP.getPosition();
                    }
                    /*Absolute Positioning using EKF*/
                    /*if (epochCounter == 0 || posX.numRows() <= 7)
                    {
                        sppEKF = new NewEKF(options, init_cord, epochCounter);
                    }
                    else
                    {
                        sppEKF = new NewEKF(options, posX, epochCounter, X_pr, P_pr);
                    }
                    sppEKF.refreshSatelliteObservations(gnssObservationAllSats);
                    sppEKF.computePosition();
                    posX = sppEKF.getX();
                    X_pr = sppEKF.getX_Pred();
                    P_pr = sppEKF.getP_Pred();
                    userCoord = sppEKF.getPosition();
                    gdop = sppEKF.getGdop();*/
                }

                Log.i("COMPUTATIONS", "SPP, " + userCoord.getLatLngAlt().toString());

            }
            /*Precise Point Positioning (PPP) NOT IMPLEMENTED YET*/
            else if (options.isPppEnabled()) // TODO : /!\ IMPLEMENTATION of PPP /!\
            {
                if (ppp == null)
                {
                    Log.d("PPP: ","Under Development");
                    //ppp = new PPP(computationData, position);
                }
                Log.d("PPP: ","Under Development");
            }

            epochCounter ++;

            /*Condition to check if the relative EKF position is within a range of 2 meters (proportional to the max. velocity of the pedestrian)*/
            if(options.isDynamicMode() && Utils.distanceBetweenCoordinates(userCoord, position) > 2)
            {
                epochCounter = 0;
            }
        }
        else
        {
            if (epochCounter == 0)
            {
                Log.i("COMPUTATIONS", "Not enough valid satellite, can't compute position.");
            }
            else
            {
                epochCounter = 0;
                Log.i("COMPUTATIONS", "GNSS Status: GNSS Blackout (Loss of Lock )");
            }
        }

        position = userCoord;
        return userCoord;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Parse the raw GnssMeasurement objects into a HashMap of GNSSObservations objects.
     * Input: Gnss Measurements object and Processing options
     */
    private void parseGnssMeasurements(GnssMeasurementsEvent gnssMeasurementsEvent, Options options)
    {
        Vector<GnssMeasurement> measurements = new Vector<>(gnssMeasurementsEvent.getMeasurements());
        GnssClock clock = gnssMeasurementsEvent.getClock();

        /* Fixing the fullBias because of clock cycling, improve measurements quality*/
        if (fullBiasNanosFix == 0) {
            fullBiasNanosFix = clock.getFullBiasNanos();
            biasNanosFix = clock.getBiasNanos();
        }

        /* Parsing measurements*/
        for (GnssMeasurement gnssMeasurement : measurements)
        {
            String key = Utils.getFormattedSatIndex(
                    gnssMeasurement.getConstellationType(),
                    gnssMeasurement.getSvid());

            GNSSObservation gnssObservation;

            gnssObservation = new GNSSObservation(clock, fullBiasNanosFix, biasNanosFix);

            // TODO: 13/10/2022 change name setGnssMeasurement to getGnssMeasurement (better)
            boolean success = gnssObservation.setGnssMeasurement(gnssMeasurement, options);

            if (success && gnssObservation.isMeasurementValid(options))
            {
                if (options.isMonoFrequencyEnabled())
                {
                    if (gnssObservation.isL1Enabled())
                    {
                        if (gnssObservationAllSats.containsKey(key) && gnssObservationTrackedSats.containsKey(key))
                        {
                            gnssObservationAllSats.get(key).addOtherFrequency(gnssObservation, options);
                            gnssObservationTrackedSats.get(key).addOtherFrequency(gnssObservation, options);
                        }
                        else if (gnssObservationAllSats.containsKey(key) && !gnssObservationTrackedSats.containsKey(key))
                        {
                            gnssObservationAllSats.get(key).addOtherFrequency(gnssObservation, options);
                            gnssObservationTrackedSats.put(key, gnssObservation);
                        }
                        else if (!gnssObservationAllSats.containsKey(key) && gnssObservationTrackedSats.containsKey(key))
                        {
                            gnssObservationAllSats.put(key, gnssObservation);
                            gnssObservationTrackedSats.get(key).addOtherFrequency(gnssObservation, options);
                        }
                        else
                        {
                            gnssObservationAllSats.put(key, gnssObservation);
                            gnssObservationTrackedSats.put(key, gnssObservation);
                        }
                    }
                }
                //else dual freq
                else
                {
                    if (gnssObservationAllSats.containsKey(key) && gnssObservationTrackedSats.containsKey(key))
                    {
                        gnssObservationAllSats.get(key).addOtherFrequency(gnssObservation, options);
                        gnssObservationTrackedSats.get(key).addOtherFrequency(gnssObservation, options);
                    }
                    else if (gnssObservationAllSats.containsKey(key) && !gnssObservationTrackedSats.containsKey(key))
                    {
                        gnssObservationAllSats.get(key).addOtherFrequency(gnssObservation, options);
                        gnssObservationTrackedSats.put(key, gnssObservation);
                    }
                    else if (!gnssObservationAllSats.containsKey(key) && gnssObservationTrackedSats.containsKey(key))
                    {
                        gnssObservationAllSats.put(key, gnssObservation);
                        gnssObservationTrackedSats.get(key).addOtherFrequency(gnssObservation, options);
                    }
                    else
                    {
                        gnssObservationAllSats.put(key, gnssObservation);
                        gnssObservationTrackedSats.put(key, gnssObservation);
                    }
                }
            }
            /* We add it to the tracked map only for display*/
            else
            {
                if(gnssObservation.getVarL1() == 1)
                {
                    if (gnssObservationTrackedSats.containsKey(key))
                    {
                        gnssObservationTrackedSats.get(key).addFreqL1(gnssObservation);
                    }
                    else
                    {
                        gnssObservationTrackedSats.put(key, gnssObservation);
                    }
                }
                else if (gnssObservation.getVarL5() == 1)
                {
                    if (gnssObservationTrackedSats.containsKey(key))
                    {
                        gnssObservationTrackedSats.get(key).addFreqL5(gnssObservation);
                    }
                    else
                    {
                        gnssObservationTrackedSats.put(key, gnssObservation);
                    }
                }

                if (gnssObservation.getVarL5_trk() == 1)
                {
                    if (gnssObservationTrackedSats.containsKey(key))
                    {
                        gnssObservationTrackedSats.get(key).addFreqL5(gnssObservation);
                    }
                    else
                    {
                        gnssObservationTrackedSats.put(key, gnssObservation);
                    }
                }
            }
        }
        mergePreviousMeasurements();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Merge with previous measurements
     */
    public void mergePreviousMeasurements()
    {
        HashMap<String, GNSSObservation> tmp = new HashMap<>();

        if(prevGnssObservations == null)
        {
            prevGnssObservations = new HashMap<>();
        }

        for (HashMap.Entry<String, GNSSObservation> obs : gnssObservationAllSats.entrySet())
        {
            if (prevGnssObservations.containsKey(obs.getKey()))
            {
                obs.getValue().setSmoothMeasurements(prevGnssObservations.get(obs.getKey()));
            }

            tmp.put(obs.getKey(), obs.getValue());
        }

        prevGnssObservations.clear();
        prevGnssObservations.putAll(tmp);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Compute satellites positions for observation time and merge with observations.
     */
    public void addSatPosToObservations()
    {
        HashMap<String, GNSSEphemeris> eph = this.gnssEphemerisAllSats;
        HashMap<String, GNSSObservation> obs = this.gnssObservationAllSats;

        HashMap<String, GNSSObservation> usedObs = new HashMap<>();

        SatellitePositionGNSS satPos = null;

        nbMeasurements = 0;
        //int k = 0;

        if (eph != null)
        {
            if (!eph.isEmpty() && !obs.isEmpty())
            {
                Iterator<Map.Entry<String, GNSSObservation>> it = obs.entrySet().iterator();
                while (it.hasNext())
                {
                    Map.Entry<String, GNSSObservation> entry = it.next();

                    entry.getValue();

                    int system = entry.getValue().getConstellation();
                    int prn = entry.getValue().getId();

                    String satIndex = Utils.getFormattedSatIndex(system, prn);

                    if (eph.get(satIndex) != null && eph.get(satIndex).getGnssSystem() == system)
                    {
                        /*Transmission time from Android API*/
                        double ttx = entry.getValue().getTtx() / 1e9;

                        try
                        {
                            /* Keplerian like constellation*/
                            /* Constellation: GALILEO*/
                            if(system == GnssStatus.CONSTELLATION_GALILEO)
                            {
                                /* Compute first without satellite clock correction*/
                                satPos = new SatellitePositionGNSS(
                                        (KeplerianEphemeris) eph.get(satIndex), ttx, position, options);

                                /* Recompute again with satellite clock correction*/
                                satPos = new SatellitePositionGNSS(
                                        (KeplerianEphemeris) eph.get(satIndex), ttx - satPos.getDtSat(), position, options);
                            }
                            /* Constellation: GPS*/
                            if (system == GnssStatus.CONSTELLATION_GPS)
                            {
                                /* Compute first without satellite clock correction*/
                                satPos = new SatellitePositionGNSS(
                                        (KeplerianEphemeris) eph.get(satIndex), ttx, position, options);

                                /* Recompute again with satellite clock correction*/
                                satPos = new SatellitePositionGNSS(
                                        (KeplerianEphemeris) eph.get(satIndex), ttx - satPos.getDtSat(), position, options);

                            }
                            /* Constellation: BEIDOU*/
                            if (system == GnssStatus.CONSTELLATION_BEIDOU)
                            {
                                /* BEIDOU shall not be used for the first 2 seconds in the beginning and after reset*/
                                if (epochCounter > 2)
                                {
                                    if (options.isDynamicMode() && posX.numRows() <= 7)
                                    {
                                        continue;
                                    }
                                    if (!(eph.get(satIndex).getPrn() == 59 || eph.get(satIndex).getPrn() == 60 || eph.get(satIndex).getPrn() == 61 ||
                                            eph.get(satIndex).getPrn() == 1 || eph.get(satIndex).getPrn() == 2 || eph.get(satIndex).getPrn() == 3 ||
                                            eph.get(satIndex).getPrn() == 4 || eph.get(satIndex).getPrn() == 5 ))
                                    {
                                        /* Compute first without satellite clock correction*/
                                        satPos = new SatellitePositionGNSS(
                                                (KeplerianEphemeris) eph.get(satIndex), ttx, position, options);

                                        /* Recompute again with satellite clock correction*/
                                        satPos = new SatellitePositionGNSS(
                                                (KeplerianEphemeris) eph.get(satIndex), ttx - satPos.getDtSat(), position, options);
                                    }
                                    else
                                    {
                                        continue;
                                    }
                                }
                                else
                                {
                                    continue;
                                }
                            }
                            /* Constellation: GLONASS*/
                            if(system == GnssStatus.CONSTELLATION_GLONASS )
                            {
                                GlonassEphemeris glo_eph = (GlonassEphemeris) eph.get(satIndex);
                                if (glo_eph.getBn() == 0) { /* Functioning satellite*/

                                    /* Compute first without satellite clock correction*/
                                    satPos = new SatellitePositionGNSS(
                                            glo_eph, ttx, position, options);

                                    /* Recompute again with satellite clock correction*/
                                    satPos = new SatellitePositionGNSS(
                                            glo_eph, ttx - satPos.getDtSat(), position, options);

                                }
                            }
                        }
                        catch (NullPointerException e)
                        {
                            Log.e("SAT", "User position not settled yet.");
                            return;
                        }

                        /* Correct earth rotation during propagation time*/
                        double dt = entry.getValue().getTransmissionTime();
                        //double dt = satPos.getGeomTransmissionTime();
                        satPos.applyEarthRotation(dt);

                        // Correct using streams for precise coordinates
                        /*if(options.isStreamsCorrectionEnabled())
                        {
                            satPos.applyPreciseCorrections(eph.get(satIndex));
                        }*/

                        /* Removing low elevation satellites*/
                        if (!options.isDynamicMode())
                        {
                            if (system == GnssStatus.CONSTELLATION_GPS || system == GnssStatus.CONSTELLATION_GALILEO)
                            {
                                if(Math.abs(satPos.getSatElevation()) < options.getCutoffAngle())
                                {
                                    continue;
                                }
                            }
                            else
                            {
                                if(Math.abs(satPos.getSatElevation()) < 30)
                                {
                                    continue;
                                }
                            }
                        }

                        /* Removing single frequency measurements when iono-free is enabled*/
                        if(options.isIonofreeEnabled()
                                && !entry.getValue().isL3Enabled())
                        {
                            continue;
                        }

                        /* Function to facilitate Single frequency solution*/
                        if(options.isMonoFrequencyEnabled()
                            && entry.getValue().isL5Enabled())
                        {
                            continue;
                        }

                        /*Conditions to facilitate GNSS Loss of Lock in Dual Frequency, Dynamic Scenarios*/
                        /*if(options.isDynamicMode() && options.isDualFrequencyEnabled())
                        {
                            if (entry.getValue().getPseudoRateL1() == 0 && entry.getValue().getPseudoRateL5() == 0)
                            {
                                continue;
                            }
                        }*/

                        /*Conditions to facilitate GNSS Loss of Lock in Single Frequency, Dynamic Scenarios*/
                        /*if(options.isDynamicMode() && options.isMonoFrequencyEnabled())
                        {
                            if (entry.getValue().getPseudoRateL1() == 0)
                            {
                                continue;
                            }
                        }*/

                        entry.getValue().setSatellitePosition(satPos);
                        usedObs.put(satIndex,entry.getValue());

                        nbMeasurements++;
                    }
                }
            }
        }

        /* Difference between hashmaps*/
        HashSet<String> unionKeys = new HashSet<>(usedObs.keySet());
        unionKeys.addAll(gnssObservationAllSats.keySet());
        unionKeys.removeAll(usedObs.keySet());

        gnssObservationAllSats.clear();
        gnssObservationAllSats.putAll(usedObs);

    }

    //----------------------------------------------------------------------------------------------

    /**
     * Check if enough satellite for computations.
     * @return Success
     */
    private boolean isEnoughSatellites()
    {

        // STRONG hypothsesis on GPS usage -by default.
        int minNbSpp = 4;
        int minNbPpp = 5;

        // TODO: 14/10/2022 if GALILEO only, then 5 sat are expected, it should not...
        /*Using one measurement from Galileo for Inter System Bias Correction between GPS & Galileo */
        if(options.getSystemsEnabled().contains(GnssStatus.CONSTELLATION_GALILEO))
        {
            minNbSpp ++;
            minNbPpp ++;
        }
        // TODO: 14/10/2022 Why BEIDOU not handled ?? On HMI, GPS, GALILEO and BEIDOU are possible 
        /*if(options.getSystemsEnabled().contains(GnssStatus.CONSTELLATION_GPS))
        {
            minNbSpp ++;
            minNbPpp ++;
        }
        if(options.getSystemsEnabled().contains(GnssStatus.CONSTELLATION_GALILEO))
        {
            minNbSpp ++;
            minNbPpp ++;
        }
        if(options.getSystemsEnabled().contains(GnssStatus.CONSTELLATION_BEIDOU))
        {
            minNbSpp ++;
            minNbPpp ++;
        }
        if(options.getSystemsEnabled().contains(GnssStatus.CONSTELLATION_GLONASS))
        {
            minNbSpp ++;
            minNbPpp ++;
        }*/

        if(options.isSppEnabled())
        {
            //return 1 if enough Sat, 0, otherwise
            return nbMeasurements >= minNbSpp;
        }
        else
        {
            //return 1 if enough Sat, 0, otherwise
            return nbMeasurements >= minNbPpp;
        }
    }

    //----------------------------------------------------------------------------------------------

    public HashMap<String, GNSSObservation> getGnssObservationAllSats() {
        return gnssObservationAllSats;
    }

    //----------------------------------------------------------------------------------------------

    /*public HashMap<String, GNSSEphemeris> getGnssEphemerisAllSats() {
        return gnssEphemerisAllSats;
    }

    //----------------------------------------------------------------------------------------------

    public void setGnssObservationAllSats(HashMap<String, GNSSObservation> gnssObservationAllSats) {
        this.gnssObservationAllSats = gnssObservationAllSats;
    }

    //----------------------------------------------------------------------------------------------

    public void setGnssEphemerisAllSats(HashMap<String, GNSSEphemeris> gnssEphemerisAllSats) {
        this.gnssEphemerisAllSats = gnssEphemerisAllSats;
    }*/

    //----------------------------------------------------------------------------------------------

    public double getGdop()
    {
        return gdop;
    }

    //----------------------------------------------------------------------------------------------

    public Coordinates getPosition()
    {
        return position;
    }

    //----------------------------------------------------------------------------------------------

    public double getEpochCounter(){return epochCounter;}

    //----------------------------------------------------------------------------------------------

    public HashMap<String, GNSSObservation> getGnssObservationTrackedSats() {
        return gnssObservationTrackedSats;
    }

    //----------------------------------------------------------------------------------------------

    /*public double getxPrec() {
        return xPrec;
    }

    public double getyPrec() {
        return yPrec;
    }

    public double getzPrec() {
        return zPrec;
    }*/


    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

//    /**
//     * Testing of the computations, useful to replay data sets and test different options.
//     * @param args args
//     * @throws IOException except
//     */
//    public static void main(String[] args) throws IOException
//    {
//        // Survey files
//
//        String line;
//        BufferedReader br;
//        //String obsFilename = "./MyData/16-07-2019_12-58-12_pseudo.txt";
//        //String satFilename = "./MyData/16-07-2019_12-58-12_satpos.txt";
//        //String ephFilename = "./MyData/16-07-2019_12-58-12_ephem.txt";
//
//        //String obsFilename = "./MyData/22-07-2019_10-31-07_pseudo.txt";
//        //String ephFilename = "./MyData/22-07-2019_10-31-07_ephem.txt";
//
//        //String obsFilename = "./MyData/22-07-2019_11-32-36_pseudo.txt";
//        //String ephFilename = "./MyData/22-07-2019_11-32-36_ephem.txt";
//
//        // Dynamic
//        //String obsFilename = "./MyData/31-07-2019_14-03-59_pseudo.txt";
//        //String ephFilename = "./MyData/31-07-2019_14-03-59_ephem.txt";
//
//        String obsFilename = "./MyData/31-07-2019_14-14-05_pseudo.txt";
//        String ephFilename = "./MyData/31-07-2019_14-14-05_ephem.txt";
//
//        // DCB L1 and L5 for post-processing
//        String dcbFilename = "./MyData/DCB.txt";
//
//        //String obsFilename = "./MyData/26-08-2019_10-22-02_pseudo.txt";
//        //String ephFilename = "./MyData/26-08-2019_10-22-02_ephem.txt";
//
//        Coordinates approxUserCoord = new Coordinates(4343420.0, -124910.0, 4653460.0);
//
//        // Computation options
//
//        Options options = new Options();
//        Vector<Integer> systemEnabled = new Vector<Integer>();
//        systemEnabled.add(GnssStatus.CONSTELLATION_GPS);
//        //systemEnabled.add(GnssStatus.CONSTELLATION_GALILEO);
//        //systemEnabled.add(GnssStatus.CONSTELLATION_BEIDOU);
//        options.setSystemsEnabled(systemEnabled);
//        options.setIonofreeEnabled(false);
//        options.setDualFrequencyEnabled(false);
//        options.setMonoFrequencyEnabled(true);
//        options.setSppEnabled(true);
//        options.setSmoothingEnabled(false);
//        options.setStreamsEnabled(true);
//        options.setStreamsCorrectionEnabled(false);
//        options.setKalmanEnabled(false);
//        options.setDcbEnabled(false);
//        options.setDynamicMode(false);
//        options.setTropoEnabled(false);
//
//        // -----------------------------------------------------------------------------------------
//        /// Reading obs
//        try
//        {
//            br = new BufferedReader(new FileReader(obsFilename));
//        }
//        catch (FileNotFoundException e)
//        {
//            System.out.println("File not found.");
//            return;
//        }
//
//        //Skipping header lines
//        line = br.readLine();
//        line = br.readLine();
//
//        double prev = 0.0;
//
//        Vector<LinkedHashMap<String, GNSSObservation>> epochs = new Vector<>();
//        LinkedHashMap<String, GNSSObservation> satelliteObservations = new LinkedHashMap<>();
//        while ((line = br.readLine()) != null)
//        {
//            String[] str = line.split(",");
//
//            if(prev > 0)
//            {
//                if(prev != Double.parseDouble(str[0]))
//                {
//                    //System.out.println(MapSatelliteObservations.size());
//
//                    epochs.add(satelliteObservations);
//                    satelliteObservations = new LinkedHashMap<>();
//                }
//            }
//
//            if(!systemEnabled.contains(Integer.parseInt(str[1])))
//            {
//                continue;
//            }
//
//            GNSSObservation gnssObservation = new GNSSObservation();
//            gnssObservation.setTrx(Double.parseDouble(str[0]) * 1e9);
//            gnssObservation.setConstellation(Integer.parseInt(str[1]));
//
//            if(gnssObservation.getConstellation() == GnssStatus.CONSTELLATION_BEIDOU)
//            {
//                gnssObservation.setTrx(Double.parseDouble(str[0]) * 1e9 - Constants.BDS_LEAP_SECONDS * 1e9);
//            }
//
//            gnssObservation.setId(Integer.parseInt(str[2]));
//            gnssObservation.setPseudorangeL1(Double.parseDouble(str[3]));
//            gnssObservation.setPhaseL1(Double.parseDouble(str[4]));
//            gnssObservation.setSmoothPseudoL1(Double.parseDouble(str[5]));
//            gnssObservation.setPseudorangeL5(Double.parseDouble(str[6]));
//            gnssObservation.setPhaseL5(Double.parseDouble(str[7]));
//            gnssObservation.setSmoothPseudoL5(Double.parseDouble(str[8]));
//            gnssObservation.setPseudorangeL3(Double.parseDouble(str[9]));
//            gnssObservation.setPhaseL3(Double.parseDouble(str[10]));
//            gnssObservation.setSmoothPseudoL3(Double.parseDouble(str[11]));
//            gnssObservation.setCn0L1(Double.parseDouble(str[12]));
//            gnssObservation.setCn0L5(Double.parseDouble(str[13]));
//            gnssObservation.setCycleSlipL1(Integer.parseInt(str[14]));
//            gnssObservation.setCycleSlipL5(Integer.parseInt(str[15]));
//
//
//            if(gnssObservation.getPseudorangeL3() > 0)
//            {
//                gnssObservation.setL3Enabled(true);
//            }
//            if(gnssObservation.getPseudorangeL1() > 0)
//            {
//                gnssObservation.setL1Enabled(true);
//            }
//            if(gnssObservation.getPseudorangeL5() > 0)
//            {
//                gnssObservation.setL5Enabled(true);
//            }
//            if(options.isSmoothingEnabled())
//            {
//                if(gnssObservation.getSmoothPseudoL3() > 0)
//                {
//                    gnssObservation.setSmoothL3Enabled(true);
//                }
//                if(gnssObservation.getSmoothPseudoL1() > 0)
//                {
//                    gnssObservation.setSmoothL1Enabled(true);
//                }
//                if(gnssObservation.getSmoothPseudoL5() > 0)
//                {
//                    gnssObservation.setSmoothL5Enabled(true);
//                }
//            }
//
//            String key = Utils.getFormattedSatIndex(gnssObservation.getConstellation(), gnssObservation.getId());
//            if(gnssObservation.getCn0L1() > options.getCutoffNoise() && gnssObservation.isL1Enabled())
//            {
//                satelliteObservations.put(key, gnssObservation);
//            }
//            else if (gnssObservation.getCn0L5() > options.getCutoffNoise() && gnssObservation.isL5Enabled())
//            {
//                satelliteObservations.put(key, gnssObservation);
//            }
//
//            prev = Double.parseDouble(str[0]);
//
//        }
//        // -----------------------------------------------------------------------------------------
//        /// Reading the DCB
//        //Skipping header lines
//
//        try
//        {
//            br = new BufferedReader(new FileReader(dcbFilename));
//        }
//        catch (FileNotFoundException e)
//        {
//            System.out.println("File not found.");
//            return;
//        }
//
//        line = br.readLine();
//        line = br.readLine();
//
//        HashMap<String, SatelliteCodeBiases.CodeBias> dcb = new LinkedHashMap<>();
//        while ((line = br.readLine()) != null)
//        {
//            String[] str = line.split(",");
//
//            int system = (int) Integer.parseInt(str[0]);
//            int prn = (int) Integer.parseInt(str[1]);
//
//            // Checking the constellation
//            int codeL1 = 0;
//            int codeL5 = 0;
//            switch (system)
//            {
//                case GnssStatus.CONSTELLATION_GPS:
//                    codeL1 = Constants.CODE_L1C;
//                    codeL5 = Constants.CODE_L5X;
//                    break;
//                case GnssStatus.CONSTELLATION_GALILEO:
//                    codeL1 = Constants.CODE_L1X;
//                    codeL5 = Constants.CODE_L5X;
//                    break;
//                case GnssStatus.CONSTELLATION_BEIDOU:
//                    codeL1 = Constants.CODE_L1I;
//                    codeL5 = Constants.CODE_L6I;
//                    break;
//                default:
//                    Log.e("COMP", "Unknown constellation.");
//                    break;
//            }
//
//            LinkedHashMap<Integer, Double> biases = new LinkedHashMap<>();
//            biases.put(codeL1, (Double) Double.parseDouble(str[2]));
//
//            if(!str[3].equals(" "))
//            {
//                biases.put(codeL5, (Double) Double.parseDouble(str[3]));
//            }
//            SatelliteCodeBiases.CodeBias cb = new SatelliteCodeBiases.CodeBias(system,prn,0.0,0,biases);
//
//            dcb.put(Utils.getFormattedSatIndex(system,prn),cb);
//
//        }
//
//        try
//        {
//            br = new BufferedReader(new FileReader(ephFilename));
//        }
//        catch (FileNotFoundException e)
//        {
//            System.out.println("File not found.");
//            return;
//        }
//
//        //Skipping header lines
//        line = br.readLine();
//        line = br.readLine();
//
//        prev = 0.0;
//        HashMap<String, GNSSEphemeris> satEphem = new LinkedHashMap<>();
//        HashMap<Double, HashMap<String, GNSSEphemeris>> allEphem = new LinkedHashMap<>();
//        Vector<Double> listEphEpochs = new Vector<>();
//        while ((line = br.readLine()) != null)
//        {
//            String[] str = line.split(",");
//
//            GNSSEphemeris eph = new GNSSEphemeris();
//
//            if(prev > 0)
//            {
//                if(prev != Double.parseDouble(str[0]))
//                {
//                    listEphEpochs.add(prev);
//                    allEphem.put(prev, (HashMap<String, GNSSEphemeris>) satEphem.clone());
//                }
//            }
//
//            eph.setGnssSystem((int) Double.parseDouble(str[1]));
//            eph.setPrn((int) Double.parseDouble(str[2]));
//            eph.setWeek((int) Double.parseDouble(str[3]));
//            eph.setToc(Double.parseDouble(str[4]));
//            eph.setAf2(Double.parseDouble(str[5]));
//            eph.setAf1(Double.parseDouble(str[6]));
//            eph.setAf0(Double.parseDouble(str[7]));
//            eph.setCrs(Double.parseDouble(str[8]));
//            eph.setDeltaN(Double.parseDouble(str[9]));
//            eph.setM0(Double.parseDouble(str[10]));
//            eph.setCuc(Double.parseDouble(str[11]));
//            eph.setEc(Double.parseDouble(str[12]));
//            eph.setCus(Double.parseDouble(str[13]));
//            eph.setSquareA(Double.parseDouble(str[14]));
//            eph.setToe(Double.parseDouble(str[15]));
//            //eph.retrieveAodo(Double.parseDouble(str[16]));
//            eph.setCic(Double.parseDouble(str[17]));
//            eph.setOmega0(Double.parseDouble(str[18]));
//            eph.setCis(Double.parseDouble(str[19]));
//            eph.setI0(Double.parseDouble(str[20]));
//            eph.setCrc(Double.parseDouble(str[21]));
//            eph.setOmega(Double.parseDouble(str[22]));
//            eph.setOmegaDot(Double.parseDouble(str[23]));
//            eph.setIdot(Double.parseDouble(str[24]));
//
//            String key = Utils.getFormattedSatIndex(eph.getGnssSystem(), eph.getPrn());
//
//            eph.setCodeBias(dcb.get(key));
//
//            satEphem.put(key, eph);
//
//            prev = Double.parseDouble(str[0]);
//        }
//        listEphEpochs.add(prev);
//        allEphem.put(prev, (HashMap<String, GNSSEphemeris>) satEphem.clone());
//
//        // -----------------------------------------------------------------------------------------
//        /// Main loop
//
//        GNSSPositioning gnssPositioning = new GNSSPositioning();
//        gnssPositioning.setApproxPosition(approxUserCoord);
//        int idxEphEpochs = 0;
//        for(int i=0; i<2000; i++)
//        {
//            if(idxEphEpochs+1 < listEphEpochs.size())
//            {
//                double obsEpoch = epochs.elementAt(i).values().stream().findFirst().get().getTrx()/1e9;
//                double ephEpoch = listEphEpochs.elementAt(idxEphEpochs+1);
//
//                if(obsEpoch == ephEpoch)
//                {
//                    idxEphEpochs++;
//                }
//            }
//
//            gnssPositioning.refreshOptions(options);
//            gnssPositioning.setGnssEphemerisAllSats(allEphem.get(listEphEpochs.elementAt(idxEphEpochs)));
//            gnssPositioning.setGnssObservationAllSats(epochs.elementAt(i));
//            gnssPositioning.mergePreviousMeasurements();
//            gnssPositioning.addSatPosToObservations();
//
//            Coordinates userCoord = gnssPositioning.computeUserPosition();
//
//            System.out.println("" + userCoord.getTow()/1e9 + "," + userCoord.getLatLngAlt());
//        }
//
//        return;
//    }
}

