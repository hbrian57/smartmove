///=================================================================================================
// Class GlonassNavigationMessage
//      Author :  Edgar LENHOF
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

import java.io.IOException;
import java.math.BigInteger;


/**
 * Class to store and retrieve data from glonass navigation message
 */
public class GlonassNavigationMessage {


    //navigation message from current satellite
    private GnssNavigationMessage mGnssNavigationMessage;
     //id : satellite id
    private int id;
     //mData: 11 bytes Navigation Message
    private byte[] mData;
     //strWords: words of variable length composing a 85 bits string
    private String str;


    GlonassNavigationMessage(GnssNavigationMessage mGnssNavigationMessage){
        this.mGnssNavigationMessage = mGnssNavigationMessage;
        id=this.mGnssNavigationMessage.getSvid();
        mData = this.mGnssNavigationMessage.getData();
        str = toBinaryStringFromByteArray(mData);

    }



    /**
     * Converts the byte data of navigation message to string of bits
     * @param b 11 bytes array
     * @return 85 characters string
     */
    private String toBinaryStringFromByteArray(byte[] b){

        String str = "";
        for (byte value : b) {
            String code = String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
            str += code;
        }

        String message = str.substring(0,85); //getting read off last 3 bits added by the Android API

        return  message;
    }



    /**
     * Extract word from the string navigation message as detailed in the Glonass ICD
     * @param firstbit first bit of the word in the string
     * @param lastbit last bit of the word
     * @return word as string of bits
     */
    public String getWord(int firstbit, int lastbit){

        // bits in nav_msg sorted like [85 84 83 ... 3 2 1]
        // but string indexes like [0 1 2 ... 82 83 84]
        return str.substring(str.length()-lastbit, str.length()-(firstbit-1));

    }



    /**
     * @return subframe id of navigation message
     */
    public int getSubFrameid(){
        return mGnssNavigationMessage.getSubmessageId();
    }



}
