package com.centrale.smartmove;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class TimestampedPosition implements Savable {

    /**
     * Double corresponding to the latitude
     */
    private double latitude;

    /**
     * Double corresponding to the longitude
     */
    private double longitude;

    /**
     *
     */

    private double height;

    /**
     * Date corresponding to exact moment the position is taken
     */
    Date dateOfCapture;


    public TimestampedPosition(double lat, double longi, double hei) {
        this.height = hei;
        this.longitude = longi;
        this.latitude = lat;
    }


    public void setDatePos(Date datePos) {
        this.dateOfCapture = datePos;
    }

    /**
     * Fonction calculant la distance entre deux positions
     * @param targetPosition la position avec laquelle on veut calculer la distance
     * @return une distance en mètre
     * @throws Exception si la latitute ou la longitude sont supérieures à 90° cela renvoie une exceptiion
     */
    public double calculateDistance(TimestampedPosition targetPosition) throws Exception {
        if((targetPosition.latitude>90) || (this.latitude>90) || (targetPosition.longitude>90) || (this.longitude>90)) {
            throw new Exception("Impossible que le degré soit supérieur à 90°");
        }
    final int R = 6371; // Radius of the earth
    double latDistance = Math.toRadians(targetPosition.latitude - this.latitude);
    double lonDistance = Math.toRadians(targetPosition.longitude - this.longitude);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(targetPosition.latitude))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c * 1000; // convert to meters
    double differenceheight = this.height - targetPosition.height;
    distance = Math.pow(distance, 2) + Math.pow(differenceheight, 2);
    return Math.sqrt(distance);
    }


    @Override
    public JSONObject getSaveFormat() {
        JSONObject JSONTimestampedPosition = new JSONObject();
        try {
            JSONTimestampedPosition.put("timestamp", dateOfCapture);
            JSONObject JSONPosition = new JSONObject();
            JSONPosition.put("latitude", latitude);
            JSONPosition.put("longitude", longitude);
            JSONPosition.put("height", height);
            JSONTimestampedPosition.put("position", JSONPosition);
        } catch (JSONException e) {
            e.printStackTrace(); //TODO : Handle the exception properly
        }
        return JSONTimestampedPosition;
    }

    public Date getDatePos() {
        return this.dateOfCapture;
    }
}