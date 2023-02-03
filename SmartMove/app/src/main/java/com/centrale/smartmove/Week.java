package com.centrale.smartmove;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Week implements Savable{

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
    public Week(Trip trip) {
        Date firstDate = trip.getFirstSegment().getFirstPosition().getDatePos();
        calendar.setTime(firstDate);
        tripList.add(trip);

    }

    public double getTotalCO2Footprint() {
        double sumCO2Footprint = 0;
        for (Trip trip : tripList) {
            sumCO2Footprint += trip.getTripCO2Footprint();
        }
        return sumCO2Footprint;
    }

    @Override
    public String getSaveFormat() {
        String saveFormat = "WEEK ";
        saveFormat += calendar.toString() + " {";
        for (Trip t:tripList) {
            saveFormat += "TRIP " + t.getSaveFormat();
        }
        return saveFormat;
    }
}
