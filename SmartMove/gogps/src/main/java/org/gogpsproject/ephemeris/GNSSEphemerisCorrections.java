///=================================================================================================
// Class GNSSEphemerisCorrections
//      Author :  Antoine GRENIER
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
package org.gogpsproject.ephemeris;

import org.gogpsproject.Utils;

import java.util.HashMap;

public class GNSSEphemerisCorrections
{
    private HashMap<String, PreciseCorrection> corrections;

    //----------------------------------------------------------------------------------------------

    public GNSSEphemerisCorrections()
    {
        this.corrections = new HashMap<>();
    }

    //----------------------------------------------------------------------------------------------

    public GNSSEphemerisCorrections(GNSSEphemerisCorrections ephCorr)
    {

        this.corrections = (HashMap<String, PreciseCorrection>) ephCorr.getCorrections().clone();
    }

    //----------------------------------------------------------------------------------------------

    public Object copy()
    {
        return new GNSSEphemerisCorrections(this);
    }

    //----------------------------------------------------------------------------------------------

    public void setCorrection(PreciseCorrection preciseCorrection)
    {
        String key = Utils.getFormattedSatIndex(preciseCorrection.getGnssSystem(), preciseCorrection.getPrn());

        this.corrections.put(key, new PreciseCorrection(preciseCorrection));
    }

    //----------------------------------------------------------------------------------------------

    public HashMap<String, PreciseCorrection> getCorrections() {
        return corrections;
    }

    //----------------------------------------------------------------------------------------------

    public PreciseCorrection getSatCorrectionById(int system, int prn)
    {
        return corrections.get(Utils.getFormattedSatIndex(system,prn));
    }

    //----------------------------------------------------------------------------------------------
}
