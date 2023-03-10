///=================================================================================================
// Class SatelliteCodeBias
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
import java.util.LinkedHashMap;

public class SatelliteCodeBiases
{
    private HashMap<String, CodeBias> cb;

    //----------------------------------------------------------------------------------------------

    public SatelliteCodeBiases()
    {
        cb = new HashMap<>();
    }

    //----------------------------------------------------------------------------------------------

    public SatelliteCodeBiases(SatelliteCodeBiases satelliteCodeBias)
    {
        cb = satelliteCodeBias.getCb();
    }

    //----------------------------------------------------------------------------------------------

    public void setCodeBias(CodeBias codeBias)
    {
        cb.put(Utils.getFormattedSatIndex(codeBias.system, codeBias.prn), codeBias);
    }

    //----------------------------------------------------------------------------------------------

    public CodeBias getSatBiasById(int system, int prn)
    {
        return cb.get(Utils.getFormattedSatIndex(system, prn));
    }

    //----------------------------------------------------------------------------------------------

    public HashMap<String, CodeBias> getCb() {
        return cb;
    }

    //----------------------------------------------------------------------------------------------

    public void setCb(HashMap<String, CodeBias> cb) {
        this.cb = cb;
    }

    //----------------------------------------------------------------------------------------------

    public Object copy()
    {
        return new SatelliteCodeBiases(this);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Class CodeBias
     *
     * Structure for storing the code bias retrieved.
     */
    static public class CodeBias
    {
        private int prn;
        private int system;
        private double tow;
        private double tod; //for glonass
        private int iod;
        private LinkedHashMap<Integer, Double> biases;

        public CodeBias(int _system, int _prn, double _tow, int _iod, LinkedHashMap _biases)
        {
            system = _system;
            prn = _prn;
            tow = _tow;
            iod = _iod;
            biases = _biases;
        }

        public CodeBias(CodeBias codeBias)
        {
            this.system = codeBias.system;
            this.prn = codeBias.prn;
            this.biases = codeBias.biases;
            this.tow = codeBias.tow;
            this.iod = codeBias.iod;
            this.tod = codeBias.iod;
        }

        public String toString(){

            String str = "System : " + system + "\n"
                    + "prn : " + prn + "\n"
                    + "TOW : " + tow + "\n"
                    + "TOD : " + tod + "\n"
                    + "IOD : " + iod + "\n";

            return str;
        }

        public void setTow(double tow) {
            this.tow = tow;
        }

        public void setIod(int iod) {
            this.iod = iod;
        }

        public double getTow() {
            return tow;
        }

        public int getIod() {
            return iod;
        }

        public LinkedHashMap<Integer, Double> getBiases() {
            return biases;
        }

        public void setBiases(LinkedHashMap<Integer, Double> biases) {
            this.biases = biases;
        }

        public void setTod(double tod) {
            this.tod = tod;
        }
        public double getTod(){return tod;}
    }
}
