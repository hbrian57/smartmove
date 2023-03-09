package com.centrale.smartmove;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private double altitude;

    /**
     * Date corresponding to exact moment the position is taken
     */
    Date dateOfCapture;

    public Date getDateOfCapture() {
        return dateOfCapture;
    }


    /**
     * Constructor of a timestampedposition with the latitude, longitude, altitude
      * @param lati latitude
     * @param longi longitude
     * @param alti altidude
     */
    public TimestampedPosition(double lati, double longi, double alti) {
        this.altitude = alti;
        this.longitude = longi;
        this.latitude = lati;
        this.dateOfCapture = new Date();
    }

    public void setDateOfCapture(Date dateOfCapture) {
        this.dateOfCapture = dateOfCapture;
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
        {throw new IllegalArgumentException(String.valueOf(R.string.positionException90deg));}
        if((targetPosition.longitude>180) || (this.longitude>180))
        {throw new IllegalArgumentException(String.valueOf(R.string.positionException180));}
        if((targetPosition.latitude<-90) || (this.latitude<-90))
        {throw new IllegalArgumentException(String.valueOf(R.string.positionException90Moins));}
        if((targetPosition.longitude<-180) || (this.longitude<-180))
        {throw new IllegalArgumentException(String.valueOf(R.string.positionException180Moins));}

        final int R = 6371000; // Radius of the earth
        double deltaLatitude = Math.toRadians(targetPosition.latitude - this.latitude);
        double deltaLongitude = Math.toRadians(targetPosition.longitude - this.longitude);
        double a = Math.sin(deltaLatitude / 2) * Math.sin(deltaLatitude / 2)
            + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(targetPosition.latitude))
            * Math.sin(deltaLongitude / 2) * Math.sin(deltaLongitude / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        double deltaAltitude = this.altitude - targetPosition.altitude;
        distance = Math.pow(distance, 2) + Math.pow(deltaAltitude, 2);
        return Math.sqrt(distance);
    }

    /**
     * Calculates the velocity between two TimestampedPosition
     * @param secondPosition
     * @return
     * @throws Exception
     */

    public double calculateVelocityBetweenTwoPoints(TimestampedPosition secondPosition) throws Exception{
        double velocity=0;
        LocalDateTime dateTime1 = LocalDateTime.ofInstant(this.dateOfCapture.toInstant(), ZoneId.systemDefault());
        LocalDateTime dateTime2 = LocalDateTime.ofInstant(secondPosition.dateOfCapture.toInstant(), ZoneId.systemDefault());
        Duration duration = Duration.between(dateTime1, dateTime2);
        long timeBetweenPoints = duration.getSeconds();
        double distanceBetweenPoints = this.calculateDistance(secondPosition);
        velocity=distanceBetweenPoints/timeBetweenPoints;
        return velocity;
    }

    @Override
    /**
     * method which allows to save all the TimeStampedPosition of User in a JSON file
     * @return the JSON file of the backup
     */
    public JSONObject getSaveFormat() {
        JSONObject JSONTimestampedPosition = new JSONObject();
        try {
            JSONTimestampedPosition.put(String.valueOf(R.string.postion_TimeStamp), dateOfCapture);
            JSONObject JSONPosition = new JSONObject();
            JSONPosition.put(String.valueOf(R.string.position_latitude), latitude);
            JSONPosition.put(String.valueOf(R.string.position_Longitude), longitude);
            JSONPosition.put(String.valueOf(R.string.position_height), altitude);
            JSONTimestampedPosition.put(String.valueOf(R.string.position_position), JSONPosition);
        } catch (JSONException e) {
            e.printStackTrace(); //TODO : Handle the exception properly
        }
        return JSONTimestampedPosition;
    }

    /**
     * Get the date of the TimeStampedPosition object.
     * @return the date.
     */
    public Date getDatePos() {
        return this.dateOfCapture;
    }
}
