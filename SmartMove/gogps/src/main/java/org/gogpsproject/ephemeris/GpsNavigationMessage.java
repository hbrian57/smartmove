///=================================================================================================
// Class GpsNavigationMessage
//      Author :  Jose Gilberto RESENDIZ FONSECA
//      Updated by Aravind RAMESH, 11/08/2022
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

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by joseresendiz on 24/04/18.
 */

/**
 * a class to store the data received by the gnss chip
 */
public class GpsNavigationMessage {
    /**
     * mGnssNavigationMessage: navigation message from current satellite
     */
    private GnssNavigationMessage mGnssNavigationMessage;
    /**
     * id : satellite id
     */
    private int id;
    /**
     * mData: 40 bytes Navigation Message
     */
    private byte[] mData;
    /**
     * words: 10 words of 4 bytes raw data
     */
    private byte[][] words;
    /**
     * strWords: 10 words of 30 bits raw data
     */
    private String[] strWords;

    GpsNavigationMessage(GnssNavigationMessage mGnssNavigationMessage){
        this.mGnssNavigationMessage = mGnssNavigationMessage;
        id=this.mGnssNavigationMessage.getSvid();
        mData = this.mGnssNavigationMessage.getData();
        //Log.d("GPSNavigationMsg", Arrays.toString(mData));
        words = setWords();
        strWords = setStrWords();
    }

    //segmenting the 40 bytes of data into 10 words of 4 bytes
    private byte[][] setWords(){
        byte[][] words = new byte[10][4];
        for (int i = 0; i<10 ; i++){
            words[i] = new byte[]{mData[i * 4], mData[i * 4 + 1], mData[i * 4 + 2], mData[i * 4 + 3]};
        }
        return words;
    }

    //converting words to 30 character strings
    private String[] setStrWords(){
        String[] strWords = new String[10];
        for (int i = 0; i<10 ; i++){
            strWords[i] = toBinaryStringFromByteArray(words[i]);
        }
        return strWords;
    }

    //getting satellite id
    public int getId(){
        return id;
    }

    /**
     *
     * @return PRN from navigation message
     */
    public int getPrn(){
        return Integer.parseInt(strWords[0].substring(8,14),2);
    }

    /**
     *
     * @return TOW from navigation message
     */
    public long getTow(){
        String tow = strWords[1].substring(0,17);
        return Long.parseLong(tow,2)*6;
    }

    /**
     *
     * @return Page id for Subframe 4 / Subframe 5 of navigation message
     */
    public int getPageid(){
        return mGnssNavigationMessage.getMessageId();
    }

    /**
     *
     * @param index index in the word
     * @return string word by index
     */
    public String getWord(int index){
        return strWords[index];
    }


    /**
     *
     * @param b 4 byte word
     * @return 30 character string
     */

    private String toBinaryStringFromByteArray(byte[] b){
        BigInteger intWord = new BigInteger(b);
        String strWord = String.format("%30s", Integer.toBinaryString(intWord.intValue())).replace(' ', '0');
        return strWord;
    }
}