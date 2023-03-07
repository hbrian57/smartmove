package com.centrale.smartmove;

import static com.centrale.smartmove.App.getAppContext;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.Date;
import java.util.HashMap;


public class Challenge {
    String title;
    String description;
    Double progression;
    ChallengeGoal goal;
    Date challengeBeginning;
    @DrawableRes int icon;

    public Challenge(String title, String description, Double progression, ChallengeGoal goal, Date challengeBeginning, int icon) {
        this.title = title;
        this.description = description;
        this.progression = progression;
        this.goal = goal;
        this.challengeBeginning = challengeBeginning;
        this.icon = icon;
    }

    public Challenge() {
        this.title = getAppContext().getString(R.string.challenge_challenge);
        this.description = getAppContext().getString(R.string.challenge_description);
        this.progression = 0.0;
        this.goal = new ChallengeGoal();
        this.challengeBeginning = new Date();
        this.icon = R.drawable.travel;
    }

    public void setChallengeBeginning(Date challengeBeginning) {
        this.challengeBeginning = challengeBeginning;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProgression(Double progression) {
        this.progression = progression;
    }

    public void setGoal(ChallengeGoal goal) {
        this.goal = goal;
    }

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

    public Double getProgressionDouble() {
        return progression;
    }

    public boolean isCompleted() {
        return false;
    }

        public String getTitle () {
            return title;
        }

        public String getDescription () {
            return description;
        }

        public Double getProgression () {
            return progression;
        }

        public ChallengeGoal getGoal () {
            return goal;
        }



        Challenge(String Title,String Description, Double Progression,ChallengeGoal Goal,Date DebutChallenge){
        this.title=Title;
        this.description=Description;
        this.progression=Progression;
        this.goal=Goal;
        }

    public @DrawableRes int getIcon() {
        return icon;
    }


    // A METTRE DANS LE ONCREATE avec this comme context
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getAppContext().getString(R.string.notif_mychannel);
            String description = getAppContext().getString(R.string.notif_notifchannel);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getAppContext().getString(R.string.idchannel), name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void notifyUser() {
        boolean challengeAccomplished = isCompleted();
        Context context = getAppContext();
        if (challengeAccomplished) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getAppContext().getString(R.string.idchannel))
                    .setContentTitle(context.getString(R.string.notification_accomplishedchall))
                    .setContentText(context.getString(R.string.notification_congratulations))
                    //.setSmallIcon(Ressource.Drawable.ic_notification); If an icon is needed
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            //.setAutoCancel(true);
            Notification notification = builder.build();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            int notificationID = 0;
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {return;}
            notificationManager.notify(notificationID, notification);
        }

    }

    /**
     * Updates the progression of the challenge based on the new trip done
     * @param newTripDone
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
}
