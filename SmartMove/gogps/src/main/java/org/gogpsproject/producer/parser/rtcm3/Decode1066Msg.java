///=================================================================================================
// Class Decode1066Msg
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

import android.location.GnssStatus;
import android.util.Log;

import org.gogpsproject.ephemeris.GNSSEphemerisCorrections;
import org.gogpsproject.ephemeris.PreciseCorrection;
import org.gogpsproject.util.Bits;

import java.util.HashMap;

public class Decode1066Msg implements Decode {

    public Decode1066Msg() {

    }

    public Object decode(boolean[] bits, int referenceTS) {

        GNSSEphemerisCorrections mEphCorrections = new GNSSEphemerisCorrections();
        HashMap< Integer, PreciseCorrection> gpsCorrections = new HashMap<>();

        int i =0;

        //Message parameters for Glonass constellation
        int indexPrn = 5;
        int indexIode = 8;
        int indexIodc = 0;
        int indexOffPrn = 0;

        // Decoding header
        i += 12;
        int tod = (int)  Bits.bitsToUInt(Bits.subset(bits, i, 17));
        i += 17;
        int updateInterval = (int)  Bits.bitsToUInt(Bits.subset(bits, i, 4));
        i += 4;
        i += 1 + 1 + 4 +16 + 4; //other information of the header
        int nbSat = (int) Bits.bitsToUInt(Bits.subset(bits, i, 6));

        // first bit of message
        i = 65;
        //Start decoding
        try {
            for(int j = 0; j < nbSat; j++)
            {
                PreciseCorrection mEphCorr = new PreciseCorrection();
                mEphCorr.setGnssSystem(GnssStatus.CONSTELLATION_GLONASS);
                mEphCorr.setPrn((int) Bits.bitsToUInt(Bits.subset(bits, i, indexPrn)));
                i += indexPrn;
                mEphCorr.setIode((int) Bits.bitsToUInt(Bits.subset(bits, i, indexIode)));
                i += indexIode;
                i += indexIodc;

                //Orbits
                mEphCorr.seteRadial((double) Bits.bitsTwoComplement(Bits.subset(bits, i, 22))* 1e-4);
                i += 22;
                mEphCorr.seteAlong((double) Bits.bitsTwoComplement(Bits.subset(bits, i, 20)) * 4e-4);
                i += 20;
                mEphCorr.seteCross((double) Bits.bitsTwoComplement(Bits.subset(bits, i, 20)) * 4e-4);
                i += 20;
                mEphCorr.seteDotRadial((double) Bits.bitsTwoComplement(Bits.subset(bits, i, 21)) * 1e-6);
                i += 21;
                mEphCorr.seteDotAlong((double) Bits.bitsTwoComplement(Bits.subset(bits, i, 19)) * 4e-6);
                i += 19;
                mEphCorr.seteDotCross((double) Bits.bitsTwoComplement(Bits.subset(bits, i, 19)) * 4e-6);
                i += 19;

                //Clocks
                mEphCorr.setC0((double) Bits.bitsTwoComplement(Bits.subset(bits, i, 22))* 1e-4);
                i += 22;
                mEphCorr.setC1((double) Bits.bitsTwoComplement(Bits.subset(bits, i, 21))* 1e-6);
                i += 21;
                mEphCorr.setC2((double) Bits.bitsTwoComplement(Bits.subset(bits, i, 27))* 2e-8);
                i += 27;

                mEphCorr.setTod(tod);
                mEphCorr.setUpdateInterval(updateInterval);

                mEphCorrections.setCorrection(mEphCorr);

            }
        }
        catch (Exception e)
        {
            Log.e("STREAMS", "RTCM3 1060 error length " + e);
            return -1;
        }


        return mEphCorrections;
    }
}
