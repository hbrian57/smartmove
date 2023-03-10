/*
 * Copyright (c) 2011 Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland, Daisuke Yoshida. All Rights Reserved.
 *
 * This file is part of goGPS Project (goGPS).
 *
 * goGPS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * goGPS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with goGPS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.gogpsproject.producer.rinex;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.gogpsproject.ephemeris.EphGps;
import org.gogpsproject.ephemeris.GNSSEphemeris;
import org.gogpsproject.ephemeris.GNSSEphemerisCorrections;
import org.gogpsproject.ephemeris.SatelliteCodeBiases;
import org.gogpsproject.positioning.Coordinates;
import org.gogpsproject.positioning.Time;
import org.gogpsproject.producer.ObservationSet;
import org.gogpsproject.producer.Observations;
import org.gogpsproject.producer.StreamEventListener;
import org.gogpsproject.producer.parser.IonoGps;
/**
 * <p>
 * Produces Rinex 3 as StreamEventListener
 * </p>
 *
 * @author Daisuke YOSHIDA (OCU)
 */


public class RinexV3Producer implements StreamEventListener {

	private String outFilename;
	private boolean headerWritten;

	private Coordinates approxPosition = null;

	private Vector<Observations> observations = new Vector<Observations>();

	private boolean needApproxPos=false;
	private boolean singleFreq=false;
	private boolean standardFilename=true;

	private FileOutputStream fos = null;
	private PrintStream ps = null;

	private ArrayList<Type> typeConfig = new ArrayList<Type>();

	private SimpleDateFormat sdfHeader = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
	private DecimalFormat dfX3 = new DecimalFormat("0.000");
	private DecimalFormat dfX7 = new DecimalFormat("0.0000000");
	private DecimalFormat dfX = new DecimalFormat("0");
	private DecimalFormat dfXX = new DecimalFormat("00");
	private DecimalFormat dfX4 = new DecimalFormat("0.0000");
	private String marker;
	private int minDOY = 0;
	private int DOYold = -1;
	private String outputDir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/test";
	private boolean enableZip = false;
	
	boolean gpsEnable = true;  // enable GPS data writing
	boolean qzsEnable = false;  // enable QZSS data writing
    boolean gloEnable = false;  // enable GLONASS data writing	
    boolean galEnable = false;  // enable Galileo data writing
    boolean bdsEnable = false;  // enable BeiDou data writing

	private final static TimeZone TZ = TimeZone.getTimeZone("GMT");

	public RinexV3Producer(boolean needApproxPos, boolean singleFreq, String marker, Boolean[] multiConstellation, int minDOY){

		this.needApproxPos = needApproxPos;
		this.singleFreq = singleFreq;
		this.marker = marker;
		this.minDOY = minDOY;

		gpsEnable = multiConstellation[0];
		qzsEnable = multiConstellation[1];
		gloEnable = multiConstellation[2];
		galEnable = multiConstellation[3];
		bdsEnable = multiConstellation[4];

		// set observation type config - Been modified by Antoine to make it work with GPP
		typeConfig.add(new Type(Type.C,1));
		typeConfig.add(new Type(Type.L,1));
		typeConfig.add(new Type(Type.D,1));
		typeConfig.add(new Type(Type.S,1));

		if (!this.singleFreq) {
			typeConfig.add(new Type(Type.C,2)); // TODO value was changed from 2 to 5 because phone only receive L5
			typeConfig.add(new Type(Type.L,2));
			typeConfig.add(new Type(Type.D,2));
			typeConfig.add(new Type(Type.S,2));

//		typeConfig.add(new Type(Type.C,1));
//		if (!this.singleFreq) {
//			typeConfig.add(new Type(Type.P,1));
//		}
//		typeConfig.add(new Type(Type.L,1));
//
//		//typeConfig.add(new Type(Type.D,1)); // NO DOPPLER MEASURMENT IN RAW DATA
//
//		typeConfig.add(new Type(Type.S,1));
//		if (!this.singleFreq) {
//			//typeConfig.add(new Type(Type.P,2));
//			typeConfig.add(new Type(Type.L,2));
//			//typeConfig.add(new Type(Type.D,2)); // NO DOPPLER MEASURMENT IN RAW DATA
//			typeConfig.add(new Type(Type.S,2));
		}
	}
	
	public RinexV3Producer(boolean needApproxPos, boolean singleFreq, String marker, Boolean[] multiConstellation){
		this(needApproxPos, singleFreq, marker, multiConstellation, 0);
	}
	
	public RinexV3Producer(boolean needApproxPos, boolean singleFreq, String marker){
		this(needApproxPos, singleFreq, marker, null, 0);
	}
	
	public RinexV3Producer(boolean needApproxPos, boolean singleFreq){
		this(needApproxPos, singleFreq, null, null, 0);
		this.standardFilename=false;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventListener#addEphemeris(org.gogpsproject.EphGps)
	 */
	@Override
	public void addEphemeris(EphGps eph) {

	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventListener#addIonospheric(org.gogpsproject.IonoGps)
	 */
	@Override
	public void addIonospheric(IonoGps iono) {

	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventListener#addObservations(org.gogpsproject.Observations)
	 */
	@Override
	public void addObservations(Observations o) {
		synchronized (this) {
			//Log.e("ERROR_RINEX", "Adding Observation");

			Time epoch = o.getRefTime();
			int DOY = epoch.getDayOfYear();
			if (DOY >= this.minDOY) {
				if (this.standardFilename && (this.outFilename == null || this.DOYold != DOY)) {
					streamClosed();

					if (this.enableZip && this.outFilename != null) {
						byte[] buffer = new byte[1024];

						try{

							String zn = this.outFilename + ".zip";
							FileOutputStream fos = new FileOutputStream(zn);
							ZipOutputStream zos = new ZipOutputStream(fos);
							String [] tokens = this.outFilename.split("/|\\\\");
							String fn = "";
							if (tokens.length > 0) {
								fn = tokens[tokens.length-1].trim();
							}
							ZipEntry ze= new ZipEntry(fn);
							zos.putNextEntry(ze);
							FileInputStream in = new FileInputStream(this.outFilename);

							int len;
							while ((len = in.read(buffer)) > 0) {
								zos.write(buffer, 0, len);
							}

							in.close();
							zos.closeEntry();
							zos.close();

							File file = new File(this.outFilename);
							file.delete();

							System.out.println("--RINEX file compressed as "+zn);

						}catch(IOException ex){
							ex.printStackTrace();
						}
					}

					File file = new File(outputDir);
					if(!file.exists() || !file.isDirectory()){
						boolean wasDirectoryMade = file.mkdirs();
						if(wasDirectoryMade)System.out.println("Directory "+outputDir+" created");
						else System.out.println("Could not create directory "+outputDir);
					}

					char session = '0';
					int year = epoch.getYear();

					SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
					dateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
					Date date = Calendar.getInstance().getTime();

					String timeStamp = dateFormat.format(date);

					String filename = marker + "00FRA" + "_R_" + String.format("%d", year)
							+ String.format("%03d", DOY) + timeStamp + "_01D_01S_MO";

					String outFile = outputDir + "/" + filename + ".rnx";
					File f = new File(outFile);

					while (f.exists()){
						session++;
						outFile = outputDir + "/" + filename + ".rnx";
						f = new File(outFile);
					}

					System.out.println("Started writing RINEX file "+outFile);
					setFilename(outFile);

					DOYold = DOY;

					headerWritten = false;
				}
				if(!headerWritten){
					observations.add(o);
					if(needApproxPos && approxPosition==null){
						return;
					}

					try {
						writeHeader(approxPosition, observations.firstElement());
					} catch (IOException e) {
						e.printStackTrace();
					}

					for(Observations obs:observations){
						try {
							writeObservation(obs);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					observations.removeAllElements();
					headerWritten = true;
				}else{
					try {
						writeObservation(o);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventListener#setDefinedPosition(org.gogpsproject.Coordinates)
	 */
	@Override
	public void setDefinedPosition(Coordinates definedPosition) {
		synchronized (this) {
			this.approxPosition = definedPosition;
		}
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventListener#streamClosed()
	 */
	@Override
	public void streamClosed() {
		try {
			ps.close();
		} catch (Exception e) {

		}
		try {
			fos.close();
		} catch (Exception e) {

		}
	}

	private void writeHeader(Coordinates approxPosition,Observations firstObservation) throws IOException{
		
//	     3.01           OBSERVATION DATA    M: Mixed            RINEX VERSION / TYPE
//	     goGPS               OCU_KU              20131021 063917 UTC PGM / RUN BY / DATE 
//	     OCUS                                                        MARKER NAME         
//	     NON_GEODETIC                                                MARKER TYPE         
//	     DY_ER               OCU_KU                                  OBSERVER / AGENCY   
//	                         NVS                                     REC # / TYPE / VERS 
//	                                                                 ANT # / TYPE        
//	             0.0000        0.0000        0.0000                  APPROX POSITION XYZ 
//	             0.0000        0.0000        0.0000                  ANTENNA: DELTA H/E/N
//	          2     0                                                WAVELENGTH FACT L1/2
//	     G    4 C1C L1C S1C D1C                                      SYS / # / OBS TYPES 
//	     R    4 C1C L1C S1C D1C                                      SYS / # / OBS TYPES 
//	     E    4 C1X L1X S1X D1X                                      SYS / # / OBS TYPES 
//	     J    4 C1C L1C S1C D1C                                      SYS / # / OBS TYPES 
//	     DBHZ                                                        SIGNAL STRENGTH UNIT
//	          1.000                                                  INTERVAL            
//	       2013    10    21     5    27   15.9996319     GPS         TIME OF FIRST OBS   
//	     G                                                           SYS / PHASE SHIFTS  
//	     R                                                           SYS / PHASE SHIFTS  
//	     E                                                           SYS / PHASE SHIFTS  
//	     J                                                           SYS / PHASE SHIFTS  
//	                                                                 END OF HEADER

		String operator = "GEOLOC/IFSTTAR";
		String prgm = "GeolocPVT_V1";
		String markerName = marker;
		String markerNumber = "10000M000";
		String receiver1 = "Xiaomi Mi8";
		String receiver2 = "BROADCOM";

		String antenna1 = "Xiaomi Mi8";
		String antenna2 = "INTEGRATED";
		
		writeLine (sf("",5)+sf("3.01",15)+sf("OBSERVATION DATA",20)+sf("M: Mixed",20)+se("RINEX VERSION / TYPE",20), false);
		appendLine(sf(prgm,20)+sf(operator,20)+sf(sdfHeader.format(Calendar.getInstance(TZ).getTime()).toUpperCase(),20)+se("PGM / RUN BY / DATE",20));
		appendLine(sf(markerName,20*3)+se("MARKER NAME",20));
		appendLine(sf(markerNumber,20*3)+se("MARKER NUMBER",20));
		appendLine(sf("NON_GEODETIC",20*3)+se("MARKER TYPE",20));
		appendLine(sf(operator,20*3)+se("OBSERVER / AGENCY",20));
		appendLine(sf(receiver1,20) + sf(receiver2,20) + se("",20) + se("REC # / TYPE / VERS",20));
		appendLine(sf(antenna1,20) + sf(antenna2,20) + se("",20) +se("ANT # / TYPE",20));
		
		if(approxPosition != null){
			appendLine(sp(dfX4.format(approxPosition.getX()),14,1)+sp(dfX4.format(approxPosition.getY()),14,1)+sp(dfX4.format(approxPosition.getZ()),14,1)+sf("",18)+se("APPROX POSITION XYZ",20));
		}else{
			appendLine(sp(dfX4.format(0.0),14,1)+sp(dfX4.format(0.0),14,1)+sp(dfX4.format(0.0),14,1)+sf("",18)+se("APPROX POSITION XYZ",20));
		}
		appendLine(sp(dfX4.format(0.0),14,1)+sp(dfX4.format(0.0),14,1)+sp(dfX4.format(0.0),14,1)+sf("",18)+se("ANTENNA: DELTA H/E/N",20));
		boolean found = false;
		for (Type t:typeConfig) {
		    if (t.toString().equals("L2")) {
		        found = true;
		        break;
		    }
		}
		int wf1 = 2; int wf2 = 0; //single frequency (hypothesizing ublox-type low-cost receiver, i.e. with half cycle ambiguities)
		if(found) {
			wf1 = 1; wf2 = 1; //dual frequency (hypothesizing full cycle ambiguities)
		}
		appendLine(sp(dfX.format(wf1),6,1)+sp(dfX.format(wf2),6,1)+sf("",6)+sf("",6)+sf("",6)+sf("",6)+sf("",6)+sf("",6)+sf("",12)+se("WAVELENGTH FACT L1/2",20));

		if (gpsEnable) {
			String line = "G";
			int cols=52;
			line += sp(dfX.format(typeConfig.size()),5,3); cols -= 6;
			for(Type t:typeConfig)
			{
				// ADDED for XIAOMI (only L5)
				String str = t.toString();
				if(str.charAt(str.length() - 1) == '2')
				{
					str = str.substring(0,str.length() - 1) + "5X";
				}
				else
				{
					str =t.toString() + 'C';
				}

				line += sp(str,4,1);cols -= 3;
			}
			line += se("",cols);
			line += se("SYS / # / OBS TYPES ",20);
			appendLine(line);
		}
		
		if (gloEnable) {
			String line = "R";
			int cols=56;
			line += sp(dfX.format(typeConfig.size()),5,3); cols -= 6;
			for(Type t:typeConfig){
				line += sp(t.toString()+'C',4,1);cols -= 3;
			}
			line += se("",cols);
			line += se("SYS / # / OBS TYPES ",20);
			appendLine(line);
		}
		
		if (galEnable) {
			String line = "E";
			int cols=52;
			line += sp(dfX.format(typeConfig.size()),5,3); cols -= 6;
			for(Type t:typeConfig)
			{
				// ADDED for XIAOMI (only L5)
				String str = t.toString();
				if(str.charAt(str.length() - 1) == '2')
				{
					str = str.substring(0,str.length() - 1) + "5X";
				}
				else
				{
					str =t.toString() + 'C';
				}

				line += sp(str,4,1);cols -= 3;
			}
			line += se("",cols);
			line += se("SYS / # / OBS TYPES ",20);
			appendLine(line);
		}
		
		if (bdsEnable) {
			String line = "C";
			int cols=52;
			line += sp(dfX.format(typeConfig.size()),5,3); cols -= 6;
			for(Type t:typeConfig){

				// ADDED for XIAOMI (only L5)
				String str = t.toString();
				if(str.charAt(str.length() - 1) == '2')
				{
					str = str.substring(0,str.length() - 1) + "5X";
				}
				else
				{
					str =t.toString() + 'C';
				}

				line += sp(str,4,1);cols -= 3;
			}
			line += se("", cols);
			line += se("SYS / # / OBS TYPES ",20);
			appendLine(line);
		}
		
		if (qzsEnable) {
			String line = "J";
			int cols=56;
			line += sp(dfX.format(typeConfig.size()),5,3); cols -= 6;
			for(Type t:typeConfig){
				line += sp(t.toString()+'C',4,1);cols -= 3;
			}
			line += se("",cols);
			line += se("SYS / # / OBS TYPES ",20);
			appendLine(line);
		}
		
		//appendLine(sp(dfX.format(1),6,1)+sf("",60-1*6)+se("INTERVAL",20));

		if(firstObservation!=null){
			Calendar c = Calendar.getInstance(TZ);
			c.setTimeInMillis(firstObservation.getRefTime().getMsec());
			appendLine(sp(dfX.format(c.get(Calendar.YEAR)),6,1)
					+sp(dfX.format(c.get(Calendar.MONTH)+1),6,1)
					+sp(dfX.format(c.get(Calendar.DATE)),6,1)
					+sp(dfX.format(c.get(Calendar.HOUR_OF_DAY)),6,1)
					+sp(dfX.format(c.get(Calendar.MINUTE)),6,1)
					+sp(dfX7.format(c.get(Calendar.SECOND)+c.get(Calendar.MILLISECOND)/1000.0),13,1)
					+sp("GPS",8,1)+sf("",9)+se("TIME OF FIRST OBS",20));
		}

		appendLine(sf("DBHZ",20*3)+se("SIGNAL STRENGTH UNIT",20));
		//appendLine(sf("G",20*3)+se("SYS / PHASE SHIFTS",20));
		appendLine(sf("",60)+se("END OF HEADER",20));


	}


/**
	 * @return the typeConfig
	 */
	public ArrayList<Type> getTypeConfig() {
		return (ArrayList<Type>)typeConfig.clone();
	}

	/**
	 * @param typeConfig the typeConfig to set
	 */
	public void setTypeConfig(ArrayList<Type> typeConfig) throws Exception {
		if(headerWritten) throw new Exception("Header already written.");
		this.typeConfig = typeConfig;
	}

//	> 2013 10 21  5 27 15.9996319  0 12
//	G02  24302142.768 7  -7040077.03107        45.000 7      3026.216 7
//	G04  21034956.023 8 -12544569.50908        51.000 8      2030.071 8
//	G12  24712920.728 4                        26.000 4      2631.261 4
//	G13  22615468.719 8 -16359274.25108        49.000 8      3201.976 8
//	G17  20229009.570 8  -8621207.89608        51.000 8       651.906 8
//	G20  22365294.155 7   8462486.38907        46.000 7     -2283.400 7
//	G23  22380053.521 7 -11806763.28207        46.000 7      1305.138 7
//	G28  23491793.548 6  11273447.98706        41.000 6     -2604.791 6
//	E12  26431512.796 7  -1032019.36107        47.000 7      2643.264 7
//	E19  25076155.320 7 -13134718.07407        43.000 7      1831.788 7
//	E20  25160867.569 7  -1070764.90207        46.000 7      -619.438 7
//	J01  38690760.026 7   1019110.27407        47.000 7       208.927 7
	
	
	private void writeObservation(Observations o) throws IOException{
		//System.out.println(o);

		//Log.e("ERROR_RINEX", "Writting Observation");

		Calendar c = Calendar.getInstance(TZ);
		c.setTimeInMillis(o.getRefTime().getMsec());

		String line = ">";
		line += sp(dfX.format(c.get(Calendar.YEAR)),5,1);
		line += sp(dfX.format(c.get(Calendar.MONTH)+1),3,1);
		line += sp(dfX.format(c.get(Calendar.DATE)),3,1);
		line += sp(dfX.format(c.get(Calendar.HOUR_OF_DAY)),3,1);
		line += sp(dfX.format(c.get(Calendar.MINUTE)),3,1);
		line += sp(dfX7.format(c.get(Calendar.SECOND)+c.get(Calendar.MILLISECOND)/1000.0+o.getRefTime().getFraction()/1000),11,1);
		line += sp(dfX.format(o.getEventFlag()),3,1);

		/*
		int gpsSize = 0;
		for(int i=0;i<o.getNumSat();i++) {
			if(o.getSatByIdx(i) != null)
			{

				if (o.getSatByIdx(i).getSatID() <= 32) {

				}

				gpsSize++;
			}
		}*/

		line += sp(dfX.format(o.getNumSat()),3,1);
		
//		int cnt=0;
//		for(int i=0;i<o.getNumSat();i++){
//			if(o.getSatByIdx(i).getSatID()<=32){ // skip non GPS IDs
//				if(cnt==12){
//					writeLine(line, true);
//					line = "                                ";
//				}
//				line += "G"+dfXX.format(o.getSatID(i));
//				cnt++;
//			}
//		}

		writeLine(line, true);

		//Log.e("ERROR_RINEX", "Writting Observation set");
		//System.out.println(o.getNumSat());

		writeObservations(o, line);
	}

	private void writeObservations(Observations o, String line) throws IOException
	{
		for(Map.Entry<String, ObservationSet> entry : o.getObsSetHM().entrySet())
		{
			ObservationSet os = entry.getValue();

			line = "";
			line += os.getSatType() + dfXX.format(os.getSatID());
			int cnt = 0;
			//System.out.println(os.getSatType());
			for (Type t : typeConfig) {
				switch (t.getType()) {
					case Type.C:
						line += Double.isNaN(os.getCodeC(t.getFrequency() - 1)) ? sf("", 16) : sp(dfX3.format(os.getCodeC(t.getFrequency() - 1)), 14, 1) + "  ";
						break;
					case Type.P:
						line += Double.isNaN(os.getCodeP(t.getFrequency() - 1)) ? sf("", 16) : sp(dfX3.format(os.getCodeP(t.getFrequency() - 1)), 14, 1) + "  ";
						break;
					case Type.L:
						if (os.getPhaseCycles(t.getFrequency() - 1) == 0 || Math.abs(os.getPhaseCycles(t.getFrequency() - 1)) < 1e-15)
							os.setPhaseCycles(t.getFrequency() - 1, Double.NaN);
						line += Double.isNaN(os.getPhaseCycles(t.getFrequency() - 1)) ? sf("", 14) : sp(dfX3.format(os.getPhaseCycles(t.getFrequency() - 1)), 14, 1); // L
						//line += os.getLossLockInd(t.getFrequency()-1)<0?" ":dfX.format(os.getLossLockInd(t.getFrequency()-1)); // L1 Loss of Lock Indicator
						//line += Float.isNaN(os.getSignalStrength(t.getFrequency()-1))?" ":dfX.format(Math.floor(os.getSignalStrength(t.getFrequency()-1)/6)); // L1 Signal Strength Indicator
						break;
					case Type.D:
						line += Float.isNaN(os.getDoppler(t.getFrequency() - 1)) ? sf("", 16) : sp(dfX3.format(os.getDoppler(t.getFrequency() - 1)), 14, 1) + "  ";
						break;
					case Type.S:
						line += Float.isNaN(os.getSignalStrength(t.getFrequency() - 1)) ? sf("", 16) : sp(dfX3.format(os.getSignalStrength(t.getFrequency() - 1)), 14, 1) + "  ";
						break;
				}
				cnt++;
				//System.out.println(line);
				if (cnt == typeConfig.size()) {
					writeLine(line, true);
					line = "";
					cnt = 0;
				}
			}
		}
		return;
	}

	// space end
	private String se(String in, int max){
		return sf(in,max,0);
	}
	// space fill with 1 space margin
	private String sf(String in, int max){
		return sf(in,max,1);
	}
	// space fill with margin
	private String sf(String in, int max,int margin){
		if(in.length()==max-margin){
			while(in.length()<max) in +=" ";
			return in;
		}
		if(in.length()>max-margin){
			return in.substring(0, max-margin)+" ";
		}
		while(in.length()<max) in +=" ";

		return in;
	}
	// space prepend with margin
	private String sp(String in, int max,int margin){
		if(in.length()==max-margin){
			while(in.length()<max) in =" "+in;
			return in;
		}
		if(in.length()>max-margin){
			return in.substring(0, max-margin)+" ";
		}
		while(in.length()<max) in =" "+in;

		return in;
	}

	private void appendLine(String line) throws IOException{
		writeLine(line, true);
	}
	private void writeLine(String line, boolean append) throws IOException{

		FileOutputStream fos = this.fos;
		PrintStream ps = this.ps;
		if(this.fos == null){
			fos = new FileOutputStream(outFilename, append);
			ps = new PrintStream(fos);
		}

		ps.println(line);
		System.out.println(line);
		//Log.e("ERROR_RINEX", fos.toString());

		//ps.flush();
		if(this.fos == null){
			ps.close();
			fos.close();
		}
	}

	@Override
	public Observations getCurrentObservations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void pointToNextObservations() {
		// TODO Auto-generated method stub
		
	}
	
	public void setFilename(String outFilename) {
		this.outFilename = outFilename;

//		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), outFilename);
//		System.out.println(file.getAbsolutePath());

		try {
			fos = new FileOutputStream(outFilename, true);
			ps = new PrintStream(fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void setOutputDir(String outDir) {
		this.outputDir = outDir;
	}
	
	public void enableCompression(boolean enableZip) {
		this.enableZip = enableZip;
	}

	@Override
	public void onStreamReceived(String msg) {
		return;
	}

	@Override
	public void addEphemeris(GNSSEphemeris eph) {

	}

	@Override
	public void addEphemerisCorr(GNSSEphemerisCorrections ephCorr)
	{

	}

	@Override
	public void addSatelliteCodeBiases(SatelliteCodeBiases _scb)
	{

	}
}
