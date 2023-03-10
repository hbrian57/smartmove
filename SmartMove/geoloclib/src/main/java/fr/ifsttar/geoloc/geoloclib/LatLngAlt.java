///=================================================================================================
// Class LatLngAlt
//      Author :  Jose Gilberto RESENDIZ FONSECA
// Modified by :  Antoine GRENIER - 2019/09/06
//        Date :  2019/09/06
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
package fr.ifsttar.geoloc.geoloclib;

import org.gogpsproject.Constants;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Class LatLngAlt
 * Calculate the coordinates geographic
 */
public class LatLngAlt {

    private double latitude;
    private double longitude;
    private double altitude;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor by default
     */
    public LatLngAlt(){
        this.latitude = 0.000000;
        this.longitude = 0.000000;
        this.altitude = 0.00;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor initializing geographic coordinates with Earth-fixed coordinates
     * @param mCoordinates Coordinates
     */
    public LatLngAlt(Coordinates mCoordinates){
        setLatLngAlt(mCoordinates.getX(),mCoordinates.getY(), mCoordinates.getZ());
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor with parameters
     * @param latitude Latitude [decimal degrees]
     * @param longitude Longitude [decimal degrees]
     * @param altitude Altitude [m]
     */
    public LatLngAlt(double latitude, double longitude, double altitude){
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Method to change coordinates cartesian to coordinates geographic
     * @param x ECEF x
     * @param y ECEF y
     * @param z ECEF z
     */
    private void setLatLngAlt(double x, double y, double z) {

        // Longitude in radians
        double lambda = Math.atan2(y,x);

        //WGS84 ellipsoid constants
        int a = 6378137; // semi-Major axis
        double f = 1.0/298.257223563; //Flattening of the Earth
        double b = (1.0 - f) * a; // Semi-Minor axis
        double e2 = f * (2.0 - f); // square of eccentricity
        double ae2 = a * e2;
        double bep2 = b * e2/(1 - e2);

        // Starting value for parametric latitude
        double rho = Math.sqrt(Math.pow(Math.abs(x),2) + Math.pow(Math.abs(y),2)); // Radial distance from polar axis
        double r = Math.sqrt(Math.pow(Math.abs(rho),2) + Math.pow(Math.abs(z),2));
        double u = a * rho;
        double v = b * z * (1.0 + bep2/r);
        double sign_u;
        double sign_v;
        if (u > 0)
        {
            sign_u = 1;
        }
        else if (u < 0)
        {
            sign_u = -1;
        }
        else
        {
            sign_u = 0;
        }

        if (v > 0)
        {
            sign_v = 1;
        }
        else if (v < 0)
        {
            sign_v = -1;
        }
        else
        {
            sign_v = 0;
        }
        double c_beta = sign_u / Math.sqrt(Math.abs(1) + Math.pow(Math.abs(v/u),2));
        double s_beta = sign_v / Math.sqrt(Math.abs(1) + Math.pow(Math.abs(u/v),2));

        // Fixed-point iteration with Bowring's formula (typically converges within three iterations or less)
        double cos_prev;
        double sin_prev;
        double sign_au;
        double sign_bv;
        double au;
        double bv;
        int count = 0;
        boolean itr = true;
        while (itr && count<5)
        {
            cos_prev = c_beta;
            sin_prev = s_beta;
            u = rho - ae2 * Math.pow(c_beta,3);
            v = z + bep2 * Math.pow(s_beta,3);
            au = a * u;
            bv = b * v;
            if (au > 0)
            {
                sign_au = 1;
            }
            else if (au < 0)
            {
                sign_au = -1;
            }
            else
            {
                sign_au = 0;
            }

            if (bv > 0)
            {
                sign_bv = 1;
            }
            else if (bv < 0)
            {
                sign_bv = -1;
            }
            else
            {
                sign_bv = 0;
            }
            c_beta = sign_au / Math.sqrt(Math.abs(1) + Math.pow(Math.abs(bv/au),2));
            s_beta = sign_bv / Math.sqrt(Math.abs(1) + Math.pow(Math.abs(au/bv),2));
            itr = Math.sqrt(Math.pow(Math.abs(c_beta - cos_prev), 2) + Math.pow(Math.abs(s_beta - sin_prev), 2)) > 2.220446049250313e-16;
            count = count + 1;
        }
        double phi = Math.atan2(v,u);
        double N = a / Math.sqrt(1 - e2 * Math.pow(Math.sin(phi),2));
        double h = rho * Math.cos(phi) + (z + e2 * N * Math.sin(phi)) * Math.sin(phi) - N;
        longitude = lambda * Constants.RAD2DEG;
        latitude = phi * Constants.RAD2DEG;
        altitude = h;

        /*double phi = Math.atan2(z, (1 - e_2) * r);
        double h = 0;

        double diff = 1;
        double prevPhi = 0;
        while(diff > 10e-10)
        {
            prevPhi = phi;

            double N = a / Math.sqrt(1 - e_2 * Math.pow(Math.sin(phi),2));
            h = r / Math.cos(phi) - N;
            phi = Math.atan2(z, (1 - e_2 * N / (N + h)) * r);

            diff = Math.abs(prevPhi - phi);
        }*/

    }

    //----------------------------------------------------------------------------------------------

    public double getAltitude() {
        return altitude;
    }

    //----------------------------------------------------------------------------------------------

    public double getLatitude() {
        return latitude;
    }

    //----------------------------------------------------------------------------------------------

    public double getLongitude() {
        return longitude;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Override toString
     * @return string of coordinates geographic
     */
    @Override
    public String toString() {
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        NumberFormat formatAlt = new DecimalFormat("#.####", otherSymbols);
        NumberFormat formatLatLon = new DecimalFormat("#.###########", otherSymbols);
        return formatLatLon.format(latitude) + "," + formatLatLon.format(longitude) +","+ formatAlt.format(altitude);
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public static void main(String[] args) throws IOException
    {
        Coordinates coordinates = new Coordinates(4343426.492, -124910.609, 4653463.091);

        LatLngAlt latLngAlt = new LatLngAlt();
        System.out.println(latLngAlt.toString());

        latLngAlt = new LatLngAlt(coordinates);
        System.out.println(latLngAlt.toString());

    }
}
