package com.centrale.smartmove.tracker;

import java.util.Observable;
import java.util.Observer;
import com.centrale.smartmove.models.TimestampedPosition;

import java.util.Timer;
import java.util.TimerTask;

public class TrackerClock extends Observable {

    //Fréquence de rafraichissement de la position
    Integer positionUpdateDelayMillis = 1000;
    //Résultat du tracker
    TimestampedPosition trackerResult;
    //Timer de rafraichissement de la position
    Timer timer;
    UserTracker tracker;

    public TrackerClock(UserTracker initializedTracker) {
        super();
        tracker = initializedTracker;
        trackerResult = new TimestampedPosition(0.0,0.0, 0);
        timer = new Timer();
        TimerTask runUpdatePosition = new TimerTask() {
            @Override
            public void run() {
                //System.out.println(trackerResult.getSaveFormat());
                setNewPosition(tracker.getLatitude(), tracker.getLongitude());

            }
        };
        timer.schedule(runUpdatePosition, 0, positionUpdateDelayMillis);
    }

    public double getLatitude() {
        return trackerResult.getLatitude();
    }
    public double getLongitude() {
        return trackerResult.getLongitude();
    }
    public void setNewPosition(double latitude, double longitude) {
        synchronized (this) {
            trackerResult.set(latitude, longitude);
            setChanged();
            notifyObservers();
        }
    }
    public void setTracker(UserTracker tracker) {
        this.tracker = tracker;
    }

    public TimestampedPosition getPosition() {
        return trackerResult;
    }
}
