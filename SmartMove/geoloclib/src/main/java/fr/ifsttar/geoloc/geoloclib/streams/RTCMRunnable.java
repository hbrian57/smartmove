///=================================================================================================
// Class RTCMRunnable
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

import org.gogpsproject.positioning.Coordinates;
import org.gogpsproject.producer.parser.rtcm3.RTCM3Client;

/**
 * Class RTCMRunnable
 *
 * Allow reception of a stream in a different thread.
 */
public class RTCMRunnable implements Runnable {

    private String mHost;
    private int mPort;
    private String mUser;
    private String mPassword;
    private String mMountpoint;

    private volatile StreamEphemerisHandler rrtcm;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * @param host Host adress
     * @param port Port
     * @param user User ID
     * @param password User password
     * @param mountpoint Mountpoint name
     */
    public RTCMRunnable(String host, int port, String user, String password, String mountpoint)
    {
        mHost = host;
        mPort = port;
        mUser = user;
        mPassword = password;
        mMountpoint = mountpoint;
    }

    //----------------------------------------------------------------------------------------------

    public StreamEphemerisHandler getSEL()
    {
        return rrtcm;
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void run() {
        try
        {
            Coordinates coordinates = Coordinates.globalGeodInstance(48.56186, 1.48164, 500);

            RTCM3Client mrtcmclient = RTCM3Client.getInstance(mHost, mPort, mUser, mPassword, mMountpoint);

            mrtcmclient.setVirtualReferenceStationPosition(coordinates);
            mrtcmclient.setMarkerName(mMountpoint);
            mrtcmclient.setReconnectionPolicy(RTCM3Client.CONNECTION_POLICY_RECONNECT);
            mrtcmclient.setExitPolicy(RTCM3Client.EXIT_NEVER);
            mrtcmclient.setReconnectionWaitingTime(10);
            mrtcmclient.setDebug(false);

            rrtcm = new StreamEphemerisHandler();

            mrtcmclient.init();

            mrtcmclient.addStreamEventListener(rrtcm);

        } catch (Exception e) {
            e.printStackTrace();
            rrtcm = null;
        }
    }
}
