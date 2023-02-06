package com.centrale.smartmove;

import java.util.Date;



public class Challenge {
    String title;
    String description;
    Double progression;
    ChallengeGoal goal;
    Date debutChallenge;

    public void notifyUser() {
    }

    public String getProgressionString() {
        switch (goal.getFormatedGoal()) {
            case 1:
                Double distDefi = goal.type.getDistance();
                double total = getProgressionDouble() * distDefi / 100;
                String progression = total + " parcourue sur " + distDefi;
                return progression;
                break;
            case 2:

        }
    }

        public Double getProgressionDouble() {
            switch (goal.getFormatedGoal()){
                case 1:
                    double total=0;
                    Double distDefi = goal.type.getDistance();
                    for (Trip t : User.getUserTrips()){
                        for (TripSegment ts : t.getListOfTripSegments()) {
                            if ( ((ts.getFirstPosition().getDatePos()).after(debutChallenge))
                                    || (ts.getTransportType() == goal.getType().getTransportUsed()) ){
                                total += ts.calculateTotalDistance();
                            }
                        }
                    }
                    Double progression = total*100/distDefi;
                    return progression;
                    break;
                case 2:
            }
        }

        public boolean isCompleted () {
            if (getProgressionDouble() < 100) {return false;}
            else{return true;}
        }

        public String getTitle () {
            return title;
        }

        public String getDescription () {
            return description;
        }

        public Double getProgression () {
            return progression;
        }

        public ChallengeGoal getGoal () {
            return goal;
        }

        public Date getDebutChallenge () {
            return debutChallenge;
        }
    }

