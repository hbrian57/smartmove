///=================================================================================================
// Class Options
//      Originally written by Antoine GRENIER
//      Enhanced and Updated by Aravind RAMESH
//        Date :  11/08/2022
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
package fr.ifsttar.geoloc.geoloclib;

import java.util.Objects;
import java.util.Vector;

/**
 * Class Options
 *
 * Storing computation options inputted by user.
 */
public class Options {

    private Vector<Integer> systemsEnabled;

    private boolean sppEnabled;
    private boolean pppEnabled;
    private boolean monoFrequencyEnabled;
    private boolean dualFrequencyEnabled;
    private boolean ionofreeEnabled;
    private boolean ionoCorrEnabled;

    //private boolean kalmanEnabled;

    //private boolean streamsEnabled;
    //private boolean streamsCorrectionEnabled;

    //private boolean smoothingEnabled;

    //private boolean dcbEnabled;

    private boolean dynamicMode;

    private boolean tropoEnabled;

    private float cutoffAngle; // decimal degrees
    private float cutoffNoise;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public Options()
    {
        systemsEnabled = new Vector<>();

        sppEnabled = false;
        pppEnabled = false;
        monoFrequencyEnabled = false;
        dualFrequencyEnabled = false;
        ionofreeEnabled = false;

        //streamsEnabled = false;
        //streamsCorrectionEnabled = false;

    }

    //----------------------------------------------------------------------------------------------

    public boolean isSppEnabled() {
        return sppEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setSppEnabled(boolean sppEnabled) {
        this.sppEnabled = sppEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isPppEnabled() {
        return pppEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setPppEnabled(boolean pppEnabled) {
        this.pppEnabled = pppEnabled;
    }

    //----------------------------------------------------------------------------------------------

    /*public boolean isStreamsEnabled() {
        return streamsEnabled;
    }*/

    //----------------------------------------------------------------------------------------------

    /*public void setStreamsEnabled(boolean streamsEnabled) {
        this.streamsEnabled = streamsEnabled;
    }*/

    //----------------------------------------------------------------------------------------------

    /*public boolean isStreamsCorrectionEnabled() {
        return streamsCorrectionEnabled;
    }*/

    /*public void setStreamsCorrectionEnabled(boolean streamsCorrectionEnabled) {
        this.streamsCorrectionEnabled = streamsCorrectionEnabled;
    }*/

    //----------------------------------------------------------------------------------------------

    public Vector<Integer> getSystemsEnabled() {
        return systemsEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setSystemsEnabled(Vector<Integer> systemsEnabled) {
        this.systemsEnabled = systemsEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isMonoFrequencyEnabled() {
        return monoFrequencyEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setMonoFrequencyEnabled(boolean monoFrequencyEnabled) {
        this.monoFrequencyEnabled = monoFrequencyEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isDualFrequencyEnabled() {
        return dualFrequencyEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setDualFrequencyEnabled(boolean dualFrequencyEnabled) {
        this.dualFrequencyEnabled = dualFrequencyEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isIonofreeEnabled() {
        return ionofreeEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isIonoCorrEnabled(){return ionoCorrEnabled;}

    //----------------------------------------------------------------------------------------------

    public void setIonoCorrEnabled(boolean ionoCorrEnabled){
        this.ionoCorrEnabled = ionoCorrEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setIonofreeEnabled(boolean ionofreeEnabled) {
        this.ionofreeEnabled = ionofreeEnabled;
    }

    //----------------------------------------------------------------------------------------------

    /*public void setSmoothingEnabled(boolean smoothingEnabled)
    {
        this.smoothingEnabled = smoothingEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isSmoothingEnabled() {
        return smoothingEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isKalmanEnabled() {
        return kalmanEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setKalmanEnabled(boolean kalmanEnabled) {
        this.kalmanEnabled = kalmanEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isDcbEnabled() {
        return dcbEnabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setDcbEnabled(boolean dcbEnabled) {
        this.dcbEnabled = dcbEnabled;
    }*/

    //----------------------------------------------------------------------------------------------

    public boolean isDynamicMode() {
        return dynamicMode;
    }

    //----------------------------------------------------------------------------------------------

    public void setDynamicMode(boolean dynamicMode) {
        this.dynamicMode = dynamicMode;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isTropoEnabled() {
        return tropoEnabled;
    }

    //----------------------------------------------------------------------------------------------

    /*public void setTropoEnabled(boolean tropoEnabled) {
        this.tropoEnabled = tropoEnabled;
    }*/

    //----------------------------------------------------------------------------------------------

    public void setCutoffAngle(float cutoffAngle) {
        this.cutoffAngle = cutoffAngle;
    }

    //----------------------------------------------------------------------------------------------

    public void setCutoffNoise(float cutoffNoise) {
        this.cutoffNoise = cutoffNoise;
    }

    //----------------------------------------------------------------------------------------------

    public float getCutoffAngle() {
        return cutoffAngle;
    }

    //----------------------------------------------------------------------------------------------

    public float getCutoffNoise() {
        return cutoffNoise;
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Options options = (Options) o;
        return  sppEnabled == options.sppEnabled &&
                pppEnabled == options.pppEnabled &&
                monoFrequencyEnabled == options.monoFrequencyEnabled &&
                dualFrequencyEnabled == options.dualFrequencyEnabled &&
                ionofreeEnabled == options.ionofreeEnabled &&
                ionoCorrEnabled == options.ionoCorrEnabled &&
                systemsEnabled.equals(options.systemsEnabled);
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return Objects.hash(systemsEnabled, sppEnabled, pppEnabled, monoFrequencyEnabled,
                dualFrequencyEnabled, ionofreeEnabled, ionoCorrEnabled);
    }
}