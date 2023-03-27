///=================================================================================================
// Class StreamEphemerisHandler
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

import org.gogpsproject.ephemeris.EphGps;
import org.gogpsproject.ephemeris.GNSSEphemeris;
import org.gogpsproject.ephemeris.GNSSEphemerisCorrections;
import org.gogpsproject.ephemeris.SatelliteCodeBiases;
import org.gogpsproject.positioning.Coordinates;
import org.gogpsproject.producer.Observations;
import org.gogpsproject.producer.StreamEventListener;
import org.gogpsproject.producer.parser.IonoGps;

import java.util.HashMap;

import fr.ifsttar.geoloc.geoloclib.Utils;

/**
 * Class StreamEphemerisHandler
 *
 * Implements the interface StreamEventListener from GoGPS
 *
 * Handle the reception of new streams for parsing of messages.
 */
public class StreamEphemerisHandler implements StreamEventListener
{
    private HashMap<String, GNSSEphemeris>  mEphemerisGNSS;
    private SatelliteCodeBiases  scb;
    private GNSSEphemerisCorrections mEphemerisGNSSCorr;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public StreamEphemerisHandler()
    {
        this.mEphemerisGNSS = new HashMap<>();
        this.mEphemerisGNSSCorr = new GNSSEphemerisCorrections();
        this.scb = new SatelliteCodeBiases();
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void streamClosed() {

    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void addObservations(Observations o) {

    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void addEphemeris(EphGps eph) {

    }

    //----------------------------------------------------------------------------------------------

    public void addEphemeris(GNSSEphemeris eph)
    {
        // TODO fix problem with glonass constellation problem
        String str = Utils.getFormattedSatIndex(eph.getGnssSystem(), eph.getPrn());
        mEphemerisGNSS.put(str , eph.copy());
    }

    //----------------------------------------------------------------------------------------------

    public HashMap< String, GNSSEphemeris>  getCurrentEphemeris()
    {
        return mEphemerisGNSS;
    }

    //----------------------------------------------------------------------------------------------

    public void addEphemerisCorr(GNSSEphemerisCorrections ephCorr)
    {
        this.mEphemerisGNSSCorr.getCorrections().putAll(ephCorr.getCorrections());
    }

    //----------------------------------------------------------------------------------------------

    public GNSSEphemerisCorrections getEphemerisCorrections()
    {
        return this.mEphemerisGNSSCorr;
    }

    //----------------------------------------------------------------------------------------------

    public void addSatelliteCodeBiases(SatelliteCodeBiases _scb)
    {
        this.scb.getCb().putAll(_scb.getCb());
    }

    //----------------------------------------------------------------------------------------------

    public SatelliteCodeBiases getSatelliteCodeBiases()
    {
        return scb;
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void addIonospheric(IonoGps iono) {

    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void setDefinedPosition(Coordinates definedPosition) {

    }

    //----------------------------------------------------------------------------------------------

    @Override
    public Observations getCurrentObservations()
    {

        return null;
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void pointToNextObservations() {

    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void onStreamReceived(String msg)
    {
    }
}

