package com.centrale.smartmove;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Trip implements Savable{



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

    public TripSegment getFirstSegment() throws Exception {
        if(this.tripSegments.isEmpty()){
            throw new Exception("The trip is empty and has no segments");
        }
            return tripSegments.get(0);
    }

    public TransportType getTripTransportType() throws Exception {return getFirstSegment().getTransportType();}

    /**
     * Method which calculates the CO2 emission of a Trip
     * @return a double corresponding to the value of the CO2 emission
     */

    public double getTripCO2Footprint()  {
        double sumCarbonFootprint = 0;
        for (TripSegment segment : tripSegments) {
            sumCarbonFootprint += segment.calculateCO2footprint();
        }
        return sumCarbonFootprint;
    }

    /**
     * allows to save in JSON format directly on the device all the tripSegment in Trip
     * @return the JSON file of the backup
     */
    @Override
    public JSONObject getSaveFormat() {
        JSONObject JSONTrip = new JSONObject();
        JSONArray JSONSegments = new JSONArray();
        for (TripSegment segment : tripSegments) {
            JSONSegments.put(segment.getSaveFormat());
        }
        try {
            JSONTrip.put("tripSegments", JSONSegments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JSONTrip;
    }

    //Returns the number of trips of each type
    public HashMap<TransportType, Integer> getTripTransportTypeUsage() {
        HashMap<TransportType, Integer> tripSegmentsUsage = new HashMap<>();
        for (TransportType transportType : TransportType.values()) {
            tripSegmentsUsage.put(transportType, 0);
        }
        for (TripSegment segment : tripSegments) {
            TransportType transportType = segment.getTransportType();
            tripSegmentsUsage.put(transportType, tripSegmentsUsage.get(transportType) + 1);
        }
        return tripSegmentsUsage;
    }

    public HashMap<TransportType, Double> getTripDistanceDonePerTransportType()  {
        HashMap<TransportType, Double> tripSegmentsDistance = new HashMap<>();
        for (TransportType transportType : TransportType.values()) {
            tripSegmentsDistance.put(transportType, 0.0);
        }
        for (TripSegment segment : tripSegments) {
            TransportType transportType = segment.getTransportType();
            tripSegmentsDistance.put(transportType, tripSegmentsDistance.get(transportType) + segment.calculateTotalDistance());
        }
        return tripSegmentsDistance;
    }
}
