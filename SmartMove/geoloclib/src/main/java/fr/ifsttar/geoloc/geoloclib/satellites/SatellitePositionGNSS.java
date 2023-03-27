///=================================================================================================
// Class SatellitePositionGNSS
//      Author :  Antoine GRENIER - 2019/09/06
//        Date :  2019/09/06
//      Updated by Edgar Lenhof 01/2020 : Glonass satellite computation added
//      Enhanced & Updated by Aravind RAMESH, 11/08/2022
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
package fr.ifsttar.geoloc.geoloclib.satellites;


import android.location.GnssStatus;
import android.util.Log;


import org.ejml.simple.SimpleMatrix;
import org.gogpsproject.Constants;
import org.gogpsproject.ephemeris.GNSSEphemeris;
import org.gogpsproject.ephemeris.GlonassEphemeris;
import org.gogpsproject.ephemeris.KeplerianEphemeris;
import org.gogpsproject.ephemeris.PreciseCorrection;
import org.gogpsproject.ephemeris.SatelliteCodeBiases;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;


import fr.ifsttar.geoloc.geoloclib.Coordinates;
import fr.ifsttar.geoloc.geoloclib.LatLngAlt;
import fr.ifsttar.geoloc.geoloclib.Options;
import fr.ifsttar.geoloc.geoloclib.Utils;





/**
 * A class to compute the coordinates of the satellites
 * Based on SatellitePositionsGPS class.
 *
 * References: [1] RTCM State Space Representation - Messages, Status and Plans, Martin Schmitz, Geo++.
 */
public class SatellitePositionGNSS
{
    private GNSSEphemeris eph;
    private PreciseCorrection ephCorrections;
    private SatelliteCodeBiases.CodeBias codeBias;

    private Coordinates satCoordinates;
    private Coordinates prevSatCoordinates;
    private Coordinates userCoord;
    private double satElevation;
    private double satAzimuth;

    private double tRecep;
    private double tEmis;
    private double dtSat;
    private double geomTransmissionTime;
    private double ionoCorr;

    // Precise corrections
    private SimpleMatrix dX_precise;
    private double dC_precise;

    // Orbital parameters
    private double uk;
    private double ik;
    private double OMEGAk;

    private final double DELTA_T = 1.0;     // [s], time between two measurements

    //----------------------------------------------------------------------------------------------

    /**
     * Default Constructor.
     */
    public SatellitePositionGNSS(){

    }

    /**
     * Constructor.
     * @param coordinates Satellites coordinates
     * @param deltaTsv Satellite clock bias
     * @param userCoord User coordinates
     */
    public SatellitePositionGNSS(Coordinates coordinates, Double deltaTsv, Coordinates userCoord)
    {
        this.satCoordinates = coordinates;
        this.userCoord = userCoord;
        this.dtSat = deltaTsv;

        computeSatelliteElevation();
    }

    //----------------------------------------------------------------------------------------------

//    /**
//     * Constructor (For KeplerianEphemeris object).
//     * @param eph Keplerian-like satellite ephemeris
//     * @param time Reception time
//     * @param ecefUserCoord User coordinates
//     */
//    public SatellitePositionGNSS(KeplerianEphemeris eph, double time, Coordinates ecefUserCoord)
//    {
//        wn = eph.getWn();
//        prn = eph.getPrn();
//        gnssSystem = getGnssSystem();
//        tRecep = time;
//        this.userCoord = ecefUserCoord;
//
//        //this.eph = eph;
//
//        computeKeplerianSatellitePosition(tRecep, eph);
//        computeSatelliteElevation();
//
//        this.geomTransmissionTime = satCoordinates.getSimpleMatrix().minus(userCoord.getSimpleMatrix()).normF()
//                / Constants.SPEED_OF_LIGHT;
//    }

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor (For KeplerianEphemeris-like object)
     * @param eph Satellite ephemeris
     * @param time Transmission time
     * @param ecefUserCoord User coordinates
     * @param processingOptions Computation options
     */
    public SatellitePositionGNSS(KeplerianEphemeris eph, double time, Coordinates ecefUserCoord, Options processingOptions)
    {
        codeBias = eph.getCodeBias();
        tRecep = time;
        this.userCoord = ecefUserCoord;

        this.eph = eph;

        satCoordinates = computeKeplerianSatellitePosition(tRecep, eph);
        computeSatelliteElevation();

        // Computing satellites coordinates at the previous epoch
        prevSatCoordinates = computeKeplerianSatellitePosition(tRecep - DELTA_T, eph);

        SimpleMatrix res = satCoordinates.getSimpleMatrix().minus(userCoord.getSimpleMatrix());

        // Computation of Euclidean Norm of the resultant matrix
        double var_1 = Math.pow(Math.abs(res.get(0,0)),2);
        double var_2 = Math.pow(Math.abs(res.get(1,0)),2);
        double var_3 = Math.pow(Math.abs(res.get(2,0)),2);

        double vec_norm = Math.sqrt(var_1 + var_2 + var_3);

        this.geomTransmissionTime =  vec_norm / Constants.SPEED_OF_LIGHT;

        //Log.d("GeoTimeEN: ", String.valueOf(geomTransmissionTime));

        /*this.geomTransmissionTime = (satCoordinates.getSimpleMatrix().minus(userCoord.getSimpleMatrix())).normF()
                / Constants.SPEED_OF_LIGHT;*/

        /*double geoTxFN = (satCoordinates.getSimpleMatrix().minus(userCoord.getSimpleMatrix())).normF()
                / Constants.SPEED_OF_LIGHT;*/

        //Log.d("GeoTimeFN: ", String.valueOf(geoTxFN));
    }

    /**
     * Constructor (For Glonass Ephemeris object)
     * @param eph Satellite ephemeris
     * @param time Transmission time
     * @param ecefUserCoord User coordinates
     * @param processingOptions Computation options
     */
    public SatellitePositionGNSS(GlonassEphemeris eph, double time, Coordinates ecefUserCoord, Options processingOptions) {

        this.eph = eph;
        codeBias = eph.getCodeBias();
        tRecep = time;
        this.userCoord = ecefUserCoord;

        satCoordinates = computeGlonassSatellitePosition(tRecep, eph);

        computeSatelliteElevation();

        // Computing satellites coordinates at the previous epoch
        prevSatCoordinates = computeGlonassSatellitePosition(tRecep - DELTA_T, eph);

        SimpleMatrix res = satCoordinates.getSimpleMatrix().minus(userCoord.getSimpleMatrix());

        // Computation of Euclidean Norm of the resultant matrix
        double var_1 = Math.pow(Math.abs(res.get(0,0)),2);
        double var_2 = Math.pow(Math.abs(res.get(1,0)),2);
        double var_3 = Math.pow(Math.abs(res.get(2,0)),2);

        double vec_norm = Math.sqrt(var_1 + var_2 + var_3);

        this.geomTransmissionTime =  vec_norm / Constants.SPEED_OF_LIGHT;

        /*this.geomTransmissionTime = satCoordinates.getSimpleMatrix().minus(userCoord.getSimpleMatrix()).normF()
                / Constants.SPEED_OF_LIGHT;*/

    }

    //---------------------------------------------------------------------------------

    /**
     * Computes the satellite position at instant t from data at instance tb
     * for more info see https://gssc.esa.int/navipedia//index.php/GLONASS_Satellite_Coordinates_Computation
     * @param reception_time time of transmission of the satellite signal
     * @param eph ephemeris data of the satellite
     * @return
     */
    private Coordinates computeGlonassSatellitePosition(double reception_time, GlonassEphemeris eph) {



        // SETP 1 : PZ90 coordinates from navigation message are transformed to inertial ECI coordinates


        double theta_G0 = getThetaG0(eph.getDate());
        double theta_Ge = theta_G0 + Constants.OMEGAE_DOT_GLO * (eph.getTb()*Constants.SEC_IN_DAY - 3*3600);
        //double theta_Ge = theta_G0 + Constants.OMEGAE_DOT_GLO * (eph.getTb()*60 - 3*3600);//convert time tb from min to sec in UTC0
        SimpleMatrix rotation = Utils.rotation_matrix(theta_Ge);


        SimpleMatrix inertial_coordinates = new SimpleMatrix(3,1);
        inertial_coordinates.set(0,0, eph.getX()*1000);
        inertial_coordinates.set(1,0, eph.getY()*1000);
        inertial_coordinates.set(2,0, eph.getZ()*1000);


        inertial_coordinates = rotation.mult(inertial_coordinates);


        SimpleMatrix inertial_velocity = new SimpleMatrix(3,1);

        inertial_velocity.set(0,0, eph.getX_dot()*1000);
        inertial_velocity.set(1,0, eph.getY_dot()*1000);
        inertial_velocity.set(2,0, eph.getZ_dot()*1000);


        inertial_velocity = rotation.mult(inertial_velocity);
        inertial_velocity.set(0,0, inertial_velocity.get(0,0) - Constants.OMEGAE_DOT_GLO*inertial_coordinates.get(1,0));
        inertial_velocity.set(1,0, inertial_velocity.get(1,0) + Constants.OMEGAE_DOT_GLO*inertial_coordinates.get(0,0));


        SimpleMatrix lunisolar_accelerations = new SimpleMatrix(3,1);

        lunisolar_accelerations.set(0,0, eph.getX_ddot()*1000);
        lunisolar_accelerations.set(1,0, eph.getY_ddot()*1000);
        lunisolar_accelerations.set(2,0, eph.getZ_ddot()*1000);


        lunisolar_accelerations = rotation.mult(lunisolar_accelerations);




        // STEP 2 : Runge-Kutta integration algorithm from tb to t_recep


        double t0 = eph.getTb()*60; //convert from min to sec
        SimpleMatrix rungeKutta = computeRungeKutta(reception_time, t0, 150, inertial_coordinates, inertial_velocity, lunisolar_accelerations);



        // STEP 3 : ECI coordinates back to PZ-90

        double theta_G = theta_G0 + Constants.OMEGAE_DOT_GLO * (reception_time - 3*3600);
        SimpleMatrix rotation_back = Utils.rotation_matrix(-theta_G);

        SimpleMatrix coordinates = rotation_back.mult(rungeKutta.rows(0,3));
        SimpleMatrix velocities = rotation_back.mult(rungeKutta.rows(3,6));
        velocities.set(0,0, velocities.get(0,0) + Constants.OMEGAE_DOT_GLO*coordinates.get(1,0));
        velocities.set(1,0, velocities.get(1,0) - Constants.OMEGAE_DOT_GLO*coordinates.get(0,0));


        // STEP 4 : convert from PZ90.02 to WGS84

        coordinates.set(0,0, coordinates.get(0,0));
        coordinates.set(1,0, coordinates.get(1,0));
        coordinates.set(2,0, coordinates.get(2,0));

/*      coordinates.set(0,0, coordinates.get(0,0) -0.36);
        coordinates.set(1,0, coordinates.get(1,0) +0.08);
        coordinates.set(2,0, coordinates.get(2,0) +0.18);*/

        // STEP 5 : correct transmission time

        // clock offset
        // TODO check that time are in same range
        double dte = eph.getTau_n() + eph.getGamma_n() * (reception_time - t0);

        this.tEmis = t0 + dte;
        this.dtSat = tRecep - tEmis;


        // TODO implement fromTODtoTOW
        return new Coordinates(coordinates.get(0,0), coordinates.get(1,0), coordinates.get(2,0), velocities.get(0,0), velocities.get(1,0), velocities.get(2,0), Utils.fromTODtoTOW(tEmis));
    }


    /**
     * Implements the Runge-Kutta algorithm to get satellite position at tEmis from satellite position at t0 (time of validity of ephemeris)
     * @param t0 time of validity of ephemeris, in seconds in the glonass day
     * @param h integration step (typically 150 sec)
     * @param coordinates satellite coordinates at t0
     * @param velocities satellite velocity at t0
     * @param ls_accelerations lunisolar accelaration on the satellite
     * @return
     */
    private SimpleMatrix computeRungeKutta(double reception_time, double t0, int h, SimpleMatrix coordinates, SimpleMatrix velocities, SimpleMatrix ls_accelerations){

        // initialise integration parameters

        SimpleMatrix Y = new SimpleMatrix(6,1);
        Y.set(0,0, coordinates.get(0,0));
        Y.set(1,0, coordinates.get(1,0));
        Y.set(2,0, coordinates.get(2,0));
        Y.set(3,0, velocities.get(0,0));
        Y.set(4,0, velocities.get(1,0));
        Y.set(5,0, velocities.get(2,0));



        if (reception_time < t0) {
            h = -h;
        }
        int nbStep = (int)Math.floor((reception_time-t0)/h) +1;
        int last_step = (int)( h *( (reception_time-t0)/h - Math.floor((reception_time-t0)/h) ) );


        // we iterate from t0 to tRecep by adding h
        for (int i = 0; i < nbStep; i++){

            // on last step we add the amount of time < h needed to get to tRecep
            if (i == nbStep - 1){

                if (last_step == 0){ //in case last step = 0 we have to avoid division by 0
                    continue;
                }

                h = last_step;
            }


            // !!!! Java considers double/int = int !!!!
            SimpleMatrix K1 = F_Y(Y, ls_accelerations);

            SimpleMatrix K2 = F_Y( Y.plus( K1.divide( 2/(float)h ) ), ls_accelerations);

            SimpleMatrix K3 = F_Y(Y.plus( K2.divide( 2/(float)h ) ), ls_accelerations);

            SimpleMatrix K4 = F_Y(Y.plus( K3.divide( 1/(float)h ) ), ls_accelerations);


            SimpleMatrix sumK = K1.plus( K2.divide( 1.0/2.0 ) ).plus( K3.divide( 1.0/2.0) ).plus(K4);
            Y = Y.plus( sumK.divide( 6/(float)h ) );

        }

        return  Y;

    }

    /**
     * Creates the integration function for runge kutta algorithm
     * @param Y runge kutta parameters (coordinates and velocities)
     * @param J_ls lunisolar acceleration
     * @return
     */
    private SimpleMatrix F_Y(SimpleMatrix Y, SimpleMatrix J_ls){

        double xa = Y.get(0,0);
        double ya = Y.get(1,0);
        double za = Y.get(2,0);
        double vxa = Y.get(3,0);
        double vya = Y.get(4,0);
        double vza = Y.get(5,0);

        SimpleMatrix F_Y = new SimpleMatrix(6, 1);
        F_Y.set(0, 0, vxa);
        F_Y.set(1, 0, vya);
        F_Y.set(2, 0, vza);


        double r = Math.sqrt(Math.pow(xa, 2) + Math.pow(ya, 2) + Math.pow(za, 2));
        double mu_bar = Constants.GM_GLO / Math.pow(r, 2);
        double rho_bar = Constants.ELL_A_GLO / r;

        double xa_bar = xa/r;
        double ya_bar = ya/r;
        double za_bar = za/r;

        double acc_x = (mu_bar * xa_bar) * (-1 + 1.5*Constants.J2_GLO * Math.pow(rho_bar, 2)*(1 - 5 * Math.pow(za_bar, 2))) + J_ls.get(0, 0);
        F_Y.set(3, 0, acc_x);

        double acc_y = (mu_bar * ya_bar) * (-1 + 1.5*Constants.J2_GLO * Math.pow(rho_bar, 2)*(1 - 5 * Math.pow(za_bar, 2))) + J_ls.get(1, 0);
        F_Y.set(4, 0, acc_y);

        double acc_z = (mu_bar * za_bar) * (-1 + 1.5*Constants.J2_GLO*Math.pow(rho_bar, 2)*(3 - 5 * Math.pow(za_bar, 2))) + J_ls.get(2, 0);
        F_Y.set(5, 0, acc_z);


        return  F_Y;
    }

    /**
     * Computes the Greenwich Aparent Sideral Time at UT1=0 for the input day
     * more info on computation at https://www.cfa.harvard.edu/~jzhao/times.html#GMST
     * @param date_of_ephemeris date of emission of the received message
     * @return
     */
    private double getThetaG0(GregorianCalendar date_of_ephemeris) {


        // this day date put back to midnight GMT
        GregorianCalendar date_GMT = new GregorianCalendar();
        date_GMT.clear();
        date_GMT.setTimeZone(TimeZone.getTimeZone("GMT"));
        date_GMT.set(date_of_ephemeris.get(date_of_ephemeris.YEAR), date_of_ephemeris.get(date_of_ephemeris.MONTH), date_of_ephemeris.get(date_of_ephemeris.DAY_OF_MONTH));
        date_GMT.set(Calendar.HOUR_OF_DAY, 0);
        date_GMT.set(Calendar.MINUTE, 0);
        date_GMT.set(Calendar.SECOND, 0);

        //date at greenwich at midnight in Julian centuries from 2000 Jan. 1st 12h UT1
        double D =(Utils.toJulianDate(date_GMT) - Constants.JD_1_JAN_2000);
        double T = D/ 36525.0;

        //Greenwich Mean Sideral Time at 00h00
        double GMST = Constants.GMST0 + Constants.GMST1*T + Constants.GMST2*Math.pow(T,2) - Constants.GMST3*Math.pow(T,3); // in seconds of time
        GMST = GMST * Math.PI/43200.0; //in radians
        // Equation of Equinoxes (values taken from pygnss toolbox)
        double d2r = Math.PI / 180.0;
        double L = 280.47 + 0.98565 * D;
        double eps = 23.4393 - 4e-7 * D;
        double omega = 125.04 -0.052954 * D;
        double dPsi = -3.19e-4 * Math.sin(omega*d2r) - 2.4e-5 * Math.sin(2*L*d2r);
        double alphaE = dPsi * Math.cos(eps*d2r); // in hours of time
        alphaE = alphaE * Math.PI/12.0;//in radians

        // Greenwich Aparent Sideral Time at 00h00
        double GAST = GMST  + alphaE; //in radians
;
        return GAST %(2 * Math.PI); // modulo 2 pi

    }

    //----------------------------------------------------------------------------------------------

    /**
     * Computes the satellite position using the ephemeris parameters.
     * @param tRecep Transmission time
     * @param eph ephemeris of the satellite
     */
    public Coordinates computeKeplerianSatellitePosition(double tRecep, KeplerianEphemeris eph)
    {

        /// Difference to ephemeris time

        double dt = tRecep - eph.getToe();
        double dt_c = tRecep - eph.getToc();

        //beginning and end of week crpréciseossovers correction
        if(dt > 302400)
        {
            dt -= 604800;
        }
        else if(dt < -302400)
        {
            dt += 604800;
        }

        //clock correction, time crossover correction term
        if(dt_c > 302400)
        {
            dt_c -= 604800;
        }
        else if(dt_c < -302400)
        {
            dt_c += 604800;
        }

        // Constellation type
        int gnssSys = eph.getGnssSystem();
        double a = Math.pow(eph.getSquareA(),2);
        double e = eph.getEc();

        // Mean motion
        double n0 = Math.sqrt(Constants.EARTH_GRAVITATIONAL_CONSTANT/Math.pow(a,3));
        double n = n0 + eph.getDeltaN();

        // Mean anomaly
        double M = eph.getM0() + n * dt;
        //double MDot = n;

        // Eccentric anomaly
        /*if (gnssSys == 5)
        {
            M = Math.IEEEremainder(M + 2 * Math.PI, 2 * Math.PI);
        }*/
        double E = M;
        double EDot = n / (1 - e * Math.cos(E));
        // For GPS Constellation
        /*if (gnssSys == 1)
        {
            for (int i = 0;i < 5;i++)
            {
                E = E + ((M - E + e * Math.sin(E)) / (1 - e * Math.cos(E)));
            }
        }*/
        // For Galileo (6) OR Beidou (5) constellation
        if (gnssSys == 1)
        {
            /*double diff = E;
            while(Math.abs(diff)>1.0e-13)
            {
                diff = E;
                E = M + e * Math.sin(E);
                //MDot = (1 - e * Math.cos(E)) * EDot;
                //EDot = MDot / (1 - e * Math.cos(E));
                diff -= E;
            }*/
            for (int i = 0;i < 5;i++)
            {
                E = E + ((M - E + e * Math.sin(E)) / (1 - e * Math.cos(E)));
            }
        }
        else
        {
            for (int i = 0;i < 5;i++)
            {
                E = M + (e * Math.sin(E));
            }
        }
        // QZSS or other
        /*else if (gnssSys == 5)
        {
            int i;
            double EkOld, dEk;

            // Eccentric anomaly iterative computation
            int maxNumIter = 12;
            for (i = 0; i < maxNumIter; i++)
            {
                EkOld = E;
                E = M + e * Math.sin(E);
                dEk = Math.IEEEremainder(E - EkOld, 2 * Math.PI);
                if (Math.abs(dEk) < 1e-12)
                    break;
            }
        }*/
        // Simple technique trial
        /*for (int i = 0;i<4;i++)
        {
            E = M + (e * Math.sin(E));
            MDot = (1 - e * Math.cos(E)) * EDot;
            EDot = MDot / (1 - e * Math.cos(E));

        }*/
        double v = 0.0;
        // Compute true anomaly
        if (gnssSys == 1)
        {
            v = 2 * Math.atan(Math.sqrt((1 + e) / (1 - e)) * Math.tan(E/2));
        }
        else
        {
            v = Math.atan2(Math.sqrt(1 - Math.pow(e,2)) * Math.sin(E), (Math.cos(E) - e));
        }
        /*else
        {
            v = 2 * Math.atan(Math.sqrt((1 + e) / (1 - e)) * Math.tan(E/2));
        }*/
        double vDot = Math.sin(E) * EDot * (1 + e * Math.cos(v)) / ((1 - Math.cos(E) * e) * Math.sin(v));
        // Argument of latitude
        double phi = v + eph.getOmega();
        /*if (gnssSys == 5)
        {
            phi = Math.IEEEremainder(phi, 2 * Math.PI);
        }*/
        double phiDot = vDot;
        double u = phi + eph.getCus() * Math.sin(2*phi) + eph.getCuc() * Math.cos(2*phi);
        double duDot = 2 * (eph.getCus() * Math.cos(2*phi) - eph.getCuc() * Math.sin(2*phi)) * phiDot;
        double uDot = phiDot + duDot;

        // Radius
        double r = a * (1 - e * Math.cos(E));
        double rDot = a * e * Math.sin(E) * EDot;
        double dr = eph.getCrs() * Math.sin(2*phi) + eph.getCrc() * Math.cos(2*phi);
        double drDot = 2 * (eph.getCrs() * Math.cos(2*phi) - eph.getCrc() * Math.sin(2*phi)) * phiDot;
        r = r + dr;
        rDot += drDot;

        // Inclination
        double i;
        double diDot = 2 * (eph.getCis() * Math.cos(2*phi) - eph.getCic() * Math.sin(2*phi)) * phiDot;
        double iDot = eph.getIdot() + diDot;
        i = eph.getI0() + eph.getIdot() * dt;
        i += eph.getCis() * Math.sin(2*phi) + eph.getCic() * Math.cos(2*phi);

        // Longitude of ascending node
        double OMEGA;
        double OMEDADot;
        /*if (gnssSys == 5)
        {
            OMEDADot = (eph.getOmegaDot() - Constants.OMEGAE_DOT_BDS);
            OMEGA = eph.getOmega0() + (eph.getOmegaDot() - Constants.OMEGAE_DOT_BDS) * dt;
            OMEGA -= Constants.OMEGAE_DOT_BDS * eph.getToe();
            OMEGA = Math.IEEEremainder(OMEGA + 2 * Math.PI, 2 * Math.PI);
        }*/
        OMEDADot = (eph.getOmegaDot() - Constants.OMEGAE_DOT_GPS);
        OMEGA = eph.getOmega0() + (eph.getOmegaDot() - Constants.OMEGAE_DOT_GPS) * dt;
        OMEGA -= Constants.OMEGAE_DOT_GPS * eph.getToe();

        // Saving orbital parameters for future computations
        uk = u;
        ik = i;
        OMEGAk = OMEGA;

        // Iono Correction Parameters for GPS, Galileo & Beidou Constellations
        if (gnssSys == 1 || gnssSys == 6)
        {
            this.ionoCorr = ionoCrrGPS(getSatElevation(),getSatAzimuth(),eph);
        }
        else if(gnssSys == 5)
        {
            this.ionoCorr = ionoCrrBDS(getSatElevation(),getSatAzimuth(),eph);
        }

        // ECEF coordinates
        double xp = r * Math.cos(u);
        double yp = r * Math.sin(u);

        double x = xp * Math.cos(OMEGA) - yp * Math.cos(i) * Math.sin(OMEGA);
        double y = xp * Math.sin(OMEGA) + yp * Math.cos(i) * Math.cos(OMEGA);
        double z = yp * Math.sin(i);

        double xDotp = rDot * Math.cos(u) - yp * uDot;
        double yDotp = rDot * Math.sin(u) + xp * uDot;

        // ECEF velocities;
        // TODO Verify the velocities
        double xDot = 0.0;
        xDot += xDotp * Math.cos(OMEGA) - yDotp * Math.cos(i) * Math.sin(OMEGA);
        xDot += yp * Math.sin(i) * Math.sin(OMEGA) * iDot - y * OMEDADot;

        double yDot = 0.0;
        yDot += xDotp * Math.sin(OMEGA) + yDotp * Math.cos(i) * Math.cos(OMEGA);
        yDot += -yp * Math.sin(i) * iDot * Math.cos(OMEGA) + x * OMEDADot;

        double zDot = yDotp * Math.sin(i) + yp * Math.cos(i) * iDot;

        /// Satellites clock error
        double _dtSat = eph.getAf0() + eph.getAf1() * dt_c + eph.getAf2() * Math.pow(dt_c, 2);
        // Relativistic clock correction
        _dtSat += Constants.RELATIVISTIC_ERROR_CONSTANT * eph.getSquareA() * e * Math.sin(E);

        this.tEmis = tRecep - _dtSat;
        if(gnssSys == 5)
        {
            this.dtSat = _dtSat - eph.getAodo();
        }
        else
        {
            this.dtSat = _dtSat;
        }
        return new Coordinates(x, y, z, xDot, yDot, zDot, tEmis);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Compute the satellite elevation, based on MATLAB's GNSS Navigation toolbox.
     */
    public void computeSatelliteElevation()
    {
        SimpleMatrix ned = getNED();

        double elevation = 0.0;
        double azimuth = 0.0;

        double N = ned.get(0,0);//enu.get(0);
        double E = ned.get(1,0);//enu.get(1);
        double D = ned.get(2,0);//enu.get(2);

        elevation = -1 * (Math.asin(D) * (180/Math.PI));

        azimuth = Math.atan2(E,N) * (180/Math.PI);

        if (azimuth < 0)
        {
            azimuth = azimuth + 360;
        }
        // Compute horizontal distance from origin to this object
        //double hDist = Math.sqrt(Math.pow(E, 2) + Math.pow(N, 2));

        // If this object is at zenith ...
        /*if (hDist < 1e-20) {
            // ... set azimuth = 0 and elevation = 90, ...
            elevation = 90;

        } else {
            // ... and elevation
            elevation = Math.toDegrees(Math.atan2(U, hDist));
        }*/

        //Log.i("ELEV", "" + id + ", " + elevation);

        this.satElevation = elevation;
        this.satAzimuth = azimuth;
        //Log.d("Elevation: ", String.valueOf(elevation));
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Get NED coordinates of the satellite relative to the given position.
     * Taken from GNSS Navigation Toolbox from MATLAB.
     * MATLAB Function: "lookangles"
     */
    private SimpleMatrix getNED()
    {
        Coordinates ecefUserCoord = this.userCoord;
        Coordinates coordinates = this.satCoordinates;

        SimpleMatrix rSat = coordinates.getSimpleMatrix();
        SimpleMatrix rUser = ecefUserCoord.getSimpleMatrix();
        SimpleMatrix diffVec;
        SimpleMatrix ned;

        diffVec = rSat.minus(rUser);
        //Estimation of Euclidean Norm
        double ele_1 = Math.abs(diffVec.get(0,0));
        double ele_2 = Math.abs(diffVec.get(1,0));
        double ele_3 = Math.abs(diffVec.get(2,0));
        double range = Math.sqrt(Math.pow(ele_1,2) + Math.pow(ele_2,2) + Math.pow(ele_3,2));

        diffVec = diffVec.divide(range);

        LatLngAlt geoUserCoord = new LatLngAlt(ecefUserCoord);

        //Compute rotation matrix from ECEF to ENU

        double lam = Math.toRadians(geoUserCoord.getLongitude());
        double phi = Math.toRadians(geoUserCoord.getLatitude());

        double cosLam = Math.cos(lam);
        double cosPhi = Math.cos(phi);
        double sinLam = Math.sin(lam);
        double sinPhi = Math.sin(phi);

        double[][] data = new double[3][3];
        //Matrix construction is incorrect
        /*data[0][0] = -sinLam;
        data[0][1] = cosLam;
        data[0][2] = 0;
        data[1][0] = -sinPhi * cosLam;
        data[1][1] = -sinPhi * sinLam;
        data[1][2] = cosPhi;
        data[2][0] = cosPhi * cosLam;
        data[2][1] = cosPhi * sinLam;
        data[2][2] = sinPhi;*/

        //Proper construction is as follows
        data[0][0] = -sinPhi * cosLam;
        data[0][1] = -sinPhi * sinLam;
        data[0][2] = cosPhi;
        data[1][0] = -sinLam;
        data[1][1] = cosLam;
        data[1][2] = 0;
        data[2][0] = -cosPhi * cosLam;
        data[2][1] = -cosPhi * sinLam;
        data[2][2] = -sinPhi;

        SimpleMatrix R = new SimpleMatrix(data);

        //Compute ENU coordinates of satellite
        ned = R.mult(diffVec);

        return ned;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Function applying corrections to orbit. Reference [1]
     */
    public void applyPreciseCorrections(GNSSEphemeris eph)
    {
        ephCorrections = eph.getEphCorrections();

        if(ephCorrections == null)
        {
            return;
        }


        // TODO check if that is correct time correction for glonass
        double delta_tk;
        if (eph.getGnssSystem() == GnssStatus.CONSTELLATION_GLONASS){
            delta_tk = tEmis - (ephCorrections.getTod() + ephCorrections.getUpdateInterval() / 2);
        }
        else {
            delta_tk = tEmis - (ephCorrections.getTow() + ephCorrections.getUpdateInterval() / 2);
        }


        // Ephemeris corrections
        SimpleMatrix X    = new SimpleMatrix(3, 1);
        SimpleMatrix xDot = new SimpleMatrix(3, 1);
        SimpleMatrix dO   = new SimpleMatrix(3, 1);

        SimpleMatrix eRadial;
        SimpleMatrix eAlong;
        SimpleMatrix eCross;

        double oRadial = ephCorrections.geteRadial();
        double oAlong = ephCorrections.geteAlong();
        double oCross = ephCorrections.geteCross();

        double oDotRadial = ephCorrections.geteDotRadial();
        double oDotAlong = ephCorrections.geteDotAlong();
        double oDotCross = ephCorrections.geteDotCross();

        dO.set(0,0,oRadial + oDotRadial * delta_tk);
        dO.set(1,0, oAlong + oDotAlong * delta_tk);
        dO.set(2,0, oCross + oDotCross * delta_tk);

        X.set(0, 0, satCoordinates.getX());
        X.set(1, 0, satCoordinates.getY());
        X.set(2, 0, satCoordinates.getZ());

        // Setting satellites velocity
        xDot = new SimpleMatrix(3,1);
        xDot.set(0,0, satCoordinates.getxDot());
        xDot.set(1,0, satCoordinates.getyDot());
        xDot.set(2,0, satCoordinates.getzDot());

        // Compute dX
        eAlong = xDot.divide(xDot.normF());
        eCross = Utils.crossMult(X, xDot).divide(Utils.crossMult(X, xDot).normF());
        eRadial = Utils.crossMult(eAlong, eCross);

        dX_precise = concat3Vec(eRadial, eAlong, eCross).mult(dO);

        X.set(0,0,X.get(0,0) - dX_precise.get(0,0));
        X.set(1,0,X.get(1,0) - dX_precise.get(1,0));
        X.set(2,0,X.get(2,0) - dX_precise.get(2,0));

        // Clocks corrections
        dC_precise = ephCorrections.getC0()
                + ephCorrections.getC1() * delta_tk
                + ephCorrections.getC2() * Math.pow(delta_tk, 2);

        dC_precise /= Constants.SPEED_OF_LIGHT;

        tEmis -= dC_precise;
        dtSat -= dC_precise;

        satCoordinates = new Coordinates(X.get(0,0), X.get(1,0), X.get(2,0),
                satCoordinates.getxDot(), satCoordinates.getyDot(), satCoordinates.getzDot(), tEmis);
    }

    /**
     * Function to concatenate 3 column vector into a 3x3 matrix
     * @param A 1st vector
     * @param B 2nd vector
     * @param C 3rd vector
     * @return Concatenated matrix
     */
    private SimpleMatrix concat3Vec(SimpleMatrix A, SimpleMatrix B, SimpleMatrix C)
    {
        SimpleMatrix D = new SimpleMatrix(3,3);

        D.set(0,0, A.get(0,0));
        D.set(1,0, A.get(1,0));
        D.set(2,0, A.get(2,0));

        D.set(0,1, B.get(0,0));
        D.set(1,1, B.get(1,0));
        D.set(2,1, B.get(2,0));

        D.set(0,2, C.get(0,0));
        D.set(1,2, C.get(1,0));
        D.set(2,2, C.get(2,0));

        return D;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Compute the rotation matrix to go from satellite body-frame to ECEF-frame
     * @return Rotation matrix
     */
    private SimpleMatrix getRotationMatrix()
    {
        SimpleMatrix Ri = new SimpleMatrix(3,3);
        SimpleMatrix ROMEGA = new SimpleMatrix(3,3);
        SimpleMatrix Romega = new SimpleMatrix(3,3);

        SimpleMatrix R;

        ROMEGA.set(0,0,  Math.cos(OMEGAk));
        ROMEGA.set(0,1, -Math.sin(OMEGAk));
        ROMEGA.set(1,0,  Math.sin(OMEGAk));
        ROMEGA.set(1,1,  Math.cos(OMEGAk));
        ROMEGA.set(2,2, 1);

        Ri.set(0,0, 1);
        Ri.set(1,1,  Math.cos(ik));
        Ri.set(1,2, -Math.sin(ik));
        Ri.set(2,1,  Math.sin(ik));
        Ri.set(2,2,  Math.cos(ik));

        Romega.set(0,0,  Math.cos(uk));
        Romega.set(0,1, -Math.sin(uk));
        Romega.set(1,0,  Math.sin(uk));
        Romega.set(1,1,  Math.cos(uk));
        Romega.set(2,2,  1);

        R = ROMEGA.mult(Ri).mult(Romega);

        return R;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Apply the correction for Earth rotation during transmission time.
     * @param dt Transmission time
     */
    public void applyEarthRotation(double dt)
    {
        // Current satellite coordinates
        SimpleMatrix X = new SimpleMatrix(3,1);
        SimpleMatrix XCorr;
        SimpleMatrix R3;

        R3 = getRotationR3(Constants.EARTH_ANGULAR_VELOCITY * dt);

        X.set(0, 0, satCoordinates.getX());
        X.set(1, 0, satCoordinates.getY());
        X.set(2, 0, satCoordinates.getZ());

        XCorr = R3.mult(X);

        //Log.d("Element 0: ", String.valueOf(Xcorr.get(0,0)));
        //Log.d("Element 1: ", String.valueOf(Xcorr.get(1,0)));
        //Log.d("Element 2: ", String.valueOf(Xcorr.get(2,0)));

        satCoordinates = new Coordinates(XCorr.get(0,0), XCorr.get(1,0), XCorr.get(2,0),
                satCoordinates.getxDot(), satCoordinates.getyDot(), satCoordinates.getzDot(), tEmis);

        // Previous epoch satellite coordinates
        X.set(0, 0, prevSatCoordinates.getX());
        X.set(1, 0, prevSatCoordinates.getY());
        X.set(2, 0, prevSatCoordinates.getZ());

        XCorr = R3.mult(X);

        prevSatCoordinates = new Coordinates(XCorr.get(0,0), XCorr.get(1,0), XCorr.get(2,0),
                prevSatCoordinates.getxDot(), prevSatCoordinates.getyDot(), prevSatCoordinates.getzDot(), tEmis-1);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Compute the rotation matrix R3 (around the z-axis).
     * @param angle Rotation angle
     */
    private SimpleMatrix getRotationR3(double angle)
    {
        SimpleMatrix R3 = new SimpleMatrix(3,3);

        R3.set(0,0,  1 * Math.cos(angle));
        R3.set(0,1,  1 * Math.sin(angle));
        R3.set(0,2,  0);
        R3.set(1,0,  -1 * Math.sin(angle));
        R3.set(1,1,  1 * Math.cos(angle));
        R3.set(1,2,  0);
        R3.set(2,0,  0);
        R3.set(2,1,  0);
        R3.set(2,2,  1);

        return R3;
    }

public double ionoCrrBDS(double E, double A, KeplerianEphemeris ep)
{
    // Constants
    // Radius of Earth
    double R = 6378e3;
    // Height of Ionosphere
    double h = 375e3;
    // Elevation of satellite in Radians
    E = Math.toRadians(E);
    A = Math.toRadians(A);

    // User co-ordinates in ECEF & Lat-Lon
    Coordinates ecefUserCoord = this.userCoord;
    LatLngAlt geoUserCoord = new LatLngAlt(ecefUserCoord);

    double lam = Math.toRadians(geoUserCoord.getLongitude());
    double phi = Math.toRadians(geoUserCoord.getLatitude());

    double Chi = (Math.PI/2) -  E - Math.asin((R/(R+h)) * Math.cos(E));
    double phim = Math.asin((Math.sin(phi) * Math.cos(Chi)) + Math.cos(phi) * Math.sin(Chi) * Math.cos(A));
    double lamm = lam + Math.asin((Math.sin(Chi) * Math.sin(A))/Math.cos(phim));

    double A2 = ep.getAl0() + ep.getAl1() * Math.pow(Math.abs(phim/Math.PI),1) + ep.getAl2() * Math.pow(Math.abs(phim/Math.PI),2) + ep.getAl3() * Math.pow(Math.abs(phim/Math.PI),3);
    if(A2 < 0)
    {
        A2 = 0;
    }

    double A4 = ep.getBt0() + ep.getBt1() * Math.pow(Math.abs(phim/Math.PI),1) + ep.getBt2() * Math.pow(Math.abs(phim/Math.PI),2) + ep.getBt3() * Math.pow(Math.abs(phim/Math.PI),3);
    if (A4 >= 172800)
    {
        A4 = 172800;
    }
    else if(A4 < 72000)
    {
        A4 = 72000;
    }

    double tTemp = ep.getTow() + lamm * 43200/Math.PI;
    double t = tTemp % 86400;
    double iDt;

    if (Math.abs(t - 50400) >= A4/4)
    {
         iDt = 5e-9;
    }
    else
    {
        iDt = 5e-9 + A2 * Math.cos((2 * Math.PI * (t - 50400))/A4);
    }

    double iVar = Math.pow(R/(R+h) * Math.cos(E),2);

    return iDt * 1 / Math.sqrt(1 - iVar);
}

    public double ionoCrrGPS(double E, double A, KeplerianEphemeris ep)
    {
        // Constants
        // Radius of Earth
        //double R = 6378e3;
        // Height of Ionosphere
        //double h = 375e3;

        // Elevation of satellite in semi circles
        E = Math.toRadians(E);
        E = E / Math.PI;
        A = Math.toRadians(A);
        A = A / Math.PI;

        // User co-ordinates in ECEF, Lat-Lon (Deg) & semi circles
        Coordinates ecefUserCoord = this.userCoord;
        LatLngAlt geoUserCoord = new LatLngAlt(ecefUserCoord);

        double lam = Math.toRadians(geoUserCoord.getLongitude());
        lam = lam / Math.PI;
        double phi = Math.toRadians(geoUserCoord.getLatitude());
        phi = phi / Math.PI;

        double Chi = (0.0137/(E + 0.11)) - 0.022;

        double phiI = phi + (Chi * Math.cos(A));
        if (phiI > 0.416)
        {
            phiI = 0.416;
        }
        else if(phiI < -0.416)
        {
            phiI = -0.416;
        }

        double lamI = lam + ((Chi * Math.sin(A))/Math.cos(phiI));

        double phiM = phiI + (0.064 * Math.cos(lamI - 1.617));

        double F = 1.0 + (16.0 * Math.pow(0.53 - E,3));

        double AMP = ep.getAl0() + ep.getAl1() * Math.pow(phiM,1) + ep.getAl2() * Math.pow(phiM,2) + ep.getAl3() * Math.pow(phiM,3);
        if (AMP < 0)
        {
            AMP = 0;
        }

        double PER = ep.getBt0() + ep.getBt1() * Math.pow(phiM,1) + ep.getBt2() * Math.pow(phiM,2) + ep.getBt3() * Math.pow(phiM,3);
        if(PER < 72000)
        {
            PER = 72000;
        }

        double t = ep.getTow() + (4.32 * Math.pow(10,4) * lamI);
        if (t >= 86400)
        {
            t = t - 86400;
        }
        else if (t < 0)
        {
            t = t + 86400;
        }

        double x = (2 * Math.PI * (t - 50400)) / PER;


        if (Math.abs(x) < 1.57)
        {
            return F * (5e-9 + AMP * (1 - (Math.pow(x,2)/2) + (Math.pow(x,4)/24)));
        }
        else
        {
            return F * 5e-9;
        }

    }

    //----------------------------------------------------------------------------------------------

//    public void setEphCorrections(PreciseCorrection ephCorrections) {
//        this.ephCorrections = ephCorrections;
//    }

    //----------------------------------------------------------------------------------------------

    public GNSSEphemeris getEph() {
        return eph;
    }

    //----------------------------------------------------------------------------------------------

    public Coordinates getSatCoordinates() {
        return satCoordinates;
    }

    //----------------------------------------------------------------------------------------------

    public double getGeomTransmissionTime() {
        return geomTransmissionTime;
    }

    //----------------------------------------------------------------------------------------------

    public double getSatElevation() {
        return satElevation;
    }

    public double getSatAzimuth() {
        return satAzimuth;
    }

    public double getIonoCorr(){return ionoCorr;}

    //----------------------------------------------------------------------------------------------

    public double getDtSat() {
        return dtSat;
    }

    //----------------------------------------------------------------------------------------------

    public Coordinates getPrevSatCoordinates() {
        return prevSatCoordinates;
    }

    //----------------------------------------------------------------------------------------------

    public SimpleMatrix getdX_precise() {
        return dX_precise;
    }

    //----------------------------------------------------------------------------------------------

    public double getdC_precise() {
        return dC_precise;
    }

    //----------------------------------------------------------------------------------------------


    public PreciseCorrection getEphCorrections() {
        return ephCorrections;
    }

    public SatelliteCodeBiases.CodeBias getCodeBias() {
        return codeBias;
    }


/*        public static void main(String[] args) throws IOException
    {

        GlonassEphemeris eph = new GlonassEphemeris();
        eph.setX(5164.3251953125);
        eph.setY(-23635.1806640625);
        eph.setZ(8064.2255859375);
        eph.setX_dot(0.18548011779785156);
        eph.setY_dot(-1.1109638214111328);
        eph.setZ_dot(-3.38399600982666);
        eph.setX_ddot(0.0);
        eph.setY_ddot(9.313225746154785e-10);
        eph.setZ_ddot(-9.313225746154785e-10);

        eph.setTb(765);
        eph.setTk(45000);

        eph.setGamma_n(9.09494701772928e-13);
        eph.setTau_n(4.08954918384552E-4);

        GregorianCalendar date = new GregorianCalendar();
        date.set(Calendar.YEAR, 2020);
        date.set(Calendar.MONTH, 1);
        date.set(Calendar.DAY_OF_MONTH,5);
        eph.setEphemerisDate(date);



        SimpleMatrix inertial_coordinates = new SimpleMatrix(3,1);
        inertial_coordinates.set(0,0, eph.getX());
        inertial_coordinates.set(1,0, eph.getY());
        inertial_coordinates.set(2,0, eph.getZ());


        System.out.println(eph.toString());


        SatellitePositionGNSS pos = new SatellitePositionGNSS(eph, 770*60, null, null);

        GlonassEphemeris eph = new GlonassEphemeris();
        eph.setX(24635.6748046875);
        eph.setY(-4060.5107421875);
        eph.setZ(5093.74951171875);
        eph.setX_dot(-0.7302684783935547);
        eph.setY_dot(-0.040630340576171875);
        eph.setZ_dot(3.5195655822753906);
        eph.setX_ddot(-9.313225746154785E-10);
        eph.setY_ddot(-2.7939677238464355E-9);
        eph.setZ_ddot(-1.862645149230957E-9);

        eph.setTb(1245);
        eph.setTk(29910);

        eph.setGamma_n(9.09494701772928E-13);
        eph.setTau_n(-2.4645589292049408E-5);

        GregorianCalendar date2 = new GregorianCalendar();
        date2.set(Calendar.YEAR, 2020);
        date2.set(Calendar.MONTH, 1);
        date2.set(Calendar.DAY_OF_MONTH,10);
        eph.setEphemerisDate(date2);


        System.out.println(eph.toString());


        SatellitePositionGNSS pos2 = new SatellitePositionGNSS(eph, 1250*60, null, null);



    }*/

//        double tow = 383400;
//
//        //Coordinates trueSatCoordinate = new Coordinates(-18416679.678594064, -13791301.967686517, -13737154.229846274);
//        Coordinates trueSatCoordinate = new Coordinates(-18416750.07578751, -13791207.95992266, -13737154.229846274);
//        Coordinates approxUserCoord = new Coordinates(4343426.0, -124910.0, 4653463.0);
//        Options options = new Options();
//
//        GNSSEphemeris eph = new GNSSEphemeris();
//
//        eph.setGnssSystem(1);
//        eph.setPrn(1);
//        eph.setToe(381584.0);
//        eph.setAf0(-3.99947166443e-05);
//        eph.setAf1(-9.66338120634e-12);
//        eph.setAf2(0.0);
//        eph.setCrc(189.40625);
//        eph.setCrs(11.96875);
//        eph.setCic(3.35276126862e-08);
//        eph.setCis(-6.14672899246e-08);
//        eph.setCuc(5.60656189919e-07);
//        eph.setCus(1.02929770947e-05);
//        eph.setSquareA(5153.65583992);
//        eph.setEc(0.00892822921742);
//        eph.setM0(2.82771526885);
//        eph.setOmega0(3.05241315139);
//        eph.setOmega(0.71538381991);
//        eph.setOmegaDot(-7.64924719328e-09);
//        eph.setI0(0.976218729714);
//        eph.setIdot(5.63237746827e-10);
//        eph.setDeltaN(3.88230457071e-09);
//
//        SatellitePositionGNSS satellitePosition = new SatellitePositionGNSS(eph, tow, approxUserCoord, options);
//        satellitePosition.computeSatellitePosition(tow);
//        //System.out.println("" + (tow) + " " + satellitePosition.satCoordinates);
//
//        satellitePosition.applyEarthRotation(0.07);
//
//        double dist = Utils.distanceBetweenCoordinates(trueSatCoordinate, satellitePosition.getSatCoordinates());
//
//        System.out.println("" + dist + ", " + satellitePosition.getDtSat());
//    }

}

