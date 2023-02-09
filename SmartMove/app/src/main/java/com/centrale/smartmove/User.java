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


    public ArrayList<Week> getWeeks() {
        return weeks;
    }

    public double updateChallenges() {
        double total = 0;
        for (int k = 0; k <= this.onGoingChallenge.size(); k++) {
            Double progression;
            Double nbDefi;
            switch (this.getOnGoingChallenge().get(k).goal.getFormatedGoal()) {
                case "defiDistance":
                    total = 0;
                    Double distDefi = this.getOnGoingChallenge().get(k).goal.type.getDistance();
                    for (Trip t : this.getUserTrips()) {
                        for (TripSegment ts : t.getListOfTripSegments()) {
                            if (((ts.getFirstPosition().getDatePos()).after(this.getOnGoingChallenge().get(k).debutChallenge))
                                    || (ts.getTransportType() == this.getOnGoingChallenge().get(k).goal.getType().getTransportUsed())) {
                                total += ts.calculateTotalDistance();
                            }
                        }
                    }

                    break;
                case "defiNumerique":
                    total = 0;
                    nbDefi = this.getOnGoingChallenge().get(k).goal.type.getNumberOfTrips();
                    for (Trip t : this.getUserTrips()) {
                        if (t.getTripTransportType() == this.getOnGoingChallenge().get(k).goal.getType().getTransportUsed()) {
                            total += 1;
                        }
                    }

                    break;
                default:
                   throw new IllegalStateException("Unexpected value: " + this.getOnGoingChallenge().get(k).goal.getFormatedGoal());
            }
        }
        return total;
    }

    public void getNewChallenge() {
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


    public ArrayList<Challenge> getOnGoingChallenge() {
        return onGoingChallenge;
    }

    public ArrayList<Trip> getUserTrips() {
        return userTrips;
    }
}