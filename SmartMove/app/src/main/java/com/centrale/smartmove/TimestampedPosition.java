package com.centrale.smartmove;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class TimestampedPosition implements Savable {

    /**
     * Double corresponding to the x in a 3D plan
     */
    private double x;

    /**
     * Double corresponding to the y in a 3D plan
     */
    private double y;
    /**
     * Double corresponding to the z in a 3D plan
     */
    private double z;
    /**
     * Date corresponding to exact moment the position is taken
     */
    Date datePos;

    public TimestampedPosition() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.datePos=null;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Date getDatePos() {
        return datePos;
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

    public void setDatePos(Date datePos) {
        this.datePos = datePos;
    }

    public double calculateDistance(TimestampedPosition targetPosition) {
        double dx = this.x - targetPosition.x;
        double dy = this.y - targetPosition.y;
        double dz = this.z - targetPosition.z;
        return Math.sqrt(dx*dx+dy*dy+dz*dz);
    }

    @Override
    public JSONObject getSaveFormat() {
        JSONObject JSONTimestampedPosition = new JSONObject();
        try {
            JSONTimestampedPosition.put("timestamp", datePos);
            JSONObject JSONPosition = new JSONObject();
            JSONPosition.put("x", x);
            JSONPosition.put("y", y);
            JSONPosition.put("z", z);
            JSONTimestampedPosition.put("position", JSONPosition);
        }  catch (JSONException e) {
            e.printStackTrace(); //TODO : Handle the exception properly
        }
        return JSONTimestampedPosition;
    }

    /**
     * Sets the position of the point to the given coordinates.
     * @param x
     * @param y
     * @param z
     */
    public void setTimestampedPosition(double x,double y,double z){
        this.x=x;
        this.y=y;
        this.z=z;
    }


}
