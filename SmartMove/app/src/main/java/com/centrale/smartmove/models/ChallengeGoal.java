package com.centrale.smartmove.models;

import com.centrale.smartmove.Savable;

import org.json.JSONObject;

public class ChallengeGoal implements Savable {

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
    public ChallengeGoal(GoalType type, TransportType transportType) {
        super();
        this.type = type;
        this.transportType = transportType;
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

    @Override
    public JSONObject getSaveFormat() {
        JSONObject JSONGoal = new JSONObject();
        try {
            JSONGoal.put("type", type.toString());
            JSONGoal.put("goalFinal", goalFinal);
            JSONGoal.put("transportType", transportType.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSONGoal;
    }

    @Override
    public void loadFromSave(JSONObject saveFormat) {
        try {
            type = GoalType.valueOf(saveFormat.getString("type"));
            goalFinal = saveFormat.getDouble("goalFinal");
            transportType = TransportType.valueOf(saveFormat.getString("transportType"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------------------------

}
