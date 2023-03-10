///=================================================================================================
// Class GalileoNavigationMessage
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

import android.util.Log;
import java.util.Arrays;
import java.math.BigInteger;

public class GalileoNavigationMessage {

    private GnssNavigationMessage mGnssNavigationMessage;

    /**
     * mData: 30 bytes Navigation Message
     */
    private byte[] mData;

    private String binStr;

    GalileoNavigationMessage(GnssNavigationMessage mGnssNavigationMessage){
        this.mGnssNavigationMessage = mGnssNavigationMessage;
        mData = this.mGnssNavigationMessage.getData();
        //Log.d("GalileoNavigationMsg", Arrays.toString(mData));
        binStr = toBinaryStringFromByteArray(mData);
    }
    /**
     * Converts navigation byte data to bit string
     * @param b 30 bytes 1-D array
     * @return 128 characters (bit) string
     */
    private String toBinaryStringFromByteArray(byte[] b){

        //BigInteger intWord = new BigInteger(b);
        StringBuilder str = new StringBuilder();
        for (byte value : b) {
            String code = String.format("%8s", Integer.toBinaryString(value & 255)).replace(' ', '0');
            str.append(code);
        }

         String page = str.substring(0,239);
         String page1 = page.substring(2,114);
         String page2 = page.substring(122,138);
         return (page1 + page2);

    }
    public String getWord(int begin, int end){
        return binStr.substring(begin, end);
    }

    /*public char getChar(int idx){
        return binStr.charAt(idx);
    }*/
}