package com.centrale.smartmove.models;

import com.centrale.smartmove.Savable;
import org.osmdroid.util.GeoPoint;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;


public class TimestampedPosition implements Savable {

    //Attributes------------------------------------------------------------------------------------
    /**
     * GeoPoint that contains the geographical coordinates
     */
    private GeoPoint geoCoordinates;

    /**
     * Timestamp corresponding to exact moment the position is taken
     */
    private Timestamp timestamp;

    //Getters and Setters---------------------------------------------------------------------------
    /**
     * Getter
     * @return double corresponding to the latitude
     */
    public double getLatitude() {
        return geoCoordinates.getLatitude();
    }

    /**
     * Getter
     * @return double corresponding to the longitude
     */
    public double getLongitude() {
        return geoCoordinates.getLongitude();
    }

    /**
     * Getter
     * @return Timestamp timestamp
     */
    public Timestamp getTimeStamp() {
        return timestamp;
    }

    /**
     * Sets the parameters of the GeoPoint geoCoordinates
     * @param latitude, a double
     * @param longitude, a double
     */
    public void set(double latitude, double longitude) {
        this.geoCoordinates.setCoords(latitude,longitude);
        this.timestamp = new Timestamp(System.currentTimeMillis());

    }



    //Constructors----------------------------------------------------------------------------------
    /**
     * Constructor that takes into parameter a TimestampedPosition
     * @param newPosition, a new TimestampedPosition
     */
    public TimestampedPosition(TimestampedPosition newPosition) {
        this.geoCoordinates = newPosition.geoCoordinates;
        this.timestamp = newPosition.getTimeStamp();
    }

    /**
     * Constructor of a TimestampedPosition with the latitude and the longitude
      * @param lati latitude
     * @param longi longitude
     */
    public TimestampedPosition(double lati, double longi) {
        GeoPoint geoPoint = new GeoPoint(lati,longi);
        this.geoCoordinates= geoPoint;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Constructor of a TimestampedPosition with the latitude, the longitude and the date
     * @param lati latitude
     * @param longi longitude
     * @param date date
     */
    public TimestampedPosition(double lati, double longi, Timestamp date) {
        GeoPoint geoPoint = new GeoPoint(lati,longi);
        this.geoCoordinates= geoPoint;
        this.timestamp = date;
    }


    //Methods-------------------------------------------------------------------------------------
    /**
     * Method that calculates the distance between two TimestampedPositions
     * @param targetPosition, a TimestampedPosition with which we want to know the distance to
     * @return a double that corresponds to the distance between the points
     */
    public double getDistanceToPosition(TimestampedPosition targetPosition){
        return this.geoCoordinates.distanceToAsDouble(targetPosition.geoCoordinates);
    }

    /**
     * Calculates the velocity between two TimestampedPosition
     * @param secondPosition, the other TimestampedPosition we want to know the velocity between
     * @return a double, the velocity between the two points
     */
    public double calculateVelocityBetweenTwoPoints(TimestampedPosition secondPosition){
        double velocity=0;
        LocalDateTime dateTime1 = LocalDateTime.ofInstant(this.timestamp.toInstant(), ZoneId.systemDefault());
        LocalDateTime dateTime2 = LocalDateTime.ofInstant(secondPosition.timestamp.toInstant(), ZoneId.systemDefault());
        Duration duration = Duration.between(dateTime1, dateTime2);
        long timeBetweenPoints = duration.getSeconds();
        double distanceBetweenPoints = this.getDistanceToPosition(secondPosition);
        velocity=distanceBetweenPoints/timeBetweenPoints;
        return velocity;
    }


    @Override
    /**
     * Method that allows to save all the TimeStampedPosition of User in a JSON file
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

}
