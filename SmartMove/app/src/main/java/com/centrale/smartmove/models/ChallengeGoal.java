package com.centrale.smartmove.models;

public class ChallengeGoal {

    //Attributes------------------------------------------------------------------------------------
    /**
     * GoalType
     */
    private GoalType type;

    /**
     * Double that is associated with the objective of the ChallengeGoal
     */
    private Double goalFinal;

    /**
     * TransportType that is associated with the objective of the ChallengeGoal
     */
    private TransportType transportType;

    //Getters and Setters---------------------------------------------------------------------------
    /**
     * Getter
     * @return GoalType type
     */
    public GoalType getType() {return type;}

    /**
     * Get the final Goal.
     * @return final Goal
     */
    public Double getGoalFinal() {
        return goalFinal;
    }

    /**
     * Setter
     * @param type, a GoalType
     */
    public void setType(GoalType type) {
        this.type = type;
    }

    /**
     * Setter
     * @param goalFinal, a Double
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
     * Setter
     * @param transportType, a TransportType
     */
    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }
    //Constructors----------------------------------------------------------------------------------
    /**
     * Constructor with no parameter
     */
    public ChallengeGoal() {
        this.type = GoalType.NUMBER_OF_TRIPS;
        this.goalFinal = 0.0;
        this.transportType = TransportType.WALKING;
    }

    //Methods---------------------------------------------------------------------------------------
    /**
     * Method that calculates the progression of the challenge that corresponds to the number of trips input
     * @param numberOfCorrespondingTrips, an int that corresponds to the number of trips corresponding to the goal
     * @return a double corresponding to the progression increment of the challenge
     */
    public Double calculateProgressionIncrementNumberOfTrips(Integer numberOfCorrespondingTrips) {
        return (double) numberOfCorrespondingTrips/goalFinal*100;
    }

    /**
     * Method that calculates the progression of the challenge that corresponds to distance covered
     * @param distanceCovered, a Double that corresponds to the distance covered in link with to the goal.
     * @return a double corresponding to the progression increment of the challenge
     */
    public Double calculateProgressionIncrementDistanceCovered(Double distanceCovered) {
        return distanceCovered/goalFinal*100;
    }

    //----------------------------------------------------------------------------------------------

}
