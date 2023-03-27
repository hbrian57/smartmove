///=================================================================================================
// Class GNSSEphemeris
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

import org.gogpsproject.ephemeris.PreciseCorrection;
import org.gogpsproject.ephemeris.SatelliteCodeBiases;

public abstract class GNSSEphemeris {

    protected int prn;
    protected int gnssSystem;

    protected int iode;       //Issue of Data Ephemeris
    protected int iodc;       //Issue of Data Clock
    protected int svh;        //Satellite health
    protected int svdv;       //Data validity status (See GALILEO ICD)
    protected int sisa;       //SIS Accuracy

    protected int wn;         //week number

    // Corrections
    protected PreciseCorrection mEphCorrections;
    protected SatelliteCodeBiases.CodeBias codeBias;


    /**
     * Constructor for recopy of a GNSS ephemeris
     * @param eph
     */
    public GNSSEphemeris(GNSSEphemeris eph)
    {
        setSvh(eph.getSvh());
        setSvdv(eph.getSvdv());
        setSisa(eph.getSisa());
        setPrn(eph.getPrn());
        setGnssSystem(eph.getGnssSystem());

        if(eph.getCodeBias() != null)
        {
            setCodeBias(new SatelliteCodeBiases.CodeBias(eph.getCodeBias()));
        }
        if(eph.getEphCorrections() != null)
        {
            setEphCorrections(eph.getEphCorrections());
        }
    }

    /**
     * Constructor without parameters
     */
    public GNSSEphemeris() {

    }


    public abstract GNSSEphemeris copy();


    /**
     * converting to decimal from binary string either it is a binary signed number or not
     * @param intBitsStr a binary string
     * @param signed a binary signed number
     * @return
     */
    protected static double toDecimalfromBinaryString(String intBitsStr, boolean signed){
        long unsigned = Long.parseLong(intBitsStr,2);
        if (!signed) {
            return unsigned;
        }
        else{
            long mask = (long) Math.pow(2, (intBitsStr.length()-1));
            return -(unsigned & mask) + (unsigned & ~mask);
        }
    }


    //SETTERS
    public void setPrn(int _prn)
    {
        this.prn = _prn;
    }
    public void setGnssSystem(int _sys)
    {
        this.gnssSystem = _sys;
    }
    public void setIode(int _iode)
    {
        iode = _iode;
    }
    public void setIodc(int _iodc)
    {
        iodc = _iodc;
    }
    public void setSvh(int svh) {
        this.svh = svh;
    }
    public void setSvdv(int svdv) {
        this.svdv = svdv;
    }
    public void setSisa(int sisa) {
        this.sisa = sisa;
    }
    public void setEphCorrections(PreciseCorrection mEphemerisGNSSCorr) { this.mEphCorrections = mEphemerisGNSSCorr; }
    public void setCodeBias(SatelliteCodeBiases.CodeBias codeBias) {
        this.codeBias = codeBias;
    }
    public void setWeek(int _wn)
    {
        wn = _wn;
    }

    // GETTERS
    public int getPrn() {
        return prn;
    }
    public int getGnssSystem() {
        return gnssSystem;
    }
    public int getIode() {
        return iode;
    }
    public int getIodc() {
        return iodc;
    }
    public int getSvh() {
        return svh;
    }
    public int getSvdv() {
        return svdv;
    }
    public int getSisa() {
        return sisa;
    }
    public SatelliteCodeBiases.CodeBias getCodeBias() {
        return codeBias;
    }
    public PreciseCorrection getEphCorrections() {
        return mEphCorrections;
    }
    public int getWn(){
        return wn;
    }

}
