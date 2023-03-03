package com.centrale.smartmove;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class User implements Savable {
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

    public User(ArrayList<Week> w, ChallengeGenerator c, ArrayList<Challenge> o, ArrayList<Trip> u){
        this.weeks=w ;
        this.coach=c;
        this.onGoingChallenge=o;
        this.userTrips=u;
    }


    public ArrayList<Week> getWeeks() {
        return weeks;
    }

    // Method that is called once a trip is finished that updates the challenges
    public void updateOnGoingChallenge(Trip newTripDone){
        for (Challenge challenge : onGoingChallenge) {
            challenge.updateProgression(newTripDone);
        }
    }

    public void addNewTrip(Trip trip)  {

        userTrips.add(trip);
        updateOnGoingChallenge(trip);
    }


    /**
     * method which allows to save all the Week of User in a JSON file
     * @return the JSON file of the backup
     */
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

    public ArrayList<Trip> getUserTrips() {
        return userTrips;
    }


}