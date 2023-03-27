///=================================================================================================
// Class Coordinates
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
package fr.ifsttar.geoloc.geoloclib;

import android.location.Location;

import org.ejml.simple.SimpleMatrix;
import org.gogpsproject.Constants;

import java.io.Serializable;

/**
 * Class coordinates
 *
 * Store coordinates in ECEF and WGS84 and allow transformation between reference systems.
 */
public class Coordinates implements Serializable {

    //Earth-fixed coordinates
    private double x;
    private double y;
    private double z;

    // ECEF velocities
    private double xDot;
    private double yDot;
    private double zDot;

    // ENU velocities
    private double eDot;
    private double nDot;
    private double uDot;

    /**
     * geographic coordinates
     */
    private LatLngAlt latLngAlt;

    private double tow;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor by default
     */
    public Coordinates(){
        this.x = 0.000;
        this.y = 0.000;
        this.z = 0.000;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor of recopy
     * @param c coordinates
     */
    public  Coordinates(Coordinates c){
        this.x = c.getX();
        this.y = c.getY();
        this.z = c.getZ();

        this.latLngAlt = new LatLngAlt(this);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor
     *@param x : x of orbital plane
     * @param y : y of orbital plane
     * @param z : corrected inclination
     */
    public  Coordinates(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;

        this.latLngAlt = new LatLngAlt(this);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * @param x x
     * @param y y
     * @param z z
     * @param tow Time of Week
     */
    public  Coordinates(double x, double y, double z, double tow){
        this.x = x;
        this.y = y;
        this.z = z;
        this.tow = tow;

        this.latLngAlt = new LatLngAlt(this);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * @param _x x
     * @param _y y
     * @param _z z
     * @param _xDot velocity in x
     * @param _yDot velocity in y
     * @param _zDot velocity in z
     */
    public Coordinates(double _x, double _y, double _z, double _xDot, double _yDot, double _zDot, double _tow){
        this.x = _x;
        this.y = _y;
        this.z = _z;

        this.xDot = _xDot;
        this.yDot = _yDot;
        this.zDot = _zDot;

        this.tow = _tow;

        this.latLngAlt = new LatLngAlt(this);

        setEnuVelocity();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * @param location Google Location object
     */
    public Coordinates(Location location)
    {
        Coordinates c = Utils.computeECEF(
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude());

        this.x = c.getX();
        this.y = c.getY();
        this.z = c.getZ();

        this.latLngAlt = new LatLngAlt(this);
    }

    //----------------------------------------------------------------------------------------------

    public void setFromGeographics(double latitude, double longitude, double height)
    {
        latitude = Math.toRadians(latitude);
        longitude = Math.toRadians(longitude);

        double a = Constants.WGS84_SEMI_MAJOR_AXIS;
        double e = Constants.ELL_E_GPS;

        double N = a / Math.sqrt(1 - Math.pow(e,2) * Math.pow(Math.sin(latitude),2));

        x = (N + height) * Math.cos(latitude) * Math.cos(longitude);
        y = (N + height) * Math.cos(latitude) * Math.sin(longitude);
        z = (N * (1 - Math.pow(e,2)) + height) * Math.sin(latitude);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Get coordinates in matrix format.
     * @return matrix
     */
    public SimpleMatrix getSimpleMatrix()
    {
        SimpleMatrix mat = new SimpleMatrix(3,1);

        mat.set(0,0, getX());
        mat.set(1,0, getY());
        mat.set(2,0, getZ());

        return mat;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Computing x in Earth-fixed coordinates
     * @param xk x in orbital plane
     * @param omegak omega of orbital plane
     * @param yk y in orbital plane
     * @param ik inclination of orbital plane
     * @return x ECEF
     */
    private double setX(double xk, double omegak, double yk, double ik) {
        return xk*Math.cos(omegak) - yk*Math.cos(ik)*Math.sin(omegak);

    }

    //----------------------------------------------------------------------------------------------

    /**
     * Computing y in Earth-fixed coordinates
     * @param xk x in orbital plane
     * @param omegak omega of orbital plane
     * @param yk y in orbital plane
     * @param ik inclination of orbital plane
     * @return y ECEF
     */
    private double setY(double xk, double omegak, double yk, double ik){
        return xk*Math.sin(omegak) + yk*Math.cos(ik)*Math.cos(omegak);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * computing z in Earth-fixed coordinates
     * @param yk y in orbital plane
     * @param ik inclination of orbital plane
     * @return z ECEF
     */
    private double setZ(double yk, double ik){
        return yk*Math.sin(ik);
    }

    //----------------------------------------------------------------------------------------------

    public double getX() {
        return x;
    }

    //----------------------------------------------------------------------------------------------

    public double getY() {
        return y;
    }

    //----------------------------------------------------------------------------------------------

    public double getZ() {
        return z;
    }

    //----------------------------------------------------------------------------------------------

    public double getxDot() {
        return xDot;
    }

    //----------------------------------------------------------------------------------------------

    public double getyDot() {
        return yDot;
    }

    //----------------------------------------------------------------------------------------------

    public double getzDot() {
        return zDot;
    }

    //----------------------------------------------------------------------------------------------

    public double getTow() {
        return tow;
    }

    //----------------------------------------------------------------------------------------------

    public double geteDot() {
        return eDot;
    }

    //----------------------------------------------------------------------------------------------

    public double getnDot() {
        return nDot;
    }

    //----------------------------------------------------------------------------------------------

    public double getuDot() {
        return uDot;
    }

    //----------------------------------------------------------------------------------------------

    public double getVelocity()
    {
        SimpleMatrix vel = new SimpleMatrix(3,1);
        vel.set(0,0, xDot);
        vel.set(1,0, yDot);
        vel.set(2,0, zDot);
        return vel.normF();
    }


    //----------------------------------------------------------------------------------------------

    public LatLngAlt getLatLngAlt()
    {
        if(latLngAlt == null)
        {
            latLngAlt = new LatLngAlt(this);
        }

        return latLngAlt;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Compute the user velocity in ENU frame.
     */
    private void setEnuVelocity()
    {
        double lam = Math.toRadians(latLngAlt.getLongitude());
        double phi = Math.toRadians(latLngAlt.getLatitude());

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

        SimpleMatrix rDot =  new SimpleMatrix(3,1);
        rDot.set(0,0, xDot);
        rDot.set(1,0, yDot);
        rDot.set(2,0, zDot);

        SimpleMatrix enu = R.mult(rDot);

        eDot = enu.get(0,0);
        nDot = enu.get(1,0);
        uDot = enu.get(2,0);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Override toString
     * @return String of coordinates
     */
    @Override
    public String toString() {
        return "" + tow + "," + x + "," + y + "," + z;
    }
}
