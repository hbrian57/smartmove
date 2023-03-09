package com.centrale.smartmove;
import java.util.Random;
import java.util.Date;

public class ChallengeGenerator {

    public ChallengeGenerator() {
    }

    /**
     * Get a random challenge.
     * @return A random challenge.
     */
    public Challenge getRandomChallenge(){
        Challenge challenge = new Challenge();
        // Goal
        Random random = new Random();
        int result = random.nextInt(2);// BOUND A CHANGER EN FONCTION DU NBR DES DEFIS
        if (result ==0){
            GoalType goalType = GoalType.NUMBER_OF_TRIPS;// A CHANGER EN FONCTION DES DEFIS
            challenge.getGoal().setType(goalType);
        }
        else {
            GoalType goalType2 =GoalType.DISTANCE_COVERED;// A CHANGER EN FONCTION DES DEFIS
            challenge.getGoal().setType(goalType2);
        }
        challenge.getGoal().setGoalFinal(5.0);// A VERIFIER
        // progression
        challenge.setProgression(70.0);
        // Date
        Date today = new Date();
        challenge.setChallengeBeginning(today);

        // Title A FAIRE EN FONCTION DES DEFIS
        challenge.setTitle("Titre du défi");

        //Description A FAIRE EN FONCTION DES DEFIS
        challenge.setDescription("Description Du Défi");

        return challenge;
    }
}


