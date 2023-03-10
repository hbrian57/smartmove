///=================================================================================================
// Class GalileoEphemeris
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
import java.math.BigInteger;

public class GalileoEphemeris extends KeplerianEphemeris {

    private GalileoNavigationMessage word1;
    private GalileoNavigationMessage word2;
    private GalileoNavigationMessage word3;
    private GalileoNavigationMessage word4;
    private GalileoNavigationMessage word5;

    /**
     * Constructor with parameters
     */
    public GalileoEphemeris(GnssNavigationMessage word1, GnssNavigationMessage word2, GnssNavigationMessage word3, GnssNavigationMessage word4, GnssNavigationMessage word5){
        super();
        this.word1 = new GalileoNavigationMessage(word1);
        this.word2 = new GalileoNavigationMessage(word2);
        this.word3 = new GalileoNavigationMessage(word3);
        this.word4 = new GalileoNavigationMessage(word4);
        this.word5 = new GalileoNavigationMessage(word5);

        gnssSystem = GnssStatus.CONSTELLATION_GALILEO;

        // Data extracted from Word 5
        tow = retrieveTow();
        wn = retrieveWn();

        //Data extracted from word 1
        toe = retrieveToe();
        m0 = retrieveM0();
        ec = retrieveEc();
        squareA = retrieveSquareA();

        //Data extracted from word 2
        omega0 = retrieveOmega0();
        i0 = retrieveI0();
        omega = retrieveOmega();
        idot = retrieveIdot();

        //Data extracted from word 3
        omegaDot = retrieveOmegaDot();
        deltaN = retrieveDeltaN();
        cuc = retrieveCuc();
        cus = retrieveCus();
        crc = retrieveCrc();
        crs = retrieveCrs();

        //Data extracted from word 4
        prn = retrievePrn();
        cic = retrieveCic();
        cis = retrieveCis();
        toc = retrieveToc();
        af0 = retrieveAf0();
        af2 = retrieveAf2();
        af1 = retrieveAf1();

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
    public GalileoEphemeris(){
        super();
        gnssSystem = GnssStatus.CONSTELLATION_GALILEO;
    }

    /**
     * Constructor for recopy of a Galileo ephemeris
     * @param eph
     */
    public GalileoEphemeris(GalileoEphemeris eph) {
        super(eph);
    }

    /**
     * Creates a copy of the Galileo ephemeris
     * @return
     */
    @Override
    public GalileoEphemeris copy(){
        return new GalileoEphemeris(this);
    }

    //Data RETRIEVERS
    @Override
    protected int retrievePrn(){
        return Integer.parseInt(this.word4.getWord(16,22),2);
    }

    @Override
    protected long retrieveTow(){
        return Long.parseLong(this.word5.getWord(85,105),2);
    }
    //Long.parseLong(this.word0.getWord(108,128),2);

    @Override
    protected int retrieveWn(){
        return Integer.parseInt(this.word5.getWord(73,85),2);
    }
    //Integer.parseInt(this.word0.getWord(96,108),2)

    @Override
    protected double retrieveToc(){
        double toc = toDecimalfromBinaryString(this.word4.getWord(54,68),false);
        return (toc * 60);
    }

    @Override
    protected double retrieveAf2(){
        double af2 = toDecimalfromBinaryString(this.word4.getWord(120,126),true);
        return (af2 * Math.pow(2,-59));
    }

    @Override
    protected double retrieveAf1(){
        double af1 = toDecimalfromBinaryString(this.word4.getWord(99,120),true);
        return (af1 * Math.pow(2,-46));
    }

    @Override
    protected double retrieveAf0(){
        double af0 = toDecimalfromBinaryString(this.word4.getWord(68,99),true);
        return (af0 * Math.pow(2,-34));
    }

    @Override
    protected double retrieveCrs(){
        double crs = toDecimalfromBinaryString(this.word3.getWord(104,120),true);
        return (crs * Math.pow(2,-5));
    }

    protected double retrieveDeltaN(){
        double dn = toDecimalfromBinaryString(this.word3.getWord(40,56),true);
        return (dn * Math.pow(2,-43) * Math.PI);
    }

    @Override
    protected double retrieveM0(){
        double mart = toDecimalfromBinaryString(this.word1.getWord(30,62),true);
        return (mart * Math.pow(2,-31) * Math.PI);
    }

    @Override
    protected double retrieveCuc(){
        double cuc = toDecimalfromBinaryString(this.word3.getWord(56,72),true);
        return (cuc * Math.pow(2,-29));
    }

    @Override
    protected double retrieveEc(){
        double ecc = toDecimalfromBinaryString(this.word1.getWord(62,94),false);
        return (ecc * Math.pow(2,-33));
    }

    @Override
    protected double retrieveCus(){
        double cus = toDecimalfromBinaryString(this.word3.getWord(72,88),true);
        return (cus * Math.pow(2,-29));
    }

    @Override
    protected double retrieveSquareA(){
        double sqta = toDecimalfromBinaryString(this.word1.getWord(94,126),false);
        return (sqta * Math.pow(2,-19));
    }

    @Override
    protected double retrieveToe(){
        double ert = toDecimalfromBinaryString(this.word1.getWord(16,30),false);
        return (ert * 60);
    }

    @Override
    protected double retrieveAodo(){
        return 0;
    }

    @Override
    protected double retrieveCic(){
        double cic = toDecimalfromBinaryString(this.word4.getWord(22,38),true);
        return (cic * Math.pow(2,-29));
    }

    @Override
    protected double retrieveOmega0(){
        double omg0 = toDecimalfromBinaryString(this.word2.getWord(16,48),true);
        return (omg0 * Math.pow(2,-31) * Math.PI);
    }

    @Override
    protected double retrieveCis(){
        double cis = toDecimalfromBinaryString(this.word4.getWord(38,54),true);
        return (cis * Math.pow(2,-29));
    }

    @Override
    protected double retrieveI0(){
        double i0 = toDecimalfromBinaryString(this.word2.getWord(48,80),true);
        return (i0 * Math.pow(2,-31) * Math.PI);
    }

    @Override
    protected double retrieveCrc(){
        double crc = toDecimalfromBinaryString(this.word3.getWord(88,104),true);
        return (crc * Math.pow(2,-5));
    }

    @Override
    protected double retrieveOmega(){
        double omg = toDecimalfromBinaryString(this.word2.getWord(80,112),true);
        return (omg * Math.pow(2,-31) * Math.PI);
    }

    @Override
    protected double retrieveOmegaDot(){
        double omgDot = toDecimalfromBinaryString(this.word3.getWord(16,40),true);
        return (omgDot * Math.pow(2,-43) * Math.PI);
    }

    @Override
    protected double retrieveIdot(){
        double idot = toDecimalfromBinaryString(this.word2.getWord(112,126),true);
        return (idot * Math.pow(2,-43) * Math.PI);
    }

    @Override
    public double retrieveAl0(){
        //double aI0 = toDecimalfromBinaryString(this.word5.getWord(6,17),false);
        //return aI0 * Math.pow(2,-2) * Math.pow(10,-22);
        return 0;
    }

    @Override
    public double retrieveAl1(){
        //double aI1 = toDecimalfromBinaryString(this.word5.getWord(17,28),true);
        //return aI1 * Math.pow(2,-8) * Math.pow(10,-22);
        return 0;
    }

    @Override
    public double retrieveAl2(){
        //double aI2 = toDecimalfromBinaryString(this.word5.getWord(28,42),true);
        //return aI2 * Math.pow(2,-15) * Math.pow(10,-22);
        return 0;
    }

    @Override
    public double retrieveAl3(){
        //return (this.word5.getChar(42));
        return 0;
    }

    @Override
    public double retrieveBt0(){
        //return (this.word5.getChar(43));
        return 0;
    }

    @Override
    public double retrieveBt1(){
        //return (this.word5.getChar(44));
        return 0;
    }

    @Override
    public double retrieveBt2(){
        //return (this.word5.getChar(45));
        return 0;
    }

    @Override
    public double retrieveBt3(){
        //return (this.word5.getChar(46));
        return 0;
    }

}
