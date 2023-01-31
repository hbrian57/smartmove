package com.centrale.smartmove;

import java.util.Date;

public class Position {
    private double x;
    private double y;
    private double z;
    Date date;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Date getDate() {
        return date;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
