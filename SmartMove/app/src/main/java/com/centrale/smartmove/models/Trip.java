package com.centrale.smartmove.models;

import com.centrale.smartmove.R;
import com.centrale.smartmove.Savable;

import org.gogpsproject.positioning.Time;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public class Trip implements Savable {



    /**
     * Vector with all the little trips contained in the trip
     */
    ArrayList<TripSegment> tripSegments;

    Week weekOfTrip;

    /**
     * Constructor of a trip with nothing in parameters
     */
    TripSegment currentTripSegment;

    public Trip(TimestampedPosition newPosition) {
        tripSegments = new ArrayList<>();
        tripSegments.add(new TripSegment(newPosition));
        currentTripSegment = tripSegments.get(0);
        weekOfTrip = new Week(newPosition.getTimestamp());
    }

    /**
     * Constructor of a trip with nothing in parameters
     */
    public Week getWeekOfTrip() {
        return weekOfTrip;
    }

    /**
     * Constructor of a trip with nothing in parameters
     */
    public void setWeekOfTrip(Week weekOfTrip) {
        this.weekOfTrip = weekOfTrip;
    }

    public Trip(TimestampedPosition firstPosition, Week weekOfTrip) {
        this.weekOfTrip = weekOfTrip;
        tripSegments = new ArrayList<>();
        tripSegments.add(new TripSegment(firstPosition));
    }

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

    //Method that adds a position to the current trip segment if it exists
    //if not, it creates a new trip segment and adds the position to it
    //if the current trip segments contains more than 60 positions, it creates a new trip segment and marks it as finished
    public void addPosition(TimestampedPosition newPosition) {
        if (currentTripSegment == null) {
            currentTripSegment = new TripSegment(newPosition);
            tripSegments.add(currentTripSegment);
        } else {
            if (currentTripSegment.getNumberOfPositions() > 60) {
                currentTripSegment.setFinished();
                currentTripSegment = new TripSegment(newPosition);
                tripSegments.add(currentTripSegment);
            } else {
                currentTripSegment.addPosition(newPosition);
            }
        }
    }

    //Method that checks all the tripSegments, compute their transportType and merges two following tripSegments if they have the same transportType
    public void mergeTripSegmentsOnTransportType() throws Exception {
        for (TripSegment segment : tripSegments) {
            segment.computeTransportType();
        }
        for (int i = 0; i < tripSegments.size() - 1; i++) {
            if (tripSegments.get(i).getTransportType() == tripSegments.get(i + 1).getTransportType()) {
                tripSegments.get(i).mergeWithSegment(tripSegments.get(i + 1));
                tripSegments.remove(i + 1);
                i--;
            }
        }
    }

    public int getNumberOfSegments() {
        return tripSegments.size();
    }

    public TripSegment getCurrentSegment() {
        return currentTripSegment;
    }


}
