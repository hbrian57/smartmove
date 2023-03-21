package com.centrale.smartmove.models;

import com.centrale.smartmove.ChallengeGenerator;
import com.centrale.smartmove.*;
import com.centrale.smartmove.Savable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;


public class User implements Savable {

    //Attributes------------------------------------------------------------------------------------
    /**
     * ArrayList<Week> with all the weeks contained in the User
     */
    private final ArrayList<Week> weeks;

    /**
     * ChallengeGenerator
     */
    private final ChallengeGenerator coach;

    /**
     * ArrayList<Challenge> that contains all the ongoing challenges
     */
    private final ArrayList<Challenge> onGoingChallenge;

    /**
     * ArrayList<Trip> that contains all the Trips the User made
     */
    private final ArrayList<Trip> userTrips;

    /**
     * Trip, the last Trip
     */
    private Trip currentTrip;

    /**
     * TimestampedPosition that is the last position obtained
     */
    private TimestampedPosition lastPositionObtained;

    /**
     * Boolean to determine the forcing of a creation of a new Trip
     */
    private boolean forceNewTrip = false;

    //Getters and Setters---------------------------------------------------------------------------

    /**
     * Getter
     * @return ArrayList<Week> weeks
     */
    public ArrayList<Week> getWeeks() {
        return weeks;
    }

    /**
     * Getter
     * @return Trip currentTrip
     */
    public Trip getCurrentTrip() {
        return currentTrip;
    }

    /**
     * Getter
     * @return ArrayList<Challenge> onGoingChallenge
     */
    public ArrayList<Challenge> getOnGoingChallenge() {
        return onGoingChallenge;
    }

    //Constructors----------------------------------------------------------------------------------
    /**
     * Constructor of a User with nothing in parameters
     */
    public User() {
        weeks = new ArrayList<>();
        coach = new ChallengeGenerator();
        onGoingChallenge = new ArrayList<>();
        userTrips = new ArrayList<>();
        lastPositionObtained = null;
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
        currentTrip = null;
    }

    //Methods---------------------------------------------------------------------------------------
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
     * @param trip, a Trip
     */
    public void addNewTrip(Trip trip)  {
        userTrips.add(trip);
        updateOnGoingChallenge(trip);
        Timestamp dateSemaine = trip.getTripSegments().get(0).getPositionList().getFirst().getTimestamp();
        for(Week w : weeks){
            if(w.isInWeek(new Date(dateSemaine.getTime()))){
                trip.setWeekOfTrip(w);
            }
        }
    }


    /**
     * Method which allows to save all the Week of User in a JSON file
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
     * Method that gives a new challenge to the User
     */
    public void getNewChallenge(){
        Challenge challenge;
        challenge = coach.getRandomChallenge();
        onGoingChallenge.add(challenge);
    }

    /**
     * Methode to get all the trips of the User.
     * @return the Array list of the trips.
     */
    public ArrayList<Trip> getUserTrips() {
        return userTrips;
    }

    /**
     * Method that returns the current week, if there is no current week, it creates a new one and adds it to the list of weeks
     * @return the current week
     */
    public Week getCurrentWeek() {
        Date today = new Date();
        for (Week week : weeks) {
            //if the week contains the date of today
            if (week.isInWeek(today)) {
                //return the week
                return week;
            }
        }
        //if there is no week containing the date of today, create a new week
        Week newWeek = new Week(new Timestamp(System.currentTimeMillis()));
        //add the new week to the list of weeks
        weeks.add(newWeek);
        //return the new week
        return newWeek;
    }

    /**
     * Method that will be launched each time we get a new position
     * The method should check if the user is in a trip or not (based on lastpositionobtained timestamp) (5min with no position = end of trip)
     * If the user is in a trip, it should add the new position to the trip
     * If the user is not in a trip, it should create a new trip and add the new position to it
     */
    public void newPositionObtained(TimestampedPosition newPosition) throws Exception {
        //If the user is in a trip
        if (lastPositionObtained != null && currentTrip != null && !forceNewTrip) {
            //If the user is still in the same trip
            if (newPosition.getTimestamp().getTime() - lastPositionObtained.getTimestamp().getTime() < 5 * 60 * 1000) {
                //Add the new position to the current trip
                currentTrip.addPosition(newPosition);
            }
            else {
                onTripFinished(currentTrip);
                //Create a new trip Add the new position to the new trip
                Trip newTrip = new Trip(newPosition);
                //Add the new trip to the list of trips
                userTrips.add(newTrip);
                //Update the current trip
                currentTrip = newTrip;

            }
        }
        //If the user is not in a trip or a new trip is forced
        else {
            //Create a new trip Add the new position to the new trip
            Trip newTrip = new Trip(newPosition);
            //Add the new trip to the list of trips
            userTrips.add(newTrip);
            //Update the current trip
            currentTrip = newTrip;
            //Reset the forceNewTrip variable
            forceNewTrip = false;
        }
        //Update the last position obtained
        lastPositionObtained = newPosition;
    }

    /**
     * Method that adds the information of the last Trip to User, to update the CO2 footprint and the challenges
     * @param currentTrip, a Trip
     */
    private void onTripFinished(Trip currentTrip) {
        //Update the challenges
        updateOnGoingChallenge(currentTrip);
        //calculer le CO2 et l'ajouter dans la week du Trip
        currentTrip.getWeek().addCarbonFootprint(currentTrip.getTotalCarbonFootprint());
    }

    /**
     * Method that sets boolean forceNewTrip on true
     */
    public void forceNewTrip() {
        forceNewTrip = true;
    }

    //----------------------------------------------------------------------------------------------
}