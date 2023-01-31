package com.centrale.smartmove;

import java.util.Date;

public class Position {

    /**
     * double corresponding to the x in a 3D plan
     */
    private double x;

    /**
     * double corresponding to the y in a 3D plan
     */
    private double y;
    /**
     * double corresponding to the z in a 3D plan
     */
    private double z;
    /**
     * date corresponding to exact moment the position is taken
     */
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
