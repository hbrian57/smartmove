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


    public ArrayList<Week> getWeeks() {
        return weeks;
    }

    /**
     * method that allows to update the progress of all the user's challenges according to his travels
     * @throws Exception
     */
//    public void updateChallenges() throws Exception {
//        double total = 0;
//        for (int k = 0; k < this.onGoingChallenge.size(); k++) {
//            Double progression;
//            int nbDefi;
//            switch (this.getOnGoingChallenge().get(k).goal.getFormatedGoal()) {
//                case "defiDistance":
//                    total = 0;
//                    Double distDefi = this.getOnGoingChallenge().get(k).goal.type.getDistance();
//                    for (Trip t : this.getUserTrips()) {
//                        for (TripSegment ts : t.getListOfTripSegments()) {
//                            if (((ts.getFirstPosition().getDatePos()).after(this.getOnGoingChallenge().get(k).debutChallenge))
//                                    && (ts.getTransportType() == this.getOnGoingChallenge().get(k).goal.getType().getTransportUsed())) {
//                                total += ts.calculateTotalDistance();
//                            }
//                        }
//                    }
//                    this.getOnGoingChallenge().get(k).progression = total * 100 / distDefi;
//                    break;
//                case "defiNumerique":
//                    total = 0;
//                    nbDefi = this.getOnGoingChallenge().get(k).goal.type.getNumberOfTrips();
//                    for (Trip t : this.getUserTrips()) {
//                        if (!t.getListOfTripSegments().isEmpty()) {
//                            if (t.getListOfTripSegments().get(0).getTransportType() == this.getOnGoingChallenge().get(k).goal.getType().getTransportUsed()){total += 1;}
//                            for (int j = 1; j < t.getListOfTripSegments().size(); j++) {
//                                if ((t.getListOfTripSegments().get(j).getTransportType() == this.getOnGoingChallenge().get(k).goal.getType().getTransportUsed()) &&
//                                        ( (t.getListOfTripSegments().get(j-1).getTransportType() != t.getListOfTripSegments().get(j).getTransportType()))){
//                                    total += 1;
//                                }
//                            }
//                        }
//                    }
//                    this.getOnGoingChallenge().get(k).progression = total * 100 / nbDefi;
//                    break;
//                default:
//                    throw new IllegalStateException("Unexpected value: " + this.getOnGoingChallenge().get(k).goal.getFormatedGoal());
//            }
//        }
//    }

    /**
     *  method that allows to update the progress of all the user's challenges according to his travels
     * @param trip : take the trip you want to take in acompt inn order to update challenges
     * @param user : take the user involve in the challenge you want to update
     * @throws Exception
     */
    public void updateChalllenges(Trip trip, User user) throws Exception {
        double total = 0;
        int nbDefi;
        double distDefi;
        for (int k = 0; k < this.onGoingChallenge.size(); k++) {
            total = 0;
            switch (this.getOnGoingChallenge().get(k).goal.getFormatedGoal()) {
                case "defiDistance":
                    distDefi = user.getOnGoingChallenge().get(k).goal.type.getDistance();
                    for (TripSegment ts : trip.getListOfTripSegments()) {
                        if (ts.getTransportType() == user.getOnGoingChallenge().get(k).goal.getType().getTransportUsed()) {
                            total += ts.calculateTotalDistance();
                        }
                    }
                user.getOnGoingChallenge().get(k).progression += total * 100 / distDefi;
                break;
                case "defiNumerique":
                    nbDefi = user.getOnGoingChallenge().get(k).goal.type.getNumberOfTrips();
                    TransportType tempTransport=null;
                    if(!trip.getListOfTripSegments().isEmpty()){tempTransport = trip.getListOfTripSegments().get(0).getTransportType();}
                    for (TripSegment ts : trip.getListOfTripSegments()) {
                        if( (ts.getTransportType() == user.getOnGoingChallenge().get(k).goal.getType().getTransportUsed()) && !(tempTransport == ts.getTransportType()) ){
                            total+=1;
                        }
                        tempTransport=ts.getTransportType();
                    }
                    if((!trip.getListOfTripSegments().isEmpty()) && (total==0)){
                        if(trip.getListOfTripSegments().get(0).getTransportType()==user.getOnGoingChallenge().get(k).goal.getType().getTransportUsed()){
                            total=1;
                        }
                    }
                user.getOnGoingChallenge().get(k).progression += total * 100 / nbDefi;
                break;
                default:
                    throw new IllegalStateException("Type de defi non reconnu");
            }
        }
    }

    public void addNewTrip(Trip trip, User user) throws Exception {
        user.userTrips.add(trip);
        updateChalllenges(trip,user);
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

    User(){
        this.weeks=null;
        this.coach=null;
        this.onGoingChallenge=null;
        this.userTrips=null;
    }

    User(ArrayList<Week> w, ChallengeGenerator c, ArrayList<Challenge> o, ArrayList<Trip> u){
        this.weeks=w ;
        this.coach=c;
        this.onGoingChallenge=o;
        this.userTrips=u;
    }

}