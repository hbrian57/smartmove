package com.centrale.smartmove;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static java.sql.Types.NULL;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;


public class TripSegment implements Savable {
    /**
     * Mean of transport
     */
    TransportType transportType;
    /**
     * List of the positions taken during all the TripSegment
     */
    LinkedList<TimestampedPosition> timestampedPositionList;


    /**
     * Constructor without any position, with just the transport type used
     * @param transportTypeUsed mean of transport
     */
    public TripSegment(TransportType transportTypeUsed){
        this.transportType=transportTypeUsed;
    }


    /**
     * Constructor of a a segment of Trip (a LitTrip) using all its attributes
     * @param transportTypeUsed mean of transport
     * @param timestampedPositions list of all the position
     */
    public TripSegment(TransportType transportTypeUsed, LinkedList<TimestampedPosition> timestampedPositions) {
        this.transportType = transportTypeUsed;
        this.timestampedPositionList = timestampedPositions;
    }

    /**
     * Method which calculates the total distance of the LitTrip
     * @return an integer corresponding to the total distance in m
     */
    public double calculateTotalDistance() {
        double totalDistance = 0;
        for (int i = 0; i < timestampedPositionList.size() - 1; i++) {
            try {
                totalDistance += timestampedPositionList.get(i).calculateDistance(timestampedPositionList.get(i + 1));
            } catch (Exception e) {
                //TODO Gestion de l'erreur, afficher un popup / Toast
            }
        }
        return totalDistance;
    }


    public TransportType getTransportType() {
        return transportType;
    }

    public LinkedList<TimestampedPosition> getPositionList() throws Exception {
        if(this.timestampedPositionList.isEmpty()){
            throw new Exception(String.valueOf(R.string.tripSegmentException1));
        }
        return timestampedPositionList;
    }

    public TimestampedPosition getFirstPosition() throws Exception {
        if(this.timestampedPositionList.isEmpty()){
            throw new Exception(String.valueOf(R.string.tripSegmentException2));
        }
        return timestampedPositionList.get(0);
    }

    /**
     * Method which enables to calculate the CO2 emission of a LitTrip
     * @return an integer corresponding to the value of the CO2 emission
     */
    public double calculateCO2footprint()  {
        float transportTypeCO2Parameter = transportType.getCO2param();
        return transportTypeCO2Parameter*this.calculateTotalDistance();
    }


    /**
     * Defines the right TransportType for each TripSegment
     */
    public void computeTransportType() throws Exception{
        if((2<this.calculateMeanVelocity()*3.6)&&(this.calculateMeanVelocity()*3.6<=6)){
            this.transportType=TransportType.WALKING;
        }if((6<calculateMeanVelocity()*3.6)&&(calculateMeanVelocity()*3.6<=20)){
            this.transportType=TransportType.BIKE;
        }if((20<calculateMeanVelocity()*3.6)){
            this.transportType=TransportType.CAR;
        }
    }


    /**
     * Returns the mean velocity of the TripSegment, doesn't take into account velocities inferior to 2 km/h
     * @return
     * @throws Exception
     */
    public double calculateMeanVelocity() throws Exception {
        int listSize = this.timestampedPositionList.size();
        double velocityMean=0;
        if(listSize>=2){
            int i=0;
            double sum=0;
            for(i=0;i<listSize-1;i++){
                double velocityBetweenTwoPoints=0;
                velocityBetweenTwoPoints = timestampedPositionList.get(i+1).calculateVelocityBetweenTwoPoints(timestampedPositionList.get(i));
                if(velocityBetweenTwoPoints>0.55){
                    sum+=velocityBetweenTwoPoints;
                };
            }
            velocityMean=sum/listSize;
            return velocityMean;

        }
        return velocityMean;
    }

    /**
     * Calculates the acceleration of the last 10 points
     */
    /*public double calculateRollingAcceleration() throws Exception{
        int listSize = this.timestampedPositionList.size();
        double accelerationMean=0;
        if(listSize>=10){
            int i=0;
            long totaltemps=0;
            double sum=0;
            for(i=0;i<8;i++){
                LocalDateTime dateTime1 = LocalDateTime.ofInstant(this.timestampedPositionList.get(listSize-i-1).dateOfCapture.toInstant()
                        , ZoneId.systemDefault());
                LocalDateTime dateTime2 = LocalDateTime.ofInstant(this.timestampedPositionList.get(listSize-i-3).dateOfCapture.toInstant(), ZoneId.systemDefault());

                Duration duration = Duration.between(dateTime1, dateTime2);
                long timeBetweenPoints = duration.getSeconds();
                totaltemps += timeBetweenPoints;

                sum+=(timestampedPositionList.get(listSize-i).calculateVelocityBetweenTwoPoints(timestampedPositionList.get(listSize-i-1))
                        -(timestampedPositionList.get(listSize-i-1).calculateVelocityBetweenTwoPoints(timestampedPositionList.get(listSize-i-2))))/totaltemps;
            }
            accelerationMean=sum/9;
            return accelerationMean;
        }
        return accelerationMean;

    }*/


    @Override
    public JSONObject getSaveFormat() {
        JSONObject JSONTripSegment = new JSONObject();

        try {
            JSONTripSegment.put(String.valueOf(R.string.tripSegmentTrasportType), transportType.name());
            JSONArray JSONPositionList = new JSONArray();
            for (TimestampedPosition pos : timestampedPositionList) {
                JSONPositionList.put(pos.getSaveFormat());
            }
            JSONTripSegment.put(String.valueOf(R.string.TripSegmentPosition), JSONPositionList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JSONTripSegment;
    }

}
