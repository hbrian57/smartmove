package com.centrale.smartmove.models;

import com.centrale.smartmove.R;
import com.centrale.smartmove.Savable;
import org.osmdroid.util.GeoPoint;
import org.json.JSONException;
import org.json.JSONObject;

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
    Calendar timestamp;


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


    public Calendar getDateOfCapture() {
        return timestamp;
    }


    /**
     * Constructor of a timestampedposition with the latitude, longitude, altitude
      * @param lati latitude
     * @param longi longitude
     */
    public TimestampedPosition(double lati, double longi) {
        this.geoCoordinates.setCoords(lati,longi);
        this.timestamp = Calendar.getInstance();
    }

    public TimestampedPosition(double lati, double longi, Calendar date) {
        this.geoCoordinates.setCoords(lati,longi);
        this.timestamp = date;
    }

    public void setDateOfCapture(Calendar dateOfCapture) {
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
        if((targetPosition.getAltitude()>90) || (this.getAltitude()>90))
        {throw new IllegalArgumentException(String.valueOf(R.string.positionException90deg));}
        if((targetPosition.getLongitude()>180) || (this.getLongitude()>180))
        {throw new IllegalArgumentException(String.valueOf(R.string.positionException180));}
        if((targetPosition.getLatitude()<-90) || (this.getLatitude()<-90))
        {throw new IllegalArgumentException(String.valueOf(R.string.positionException90Moins));}
        if((targetPosition.getLongitude()<-180) || (this.getLongitude()<-180))
        {throw new IllegalArgumentException(String.valueOf(R.string.positionException180Moins));}
        else{

        final int R = 6371000; // Radius of the earth
        double deltaLatitude = Math.toRadians(targetPosition.getLatitude() - this.getLatitude());
        double deltaLongitude = Math.toRadians(targetPosition.getLongitude() - this.getLongitude());
        double a = Math.sin(deltaLatitude / 2) * Math.sin(deltaLatitude / 2)
            + Math.cos(Math.toRadians(this.getLatitude())) * Math.cos(Math.toRadians(targetPosition.getLatitude()))
            * Math.sin(deltaLongitude / 2) * Math.sin(deltaLongitude / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        double deltaAltitude = this.getAltitude() - targetPosition.getAltitude();
        distance = Math.pow(distance, 2) + Math.pow(deltaAltitude, 2);
        return Math.sqrt(distance);
    }}

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
        this.timestamp = Calendar.getInstance();

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
    public Calendar getTimestamp() {
        return this.timestamp;
    }

}
