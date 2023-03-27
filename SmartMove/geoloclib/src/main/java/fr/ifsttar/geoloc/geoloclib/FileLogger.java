///=================================================================================================
// Class FileLogger
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

import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import org.gogpsproject.Constants;
import org.gogpsproject.ephemeris.GNSSEphemeris;
import org.gogpsproject.ephemeris.PreciseCorrection;
import org.gogpsproject.ephemeris.SatelliteCodeBiases;
import org.gogpsproject.positioning.Time;
import org.gogpsproject.producer.ObservationSet;
import org.gogpsproject.producer.Observations;
import org.gogpsproject.producer.rinex.RinexV3Producer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import fr.ifsttar.geoloc.geoloclib.satellites.GNSSObservation;
import fr.ifsttar.geoloc.geoloclib.satellites.SatellitePositionGNSS;

public class FileLogger
{
    File parentDir;
    File surveyDir;

    String timeStamp;

    private StringBuilder fileContentsPseudo;
    private StringBuilder fileContentsEphem;
    private StringBuilder fileContentsSatPos;
    private StringBuilder fileContentsPos;
    private StringBuilder fileContentsPreciseCorr;
    private StringBuilder fileConfig;

    private String fileNamePseudo;
    private String fileNameEphem;
    private String fileNameSatPos;
    private String fileNamePos;
    private String fileNamePrecCorr;
    private String fileNameConfig;

    private HashMap<String, GNSSObservation> gnssObservations;
    private HashMap<String, GNSSEphemeris> satelliteEphemeris;
    private HashMap<String, Integer> prevSatelliteEphemeris;
    private GnssMeasurementsEvent gnssMeasurementsEvent;
    private Coordinates userPosition;
    private double timeOfWeek;
    private int i=0;

    Options options; // Current processing options

    private RinexV3Producer rinexObs;

    // Constants
    static final private String OBS_HEADER = "# Header Description:\n"
            + "# Tow(s),Constellation,SvId,PseudoRange L1(m),Phase L1 (cycle), Smooth Pseudo L1,PseudoRange L5(m),"
            + "Phase L5 (cycle), Smooth Pseudo L5, PseudoRange L3(m), Phase L3 (cycle), Smooth Pseudo L3, C/n0 L1, C/n0 L5, Cycle Slip L1, Cycle Slip L5\n";

    static final private String EPHEM_HEADER ="# Header Description:\n"
            + "# Tow(s),System,PRN,WN,Toc(s),Af2,Af1,Af0,Crs,DeltaN,M0,Cuc,"
            + "E,Cus,SquareA,Toe,Aodo,Cic,Omega0,Cis,I0,Crc, Omega,OmegaDot,Idot\n";

    static final private String SAT_POS_HEADER = "# Header Description:\n"
            + "# Tow(s),X,Y,Z (with corrections [m]), dX,dY,dZ (precise corrections [m])\n";

    static final private String POS_HEADER = "# Header Description:\n"
            + "# Tow(s),Latitude,Longitude,Altitude(m),Nb of satellites used \n";

    static final private String CORR_HEADER = "# Header Description:\n"
            + "# Tow(s),System,PRN,updateInterval(s),IOD,eRadial,eAlong,eCross,eDotRadial,eDotAlong,eDotCross,c0,c1,c2,DCB L1,DCB L5\n";

    static final private String CONFIG_HEADER = "Configuration Used:\n";

    // GNSS Logger
    private File mFile;
    private final Object mFileLock = new Object();
    private BufferedWriter mFileWriter;
    private static final String FILE_PREFIX = "gnss_log";
    private static final String COMMENT_START = "# ";
    private static final String VERSION_TAG = "Version: ";
    private static final int MAX_FILES_STORED = 100;
    private static final int MINIMUM_USABLE_FILE_SIZE_BYTES = 1000;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * @param _parentDir Survey directory
     * @param _options Computation options
     */
    public FileLogger(File _parentDir, Options _options)
    {
        parentDir = _parentDir;
        if (!parentDir.exists()) {
            //we create directory
            parentDir.mkdirs();
        }

        options = _options;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        Date date = Calendar.getInstance().getTime();

        timeStamp = dateFormat.format(date);

        //creating log file names and adding headers to the contents
        fileContentsPseudo = new StringBuilder();
        fileContentsPseudo.append(OBS_HEADER);
        fileContentsEphem = new StringBuilder();
        fileContentsEphem.append(EPHEM_HEADER);
        fileContentsSatPos = new StringBuilder();
        fileContentsSatPos.append(SAT_POS_HEADER);
        fileContentsPos = new StringBuilder();
        fileContentsPos.append(POS_HEADER);
        fileContentsPreciseCorr = new StringBuilder();
        fileContentsPreciseCorr.append(CORR_HEADER);
        fileConfig =new StringBuilder();
        fileConfig.append(CONFIG_HEADER);

        //Add computation settings
        fileContentsPos.append(getProcessingHeader());

        //filenames
        fileNamePseudo = dateFormat.format(date) + "_pseudo.txt";
        fileNameEphem = dateFormat.format(date) + "_ephem.txt";
        fileNameSatPos = dateFormat.format(date) + "_satpos.txt";
        fileNamePos = dateFormat.format(date) + "_pos.txt";
        fileNamePrecCorr = dateFormat.format(date) + "_corr.txt";
        fileNameConfig = dateFormat.format(date) + "_configuration.ini";

        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        surveyDir = new File(Environment.getExternalStorageDirectory() + "/" + parentDir.getName(),
                dateFormat.format(date));

        if (!surveyDir.exists())
        {
            surveyDir.mkdirs();
        }

        prevSatelliteEphemeris = new HashMap<>();

        // RINEX
        Boolean[] sysEnabledRinex = getSystemInRinex();
        Boolean[] constRinex = {true, false, false, true, false};
        rinexObs = new RinexV3Producer(false,false, "XIMI",
                sysEnabledRinex, 0);

        rinexObs.setOutputDir(surveyDir.getAbsolutePath());

        startNewLog();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Refresh with new data to be saved.
     * @param _gnssObservations Observation data
     * @param _gnssMeasurementsEvent Raw measurement data
     * @param _satelliteEphemeris Ephemeris data
     * @param _userPosition User coordinates
     * @param _timeOfWeek Reception time
     */
    public void refreshData(HashMap<String, GNSSObservation> _gnssObservations,
                            GnssMeasurementsEvent _gnssMeasurementsEvent,
                            HashMap<String, GNSSEphemeris> _satelliteEphemeris,
                            Coordinates _userPosition,
                            double _timeOfWeek)
    {
        gnssObservations = _gnssObservations;
        satelliteEphemeris = _satelliteEphemeris;
        userPosition = _userPosition;
        timeOfWeek = _timeOfWeek;
        gnssMeasurementsEvent = _gnssMeasurementsEvent;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Log data.
     * @param bLogObs Logging observations
     * @param bLogPos Logging computed positions
     * @param bLogSatPos Logging satellite positions
     * @param bLogEph Logging ephemeris
     */
    public void logData(Boolean bLogObs, Boolean bLogPos, boolean bLogSatPos, boolean bLogEph)
    {
        i = i + 1;
        if (i == 1)
        {
            logConfig();
        }
        //if directory doesn't exist
        if(bLogObs && !gnssObservations.isEmpty())
        {
            logObservations();

            // GNSS Logger files
            for(GnssMeasurement gnssMeasurement : gnssMeasurementsEvent.getMeasurements())
            {
                try {
                    writeGnssMeasurementToFile(gnssMeasurementsEvent.getClock(), gnssMeasurement);
                }
                catch (IOException e)
                {
                    Log.e("FILE", "ERROR LOGGING RAW MEASUREMENTS IN FILE.");
                }
            }
        }

        if(bLogSatPos && !gnssObservations.isEmpty())
        {
            logSatPositions();
        }

        if(bLogPos)
        {
            logPositions();
        }

        if(bLogEph && !satelliteEphemeris.isEmpty())
        {
            logEphemeris();
            logPreciseCorrections();
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Log observations data
     */
    private void logObservations()
    {
        FileOutputStream outputStream;
        OutputStreamWriter outputStreamWriter;
        File filePseudo;

        //Setting the observations for RINEX
        //Flag 0 mean observation is OK (see GoGPS)
        ObservationSet observationSet;

        // Getting observations reference time using the first key in observations hashmap
        GNSSObservation firstValue = gnssObservations.values().stream().findFirst().get();
        int wn = org.gogpsproject.Constants.ROLLOVER_WEEKS * 1024 + firstValue.getSatellitePosition().getEph().getWn();
        double tow = firstValue.getTow();
        Time time = new Time(wn, tow);
        Observations observations = new Observations(time, 0);

        if(gnssObservations == null || gnssObservations.isEmpty())
        {
            return;
        }

        filePseudo = new File(surveyDir, fileNamePseudo);

        for(HashMap.Entry<String, GNSSObservation> entry : gnssObservations.entrySet())
        {
            fileContentsPseudo.append(entry.getValue().getTow())
                    .append(",")
                    .append(entry.getValue().toString())
                    .append("\n");

            observationSet = getObservationSet(entry.getValue());
            if(observationSet != null)
            {
                observations.setGps(entry.getValue().getConstellation() * 100 + entry.getValue().getId(),
                        observationSet);
            }

        }

        try {
            if(!filePseudo.exists())
            {
                filePseudo.createNewFile();
            }

            outputStream = new FileOutputStream(filePseudo, true);
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append(fileContentsPseudo);
            outputStreamWriter.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //clearing contents to next loop
        fileContentsPseudo = new StringBuilder();

        // Rinex logging
        rinexObs.addObservations(observations);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Log satellite positions data.
     */
    private void logSatPositions()
    {
        FileOutputStream outputStream;
        OutputStreamWriter outputStreamWriter;
        File fileSatPos;

        if(gnssObservations == null)
        {
            return;
        }

        fileSatPos = new File(surveyDir, fileNameSatPos);

        for(HashMap.Entry<String, GNSSObservation> entry : gnssObservations.entrySet())
        {
            SatellitePositionGNSS satPos = entry.getValue().getSatellitePosition();
            fileContentsSatPos.append(timeOfWeek + ",")
                    .append(satPos.getEph().getGnssSystem() + ","
                            + satPos.getEph().getPrn() + ","
                            + satPos.getSatCoordinates().getX() + ","
                            + satPos.getSatCoordinates().getY() + ","
                            + satPos.getSatCoordinates().getZ() + ","
                            + satPos.getDtSat());

            if(satPos.getEphCorrections() != null)
            {
                fileContentsSatPos.append(",")
                        .append(satPos.getdX_precise().get(0,0) + ","
                                + satPos.getdX_precise().get(1,0)+ ","
                                + satPos.getdX_precise().get(2,0) + ","
                                + satPos.getdC_precise());
            }
            else
            {
                fileContentsSatPos.append(", , , , ");
            }

            fileContentsSatPos.append("\n");
        }

        try {
            if(!fileSatPos.exists())
            {
                fileSatPos.createNewFile();
            }

            outputStream = new FileOutputStream(fileSatPos, true);
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append(fileContentsSatPos);
            outputStreamWriter.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //clearing contents to next loop
        fileContentsSatPos = new StringBuilder();

    }

    //----------------------------------------------------------------------------------------------

    /**
     * Log computed positions data.
     */
    private void logPositions()
    {
        FileOutputStream outputStream;
        OutputStreamWriter outputStreamWriter;
        File filePos;

        if(userPosition == null)
        {
            return;
        }

        filePos = new File(surveyDir,fileNamePos);

        fileContentsPos.append(timeOfWeek)
                .append(", ")
                .append(userPosition.getLatLngAlt().toString())
                .append(", ")
                .append(gnssObservations.size())
                .append("\n");

        try{
            if(!filePos.exists())
            {
                filePos.createNewFile();
            }

            outputStream = new FileOutputStream(filePos, true);
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append(fileContentsPos);
            outputStreamWriter.close();
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        //clearing contents for next loop
        fileContentsPos = new StringBuilder();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Log Configuration Information
     */
    private void logConfig()
    {
        FileOutputStream outputStream;
        OutputStreamWriter outputStreamWriter;
        File filePos;
        String header = "";

        //----------------------------------------------------------------------------------------------
        Vector<Integer> systemsEnabled = options.getSystemsEnabled();
        if(systemsEnabled.contains(GnssStatus.CONSTELLATION_GPS) && systemsEnabled.contains(GnssStatus.CONSTELLATION_GALILEO) && systemsEnabled.contains(GnssStatus.CONSTELLATION_BEIDOU))
        {
            header += "GPS + GALILEO + Beidou\n";
        }
        else if(systemsEnabled.contains(GnssStatus.CONSTELLATION_GPS) && systemsEnabled.contains(GnssStatus.CONSTELLATION_GALILEO) && !systemsEnabled.contains(GnssStatus.CONSTELLATION_BEIDOU))
        {
            header += "GPS + GALILEO\n";
        }
        else if (systemsEnabled.contains(GnssStatus.CONSTELLATION_GPS) && !systemsEnabled.contains(GnssStatus.CONSTELLATION_GALILEO) && systemsEnabled.contains(GnssStatus.CONSTELLATION_BEIDOU))
        {
            header += "GPS + Beidou\n";
        }
        else if (systemsEnabled.contains(GnssStatus.CONSTELLATION_GPS) && !systemsEnabled.contains(GnssStatus.CONSTELLATION_GALILEO) && !systemsEnabled.contains(GnssStatus.CONSTELLATION_BEIDOU))
        {
            header += "GPS Only\n";
        }
        else if (!systemsEnabled.contains(GnssStatus.CONSTELLATION_GPS) && systemsEnabled.contains(GnssStatus.CONSTELLATION_GALILEO) && systemsEnabled.contains(GnssStatus.CONSTELLATION_BEIDOU))
        {
            header += "Galileo + Beidou\n";
        }
        else if (!systemsEnabled.contains(GnssStatus.CONSTELLATION_GPS) && systemsEnabled.contains(GnssStatus.CONSTELLATION_GALILEO) && !systemsEnabled.contains(GnssStatus.CONSTELLATION_BEIDOU))
        {
            header += "Galileo only\n";
        }
        else if (!systemsEnabled.contains(GnssStatus.CONSTELLATION_GPS) && !systemsEnabled.contains(GnssStatus.CONSTELLATION_GALILEO) && systemsEnabled.contains(GnssStatus.CONSTELLATION_BEIDOU))
        {
            header += "Beidou only\n";
        }
        else
        {
            header += "Invalid Solution\n";
        }

        //----------------------------------------------------------------------------------------------
        if(options.isMonoFrequencyEnabled())
        {
            header += "L1 only Solution\n";
        }
        else if (options.isDualFrequencyEnabled())
        {
            header += "L1/L5 Solution\n";
        }
        else
        {
            header += "Iono-free Solution\n";
        }

        //----------------------------------------------------------------------------------------------
        if(options.isIonoCorrEnabled() && options.isTropoEnabled())
        {
            header += "Ionospheric Correction  + Tropospheric Correction\n";
        }
        else if (options.isIonoCorrEnabled() && !options.isTropoEnabled())
        {
            header += "Ionospheric Correction\n";
        }
        else if (!options.isIonoCorrEnabled() && options.isTropoEnabled())
        {
            header += "Tropospheric Correction\n";
        }
        else
        {
            header += "No Atmospheric Corrections\n";
        }

        //----------------------------------------------------------------------------------------------
        if (options.isSppEnabled())
        {
            header += "Single Point Positioning (SPP)\n";
        }

        //----------------------------------------------------------------------------------------------
        if (options.isDynamicMode())
        {
            header += "Dynamic Mode: Extended kalman Filter using TDCP\n";
        }
        else
        {
            header += "Static Mode using Least Squares Estimation Technique\n";
        }

        //----------------------------------------------------------------------------------------------
        header += "Elevation Cutoff: "+ options.getCutoffAngle();
        header += "\n";
        header += "Noise(CN0) Cutoff: "+ options.getCutoffNoise();

        //----------------------------------------------------------------------------------------------
        filePos = new File(surveyDir,fileNameConfig);

        fileConfig.append(header);

        try
        {
            if(!filePos.exists())
            {
                filePos.createNewFile();
            }

            outputStream = new FileOutputStream(filePos, true);
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append(fileConfig);
            outputStreamWriter.close();
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        fileConfig = new StringBuilder();
    }

    /**
     * Log ephemeris data.
     */
    private void logEphemeris()
    {
        FileOutputStream outputStream;
        OutputStreamWriter outputStreamWriter;
        File fileEphem;

        if(satelliteEphemeris == null)
        {
            return;
        }

        fileEphem = new File(surveyDir,fileNameEphem);

        for(Map.Entry<String, GNSSEphemeris> entry : satelliteEphemeris.entrySet())
        {
            GNSSEphemeris eph = entry.getValue();

            // Analysis: remove ""

            if(prevSatelliteEphemeris.containsKey(entry.getKey())
                && prevSatelliteEphemeris.get(entry.getKey()) == eph.getIode())
            {
                continue; // Ephemeris already written in file
            }

            prevSatelliteEphemeris.put(entry.getKey(), eph.getIode());

            fileContentsEphem.append(timeOfWeek + ",");
            fileContentsEphem.append(eph.toString());
            fileContentsEphem.append("\n");
        }

        try {
            if(!fileEphem.exists())
            {
                fileEphem.createNewFile();
            }

            outputStream = new FileOutputStream(fileEphem, true);
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append(fileContentsEphem);
            outputStreamWriter.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //clearing contents to next loop
        fileContentsEphem = new StringBuilder();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Log satellite precise corrections data.
     */
    private void logPreciseCorrections()
    {
        FileOutputStream outputStream;
        OutputStreamWriter outputStreamWriter;
        File filePreciseCorr;

        if(satelliteEphemeris == null)
        {
            return;
        }

        filePreciseCorr = new File(surveyDir, fileNamePrecCorr);

        for(Map.Entry<String, GNSSEphemeris> entry : satelliteEphemeris.entrySet())
        {
            PreciseCorrection preciseCorr = entry.getValue().getEphCorrections();
            SatelliteCodeBiases.CodeBias codeBias = entry.getValue().getCodeBias();

            fileContentsPreciseCorr.append(timeOfWeek);

            String str = "";
            if(preciseCorr != null)
            {
                str += "," + preciseCorr.toString();
            }
            else
            {
                str += "," + entry.getValue().getGnssSystem() + "," + entry.getValue().getPrn();

                // Filling the space for precise corrections in file
                for(int i = 0; i < 14; i++)
                {
                    str += ", ";
                }
            }

            if(codeBias != null)
            {
                int codeL1 = 0;
                int codeL5 = 0;

                // Checking the constellation
                switch (entry.getValue().getGnssSystem())
                {
                    case GnssStatus.CONSTELLATION_GPS:
                        codeL1 = org.gogpsproject.Constants.CODE_L1C;
                        codeL5 = org.gogpsproject.Constants.CODE_L5X;
                        break;
                    case GnssStatus.CONSTELLATION_GALILEO:
                        codeL1 = org.gogpsproject.Constants.CODE_L1X;
                        codeL5 = org.gogpsproject.Constants.CODE_L5X;
                        break;
                    case GnssStatus.CONSTELLATION_BEIDOU:
                        codeL1 = org.gogpsproject.Constants.CODE_L1I;
                        codeL5 = org.gogpsproject.Constants.CODE_L6I;
                        break;
                    case GnssStatus.CONSTELLATION_GLONASS:
                        codeL1 = org.gogpsproject.Constants.CODE_L1C;
                        codeL5 = org.gogpsproject.Constants.CODE_L5X;
                        break;
                    default:
                        Log.e("COMP", "Unknown constellation.");
                        break;
                }

                str += "," + codeBias.getBiases().get(codeL1);

                // Checking if the satellite is sending L5
                if(codeBias.getBiases().get(codeL5) != null)
                {
                    str += "," + codeBias.getBiases().get(codeL5);
                }

            }
            else
            {
                str += ", , ";
            }

            fileContentsPreciseCorr.append(str);
            fileContentsPreciseCorr.append("\n");
        }

        try {
            if(!filePreciseCorr.exists())
            {
                filePreciseCorr.createNewFile();
            }

            outputStream = new FileOutputStream(filePreciseCorr, true);
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append(fileContentsPreciseCorr);
            outputStreamWriter.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //clearing contents to next loop
        fileContentsPreciseCorr = new StringBuilder();
    }

    //----------------------------------------------------------------------------------------------



    //----------------------------------------------------------------------------------------------

    /**
     * Build file header with current options.
     * @return Header
     */
    private String getProcessingHeader()
    {
        String header = "# ";

        if(options.isMonoFrequencyEnabled())
        {
            header += "L1 only, ";
        }
        else if (options.isDualFrequencyEnabled())
        {
            header += "L1/L5, ";
        }
        else
        {
            header += "Iono-free, ";
        }

        Vector<Integer> systemsEnabled = options.getSystemsEnabled();
        if(systemsEnabled.contains(GnssStatus.CONSTELLATION_GPS))
        {
            header += "GPS";
            if(systemsEnabled.contains(GnssStatus.CONSTELLATION_GALILEO))
            {
                header += "+GALILEO, ";
                if(systemsEnabled.contains(GnssStatus.CONSTELLATION_BEIDOU))
                {
                    header += "+BEIDOU";
                }
            }
        }
        /*else if(systemsEnabled.contains(GnssStatus.CONSTELLATION_GALILEO)){
            header += "GALILEO,";
        }

        if(options.isStreamsEnabled())
        {
            header += "Streamed ephemeris";
            if(options.isStreamsCorrectionEnabled())
            {
                header += " with corrections";
            }
        }

        if(options.isSmoothingEnabled())
        {
            header += ",Phase smoothing activated";
        }*/

        header += "\n";
        return header;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Get array of systems for RINEX logging.
     */
    private Boolean[] getSystemInRinex()
    {
        Vector<Integer> sysEnabled = options.getSystemsEnabled();

        Boolean[] sysEnabledRinex = {false, false, false, false, false};

        for(Integer sys : sysEnabled)
        {
            switch (sys)
            {
                case GnssStatus.CONSTELLATION_GPS:
                    sysEnabledRinex[0] = true;
                    break;
                case GnssStatus.CONSTELLATION_QZSS:
                    sysEnabledRinex[1] = true;
                    break;
                case GnssStatus.CONSTELLATION_GLONASS:
                    sysEnabledRinex[2] = true;
                    break;
                case GnssStatus.CONSTELLATION_GALILEO:
                    sysEnabledRinex[3] = true;
                    break;
                case GnssStatus.CONSTELLATION_BEIDOU:
                    sysEnabledRinex[4] = true;
                    break;
                default:
                    break;
            }
        }

        return sysEnabledRinex;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Transform observations into observationSet for RINEX logging.
     * Input: gnssObservation
     * Output: Observation set in RINEX form
     */
    private ObservationSet getObservationSet(GNSSObservation gnssObservation)
    {
        ObservationSet observationSet = new ObservationSet();

        /*if((gnssObservation.getPseudorangeL1() == 0)
                && (gnssObservation.getPseudorangeL5() == 0))
        {
            return null;
        }*/

        double pseudorange = 0.0;
        double phase = 0.0;
        double doppler = 0.0;

        double WL1;
        double WL5;

        switch (gnssObservation.getConstellation())
        {
            case GnssStatus.CONSTELLATION_GPS:
                WL1 = org.gogpsproject.Constants.WL1;
                WL5 = org.gogpsproject.Constants.WL5;
                break;
            case GnssStatus.CONSTELLATION_GALILEO:
                WL1 = org.gogpsproject.Constants.WE1;
                WL5 = org.gogpsproject.Constants.WE5a;
                break;
            case GnssStatus.CONSTELLATION_BEIDOU:
                WL1 = org.gogpsproject.Constants.WB1C;
                WL5 = org.gogpsproject.Constants.WB2a;
                break;
            default:
                return null;
        }

        if(gnssObservation.getPseudorangeL1() > 0)
        {
            pseudorange = gnssObservation.getPseudorangeL1();
            phase = gnssObservation.getPhaseL1() / -WL1;
            doppler = gnssObservation.getGnssMeasurementL1().getPseudorangeRateMetersPerSecond()/-WL1;

            observationSet.setCodeC(0, pseudorange);
            observationSet.setPhaseCycles(0, phase);
            observationSet.setSatID(gnssObservation.getGnssMeasurementL1().getSvid());
            observationSet.setSignalStrength(0, (float) gnssObservation.getGnssMeasurementL1().getCn0DbHz());
            observationSet.setDoppler(0, (float) doppler);
            observationSet.setSatType(Utils.getConstellationLetter(gnssObservation.getGnssMeasurementL1().getConstellationType()));
        }
        else
        {
            observationSet.setCodeC(0, 0.0);
            observationSet.setPhaseCycles(0, 0.0);
            observationSet.setSatID(-1);
            observationSet.setSignalStrength(0, (float) 0.0);
            observationSet.setDoppler(0, (float) 0.0);
            observationSet.setSatType('U');
        }
        if(gnssObservation.getPseudorangeL5() > 0)
        {
            pseudorange = gnssObservation.getPseudorangeL5();
            phase = gnssObservation.getPhaseL5() / -WL5;
            doppler = gnssObservation.getGnssMeasurementL5().getPseudorangeRateMetersPerSecond()/-WL5;

            observationSet.setCodeC(1, pseudorange);
            observationSet.setPhaseCycles(1, phase);
            observationSet.setSatID(gnssObservation.getGnssMeasurementL5().getSvid());
            observationSet.setSignalStrength(1, (float) gnssObservation.getGnssMeasurementL5().getCn0DbHz());
            observationSet.setDoppler(1, (float) doppler);
            observationSet.setSatType(Utils.getConstellationLetter(gnssObservation.getGnssMeasurementL5().getConstellationType()));
        }
        else
        {
            observationSet.setCodeC(0, 0.0);
            observationSet.setPhaseCycles(0, 0.0);
            observationSet.setSatID(-1);
            observationSet.setSignalStrength(0, (float) 0.0);
            observationSet.setDoppler(0, (float) 0.0);
            observationSet.setSatType('U');
        }

        return observationSet;
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    //-- GNSS Logger files, Copyright (C) 2017 The Android Open Source Project

    /**
     * Start a new file logging process.
     */
    public void startNewLog() {
        synchronized (mFileLock) {
            File baseDirectory;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                baseDirectory = surveyDir;
                if(!baseDirectory.exists()) {
                    baseDirectory.mkdirs();
                }
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                Log.e("FILE", "Cannot write to external storage.");
                return;
            } else {
                Log.e("FILE", "Cannot read external storage.");
                return;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyy_MM_dd_HH_mm_ss");
            Date now = new Date();
            String fileName = String.format("%s_%s.txt", FILE_PREFIX, formatter.format(now));
            File currentFile = new File(baseDirectory, fileName);
            String currentFilePath = currentFile.getAbsolutePath();
            BufferedWriter currentFileWriter;
            try {
                currentFileWriter = new BufferedWriter(new FileWriter(currentFile));
            } catch (IOException e) {
                Log.e("FILE", "Could not open file: " + currentFilePath, e);
                return;
            }

            // initialize the contents of the file
            try {
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.write("Header Description:");
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.write(VERSION_TAG);
                String manufacturer = Build.MANUFACTURER;
                String model = Build.MODEL;
                String fileVersion =
                                " Platform: "
                                + Build.VERSION.RELEASE
                                + " "
                                + "Manufacturer: "
                                + manufacturer
                                + " "
                                + "Model: "
                                + model;
                currentFileWriter.write(fileVersion);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.write(
                        "Raw,ElapsedRealtimeMillis,TimeNanos,LeapSecond,TimeUncertaintyNanos,FullBiasNanos,"
                                + "BiasNanos,BiasUncertaintyNanos,DriftNanosPerSecond,DriftUncertaintyNanosPerSecond,"
                                + "HardwareClockDiscontinuityCount,Svid,TimeOffsetNanos,State,ReceivedSvTimeNanos,"
                                + "ReceivedSvTimeUncertaintyNanos,Cn0DbHz,PseudorangeRateMetersPerSecond,"
                                + "PseudorangeRateUncertaintyMetersPerSecond,"
                                + "AccumulatedDeltaRangeState,AccumulatedDeltaRangeMeters,"
                                + "AccumulatedDeltaRangeUncertaintyMeters,CarrierFrequencyHz,CarrierCycles,"
                                + "CarrierPhase,CarrierPhaseUncertainty,MultipathIndicator,SnrInDb,"
                                + "ConstellationType,AgcDb,CarrierFrequencyHz");
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.write(
                        "Fix,Provider,Latitude,Longitude,Altitude,Speed,Accuracy,(UTC)TimeInMs");
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.write("Nav,Svid,Type,Status,MessageId,Sub-messageId,Data(Bytes)");
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();
            } catch (IOException e) {
                Log.e("FILE","Count not initialize file: " + currentFilePath, e);
                return;
            }

            if (mFileWriter != null) {
                try {
                    mFileWriter.close();
                } catch (IOException e) {
                    Log.e("FILE","Unable to close all file streams.", e);
                    return;
                }
            }

            mFile = currentFile;
            mFileWriter = currentFileWriter;

            // To make sure that files do not fill up the external storage:
            // - Remove all empty files
            FileFilter filter = new FileToDeleteFilter(mFile);
            for (File existingFile : baseDirectory.listFiles(filter)) {
                existingFile.delete();
            }
            // - Trim the number of files with data
            File[] existingFiles = baseDirectory.listFiles();
            int filesToDeleteCount = existingFiles.length - MAX_FILES_STORED;
            if (filesToDeleteCount > 0) {
                Arrays.sort(existingFiles);
                for (int i = 0; i < filesToDeleteCount; ++i) {
                    existingFiles[i].delete();
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    private void writeGnssMeasurementToFile(GnssClock clock, GnssMeasurement measurement)
            throws IOException {
        String clockStream =
                String.format(
                        "Raw,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        SystemClock.elapsedRealtime(),
                        clock.getTimeNanos(),
                        clock.hasLeapSecond() ? clock.getLeapSecond() : "",
                        clock.hasTimeUncertaintyNanos() ? clock.getTimeUncertaintyNanos() : "",
                        clock.getFullBiasNanos(),
                        clock.hasBiasNanos() ? clock.getBiasNanos() : "",
                        clock.hasBiasUncertaintyNanos() ? clock.getBiasUncertaintyNanos() : "",
                        clock.hasDriftNanosPerSecond() ? clock.getDriftNanosPerSecond() : "",
                        clock.hasDriftUncertaintyNanosPerSecond()
                                ? clock.getDriftUncertaintyNanosPerSecond()
                                : "",
                        clock.getHardwareClockDiscontinuityCount() + ",");
        mFileWriter.write(clockStream);

        String measurementStream =
                String.format(
                        "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        measurement.getSvid(),
                        measurement.getTimeOffsetNanos(),
                        measurement.getState(),
                        measurement.getReceivedSvTimeNanos(),
                        measurement.getReceivedSvTimeUncertaintyNanos(),
                        measurement.getCn0DbHz(),
                        measurement.getPseudorangeRateMetersPerSecond(),
                        measurement.getPseudorangeRateUncertaintyMetersPerSecond(),
                        measurement.getAccumulatedDeltaRangeState(),
                        measurement.getAccumulatedDeltaRangeMeters(),
                        measurement.getAccumulatedDeltaRangeUncertaintyMeters(),
                        measurement.hasCarrierFrequencyHz() ? measurement.getCarrierFrequencyHz() : "",
                        measurement.hasCarrierCycles() ? measurement.getCarrierCycles() : "",
                        measurement.hasCarrierPhase() ? measurement.getCarrierPhase() : "",
                        measurement.hasCarrierPhaseUncertainty()
                                ? measurement.getCarrierPhaseUncertainty()
                                : "",
                        measurement.getMultipathIndicator(),
                        measurement.hasSnrInDb() ? measurement.getSnrInDb() : "",
                        measurement.getConstellationType(),
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                                && measurement.hasAutomaticGainControlLevelDb()
                                ? measurement.getAutomaticGainControlLevelDb()
                                : "",
                        measurement.hasCarrierFrequencyHz() ? measurement.getCarrierFrequencyHz() : "");
        mFileWriter.write(measurementStream);
        mFileWriter.newLine();
    }

    /**
     * Implements a {@link FileFilter} to delete files that are not in the
     * {@link FileToDeleteFilter#mRetainedFiles}.
     */
    private static class FileToDeleteFilter implements FileFilter {
        private final List<File> mRetainedFiles;

        public FileToDeleteFilter(File... retainedFiles) {
            this.mRetainedFiles = Arrays.asList(retainedFiles);
        }

        /**
         * Returns {@code true} to delete the file, and {@code false} to keep the file.
         *
         * <p>Files are deleted if they are not in the {@link FileToDeleteFilter#mRetainedFiles} list.
         */
        @Override
        public boolean accept(File pathname) {
            if (pathname == null || !pathname.exists()) {
                return false;
            }
            if (mRetainedFiles.contains(pathname)) {
                return false;
            }
            return pathname.length() < MINIMUM_USABLE_FILE_SIZE_BYTES;
        }
    }
}
