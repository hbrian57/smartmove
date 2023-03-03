package com.centrale.smartmove;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static java.sql.Types.NULL;
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
     * Constructor without any position, with just the transport type used
     * @param transportTypeUsed mean of transport
     */
    public TripSegment(TransportType transportTypeUsed){
        this.transportType=transportTypeUsed;
    }


    /**
     * Constructor of a a segment of Trip (a LitTrip) using all its attributes
     * @param transportTypeUsed mean of transport
     * @param timestampedPositions list of all the position
     */
    public TripSegment(TransportType transportTypeUsed, LinkedList<TimestampedPosition> timestampedPositions) {
        this.transportType = transportTypeUsed;
        this.timestampedPositionList = timestampedPositions;
    }

    /**
     * Method which calculates the total distance of the LitTrip
     * @return an integer corresponding to the total distance in m
     */
    //TODO: faire l'exception, je ne sais pas laquelle c'est ? Je croyais c'était liste VIDE mais comme on pose totalDistance = 0 ca marche
    public double calculateTotalDistance() throws Exception {
        double totalDistance = 0;
        for (int i = 0; i < timestampedPositionList.size() - 1; i++) {
            totalDistance += timestampedPositionList.get(i).calculateDistance(timestampedPositionList.get(i + 1));
        }
        return totalDistance;
    }


    public TransportType getTransportType() {
        return transportType;
    }

    public LinkedList<TimestampedPosition> getPositionList() {
        return timestampedPositionList;
    }

    public TimestampedPosition getFirstPosition() {
        return timestampedPositionList.get(0);
    }

    /**
     * Method which enables to calculate the CO2 emission of a LitTrip
     * @return an integer corresponding to the value of the CO2 emission
     */
    public double calculateCO2footprint() throws Exception{
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
