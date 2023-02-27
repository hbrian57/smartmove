package com.centrale.smartmove;

public enum GoalType {
    EXEMPLE(0,23.0,TransportType.FOOT);
    int numberOfTrips;
    private Double distance;

    private TransportType transportUsed;


    private GoalType(int numberOfTrips, Double distance,TransportType transportUsed) {
        this.numberOfTrips = numberOfTrips;
        this.distance = distance;
        this.transportUsed = transportUsed;
    }

    public int getNumberOfTrips() {
        return numberOfTrips;
    }

    public Double getDistance() {
        return distance;
    }

    public TransportType getTransportUsed() {
        return transportUsed;
    }
}
