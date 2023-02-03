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


    public ArrayList<Week> getWeeks() {
        return weeks;
    }
    public void updateChallenges(){
    }
    public void getNewChallenge(){
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

}
