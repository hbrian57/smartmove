package com.centrale.smartmove;

import java.util.Vector;

public class Week {
    int idWeek;
    Vector<Trip> trips;
    int consWeek;

    public void setIdWeek(int idWeek) {
        this.idWeek = idWeek;
    }

    public void setTrips(Vector<Trip> trips) {
        this.trips = trips;
    }

    public void setConsWeek(int consWeek) {
        this.consWeek = consWeek;
    }

    public int getIdWeek() {
        return idWeek;
    }

    public Vector<Trip> getTrips() {
        return trips;
    }

    public int getConsWeek() {
        return consWeek;
    }
}
