package com.centrale.smartmove;

public enum GoalType {
    EXEMPLE(10.2,20.3);
    private Double numberOfTrips;
    private Double distance;

    private GoalType(Double numberOfTrips, Double distance) {
        this.numberOfTrips = numberOfTrips;
        this.distance = distance;
    }
}
