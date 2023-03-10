///=================================================================================================
// Class GpsEphemeris
//      Author :  Jose Gilberto RESENDIZ FONSECA
//      modified by Edgar LENHOF
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


package org.gogpsproject.ephemeris;

import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.util.Log;

import java.util.Arrays;

/**
 * Class GpsEphemeris
 */
public class GpsEphemeris extends KeplerianEphemeris {

    //sub-frames needed to recuperate all ephemeris
    private GpsNavigationMessage subFrame1;
    private GpsNavigationMessage subFrame2;
    private GpsNavigationMessage subFrame3;
    private GpsNavigationMessage subFrame4;

    /**
     * Constructor with parameters
     * @param subFrame1 sub-frame1
     * @param subFrame2 sub-frame2
     * @param subFrame3 sub-frame3
     */
    public GpsEphemeris(GnssNavigationMessage subFrame1 , GnssNavigationMessage subFrame2, GnssNavigationMessage subFrame3, GnssNavigationMessage subFrame4) {
        super();
        this.subFrame1 = new GpsNavigationMessage(subFrame1);
        this.subFrame2 = new GpsNavigationMessage(subFrame2);
        this.subFrame3 = new GpsNavigationMessage(subFrame3);
        this.subFrame4 = new GpsNavigationMessage(subFrame4);

        gnssSystem = GnssStatus.CONSTELLATION_GPS;

        prn = retrievePrn();
        tow = retrieveTow();

        wn = retrieveWn();
        toc = retrieveToc();
        af2 = retrieveAf2();
        af1 = retrieveAf1();
        af0 = retrieveAf0();

        crs = retrieveCrs();
        deltaN = retrieveDeltaN();
        m0 = retrieveM0();
        cuc = retrieveCuc();
        ec = retrieveEc();
        cus = retrieveCus();
        squareA = retrieveSquareA();
        toe = retrieveToe();
        aodo = retrieveAodo();

        cic = retrieveCic();
        omega0 = retrieveOmega0();
        cis = retrieveCis();
        i0 = retrieveI0();
        crc = retrieveCrc();
        omega = retrieveOmega();
        omegaDot = retrieveOmegaDot();
        idot = retrieveIdot();

        // Ionospheric Correction Parameters
        Al0 = retrieveAl0();
        Al1 = retrieveAl1();
        Al2 = retrieveAl2();
        Al3 = retrieveAl3();
        Bt0 = retrieveBt0();
        Bt1 = retrieveBt1();
        Bt2 = retrieveBt2();
        Bt3 = retrieveBt3();
    }

    /**
     * Constructor without parameters
     */
    public GpsEphemeris(){
        super();
        gnssSystem = GnssStatus.CONSTELLATION_GPS;
    }

    /**
     * Constructor for recopy of a GPS ephemeris
     * @param eph
     */
    public GpsEphemeris(GpsEphemeris eph) {
        super(eph);
    }

    /**
     * Creates a copy of the GPS ephemeris
     * @return
     */
    @Override
    public GpsEphemeris copy(){
        return new GpsEphemeris(this);
    }


    //RETRIEVERS
    @Override
    protected int retrievePrn(){
        return subFrame1.getId();
    }

    @Override
    protected long retrieveTow(){
        return subFrame2.getTow();
    }

    @Override
    protected int retrieveWn(){
        return (int)toDecimalfromBinaryString(this.subFrame1.getWord(2).substring(0,10), false);
    }

    @Override
    protected double retrieveToc(){
        return toDecimalfromBinaryString(this.subFrame1.getWord(7).substring(8,24), false)
                * Math.pow(2,4);
    }

    @Override
    protected double retrieveAf2(){
        return toDecimalfromBinaryString(this.subFrame1.getWord(8).substring(0,8),true)
                * Math.pow(2,-55);
    }

    @Override
    protected double retrieveAf1(){
        return toDecimalfromBinaryString(this.subFrame1.getWord(8).substring(8,24),true)
                * Math.pow(2,-43);
    }

    @Override
    protected double retrieveAf0(){
        return toDecimalfromBinaryString(this.subFrame1.getWord(9).substring(0,22),true)
                * Math.pow(2,-31);
    }

    @Override
    protected double retrieveCrs(){
        return toDecimalfromBinaryString(this.subFrame2.getWord(2).substring(8,24), true)
                * Math.pow(2,-5);
    }

    protected double retrieveDeltaN(){
        return toDecimalfromBinaryString(this.subFrame2.getWord(3).substring(0,16), true)
                * Math.pow(2,-43) * Math.PI;
    }

    @Override
    protected double retrieveM0(){
        return toDecimalfromBinaryString(this.subFrame2.getWord(3).substring(16,24)
                + this.subFrame2.getWord(4).substring(0,24), true) * Math.pow(2,-31) * Math.PI;
    }

    @Override
    protected double retrieveCuc(){
        return toDecimalfromBinaryString(this.subFrame2.getWord(5).substring(0,16), true)
                * Math.pow(2,-29);
    }

    @Override
    protected double retrieveEc(){
        return toDecimalfromBinaryString(this.subFrame2.getWord(5).substring(16,24)
                + this.subFrame2.getWord(6).substring(0,24), false) * Math.pow(2,-33);
    }

    @Override
    protected double retrieveCus(){
        return toDecimalfromBinaryString(this.subFrame2.getWord(7).substring(0,16), true)
                * Math.pow(2,-29);
    }

    @Override
    protected double retrieveSquareA(){
        return toDecimalfromBinaryString(this.subFrame2.getWord(7).substring(16,24)
                + this.subFrame2.getWord(8).substring(0,24), false) * Math.pow(2,-19);
    }

    @Override
    protected double retrieveToe(){
        return toDecimalfromBinaryString(this.subFrame2.getWord(9).substring(0,16), false)
                * Math.pow(2,4);
    }

    @Override
    protected double retrieveAodo(){
        return toDecimalfromBinaryString(this.subFrame2.getWord(9).substring(17,22),false)
                * 900;
    }

    @Override
    protected double retrieveCic(){
        return toDecimalfromBinaryString(this.subFrame3.getWord(2).substring(0,16), true)
                * Math.pow(2,-29);
    }

    @Override
    protected double retrieveOmega0(){
        return toDecimalfromBinaryString(this.subFrame3.getWord(2).substring(16,24)
                + this.subFrame3.getWord(3).substring(0,24), true) * Math.pow(2,-31) * Math.PI;
    }

    @Override
    protected double retrieveCis(){
        return toDecimalfromBinaryString(this.subFrame3.getWord(4).substring(0,16), true)
                * Math.pow(2,-29);
    }

    @Override
    protected double retrieveI0(){
        return toDecimalfromBinaryString(this.subFrame3.getWord(4).substring(16,24)
                + this.subFrame3.getWord(5).substring(0,24), true) * Math.pow(2,-31) * Math.PI;
    }

    @Override
    protected double retrieveCrc(){
        return toDecimalfromBinaryString(this.subFrame3.getWord(6).substring(0,16), true)
                * Math.pow(2, -5);
    }

    @Override
    protected double retrieveOmega(){
        return toDecimalfromBinaryString(this.subFrame3.getWord(6).substring(16,24)
                +this.subFrame3.getWord(7).substring(0,24), true) * Math.pow(2, -31) * Math.PI;
    }

    @Override
    protected double retrieveOmegaDot(){
        return toDecimalfromBinaryString(this.subFrame3.getWord(8).substring(0,24), true)
                * Math.pow(2,-43) * Math.PI;
    }

    @Override
    protected double retrieveIdot(){
        return toDecimalfromBinaryString(this.subFrame3.getWord(9).substring(8,22), true)
                * Math.pow(2,-43) * Math.PI;
    }

    @Override
    public double retrieveAl0(){
        if (this.subFrame4.getPageid() == 18)
        {
            return toDecimalfromBinaryString(this.subFrame4.getWord(2).substring(8,16), true) * Math.pow(2,-30);
        }
        else
        {
            return 0;
        }
    }

    @Override
    public double retrieveAl1(){
        if (this.subFrame4.getPageid() == 18)
        {
            return toDecimalfromBinaryString(this.subFrame4.getWord(2).substring(16,24), true) * Math.pow(2,-27);
        }
        else
        {
            return 0;
        }
    }

    @Override
    public double retrieveAl2(){
        if (this.subFrame4.getPageid() == 18)
        {
            return toDecimalfromBinaryString(this.subFrame4.getWord(3).substring(0,8), true) * Math.pow(2,-24);
        }
        else
        {
            return 0;
        }
    }

    @Override
    public double retrieveAl3(){
        if (this.subFrame4.getPageid() == 18)
        {
            return toDecimalfromBinaryString(this.subFrame4.getWord(3).substring(8,16), true) * Math.pow(2,-24);
        }
        else
        {
            return 0;
        }
    }

    @Override
    public double retrieveBt0(){
        if (this.subFrame4.getPageid() == 18)
        {
            return toDecimalfromBinaryString(this.subFrame4.getWord(3).substring(16,24), true) * Math.pow(2,11);
        }
        else
        {
            return 0;
        }
    }

    @Override
    public double retrieveBt1(){
        if (this.subFrame4.getPageid() == 18)
        {
            return toDecimalfromBinaryString(this.subFrame4.getWord(4).substring(0,8), true) * Math.pow(2,14);
        }
        else
        {
            return 0;
        }
    }

    @Override
    public double retrieveBt2(){
        if (this.subFrame4.getPageid() == 18)
        {
            return toDecimalfromBinaryString(this.subFrame4.getWord(4).substring(8,16), true) * Math.pow(2,16);
        }
        else
        {
            return 0;
        }
    }

    @Override
    public double retrieveBt3(){
        if (this.subFrame4.getPageid() == 18)
        {
            return toDecimalfromBinaryString(this.subFrame4.getWord(4).substring(16,24), true) * Math.pow(2,16);
        }
        else
        {
            return 0;
        }
    }

}
