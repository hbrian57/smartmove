///=================================================================================================
// Class Decode1020Msg
//      Author :  Edgar LENHOF
//        Date :  02/2020
///=================================================================================================
// This code is based on the GoGPS library - Geomatics Laboratory of Politecnico di Milano, Italy.
// The GoGPS library is licenced under the LGPL.
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


package org.gogpsproject.producer.parser.rtcm3;

import android.util.Log;

import org.gogpsproject.Constants;
import org.gogpsproject.ephemeris.GlonassEphemeris;
import org.gogpsproject.util.Bits;

public class Decode1020Msg implements Decode{

    public Decode1020Msg() {

    }

    public Object decode(boolean[] bits, int referenceTS) {
        GlonassEphemeris mEph = new GlonassEphemeris();

        int i = 0;

        i += 12; //Bits of message number

        //Start decoding
        try {
            mEph.setPrn((int) Bits.bitsToUInt(Bits.subset(bits, i, 6)));
            i += 6;
            i += 5+2+2; //frequency information
            mEph.setTk( ( Bits.bitsToUInt(Bits.subset(bits, i, 5)) * 3600
                    + Bits.bitsToUInt(Bits.subset(bits, i+5, 6)) * 60
                    + Bits.bitsToUInt(Bits.subset(bits, i+5+6, 1)) * 30) );
            i += 5+6+1;
            mEph.setBn((int) Bits.bitsToUInt(Bits.subset(bits, i, 1)));
            i += 1+1; //Bn
            mEph.setTb((int) Bits.bitsToUInt(Bits.subset(bits, i, 7)) * 15);
            i += 7;
            mEph.setX_dot(getValue(bits, i,24)*Constants.P2_20);
            i += 24;
            mEph.setX(getValue(bits, i,27)*Constants.P2_11);
            i += 27;
            mEph.setX_ddot(getValue(bits, i,5)*Constants.P2_30);
            i += 5;
            mEph.setY_dot(getValue(bits, i,24)*Constants.P2_20);
            i += 24;
            mEph.setY(getValue(bits, i,27)*Constants.P2_11);
            i += 27;
            mEph.setY_ddot(getValue(bits, i,5)*Constants.P2_30);
            i += 5;
            mEph.setZ_dot(getValue(bits, i,24)*Constants.P2_20);
            i += 24;
            mEph.setZ(getValue(bits, i,27)*Constants.P2_11);
            i += 27;
            mEph.setZ_ddot(getValue(bits, i,5)*Constants.P2_30);
            i += 5+1;
            mEph.setGamma_n(getValue(bits, i,11)*Constants.P2_40);
            i += 11+3;
            mEph.setTau_n(getValue(bits, i,22)*Constants.P2_30);

            mEph.computeGregorianDate();
            mEph.computeWn();
        }
        catch (Exception e)
        {
            Log.e("STREAMS", "RTCM3 1020 error length" + e);
            return -1;
        }

        return mEph;
    }

    /**
     * gets the value of the word in the rtcm3 message
     * @param bits full RTCM3 message
     * @param start first bit of the word
     * @param length length of the word
     * @return
     */
    private double getValue(boolean[] bits, int start, int length) {

        int sign = 1;
        if (bits[start]){ // 0 is + and 1 is -
            sign = -1;
        }
        return (double)(Bits.bitsToUInt(Bits.subset(bits, start+1, length-1)) * sign);
    }
}
