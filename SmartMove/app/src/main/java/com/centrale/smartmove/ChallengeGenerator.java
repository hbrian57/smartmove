package com.centrale.smartmove;
import android.content.res.Resources;

import com.centrale.smartmove.R;
import com.centrale.smartmove.models.Challenge;
import com.centrale.smartmove.models.ChallengeGoal;
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
    ArrayList<Challenge> availableChallenges;

    public ChallengeGenerator(ArrayList<Challenge> challengeList) {
       availableChallenges = challengeList;
    }



    /**
     * Get a random challenge.
     * @return A random challenge.
     */
   /** public Challenge getRandomChallenge(){

    //Methods---------------------------------------------------------------------------------------
    /**
     * Method that picks a random challenge.
     * @return Challenge challeng
     */
    public Challenge getRandomChallenge(){
        Random rand = new Random();
        if (availableChallenges.size() == 0) {
            return new Challenge();
        }
        int index = rand.nextInt(availableChallenges.size());
        Challenge newChallenge = availableChallenges.get(index);
        newChallenge.getGoal().setGoalFinal(5.0); //à automatiser pour s'adapter à l'utilisateur, ici 5 peut correspondre à 5 km ou 5 trips
        return newChallenge;
    }
}


