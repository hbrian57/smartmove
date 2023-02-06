package com.centrale.smartmove;

public enum GoalType {
    EXEMPLE(10.2,23.4,TransportType.BIKE);
    private Double numberOfTrips;
    private Double distance;

    private TransportType transportUsed;


    private GoalType(Double numberOfTrips, Double distance,TransportType transportUsed) {
        this.numberOfTrips = numberOfTrips;
        this.distance = distance;
        this.transportUsed = transportUsed;
    }

    public Double getNumberOfTrips() {
        return numberOfTrips;
    }

    public Double getDistance() {
        return distance;
    }

    public TransportType getTransportUsed() {
        return transportUsed;
    }
}
