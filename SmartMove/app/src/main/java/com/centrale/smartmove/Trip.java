package com.centrale.smartmove;

import java.util.Vector;

public class Trip {
    Vector<LilTrip> LilTrip;

    public Vector<com.centrale.smartmove.LilTrip> getLilTrip() {
        return LilTrip;
    }

    public void setLilTrip(Vector<com.centrale.smartmove.LilTrip> lilTrip) {
        LilTrip = lilTrip;
    }
}
