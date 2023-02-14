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
     * Double corresponding to the altitude
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
     * @throws Exception si la latitude est supérieure à 180°
     * @throws Exception si la longitude est inférieure à -90°
     * @throws Exception si la longitude est supérieure à 90°
     * @throws Exception si la latitude est inférieure à -180°
     * @throws Exception si une des coordonnées est vide
     */
    public double calculateDistance(TimestampedPosition targetPosition) throws Exception {
        if((targetPosition.latitude>90) || (this.latitude>90))
        {throw new Exception("Impossible que la latitude soit supérieure à 90°");}
        if((targetPosition.longitude>180) || (this.longitude>180))
        {throw new Exception("Impossible que la longitude soit supérieure à 180°");}
        if((targetPosition.latitude<-90) || (this.latitude<-90))
        {throw new Exception("Impossible que la latitude soit inférieure à -90°");}
        if((targetPosition.longitude<-180) || (this.longitude<-180))
        {throw new Exception("Impossible que la longitude soit inférieure à -180°");}
        /** //TODO: faire l'exception quand c'est NULL, mais là ne fonctionne pas car NULL=O, je ne sais pas comment faire
        if((targetPosition.latitude==NULL) || (this.latitude==NULL) || (targetPosition.longitude==NULL) || (this.longitude==NULL))
        { throw new Exception("Impossible avec une position dont une coordonnée est vide (latitude ou longitude)");}*/
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