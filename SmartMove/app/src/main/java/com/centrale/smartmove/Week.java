package com.centrale.smartmove;

import java.util.Vector;

public class Week {

    /**
     * integer corresponding to the number of the week
     */
    int idWeek;
    /**
     * vector with all the trips of that week
     */
    Vector<Trip> trips;
    /**
     * total consumption of CO2 of that week
     */
    int consWeek;

    /**
     * constructor of a week with all the parameters
     * @param id number of the week
     * @param tr vector with all the trips
     * @param cons consumption of CO2 during that week
     */
    public Week(int id,Vector<Trip> tr, int cons){
        this.consWeek=cons;
        this.idWeek= id;
        this.trips=tr;
    }

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
