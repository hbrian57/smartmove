package com.centrale.smartmove;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class User implements Savable{
    ArrayList<Week> weeks;
    ChallengeGenerator coach;


    ArrayList<Challenge> onGoingChallenge;
    ArrayList<Trip> userTrips;

    public User() {
        weeks = new ArrayList<>();
        coach = new ChallengeGenerator();
        onGoingChallenge = new ArrayList<>();
        userTrips = new ArrayList<>();
    }


    public ArrayList<Week> getWeeks() {
        return weeks;
    }
    public void updateChallenges(){
    }


    @Override
    public JSONObject getSaveFormat() {
        JSONObject JSONUser = new JSONObject();
        JSONArray JSONWeeks = new JSONArray();
        for (Week week : weeks) {
            JSONWeeks.put(week.getSaveFormat());
        }
        try {
            JSONUser.put("weeks", JSONWeeks);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JSONUser;
    }

    public void getNewChallenge(){
        Challenge challenge;
        challenge = coach.getRandomChallenge();
        onGoingChallenge.add(challenge);
    }

    public ArrayList<Challenge> getOnGoingChallenge() {
        return onGoingChallenge;
    }
}
