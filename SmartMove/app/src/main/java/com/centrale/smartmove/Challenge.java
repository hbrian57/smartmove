package com.centrale.smartmove;

public class Challenge {
    String title;
    String description;
    Double progression;
    ChallengeGoal goal;

    public void notifyUser(){
    }

    public String getPorgressionString(){
        return null;
    }

    public Double getProgressionDouble(){
        return null;
    }

    public boolean isCompleted(){
        return false;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Double getProgression() {
        return progression;
    }

    public ChallengeGoal getGoal() {
        return goal;
    }
}
