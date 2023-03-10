package com.centrale.smartmove.tracker;

import com.centrale.smartmove.models.TimestampedPosition;

import java.util.Timer;
import java.util.TimerTask;

public class TrackerClock {

    //Fréquence de rafraichissement de la position
    Integer positionUpdateDelayMillis = 1000;
    //Résultat du tracker
    TimestampedPosition trackerResult;
    //Timer de rafraichissement de la position
    Timer timer;

    public TrackerClock(UserTracker tracker){
        trackerResult = new TimestampedPosition(0.0,0.0, 0);
        timer = new Timer();
        TimerTask runUpdatePosition = new TimerTask() {
            @Override
            public void run() {
                trackerResult.set(tracker.getLatitude(), tracker.getLongitude(), tracker.getAltitude());
                System.out.println(trackerResult.getSaveFormat());

            }
        };
        timer.schedule(runUpdatePosition, 0, positionUpdateDelayMillis);
    }

}
