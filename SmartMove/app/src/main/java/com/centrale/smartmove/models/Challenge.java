package com.centrale.smartmove.models;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.centrale.smartmove.R;
import com.centrale.smartmove.Savable;
import com.centrale.smartmove.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.runner.manipulation.Ordering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;


public class Challenge implements Savable {

    //Attributes------------------------------------------------------------------------------------
    /**
     *String corresponding to the identification of the title of the Challenge
     */
    private String title;

    /**
     *String corresponding to the identification of the short description of the Challenge
     */
    private String short_description;

    /**
     *string corresponding to the identification of the long description of the Challenge
     */
    private String long_description;
    /**
     *Double corresponding to the completion of a Challenge
     */
    private Double progression;

    /**
     *ChallengeGoal t
     */
    private ChallengeGoal goal;

    /**
     *Int corresponding to the identification of the equivalent image
     */
    private @DrawableRes int icon;

    //Getters and Setters---------------------------------------------------------------------------
    /**
     * Setter
     */
    public void setProgression(Double progression) {
        this.progression = progression;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShort_description() {
        return short_description;
    }

    public String getLong_description() {
        return long_description;
    }

    public void setLong_description(String long_description) {
        this.long_description = long_description;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    /**
     * Getter
     * @return Double progression
     */
    public Double getProgression () {
        return progression;
    }

    /**
     * Getter
     * @return ChallengeGoal goal
     */
    public ChallengeGoal getGoal () {
        return goal;
    }

    /**
     * Getter
     * @return int icon
     */
    public @DrawableRes int getIcon() {
        return icon;
    }

    /**
     * Get the progression in a double.
     * @return the progression
     */
    public Double getProgressionDouble() {
        return progression;
    }


    /**
     * Setter
     * @param goal the goal of the challenge
     */
    public void setGoal(ChallengeGoal goal) {
        this.goal = goal;
    }


    //Constructors----------------------------------------------------------------------------------
    /**
     * Challenge constructor.
     * @param title the title of the challenge
     * @param short_descrip the short description of the challenge
     * @param long_descrip the long description of the challenge
     * @param progression the progression of the challenge
     * @param goal the goal of the challenge
     * @param icon the icon of the challenge (resource id)
     */
    public Challenge( String title, String short_descrip, String long_descrip, Double progression, ChallengeGoal goal, int icon) {
        this.title = title;
        this.short_description = short_descrip;
        this.long_description = long_descrip;
        this.progression = progression;
        this.goal = goal;
        this.icon = icon;
    }

    /**
     * Challenge constructor.
     * @param title the title of the challenge
     * @param short_descrip the short description of the challenge
     * @param long_descrip the long description of the challenge
     */
    public Challenge( String title, String short_descrip, String long_descrip) {
        this();
        this.title = title;
        this.short_description = short_descrip;
        this.long_description = long_descrip;

    }

    /**
     * Constructor with no parameter
     */
    public Challenge() {
        this.title = "Challenge";
        this.short_description = "Challenge description";
        this.progression = 0.0;
        this.goal = new ChallengeGoal();
        this.icon = R.drawable.travel;
    }
    //
    //Methods---------------------------------------------------------------------------------------

    /**
     * Method that gets the progression in a string, like a toString() method
     * @return a String corresponding to the progression
     */
    public String getProgressionString() {
        //depending on the goalType, the progression is displayed differently
        Double progressionReality = progression / 100 * goal.getGoalFinal();
        switch (goal.getType()) {
            case NUMBER_OF_TRIPS:
                return progressionReality.intValue() + "/" + goal.getGoalFinal().intValue() + " trajets";
            case DISTANCE_COVERED:
                return progressionReality + "/" + goal.getGoalFinal() + "km";
            default:
                return "error";
        }
    }

    /**
     * Method that updates the progression of the challenge based on the new trip done
     * @param newTripDone, a Trip
     */
    public void updateProgression(Trip newTripDone) {
        //depending on the goalType, the progression is updated differently
        TransportType transportTypeNeededForChallenge = goal.getTransportType();
        switch (goal.getType()) {
            case NUMBER_OF_TRIPS:
                HashMap<TransportType, Integer> numberOfTripSegmentsPerTransportType = newTripDone.getTripTransportTypeUsage();
                progression += goal.calculateProgressionIncrementNumberOfTrips(numberOfTripSegmentsPerTransportType.get(transportTypeNeededForChallenge));
                break;
            case DISTANCE_COVERED:
                HashMap<TransportType, Double> distanceCoveredByTransportType = newTripDone.getTripDistanceDonePerTransportType();
                progression += goal.calculateProgressionIncrementDistanceCovered(distanceCoveredByTransportType.get(transportTypeNeededForChallenge));
                break;
            default:
                break;
        }
        boolean challengeAccomplished = isCompleted();
        if (challengeAccomplished) {
            notifyUser();
        }
    }

    public boolean isCompleted(){
        if(this.progression<100){
            return false;
        }else{return true;}
    }



    public void notifyUser() {
        Context context = MainActivity.getContext();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.ChannelID));
        builder.setContentTitle(context.getString(R.string.notification_accomplishedchall));
        builder.setContentText(context.getString(R.string.notification_congratulations));
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        }
    }

    @Override
    public JSONObject getSaveFormat() {
        JSONObject JSONChallenge = new JSONObject();
        try {
            JSONChallenge.put("title", title);
            JSONChallenge.put("short_description", short_description);
            JSONChallenge.put("long_description", long_description);
            JSONChallenge.put("progression", progression);
            JSONChallenge.put("goal", goal.getSaveFormat());
            JSONChallenge.put("icon", icon);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JSONChallenge;
    }

    @Override
    public void loadFromSave(JSONObject saveFormat) {
        try {
            this.title = saveFormat.getString("title");
            this.short_description = saveFormat.getString("short_description");
            this.long_description = saveFormat.getString("long_description");
            this.progression = saveFormat.getDouble("progression");
            this.goal = new ChallengeGoal();
            this.goal.loadFromSave(saveFormat.getJSONObject("goal"));
            this.icon = saveFormat.getInt("icon");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setGoalType(GoalType goalType) {
        this.goal.setType(goalType);
    }

    public void setTransportType(TransportType transportType) {
        this.goal.setTransportType(transportType);
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}