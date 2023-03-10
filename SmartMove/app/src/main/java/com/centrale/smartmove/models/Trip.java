package com.centrale.smartmove.models;

import com.centrale.smartmove.R;
import com.centrale.smartmove.Savable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Trip implements Savable {



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
     * Get the first segment of the trip.
     * @return the first segment.
     * @throws Exception There is no segment in the trip.
     */
    public TripSegment getFirstSegment() throws Exception {
        if(this.tripSegments.isEmpty()){
            throw new Exception(String.valueOf(R.string.TripException));
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

    /**
     * Returns the number of trips of each type.
     * @return A HashMap with the transport type and the number of trips.
     */
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

    /**
     * Returns the distance done per transport type.
     * @return A HashMap with the transport type and the distance done.
     */
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
