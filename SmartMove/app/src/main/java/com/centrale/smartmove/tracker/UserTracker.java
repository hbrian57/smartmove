package com.centrale.smartmove.tracker;

import androidx.appcompat.app.AppCompatActivity;

public abstract class UserTracker {
    public abstract double getLatitude();
    public abstract double getLongitude();
    public abstract double getAltitude();
    public abstract String getFormattedName();
}
