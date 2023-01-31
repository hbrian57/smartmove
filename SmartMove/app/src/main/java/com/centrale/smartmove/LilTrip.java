package com.centrale.smartmove;


import java.util.Date;
import java.util.List;

public class LilTrip {
    Transport moyenTransport;
    List<Position> trace2D;
    Date InitialDate;
    Date FinalDate;
    int distanceTotal;


    public int calculTotalDistance(){
    return distanceTotal;
    }


    public void setTransport(Transport transport) {
        this.moyenTransport = transport;
    }

    public void setTrace2D(List<Position> trace2D) {
        this.trace2D = trace2D;
    }

    public void setInitialDate(Date initialDate) {
        InitialDate = initialDate;
    }

    public void setFinalDate(Date finalDate) {
        FinalDate = finalDate;
    }

    public Transport getTransport() {
        return moyenTransport;
    }

    public List<Position> getTrace2D() {
        return trace2D;
    }

    public Date getInitialDate() {
        return InitialDate;
    }

    public Date getFinalDate() {
        return FinalDate;
    }

    public int getDistanceTotal() {
        return distanceTotal;
    }

}
