///=================================================================================================
// Class PreciseCorrection
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
package org.gogpsproject.ephemeris;

public class PreciseCorrection
{
    private double tow;
    private double tod; //for glonass time of day
    private double updateInterval;
    private int prn;
    private int gnssSystem;

    //Position correction to be applied to brdc positions, given in satellite reference frame
    private int iode;
    private double eRadial;
    private double eAlong;
    private double eCross;
    private double eDotRadial;
    private double eDotAlong;
    private double eDotCross;

    //Clocks correction to be applied to brdc clocks
    private double c0;
    private double c1;
    private double c2;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public PreciseCorrection() {
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Contructor of recopy
     * @param ephCorr Precise ephemeris corrections
     */
    public PreciseCorrection(PreciseCorrection ephCorr)
    {
        setTow(ephCorr.getTow());
        setTod(ephCorr.getTod()); // for glonass
        setPrn(ephCorr.getPrn());
        setGnssSystem(ephCorr.getGnssSystem());
        setIode(ephCorr.getIode());
        seteRadial(ephCorr.geteRadial());
        seteAlong(ephCorr.geteAlong());
        seteCross(ephCorr.geteCross());
        seteDotRadial(ephCorr.geteDotRadial());
        seteDotAlong(ephCorr.geteDotAlong());
        seteDotCross(ephCorr.geteDotCross());
        setC0(ephCorr.getC0());
        setC1(ephCorr.getC1());
        setC2(ephCorr.getC2());
        setUpdateInterval(ephCorr.getUpdateInterval());
    }

    //----------------------------------------------------------------------------------------------

    public double getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(double updateInterval) {
        this.updateInterval = updateInterval;
    }

    public double getTow() {
        return tow;
    }

    public double getTod() {
        return tod;
    }

    public void setTow(double tow) {
        this.tow = tow;
    }

    public void setTod(double tod) {
        this.tod = tod;
    }

    public int getPrn() {
        return prn;
    }

    public void setPrn(int prn) {
        this.prn = prn;
    }

    public int getGnssSystem() {
        return gnssSystem;
    }

    public void setGnssSystem(int gnssSystem) {
        this.gnssSystem = gnssSystem;
    }

    public int getIode() {
        return iode;
    }

    public void setIode(int iode) {
        this.iode = iode;
    }

    public double geteRadial() {
        return eRadial;
    }

    public void seteRadial(double eRadial) {
        this.eRadial = eRadial;
    }

    public double geteAlong() {
        return eAlong;
    }

    public void seteAlong(double eAlong) {
        this.eAlong = eAlong;
    }

    public double geteCross() {
        return eCross;
    }

    public void seteCross(double eCross) {
        this.eCross = eCross;
    }

    public double geteDotRadial() {
        return eDotRadial;
    }

    public void seteDotRadial(double eDotRadial) {
        this.eDotRadial = eDotRadial;
    }

    public double geteDotAlong() {
        return eDotAlong;
    }

    public void seteDotAlong(double eDotAlong) {
        this.eDotAlong = eDotAlong;
    }

    public double geteDotCross() {
        return eDotCross;
    }

    public void seteDotCross(double eDotCross) {
        this.eDotCross = eDotCross;
    }

    public double getC0() {
        return c0;
    }

    public void setC0(double c0) {
        this.c0 = c0;
    }

    public double getC1() {
        return c1;
    }

    public void setC1(double c1) {
        this.c1 = c1;
    }

    public double getC2() {
        return c2;
    }

    public void setC2(double c2) {
        this.c2 = c2;
    }

    @Override
    public String toString()
    {
        String str =
                "System : " + gnssSystem + "\n"
                + "prn : " + prn + "\n"
                + "UI : " + updateInterval + "\n"
                + "IODE : " + iode + "\n"
                + "ERAD : " + eRadial + "\n"
                + "EAL : " + eAlong + "\n"
                + "ECROSS : " + eCross + "\n"
                + "DOT : " + eDotRadial + "\n"
                + "DOT : " + eDotAlong + "\n"
                + "DOT : " + eDotCross + "\n"
                + "C0 : " + c0 + "\n"
                + "C1 : " + c1 + "\n"
                + "C2 : " + c2;

        return str;
    }
}

