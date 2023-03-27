///=================================================================================================
// Class TropoCorrections
//      Author :  Antoine GRENIER - 2019/09/06
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

import org.gogpsproject.Constants;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import fr.ifsttar.geoloc.geoloclib.Coordinates;
import fr.ifsttar.geoloc.geoloclib.LatLngAlt;
import fr.ifsttar.geoloc.geoloclib.satellites.SatellitePositionGNSS;

/**
 * Class TropoCorrections
 * Compute the tropospheric corrections to be applied on the raw measurements.
 *
 * The model used is the developed in the software GIPSY/OASIS-II by :
 * Webb, F. and Zumberge, 1993, An Introduction to GIPSY/OASIS-II. Jet Propulsion Laboratory,
 * Pasadena, CA, USA.
 *
 * Reference [ESA, GNSS Data Processing, Volume 1: Fundamentals and Algorithms]
 *
 * This code is licensed under the GNU Lesser General Public License v3.0.
 */
public class TropoCorrections {

    // See ESA GNSS Book - Table 5.2
    // [LATITUDE: {{AVERAGE: a, b, c} {AMPLITUDE: a, b, c} {HEIGHT CORRECTION: a, b, c}}]
    public static final HashMap<Integer, double[][]> HYDROSTATIC_MAPPING = new HashMap <Integer, double[][]> () {{
        put(15, new double[][]{{1.2769934e-3, 2.9153695e-3, 62.610505e-3}, {0.0, 0.0, 0.0}, {2.53e-5, 5.49e-3, 1.14e-3}});
        put(30, new double[][]{{1.2683230e-3, 2.9152299e-3, 62.837393e-3}, {1.2709626e-5, 2.1414979e-5, 9.0128400e-5}, {2.53e-5, 5.49e-3, 1.14e-3}});
        put(45, new double[][]{{1.2465397e-3, 2.9288445e-3, 63.721774e-3}, {2.6523662e-5, 3.0160779e-5, 4.3497037e-5}, {2.53e-5, 5.49e-3, 1.14e-3}});
        put(60, new double[][]{{1.2196049e-3, 2.9022565e-3, 63.824265e-3}, {3.4000452e-5, 7.2562722e-5, 84.795348e-5}, {2.53e-5, 5.49e-3, 1.14e-3}});
        put(75, new double[][]{{1.2045996e-3, 2.9024912e-3, 64.258455e-3}, {4.1202191e-5, 11.723375e-5, 170.37206e-5}, {2.53e-5, 5.49e-3, 1.14e-3}});
    }};

    // See ESA GNSS Book - Table 5.3
    // [LATITUDE: {a, b, c}]
    public static final HashMap<Integer, double[]> WET_MAPPING = new HashMap <Integer, double[]> () {{
        put(15, new double[]{5.8021897e-4, 1.4275268e-3, 4.3472961e-2});
        put(30, new double[]{5.6794847e-4, 1.5138625e-3, 4.6729510e-2});
        put(45, new double[]{5.8118019e-4, 1.4572752e-3, 4.3908931e-2});
        put(60, new double[]{5.9727542e-4, 1.5007428e-3, 4.4626982e-2});
        put(75, new double[]{6.1641693e-4, 1.7599082e-3, 5.4736038e-2});
    }};

    // See ESA GNSS BOOK - Section 5.4.2.2
    public static final double ALPHA_DRY = 2.3;  // in meters
    public static final double BETA_DRY = 0.116e-3;
    public static final double TR_Z0_WET = 0.1;  // in meters
    public static final double HYDROSTATIC_T0 = 28.0; // in DOY

    //Receiver coordinates
    private Coordinates userCoord;

//--------------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * @param userCoord Approx user coordinates
     */
    public TropoCorrections(Coordinates userCoord)
    {
        this.userCoord = userCoord;
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Compute the M_wet coefficient from the satellite position.
     *
     * @param satPos Satellite position.
     * @return Coefficient of the wet mapping function.
     */
    public double getMwet(SatellitePositionGNSS satPos)
    {
        double Mwet = 0.0;

        double a = 0.0;
        double b = 0.0;
        double c = 0.0;

        LatLngAlt satPosGeo = new LatLngAlt(satPos.getSatCoordinates());
        double elev = Math.toRadians(satPos.getSatElevation());

        int idx = selectClosestLatitude(satPosGeo.getLatitude());

        // No values for satellites below 15° and above 75°, we take the nearest one.
        if(idx == 15)
        {
            double[] params = WET_MAPPING.get(idx);

            a = params[0];
            b = params[1];
            c = params[2];
        }
        else if (idx == 0)
        {
            double[] params = WET_MAPPING.get(75);

            a = params[0];
            b = params[1];
            c = params[2];
        }
        //Otherwise we linearly interpolates the parameters.
        else
        {
            double[] params1 = WET_MAPPING.get(idx-15);
            double[] params2 = WET_MAPPING.get(idx);

            a = interpolateParam(idx-15, idx, satPosGeo.getLatitude(), params1[0], params2[0]);
            b = interpolateParam(idx-15, idx, satPosGeo.getLatitude(), params1[1], params2[1]);
            c = interpolateParam(idx-15, idx, satPosGeo.getLatitude(), params1[2], params2[2]);
        }

        Mwet = getNormalisedMapping(a, b, c, elev);

        return Mwet;
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Compute the M_dry coefficient from the satellite position.
     *
     * @param satPos Satellite position.
     * @return Coefficient of the dry mapping function.
     */
    private double getMdry(SatellitePositionGNSS satPos)
    {
        double Mdry = 0.0;
        double a = 0.0;
        double b = 0.0;
        double c = 0.0;

        LatLngAlt satPosGeo = new LatLngAlt(satPos.getSatCoordinates());
        double elev = Math.toRadians(satPos.getSatElevation());

        // Compute m
        int idx = selectClosestLatitude(satPosGeo.getLatitude());

        // Same issue than the M_wet part, see above function.
        if(idx == 15)
        {
            double[][] params = HYDROSTATIC_MAPPING.get(idx);

            a = getHydrostaticParam(params[0][0], params[1][0]);
            b = getHydrostaticParam(params[0][1], params[1][1]);
            c = getHydrostaticParam(params[0][2], params[1][2]);
        }
        else if (idx == 0)
        {
            double[][] params = HYDROSTATIC_MAPPING.get(75);

            a = getHydrostaticParam(params[0][0], params[1][0]);
            b = getHydrostaticParam(params[0][1], params[1][1]);
            c = getHydrostaticParam(params[0][2], params[1][2]);
        }
        else
        {
            double[][] params1 = HYDROSTATIC_MAPPING.get(idx-15);
            double[][] params2 = HYDROSTATIC_MAPPING.get(idx);

            double tmpAvg = 0.0;
            double tmpAmp = 0.0;
            tmpAvg = interpolateParam(idx-15, idx, satPosGeo.getLatitude(), params1[0][0], params2[0][0]);
            tmpAmp = interpolateParam(idx-15, idx, satPosGeo.getLatitude(), params1[1][0], params2[1][0]);
            a = getHydrostaticParam(tmpAvg, tmpAmp);

            tmpAvg = interpolateParam(idx-15, idx, satPosGeo.getLatitude(), params1[0][1], params2[0][1]);
            tmpAmp = interpolateParam(idx-15, idx, satPosGeo.getLatitude(), params1[1][1], params2[1][1]);
            b = getHydrostaticParam(tmpAvg, tmpAmp);

            tmpAvg = interpolateParam(idx-15, idx, satPosGeo.getLatitude(), params1[0][2], params2[0][2]);
            tmpAmp = interpolateParam(idx-15, idx, satPosGeo.getLatitude(), params1[1][2], params2[1][2]);
            c = getHydrostaticParam(tmpAvg, tmpAmp);

        }

        Mdry += getNormalisedMapping(a, b, c, elev);

        //System.out.println("" + elev);

        // Compute DELTA_m
        a = HYDROSTATIC_MAPPING.get(idx)[2][0];
        b = HYDROSTATIC_MAPPING.get(idx)[2][1];
        c = HYDROSTATIC_MAPPING.get(idx)[2][2];

        Mdry += (1/Math.sin(elev) - getNormalisedMapping(a, b, c, elev)) * satPosGeo.getAltitude();

        return Mdry;
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Compute the hydrostatic parameter to it's value in the current day of year.
     * @param average Average value of the parameter.
     * @param amplitude Amplitude value of the parameter.
     * @return Final value of parameter
     */
    private double getHydrostaticParam(double average, double amplitude)
    {
        double param = 0.0;

        Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
        int doy = localCalendar.get(Calendar.DAY_OF_YEAR); //Current day of year

        //Log.i("TROPO", "DOY: " + doy);

        param = average - amplitude * Math.cos(2*Math.PI*(doy - HYDROSTATIC_T0)/365.25);

        return param;
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Compute the constant part of the tropospheric correction.
     * @param satPos Satellite position.
     * @return Tr0
     */
    public double getTr0(SatellitePositionGNSS satPos)
    {
        double Tr0 = 0.0;
        double TrDry = 0.0;

        TrDry = ALPHA_DRY * Math.exp(-BETA_DRY * satPos.getSatCoordinates().getLatLngAlt().getAltitude());

        Tr0 = TrDry * getMdry(satPos) + TR_Z0_WET * getMwet(satPos);

        return Tr0;
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Linearly interpolate a value between two points.
     * @param lat1 Latitude of 1st point
     * @param lat2 Latitude of 2nd point
     * @param mlat Latitude of the wanted value
     * @param a1   Value of the 1st point
     * @param a2   Value of the 2nd point
     * @return The interpolated value
     */
    private double interpolateParam(double lat1, double lat2, double mlat, double a1, double a2)
    {
        double inter = 0.0;

        inter = a1 + (mlat - lat1) * (a2 - a1)/(lat2 - lat1);

        return inter;
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Compute the value of the mapping normalised to unity at zenith (see reference).
     * @param a Parameter a
     * @param b Parameter b
     * @param c Parameter c
     * @param elev Satellite elevation
     * @return Map value
     */
    private double getNormalisedMapping(double a, double b, double c, double elev)
    {
        double m = 0.0;

        m  = (1 + a / (1 + b / (1 + c)));
        m /= (Math.sin(elev) + a / (Math.sin(elev) + b / (Math.sin(elev) + c)));

        return m;
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Select closest latitude in the WET_MAPPING Map.
     * @param satLat Satellite latitude
     * @return Latitude
     */
    private int selectClosestLatitude(double satLat)
    {
        int idx = 0;
        for(HashMap.Entry<Integer, double[]> entry: WET_MAPPING.entrySet())
        {
            if(entry.getKey() > satLat)
            {
                idx = entry.getKey();
                break;
            }
        }

        return idx;
    }


//--------------------------------------------------------------------------------------------------

    /**
     * Troposphere correction based on the Saastamoinen model. Created by Sebastian Ciuban on 10/02/2018.
     * Source: GNSS Compare, the Galfins
     * @param _elevation Satellite elevation
     * @return Correction
     */
    public double getSaastamoinenCorrection(double _elevation)
    {

        // Get the user's height
        double height = userCoord.getLatLngAlt().getAltitude();

        // Assign the elevation information to a new variable
        double elevation = _elevation;
        double tropoCorr = 0;

        if (height > 5000)
            return -1;

        elevation = Math.toRadians(Math.abs(elevation));
        if (elevation == 0){
            elevation = elevation + 0.01;
        }

        // Numerical constants and tables for Saastamoinen algorithm
        // (troposphere correction)
        final double hr = 50.0;
        final int[] ha = {0, 500, 1000, 1500, 2000, 2500, 3000, 4000, 5000 };
        final double[] ba = { 1.156, 1.079, 1.006, 0.938, 0.874, 0.813, 0.757, 0.654, 0.563 };

        // Saastamoinen algorithm
        double P = Constants.STANDARD_PRESSURE * Math.pow((1 - 0.0000226 * height), 5.225);
        double T = Constants.STANDARD_TEMPERATURE - 0.0065 * height;
        double H = hr * Math.exp(-0.0006396 * height);

        // If height is below zero, keep the maximum correction value
        double B = ba[0];
        // Otherwise, interpolate the tables
        if (height >= 0) {
            int i = 1;
            while (height > ha[i]) {
                i++;
            }
            double m = (ba[i] - ba[i - 1]) / (ha[i] - ha[i - 1]);
            B = ba[i - 1] + m * (height - ha[i - 1]);
        }

        double e = 0.01
                * H
                * Math.exp(-37.2465 + 0.213166 * T - 0.000256908
                * Math.pow(T, 2));

        tropoCorr = ((0.002277 / Math.sin(elevation))
                * (P - (B / Math.pow(Math.tan(elevation), 2))) + (0.002277 / Math.sin(elevation))
                * (1255 / T + 0.05) * e);

        return tropoCorr;
    }

}

//END OF FILE