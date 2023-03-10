///=================================================================================================
// Class Utils
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
package org.gogpsproject;

import android.location.GnssStatus;

import java.util.GregorianCalendar;

public class Utils {

    static public char getConstellationLetter(int system) {
        switch (system) {
            case GnssStatus.CONSTELLATION_GPS:
                return 'G';
            case GnssStatus.CONSTELLATION_GLONASS:
                return 'R';
            case GnssStatus.CONSTELLATION_GALILEO:
                return 'E';
            case GnssStatus.CONSTELLATION_BEIDOU:
                return 'C';

            default:
                return 'U';
        }
    }

    static public String getFormattedSatIndex(int system, int prn) {
        char systemStr = Utils.getConstellationLetter(system);
        String str = String.format("%c %02d", systemStr, prn);

        return str;
    }

    /**
     *
     * @param date
     * @return
     */
    public static String dateToString(GregorianCalendar date){
        String str = "Date : "  + date.get(date.DAY_OF_MONTH) +"/"+ (date.get(date.MONTH)+1) +"/"+ date.get(date.YEAR)
                +"   "+ date.get(date.HOUR_OF_DAY) +":"+ date.get(date.MINUTE) + "'"+ date.get(date.SECOND)+"'' "+date.getTimeZone().getDisplayName();
        return str;
    }

}
