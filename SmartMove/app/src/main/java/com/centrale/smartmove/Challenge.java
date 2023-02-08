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
        Double distDefi;
        double total;
        String progression;
        Double nbDefi;
        switch (this.goal.getFormatedGoal()) {
            case "defiDistance":
                distDefi = this.goal.type.getDistance();
                total = this.getProgressionDouble() * distDefi / 100;
                progression = total + " parcourue sur " + distDefi;
            break;
            case "defiNumerique":
                nbDefi = this.goal.type.getNumberOfTrips();
                total = this.getProgressionDouble() * nbDefi / 100;
                if(total>1){
                    progression = total + " trajets parcourue sur " + nbDefi;
                }
                else{progression = total + " trajet parcourue sur " + nbDefi;}
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + this.goal.getFormatedGoal());
        }
        return progression;
    }

        public Double getProgressionDouble() {
            Double progression;
            double total;
            Double nbDefi;
            switch (this.goal.getFormatedGoal()){
                case "defiDistance":
                    total=0;
                    Double distDefi = this.goal.type.getDistance();
                    for (Trip t : User.getUserTrips()){
                        for (TripSegment ts : t.getListOfTripSegments()) {
                            if ( ((ts.getFirstPosition().getDatePos()).after(debutChallenge))
                                    || (ts.getTransportType() == this.goal.getType().getTransportUsed()) ){
                                total += ts.calculateTotalDistance();
                            }
                        }
                    }
                    progression = total*100/distDefi;
                break;
                case "defiNumerique":
                    total = 0;
                    nbDefi = this.goal.type.getNumberOfTrips();
                    for (Trip t : User.getUserTrips()){
                        if(t.getTripTransportType() == this.goal.getType().getTransportUsed()){
                            total += 1;
                        }
                    }
                    progression = total*100/nbDefi;
                break;
                default:
                    throw new IllegalStateException("Unexpected value: " + this.goal.getFormatedGoal());
            }
            return progression;
        }

        public boolean isCompleted () {
            if (this.getProgressionDouble() < 100) {return false;}
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

