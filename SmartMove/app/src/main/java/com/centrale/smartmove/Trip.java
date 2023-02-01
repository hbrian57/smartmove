package com.centrale.smartmove;

import java.util.ArrayList;

public class Trip {

    /**
     * Vector with all the little trips contained in the trip
     */
    ArrayList<TripSegment> tripSegments;

    /**
     * Constructor of a trip with all the little trips contained in this trip as a parameter
     * @param listSegments list of the little trips contained in this trip
     */
    public Trip(ArrayList<TripSegment> listSegments) {
        this.tripSegments = listSegments;
    }

    public ArrayList<TripSegment> getListOfTripSegments() {
        return tripSegments;
    }

    /**
     * Method which calculates the CO2 emission of a Trip
     * @return a double corresponding to the value of the CO2 emission
     */
    public double calculateCO2Footprint(Trip t){
        double sumCarbonFootprint = 0;
        for(TripSegment segment : tripSegments) {
            sumCarbonFootprint += segment.calculateCO2footprint();
        }
        return sumCarbonFootprint;
    }
}
