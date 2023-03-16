package com.centrale.smartmove.models;

import com.centrale.smartmove.Savable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Trip  implements Savable {

    //Attributes------------------------------------------------------------------------------------
    /**
     * Array list with all the TripSegment contained in the trip
     */
    private ArrayList<TripSegment> listOfTripSegments;

    /**
     * Week in which the trip takes place
     */
    private Week weekOfTrip;

    /**
     * TripSegment that is the current one
     */
    private TripSegment currentTripSegment;

    //Constructors----------------------------------------------------------------------------------
    /**
     * Constructor of Trip which uses a new TimestampedPosition to generate a Trip
     * @param newPosition, a new TimestampedPosition
     */
    public Trip(TimestampedPosition newPosition) {
        listOfTripSegments = new ArrayList<>();
        listOfTripSegments.add(new TripSegment(newPosition));
        currentTripSegment = listOfTripSegments.get(0);
        weekOfTrip = new Week(newPosition.getTimestamp());
    }

    /**
     * Constructor of a trip with the list of the TripSegment contained in this trip as a parameter
     * @param listSegments list of the TripSegment contained in this trip
     */
    public Trip(ArrayList<TripSegment> listSegments) {
        this.listOfTripSegments = listSegments;
    }

    //Getters and Setters---------------------------------------------------------------------------

    /**
     * Getter
     * @return  ArrayList<TripSegment> listOfTripSegments
     */
    public ArrayList<TripSegment> getTripSegments() {
        return listOfTripSegments;
    }

    /**
     * Getter
     * @return Week weekOfTrip
     */
    public Week getWeek() {
        return weekOfTrip;
    }

    /**
     * Getter
     * @return TripSegment currentTripSegment
     */
    public TripSegment getCurrentSegment() {
        return currentTripSegment;
    }

    /**
     * Setter
     * @param weekOfTrip the week in which the Trip takes place
     */
    public void setWeekOfTrip(Week weekOfTrip) {
        this.weekOfTrip = weekOfTrip;
    }

    //Methods---------------------------------------------------------------------------------------
    /**
     * Method which calculates the CO2 emission of a Trip
     * @return a double corresponding to the value of the CO2 emission
     */
    public double getTripCO2Footprint()  {
        double sumCarbonFootprint = 0;
        for (TripSegment segment : listOfTripSegments) {
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
        for (TripSegment segment : listOfTripSegments) {
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
        for (TripSegment segment : listOfTripSegments) {
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
        for (TripSegment segment : listOfTripSegments) {
            TransportType transportType = segment.getTransportType();
            tripSegmentsDistance.put(transportType, tripSegmentsDistance.get(transportType) + segment.calculateTotalDistance());
        }
        return tripSegmentsDistance;
    }

    /**Method that adds a position to the current trip segment if it exists
     *if not, it creates a new trip segment and adds the position to it
     *if the current trip segments contains more than 60 positions, it creates a new trip segment and marks it as finished
     */
    public void addPosition(TimestampedPosition newPosition) throws Exception {
        if (currentTripSegment == null) {
            currentTripSegment = new TripSegment(newPosition);
            listOfTripSegments.add(currentTripSegment);
            if(currentTripSegment.getNumberOfPositions() >=3) {
                currentTripSegment.computeTransportType();
            }
        } else {
            if (currentTripSegment.getNumberOfPositions() >= 60) {
                currentTripSegment.setFinished();
                currentTripSegment = new TripSegment(newPosition);
                listOfTripSegments.add(currentTripSegment);
            } else {
                currentTripSegment.addPosition(newPosition);
            }
        }
    }

    /**
     * Method that checks all the tripSegments, compute their transportType and merges two following tripSegments if they have the same transportType
     * @throws Exception if there is no TripSgment
     */
    public void mergeTripSegmentsOnTransportType() throws Exception {
        for (TripSegment segment : listOfTripSegments) {
            segment.computeTransportType();
        }
        for (int i = 0; i < listOfTripSegments.size() - 1; i++) {
            if (listOfTripSegments.get(i).getTransportType() == listOfTripSegments.get(i + 1).getTransportType()) {
                listOfTripSegments.get(i).mergeWithSegment(listOfTripSegments.get(i + 1));
                listOfTripSegments.remove(i + 1);
                i--;
            }
        }
    }

    /**
     * Method that returns the number of TripSegments in a Trip
     * @return an integer, the number of TripSegment in a Trip
     * */
    public int getNumberOfSegments() {
        return listOfTripSegments.size();
    }


    /**
     * Method that calculates the CO2 sum of a Trip
     * @return double totalCarbonFootprint
     */
    public double getTotalCarbonFootprint() {
        double totalCarbonFootprint = 0;
        for (TripSegment segment : listOfTripSegments) {
            totalCarbonFootprint += segment.calculateCO2footprint();
        }
        return totalCarbonFootprint;
    }


}
