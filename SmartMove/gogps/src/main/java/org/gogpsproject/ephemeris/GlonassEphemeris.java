///=================================================================================================
// Class GlonassEpehemeris
//      Author :  Edgar LENHOF
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

import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.util.Log;

import org.gogpsproject.Constants;

import org.gogpsproject.Utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Class Glonass ephemeris
 * see the Control Document Interface for Glonass system for further information on ephemeris parameters :
 * russianspacesystems.ru/wp-content/uploads/2016/08/ICD_GLONASS_eng_v5.1.pdf
 */
public class GlonassEphemeris extends GNSSEphemeris {

    //sub-frames needed to recuperate ephemeris data needed for statellite position computation
    private GlonassNavigationMessage subFrame1;
    private GlonassNavigationMessage subFrame2;
    private GlonassNavigationMessage subFrame3;
    private GlonassNavigationMessage subFrame4;
    private GlonassNavigationMessage subFrame5;

    // current date, calendar number of day within four-year interval starting from the 1-st of January in a leap year
    private int Nt;
    // number of leap years ellapsed since 1996
    private int N4;
    //reference time of validity of navigation message data (in minute of the utc+3 day)
    // takes value in range [15-1425] at rate 15
    private int tb;
    // time of emission in the day of the message in sec
    private long tk;
    //Health flag
    private int Bn; // 1 indicates malfunction of satellite
    // date of ephemeris validity
    private GregorianCalendar date_of_ephemeris;
    // position of satellite at tb (in km)
    // takes value in range +- 2,7e4
    private double x;
    private double y;
    private double z;
    // speed of satellite at tb (in km/s)
    // takes value in range +- 4,3
    private double x_dot;
    private double y_dot;
    private double z_dot;
    // acceleration at tb (in km/s2)
    // takes value in range +- 6,2e-9
    private double X_ddot;
    private double Y_ddot;
    private double Z_ddot;
    // SV clock offset at tb (in s)
    // takes value in range +-2^9
    private double tau_n;
    // SV relative frequency offset at tb (dimensionless)
    // takes value in range +-2^30
    private double gamma_n;


    /**
     * Constructor with parameters
     * @param subFrame1 sub-frame1
     * @param subFrame2 sub-frame2
     * @param subFrame3 sub-frame3
     * @param subFrame4 sub-frame4
     * @param subFrame5 sub-frame5
     */
    public GlonassEphemeris(GnssNavigationMessage subFrame1 , GnssNavigationMessage subFrame2, GnssNavigationMessage subFrame3, GnssNavigationMessage subFrame4, GnssNavigationMessage subFrame5) {
        super();
        this.subFrame1 = new GlonassNavigationMessage(subFrame1);
        this.subFrame2 = new GlonassNavigationMessage(subFrame2);
        this.subFrame3 = new GlonassNavigationMessage(subFrame3);
        this.subFrame4 = new GlonassNavigationMessage(subFrame4);
        this.subFrame5 = new GlonassNavigationMessage(subFrame5);

        gnssSystem = GnssStatus.CONSTELLATION_GLONASS;
        prn = retrievePrn();

        Nt = retrieveNt();
        N4 = retrieveN4();

        Bn = retrieveBn();

        tb = retrieveTb();
        tk = retrieveTkInSec();

        computeGregorianDate();
        computeWn();

        x = retrieveX();
        y = retrieveY();
        z = retrieveZ();

        x_dot = retrieveXdot();
        y_dot = retrieveYdot();
        z_dot = retrieveZdot();

        X_ddot = retrieveX_ddot();
        Y_ddot = retrieveY_ddot();
        Z_ddot = retrieveZ_ddot();

        tau_n = retrieveTauN();
        gamma_n = retrieveGammaN();


        //System.out.println(this.toString());


    }


    /**
     * Constructor without parameters
     */
    public GlonassEphemeris(){
        super();
        gnssSystem = GnssStatus.CONSTELLATION_GLONASS;
    }

    /**
     * Constructor for recopy of a Glonass ephemeris
     * @param eph
     */
    public GlonassEphemeris(GlonassEphemeris eph) {


        super(eph);
        setTb(eph.getTb());
        setTk(eph.getTk());

        setNt(eph.getNt());
        setN4(eph.getN4());

        computeGregorianDate();
        computeWn();

        setBn(eph.getBn());


        setX(eph.getX());
        setX_dot(eph.getX_dot());
        setX_ddot(eph.getX_ddot());

        setY(eph.getY());
        setY_dot(eph.getY_dot());
        setY_ddot(eph.getY_ddot());

        setZ(eph.getZ());
        setZ_dot(eph.getZ_dot());
        setZ_ddot(eph.getZ_ddot());

        setGamma_n(eph.getGamma_n());
        setTau_n(eph.getTau_n());

    }


    /**
     * Creates a copy of the Glonass ephemeris
     * @return
     */
    @Override
    public GlonassEphemeris copy(){
        return new GlonassEphemeris(this);
    }


    /**
     * computes the corresponding GPS week number of the message from the date of ephemeris
     * @return
     */
    public int computeWn() {

        GregorianCalendar six_jan_1980 = new GregorianCalendar(); // point zero of GPS weeks system
        six_jan_1980.set(1980,0,6,0,0);

        try {
            double diff = (date_of_ephemeris.getTimeInMillis() - six_jan_1980.getTimeInMillis()) / 1000;
            wn = (int) Math.floor(diff / (2 * Constants.SEC_IN_HALF_WEEK));
        }
        catch (Exception e)
        {
            Log.e("Glonass Week number", "Problem computing glonass GPS week number" + e);
            return -1;
        }

        return 0;
    }


    /**
     * Computes the gregorian date of ephemeris validity from the attributes Nt and N4
     * @return
     */
    public int computeGregorianDate() {

        GregorianCalendar date_of_validity = new GregorianCalendar();
        date_of_validity.clear();
        date_of_validity.setTimeZone(TimeZone.getTimeZone("GMT+3"));

        int J; // year number in the four-year interval
        int day_of_year;

        if (1<=Nt && Nt<=366){ J = 1; day_of_year = Nt;}
        else if (367<=Nt && Nt<=731){ J = 2; day_of_year = Nt-366;}
        else if (732<=Nt && Nt<=1096){ J = 3; day_of_year = Nt-731;}
        else if (1097<=Nt && Nt<=1461){ J = 4; day_of_year = Nt-1096;}

        else{
            // Nt is not transmitted so we create the date manually
            date_of_validity =  (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT+3"));
            date_of_ephemeris = preciseEphemerisDate(date_of_validity);

            return 0;
        }


        int year = 1996 + 4 * (N4 - 1) + (J - 1); // year of acquisition

        date_of_validity.set(date_of_validity.YEAR, year);
        date_of_validity.set(date_of_validity.DAY_OF_YEAR, day_of_year);

        date_of_ephemeris = preciseEphemerisDate(date_of_validity);
        return 0;
    }

    /**
     * Adds the time in the day of the date of emission from the parameter tb
     * @param date_of_validity
     * @return
     */
    public GregorianCalendar preciseEphemerisDate(GregorianCalendar date_of_validity) {

        // if date of ephemeris validity is of the previous day
        if (tb / 60 > 20 && tk / 3600 < 4) {
            date_of_validity.add(date_of_validity.DAY_OF_MONTH, - 1);
        }
        int h = (int) Math.floor(tb/60);
        int m = (int) (((float)tb/60 - h) * 60);
        date_of_validity.set(Calendar.HOUR_OF_DAY, h);
        date_of_validity.set(Calendar.MINUTE, m);
        date_of_validity.set(Calendar.SECOND, 0); // tb is in integer minutes

        return date_of_validity;
    }

    @Override
    public String toString(){

        String str = "Constellation : " + gnssSystem + "\n"
                + "prn : " + prn + "\n"
                + "Bn : " + Bn + "\n"
                + "Tb : " + tb + "\n"
                + "Tk : " + tk + "\n"
                + "Coordinates : " + x +", "+ y +", " + z + "\n"
                + "Velocity : " +  x_dot +", "+ y_dot +", " + z_dot + "\n"
                + "Acceleration : " + X_ddot +", "+ Y_ddot +", " + Z_ddot + "\n"
                + "tauN : " + tau_n + "\n"
                + "gammaN : " + gamma_n + "\n"
                + "Nt : " + Nt + "\n"
                + "N4 : " + N4 + "\n"
                + "Date of ephemeris: " + Utils.dateToString(date_of_ephemeris) +"\n"
                + "Wn : " + wn + "\n";


                // nice to check the correct decoding of nav msg
                /*+ "message 1 : " + getValueFromWord(this.subFrame1.getWord(81, 84), false,1) +"\n"
                + "message 2 : " + getValueFromWord(this.subFrame2.getWord(81, 84), false,1) +"\n"
                + "message 3 : " + getValueFromWord(this.subFrame3.getWord(81, 84), false,1) +"\n"
                + "message 4 : " + getValueFromWord(this.subFrame4.getWord(81, 84), false,1) +"\n"
                + "message 5 : " + getValueFromWord(this.subFrame5.getWord(81, 84), false,1) +"\n";*/

        return str;
    }


    /**
     * Gives the value corresponding to a binary word of the navigation message : see Glonass Interface Document (ICD) for mor info
     * @param word the complete binary word as specified in the ICD
     * @param sign_in_word if the sign of the value is part of the word as Most Significant bit (MSB)
     * @param scale_factor the scale factor to be applied to the value retrieve from binary word
     * @return
     */
    public double getValueFromWord(String word, Boolean sign_in_word, double scale_factor){

        int sign = 1;

        if (sign_in_word){ //some words consider the MSB as sign and other bits as value

            if (word.substring(0,1).equals("1")){ //MSB = 1 --> negative value
                sign = -1;
            }
            word = word.substring(1);
        }

        return toDecimalfromBinaryString(word, false)*scale_factor*sign;
    }


    //RETRIEVERS
    private int retrievePrn(){

        return (int)getValueFromWord(this.subFrame4.getWord(11, 15), false, 1);
    }

    private int retrieveNt(){
        return (int)getValueFromWord(this.subFrame4.getWord(16, 26), false, 1);
    }

    private int retrieveN4(){
        return (int)getValueFromWord(this.subFrame5.getWord(32, 36), false, 1);
    }

    private int retrieveBn(){
        return (int)getValueFromWord(this.subFrame2.getWord(80, 80), false, 1);
    }

    private int retrieveTb(){
        return (int)getValueFromWord(this.subFrame2.getWord(70, 76), false, 15);
    }

    private long retrieveTkInSec(){
        int hour = (int)getValueFromWord(this.subFrame1.getWord(65, 69), false, 1);
        int min = (int)getValueFromWord(this.subFrame1.getWord(70, 75), false, 1);
        int sec = (int)getValueFromWord(this.subFrame1.getWord(76, 76), false, 30);

        return (long)(hour * 3600 + min * 60 + sec);
    }

    private double retrieveX(){
        return getValueFromWord(this.subFrame1.getWord(9, 35), true, Constants.P2_11);
    }

    private double retrieveY(){
        return getValueFromWord(this.subFrame2.getWord(9, 35), true, Constants.P2_11);
    }

    private double retrieveZ(){
        return getValueFromWord(this.subFrame3.getWord(9, 35), true, Constants.P2_11);
    }

    private double retrieveXdot(){
        return getValueFromWord(this.subFrame1.getWord(41, 64), true, Constants.P2_20);
    }

    private double retrieveYdot(){
        return getValueFromWord(this.subFrame2.getWord(41, 64), true, Constants.P2_20);
    }

    private double retrieveZdot(){
        return getValueFromWord(this.subFrame3.getWord(41, 64), true, Constants.P2_20);
    }

    private double retrieveX_ddot(){
        return getValueFromWord(this.subFrame1.getWord(36, 40), true, Constants.P2_30);
    }

    private double retrieveY_ddot(){
        return getValueFromWord(this.subFrame2.getWord(36, 40), true, Constants.P2_30);
    }

    private double retrieveZ_ddot(){
        return getValueFromWord(this.subFrame3.getWord(36, 40), true, Constants.P2_30);
    }

    private double retrieveTauN(){
        return getValueFromWord(this.subFrame4.getWord(59, 80), true, Constants.P2_30);
    }

    private double retrieveGammaN(){
        return getValueFromWord(this.subFrame3.getWord(69, 79), true, Constants.P2_40);
    }

    //GETTERS

    public int getTb() {
        return tb;
    }

    public long getTk() {
        return tk;
    }

    public int getNt(){return Nt;}

    public int getN4(){return N4;}

    public int getBn(){return Bn;}

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getX_dot() {
        return x_dot;
    }

    public double getY_dot() {
        return y_dot;
    }

    public double getZ_dot() {
        return z_dot;
    }

    public double getX_ddot() {
        return X_ddot;
    }

    public double getY_ddot() {
        return Y_ddot;
    }

    public double getZ_ddot() {
        return Z_ddot;
    }

    public double getTau_n() {
        return tau_n;
    }

    public double getGamma_n() {
        return gamma_n;
    }

    public GregorianCalendar getDate(){
        return  date_of_ephemeris;
    }




    // SETTERS

    public  void setBn(int Bn){
        this.Bn = Bn;
    }

    public void setTb(int tb) {
        this.tb = tb;
    }

    public void setTk(long tk) {
        this.tk = tk;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setX_dot(double x_dot) {
        this.x_dot = x_dot;
    }

    public void setY_dot(double y_dot) {
        this.y_dot = y_dot;
    }

    public void setZ_dot(double z_dot) {
        this.z_dot = z_dot;
    }

    public void setX_ddot(double x_ddot) {
        X_ddot = x_ddot;
    }

    public void setY_ddot(double y_ddot) {
        Y_ddot = y_ddot;
    }

    public void setZ_ddot(double z_ddot) {
        Z_ddot = z_ddot;
    }

    public void setTau_n(double tau_n) {
        this.tau_n = tau_n;
    }

    public void setGamma_n(double gamma_n) {
        this.gamma_n = gamma_n;
    }

    public void setNt(int nt){
        this.Nt = nt;
    }

    public void setN4(int n4){
        this.N4 = n4;
    }

    public void setEphemerisDate(GregorianCalendar date){
        this.date_of_ephemeris = preciseEphemerisDate(date);
    }


}