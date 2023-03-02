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


    public String getFormatedGoal(){
        return null;
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
}
