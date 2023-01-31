package com.centrale.smartmove;

import java.util.Vector;

public class Trip {

    /**
     * Vector with all the little trips contained in the trip
     */
    Vector<LilTrip> listLilTrips;

    /**
     * Constructor of a trip with all the little trips contained in this trip as a parameter
     * @param listLT list of the little trips contained in this trip
     */
    public Trip(Vector<LilTrip> listLT) {
        this.listLilTrips=listLT;
    }

    public Vector<com.centrale.smartmove.LilTrip> getListLilTrips() {
        return listLilTrips;
    }

    }
