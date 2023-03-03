package com.centrale.smartmove;

public class ChallengeGoal {
    GoalType type;
    Double goalFinal;
    TransportType transportType;

    public ChallengeGoal(GoalType type, Double goalFinal, TransportType transportType) {
        this.type = type;
        this.goalFinal = goalFinal;
        this.transportType = transportType;
    }

    public ChallengeGoal() {
        this.type = GoalType.NUMBER_OF_TRIPS;
        this.goalFinal = 0.0;
        this.transportType = TransportType.WALKING;
    }


    public GoalType getType() {
        return type;
    }

    public Double getGoalFinal() {
        return goalFinal;
    }

    public void setType(GoalType type) {
        this.type = type;
    }

    public void setGoalFinal(Double goalFinal) {
        this.goalFinal = goalFinal;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    /**
     * Method which calculates the progression of the challenge that corresponds to the number of trips input
     * @param numberOfCorrespondingTrips number of trips corresponding to the goal
     * @return a double corresponding to the progression increment of the challenge
     */
    public Double calculateProgressionIncrementNumberOfTrips(Integer numberOfCorrespondingTrips) {
        return (double) numberOfCorrespondingTrips/goalFinal*100;
    }

    public Double calculateProgressionIncrementDistanceCovered(Double distanceCovered) {
        return distanceCovered/goalFinal*100;
    }
}
