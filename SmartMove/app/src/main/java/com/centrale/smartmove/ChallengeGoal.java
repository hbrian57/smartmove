package com.centrale.smartmove;

public class ChallengeGoal {
    GoalType type;
    Double goalFinal;

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
}
