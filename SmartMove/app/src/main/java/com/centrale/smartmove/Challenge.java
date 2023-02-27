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

    /**
     * method to display the progress of a Challenge as a sentence
     * @return the String that corresponds to the sentence that treads the progression
     */
    public String getProgressionString() {
        Double distDefi;
        Double total;
        String progression;
        int nbDefi;
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
                String nbDefiString = String.valueOf(nbDefi);
                total.toString();
                if(total>1){
                    progression = total + " trajets parcourue sur " + nbDefiString;
                }
                else{progression = total + " trajet parcourue sur " + nbDefiString;}
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + this.goal.getFormatedGoal());
        }
        return progression;
    }

    /**
     * method to display the progress of a Challenge as a Double
     * @return the Double that corresponds to the progression
     */
    public Double getProgressionDouble() {
            return this.progression ;
        }

    /**
     * method to know if a challenge is finished
     * @return a boolean which corresponds to the completed (true) or uncompleted (false)
     * state of the Challenge
     */
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


        Challenge(String Title,String Description, Double Progression,ChallengeGoal Goal,Date DebutChallenge){
        this.title=Title;
        this.description=Description;
        this.progression=Progression;
        this.goal=Goal;
        this.debutChallenge=DebutChallenge;
        }

    }

