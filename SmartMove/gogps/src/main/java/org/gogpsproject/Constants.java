/*
 * Copyright (c) 2010, Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
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
 *
 */
package org.gogpsproject;

import android.util.Log;

/**
 * <p>
 * Constants
 * </p>
 *
 * @author Eugenio Realini, Cryms.com
 */
public class Constants {

	// Speed of Light [m/s]
	public static final double SPEED_OF_LIGHT = 299792458.0;

	// Physical quantities as in IS-GPS
	public static final double EARTH_GRAVITATIONAL_CONSTANT = 3.986005e14;
	public static final double EARTH_ANGULAR_VELOCITY = 7.2921151467e-5;
	public static final double RELATIVISTIC_ERROR_CONSTANT = -4.442807633e-10;

	// GPS signal approximate travel time
	public static final double GPS_APPROX_TRAVEL_TIME = 0.072;

	// WGS84 ellipsoid features
	public static final double WGS84_SEMI_MAJOR_AXIS = 6378137;
	public static final double WGS84_FLATTENING = 1 / 298.257222101;
	public static final double WGS84_ECCENTRICITY = Math.sqrt(1 - Math.pow(
			(1 - WGS84_FLATTENING), 2));

	// Time-related values
	public static final long DAYS_IN_WEEK = 7L;
	public static final long SEC_IN_DAY = 86400L;
	public static final long SEC_IN_HOUR = 3600L;
	public static final long MILLISEC_IN_SEC = 1000L;
	public static final long SEC_IN_HALF_WEEK = 302400L;
	public static final double JD_1_JAN_2000 = 2451545.0;
	// Days difference between UNIX time and GPS time
	public static final long UNIX_GPS_DAYS_DIFF = 3657L;

	// Standard atmosphere - Berg, 1948 (Bernese)
	public static final double STANDARD_PRESSURE = 1013.25;
	public static final double STANDARD_TEMPERATURE = 291.15;

	// Parameters to weigh observations by signal-to-noise ratio
	public static final float SNR_a = 30;
	public static final float SNR_A = 30;
	public static final float SNR_0 = 10;
	public static final float SNR_1 = 50;
	
	/*
	 CONSTELLATION REF 
     CRS parameters, according to each GNSS system CRS definition
     (ICD document in brackets):
    
     *_GPS --> WGS-84  (IS-GPS200E)
     *_GLO --> PZ-90   (GLONASS-ICD 5.1)
     *_GAL --> GTRF    (Galileo-ICD 1.1)
     *_BDS --> CSG2000 (BeiDou-ICD 1.0)
     *_QZS --> WGS-84  (IS-QZSS 1.5D)
    */

	// coefficient of GMST
	public static final double GMST0 = 24110.54841;
	public static final double GMST1 = 8640184.812866;
	public static final double GMST2 = 0.093104;
	public static final double GMST3 = 6.2e-6;


	//GNSS frequencies
	public static final double FL1 = 1575.420e6; // GPS
	public static final double FL2 = 1227.600e6;
	public static final double FL5 = 1176.450e6;

	public static final double WL1 = SPEED_OF_LIGHT/FL1; //Wavelenght
	public static final double WL5 = SPEED_OF_LIGHT/FL5;

	//public static final double WL3 = SPEED_OF_LIGHT * (FL1-FL2)/(FL1*FL1+FL5*FL5);

	public static final double WL3 = SPEED_OF_LIGHT * (FL1 - FL5) / (FL1*FL1 - FL5*FL5);

	public static final double FR1_base = 1602.000e6; // GLONASS
	public static final double FR2_base = 1246.000e6;
	public static final double FR1_delta = 0.5625;
	public static final double FR2_delta = 0.4375;
	
	public static final double FE1  = FL1; // Galileo
	public static final double FE5a = FL5;
	public static final double FE5b = 1207.140e6;
	public static final double FE5  = 1191.795e6;
	public static final double FE6  = 1278.750e6;

	public static final double WE1 = SPEED_OF_LIGHT/FE1; //Wavelenght
	public static final double WE5a = SPEED_OF_LIGHT/FE5a;
	
	public static final double FC1  = 1589.740e6; // BeiDou
	public static final double FC2  = 1561.098e6;
	public static final double FC5b = FE5b;
	public static final double FC6  = 1268.520e6;

	public static final double FB1C = FL1;
	public static final double FB2a = FL5;

	public static final double WB1C = SPEED_OF_LIGHT/FB1C; //Wavelenght
	public static final double WB2a = SPEED_OF_LIGHT/FB2a;
	
	public static final double FJ1  = FL1; // QZSS
	public static final double FJ2  = FL2;
	public static final double FJ5  = FL5;
	public static final double FJ6  = FE6;

	// other GNSS parameters
	public static final long ELL_A_GPS = 6378137;                          // GPS (WGS-84)     Ellipsoid semi-major axis [m]
	public static final long ELL_A_GLO = 6378136;                          // GLONASS (PZ-90)  Ellipsoid semi-major axis [m]
	public static final long ELL_A_GAL = 6378137;                          // Galileo (GTRF)   Ellipsoid semi-major axis [m]
	public static final long ELL_A_BDS = 6378136;                          // BeiDou (CSG2000) Ellipsoid semi-major axis [m]
	public static final long ELL_A_QZS = 6378137;                          // QZSS (WGS-84)    Ellipsoid semi-major axis [m]
    
	public static final double ELL_F_GPS = 1/298.257222101;                  // GPS (WGS-84)     Ellipsoid flattening
	public static final double ELL_F_GLO = 1/298.257222101;                  // GLONASS (PZ-90)  Ellipsoid flattening
	public static final double ELL_F_GAL = 1/298.257222101;                  // Galileo (GTRF)   Ellipsoid flattening
	public static final double ELL_F_BDS = 1/298.257222101;                  // BeiDou (CSG2000) Ellipsoid flattening
	public static final double ELL_F_QZS = 1/298.257222101;                  // QZSS (WGS-84)    Ellipsoid flattening
    
	public static final double ELL_E_GPS = Math.sqrt(1-(1- Math.pow(ELL_F_GPS, 2)));   // GPS (WGS-84)     Eccentricity
	public static final double ELL_E_GLO = Math.sqrt(1-(1- Math.pow(ELL_F_GLO, 2)));   // GLONASS (PZ-90)  Eccentricity
	public static final double ELL_E_GAL = Math.sqrt(1-(1- Math.pow(ELL_F_GAL, 2)));   // Galileo (GTRF)   Eccentricity
	public static final double ELL_E_BDS = Math.sqrt(1-(1- Math.pow(ELL_F_BDS, 2)));   // BeiDou (CSG2000) Eccentricity
	public static final double ELL_E_QZS = Math.sqrt(1-(1- Math.pow(ELL_F_QZS, 2)));   // QZSS (WGS-84)    Eccentricity
    
	public static final double GM_GPS = 3.986005e14;                     // GPS     Gravitational constant * (mass of Earth) [m^3/s^2]
	public static final double GM_GLO = 3.9860044e14;                    // GLONASS Gravitational constant * (mass of Earth) [m^3/s^2]
	public static final double GM_GAL = 3.986004418e14;                  // Galileo Gravitational constant * (mass of Earth) [m^3/s^2]
	public static final double GM_BDS = 3.986004418e14;                  // BeiDou  Gravitational constant * (mass of Earth) [m^3/s^2]
	public static final double GM_QZS = 3.986005e14;                     // QZSS    Gravitational constant * (mass of Earth) [m^3/s^2]
    
	public static final double OMEGAE_DOT_GPS = 7.2921151467e-5;             // GPS     Angular velocity of the Earth rotation [rad/s]
	public static final double OMEGAE_DOT_GLO = 7.292115e-5;                 // GLONASS Angular velocity of the Earth rotation [rad/s]
	public static final double OMEGAE_DOT_GAL = 7.2921151467e-5;             // Galileo Angular velocity of the Earth rotation [rad/s]
	public static final double OMEGAE_DOT_BDS = 7.292115e-5;                 // BeiDou  Angular velocity of the Earth rotation [rad/s]
	public static final double OMEGAE_DOT_QZS = 7.2921151467e-5;             // QZSS    Angular velocity of the Earth rotation [rad/s]
    
	public static final double J2_GLO = -1.08263e-3;                          // GLONASS second zonal harmonic of the geopotential
    
	public static final double PI_ORBIT = 3.1415926535898;                   // pi value used for orbit computation
	public static final double CIRCLE_RAD = 2 * PI_ORBIT;                    // 2 pi

	public static final double RAD2DEG = 180/PI_ORBIT;                       // Conversion radian to degree

	//Added by Antoine Grenier for streams decoding
	// From RTKLIB, rtklib.h, GPS SPS Signal Specification, 2.4.3

	// For messages with DCBs (1059,...)
	public static final int CODE_NONE = 0;                  /* obs code: none or unknown */
	public static final int CODE_L1C = 1;                   /* obs code: L1C/A,G1C/A,E1C (GPS,GLO,GAL,QZS,SBS) */
	public static final int CODE_L1P = 2;                   /* obs code: L1P,G1P    (GPS,GLO) */
	public static final int CODE_L1W = 3;                   /* obs code: L1 Z-track (GPS) */
	public static final int CODE_L1Y = 4;                   /* obs code: L1Y        (GPS) */
	public static final int CODE_L1M = 5;                   /* obs code: L1M        (GPS) */
	public static final int CODE_L1N = 6;                   /* obs code: L1codeless (GPS) */
	public static final int CODE_L1S = 7;                   /* obs code: L1C(D)     (GPS,QZS) */
	public static final int CODE_L1L = 8;                   /* obs code: L1C(P)     (GPS,QZS) */
	public static final int CODE_L1E = 9;                   /* (not used) */
	public static final int CODE_L1A = 10;                  /* obs code: E1A        (GAL) */
	public static final int CODE_L1B = 11;                  /* obs code: E1B        (GAL) */
	public static final int CODE_L1X = 12;                  /* obs code: E1B+C,L1C(D+P) (GAL,QZS) */
	public static final int CODE_L1Z = 13;                  /* obs code: E1A+B+C,L1SAIF (GAL,QZS) */
	public static final int CODE_L2C = 14;                  /* obs code: L2C/A,G1C/A (GPS,GLO) */
	public static final int CODE_L2D = 15;                  /* obs code: L2 L1C/A-(P2-P1) (GPS) */
	public static final int CODE_L2S = 16;                  /* obs code: L2C(M)     (GPS,QZS) */
	public static final int CODE_L2L = 17;                  /* obs code: L2C(L)     (GPS,QZS) */
	public static final int CODE_L2X = 18;                  /* obs code: L2C(M+L),B1I+Q (GPS,QZS,CMP) */
	public static final int CODE_L2P = 19;                  /* obs code: L2P,G2P    (GPS,GLO) */
	public static final int CODE_L2W = 20;                  /* obs code: L2 Z-track (GPS) */
	public static final int CODE_L2Y = 21;                  /* obs code: L2Y        (GPS) */
	public static final int CODE_L2M = 22;                  /* obs code: L2M        (GPS) */
	public static final int CODE_L2N = 23;                  /* obs code: L2codeless (GPS) */
	public static final int CODE_L5I = 24;                  /* obs code: L5/E5aI    (GPS,GAL,QZS,SBS) */
	public static final int CODE_L5Q = 25;                  /* obs code: L5/E5aQ    (GPS,GAL,QZS,SBS) */
	public static final int CODE_L5X = 26;                  /* obs code: L5/E5aI+Q/L5B+C (GPS,GAL,QZS,IRN,SBS) */
	public static final int CODE_L7I = 27;                  /* obs code: E5bI,B2I   (GAL,CMP) */
	public static final int CODE_L7Q = 28;                  /* obs code: E5bQ,B2Q   (GAL,CMP) */
	public static final int CODE_L7X = 29;                  /* obs code: E5bI+Q,B2I+Q (GAL,CMP) */
	public static final int CODE_L6A = 30;                  /* obs code: E6A        (GAL) */
	public static final int CODE_L6B = 31;                  /* obs code: E6B        (GAL) */
	public static final int CODE_L6C = 32;                  /* obs code: E6C        (GAL) */
	public static final int CODE_L6X = 33;                  /* obs code: E6B+C,LEXS+L,B3I+Q (GAL,QZS,CMP) */
	public static final int CODE_L6Z = 34;                  /* obs code: E6A+B+C    (GAL) */
	public static final int CODE_L6S = 35;                  /* obs code: LEXS       (QZS) */
	public static final int CODE_L6L = 36;                  /* obs code: LEXL       (QZS) */
	public static final int CODE_L8I = 37;                  /* obs code: E5(a+b)I   (GAL) */
	public static final int CODE_L8Q = 38;                  /* obs code: E5(a+b)Q   (GAL) */
	public static final int CODE_L8X = 39;                  /* obs code: E5(a+b)I+Q (GAL) */
	public static final int CODE_L2I = 40;                  /* obs code: B1I        (BDS) */
	public static final int CODE_L2Q = 41;                  /* obs code: B1Q        (BDS) */
	public static final int CODE_L6I = 42;                  /* obs code: B3I        (BDS) */
	public static final int CODE_L6Q = 43;                  /* obs code: B3Q        (BDS) */
	public static final int CODE_L3I = 44;                  /* obs code: G3I        (GLO) */
	public static final int CODE_L3Q = 45;                  /* obs code: G3Q        (GLO) */
	public static final int CODE_L3X = 46;                  /* obs code: G3I+Q      (GLO) */
	public static final int CODE_L1I = 47;                  /* obs code: B1I        (BDS) */
	public static final int CODE_L1Q = 48;                  /* obs code: B1Q        (BDS) */
	public static final int CODE_L5A = 49;                  /* obs code: L5A SPS    (IRN) */
	public static final int CODE_L5B = 50;                  /* obs code: L5B RS(D)  (IRN) */
	public static final int CODE_L5C = 51;                  /* obs code: L5C RS(P)  (IRN) */
	public static final int CODE_L9A = 52;                  /* obs code: SA SPS     (IRN) */
	public static final int CODE_L9B = 53;                  /* obs code: SB RS(D)   (IRN) */
	public static final int CODE_L9C = 54;                  /* obs code: SC RS(P)   (IRN) */
	public static final int CODE_L9X = 55;                  /* obs code: SB+C       (IRN) */
	public static final int MAXCODE  = 55;                  /* max number of obs code */

	public static final int[] CODES_GPS = {
			CODE_L1C,CODE_L1P,CODE_L1W,CODE_L1Y,CODE_L1M,CODE_L2C,CODE_L2D,CODE_L2S,
			CODE_L2L,CODE_L2X,CODE_L2P,CODE_L2W,CODE_L2Y,CODE_L2M,CODE_L5I,CODE_L5Q,
			CODE_L5X
	};
	public static final int[] CODES_GAL = {
			CODE_L1A,CODE_L1B,CODE_L1C,CODE_L1X,CODE_L1Z,CODE_L5I,CODE_L5Q,CODE_L5X,
			CODE_L7I,CODE_L7Q,CODE_L7X,CODE_L8I,CODE_L8Q,CODE_L8X,CODE_L6A,CODE_L6B,
			CODE_L6C,CODE_L6X,CODE_L6Z
	};
	public static final int[] CODES_BDS = {
			CODE_L1I,CODE_L1Q,CODE_L1X,CODE_L7I,CODE_L7Q,CODE_L7X,CODE_L6I,CODE_L6Q,
			CODE_L6X
	};
	public static final int[] CODES_GLONASS = {
			CODE_L1C,CODE_L1P,CODE_L2C,CODE_L2P
	};

	public static final double P2_5 = 0.03125;                /* 2^-5 */
	public static final double P2_6 = 0.015625;               /* 2^-6 */
	public static final double P2_11 = 4.882812500000000E-04; /* 2^-11 */
	public static final double P2_15 = 3.051757812500000E-05; /* 2^-15 */
	public static final double P2_17 = 7.629394531250000E-06; /* 2^-17 */
	public static final double P2_19 = 1.907348632812500E-06; /* 2^-19 */
	public static final double P2_20 = 9.536743164062500E-07; /* 2^-20 */
	public static final double P2_21 = 4.768371582031250E-07; /* 2^-21 */
	public static final double P2_23 = 1.192092895507810E-07; /* 2^-23 */
	public static final double P2_24 = 5.960464477539063E-08; /* 2^-24 */
	public static final double P2_27 = 7.450580596923828E-09; /* 2^-27 */
	public static final double P2_29 = 1.862645149230957E-09; /* 2^-29 */
	public static final double P2_30 = 9.313225746154785E-10; /* 2^-30 */
	public static final double P2_31 = 4.656612873077393E-10; /* 2^-31 */
	public static final double P2_32 = 2.328306436538696E-10; /* 2^-32 */
	public static final double P2_33 = 1.164153218269348E-10; /* 2^-33 */
	public static final double P2_35 = 2.910383045673370E-11; /* 2^-35 */
	public static final double P2_38 = 3.637978807091710E-12; /* 2^-38 */
	public static final double P2_39 = 1.818989403545856E-12; /* 2^-39 */
	public static final double P2_40 = 9.094947017729280E-13; /* 2^-40 */
	public static final double P2_43 = 1.136868377216160E-13; /* 2^-43 */
	public static final double P2_48 = 3.552713678800501E-15; /* 2^-48 */
	public static final double P2_50 = 8.881784197001252E-16; /* 2^-50 */
	public static final double P2_55 = 2.775557561562891E-17; /* 2^-55 */
	public static final double P2_10 = 0.0009765625;          /* 2^-10 */
	public static final double P2_34 = 5.820766091346740E-11; /* 2^-34 */
	public static final double P2_46 = 1.421085471520200E-14; /* 2^-46 */
	public static final double P2_59 = 1.734723475976810E-18; /* 2^-59 */
	public static final double P2_66 = 1.355252715606880E-20; /* 2^-66 */

	public static final int LEAP_SECONDS = 18; // Number of leap seconds in 2019/05/22

	public static final int ROLLOVER_WEEKS = 2;

	public static final int BDS_LEAP_SECONDS = 14;

}
