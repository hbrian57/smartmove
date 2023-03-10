///=================================================================================================
// Class Streams
//      Author :  Antoine GRENIER - 2019/09/06
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
package fr.ifsttar.geoloc.geoloclib.streams;

import android.util.Log;

import org.gogpsproject.producer.parser.rtcm3.ConnectionSettings;

import java.util.ArrayList;
import java.util.List;

public class Streams
{
    public StreamEphemerisHandler corrSel;
    public StreamEphemerisHandler ephSel;

    public ArrayList<StreamEphemerisHandler> listSEH;

    private ConnectionParameters connectionParameters;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public Streams(ConnectionParameters _connectionParameters)
    {
        listSEH = new ArrayList<>();

        connectionParameters = _connectionParameters;

    }

    //----------------------------------------------------------------------------------------------

    /**
     * Register a connection to a mount point for stream retrieval.
     * @param mountpoint Mount point name
     * @return Listener of the stream
     */
    public boolean registerCorrectionStream(String mountpoint)
    {
        StreamEphemerisHandler rrtcm;
        boolean success = false;

        RTCMRunnable rtcmRunnable = new RTCMRunnable(
                connectionParameters.getHost(),
                connectionParameters.getPort(),
                connectionParameters.getUser(),
                connectionParameters.getPassword(),
                mountpoint);

        Thread thread = new Thread(rtcmRunnable);
        thread.start();
        try {
            thread.join();
        }
        catch(Exception e)
        {
            // do something
        }

        rrtcm = rtcmRunnable.getSEL();

        if(rrtcm != null)
        {
            listSEH.add(rtcmRunnable.getSEL());
            success = true;
        }

        return success;
    }

    //----------------------------------------------------------------------------------------------
}
