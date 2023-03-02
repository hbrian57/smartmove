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
        this.title = "Challenge";
        this.description = "Description";
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

    public String getPorgressionString() {
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

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Double getProgression() {
        return progression;
    }

    public ChallengeGoal getGoal() {
        return goal;
    }

    public @DrawableRes int getIcon() {
        return icon;
    }


    // A METTRE DANS LE ONCREATE avec this comme context
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "Notification channel for my app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("idChannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void notifyUser() {
        boolean challengeAccomplished = isCompleted();
        Context context = getAppContext();
        if (challengeAccomplished) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "idChannel")
                    .setContentTitle("Challenge accomplished")
                    .setContentText("Congratulations!")
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

}
