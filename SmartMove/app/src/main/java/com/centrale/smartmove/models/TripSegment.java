package com.centrale.smartmove.models;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.centrale.smartmove.*;
import com.centrale.smartmove.Savable;

import java.util.LinkedList;


public class TripSegment implements Savable {

    //Attributes--------------------------------------------------------------------------------------
    /**
     * TransportType that defines de transport type used during the TripSegment
     */
    private TransportType transportType;

    /**
     * List of the TimestampedPositions taken during all the TripSegment
     */
    private LinkedList<TimestampedPosition> positions;
    
    /** 
     * Boolean which indicates wether the TripSegment is finished
     */
    private boolean isFinished = false;


    //Getters and Setters---------------------------------------------------------------------------
    /**
     * Getter
     * @return boolean isFinished
     */
    public boolean isFinished() {
        return isFinished;
    }

    /**
     * Getter
     * @return TransportType transportType
     */
    public TransportType getTransportType() {
        return transportType;
    }

    /**
     * Getter
     * @return LinkedList<TimestampedPosition> positions
     */
    public LinkedList<TimestampedPosition> getPositionList() {
        return positions;
    }

    //Constructors----------------------------------------------------------------------------------
    /**
     * Constructor of a TripSegment with the transport type used
     * @param transportTypeUsed mean of transport
     */
    public TripSegment(TransportType transportTypeUsed){
        this.transportType=transportTypeUsed;
    }


    /**
     * Constructor of a TripSegment using all its attributes
     * @param transportTypeUsed the TransportType
     * @param timestampedPositions the LinkedList<TimestampedPosition>
     */
    public TripSegment(TransportType transportTypeUsed, LinkedList<TimestampedPosition> timestampedPositions) {
        this.transportType = transportTypeUsed;
        this.positions = timestampedPositions;
    }


    /**
     * Constructor of a TripSegment using the first position on the LinkedList<TimestampedPosition>
     * @param firstPosition the first TimestampedPosition of the LinkedList<TimestampedPosition>
     */
    public TripSegment(TimestampedPosition firstPosition) {
        this.transportType = TransportType.STATIC;
        this.positions = new LinkedList<>();
        this.positions.add(firstPosition);
    }


    //Methods---------------------------------------------------------------------------------------
    /**
     * Method which calculates the sum of the distance between all the TimestampedPositions in LinkedList<TimestampedPosition>
     * @return a double corresponding to the total distance travelled
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

    /**
     * Method which enables to calculate the CO2 emission of a TripSegment
     * @return a double corresponding to the value of the CO2 emission of the TripSegment
     */
    public double calculateCO2footprint()  {
        float transportTypeCO2Parameter = transportType.getCO2param();
        return transportTypeCO2Parameter*this.calculateTotalDistance();
    }

    /**
     * Sets the right TransportType for the TripSegment
     */
    public void computeTransportType() throws Exception{
        if (this.positions ==null){
            throw new NullPointerException("Le TripSegment n'a pas été initialisé.");}
        if(this.positions.size()>=2){
            int listSize = this.positions.size();
            double lastTwoPointsVelocity;
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

    /**
     * Method which allows to save all the TripSegment of User in a JSON file
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
     * @return a double that is the mean velocity of the user during the TripSegment
     * @throws Exception if the size of the list is not long enough
     */
    public double calculateMeanVelocity() throws Exception {
        int listSize = this.positions.size();
        double velocityMean=0;
        if(listSize>=2){
            int i;
            double sum=0;
            for(i=0;i<listSize-1;i++){
                double velocityBetweenTwoPoints;
                velocityBetweenTwoPoints = positions.get(i+1).calculateVelocityBetweenTwoPoints(positions.get(i));
                if(velocityBetweenTwoPoints>0.55){
                    sum+=velocityBetweenTwoPoints;
                }
            }
            velocityMean=sum/listSize;
            return velocityMean;

        }
        return velocityMean;
    }


    /**
     * Method that gives the size of the LinkedList<TimestampedPosition>
     * @return an integer that is the size of the LinkedList<TimestampedPosition>
     */
    public int getNumberOfPositions() {
        return positions.size();
    }

    /**
     * Method that adds to the LinkedList<TimestampedPosition> a new TimestampedPosition
     * @param newPosition, a new TimestampedPosition
     */
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

    /**
     * Method that merges two LinkedList<TimestampedPosition> of two TripSegments
     * @param source, another TripSegment
     */
    public void mergeWithSegment(TripSegment source) {
        this.positions.addAll(source.getPositionList());
    }

    /**
     * Method that sets the boolean isFinished on true
     */
    public void setFinished() {
        this.isFinished = true;
    }

    //----------------------------------------------------------------------------------------------
}
