///=================================================================================================
// Class KeplerianEphemeris
//      Author : Edgar LENHOF
//      Date : 2019/12/29
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

package org.gogpsproject.ephemeris;

import org.gogpsproject.ephemeris.GNSSEphemeris;

/**
 * Abstract Class KeplerianEphemeris
 */
public abstract class KeplerianEphemeris extends GNSSEphemeris {


    protected long tow; //time of week


    protected double toc;     //clock data reference time in seconds
    protected double af2;     //polynomial coefficient (sec/sec^2)
    protected double af1;     //polynomial coefficient (sec/sec)
    protected double af0;     //polynomial coefficient (seconds)

    protected double crs;     //Amplitude of the Sine Harmonic Correction Term to the Orbit Radius
    protected double deltaN;  //Mean Motion Difference From Computed Value
    protected double m0;      //Mean Anomaly at Reference Time
    protected double cuc;     //Amplitude of the Cosine Harmonic Correction Term to the Argument of Latitude
    protected double ec;      //Eccentricity
    protected double cus;     //Amplitude of the Sine Harmonic Correction Term to the Argument of Latitude
    protected double squareA; //Square Root of the Semi-Major Axis
    protected double toe;     //Reference Time Ephemeris
    protected double aodo;    //Age of data offset

    protected double cic;     //Amplitude of the Cosine Harmonic Correction Term to the Angle of Inclination
    protected double omega0;  //Longitude of Ascending Node of Orbit Plane at Weekly Epoch
    protected double cis;     //Amplitude of the Sine Harmonic Correction Term to the Angle of Inclination
    protected double i0;      //Inclination Angle at Reference Time
    protected double crc;     //Amplitude of the Cosine Harmonic Correction Term to the Orbit Radius
    protected double omega;   //Argument of Perigee
    protected double omegaDot;//Rate of Right Ascension
    protected double idot;    //Rate of Inclination Angle

    // Ionospheric Correction Parameters
    protected double Al0;
    protected double Al1;
    protected double Al2;
    protected double Al3;
    protected double Bt0;
    protected double Bt1;
    protected double Bt2;
    protected double Bt3;


    /**
     * Constructor for recopy of a Keplerian ephemeris
     * @param eph
     */
    public KeplerianEphemeris(KeplerianEphemeris eph)
    {
        super(eph);
        setWeek(eph.getWn());
        setIdot(eph.getIdot());
        setIode(eph.getIode());
        setToc(eph.getToc());
        setToe(eph.getToe());
        setAf2(eph.getAf2());
        setAf1(eph.getAf1());
        setAf0(eph.getAf0());
        setIodc(eph.getIodc());
        setCrs(eph.getCrs());
        setCrc(eph.getCrc());
        setDeltaN(eph.getDeltaN());
        setM0(eph.getM0());
        setCuc(eph.getCuc());
        setEc(eph.getEc());
        setCus(eph.getCus());
        setSquareA(eph.getSquareA());
        setToe(eph.getToe());
        setCic(eph.getCic());
        setOmega0(eph.getOmega0());
        setCis(eph.getCis());
        setI0(eph.getI0());
        setOmega(eph.getOmega());
        setOmegaDot(eph.getOmegaDot());
        setAl0(eph.getAl0());
        setAl1(eph.getAl1());
        setAl2(eph.getAl2());
        setAl3(eph.getAl3());
        setBt0(eph.getBt0());
        setBt1(eph.getBt1());
        setBt2(eph.getBt2());
        setBt3(eph.getBt3());
    }
    /**
     * Constructor without parameters
     */
    public KeplerianEphemeris() {
        super();
    }


    //SETTERS
    public void setIdot(double _idot)
    {
        idot = _idot;
    }
    public void setToc(double _toc)
    {
        toc = _toc;
    }
    public void setAf2(double af2) {
        this.af2 = af2;
    }
    public void setAf1(double af1) {
        this.af1 = af1;
    }
    public void setAf0(double af0) {
        this.af0 = af0;
    }
    public void setIodc(int iodc) {
        this.iodc = iodc;
    }
    public void setCrs(double crs) {
        this.crs = crs;
    }
    public void setCrc(double crc) {
        this.crc = crc;
    }
    public void setDeltaN(double deltaN) {
        this.deltaN = deltaN;
    }
    public void setM0(double m0) {
        this.m0 = m0;
    }
    public void setCuc(double cuc) {
        this.cuc = cuc;
    }
    public void setEc(double ec) {
        this.ec = ec;
    }
    public void setCus(double cus) {
        this.cus = cus;
    }
    public void setSquareA(double squareA) {
        this.squareA = squareA;
    }
    public void setToe(double toe) {
        this.toe = toe;
    }
    public void setCic(double cic) {
        this.cic = cic;
    }
    public void setOmega0(double omega0) {
        this.omega0 = omega0;
    }
    public void setCis(double cis) {
        this.cis = cis;
    }
    public void setI0(double i0) {
        this.i0 = i0;
    }
    public void setOmega(double omega) {
        this.omega = omega;
    }
    public void setOmegaDot(double omegaDot) {
        this.omegaDot = omegaDot;
    }
    public void setAl0(double Al0){this.Al0 = Al0;}
    public void setAl1(double Al1){this.Al1 = Al1;}
    public void setAl2(double Al2){this.Al2 = Al2;}
    public void setAl3(double Al3){this.Al3 = Al3;}
    public void setBt0(double Bt0){this.Bt0 = Bt0;}
    public void setBt1(double Bt1){this.Bt1 = Bt1;}
    public void setBt2(double Bt2){this.Bt2 = Bt2;}
    public void setBt3(double Bt3){this.Bt3 = Bt3;}

    //GETTERS
    public long getTow() { return tow; }
    public double getToc(){
        return toc;
    }
    public double getAf2(){
        return af2;
    }
    public double getAf1(){
        return af1;
    }
    public double getAf0(){
        return af0;
    }
    public double getCrs(){
        return crs;
    }
    public double getDeltaN(){
        return deltaN;
    }
    public double getM0(){
        return m0;
    }
    public double getCuc(){
        return cuc;
    }
    public double getEc(){
        return ec;
    }
    public double getCus(){
        return cus;
    }
    public double getSquareA(){
        return squareA;
    }
    public double getToe(){
        return toe;
    }
    public double getCic(){
        return cic;
    }
    public double getOmega0(){
        return omega0;
    }
    public double getCis(){
        return cis;
    }
    public double getI0(){
        return i0;
    }
    public double getCrc(){
        return crc;
    }
    public double getOmega(){
        return omega;
    }
    public double getOmegaDot(){
        return omegaDot;
    }
    public double getIdot(){
        return idot;
    }
    public double getAodo(){
        return aodo;
    }
    public double getAl0(){return Al0;}
    public double getAl1(){return Al1;}
    public double getAl2(){return Al2;}
    public double getAl3(){return Al3;}
    public double getBt0(){return Bt0;}
    public double getBt1(){return Bt1;}
    public double getBt2(){return Bt2;}
    public double getBt3(){return Bt3;}

    //RETRIEVERS
    // retrieve values from subframes
    protected abstract int retrievePrn();
    protected abstract long retrieveTow();
    protected abstract int retrieveWn();
    protected abstract double retrieveToc();
    protected abstract double retrieveAf2();
    protected abstract double retrieveAf1();
    protected abstract double retrieveAf0();
    protected abstract double retrieveCrs();
    protected abstract double retrieveDeltaN();
    protected abstract double retrieveM0();
    protected abstract double retrieveCuc();
    protected abstract double retrieveEc();
    protected abstract double retrieveCus();
    protected abstract double retrieveSquareA();
    protected abstract double retrieveToe();
    protected abstract double retrieveAodo();
    protected abstract double retrieveCic();
    protected abstract double retrieveOmega0();
    protected abstract double retrieveCis();
    protected abstract double retrieveI0();
    protected abstract double retrieveCrc();
    protected abstract double retrieveOmega();
    protected abstract double retrieveOmegaDot();
    protected abstract double retrieveIdot();
    protected abstract double retrieveAl0();
    protected abstract double retrieveAl1();
    protected abstract double retrieveAl2();
    protected abstract double retrieveAl3();
    protected abstract double retrieveBt0();
    protected abstract double retrieveBt1();
    protected abstract double retrieveBt2();
    protected abstract double retrieveBt3();


    /**
     * Override toString
     * @return String of ephemeris
     */
    @Override
    public String toString() {
        String str = prn +
                    "," + tow +
                    "," + wn +
                    "," + toc +
                    "," + af2 +
                    "," + af1 +
                    "," + af0 +
                    "," + crs +
                    "," + deltaN +
                    "," + m0 +
                    "," + cuc +
                    "," + ec +
                    "," + cus +
                    "," + squareA +
                    "," + toe +
                    "," + aodo +
                    "," + cic +
                    "," + omega0 +
                    "," + cis +
                    "," + i0 +
                    "," + crc +
                    "," + omega +
                    "," + omegaDot +
                    "," + idot;
        return str;
    }


    @Override
    public abstract KeplerianEphemeris copy();



}