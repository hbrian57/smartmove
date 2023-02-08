package com.centrale.smartmove;

public class ChallengeGoal {
    GoalType type;
    Double goalFinal;

    public String getFormatedGoal(){
        String defiDeType;
        if(type.getNumberOfTrips()<=0){
            defiDeType = "defiDistance";
            return defiDeType; }
        else{ defiDeType = "defiNumerique";
            return defiDeType;
        }
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
}
