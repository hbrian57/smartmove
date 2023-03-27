///=================================================================================================
// Class PPP
//      Author :  Antoine GRENIER
//        Date :  2019/09/06
///=================================================================================================
// /!\ CAUTION /!\ This class is in an alpha version and is not working yet.
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

import org.ejml.data.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;
import org.gogpsproject.Constants;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

import fr.ifsttar.geoloc.geoloclib.Coordinates;
import fr.ifsttar.geoloc.geoloclib.Utils;
import fr.ifsttar.geoloc.geoloclib.satellites.GNSSObservation;
import fr.ifsttar.geoloc.geoloclib.satellites.SatellitePositionGNSS;

/**
 * Class PPP
 * Compute the receiver position using a Precise Point Positioning algorithm.
 *
 * Reference [ESA, GNSS Data Processing, Volume 1: Fundamentals and Algorithms]
 */
public class PPP
{
    //LSE matrices
    private SimpleMatrix A;
    private SimpleMatrix B;
    private SimpleMatrix W;
    private SimpleMatrix Q;
    private SimpleMatrix X;
    private SimpleMatrix dX;
    private SimpleMatrix C;
    private SimpleMatrix N;

    private SimpleMatrix X0;

    private SimpleMatrix prevN;
    private SimpleMatrix prevC;
    private SimpleMatrix prevdX;

    private SimpleMatrix prevPinv;
    private SimpleMatrix prevX;

    SimpleMatrix phaseMeasurements;

    private HashMap<Integer, Integer> systemsIndex;
    private Vector<Integer> systemsEnabled;
    private int referenceSystem;

    private HashMap<String, Integer> satIndex;

    private int nbSat;
    private int nbParam;
    private int nbBasicParam = 5;
    private int nbEpoch = 0;

    private int idxX = 0;
    private int idxY = 1;
    private int idxZ = 2;
    private int idxCdt = 3;
    private int idxdTrop = 4;

    // Constants
    private static final double STOPPING_LSE_CRITERIA  = 10e-5;
    private static final double SIGMA_POSITION = 5;             // ~5m
    private static final double SIGMA_AMBIGUITY = 10e4;         // ~10 000m
    private static final double SIGMA_CODE = 2;                 // ~2m
    private static final double SIGMA_PHASE = 0.01;             // ~1cm
    private static final double SIGMA_CDT = 3e3;                // ~1ms = 300 000 m
    private static final double SIGMA_DTROP = Math.sqrt(0.25);  // ~0.25m²
    private static final double DELTA_SIGMA_DTROP = 2.78e-6;    // ~0.01 m/h
    private static final double DELTA_EPOCH = 1.0;              // ~1s
    private static final double SIGMA_GGTO = 1e3;

    // Computation data, satellites positions and observations
    HashMap<SatellitePositionGNSS, GNSSObservation> satPosObs;

    ExtendedKalmanFilter kalmanFilter;

    private Coordinates approxPos;

    private Set<String> presentSat;
    private Set<String> newSat;

//--------------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * @param satelliteObservations
     * @param
     */
    public PPP(HashMap<SatellitePositionGNSS, GNSSObservation> satelliteObservations, Coordinates approxPos)
    {
        nbSat = 0;
        nbParam = 4;  //dx, dy, dz, cdt

        //nbParam += 1;     // Tropospheric correction, wet part

        satPosObs = satelliteObservations;
        systemsEnabled = new Vector<>();
        satIndex = new HashMap<>();

        // Looking into the observations content, in order to set up the matrices sizes later.
        for(HashMap.Entry<SatellitePositionGNSS, GNSSObservation> entry : satelliteObservations.entrySet())
        {
            if(!systemsEnabled.contains(entry.getValue().getConstellation()))
            {
                systemsEnabled.add(entry.getValue().getConstellation());
            }

            String key = Utils.getFormattedSatIndex(entry.getValue().getConstellation(), entry.getValue().getId());
            if(!satIndex.containsKey(key))
            {
                satIndex.put(key, nbSat);
                nbSat ++;
            }
        }

        setTimeOffsetParamIndex();
        nbParam += (systemsEnabled.size() - 1); // GPS to Galileo Time Offset, etc ...

        nbParam += nbSat; // Ambiguities, one per satellite

        this.approxPos = approxPos;

        phaseMeasurements = new SimpleMatrix(100, satPosObs.size());
    }

    /**
     * Compute the user's position through least square in a PPP algorithm.
     * @return The user's precises coordinates.
     */
    public Coordinates computeUserPositionStatic(HashMap<SatellitePositionGNSS, GNSSObservation> satelliteObservations)
    {
        TropoCorrections tropoCorrections = new TropoCorrections(approxPos);
        satPosObs = satelliteObservations;
        presentSat = new HashSet<>();
        newSat = new HashSet<>();

        // Init the matrices for LSE
        initMatrices();

        int loopCount = 0;
        do {
            int i = 0;
            for (HashMap.Entry<SatellitePositionGNSS, GNSSObservation> entry : satPosObs.entrySet()) {

                SatellitePositionGNSS currentSatPos = entry.getKey();
                GNSSObservation currentSatObs = entry.getValue();
                approxPos = new Coordinates(X.get(0, 0), X.get(1, 0), X.get(2, 0));
                Coordinates satPosCoord = currentSatPos.getSatCoordinates();

                double p = getGeometricDistance(approxPos, satPosCoord);

                // Tropospheric model computations
                double Mwet = tropoCorrections.getMwet(currentSatPos);
                double Tr0 = tropoCorrections.getTr0(currentSatPos);

                //Log.i("PPP", "Mwet: " + Mwet);
                //Log.i("PPP", "Tr0: " + Tr0);

                String key = Utils.getFormattedSatIndex(currentSatObs.getConstellation(), currentSatObs.getId());
                int idxSat = satIndex.get(key);

                // Pseudorange measurements
                A.set(i, 0, (X.get(0, 0) - satPosCoord.getX()) / p);
                A.set(i, 1, (X.get(1, 0) - satPosCoord.getY()) / p);
                A.set(i, 2, (X.get(2, 0) - satPosCoord.getZ()) / p);
                A.set(i, 3, 1);
                //A.set(i, 4, Mwet);

                // Phase measurements
                A.set(i + 1, 0, (X.get(0, 0) - satPosCoord.getX()) / p);
                A.set(i + 1, 1, (X.get(1, 0) - satPosCoord.getY()) / p);
                A.set(i + 1, 2, (X.get(2, 0) - satPosCoord.getZ()) / p);
                A.set(i + 1, 3, 1);
                //A.set(i + 1, 4, Mwet);

                //System.out.println(key + ", " + idxSat);

                A.set(i + 1, nbBasicParam + idxSat, 1);
                //phaseMeasurements.set(nbEpoch, idxSat, currentSatObs.getPhaseL3());

                //X.set(5 + idxSat, 0, -2.4e7);

                if (systemsIndex.containsKey(currentSatObs.getConstellation()))
                {
                    int idxGTO = systemsIndex.get(currentSatObs.getConstellation());

                    A.set(i, idxGTO, 1);
                    A.set(i + 1, idxGTO, 1);
                    B.set(i, 0,
                            currentSatObs.getPseudorangeL3()
                                    - p
                                    + Constants.SPEED_OF_LIGHT * currentSatPos.getDtSat()
                                    - X.get(3, 0)             // c*dtr
                                    - X.get(idxGTO, 0));             // c*GGTO
                                    //- Tr0
                                    //- X.get(idxdTrop, 0) * Mwet);  // Wet part

                    B.set(i + 1, 0,
                            currentSatObs.getPhaseL3()*Constants.WL3
                                    - p
                                    + Constants.SPEED_OF_LIGHT * currentSatPos.getDtSat()
                                    - X.get(3, 0)            // c*dtr
                                    - X.get(idxGTO, 0)               // c*GGTO
                                    - X.get(nbBasicParam + idxSat));               // N*Lambda
                                    //- Tr0
                                    //- X.get(idxdTrop, 0) * Mwet);// Wet part

                }
                else
                {
                    B.set(i, 0,
                            currentSatObs.getPseudorangeL3()
                                    - p
                                    + Constants.SPEED_OF_LIGHT * currentSatPos.getDtSat()
                                    - X.get(3, 0));             // c*dtr
                                    //- Tr0
                                    //- X.get(idxdTrop, 0) * Mwet);  // Wet part

                    B.set(i + 1, 0,
                            currentSatObs.getPhaseL3()*Constants.WL3
                                    - p
                                    + Constants.SPEED_OF_LIGHT * currentSatPos.getDtSat()
                                    - X.get(3, 0)             //c*dtr
                                    - X.get(nbBasicParam + idxSat));                 // N*Lambda
                                    //- Tr0
                                    //- X.get(idxdTrop, 0) * Mwet);  // Wet part


                }
                //TODO ADD PHASE WIND UP EFFECT (cm level correction)

                W.set(i, i, getWeightValue(currentSatPos, SIGMA_CODE));
                W.set(i+1, i+1, getWeightValue(currentSatPos, SIGMA_PHASE));

                i += 2;

                // TODO VERIFY THAT TOTAL TROPO CORR IS ~2.5m
                //System.out.println("TROPO: " + currentSatObs.getConstellation() + ", " + (Tr0 + Mwet * X.get(4,0)));

                //System.out.println("B:\n" + B.toString());
            }

            Q = W.invert();
            C = A.transpose().mult(Q).mult(B);
            N = A.transpose().mult(Q).mult(A);

            solveLSE();


            /*
            /// Kalman
            // Setting up the X

            // Setting up the transition matrix - Static mode
            SimpleMatrix F = SimpleMatrix.identity(nbParam);
            F.set(idxCdt, idxCdt, 0.0);
            F.set(idxdTrop, idxdTrop, 0.0);

            // Setting up the noise matrix - Static mode
            SimpleMatrix Q = new SimpleMatrix(nbParam, nbParam);
            Q.set(idxCdt, idxCdt, SIGMA_CDT*SIGMA_CDT);
            Q.set(idxdTrop, idxdTrop, Math.pow(SIGMA_DTROP + DELTA_SIGMA_DTROP * DELTA_EPOCH, 2));

            // Setting up the inital parameters noise P matrix
            SimpleMatrix P = SimpleMatrix.identity(nbParam);
            P = P.scale(Math.pow(SIGMA_AMBIGUITY, 2));
            P.set(idxX, idxX, Math.pow(SIGMA_POSITION, 2));
            P.set(idxY, idxY, Math.pow(SIGMA_POSITION, 2));
            P.set(idxZ, idxZ, Math.pow(SIGMA_POSITION, 2));
            P.set(idxCdt, idxCdt, Math.pow(SIGMA_CDT, 2));
            P.set(idxdTrop, idxdTrop, Math.pow(SIGMA_DTROP + DELTA_SIGMA_DTROP * DELTA_EPOCH, 2));

            X.set(F.mult(X));
            P = F.mult(P.mult(F.transpose())).plus(Q);

            for(HashMap.Entry<Integer, Integer> entry: systemsIndex.entrySet())
            {
                P.set(entry.getValue(), entry.getValue(), SIGMA_GGTO);
            }

            if(kalmanFilter == null)
            {
                kalmanFilter = new KalmanFilter(X, P);
            }

            kalmanFilter.init(A, B, W, F, Q);
            X = kalmanFilter.computeSolution();

            */

            //----

            //System.out.println("A:\n" + A.toString());
            //System.out.println("B:\n" + B.toString());
            //System.out.println("Q:\n" + Q.toString());
            //System.out.println("dX:\n" + dX.toString());

           // System.out.println("X:\n" + X.transpose().toString());


            loopCount ++;

        } while (/*dX.extractMatrix(0,3,0,1).normF() > STOPPING_LSE_CRITERIA
                && */(loopCount < 5));

        //System.out.println("X:\n" + X.transpose().toString());

        //Log.i("PPP", X.toString());

        //System.out.println("X:\n" + X.toString());
        //System.out.println("A:\n" + A.toString());
        //System.out.println("B:\n" + B.toString());

        //System.out.println("X:\n" + X.transpose().toString());

        if(prevN != null)
        {
            adaptNormalMatrices();

            if(!newSat.isEmpty())
            {
                for(String key : newSat)
                {
                    SimpleMatrix tmp = new SimpleMatrix(prevN.numRows() + 1, prevN.numCols() + 1);
                    prevN = Utils.extendMatrix(prevN, 1,1);
                    prevC = Utils.extendMatrix(prevC, 1,0);
                }
            }

            C = prevC.plus(A.transpose().mult(Q).mult(B));
            N = prevN.plus(A.transpose().mult(Q).mult(A));

            //solveLSE();

            X = recursiveLSE(A, Q, X);

            //System.out.println("X:\n" + X.transpose().toString());

            //dX = dX.minus(prevdX);
        }

        String str = "";
        for (int i=0; i < nbBasicParam; i++)
        {
            str += X.get(i,0);
            str += " ";
        }

        for (HashMap.Entry<String, Integer> entry: satIndex.entrySet())
        {
            str += X.get(nbBasicParam + entry.getValue(),0);
            str += " ";
        }

        str += X.get(nbParam-1);

        System.out.println(str);

        prevC = C;
        prevN = N;
        prevdX = dX;

        prevPinv = N;
        prevX = X;

        nbEpoch += 1;

        Coordinates coord = new Coordinates(X.get(0,0), X.get(1,0), X.get(2,0));

        return coord;
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Initialisation of the LSE matrices with the current observations
     */
    private void initMatrices() {
        for (HashMap.Entry<SatellitePositionGNSS, GNSSObservation> entry : satPosObs.entrySet()) {

            GNSSObservation currentSatObs = entry.getValue();
            String key = Utils.getFormattedSatIndex(currentSatObs.getConstellation(), currentSatObs.getId());

            // Check the contained satellites
            if (satIndex.get(key) == null) {
                satIndex.put(key, (nbParam - nbBasicParam));
                newSat.add(key);
                nbParam++;
            }
            presentSat.add(key);
        }

        A = new SimpleMatrix(satPosObs.size()*2, nbParam);
        W = SimpleMatrix.identity(satPosObs.size()*2);
        B = new SimpleMatrix(satPosObs.size()*2, 1);

        // Filling the X vector with a priori values
        X = new SimpleMatrix(nbParam, 1);
        X.set(0,0, approxPos.getX());
        X.set(1,0, approxPos.getY());
        X.set(2,0, approxPos.getZ());

        X0 = X.copy();

        dX = new SimpleMatrix(nbParam, 1);

    }

//--------------------------------------------------------------------------------------------------

    /**
     * Compute a weighted Least Square Estimation.
     */
    public void solveLSE()
    {
        adaptNormalMatrices();

        try
        {
            dX = N.invert().mult(C);
        }
        catch (SingularMatrixException e)
        {
            System.out.println("Matrix singular !");
        }

        X = X.plus(dX);

        return;
    }

//--------------------------------------------------------------------------------------------------

    public SimpleMatrix recursiveLSE(SimpleMatrix A, SimpleMatrix Q, SimpleMatrix X)
    {
        SimpleMatrix yTilde = A.mult(X);
        SimpleMatrix yHat = A.mult(prevX);
        SimpleMatrix Pinv = prevPinv.plus(A.transpose().mult(Q).mult(A));
        SimpleMatrix K = Pinv.invert().mult(A.transpose().mult(Q));

        SimpleMatrix newX = prevX.plus(K.mult(yTilde.minus(yHat)));

        prevPinv = Pinv;
        prevdX = X0.minus(newX);
        prevX = newX;
        //X0 = newX;

        return newX;
    }

//--------------------------------------------------------------------------------------------------

    private void adaptNormalMatrices()
    {
        // Check present sats
        Set<String> diffSets = new HashSet<>();

        diffSets.addAll(satIndex.keySet()); // Copy set
        diffSets.removeAll(presentSat);

        if(!diffSets.isEmpty())
        {
            for(String key : diffSets)
            {
                N.setColumn(satIndex.get(key) + nbBasicParam, 0, 0);
                N.setRow(satIndex.get(key) + nbBasicParam, 0, 0);
                N.set(satIndex.get(key) + nbBasicParam, satIndex.get(key) + nbBasicParam, 1);
            }
        }
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Compute the number of time offsets parameters to be estimated, on for each system.
     */
    private void setTimeOffsetParamIndex()
    {
        systemsIndex = new HashMap<>();

        if(systemsEnabled.contains(GnssStatus.CONSTELLATION_GPS))
        {
            referenceSystem = GnssStatus.CONSTELLATION_GPS;
        }
        else if (systemsEnabled.contains(GnssStatus.CONSTELLATION_GALILEO))
        {
            referenceSystem = GnssStatus.CONSTELLATION_GALILEO;
        }
        else
        {
            referenceSystem = GnssStatus.CONSTELLATION_BEIDOU;
        }

        int idx = nbParam;
        for(Integer system: systemsEnabled)
        {
            if(system != referenceSystem)
            {
                systemsIndex.put(system, idx);
                idx ++;
            }
        }

        return;
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Compute the geometric distance between two coordinates.
     * @param approxCoord Approximate position of the user.
     * @param satPos Current satellite position
     * @return The geometric distance
     */
    private double getGeometricDistance(Coordinates approxCoord, Coordinates satPos)
    {
        SimpleMatrix XSat = new SimpleMatrix(3,1);
        XSat.set(0,0, satPos.getX());
        XSat.set(1,0, satPos.getY());
        XSat.set(2,0, satPos.getZ());

        SimpleMatrix XRec = new SimpleMatrix(3,1);
        XRec.set(0,0, approxCoord.getX());
        XRec.set(1,0, approxCoord.getY());
        XRec.set(2,0, approxCoord.getZ());

        double p = XRec.minus(XSat).normF();

        return p;
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Compute the weight value using the measurement precision and the satellite elevation.
     * @param satPos Satellite position
     * @param sigmaObs Precision of the measurement (See constant definition on top)
     * @return The weight value
     */
    private double getWeightValue(SatellitePositionGNSS satPos, double sigmaObs)
    {
        double sigma = 0.0;
        double elevation = satPos.getSatElevation();

        //System.out.println(elevation);

        sigma = Math.sin(Math.toRadians(elevation));
        sigma = sigmaObs / Math.pow(sigma, 2);

        return sigma;
    }

    public static void main(String[] args) throws IOException
    {
        Vector<HashMap<SatellitePositionGNSS, GNSSObservation>> vecObs = new Vector<>();
        String line = null;

        BufferedReader br;

        String filename = "./MyData/29-05-2019_101740_ephem.txt";

        Coordinates approxUserCoord = new Coordinates(4343420.0, -124910.0, 4653460.0);

        try
        {
            br = new BufferedReader(new FileReader(filename));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found.");
            return;
        }

        //Skipping header lines
        line = br.readLine();
        line = br.readLine();

        Double prev = 0.0;
        HashMap<SatellitePositionGNSS, GNSSObservation> satelliteObservations = new HashMap<>();
        while ((line = br.readLine()) != null)
        {
            String str[] = line.split(",");

            if(prev > 0)
            {
                if(prev != Double.parseDouble(str[0]))
                {
                    vecObs.add(satelliteObservations);
                    satelliteObservations = new HashMap<>();
                }
            }

            GNSSObservation gnssObservation = new GNSSObservation();

            Coordinates satCoord = new Coordinates(
                    Double.parseDouble(str[3]),
                    Double.parseDouble(str[4]),
                    Double.parseDouble(str[5]));

            SatellitePositionGNSS satPos = new SatellitePositionGNSS(
                    satCoord,
                    Double.parseDouble(str[6]),
                    approxUserCoord);

            gnssObservation.setConstellation(Integer.parseInt(str[1]));
            gnssObservation.setId(Integer.parseInt(str[2]));
            gnssObservation.setPseudorangeL1(Double.parseDouble(str[7]));
            gnssObservation.setPhaseL1(Double.parseDouble(str[8]));
            gnssObservation.setPseudorangeL5(Double.parseDouble(str[9]));
            gnssObservation.setPhaseL5(Double.parseDouble(str[10]));
            gnssObservation.setPseudorangeL3(Double.parseDouble(str[11]));
            gnssObservation.setPhaseL3(Double.parseDouble(str[12]));

            satelliteObservations.put(satPos, gnssObservation);

            prev = Double.parseDouble(str[0]);
        }

        //System.out.println("Hello World!");

        LinkedHashMap<SatellitePositionGNSS, GNSSObservation> stackObs = new LinkedHashMap<>();
        PPP ppp = new PPP(vecObs.firstElement(), approxUserCoord);
        for(int i=0; i<20; i++)
        {
            //PPP ppp = new PPP(vecObs.firstElement(), approxUserCoord);
            Coordinates coord = ppp.computeUserPositionStatic(vecObs.elementAt(i));
            //System.out.println(coord.getLatLngAlt().toString());
        }

        /*
        PPP ppp = new PPP(stackObs, false);
        Coordinates coord = ppp.computeUserPositionStatic(approxUserCoord);
        System.out.println(coord.toString());
        */


        /*//TO PERFORM ONE EPOCH ONLY SOLUTION
        for(HashMap<SatellitePositionGNSS, GNSSObservation> entry: vecObs)
        {
            PPP ppp = new PPP(entry, false);
            Coordinates coord = ppp.computeUserPositionStatic(approxUserCoord);
            System.out.println(coord.getLatLngAlt().toString());
        }*/

        return;
    }
}


//END OF FILE