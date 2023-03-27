///=================================================================================================
// Class Utils
//      Author :  Antoine GRENIER
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
package fr.ifsttar.geoloc.geoloclib;

import android.location.GnssStatus;

import org.ejml.simple.SimpleMatrix;

import java.util.Calendar;
import java.util.GregorianCalendar;




/**
 * Class Utils
 *
 * Miscellaneous of useful functions.
 */
public class Utils {

    /**
     * Get the constellation letter from constellation ID.
     * @param system System ID from Google API
     * @return Constellation letter
     */
    static public char getConstellationLetter(int system)
    {
        switch (system)
        {
            case GnssStatus.CONSTELLATION_GPS:
                return 'G';
            case GnssStatus.CONSTELLATION_GALILEO:
                return 'E';
            case GnssStatus.CONSTELLATION_BEIDOU:
                return 'C';
            case GnssStatus.CONSTELLATION_GLONASS:
                return 'R';
            default:
                return 'U';
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Get the tag containing satellite system and PRN for indexing in HashMaps.
     * @param system Constellation
     * @param prn PRN
     * @return Tag
     */
    static public String getFormattedSatIndex(int system, int prn)
    {
        char systemStr = Utils.getConstellationLetter(system);
        String str = String.format("%c %02d", systemStr, prn);

        return str;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Extend a matrix by a specified number of row and column.
     * @param mat Matrix to be extended
     * @param nbRows Number of rows to add
     * @param nbCols Number of columns to add
     * @return Extended matrix
     */
    static public SimpleMatrix extendMatrix(SimpleMatrix mat, int nbRows, int nbCols)
    {
        SimpleMatrix tmp = new SimpleMatrix(mat.numRows() + nbRows, mat.numCols() + nbCols);

        for(int i = 0; i<mat.numRows(); i++)
        {
            for(int j = 0; j<mat.numCols(); j++)
            {
                tmp.set(i,j,mat.get(i,j));
            }
        }

        mat = tmp.copy();
        return mat;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Compute the geometrical distance between two coordinates.
     * @param coordA First coordinates
     * @param coordB Second coordinates
     * @return
     */
    static public double distanceBetweenCoordinates(Coordinates coordA, Coordinates coordB)
    {
        double dist = Math.sqrt(Math.pow(coordB.getX() - coordA.getX(), 2)
                + Math.pow(coordB.getY() - coordA.getY(), 2)
                + Math.pow(coordB.getZ() - coordA.getZ(), 2));

        return dist;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Transform geographical coordinates into ECEF.
     * @param _lat Latitude (decimal degrees)
     * @param _lon Longitude (decimal degrees)
     * @param _h Height to ellipsoid
     * @return Coordinates object
     */
    static public Coordinates computeECEF(double _lat, double _lon, double _h) {
        final long a = 6378137;
        final double finv = 298.257223563d;

        double dphi = _lat;
        double dlambda = _lon;
        double h = _h;

        // compute degree-to-radian factor
        double dtr = Math.PI/180;

        // compute square of eccentricity
        double esq = (2-1/finv)/finv;
        double sinphi = Math.sin(dphi*dtr);
        // compute radius of curvature in prime vertical
        double N_phi = a/Math.sqrt(1-esq*sinphi*sinphi);

        // compute P and Z
        // P is distance from Z axis
        double P = (N_phi + h)*Math.cos(dphi*dtr);
        double Z = (N_phi*(1-esq) + h) * sinphi;
        double X = P*Math.cos(dlambda*dtr);
        double Y = P*Math.sin(dlambda*dtr);

        return new Coordinates(X,Y,Z);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Function to perform cross product, not handled by EJML.
     * @param A
     * @param B
     * @return
     */
    static public SimpleMatrix crossMult(SimpleMatrix A, SimpleMatrix B)
    {
        SimpleMatrix C = new SimpleMatrix(3,1);

        C.set(0,0, A.get(1,0) * B.get(2,0) - A.get(2,0)* B.get(1,0));
        C.set(1,0, A.get(2,0) * B.get(0,0) - A.get(0,0)* B.get(2,0));
        C.set(2,0, A.get(0,0) * B.get(1,0) - A.get(1,0)* B.get(0,0));

        return C;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Get ENU coordinates of the satellite relative to the given position.
     * Taken from the GNSS Compare library.
     */
    static public SimpleMatrix getENU(Coordinates refCoordinates, Coordinates coord)
    {
        Coordinates coordinates = coord;

        SimpleMatrix rSat = coordinates.getSimpleMatrix();
        SimpleMatrix rUser = refCoordinates.getSimpleMatrix();
        SimpleMatrix diffVec;
        SimpleMatrix enu;

        diffVec = rSat.minus(rUser);

        LatLngAlt geoUserCoord = new LatLngAlt(refCoordinates);

        //Compute rotation matrix from ECEF to ENU

        double lam = Math.toRadians(geoUserCoord.getLongitude());
        double phi = Math.toRadians(geoUserCoord.getLatitude());

        double cosLam = Math.cos(lam);
        double cosPhi = Math.cos(phi);
        double sinLam = Math.sin(lam);
        double sinPhi = Math.sin(phi);

        double[][] data = new double[3][3];
        data[0][0] = -sinLam;
        data[0][1] = cosLam;
        data[0][2] = 0;
        data[1][0] = -sinPhi * cosLam;
        data[1][1] = -sinPhi * sinLam;
        data[1][2] = cosPhi;
        data[2][0] = cosPhi * cosLam;
        data[2][1] = cosPhi * sinLam;
        data[2][2] = sinPhi;

        SimpleMatrix R = new SimpleMatrix(data);

        //Compute ENU coordinates of satellite
        enu = R.mult(diffVec);

        return enu;
    }

    /**
     * Converts a gregorian date to a julian ephemeris date (JDE)
     * Details on algorithm implemented at https://fr.wikipedia.org/wiki/Jour_julien#Jour_julien_astronomique_(AJD)_ou_jour_julien_des_%C3%A9ph%C3%A9m%C3%A9rides_(JDE)
     * @param date the gregorian date we want in julian date
     * @return
     */
    public static double toJulianDate(GregorianCalendar date) {
        double julianDate = 0;

        float year = date.get(Calendar.YEAR);
        float month = date.get(Calendar.MONTH)+1; //month are from 0 to 11
        float day = date.get(Calendar.DAY_OF_MONTH);
        float hour = date.get(Calendar.HOUR_OF_DAY);
        float min = date.get(Calendar.MINUTE);
        float sec = date.get(Calendar.SECOND);

        if (month == 1 || month == 2){
            year += -1;
            month += 12;
        }
        double S = Math.floor(year/100);
        double B = (2 - S + Math.floor(S/4));


        julianDate += Math.floor((year + 4716)*365.25) + Math.floor(30.6001*(month+1)) + day + B - 1524;

        julianDate += (hour -12)/24;
        julianDate +=  min/(24*60);
        julianDate +=  sec/(24*3600);

        return julianDate;
    }

    /**
     * uses the dateToString method from gogps.Utils
     * @param date
     * @return
     */
    public static String dateToString(GregorianCalendar date){

        return org.gogpsproject.Utils.dateToString(date);
    }

    /**
     * Creates a z rotation matrix
     * @param theta angle of rotation
     * @return
     */
    public static SimpleMatrix rotation_matrix(double theta){

        SimpleMatrix R = new SimpleMatrix(3,3);
        R.zero();
        R.set(0,0,Math.cos(theta));
        R.set(0,1,-Math.sin(theta));

        R.set(1,0,Math.sin(theta));
        R.set(1,1,Math.cos(theta));

        R.set(2,2,1);

        return R;
    }

    // TODO
    public static double fromTODtoTOW(double t){
        return t;
    }


}
