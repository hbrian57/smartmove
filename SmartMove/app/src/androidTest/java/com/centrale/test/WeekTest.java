package com.centrale.test;
import static org.junit.Assert.*;

import com.centrale.smartmove.TimestampedPosition;
import com.centrale.smartmove.TransportType;
import com.centrale.smartmove.Trip;
import com.centrale.smartmove.TripSegment;
import com.centrale.smartmove.Week;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;

public class WeekTest {

    @Test
    public void testGetTotalCO2FootprintCaseStatic() throws Exception {
        //Le voyage 1
        LinkedList<TimestampedPosition> bikePositions = new LinkedList<>();
        bikePositions.add(new TimestampedPosition(0, 0, 0));
        bikePositions.add(new TimestampedPosition(0, 0, 0));
        TripSegment tripsegm1 = new TripSegment(TransportType.BIKE, bikePositions);

        //Le voyage 2
        LinkedList<TimestampedPosition> carPositions = new LinkedList<>();
        carPositions.add(new TimestampedPosition(0, 0, 0));
        carPositions.add(new TimestampedPosition(0, 0, 0));
        TripSegment tripsegm2 = new TripSegment(TransportType.BIKE, carPositions);

        //Un trip composé des deux voyages
        ArrayList<TripSegment> tripSegments1 = new ArrayList<>();
        tripSegments1.add(tripsegm1);
        tripSegments1.add(tripsegm2);
        Trip trip1= new Trip(tripSegments1);


        //Le voyage 3
        LinkedList<TimestampedPosition> bikePositions2 = new LinkedList<>();
        bikePositions2.add(new TimestampedPosition(0, 0, 0));
        bikePositions2.add(new TimestampedPosition(0, 0, 0));
        TripSegment tripsegm3 = new TripSegment(TransportType.BIKE, bikePositions2);

        //Le voyage 4
        LinkedList<TimestampedPosition> carPositions2 = new LinkedList<>();
        carPositions.add(new TimestampedPosition(0, 0, 0));
        carPositions.add(new TimestampedPosition(0, 0, 0));
        TripSegment tripsegm4 = new TripSegment(TransportType.BIKE, carPositions);

        //Un trip composé des deux voyages
        ArrayList<TripSegment> tripSegments2 = new ArrayList<>();
        tripSegments1.add(tripsegm3);
        tripSegments1.add(tripsegm4);
        Trip trip2= new Trip(tripSegments2);

        //Création de la week
        ArrayList trips = new ArrayList<Trip>();
        trips.add(trip1);
        trips.add(trip2);
        Week week = new Week(trips);


        // call la methode et vérification du résultat
        double expected = 0;
        double actual = week.getTotalCO2Footprint();
        assertEquals(expected, actual, 2);
    }



}