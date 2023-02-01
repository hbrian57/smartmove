package com.centrale.smartmove;


import java.util.List;

public class TripSegment {
    /**
     * Mean of transport
     */
    TransportType transportType;
    /**
     * List of the positions taken during all the LitTrip
     */
    List<Position> positionList;
    /**
     * Total distance of the LitTrip
     */
    int distanceTotal;

    /**
     * Constructor of a a segment of Trip (a LitTrip) using all its attributes
     * @param transportTypeUsed mean of transport
     * @param positions list of all the position in 2D
     * @param distanceTotal total distance of the littrip
     */
    public TripSegment(TransportType transportTypeUsed, List<Position> positions, int distanceTotal) {
        this.transportType = transportTypeUsed;
        this.positionList = positions;
        this.distanceTotal = distanceTotal;
    }

    /**
     * Method which calculates the total distance of the LitTrip
     * @return an integer corresponding to the total distance in m
     */
    public int calculateTotalDistance(){
    return distanceTotal;
    }


    public void setPositionList(List<Position> positionList) {
        this.positionList = positionList;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public List<Position> getPositionList() {
        return positionList;
    }

    public int getDistanceTotal() {
        return distanceTotal;
    }

    /**
     * Method which enables to calculate the CO2 emission of a LitTrip
     * @return an integer corresponding to the value of the CO2 emission
     */
    public double calculateCO2footprint(){
        float transportTypeCO2Parameter = transportType.getCO2param();
        return transportTypeCO2Parameter*this.getDistanceTotal();
    }

}
