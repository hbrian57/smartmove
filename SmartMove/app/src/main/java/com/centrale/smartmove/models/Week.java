package com.centrale.smartmove.models;

import com.centrale.smartmove.R;
import com.centrale.smartmove.Savable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Week implements Savable {

    /**
     * Calendar object to compute Date & Time transformations.
     */
    Calendar calendar;

    /**
     * Vector with all the trips of that week
     */
    ArrayList<Trip> tripList;

    /** Constructor that creates a week based on a new trip
     * @param trip first trip of the new week
     */
    public Week(Trip trip) throws Exception {
        Date firstDate = trip.getFirstSegment().getFirstPosition().getDatePos();
        calendar.setTime(firstDate);
        tripList.add(trip);
    }

    /**
     * Constructor that creates a week based on a list of trips
     * @param trips the list of trips
     */
    public Week(ArrayList<Trip> trips){
        this.tripList=trips;
    }

    /**
     * method which enables to obtain the CO2 footprint of a whole trip
     * @return a double corresponding to the CO2 footprint
     */
    public double getTotalCO2Footprint() {
        double sumCO2Footprint = 0;
        for (Trip trip : tripList) {
            sumCO2Footprint += trip.getTripCO2Footprint();
        }
        return sumCO2Footprint;
    }

    /**
     * method that allows to give an id to a week with the number of the week in the year
     * and the current year
     * @return a string with the id of the week
     */
    public String getWeekID() {
        return calendar.get(Calendar.WEEK_OF_YEAR) + ":" + calendar.get(Calendar.YEAR);
    }

    /**
     * allows you to add a Trip object to the tripList
     * @param t is the Trip object we want to add
     */
    public void addNewTrip(Trip t){
        this.tripList.add(t);
    }

    /**
     * allows to save in JSON format directly on the device all the Trip in tripList
     * @return the JSON file of the backup
     */
    @Override
    public JSONObject getSaveFormat() {
        JSONObject JSONWeek = new JSONObject();
        JSONArray JSONTrips = new JSONArray();
        for (Trip trip : tripList) {
            JSONTrips.put(trip.getSaveFormat());
        }
        try {
            JSONWeek.put(String.valueOf(R.string.weekTrips), JSONTrips);
            JSONWeek.put(String.valueOf(R.string.weekWeekID), getWeekID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JSONWeek;
    }
}
