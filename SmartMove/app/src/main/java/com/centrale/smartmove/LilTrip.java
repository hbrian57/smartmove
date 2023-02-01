package com.centrale.smartmove;


import java.util.Date;
import java.util.List;

public class LilTrip {
    /**
     * Mean of transport
     */
    Transport moyenTransport;
    /**
     * List of the positions taken during all the LitTrip
     */
    List<Position> trace2D;
    /**
     * Initial date of the LitTrip
     */
    Date InitialDate;
    /**
     * Final date of the LitTrip
     */
    Date FinalDate;
    /**
     * Total distance of the LitTrip
     */
    int distanceTotal;

    /**
     * Constructor of a a segment of Trip (a LitTrip) using all its attributes
     * @param moyenTransport mean of transport
     * @param trace2D list of all the position in 2D
     * @param InitialDate initial date of the littrip
     * @param FinalDate Final date of the littrip
     * @param distanceTotal total distance of the littrip
     */
    public LilTrip(Transport moyenTransport, List<Position> trace2D, Date InitialDate, Date FinalDate, int distanceTotal) {
        this.moyenTransport = moyenTransport;
        this.trace2D = trace2D;
        this.InitialDate = InitialDate;
        this.FinalDate = FinalDate;
        this.distanceTotal = distanceTotal;
    }

    /**
     * Method which calculates the total distance of the LitTrip
     * @return an integer corresponding to the total distance in m
     */
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
