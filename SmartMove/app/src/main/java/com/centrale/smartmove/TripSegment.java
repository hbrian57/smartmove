package com.centrale.smartmove;


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
    public double calculateCO2footprint(){
        float transportTypeCO2Parameter = transportType.getCO2param();
        return transportTypeCO2Parameter*this.calculateTotalDistance();
    }

    @Override
    public String getSaveFormat() {
        return null; //TODO
    }
}
