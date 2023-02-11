package com.centrale.smartmove;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;


public class TripSegment implements Savable {
    /**
     * Mean of transport
     */
    TransportType transportType;
    /**
     * List of the positions taken during all the LitTrip
     */
    LinkedList<TimestampedPosition> timestampedPositionList;


    /**
     * Constructor of a a segment of Trip (a LitTrip) using all its attributes
     * @param transportTypeUsed mean of transport
     * @param timestampedPositions list of all the position in 2D
     */
    public TripSegment(TransportType transportTypeUsed, LinkedList<TimestampedPosition> timestampedPositions) {
        this.transportType = transportTypeUsed;
        this.timestampedPositionList = timestampedPositions;
    }

    /**
     * Method which calculates the total distance of the LitTrip
     * @return an integer corresponding to the total distance in m
     */
    public double calculateTotalDistance(){
        double totalDistance = 0;
        Iterator<TimestampedPosition> iteratorPositions = timestampedPositionList.iterator();
        TimestampedPosition initialPos = iteratorPositions.next();
        while (iteratorPositions.hasNext()) {
            totalDistance += initialPos.calculateDistance(iteratorPositions.next());
        }
        return totalDistance;
    }


    public TransportType getTransportType() {
        return transportType;
    }

    public LinkedList<TimestampedPosition> getPositionList() throws Exception {
        if(this.timestampedPositionList.isEmpty()){
            throw new Exception("timestampedPositionList vide lors de l'utilisation de la " +
                    "fonction getPositionList()");
        }
        return timestampedPositionList;
    }

    public TimestampedPosition getFirstPosition() throws Exception {
        if(this.timestampedPositionList.isEmpty()){
            throw new Exception("timestampedPositionList vide lors de l'utilisation de la " +
                    "fonction getFirstPosition()");
        }
        return timestampedPositionList.get(0);
    }

    /**
     * Method which enables to calculate the CO2 emission of a LitTrip
     * @return an integer corresponding to the value of the CO2 emission
     */
    public double calculateCO2footprint(){
        float transportTypeCO2Parameter = transportType.getCO2param();
        return transportTypeCO2Parameter*this.calculateTotalDistance();
    }

    @Override
    public JSONObject getSaveFormat() {
        JSONObject JSONTripSegment = new JSONObject();

        try {
            JSONTripSegment.put("transportType", transportType.name());
            JSONArray JSONPositionList = new JSONArray();
            for (TimestampedPosition pos : timestampedPositionList) {
                JSONPositionList.put(pos.getSaveFormat());
            }
            JSONTripSegment.put("positions", JSONPositionList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JSONTripSegment;
    }
}
