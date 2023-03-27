///=================================================================================================
// Class SPP
//      Author :  Jose Gilberto RESENDIZ FONSECA
// Modified by :  Antoine GRENIER - 2019/09/06
//        Date :  2019/09/06
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
import org.gogpsproject.ephemeris.GNSSEphemeris;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import fr.ifsttar.geoloc.geoloclib.Coordinates;
import fr.ifsttar.geoloc.geoloclib.Options;
import fr.ifsttar.geoloc.geoloclib.Utils;
import fr.ifsttar.geoloc.geoloclib.satellites.GNSSObservation;
import fr.ifsttar.geoloc.geoloclib.satellites.SatellitePositionGNSS;

/**
 * Class GNSSPosition
 * Compute the position of receiver
 */
public class SPP extends PVT
{
    private Map<String, GNSSObservation> satelliteObservations;

    private Coordinates position;

    private Coordinates approxPosition;
    private SatellitePositionGNSS satellitePosition;
    private GNSSObservation satelliteObservation;

    HashMap<Integer, Integer> hashMapSystemIndex;

    private Options processingOptions;
    private boolean firstExecution;

    private final double DELTA_T = 1.0;     // [s], time between two measurements

    private final int MINIMAL_PARAM = 4;
    private final int IDX_X = 0;
    private final int IDX_Y = 1;
    private final int IDX_Z = 2;
    private final int IDX_C_DTR = 3;

    private double gdop;
    private double pdop;
    private double tdop;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor of SPP
     * @param processingOptions Processing options of the computations
     * @param _approxPosition Approx position of the receiver
     */
    public SPP(Options processingOptions, Coordinates _approxPosition)
    {
        approxPosition = _approxPosition;

        this.processingOptions = processingOptions;
        firstExecution = true;

    }

    //----------------------------------------------------------------------------------------------

    /**
     * Refresh with new observations.
     * @param _satelliteObservations Satellite observations
     */
    public void refreshSatelliteObservations(HashMap<String, GNSSObservation> _satelliteObservations)
    {
        this.satelliteObservations = _satelliteObservations;
        setCurrentSystems();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Least square method to compute position
     * @return the coordinates of receiver
     */
    public void computePosition(){

        Log.d("Message: ", "Entry into the position estimation algorithm");
        int nbObs = getNumberObservations();

        // Setting the x to create matrices with the right size afterwards
        int nbParam = MINIMAL_PARAM + (hashMapSystemIndex.size() - 1);

        if(firstExecution)
        {
            // Paremeter vector
            X = new SimpleMatrix(nbParam, 1);
            X.set(IDX_X,0, approxPosition.getX());
            X.set(IDX_Y,0, approxPosition.getY());
            X.set(IDX_Z,0, approxPosition.getZ());
        }

        // Design Matrix
        SimpleMatrix A = new SimpleMatrix(nbObs, X.numRows());

        // Misclosure vector
        SimpleMatrix B = new SimpleMatrix(nbObs,1);

        // Weighting matrix
        SimpleMatrix Q = SimpleMatrix.identity(nbObs);

        //sigma0_2 = 1.;

        double tow = 0;
        int loopcount = 0;
        do
        {
            position = new Coordinates(X.get(IDX_X,0), X.get(IDX_Y,0), X.get(IDX_Z,0));

            int i = 0;
            for(Map.Entry<String, GNSSObservation> entry : satelliteObservations.entrySet())
            {
                Vector<Double> pseudoranges;

                satelliteObservation = entry.getValue();
                satellitePosition = satelliteObservation.getSatellitePosition();

                tow = satelliteObservation.getTrx();

                if(satellitePosition == null)
                {
                    return;
                }

                double satX = satellitePosition.getSatCoordinates().getX();
                double satY = satellitePosition.getSatCoordinates().getY();
                double satZ = satellitePosition.getSatCoordinates().getZ();
                double satDt = satellitePosition.getDtSat();

                pseudoranges = selectMeasurements(satelliteObservation);

                double geometricDistance = Utils.distanceBetweenCoordinates(
                        position,
                        satellitePosition.getSatCoordinates());

                int j = 0;
                for(double pseudorange: pseudoranges)
                {
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
                        B.set(i, 0,
                                pseudorange
                                        - geometricDistance
                                        - X.get(IDX_C_DTR)
                                        + (Constants.SPEED_OF_LIGHT * satDt)
                                        - X.get(idxGTO)); // System Time Offset
                    }
                    else
                    {
                        B.set(i, 0,
                                pseudorange
                                        - geometricDistance
                                        - X.get(IDX_C_DTR)
                                        + (Constants.SPEED_OF_LIGHT * satDt));
                    }

                    // Setting the weight
                    double sigmaMeas = getWeightValue(getCn0Measurement(pseudoranges, j), satellitePosition.getSatElevation(), j);
                    double wt = 1 / sigmaMeas;
                    Q.set(i,i,wt);

                    i++;
                    j++;
                }

            }
            computeLSE(A, B, Q);
            loopcount ++;

        }while((loopcount < 25) && (dX.normF() > 10e-2));

        position = new Coordinates(X.get(0,0),X.get(1,0),X.get(2,0), tow);

    }

    //----------------------------------------------------------------------------------------------

    /**
     * Computation of the Dilution of Precision for the current epoch.
     * @param A Design matrix
     */
    private void computeDOP(SimpleMatrix A)
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
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Build pseudo-range vector from GNSSObservation object and processing options
     * @param satelliteObservation Observations
     * @return Vector of pseudo-ranges
     */
    private Vector<Double> selectMeasurements(GNSSObservation satelliteObservation)
    {
        Vector<Double> pseudoranges = new Vector<>();

        if(processingOptions.isMonoFrequencyEnabled())
        {
            if(satelliteObservation.isL1Enabled())
            {
                //if(satelliteObservation.isSmoothL1Enabled())
                //{
                    //pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                //}
                //else
                //{
                    pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                //}
            }
        }
        else if(processingOptions.isDualFrequencyEnabled())
        {
                if (processingOptions.isIonoCorrEnabled())
                {
                    if (satelliteObservation.isL1Enabled() && satelliteObservation.isL5Enabled())
                    {
                        //if(processingOptions.isSmoothingEnabled())
                        //{
                            //pseudoranges.add(satelliteObservation.getSmoothPseudoL3());
                        //}
                        //else
                        //{
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL5());
                        //}
                    }
                    else if (satelliteObservation.isL1Enabled() && !satelliteObservation.isL5Enabled())
                    {
                        //if(processingOptions.isSmoothingEnabled())
                        //{
                            //pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                        //}
                        //else
                        //{
                            pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                        //}
                    }
                    else if (!satelliteObservation.isL1Enabled() && satelliteObservation.isL5Enabled())
                    {
                        //if(processingOptions.isSmoothingEnabled())
                        //{
                            //pseudoranges.add(satelliteObservation.getSmoothPseudoL5());
                       // }
                       // else
                        //{
                            pseudoranges.add(satelliteObservation.getSmoothPseudoL5());
                        //}
                    }
                }
                else
                {
                    if(satelliteObservation.isL1Enabled() && satelliteObservation.isL5Enabled())
                    {
                        //if(processingOptions.isSmoothingEnabled())
                        //{
                            //pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                            //pseudoranges.add(satelliteObservation.getSmoothPseudoL5());
                       // }
                        //else
                        //{
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL5());
                        //}
                    }
                    else if(satelliteObservation.isL1Enabled() && !satelliteObservation.isL5Enabled())
                    {
                        //if(processingOptions.isSmoothingEnabled())
                        //{
                           // pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                        //}
                       // else
                        //{
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                        //}
                    }
                    else if (!satelliteObservation.isL1Enabled() && satelliteObservation.isL5Enabled())
                    {
                       // if(processingOptions.isSmoothingEnabled())
                        //{
                            //pseudoranges.add(satelliteObservation.getSmoothPseudoL5());
                        //}
                        //else
                        //{
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL5());
                        //}
                    }
                }
            /*else
            {
                if(satelliteObservation.isL1Enabled() && satelliteObservation.isL5Enabled() && !satelliteObservation.isMultipathPresent())
                {
                    if(satelliteObservation.isSmoothL1Enabled() && satelliteObservation.isSmoothL5Enabled())
                    {
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL5());
                    }
                    else if (satelliteObservation.isSmoothL1Enabled() && !satelliteObservation.isSmoothL5Enabled())
                    {
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                    }
                    else if (!satelliteObservation.isSmoothL1Enabled() && satelliteObservation.isSmoothL5Enabled())
                    {
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL5());
                    }
                    else
                    {
                        pseudoranges.add(satelliteObservation.getPseudorangeL1());
                        pseudoranges.add(satelliteObservation.getPseudorangeL5());
                    }
                }
                else if(satelliteObservation.isL1Enabled() && !satelliteObservation.isL5Enabled() && !satelliteObservation.isMultipathPresent())
                {
                    if(satelliteObservation.isSmoothL1Enabled())
                    {
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                    }
                    else
                    {
                        pseudoranges.add(satelliteObservation.getPseudorangeL1());
                    }
                }
                else if (!satelliteObservation.isL1Enabled() && satelliteObservation.isL5Enabled() && !satelliteObservation.isMultipathPresent())
                {
                    if(satelliteObservation.isSmoothL5Enabled())
                    {
                        pseudoranges.add(satelliteObservation.getSmoothPseudoL5());
                    }
                    else
                    {
                        pseudoranges.add(satelliteObservation.getPseudorangeL5());
                    }
                }
                else
                {
                    Log.d("Measurement Selection: ","Invalid");
                }
            }
            /*if(satelliteObservation.isL1Enabled())
            {
                if(satelliteObservation.isSmoothL1Enabled())
                {
                    pseudoranges.add(satelliteObservation.getSmoothPseudoL1());
                }
                else
                {
                    pseudoranges.add(satelliteObservation.getPseudorangeL1());
                }
            }

            if(satelliteObservation.isL5Enabled())
            {
                if(satelliteObservation.isSmoothL5Enabled())
                {
                    pseudoranges.add(satelliteObservation.getSmoothPseudoL5());
                }
                else
                {
                    pseudoranges.add(satelliteObservation.getPseudorangeL5());
                }
            }*/
        }
        else if(processingOptions.isIonofreeEnabled()
                && satelliteObservation.isL3Enabled())
        {
            //if(processingOptions.isSmoothingEnabled()
                    //&& satelliteObservation.isSmoothL3Enabled())
            //{
               // pseudoranges.add(satelliteObservation.getSmoothPseudoL3());
           // }
            //else
            //{
                pseudoranges.add(satelliteObservation.getSmoothPseudoL3());
            //}
        }
        return pseudoranges;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Return the C/n0 value of measurement
     * @param pseudoranges vector of pseudoranges
     * @param j Index of measurement
     * @return C/n0 value
     */
    private double getCn0Measurement(Vector<Double> pseudoranges, int j)
    {
        double cn0 = 0.0;

        if(pseudoranges.size() > 1)
        {
            if(j == 0)
            {
                cn0 = satelliteObservation.getCn0L1();
            }
            else if (j == 1)
            {
                cn0 = satelliteObservation.getCn0L5();
            }
        }
        else
        {
            if(satelliteObservation.isL5Enabled()
                    && !satelliteObservation.isL3Enabled()
                    && processingOptions.isDualFrequencyEnabled())
            {
                cn0 = satelliteObservation.getCn0L5();
            }
            else
            {
                cn0 = satelliteObservation.getCn0L1();
            }
        }

        return cn0;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * @return Computed position.
     */
    public Coordinates getPosition()
    {
        // Wrong computations
        if(position.getX() == 0)
        {
            return null;
        }
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

        for (HashMap.Entry<String, GNSSObservation> entry : satelliteObservations.entrySet())
        {
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
     * @return
     */
    public int getNumberObservations()
    {
        int nbObs = 0;
        for(Map.Entry<String, GNSSObservation> entry : satelliteObservations.entrySet()){
            satelliteObservation = entry.getValue();
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
     * Compute the weight value of the obs, using the elevation of the satellite and C/N0 of the signal.
     * Formula: sigma = sigma_measurement / sin²(elevation)
     * @return sigma of measurement
     */
    private double getWeightValue(double cn0,double elev, int sig_type)
    {

        double sigma;

        /*double a = 10;
        double b = Math.pow(150, 2);

        sigma = (a + b * Math.pow(10, (-0.1 * cn0)));*/
         //Original Code
          /*double a = 10;
          double b = Math.pow(150, 2);
          sigma = a + b * Math.pow(10, (-0.1 * cn0));*/


        //LOS/NLOS Factor
        //double k = 2;
        //Elevation angle of Satellites
        double elev_r = Math.toRadians(elev);
        //Elevation angle factor
        double elev_fac = (1 - Math.cos(2 * elev_r)) / 2;
        // Factor C_1 & C_5 depends on Carrier Tracking Loop Bandwidth & conversion term from cycles to m.
        double C_1 = 1.61e4;
        //double C_5 = 8.050e3;
        // L1 & L5 signal wavelength in m
        //double L1_w = (299792458.0)/1575.420e6;
        //double L5_w = (299792458.0)/1176.450e6;

        //sigma = k * (Math.pow(10, (-0.1 * cn0)) / elev_fac);
        // Sigma e model considering elevation of satellites
        if (sig_type == 0)
        {
            sigma = C_1 * Math.pow(10, (-0.1 * cn0));
            //sigma = (Math.pow(L1_w,2) * bw_cl * 1e3) * Math.pow(10, (-0.1 * cn0)) / elev_fac;
        }
        else if (sig_type == 1)
        {
            sigma = 1 * Math.pow(10, (-0.1 * cn0))/elev_fac;
            //sigma = (Math.pow(L5_w,2) * bw_cl * 1e3) * Math.pow(10, (-0.1 * cn0)) / elev_fac;
        }
        else
        {
            sigma = 1.0;

        }

        //Log.d("Noise variance: ", String.valueOf(sigma));

        return sigma;
    }

    //----------------------------------------------------------------------------------------------

    public SimpleMatrix getX()
    {
        return X;
    }


    //----------------------------------------------------------------------------------------------


    public double getGdop() {
        return gdop;
    }

    public double getPdop() {
        return pdop;
    }

    public double getTdop() {
        return tdop;
    }

//    /**
//     * Testing of the computations, useful to replay data sets and test different options.
//     * @param args args
//     * @throws IOException except
//     */
//    public static void main(String[] args) throws IOException
//    {
//        String line;
//        BufferedReader br;
//        //String obsFilename = "./MyData/16-07-2019_12-58-12_pseudo.txt";
//        //String satFilename = "./MyData/16-07-2019_12-58-12_satpos.txt";
//        //String ephFilename = "./MyData/16-07-2019_12-58-12_ephem.txt";
//
//        String obsFilename = "./MyData/22-07-2019_10-31-07_pseudo.txt";
//        String ephFilename = "./MyData/22-07-2019_10-31-07_ephem.txt";
//
//        //String obsFilename = "./MyData/22-07-2019_11-32-36_pseudo.txt";
//        //String ephFilename = "./MyData/22-07-2019_11-32-36_ephem.txt";
//
//        Coordinates approxUserCoord = new Coordinates(4343420.0, -124910.0, 4653460.0);
//
//        double cutoff = 30.0; //cn0 db
//
//        Options options = new Options();
//        Vector<Integer> systemEnabled = new Vector<Integer>();
//        systemEnabled.add(GnssStatus.CONSTELLATION_GPS);
//        systemEnabled.add(GnssStatus.CONSTELLATION_GALILEO);
//        options.setSystemsEnabled(systemEnabled);
//        options.setDualFrequencyEnabled(true);
//        options.setMonoFrequencyEnabled(false);
//        options.setSppEnabled(true);
//        options.setSmoothingEnabled(false);
//        options.setStreamsEnabled(true);
//        options.setStreamsCorrectionEnabled(false);
//
//        // Reading obs
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
//                    //System.out.println(satelliteObservations.size());
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
//
//            gnssObservation.setTrx(Double.parseDouble(str[0]) * 1e9);
//            gnssObservation.setConstellation(Integer.parseInt(str[1]));
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
//            if(gnssObservation.getSmoothPseudoL3() > 0)
//            {
//                gnssObservation.setSmoothL3Enabled(true);
//            }
//            if(gnssObservation.getSmoothPseudoL1() > 0)
//            {
//                gnssObservation.setSmoothL1Enabled(true);
//            }
//            if(gnssObservation.getSmoothPseudoL5() > 0)
//            {
//                gnssObservation.setSmoothL5Enabled(true);
//            }
//
//            String key = Utils.getFormattedSatIndex(gnssObservation.getConstellation(), gnssObservation.getId());
//            if(gnssObservation.getCn0L1() > cutoff && gnssObservation.isL1Enabled())
//            {
//                satelliteObservations.put(key, gnssObservation);
//            }
//            else if (gnssObservation.getCn0L5() > cutoff && gnssObservation.isL5Enabled())
//            {
//                satelliteObservations.put(key, gnssObservation);
//            }
//
//            prev = Double.parseDouble(str[0]);
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
//        HashMap<String, GNSSEphemeris> satEphem = new LinkedHashMap<>();
//        while ((line = br.readLine()) != null)
//        {
//            String[] str = line.split(",");
//
//            GNSSEphemeris eph = new GNSSEphemeris();
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
//            satEphem.put(key, eph);
//        }
//
//        GNSSPositioning gnssPositioning = new GNSSPositioning();
//        gnssPositioning.setApproxPosition(approxUserCoord);
//        for(int i=0; i<3500; i++)
//        {
//            gnssPositioning.refreshOptions(options);
//            gnssPositioning.setGnssEphemerisAllSats(satEphem);
//            gnssPositioning.setGnssObservationAllSats(epochs.elementAt(i));
//            gnssPositioning.addSatPosToObservations();
//
//            Coordinates userCoord = gnssPositioning.computeUserPosition();
//
//            //System.out.println("" + gnssPositioning.getSpp().getNumberObservations());
//            System.out.println("" + userCoord.getTow()/1e9 + "," + userCoord.getLatLngAlt());
//        }
//
//        return;
//    }

}
