///=================================================================================================
// Class BeidouEphemeris
//      Author :  Aravind RAMESH, Research Engineer, UGE Nantes
//      Date: 11/08/2022
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

/**
 * Class BeidouEphemeris
 */

public class BeidouEphemeris extends KeplerianEphemeris {

    //Sub-frames required to extract Ephemeris
    private BeidouNavigationMessage subFrame1;
    private BeidouNavigationMessage subFrame2;
    private BeidouNavigationMessage subFrame3;

    public BeidouEphemeris(GnssNavigationMessage subFrame1 , GnssNavigationMessage subFrame2, GnssNavigationMessage subFrame3) {
        super();
        this.subFrame1 = new BeidouNavigationMessage(subFrame1);
        this.subFrame2 = new BeidouNavigationMessage(subFrame2);
        this.subFrame3 = new BeidouNavigationMessage(subFrame3);

        gnssSystem = GnssStatus.CONSTELLATION_BEIDOU;

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

        // Ionospheric Correction parameters
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
    public BeidouEphemeris(){
        super();
        gnssSystem = GnssStatus.CONSTELLATION_BEIDOU;
    }

    /**
     * Constructor for recopy of a Beidou ephemeris
     * @param eph
     */
    public BeidouEphemeris(BeidouEphemeris eph) {
        super(eph);
    }


    /**
     * Creates a copy of the Beidou ephemeris
     * @return
     */
    @Override
    public BeidouEphemeris copy(){
        return new BeidouEphemeris(this);
    }


    //Retrievers to get data
    @Override
    protected int retrievePrn(){
        return subFrame1.getId();
    }

    @Override
    protected long retrieveTow(){
        String sow = this.subFrame1.getWord(0).substring(18,26) + this.subFrame1.getWord(1).substring(0,12);
        return Long.parseLong(sow,2);
    }

    @Override
    protected int retrieveWn(){
        String wn = this.subFrame1.getWord(2).substring(0,13);
        return Integer.parseInt(wn,2);
    }

    @Override
    protected double retrieveToc(){
        double toc = toDecimalfromBinaryString(this.subFrame1.getWord(2).substring(13,22)
                + this.subFrame1.getWord(3).substring(0,8),false);
        return toc * Math.pow(2,3);
    }

    @Override
    protected double retrieveAf2(){
        double af2 = toDecimalfromBinaryString(this.subFrame1.getWord(7).substring(4,15),true);
        return af2 * Math.pow(2,-66);
    }

    @Override
    protected double retrieveAf1(){
        double af1 = toDecimalfromBinaryString(this.subFrame1.getWord(8).substring(17,22)
                + this.subFrame1.getWord(9).substring(0,17),true);
        return af1 * Math.pow(2,-50);
    }

    @Override
    protected double retrieveAf0(){
        double af0 = toDecimalfromBinaryString(this.subFrame1.getWord(7).substring(15,22)
                + this.subFrame1.getWord(8).substring(0,17),true);
        return af0 * Math.pow(2,-33);
    }

    @Override
    protected double retrieveCrs(){
        double crs = toDecimalfromBinaryString(this.subFrame2.getWord(7).substring(14,22)
                + this.subFrame2.getWord(8).substring(0,10),true);
        return crs * Math.pow(2,-6);
    }

    protected double retrieveDeltaN(){
        double dn = toDecimalfromBinaryString(this.subFrame2.getWord(1).substring(12,22)
                + this.subFrame2.getWord(2).substring(0,6),true);
        return dn * Math.pow(2,-43) * Math.PI;
    }
    //* Math.PI
    @Override
    protected double retrieveM0(){
        double m0 = toDecimalfromBinaryString(this.subFrame2.getWord(3).substring(2,22)
                + this.subFrame2.getWord(4).substring(0,12), true);
        return m0 * Math.pow(2,-31) * Math.PI;
    }
    //* Math.PI
    @Override
    protected double retrieveCuc(){
        double cuc = toDecimalfromBinaryString(this.subFrame2.getWord(2).substring(6,22)
                + this.subFrame2.getWord(3).substring(0,2),true);
        return cuc * Math.pow(2,-31);
    }

    @Override
    protected double retrieveEc(){
        double ecc = toDecimalfromBinaryString(this.subFrame2.getWord(4).substring(12,22)
                + this.subFrame2.getWord(5).substring(0,22),false);
        return ecc * Math.pow(2,-33);
    }

    @Override
    protected double retrieveCus(){
        double cus = toDecimalfromBinaryString(this.subFrame2.getWord(6).substring(0,18),true);
        return cus * Math.pow(2,-31);
    }

    @Override
    protected double retrieveSquareA(){
        double sa = toDecimalfromBinaryString(this.subFrame2.getWord(8).substring(10,22)
                + this.subFrame2.getWord(9).substring(0,20),false);
        return sa * Math.pow(2,-19);
    }

    @Override
    protected double retrieveToe(){
        double toe = toDecimalfromBinaryString(this.subFrame2.getWord(9).substring(20,22)
                + this.subFrame3.getWord(1).substring(12,22)
                + this.subFrame3.getWord(2).substring(0,5),false);
        return toe * Math.pow(2,3);
    }

    //Equipment group Delay Differential TGD1
    @Override
    protected double retrieveAodo(){
        double tgd1 = toDecimalfromBinaryString(this.subFrame1.getWord(3).substring(8,18),true);
        return (tgd1 * 0.1)/Math.pow(10,9);
    }

    @Override
    protected double retrieveCic(){
        double cic = toDecimalfromBinaryString(this.subFrame3.getWord(3).substring(15,22)
                + this.subFrame3.getWord(4).substring(0,11),true);
        return cic * Math.pow(2,-31);
    }

    @Override
    protected double retrieveOmega0(){
        double w0 = toDecimalfromBinaryString(this.subFrame3.getWord(7).substring(1,22)
                + this.subFrame3.getWord(8).substring(0,11),true);
        return w0 * Math.pow(2,-31) * Math.PI;
    }
    //* Math.PI
    @Override
    protected double retrieveCis(){
        double cis = toDecimalfromBinaryString(this.subFrame3.getWord(5).substring(13,22)
                + this.subFrame3.getWord(6).substring(0,9),true);
        return cis * Math.pow(2,-31);
    }

    @Override
    protected double retrieveI0(){
        double i0 = toDecimalfromBinaryString(this.subFrame3.getWord(2).substring(5,22)
                + this.subFrame3.getWord(3).substring(0,15),true);
        return i0 * Math.pow(2,-31) * Math.PI;
    }
    //* Math.PI
    @Override
    protected double retrieveCrc(){
        double crc = toDecimalfromBinaryString(this.subFrame2.getWord(6).substring(18,22)
                + this.subFrame2.getWord(7).substring(0,14),true);
        return crc * Math.pow(2,-6);
    }

    @Override
    protected double retrieveOmega(){
        double w = toDecimalfromBinaryString(this.subFrame3.getWord(8).substring(11,22)
                + this.subFrame3.getWord(9).substring(0,21),true);
        return w * Math.pow(2,-31) * Math.PI;
    }
    //* Math.PI
    @Override
    protected double retrieveOmegaDot(){
        double wd = toDecimalfromBinaryString(this.subFrame3.getWord(4).substring(11,22)
                + this.subFrame3.getWord(5).substring(0,13),true);
        return wd * Math.pow(2,-43) * Math.PI;
    }
    //* Math.PI
    @Override
    protected double retrieveIdot(){
        double id = toDecimalfromBinaryString(this.subFrame3.getWord(6).substring(9,22)
                + this.subFrame3.getWord(7).charAt(0),true);
        return id * Math.pow(2,-43) * Math.PI;
    }

    @Override
    public double retrieveAl0(){
        double al0 = toDecimalfromBinaryString(this.subFrame1.getWord(4).substring(6,14),true);
        return al0 * Math.pow(2,-30);
    }

    @Override
    public double retrieveAl1(){
        double al1 = toDecimalfromBinaryString(this.subFrame1.getWord(4).substring(14,22),true);
        return al1 * Math.pow(2,-27);
    }

    @Override
    public double retrieveAl2(){
        double al2 = toDecimalfromBinaryString(this.subFrame1.getWord(5).substring(0,8),true);
        return al2 * Math.pow(2,-24);
    }

    @Override
    public double retrieveAl3(){
        double al3 = toDecimalfromBinaryString(this.subFrame1.getWord(5).substring(8,16),true);
        return al3 * Math.pow(2,-24);
    }

    @Override
    public double retrieveBt0(){
        double bt0 = toDecimalfromBinaryString(this.subFrame1.getWord(5).substring(16,22)
                + this.subFrame1.getWord(6).substring(0,2), true);
        return bt0 * Math.pow(2,11);
    }

    @Override
    public double retrieveBt1(){
        double bt1 = toDecimalfromBinaryString(this.subFrame1.getWord(6).substring(2,10) ,true);
        return bt1 * Math.pow(2,14);
    }

    @Override
    public double retrieveBt2(){
        double bt2 = toDecimalfromBinaryString(this.subFrame1.getWord(6).substring(10,18) ,true);
        return bt2 * Math.pow(2,16);
    }

    @Override
    public double retrieveBt3(){
        double bt3 = toDecimalfromBinaryString(this.subFrame1.getWord(6).substring(18,22)
                + this.subFrame1.getWord(7).substring(0,4), true);
        return bt3 * Math.pow(2,16);
    }

}
//* Math.PI