package com.centrale.smartmove;

import java.util.Date;
import java.lang.Double;


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
        Double total;
        String progression;
        Double nbDefi;
        switch (this.goal.getFormatedGoal()) {
            case "defiDistance":
                distDefi = this.goal.type.getDistance();
                total = this.getProgressionDouble() * distDefi / 100;
                distDefi.toString();
                total.toString();
                progression = total + " parcourue sur " + distDefi;
            break;
            case "defiNumerique":
                nbDefi = this.goal.type.getNumberOfTrips();
                total = this.getProgressionDouble() * nbDefi / 100;
                nbDefi.toString();
                total.toString();
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

