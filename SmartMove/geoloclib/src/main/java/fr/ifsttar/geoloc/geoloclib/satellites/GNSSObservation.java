///=================================================================================================
// Class GNSSObservation
//      Author : Aravind RAMESH, Research Engineer, AME-GEOLOC Laboratory, UGE, Nantes
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
package fr.ifsttar.geoloc.geoloclib.satellites;

import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssStatus;
import android.util.Log;

import org.gogpsproject.Constants;

import fr.ifsttar.geoloc.geoloclib.Options;
import fr.ifsttar.geoloc.geoloclib.Utils;
import fr.ifsttar.geoloc.geoloclib.computations.GNSSPositioning;

import static android.location.GnssMeasurement.ADR_STATE_VALID;
import static android.location.GnssMeasurement.STATE_CODE_LOCK;
import static android.location.GnssMeasurement.STATE_GAL_E1B_PAGE_SYNC;
import static android.location.GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK;
import static android.location.GnssMeasurement.STATE_GLO_TOD_DECODED;
import static android.location.GnssMeasurement.STATE_GLO_TOD_KNOWN;
import static android.location.GnssMeasurement.STATE_TOW_DECODED;
import static android.location.GnssMeasurement.STATE_TOW_KNOWN;

import java.util.Arrays;

/**
 * Class GNSSObservation
 *
 * Store the measurements of one satellite, for computations of observations (e.g., pseudoranges)
 *
 * References : [1] ESA, GNSS Data Processing, Volume 1: Fundamentals and Algorithms
 */
public class GNSSObservation {

    private GnssClock mGnssClock;

    /**
     * mGnssMeasurement the data from the chip
     */
    private GnssMeasurement gnssMeasurementL1;
    private GnssMeasurement gnssMeasurementL5;

    /**
     * id : id of the satellite
     */
    private int id;

    /**
     * constellation constellation of current satellite
     */
    private int constellation;

    /**
     * ttx time at signal transmission
     */
    private double ttx;

    /**
     * trx time at signal received UTC
     */
    private double trx;

    /**
     * trxGnss time at signal received GPS TIME
     */
    private double trxGnss;


    private long fullBiasNanosFix; /*this value must be fixed because of clock resets in the receiver*/
    //private long rxSVTNFix;
    //private long rxTNFix;
    private double biasNanosFix;

    private double transmissionTime;

    private double carrFreq;
    private double pseudorangeL1;
    private double pseudorangeL5;
    private double phaseL1;
    private double phaseL5;
    private double cn0L1;
    private double cn0L5;
    private boolean L1Enabled;
    private boolean L5Enabled;

    /* Observations of the iono-free combination*/
    private double pseudorangeL3;
    private double phaseL3;
    private boolean L3Enabled;

    /* Observation for Pseudorate from Doppler Measurements*/
    private double pseudoRateL1;
    private double pseudoRateL5;
    private double pseudoRateL3;

    /* Variables for Smoothed pseudorange for future use*/
    private double smoothPseudoL1;
    private double smoothPseudoL5;
    private double smoothPseudoL3;
    private double smoothPseudoDF; /* Divergence-free combination*/
    private double phaseDF;
    private boolean smoothL1Enabled;
    private boolean smoothL5Enabled;
    private boolean smoothL3Enabled;
    private boolean multL1Stat = false;
    //private boolean multL5Stat;

    private int nbObsL1;
    private int nbObsL5;
    private int nbObsL3;
    private int varL5 = 0;
    private int varL5_trk = 0;
    private int varL1 = 0;

    private int cycleSlipL1;
    private int cycleSlipL5;

    /* Previous observation with this sat at epoch k-1*/
    private GNSSObservation prevObs;

    /* Satellite position for this measurement*/

    private SatellitePositionGNSS satellitePosition;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public GNSSObservation()
    {

    }

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * @param mGnssClock GNSS clock of measurement
     * @param _fullBiasNanosFix First value of the clock fullBias
     * @param _biasNanosFix First value of the clock bias
     */
    public GNSSObservation(GnssClock mGnssClock, long _fullBiasNanosFix, double _biasNanosFix)
    {
        this.mGnssClock = mGnssClock;
        this.fullBiasNanosFix = _fullBiasNanosFix;
        this.biasNanosFix = _biasNanosFix;
        //this.rxSVTNFix = _rxSVTNFix;
        //this.rxTNFix = _rxTNFix;
    }

    //----------------------------------------------------------------------------------------------

    /*/**
     * Searching for cycle slip or multipath in the measurement.
     * @return Continuity validity
     */
    /*public boolean isContinuityValid()
    {
        /// Checking L1 measurements
        if(gnssMeasurementL1 != null)
        {
            // Combination of several flags, see GnssMeasurements developer site for more info ...
            if((gnssMeasurementL1.getAccumulatedDeltaRangeState() & ADR_STATE_VALID) != 0)
            {
                cycleSlipL1 = 1;
                return false;
            }

            if(gnssMeasurementL1.getMultipathIndicator() == GnssMeasurement.MULTIPATH_INDICATOR_DETECTED)
            {
                return false;
            }
        }

        /// Checking L5 measurements
        else if(gnssMeasurementL5 != null)
        {
            // Combination of several constants, see GnssMeasurements developer site for more info ...
            if((gnssMeasurementL5.getAccumulatedDeltaRangeState() & ADR_STATE_VALID) != 0)
            {
                cycleSlipL5 = 1;
                return false;
            }

            if(gnssMeasurementL5.getMultipathIndicator() == GnssMeasurement.MULTIPATH_INDICATOR_DETECTED)
            {
                return false;
            }
        }

        return true;
    }*/

    /*/**
     * Searching for Multipath in the measurement.
     * @return Multipath status
     */
    /*public boolean isMultipathPresent()
    {
        return multL1Stat;
    }*/

    //Unwanted code
    /*/// Checking L1 measurements
        if(gnssMeasurementL1 != null)
    {
        if (gnssMeasurementL1.getMultipathIndicator() == GnssMeasurement.MULTIPATH_INDICATOR_DETECTED)
        {
            return true;
        }

    }

    /// Checking L5 measurements
        if(gnssMeasurementL5 != null)
    {
        if (gnssMeasurementL5.getMultipathIndicator() == GnssMeasurement.MULTIPATH_INDICATOR_DETECTED)
        {
            return true;
        }
    }

        return false;*/

    //----------------------------------------------------------------------------------------------

    /**
     * Check if the measurement suitable with the current processing options.
     * @param options Current computations options
     * @return Measurement validity
     */
    public boolean isMeasurementValid(Options options)
    {
        String prn = Utils.getFormattedSatIndex(constellation, id);
        double cutoffNoise = options.getCutoffNoise();

        /* Checking constellation*/
        if(!options.getSystemsEnabled().contains(constellation))
        {
            return false;
        }

        /* Checking L1 measurements*/
        if(gnssMeasurementL1 != null)
        {
            if(gnssMeasurementL1.getCn0DbHz() < cutoffNoise)
            {
                return false;
            }
        }
        /* Checking L5 measurements*/
        if(gnssMeasurementL5 != null)
        {
            ///
            if(!options.isDualFrequencyEnabled() && !options.isIonofreeEnabled())
            {
                return false;
            }
            if(gnssMeasurementL5.getCn0DbHz() < cutoffNoise)
            {
                return false;
            }
        }

        return true;
    }

    /*/**
     * Function facilitating L1 only (single frequency) solution
     * @param options User options
     * @returns boolean true/false
     */

    /*public boolean isL5MeasurementValid(Options options)
    {
        double cutoffNoise = options.getCutoffNoise();

        /// Checking constellation
        if(!options.getSystemsEnabled().contains(constellation))
        {
            return false;
        }

        /// Checking L5 measurements
        if(gnssMeasurementL5 != null && gnssMeasurementL5.getCn0DbHz() < cutoffNoise)
        {
            return false;
        }
        else
        {
            return true;
        }

    }*/

    //----------------------------------------------------------------------------------------------

    /**
     * Retrieve and parse the observations into the class variable.
     * The iono-free observation is computed if both frequencies are filled.
     * @param gnssMeasurement GNSS Measurement
     */
    public boolean setGnssMeasurement(GnssMeasurement gnssMeasurement, Options options)
    {
        this.id = gnssMeasurement.getSvid();
        this.constellation = gnssMeasurement.getConstellationType();
        this.carrFreq = gnssMeasurement.getCarrierFrequencyHz();
        double prr_th = 0.1;

        /*if (options.isMonoFrequencyEnabled())
        {
            if (gnssMeasurement.getCarrierFrequencyHz() > 1.545e9 && gnssMeasurement.getCarrierFrequencyHz() < 1.605e9)
            {
                this.gnssMeasurementL1 = gnssMeasurement;
                this.phaseL1 = gnssMeasurementL1.getAccumulatedDeltaRangeMeters();
                this.cn0L1 = gnssMeasurementL1.getCn0DbHz();
                this.pseudorangeL1 = computePseudorangeL1(gnssMeasurement, options);

                if (pseudorangeL1 > 0)
                {
                    transmissionTime = pseudorangeL1 / Constants.SPEED_OF_LIGHT;
                    L1Enabled = true;
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if(gnssMeasurement.getCarrierFrequencyHz() > 1.146e9 && gnssMeasurement.getCarrierFrequencyHz() < 1.208e9)
            {
                varL5 = 1;
                this.gnssMeasurementL5 = gnssMeasurement;
                this.cn0L5 = gnssMeasurementL5.getCn0DbHz();
                return false;
            }
            else
            {
                return false;
            }
        }
        else
        {
            if (gnssMeasurement.getCarrierFrequencyHz() > 1.545e9 && gnssMeasurement.getCarrierFrequencyHz() < 1.605e9)
            {
                this.gnssMeasurementL1 = gnssMeasurement;
                this.phaseL1 = gnssMeasurementL1.getAccumulatedDeltaRangeMeters();
                this.cn0L1 = gnssMeasurementL1.getCn0DbHz();
                //this.pseudoRateL1 = gnssMeasurementL1.getPseudorangeRateMetersPerSecond();
                this.pseudorangeL1 = computePseudorangeL1(gnssMeasurement, options);

                if (pseudorangeL1 > 0)
                {
                    transmissionTime = pseudorangeL1 / Constants.SPEED_OF_LIGHT;
                    L1Enabled = true;
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (gnssMeasurement.getCarrierFrequencyHz() > 1.146e9 && gnssMeasurement.getCarrierFrequencyHz() < 1.208e9)
            {
                this.gnssMeasurementL5 = gnssMeasurement;
                this.phaseL5 = gnssMeasurementL5.getAccumulatedDeltaRangeMeters();
                this.cn0L5 = gnssMeasurementL5.getCn0DbHz();
                //this.pseudoRateL5 = gnssMeasurementL5.getPseudorangeRateMetersPerSecond();
                this.pseudorangeL5 = computePseudorangeL5(gnssMeasurement, options);

                if (pseudorangeL5 > 0)
                {
                    L5Enabled = true;
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }

        }*/
        //test single frequency
        if (options.isMonoFrequencyEnabled())
        {
            /*Check if the received signals belong to L1 GNSS band (Center Frequency: 1575.42 MHz +/- 30 MHz)*/
            if (gnssMeasurement.getCarrierFrequencyHz() > 1.545e9 && gnssMeasurement.getCarrierFrequencyHz() < 1.605e9)
            {
                //local copy of gnssMeasurement for L1 processing
                // TODO: 13/10/2022 why using gnssMeasurementL1 ?? instead of gnssMeasurement.getXXX
                this.gnssMeasurementL1 = gnssMeasurement;
                this.phaseL1 = gnssMeasurementL1.getAccumulatedDeltaRangeMeters();
                this.cn0L1 = gnssMeasurementL1.getCn0DbHz();
                // empirical test to validate PRR
                if (gnssMeasurementL1.getPseudorangeRateUncertaintyMetersPerSecond() <= prr_th) /* Measured in [m/s]*/
                {
                    this.pseudoRateL1 = gnssMeasurementL1.getPseudorangeRateMetersPerSecond();
                }
                else
                {
                    this.pseudoRateL1 = 0.0;
                }

                this.pseudorangeL1 = computePseudorangeL1(gnssMeasurement, options);

                //pseudorangeL1 > 0 means valid (according computePseudorangeL1)
                if (pseudorangeL1 > 0)
                {
                    // TODO: 13/10/2022 test with ttx -comptuted inside computePseudorangeL1- instead of transmissionTime 
                    transmissionTime = pseudorangeL1 / Constants.SPEED_OF_LIGHT;
                    L1Enabled = true;
                    return true;
                }
                // TODO: 13/10/2022 WARNING : flag bad measurment (same varL5) ? 
                else if (cn0L1 > 20)
                {
                    varL1 = 1;
                    return false;
                }
                //pseudorangeL1 = 0 means invalid (according computePseudorangeL1)
                else
                {
                    return false;
                }
            }
            // TODO: 13/10/2022  check id this is really "dead code", because "L1 only" / "L1/L5" and NO "L5 only"...
            /*Check if the received signals belong to L5 GNSS band (Center Frequency: 1176.46 MHz +/- 30 MHz)*/
            else if(gnssMeasurement.getCarrierFrequencyHz() > 1.146e9 && gnssMeasurement.getCarrierFrequencyHz() < 1.208e9)
            {
                this.gnssMeasurementL5 = gnssMeasurement;
                this.cn0L5 = gnssMeasurementL5.getCn0DbHz();
                varL5_trk = 1;
                return false;
            }
            else
            {
                return false;
            }
        }
        //else : dual frequency case
        else
        {
            /*Check if the received signals belong to L1 GNSS band (Center Frequency: 1575.42 MHz +/- 30 MHz)*/
            // TODO: 13/10/2022 add check : The value is only available if hasCarrierFrequencyHz() is true.
            //Frequency interval accounts for the GNSS carrier frequency differences
            if (gnssMeasurement.getCarrierFrequencyHz() > 1.545e9 && gnssMeasurement.getCarrierFrequencyHz() < 1.605e9)
            {
                this.gnssMeasurementL1 = gnssMeasurement;
                this.phaseL1 = gnssMeasurementL1.getAccumulatedDeltaRangeMeters();
                this.cn0L1 = gnssMeasurementL1.getCn0DbHz();
                if (gnssMeasurementL1.getPseudorangeRateUncertaintyMetersPerSecond() <= prr_th) /* Measured in [m/s]*/
                {
                    this.pseudoRateL1 = gnssMeasurementL1.getPseudorangeRateMetersPerSecond();
                }
                else
                {
                    this.pseudoRateL1 = 0.0;
                }

                this.pseudorangeL1 = computePseudorangeL1(gnssMeasurement, options);

                if (pseudorangeL1 > 0)
                {
                    // TODO: 13/10/2022 test with ttx -comptuted inside computePseudorangeL1- instead of transmissionTime
                    transmissionTime = pseudorangeL1 / Constants.SPEED_OF_LIGHT;
                    L1Enabled = true;
                    return true;
                }
                else if (cn0L1 > 20)
                {
                    varL1 = 1;
                    return false;
                }
                else
                {
                    return false;
                }
            }
            /*Check if the received signals belong to L5 GNSS band (Center Frequency: 1176.46 MHz +/- 30 MHz)*/
            else if (gnssMeasurement.getCarrierFrequencyHz() > 1.146e9 && gnssMeasurement.getCarrierFrequencyHz() < 1.208e9)
            {
                this.gnssMeasurementL5 = gnssMeasurement;
                this.phaseL5 = gnssMeasurementL5.getAccumulatedDeltaRangeMeters();
                this.cn0L5 = gnssMeasurementL5.getCn0DbHz();
                if (gnssMeasurementL5.getPseudorangeRateUncertaintyMetersPerSecond() <= prr_th) /* Measured in [m/s]*/
                {
                    this.pseudoRateL5 = gnssMeasurementL5.getPseudorangeRateMetersPerSecond();
                }
                else
                {
                    this.pseudoRateL5 = 0.0;
                }

                this.pseudorangeL5 = computePseudorangeL5(gnssMeasurement, options);

                if (pseudorangeL5 > 0)
                {
                    transmissionTime = pseudorangeL5 / Constants.SPEED_OF_LIGHT;
                    L5Enabled = true;
                    return true;
                }
                else if (cn0L5 > 20)
                {
                    varL5 = 1;
                    return false;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Merge current observation object with another observation of another frequency.
     * @param gnssObservation Observation on other frequency and processing options
     */
    public void addOtherFrequency(GNSSObservation gnssObservation, Options options)
    {
            if(gnssObservation.isL1Enabled())
            {
                this.gnssMeasurementL1 = gnssObservation.gnssMeasurementL1;
                this.pseudorangeL1 = gnssObservation.getPseudorangeL1();
                this.phaseL1 = gnssObservation.getPhaseL1();
                this.smoothPseudoL1 = gnssObservation.getSmoothPseudoL1();
                this.nbObsL1 = gnssObservation.getNbObsL1();
                this.L1Enabled = true;
                this.cn0L1 = gnssObservation.getCn0L1();
                this.pseudoRateL1 = gnssObservation.getPseudoRateL1();
                this.cycleSlipL1 = cycleSlipL1;
            }
            if(gnssObservation.isL5Enabled())
            {
                this.gnssMeasurementL5 = gnssObservation.gnssMeasurementL5;
                this.pseudorangeL5 = gnssObservation.getPseudorangeL5();
                this.phaseL5 = gnssObservation.getPhaseL5();
                this.smoothPseudoL5 = gnssObservation.getSmoothPseudoL5();
                this.nbObsL5 = gnssObservation.getNbObsL5();
                this.L5Enabled = true;
                this.cn0L5 = gnssObservation.getCn0L5();
                this.pseudoRateL5 = gnssObservation.getPseudoRateL5();
                this.cycleSlipL5 = cycleSlipL5;
            }
            /* Compute L3 (iono-free) if L1 AND L5 measurements are available*/
            if(isL1Enabled() && isL5Enabled())
            {
                L3Enabled = true;
                double f1_2 = Math.pow(Constants.FL1, 2);
                double f2_2 = Math.pow(Constants.FL5, 2);
                double alpha = f1_2 / (f1_2 - f2_2);
                this.phaseL3 = alpha * phaseL1 - (alpha-1) * phaseL5;
                this.pseudorangeL3 = alpha * pseudorangeL1 - (alpha-1) * pseudorangeL5;
            }
    }

    /**
     * Functions facilitating L1 only (single frequency) solution
     * @param gnssObservation Observation object for adding L5 to tracked map only
     */

    public void addFreqL5(GNSSObservation gnssObservation)
    {
        this.gnssMeasurementL5 = gnssObservation.gnssMeasurementL5;
        this.cn0L5 = gnssObservation.getCn0L5();
    }

    public void addFreqL1(GNSSObservation gnssObservation)
    {
        this.gnssMeasurementL1 = gnssObservation.gnssMeasurementL1;
        this.cn0L1 = gnssObservation.getCn0L1();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Check if the measurement can be smoothed and then call the phase smoothing algorithm.
     * @param _prevObs Measurement from the previous epoch.
     */
    public void setSmoothMeasurements(GNSSObservation _prevObs)
    {
        if(_prevObs == null)
        {
            return;
        }

        this.prevObs = _prevObs;

        if(isL1Enabled())
        {
            //double deltaPhase = Math.abs(phaseL1 - prevObs.getPhaseL1());
            //if(getCycleSlipL1() == 0 && prevObs.isL1Enabled())

            if(prevObs.isL1Enabled())
            {
                //----------------------------------------------------------------------------------
                nbObsL1 = prevObs.getNbObsL1() + 1;
                smoothPseudoL1 = computeSmoothedPseudorange(
                        pseudorangeL1, prevObs.getSmoothPseudoL1(),
                        phaseL1, prevObs.getPhaseL1(),
                        nbObsL1);

                //pseudoRateL1 = pseudorangeL1 - prevObs.getPseudorangeL1();
                //pseudoRateL1 = phaseL1 - prevObs.getPhaseL1();

                smoothL1Enabled = true;
                //----------------------------------------------------------------------------------
                /*double deltaPRL1 = pseudorangeL1 - prevObs.getPseudorangeL1();
                double deltaPHL1 = phaseL1 - prevObs.getPhaseL1();
                double deltaCMCL1 = deltaPRL1 - deltaPHL1;
                //Log.d("L1 Multipath:",String.valueOf(deltaCMCL1));
                multL1Stat = Math.abs(deltaCMCL1) >= 2.0;*/
                //----------------------------------------------------------------------------------
            }
            else
            {
                nbObsL1 = 1;
                smoothPseudoL1 = pseudorangeL1;
                //multL1Stat = false;
            }
        }

        if(isL5Enabled())
        {
            //double deltaPhase = Math.abs(phaseL5 - prevObs.getPhaseL5());
            //if(getCycleSlipL5() == 0 && prevObs.isL5Enabled())

            if(prevObs.isL5Enabled())
            {
                //----------------------------------------------------------------------------------
                nbObsL5 = prevObs.getNbObsL5() + 1;
                smoothPseudoL5 = computeSmoothedPseudorange(
                        pseudorangeL5, prevObs.getSmoothPseudoL5(),
                        phaseL5, prevObs.getPhaseL5(),
                        nbObsL5);

                //pseudoRateL5 = pseudorangeL5 - prevObs.getPseudorangeL5();
                //pseudoRateL5 = phaseL5 - prevObs.getPhaseL5();

                smoothL5Enabled = true;
                //----------------------------------------------------------------------------------
                /*double deltaPRL5 = pseudorangeL5 - prevObs.getPseudorangeL5();
                double deltaPHL5 = phaseL5 - prevObs.getPhaseL5();
                double deltaCMCL5 = deltaPRL5 - deltaPHL5;
                //Log.d("L5 Multipath:",String.valueOf(deltaCMCL5));
                multL5Stat = Math.abs(deltaCMCL5) >= 1.0;*/
                //----------------------------------------------------------------------------------
            }
            else
            {
                nbObsL5 = 1;
                smoothPseudoL5 = pseudorangeL5;
                //multL5Stat = false;
            }
        }

        if(isL3Enabled())
        {
            double f1_2 = Math.pow(Constants.FL1, 2);
            double f2_2 = Math.pow(Constants.FL5, 2);

            /* Iono-free smoother*/

            double deltaPhase = Math.abs(phaseL1 - prevObs.getPhaseL1());

            if(getCycleSlipL1() == 0 && getCycleSlipL5() == 0 && prevObs.isL3Enabled()
                    && (deltaPhase < 1e3 || deltaPhase > 10))
            {
                //----------------------------------------------------------------------------------
                nbObsL3 = prevObs.getNbObsL3() + 1;
                smoothPseudoL3 = computeSmoothedPseudorange(
                        pseudorangeL3, prevObs.getSmoothPseudoL3(),
                        phaseL3, prevObs.getPhaseL3(),
                        nbObsL3);

                //pseudoRateL3 = pseudorangeL3 - prevObs.getPseudorangeL3();
                pseudoRateL3 = phaseL3 - prevObs.getPhaseL3();

                smoothL3Enabled = true;
                //----------------------------------------------------------------------------------
            }
            else
            {
                nbObsL3 = 1;
                smoothPseudoL3 = pseudorangeL3;
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Compute L1 pseudorange given a measurement.
     * Enhanced version of getPseudoRange from SatellitePseudorange class.
     * Input: GNSS Measurements and processing options
     * output: L1 Pseudorange
     */
    private double computePseudorangeL1(GnssMeasurement gnssMeasurement, Options options)
    {
        double c = 299792458; /* speed of light in m/s */
        double numberNanoSecondsWeek = 604800e9; /* nanoseconds in one week*/
        double numberNanoSecondsDay = 86400e9; /* nanoseconds in one day*/
        double numberNanoSecondsSecond = 1e9; /*nanoseconds in one second*/
        //double numberNanoSeconds100Milli = 1e8;  /* Nanoseconds in 100 milliseconds*/
        //double numberNanoSeconds2S = 30e9;  /* Nanoseconds in 100 milliseconds*/
        double pseudoRange;
        //double rxTime = (mGnssClock.getTimeNanos() - mGnssClock.getFullBiasNanos())%numberNanoSecondsWeek;
        double timeOffsetNanos = gnssMeasurement.getTimeOffsetNanos();
        //double phaseCycles = 0;
        /*
        if (gnssMeasurement.hasCarrierCycles())
        {
            phaseCycles = gnssMeasurement.getCarrierCycles();
        }

        double rangeRate = gnssMeasurement.getCarrierCycles();
        Log.d("carrierCycles: ", String.valueOf(rangeRate));*/

        //long propagationTimeRef = 70000000;

        // If the received time uncertainty is superior to 1000 ns, something is wrong in the measurement.
        /*if(gnssMeasurement.getReceivedSvTimeUncertaintyNanos() > 1e3)
        {
            return pseudoRange;
        }/*

        /*Analysis 1: Removal of below conditional statements / filters
        1.  && (gnssMeasurement.getState() & STATE_CODE_LOCK) != 0 &&
                    (gnssMeasurement.getState() & STATE_TOW_KNOWN) != 0
        */
        switch (constellation) {
            case GnssStatus.CONSTELLATION_GPS:
                //see p20 WhitePaper GSA
                if ((gnssMeasurement.getState() & STATE_TOW_DECODED) != 0)
                {
                    trxGnss = getTrxGnss() + timeOffsetNanos;
                    trx = trxGnss % numberNanoSecondsWeek;
		    // TODO: 29/11/2022 :why ttx is computed with + timeOffsetNanos ? This term is absent from White Paper 2.4.2
                    ttx = gnssMeasurement.getReceivedSvTimeNanos() + timeOffsetNanos;

                    // "integrity test" Aravind' style
                    if(gnssMeasurement.getReceivedSvTimeUncertaintyNanos() > 1e3)
                    {
                        pseudoRange = 0;
                        return pseudoRange;
                    }

                    pseudoRange = ((trx - ttx) * c)/1e9;

                    if(satellitePosition == null)
                    {
                        satellitePosition = new SatellitePositionGNSS();
                    }
                    /*Apply Ionospheric Correction, If enabled*/
                    if(options.isIonoCorrEnabled())
                    {
                        pseudoRange = pseudoRange - satellitePosition.getIonoCorr() * c;
                    }
                    else
                    {
                        pseudoRange = pseudoRange - (c * 0);
                    }
                    /*Discard anomalous range measurements*/
                    if((pseudoRange < 1.8e7) || (pseudoRange > 3e7))
                    {
                        pseudoRange = 0;
                        return pseudoRange;
                    }

                    return pseudoRange;

                }
                else
                {
                    trx = 0;
                    ttx = 0;
                }
                break;
            case GnssStatus.CONSTELLATION_GALILEO:
                if ((gnssMeasurement.getState() & STATE_TOW_DECODED) != 0)
                {
                    trxGnss = timeOffsetNanos + getTrxGnss();
                    trx = trxGnss % numberNanoSecondsWeek;
                    ttx = gnssMeasurement.getReceivedSvTimeNanos() + timeOffsetNanos;

                    if(options.isMonoFrequencyEnabled())
                    {
                        if(gnssMeasurement.getReceivedSvTimeUncertaintyNanos() > 1e3)
                        {
                            pseudoRange = 0;
                            return pseudoRange;
                        }

                        pseudoRange = ((trx - ttx) * c)/1e9;

                        if(satellitePosition == null)
                        {
                            satellitePosition = new SatellitePositionGNSS();
                        }
                        /*Apply Ionospheric Correction, If enabled*/
                        if(options.isIonoCorrEnabled())
                        {
                            pseudoRange = pseudoRange - satellitePosition.getIonoCorr() * c;
                        }
                        else
                        {
                            pseudoRange = pseudoRange - (c * 0);
                        }
                        /*Discard anomalous range measurements*/
                        if((pseudoRange < 1.8e7) || (pseudoRange > 3e7))
                        {
                            pseudoRange = 0;
                            return pseudoRange;
                        }

                        return pseudoRange;
                    }
                }
                else
                {
                    trx = 0;
                    ttx = 0;
                }

                /*else if ((gnssMeasurement.getState() & STATE_TOW_DECODED) != 0
                        && (gnssMeasurement.getState() & STATE_TOW_KNOWN) != 0
                        && (gnssMeasurement.getState() & STATE_GAL_E1C_2ND_CODE_LOCK) != 0)
                {
                    trx = setTrx(gnssMeasurement);
                    ttx = gnssMeasurement.getReceivedSvTimeNanos() - timeOffsetNanos;

                    pseudoRange = ((getTrxGnss() - ttx)%numberNanoSeconds100Milli) / numberNanoSecondsSecond * c;

                    return pseudoRange;
                }*/
                break;
            case GnssStatus.CONSTELLATION_BEIDOU:
                if ((gnssMeasurement.getState() & STATE_TOW_DECODED) != 0)
                {
                    trxGnss = getTrxGnss() + timeOffsetNanos;
                    trx = (trxGnss % numberNanoSecondsWeek) - (Constants.BDS_LEAP_SECONDS * numberNanoSecondsSecond);
                    ttx = gnssMeasurement.getReceivedSvTimeNanos() + timeOffsetNanos;

                    if(gnssMeasurement.getReceivedSvTimeUncertaintyNanos() > 1e3)
                    {
                        pseudoRange = 0;
                        return pseudoRange;
                    }

                    pseudoRange = ((trx - ttx) * c)/1e9;

                    if(satellitePosition == null)
                    {
                        satellitePosition = new SatellitePositionGNSS();
                    }
                    /*Apply Ionospheric Correction, If enabled*/
                    if(options.isIonoCorrEnabled())
                    {
                        pseudoRange = pseudoRange - satellitePosition.getIonoCorr() * c;
                    }
                    else
                    {
                        pseudoRange = pseudoRange - (c * 0);
                    }
                    /*Discard anomalous range measurements*/
                    if((pseudoRange < 1.8e7) || (pseudoRange > 3e7))
                    {
                        pseudoRange = 0;
                        return pseudoRange;
                    }

                    return pseudoRange;
                }
                else
                {
                    trx = 0;
                    ttx = 0;
                }
                break;
            case GnssStatus.CONSTELLATION_GLONASS:
                //trx = setTrx(gnssMeasurement);
                if ((gnssMeasurement.getState() & STATE_GLO_TOD_DECODED) != 0)
                {
                    trxGnss = getTrxGnss() + timeOffsetNanos;
                    trx = (trxGnss % numberNanoSecondsDay) + (3*60*60 - Constants.LEAP_SECONDS) * numberNanoSecondsSecond;
                    //trx = trxGnss;
                    ttx = gnssMeasurement.getReceivedSvTimeNanos() + timeOffsetNanos;
                }
                else
                {
                    trx = 0;
                    ttx = 0;
                }
                break;
            /*values by default*/
            default:
                ttx = 0;
                trx = 0;
                break;
        }
        if(gnssMeasurement.getReceivedSvTimeUncertaintyNanos() > 1e3)
        {
            pseudoRange = 0;
        }
        else if (trx != 0 && ttx != 0)
        {
            pseudoRange = ((trx - ttx) * c)/1e9;
        }
        else
        {
            pseudoRange = 0;
        }

        /*Discard anomalous range measurements*/
        if((pseudoRange < 1.8e7) || (pseudoRange > 3e7))
        {
            pseudoRange = 0;
        }

        return pseudoRange;
    }

    /**
     * Compute L5 pseudorange given a measurement.
     * Enhanced version of getPseudoRange from SatellitePseudorange class.
     * Input: GNSS Measurements and processing options
     * Output: L5 Pseudorange
     */

    private double computePseudorangeL5(GnssMeasurement gnssMeasurement, Options options)
    {
        double c = 299792458; /*speed of light in m/s */
        double numberNanoSecondsWeek = 604800e9; /* nanoseconds in one week*/
        double numberNanoSecondsDay = 86400e9; /* nanoseconds in one day*/
        double numberNanoSecondsSecond = 1e9; /*nanoseconds in one second*/
        double pseudoRange;
        double timeOffsetNanos = gnssMeasurement.getTimeOffsetNanos();

        switch (constellation) {
            case GnssStatus.CONSTELLATION_GPS:
                //see p20 WhitePaper GSA
                if ((gnssMeasurement.getState() & STATE_TOW_DECODED) != 0)
                {
                    trxGnss = getTrxGnss() + timeOffsetNanos;
                    trx = trxGnss % numberNanoSecondsWeek;
                    ttx = gnssMeasurement.getReceivedSvTimeNanos() + timeOffsetNanos;
                }
                else
                {
                    trx = 0;
                    ttx = 0;
                }
                break;
            case GnssStatus.CONSTELLATION_GALILEO:
                if ((gnssMeasurement.getState() & STATE_TOW_DECODED) != 0)
                {
                    trxGnss = timeOffsetNanos + getTrxGnss();
                    trx = trxGnss % numberNanoSecondsWeek;
                    ttx = gnssMeasurement.getReceivedSvTimeNanos() + timeOffsetNanos;
                }
                else
                {
                    trx = 0;
                    ttx = 0;
                }
                break;
            case GnssStatus.CONSTELLATION_BEIDOU:
                if ((gnssMeasurement.getState() & STATE_TOW_DECODED) != 0)
                {
                    trxGnss = getTrxGnss() + timeOffsetNanos;
                    trx = (trxGnss % numberNanoSecondsWeek) - (Constants.BDS_LEAP_SECONDS * numberNanoSecondsSecond);
                    ttx = gnssMeasurement.getReceivedSvTimeNanos() + timeOffsetNanos;
                }
                else
                {
                    trx = 0;
                    ttx = 0;
                }
                break;
            case GnssStatus.CONSTELLATION_GLONASS:
                if ((gnssMeasurement.getState() & STATE_GLO_TOD_DECODED) != 0)
                {
                    trxGnss = getTrxGnss() + timeOffsetNanos;
                    trx = (trxGnss % numberNanoSecondsDay) + (3*60*60 - Constants.LEAP_SECONDS) * numberNanoSecondsSecond;
                    ttx = gnssMeasurement.getReceivedSvTimeNanos() + timeOffsetNanos;
                }
                else
                {
                    trx = 0;
                    ttx = 0;
                }
                break;
            /*values by default*/
            default:
                ttx = 0;
                trx = 0;
                break;
        }
        if(gnssMeasurement.getReceivedSvTimeUncertaintyNanos() > 1e3)
        {
            pseudoRange = 0;
        }
        else if (trx != 0 && ttx != 0)
        {
            pseudoRange = ((trx - ttx) * c)/1e9;
        }
        else
        {
            pseudoRange = 0;
        }

        /*Discard anomalous range measurements*/
        if((pseudoRange < 1.8e7) || (pseudoRange > 3e7))
        {
            pseudoRange = 0;
        }

        return pseudoRange;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Pseudorange phase smoothing algorithm extracted from ESA GNSS Fundamentals, p.79.
     * @param pseudo Pseudorange of current epoch
     * @param prevSmoothPseudo Pseudorange from previous epoch
     * @param phase Phase of current epoch
     * @param prevPhase Phase of previous epoch
     * @param nbObs Number of previous observations
     * @return Smoothed pseudorange for current epoch
     */
    private double computeSmoothedPseudorange(double pseudo, double prevSmoothPseudo,
                                              double phase, double prevPhase, double nbObs)
    {
        double smoothPseudo;
        double N = 30.0;

        if(nbObs < N)
        {
            smoothPseudo =  ((1/nbObs) * pseudo) + ((nbObs-1)/nbObs * (prevSmoothPseudo + (phase - prevPhase)));
        }
        else
        {
            smoothPseudo =  ((1/N) * pseudo) + ((N-1)/N * (prevSmoothPseudo + (phase - prevPhase)));
        }
        return smoothPseudo;
    }

    //----------------------------------------------------------------------------------------------

    /*
     * Compute the reception time from GNSS measurements.
     * @param gnssMeasurement GNSS Measurement object received in event.
     * @return Reception time of measurements
     */
    // Analysis 1: GLONASS receiver time,
    // Original statement:
    // trx = trxGnss - Math.floor((-fullBiasNanos)/numberNanoSecondsDay)*numberNanoSecondsDay + 3*numberNanoSecondsHour - Constants.LEAP_SECONDS*numberNanoSecondsSecond;
    // Modified statement:
    // trx = trxGnss + 3*numberNanoSecondsHour - Constants.LEAP_SECONDS*numberNanoSecondsSecond;
    // BEIDOU leap seconds removal (- Constants.BDS_LEAP_SECONDS * 1e9)
    // GLONASS modification (3*numberNanoSecondsHour)
    /*public double setTrx(GnssMeasurement gnssMeasurement){
        double trx;             //received time depending on each constellation
        long fullBiasNanos;     //difference between hardware clock and the true gps time

        double numberNanoSecondsWeek = 604800e9;
        double numberNanoSecondsDay = 86400e9;
        //double numberNanoSecondsHour = 3600e9;
        double numberNanoSecondsSecond = 1e9;
        double numberNanoSeconds100Milli = 1e8;  // Nanoseconds in 100 milliseconds

        //fullBiasNanos = mGnssClock.getFullBiasNanos();
        fullBiasNanos = fullBiasNanosFix;

        trxGnss = getTrxGnss();

        switch (constellation) {
            case GnssStatus.CONSTELLATION_GLONASS:
                trx = (trxGnss % numberNanoSecondsDay) + (3*60*60 - Constants.LEAP_SECONDS)*numberNanoSecondsSecond;
                break;

            case GnssStatus.CONSTELLATION_GPS:
                trx = trxGnss % numberNanoSecondsWeek;
                break;

            case GnssStatus.CONSTELLATION_BEIDOU:
                trx = (trxGnss % numberNanoSecondsWeek) + (Constants.BDS_LEAP_SECONDS * numberNanoSecondsSecond);
                break;

            case GnssStatus.CONSTELLATION_GALILEO:
                trx = trxGnss % numberNanoSeconds100Milli;
                break;

            default:
                trx = 0;
                break;
        }

        return trx;
    }*/

    //----------------------------------------------------------------------------------------------

    /**
     * Get received time from the GNSS receiver.
     * @return Reception time in GNSS time
     */
    public double getTrxGnss(){
        long timeNanos;     /*time of receiver's clock at measurement time*/
        long fullBiasNanos; /*difference between hardware clock and the true gps time*/
        double biasNanos;   /*clock's sub-nanoseconds bias*/

        timeNanos = mGnssClock.getTimeNanos();
        fullBiasNanos = fullBiasNanosFix;
        biasNanos = biasNanosFix;
        return timeNanos - (fullBiasNanos + biasNanos);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Getting time of day in seconds.
     *
     * @return Time of Day
     */
    public double getTod(){
        double numberNanoSecondsDay = 8.64e13;
        double numberNanoSecondsSecond = 1e9;
        double tod;
        tod = trxGnss - Math.floor((-mGnssClock.getFullBiasNanos())/numberNanoSecondsDay)*numberNanoSecondsDay - Constants.LEAP_SECONDS*numberNanoSecondsSecond;
        tod /= 1e9;
        return tod;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Getting time of week in seconds.
     *
     * @return Time of Week
     */
    public double getTow(){
        double numberNanoSecondsWeek = 6.048e14;
        double numberNanosecondsSecond = 1e9;
        double tow;
        tow = trxGnss - Math.floor((-fullBiasNanosFix)/numberNanoSecondsWeek)*numberNanoSecondsWeek;
        tow /= 1e9;
        return tow;
    }
// TODO: 14/10/2022 Doublon avec GnssMeasurement.class ??
    //----------------------------------------------------------------------------------------------

    public void setTrx(double _tow)
    {
        trx = _tow;
    }

    //----------------------------------------------------------------------------------------------

    public int getId() {
        return id;
    }

    //----------------------------------------------------------------------------------------------

    public int getConstellation() {
        return constellation;
    }

    //----------------------------------------------------------------------------------------------

    public double getTtx() {
        return ttx;
    }

    //----------------------------------------------------------------------------------------------

    public void setId(int id) {
        this.id = id;
    }

    //----------------------------------------------------------------------------------------------

    public void setConstellation(int constellation) {
        this.constellation = constellation;
    }

    //----------------------------------------------------------------------------------------------

    public double getPseudorangeL1() {
        return pseudorangeL1;
    }

    //----------------------------------------------------------------------------------------------

    public double getPseudorangeL5() {
        return pseudorangeL5;
    }

    //----------------------------------------------------------------------------------------------

    public double getPseudorangeL3() {
        return pseudorangeL3;
    }

    //----------------------------------------------------------------------------------------------

    public double getSmoothPseudoL1() {
        return smoothPseudoL1;
    }

    //----------------------------------------------------------------------------------------------

    public double getSmoothPseudoL5() {
        return smoothPseudoL5;
    }

    //----------------------------------------------------------------------------------------------

    public double getSmoothPseudoL3() {
        return smoothPseudoL3;
    }

    //----------------------------------------------------------------------------------------------

    public double getSmoothPseudoDF() {
        return smoothPseudoDF;
    }

    //----------------------------------------------------------------------------------------------

    public int getNbObsL1() {
        return nbObsL1;
    }

    //----------------------------------------------------------------------------------------------

    public int getNbObsL5() {
        return nbObsL5;
    }

    //----------------------------------------------------------------------------------------------

    public int getNbObsL3() {
        return nbObsL3;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isL3Enabled() {
        return L3Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isL1Enabled() {
        return L1Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isL5Enabled() {
        return L5Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isSmoothL1Enabled() {
        return smoothL1Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isSmoothL5Enabled() {
        return smoothL5Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public boolean isSmoothL3Enabled() {
        return smoothL3Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setL1Enabled(boolean l1Enabled) {
        L1Enabled = l1Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setL5Enabled(boolean l5Enabled) {
        L5Enabled = l5Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setL3Enabled(boolean l3Enabled) {
        L3Enabled = l3Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setSmoothL1Enabled(boolean smoothL1Enabled) {
        this.smoothL1Enabled = smoothL1Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setSmoothL5Enabled(boolean smoothL5Enabled) {
        this.smoothL5Enabled = smoothL5Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setSmoothL3Enabled(boolean smoothL3Enabled) {
        this.smoothL3Enabled = smoothL3Enabled;
    }

    //----------------------------------------------------------------------------------------------

    public void setSmoothPseudoL1(double smoothPseudoL1) {
        this.smoothPseudoL1 = smoothPseudoL1;
    }

    //----------------------------------------------------------------------------------------------

    public void setSmoothPseudoL5(double smoothPseudoL5) {
        this.smoothPseudoL5 = smoothPseudoL5;
    }

    //----------------------------------------------------------------------------------------------

    public void setSmoothPseudoL3(double smoothPseudoL3) {
        this.smoothPseudoL3 = smoothPseudoL3;
    }

    //----------------------------------------------------------------------------------------------

    public SatellitePositionGNSS getSatellitePosition() {
        return satellitePosition;
    }

    //----------------------------------------------------------------------------------------------

    public void setSatellitePosition(SatellitePositionGNSS satellitePosition) {
        this.satellitePosition = satellitePosition;
    }

    //----------------------------------------------------------------------------------------------

    public void setPseudorangeL1(double pseudorangeL1) {
        this.pseudorangeL1 = pseudorangeL1;
    }

    //----------------------------------------------------------------------------------------------

    public void setPseudorangeL5(double pseudorangeL5) {
        this.pseudorangeL5 = pseudorangeL5;
    }

    //----------------------------------------------------------------------------------------------

    public void setPseudorangeL3(double pseudorangeL3) {
        this.pseudorangeL3 = pseudorangeL3;
    }

    //----------------------------------------------------------------------------------------------

    public double getPhaseL1() {
        return phaseL1;
    }

    //----------------------------------------------------------------------------------------------

    public double getPhaseL5() {
        return phaseL5;
    }

    //----------------------------------------------------------------------------------------------

    public double getPhaseL3() {
        return phaseL3;
    }

    //----------------------------------------------------------------------------------------------

    public double getPhaseDF() {
        return phaseDF;
    }

    //----------------------------------------------------------------------------------------------

    public void setPhaseL1(double phaseL1) {
        this.phaseL1 = phaseL1;
    }

    //----------------------------------------------------------------------------------------------

    public void setPhaseL5(double phaseL5) {
        this.phaseL5 = phaseL5;
    }

    //----------------------------------------------------------------------------------------------

    public void setPhaseL3(double phaseL3) {
        this.phaseL3 = phaseL3;
    }

    //----------------------------------------------------------------------------------------------

    public double getPseudoRateL1() {
        return pseudoRateL1;
    }

    //----------------------------------------------------------------------------------------------

    public double getPseudoRateL5() {
        return pseudoRateL5;
    }

    //----------------------------------------------------------------------------------------------

    public double getPseudoRateL3() {
        return pseudoRateL3;
    }

    //----------------------------------------------------------------------------------------------

    public void setPseudoRateL1(double pseudoRateL1) {
        this.pseudoRateL1 = pseudoRateL1;
    }

    //----------------------------------------------------------------------------------------------

    public void setPseudoRateL5(double pseudoRateL5) {
        this.pseudoRateL5 = pseudoRateL5;
    }

    //----------------------------------------------------------------------------------------------

    public void setPseudoRateL3(double pseudoRateL3) {
        this.pseudoRateL3 = pseudoRateL3;
    }

    //----------------------------------------------------------------------------------------------

    public double getTransmissionTime() {
        return transmissionTime;
    }

    //----------------------------------------------------------------------------------------------

    public GnssMeasurement getGnssMeasurementL1() {
        return gnssMeasurementL1;
    }

    //----------------------------------------------------------------------------------------------

    public GnssMeasurement getGnssMeasurementL5() {
        return gnssMeasurementL5;
    }

    //----------------------------------------------------------------------------------------------

    public double getCn0L1() {
        return cn0L1;
    }

    //----------------------------------------------------------------------------------------------
    public double getCarrFreq(){return carrFreq;}
    //----------------------------------------------------------------------------------------------

    public double getCn0L5() {
        return cn0L5;
    }

    //----------------------------------------------------------------------------------------------

    public void setCn0L1(double cn0L1) {
        this.cn0L1 = cn0L1;
    }

    //----------------------------------------------------------------------------------------------

    public void setCn0L5(double cn0L5) {
        this.cn0L5 = cn0L5;
    }

    //----------------------------------------------------------------------------------------------

    public double getTrx() {
        return trx;
    }

    //----------------------------------------------------------------------------------------------

    public int getCycleSlipL1() {
        return cycleSlipL1;
    }

    //----------------------------------------------------------------------------------------------

    public int getCycleSlipL5() {
        return cycleSlipL5;
    }

    //----------------------------------------------------------------------------------------------

    public void setCycleSlipL1(int cycleSlipL1) {
        this.cycleSlipL1 = cycleSlipL1;
    }

    //----------------------------------------------------------------------------------------------

    public void setCycleSlipL5(int cycleSlipL5) {
        this.cycleSlipL5 = cycleSlipL5;
    }

    public int getVarL5(){
        return varL5;
    }

    public int getVarL1(){
        return varL1;
    }

    public int getVarL5_trk(){
        return varL5_trk;
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        String str = "";

        str = "" + constellation + ","
                + id + ","
                + getPseudorangeL1() + ","
                + getPhaseL1() + ","
                + getSmoothPseudoL1() + ","
                + getPseudorangeL5() + ","
                + getPhaseL5() + ","
                + getSmoothPseudoL5() + ","
                + getPseudorangeL3() + ","
                + getPhaseL3() + ","
                + getSmoothPseudoL3() + ","
                + getCn0L1() + ","
                + getCn0L5() + ","
                + cycleSlipL1 + ","
                + cycleSlipL5;

        return str;
    }
}
