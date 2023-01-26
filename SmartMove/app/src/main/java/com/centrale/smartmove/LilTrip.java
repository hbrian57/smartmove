package com.centrale.smartmove;


import java.util.Date;
import java.util.List;

public class LilTrip {
    Transport transport;
    List<Position> trace2D;
    Date InitialDate;
    Date FinalDate;

    public void setTransport(Transport transport) {
        this.transport = transport;
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
        return transport;
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
}
