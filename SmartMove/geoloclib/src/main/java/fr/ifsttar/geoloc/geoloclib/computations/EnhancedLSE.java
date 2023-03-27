///=================================================================================================
// Class EnhancedLSE (Weighted Least Squares Estimation Technique)
//      Author : Aravind RAMESH, Research Engineer, AME-GEOLOC Laboratory, UGE, Nantes
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

import android.location.GnssStatus;
import android.util.Log;

import org.ejml.simple.SimpleMatrix;
import org.gogpsproject.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import fr.ifsttar.geoloc.geoloclib.Coordinates;
import fr.ifsttar.geoloc.geoloclib.Options;
import fr.ifsttar.geoloc.geoloclib.Utils;
import fr.ifsttar.geoloc.geoloclib.satellites.GNSSObservation;
import fr.ifsttar.geoloc.geoloclib.satellites.SatellitePositionGNSS;


/**
 * Class EnhancedLSE
 *
 * Functions for computation of weighted least squares (WLS) solution.
 *
 */

public class EnhancedLSE extends PVT
{
    /*Variables to store GNSS Measurements, processing options and estimated position*/
    private Map<String, GNSSObservation> MapSatelliteObservations;
    private SatellitePositionGNSS satellitePosition;
    private GNSSObservation satelliteObservation;

    private Coordinates position;
    private SimpleMatrix initX;

    private HashMap<Integer, Integer> hashMapSystemIndex;

    private Options processingOptions;

    /* Constants*/
    private final double SIGMA_CODE_L1 = 5; // [m], precision "a priori" of a pseudorange measurement.

    /* Index of the State vector variables*/
    private int MINIMAL_PARAM = 4; /* Minimum number of parameters */
    private int IDX_X = 0; /* Position 'X'*/
    private int IDX_Y = 1; /* Position 'Y'*/
    private int IDX_Z = 2; /* Position 'Z'*/
    private int IDX_C_DTR = 3; /* Receiver Clock Error*/
    //private double counter;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * @param processingOptions users options for computations
     * @param initX initial values for vector state
     */
    public EnhancedLSE(Options processingOptions, SimpleMatrix initX)
    {
        //this.counter = epochCounter;
        this.initX = initX;
        this.processingOptions = processingOptions;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Refresh with new satellite measurements.
     * @param _satelliteObservations New measurements.
     */
    public void refreshSatelliteObservations(HashMap<String, GNSSObservation> _satelliteObservations)
    {
        this.MapSatelliteObservations = _satelliteObservations;
        setCurrentSystems();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Compute the position with the current data using Weighted LSE
     */
    public void computePosition()
    {
        int nbParam = MINIMAL_PARAM + (hashMapSystemIndex.size() - 1);
        int nbObs = getNumberObservations();

        // Setting the state vector X to create matrices with the right size afterwards*/
        X = new SimpleMatrix(nbParam, 1);
        X.set(IDX_X,0, initX.get(0));
        X.set(IDX_Y,0, initX.get(1));
        X.set(IDX_Z,0, initX.get(2));

        /* Design Matrix*/
        SimpleMatrix A = new SimpleMatrix(nbObs, X.numRows());

        /* Measurements vector*/
        SimpleMatrix B = new SimpleMatrix(nbObs,1);

        /* Weighting matrix*/
        SimpleMatrix Q = SimpleMatrix.identity(nbObs);

        double tow = 0;
        int loopcount = 0;

        do
        {
            // TODO: 17/10/2022 Why instatiation of position + TropoCorrection here ? instead of before do ?
            position = new Coordinates(X.get(IDX_X,0), X.get(IDX_Y,0), X.get(IDX_Z,0));
            TropoCorrections tropoCorrections = new TropoCorrections(position);

            int i = 0;
            for(Map.Entry<String, GNSSObservation> entry : MapSatelliteObservations.entrySet())
            {
                Vector<EnhancedLSE.Measurements> measurementsVector;

                satelliteObservation = entry.getValue();

                satellitePosition = satelliteObservation.getSatellitePosition();

                tow = satelliteObservation.getTrx();

                // TODO: 14/10/2022 What does it mean ? "break" instead of "continue" (line 152)?
                if (satellitePosition == null)
                {
                    continue;
                }

                double satX = satellitePosition.getSatCoordinates().getX();
                double satY = satellitePosition.getSatCoordinates().getY();
                double satZ = satellitePosition.getSatCoordinates().getZ();
                // TODO: 17/10/2022 where does this DtSat come from ? Which Clock bias is it?
                double satDt = satellitePosition.getDtSat();

                /* Computation & application of Tropospheric correction*/
                double tropoCorr = 0;
                // TODO: 06/12/2022 : link between checkbox Tropo and value of tropoEnabled broken or absent. tropoEnabled = 0 (init value).
                if (processingOptions.isTropoEnabled())
                {
                    // TODO: 06/12/2022 : of pb inside method, then -1 is returned. AND THIS IS NOT CHECKED 
                    tropoCorr = tropoCorrections.getSaastamoinenCorrection(satellitePosition.getSatElevation());
                }

                measurementsVector = selectMeasurements(satelliteObservation);

                double geometricDistance = Utils.distanceBetweenCoordinates(
                        position,
                        satellitePosition.getSatCoordinates());

                if (measurementsVector != null)
                {
                    for(EnhancedLSE.Measurements obs: measurementsVector)
                    {
                        /* Setting the weight*/
                        double sigmaMeas = getWeightValue(obs.cn0);

                        A.set(i, IDX_X, (X.get(IDX_X) - satX) / geometricDistance);
                        A.set(i, IDX_Y, (X.get(IDX_Y) - satY) / geometricDistance);
                        A.set(i, IDX_Z, (X.get(IDX_Z) - satZ) / geometricDistance);
                        A.set(i, IDX_C_DTR, 1);

                        // TODO: 17/10/2022  second part of "if" seems not valid => IDX_C_DTR not coherent with getConst.
                        /* Checking if we have multiple system, and if satellite is part of the reference constellation*/
                        if (hashMapSystemIndex.size() > 1
                                && hashMapSystemIndex.get(entry.getValue().getConstellation()) != IDX_C_DTR)
                        {
                            int idxGTO = hashMapSystemIndex.get(entry.getValue().getConstellation());

                            A.set(i, idxGTO, 1);
                            B.set(i, 0,
                                    obs.pseudo
                                            - geometricDistance
                                            - X.get(IDX_C_DTR)
                                            + Constants.SPEED_OF_LIGHT  * satDt
                                            - X.get(idxGTO) /* System Time Offset*/
                                            - tropoCorr);
                        }
                        else
                        {
                            B.set(i, 0,
                                    obs.pseudo
                                            - geometricDistance
                                            - X.get(IDX_C_DTR)
                                            + Constants.SPEED_OF_LIGHT  * satDt
                                            - tropoCorr);
                        }

                        // TODO: 07/12/2022 : investigate why a 5 factor was applied (SIGMA_CODE_L1)
                        Q.set(i,i, Math.pow(SIGMA_CODE_L1 * (sigmaMeas), 2));
                        i++;
                    }
                }
            }
            /* Function (part of "PVT" class) that computes Least squares Solution */
            computeLSE(A, B, Q);
            loopcount ++;

        }
        // TODO: 14/10/2022 stop condition = loopcount >=25 OR dX.normF <=10e-2 (10 cm good or not ?)
        while(loopcount < 25 && dX.normF() > 10e-2);

        position = new Coordinates(X.get(IDX_X,0),X.get(IDX_Y,0),X.get(IDX_Z,0), tow);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Build pseudoranges vector from GNSSObservation object and processing options
     * @param satelliteObservation Observations
     * @return Vector of pseudoranges
     */
    private Vector<EnhancedLSE.Measurements> selectMeasurements(GNSSObservation satelliteObservation)
    {

        Vector<EnhancedLSE.Measurements> measurementsList = new Vector<>();

        EnhancedLSE.Measurements meas;

            /*int codeL1;
            int codeL5;
            // Checking the constellation
            switch (satelliteObservation.getConstellation()) {
                case GnssStatus.CONSTELLATION_GPS:
                    codeL1 = Constants.CODE_L1C;
                    codeL5 = Constants.CODE_L5X;
                    break;
                case GnssStatus.CONSTELLATION_GALILEO:
                    codeL1 = Constants.CODE_L1X;
                    codeL5 = Constants.CODE_L5X;
                    break;
                case GnssStatus.CONSTELLATION_BEIDOU:
                    codeL1 = Constants.CODE_L1I;
                    codeL5 = Constants.CODE_L6I;
                    break;
                case GnssStatus.CONSTELLATION_GLONASS:
                    codeL1 = Constants.CODE_L1C;
                    codeL5 = Constants.CODE_L5X;
                    break;
                default:
                    Log.e("COMP", "Unknown constellation.");
                    return null;
            }|| satelliteObservation.getConstellation() == 6*/
        //1: GPS, 5:BEIDOU, 6:GALILEO
        if (satelliteObservation.getConstellation() == 1)
        {

            /* Selecting the right code for the measurement: ionofree selected, L3 computed*/
            if (processingOptions.isIonofreeEnabled()
                    && satelliteObservation.isL3Enabled())
            {
                //int codeL3 = 0; // No DCB if iono-free (in theory...)

                meas = new EnhancedLSE.Measurements(
                        //get ionofree combination L3
                        satelliteObservation.getPseudorangeL3(),
                        //Cn0L1 used as Weight
                        satelliteObservation.getCn0L1());

                measurementsList.add(meas);
            }
            else if (processingOptions.isDualFrequencyEnabled())
            {
                if (satelliteObservation.isL1Enabled()) {
                    meas = new EnhancedLSE.Measurements(
                            satelliteObservation.getPseudorangeL1(),
                            satelliteObservation.getCn0L1());

                    measurementsList.add(meas);
                }

                if (satelliteObservation.isL5Enabled()) {
                    meas = new EnhancedLSE.Measurements(
                            satelliteObservation.getPseudorangeL5(),
                            satelliteObservation.getCn0L5());

                    measurementsList.add(meas);
                }
            }
            else if (processingOptions.isMonoFrequencyEnabled())
            {
                if (satelliteObservation.isL1Enabled()) {
                    meas = new EnhancedLSE.Measurements(
                            satelliteObservation.getPseudorangeL1(),
                            satelliteObservation.getCn0L1());

                    measurementsList.add(meas);
                }
            }
            return measurementsList;
        }
        else
        {
            return null;
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * @return Computed position.
     */
    public Coordinates getPosition()
    {
        return position;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Set the index for the inter-system clock bias parameter.
     */
    private void setCurrentSystems()
    {
        hashMapSystemIndex = new HashMap<>();
        int k = 0;

        for (HashMap.Entry<String, GNSSObservation> entry : MapSatelliteObservations.entrySet())
        {
            /*Reject Beidou & Galileo Measurements*/
            if (entry.getValue().getConstellation() == 5 || entry.getValue().getConstellation() == 6)
            {
                continue;
            }

            if(hashMapSystemIndex.isEmpty())
            {
                hashMapSystemIndex.put(entry.getValue().getConstellation(), IDX_C_DTR);
            }
            else if(!hashMapSystemIndex.keySet().contains(entry.getValue().getConstellation()))
            {
                hashMapSystemIndex.put(entry.getValue().getConstellation(), MINIMAL_PARAM + k);

                k += 1;
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Get the number of observations (L1/L5/L3) available, to build the matrices later.
     * @return Number of observations
     */
    public int getNumberObservations()
    {
        int nbObs = 0;
        for(Map.Entry<String, GNSSObservation> entry : MapSatelliteObservations.entrySet())
        {
            satelliteObservation = entry.getValue();
            if (satelliteObservation.getConstellation() == 5 || satelliteObservation.getConstellation() == 6)
            {
                continue;
            }

            if(processingOptions.isIonofreeEnabled())
            {
                if(satelliteObservation.getPseudorangeL3() > 0.0)
                {
                    nbObs ++;
                }
            }
            else if(processingOptions.isDualFrequencyEnabled())
            {
                if(satelliteObservation.getPseudorangeL1() > 0.0)
                {
                    nbObs ++;
                }
                if(satelliteObservation.getPseudorangeL5() > 0.0)
                {
                    nbObs ++;
                }
            }
            else if(processingOptions.isMonoFrequencyEnabled())
            {
                if(satelliteObservation.getPseudorangeL1() > 0.0)
                {
                    nbObs ++;
                }
            }
        }

        return nbObs;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Compute the weight value of the obs, using C/N0 of the signal.
     * @return sigma of measurement
     */
    private double getWeightValue(double cn0)
    {
        double sigma = 0.0;

        double a = 10;
        double b = Math.pow(150, 2);

        sigma = (a + b * Math.pow(10, (-0.1 * cn0)));

        return sigma;
    }

    /**
     * Structure for easier handling of measurements in computations.
     */
    class Measurements
    {
        double pseudo;
        double cn0;
        //int codeFreq;

        public Measurements(double pseudo, double cn0)
        {
            this.pseudo = pseudo;
            this.cn0 = cn0;
            //this.codeFreq = codeFreq;
        }
    }

    //----------------------------------------------------------------------------------------------

    public SimpleMatrix getX()
    {
        return X;
    }

    public double getGdop() {
        return 0.0;
    }

}
