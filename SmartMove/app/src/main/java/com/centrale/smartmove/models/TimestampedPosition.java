package com.centrale.smartmove.models;

import com.centrale.smartmove.R;
import com.centrale.smartmove.Savable;
import org.osmdroid.util.GeoPoint;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;


public class TimestampedPosition implements Savable {

    /**
     * Double corresponding to the latitude
     */
    private GeoPoint geoCoordinates;

    /**
     * Date corresponding to exact moment the position is taken
     */
    Timestamp timestamp;


    public TimestampedPosition(TimestampedPosition newPosition) {
        this.geoCoordinates = newPosition.geoCoordinates;
        this.timestamp = newPosition.getDateOfCapture();
    }

    private double getAltitude() {
        return geoCoordinates.getAltitude();
    }

    public double getLatitude() {
        return geoCoordinates.getLatitude();
    }

    public double getLongitude() {
        return geoCoordinates.getLongitude();
    }


    public Timestamp getDateOfCapture() {
        return timestamp;
    }


    /**
     * Constructor of a timestampedposition with the latitude, longitude, altitude
      * @param lati latitude
     * @param longi longitude
     */
    public TimestampedPosition(double lati, double longi) {
        GeoPoint geoPoint = new GeoPoint(lati,longi);
        this.geoCoordinates= geoPoint;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public TimestampedPosition(double lati, double longi, Timestamp date) {
        GeoPoint geoPoint = new GeoPoint(lati,longi);
        this.geoCoordinates= geoPoint;
        this.timestamp = date;
    }

    public void setDateOfCapture(Timestamp dateOfCapture) {
        this.timestamp = dateOfCapture;
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
        return this.geoCoordinates.distanceToAsDouble(targetPosition.geoCoordinates);
    }



    /**
     * Calculates the velocity between two TimestampedPosition
     * @param secondPosition
     * @return
     * @throws Exception
     */

    public double calculateVelocityBetweenTwoPoints(TimestampedPosition secondPosition) throws Exception{
        double velocity=0;
        LocalDateTime dateTime1 = LocalDateTime.ofInstant(this.timestamp.toInstant(), ZoneId.systemDefault());
        LocalDateTime dateTime2 = LocalDateTime.ofInstant(secondPosition.timestamp.toInstant(), ZoneId.systemDefault());
        Duration duration = Duration.between(dateTime1, dateTime2);
        long timeBetweenPoints = duration.getSeconds();
        double distanceBetweenPoints = this.calculateDistance(secondPosition);
        velocity=distanceBetweenPoints/timeBetweenPoints;
        return velocity;
    }
    public void set(double latitude, double longitude) {
        this.geoCoordinates.setCoords(latitude,longitude);
        this.timestamp = new Timestamp(System.currentTimeMillis());

    }

    @Override
    /**
     * method which allows to save all the TimeStampedPosition of User in a JSON file
     * @return the JSON file of the backup
     */
    @SuppressWarnings("lint:HardCodedStringLiteral")
    public JSONObject getSaveFormat() {
        JSONObject JSONTimestampedPosition = new JSONObject();
        try {
            JSONTimestampedPosition.put("timestamp", timestamp);
            JSONObject JSONPosition = new JSONObject();
            JSONPosition.put("latitude", this.geoCoordinates.getLatitude());
            JSONPosition.put("longitude", this.geoCoordinates.getLongitude());
            JSONTimestampedPosition.put("position", JSONPosition);
        } catch (JSONException e) {
            e.printStackTrace(); //TODO : Handle the exception properly
        }
        return JSONTimestampedPosition;
    }

    /**
     * Get the date of the TimeStampedPosition object.
     * @return the date.
     */
    public Timestamp getTimestamp() {
        return this.timestamp;
    }

}
