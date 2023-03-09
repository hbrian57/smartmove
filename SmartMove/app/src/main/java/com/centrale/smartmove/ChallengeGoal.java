package com.centrale.smartmove;

public class ChallengeGoal {
    GoalType type;
    Double goalFinal;
    TransportType transportType;

    /**
     * Challenge Goal Constructor.
     * @param type
     * @param goalFinal
     * @param transportType
     */
    public ChallengeGoal(GoalType type, Double goalFinal, TransportType transportType) {
        this.type = type;
        this.goalFinal = goalFinal;
        this.transportType = transportType;
    }

    /**
     * Empty challenge Goal Constructor.
     */
    public ChallengeGoal() {
        this.type = GoalType.NUMBER_OF_TRIPS;
        this.goalFinal = 0.0;
        this.transportType = TransportType.WALKING;
    }

    /**
     * Get the type.
     * @return The Goal Type
     */
    public GoalType getType() {
        return type;
    }

    /**
     * Get the final Goal.
     * @return final Goal
     */
    public Double getGoalFinal() {
        return goalFinal;
    }

    /**
     * Set the type.
     * @param type
     */
    public void setType(GoalType type) {
        this.type = type;
    }

    /**
     * Set the final goal.
     * @param goalFinal
     */
    public void setGoalFinal(Double goalFinal) {
        this.goalFinal = goalFinal;
    }

    /**
     * Get the transport type.
     * @return the transport type.
     */
    public TransportType getTransportType() {
        return transportType;
    }

    /**
     * Set the transport Type
     * @param transportType the transport Type
     */
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

    /**
     *
     * Method which calculates the progression of the challenge that corresponds to distance covered
     * @param distanceCovered distance covered corresponding to the goal.
     * @return a double corresponding to the progression increment of the challenge
     */
    public Double calculateProgressionIncrementDistanceCovered(Double distanceCovered) {
        return distanceCovered/goalFinal*100;
    }
}
