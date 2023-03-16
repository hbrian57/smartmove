package com.centrale.smartmove.models;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.centrale.smartmove.R;

import java.util.Date;
import java.util.HashMap;


public class Challenge {

    //Attributes------------------------------------------------------------------------------------
    /**
     *Int corresponding to the identification of the title of the Challenge
     */
    private @StringRes int title;

    /**
     *Int corresponding to the identification of the description of the Challenge
     */
    private @StringRes int description;

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

    /**
     * Getter
     * @return int title
     */
    public @StringRes int getTitle () {
        return title;
    }

    /**
     * Getter
     * @return int description
     */
    public @StringRes int getDescription () {
        return description;
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
     * Seter
     * @param title the title of the challenge
     */
    public void setTitle(@StringRes int title) {
        this.title = title;
    }

    /**
     * Setter
     * @param description the description of the challenge
     */
    public void setDescription(@StringRes int description) {
        this.description = description;
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
     * @param description the description of the challenge
     * @param progression the progression of the challenge
     * @param goal the goal of the challenge
     * @param icon the icon of the challenge (resource id)
     */
    public Challenge(@StringRes int title, @StringRes int description, Double progression, ChallengeGoal goal, int icon) {
        this.title = title;
        this.description = description;
        this.progression = progression;
        this.goal = goal;
        this.icon = icon;
    }

    /**
     * Constructor with no parameter
     */
    public Challenge() {
        this.title = R.string.challenge_title;
        this.description = R.string.challenge_description;
        this.progression = 0.0;
        this.goal = new ChallengeGoal();
        this.icon = R.drawable.travel;
    }
    //Methods---------------------------------------------------------------------------------------
    /**
     * Method that gets the progression in a string, like a toString() method
     * @return a String corresponding to the progression
     */
    public String getProgressionString() {
        //depending on the goalType, the progression is displayed differently
        Double progressionReality = progression/100*goal.getGoalFinal();
        switch (goal.getType()) {
            case NUMBER_OF_TRIPS:
                return progressionReality.intValue() + "/" + goal.getGoalFinal().intValue() + "trips";
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
                HashMap<TransportType,Double> distanceCoveredByTransportType = newTripDone.getTripDistanceDonePerTransportType();
                progression += goal.calculateProgressionIncrementDistanceCovered(distanceCoveredByTransportType.get(transportTypeNeededForChallenge));
                break;
            default:
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    //    /**
//     * Creation of the notification channel.
//     * @param context the context of the app.
//     */
//    // A METTRE DANS LE ONCREATE avec this comme context
//    private void createNotificationChannel(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = getAppContext().getString(R.string.notif_mychannel);
//            String description = getAppContext().getString(R.string.notif_notifchannel);
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(getAppContext().getString(R.string.idchannel), name, importance);
//            channel.setDescription(description);
//
//            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }
//
//    /**
//     * Send a notification when a challenge is accomplished.
//     */
//    public void notifyUser() {
//        boolean challengeAccomplished = isCompleted();
//        Context context = getAppContext();
//        if (challengeAccomplished) {
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getAppContext().getString(R.string.idchannel))
//                    .setContentTitle(context.getString(R.string.notification_accomplishedchall))
//                    .setContentText(context.getString(R.string.notification_congratulations))
//                    //.setSmallIcon(Ressource.Drawable.ic_notification); If an icon is needed
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//            //.setAutoCancel(true);
//            Notification notification = builder.build();
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//            int notificationID = 0;
//            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {return;}
//            notificationManager.notify(notificationID, notification);
//        }
//
//    }
}
