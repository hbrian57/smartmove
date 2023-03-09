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


    /**
     * Constructor of a User with nothing in parameters
     */
    public User() {
        weeks = new ArrayList<>();
        coach = new ChallengeGenerator();
        onGoingChallenge = new ArrayList<>();
        userTrips = new ArrayList<>();
    }

    /**
     * Constructor of a User with all the parameters
     * @param w the list of the weeks
     * @param c coach which generate the challenge
     * @param o list of challenges which are currently ongoing
     * @param u the list of the trips
     */
    public User(ArrayList<Week> w, ChallengeGenerator c, ArrayList<Challenge> o, ArrayList<Trip> u){
        this.weeks=w ;
        this.coach=c;
        this.onGoingChallenge=o;
        this.userTrips=u;
    }


    public ArrayList<Week> getWeeks() {
        return weeks;
    }

    /**
     * Method that is called once a trip is finished that updates the challenges
     */
    public void updateOnGoingChallenge(Trip newTripDone){
        for (Challenge challenge : onGoingChallenge) {
            challenge.updateProgression(newTripDone);
        }
    }

    /**
     * Method that adds a trip to the list of the trip, and which updates the challenge according to the new trip
     * @param trip
     */
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
            JSONUser.put(String.valueOf(R.string.userWeeks), JSONWeeks);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JSONUser;
    }

    /**
     * Give a new challenge to the User.
     */
    public void getNewChallenge(){
        Challenge challenge;
        challenge = coach.getRandomChallenge();
        onGoingChallenge.add(challenge);
    }

    /**
     * Methode to get all the onGoing challenges.
     * @return theArrayList of the ongoing challenges
     */
    public ArrayList<Challenge> getOnGoingChallenge() {
        return onGoingChallenge;
    }

    /**
     * Methode to get all the trips of the User.
     * @return the Array list of the trips.
     */
    public ArrayList<Trip> getUserTrips() {
        return userTrips;
    }

    /**
     * method that calculate the current week carbon footprint
     * @return the total co2 footprint
     */
    public Double calculateCurrentWeekCarbonFootprint() {
        Week currentWeek = weeks.get(weeks.size()-1);
        return currentWeek.getTotalCO2Footprint();

    }

    public Trip getCurrentTrip(){
        Trip currentTrip = this.userTrips.get(-1);
        return currentTrip;
    }

    public void newTSPobtained(TimestampedPosition currentTimeStampedPosition) throws Exception{
    }

}