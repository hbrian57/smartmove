///=================================================================================================
// Class EnhancedSPP
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
 * Class EnhancedSPP
 * Class for computation of a SPP solution, with dynamic positioning using TDCP method.
 */
public class EnhancedSPP extends PVT
{
    private Map<String, GNSSObservation> MapSatelliteObservations;
    private SatellitePositionGNSS satellitePosition;
    private GNSSObservation satelliteObservation;

    private Coordinates position;
    private SimpleMatrix initX;
    private SimpleMatrix X_Pred;
    private SimpleMatrix P_Pred;

    private HashMap<Integer, Integer> hashMapSystemIndex;

    private Options processingOptions;

    private ExtendedKalmanFilter ekf;
    //private int prevObs;

    // Constants
    private final double SIGMA_CODE_L1 = 5; /* [m], precision "a priori" of a pseudorange measurement*/
    private final double SIGMA_CODE_L5 = 3;
    private final double SIGMA_PHASE = 1e-2;
    private final double DELTA_T = 1.0;     /* [s], time between two measurements*/

    private int MINIMAL_PARAM = 8; /* Minimum number of parameters */
    private int IDX_X = 0; /* Position along 'X'*/
    private int IDX_XDOT = 1; /* Velocity along 'X'*/
    private int IDX_Y = 2; /* Position along 'Y'*/
    private int IDX_YDOT = 3; /* Velocity along 'Y'*/
    private int IDX_Z = 4; /* Position along 'Z'*/
    private int IDX_ZDOT = 5; /* Velocity along 'Z'*/
    private int IDX_C_DTR = 6; /* Receiver Clock Offset*/
    private int IDX_CLOCK_DRIFT = 7; /* Receiver Clock Drift*/

    private double counter;
    //private boolean rstFlag;

    //private double gdop;
    //private double pdop;
    //private double tdop;

    /* Extracted from GNSS Compare*/
    /** sqrt(sigma) in the horizontal dimension (x, y) of the dynamic model
     */
    // TODO: 08/12/2022 values of x_y = 1m instead 0.2m, and z = 1 instead of 0,01m (lesson VR) 
    private static final double S_xy = 1;
    /** sqrt(sigma) in the vertical dimension(z) of the dynamic model
     */
    private static final double S_z = 1;

    /* h_* constants basically depend on the chipset of the receiver*/
    /** Allan variance coefficient h_{-2} in meters. TCXO low quality
     */
    private static final double h_2 = 2.0e-20 * Math.pow(Constants.SPEED_OF_LIGHT,2);
    /** Allan variance coefficient h_0 in meters. TCXO low quality
     */
    private static final double h_0 = 2.0e-19 * Math.pow(Constants.SPEED_OF_LIGHT,2);
    /** receiver clock phase error
     */
    private static final double S_g = 2.0 * Math.pow(Constants.PI_ORBIT,2) * h_2;
    /** receiver clock frequency error
     */
    private double S_f = h_0 / 2.0; // receiver clock frequency error

    private static final double INITIAL_SIGMAPOS = 10.; // 100 meters
    /** initial guess of sigma of the speed in the horizontal in meters per second
     */
    private static final double INITIAL_SIGMASPEED = 0.01;
    /** initial guess of sigma of the clock bias in meters for the process noise matrix Q
     */
    private static final double INITIAL_SIGMACLOCKBIAS = 3000000.;
    /** initial guess of sigma of the clock bias drift in meters.
     */
    private static final double INITIAL_SIGMACLOCKDRIFT = 10.;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * @param processingOptions users options for computations
     * @param initX initial values for vector state
     */
    public EnhancedSPP(Options processingOptions, SimpleMatrix initX, double epochCounter)
    {
        this.counter = epochCounter;
        this.initX = initX;
        this.processingOptions = processingOptions;
        //this.rstFlag = rstFlag;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Alternate Constructor
     *
     */
    public EnhancedSPP(Options processingOptions, SimpleMatrix initX, double epochCounter, SimpleMatrix X_Pred, SimpleMatrix P_Pred)
    {
        this.counter = epochCounter;
        this.initX = initX;
        this.X_Pred = X_Pred;
        this.P_Pred = P_Pred;
        this.processingOptions = processingOptions;
        //this.rstFlag = rstFlag;
        //this.prevObs = totalObs;
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
     * Compute the position with the current data using EKF
     */
    public void computePosition()
    {
        int nbParam = MINIMAL_PARAM + (hashMapSystemIndex.size() - 1);
        int nbObs = getNumberObservations();

        /* Setting the x to create matrices with the right size afterwards*/
        if (counter == 1 || initX.numRows() <= 7)
        {
            X = new SimpleMatrix(nbParam, 1);
            X.set(IDX_X,0, initX.get(0));
            X.set(IDX_Y,0, initX.get(1));
            X.set(IDX_Z,0, initX.get(2));
        }
        /*else if (initX.numRows() <= 5)
        {
            X = new SimpleMatrix(nbParam, 1);
            X.set(IDX_X,0, initX.get(0));
            X.set(IDX_Y,0, initX.get(1));
            X.set(IDX_Z,0, initX.get(2));
            X.set(IDX_C_DTR,0, initX.get(3));
        }*/
        else
        {
            X = new SimpleMatrix(nbParam, 1);
            X.set(IDX_X,0, initX.get(0));
            X.set(IDX_XDOT,0,initX.get(1));
            X.set(IDX_Y,0, initX.get(2));
            X.set(IDX_YDOT,0,initX.get(3));
            X.set(IDX_Z,0, initX.get(4));
            X.set(IDX_ZDOT,0, initX.get(5));
            X.set(IDX_C_DTR,0, initX.get(6));
            X.set(IDX_CLOCK_DRIFT,0, initX.get(7));
        }

        /*if (nbParam > 8 && hashMapSystemIndex.size() > 1 && initX.numRows() > 8)
        {
            X.set(IDX_CLOCK_DRIFT + 1,0,initX.get(8));
        }*/

        /*if (prevObs != nbObs)
        {
            X.set(IDX_C_DTR,0, 0);
            X.set(IDX_CLOCK_DRIFT,0, 0);
        }*/
        //prevObs = nbObs;

        /* Design Matrix*/
        SimpleMatrix A = new SimpleMatrix(nbObs, X.numRows());

        /* Measurements vector*/
        SimpleMatrix B = new SimpleMatrix(nbObs,1);

        /* Weighting matrix*/
        SimpleMatrix Q = SimpleMatrix.identity(nbObs);

        //sigma0_2 = 1.;

        double tow = 0;

        position = new Coordinates(X.get(IDX_X,0), X.get(IDX_Y,0), X.get(IDX_Z,0));
        //TropoCorrections tropoCorrections = new TropoCorrections(position);

        int i = 0;
        for(Map.Entry<String, GNSSObservation> entry : MapSatelliteObservations.entrySet())
        {

            Vector<Measurements> measurementsVector;

            satelliteObservation = entry.getValue();

            satellitePosition = satelliteObservation.getSatellitePosition();

            tow = satelliteObservation.getTrx();

            if(satellitePosition == null)
            {
                continue;
            }

            SimpleMatrix userCoordSM = position.getSimpleMatrix();
            SimpleMatrix satCoordSM = satellitePosition.getSatCoordinates().getSimpleMatrix();
            SimpleMatrix prevSatCoordSM = satellitePosition.getPrevSatCoordinates().getSimpleMatrix();

            double satX = satellitePosition.getSatCoordinates().getX();
            double satY = satellitePosition.getSatCoordinates().getY();
            double satZ = satellitePosition.getSatCoordinates().getZ();
            double satDt = satellitePosition.getDtSat();

            /* compute & apply tropspheric correction*/
            /*double tropoCorr = 0;
            if(processingOptions.isTropoEnabled())
            {
                tropoCorr = tropoCorrections.getSaastamoinenCorrection(satellitePosition.getSatElevation());
            }*/

            measurementsVector = selectMeasurements(satelliteObservation);

            double geometricDistance = Utils.distanceBetweenCoordinates(
                    position,
                    satellitePosition.getSatCoordinates());

            for(Measurements obs: measurementsVector)
            {
                /* Setting the weight*/
                double sigmaMeas = getWeightValue(obs.cn0);

                /*double dcb = 0.0;
                if(satellitePosition.getCodeBias() != null
                        && processingOptions.isDcbEnabled())
                {
                    dcb = satellitePosition.getCodeBias().getBiases().get(obs.codeFreq);
                }*/

                A.set(i, IDX_X, (X.get(IDX_X) - satX) / geometricDistance);
                A.set(i, IDX_Y, (X.get(IDX_Y) - satY) / geometricDistance);
                A.set(i, IDX_Z, (X.get(IDX_Z) - satZ) / geometricDistance);
                A.set(i, IDX_C_DTR, 1);

                // Checking if we have multiple system, and if satellite is part of the reference constellation
                if (hashMapSystemIndex.size() > 1
                        && hashMapSystemIndex.get(entry.getValue().getConstellation()) != IDX_C_DTR)
                {
                    int idxGTO = hashMapSystemIndex.get(entry.getValue().getConstellation());
                    A.set(i, idxGTO, 1);
                }

                double matValB;
                /*if (counter == 1 || initX.numRows() <= 7)
                {
                    matValB = obs.pseudo - geometricDistance + Constants.SPEED_OF_LIGHT  * satDt
                            - dcb - tropoCorr;
                }
                else
                {*/
                //double tmpCond1 = obs.pseudo - geometricDistance + Constants.SPEED_OF_LIGHT  * satDt
                        //- dcb - tropoCorr;

                /* Simple way to stabilize pseudorange residual measurements because they are not used in the computation
                 */
                double random = ThreadLocalRandom.current().nextDouble(0.1e-9, 0.15e-9);
                //double tmpCond2 = tmpCond1 + (random * Constants.SPEED_OF_LIGHT);
                //matValB = Math.abs(tmpCond1 - tmpCond2);
                matValB = Math.abs(random * Constants.SPEED_OF_LIGHT);
                //}

                //if (counter == 0 || (tmpCond > -8993.77374 && tmpCond < 8993.77374))
                //{
                Q.set(i,i, Math.pow(SIGMA_CODE_L1 * (sigmaMeas), 2));
                //}
                /*else
                {
                    matValB = 0;
                    Q.set(i,i,1);
                }*/
                //- (Tr0 + X.get(IDX_TROPO, 0) * Mwet)); X.get(IDX_C_DTR), X.get(idxGTO)
                    // + Constants.SPEED_OF_LIGHT  * satDt - X.get(idxGTO)
                //}
                //else
                //{
                    //double matValB = obs.pseudo - geometricDistance - X.get(IDX_C_DTR)
                            //+ Constants.SPEED_OF_LIGHT  * satDt - dcb - tropoCorr;
                    //- (Tr0 + X.get(IDX_TROPO, 0) * Mwet));

                    //B.set(i, 0, matValB);
                //}
                B.set(i, 0, matValB);
                i++;

                /* Using doppler measurements for velocity estimation*/
                if(obs.pseudoRate != 0.0)
                {
                    SimpleMatrix e = satCoordSM.minus(userCoordSM).scale(1/satCoordSM.minus(userCoordSM).normF());
                    SimpleMatrix prev_e = prevSatCoordSM.minus(userCoordSM).scale(1/prevSatCoordSM.minus(userCoordSM).normF());

                    SimpleMatrix delta_g = e.transpose().mult(userCoordSM).minus(prev_e.transpose().mult(userCoordSM));
                    SimpleMatrix delta_D = e.transpose().mult(satCoordSM).minus(prev_e.transpose().mult(prevSatCoordSM));

                    A.set(i, IDX_XDOT, -e.get(0,0));
                    A.set(i, IDX_YDOT, -e.get(1,0));
                    A.set(i, IDX_ZDOT, -e.get(2,0));
                    A.set(i, IDX_CLOCK_DRIFT, 1);

                    double matValBr = obs.pseudoRate - delta_D.get(0,0) + delta_g.get(0,0)
                            + e.get(0,0) * X.get(IDX_XDOT,0) + e.get(1,0) * X.get(IDX_YDOT,0)
                            + e.get(2,0) * X.get(IDX_ZDOT,0) - X.get(IDX_CLOCK_DRIFT);
                    //X.get(IDX_CLOCK_DRIFT);

                    B.set(i, 0, matValBr);
                    Q.set(i,i, Math.pow(SIGMA_PHASE * (sigmaMeas), 2));
                    i++;
                }
            }
        }

        if (counter == 1 || initX.numRows() <= 7)
        {
            initEKF();
        }
        else
        {
            initEKF_Res();
        }
        //B.print();
        ekf.computeSolution(A, B, Q);
        X = ekf.getX_meas();
        X_Pred = ekf.getxHat_pred();
        P_Pred = ekf.getP_pred();
        //computeDOP(A);

        position = new Coordinates(X.get(IDX_X,0),X.get(IDX_Y,0),X.get(IDX_Z,0),
                X.get(IDX_XDOT,0), X.get(IDX_YDOT,0), X.get(IDX_ZDOT,0), tow);

    }

    //----------------------------------------------------------------------------------------------

    /*/**
     * Computation of the Dilution of Precision for the current epoch.
     * @param A Design matrix
     */
    /*private void computeDOP(SimpleMatrix A)
    {
        /// Computing DOP
        SimpleMatrix DOP = A.transpose().mult(A).invert();

        // Geometric DOP
        gdop = Math.sqrt(DOP.get(IDX_X, IDX_X) + DOP.get(IDX_Y, IDX_Y) + DOP.get(IDX_Z, IDX_Z)
                + DOP.get(IDX_C_DTR, IDX_C_DTR));
        // Position DOP
        pdop = Math.sqrt(DOP.get(IDX_X, IDX_X) + DOP.get(IDX_Y, IDX_Y) + DOP.get(IDX_Z, IDX_Z));
        // Time DOP
        tdop = Math.sqrt(DOP.get(IDX_C_DTR, IDX_C_DTR));
    }*/

    //----------------------------------------------------------------------------------------------

    /**
     * Build pseudoranges vector from GNSSObservation object and processing options
     * @param satelliteObservation Observations
     * @return Vector of pseudoranges
     */
    private Vector<Measurements> selectMeasurements(GNSSObservation satelliteObservation)
    {
        Vector<Measurements> measurementsList = new Vector<>();

        Measurements meas;

        int codeL1;
        int codeL5;

        /* Checking the constellation*/
        switch (satelliteObservation.getConstellation())
        {
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
        }

        /* Selecting the right code for the measurement*/

        if(processingOptions.isIonofreeEnabled()
                && satelliteObservation.isL3Enabled())
        {
            int codeL3 = 0; // No DCB if iono-free (in theory...)

            //if(processingOptions.isSmoothingEnabled()
                    //&& satelliteObservation.isSmoothL3Enabled())
            //{
                meas = new Measurements(
                        satelliteObservation.getPseudorangeL3(),
                        satelliteObservation.getPseudoRateL3(),
                        satelliteObservation.getCn0L1(),
                        codeL3);
            //}
            /*else
            {
               meas = new Measurements(
                        satelliteObservation.getPseudorangeL3(),
                        0.0,
                        satelliteObservation.getCn0L1(),
                        codeL3);
            }*/

            measurementsList.add(meas);
        }
        else if(processingOptions.isDualFrequencyEnabled())
        {
            if(satelliteObservation.isL1Enabled())
            {
                //if(satelliteObservation.isSmoothL1Enabled())
                //{
                    meas = new Measurements(
                            satelliteObservation.getPseudorangeL1(),
                            satelliteObservation.getPseudoRateL1(),
                            satelliteObservation.getCn0L1(),
                            codeL1);
                //}
                /*else
                {
                    meas = new Measurements(
                            satelliteObservation.getPseudorangeL1(),
                            0.0,
                            satelliteObservation.getCn0L1(),
                            codeL1);
                }*/

                measurementsList.add(meas);
            }

            if(satelliteObservation.isL5Enabled())
            {
                //if(satelliteObservation.isSmoothL5Enabled())
                //{
                    meas = new Measurements(
                            satelliteObservation.getPseudorangeL5(),
                            satelliteObservation.getPseudoRateL5(),
                            satelliteObservation.getCn0L5(),
                            codeL5);
                //}
                /*else
                {
                    meas = new Measurements(
                            satelliteObservation.getPseudorangeL5(),
                            0.0,
                            satelliteObservation.getCn0L5(),
                            codeL5);
                }*/

                measurementsList.add(meas);
            }
        }
        else if(processingOptions.isMonoFrequencyEnabled())
        {
            if(satelliteObservation.isL1Enabled())
            {
                //if(satelliteObservation.isSmoothL1Enabled())
                //{
                    meas = new Measurements(
                            satelliteObservation.getPseudorangeL1(),
                            satelliteObservation.getPseudoRateL1(),
                            satelliteObservation.getCn0L1(),
                            codeL1);
                //}
                /*else
                {
                    meas = new Measurements(
                            satelliteObservation.getPseudorangeL1(),
                            0.0,
                            satelliteObservation.getCn0L1(),
                            codeL1);
                }*/
                measurementsList.add(meas);
            }
        }
        return measurementsList;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Initialize the Extended Kalman Filter matrices for the first time.
     *
     */
    private void initEKF()
    {
        //if(!dynamicMode)
        //{
            /*int nbParam = MINIMAL_PARAM + (hashMapSystemIndex.size() - 1);
            double sigmaDtr = Constants.SPEED_OF_LIGHT * 1e-3;

            SimpleMatrix F = SimpleMatrix.identity(nbParam);
            F.set(IDX_C_DTR, IDX_C_DTR, 0.0);

            SimpleMatrix Q = new SimpleMatrix(nbParam,nbParam);

            Q.set(IDX_C_DTR, IDX_C_DTR, Math.pow(sigmaDtr,2));

            // Doesn't matter that much, could be set to identity and still works
            SimpleMatrix P0 = new SimpleMatrix(nbParam,nbParam);
            P0.set(IDX_X,IDX_X, INITIAL_SIGMAPOS);
            P0.set(IDX_XDOT,IDX_XDOT, INITIAL_SIGMASPEED);
            P0.set(IDX_Y,IDX_Y, INITIAL_SIGMAPOS);
            P0.set(IDX_YDOT,IDX_YDOT, INITIAL_SIGMASPEED);
            P0.set(IDX_Z,IDX_Z, INITIAL_SIGMAPOS);
            P0.set(IDX_ZDOT,IDX_ZDOT, INITIAL_SIGMASPEED);
            P0.set(IDX_C_DTR,IDX_C_DTR, INITIAL_SIGMACLOCKBIAS);
            P0.set(IDX_CLOCK_DRIFT,IDX_CLOCK_DRIFT, INITIAL_SIGMACLOCKDRIFT);

            ekf = new ExtendedKalmanFilter(F, Q, X, P0);*/
        //}
        //else
        //{
            int nbParam = MINIMAL_PARAM + (hashMapSystemIndex.size() - 1);

            SimpleMatrix F = SimpleMatrix.identity(nbParam);
            F.set(IDX_X, IDX_XDOT, DELTA_T);
            F.set(IDX_Y, IDX_YDOT, DELTA_T);
            F.set(IDX_Z, IDX_ZDOT, DELTA_T);
            F.set(IDX_C_DTR, IDX_CLOCK_DRIFT, DELTA_T);

            SimpleMatrix Q = new SimpleMatrix(nbParam,nbParam);

            Q.set(IDX_X, IDX_X, Math.pow(S_xy, 2.) * Math.pow(DELTA_T,3) /3.);
            Q.set(IDX_XDOT, IDX_X, Math.pow(S_xy, 2.) * Math.pow(DELTA_T,2) / 2.);
            Q.set(IDX_X, IDX_XDOT, Q.get(IDX_XDOT, IDX_X)); // assure symmetry of matrix
            Q.set(IDX_XDOT, IDX_XDOT, Math.pow(S_xy, 2.) * DELTA_T);

            Q.set(IDX_Y, IDX_Y, Math.pow(S_xy, 2.) * Math.pow(DELTA_T,3) /3.);
            Q.set(IDX_YDOT, IDX_Y, Math.pow(S_xy, 2.) * Math.pow(DELTA_T, 2) /2.);
            Q.set(IDX_Y, IDX_YDOT, Q.get(IDX_YDOT, IDX_Y)); // symmetry
            Q.set(IDX_YDOT, IDX_YDOT, Math.pow(S_xy, 2.) * DELTA_T);

            Q.set(IDX_Z, IDX_Z, Math.pow(S_z, 2.) * Math.pow(DELTA_T, 3) /3.);
            Q.set(IDX_Z, IDX_ZDOT, Math.pow(S_z, 2.) * Math.pow(DELTA_T, 2) /2.);
            Q.set(IDX_ZDOT, IDX_Z, Q.get(IDX_Z, IDX_ZDOT)); // symmetry
            Q.set(IDX_ZDOT, IDX_ZDOT, Math.pow(S_z, 2.) * DELTA_T);

            // Tuning of the process noise matrix (Q)
            Q.set(IDX_C_DTR, IDX_C_DTR, S_f + S_g * Math.pow(DELTA_T,3) / 3.0);
            Q.set(IDX_C_DTR, IDX_CLOCK_DRIFT, S_g * Math.pow(DELTA_T,2) / 2.0);
            Q.set(IDX_CLOCK_DRIFT, IDX_C_DTR, S_g * Math.pow(DELTA_T,2) / 2.0);
            Q.set(IDX_CLOCK_DRIFT, IDX_CLOCK_DRIFT, S_g * DELTA_T);

            SimpleMatrix P0 = new SimpleMatrix(nbParam,nbParam);
        // TODO: 08/12/2022 caution, squares missed (pow(...,2) for all P0 component
            P0.set(IDX_X,IDX_X, INITIAL_SIGMAPOS);
            P0.set(IDX_XDOT,IDX_XDOT, INITIAL_SIGMASPEED);
            P0.set(IDX_Y,IDX_Y, INITIAL_SIGMAPOS);
            P0.set(IDX_YDOT,IDX_YDOT, INITIAL_SIGMASPEED);
            P0.set(IDX_Z,IDX_Z, INITIAL_SIGMAPOS);
            P0.set(IDX_ZDOT,IDX_ZDOT, INITIAL_SIGMASPEED);
            P0.set(IDX_C_DTR,IDX_C_DTR, INITIAL_SIGMACLOCKBIAS);
            P0.set(IDX_CLOCK_DRIFT,IDX_CLOCK_DRIFT, INITIAL_SIGMACLOCKDRIFT);
        //}

        /*int nbParam = MINIMAL_PARAM + (hashMapSystemIndex.size() - 1);

        SimpleMatrix F = SimpleMatrix.identity(nbParam);
        F.set(IDX_X, IDX_X+3, DELTA_T);
        F.set(IDX_Y, IDX_Y+3, DELTA_T);
        F.set(IDX_Z, IDX_Z+3, DELTA_T);


        SimpleMatrix Q = new SimpleMatrix(nbParam,nbParam);

        Q.set(IDX_X, IDX_X, 0.7);
        Q.set(IDX_XDOT, IDX_XDOT, 0.2);

        Q.set(IDX_Y, IDX_Y, 0.7);
        Q.set(IDX_YDOT, IDX_YDOT, 0.2);

        Q.set(IDX_Z, IDX_Z, 0.7);
        Q.set(IDX_ZDOT, IDX_ZDOT, 0.2);

        // Tuning of the process noise matrix (Q)
        Q.set(IDX_C_DTR, IDX_C_DTR, 1);
        Q.set(IDX_CLOCK_DRIFT, IDX_CLOCK_DRIFT, 1);

        SimpleMatrix P0 = new SimpleMatrix(nbParam,nbParam);
        P0.set(IDX_X,IDX_X,1);
        P0.set(IDX_XDOT,IDX_XDOT, 0.5);
        P0.set(IDX_Y,IDX_Y, 1);
        P0.set(IDX_YDOT,IDX_YDOT, 0.5);
        P0.set(IDX_Z,IDX_Z, 1);
        P0.set(IDX_ZDOT,IDX_ZDOT, 0.5);
        P0.set(IDX_C_DTR,IDX_C_DTR, 100);
        P0.set(IDX_CLOCK_DRIFT,IDX_CLOCK_DRIFT, 10);*/

        ekf = new ExtendedKalmanFilter(F, Q, X, P0);
    }

    /**
     * Initialize the Extended Kalman Filter matrices for the rest of the duration.
     *
     */

    public void initEKF_Res()
    {
        int nbParam = MINIMAL_PARAM + (hashMapSystemIndex.size() - 1);

        SimpleMatrix F = SimpleMatrix.identity(nbParam);
        F.set(IDX_X, IDX_XDOT, DELTA_T);
        F.set(IDX_Y, IDX_YDOT, DELTA_T);
        F.set(IDX_Z, IDX_ZDOT, DELTA_T);
        F.set(IDX_C_DTR, IDX_CLOCK_DRIFT, DELTA_T);

        SimpleMatrix Q = new SimpleMatrix(nbParam,nbParam);

        Q.set(IDX_X, IDX_X, Math.pow(S_xy, 2.) * Math.pow(DELTA_T,3) /3.);
        Q.set(IDX_XDOT, IDX_X, Math.pow(S_xy, 2.) * Math.pow(DELTA_T,2) / 2.);
        Q.set(IDX_X, IDX_XDOT, Q.get(IDX_XDOT, IDX_X)); // assure symmetry of matrix
        Q.set(IDX_XDOT, IDX_XDOT, Math.pow(S_xy, 2.) * DELTA_T);

        Q.set(IDX_Y, IDX_Y, Math.pow(S_xy, 2.) * Math.pow(DELTA_T,3) /3.);
        Q.set(IDX_YDOT, IDX_Y, Math.pow(S_xy, 2.) * Math.pow(DELTA_T, 2) /2.);
        Q.set(IDX_Y, IDX_YDOT, Q.get(IDX_YDOT, IDX_Y)); // symmetry
        Q.set(IDX_YDOT, IDX_YDOT, Math.pow(S_xy, 2.) * DELTA_T);

        Q.set(IDX_Z, IDX_Z, Math.pow(S_z, 2.) * Math.pow(DELTA_T, 3) /3.);
        Q.set(IDX_Z, IDX_ZDOT, Math.pow(S_z, 2.) * Math.pow(DELTA_T, 2) /2.);
        Q.set(IDX_ZDOT, IDX_Z, Q.get(IDX_Z, IDX_ZDOT)); // symmetry
        Q.set(IDX_ZDOT, IDX_ZDOT, Math.pow(S_z, 2.) * DELTA_T);

        // Tuning of the process noise matrix (Q)
        Q.set(IDX_C_DTR, IDX_C_DTR, S_f + S_g * Math.pow(DELTA_T,3) / 3.0);
        Q.set(IDX_C_DTR, IDX_CLOCK_DRIFT, S_g * Math.pow(DELTA_T,2) / 2.0);
        Q.set(IDX_CLOCK_DRIFT, IDX_C_DTR, S_g * Math.pow(DELTA_T,2) / 2.0);
        Q.set(IDX_CLOCK_DRIFT, IDX_CLOCK_DRIFT, S_g * DELTA_T);

        if (X.numRows() != X_Pred.numRows())
        {
            SimpleMatrix X_temp = new SimpleMatrix(X.numRows(),1);
            X_temp.set(0,0,X_Pred.get(0,0));
            X_temp.set(1,0,X_Pred.get(1,0));
            X_temp.set(2,0,X_Pred.get(2,0));
            X_temp.set(3,0,X_Pred.get(3,0));
            X_temp.set(4,0,X_Pred.get(4,0));
            X_temp.set(5,0,X_Pred.get(5,0));
            X_temp.set(6,0,X_Pred.get(6,0));
            X_temp.set(7,0,X_Pred.get(7,0));

            SimpleMatrix P_temp = new SimpleMatrix(X.numRows(),X.numRows());
            P_temp.set(0,0,P_Pred.get(0,0));
            P_temp.set(1,1,P_Pred.get(1,1));
            P_temp.set(2,2,P_Pred.get(2,2));
            P_temp.set(3,3,P_Pred.get(3,3));
            P_temp.set(4,4,P_Pred.get(4,4));
            P_temp.set(5,5,P_Pred.get(5,5));
            P_temp.set(6,6,P_Pred.get(6,6));
            P_temp.set(7,7,P_Pred.get(7,7));

            ekf = new ExtendedKalmanFilter(F, Q, X_temp, P_temp);
        }
        else
        {
            ekf = new ExtendedKalmanFilter(F, Q, X_Pred, P_Pred);
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
            //Reject Beidou Measurements
            /*if (entry.getValue().getConstellation() == 5)
            {
                continue;
            }*/

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
        for(Map.Entry<String, GNSSObservation> entry : MapSatelliteObservations.entrySet()){
            satelliteObservation = entry.getValue();

            if(processingOptions.isIonofreeEnabled())
            {
                if(satelliteObservation.getPseudorangeL3() > 0.0)
                {
                    nbObs ++;

                    if(satelliteObservation.getPseudoRateL3() != 0.0)
                    {
                        nbObs ++;
                    }
                }
            }
            else if(processingOptions.isDualFrequencyEnabled())
            {
                if(satelliteObservation.getPseudorangeL1() > 0.0)
                {
                    nbObs ++;
                    if(satelliteObservation.getPseudoRateL1() != 0.0)
                    {
                        nbObs++;
                    }
                }
                if(satelliteObservation.getPseudorangeL5() > 0.0)
                {
                    nbObs ++;
                    if(satelliteObservation.getPseudoRateL5() != 0.0)
                    {
                        nbObs ++;
                    }
                }
            }
            else if(processingOptions.isMonoFrequencyEnabled())
            {
                if(satelliteObservation.getPseudorangeL1() > 0.0)
                {
                    nbObs ++;
                    if(satelliteObservation.getPseudoRateL1() != 0.0)
                    {
                        nbObs ++;
                    }
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

    //----------------------------------------------------------------------------------------------

    /**
     * Structure for easier handling of measurements in computations.
     */
    class Measurements
    {
        double pseudo;
        double pseudoRate;
        double cn0;
        int codeFreq;

        public Measurements(double pseudo, double pseudoRate, double cn0, int codeFreq)
        {
            this.pseudo = pseudo;
            this.pseudoRate = pseudoRate;
            this.cn0 = cn0;
            this.codeFreq = codeFreq;
        }
    }

    //----------------------------------------------------------------------------------------------

    /*public double getGdop() {
        return gdop;
    }*/

    public double getGdop() {
        return 0.0;
    }

    /*public double getPdop() {
        return pdop;
    }

    public double getTdop() {
        return tdop;
    }*/

    public SimpleMatrix getX() {
        return X;
    }

    public SimpleMatrix getX_Pred(){
        return X_Pred;
    }

    public SimpleMatrix getP_Pred(){
        return P_Pred;
    }

    /*public int getPrevObs()
    {return prevObs;}*/
}
