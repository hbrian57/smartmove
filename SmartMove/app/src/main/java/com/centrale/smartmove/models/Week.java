package com.centrale.smartmove.models;

import com.centrale.smartmove.R;
import com.centrale.smartmove.Savable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Week implements Savable {

    //Attributes------------------------------------------------------------------------------------
    /**
     * Calendar object to compute Date & Time transformations.
     */
    private Calendar calendar;

    /**
     * Double that corresponds to the total of CO2 produced in Week
     */
    private double carbonFootprint;

    //Getters and Setters---------------------------------------------------------------------------

    /**
     * Getter
     * @return double carbonFootprint
     */
    public double getCarbonFootprint() {
        return carbonFootprint;
    }

    /**
     * Sets double carbonFootprint on the parameter
     * @param carbonFootprint, a double
     */
    public void setCarbonFootprint(double carbonFootprint) {
        this.carbonFootprint = carbonFootprint;
    }


    //Constructors----------------------------------------------------------------------------------
    /** Constructor that creates a week based on a date of a day in the week.
     * @param dayInWeek, a Timestamp
     */
    public Week(Timestamp dayInWeek) {

   calendar = Calendar.getInstance();
   calendar.setTimeInMillis(dayInWeek.getTime());
        //set the calendar to the first day of the week
    calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
    }


    //Methods---------------------------------------------------------------------------------------
    /**
     * Method that adds the parameter to the total of carbonFootprint
     * @param newCarbonFootprintToAdd, a double
     */
    public void addCarbonFootprint(double newCarbonFootprintToAdd) {
        this.carbonFootprint += newCarbonFootprintToAdd;
    }

    /**
     * Method that allows to give an id to a week with the number of the week in the year
     * and the current year
     * @return a string with the id of the week
     */
    public String getWeekID() {
        return calendar.get(Calendar.WEEK_OF_YEAR) + ":" + calendar.get(Calendar.YEAR);
    }


    /**
     * Method that allows to know if a date is in the week
     * @param date the date to check
     * @return a boolean which is true if the date is in the week
     */
    public boolean isInWeek(Date date){
        //get the number of days between the date and the first day of the week
        int daysBetween = (int) ((date.getTime() - calendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
        //if the number of days is between 0 and 6, the date is in the week
        return daysBetween >= 0 && daysBetween <= 6;
    }


    /**
     * Method that allows to save in JSON format
     * @return the JSON file of the week
     */
    @Override
    public JSONObject getSaveFormat() {
        JSONObject JSONWeek = new JSONObject();
        try {
            JSONWeek.put(String.valueOf(R.string.weekWeekID), getWeekID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JSONWeek;
    }
}
