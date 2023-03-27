package com.centrale.smartmove;

import com.centrale.smartmove.models.Challenge;
import com.centrale.smartmove.models.GoalType;

import java.util.Random;

public class ChallengeGenerator {

    //Constructors----------------------------------------------------------------------------------

    /**
     * Constructor that takes no parameter
     */
    public ChallengeGenerator(){}

    //Methods---------------------------------------------------------------------------------------
    /**
     * Method that picks a random challenge.
     * @return Challenge challeng
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

        // Title A FAIRE EN FONCTION DES DEFIS
        challenge.setTitle(R.string.challenge_title);

        //Description A FAIRE EN FONCTION DES DEFIS
        challenge.setDescription(R.string.challenge_description);

        return challenge;
    }

    //----------------------------------------------------------------------------------------------
}


