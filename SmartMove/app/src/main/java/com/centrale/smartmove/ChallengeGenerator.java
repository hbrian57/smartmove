package com.centrale.smartmove;
import java.util.Random;
import java.util.Date;

public class ChallengeGenerator {

    public Challenge getRandomChallenge(){
        Challenge challenge = null;
        // Goal
        Random random = new Random();
        int result = random.nextInt(2);// BOUND A CHANGER EN FONCTION DU NBR DES DEFIS
        if (result ==0){
            GoalType goalType = GoalType.EXEMPLE1;// A CHANGER EN FONCTION DES DEFIS
            challenge.getGoal().setType(goalType);
        }
        else {
            GoalType goalType2 =GoalType.EXEMPLE2;// A CHANGER EN FONCTION DES DEFIS
            challenge.getGoal().setType(goalType2);
        }
        challenge.getGoal().setGoalFinal(challenge.getGoal().getType().getNumberOfTrips());// A VERIFIER
        // progression
        challenge.setProgression(0.0);
        // Date
        Date today = new Date();
        challenge.setChallengeBeginnig(today);

        // Title A FAIRE EN FONCTION DES DEFIS
        challenge.setTitle("title");

        //Description A FAIRE EN FONCTION DES DEFIS
        challenge.setDescription("Description");

        return challenge;
    }
}


