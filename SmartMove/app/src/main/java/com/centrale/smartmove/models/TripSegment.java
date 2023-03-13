package com.centrale.smartmove.models;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.centrale.smartmove.R;
import com.centrale.smartmove.Savable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
     * Boolean which indicates if the TripSegment is finished or not (ready to be analyzed)
     */
    boolean isFinished = false;


    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished() {
        this.isFinished = true;
    }




    /**
     * Constructor without any position, with just the transport type used
     * @param transportTypeUsed mean of transport
     */
    public TripSegment(TransportType transportTypeUsed){
        this.transportType=transportTypeUsed;
    }


    /**
     * Constructor of a a segment of Trip (a TripSegment) using all its attributes
     * @param transportTypeUsed mean of transport
     * @param timestampedPositions list of all the position
     */
    public TripSegment(TransportType transportTypeUsed, LinkedList<TimestampedPosition> timestampedPositions) {
        this.transportType = transportTypeUsed;
        this.timestampedPositionList = timestampedPositions;
    }

    public TripSegment(TimestampedPosition firstPosition) {
        this.transportType = TransportType.STATIC;
        this.timestampedPositionList = new LinkedList<>();
        this.timestampedPositionList.add(firstPosition);
    }

    /**
     * Method which calculates the total distance of the TripSegment
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

    public LinkedList<TimestampedPosition> getPositionList() {
        return timestampedPositionList;
    }

    public TimestampedPosition getFirstPosition() throws Exception {
        if(this.timestampedPositionList.isEmpty()){
            throw new Exception(String.valueOf(R.string.tripSegmentException2));
        }
        return timestampedPositionList.get(0);
    }

    /**
     * Method which enables to calculate the CO2 emission of a TripSegment
     * @return an integer corresponding to the value of the CO2 emission
     */
    public double calculateCO2footprint()  {
        float transportTypeCO2Parameter = transportType.getCO2param();
        return transportTypeCO2Parameter*this.calculateTotalDistance();
    }

    /**
     * Defines the right TransportType for the TripSegment
     */
    public void computeTransportType() throws Exception{
        int listSize = this.timestampedPositionList.size();
        double lastTwoPointsVelocity=0;
        int changement=0;
        lastTwoPointsVelocity=timestampedPositionList.get(listSize-1).calculateVelocityBetweenTwoPoints(timestampedPositionList.get(listSize-2));
        if(lastTwoPointsVelocity*3.6>2){
            if((2<calculateRollingVelocity()*3.6)&&(calculateRollingVelocity()*3.6<=6)&&(transportType!=TransportType.WALKING)){
                this.transportType = TransportType.WALKING;
            }if((6<calculateRollingVelocity()*3.6)&&(calculateRollingVelocity()*3.6<=20)&&(transportType!=TransportType.BIKE)){
                this.transportType = TransportType.BIKE;
            }if((20<calculateRollingVelocity()*3.6)&&(transportType!=TransportType.CAR)){
                this.transportType = TransportType.CAR;
        }}


    }

    //TODO régler les problèmes d'indice(dépassements)
    /**
    Calculates the mean of the last 10 points
     */
    public double calculateRollingVelocity() throws Exception {
        int listSize = this.timestampedPositionList.size();
        double velocityMean=0;
        if(listSize>=10){
            int i=0;
            double sum=0;
            for(i=0;i<9;i++){
                sum+=timestampedPositionList.get(listSize-i-1).calculateVelocityBetweenTwoPoints(timestampedPositionList.get(listSize-i-2));
            }
            velocityMean=sum/10;
            return velocityMean;

        }
        return velocityMean;
    }

    /**
     * Calculates the acceleration of the last 10 points
     */
    public double calculateRollingAcceleration() throws Exception{
        int listSize = this.timestampedPositionList.size();
        double accelerationMean=0;
        if(listSize>=10){
            int i=0;
            long totaltemps=0;
            double sum=0;
            for(i=0;i<8;i++){
                LocalDateTime dateTime1 = LocalDateTime.ofInstant(this.timestampedPositionList.get(listSize-i-1).timestamp.toInstant()
                        , ZoneId.systemDefault());
                LocalDateTime dateTime2 = LocalDateTime.ofInstant(this.timestampedPositionList.get(listSize-i-3).timestamp.toInstant(), ZoneId.systemDefault());

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

    }

    /**
     * method which allows to save all the TripSegment of User in a JSON file
     * @return the JSON file of the backup
     */
    @Override
    public JSONObject getSaveFormat() {
        JSONObject JSONTripSegment = new JSONObject();

        try {
            JSONTripSegment.put(String.valueOf(R.string.tripSegmentTransportType), transportType.name());
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


    public int getNumberOfPositions() {
        return timestampedPositionList.size();
    }

    public void addPosition(TimestampedPosition newPosition) {
        //We store the position in the list, making sure to copy it to avoid any reference issues
        timestampedPositionList.add(new TimestampedPosition(newPosition));
        //Compute the transport type if the number of positions is superior to 10
        if(timestampedPositionList.size()>10){
            try {
                computeTransportType();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void mergeWithSegment(TripSegment source) {
        this.timestampedPositionList.addAll(source.getPositionList());
    }
}
