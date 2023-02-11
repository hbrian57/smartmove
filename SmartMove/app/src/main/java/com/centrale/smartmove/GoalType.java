package com.centrale.smartmove;

public enum GoalType {
    EXEMPLE1(10.2,20.3),
    EXEMPLE2(2.4,5.0);
    private Double numberOfTrips;
    private Double distance;

    private GoalType(Double numberOfTrips, Double distance) {
        this.numberOfTrips = numberOfTrips;
        this.distance = distance;
    }

    public Double getNumberOfTrips() {
        return numberOfTrips;
    }
}
