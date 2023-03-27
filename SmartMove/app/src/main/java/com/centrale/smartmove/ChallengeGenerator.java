package com.centrale.smartmove;
import com.centrale.smartmove.R;
import com.centrale.smartmove.models.Challenge;
import com.centrale.smartmove.models.Equivalent;
import com.centrale.smartmove.models.GoalType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Date;

public class ChallengeGenerator {

    public ChallengeGenerator() {
    }

    /**
     * Get a random challenge.
     * @return A random challenge.
     */
   /** public Challenge getRandomChallenge(){

        //Sélection du challenge
        Challenge challenge = new Challenge();
        ArrayList challenges = challenge.creationOfTheChallengeList();
        Random rand = new Random();
        int index = rand.nextInt(challenges.size());
        return (Challenge) challenges.get(index);


    // Goal
        int result = rand.nextInt(2); // BOUND A CHANGER EN FONCTION DU NBR DES DEFIS
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


        // Title A FAIRE EN FONCTION DES DEFIS
        challenge.setTitle(challenge.getTitle());

        //Description A FAIRE EN FONCTION DES DEFIS
        challenge.setShort_description(challenge.getShort_description());

        //Description A FAIRE EN FONCTION DES DEFIS
        challenge.setLong_description("long_description");

        return challenge;**/
    }
}


