package com.centrale.smartmove.models;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.centrale.smartmove.R;
import com.centrale.smartmove.Savable;

import java.util.LinkedList;


public class TripSegment implements Savable {
    /**
     * Mean of transport
     */
    private TransportType transportType;
    /**
     * List of the positions taken during all the TripSegment
     */
    private LinkedList<TimestampedPosition> positions;
    
    /** 
     * Boolean which indicates if the TripSegment is finished or not (ready to be analyzed)
     */
    private boolean isFinished = false;


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
        this.positions = timestampedPositions;
    }

    public TripSegment(TimestampedPosition firstPosition) {
        this.transportType = TransportType.STATIC;
        this.positions = new LinkedList<>();
        this.positions.add(firstPosition);
    }

    /**
     * Method which calculates the total distance of the TripSegment
     * @return an integer corresponding to the total distance in m
     */
    public double calculateTotalDistance() {
        double totalDistance = 0;
        for (int i = 0; i < positions.size() - 1; i++) {
            try {
                totalDistance += positions.get(i).getDistanceToPosition(positions.get(i + 1));
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
        return positions;
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
        if (this.positions ==null){
            throw new NullPointerException(String.valueOf("Le TripSegment n'a pas été initialisé."));}
        if(this.positions.size()>=2){
            int listSize = this.positions.size();
            double lastTwoPointsVelocity=0;
            double meanVelocity = this.calculateMeanVelocity()*3.6; //convert to km/h
            lastTwoPointsVelocity= positions.get(listSize-1).calculateVelocityBetweenTwoPoints(positions.get(listSize-2));
            if(lastTwoPointsVelocity*3.6>2){
                if((2<meanVelocity)&&(meanVelocity<=6)){
                    this.transportType = TransportType.WALKING;
                }
                if((6<meanVelocity)&&(meanVelocity<=20)){
                    this.transportType = TransportType.BIKE;
                }else{
                    this.transportType = TransportType.CAR;
        }}
    }}

    //TODO régler les problèmes d'indice(dépassements)
    /**
    Calculates the mean of the last 10 points
     */
    public double calculateRollingVelocity() throws Exception {
        int listSize = this.positions.size();
        double velocityMean=0;
        if(listSize>=10){
            int i=0;
            double sum=0;
            for(i=0;i<9;i++){
                sum+= positions.get(listSize-i-1).calculateVelocityBetweenTwoPoints(positions.get(listSize-i-2));
            }
            velocityMean=sum/10;
            return velocityMean;

        }
        return velocityMean;
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
            for (TimestampedPosition pos : positions) {
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
        int listSize = this.positions.size();
        double velocityMean=0;
        if(listSize>=2){
            int i=0;
            double sum=0;
            for(i=0;i<listSize-1;i++){
                double velocityBetweenTwoPoints=0;
                velocityBetweenTwoPoints = positions.get(i+1).calculateVelocityBetweenTwoPoints(positions.get(i));
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
        return positions.size();
    }

    public void addPosition(TimestampedPosition newPosition) {
        //We store the position in the list, making sure to copy it to avoid any reference issues
        positions.add(new TimestampedPosition(newPosition));
        //Compute the transport type if the number of positions is superior to 10
        if(positions.size()>10){
            try {
                computeTransportType();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void mergeWithSegment(TripSegment source) {
        this.positions.addAll(source.getPositionList());
    }
}
